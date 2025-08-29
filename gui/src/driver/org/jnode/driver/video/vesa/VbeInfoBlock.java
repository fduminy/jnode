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

import java.util.ArrayList;
import java.util.List;

import org.jnode.annotation.MagicPermission;
import org.jnode.vm.Unsafe;
import org.vmmagic.unboxed.Address;

@MagicPermission
public class VbeInfoBlock {
    private final Address address;

    // VBE signatures according to specification
    public static final int VBE_SIGNATURE = 0x41534556; // 'VESA' in little-endian
    public static final int VBE2_SIGNATURE = 0x32454256; // 'VBE2' in little-endian

    // Capability bit masks per VBE 2.0/3.0 spec
    public static final int CAP_DAC_8BIT = 0x00000001;
    public static final int CAP_NOT_VGA = 0x00000002;
    public static final int CAP_USE_VBE_PALETTE_FUNCS = 0x00000004;

    VbeInfoBlock(Address address) {
        this.address = address;
    }

    public int getSignature() {
        return address.loadInt();
    }

    public short getVersion() {
        return address.add(4).loadShort();
    }

    public int getCapabilities() {
        // VBE Control Info: Capabilities is a DWORD at offset 0x0A
        return address.add(0x0A).loadInt();
    }

    public Address getOemStringPtr() {
        // OEM String Pointer at offset 0x06 (far pointer)
        int farPtr = address.add(0x06).loadInt();
        if (farPtr == 0) return Address.zero();
        return convertFarPointer(farPtr);
    }

    public String getOemString() {
        Address oem = getOemStringPtr();
        if (oem.isZero()) return "";

        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < 256; i++) {
                byte b = oem.add(i).loadByte();
                if (b == 0) break;
                sb.append((char) b);
            }
        } catch (Exception e) {
            // Ignore memory access errors
        }
        return sb.toString();
    }

    /**
     * Convert a 16-bit far pointer to a linear address.
     * Far pointer format: high 16 bits = segment, low 16 bits = offset
     */
    private Address convertFarPointer(int farPointer) {
        if (farPointer == 0) return Address.zero();

        int segment = (farPointer >>> 16) & 0xFFFF;
        int offset = farPointer & 0xFFFF;
        long linearAddr = (segment << 4) + offset;

        // Ensure the address is accessible (within reasonable bounds)
        if (linearAddr < 0x1000 || linearAddr > 0xFFFFF) {
            return Address.zero();
        }

        return Address.fromLong(linearAddr);
    }

    public boolean isEmpty() {
        if (address.isZero()) return true;

        // Check signature
        int sig = getSignature();
        if (sig != VBE_SIGNATURE && sig != VBE2_SIGNATURE) return true;

        // Additional validation: check version
        short version = getVersion();
        return version < 0x0200; // Need at least VBE 2.0
    }

    public List<Short> getVideoModeList() {
        List<Short> modes = new ArrayList<Short>();
        if (address.isZero()) return modes;

        // VideoModePtr at offset 0x0E is a far pointer
        int farPtr = address.add(0x0E).loadInt();
        if (farPtr == 0) return modes;

        Address addr = convertFarPointer(farPtr);
        if (addr.isZero()) {
            Unsafe.debug("Invalid video mode list pointer: " + Integer.toHexString(farPtr) + "\n");
            return modes;
        }

        Unsafe.debug("Video mode list at linear address " + Long.toHexString(addr.toLong()) + "\n");

        try {
            short mode = addr.loadShort();
            int counter = 0;
            while ((mode != (short) 0xFFFF) && (counter++ < 256)) {
                modes.add(mode);
                addr = addr.add(2);
                mode = addr.loadShort();
            }
        } catch (Exception e) {
            Unsafe.debug("Error reading video mode list: " + e.getMessage() + "\n");
        }

        return modes;
    }

    public String toString() {
        if (address.isZero()) return "<VBE Control Info: null>";

        StringBuilder sb = new StringBuilder();
        int sig = getSignature();
        short ver = getVersion();

        sb.append("VBE signature=0x").append(Integer.toHexString(sig))
          .append(" version ").append(Integer.toHexString(ver)).append('\n');
        sb.append("capabilities=0x").append(Integer.toHexString(getCapabilities())).append('\n');
        sb.append("OEM string: ").append(getOemString()).append('\n');

        sb.append("video modes: ");
        List<Short> modes = getVideoModeList();
        if (modes.isEmpty()) {
            sb.append("none found");
        } else {
            for (int i = 0; i < Math.min(modes.size(), 10); i++) {
                if (i > 0) sb.append(", ");
                int modeUnsigned = modes.get(i) & 0xFFFF;
                sb.append("0x").append(Integer.toHexString(modeUnsigned));
            }
            if (modes.size() > 10) {
                sb.append("... (").append(modes.size() - 10).append(" more)");
            }
        }

        return sb.toString();
    }
}
