package org.jnode.mavenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class PluginMavenizer extends Task {
    private final MavenPluginProject mavenPluginProject;
    
    public PluginMavenizer(MavenPluginProject mavenPluginProject) {
        this.mavenPluginProject = mavenPluginProject;
        
        setTaskName("PluginMavenizer");
        setDescription("maven a jnode plugin");
    }

    @Override
    public void execute() throws BuildException {
        SourceRoot srcRoot = mavenPluginProject.getSrcRoot();
        PluginInfo pluginInfo = mavenPluginProject.getPluginInfo();
        DestinationRoot destinationRoot = mavenPluginProject.getRoot().getDestinationRoot();

        new PluginPOMWriter(destinationRoot).write(pluginInfo);
        new PluginDescriptorCopier(srcRoot, destinationRoot).copy(pluginInfo);
        new SourceCopier(srcRoot, destinationRoot).copy(pluginInfo);
    }
}
