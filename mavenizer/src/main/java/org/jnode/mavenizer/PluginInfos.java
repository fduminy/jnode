package org.jnode.mavenizer;

import java.util.HashMap;
import java.util.Map;

public class PluginInfos {
    private final Map<String, PluginInfo> idToPlugin = new HashMap<String, PluginInfo>();

    final PluginInfo getPlugin(String id) {
        return idToPlugin.get(id);
    }

    public void add(PluginInfo pluginInfo) {
        idToPlugin.put(pluginInfo.getId(), pluginInfo);
    }

    public Iterable<? extends PluginInfo> plugins() {
        return idToPlugin.values();
    }
}
