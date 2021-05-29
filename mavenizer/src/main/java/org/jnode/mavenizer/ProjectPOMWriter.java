package org.jnode.mavenizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import static org.jnode.mavenizer.Constants.JNODE_VERSION;

public class ProjectPOMWriter extends AbstractPOMWriter {

    private final SourceRoot sourceRoot;

    public ProjectPOMWriter(SourceRoot sourceRoot, DestinationRoot destinationRoot) {
        super(destinationRoot);
        this.sourceRoot = sourceRoot;
    }

    public final File write(Project project) {
        File pomDirectory = new File(destinationRoot.getDirectory(), project.getDirectory());
        File file = write(pomDirectory, project.getDirectory(), "project", JNODE_VERSION, "pom");
        addModules(file, getModules(project));
        return file;
    }

    private List<String> getModules(Project project) {
        List<String> modules = new ArrayList<String>();
        File[] descriptors = project.getDescriptorsDirectory(sourceRoot).listFiles();
        if ((descriptors != null) && (descriptors.length > 0)) {
            for (File file : descriptors) {
                String name = file.getName();
                modules.add(name.substring(0, name.lastIndexOf('.')));
            }
        }
        return modules;
    }
}
