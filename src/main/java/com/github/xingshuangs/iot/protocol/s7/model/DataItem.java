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

package com.github.xingshuangs.iot.protocol.s7.model;


import com.github.xingshuangs.iot.exceptions.S7CommException;
import com.github.xingshuangs.iot.common.IObjectByteArray;
import com.github.xingshuangs.iot.protocol.s7.enums.EDataVariableType;
import com.github.xingshuangs.iot.protocol.s7.enums.EReturnCode;
import com.github.xingshuangs.iot.utils.BooleanUtil;
import com.github.xingshuangs.iot.common.buff.ByteReadBuff;
import com.github.xingshuangs.iot.common.buff.ByteWriteBuff;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Data item.
 * Returns data
 *
 * @author xingshuang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataItem extends ReturnItem implements IObjectByteArray {

    /**
     * Data variable type.
     * Variable type <br>
     * Byte size: 1 <br>
     * Alphabyte number: 1
     */
    private EDataVariableType variableType = EDataVariableType.BYTE_WORD_DWORD;

    /**
     * The data length is calculated by bit. If it is byte data, /8 or *8 operation is required to read it. If it is bit data, no additional operation is required.
     * The length of the data, calculated in bits, requires /8 or *8 operations for byte data reads,
     * and does not require any additional operations for bit data <br>
     * Byte size：2 <br>
     * Alphanus：2-3
     */
    private int count = 0x0000;

    /**
     * Data content.
     * Data content
     */
    private byte[] data = new byte[0];

    @Override
    public int byteArrayLength() {
        return 4 + this.data.length;
    }

    @Override
    public byte[] toByteArray() {
        int length = 4 + this.data.length;
        ByteWriteBuff buff = ByteWriteBuff.newInstance(length)
                .putByte(this.returnCode.getCode())
                .putByte(this.variableType.getCode());
        // If the data type is bits, *8 is not required, and if it is of other types, *8 is required
        switch (this.variableType) {
            case NULL:
            case BYTE_WORD_DWORD:
            case INTEGER:
                buff.putShort(this.count * 8);
                break;
            case BIT:
            case DINTEGER:
            case REAL:
            case OCTET_STRING:
                buff.putShort(this.count);
                break;
            default:
                throw new S7CommException("Data type can not be recognized");
        }
        buff.putBytes(this.data);
        return buff.getData();
    }

    /**
     * Copy
     * (Copy a new object)
     *
     * @return DataItem
     */
    public DataItem copy() {
        DataItem dataItem = new DataItem();
        dataItem.returnCode = this.returnCode;
        dataItem.variableType = this.variableType;
        dataItem.count = this.count;
        dataItem.data = this.data;
        return dataItem;
    }

    /**
     * Parses byte array and converts it to object.
     *
     * @param data byte array
     * @return DataItem
     */
    public static DataItem fromBytes(final byte[] data) {
        ByteReadBuff buff = new ByteReadBuff(data);
        DataItem dataItem = new DataItem();
        dataItem.returnCode = EReturnCode.from(buff.getByte());
        dataItem.variableType = EDataVariableType.from(buff.getByte());
        // If it's a bit, it's parsed normally, and if it's a byte, you need to divide by 8
        switch (dataItem.variableType) {
            case NULL:
            case BYTE_WORD_DWORD:
            case INTEGER:
                dataItem.count = buff.getUInt16() / 8;
                break;
            case BIT:
            case DINTEGER:
            case REAL:
            case OCTET_STRING:
                dataItem.count = buff.getUInt16();
                break;
            default:
                throw new S7CommException("Data type can not be recognized");
        }
        // If the returned data type is null, there is no data
        if (dataItem.variableType != EDataVariableType.NULL) {
            dataItem.data = buff.getBytes(dataItem.count);
        }
        return dataItem;
    }

    /**
     * Create data item by byte.
     * (Converted to DataItem data by byte data type)
     *
     * @param data byte data
     * @return DataItem
     */
    public static DataItem createReqByByte(byte data) {
        return createReqByByte(new byte[]{data});
    }

    /**
     * Create data item by byte array.
     * (Converted to DataItem data via byte array data type)
     *
     * @param data byte array
     * @return DataItem
     */
    public static DataItem createReqByByte(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("data length is null or empty");
        }
        DataItem dataItem = new DataItem();
        dataItem.setReturnCode(EReturnCode.RESERVED);
        dataItem.setVariableType(EDataVariableType.BYTE_WORD_DWORD);
        dataItem.setCount(data.length);
        dataItem.setData(data);
        return dataItem;
    }

    /**
     * Create data item by boolean.
     * (Converted to DataItem data via boolean data type)
     *
     * @param data Boolean data
     * @return DataItem data
     */
    public static DataItem createReqByBoolean(boolean data) {
        DataItem dataItem = new DataItem();
        dataItem.setReturnCode(EReturnCode.RESERVED);
        dataItem.setVariableType(EDataVariableType.BIT);
        dataItem.setCount(1);
        dataItem.setData(new byte[]{BooleanUtil.toByte(data)});
        return dataItem;
    }

    /**
     * Convert to DataItem data by byte array + data type
     * (Convert to DataItem data via byte array + data type)
     *
     * @param data             byte array
     * @param dataVariableType data variable type
     * @return DataItem
     */
    public static DataItem createReq(byte[] data, EDataVariableType dataVariableType) {
        DataItem dataItem = new DataItem();
        dataItem.setReturnCode(EReturnCode.RESERVED);
        dataItem.setVariableType(dataVariableType);
        dataItem.setCount(data.length);
        dataItem.setData(data);
        return dataItem;
    }

    /**
     * Convert to DataItem data by byte array + data type
     * (Convert to DataItem data via byte array + data type)
     *
     * @param data             byte array
     * @param dataVariableType data variable type
     * @return DataItem
     */
    public static DataItem createAckBy(byte[] data, EDataVariableType dataVariableType) {
        DataItem dataItem = new DataItem();
        dataItem.setReturnCode(EReturnCode.SUCCESS);
        dataItem.setVariableType(dataVariableType);
        dataItem.setCount(data.length);
        dataItem.setData(data);
        return dataItem;
    }
}
