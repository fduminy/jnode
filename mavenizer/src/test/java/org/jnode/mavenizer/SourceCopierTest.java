package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.util.Collections.emptyList;
import static org.jnode.mavenizer.AbstractPOMWriterTest.getPluginInfo;
import static org.jnode.mavenizer.Conditions.extension;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriterTest.JNODE_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;
import static org.jnode.mavenizer.PluginPOMWriterTest.THIRD_PARTY_PLUGIN_ID;

@ExtendWith(SoftAssertionsExtension.class)
public class SourceCopierTest extends AbstractTestWithDestinationRoot {
    @Test
    public void copy_jnode_plugin(SoftAssertions softly) throws IOException {
        new SourceCopier(ANT_PROJECT, SRC_ROOT, destinationRoot).copy(getPluginInfo(TEST_PROJECT, JNODE_PLUGIN_ID));

        Path pluginRoot = getPluginRoot(TEST_PROJECT, JNODE_PLUGIN_ID);
        Path srcMainJava = pluginRoot.resolve(get("src", "main", "java"));
        Path srcTestJava = pluginRoot.resolve(get("src", "test", "java"));
        List<Path> files = files(softly, srcMainJava);
        int nbJavaFiles = 40;
        Path baseMainPackage = srcMainJava.resolve(get("org", "jnode", "fs"));
        Path baseTestPackage = srcTestJava.resolve(get("org", "jnode", "fs"));
        softly.assertThat(files).hasSize(nbJavaFiles).haveExactly(nbJavaFiles, extension("java"));
        softly.assertThat(filter(files, baseMainPackage)).hasSize(19);
        softly.assertThat(filter(files, baseMainPackage.resolve(get("service", "def")))).hasSize(12);
        softly.assertThat(filter(files, baseMainPackage.resolve("spi"))).hasSize(7);
        softly.assertThat(filter(files, baseMainPackage.resolve("util"))).hasSize(2);

        softly.assertThat(files(softly, srcTestJava))
            .containsExactly(baseTestPackage.resolve(get("service", "def", "FileSystemManagerTest.java")));
    }

    @Test
    public void copy_third_party_plugin(SoftAssertions softly) {
        new SourceCopier(ANT_PROJECT, SRC_ROOT, destinationRoot).copy(
            getPluginInfo(TEST_PROJECT, THIRD_PARTY_PLUGIN_ID));
        Path pluginRoot = getPluginRoot(TEST_PROJECT, THIRD_PARTY_PLUGIN_ID);
        softly.assertThat(pluginRoot).doesNotExist();
    }

    private List<Path> filter(List<Path> files, Path parentPath) {
        return files.stream().filter(path -> path.getParent().toString().equals(parentPath.toString()))
            .toList();
    }

    private static List<Path> files(SoftAssertions softly, final Path rootDir) throws IOException {
        softly.assertThat(rootDir).exists();
        if (!softly.wasSuccess()) {
            return emptyList();
        }
        return walk(rootDir).filter(Files::isRegularFile).toList();
    }
}