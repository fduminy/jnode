package org.jnode.mavenizer;

import java.io.File;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginReference;

import static org.apache.bsf.util.StringUtils.lineSeparator;
import static org.jnode.mavenizer.POMBuilder.getVersion;
import static org.jnode.mavenizer.Utils.getPluginHome;

public class PluginPOMWriter extends AbstractPOMWriter {
    static final String DEPENDENCIES_BEGIN = "<dependencies>";
    static final String DEPENDENCIES_END = "</dependencies>";
    static final String DEPENDENCY_BEGIN = "<dependency>";
    static final String DEPENDENCY_END = "</dependency>";

    public PluginPOMWriter(DestinationRoot destinationRoot) {
        super(destinationRoot);
    }

    public final File write(PluginInfos pluginInfos, PluginInfo pluginInfo) {
        File pomDirectory = getPluginHome(destinationRoot, pluginInfo);
        File file = write(pomDirectory, pluginInfo.getProjectId(),
            pluginInfo.getId(), pluginInfo.getVersion(), "jar");
        if (!pluginInfo.isThirdParty()) {
            addDependencies(file, pluginInfos, pluginInfo);
        }
        return file;
    }

    private void addDependencies(File pomFile, PluginInfos pluginInfos, PluginInfo pluginInfo) {
        StringBuilder xml = new StringBuilder(INDENT).append(DEPENDENCIES_BEGIN).append(lineSeparator);
        for (PluginPrerequisite dependency : pluginInfo.getPluginDescriptor().getPrerequisites()) {
            PluginReference reference = dependency.getPluginReference();
            indent(xml).append(DEPENDENCY_BEGIN).append(lineSeparator);
            appendValue(xml,"groupId", "org.jnode." + getProjectId(pluginInfo.getId(), pluginInfos, reference));
            appendValue(xml,"artifactId", reference.getId());
            appendValue(xml,"version", getVersion(reference.getVersion()));
            indent(xml).append(DEPENDENCY_END).append(lineSeparator);
        }
        xml.append(INDENT).append(DEPENDENCIES_END).append(lineSeparator);
        append(pomFile, xml);
    }

    private String getProjectId(String pluginId, PluginInfos pluginInfos, PluginReference reference) {
        try {
            String projectId = pluginInfos.getPlugin(reference.getId()).getProjectId();
            Log.trace("FOUND plugin " + reference.getId() + " required by plugin " + pluginId);
            return projectId;
        } catch (Exception e) {
            String message = "Can't find plugin " + reference.getId() + " required by plugin " + pluginId;
            Log.warn(message);
            return message;
        }
    }

    private StringBuilder indent(StringBuilder xml) {
        return xml.append(INDENT).append(INDENT);
    }

    private StringBuilder appendValue(StringBuilder xml, String tag, String value) {
        return indent(xml).append(INDENT)
            .append('<').append(tag).append('>')
            .append(value)
            .append("</").append(tag).append('>')
            .append(lineSeparator);
    }
}
