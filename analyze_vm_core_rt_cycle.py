#!/usr/bin/env python3
"""
Detailed analysis of the circular dependency between org.jnode.vm.core and rt.

This script:
1. Identifies all Java classes in org.jnode.vm.core that import from rt
2. Identifies all Java classes in rt that import from org.jnode.vm.core
3. Sorts classes by the number of circular dependencies (least first)
4. Generates a detailed report with recommendations to break the cycle

Usage:
    python3 analyze_vm_core_rt_cycle.py
"""

import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from collections import defaultdict
from typing import Dict, List, Set, Tuple, Optional


class VMCoreRTCycleAnalyzer:
    def __init__(self, repo_root: str):
        self.repo_root = Path(repo_root)
        self.vm_core_packages = set()
        self.rt_packages = set()
        self.class_dependencies = defaultdict(lambda: {'imports_from_other': [], 'imported_by_other': []})
        
    def load_plugin_exports(self, plugin_id: str) -> Set[str]:
        """Load package exports for a specific plugin."""
        packages = set()
        
        # Find the descriptor file
        for desc_dir in self.repo_root.glob('*/descriptors'):
            if 'all/build' in str(desc_dir):
                continue
            for xml_file in desc_dir.glob('*.xml'):
                try:
                    tree = ET.parse(xml_file)
                    root = tree.getroot()
                    
                    if root.get('id') == plugin_id or (root.tag == 'fragment' and root.get('plugin-id') == plugin_id):
                        # Extract runtime library exports
                        runtime = root.find('runtime')
                        if runtime is not None:
                            for library in runtime.findall('library'):
                                for export in library.findall('export'):
                                    export_name = export.get('name')
                                    if export_name:
                                        if export_name.endswith('.*'):
                                            packages.add(export_name[:-2])
                                        else:
                                            # Specific class export
                                            if '.' in export_name:
                                                packages.add(export_name.rsplit('.', 1)[0])
                except Exception as e:
                    pass
        
        return packages
    
    def find_java_files_for_plugin(self, packages: Set[str]) -> List[Path]:
        """Find all Java files that belong to the given packages."""
        java_files = []
        
        for src_dir in self.repo_root.glob('*/src'):
            if 'all/build' in str(src_dir):
                continue
            
            for java_file in src_dir.rglob('*.java'):
                # Extract package from file
                file_package = self.extract_package_from_file(java_file)
                if file_package:
                    # Check if this file belongs to any of our packages
                    for pkg in packages:
                        if file_package == pkg or file_package.startswith(pkg + '.'):
                            java_files.append(java_file)
                            break
        
        return java_files
    
    def extract_package_from_file(self, java_file: Path) -> Optional[str]:
        """Extract the package declaration from a Java file."""
        try:
            with open(java_file, 'r', encoding='utf-8', errors='ignore') as f:
                for line in f:
                    match = re.match(r'^\s*package\s+([\w.]+)\s*;', line)
                    if match:
                        return match.group(1)
        except Exception:
            pass
        return None
    
    def extract_class_name(self, java_file: Path) -> str:
        """Extract the class name from the file path."""
        return java_file.stem
    
    def extract_imports_from_file(self, java_file: Path) -> List[str]:
        """Extract import statements from a Java file."""
        imports = []
        try:
            with open(java_file, 'r', encoding='utf-8', errors='ignore') as f:
                for line in f:
                    # Look for import statements (including static imports)
                    match = re.match(r'^\s*import\s+(?:static\s+)?([\w.]+)(?:\.\*)?\s*;', line)
                    if match:
                        import_stmt = match.group(1)
                        imports.append(import_stmt)
        except Exception:
            pass
        return imports
    
    def is_import_in_packages(self, import_stmt: str, packages: Set[str]) -> bool:
        """Check if an import belongs to any of the given packages."""
        for pkg in packages:
            if import_stmt == pkg or import_stmt.startswith(pkg + '.'):
                return True
        return False
    
    def analyze_cycle(self):
        """Analyze the circular dependency between org.jnode.vm.core and rt."""
        print("Loading plugin exports...")
        
        # Load packages for both plugins
        self.vm_core_packages = self.load_plugin_exports('org.jnode.vm.core')
        self.rt_packages = self.load_plugin_exports('rt')
        
        # Also check for rt.vm (which is often grouped with rt)
        rt_vm_packages = self.load_plugin_exports('rt.vm')
        self.rt_packages.update(rt_vm_packages)
        
        print(f"org.jnode.vm.core exports {len(self.vm_core_packages)} packages")
        print(f"rt exports {len(self.rt_packages)} packages")
        
        # Find Java files for each plugin
        print("\nFinding Java files...")
        vm_core_files = self.find_java_files_for_plugin(self.vm_core_packages)
        rt_files = self.find_java_files_for_plugin(self.rt_packages)
        
        print(f"org.jnode.vm.core has {len(vm_core_files)} Java files")
        print(f"rt has {len(rt_files)} Java files")
        
        # Analyze imports from vm.core to rt
        print("\nAnalyzing imports from org.jnode.vm.core to rt...")
        vm_core_to_rt = []
        for java_file in vm_core_files:
            imports = self.extract_imports_from_file(java_file)
            rt_imports = [imp for imp in imports if self.is_import_in_packages(imp, self.rt_packages)]
            
            if rt_imports:
                package = self.extract_package_from_file(java_file)
                class_name = self.extract_class_name(java_file)
                fqcn = f"{package}.{class_name}" if package else class_name
                
                vm_core_to_rt.append({
                    'class': fqcn,
                    'file': str(java_file.relative_to(self.repo_root)),
                    'imports': rt_imports,
                    'count': len(rt_imports)
                })
                
                self.class_dependencies[fqcn]['imports_from_other'] = rt_imports
        
        # Analyze imports from rt to vm.core
        print("Analyzing imports from rt to org.jnode.vm.core...")
        rt_to_vm_core = []
        for java_file in rt_files:
            imports = self.extract_imports_from_file(java_file)
            vm_core_imports = [imp for imp in imports if self.is_import_in_packages(imp, self.vm_core_packages)]
            
            if vm_core_imports:
                package = self.extract_package_from_file(java_file)
                class_name = self.extract_class_name(java_file)
                fqcn = f"{package}.{class_name}" if package else class_name
                
                rt_to_vm_core.append({
                    'class': fqcn,
                    'file': str(java_file.relative_to(self.repo_root)),
                    'imports': vm_core_imports,
                    'count': len(vm_core_imports)
                })
                
                self.class_dependencies[fqcn]['imports_from_other'] = vm_core_imports
        
        # Sort by dependency count (least dependencies first)
        vm_core_to_rt.sort(key=lambda x: x['count'])
        rt_to_vm_core.sort(key=lambda x: x['count'])
        
        return vm_core_to_rt, rt_to_vm_core
    
    def generate_report(self, vm_core_to_rt: List[Dict], rt_to_vm_core: List[Dict]):
        """Generate a detailed report."""
        report = []
        
        report.append("# Detailed Analysis: org.jnode.vm.core ↔ rt Circular Dependency")
        report.append("")
        report.append(f"**Date:** {self._get_date()}")
        report.append("")
        
        report.append("## Summary")
        report.append("")
        report.append(f"- **Classes in org.jnode.vm.core that import from rt:** {len(vm_core_to_rt)}")
        report.append(f"- **Classes in rt that import from org.jnode.vm.core:** {len(rt_to_vm_core)}")
        report.append(f"- **Total classes involved in circular dependency:** {len(vm_core_to_rt) + len(rt_to_vm_core)}")
        report.append("")
        
        report.append("## Dependency Details")
        report.append("")
        report.append("### Classes in org.jnode.vm.core importing from rt")
        report.append("")
        report.append("(Sorted by number of dependencies, least dependencies first)")
        report.append("")
        
        for i, entry in enumerate(vm_core_to_rt, 1):
            report.append(f"#### {i}. {entry['class']} ({entry['count']} import{'s' if entry['count'] > 1 else ''})")
            report.append(f"**File:** `{entry['file']}`")
            report.append("")
            report.append("**Imports from rt:**")
            for imp in sorted(entry['imports']):
                report.append(f"- `{imp}`")
            report.append("")
        
        report.append("### Classes in rt importing from org.jnode.vm.core")
        report.append("")
        report.append("(Sorted by number of dependencies, least dependencies first)")
        report.append("")
        
        for i, entry in enumerate(rt_to_vm_core, 1):
            report.append(f"#### {i}. {entry['class']} ({entry['count']} import{'s' if entry['count'] > 1 else ''})")
            report.append(f"**File:** `{entry['file']}`")
            report.append("")
            report.append("**Imports from org.jnode.vm.core:**")
            for imp in sorted(entry['imports']):
                report.append(f"- `{imp}`")
            report.append("")
        
        report.append("## Recommendations to Break the Circular Dependency")
        report.append("")
        report.append("### Strategy 1: Extract Common API Interfaces")
        report.append("")
        report.append("Create a new plugin `org.jnode.vm.api` that contains only interfaces:")
        report.append("")
        report.append("1. **Identify common interfaces** used by both vm.core and rt")
        report.append("2. **Move interfaces** to the new API plugin")
        report.append("3. **Update dependencies:**")
        report.append("   - Both `org.jnode.vm.core` and `rt` depend on `org.jnode.vm.api`")
        report.append("   - Remove direct dependencies between vm.core and rt")
        report.append("")
        
        report.append("### Strategy 2: Move Classes with Fewest Dependencies")
        report.append("")
        report.append("Start with classes that have the least dependencies (listed above):")
        report.append("")
        
        if vm_core_to_rt:
            report.append("**From org.jnode.vm.core to rt (or new plugin):**")
            for entry in vm_core_to_rt[:5]:  # Top 5 candidates
                report.append(f"- `{entry['class']}` ({entry['count']} dependency/dependencies)")
            report.append("")
        
        if rt_to_vm_core:
            report.append("**From rt to org.jnode.vm.core (or new plugin):**")
            for entry in rt_to_vm_core[:5]:  # Top 5 candidates
                report.append(f"- `{entry['class']}` ({entry['count']} dependency/dependencies)")
            report.append("")
        
        report.append("### Strategy 3: Dependency Inversion")
        report.append("")
        report.append("Apply the Dependency Inversion Principle:")
        report.append("")
        report.append("1. **Define interfaces** for high-level dependencies")
        report.append("2. **Inject implementations** at runtime")
        report.append("3. **Use service locators** or dependency injection")
        report.append("")
        
        report.append("### Strategy 4: Consolidate Plugins")
        report.append("")
        report.append("If separation is not critical:")
        report.append("")
        report.append("1. **Merge vm.core and rt** into a single plugin")
        report.append("2. **Document the merger** in plugin documentation")
        report.append("3. **Update all plugin descriptors** that reference these plugins")
        report.append("")
        
        report.append("### Priority Actions")
        report.append("")
        report.append("Based on the analysis, prioritize:")
        report.append("")
        report.append("1. **Quick wins:** Classes with 1-2 dependencies")
        report.append("2. **Medium effort:** Classes with 3-5 dependencies")
        report.append("3. **Major refactoring:** Classes with >5 dependencies")
        report.append("")
        
        report.append("## Next Steps")
        report.append("")
        report.append("1. Review this detailed analysis with the development team")
        report.append("2. Choose a strategy based on project constraints and goals")
        report.append("3. Create a plan for incremental refactoring")
        report.append("4. Test each change thoroughly to ensure system stability")
        report.append("5. Document the architectural decisions")
        report.append("")
        
        return '\n'.join(report)
    
    def _get_date(self) -> str:
        """Get current date."""
        from datetime import datetime
        return datetime.now().strftime('%Y-%m-%d')


