package org.jnode.mavenizer;

import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import java.io.File;
import java.util.Stack;

import static java.lang.String.format;


/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public abstract class MavenProject {
    private final SourceRoot sourceRoot;
    private final DestinationRoot destinationRoot;
    private MavenMultiProject parent;
    private String name;

    public MavenProject(MavenMultiProject parent, SourceRoot sourceRoot, DestinationRoot destinationRoot, String name) {
        super();
        this.sourceRoot = sourceRoot;
        this.destinationRoot = destinationRoot;
        init(parent, name);
    }
    
    public final void init(MavenMultiProject parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public final MavenProject getParent() {
        return parent;
    }

    public final String getName() {
        return name;
    }

    public final SourceRoot getSrcRoot() {
        return getRoot().sourceRoot;
    }

    public final File getBaseDirectory() {
        MavenProject currentProject = this;
        Stack<String> stack = new Stack<String>();
        while (currentProject != null) {
            stack.push(currentProject.getName());
            currentProject = currentProject.getParent();
        }
        StringBuilder path = new StringBuilder();
        while (!stack.isEmpty()) {
            path.append('/').append(stack.pop());
        }
        return new File(getRoot().destinationRoot.getDirectory(), path.toString());
    }

    public DestinationRoot getDestinationRoot() {
        return destinationRoot;
    }

    @Override
    public String toString() {
        return format("%s(%s)", getClass().getSimpleName(), getName());
    }

    public abstract void accept(MavenProjectVisitor visitor) throws Exception;

    final MavenProject getRoot() {
        MavenProject project = this;
        while (project.getParent() != null) {
            project = project.getParent();
        }
        return project;
    }
}
