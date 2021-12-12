package org.jnode.mavenizer;

import java.io.IOException;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import static java.nio.file.Paths.get;
import static java.util.Arrays.stream;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Directory.DestinationRoot.destinationRoot;
import static org.jnode.mavenizer.Directory.SourceRoot.sourceRoot;
import static org.jnode.mavenizer.Files.deleteAll;
import static org.jnode.mavenizer.Project.values;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Mavenizer {
    public static final String JNODE_HOME = "/home/fabien/projets/jnode/jnode2021";
    static final SourceRoot SRC_ROOT = sourceRoot(get(JNODE_HOME));
    protected static final String MAVEN_PLUGINS_DIR = "maven_plugins"; 
    protected static final String MAVEN_MIGRATION_DIR = "maven"; 
    
    // for faster process, use memory filesystem
    static final DestinationRoot DEST_ROOT = destinationRoot(get("/dev", "shm", "jnode_maven"));
//    private static final DestinationRoot DEST_ROOT = destinationRoot(get(JNODE_HOME, "/jnode_maven"));

    public static void main(String[] args) throws IOException {
        Log.debug("Migration from " + SRC_ROOT + " to " + DEST_ROOT);
        deleteAll(DEST_ROOT.getDirectory());

        new RootPOMWriter(DEST_ROOT).write();
        PluginInfos pluginInfos = findPlugins();
        writeProjectPOMs(pluginInfos);
        buildPluginProjects(pluginInfos);
    }

    private static void buildPluginProjects(PluginInfos pluginInfos) {
        DependencyFinder dependencyFinder = new DependencyFinder();
        MissingDependencyFinder missingDependencyFinder =
            new MissingDependencyFinder(ANT_PROJECT, pluginInfos, dependencyFinder, SRC_ROOT);
        PluginPOMWriter pluginPOMWriter = new PluginPOMWriter(ANT_PROJECT, DEST_ROOT, missingDependencyFinder);
        PluginDescriptorCopier pluginDescriptorCopier = new PluginDescriptorCopier(SRC_ROOT, DEST_ROOT);
        SourceCopier sourceCopier = new SourceCopier(ANT_PROJECT, SRC_ROOT, DEST_ROOT);
        pluginInfos.plugins().forEach(pluginInfo -> {
            pluginPOMWriter.write(pluginInfos, pluginInfo);
            pluginDescriptorCopier.copy(pluginInfo);
            sourceCopier.copy(pluginInfo);
        });
    }

    private static void writeProjectPOMs(PluginInfos pluginInfos) {
        ProjectPOMWriter projectPOMWriter = new ProjectPOMWriter(SRC_ROOT, DEST_ROOT, pluginInfos);
        stream(values()).forEach(projectPOMWriter::write);
    }

    private static PluginInfos findPlugins() {
        PluginInfoFinder pluginInfoFinder = new PluginInfoFinder(SRC_ROOT);
        PluginInfos pluginInfos = new PluginInfos();
        stream(values()).flatMap(project -> pluginInfoFinder.find(project).stream()).forEach(pluginInfos::add);
        return pluginInfos;
    }
}
