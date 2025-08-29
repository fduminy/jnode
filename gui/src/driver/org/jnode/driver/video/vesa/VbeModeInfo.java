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

import org.vmmagic.unboxed.Address;

/**
 * VBE Mode Information structure for runtime mode queries
 * Similar to ModeInfoBlock but used for dynamic mode information
 * 
 * @author JNode AI Assistant
 */
public class VbeModeInfo {
    private final Address address;

    // Mode attribute bit masks
    public static final int MODE_SUPPORTED = 0x0001;
    public static final int MODE_TTY_OUTPUT = 0x0004;
    public static final int MODE_COLOR = 0x0008;
    public static final int MODE_GRAPHICS = 0x0010;
    public static final int MODE_NON_VGA = 0x0020;
    public static final int MODE_NO_BANK_SWITCH = 0x0040;
    public static final int MODE_LINEAR_FRAMEBUFFER = 0x0080;
    public static final int MODE_DOUBLE_SCAN = 0x0100;
    public static final int MODE_INTERLACED = 0x0200;
    public static final int MODE_TRIPLE_BUFFER = 0x0400;
    public static final int MODE_STEREOSCOPIC = 0x0800;
    public static final int MODE_DUAL_START_ADDR = 0x1000;

    public VbeModeInfo(Address address) {
        this.address = address;
    }

    public short getModeAttributes() {
        return address.loadShort();
    }

    public boolean isSupported() {
        return (getModeAttributes() & MODE_SUPPORTED) != 0;
    }

    public boolean isGraphicsMode() {
        return (getModeAttributes() & MODE_GRAPHICS) != 0;
    }

    public boolean isColorMode() {
        return (getModeAttributes() & MODE_COLOR) != 0;
    }

    public boolean isLinearFramebufferAvailable() {
        return (getModeAttributes() & MODE_LINEAR_FRAMEBUFFER) != 0;
    }

    public boolean isVgaCompatible() {
        return (getModeAttributes() & MODE_NON_VGA) == 0;
    }

    public short getXResolution() {
        return address.add(0x12).loadShort();
    }

    public short getYResolution() {
        return address.add(0x14).loadShort();
    }

    public byte getBitsPerPixel() {
        return address.add(0x19).loadByte();
    }

    public byte getMemoryModel() {
        return address.add(0x1B).loadByte();
    }

    public short getBytesPerScanLine() {
        return address.add(0x10).loadShort();
    }

    public int getPhysBasePtr() {
        return address.add(0x28).loadInt();
    }

    public byte getRedMaskSize() {
        return address.add(0x1F).loadByte();
    }

    public byte getRedFieldPosition() {
        return address.add(0x20).loadByte();
    }

    public byte getGreenMaskSize() {
        return address.add(0x21).loadByte();
    }

    public byte getGreenFieldPosition() {
        return address.add(0x22).loadByte();
    }

    public byte getBlueMaskSize() {
        return address.add(0x23).loadByte();
    }

    public byte getBlueFieldPosition() {
        return address.add(0x24).loadByte();
    }

    public byte getReservedMaskSize() {
        return address.add(0x25).loadByte();
    }

    public byte getReservedFieldPosition() {
        return address.add(0x26).loadByte();
    }

    /**
     * Calculate color masks based on field information
     */
    public int getRedMask() {
        return VesaUtils.calculateColorMask(getRedMaskSize(), getRedFieldPosition());
    }

    public int getGreenMask() {
        return VesaUtils.calculateColorMask(getGreenMaskSize(), getGreenFieldPosition());
    }

    public int getBlueMask() {
        return VesaUtils.calculateColorMask(getBlueMaskSize(), getBlueFieldPosition());
    }

    public int getAlphaMask() {
        return VesaUtils.calculateColorMask(getReservedMaskSize(), getReservedFieldPosition());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getXResolution()).append("x").append(getYResolution())
          .append("x").append(getBitsPerPixel());

        if (isLinearFramebufferAvailable()) {
            sb.append(" (linear@0x").append(Integer.toHexString(getPhysBasePtr())).append(")");
        } else {
            sb.append(" (banked)");
        }

        if (isSupported()) {
            sb.append(" [SUPPORTED]");
        } else {
            sb.append(" [NOT SUPPORTED]");
        }

        return sb.toString();
    }
}
