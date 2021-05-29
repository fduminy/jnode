package org.jnode.mavenizer;

import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.Conditions.childOf;
import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriterTest.JNODE_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;
import static org.jnode.mavenizer.PluginPOMWriterTest.THIRD_PARTY_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.XML_EXTENSION;
import static org.jnode.mavenizer.Project.Core;
import static org.jnode.mavenizer.Utils.findPlugin;

public class PluginInfoFinderTest {
    static final int NB_TEST_PROJECT_MODULES = 48;

    @Test
    public void find() {
        List<PluginInfo> infos = new PluginInfoFinder(SRC_ROOT).find(TEST_PROJECT);

        assertThat(infos).hasSize(NB_TEST_PROJECT_MODULES);
        int nbThirdParty = 0;
        for (PluginInfo info : infos) {
            String fileName = info.getDescriptorFile().getName();
            assertThat(info.getDescriptorFile())
                .as(fileName)
                .isFile().exists()
                .hasExtension(XML_EXTENSION)
                .is(childOf(SRC_ROOT, TEST_PROJECT, "descriptors"));
            assertThat(info.getProject())
                .as(fileName)
                .isEqualTo(TEST_PROJECT);
            assertThat(info.getId())
                .as(fileName)
                .isEqualTo(info.getPluginDescriptor().getId());

            if (info.isThirdParty()) {
                nbThirdParty++;
                assertThat(info.getVersion())
                    .as(fileName)
                    .isNotEqualTo(JNODE_VERSION);
            } else {
                assertThat(info.getVersion())
                    .as(fileName)
                    .isEqualTo(JNODE_VERSION);
            }
        }
        assertThat(nbThirdParty).isEqualTo(2);

        PluginInfo jnodePlugin = findPlugin(infos, JNODE_PLUGIN_ID);
        assertThat(jnodePlugin.isThirdParty()).isFalse();

        PluginInfo thirdPartyPlugin = findPlugin(infos, THIRD_PARTY_PLUGIN_ID);
        assertThat(thirdPartyPlugin.isThirdParty()).isTrue();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void find_pluginId_not_equals_to_filename() {
        List<PluginInfo> infos = new PluginInfoFinder(SRC_ROOT).find(Core);

        String pluginFile = "org.jnode.driver.textscreen_x86.xml";
        PluginInfo plugin = null;
        for (PluginInfo info : infos) {
            if (info.getDescriptorFile().getName().equals(pluginFile)) {
                plugin = info;
                break;
            }
        }

        assertThat(plugin).overridingErrorMessage("plugin file %s not found", pluginFile).isNotNull();
        assertThat(plugin.getId()).isEqualTo(plugin.getPluginDescriptor().getId());
    }
}