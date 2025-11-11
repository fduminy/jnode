package org.jnode.mavenizer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.function.Predicate;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import static java.nio.file.Files.readAllLines;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.jnode.mavenizer.Constants.ANT_PROJECT;
import static org.jnode.mavenizer.Directory.DestinationRoot.destinationRoot;
import static org.jnode.mavenizer.Directory.SourceRoot.sourceRoot;
import static org.jnode.mavenizer.Files.deleteAll;
import static org.jnode.mavenizer.Project.allProjects;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class Mavenizer {
    public static final String JNODE_HOME = ".";
    static final SourceRoot SRC_ROOT = sourceRoot(get(JNODE_HOME));
    protected static final String MAVEN_PLUGINS_DIR = "maven_plugins";
    protected static final String MAVEN_MIGRATION_DIR = "maven";

    static final DestinationRoot DEST_ROOT = destinationRoot(get("..", "jnode_mavenized"));
    static final Predicate<Project> PROJECT_FILTER = other -> true;
    static final Predicate<PluginInfo> PLUGIN_FILTER = pluginInfo -> true;

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
        PluginPOMWriter pluginPOMWriter = new PluginPOMWriter(ANT_PROJECT, DEST_ROOT);
        PluginDescriptorCopier pluginDescriptorCopier = new PluginDescriptorCopier(SRC_ROOT, DEST_ROOT);
        SourceCopier sourceCopier = new SourceCopier(ANT_PROJECT, SRC_ROOT, DEST_ROOT);
        pluginInfos.plugins().stream().filter(PLUGIN_FILTER).forEach(pluginInfo -> {
            addMissingDependencies(pluginInfo, missingDependencyFinder.findMissingDependencies(pluginInfo));
            pluginPOMWriter.write(pluginInfos, pluginInfo);
            pluginDescriptorCopier.copy(pluginInfo);
            sourceCopier.copy(pluginInfo);
        });
    }

    private static void addMissingDependencies(PluginInfo pluginInfo, SortedSet<String> missingDependencies) {
        if (!pluginInfo.isThirdParty()) {
            Properties thirdPartyArtifacts = new Properties();
            try {
                thirdPartyArtifacts.load(PluginPOMWriter.class.getResourceAsStream("third_party_artifacts.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            missingDependencies.clear();

            String property = thirdPartyArtifacts.getProperty("missing.dependencies." + pluginInfo.getId(), "");
            if (property != null && !property.trim().isEmpty()) {
                String[] deps = property.split(",");
                missingDependencies.addAll(asList(deps));
                missingDependencies
                    .forEach(missingDependency -> {
                        Log.debug("Adding missing dependency " + pluginInfo.getId() + "->" + missingDependency );
                        pluginInfo.getPluginDescriptor().addPrerequisite(missingDependency);
                    });
            }
        }
    }

    private static void writeProjectPOMs(PluginInfos pluginInfos) {
        ProjectPOMWriter projectPOMWriter = new ProjectPOMWriter(SRC_ROOT, DEST_ROOT, pluginInfos);
        allProjects().forEach(projectPOMWriter::write);
    }

    private static PluginInfos findPlugins() {
        PluginInfoFinder pluginInfoFinder = new PluginInfoFinder(SRC_ROOT);
        PluginInfos pluginInfos = new PluginInfos();
        allProjects().flatMap(project -> pluginInfoFinder.find(project).stream()).forEach(pluginInfos::add);
        return pluginInfos;
    }
}
