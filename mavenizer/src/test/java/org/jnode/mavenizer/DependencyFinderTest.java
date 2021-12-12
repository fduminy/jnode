package org.jnode.mavenizer;

import java.nio.file.Path;
import java.util.SortedSet;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.nio.file.Paths.get;
import static org.jnode.mavenizer.AbstractPOMWriterTest.getPluginInfo;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;

@ExtendWith(SoftAssertionsExtension.class)
class DependencyFinderTest {
    private Path sourceDirectory;

    @BeforeEach
    private void setUp() {
        Path pluginHome = TEST_PROJECT.getRoot(SRC_ROOT);
        sourceDirectory = pluginHome.resolve(get("src", "fs"));
    }

    @Test
    void find(SoftAssertions softly) {
        PluginInfo pluginInfo = getPluginInfo(TEST_PROJECT, "org.jnode.fs.exfat");
        SortedSet<String> dependencies = new DependencyFinder().find(ANT_PROJECT, sourceDirectory, pluginInfo);

        softly.assertThat(dependencies).containsExactly(
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
}