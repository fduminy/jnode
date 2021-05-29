package org.jnode.mavenizer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import org.jnode.mavenizer.Directory.SourceRoot;

import static java.util.Arrays.asList;
import static org.jnode.mavenizer.Utils.isEmpty;

public class PluginInfoFinder {
    private final SourceRoot sourceRoot;

    public PluginInfoFinder(SourceRoot sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    List<PluginInfo> find(Project project) {
        List<PluginInfo> result = new ArrayList<PluginInfo>();
        for (File descriptorFile : getDescriptorFiles(project)) {
            result.add(new PluginInfo(project, descriptorFile));
        }
        return result;
    }

    private List<File> getDescriptorFiles(Project project) {
        List<File> result = new ArrayList<File>();
        File descriptorsDir = project.getDescriptorsDirectory(sourceRoot);
        File[] descriptorFiles = descriptorsDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".xml");
            }
        });

        if (!isEmpty(descriptorFiles)) {
            result.addAll(asList(descriptorFiles));
        }
        return result;
    }
}
