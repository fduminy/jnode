package org.jnode.mavenizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static java.io.File.separator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.Conditions.extension;
import static org.jnode.mavenizer.FileFinder.Action.CONTINUE;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginPOMWriterTest.JNODE_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.THIRD_PARTY_PLUGIN_ID;
import static org.jnode.mavenizer.PluginPOMWriterTest.getPluginInfo;

public class SourceCopierTest extends AbstractTestWithDestinationRoot {
    @Test
    public void copy_jnode_plugin() {
        new SourceCopier(SRC_ROOT, destinationRoot).copy(getPluginInfo(JNODE_PLUGIN_ID));

        File pluginRoot = new File(getPluginRoot(JNODE_PLUGIN_ID));
        List<String> files = files(new File(pluginRoot, "src/main/java"));
        int nbJavaFiles = 40;
        assertThat(files).hasSize(nbJavaFiles).haveExactly(nbJavaFiles, extension("java"));
        assertThat(filter(files, "org/jnode/fs")).hasSize(19);
        assertThat(filter(files, "org/jnode/fs/service/def")).hasSize(12);
        assertThat(filter(files, "org/jnode/fs/spi")).hasSize(7);
        assertThat(filter(files, "org/jnode/fs/util")).hasSize(2);

        assertThat(files(new File(pluginRoot, "src/test/java")))
            .containsExactly("org/jnode/fs/service/def/FileSystemManagerTest.java");
    }

    @Test
    public void copy_third_party_plugin() {
        new SourceCopier(SRC_ROOT, destinationRoot).copy(getPluginInfo(THIRD_PARTY_PLUGIN_ID));
        File pluginRoot = new File(getPluginRoot(THIRD_PARTY_PLUGIN_ID));
        assertThat(pluginRoot).doesNotExist();
    }

    private List<String> filter(List<String> files, String parentPath) {
        List<String> result = new ArrayList<String>();
        for (String file : files) {
            if (file.substring(0, file.lastIndexOf(separator) + 1).equals(parentPath + separator)) {
                result.add(file);
            }
        }
        return result;
    }

    private static List<String> files(final File rootDir) {
        assertThat(rootDir).exists();
        final List<String> files = new ArrayList<String>();
        new FileFinder<List<String>>() {
            @Override
            protected Action processFile(File file) {
                String absolutePath = file.getAbsolutePath();
                files.add(absolutePath.substring(rootDir.getAbsolutePath().length() + 1));
                return CONTINUE;
            }
        }.iterate(rootDir);
        return files;
    }
}