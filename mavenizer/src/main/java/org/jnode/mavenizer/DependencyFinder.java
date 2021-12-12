package org.jnode.mavenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;

import static java.io.File.separator;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.walk;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.unmodifiableSortedSet;
import static org.jnode.mavenizer.Utils.getExports;
import static org.jnode.mavenizer.Utils.removeDotStarFilterAtTheEndOf;

public class DependencyFinder {
    private static final String BEGIN_PACKAGE = "import";
    private static final String END_PACKAGE = ";";

    private final Map<Path, SortedSet<String>> javaFileToDependencies = synchronizedMap(new TreeMap<>());

    SortedSet<String> find(IAntProject project, Path start, PluginInfo pluginInfo) {
        List<Export> exports = getExports(project, start, pluginInfo);
        try {
            return walk(start).parallel()
                .filter(this::javaFile)
                .filter(path -> isExported(exports, start.relativize(path).getParent().toString()))
                .map(this::findInJavaFile)
                .reduce(new TreeSet<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private boolean isExported(List<Export> exports, String relativePathToPackage) {
        for (Export export : exports) {
            for (String packageFilter : export.getPackageFilters()) {
                packageFilter = removeDotStarFilterAtTheEndOf(packageFilter);
                packageFilter = packageFilter.replace(".", separator);
                if (relativePathToPackage.equals(packageFilter)) {
                    return true;
                }
            }
        }
        return false;
    }

    private SortedSet<String> findInJavaFile(Path javaFile) {
        return javaFileToDependencies.computeIfAbsent(javaFile, file -> {
            SortedSet<String> dependencies = new TreeSet<>();
            try (BufferedReader reader = newBufferedReader(javaFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(" class ")) {
                        break;
                    }
                    String packageName = extractPackage(line);
                    if (packageName != null) {
                        dependencies.add(packageName);
                    }
                }
            } catch (IOException e) {
                throw new BuildException(e);
            }
            return unmodifiableSortedSet(dependencies);
        });
    }

    private boolean javaFile(Path path) {
        return isRegularFile(path) && path.getFileName().toString().endsWith(".java");
    }

    private String extractPackage(String line) {
        String className = null;
        int beginPackage = line.indexOf(BEGIN_PACKAGE);
        if (beginPackage >= 0) {
            beginPackage += BEGIN_PACKAGE.length();
            int endPackage = line.indexOf(END_PACKAGE, beginPackage);
            if (endPackage >= 0) {
                className = line.substring(beginPackage, endPackage).trim();
            }
        }
        return (className == null) ? null : className.substring(0, className.lastIndexOf('.'));
    }
}
