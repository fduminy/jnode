package org.jnode.mavenizer;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jnode.plugin.PluginPrerequisite;

import static java.util.Arrays.stream;

class CircularDependencyFilter {
    private final PluginInfos pluginInfos;
    
    public CircularDependencyFilter(PluginInfos pluginInfos) {
        this.pluginInfos = pluginInfos;
    }

    /**
     * Filter circular dependencies by replacing them by a dependency to jnode-api
     */
    void filterCircularDependencies(PluginInfo currentPlugin) {
        SortedSet<String> requiredPlugins = stream(currentPlugin.getPluginDescriptor().getPrerequisites()).map(
            desc -> desc.getPluginReference().getId()).collect(Collectors.toCollection(TreeSet::new));
        SortedSet<String> filteredDependencies = new TreeSet<>(requiredPlugins);

//        // Liste des packages d'interfaces qui seront déplacés vers jnode-api
//        Set<String> apiPackages = Set.of(
//            "org.jnode.driver.video", // Interfaces pour les drivers vidéo
//            "org.jnode.driver.bus", // Interfaces bus système
//            "org.jnode.plugin", // Interfaces plugin registry
//            "org.jnode.driver", // Interfaces drivers génériques
//            "org.jnode.system.resource", // Interfaces ressources système
//            "org.jnode.vm", // Interfaces VM de base
//            "org.jnode.fs", // Interfaces système de fichiers
//            "org.jnode.naming", // Interfaces naming
//            "org.jnode.bootlog" // Interfaces bootlog
//        );

        // Remove dependencies creating cycles and that can be satisfied by jnode-api
        String currentPluginId = currentPlugin.getId();
        for (String requiredPlugin : new TreeSet<>(requiredPlugins)) {
            if (wouldCreateCircularDependency(currentPluginId, requiredPlugin)) {
                Log.debug("Circular dependency detected between " + currentPluginId + " and " + requiredPlugin +
                    " - can be resolved by jnode-api");
                filteredDependencies.remove(requiredPlugin);
            }
        }

        currentPlugin.getPluginDescriptor().setPrerequisites(stream(currentPlugin.getPluginDescriptor().getPrerequisites())
            .filter(desc -> filteredDependencies.contains(desc.getPluginReference().getId()))
            .toArray(PluginPrerequisite[]::new));
    }

    /**
     * Detect if the requiredPlugin would create a cycle.
     */
    private boolean wouldCreateCircularDependency(String currentPlugin, String requiredPlugin) {
        // check if the required plugin depends already on current plugin
        PluginInfo requiredPluginInfo = pluginInfos.getPlugin(requiredPlugin);
        if (requiredPluginInfo == null) return false;

        return stream(requiredPluginInfo.getPluginDescriptor().getPrerequisites())
            .anyMatch(prereq -> prereq.getPluginReference().getId().equals(currentPlugin));
    }
}
