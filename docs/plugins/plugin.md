plugin
======

Target audience: automated tools and AI agents that consume and analyze JNode plugin descriptor files.

Purpose
-------
This document explains where plugin descriptor files are typically located in the repository, which XML elements and attributes are important to extract, and gives practical guidance and pseudocode for robust parsing. It is written for programs (including AIs) that must discover, validate and reason about plugins in the JNode source tree.

Where plugin descriptors live
----------------------------
- Each sub-project commonly contains a `descriptors/` directory that holds plugin descriptor XML files (e.g. `<subprj>/descriptors/`).
- There may also be aggregated descriptor directories such as `all/descriptors` depending on the repository layout. Tools should search the workspace for `**/descriptors/*.xml` when discovering plugin descriptors.

Java source layout
------------------
- Java source files for each sub-project follow the standard Java directory layout under a `src` directory. The canonical pattern is:

  `<subproject>/src/<intermediate directories>/<java package as path>/.../*.java`

  - `<subproject>` is one of: `cli`, `core`, `distr`, `fs`, `gui`, `net`, `shell`, `sound`, `textui`.
  - The `<intermediate directories>` part may contain build-related source tree levels (for example `main/java` in some layouts) — tools should be tolerant and search recursively under `**/<subproject>/src/**`.
  - The Java package name is mapped to a relative path by replacing `.` with `/`. For example the Java package `org.jnode.driver.console` corresponds to the path `org/jnode/driver/console` under `src`.

Examples:

  - API interfaces for console drivers might live under: `core/src/org/jnode/driver/console/api/SomeInterface.java`.
  - An implementation class could be at: `gui/src/org/jnode/driver/textscreen/fb/FbTextScreenPlugin.java`.

What agents should do:

  - To locate Java sources for a plugin id or package, search for files under `**/<subproject>/src/**/<package-with-slashes>/**/*.java`.
  - Prefer source files found under `*/src/` over any generated artefacts (see the rule about ignoring `all/build/`).
  - Do not move or modify Java source files. If a refactor is needed (for instance extracting an API package), create a migration plan and update build configuration (Ant/Maven) rather than directly editing many files in the repository.

Quick pseudocode to map package -> source files:

  def find_java_sources(workspace_root, subproject, package_name):
      pkg_path = package_name.replace('.', '/')
      pattern = f"**/{subproject}/src/**/{pkg_path}/**/*.java"
      return find_files(pattern)

Metadata to include in outputs:

  - source_paths: list of file paths (or directories) where relevant Java sources for the plugin are found (optional but recommended).

Implementation hints:

  - Search recursively under `**/<subproject>/src/**` rather than relying on a single fixed layout (handles `src/main/java` or `src/` variants).
  - Record provenance: for each discovered source file include its path and the matched package name.
  - Respect `.gitignore` and repository conventions; do not follow symlinks that point outside the workspace.

- Plugin files convention (added):
  - A plugin is described by an XML file located under a directory of the form `<subproject>/descriptors`, where `<subproject>` is one of: `cli`, `core`, `distr`, `fs`, `gui`, `net`, `shell`, `sound`, `textui`.
  - The plugin descriptor XML has the root element `plugin` and the root element must have an `id` attribute that provides the plugin identifier. Example:

```xml
<plugin id="com.sun.tools.javac">
  ...
</plugin>
```

  - By convention the filename for the descriptor is the plugin id with a `.xml` suffix. For the example above the filename would be `com.sun.tools.javac.xml`.

What to extract
----------------
For each plugin descriptor XML file, the following fields are typically useful to extract and report:
- filename: path to the XML file
- plugin_id: the plugin identifier (often an attribute such as `id` on the top-level element or a child element)
- version: if present
- implementation class(es): fully-qualified class name(s) associated with the plugin
- manifest attributes: any `<manifest>` / `<attribute key=... value=.../>` entries (including `Main-Class` etc.)
- dependencies: declared dependencies on other plugins or components
- human-friendly metadata: name, description, author, license when present
- validation_warnings: a list of issues found parsing or validating the descriptor
- system: boolean flag (true if the plugin declares `system="true"`) indicating the plugin's classes are globally visible and do not require explicit imports from other plugins.
- runtime_libraries: list of libraries referenced by this plugin via the `<runtime>` section. Each library has a `name` (e.g. `jnode-cli.jar`) and an optional ordered list of `export` filters (strings) that act like Java import patterns (e.g. `org.jnode.command.common.*`).

