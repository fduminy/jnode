#!/usr/bin/env python3
"""
Analyze JNode plugin dependencies at the Java source code level.

This script:
1. Maps Java packages/classes to plugins based on <export> declarations
2. Analyzes Java import statements to find package dependencies
3. Maps package dependencies back to plugin-level dependencies
4. Detects circular dependencies caused by Java code imports
5. Sorts cycles by difficulty to fix

Usage:
    python3 analyze_source_cycles.py

The script will:
- Scan all plugin descriptors to build package → plugin mapping
- Analyze Java source files to extract import statements
- Build a plugin dependency graph based on actual code usage
- Detect circular dependencies at the plugin level
- Generate SOURCE_LEVEL_CYCLES_REPORT.md with findings

This complements analyze_plugin_cycles.py by analyzing actual code dependencies
rather than just plugin descriptor imports.
"""

import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from collections import defaultdict
from typing import Dict, List, Set, Tuple, Optional


class SourceLevelAnalyzer:
    def __init__(self, repo_root: str):
        self.repo_root = Path(repo_root)
        self.plugins: Dict[str, Dict] = {}
        self.package_to_plugin: Dict[str, str] = {}  # package → plugin_id
        self.plugin_dependencies: Dict[str, Set[str]] = defaultdict(set)  # Based on source code
        self.java_files_by_plugin: Dict[str, List[Path]] = defaultdict(list)
        
    def discover_descriptors(self) -> List[Path]:
        """Find all plugin descriptor XML files, excluding all/build directory."""
        descriptors = []
        for desc_dir in self.repo_root.glob('*/descriptors'):
            if 'all/build' in str(desc_dir):
                continue
            for xml_file in desc_dir.glob('*.xml'):
                descriptors.append(xml_file)
        return sorted(descriptors)
    
    def parse_descriptor(self, xml_path: Path) -> Optional[Dict]:
        """Parse a plugin descriptor XML file."""
        try:
            tree = ET.parse(xml_path)
            root = tree.getroot()
            
            # Handle both <plugin> and <fragment> elements
            if root.tag not in ['plugin', 'fragment']:
                return None
            
            plugin_id = root.get('id')
            if not plugin_id:
                return None
            
            # Extract runtime library exports
            exports = []
            runtime = root.find('runtime')
            if runtime is not None:
                for library in runtime.findall('library'):
                    for export in library.findall('export'):
                        export_name = export.get('name')
                        if export_name:
                            exports.append(export_name)
            
            # Get subproject from file path
            subproject = xml_path.parent.parent.name
            
            return {
                'id': plugin_id,
                'file': str(xml_path.relative_to(self.repo_root)),
                'exports': exports,
                'subproject': subproject,
                'is_fragment': root.tag == 'fragment'
            }
        except Exception as e:
            print(f"Error parsing {xml_path}: {e}", file=sys.stderr)
            return None
    
    def load_plugins(self):
        """Load all plugin descriptors and build package → plugin mapping."""
        descriptors = self.discover_descriptors()
        print(f"Found {len(descriptors)} plugin descriptors")
        
        for desc_path in descriptors:
            plugin_data = self.parse_descriptor(desc_path)
            if plugin_data:
                plugin_id = plugin_data['id']
                self.plugins[plugin_id] = plugin_data
                
                # Build package → plugin mapping from exports
                for export in plugin_data['exports']:
                    # Handle export patterns like "org.jnode.driver.*"
                    if export.endswith('.*'):
                        package = export[:-2]  # Remove .*
                    else:
                        # Specific class export - treat as package
                        if '.' in export:
                            package = export.rsplit('.', 1)[0]
                        else:
                            package = export
                    
                    # Map this package and all subpackages to this plugin
                    self.package_to_plugin[package] = plugin_id
        
        print(f"Loaded {len(self.plugins)} plugins")
        print(f"Mapped {len(self.package_to_plugin)} packages to plugins")
    
    def find_plugin_for_package(self, package: str) -> Optional[str]:
        """Find which plugin owns a given package."""
        # Direct match
        if package in self.package_to_plugin:
            return self.package_to_plugin[package]
        
        # Check parent packages (e.g., org.jnode.driver.console → org.jnode.driver)
        parts = package.split('.')
        for i in range(len(parts), 0, -1):
            parent_pkg = '.'.join(parts[:i])
            if parent_pkg in self.package_to_plugin:
                return self.package_to_plugin[parent_pkg]
        
        return None
    
    def discover_java_files(self) -> List[Path]:
        """Find all Java source files, excluding all/build."""
        java_files = []
        for src_dir in self.repo_root.glob('*/src'):
            if 'all/build' in str(src_dir):
                continue
            for java_file in src_dir.rglob('*.java'):
                java_files.append(java_file)
        return java_files
    
    def extract_package_from_file(self, java_file: Path) -> Optional[str]:
        """Extract the package declaration from a Java file."""
        try:
            with open(java_file, 'r', encoding='utf-8', errors='ignore') as f:
                for line in f:
                    # Look for package declaration
                    match = re.match(r'^\s*package\s+([\w.]+)\s*;', line)
                    if match:
                        return match.group(1)
        except Exception as e:
            pass
        return None
    
    def extract_imports_from_file(self, java_file: Path) -> List[str]:
        """Extract import statements from a Java file."""
        imports = []
        try:
            with open(java_file, 'r', encoding='utf-8', errors='ignore') as f:
                for line in f:
                    # Look for import statements (skip static imports for simplicity)
                    match = re.match(r'^\s*import\s+(?!static\s+)([\w.]+(?:\.\*)?);\s*$', line)
                    if match:
                        import_stmt = match.group(1)
                        # Convert import to package
                        if import_stmt.endswith('.*'):
                            package = import_stmt[:-2]
                        else:
                            # Specific class import - extract package
                            if '.' in import_stmt:
                                package = import_stmt.rsplit('.', 1)[0]
                            else:
                                package = import_stmt
                        imports.append(package)
        except Exception as e:
            pass
        return imports
    
    def analyze_source_dependencies(self):
        """Analyze Java source files to build plugin dependency graph."""
        java_files = self.discover_java_files()
        print(f"Analyzing {len(java_files)} Java files...")
        
        analyzed = 0
        skipped = 0
        
        for java_file in java_files:
            # Get the package of this file
            file_package = self.extract_package_from_file(java_file)
            if not file_package:
                skipped += 1
                continue
            
            # Find which plugin owns this file
            source_plugin = self.find_plugin_for_package(file_package)
            if not source_plugin:
                skipped += 1
                continue
            
            # Track which Java files belong to which plugin
            self.java_files_by_plugin[source_plugin].append(java_file)
            
            # Extract imports
            imports = self.extract_imports_from_file(java_file)
            
            # Map each import to a plugin
            for import_package in imports:
                # Skip java.*, javax.*, etc.
                if import_package.startswith('java.') or import_package.startswith('javax.'):
                    continue
                
                target_plugin = self.find_plugin_for_package(import_package)
                if target_plugin and target_plugin != source_plugin:
                    # Found a dependency: source_plugin → target_plugin
                    self.plugin_dependencies[source_plugin].add(target_plugin)
            
            analyzed += 1
            if analyzed % 500 == 0:
                print(f"  Analyzed {analyzed}/{len(java_files)} files...")
        
        print(f"Successfully analyzed {analyzed} files ({skipped} skipped)")
        print(f"Found dependencies for {len(self.plugin_dependencies)} plugins")
    
    def find_strongly_connected_components(self) -> List[List[str]]:
        """Find all strongly connected components (cycles) using Tarjan's algorithm."""
        index_counter = [0]
        stack = []
        lowlinks = {}
        index = {}
        on_stack = defaultdict(bool)
        sccs = []
        
        def strongconnect(node):
            index[node] = index_counter[0]
            lowlinks[node] = index_counter[0]
            index_counter[0] += 1
            stack.append(node)
            on_stack[node] = True
            
            for successor in self.plugin_dependencies.get(node, []):
                if successor not in index:
                    strongconnect(successor)
                    lowlinks[node] = min(lowlinks[node], lowlinks[successor])
                elif on_stack[successor]:
                    lowlinks[node] = min(lowlinks[node], index[successor])
            
            if lowlinks[node] == index[node]:
                component = []
                while True:
                    successor = stack.pop()
                    on_stack[successor] = False
                    component.append(successor)
                    if successor == node:
                        break
                sccs.append(component)
        
        for node in self.plugin_dependencies:
            if node not in index:
                strongconnect(node)
        
        return sccs
    
    def find_cycles(self) -> List[List[str]]:
        """Find all cycles (SCCs with more than one node)."""
        sccs = self.find_strongly_connected_components()
        cycles = [scc for scc in sccs if len(scc) > 1]
        return cycles
    
    def get_cycle_edges(self, cycle: List[str]) -> List[Tuple[str, str]]:
        """Get all edges within a cycle."""
        cycle_set = set(cycle)
        edges = []
        for plugin in cycle:
            for dep in self.plugin_dependencies.get(plugin, []):
                if dep in cycle_set:
                    edges.append((plugin, dep))
        return edges
    
    def classify_cycle_difficulty(self, cycle: List[str]) -> Tuple[str, int, str]:
        """
        Classify a cycle by difficulty to fix.
        Returns: (difficulty_category, priority_number, reasoning)
        """
        cycle_set = set(cycle)
        
        # Check for specific patterns
        vm_core_plugins = {'org.jnode.vm.core', 'org.jnode.vm', 'rt.vm', 'rt'}
        runtime_plugins = {'org.jnode.runtime.core', 'org.jnode.runtime.core.bootlog'}
        plugin_impl = {'org.jnode.plugin.impl', 'org.jnode.plugin'}
        
        # Count VM/runtime involvement
        vm_count = len(cycle_set & vm_core_plugins)
        runtime_count = len(cycle_set & runtime_plugins)
        plugin_count = len(cycle_set & plugin_impl)
        
        cycle_str = ' ↔ '.join(sorted(cycle))
        
        # Very hard: involves multiple VM core or runtime core components
        if vm_count >= 2 or (vm_count >= 1 and runtime_count >= 1):
            return ('VERY_HARD', 100 + len(cycle), 
                    f'Cycle involves {vm_count} VM components and {runtime_count} runtime components - requires architectural refactoring of bootstrap system.')
        
        # Very hard: involves plugin system and VM/runtime
        if plugin_count >= 1 and (vm_count >= 1 or runtime_count >= 1):
            return ('VERY_HARD', 90 + len(cycle),
                    f'Cycle involves plugin system with VM/runtime - requires refactoring of plugin initialization.')
        
        # Hard: involves VM or runtime core
        if vm_count >= 1 or runtime_count >= 1:
            return ('HARD', 50 + len(cycle), 
                    f'Cycle involves VM or runtime core ({vm_count} VM, {runtime_count} runtime) - requires interface separation.')
        
        # Medium: involves multiple subsystems (more than 2 plugins)
        if len(cycle) > 2:
            return ('MEDIUM', 30 + len(cycle), 
                    f'Cycle involves {len(cycle)} plugins across multiple subsystems - requires extracting common interfaces.')
        
        # Easy: simple two-way cycle
        return ('EASY', 20, 
                f'Simple cycle between two plugins - can be resolved by extracting interfaces to a separate module.')
    
    def generate_report(self) -> str:
        """Generate a comprehensive report of all circular dependencies."""
        cycles = self.find_cycles()
        
        report = []
        report.append("=" * 80)
        report.append("ANALYSE DES DÉPENDANCES CIRCULAIRES AU NIVEAU DU CODE SOURCE")
        report.append("=" * 80)
        report.append("")
        report.append(f"Nombre total de plugins analysés: {len(self.plugins)}")
        report.append(f"Nombre de plugins avec des fichiers Java: {len(self.java_files_by_plugin)}")
        report.append(f"Nombre total de cycles détectés: {len(cycles)}")
        report.append("")
        
        if not cycles:
            report.append("✅ RÉSULTAT: Aucun cycle de dépendance détecté au niveau du code source!")
            report.append("")
            report.append("Les dépendances Java (imports) entre les packages de différents plugins")
            report.append("ne créent pas de dépendances circulaires au niveau des plugins.")
            return "\n".join(report)
        
        # Classify and sort cycles
        classified_cycles = []
        for cycle in cycles:
            difficulty, priority, reasoning = self.classify_cycle_difficulty(cycle)
            classified_cycles.append({
                'cycle': cycle,
                'difficulty': difficulty,
                'priority': priority,
                'reasoning': reasoning,
                'edges': self.get_cycle_edges(cycle)
            })
        
        # Sort by priority (lower number = easier to fix)
        classified_cycles.sort(key=lambda x: x['priority'])
        
        # Group by difficulty
        by_difficulty = defaultdict(list)
        for item in classified_cycles:
            by_difficulty[item['difficulty']].append(item)
        
        for difficulty in ['EASY', 'MEDIUM', 'HARD', 'VERY_HARD']:
            if difficulty not in by_difficulty:
                continue
            
            report.append("")
            report.append("=" * 80)
            report.append(f"DIFFICULTÉ: {difficulty}")
            report.append("=" * 80)
            
            for item in by_difficulty[difficulty]:
                cycle = item['cycle']
                report.append("")
                report.append(f"Cycle #{item['priority']}: {' ↔ '.join(sorted(cycle))}")
                report.append("-" * 80)
                report.append(f"Difficulté: {difficulty}")
                report.append(f"Raisonnement: {item['reasoning']}")
                report.append("")
                report.append("Plugins impliqués:")
                for plugin_id in sorted(cycle):
                    plugin_data = self.plugins.get(plugin_id, {})
                    report.append(f"  - {plugin_id}")
                    report.append(f"    Fichier: {plugin_data.get('file', 'N/A')}")
                    report.append(f"    Sous-projet: {plugin_data.get('subproject', 'N/A')}")
                    num_files = len(self.java_files_by_plugin.get(plugin_id, []))
                    report.append(f"    Fichiers Java: {num_files}")
                
                report.append("")
                report.append("Dépendances circulaires (au niveau du code source):")
                for src, dst in sorted(item['edges']):
                    report.append(f"  {src} → {dst}")
                report.append("")
        
        # Add summary statistics
        report.append("")
        report.append("=" * 80)
        report.append("STATISTIQUES")
        report.append("=" * 80)
        report.append("")
        report.append(f"Total de cycles: {len(cycles)}")
        for difficulty in ['EASY', 'MEDIUM', 'HARD', 'VERY_HARD']:
            count = len(by_difficulty.get(difficulty, []))
            if count > 0:
                report.append(f"  {difficulty}: {count} cycle(s)")
        
        return "\n".join(report)


def main():
    repo_root = os.path.dirname(os.path.abspath(__file__))
    
    print(f"Analyzing JNode source code dependencies in: {repo_root}")
    print()
    
    analyzer = SourceLevelAnalyzer(repo_root)
    
    print("Step 1: Loading plugin descriptors...")
    analyzer.load_plugins()
    
    print()
    print("Step 2: Analyzing Java source files...")
    analyzer.analyze_source_dependencies()
    
    print()
    print("Step 3: Detecting circular dependencies...")
    report = analyzer.generate_report()
    print(report)
    
    # Also save to file
    output_file = Path(repo_root) / "SOURCE_LEVEL_CYCLES_REPORT.md"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(report)
    
    print()
    print(f"Rapport sauvegardé dans: {output_file}")


if __name__ == '__main__':
    main()
