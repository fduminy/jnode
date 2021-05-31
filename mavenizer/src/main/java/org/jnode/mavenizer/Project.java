package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.jnode.mavenizer.Directory.SourceRoot;

import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walk;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public enum Project {
    Builder("builder") {
        protected String[] getExcludeDirectories() {
            return new String[]{Mavenizer.MAVEN_MIGRATION_DIR, Mavenizer.MAVEN_PLUGINS_DIR};
        }        
    },

    Core("core") {
        public String[] getSpecialSourceDirectories() {
            return new String[] {"classpath", "openjdk"};
        }
    },

    Distr("distr") {
    },

    FS("fs") {
    },

    GUI("gui") {
    },

    Net("net") {
    },

    Shell("shell") {
    },

    Sound("sound") {
    },

    TextUI("textui") {
    };

    private static final String SOURCE_DIRECTORY = "src";
    private static final String TEST_DIRECTORY = "test";
    
    private final String directory;
                         
    Project(String directory) {
        this.directory = directory;
    }

    public static List<String> allProjects() {
        return stream(Project.values()).map(Project::getDirectory).toList();
    }

    public final Path getDescriptorsDirectory(SourceRoot root) {
        return root.getDirectory().resolve(directory).resolve("descriptors");
    }

    public final Collection<Path> getDescriptorFiles() {
        return getDescriptorFiles(SRC_ROOT);
    }

    public final Collection<Path> getDescriptorFiles(SourceRoot sourceRoot) {
        File[] descriptorFiles = getDescriptorsDirectory(sourceRoot).toFile().listFiles();
        return (descriptorFiles != null) ?
            stream(descriptorFiles).map(File::toPath).toList() :
            emptySet();
    }

    public final Path getRoot(Directory root) {
        return root.getDirectory().resolve(directory);
    }
        
    public final Path getRootSourceDirectory(SourceRoot root) {
        return getRoot(root).resolve(SOURCE_DIRECTORY);
    }

    public final Path[] getSourceDirectories(SourceRoot root) {
        return getDirectoriesImpl(getRootSourceDirectory(root), TEST_DIRECTORY, getSpecialSourceDirectories());
    }
    
    public final Path getTestDirectory(SourceRoot root) {
        return getRootSourceDirectory(root).resolve(TEST_DIRECTORY);
    }
    
    protected String[] getSpecialSourceDirectories() {
        return new String[0];
    }
    
    protected String[] getExcludeDirectories() {
        return new String[0];
    }


    private Path[] getDirectoriesImpl(Path baseDir, final String excludeSubDir, String... specialDirs) {
        final List<String> specialDirectories = asList(specialDirs);
        final List<String> excludeDirectories = asList(getExcludeDirectories());
        
        List<Path> result = new ArrayList<>();

        try {
            walk(baseDir, 1)
                .filter(path -> {
                    boolean exclude = false;
                    if (excludeSubDir != null) {
                        exclude = path.toAbsolutePath().endsWith(excludeSubDir);
                    }
                    exclude |= excludeDirectories.contains(path.getFileName().toString());

                    return isDirectory(path) && !path.getFileName().toString().startsWith(".") && !exclude;
                })
                .forEach(path -> {
                    if (specialDirectories.contains(path.getFileName().toString())) {
                        result.addAll(asList(getDirectoriesImpl(path, null)));
                    } else {
                        result.add(path);
                    }
                });
        } catch (IOException e) {
            throw new BuildException(e);
        }

        return result.toArray(new Path[0]);
    }

    public String getDirectory() {
        return directory;
    }
}
