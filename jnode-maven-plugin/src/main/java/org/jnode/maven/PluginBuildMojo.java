/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.maven;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jnode.nanoxml.XMLElement;
import org.jnode.nanoxml.XMLParseException;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.Runtime;
import org.jnode.plugin.model.Factory;

/**
 * Maven Mojo for building JNode plugins.
 * This is the Maven equivalent of the Ant PluginTask.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Converted to Maven by JNode Team
 */
@Mojo(name = "build-plugin", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class PluginBuildMojo extends AbstractMojo {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Directory containing plugin descriptor XML files.
     */
    @Parameter(property = "jnode.descriptorsDirectory", required = true)
    private File descriptorsDirectory;

    /**
     * Output directory for generated plugin JAR files.
     */
    @Parameter(property = "jnode.outputDirectory", defaultValue = "${project.build.directory}/plugins", required = true)
    private File outputDirectory;

    /**
     * Temporary directory for intermediate files.
     */
    @Parameter(property = "jnode.tmpDirectory", defaultValue = "${project.build.directory}/tmp/plugins", required = true)
    private File tmpDirectory;

    /**
     * Directory containing source classes and resources for plugins.
     */
    @Parameter(property = "jnode.pluginDirectory", required = true)
    private File pluginDirectory;

    /**
     * Library aliases mapping.
     * Maps library names (e.g., "jnode-core.jar") to actual file locations.
     */
    @Parameter
    private Map<String, File> libraryAliases = new HashMap<>();

    /**
     * Maximum number of concurrent threads for building plugins.
     */
    @Parameter(property = "jnode.maxThreadCount", defaultValue = "10")
    private int maxThreadCount;

    /**
     * Maximum number of plugins that can be queued.
     */
    @Parameter(property = "jnode.maxPluginCount", defaultValue = "500")
    private int maxPluginCount;

    /**
     * Whether to compress the generated JAR files.
     */
    @Parameter(property = "jnode.compressJars", defaultValue = "false")
    private boolean compressJars;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateParameters();
        createDirectories();

        getLog().info("Building JNode plugins from descriptors in: " + descriptorsDirectory);
        getLog().info("Output directory: " + outputDirectory);

        final AtomicBoolean failure = new AtomicBoolean(false);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            maxThreadCount, 
            maxThreadCount, 
            60, 
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(maxPluginCount)
        ) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if (t != null) {
                    getLog().error("Plugin build failed", t);
                    failure.set(true);
                }
            }
        };

        final Map<String, File> descriptors = new HashMap<>();
        
        // Find all plugin descriptor XML files
        File[] descriptorFiles = descriptorsDirectory.listFiles((dir, name) -> 
            name.endsWith(".xml") && !name.endsWith("-plugin-list.xml")
        );

        if (descriptorFiles == null || descriptorFiles.length == 0) {
            getLog().warn("No plugin descriptors found in " + descriptorsDirectory);
            return;
        }

        getLog().info("Found " + descriptorFiles.length + " plugin descriptors");

        // Submit build tasks for each descriptor
        for (final File descriptorFile : descriptorFiles) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        buildPlugin(descriptors, descriptorFile);
                    } catch (Exception e) {
                        getLog().error("Failed to build plugin from " + descriptorFile, e);
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        // Wait for all tasks to complete
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                getLog().error("Plugin building timed out after 10 minutes");
                throw new MojoExecutionException("Plugin building timed out");
            }
        } catch (InterruptedException ie) {
            throw new MojoExecutionException("Building plugins interrupted", ie);
        }

        if (failure.get()) {
            throw new MojoExecutionException("At least one plugin build failed - see above errors");
        }

        getLog().info("Successfully built " + descriptors.size() + " plugins");
    }

    private void validateParameters() throws MojoExecutionException {
        if (descriptorsDirectory == null || !descriptorsDirectory.exists()) {
            throw new MojoExecutionException("Descriptors directory does not exist: " + descriptorsDirectory);
        }
        if (pluginDirectory == null || !pluginDirectory.exists()) {
            throw new MojoExecutionException("Plugin directory does not exist: " + pluginDirectory);
        }
    }

    private void createDirectories() throws MojoExecutionException {
        try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            if (!tmpDirectory.exists()) {
                tmpDirectory.mkdirs();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create directories", e);
        }
    }

    /**
     * Build a single plugin from its descriptor.
     *
     * @param descriptors map of fullPluginId to File descriptor
     * @param descriptorFile the plugin descriptor XML file
     */
    private synchronized void buildPlugin(Map<String, File> descriptors, File descriptorFile) 
            throws MojoExecutionException {
        
        try {
            final PluginDescriptor descr = readDescriptor(descriptorFile);
            final String fullId = descr.getId() + "_" + descr.getVersion();

            // Check for duplicate plugin IDs
            if (descriptors.containsKey(fullId)) {
                File otherDesc = descriptors.get(fullId);
                throw new MojoExecutionException(
                    "Duplicate plugin id (" + fullId + ") in: " + otherDesc + " and " + descriptorFile
                );
            }
            descriptors.put(fullId, descriptorFile);

            File destFile = new File(outputDirectory, fullId + ".jar");

            // Check if plugin needs rebuilding
            if (isUpToDate(descriptorFile, descr, destFile)) {
                getLog().debug("Plugin " + fullId + " is up to date");
                return;
            }

            getLog().info("Building plugin: " + fullId);

            // Create the plugin JAR
            createPluginJar(descr, descriptorFile, destFile);

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to build plugin from " + descriptorFile, e);
        }
    }

    /**
     * Read and parse a plugin descriptor XML file.
     */
    private PluginDescriptor readDescriptor(File descriptor) throws PluginException, IOException, XMLParseException {
        final XMLElement root = new XMLElement(new Hashtable<Object, Object>(), true, false);
        try (FileReader r = new FileReader(descriptor)) {
            root.parseFromReader(r);
        }
        return Factory.parseDescriptor(root);
    }

    /**
     * Check if the plugin JAR is up to date.
     */
    private boolean isUpToDate(File descriptorFile, PluginDescriptor descr, File destFile) {
        if (!destFile.exists()) {
            return false;
        }
        
        long destLastModified = destFile.lastModified();

        // Check descriptor modification time
        if (descriptorFile.lastModified() > destLastModified) {
            return false;
        }

        // Check runtime library modification times
        final Runtime rt = descr.getRuntime();
        if (rt != null) {
            for (Library lib : rt.getLibraries()) {
                File libFile = getLibraryFile(lib);
                long lastModified = getLastModified(libFile);
                if (lastModified > destLastModified) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get the last modification time of a file or directory (recursively).
     */
    private long getLastModified(File file) {
        if (file.isDirectory()) {
            return getDeepLastModified(file, Long.MIN_VALUE);
        } else {
            return file.lastModified();
        }
    }

    /**
     * Recursively get the latest modification time in a directory tree.
     */
    private long getDeepLastModified(File dir, long maxLastModified) {
        long localMax = maxLastModified;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    localMax = Math.max(localMax, getDeepLastModified(f, localMax));
                }
            }
        } else {
            localMax = Math.max(localMax, dir.lastModified());
        }
        return localMax;
    }

    /**
     * Create the plugin JAR file.
     */
    private void createPluginJar(PluginDescriptor descr, File descriptorFile, File destFile) 
            throws IOException, MojoExecutionException {
        
        final String fullId = descr.getId() + "_" + descr.getVersion();

        // Create manifest
        Manifest manifest = createManifest(descr);

        // Create the JAR file
        try (JarOutputStream jos = new JarOutputStream(
                Files.newOutputStream(destFile.toPath()), manifest)) {
            
            if (!compressJars) {
                jos.setMethod(JarOutputStream.STORED);
            }

            // Add plugin.xml to the JAR
            addPluginDescriptorToJar(jos, descriptorFile);

            // Add runtime resources
            final Runtime rt = descr.getRuntime();
            if (rt != null) {
                final Library[] libs = rt.getLibraries();
                for (Library lib : libs) {
                    addLibraryToJar(jos, lib);
                }
            }
        }

        getLog().info("Created plugin JAR: " + destFile.getName());
    }

    /**
     * Create the manifest for the plugin JAR.
     */
    private Manifest createManifest(PluginDescriptor descr) {
        Manifest manifest = new Manifest();
        Attributes mainAttrs = manifest.getMainAttributes();
        mainAttrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttrs.putValue("Bundle-SymbolicName", descr.getId());
        mainAttrs.putValue("Bundle-ManifestVersion", "2");
        mainAttrs.putValue("Bundle-Version", descr.getVersion().toString());
        return manifest;
    }

    /**
     * Add the plugin descriptor XML to the JAR.
     */
    private void addPluginDescriptorToJar(JarOutputStream jos, File descriptorFile) throws IOException {
        JarEntry entry = new JarEntry("plugin.xml");
        entry.setTime(descriptorFile.lastModified());
        jos.putNextEntry(entry);
        Files.copy(descriptorFile.toPath(), jos);
        jos.closeEntry();
    }

    /**
     * Add a library's contents to the JAR.
     */
    private void addLibraryToJar(JarOutputStream jos, Library lib) throws IOException, MojoExecutionException {
        File libFile = getLibraryFile(lib);
        
        if (!libFile.exists()) {
            throw new MojoExecutionException("Library file not found: " + libFile);
        }

        // TODO: Implement proper library export/exclude filtering
        // For now, this is a simplified version
        getLog().debug("Adding library: " + lib.getName());
        
        // If it's a JAR file, extract and add its contents
        // If it's a directory, add its contents
        // Apply export/exclude filters from the Library
        
        // This is a placeholder - full implementation would require
        // proper handling of exports and excludes similar to the Ant version
    }

    /**
     * Get the file for a library, resolving aliases.
     */
    private File getLibraryFile(Library lib) throws MojoExecutionException {
        String libName = lib.getName();
        
        // Check if there's an alias for this library
        if (libraryAliases.containsKey(libName)) {
            return libraryAliases.get(libName);
        }

        // Otherwise, look in the plugin directory
        File libFile = new File(pluginDirectory, libName);
        if (!libFile.exists()) {
            throw new MojoExecutionException(
                "Library file not found: " + libFile + " (no alias defined for " + libName + ")"
            );
        }
        
        return libFile;
    }
}
