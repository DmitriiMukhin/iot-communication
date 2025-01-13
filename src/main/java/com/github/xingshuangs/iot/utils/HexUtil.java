/*
 * MIT License
 *
 * Copyright (c) 2021-2099 Oscura (xingshuang) <xingshuang_cool@163.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.xingshuangs.iot.utils;


import com.github.xingshuangs.iot.exceptions.HexParseException;

/**
 * Hex tool.
 *
 * @author xingshuang
 */
public class HexUtil {

    private HexUtil() {
        // do nothing
    }

/**
     * Verify regular expressions for hexadecimal strings.
     * Validate regular expressions for hexadecimal strings
     * ^ = start
     * $ = end
     * + = Match the preceding subexpression one or more times.
     * [] = The beginning and end of the expression
     */
    private static final String REGEX = "^[a-f0-9A-F]+$";

    /**
     * Converts a string to a hexadecimal array, string like 1a6BdE8c.
     * (Convert strings to hexadecimal arrays)
     *
     * @param src string
     * @return byte array
     */
    public static byte[] toHexArray(String src) {
        if (src == null || src.length() == 0) {
            // The string cannot be null or the length cannot be 0
            throw new HexParseException("The string cannot be null or the length cannot be 0");
        }
        if ((src.length() & -src.length()) == 1) {
            // The number of strings entered must be even
            throw new HexParseException("The number of strings entered must be an even number");
        }
        if (!src.matches(REGEX)) {
            // The string content must be [0-9|a-f|A-F]
            throw new HexParseException("The string content must be [0-9|a-f|A-F].");
        }

        char[] chars = src.toCharArray();
        final byte[] out = new byte[chars.length >> 1];
        for (int i = 0; i < chars.length; i = i + 2) {
            int high = Character.digit(chars[i], 16) << 4;
            int low = Character.digit(chars[i + 1], 16);
            out[i / 2] = (byte) ((high | low) & 0xFF);
        }
        return out;
    }

    /**
     * Converts a byte array to a hexadecimal string, separated by Spaces by default.
     * (Converts byte arrays to hexadecimal strings and separates them by spaces by default)
     *
     * @param src Byte arrays
     * @return String
     */
    public static String toHexString(byte[] src) {
        return toHexString(src, " ", true);
    }

    /**
     * Converts a byte array to a hexadecimal string.
     * (Convert byte arrays to hexadecimal strings)
     *
     * @param src      byte array
     * @param splitStr separator string
     * @return string
     */
    public static String toHexString(byte[] src, String splitStr) {
        return toHexString(src, splitStr, true);
    }

    /**
     * Converts a byte array to a hexadecimal string.
     * (Convert byte arrays to hexadecimal strings)
     *
     * @param src       byte array
     * @param splitStr  separator string
     * @param upperCase upper case
     * @return string
     */
    public static String toHexString(byte[] src, String splitStr, boolean upperCase) {
        if (src == null || src.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String format = upperCase ? "%02X" : "%02x";
        for (int i = 0; i < src.length; i++) {
            sb.append(String.format(format, src[i]));
            if (i != src.length - 1) {
                sb.append(splitStr);
            }
        }
        return sb.toString().trim();
    }
}
