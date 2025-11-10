package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.SortedSet;
import org.apache.tools.ant.BuildException;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.plugin.Library;

import static java.lang.System.lineSeparator;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Paths.get;
import static java.util.Arrays.stream;
import static org.jnode.mavenizer.Utils.getLibrary;
import static org.jnode.mavenizer.Utils.getPluginHome;

public class PluginPOMWriter extends AbstractPOMWriter {
    static final String DEPENDENCIES_BEGIN = "<dependencies>";
    static final String DEPENDENCIES_END = "</dependencies>";
    static final String DEPENDENCY_BEGIN = "<dependency>";
    static final String DEPENDENCY_END = "</dependency>";

    private final Properties thirdPartyArtifacts = new Properties();
    private final IAntProject jnodeAntProject;

    public PluginPOMWriter(IAntProject jnodeAntProject, DestinationRoot destinationRoot) {
        super(destinationRoot);
        this.jnodeAntProject = jnodeAntProject;
        try {
            thirdPartyArtifacts.load(PluginPOMWriter.class.getResourceAsStream("third_party_artifacts.properties"));
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    public final Path write(PluginInfos pluginInfos, PluginInfo pluginInfo) {
        Path pomDirectory = getPluginHome(destinationRoot, pluginInfo);
        Path file = write(pomDirectory, pluginInfo.getProjectId(),
            pluginInfo.getId(), pluginInfo.getVersion(), "jar");

        StringBuilder xml = new StringBuilder(INDENT).append(DEPENDENCIES_BEGIN).append(lineSeparator());
        if (pluginInfo.isThirdParty()) {
            stream(pluginInfo.getPluginDescriptor().getRuntime().getLibraries())
                .forEach(lib -> addDependency(pluginInfo, xml, lib));
        } else {
            stream(pluginInfo.getPluginDescriptor().getPrerequisites())
                .forEach(dependency -> addDependency(pluginInfos, xml, dependency.getPluginReference().getId()));
        }

        xml.append(INDENT).append(DEPENDENCIES_END).append(lineSeparator());
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
        Path libraryFile = getLibrary(jnodeAntProject, library.getName());
        if (libraryFile == null) {
            Log.warn(
                "Provided third party library not found for " + library.getName() + " in plugin " + pluginInfo.getId());
            return null;
        } else if (isDirectory(libraryFile)) {
            Log.warn("Library " + library.getName() + " is a directory in plugin " + pluginInfo.getId());
            return null;
        }
        try {
            Path libraryDirectory = getPluginHome(destinationRoot, pluginInfo).resolve("lib");
            createDirectories(libraryDirectory);
            copy(libraryFile, libraryDirectory.resolve(libraryFile.getFileName()));
        } catch (IOException e) {
            throw new BuildException(e);
        }

        Path libDirectory = get("${project.basedir}", "lib");
        return new String[]{"org.jnode.provided.library", pluginInfo.getId(), "1.0.0",
            "system", libDirectory.resolve(libraryFile.getFileName().toString()).toString()};
    }

    private void addDependency(PluginInfos pluginInfos, StringBuilder xml, String artifactId) {
        PluginInfo dependencyInfo = pluginInfos.getPlugin(artifactId);
        if (dependencyInfo == null) {
            Log.warn("artifact " + artifactId + " not found");
            return;
        }
        String groupId = "org.jnode." + dependencyInfo.getProjectId();
        String version =
            dependencyInfo.getVersion(); // don't use reference.getVersion() which default to jnode version if unspecified
        addDependency(xml, groupId, artifactId, version, null, null);
    }

    private void addDependency(StringBuilder xml, String groupId, String artifactId, String version,
                               String scope, String systemPath) {
        indent(xml).append(DEPENDENCY_BEGIN).append(lineSeparator());
        appendValue(xml, "groupId", groupId);
        appendValue(xml, "artifactId", artifactId);
        appendValue(xml,"version", version);
        appendValue(xml,"scope", scope);
        appendValue(xml,"systemPath", systemPath);
        indent(xml).append(DEPENDENCY_END).append(lineSeparator());
    }

    private StringBuilder indent(StringBuilder xml) {
        return xml.append(INDENT).append(INDENT);
    }

    private void appendValue(StringBuilder xml, String tag, String value) {
        if (value != null) {
            indent(xml).append(INDENT)
                .append('<').append(tag).append('>')
                .append(value)
                .append("</").append(tag).append('>')
                .append(lineSeparator());
        }
    }
}
