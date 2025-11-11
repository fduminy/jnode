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
 
package org.jnode.plugin;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Plugin utility methods.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class PluginUtils {

    /**
     * Gets the descriptor of the plugin that contains the given class.
     *
     * @param clazz
     * @return The descriptor, or null if this class is not contained in a plugin or part of a system plugin.
     */
    public static PluginDescriptor getPluginDescriptor(Class<?> clazz) {
        final ClassLoader cl = clazz.getClassLoader();
        if (cl instanceof PluginClassLoader) {
            return ((PluginClassLoader) cl).getDeclaringPluginDescriptor();
        } else {
            return null;
        }
    }

    public static String getLocalizedMessage(Class<?> parent, String bundleName,
                                             String messageKey) {
        return getLocalizedMessage(parent, bundleName, messageKey, false);
    }

    public static String getLocalizedMessage(Class<?> parent, String bundleName,
                                             String messageKey, boolean cleanFallback) {
        String fullName;
        ClassLoader loader;
        if (parent == null) {
            // no parent means that name is already absolute
            fullName = bundleName;
            loader = PluginUtils.class.getClassLoader();
        } else {
            // relative name to absolute
            fullName = parent.getPackage().getName() + '.' + bundleName;
            loader = parent.getClassLoader();
        }

        ResourceBundle bundle = null;
        String message = null;

        try {
            // Debug: messageKey with default locale
            bundle = ResourceBundle.getBundle(fullName, Locale.getDefault(), loader);
        } catch (MissingResourceException e) {
            try {
                // Debug: trying with English
                bundle = ResourceBundle.getBundle(fullName, Locale.ENGLISH, loader);
            } catch (MissingResourceException mre) {
                if (!cleanFallback) {
                    System.err.println("can't get message");
                    mre.printStackTrace(System.err);
                }
            }
        }

        // Debug: bundle found
        if (bundle != null) {
            try {
                // Debug: got bundle
                message = bundle.getString(messageKey);
            } catch (MissingResourceException mre) {
                if (!cleanFallback)
                    mre.printStackTrace();
            }
        }

        if (message == null && !cleanFallback) {
            System.err.println("can't get message from bundle " + bundleName + " with key " + messageKey);
        }

        return (message == null) ? (cleanFallback ? messageKey : ('?' + messageKey + '?')) : message;
    }

}
