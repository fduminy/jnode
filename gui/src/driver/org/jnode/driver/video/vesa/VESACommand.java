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

import java.util.List;

import javax.naming.NameNotFoundException;

import org.jnode.vm.Unsafe;
import org.jnode.vm.x86.UnsafeX86;
import org.vmmagic.unboxed.Address;

/**
 * VESA command to display VBE information provided by GRUB
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class VESACommand {

    private static void print(String message) {
        System.out.print(message);
    }

    private static void println(String message) {
        System.out.println(message);
    }

    private static void printError(String message) {
        System.err.println("ERROR: " + message);
    }

    public static void main(String[] args) {
        try {
            println("=== JNode VESA Information ===");
            displayVbeInfo();
        } catch (Exception e) {
            printError("Failed to display VESA information: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Display VBE Control and Mode information provided by GRUB
     */
    public static void displayVbeInfo() {
        try {
            println("\n--- VBE Control Information ---");
            Address vbeControlInfo = UnsafeX86.getVbeControlInfos();
            VbeInfoBlock vbeInfoBlock = new VbeInfoBlock(vbeControlInfo);

            if (vbeInfoBlock.isEmpty()) {
                println("VBE Control Info is not available or invalid.");
                println("This usually means GRUB didn't switch to VBE graphics mode.");
            } else {
                println("VBE Control Info found:");
                displayVbeControlInfo(vbeInfoBlock);
            }

            println("\n--- VBE Current Mode Information ---");
            Address vbeModeInfo = UnsafeX86.getVbeModeInfos();
            ModeInfoBlock modeInfoBlock = new ModeInfoBlock(vbeModeInfo);

            if (modeInfoBlock.isEmpty()) {
                println("VBE Mode Info is not available or invalid.");
                println("No current graphics mode information available.");
            } else {
                println("Current VBE Mode Info:");
                displayVbeModeInfo(modeInfoBlock);
            }

        } catch (Exception e) {
            printError("Error accessing VBE information: " + e.getMessage());
        }
    }

    /**
     * Display detailed VBE Control Information
     */
    private static void displayVbeControlInfo(VbeInfoBlock vbeInfo) {
        try {
            int signature = vbeInfo.getSignature();
            println("  Signature: 0x" + Integer.toHexString(signature) + 
                    " (" + signatureToString(signature) + ")");

            short version = vbeInfo.getVersion();
            println("  Version: " + Integer.toHexString(version) + 
                    " (" + ((version >> 8) & 0xFF) + "." + (version & 0xFF) + ")");

            int capabilities = vbeInfo.getCapabilities();
            println("  Capabilities: 0x" + Integer.toHexString(capabilities));
            displayCapabilities(capabilities);

            String oem = vbeInfo.getOemString();
            if (!oem.isEmpty()) {
                println("  OEM String: " + oem);
            }

            println("  Available Video Modes:");
            List<Short> modes = vbeInfo.getVideoModeList();
            if (modes.isEmpty()) {
                println("    No video modes found or mode list unavailable");
            } else {
                displayVideoModes(modes);
            }

        } catch (Exception e) {
            printError("Error displaying VBE control info: " + e.getMessage());
        }
    }

    /**
     * Display detailed VBE Mode Information
     */
    private static void displayVbeModeInfo(ModeInfoBlock modeInfo) {
        try {
            short attrs = modeInfo.getModeAttributes();
            println("  Mode Attributes: 0x" + Integer.toHexString(attrs));
            displayModeAttributes(attrs);

            println("  Resolution: " + modeInfo.getXResolution() + "x" + modeInfo.getYResolution());
            println("  Bits per Pixel: " + modeInfo.getBitsPerPixel());
            println("  Bytes per Scan Line: " + modeInfo.getBytesPerScanLine());
            println("  Memory Model: " + modeInfo.getMemoryModel());

            if (modeInfo.isLinearFrameBufferAvailable()) {
                int fbAddr = modeInfo.getLinearFrameBufferAddress();
                println("  Linear Frame Buffer: 0x" + Integer.toHexString(fbAddr));
            } else {
                println("  Banking Mode: " + modeInfo.getNumberOfBanks() + " banks, " + 
                        modeInfo.getBankSize() + "KB each");
            }

            // Color mask information for RGB modes
            if (modeInfo.getBitsPerPixel() >= 15) {
                println("  Color Masks:");
                println("    Red:   " + modeInfo.getRedMaskSize() + " bits at position " + 
                        modeInfo.getRedFieldPosition());
                println("    Green: " + modeInfo.getGreenMaskSize() + " bits at position " + 
                        modeInfo.getGreenFieldPosition());
                println("    Blue:  " + modeInfo.getBlueMaskSize() + " bits at position " + 
                        modeInfo.getBlueFieldPosition());
                if (modeInfo.getReservedMaskSize() > 0) {
                    println("    Alpha: " + modeInfo.getReservedMaskSize() + " bits at position " + 
                            modeInfo.getReservedFieldPosition());
                }
            }

        } catch (Exception e) {
            printError("Error displaying VBE mode info: " + e.getMessage());
        }
    }

    /**
     * Display VBE capabilities in human-readable form
     */
    private static void displayCapabilities(int capabilities) {
        println("    - DAC 8-bit capable: " + 
                ((capabilities & VbeInfoBlock.CAP_DAC_8BIT) != 0 ? "Yes" : "No"));
        println("    - VGA compatible: " + 
                ((capabilities & VbeInfoBlock.CAP_NOT_VGA) != 0 ? "No" : "Yes"));
        println("    - Use VBE palette functions: " + 
                ((capabilities & VbeInfoBlock.CAP_USE_VBE_PALETTE_FUNCS) != 0 ? "Yes" : "No"));
    }

    /**
     * Display mode attributes in human-readable form
     */
    private static void displayModeAttributes(short attributes) {
        println("    - Supported: " + ((attributes & ModeInfoBlock.MODE_SUPPORTED) != 0 ? "Yes" : "No"));
        println("    - Graphics mode: " + ((attributes & ModeInfoBlock.MODE_GRAPHICS) != 0 ? "Yes" : "No"));
        println("    - Color mode: " + ((attributes & ModeInfoBlock.MODE_COLOR) != 0 ? "Yes" : "No"));
        println("    - Linear framebuffer: " + ((attributes & ModeInfoBlock.MODE_LINEAR_FRAMEBUFFER) != 0 ? "Yes" : "No"));
        println("    - VGA compatible: " + ((attributes & ModeInfoBlock.MODE_NON_VGA) != 0 ? "No" : "Yes"));
    }

    /**
     * Display list of available video modes
     */
    private static void displayVideoModes(List<Short> modes) {
        final int MODES_PER_LINE = 8;
        int count = 0;
        print("    ");

        for (Short mode : modes) {
            int modeUnsigned = mode & 0xFFFF;
            print("0x" + Integer.toHexString(modeUnsigned).toUpperCase());

            count++;
            if (count % MODES_PER_LINE == 0) {
                println("");
                if (count < modes.size()) {
                    print("    ");
                }
            } else {
                print(" ");
            }
        }

        if (count % MODES_PER_LINE != 0) {
            println("");
        }

        println("    Total: " + modes.size() + " video modes available");
    }

    /**
     * Convert signature integer to string representation
     */
    private static String signatureToString(int signature) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) (signature & 0xFF));
        sb.append((char) ((signature >> 8) & 0xFF));
        sb.append((char) ((signature >> 16) & 0xFF));
        sb.append((char) ((signature >> 24) & 0xFF));
        return sb.toString();
    }
}
