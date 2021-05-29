package org.jnode.mavenizer;

import java.io.File;
import org.jnode.mavenizer.Directory.DestinationRoot;

import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.mavenizer.Project.allProjects;

public class RootPOMWriter extends AbstractPOMWriter {
    public RootPOMWriter(DestinationRoot destinationRoot) {
        super(destinationRoot);
    }

    public final File write() {
        File pomDirectory = destinationRoot.getDirectory();
        File file = write(pomDirectory, "root", "project", JNODE_VERSION, "pom");
        addModules(file, allProjects());
        return file;
    }
}
