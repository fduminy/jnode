package org.jnode.mavenizer;


/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public interface MavenProjectVisitor {
    void visitMultiProject(MavenMultiProject project) throws Exception;

    void visitPluginProject(MavenPluginProject mavenPluginProject) throws Exception;
}
