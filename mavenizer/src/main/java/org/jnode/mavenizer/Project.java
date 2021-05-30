package org.jnode.mavenizer;

import org.jnode.mavenizer.Directory.SourceRoot;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.jnode.mavenizer.Mavenizer.SRC_ROOT;
import static org.jnode.mavenizer.Utils.isEmpty;

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
                         
    private Project(String directory) {
        this.directory = directory;
    }

    public static List<String> allProjects() {
        List<String> projects = new ArrayList<String>();
        for (Project project : Project.values()) {
            projects.add(project.getDirectory());
        }
        return projects;
    }

    public final File getDescriptorsDirectory(SourceRoot root) {
        return new File(new File(root.getDirectory(), directory), "descriptors");
    }

    public final File[] getDescriptorFiles() {
        File[] descriptorFiles = getDescriptorsDirectory(SRC_ROOT).listFiles();
        return (descriptorFiles != null) ? descriptorFiles : new File[0];
    }

    public final File getRoot(Directory root) {
        return new File(root.getDirectory(), directory);
    }
        
    public final File getRootSourceDirectory(SourceRoot root) {
        return new File(getRoot(root), SOURCE_DIRECTORY);
    }

    public final File[] getSourceDirectories(SourceRoot root) {
        return getDirectoriesImpl(getRootSourceDirectory(root), TEST_DIRECTORY, getSpecialSourceDirectories());
    }
    
    public final File getTestDirectory(SourceRoot root) {
        return new File(getRootSourceDirectory(root), TEST_DIRECTORY);
    }
    
    protected String[] getSpecialSourceDirectories() {
        return new String[0];
    }
    
    protected String[] getExcludeDirectories() {
        return new String[0];
    }


    private File[] getDirectoriesImpl(File baseDir, final String excludeSubDir, String... specialDirs) {
        final List<String> specialDirectories = asList(specialDirs);
        final List<String> excludeDirectories = asList(getExcludeDirectories());
        
        List<File> result = new ArrayList<File>();
        
        File[] files = baseDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean exclude = false;
                if (excludeSubDir != null) {
                    exclude = pathname.getAbsolutePath().endsWith(excludeSubDir);
                }
                exclude |= excludeDirectories.contains(pathname.getName());
                
                return pathname.isDirectory() && !pathname.getName().startsWith(".") && !exclude;
            }
        });
        
        if (!isEmpty(files)) {
            for (File f : files) {
                if (specialDirectories.contains(f.getName())) {
                    result.addAll(asList(getDirectoriesImpl(f, null)));
                } else {
                    result.add(f);
                }
            }
        }
        
        return result.toArray(new File[0]);
    }

    public String getDirectory() {
        return directory;
    }
}
