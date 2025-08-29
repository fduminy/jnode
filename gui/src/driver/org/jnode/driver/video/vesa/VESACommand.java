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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

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

            // Ask user if they want to select a video mode
            println("\nWould you like to select a video mode? (y/n): ");
            String response = readLine();
            if (response != null && response.toLowerCase().startsWith("y")) {
                selectVideoMode();
            }
        } catch (Exception e) {
            printError("Failed to display VESA information: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            printError("Failed to read input: " + e.getMessage());
            return null;
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
     * Allow user to select a video mode from available modes
     */
    private static void selectVideoMode() {
        try {
            println("\n=== Video Mode Selection ===");

            Address vbeControlInfo = UnsafeX86.getVbeControlInfos();
            VbeInfoBlock vbeInfoBlock = new VbeInfoBlock(vbeControlInfo);

            if (vbeInfoBlock.isEmpty()) {
                println("No VBE information available for mode selection.");
                return;
            }

            List<Short> modes = vbeInfoBlock.getVideoModeList();
            if (modes.isEmpty()) {
                println("No video modes available for selection.");
                return;
            }

            // Display available modes with numbers
            println("Available video modes:");
            println("Mode #  | VBE Mode | Description");
            println("--------+----------+--------------------");

            for (int i = 0; i < modes.size(); i++) {
                int modeValue = modes.get(i) & 0xFFFF;
                String description = getModeDescription(modeValue);
                println(String.format("%6d  | 0x%04X   | %s", 
                       i + 1, modeValue, description));

                // Don't overwhelm the display with too many modes
                if (i >= 19) {
                    println("   ... and " + (modes.size() - 20) + " more modes");
                    break;
                }
            }

            // Get user selection
            println("\nEnter mode number (1-" + Math.min(modes.size(), 20) + "), or 0 to cancel: ");
            String input = readLine();

            if (input == null || input.trim().isEmpty()) {
                println("No selection made.");
                return;
            }

            try {
                int selection = Integer.parseInt(input.trim());

                if (selection == 0) {
                    println("Selection cancelled.");
                    return;
                }

                if (selection < 1 || selection > Math.min(modes.size(), 20)) {
                    println("Invalid selection. Please choose a number between 1 and " + 
                            Math.min(modes.size(), 20));
                    return;
                }

                // Display selected mode details
                int selectedModeValue = modes.get(selection - 1) & 0xFFFF;
                displaySelectedModeInfo(selectedModeValue);

            } catch (NumberFormatException e) {
                println("Invalid input. Please enter a number.");
            }

        } catch (Exception e) {
            printError("Error in mode selection: " + e.getMessage());
        }
    }

    /**
     * Get a description for a VBE mode value
     */
    private static String getModeDescription(int modeValue) {
        // Common VBE mode descriptions
        switch (modeValue) {
            case 0x100: return "640x400x8";
            case 0x101: return "640x480x8";
            case 0x102: return "800x600x4";
            case 0x103: return "800x600x8";
            case 0x104: return "1024x768x4";
            case 0x105: return "1024x768x8";
            case 0x106: return "1280x1024x4";
            case 0x107: return "1280x1024x8";
            case 0x10D: return "320x200x15";
            case 0x10E: return "320x200x16";
            case 0x10F: return "320x200x24";
            case 0x110: return "640x480x15";
            case 0x111: return "640x480x16";
            case 0x112: return "640x480x24";
            case 0x113: return "800x600x15";
            case 0x114: return "800x600x16";
            case 0x115: return "800x600x24";
            case 0x116: return "1024x768x15";
            case 0x117: return "1024x768x16";
            case 0x118: return "1024x768x24";
            case 0x119: return "1280x1024x15";
            case 0x11A: return "1280x1024x16";
            case 0x11B: return "1280x1024x24";
            case 0x140: return "800x600x32";
            case 0x141: return "1024x768x32";
            case 0x142: return "1280x1024x32";
            case 0x143: return "1600x1200x32";
            default:
                // Try to guess based on mode number patterns
                if (modeValue >= 0x100 && modeValue <= 0x11F) {
                    return "Standard VBE mode";
                } else if (modeValue >= 0x140 && modeValue <= 0x15F) {
                    return "32-bit color mode";
                } else {
                    return "Unknown mode";
                }
        }
    }

    /**
     * Display detailed information about the selected mode
     */
    private static void displaySelectedModeInfo(int modeValue) {
        println("\n=== Selected Mode Information ===");
        println("VBE Mode: 0x" + Integer.toHexString(modeValue).toUpperCase());
        println("Description: " + getModeDescription(modeValue));

        // Try to get detailed mode information
        VbeModeInfo modeInfo = null;
        try {
            modeInfo = VbeModeSwitch.getModeInfo(modeValue);
        } catch (Exception e) {
            println("Failed to get mode information: " + e.getMessage());
        }

        if (modeInfo != null) {
            println("\nDetailed Mode Information:");
            println("- Resolution: " + modeInfo.getXResolution() + "x" + modeInfo.getYResolution());
            println("- Color depth: " + modeInfo.getBitsPerPixel() + " bits per pixel");
            println("- Memory model: " + modeInfo.getMemoryModel());
            println("- Bytes per line: " + modeInfo.getBytesPerScanLine());
            println("- Supported: " + (modeInfo.isSupported() ? "Yes" : "No"));
            println("- Graphics mode: " + (modeInfo.isGraphicsMode() ? "Yes" : "No"));
            println("- Color mode: " + (modeInfo.isColorMode() ? "Yes" : "No"));
            println("- Linear framebuffer: " + (modeInfo.isLinearFramebufferAvailable() ? "Yes" : "No"));

            if (modeInfo.isLinearFramebufferAvailable()) {
                println("- Framebuffer address: 0x" + Integer.toHexString(modeInfo.getPhysBasePtr()));
            }

            if (modeInfo.getBitsPerPixel() >= 15) {
                println("- Color masks:");
                println("  Red:   0x" + Integer.toHexString(modeInfo.getRedMask()));
                println("  Green: 0x" + Integer.toHexString(modeInfo.getGreenMask()));
                println("  Blue:  0x" + Integer.toHexString(modeInfo.getBlueMask()));
                if (modeInfo.getAlphaMask() != 0) {
                    println("  Alpha: 0x" + Integer.toHexString(modeInfo.getAlphaMask()));
                }
            }
        }

        println("\n=== Runtime Mode Switching ===");
        println("WARNING: VBE mode switching is currently in experimental state.");
        println("The implementation requires native code support that may not be complete.");

        if (modeInfo != null && modeInfo.isSupported() && modeInfo.isGraphicsMode()) {
            println("This mode appears to be compatible with runtime switching.");
            println("Would you like to attempt to switch to this mode now? (y/n): ");

            String response = readLine();
            if (response != null && response.toLowerCase().startsWith("y")) {
                attemptModeSwitch(modeValue, modeInfo);
            } else {
                println("Mode switch cancelled.");
            }
        } else {
            println("This mode is not suitable for runtime switching.");
            if (modeInfo != null) {
                if (!modeInfo.isSupported()) {
                    println("Reason: Mode not supported by hardware");
                }
                if (!modeInfo.isGraphicsMode()) {
                    println("Reason: Not a graphics mode");
                }
            } else {
                println("Reason: Could not retrieve mode information");
            }
        }

        println("\nCurrent system information:");
        Address vbeModeInfo = UnsafeX86.getVbeModeInfos();
        ModeInfoBlock currentMode = new ModeInfoBlock(vbeModeInfo);

        if (!currentMode.isEmpty()) {
            println("- Current mode: " + currentMode.getXResolution() + "x" + 
                   currentMode.getYResolution() + "x" + currentMode.getBitsPerPixel());
            println("- Linear framebuffer: " + 
                   (currentMode.isLinearFrameBufferAvailable() ? "Available" : "Not available"));
            println("- Memory at: 0x" + 
                   Integer.toHexString(currentMode.getLinearFrameBufferAddress()));
        }
    }

    /**
     * Attempt to switch to the specified VBE mode
     */
    private static void attemptModeSwitch(int modeValue, VbeModeInfo modeInfo) {
        println("\n=== Attempting Mode Switch ===");
        println("WARNING: This operation may destabilize the system!");
        println("Make sure you have saved any important work.");
        println("\nProceed with mode switch? (yes/no): ");

        String confirmation = readLine();
        if (confirmation == null || !confirmation.toLowerCase().equals("yes")) {
            println("Mode switch aborted.");
            return;
        }

        println("Switching to mode 0x" + Integer.toHexString(modeValue) + "...");

        // Attempt the mode switch
        boolean useLinearFb = modeInfo.isLinearFramebufferAvailable();
        boolean success = VbeModeSwitch.switchToMode(modeValue, useLinearFb);

        if (success) {
            println("Mode switch successful!");
            println("New mode should be active now.");

            // Verify the switch
            int currentMode = VbeModeSwitch.getCurrentMode();
            if (currentMode >= 0) {
                println("Current VBE mode: 0x" + Integer.toHexString(currentMode));

                // Check if it matches what we requested
                int expectedMode = modeValue;
                if (useLinearFb) {
                    expectedMode |= VbeModeSwitch.VBE_MODE_LINEAR_FRAMEBUFFER;
                }

                if (currentMode == expectedMode || (currentMode & 0x3FFF) == (expectedMode & 0x3FFF)) {
                    println("Mode switch verification: PASSED");
                } else {
                    println("Mode switch verification: FAILED");
                    println("Expected: 0x" + Integer.toHexString(expectedMode));
                    println("Actual:   0x" + Integer.toHexString(currentMode));
                }
            }

            println("\nNOTE: You may need to restart graphics applications");
            println("or reinitialize the display system for changes to take effect.");

        } else {
            println("Mode switch FAILED!");
            println("The system should still be in the previous mode.");
            println("Possible reasons:");
            println("- Mode not supported by hardware");
            println("- Insufficient video memory");
            println("- BIOS/VBE implementation issues");
            println("- System security restrictions");
        }
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
