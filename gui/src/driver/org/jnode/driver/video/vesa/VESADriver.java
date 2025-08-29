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
 
package org.jnode.driver.video.vesa;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.video.AbstractFrameBufferDriver;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.NotOpenException;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.UnknownConfigurationException;
import org.jnode.system.resource.ResourceNotFreeException;
import org.jnode.vm.x86.UnsafeX86;
import org.vmmagic.unboxed.Address;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class VESADriver extends AbstractFrameBufferDriver {

    private FrameBufferConfiguration currentConfig;
    private VESACore kernel;

    private FrameBufferConfiguration[] configs;

    /**
     * Create a new instance
     */
    public VESADriver() {
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getConfigurations()
     */
    public final FrameBufferConfiguration[] getConfigurations() {
        return configs;
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentConfiguration()
     */
    public final FrameBufferConfiguration getCurrentConfiguration() {
        return currentConfig;
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#open(org.jnode.driver.video.FrameBufferConfiguration)
     */
    public synchronized Surface open(FrameBufferConfiguration config)
        throws UnknownConfigurationException, AlreadyOpenException, DeviceException {
        for (int i = 0; i < configs.length; i++) {
            if (config.equals(configs[i])) {
                kernel.open(config);
                this.currentConfig = config;
                return kernel;
            }
        }

        throw new UnknownConfigurationException();
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentSurface()
     */
    public synchronized Surface getCurrentSurface() throws NotOpenException {
        if (currentConfig != null) {
            return kernel;
        } else {
            throw new NotOpenException();
        }
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#isOpen()
     */
    public final synchronized boolean isOpen() {
        return (currentConfig != null);
    }

    /**
     * Notify of a close of the graphics object
     * 
     * @param graphics
     */
    final synchronized void close(VESACore graphics) {
        this.currentConfig = null;
    }

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        ModeInfoBlock modeInfoBlock = null;
        try {
            Address vbeControlInfo = UnsafeX86.getVbeControlInfos();
            VbeInfoBlock vbeInfoBlock = new VbeInfoBlock(vbeControlInfo);
            if (vbeInfoBlock.isEmpty()) {
                throw new DriverException(
                        "VBE Control Info is invalid or empty. GRUB may not have enabled VBE mode properly.");
            }

            // Verify VBE signature and version
            int signature = vbeInfoBlock.getSignature();
            if (signature != VbeInfoBlock.VBE_SIGNATURE && signature != VbeInfoBlock.VBE2_SIGNATURE) {
                throw new DriverException(
                        "Invalid VBE signature: 0x" + Integer.toHexString(signature) + 
                        " (expected VESA or VBE2)");
            }

            short version = vbeInfoBlock.getVersion();
            if (version < 0x0200) {
                throw new DriverException(
                        "VBE version too old: " + Integer.toHexString(version) + 
                        " (need 2.0 or higher)");
            }

            Address vbeModeInfo = UnsafeX86.getVbeModeInfos();
            modeInfoBlock = new ModeInfoBlock(vbeModeInfo);
            if (modeInfoBlock.isEmpty()) {
                throw new DriverException(
                        "VBE Mode Info is invalid or empty. Current video mode not properly initialized by GRUB.");
            }

            // Verify the current mode is usable
            if (!modeInfoBlock.isSupported()) {
                throw new DriverException("Current VBE mode is not supported by hardware");
            }

            if (!modeInfoBlock.isGraphicsMode()) {
                throw new DriverException("Current VBE mode is not a graphics mode");
            }

            if (!modeInfoBlock.isLinearFrameBufferAvailable()) {
                throw new DriverException("Linear framebuffer not available in current VBE mode");
            }

            // Check for reasonable mode parameters
            if (modeInfoBlock.getXResolution() <= 0 || modeInfoBlock.getYResolution() <= 0) {
                throw new DriverException("Invalid resolution: " + 
                        modeInfoBlock.getXResolution() + "x" + modeInfoBlock.getYResolution());
            }

            if (modeInfoBlock.getBitsPerPixel() < 8) {
                throw new DriverException("Unsupported color depth: " + modeInfoBlock.getBitsPerPixel() + "bpp");
            }

            kernel = new VESACore(this, vbeInfoBlock, modeInfoBlock, (PCIDevice) getDevice());            
            configs = kernel.getConfigs();

            if (configs == null || configs.length == 0) {
                throw new DriverException("No valid VESA configurations found");
            }

        } catch (ResourceNotFreeException ex) {
            throw new DriverException("Resource allocation failed: " + ex.getMessage(), ex);
        } catch (DriverException ex) {
            // Re-throw DriverExceptions as-is
            throw ex;
        } catch (Throwable t) {
            throw new DriverException("VESA driver initialization failed: " + t.getMessage(), t);
        }
        final Device dev = getDevice();
        super.startDevice();

        dev.registerAPI(HardwareCursorAPI.class, kernel);
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        final Device dev = getDevice();
        dev.unregisterAPI(HardwareCursorAPI.class);
        if (currentConfig != null) {
            kernel.close();
        }
        if (kernel != null) {
            kernel.release();
            kernel = null;
        }
        super.stopDevice();
    }
}
