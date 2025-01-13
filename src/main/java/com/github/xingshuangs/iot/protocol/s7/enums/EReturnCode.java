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

package com.github.xingshuangs.iot.protocol.s7.enums;


import java.util.HashMap;
import java.util.Map;

/**
 * Return code.
 * The return value of the operation, 0xff signal success. In the Write Request message, this field is always set to zero.
 *
 * @author xingshuang
 */
public enum EReturnCode {

    /**
     * Undefined, Reserved
     */
    RESERVED((byte) 0x00, "reserved"),

    /**
     * Hardware error
     */
    HARDWARE_ERROR((byte) 0x01, "hardware error"),

    /**
     * The object is not allowed to be accessed
     */
    ACCESSING_THE_OBJECT_NOT_ALLOWED((byte) 0x03, "accessing the object not allowed"),

    /**
     * Invalid address, the address required is beyond the limits of this PLC
     */
    INVALID_ADDRESS((byte) 0x05, "invalid address"),

    /**
     * Data types are not supported
     */
    DATA_TYPE_NOT_SUPPORTED((byte) 0x06, "data type not supported"),

    /**
     * The data types are inconsistent
     */
    DATA_TYPE_INCONSISTENT((byte) 0x07, "data type inconsistent"),

    /**
     * The object does not exist
     */
    OBJECT_DOES_NOT_EXIST((byte) 0x0A, "object does not exist"),

    /**
     * Success
     */
    SUCCESS((byte) 0xFF, "success"),

    ;

    private static Map<Byte, EReturnCode> map;

    public static EReturnCode from(byte data) {
        if (map == null) {
            map = new HashMap<>();
            for (EReturnCode item : EReturnCode.values()) {
                map.put(item.code, item);
            }
        }
        return map.get(data);
    }

    private final byte code;

    private final String description;

    EReturnCode(byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public byte getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
