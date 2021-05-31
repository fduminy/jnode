package org.jnode.mavenizer;

import java.nio.file.Path;
import org.jnode.mavenizer.Directory.DestinationRoot;

import static org.jnode.mavenizer.Constants.JNODE_VERSION;
import static org.jnode.mavenizer.Project.allProjects;

public class RootPOMWriter extends AbstractPOMWriter {
    public RootPOMWriter(DestinationRoot destinationRoot) {
        super(destinationRoot);
    }

    public final Path write() {
        Path pomDirectory = destinationRoot.getDirectory();
        Path file = write(pomDirectory, "root", "project", JNODE_VERSION, "pom");
        addModules(file, allProjects());
        return file;
    }
}
