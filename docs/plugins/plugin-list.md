plugin-list
============

Target audience: automated tools and AI agents that parse JNode plugin list configurations.

Purpose
-------
This document describes the naming convention used for plugin-list files found under the `all/conf` directory and provides practical guidance for programs (including AIs) that need to discover and validate plugin-list definitions and map them to boot menu entries.

What to look for
----------------
- Files: The directory `all/conf` contains files whose names represent plugin lists.
- Active lists: By convention active plugin-list files end with `-plugin-list.xml` and the filename prefix is the plugin-list name.
  - Example: the plugin-list named "default" is described in the file `default-plugin-list.xml`.
- Disabled lists: By convention a disabled plugin-list file is named with the suffix `-plugin-list_disabled.xml`. These should be treated as disabled/ignored by discovery processes for active lists (they do not match the active pattern `-plugin-list.xml`).
- XML root: The XML element `plugin-list` is the root (top-level) element of these files and must appear as the document element, for example:

```xml
<plugin-list name="default">
    ...
</plugin-list>
```

Parsing and validation recommendations
--------------------------------------
Follow these steps to robustly discover and validate plugin-list definitions:

1. File discovery
   - List files in `all/conf` and keep those that match the active filename pattern `^(.+)-plugin-list\.xml$`.
   - Explicitly ignore files that match the disabled suffix pattern `^(.+)-plugin-list_disabled\.xml$` when enumerating active lists.

2. XML parsing
   - Parse the file as XML using a proper XML parser that handles encoding (typically UTF-8).
   - Confirm the document element (XML root) is `plugin-list` and read its `name` attribute.
   - If the document element is not `plugin-list`, treat the file as malformed and report an error.

3. Cross-check filename vs XML
   - Compare the name extracted from the filename prefix with the `name` attribute of the `plugin-list` element.
   - If they differ, emit a warning (or error depending on policy). Prefer the explicit XML `name` when authoritative but log mismatches for maintainers.

4. Handling edge cases
   - Missing `name` attribute: treat as malformed and report an error.
   - Multiple plugin-list elements: the file's root should be `plugin-list`; if multiple appear, handle per-policy (report and process each if needed).
   - Disabled files: do not treat files ending with `-plugin-list_disabled.xml` as active lists.
   - Invalid XML or encoding issues: report parsing errors clearly.

Includes
--------
- Including other plugin-lists: a plugin-list file can include another plugin-list using an XML element like:

```xml
<include file="full-plugin-list.xml"/>
```

  - The `file` attribute names the plugin-list file to include (typically a filename located in `all/conf`, e.g. `full-plugin-list.xml`).
  - Tools should resolve includes relative to the including file's directory (usually `all/conf`) and parse the referenced file.
  - Semantics: treat an `include` as a logical inclusion of the referenced plugin-list's contents into the including list (for example, append the included file's plugin entries). Record provenance so it is possible to report which items came from which file.
  - Disabled includes: if the referenced filename matches the disabled pattern (e.g. `...-plugin-list_disabled.xml`), treat it as disabled unless the project policy explicitly allows including disabled lists; at minimum warn of a reference to a disabled list.
  - Non-existent includes: if the referenced file does not exist, report an error.
  - Inclusion cycles: detect cycles by tracking visited filenames during include resolution. On detecting a cycle, report an error and abort processing that chain. Implement a recursion depth limit (recommended max depth 10) to avoid runaway processing.

Practical guidance:
  - Resolve relative paths and normalize filenames before comparison to detect duplicates/cycles.
  - Log warnings for mismatches between included-file name and its internal `<plugin-list name=...>` attribute as well.

Manifest and main class
-----------------------
- A plugin-list file may declare a Java manifest block that specifies runtime attributes. A common usage is to declare the main Java class to launch after boot. Example:

