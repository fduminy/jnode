package org.jnode.mavenizer;

import java.io.File;
import java.util.ArrayList;
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

    public final File write(Project project) {
        File pomDirectory = new File(destinationRoot.getDirectory(), project.getDirectory());
        File file = write(pomDirectory, project.getDirectory(), "project", JNODE_VERSION, "pom");
        addModules(file, getModules(project));
        return file;
    }

    private List<String> getModules(Project project) {
        List<String> modules = new ArrayList<String>();
        File[] descriptors = project.getDescriptorsDirectory(sourceRoot).listFiles();
        if ((descriptors != null) && (descriptors.length > 0)) {
            for (File file : descriptors) {
                modules.add(findPlugin(file).getId());
            }
        }
        return modules;
    }

    private PluginInfo findPlugin(File file) {
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
