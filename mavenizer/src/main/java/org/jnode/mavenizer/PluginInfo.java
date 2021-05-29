package org.jnode.mavenizer;

import java.io.File;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginDescriptor;

import static org.jnode.mavenizer.Utils.readDescriptor;

public class PluginInfo {
    private final Project project;
    private final File descriptorFile;
    private final PluginDescriptor descriptor;

    public PluginInfo(Project project, File descriptorFile) {
        this.project = project;
        this.descriptorFile = descriptorFile;
        descriptor = readDescriptor(descriptorFile);
    }

    public Project getProject() {
        return project;
    }

    public File getDescriptorFile() {
        return descriptorFile;
    }

    public String getId() {
        String fileName = descriptorFile.getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public boolean isThirdParty() {
        if (descriptor.getRuntime() == null) {
            return false;
        }
        Library[] libraries = descriptor.getRuntime().getLibraries();
        if ((libraries == null) || (libraries.length == 0)) {
            return false;
        }
        return !libraries[0].getName().startsWith("jnode-");
    }

    public String getVersion() {
        return descriptor.getVersion().toString();
    }

    public PluginDescriptor getPluginDescriptor() {
        return descriptor;
    }
}