Parsing recommendations
-----------------------
- Use a proper XML parser that supports namespaces, encoding, and entity handling (e.g., lxml or ElementTree in Python, javax.xml in Java).
- Do not rely on fragile string matching. Read the XML DOM and extract attributes/elements explicitly.
- Be robust to optional elements: many fields may be missing; treat missing optional fields as empty/null and record warnings when fields expected by a policy are absent.
- Handle encoding and whitespace: trim values where appropriate but preserve case for identifiers and class names.
- Record provenance: include the filename and the XML node path (or line number) where each extracted value came from to help debugging.

+System plugins
+--------------
+- If the root `<plugin>` element contains the attribute `system="true"`, then the plugin is considered a system plugin. Classes provided by system plugins are visible to all other plugins at runtime without those plugins needing to declare an explicit dependency/import on the system plugin.
+  - Example: `<plugin id="rt" system="true">` declares the `rt` plugin as system-level; other plugins can reference its classes without an `<import plugin="rt"/>` or equivalent.
+  - Tools should extract the `system` attribute (boolean) and expose it in the plugin record (e.g. `record['system'] = True`).
+  - When validating dependencies, do not treat missing imports for system plugins as errors; however, you may still report the implicit dependency in analyses that build a full dependency graph.
+
+Practical notes:
+  - Normalize the attribute value to a boolean: treat `"true"` (case-insensitive) as True; any other value or absence means False.
+  - Even if a plugin is marked `system="true"`, you may still find explicit `requires/import` entries referencing it; treat that as redundant but harmless and optionally warn about redundancy.

Example descriptor fragment
---------------------------
```xml
<plugin id="org.example.foo" version="1.2">
  <name>Example Plugin</name>
  <description>Provides foo functionality</description>
  <implementation class="org.example.foo.FooPlugin"/>
  <manifest>
    <attribute key="Main-Class" value="org.example.foo.Main"/>
  </manifest>
  <requires>
    <import plugin="org.example.base"/>
  </requires>
</plugin>
```

- In this example extract: `plugin_id = "org.example.foo"`, `version = "1.2"`, `implementation class = "org.example.foo.FooPlugin"`, `Main-Class = "org.example.foo.Main"`, and dependencies `org.example.base`.

Pseudocode (robust extraction)
------------------------------
- files = find_files('**/descriptors/*.xml')
- for f in files:
    xml = parse_xml(f)
    root = xml.getroot()
    record = {filename: f, validation_warnings: []}
    # plugin id
    plugin_id = root.get('id') or root.findtext('id')
    if not plugin_id:
        record['validation_warnings'].append('missing plugin id')
    record['plugin_id'] = plugin_id
+    # system flag
+    system_flag = False
+    sys_attr = root.get('system')
+    if sys_attr and str(sys_attr).lower() == 'true':
+        system_flag = True
+    record['system'] = system_flag
    # version
    record['version'] = root.get('version') or root.findtext('version')
    # implementation classes
    impls = []
    for impl in root.findall('.//implementation'):
        cls = impl.get('class')
        if cls:
            impls.append(cls)
    record['implementation_classes'] = impls
    # manifest attributes
    manifest_attrs = {}
    for attr in root.findall('.//manifest/attribute'):
        k = attr.get('key')
        v = attr.get('value')
        if k:
            manifest_attrs.setdefault(k, []).append(v)
    record['manifest'] = manifest_attrs
    # runtime libraries and export filters
    # pattern: <runtime><library name="jnode-<subproject>.jar"> <export name="org.example.*"/>...</library></runtime>
    runtime_libs = []
    for lib in root.findall('.//runtime/library'):
        lib_name = lib.get('name')
        exports = []
        for exp in lib.findall('.//export'):
            en = exp.get('name')
            if en:
                exports.append(en)
        runtime_libs.append({'name': lib_name, 'exports': exports})
    record['runtime_libraries'] = runtime_libs
    # dependencies (canonical JNode pattern)
    # JNode uses <requires><import plugin="..."/> to declare plugin dependencies.
    deps = []
    for imp in root.findall('.//requires//import'):
        pid = imp.get('plugin') or imp.get('id') or imp.get('name')
        if pid:
            deps.append(pid)
    # deduplicate while preserving order
    seen = set()
    deps_unique = []
    for d in deps:
        if d not in seen:
            seen.add(d)
            deps_unique.append(d)
    record['dependencies'] = deps_unique
    # other metadata
    record['name'] = root.findtext('name')
    record['description'] = root.findtext('description')
    # output or store record

