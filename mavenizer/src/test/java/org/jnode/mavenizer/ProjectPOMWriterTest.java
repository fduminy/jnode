package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.PluginInfoFinderTest.NB_TEST_PROJECT_MODULES;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;

public class ProjectPOMWriterTest extends AbstractPOMWriterTest {
    @Test
    public void write() throws IOException {
        ProjectPOMWriter writer = new ProjectPOMWriter(SRC_ROOT, destinationRoot);

        File pomFile = writer.write(TEST_PROJECT);

        String projectDir = new File(destinationRoot.getDirectory(), TEST_PROJECT.getDirectory()).getAbsolutePath();
        String pom = assertCommon(projectDir, "project", false, pomFile, "pom", TEST_PROJECT.getDirectory());
        assertThat(extractModules(pom)).doesNotHaveDuplicates().hasSize(NB_TEST_PROJECT_MODULES);
    }
}