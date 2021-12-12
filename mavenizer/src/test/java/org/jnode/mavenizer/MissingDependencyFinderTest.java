package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.SortedSet;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jnode.mavenizer.Directory.SourceRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.util.Sets.newTreeSet;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class MissingDependencyFinderTest extends AbstractTestWithTemporaryFolder {
    @Mock
    private DependencyFinder dependencyFinder;
    @Mock
    private IAntProject antProject;
    @Mock
    private IProject project;

    private PluginInfos pluginInfos;

    @BeforeEach
    void setUp() {
        pluginInfos = new PluginInfos();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "plugin1",
        "!plugin1",
        "plugin1,!plugin2,!plugin3,plugin4"
    })
    void findMissingDependencies(String reqPlugins, SoftAssertions softly) throws IOException {
        SourceRoot srcRoot = SRC_ROOT;
        MissingDependencyFinder missingDependencyFinder =
            new MissingDependencyFinder(antProject, pluginInfos, dependencyFinder, srcRoot);
        Path projectDir = temporaryFolder.resolve("mockProject");
        when(project.getDirectory()).thenReturn(projectDir.toString());
        String[] requiredPlugins = reqPlugins.split(",");
        PluginInfo plugin = createPlugin(project, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE plugin SYSTEM \"jnode.dtd\">" +
            "<plugin id=\"plugin\" version=\"@VERSION@\" name=\"plugin\"" +
            "  license-name=\"lgpl\">" +
            "  <requires>" +
            stream(requiredPlugins).filter(this::isProvided)
                .map(requiredPlugin -> "<import plugin=\"" + requiredPlugin + "\"/>")
                .collect(joining()) +
            "  </requires>" +
            "</plugin>", "plugin");
        Path pluginDir = projectDir.resolve("plugin");
        createDirectories(pluginDir);
        List<Path> srcDir = stream(requiredPlugins).map(projectDir::resolve).collect(toList());
        srcDir.add(pluginDir);
        when(project.getSourceDirectories(srcRoot)).thenReturn(srcDir.toArray(Path[]::new));
        for (int i = 0; i < srcDir.size() - 1/*exclude last directory*/; i++) {
            createPlugin(antProject, plugin, requiredPlugins[i], "package" + i, srcDir.get(i));
        }

        SortedSet<String> missingPluginIds = missingDependencyFinder.findMissingDependencies(plugin);

        softly.assertThat(missingPluginIds).containsExactlyElementsOf(
            stream(requiredPlugins).filter(requiredPlugin -> !isProvided(requiredPlugin)).toList());
    }

    private void createPlugin(IAntProject project, PluginInfo plugin, String requiredPluginId, String exportedPackage,
                              Path pluginSources) throws IOException {
        String libraryName = requiredPluginId + ".jar";
        createPlugin(plugin.getProject(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE plugin SYSTEM \"jnode.dtd\">" +
            "<plugin id=\"" + requiredPluginId + "\" version=\"@VERSION@\" name=\"requiredPlugin\"" +
            "  license-name=\"lgpl\">" +
            "  <runtime>" +
            "    <library name=\"" + libraryName + "\">" +
            "      <export name=\"" + exportedPackage + ".*\"/>" +
            "    </library>" +
            "  </runtime>" +
            "</plugin>", requiredPluginId);
        when(dependencyFinder.find(project, pluginSources, plugin)).thenReturn(newTreeSet(exportedPackage));
        Path pathToLib = temporaryFolder.resolve(libraryName + ".directory");
        createDirectories(pathToLib);
        when(project.getProperty(libraryName)).thenReturn(pathToLib.toString());
    }

    private PluginInfo createPlugin(IProject project, String pluginXML, String pluginId) throws IOException {
        PluginInfo pluginInfo = buildPluginInfo(project, pluginXML, pluginId);
        pluginInfos.add(pluginInfo);
        return pluginInfo;
    }

    private boolean isProvided(String pluginId) {
        return pluginId.startsWith("!");
    }
}