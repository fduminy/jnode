package org.jnode.mavenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.jnode.mavenizer.Directory.DestinationRoot;
import org.jnode.mavenizer.Directory.SourceRoot;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Paths.get;
import static org.jnode.mavenizer.SourceFileType.JAVA;
import static org.jnode.mavenizer.SourceFileType.RESOURCES;
import static org.jnode.mavenizer.Utils.getExports;
import static org.jnode.mavenizer.Utils.getPluginHome;

public record SourceCopier(IAntProject jnodeAntProject, SourceRoot sourceRoot, DestinationRoot destinationRoot) {
    public void copy(PluginInfo pluginInfo) {
        if (pluginInfo.isThirdParty()) {
            return;
        }
        Path pluginHome = getPluginHome(destinationRoot, pluginInfo);
        try {
            createDirectories(pluginHome);
        } catch (IOException e) {
            throw new BuildException(e);
        }

        copy("main", true, pluginInfo, pluginHome, pluginInfo.getProject().getSourceDirectories(sourceRoot));
        copy("test", false, pluginInfo, pluginHome, pluginInfo.getProject().getTestDirectory(sourceRoot));
    }


    private void copy(String mavenPart, boolean mandatory, PluginInfo pluginInfo, Path pluginHome,
                      Path... srcDirectories) {
        copyImpl(pluginInfo, pluginHome, mavenPart, JAVA, mandatory, srcDirectories);
        copyImpl(pluginInfo, pluginHome, mavenPart, RESOURCES, false, srcDirectories);
    }

    private void copyImpl(PluginInfo pluginInfo, Path pluginHome, String mavenPart, SourceFileType type,
                          boolean mandatory, Path... srcDirectories) {
        Copy c = new Copy();
        Path target = pluginHome.resolve(get("src", mavenPart, type.getMavenDirectory()));
        c.setTodir(target.toFile());
        try {
            createDirectories(target);
        } catch (IOException e) {
            throw new BuildException(e);
        }
        boolean sourceIsDefined = false;

        for (Path srcDir : srcDirectories) {
            for (Export export : getExports(jnodeAntProject, srcDir, pluginInfo)) {
                FileSet fs = new FileSet();
                fs.setDir(srcDir.toFile());
                sourceIsDefined = true;

                for (String incl : export.getPackageFilters()) {
                    addIncludes(fs, incl, type);
                }
                c.addFileset(fs);
            }
        }

        if (sourceIsDefined) {
            IAntProject antProject = new MavenizerAntProject();
            antProject.setProjectFor(c);
            c.execute();
        } else if (mandatory) {
            Log.warn("No source file to copy from " + Arrays.toString(srcDirectories));
        }
    }

    private void addIncludes(FileSet fileSet, String export, SourceFileType type) {
        for (String fileExtension : type.getFileExtensions()) {
            fileExtension = '.' + fileExtension;
//            if (export.equals("*")) {
//            addInclude(fileSet, "**/*" + fileExtension);
//            } else {
            String exp = export.replace('.', '/');
            addInclude(fileSet, exp + fileExtension);
//            }
        }
    }

    private void addInclude(FileSet fileSet, String include) {
        fileSet.createInclude().setName(include);
    }
}
