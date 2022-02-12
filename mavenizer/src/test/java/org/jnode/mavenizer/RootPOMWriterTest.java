package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.jnode.mavenizer.Project.allProjectDirectories;

public class RootPOMWriterTest extends AbstractPOMWriterTest {
    @Test
    public void write() throws IOException {
        RootPOMWriter writer = new RootPOMWriter(destinationRoot);

        Path pomFile = writer.write();

        Path projectDir = destinationRoot.getDirectory().toAbsolutePath();
        String pom = assertCommon(projectDir, "project", false,
            pomFile, "pom", "root");
        softly.assertThat(extractModules(pom)).containsExactlyElementsOf(allProjectDirectories());
        softly.assertThat(extractDependencies(pom)).isEmpty();
    }
}