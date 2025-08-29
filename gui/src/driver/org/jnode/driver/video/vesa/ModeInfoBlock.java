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
import org.vmmagic.unboxed.Address;

/**
 * VBE Mode Information Block according to VBE 2.0/3.0 specification
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
@MagicPermission
class ModeInfoBlock {
    private final Address address;

    // Mode attribute bit masks
    public static final int MODE_SUPPORTED = 0x0001;
    public static final int MODE_OPTIONAL_INFO_AVAILABLE = 0x0002;
    public static final int MODE_BIOS_OUTPUT_SUPPORT = 0x0004;
    public static final int MODE_COLOR = 0x0008;
    public static final int MODE_GRAPHICS = 0x0010;
    public static final int MODE_NON_VGA = 0x0020;
    public static final int MODE_NO_BANK_SWITCH = 0x0040;
    public static final int MODE_LINEAR_FRAMEBUFFER = 0x0080;

    ModeInfoBlock(Address address) {
        this.address = address;
    }

    public short getModeAttributes() {
        // ModeAttributes at offset 0x00
        return address.loadShort();
    }

    public boolean isSupported() {
        return (getModeAttributes() & MODE_SUPPORTED) != 0;
    }

    public boolean isLinearFrameBufferAvailable() {
        return (getModeAttributes() & MODE_LINEAR_FRAMEBUFFER) != 0;
    }

    public boolean isGraphicsMode() {
        return (getModeAttributes() & MODE_GRAPHICS) != 0;
    }

    short getXResolution() {
        // XResolution at offset 0x12
        return address.add(0x12).loadShort();
    }

    short getYResolution() {
        // YResolution at offset 0x14
        return address.add(0x14).loadShort();
    }

    byte getBitsPerPixel() {
        // BitsPerPixel at offset 0x19
        return address.add(0x19).loadByte();
    }

    public int getRamBase() {
        // PhysBasePtr at offset 0x28 (VBE 2.0+)
        return address.add(0x28).loadInt();
    }

    public int getLinearFrameBufferAddress() {
        return getRamBase();
    }

    public byte getBankSize() {
        // BankSize at offset 0x04 (in KB)
        return address.add(0x04).loadByte();
    }

    public byte getNumberOfBanks() {
        // NumberOfImagePages at offset 0x1D
        return address.add(0x1D).loadByte();
    }

    public short getBytesPerScanLine() {
        // BytesPerScanLine at offset 0x10 - this should return a short, not byte
        return address.add(0x10).loadShort();
    }

    public byte getMemoryModel() {
        // MemoryModel at offset 0x1B
        return address.add(0x1B).loadByte();
    }

    public byte getRedMaskSize() {
        // RedMaskSize at offset 0x1F
        return address.add(0x1F).loadByte();
    }

    public byte getRedFieldPosition() {
        // RedFieldPosition at offset 0x20
        return address.add(0x20).loadByte();
    }

    public byte getGreenMaskSize() {
        // GreenMaskSize at offset 0x21
        return address.add(0x21).loadByte();
    }

    public byte getGreenFieldPosition() {
        // GreenFieldPosition at offset 0x22
        return address.add(0x22).loadByte();
    }

    public byte getBlueMaskSize() {
        // BlueMaskSize at offset 0x23
        return address.add(0x23).loadByte();
    }

    public byte getBlueFieldPosition() {
        // BlueFieldPosition at offset 0x24
        return address.add(0x24).loadByte();
    }

    public byte getReservedMaskSize() {
        // ReservedMaskSize at offset 0x25
        return address.add(0x25).loadByte();
    }

    public byte getReservedFieldPosition() {
        // ReservedFieldPosition at offset 0x26
        return address.add(0x26).loadByte();
    }

    public boolean isEmpty() {
        if (address.isZero()) return true;

        // Check if the mode is supported
        if (!isSupported()) return true;

        // Check for reasonable values
        short width = getXResolution();
        short height = getYResolution();
        byte bpp = getBitsPerPixel();

        return width <= 0 || height <= 0 || bpp <= 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "<empty mode info>";

        StringBuilder sb = new StringBuilder();
        sb.append(getXResolution()).append("x").append(getYResolution())
          .append("x").append(getBitsPerPixel());

        if (isLinearFrameBufferAvailable()) {
            sb.append(" (linear@0x").append(Integer.toHexString(getRamBase())).append(")");
        } else {
            sb.append(" (banked)");
        }

        sb.append(" pitch=").append(getBytesPerScanLine());

        return sb.toString();
    }
}
