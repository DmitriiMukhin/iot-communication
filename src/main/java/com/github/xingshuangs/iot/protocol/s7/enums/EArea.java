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
 * Data area.
 * Data area
 *
 * @author xingshuang
 */
public enum EArea {

    /**
     * 200 series system information
     */
    SYSTEM_INFO_OF_200_FAMILY ((byte) 0x03),

    /**
     * Family 200 series system logo
     */
    SYSTEM_FLAGS_OF_200_FAMILY ((byte) 0x05),

    /**
     * 200 series analog input
     */
    ANALOG_INPUTS_OF_200_FAMILY ((byte) 0x06),

    /**
     * 200 system mode output
     */
    ANALOG_OUTPUTS_OF_200_FAMILY ((byte) 0x07),

    /**
     * Direct access to peripherals
     */
    DIRECT_PERIPHERAL_ACCESS ((byte) 0x80),

    /**
     * Input (I)
     */
    INPUTS((byte) 0x81),

    /**
     * Output (Q)
     */
    OUTPUTS((byte) 0x82),

    /**
     * Internal logo (M)
     */
    FLAGS((byte) 0x83),

    /**
     * Data Block (DB)
     */
    DATA_BLOCKS((byte) 0x84),

    /**
     * Background data block (DI)
     */
    INSTANCE_DATA_BLOCKS ((byte) 0x85),

    /**
     * Local variables (L)
     */
    LOCAL_DATA((byte) 0x86),

    /**
     * Global variables (V)
     */
    UNKNOWN_YET((byte) 0x87),

    /**
     * S7 Counter (C)
     */
    S7_COUNTERS((byte) 0x1C),

    /**
     * S7 Timer (T)
     */
    S7_TIMERS((byte) 0x1D),

    /**
     * IEC counter (200 series)
     */
    IEC_COUNTERS((byte) 0x1E),

    /**
     * IEC Timer (200 Series)
     */
    IEC_TIMERS((byte) 0x1F),

    ;

    private static Map<Byte, EArea> map;

    public static EArea from(byte data) {
        if (map == null) {
            map = new HashMap<>();
            for (EArea item : EArea.values()) {
                map.put(item.code, item);
            }
        }
        return map.get(data);
    }

    private final byte code;

    EArea(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
}