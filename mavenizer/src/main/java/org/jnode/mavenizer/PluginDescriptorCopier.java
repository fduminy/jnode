package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;
import static org.jnode.mavenizer.SourceFileType.RESOURCES;
import static org.jnode.mavenizer.Utils.getPluginHome;

public record PluginDescriptorCopier(SourceRoot sourceRoot, DestinationRoot destinationRoot) {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void copy(PluginInfo pluginInfo) {
        Path pluginHome = getPluginHome(destinationRoot, pluginInfo);
        try {
            createDirectories(pluginHome);
        } catch (IOException e) {
            throw new BuildException(e);
        }

        pluginInfo.getProject().getSourceDirectories(sourceRoot);
        Copy c = new Copy();
        c.setProject(new org.apache.tools.ant.Project());
        c.setFile(pluginInfo.getDescriptorFile().toFile());
        Path srcMainResources = pluginHome.resolve(get("src", "main", RESOURCES.getMavenDirectory()));
        c.setTofile(srcMainResources.resolve("plugin.xml").toFile());
        c.execute();
    }
}
