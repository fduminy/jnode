package org.jnode.mavenizer;

import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import static org.jnode.mavenizer.Directory.DestinationRoot.destinationRoot;
import static org.jnode.mavenizer.Directory.SourceRoot.sourceRoot;
import static org.jnode.mavenizer.FileFinder.delete;
import static org.jnode.mavenizer.Project.values;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Mavenizer {
    public static final String JNODE_HOME = "/home/fabien/projets/jnode/jnode2021";
    static final SourceRoot SRC_ROOT = sourceRoot(JNODE_HOME);
    protected static final String MAVEN_PLUGINS_DIR = "maven_plugins"; 
    protected static final String MAVEN_MIGRATION_DIR = "maven"; 
    
    // for faster process, use memory filesystem
    static final DestinationRoot DEST_ROOT = destinationRoot("/dev/shm/jnode_maven");
//    private static final DestinationRoot DEST_ROOT = destinationRoot(JNODE_HOME + "/jnode_maven");

    public static void main(String[] args) throws Exception {
        Log.debug("Migration from " + SRC_ROOT + " to " + DEST_ROOT);
        delete(DEST_ROOT.getDirectory());

        PluginInfoFinder pluginInfoFinder = new PluginInfoFinder(SRC_ROOT);
        PluginPOMWriter pluginPOMWriter = new PluginPOMWriter(DEST_ROOT);
        ProjectPOMWriter projectPOMWriter = new ProjectPOMWriter(SRC_ROOT, DEST_ROOT);
        PluginDescriptorCopier pluginDescriptorCopier = new PluginDescriptorCopier(SRC_ROOT, DEST_ROOT);
        SourceCopier sourceCopier = new SourceCopier(SRC_ROOT, DEST_ROOT);
        new RootPOMWriter(DEST_ROOT).write();
        PluginInfos pluginInfos = new PluginInfos();
        for (org.jnode.mavenizer.Project project : values()) {
            projectPOMWriter.write(project);
            for (PluginInfo pluginInfo : pluginInfoFinder.find(project)) {
                pluginInfos.add(pluginInfo);
            }
        }
        for (PluginInfo pluginInfo : pluginInfos.plugins()) {
            pluginPOMWriter.write(pluginInfos, pluginInfo);
            pluginDescriptorCopier.copy(pluginInfo);
            sourceCopier.copy(pluginInfo);
        }
    }
}
