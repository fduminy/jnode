package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singleton;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.Project.Core;
import static org.jnode.mavenizer.Project.FS;
import static org.jnode.mavenizer.Utils.getPluginHome;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginPOMWriterTest extends AbstractPOMWriterTest {
    static final String XML_EXTENSION = "xml";
    static final Project TEST_PROJECT = FS;

    static final String JNODE_PLUGIN_ID = "org.jnode.fs";
    static final String THIRD_PARTY_PLUGIN_ID = "jcifs";

    private PluginInfos pluginInfos;

    @Mock
    private MissingDependencyFinder missingDependencyFinder;

    @BeforeEach
    public void setUp() {
        pluginInfos = new PluginInfos();
    }

    @Test
    void write_jnode_plugin() throws IOException {
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.driver.block"));
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.fs.service"));
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.partitions"));
        PluginInfo pluginInfo = getPluginInfo(TEST_PROJECT, JNODE_PLUGIN_ID);

        String pom = write(pluginInfo, false);

        softly.assertThat(extractDependencies(pom)).containsExactly(
            "org.jnode.fs:org.jnode.driver.block:0.2.9-dev",
            "org.jnode.fs:org.jnode.fs.service:0.2.9-dev",
            "org.jnode.fs:org.jnode.partitions:0.2.9-dev");
    }

    @Test
    void write_jnode_plugin_should_add_missing_dependencies() throws IOException {
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.driver.block"));
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.fs.service"));
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.partitions"));
        String missingDependency = "org.jnode.driver.block.ide.disk";
        pluginInfos.add(getPluginInfo(TEST_PROJECT, missingDependency));
        PluginInfo pluginInfo = getPluginInfo(TEST_PROJECT, JNODE_PLUGIN_ID);
        when(missingDependencyFinder.findMissingDependencies(pluginInfo)).thenReturn(
            new TreeSet<>(singleton(missingDependency)));

        String pom = write(pluginInfo, false);

        softly.assertThat(extractDependencies(pom)).contains("org.jnode.fs:" + missingDependency + ":" + JNODE_VERSION);
    }

    @Test
    void write_third_party_plugin() throws IOException {
        PluginInfo pluginInfo = getPluginInfo(TEST_PROJECT, THIRD_PARTY_PLUGIN_ID);
        String pom = write(pluginInfo, true);

        softly.assertThat(extractDependencies(pom)).containsExactly("jcifs:jcifs:1.2.6");
    }

    @Test
    void write_third_party_plugin_used_by_another_plugin() throws IOException {
        pluginInfos.add(getPluginInfo(Core, "org.apache.jakarta.log4j"));
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE plugin SYSTEM \"jnode.dtd\">" +
            "<plugin id=\"org.jnode.util\" version=\"@VERSION@\" name=\"JNode utility classes\"" +
            "  license-name=\"lgpl\">" +
            "  <requires>" +
            "    <import plugin=\"org.apache.jakarta.log4j\"/>" +
            "  </requires>" +
            "</plugin>";
        PluginInfo pluginInfo = buildPluginInfo(Core, xml, "");
        pluginInfos.add(pluginInfo);

        String pom = write(pluginInfo, false);

        softly.assertThat(extractDependencies(pom)).contains("org.jnode.core:org.apache.jakarta.log4j:1.2.8");
    }

    @Test
    void write_provided_library_plugin() throws IOException {
        PluginInfo pluginInfo = getPluginInfo(Core, "gnu.mauve");

        String pom = write(pluginInfo, false);

        String pathToLib = "lib/mauve.jar";
        Path actualLib = getPluginHome(destinationRoot, pluginInfo).resolve(pathToLib);
        Path expectedLib = SRC_ROOT.getDirectory().resolve(pluginInfo.getProject().getDirectory()).resolve(pathToLib);
        softly.assertThat(actualLib.toFile()).hasSameBinaryContentAs(expectedLib.toFile());
        softly.assertThat(extractDependencies(pom)).containsExactly("org.jnode.provided.library:gnu.mauve:1.0.0");
        softly.assertThat(pom)
            .contains("<scope>system</scope>")
            .contains("<systemPath>${project.basedir}/" + pathToLib + "</systemPath>");
    }

    private String write(PluginInfo pluginInfo, boolean thirdParty) throws IOException {
        Path pomFile =
            new PluginPOMWriter(ANT_PROJECT, destinationRoot).write(pluginInfos, pluginInfo);
        String pom =
            assertCommon(getPluginRoot(pluginInfo.getProject(), pluginInfo.getId()), pluginInfo.getId(), thirdParty,
                pomFile, "jar",
                pluginInfo.getProject().getDirectory());
        softly.assertThat(extractModules(pom)).isEmpty();
        return pom;
    }
}