package org.jnode.mavenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        Collection<String> exportedPackages = exportedPackages(getExports(project, start, pluginInfo));
        try {
            return walk(start).parallel()
                .filter(this::javaFile)
                .filter(path -> isExported(exportedPackages, start.relativize(path).getParent().toString()))
                .map(javaFile -> findInJavaFile(exportedPackages, javaFile))
                .reduce(Collections.synchronizedSortedSet(new TreeSet<>()), (a, b) -> {
                    a.addAll(b);
                    return a;
                });
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private Collection<String> exportedPackages(List<Export> exports) {
        List<String> exportedPackages = new ArrayList<>();
        for (Export export : exports) {
            for (String packageFilter : export.getPackageFilters()) {
                exportedPackages.add(removeDotStarFilterAtTheEndOf(packageFilter));
            }
        }
        return exportedPackages;
    }

    private boolean isExported(Collection<String> exportedPackages, String relativePathToPackage) {
        for (String exportedPackage : exportedPackages) {
            String packageFilter = exportedPackage.replace(".", separator);
            if (relativePathToPackage.equals(packageFilter)) {
                return true;
            }
        }
        return false;
    }

    private SortedSet<String> findInJavaFile(Collection<String> exportedPackages, Path javaFile) {
        return javaFileToDependencies.computeIfAbsent(javaFile, file -> {
            SortedSet<String> dependencies = new TreeSet<>();
            try (BufferedReader reader = newBufferedReader(javaFile)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(" class ")) {
                        break;
                    }
                    String packageName = extractPackage(line);
                    if ((packageName != null) && !exportedPackages.contains(packageName)) {
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
