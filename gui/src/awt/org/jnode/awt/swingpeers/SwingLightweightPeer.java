/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.awt.swingpeers;

import javax.swing.JComponent;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.peer.LightweightPeer;

/**
 * AWT lightweight component peers that does nothing.
 */

final class SwingLightweightPeer extends
        SwingComponentPeer<Component, SwingLightweightComponent> implements
        LightweightPeer {
    
	public SwingLightweightPeer(SwingToolkit toolkit, Component component) {
		super(toolkit, component, new SwingLightweightComponent(component));
	}

}

final class SwingLightweightComponent extends JComponent implements ISwingPeer<Component> {

    private final Component awtComponent;
    
    public SwingLightweightComponent(Component awtComponent) {
        this.awtComponent = awtComponent;
    }
    
    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#getAWTComponent()
     */
    public Component getAWTComponent() {
        return awtComponent;
    }
    
    /**
     * Pass an event onto the AWT component.
     * @see java.awt.Component#processEvent(java.awt.AWTEvent)
     */
    protected final void processEvent(AWTEvent event) {
        awtComponent.dispatchEvent(SwingToolkit.convertEvent(event, awtComponent));
    }
    
    /**
     * Process an event within this swingpeer
     * @param event
     */
    public final void processAWTEvent(AWTEvent event) {
        super.processEvent(event);
    }

    /**
     * @see org.jnode.awt.swingpeers.ISwingPeer#validatePeerOnly()
     */
    public final void validatePeerOnly() {
        super.validate();
    }    
}
