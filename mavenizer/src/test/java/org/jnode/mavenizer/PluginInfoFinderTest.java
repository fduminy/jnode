package org.jnode.mavenizer;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.jnode.mavenizer.Conditions.childOf;
import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriterTest.JNODE_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;
import static org.jnode.mavenizer.PluginPOMWriterTest.THIRD_PARTY_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.XML_EXTENSION;
import static org.jnode.mavenizer.Project.Core;
import static org.jnode.mavenizer.Utils.findPlugin;

@ExtendWith(SoftAssertionsExtension.class)
public class PluginInfoFinderTest {
    static final int NB_TEST_PROJECT_MODULES = 48;

    @Test
    public void find(SoftAssertions softly) {
        List<PluginInfo> infos = new PluginInfoFinder(SRC_ROOT).find(TEST_PROJECT);

        softly.assertThat(infos).hasSize(NB_TEST_PROJECT_MODULES);
        int nbThirdParty = 0;
        for (PluginInfo info : infos) {
            String fileName = info.getDescriptorFile().getFileName().toString();
            softly.assertThat(info.getDescriptorFile())
                .as(fileName)
                .isRegularFile().exists()
                .is(childOf(SRC_ROOT, TEST_PROJECT, "descriptors"));
            softly.assertThat(info.getDescriptorFile().getFileName().toString())
                .as(fileName)
                .endsWith('.' + XML_EXTENSION);
            softly.assertThat(info.getProject())
                .as(fileName)
                .isEqualTo(TEST_PROJECT);
            softly.assertThat(info.getId())
                .as(fileName)
                .isEqualTo(info.getPluginDescriptor().getId());

            if (info.isThirdParty()) {
                nbThirdParty++;
                softly.assertThat(info.getVersion())
                    .as(fileName)
                    .isNotEqualTo(JNODE_VERSION);
            } else {
                softly.assertThat(info.getVersion())
                    .as(fileName)
                    .isEqualTo(JNODE_VERSION);
            }
        }
        softly.assertThat(nbThirdParty).isEqualTo(2);

        PluginInfo jnodePlugin = findPlugin(infos, JNODE_PLUGIN_ID);
        softly.assertThat(jnodePlugin.isThirdParty()).isFalse();

        PluginInfo thirdPartyPlugin = findPlugin(infos, THIRD_PARTY_PLUGIN_ID);
        softly.assertThat(thirdPartyPlugin.isThirdParty()).isTrue();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void find_pluginId_not_equals_to_filename(SoftAssertions softly) {
        List<PluginInfo> infos = new PluginInfoFinder(SRC_ROOT).find(Core);

        String pluginFile = "org.jnode.driver.textscreen_x86.xml";
        PluginInfo plugin = null;
        for (PluginInfo info : infos) {
            if (info.getDescriptorFile().getFileName().toString().equals(pluginFile)) {
                plugin = info;
                break;
            }
        }

        softly.assertThat(plugin).overridingErrorMessage("plugin file %s not found", pluginFile).isNotNull();
        softly.assertThat(plugin.getId()).isEqualTo(plugin.getPluginDescriptor().getId());
    }
}