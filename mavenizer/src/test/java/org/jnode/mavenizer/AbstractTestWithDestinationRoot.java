package org.jnode.mavenizer;

import java.io.IOException;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import static org.jnode.mavenizer.Conditions.getExpectedPath;
import static org.jnode.mavenizer.Directory.DestinationRoot.destinationRoot;
import static org.jnode.mavenizer.PluginPOMWriterTest.TEST_PROJECT;

abstract public class AbstractTestWithDestinationRoot {
    @Rule
    final public TemporaryFolder temporaryFolder = new TemporaryFolder();

    DestinationRoot destinationRoot;

    @Before
    public final void createDestinationRoot() throws IOException {
        temporaryFolder.create();
        destinationRoot = destinationRoot(temporaryFolder.getRoot().getAbsolutePath());
    }

    String getPluginRoot(String pluginId) {
        return getExpectedPath(destinationRoot, TEST_PROJECT, pluginId);
    }
}