-Requires / import pattern
--------------------------
-- Some plugin descriptors use a `<requires>` element that groups one or more `<import>` child elements, each specifying a dependent plugin via the `plugin` attribute. Example:
-
-```xml
-<requires>
-  <import plugin="org.jnode.driver.console.core"/>
-  <import plugin="org.jnode.some.other"/>
-</requires>
-
-  - Tools should treat each `<import plugin="..."/>` as a dependency on the named plugin id.
-  - Resolve these plugin ids the same way you do for `<depends><plugin id="..."/>` entries and include them in `record['dependencies']`.
-  - Validate presence (report missing descriptors) and detect cycles at the tool-level when analyzing full dependency graphs.
+Requires / import pattern
+-------------------------
+- JNode declares plugin dependencies using a `<requires>` element that contains one or more `<import>` child elements, each specifying a dependent plugin via the `plugin` attribute. This is the canonical and preferred pattern. Example:
+
+```xml
+<requires>
+  <import plugin="org.jnode.driver.console.core"/>
+  <import plugin="org.jnode.some.other"/>
+</requires>
+
+  - Tools should treat each `<import plugin="..."/>` as a dependency on the named plugin id and include them in `record['dependencies']`.
+  - Validate presence (report missing descriptors) and detect cycles at the tool-level when analyzing the full dependency graph.
+  - If you encounter historical descriptors using non-canonical patterns, treat them as legacy and migrate to `<requires>/<import>` rather than supporting both patterns in new tooling.

Contract (for tools / AI agents)
--------------------------------
- Inputs: workspace root.
- Outputs: list of plugin descriptor records with fields {
    filename: string,
    plugin_id: string|null,
    version: string|null,
+    runtime_libraries: [{name: string, exports: [string]}],
    system: boolean,
    implementation_classes: [string],
    manifest: { key: [values] },
    dependencies: [string],
    name: string|null,
    description: string|null,
    validation_warnings: [string]
  }
- Error modes: unreadable file, invalid XML, missing required fields, inconsistent data.
- Success criteria: all descriptor files are parsed; required identifiers are extracted or reported as missing; manifest attributes (like Main-Class) are captured; dependencies are listed.

Linking plugin descriptors with plugin-lists
-------------------------------------------
- Plugin-list files (see `docs/plugins/plugin-list.md`) reference plugins by id using entries such as `<plugin id="org.example.foo"/>`.
- Tools should cross-reference plugin-list membership with discovered plugin descriptors using `plugin_id` to ensure plugin ids referenced by lists exist and to surface missing plugins as validation warnings.

