package org.jnode.mavenizer;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.jnode.mavenizer.Directory.SourceRoot;

import static java.util.Arrays.stream;
import static org.jnode.mavenizer.Utils.getExports;

public class MissingDependencyFinder {
    private final IAntProject project;
    private final PluginInfos pluginInfos;
    private final DependencyFinder dependencyFinder;
    private final SourceRoot srcRoot;

    public MissingDependencyFinder(IAntProject project, PluginInfos pluginInfos, DependencyFinder dependencyFinder,
                                   SourceRoot srcRoot) {
        this.project = project;
        this.pluginInfos = pluginInfos;
        this.dependencyFinder = dependencyFinder;
        this.srcRoot = srcRoot;
    }

    SortedSet<String> findMissingDependencies(PluginInfo plugin) {
        SortedSet<String> importedPackages = getImportedPackages(plugin);
        SortedSet<String> requiredPlugins = getRequiredPlugins(importedPackages);

        stream(plugin.getPluginDescriptor().getPrerequisites())
            .map(required -> required.getPluginReference().getId())
            .forEach(requiredPlugins::remove);

        return requiredPlugins;
    }

    private SortedSet<String> getImportedPackages(PluginInfo pluginInfo) {
        SortedSet<String> importedPackages = new TreeSet<>();
        for (Path srcDir : pluginInfo.getProject().getSourceDirectories(srcRoot)) {
            importedPackages.addAll(dependencyFinder.find(project, srcDir, pluginInfo));
        }
        return importedPackages;
    }

    private SortedSet<String> getRequiredPlugins(SortedSet<String> importedPackages) {
        SortedSet<String> requiredPlugins = new TreeSet<>();
        pluginInfos.plugins().forEach(plugin -> getProvidedPackages(plugin)
            .forEach(providedPackage -> {
                if (importedPackages.remove(providedPackage)) {
                    requiredPlugins.add(plugin.getId());
                }
            }));
        return requiredPlugins;
    }

    private Set<String> getProvidedPackages(PluginInfo pluginInfo) {
        Set<String> providedPackages = new LinkedHashSet<>();
        for (Path srcDir : pluginInfo.getProject().getSourceDirectories(srcRoot)) {
            List<String> exportedPackages = getExports(project, srcDir, pluginInfo).stream()
                .flatMap(export -> export.getPackageFilters().stream()
                    .map(Utils::removeDotStarFilterAtTheEndOf))
                .toList();
            providedPackages.addAll(exportedPackages);
        }
        return providedPackages;
    }
}
