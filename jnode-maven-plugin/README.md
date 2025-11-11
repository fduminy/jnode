# JNode Maven Plugin

This Maven plugin provides the functionality to build JNode plugins, converting the previous Ant-based `PluginTask` to a Maven Mojo.

## Overview

The JNode Maven Plugin (`jnode-maven-plugin`) is used to build JNode plugin JAR files from plugin descriptor XML files. It reads plugin descriptors, processes runtime libraries with export/exclude filters, and generates plugin JARs with appropriate manifests.

## Conversion from Ant

This plugin replaces the Ant `PluginTask` defined in `builder/src/builder/org/jnode/build/PluginTask.java`. The main differences:

### Ant Task (before)
```xml
<taskdef name="plugin" classname="org.jnode.build.PluginTask" classpathref="cp-jnode"/>
<plugin todir="${plugins.dir}" tmpdir="${build.dir}/tmp/plugins" pluginDir="${descriptors.dir}">
    <libalias name="jnode-core.jar" alias="${jnode-core.jar}"/>
    <!-- more aliases -->
    <descriptors dir="${descriptors.dir}/">
        <include name="*.xml"/>
        <exclude name="*plugin-list.xml"/>
    </descriptors>
</plugin>
```

### Maven Plugin (after)
```xml
<plugin>
    <groupId>org.jnode</groupId>
    <artifactId>jnode-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>build-plugin</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <descriptorsDirectory>${basedir}/descriptors</descriptorsDirectory>
        <outputDirectory>${project.build.directory}/plugins</outputDirectory>
        <pluginDirectory>${project.build.directory}/classes</pluginDirectory>
        <libraryAliases>
            <jnode-core.jar>${jnode-core.jar}</jnode-core.jar>
            <!-- more aliases -->
        </libraryAliases>
    </configuration>
</plugin>
```

## Configuration Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `descriptorsDirectory` | File | Yes | - | Directory containing plugin descriptor XML files |
| `outputDirectory` | File | Yes | `${project.build.directory}/plugins` | Output directory for generated plugin JAR files |
| `tmpDirectory` | File | No | `${project.build.directory}/tmp/plugins` | Temporary directory for intermediate files |
| `pluginDirectory` | File | Yes | - | Directory containing source classes and resources for plugins |
| `libraryAliases` | Map<String, File> | No | empty | Mapping of library names to actual file locations |
| `maxThreadCount` | int | No | 10 | Maximum number of concurrent threads for building plugins |
| `maxPluginCount` | int | No | 500 | Maximum number of plugins that can be queued |
| `compressJars` | boolean | No | false | Whether to compress the generated JAR files |

## Usage Example

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jnode</groupId>
            <artifactId>jnode-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <id>build-plugins</id>
                    <phase>package</phase>
                    <goals>
                        <goal>build-plugin</goal>
                    </goals>
                    <configuration>
                        <descriptorsDirectory>${basedir}/core/descriptors</descriptorsDirectory>
                        <outputDirectory>${project.build.directory}/plugins</outputDirectory>
                        <pluginDirectory>${project.build.directory}/classes</pluginDirectory>
                        <libraryAliases>
                            <jnode-core.jar>${basedir}/core/build/classes</jnode-core.jar>
                            <jnode-fs.jar>${basedir}/fs/build/classes</jnode-fs.jar>
                        </libraryAliases>
                        <maxThreadCount>4</maxThreadCount>
                        <compressJars>false</compressJars>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Plugin Descriptor Format

Plugin descriptors are XML files that describe JNode plugins. See [docs/plugins/plugin.md](../../docs/plugins/plugin.md) for detailed documentation on the plugin descriptor format.

Example descriptor:
```xml
<plugin id="org.jnode.example" version="1.0">
    <runtime>
        <library name="jnode-core.jar">
            <export name="org.jnode.example.*"/>
        </library>
    </runtime>
</plugin>
```

## Building the Plugin

To build the Maven plugin itself:

```bash
cd jnode-maven-plugin
mvn clean install
```

This will install the plugin in your local Maven repository, making it available for use in other JNode projects.

## Key Features

- **Parallel Plugin Building**: Builds multiple plugins concurrently using a thread pool
- **Incremental Builds**: Only rebuilds plugins when descriptors or dependencies have changed
- **Library Aliasing**: Maps logical library names (e.g., `jnode-core.jar`) to actual file locations
- **Manifest Generation**: Automatically generates OSGi-style manifests with Bundle-SymbolicName and Bundle-Version
- **Export/Exclude Filtering**: Processes library export and exclude patterns from plugin descriptors

## Limitations

The current implementation is a direct port of the Ant task and includes:
- Basic library export/exclude filtering (full implementation pending)
- No support for packager subtasks (may be added in future versions)
- Simplified JAR creation compared to the Ant version

## See Also

- [Plugin Documentation](../../docs/plugins/plugin.md) - Details on plugin descriptor format
- [Plugin List Documentation](../../docs/plugins/plugin-list.md) - Information about plugin lists
- Original Ant task: `builder/src/builder/org/jnode/build/PluginTask.java`
