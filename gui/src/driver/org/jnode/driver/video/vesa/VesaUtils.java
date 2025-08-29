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
import org.jnode.annotation.MagicPermission;

/**
 * Utility methods for VESA driver implementation.
 */
@MagicPermission
public class VesaUtils {

    /**
     * Check if a memory block appears to be empty or invalid.
     * This is a safer version that handles potential memory access errors.
     * 
     * @param address the address to check
     * @param size the size to check
     * @return true if the block appears empty/invalid
     */
    public static boolean isEmpty(Address address, int size) {
        if (address.isZero()) return true;

        try {
            // Check if all bytes are zero (common sign of uninitialized data)
            int checkSize = Math.min(size, 64); // Limit check size for performance
            for (int i = 0; i < checkSize; i++) {
                if (address.add(i).loadByte() != 0) {
                    return false; // Found non-zero data
                }
            }
            return true; // All checked bytes are zero
        } catch (Exception e) {
            return true; // Memory access failed, consider it empty
        }
    }

    /**
     * Convert a 16-bit far pointer to a linear address.
     * Far pointer format: high 16 bits = segment, low 16 bits = offset
     * 
     * @param farPointer 32-bit far pointer (segment:offset)
     * @return linear address or zero if invalid
     */
    public static Address convertFarPointer(int farPointer) {
        if (farPointer == 0) return Address.zero();

        int segment = (farPointer >>> 16) & 0xFFFF;
        int offset = farPointer & 0xFFFF;
        long linearAddr = (segment << 4) + offset;

        // Ensure the address is accessible (within first 1MB for real mode)
        if (linearAddr < 0x1000 || linearAddr > 0xFFFFF) {
            return Address.zero();
        }

        return Address.fromLong(linearAddr);
    }

    /**
     * Safely read a string from memory, handling potential access violations.
     * 
     * @param address starting address
     * @param maxLength maximum string length
     * @return the string or empty string if read failed
     */
    public static String safeReadString(Address address, int maxLength) {
        if (address.isZero()) return "";

        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < maxLength; i++) {
                byte b = address.add(i).loadByte();
                if (b == 0) break; // Null terminator
                if (b < 32 || b > 126) break; // Non-printable character
                sb.append((char) b);
            }
            return sb.toString();
        } catch (Exception e) {
            return ""; // Memory access failed
        }
    }

    /**
     * Calculate color mask from field size and position.
     * 
     * @param size mask size in bits
     * @param position field position in bits
     * @return color mask
     */
    public static int calculateColorMask(int size, int position) {
        if (size <= 0 || size > 32 || position < 0 || position > 31) {
            return 0;
        }

        int mask = (1 << size) - 1; // Create mask with 'size' bits set
        return mask << position;     // Shift to correct position
    }
}
