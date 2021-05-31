package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.file.Files.writeString;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.Project.Core;
import static org.jnode.mavenizer.Project.FS;
import static org.jnode.mavenizer.Utils.getPluginHome;

public class PluginPOMWriterTest extends AbstractPOMWriterTest {
    static final String XML_EXTENSION = "xml";
    static final Project TEST_PROJECT = FS;

    static final String JNODE_PLUGIN_ID = "org.jnode.fs";
    static final String THIRD_PARTY_PLUGIN_ID = "jcifs";

    private PluginInfos pluginInfos;

    @BeforeEach
    public void setUp() {
        pluginInfos = new PluginInfos();
    }

    @Test
    public void write_jnode_plugin() throws IOException {
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
    public void write_third_party_plugin() throws IOException {
        PluginInfo pluginInfo = getPluginInfo(TEST_PROJECT, THIRD_PARTY_PLUGIN_ID);
        String pom = write(pluginInfo, true);

        softly.assertThat(extractDependencies(pom)).containsExactly("jcifs:jcifs:1.2.6");
    }

    @Test
    public void write_third_party_plugin_used_by_another_plugin() throws IOException {
        pluginInfos.add(getPluginInfo(Core, "org.apache.jakarta.log4j"));
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE plugin SYSTEM \"jnode.dtd\">" +
            "<plugin id=\"org.jnode.util\" version=\"@VERSION@\" name=\"JNode utility classes\"" +
            "  license-name=\"lgpl\">" +
            "  <requires>" +
            "    <import plugin=\"org.apache.jakarta.log4j\"/>" +
            "  </requires>" +
            "</plugin>";
        PluginInfo pluginInfo = buildPluginInfo(xml);
        pluginInfos.add(pluginInfo);

        String pom = write(pluginInfo, false);

        softly.assertThat(extractDependencies(pom)).contains("org.jnode.core:org.apache.jakarta.log4j:1.2.8");
    }

    @Test
    public void write_provided_library_plugin() throws IOException {
        Project project = Core;
        PluginInfo pluginInfo = getPluginInfo(project, "gnu.mauve");
        String pom = write(pluginInfo, false);

        String pathToLib = "lib/mauve.jar";
        softly.assertThat(getPluginHome(destinationRoot, pluginInfo).resolve(pathToLib))
            .hasSameBinaryContentAs(project.getRoot(SRC_ROOT).resolve(pathToLib));
        softly.assertThat(extractDependencies(pom)).containsExactly("org.jnode.provided.library:gnu.mauve:1.0.0");
        softly.assertThat(pom)
            .contains("<scope>system</scope>")
            .contains("<systemPath>${project.basedir}/" + pathToLib + "</systemPath>");
    }

    private PluginInfo buildPluginInfo(String xml) throws IOException {
        Path descriptorFile = temporaryFolder.resolve("plugin.xml");
        writeString(descriptorFile, xml);
        return new PluginInfo(Core, descriptorFile);
    }

    private String write(PluginInfo pluginInfo, boolean thirdParty) throws IOException {
        Path pomFile = new PluginPOMWriter(destinationRoot).write(pluginInfos, pluginInfo);
        String pom = assertCommon(getPluginRoot(pluginInfo.getProject(), pluginInfo.getId()), pluginInfo.getId(), thirdParty, pomFile, "jar",
            pluginInfo.getProject().getDirectory());
        softly.assertThat(extractModules(pom)).isEmpty();
        return pom;
    }
}