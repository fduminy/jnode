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
 
package org.jnode.plugin.model;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jnode.plugin.PluginClassLoader;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class PluginClassLoaderImpl extends ClassLoader implements PluginClassLoader {

    /**
     * The registry
     */
    private final PluginRegistryModel registry;

    /**
     * The descriptor
     */
    private final PluginDescriptorModel descriptor;

    /**
     * The plugin jar file
     */
    private final PluginJar jar;

    /**
     * The classloaders of the prerequisite plugins
     */
    private final PluginClassLoaderImpl[] prerequisiteLoaders;

    /**
     * Initialize this instance.
     *
     * @param jar
     */
    public PluginClassLoaderImpl(PluginRegistryModel registry,
                                 PluginDescriptorModel descr, PluginJar jar,
                                 PluginClassLoaderImpl[] prerequisiteLoaders) {
        this.registry = registry;
        this.descriptor = descr;
        this.jar = jar;
        this.prerequisiteLoaders = prerequisiteLoaders;
    }

    /**
     * Gets the names of the classes contained in this plugin.
     *
     * @return
     */
    public Set<String> getClassNames() {
        HashSet<String> classNames = new HashSet<String>();
        for (String name : jar.resourceNames()) {
            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - 6);
                classNames.add(name.replace('/', '.'));
            }
        }
        return classNames;
    }

    /**
     * Gets the names of the resources contained in this plugin.
     *
     * @return the set of contained resources
     */
    public Collection<String> getResources() {
        return jar.resourceNames();
    }
    
    /**
     * Finds the specified class. This method should be overridden by class
     * loader implementations that follow the new delegation model for loading
     * classes, and will be called by the loadClass method after checking the
     * parent class loader for the requested class. The default implementation
     * throws ClassNotFoundException.
     *
     * @param name
     * @return Class
     * @throws ClassNotFoundException
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    protected final Class<?> findClass(String name) throws ClassNotFoundException {
        final Class<?> cls = findPluginClass(name);
        if (cls != null) {
            return cls;
        } else {
            // Not found
            throw new ClassNotFoundException(name);
        }
    }

    /**
     * Finds the specified class. This method should be overridden by class
     * loader implementations that follow the new delegation model for loading
     * classes, and will be called by the loadClass method after checking the
     * parent class loader for the requested class. The default implementation
     * throws ClassNotFoundException.
     *
     * @param name
     * @return Class The class, or null if not found.
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    private final Class<?> findPluginClass(String name) {
        return null;
    }

    /**
     * Does this classloader contain the specified class.
     *
     * @return boolean
     */
    protected final boolean containsClass(String name) {
        return false;
    }

    /**
     * Finds the resource with the given name. Class loader implementations
     * should override this method to specify where to find resources.
     *
     * @param name
     * @return URL
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    protected final URL findResource(String name) {
        // Try the prerequisite loaders first
        final int max = prerequisiteLoaders.length;
        for (int i = 0; i < max; i++) {
            final PluginClassLoaderImpl cl = prerequisiteLoaders[i];
            if (cl != null) {
                final URL url = cl.findResource(name);
                if (url != null) {
                    return url;
                }
            }
        }

        // Try the fragments
        URL url = null;
        FragmentDescriptorModel fragment = null;
        for (FragmentDescriptorModel f : descriptor.fragments()) {
            url = f.getResource(name);
            if (url != null) {
                fragment = f;
                break;
            }
        }

        // Not found, try my own plugin
        // System.out.println("Try resource " + name + " on " +
        // jar.getDescriptor().getId());
        if (url == null) {
            url = jar.getResource(name);
        }
        if (url != null) {
            try {
                startPlugin();
                if (fragment != null) {
                    fragment.startPlugin(registry);
                }
            } catch (PluginException ex) {
            }
        }
        return url;
    }

    /**
     * Make sure that the plugin gets started. This method ensures that this
     * classloader can be used to start the plugin.
     */
    private final void startPlugin() throws PluginException {
        descriptor.startPlugin(registry);
    }

    /**
     * @see org.jnode.plugin.PluginClassLoader#getDeclaringPluginDescriptor()
     */
    public PluginDescriptor getDeclaringPluginDescriptor() {
        return descriptor;
    }
        
    public String toString() {
        return getClass().getName() + '(' + getDeclaringPluginDescriptor().getId() + ')';
    }    
}
