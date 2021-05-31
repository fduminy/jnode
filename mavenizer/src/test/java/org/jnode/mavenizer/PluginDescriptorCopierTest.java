package org.jnode.mavenizer;

import java.nio.file.Path;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.nio.file.Paths.get;
import static org.jnode.mavenizer.AbstractPOMWriterTest.getPluginInfo;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriterTest.JNODE_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;
import static org.jnode.mavenizer.PluginPOMWriterTest.THIRD_PARTY_PLUGIN_ID;
import static org.jnode.mavenizer.SourceFileType.RESOURCES;

@ExtendWith(SoftAssertionsExtension.class)
public class PluginDescriptorCopierTest extends AbstractTestWithDestinationRoot {
    @Test
    public void copy_jnode_plugin(SoftAssertions softly) {
        copy(softly, JNODE_PLUGIN_ID);
    }

    @Test
    public void copy_third_party_plugin(SoftAssertions softly) {
        copy(softly, THIRD_PARTY_PLUGIN_ID);
    }

    private void copy(SoftAssertions softly, String pluginId) {
        PluginInfo pluginInfo = getPluginInfo(TEST_PROJECT, pluginId);
        new PluginDescriptorCopier(SRC_ROOT, destinationRoot).copy(pluginInfo);

        Path pluginRoot = getPluginRoot(TEST_PROJECT, pluginId);
        Path srcMainResources = pluginRoot.resolve(get("src", "main", RESOURCES.getMavenDirectory()));
        Path descriptorFile = srcMainResources.resolve("plugin.xml");
        softly.assertThat(descriptorFile).hasSameTextualContentAs(pluginInfo.getDescriptorFile());
    }
}