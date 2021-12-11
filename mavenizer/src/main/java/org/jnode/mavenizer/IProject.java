package org.jnode.mavenizer;

import java.nio.file.Path;
import java.util.Collection;
import org.jnode.mavenizer.Directory.SourceRoot;

public interface IProject {
    String getDirectory();

    Path[] getSourceDirectories(SourceRoot sourceRoot);

    Path getTestDirectory(SourceRoot sourceRoot);

    Collection<Path> getDescriptorFiles();

    Collection<Path> getDescriptorFiles(SourceRoot sourceRoot);
}
