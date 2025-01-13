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
 * Message Type: The general type of message (sometimes called the ROSCTR type). The rest of the message depends largely on the Message Type and Function Code.
 *
 * @author xingshuang
 */
public enum EMessageType {

    /**
     * Start work.
     * The meaning of starting work is that the master device sends a "work" command to the slave device through the job. Whether it is reading data or writing data is determined by the parameter
     */
    JOB((byte) 0x01),

    /**
     * Confirm that there are no data fields.
     * Confirm whether there is a data field
     */
    ACK((byte) 0x02),

    /**
     * The slave device responds to the job of the master device.
     * The slave device responds to the master device's job
     */
    ACK_DATA((byte) 0x03),

    /**
     * An extension of the original protocol.
     * Extension of the original protocol, parameter field contains request/response id, (for programming/debugging, SZL reading, security functions, time setting, cyclic reading...)
     */
    USER_DATA((byte) 0x07),
    ;

    private static Map<Byte, EMessageType> map;

    public static EMessageType from(byte data) {
        if (map == null) {
            map = new HashMap<>();
            for (EMessageType item : EMessageType.values()) {
                map.put(item.code, item);
            }
        }
        return map.get(data);
    }

    private final byte code;

    EMessageType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
}
