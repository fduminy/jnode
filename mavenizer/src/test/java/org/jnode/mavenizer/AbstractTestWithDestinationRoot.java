package org.jnode.mavenizer;

import java.nio.file.Path;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.junit.jupiter.api.BeforeEach;

import static org.jnode.mavenizer.Conditions.getExpectedPath;
import static org.jnode.mavenizer.Directory.DestinationRoot.destinationRoot;

abstract public class AbstractTestWithDestinationRoot extends AbstractTestWithTemporaryFolder {
    DestinationRoot destinationRoot;

    @BeforeEach
    public final void createDestinationRoot() {
        destinationRoot = destinationRoot(temporaryFolder.toAbsolutePath());
    }

    final Path getPluginRoot(IProject project, String pluginId) {
        return getExpectedPath(destinationRoot, project, pluginId);
    }
}
