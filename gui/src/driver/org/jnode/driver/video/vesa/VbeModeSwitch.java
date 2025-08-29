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

import org.jnode.annotation.MagicPermission;
import org.jnode.system.resource.MemoryResource;
import org.jnode.system.resource.ResourceManager;
import org.jnode.system.resource.ResourceNotFreeException;
import org.jnode.system.resource.ResourceOwner;
import org.jnode.system.resource.SimpleResourceOwner;
import org.jnode.vm.Unsafe;
import org.jnode.vm.x86.VmX86Processor;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

import javax.naming.InitialNaming;
import javax.naming.NameNotFoundException;

/**
 * VBE Mode Switching implementation for runtime mode changes
 * 
 * @author JNode AI Assistant
 */
@MagicPermission
public class VbeModeSwitch {

    // VBE Function codes
    public static final int VBE_GET_CONTROLLER_INFO = 0x4F00;
    public static final int VBE_GET_MODE_INFO = 0x4F01;
    public static final int VBE_SET_MODE = 0x4F02;
    public static final int VBE_GET_CURRENT_MODE = 0x4F03;
    public static final int VBE_SAVE_RESTORE_STATE = 0x4F04;

    // VBE Mode flags
    public static final int VBE_MODE_LINEAR_FRAMEBUFFER = 0x4000;
    public static final int VBE_MODE_PRESERVE_MEMORY = 0x8000;

    // VBE Return codes
    public static final int VBE_SUCCESS = 0x004F;
    public static final int VBE_FAILED = 0x014F;
    public static final int VBE_NOT_SUPPORTED = 0x024F;
    public static final int VBE_INVALID_MODE = 0x034F;

    private static final ResourceOwner RESOURCE_OWNER = new SimpleResourceOwner("VbeModeSwitch");

    /**
     * Switch to the specified VBE mode
     * 
     * @param mode VBE mode number (e.g., 0x117 for 1024x768x16)
     * @param useLinearFrameBuffer true to enable linear framebuffer
     * @return true if mode switch successful
     */
    public static boolean switchToMode(int mode, boolean useLinearFrameBuffer) {
        try {
            // Add linear framebuffer flag if requested
            if (useLinearFrameBuffer) {
                mode |= VBE_MODE_LINEAR_FRAMEBUFFER;
            }

            Unsafe.debug("Attempting to switch to VBE mode 0x" + 
                        Integer.toHexString(mode) + "\n");

            // Call VBE Set Mode function
            int result = callVbeFunction(VBE_SET_MODE, mode, Address.zero());

            if ((result & 0xFFFF) == VBE_SUCCESS) {
                Unsafe.debug("VBE mode switch successful\n");
                return true;
            } else {
                Unsafe.debug("VBE mode switch failed: 0x" + 
                            Integer.toHexString(result) + "\n");
                return false;
            }

        } catch (Exception e) {
            Unsafe.debug("Exception during VBE mode switch: " + e.getMessage() + "\n");
            return false;
        }
    }

    /**
     * Get information about a specific VBE mode
     * 
     * @param mode VBE mode number
     * @return VBE mode information block, or null if failed
     */
    public static VbeModeInfo getModeInfo(int mode) {
        MemoryResource buffer = null;
        try {
            ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);

            // Allocate buffer for mode info (256 bytes should be enough)
            buffer = rm.claimMemoryResource(RESOURCE_OWNER, 
                                          null, 
                                          Extent.fromIntZeroExtend(256), 
                                          ResourceManager.MEMMODE_NORMAL);

            Address bufferAddr = buffer.getAddress();

            // Clear buffer
            for (int i = 0; i < 256; i++) {
                bufferAddr.add(i).store((byte) 0);
            }

            // Call VBE Get Mode Info function
            int result = callVbeFunction(VBE_GET_MODE_INFO, mode, bufferAddr);

            if ((result & 0xFFFF) == VBE_SUCCESS) {
                return new VbeModeInfo(bufferAddr);
            } else {
                Unsafe.debug("VBE Get Mode Info failed: 0x" + 
                            Integer.toHexString(result) + "\n");
                return null;
            }

        } catch (Exception e) {
            Unsafe.debug("Exception during VBE Get Mode Info: " + e.getMessage() + "\n");
            return null;
        } finally {
            if (buffer != null) {
                buffer.release();
            }
        }
    }

    /**
     * Get the current VBE mode
     * 
     * @return current VBE mode number, or -1 if failed
     */
    public static int getCurrentMode() {
        try {
            int result = callVbeFunction(VBE_GET_CURRENT_MODE, 0, Address.zero());

            if ((result & 0xFFFF) == VBE_SUCCESS) {
                return (result >> 16) & 0xFFFF; // Mode is in high word
            } else {
                Unsafe.debug("VBE Get Current Mode failed: 0x" + 
                            Integer.toHexString(result) + "\n");
                return -1;
            }

        } catch (Exception e) {
            Unsafe.debug("Exception during VBE Get Current Mode: " + e.getMessage() + "\n");
            return -1;
        }
    }

    /**
     * Test if a VBE mode is supported
     * 
     * @param mode VBE mode number to test
     * @return true if mode is supported
     */
    public static boolean isModeSupported(int mode) {
        VbeModeInfo info = getModeInfo(mode);
        if (info == null) return false;

        return info.isSupported() && info.isGraphicsMode();
    }

    /**
     * Call a VBE function using real-mode transition
     * This is the core method that handles the protected->real mode switch
     */
    private static int callVbeFunction(int function, int parameter, Address buffer) {
        // This is a critical section - we need to disable interrupts
        // and carefully manage the CPU state transition

        try {
            // Disable interrupts during mode switch
            VmX86Processor processor = (VmX86Processor) VmProcessor.current();
            boolean oldIrqState = processor.isInInterruptContext();

            if (!oldIrqState) {
                Unsafe.disableInterrupts();
            }

            try {
                // Prepare registers for VBE call
                // AX = VBE function
                // BX = mode/parameter  
                // ES:DI = buffer (if needed)

                int result = performRealModeCall(function, parameter, buffer);
                return result;

            } finally {
                if (!oldIrqState) {
                    Unsafe.enableInterrupts();
                }
            }

        } catch (Exception e) {
            Unsafe.debug("Critical error in VBE function call: " + e.getMessage() + "\n");
            return VBE_FAILED;
        }
    }

    /**
     * Perform the actual real-mode call
     * This method would need to be implemented in native code or assembly
     */
    private static native int performRealModeCall(int function, int parameter, Address buffer);

    /**
     * Initialize the VBE mode switching system
     * This should be called during system startup
     */
    public static boolean initialize() {
        try {
            // Check if VBE is available
            int currentMode = getCurrentMode();
            if (currentMode >= 0) {
                Unsafe.debug("VBE mode switching initialized. Current mode: 0x" + 
                            Integer.toHexString(currentMode) + "\n");
                return true;
            } else {
                Unsafe.debug("VBE not available for mode switching\n");
                return false;
            }
        } catch (Exception e) {
            Unsafe.debug("Failed to initialize VBE mode switching: " + e.getMessage() + "\n");
            return false;
        }
    }
}
