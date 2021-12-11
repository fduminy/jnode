package org.jnode.mavenizer;

import java.nio.file.Path;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginDescriptor;

import static org.jnode.mavenizer.Utils.readDescriptor;

public class PluginInfo {
    private final IProject project;
    private final Path descriptorFile;
    private final PluginDescriptor descriptor;

    public PluginInfo(IProject project, Path descriptorFile) {
        this.project = project;
        this.descriptorFile = descriptorFile;
        descriptor = readDescriptor(descriptorFile);
    }

    public IProject getProject() {
        return project;
    }

    public Path getDescriptorFile() {
        return descriptorFile;
    }

    public String getId() {
        return descriptor.getId();
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

    public String getProjectId() {
        return getProject().getDirectory();
    }

    @Override
    public String toString() {
        return "PluginInfo{" +
            "project=" + project +
            ", descriptorFile=" + descriptorFile +
            ", descriptor=" + descriptor +
            '}';
    }
}