+Library exports and class ownership rules
+-----------------------------------------
+These rules are authoritative guidance for tools and AI agents that analyze plugin descriptors and Java sources. They enforce that runtime "exports" declared by plugin descriptors correspond to real Java classes in the source tree and that no Java class is owned (exported) by more than one plugin.
+
+Requirements (machine-checkable)
+- A `library` element (inside a plugin's `<runtime>`) MUST contain at least one `<export>` child. A library with zero exports is considered invalid for analysis purposes.
+- Each `<export name="..."/>` entry is a filter that must match at least one Java class in the repository (applied to the appropriate subproject source tree). If an export filter matches zero Java source files, report a validation error.
+- No Java class file may be matched by export filters from two different plugins. In other words, the sets of Java classes included by export filters across all plugins must be pairwise disjoint; any overlap is a violation that must be reported.
+
+How export filters are interpreted
+- Export filter syntax follows Java import/package-like patterns used elsewhere in the project (examples: `org.jnode.command.common.*`, `org.jnode.driver.console.MyClass`). Translating to file paths:
+  - Replace `.` with `/` to form a package path fragment.
+  - A trailing `.*` means "all classes in this package and subpackages". It maps to a glob like `**/<package-path>/**/*.java` under a `src` tree.
+  - A filter that ends with a concrete class name (no `*`) targets a specific Java source file (e.g. `org.jnode.Foo` → `**/org/jnode/Foo.java`).
+
+Verification algorithm (detailed pseudocode)
+-----------------------------------------
+1) Discovery (exclude generated artifacts)
+   - descriptors = find_files('**/descriptors/*.xml') EXCLUDING `all/build/**` (see exclusion policy).
+   - java_files = find_files('**/*/src/**/*.java') EXCLUDING `all/build/**`.
+
+2) Index Java files by package/class
+   - For each path in java_files:
+       - read the file and try to extract the package declaration `package a.b.c;` (fallback: derive package from path relative to the nearest `src` root).
+       - compute the fully qualified class name (FQCN) = package + `.` + filename_without_extension.
+       - record mapping fqcn -> filepath.
+
+3) For each plugin descriptor `d`:
+   - for each `<runtime><library name="L">` in `d`:
+       - exports = list of `<export name="filter"/>` children.
+       - if exports is empty: report error `library L in plugin <id> has no exports`.
+       - for each export filter `f` in exports:
+           - if f ends with `.*`:
+               pkg = f.rstrip('.*')
+               pkg_path = pkg.replace('.', '/')
+               matches = [p for fqcn,p in fqcn_index.items() if fqcn.startswith(pkg + '.') or fqcn == pkg]
+               # equivalently use path glob: `**/<pkg_path>/**/*.java`
+           - else (no trailing *):
+               # exact class
+               if f in fqcn_index: matches = [fqcn_index[f]] else matches = []
+           - if matches is empty: report error `export f in plugin <id> matches no Java classes`.
+           - record mapping: for each matched class file path c => owners[c].add(plugin_id)
+
+4) Global ownership validation
+   - For each class file `c` in owners:
+       - if len(owners[c]) > 1: report error `class c matched by exports of multiple plugins: owner-list`.
+
+Examples and edge-cases
+- If a `<library>` name clearly indicates the subproject (for example `jnode-cli.jar` or `jnode-core.jar`), you may restrict the search to that subproject's `src` tree to avoid accidental matches across unrelated modules. Otherwise search all `src` trees.
+- If export filters use patterns more advanced than `*` (not common), treat them conservatively and fall back to scanning all Java files and testing if their FQCN matches the filter semantics.
+- If you encounter generated Java sources (under `build` directories), ignore them — only consider sources under `*/src/**`.
+
+Implementation hints for agents
+- Performance: build an index of FQCN → path once and reuse it for all plugin descriptors rather than re-scanning files per descriptor.
+- Robustness: prefer parsing the `package` declaration in the Java file to determine FQCN; if absent, infer from the file path relative to the `src` root.
+- Tolerance: treat whitespace, comments or line-wrapped package declarations carefully (use a simple regex to extract `^\s*package\s+([a-zA-Z0-9_.]+)\s*;`).
+- Reporting: include provenance (descriptor filename + library name + export filter + matched paths) in validation messages to help maintainers fix issues.
+
+Quick verification pseudocode (Python-like)
+
+def build_fqcn_index():
+    files = find_files('**/*/src/**/*.java', exclude=['all/build/**'])
+    index = {}
+    for f in files:
+        pkg = parse_package_declaration(f) or infer_pkg_from_path(f)
+        cls = os.path.splitext(os.path.basename(f))[0]
+        fqcn = pkg + '.' + cls if pkg else cls
+        index[fqcn] = f
+    return index
+
+def check_exports(descriptors, fqcn_index):
+    owners = defaultdict(set)
+    errors = []
+    for d in descriptors:
+        pid = extract_plugin_id(d)
+        for lib in d.findall('.//runtime/library'):
+            libname = lib.get('name')
+            exports = lib.findall('.//export')
+            if not exports:
+                errors.append(f"{d}: library {libname} has no exports")
+                continue
+            for exp in exports:
+                pattern = exp.get('name')
+                matched = match_fqcn_pattern(pattern, fqcn_index)
+                if not matched:
+                    errors.append(f"{d}: export {pattern} matches no Java classes")
+                for fqcn in matched:
+                    owners[fqcn_index[fqcn]].add(pid)
+    for path, pset in owners.items():
+        if len(pset) > 1:
+            errors.append(f"Class {path} is exported by multiple plugins: {sorted(pset)}")
+    return errors
+
+Notes on automation
+- This check can be integrated into CI as a fast verification step (no full compilation required). It prevents accidental overlapping exports and empty exports.
+- Optionally, fail the build on any of these errors or surface them as actionable CI comments for a PR.
+
+If you want, I can implement a small Python script (or an Ant task) that performs these checks and emits a JSON report; tell me which format you prefer and I will add it under `tools/` with a README and a command example.
