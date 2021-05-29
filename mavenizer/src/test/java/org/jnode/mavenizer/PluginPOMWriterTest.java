package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.Project.FS;

public class PluginPOMWriterTest extends AbstractPOMWriterTest {
    static final String XML_EXTENSION = "xml";
    static final Project TEST_PROJECT = FS;

    static final String JNODE_PLUGIN_ID = "org.jnode.fs";
    static final String THIRD_PARTY_PLUGIN_ID = "jcifs";

    @Test
    public void write_jnode_plugin() throws IOException {
        write(JNODE_PLUGIN_ID, false);
    }

    @Test
    public void write_third_party_plugin() throws IOException {
        write(THIRD_PARTY_PLUGIN_ID, true);
    }

    private void write(String pluginId, boolean thirdParty) throws IOException {
        PluginInfos pluginInfos = new PluginInfos();
        pluginInfos.add(getPluginInfo("org.jnode.driver.block"));
        pluginInfos.add(getPluginInfo("org.jnode.fs.service"));
        pluginInfos.add(getPluginInfo("org.jnode.partitions"));

        File pomFile = new PluginPOMWriter(destinationRoot).write(pluginInfos, getPluginInfo(pluginId));
        String pom = assertCommon(getPluginRoot(pluginId), pluginId, thirdParty, pomFile, "jar",
            TEST_PROJECT.getDirectory());
        assertThat(extractModules(pom)).isEmpty();
        if (thirdParty) {
            assertThat(extractDependencies(pom)).containsExactly("jcifs:jcifs:1.2.6");
        } else {
            assertThat(extractDependencies(pom)).containsExactly(
                "org.jnode.fs:org.jnode.driver.block:0.2.9-dev",
                "org.jnode.fs:org.jnode.fs.service:0.2.9-dev",
                "org.jnode.fs:org.jnode.partitions:0.2.9-dev");
        }
    }
}