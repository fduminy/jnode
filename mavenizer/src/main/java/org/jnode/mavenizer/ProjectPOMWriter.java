package org.jnode.mavenizer;

import java.nio.file.Path;
import java.util.List;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import static org.jnode.mavenizer.Constants.JNODE_VERSION;

public class ProjectPOMWriter extends AbstractPOMWriter {

    private final SourceRoot sourceRoot;
    private final PluginInfos pluginInfos;

    public ProjectPOMWriter(SourceRoot sourceRoot, DestinationRoot destinationRoot,
                            PluginInfos pluginInfos) {
        super(destinationRoot);
        this.sourceRoot = sourceRoot;
        this.pluginInfos = pluginInfos;
    }

    public final Path write(IProject project) {
        Path pomDirectory = destinationRoot.getDirectory().resolve(project.getDirectory());
        Path file = write(pomDirectory, project.getDirectory(), "project", JNODE_VERSION, "pom");
        addModules(file, getModules(project));
        return file;
    }

    private List<String> getModules(IProject project) {
        return project.getDescriptorFiles(sourceRoot).stream()
            .map(file -> findPlugin(file).getId())
            .toList();
    }

    private PluginInfo findPlugin(Path file) {
        PluginInfo result = null;
        for (PluginInfo pluginInfo : pluginInfos.plugins()) {
            if (pluginInfo.getDescriptorFile().equals(file)) {
                result = pluginInfo;
                break;
            }
        }
        return result;
    }
}
