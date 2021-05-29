package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jnode.mavenizer.Project.allProjects;

public class RootPOMWriterTest extends AbstractPOMWriterTest {
    @Test
    public void write() throws IOException {
        RootPOMWriter writer = new RootPOMWriter(destinationRoot);

        File pomFile = writer.write();

        String projectDir = destinationRoot.getDirectory().getAbsolutePath();
        String pom = assertCommon(projectDir, "project", false,
            pomFile, "pom", "root");
        assertThat(extractModules(pom)).containsExactlyElementsOf(allProjects());
    }
}