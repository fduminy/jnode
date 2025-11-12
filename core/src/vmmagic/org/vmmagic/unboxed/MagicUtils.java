/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.vmmagic.unboxed;

/**
 * Utility class for magic classes.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class MagicUtils {

    private static transient int refSize;
    
    /**
     * Convert to a String representation.
     * @param v
     * @return the String value of the Address
     */
    public static String toString(Address v) {
        if (getRefSize() == 4) {
            return hex(v.toInt());
        } else {
            return hex(v.toLong());            
        }
    }
    
    /**
     * Convert to a String representation.
     * @param v
     * @return  the String value of the Extent
     */
    public static String toString(Extent v) {
        if (getRefSize() == 4) {
            return hex(v.toInt());
        } else {
            return hex(v.toLong());            
        }
    }
    
    /**
     * Convert to a String representation.
     * @param v
     * @return the String value of the Offset
     */
    public static String toString(Offset v) {
        if (getRefSize() == 4) {
            return hex(v.toInt());
        } else {
            return hex(v.toLong());            
        }
    }
    
    /**
     * Convert to a String representation.
     * @param v
     * @return the String value of the Word
     */
    public static String toString(Word v) {
        if (getRefSize() == 4) {
            return hex(v.toInt());
        } else {
            return hex(v.toLong());            
        }
    }
    
    /**
     * Gets the reference size for this architecture.
     * The reference size is provided via the system property "jnode.arch.refsize"
     * which is set during VM initialization. If the property is not set, 
     * defaults to 8 bytes (64-bit).
     * 
     * @return the reference size in bytes (4 or 8)
     */
    private static final int getRefSize() {
        if (refSize == 0) {
            String prop = System.getProperty("jnode.arch.refsize");
            refSize = (prop != null) ? Integer.parseInt(prop) : 8; // default to 64-bit
        }
        return refSize;
    }
    
    /**
     * Gets the hexadecimal representation of the given number that is
     * 8 digits long.
     *
     * @param number
     * @return String
     */
    private static String hex(int number) {
        return hex(number, 8);
    }

    /**
     * Gets the hexadecimal representation of the given number that is
     * 16 digits long.
     *
     * @param number
     * @return String
     */
    private static String hex(long number) {
        return hex(number, 16);
    }

    /**
     * Gets the hexadecimal representation of the given number. The result is
     * prefixed with '0' until the given length is reached.
     *
     * @param number
     * @param length
     * @return String
     */
    private static String hex(int number, int length) {
        StringBuilder buf = new StringBuilder(length);
        int2HexString(buf, number);
        return prefixZero(buf, length);
    }

    /**
     * Gets the hexadecimal representation of the given number. The result is
     * prefixed with '0' until the given length is reached.
     *
     * @param number
     * @param length
     * @return String
     */
    private static String hex(long number, int length) {
        StringBuilder buf = new StringBuilder(length);
        long2HexString(buf, number);
        return prefixZero(buf, length);
    }

    /**
     * Convert the given integer to its hexadecimal representation.
     * 
     * @param buf the buffer to append to
     * @param value the value to convert
     */
    private static void int2HexString(StringBuilder buf, int value) {
        int rem = value & 0x0F;
        int q = value >>> 4;
        if (q != 0) {
            int2HexString(buf, q);
        }

        if (rem < 10) {
            buf.append((char) ('0' + rem));
        } else {
            buf.append((char) ('A' + rem - 10));
        }
    }

    /**
     * Convert the given long to its hexadecimal representation.
     * 
     * @param buf the buffer to append to
     * @param value the value to convert
     */
    private static void long2HexString(StringBuilder buf, long value) {
        int rem = (int) (value & 0x0FL);
        long q = value >>> 4;
        if (q != 0) {
            long2HexString(buf, q);
        }

        if (rem < 10) {
            buf.append((char) ('0' + rem));
        } else {
            buf.append((char) ('A' + rem - 10));
        }
    }

    /**
     * Prefix with zeros or truncate to achieve the desired length.
     * 
     * @param v the buffer
     * @param length the desired length
     * @return the resulting string
     */
    private static String prefixZero(StringBuilder v, int length) {
        if (v.length() > length) {
            // truncate leading chars
            return v.substring(v.length() - length);
        } else {
            // insert leading '0's
            while (v.length() < length) {
                v.insert(0, '0');
            }
            return v.toString();
        }
    }
    
}
