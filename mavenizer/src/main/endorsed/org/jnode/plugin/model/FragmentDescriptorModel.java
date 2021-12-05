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
import java.nio.ByteBuffer;
import java.util.List;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.PluginException;
import org.jnode.util.Version;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FragmentDescriptorModel extends PluginDescriptorModel {

    private final String pluginId;

    private final Version pluginVersion;

    private PluginDescriptorModel plugin;

    /**
     * @param jarFile
     * @param e
     * @throws PluginException
     */
    public FragmentDescriptorModel(PluginJar jarFile, XMLElement e)
        throws PluginException {
        super(jarFile, e);
        this.pluginId = getAttribute(e, "plugin-id", true);
        this.pluginVersion = new Version(getAttribute(e, "plugin-version", true));
    }

    /**
     * @param e
     * @throws PluginException
     */
    public FragmentDescriptorModel(XMLElement e) throws PluginException {
        this(null, e);
    }

    public boolean containsResource(String resourceName) {
        final PluginJar jar = getJarFile();
        if (jar != null) {
            return jar.containsResource(resourceName);
        } else {
            return false;
        }
    }

    public ByteBuffer getResourceAsBuffer(String resourceName) {
        final PluginJar jar = getJarFile();
        if (jar != null) {
            return jar.getResourceAsBuffer(resourceName);
        } else {
            return null;
        }
    }

    public final String getPluginId() {
        return pluginId;
    }

    public final Version getPluginVersion() {
        return pluginVersion;
    }

    public URL getResource(String resourceName) {
        final PluginJar jar = getJarFile();
        if (jar != null) {
            return jar.getResource(resourceName);
        } else {
            return null;
        }
    }

    /**
     * @see PluginDescriptorModel#getPluginClassLoader()
     */
    public ClassLoader getPluginClassLoader() {
        if (plugin != null) {
            return plugin.getPluginClassLoader();
        } else {
            throw new IllegalStateException("Fragment not yet resolved");
        }
    }

    protected void initializeRequiresList(List<PluginPrerequisiteModel> list,
                                          XMLElement e) throws PluginException {
        final String pluginId = getAttribute(e, "plugin-id", true);
        final Version pluginVersion = new Version(getAttribute(e, "plugin-version", true));
        list.add(new PluginPrerequisiteModel(this, pluginId, pluginVersion));
    }

    /**
     * @see PluginDescriptorModel#resolve(PluginRegistryModel)
     */
    public void resolve(PluginRegistryModel registry) throws PluginException {
        super.resolve(registry);
        plugin = (PluginDescriptorModel) registry.getPluginDescriptor(pluginId);
        if (plugin == null) {
            throw new PluginException("Plugin " + getPluginId() + " not found");
        }
        plugin.add(this);
    }

    /**
     * @see PluginDescriptorModel#unresolve(PluginRegistryModel)
     */
    protected void unresolve(PluginRegistryModel registry) throws PluginException {
        if (plugin != null) {
            plugin.remove(this);
            plugin = null;
        }
        super.unresolve(registry);
    }

    /**
     * @see PluginDescriptorModel#isFragment()
     */
    public boolean isFragment() {
        return true;
    }
}
