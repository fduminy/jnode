package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Before
    public void seUp() {
        pluginInfos = new PluginInfos();
    }

    @Test
    public void write_jnode_plugin() throws IOException {
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.driver.block"));
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.fs.service"));
        pluginInfos.add(getPluginInfo(TEST_PROJECT, "org.jnode.partitions"));

        String pom = write(TEST_PROJECT, JNODE_PLUGIN_ID, false);

        assertThat(extractDependencies(pom)).containsExactly(
            "org.jnode.fs:org.jnode.driver.block:0.2.9-dev",
            "org.jnode.fs:org.jnode.fs.service:0.2.9-dev",
            "org.jnode.fs:org.jnode.partitions:0.2.9-dev");
    }

    @Test
    public void write_third_party_plugin() throws IOException {
        String pom = write(TEST_PROJECT, THIRD_PARTY_PLUGIN_ID, true);

        assertThat(extractDependencies(pom)).containsExactly("jcifs:jcifs:1.2.6");
    }

    @Test
    public void write_provided_library_plugin() throws IOException {
        Project project = Core;
        String pluginId = "gnu.mauve";
        String pom = write(project, pluginId, false);

        String pathToLib = "lib/mauve.jar";
        PluginInfo pluginInfo = getPluginInfo(project, pluginId);
        assertThat(new File(getPluginHome(destinationRoot, pluginInfo), pathToLib))
            .hasContentEqualTo(new File(project.getRoot(SRC_ROOT), pathToLib));
        assertThat(extractDependencies(pom)).containsExactly("org.jnode.provided.library:gnu.mauve:1.0.0");
        assertThat(pom)
            .contains("<scope>system</scope>")
            .contains("<systemPath>${project.basedir}/" + pathToLib + "</systemPath>");
    }

    private String write(Project project, String pluginId, boolean thirdParty) throws IOException {
        File pomFile = new PluginPOMWriter(destinationRoot).write(pluginInfos, getPluginInfo(project, pluginId));
        String pom = assertCommon(getPluginRoot(project, pluginId), pluginId, thirdParty, pomFile, "jar",
            project.getDirectory());
        assertThat(extractModules(pom)).isEmpty();
        return pom;
    }
}