```xml
<plugin-list name="fullgui">
  <manifest>
    <attribute key="Main-Class" value="org.jnode.awt.StartAwt"/>
  </manifest>
  ...
</plugin-list>
```

  - The `Main-Class` attribute value is the fully-qualified Java class name to be launched after boot (here: `org.jnode.awt.StartAwt`).
  - Tools should extract the `Main-Class` value (if present) and include it in the discovered plugin-list metadata as `main_class`.
  - If multiple `manifest` sections or multiple `Main-Class` attributes exist, follow project policy (recommendation: use the first occurrence and log a warning for duplicates).
  - If the `Main-Class` value references a class that is not present in the generated runtime image, tools may optionally validate presence and warn if missing.

Requirements for the Java main class
------------------------------------
- The declared main class must conform to the standard Java entry-point contract: it must declare a method with the exact signature `public static void main(String[] argv)`. Tools and maintainers can rely on this contract when launching the class at runtime.
- Tools may optionally validate that the class exists in the runtime image and that it exposes a public static `main(String[])` method; if validation fails, report a clear error or warning depending on policy.

Parsing notes:
  - Parse the `manifest` element and then its `attribute` children. Match `attribute` nodes where `key="Main-Class"` and read the `value` attribute.
  - Preserve the exact value (case-sensitive) and do not normalize package or class name case.

Main-Class arguments
--------------------
- A plugin-list XML may specify one or more arguments to pass to the main class's `main(String[] argv)` method using `attribute` elements with `key="Main-Class-Arg"`. Example:

```xml
<plugin-list name="fullgui">
  <manifest>
    <attribute key="Main-Class" value="org.jnode.awt.StartAwt"/>
    <attribute key="Main-Class-Arg" value="boot"/>
    <attribute key="Main-Class-Arg" value="-verbose"/>
  </manifest>
</plugin-list>
```

  - The order of `Main-Class-Arg` elements is significant: the first `Main-Class-Arg` encountered becomes `argv[0]`, the second `argv[1]`, and so on.
  - Tools should collect all `Main-Class-Arg` values in document order into a list `main_args` and include it in the plugin-list metadata.
  - If both an including file and an included file provide `Main-Class-Arg` entries, follow project policy for ordering; recommended behavior: includeer's `Main-Class-Arg` entries appear before included file's entries unless otherwise specified. The pseudocode below follows a policy of appending included args after the including file's args.
  - If no `Main-Class-Arg` are present, `main_args` should be an empty list.

Parsing notes for args:
  - Read `attribute` nodes with `key="Main-Class-Arg"` and collect their `value` attributes in order. Treat values as raw strings (do not split on whitespace).
  - Validate presence of values and report a warning for empty `value` attributes.

Updated recursive parsing pseudocode (include manifest and args extraction)
-----------------------------------------------------------------------
def parse_plugin_list(path, visited=set(), depth=0, max_depth=10):
    if depth > max_depth:
        report_error(path, 'include depth exceeds max allowed')
        return None
    if path in visited:
        report_error(path, 'include cycle detected')
        return None
    visited.add(path)
    xml = parse_xml(path)
    if xml.root.tag != 'plugin-list':
        report_error(path, 'root element is not plugin-list')
        return None
    name = xml.root.get('name')
    if not name:
        report_error(path, 'missing name attribute')
    plugins = []
    main_class = None
    main_args = []
    for child in xml.root:
        if child.tag == 'include':
            inc_file = child.get('file')
            if not inc_file:
                report_error(path, 'include without file attribute')
                continue
            inc_path = resolve_path(path, inc_file)
            included = parse_plugin_list(inc_path, visited, depth+1, max_depth)
            if included:
                plugins.extend(included.plugins)
                # append included's args after current args
                main_args.extend(getattr(included, 'main_args', []) or [])
                # inherit main_class if not set
                if not main_class and getattr(included, 'main_class', None):
                    main_class = included.main_class
        elif child.tag == 'manifest':
            # manifest may contain multiple attribute elements
            for attr in child.findall('attribute'):
                key = attr.get('key')
                val = attr.get('value')
                if key == 'Main-Class' and val:
                    if main_class:
                        report_warning(path, 'multiple Main-Class entries; using first occurrence')
                    else:
                        main_class = val
                elif key == 'Main-Class-Arg':
                    if val is None:
                        report_warning(path, 'Main-Class-Arg attribute without value')
                    else:
                        # preserve order, append to main_args
                        main_args.append(val)
        elif child.tag == 'plugin':
            plugins.append(child.get('id'))
        else:
            # handle other expected child nodes if any
            pass
    visited.remove(path)
    pl = PluginList(name=name, plugins=plugins)
    pl.main_class = main_class
    pl.main_args = main_args
    return pl