def main():
    repo_root = os.getcwd()
    
    analyzer = VMCoreRTCycleAnalyzer(repo_root)
    
    print("=" * 80)
    print("DETAILED ANALYSIS: org.jnode.vm.core ↔ rt CIRCULAR DEPENDENCY")
    print("=" * 80)
    print()
    
    vm_core_to_rt, rt_to_vm_core = analyzer.analyze_cycle()
    
    print("\n" + "=" * 80)
    print("GENERATING REPORT")
    print("=" * 80)
    
    report = analyzer.generate_report(vm_core_to_rt, rt_to_vm_core)
    
    output_file = Path(repo_root) / 'VM_CORE_RT_CYCLE_DETAILED_ANALYSIS.md'
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(report)
    
    print(f"\nReport generated: {output_file}")
    print("\nSummary:")
    print(f"  - Classes in org.jnode.vm.core importing from rt: {len(vm_core_to_rt)}")
    print(f"  - Classes in rt importing from org.jnode.vm.core: {len(rt_to_vm_core)}")
    print(f"  - Total classes involved: {len(vm_core_to_rt) + len(rt_to_vm_core)}")
    
    if vm_core_to_rt:
        print(f"\n  Top 3 classes in vm.core with least rt dependencies:")
        for entry in vm_core_to_rt[:3]:
            print(f"    - {entry['class']} ({entry['count']} dependency/dependencies)")
    
    if rt_to_vm_core:
        print(f"\n  Top 3 classes in rt with least vm.core dependencies:")
        for entry in rt_to_vm_core[:3]:
            print(f"    - {entry['class']} ({entry['count']} dependency/dependencies)")


if __name__ == '__main__':
    main()
