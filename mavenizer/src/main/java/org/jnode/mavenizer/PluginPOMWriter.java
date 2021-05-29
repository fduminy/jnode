package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.plugin.Library;
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

    private final Properties thirdPartyArtifacts = new Properties();

    public PluginPOMWriter(DestinationRoot destinationRoot) {
        super(destinationRoot);
        try {
            thirdPartyArtifacts.load(PluginPOMWriter.class.getResourceAsStream("third_party_artifacts.properties"));
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    public final File write(PluginInfos pluginInfos, PluginInfo pluginInfo) {
        File pomDirectory = getPluginHome(destinationRoot, pluginInfo);
        File file = write(pomDirectory, pluginInfo.getProjectId(),
            pluginInfo.getId(), pluginInfo.getVersion(), "jar");

        StringBuilder xml = new StringBuilder(INDENT).append(DEPENDENCIES_BEGIN).append(lineSeparator);
        if (pluginInfo.isThirdParty()) {
            for (Library lib : pluginInfo.getPluginDescriptor().getRuntime().getLibraries()) {
                addDependency(pluginInfo, xml, lib);
            }
        } else {
            for (PluginPrerequisite dependency : pluginInfo.getPluginDescriptor().getPrerequisites()) {
                addDependency(pluginInfos, pluginInfo, xml, dependency);
            }
        }
        xml.append(INDENT).append(DEPENDENCIES_END).append(lineSeparator);
        append(file, xml);

        return file;
    }

    private void addDependency(PluginInfo pluginInfo, StringBuilder xml, Library library) {
        String property = thirdPartyArtifacts.getProperty(library.getName());
        if (property == null) {
            Log.warn("Third party artifact not defined for " + library.getName() + " in plugin " + pluginInfo.getId());
            return;
        }
        String[] mavenArtifact = property.split(":");
        if (mavenArtifact.length < 3) {
            Log.warn("Invalid third party artifact for " + library.getName() + " in plugin " + pluginInfo.getId());
            return;
        }
        addDependency(xml, mavenArtifact[0], mavenArtifact[1], mavenArtifact[2]);
    }

    private void addDependency(PluginInfos pluginInfos, PluginInfo pluginInfo, StringBuilder xml,
                           PluginPrerequisite dependency) {
        PluginReference reference = dependency.getPluginReference();
        String groupId = "org.jnode." + getProjectId(pluginInfo.getId(), pluginInfos, reference);
        addDependency(xml, groupId, reference.getId(), getVersion(reference.getVersion()));
    }

    private void addDependency(StringBuilder xml, String groupId, String artifactId, String version) {
        indent(xml).append(DEPENDENCY_BEGIN).append(lineSeparator);
        appendValue(xml,"groupId", groupId);
        appendValue(xml,"artifactId", artifactId);
        appendValue(xml,"version", version);
        indent(xml).append(DEPENDENCY_END).append(lineSeparator);
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
