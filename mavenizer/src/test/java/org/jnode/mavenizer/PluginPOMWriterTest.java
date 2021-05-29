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
        File pomFile = new PluginPOMWriter(destinationRoot).write(getPluginInfo(pluginId));
        String pom = assertCommon(getPluginRoot(pluginId), pluginId, thirdParty, pomFile, "jar",
            TEST_PROJECT.getDirectory());
        assertThat(extractModules(pom)).isEmpty();
    }
}