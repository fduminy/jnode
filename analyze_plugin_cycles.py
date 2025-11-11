#!/usr/bin/env python3
"""
Analyze JNode plugin dependencies and detect circular dependencies.

This script:
1. Discovers all plugin descriptor XML files in the repository
2. Parses each descriptor to extract plugin ID and dependencies
3. Builds a dependency graph
4. Detects all circular dependencies using Tarjan's algorithm
5. Sorts cycles by difficulty to fix
"""

import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from collections import defaultdict, deque
from typing import Dict, List, Set, Tuple


class PluginAnalyzer:
    def __init__(self, repo_root: str):
        self.repo_root = Path(repo_root)
        self.plugins: Dict[str, Dict] = {}
        self.dependencies: Dict[str, Set[str]] = defaultdict(set)
        self.reverse_deps: Dict[str, Set[str]] = defaultdict(set)
        
    def discover_descriptors(self) -> List[Path]:
        """Find all plugin descriptor XML files, excluding all/build directory."""
        descriptors = []
        for desc_dir in self.repo_root.glob('*/descriptors'):
            # Skip all/build directory
            if 'all/build' in str(desc_dir):
                continue
            for xml_file in desc_dir.glob('*.xml'):
                descriptors.append(xml_file)
        return sorted(descriptors)
    
    def parse_descriptor(self, xml_path: Path) -> Dict:
        """Parse a plugin descriptor XML file."""
        try:
            tree = ET.parse(xml_path)
            root = tree.getroot()
            
            if root.tag != 'plugin':
                return None
            
            plugin_id = root.get('id')
            if not plugin_id:
                return None
            
            # Extract dependencies from <requires><import plugin="..."/>
            dependencies = []
            requires = root.find('requires')
            if requires is not None:
                for import_elem in requires.findall('import'):
                    dep_plugin = import_elem.get('plugin')
                    if dep_plugin:
                        dependencies.append(dep_plugin)
            
            # Check if it's a system plugin
            system = root.get('system', '').lower() == 'true'
            
            return {
                'id': plugin_id,
                'file': str(xml_path.relative_to(self.repo_root)),
                'dependencies': dependencies,
                'system': system,
                'name': root.findtext('name', ''),
                'version': root.get('version', '')
            }
        except Exception as e:
            print(f"Error parsing {xml_path}: {e}", file=sys.stderr)
            return None
    
    def load_all_plugins(self):
        """Load all plugin descriptors and build the dependency graph."""
        descriptors = self.discover_descriptors()
        print(f"Found {len(descriptors)} plugin descriptors")
        
        for desc_path in descriptors:
            plugin_data = self.parse_descriptor(desc_path)
            if plugin_data:
                plugin_id = plugin_data['id']
                self.plugins[plugin_id] = plugin_data
                
                # Build dependency graph
                for dep in plugin_data['dependencies']:
                    self.dependencies[plugin_id].add(dep)
                    self.reverse_deps[dep].add(plugin_id)
        
        print(f"Loaded {len(self.plugins)} plugins")
    
    def find_strongly_connected_components(self) -> List[List[str]]:
        """
        Find all strongly connected components (SCCs) using Tarjan's algorithm.
        Each SCC with more than one node represents a cycle.
        """
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
            
            # Consider successors
            for successor in self.dependencies.get(node, []):
                if successor not in self.plugins:
                    continue  # Skip missing plugins
                if successor not in index:
                    strongconnect(successor)
                    lowlinks[node] = min(lowlinks[node], lowlinks[successor])
                elif on_stack[successor]:
                    lowlinks[node] = min(lowlinks[node], index[successor])
            
            # If node is a root node, pop the stack and generate an SCC
            if lowlinks[node] == index[node]:
                component = []
                while True:
                    successor = stack.pop()
                    on_stack[successor] = False
                    component.append(successor)
                    if successor == node:
                        break
                sccs.append(component)
        
        for node in self.plugins:
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
            for dep in self.dependencies.get(plugin, []):
                if dep in cycle_set:
                    edges.append((plugin, dep))
        return edges
    
    def classify_cycle_difficulty(self, cycle: List[str]) -> Tuple[str, int, str]:
        """
        Classify a cycle by difficulty to fix.
        Returns: (difficulty_category, priority_number, reasoning)
        
        Categories:
        - EASY: interfaces isolated, few dependencies
        - MEDIUM: requires extracting interfaces
        - HARD: cycles at VM core, complex interdependencies
        - VERY_HARD: fundamental architectural cycles
        """
        cycle_set = set(cycle)
        
        # Check for specific patterns based on TODO_plugin_cycles.md
        vm_core_plugins = {'org.jnode.vm.core', 'org.jnode.vm', 'rt.vm'}
        runtime_plugins = {'org.jnode.runtime.core', 'org.jnode.runtime.core.bootlog'}
        permission_plugins = {'org.jnode.permission'}
        plugin_impl = {'org.jnode.plugin.impl', 'org.jnode.plugin'}
        fs_plugins = {'org.jnode.fs', 'org.jnode.fs.service'}
        driver_plugins = {'org.jnode.driver.block'}
        util_plugins = {'org.jnode.util'}
        classpath_plugins = {'org.classpath.ext.core.vm'}
        resource_plugins = {'org.jnode.runtime.core.resource'}
        
        cycle_str = ' ↔ '.join(sorted(cycle))
        
        # Check if it's a known cycle from TODO_plugin_cycles.md
        if cycle_set == {'org.jnode.permission', 'rt.vm'}:
            return ('EASY', 1, 'Permission utilise probablement des types VM basiques. Solution: créer un module d\'interfaces de sécurité.')
        
        if cycle_set == {'org.jnode.runtime.core', 'org.jnode.runtime.core.bootlog'}:
            return ('EASY', 2, 'Bootlog est une interface simple. Solution: déplacer l\'interface bootlog vers runtime.core ou créer un module api.')
        
        if cycle_set == {'org.jnode.fs', 'org.jnode.fs.service'}:
            return ('MEDIUM', 3, 'org.jnode.fs importe fs.service. Solution: extraire les interfaces de service vers un module séparé.')
        
        if cycle_set == {'org.jnode.driver.block', 'org.jnode.fs'}:
            return ('MEDIUM', 4, 'Similar au cycle driver.block/partitions déjà résolu. Solution: identifier et déplacer les interfaces communes.')
        
        if cycle_set == {'org.jnode.plugin.impl', 'org.jnode.runtime.core'}:
            return ('MEDIUM', 5, 'Implémentation vs runtime. Solution: séparer les interfaces d\'implémentation.')
        
        if cycle_set == {'org.jnode.runtime.core.resource', 'org.jnode.vm.core'}:
            return ('HARD', 6, 'Resources système vs VM core.')
        
        if cycle_set == {'rt.vm', 'org.jnode.util'}:
            return ('HARD', 7, 'Runtime VM de base utilise des utilitaires. Solution: extraire interfaces utilitaires minimales.')
        
        if cycle_set == {'org.jnode.vm.core', 'org.jnode.vm'}:
            return ('HARD', 8, 'Séparation VM/VM.core peu claire.')
        
        if cycle_set == {'org.jnode.vm', 'org.jnode.runtime.core'}:
            return ('HARD', 9, 'runtime.core importe vm, interdépendance bidirectionnelle profonde.')
        
        if cycle_set == {'org.jnode.plugin', 'org.jnode.vm.core'}:
            return ('VERY_HARD', 10, 'org.jnode.plugin importe vm.core, cycle au cœur du bootstrap. Refonte architecturale majeure requise.')
        
        if cycle_set == {'rt.vm', 'org.classpath.ext.core.vm'}:
            return ('VERY_HARD', 11, 'Cycle entre runtime et classpath VM, touches aux fondations du système.')
        
        if cycle_set == {'org.jnode.runtime.core', 'org.jnode.vm.core'}:
            return ('VERY_HARD', 12, 'runtime.core importe vm, le plus complexe car au cœur du bootstrap système.')
        
        # For other cycles, classify based on characteristics
        
        # Count VM/runtime involvement
        vm_count = len(cycle_set & vm_core_plugins)
        runtime_count = len(cycle_set & runtime_plugins)
        
        # Very hard: involves multiple VM core or runtime core components
        if vm_count >= 2 or (vm_count >= 1 and runtime_count >= 1):
            return ('VERY_HARD', 100, f'Cycle implique {vm_count} composants VM et {runtime_count} composants runtime - refonte architecturale requise.')
        
        # Hard: involves VM or runtime core
        if vm_count >= 1 or runtime_count >= 1:
            return ('HARD', 50, 'Cycle implique des composants VM ou runtime core - nécessite une séparation d\'interfaces.')
        
        # Medium: involves multiple subsystems
        if len(cycle) > 2:
            return ('MEDIUM', 30, f'Cycle implique {len(cycle)} plugins - nécessite extraction d\'interfaces.')
        
        # Easy: simple two-way cycle
        return ('EASY', 20, 'Cycle simple entre deux plugins - extraction d\'interfaces suffisante.')
    
    def generate_report(self) -> str:
        """Generate a comprehensive report of all circular dependencies."""
        cycles = self.find_cycles()
        
        report = []
        report.append("=" * 80)
        report.append("ANALYSE DES DÉPENDANCES CIRCULAIRES ENTRE LES PLUGINS JNODE")
        report.append("=" * 80)
        report.append("")
        report.append(f"Nombre total de plugins analysés: {len(self.plugins)}")
        report.append(f"Nombre total de cycles détectés: {len(cycles)}")
        report.append("")
        
        if not cycles:
            report.append("✅ RÉSULTAT: Aucun cycle de dépendance détecté dans les imports de plugins!")
            report.append("")
            report.append("Cela signifie que les dépendances explicites déclarées dans les sections")
            report.append("<requires><import plugin=\"...\"/> des descripteurs XML ne forment pas de cycles.")
            report.append("")
            report.append("Note: Des dépendances circulaires pourraient encore exister au niveau du")
            report.append("code Java lui-même (imports de classes) même si les descripteurs de plugins")
            report.append("ne montrent pas de dépendances circulaires explicites.")
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
        
        # Generate report
        report = []
        report.append("=" * 80)
        report.append("ANALYSE DES DÉPENDANCES CIRCULAIRES ENTRE LES PLUGINS JNODE")
        report.append("=" * 80)
        report.append("")
        report.append(f"Total de cycles détectés: {len(cycles)}")
        report.append("")
        
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
                report.append(f"#{item['priority']}. {' ↔ '.join(sorted(cycle))}")
                report.append("-" * 80)
                report.append(f"Difficulté: {difficulty}")
                report.append(f"Raisonnement: {item['reasoning']}")
                report.append("")
                report.append("Plugins impliqués:")
                for plugin_id in sorted(cycle):
                    plugin_data = self.plugins.get(plugin_id, {})
                    report.append(f"  - {plugin_id}")
                    report.append(f"    Fichier: {plugin_data.get('file', 'N/A')}")
                    if plugin_data.get('name'):
                        report.append(f"    Nom: {plugin_data['name']}")
                
                report.append("")
                report.append("Dépendances circulaires:")
                for src, dst in sorted(item['edges']):
                    report.append(f"  {src} → {dst}")
                report.append("")
        
        return "\n".join(report)


def main():
    repo_root = os.path.dirname(os.path.abspath(__file__))
    
    print(f"Analyzing JNode plugins in: {repo_root}")
    print()
    
    analyzer = PluginAnalyzer(repo_root)
    analyzer.load_all_plugins()
    
    print()
    report = analyzer.generate_report()
    print(report)
    
    # Also save to file
    output_file = Path(repo_root) / "CIRCULAR_DEPENDENCIES_REPORT.md"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(report)
    
    print()
    print(f"Rapport sauvegardé dans: {output_file}")


if __name__ == '__main__':
    main()
