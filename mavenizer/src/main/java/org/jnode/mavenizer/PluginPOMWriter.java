package org.jnode.mavenizer;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginReference;

import static java.io.File.separator;
import static org.apache.bsf.util.StringUtils.lineSeparator;
import static org.jnode.mavenizer.Utils.createAntProject;
import static org.jnode.mavenizer.Utils.getLibrary;
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
                addDependency(pluginInfos, xml, dependency);
            }
        }
        xml.append(INDENT).append(DEPENDENCIES_END).append(lineSeparator);
        append(file, xml);

        return file;
    }

    private void addDependency(PluginInfo pluginInfo, StringBuilder xml, Library library) {
        String property = thirdPartyArtifacts.getProperty(library.getName());
        String[] mavenArtifact;
        if (property == null) {
            mavenArtifact = getProvidedLibrary(pluginInfo, library);
            if (mavenArtifact == null) {
                return;
            }
        } else {
            mavenArtifact = property.split(":");
        }
        if (mavenArtifact.length < 3) {
            Log.warn("Invalid third party artifact for " + library.getName() + " in plugin " + pluginInfo.getId());
            return;
        }
        addDependency(xml, mavenArtifact[0], mavenArtifact[1], mavenArtifact[2],
            (mavenArtifact.length > 3) ? mavenArtifact[3] : null,
            (mavenArtifact.length > 4) ? mavenArtifact[4] : null);
    }

    private String[] getProvidedLibrary(PluginInfo pluginInfo, Library library) {
        File libraryFile = getLibrary(library.getName());
        if (libraryFile == null) {
            Log.warn(
                "Provided third party library not found for " + library.getName() + " in plugin " + pluginInfo.getId());
            return null;
        } else if (libraryFile.isDirectory()) {
            Log.warn(
                "Library " + library.getName() + " is a directory in plugin " + pluginInfo.getId());
            return null;
        }
        Copy copy = new Copy();
        copy.setProject(createAntProject());
        copy.setFailOnError(true);
        copy.setFile(libraryFile);
        copy.setTodir(new File(getPluginHome(destinationRoot, pluginInfo), "lib"));
        copy.execute();

        return new String[]{"org.jnode.provided.library", pluginInfo.getId(), "1.0.0",
            "system", "${project.basedir}/lib" + separator + libraryFile.getName()};
    }

    private void addDependency(PluginInfos pluginInfos, StringBuilder xml, PluginPrerequisite dependency) {
        PluginReference reference = dependency.getPluginReference();
        PluginInfo dependencyInfo = pluginInfos.getPlugin(reference.getId());
        String groupId = "org.jnode." + dependencyInfo.getProjectId();
        String version = dependencyInfo.getVersion(); // don't use reference.getVersion() which default to jnode version if unspecified
        addDependency(xml, groupId, reference.getId(), version, null, null);
    }

    private void addDependency(StringBuilder xml, String groupId, String artifactId, String version,
                               String scope, String systemPath) {
        indent(xml).append(DEPENDENCY_BEGIN).append(lineSeparator);
        appendValue(xml,"groupId", groupId);
        appendValue(xml,"artifactId", artifactId);
        appendValue(xml,"version", version);
        appendValue(xml,"scope", scope);
        appendValue(xml,"systemPath", systemPath);
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
        if (value != null) {
            indent(xml).append(INDENT)
                .append('<').append(tag).append('>')
                .append(value)
                .append("</").append(tag).append('>')
                .append(lineSeparator);
        }
        return xml;
    }
}
