package org.jnode.mavenizer;

import java.nio.file.Path;
import java.util.SortedSet;
import org.junit.jupiter.api.Test;

import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.AbstractPOMWriterTest.getPluginInfo;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;
import static org.jnode.mavenizer.Project.Core;

class DependencyFinderTest {
    @Test
    void find() {
        Path sourceDirectory = getSourceDirectory(TEST_PROJECT, get("src", "fs"));
        PluginInfo pluginInfo = getPluginInfo(TEST_PROJECT, "org.jnode.fs.exfat");
        SortedSet<String> dependencies = new DependencyFinder().find(ANT_PROJECT, sourceDirectory, pluginInfo);

        assertThat(dependencies).containsExactly(
            "java.io",
            "java.nio",
            "java.util",
            "org.apache.log4j",
            "org.jnode.driver",
            "org.jnode.driver.block",
            "org.jnode.fs",
            "org.jnode.fs.spi",
            "org.jnode.partitions"); // sorted values
    }

    @Test
    void find_when_plugin_has_subpackages() {
        Path sourceDirectory = getSourceDirectory(Core, get("src", "driver"));
        PluginInfo pluginInfo = getPluginInfo(Core, "org.jnode.driver.system.cmos");
        SortedSet<String> dependencies = new DependencyFinder().find(ANT_PROJECT, sourceDirectory, pluginInfo);

        assertThat(dependencies).containsExactly(
            "java.security",
            "javax.naming",
            "org.jnode.naming",
            "org.jnode.plugin",
            "org.jnode.system.resource",
            "org.jnode.util",
            "org.jnode.vm"
        ); // sorted values
    }

    private Path getSourceDirectory(Project project, Path path) {
        return project.getRoot(SRC_ROOT).resolve(path);
    }
}