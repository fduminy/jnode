package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;

public class AbstractTestWithTemporaryFolder {
    @TempDir
    Path temporaryFolder;

    final PluginInfo buildPluginInfo(IProject project, String xml, String pluginId) throws IOException {
        if (pluginId.isEmpty()) {
            pluginId = "plugin";
        }
        Path pluginDir = temporaryFolder.resolve(project.getDirectory()).resolve(pluginId);
        createDirectories(pluginDir);
        Path descriptorFile = pluginDir.resolve(pluginId + ".xml");
        writeString(descriptorFile, xml);
        return new PluginInfo(project, descriptorFile);
    }
}
