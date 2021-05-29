package org.jnode.mavenizer;

import java.io.File;
import org.apache.tools.ant.taskdefs.Copy;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import static org.jnode.mavenizer.SourceFileType.RESOURCES;
import static org.jnode.mavenizer.Utils.getPluginHome;

public class PluginDescriptorCopier {
    private final SourceRoot sourceRoot;
    private final DestinationRoot destinationRoot;

    public PluginDescriptorCopier(SourceRoot sourceRoot, DestinationRoot destinationRoot) {
        this.sourceRoot = sourceRoot;
        this.destinationRoot = destinationRoot;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void copy(PluginInfo pluginInfo) {
        File pluginHome = getPluginHome(destinationRoot, pluginInfo);
        pluginHome.mkdirs();

        pluginInfo.getProject().getSourceDirectories(sourceRoot);
        Copy c = new Copy();
        c.setProject(new org.apache.tools.ant.Project());
        c.setFile(pluginInfo.getDescriptorFile());
        File srcMainResources = new File(pluginHome, "src/main/" + RESOURCES.getMavenDirectory());
        c.setTofile(new File(srcMainResources, "plugin.xml"));
        c.execute();
    }
}
