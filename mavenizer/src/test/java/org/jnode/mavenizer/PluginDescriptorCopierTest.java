package org.jnode.mavenizer;

import java.io.File;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriterTest.JNODE_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.THIRD_PARTY_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.getPluginInfo;
import static org.jnode.mavenizer.SourceFileType.RESOURCES;

public class PluginDescriptorCopierTest extends AbstractTestWithDestinationRoot {
    @Test
    public void copy_jnode_plugin() {
        copy(JNODE_PLUGIN_ID);
    }

    @Test
    public void copy_third_party_plugin() {
        copy(THIRD_PARTY_PLUGIN_ID);
    }

    private void copy(String pluginId) {
        PluginInfo pluginInfo = getPluginInfo(pluginId);
        new PluginDescriptorCopier(SRC_ROOT, destinationRoot).copy(pluginInfo);

        File pluginRoot = new File(getPluginRoot(pluginId));
        File srcMainResources = new File(pluginRoot, "src/main/" + RESOURCES.getMavenDirectory());
        File descriptorFile = new File(srcMainResources, "plugin.xml");
        assertThat(descriptorFile).hasContentEqualTo(pluginInfo.getDescriptorFile());
    }
}