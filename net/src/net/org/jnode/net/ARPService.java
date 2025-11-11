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
 
package org.jnode.net;

import org.jnode.driver.Device;
import org.jnode.util.TimeoutException;

/**
 * Interface for ARP services that can resolve protocol addresses to hardware addresses.
 * This interface allows IPv4 and other protocols to use ARP without depending on the
 * ARP implementation package.
 * 
 * @author epr
 */
public interface ARPService {

    /**
     * Gets the hardware address for a given protocol address.
     * 
     * @param address The protocol address to resolve
     * @param myAddress My protocol address
     * @param device The device to use for the ARP request
     * @param timeout Timeout in milliseconds
     * @return The hardware address
     * @throws TimeoutException If the address could not be resolved within the timeout
     * @throws org.jnode.driver.net.NetworkException If a network error occurs
     */
    HardwareAddress getHardwareAddress(ProtocolAddress address, ProtocolAddress myAddress,
                                       Device device, long timeout) throws TimeoutException, org.jnode.driver.net.NetworkException;

    /**
     * Sets an entry in the ARP cache.
     * 
     * @param hardwareAddress The hardware address
     * @param protocolAddress The protocol address
     * @param permanent If true, the entry is permanent and won't expire
     */
    void setCacheEntry(HardwareAddress hardwareAddress, ProtocolAddress protocolAddress, boolean permanent);
}