Notes:
- `main_class` is added to the returned PluginList object to advertise the main class to launch after boot.
- `main_args` is a list of strings representing argv for the main class; `argv[0]` corresponds to the first `Main-Class-Arg` element encountered in the including file and its included files according to the chosen ordering policy.
- Inclusion rules: if an included file defines a `Main-Class` and the including file does not, the included value may be used. If both define it, prefer the including file's manifest (or follow project policy) — above code prefers including file over included ones.

Mapping plugin-lists to GRUB menu entries
-----------------------------------------
Each plugin-list usually corresponds to one GRUB menu entry used at boot. The GRUB menu entries are typically stored under `local/menu-cdrom.lst` (or other menu descriptor files). To map a plugin-list name to the GRUB entry:

- Find entries in `local/menu-cdrom.lst` that reference the plugin-list name via the module name pattern `module /<listname>.jgz`.
  - Example: for the plugin-list named `full`:
    - The plugin list file is `all/conf/full-plugin-list.xml`.
    - The GRUB menu file `local/menu-cdrom.lst` contains a menu entry that includes a line such as `module /full.jgz`.
    - The presence of `module /full.jgz` indicates the menu entry boots with the `full` plugin-list.

Recommendations for AIs/tools when mapping:
- Search `local/menu-cdrom.lst` (and other relevant menu files) for occurrences of the pattern `module /<name>.jgz` or for menu entries that reference the plugin-list name.
- Normalize and trim whitespace and ignore commented lines when searching.
- Treat the mapping as many-to-many: a plugin-list may be referenced by multiple menu entries (for example, different boot options that load the same plugin list), and a menu entry might reference multiple modules.

Minimal parsing + mapping pseudocode
-----------------------------------
- files = list_files('all/conf')
- active_files = [f for f in files if re.match(r'^(.+)-plugin-list\.xml$', f) and not re.match(r'^(.+)-plugin-list_disabled\.xml$', f)]
- for f in active_files:
    prefix = capture_prefix(f)
    xml = parse_xml(f)
    if xml.root.tag != 'plugin-list':
        report_error(f, 'root element is not plugin-list')
        continue
    xml_name = xml.root.get('name')
    if not xml_name:
        report_error(f, 'missing name attribute')
    if xml_name != prefix:
        report_warning(f, f'filename prefix {prefix} != xml name {xml_name}')
    # Map to grub entries
    menu_text = read_file('local/menu-cdrom.lst')
    if re.search(rf"module\s+/\b{re.escape(xml_name)}\.jgz\b", menu_text):
        map[f] = 'found in local/menu-cdrom.lst'

Contract (for tools/AI components)
----------------------------------
- Inputs: repository workspace path; directories `all/conf` and `local`.
- Outputs: list of plugin-lists with {filename, filename_prefix, xml_name, active_status, main_class, included_files, main_args, grub_menu_matches, validation_warnings}.
- Error modes: missing directory/file, unreadable file, invalid XML, mismatched names, include resolution errors.
- Success criteria: active `*-plugin-list.xml` files are parsed; disabled lists are ignored; included plugin-lists are resolved without cycles; `Main-Class` (if present) is extracted into `main_class`; `Main-Class-Arg` entries (if present) are collected into `main_args` in document order; mapping to GRUB entries is attempted and reported.
