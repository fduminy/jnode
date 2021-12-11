package org.jnode.mavenizer;

import java.nio.file.Path;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.jnode.mavenizer.AbstractPOMWriterTest.getPluginInfo;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.Project.Core;

@ExtendWith(SoftAssertionsExtension.class)
class UtilsTest {
    @Test
    void getExports(SoftAssertions softly) {
        Path projectSourceDirectory = Core.getSourceDirectories(SRC_ROOT)[0];
        PluginInfo pluginInfo = getPluginInfo(Core, "org.jnode.security");

        List<Export> exports = Utils.getExports(ANT_PROJECT, projectSourceDirectory, pluginInfo);

        softly.assertThat(exports).hasSize(1);
        if (softly.wasSuccess()) {
            Export export = exports.get(0);
            softly.assertThat(export.getSourceDirectory()).hasToString(projectSourceDirectory.toAbsolutePath().toString());
            softly.assertThat(export.getPackageFilters()).containsExactly("org.jnode.security.*", "security.*"); // sorted values
        }
    }
}