package org.jnode.mavenizer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.EnumSet.allOf;
import static org.jnode.mavenizer.Mavenizer.JNODE_HOME;
import static org.jnode.mavenizer.Project.Builder;
import static org.jnode.mavenizer.Project.Sound;

//TODO integrate this class in actual (ant based) JNode build system ?
@SuppressWarnings({"java:S106", "java:S1604", "java:S3012", "java:S112", "java:S1075", "java:S2093",
    "TryFinallyCanBeTryWithResources", "Java8MapApi"})
public class CheckMissingClasses {
    private static final File PLUGIN_DIR = new File(JNODE_HOME, "all/build/plugins");
    private static final EnumSet<Project> PROJECTS;

    static {
        PROJECTS = allOf(Project.class);
        PROJECTS.remove(Builder);
        PROJECTS.remove(Sound);
    }

    private static final ClassFilter CLASS_FILTER = new ClassFilter() {
        @Override
        public boolean accept(String className) {
            return !className.endsWith(".html") && // "package.html"
                !className.endsWith(".xml") && // org/jnode/net/ipv4/bootp/example.xml
                !className.endsWith(".template") && // mmtk template classes
                !className.startsWith("org/jnode/emu/") && // Emu classes
                !className.startsWith("org/jnode/apps/vmware/") && // vmware classes

                // FIXME these classes should be in which plugin ?
                !Arrays.asList("com/sun/java/util/jar/pack/NativeNativeUnpack.class",
                    "com/sun/java/util/jar/pack/Pack200Command.class",
                    "com/sun/java/util/jar/pack/Unpack200Command.class").contains(className) &&

                // tests built from templates
                !className.endsWith(".jtemplate") &&
                !className.matches("org/jnode/test/Primitive.*Test\\.class");
        }
    };

    public static void main(String[] args) throws IOException {
        SortedSet<String> pluginClasses = new TreeSet<>();
        for (File file : getPluginJars()) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.isDirectory()) {
                        continue;
                    }
                    String className = jarEntry.getName();
                    if (CLASS_FILTER.accept(className)) {
                        pluginClasses.add(className);
                    }
                }
            } finally {
                if (jarFile != null) {
                    jarFile.close();
                }
            }
        }
        if (pluginClasses.isEmpty()) {
            throw new RuntimeException("no class found in " + PLUGIN_DIR);
        }

        for (Project project : PROJECTS) {
            SortedMap<String, SortedSet<String>> missingClasses = new TreeMap<>();
            int missingClassesCount = 0;
            File directory = new File(JNODE_HOME, project.getDirectory());
            directory = new File(directory, "build/classes").getAbsoluteFile();
            List<File> files = new ArrayList<>();
            listFiles(directory, files);
            for (File file : files) {
                String className = file.getAbsolutePath().substring(directory.getAbsolutePath().length() + 1);
                if (!pluginClasses.contains(className) && CLASS_FILTER.accept(className)) {
                    String packageName = className.substring(0, className.lastIndexOf('/'));
                    SortedSet<String> classes = missingClasses.get(packageName);
                    if (classes == null) {
                        classes = new TreeSet<>();
                        missingClasses.put(packageName, classes);
                    }
                    classes.add(className);
                    missingClassesCount++;
                }
            }
            if (!missingClasses.isEmpty()) {
                System.out.println(missingClassesCount + " missing classes for " + project + ":");
                for (Entry<String, SortedSet<String>> entry : missingClasses.entrySet()) {
                    System.out.println("\t" + entry.getKey().replace('/', '.') + " : " + entry.getValue());
                }

            }
        }
    }

    private static void listFiles(File directory, List<File> result) {
        File[] files = directory.listFiles();
        if ((files == null) || (files.length == 0)) {
            throw new RuntimeException("directory " + directory.getAbsolutePath() + " not built");
        }

        for (File file : files) {
            if (file.isDirectory()) {
                listFiles(file, result);
                continue;
            }
            result.add(file);
        }
    }

    private static File[] getPluginJars() {
        File[] pluginJars = PLUGIN_DIR.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".jar");
            }
        });
        if ((pluginJars == null) || (pluginJars.length == 0)) {
            throw new RuntimeException("plugin jars not built");
        }
        return pluginJars;
    }

    private static interface ClassFilter {
        boolean accept(String className);
    }
}
