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


import com.github.xingshuangs.iot.common.IObjectByteArray;
import com.github.xingshuangs.iot.common.buff.ByteReadBuff;
import com.github.xingshuangs.iot.common.buff.ByteWriteBuff;
import com.github.xingshuangs.iot.protocol.s7.enums.EDestinationFileSystem;
import com.github.xingshuangs.iot.protocol.s7.enums.EFileBlockType;
import com.github.xingshuangs.iot.protocol.s7.enums.EFunctionCode;
import com.github.xingshuangs.iot.utils.BooleanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Download parameter.
 * Download parameters
 *
 * @author xingshuang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DownloadParameter extends UploadAckParameter implements IObjectByteArray {

    /**
     * Unknown bytes, 2-bytes
     * Unknown byte, 2 bytes
     */
    protected byte[] errorCode = new byte[]{0x01, 0x00};

    /**
     * Download id, 4-bytes.
     * Download ID, 4 bytes (not used)
     */
    protected long id = 0x00000000;

    /**
     * File name length, 1-byte.
     * File name length, 1 byte
     */
    protected int fileNameLength = 9;

    /**
     * File id, 1-byte.
     * File id, 1 byte
     */
    protected String fileIdentifier = "_";

    /**
     * File block type, 2-bytes.
     * Data block type, 2 bytes
     */
    protected EFileBlockType blockType = EFileBlockType.OB;

    /**
     * Block number, 5-bytes.
     * Data block number, 5 bytes, range 00000-99999
     */
    protected int blockNumber = 0;

    /**
     * Destination file system, 1-byte.
     * Target file system, 1 byte
     */
    protected EDestinationFileSystem destinationFileSystem = EDestinationFileSystem.P;


    public DownloadParameter() {
        this.functionCode = EFunctionCode.DOWNLOAD;
    }

    @Override
    public int byteArrayLength() {
        return 18;
    }

    @Override
    public byte[] toByteArray() {
        return ByteWriteBuff.newInstance(18)
                .putByte(this.functionCode.getCode())
                .putByte((byte) (BooleanUtil.setBit(0, this.moreDataFollowing) | BooleanUtil.setBit(1, this.errorStatus)))
                .putBytes(this.errorCode)
                .putInteger(this.id)
                .putByte(this.fileNameLength)
                .putString(this.fileIdentifier)
                .putBytes(this.blockType.getByteArray())
                .putString(String.format("%05d", this.blockNumber))
                .putByte(this.destinationFileSystem.getCode())
                .getData();
    }

    /**
     * Parses byte array and converts it to object.
     *
     * @param data byte array
     * @return DownloadParameter
     */
    public static DownloadParameter fromBytes(final byte[] data) {
        return fromBytes(data, 0);
    }

    /**
     * Parses byte array and converts it to object.
     *
     * @param data byte array
     * @param offset index offset
     * @return DownloadParameter
     */
    public static DownloadParameter fromBytes(final byte[] data, final int offset) {
        if (data.length < 18) {
            throw new IndexOutOfBoundsException("DownloadParameter, data length < 18");
        }
        ByteReadBuff buff = new ByteReadBuff(data, offset);
        DownloadParameter res = new DownloadParameter();
        res.functionCode = EFunctionCode.from(buff.getByte());
        byte b = buff.getByte();
        res.moreDataFollowing = BooleanUtil.getValue(b, 0);
        res.errorStatus = BooleanUtil.getValue(b, 1);
        res.errorCode = buff.getBytes(2);
        res.id = buff.getUInt32();
        res.fileNameLength = buff.getByteToInt();
        res.fileIdentifier = buff.getString(1);
        res.blockType = EFileBlockType.from(buff.getString(2));
        res.blockNumber = Integer.parseInt(buff.getString(5));
        res.destinationFileSystem = EDestinationFileSystem.from(buff.getByte());
        return res;
    }

    /**
     * Create default download parameter.
     * (Create default download parameters)
     *
     * @param blockType block type data block type
     * @param blockNumber block number data block number
     * @param destinationFileSystem destination file system target file system
     * @param moreDataFollowing more data following whether there is more data
     * @return DownloadParameter
     */
    public static DownloadParameter createDefault(EFileBlockType blockType,
                                                  int blockNumber,
                                                  EDestinationFileSystem destinationFileSystem,
                                                  boolean moreDataFollowing) {
        DownloadParameter parameter = new DownloadParameter();
        parameter.blockType = blockType;
        parameter.blockNumber = blockNumber;
        parameter.destinationFileSystem = destinationFileSystem;
        parameter.setMoreDataFollowing(moreDataFollowing);
        return parameter;
    }
}
