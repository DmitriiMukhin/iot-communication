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


import com.github.xingshuangs.iot.common.buff.ByteReadBuff;
import com.github.xingshuangs.iot.common.buff.ByteWriteBuff;
import com.github.xingshuangs.iot.protocol.s7.enums.EArea;
import com.github.xingshuangs.iot.protocol.s7.enums.EParamVariableType;
import com.github.xingshuangs.iot.protocol.s7.enums.ESyntaxID;
import com.github.xingshuangs.iot.utils.IntegerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request item.
 * Standard data read request items
 *
 * @author xingshuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestItem extends RequestBaseItem {

    public static final int BYTE_LENGTH = 12;

    public RequestItem() {
        this.specificationType = (byte) 0x12;
        this.lengthOfFollowing = 0x0A;
        this.syntaxId = ESyntaxID.S7ANY;
    }

    /**
     * Variable type.
     * Variable type and length BIT, BYTE, WORD, DWORD, COUNTER<br>
     * Byte size: 1 <br>
     * Byte ordinal: 3
     */
    private EParamVariableType variableType = EParamVariableType.BYTE;

    /**
     * Data length.
     * Read Length <br>
     * Byte size: 2 <br>
     * Byte ordinal: 4-5
     */
    private int count = 0x0000;

    /**
     * DB number.
     * DB number. If the access is not to a DB area, this is 0x0000 <br>
     * Byte size: 2 <br>
     * Byte ordinal: 6-7
     */
    private int dbNumber = 0x0000;

    /**
     * Area.
     * Storage area type DB storage area <br>
     * Byte size: 1 <br>
     * Byte ordinal: 8
     */
    private EArea area = EArea.INPUTS;

    /**
     * Byte address.
     * Byte address, located 3 bytes into the starting byte address address, counting starts from the 4th bit <br>
     * Byte size: 3 <br>
     * Byte ordinal: 9-11
     */
    private int byteAddress = 0;

    /**
     * Bit address.
     * Bit address, located at the last 3 bits of the 3 bytes in the starting byte address address
     */
    private int bitAddress = 0;

    @Override
    public int byteArrayLength() {
        return BYTE_LENGTH;
    }

    @Override
    public byte[] toByteArray() {
        return ByteWriteBuff.newInstance(BYTE_LENGTH)
                .putByte(this.specificationType)
                .putByte(this.lengthOfFollowing)
                .putByte(this.syntaxId.getCode())
                .putByte(this.variableType.getCode())
                .putShort(this.count)
                .putShort(this.dbNumber)
                .putByte(this.area.getCode())
                // There are only 3 bytes, so only the next 3 bytes are taken and the first byte is discarded
                .putBytes(IntegerUtil.toByteArray((this.byteAddress << 3) + this.bitAddress), 1)
                .getData();
    }

    /**
     * Copy.
     * Duplicate a new object
     *
     * @return requestItem
     */
    public RequestItem copy() {
        RequestItem requestItem = new RequestItem();
        requestItem.specificationType = this.specificationType;
        requestItem.lengthOfFollowing = this.lengthOfFollowing;
        requestItem.syntaxId = this.syntaxId;
        requestItem.variableType = this.variableType;
        requestItem.count = this.count;
        requestItem.dbNumber = this.dbNumber;
        requestItem.area = this.area;
        requestItem.byteAddress = this.byteAddress;
        requestItem.bitAddress = this.bitAddress;
        return requestItem;
    }

    /**
     * Parses byte array and converts it to object.
     *
     * @param data byte array
     * @return RequestItem
     */
    public static RequestItem fromBytes(final byte[] data) {
        return fromBytes(data, 0);
    }

    /**
     * Parses byte array and converts it to object.
     *
     * @param data   byte array
     * @param offset index offset
     * @return RequestItem
     */
    public static RequestItem fromBytes(final byte[] data, final int offset) {
        ByteReadBuff buff = new ByteReadBuff(data, offset);
        RequestItem requestItem = new RequestItem();
        requestItem.specificationType = buff.getByte();
        requestItem.lengthOfFollowing = buff.getByteToInt();
        requestItem.syntaxId = ESyntaxID.from(buff.getByte());
        requestItem.variableType = EParamVariableType.from(buff.getByte());
        requestItem.count = buff.getUInt16();
        requestItem.dbNumber = buff.getUInt16();
        requestItem.area = EArea.from(buff.getByte());
        requestItem.byteAddress = IntegerUtil.toInt32In3Bytes(data, 9 + offset) >> 3;
        requestItem.bitAddress = buff.getByte(11 + offset) & 0x07;
        return requestItem;
    }

    /**
     * Create request item.
     * Create a request item with parameters
     *
     * @param variableType variable type parameter type
     * @param count data count number of data
     * @param area area
     * @param dbNumber db number DB block number
     * @param byteAddress byte address byte address
     * @param bitAddress bit address bit address
     * @return request item
     */
    public static RequestItem createByParams(EParamVariableType variableType, int count, EArea area, int dbNumber, int byteAddress, int bitAddress) {
        RequestItem requestItem = new RequestItem();
        requestItem.setVariableType(variableType);
        requestItem.setCount(count);
        requestItem.setArea(area);
        requestItem.setDbNumber(dbNumber);
        requestItem.setByteAddress(byteAddress);
        requestItem.setBitAddress(bitAddress);
        return requestItem;
    }
}
