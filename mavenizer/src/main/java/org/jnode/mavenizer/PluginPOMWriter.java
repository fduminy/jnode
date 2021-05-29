package org.jnode.mavenizer;

import java.io.File;
import org.jnode.mavenizer.Directory.DestinationRoot;

import static org.jnode.mavenizer.Utils.getPluginHome;

public class PluginPOMWriter extends AbstractPOMWriter {
    public PluginPOMWriter(DestinationRoot destinationRoot) {
        super(destinationRoot);
    }

    public final File write(PluginInfo pluginInfo) {
        File pomDirectory = getPluginHome(destinationRoot, pluginInfo);
        return write(pomDirectory, pluginInfo.getProject().getDirectory(),
            pluginInfo.getId(), pluginInfo.getVersion(), "jar");
    }
}
