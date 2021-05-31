package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.jnode.mavenizer.Directory.SourceRoot;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.walk;
import static java.util.Collections.emptyList;

public record PluginInfoFinder(SourceRoot sourceRoot) {
    List<PluginInfo> find(Project project) {
        return getDescriptorFiles(project).stream().map(descriptorFile -> new PluginInfo(project, descriptorFile))
            .toList();
    }

    private List<Path> getDescriptorFiles(Project project) {
        Path descriptorsDir = project.getDescriptorsDirectory(sourceRoot);
        if (!exists(descriptorsDir)) {
            return emptyList();
        }
        
        try {
            return walk(descriptorsDir, 1)
                .filter(path -> isRegularFile(path) && path.getFileName().toString().endsWith(".xml"))
                .toList();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
