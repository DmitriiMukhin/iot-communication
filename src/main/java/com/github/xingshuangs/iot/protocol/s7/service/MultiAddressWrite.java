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

package com.github.xingshuangs.iot.protocol.s7.service;


import com.github.xingshuangs.iot.protocol.s7.model.DataItem;
import com.github.xingshuangs.iot.protocol.s7.model.RequestItem;
import com.github.xingshuangs.iot.protocol.s7.utils.AddressUtil;
import com.github.xingshuangs.iot.utils.*;
import lombok.Data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper address for writing.
 * Multiple address write
 *
 * @author xingshuang
 */
@Data
public class MultiAddressWrite {

    /**
     * Request items.
     * (list of requested items)
     */
    List<RequestItem> requestItems = new ArrayList<>();

    /**
     * Data items.
     * (list of data items)
     */
    List<DataItem> dataItems = new ArrayList<>();

    /**
     * Add boolean.
     * (add boolean data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addBoolean(String address, boolean data) {
        this.requestItems.add(AddressUtil.parseBit(address));
        this.dataItems.add(DataItem.createReqByBoolean(data));
        return this;
    }

    /**
     * Add byte.
     * (add byte data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addByte(String address, byte data) {
        this.requestItems.add(AddressUtil.parseByte(address, 1));
        this.dataItems.add(DataItem.createReqByByte(data));
        return this;
    }

    /**
     * Add byte.
     * (add byte array)
     *
     * @param address address string
     * @param data byte array data
     * @return this object
     */
    public MultiAddressWrite addByte(String address, byte[] data) {
        this.requestItems.add(AddressUtil.parseByte(address, data.length));
        this.dataItems.add(DataItem.createReqByByte(data));
        return this;
    }

    /**
     * Add uint16.
     * (add uint16 data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addUInt16(String address, int data) {
        byte[] bytes = ShortUtil.toByteArray(data);
        this.addByte(address, bytes);
        return this;
    }

    /**
     * Add int16.
     * (add int16 data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addInt16(String address, short data) {
        byte[] bytes = ShortUtil.toByteArray(data);
        this.addByte(address, bytes);
        return this;
    }

    /**
     * Add int16.
     * (add int16 data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addInt16(String address, int data) {
        byte[] bytes = ShortUtil.toByteArray(data);
        this.addByte(address, bytes);
        return this;
    }

    /**
     * Add uint32.
     * (add uint32 data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addUInt32(String address, long data) {
        byte[] bytes = IntegerUtil.toByteArray(data);
        this.addByte(address, bytes);
        return this;
    }

    /**
     * Add int32.
     * (add int32 data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addInt32(String address, int data) {
        byte[] bytes = IntegerUtil.toByteArray(data);
        this.addByte(address, bytes);
        return this;
    }

    /**
     * Add int64.
     * (add int64 data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addInt64(String address, long data) {
        byte[] bytes = LongUtil.toByteArray(data);
        this.addByte(address, bytes);
        return this;
    }


    /**
     * Add float32.
     * (add float32 data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addFloat32(String address, float data) {
        byte[] bytes = FloatUtil.toByteArray(data);
        this.addByte(address, bytes);
        return this;
    }

    /**
     * Add float64.
     * (add double data)
     *
     * @param address address string
     * @param data target data
     * @return this object
     */
    public MultiAddressWrite addFloat64(String address, double data) {
        byte[] bytes = FloatUtil.toByteArray(data);
        this.addByte(address, bytes);
        return this;
    }

    /**
     * Add string, for non-200SMART PLC
     * (Add string for non-200smart PLC)
     *
     * @param address address string
     * @param data string data
     * @return this object
     */
    public MultiAddressWrite addString(String address, String data) {
        return this.addString(address, data, StandardCharsets.UTF_8);
    }

    /**
     * Added string for non-200smart PLC
     *
     * @param address address
     * @param data string data
     * @param charset character set
     * @return the object itself
     */
    public MultiAddressWrite addString(String address, String data, Charset charset) {
        this.addStringCustom(address, data, 1, charset);
        return this;
    }

    /**
     * Add string, for 200SMART PLC
     * (Add string for 200smart PLC)
     *
     * @param address address string
     * @param data string data
     * @return this object
     */
    public MultiAddressWrite addStringIn200Smart(String address, String data) {
        return this.addStringIn200Smart(address, data, StandardCharsets.UTF_8);
    }

    /**
     * Added string for 200smart PLC
     *
     * @param address address
     * @param data string data
     * @param charset character set
     * @return the object itself
     */
    public MultiAddressWrite addStringIn200Smart(String address, String data, Charset charset) {
        this.addStringCustom(address, data, 0, charset);
        return this;
    }

    /**
     * Add string by custom.
     * (Custom add string)
     *
     * @param address address string
     * @param data string data
     * @param offset index offset
     */
    @SuppressWarnings("DuplicatedCode")
    private void addStringCustom(String address, String data, int offset, Charset charset) {
        byte[] dataBytes = data.getBytes(charset);
        byte[] tmp = new byte[1 + dataBytes.length];
        tmp[0] = ByteUtil.toByte(dataBytes.length);
        System.arraycopy(dataBytes, 0, tmp, 1, dataBytes.length);
        // Non-200smart, byte index + 1
        RequestItem requestItem = AddressUtil.parseByte(address, tmp.length);
        requestItem.setByteAddress(requestItem.getByteAddress() + offset);
        this.requestItems.add(requestItem);
        this.dataItems.add(DataItem.createReqByByte(tmp));
    }
}
