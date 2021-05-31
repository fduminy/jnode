package org.jnode.mavenizer;

import java.nio.file.Path;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import static org.jnode.mavenizer.Conditions.getExpectedPath;
import static org.jnode.mavenizer.Directory.DestinationRoot.destinationRoot;

abstract public class AbstractTestWithDestinationRoot {
    @TempDir
    Path temporaryFolder;

    DestinationRoot destinationRoot;

    @BeforeEach
    public final void createDestinationRoot() {
        destinationRoot = destinationRoot(temporaryFolder.toAbsolutePath());
    }

    Path getPluginRoot(Project project, String pluginId) {
        return getExpectedPath(destinationRoot, project, pluginId);
    }
}
