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

package com.github.xingshuangs.iot.common.buff;


import com.github.xingshuangs.iot.utils.*;
import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Write byte array buffer class.
 * write byte cache
 *
 * @author xingshuang
 */
@Getter
public class ByteWriteBuff {

    /**
     * 4 - or 8-bytes encoding format.
     * (4-byte or 8-byte encoding format)
     */
    private final EByteBuffFormat format;

    /**
     * Data source.
     * (data)
     */
    private final byte[] data;

    /**
     * Current offset.
     * (offset)
     */
    private int offset = 0;

    /**
     * Is little endian. The default is not, big endian mode.
     * (Whether it is little endian mode, the default is not, it is big endian mode)
     */
    private final boolean littleEndian;

    /**
     * Construct
     * (Construction method)
     *
     * @param capacity capacity
     */
    public ByteWriteBuff(int capacity) {
        this(capacity, false, EByteBuffFormat.DC_BA);
    }

    public ByteWriteBuff(int capacity, boolean littleEndian) {
        this(capacity, littleEndian, EByteBuffFormat.DC_BA);
    }

    public ByteWriteBuff(int capacity, EByteBuffFormat format) {
        this(capacity, false, format);
    }

    public ByteWriteBuff(int capacity, boolean littleEndian, EByteBuffFormat format) {
        this.littleEndian = littleEndian;
        this.data = new byte[capacity];
        this.format = format;
    }

    public static ByteWriteBuff newInstance(int capacity) {
        return new ByteWriteBuff(capacity);
    }

    public static ByteWriteBuff newInstance(int capacity, boolean littleEndian) {
        return new ByteWriteBuff(capacity, littleEndian, EByteBuffFormat.DC_BA);
    }

    public static ByteWriteBuff newInstance(int capacity, EByteBuffFormat format) {
        return new ByteWriteBuff(capacity, false, format);
    }

    public static ByteWriteBuff newInstance(int capacity, boolean littleEndian, EByteBuffFormat format) {
        return new ByteWriteBuff(capacity, littleEndian, format);
    }

    /**
     * Get a byte by byte index.
     * (Get the bytes at the specified index)
     *
     * @param index byte index
     * @return byte data
     */
    public byte getByte(int index) {
        if (index > data.length - 1) {
            throw new IndexOutOfBoundsException("index");
        }
        return this.data[index];
    }

    /**
     * Check condition.
     * (Verification conditions)
     *
     * @param desIndex     destination index
     * @param targetLength target length
     */
    private void checkCondition(int desIndex, int targetLength) {
        if (desIndex + targetLength > data.length) {
            // 超过字节数组最大容量
            throw new IllegalArgumentException("Exceeds the maximum capacity of the byte array");
        }
    }

    /**
     * Add a byte data.
     * (Add byte data)
     *
     * @param src a byte data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putByte(byte src) {
        return this.putByte(src, this.offset);
    }

    /**
     * Add a byte data by destination index.
     *
     * @param src      a byte data
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putByte(byte src, int desIndex) {
        this.checkCondition(desIndex, 1);
        this.data[desIndex] = src;
        if (this.offset == desIndex) {
            this.offset++;
        }
        return this;
    }

    /**
     * Add an int data.
     * (Add int type single-byte data)
     *
     * @param src a byte data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putByte(int src) {
        return this.putByte(ByteUtil.toByte(src));
    }

    /**
     * Add byte array.
     * (Add byte array data)
     *
     * @param src a byte data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putBytes(byte[] src) {
        return this.putBytes(src, 0, this.offset);
    }

    /**
     * Add byte array by source index.
     * (Add byte array data)
     *
     * @param src      source data
     * @param srcIndex source data index
     * @return 对象本身
     */
    public ByteWriteBuff putBytes(byte[] src, int srcIndex) {
        return this.putBytes(src, srcIndex, this.offset);
    }

    /**
     * Add byte array by source index and destination index. If destination index equal current offset,
     * then current offset +1, otherwise leave as is.
     * (Add byte array data. When desIndex==this.offset, this.offset will be offset, otherwise it will remain unchanged)
     *
     * @param src      data source
     * @param srcIndex source data index
     * @param desIndex destination data index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putBytes(byte[] src, int srcIndex, int desIndex) {
        if (src == null) {
            throw new NullPointerException("src");
        }
        this.checkCondition(desIndex, src.length - srcIndex);
        System.arraycopy(src, srcIndex, this.data, desIndex, src.length - srcIndex);
        if (desIndex == this.offset) {
            this.offset += src.length;
        }
        return this;
    }

    /**
     * Add a short data, 2-bytes.
     * (Add short type data)
     *
     * @param src a short data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putShort(short src) {
        return this.putShort(src, this.offset, this.littleEndian);
    }

    /**
     * Add a short data by destination index, 2-bytes.
     * (Add short type data)
     *
     * @param src      a short data
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putShort(short src, int desIndex) {
        return this.putShort(src, desIndex, this.littleEndian);
    }

    /**
     * Add a short data from int data, 2-bytes.
     * (Add short type data)
     *
     * @param src a int data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putShort(int src) {
        return this.putBytes(ShortUtil.toByteArray(src, this.littleEndian));
    }

    /**
     * Add a short data from int data by destination index, 2-bytes.
     * (Add short data)
     *
     * @param src      a int data
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putShort(int src, int desIndex) {
        return this.putBytes(ShortUtil.toByteArray(src, this.littleEndian), 0, desIndex);
    }

    /**
     * Add a int data, 4-bytes.
     * (Add integer data)
     *
     * @param src a int data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putInteger(int src) {
        return this.putInteger(src, this.offset, this.littleEndian);
    }

    /**
     * Add an int data by destination index, 4-bytes.
     * Add Integer data
     *
     * @param src      a int data
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putInteger(int src, int desIndex) {
        return this.putInteger(src, desIndex, this.littleEndian);
    }

    /**
     * Add an int data from a long data, 4-bytes.
     * (Add integer data)
     *
     * @param src a long data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putInteger(long src) {
        return this.putInteger(src, this.offset, this.littleEndian);
    }

    /**
     * Add an int data from a long data by destination index, 4-bytes.
     * (Add Integer data)
     *
     * @param src      a long data
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putInteger(long src, int desIndex) {
        return this.putInteger(src, desIndex, this.littleEndian);
    }

    /**
     * Add a long data, 8-bytes.
     * (Add long data)
     *
     * @param src a long data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putLong(long src) {
        return this.putLong(src, this.offset, this.littleEndian);
    }

    /**
     * Add a long data by destination index, 8-bytes.
     * (Add long data)
     *
     * @param src      a long data
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putLong(long src, int desIndex) {
        return this.putLong(src, desIndex, this.littleEndian);
    }

    /**
     * Add a float data, 4-bytes.
     * (Add float data)
     *
     * @param src a float data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putFloat(float src) {
        return this.putFloat(src, this.offset, this.littleEndian);
    }

    /**
     * Add a float data by destination index, 4-bytes.
     * (Add float data)
     *
     * @param src      a float data
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putFloat(float src, int desIndex) {
        return this.putFloat(src, desIndex, this.littleEndian);
    }

    /**
     * Add a double data, 8-bytes.
     * (Add double data)
     *
     * @param src a double data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putDouble(double src) {
        return this.putDouble(src, this.offset, this.littleEndian);
    }

    /**
     * Add a double data by destination index, 8-bytes.
     * (Add double data)
     *
     * @param src      a double data
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putDouble(double src, int desIndex) {
        return this.putDouble(src, desIndex, this.littleEndian);
    }

    /**
     * Add a string data.
     * (Add string data)
     *
     * @param src a string data
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putString(String src) {
        return this.putString(src, StandardCharsets.UTF_8, this.offset);
    }

    /**
     * Add a string data by charsets.
     * (Add string data)
     *
     * @param src      a string data
     * @param charsets target charsets
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putString(String src, Charset charsets) {
        return this.putString(src, charsets, this.offset);
    }

    /**
     * Add a short data by destination index and endian, 2-bytes.
     * (Add short data)
     *
     * @param src          a short data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putShort(short src, int desIndex, boolean littleEndian) {
        return this.putBytes(ShortUtil.toByteArray(src, littleEndian), 0, desIndex);
    }

    /**
     * Add a short data from an int data by destination index and endian, 2-bytes.
     * (Add short data)
     *
     * @param src          a int data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putShort(int src, int desIndex, boolean littleEndian) {
        return this.putBytes(ShortUtil.toByteArray(src, littleEndian), 0, desIndex);
    }

    /**
     * Add an int data by destination index and endian, 4-bytes.
     * (Add integer data)
     *
     * @param src          a int data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putInteger(int src, int desIndex, boolean littleEndian) {
        return this.putBytes(this.format.formatIn4Bytes(IntegerUtil.toByteArray(src, littleEndian)), 0, desIndex);
    }

    /**
     * Add an int data from a long data by destination index and endian, 4-bytes.
     * (Add integer data)
     *
     * @param src          a long data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putInteger(long src, int desIndex, boolean littleEndian) {
        return this.putBytes(this.format.formatIn4Bytes(IntegerUtil.toByteArray((int) src, littleEndian)), 0, desIndex);
    }

    /**
     * Add an int data from a long data by destination index, endian and format, 4-bytes.
     * (Add integer data, for special EByteBuffFormat processing)
     *
     * @param src          a long data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @param format       EByteBuffFormat format
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putInteger(long src, int desIndex, boolean littleEndian, EByteBuffFormat format) {
        return this.putBytes(format.formatIn4Bytes(IntegerUtil.toByteArray((int) src, littleEndian)), 0, desIndex);
    }

    /**
     * Add a long data by destination index and endian, 8-bytes.
     * (Add long data)
     *
     * @param src          a long data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putLong(long src, int desIndex, boolean littleEndian) {
        return this.putBytes(this.format.formatIn8Bytes(LongUtil.toByteArray(src, littleEndian)), 0, desIndex);
    }

    /**
     * Add a long data by destination index, endian and format, 8-bytes.
     * (Add long data, for special EByteBuffFormat processing)
     *
     * @param src          a long data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @param format       EByteBuffFormat format
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putLong(long src, int desIndex, boolean littleEndian, EByteBuffFormat format) {
        return this.putBytes(format.formatIn8Bytes(LongUtil.toByteArray(src, littleEndian)), 0, desIndex);
    }

    /**
     * Add a float data by destination index and endian, 4-bytes.
     * (Add float data)
     *
     * @param src          a float data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putFloat(float src, int desIndex, boolean littleEndian) {
        return this.putBytes(this.format.formatIn4Bytes(FloatUtil.toByteArray(src, littleEndian)), 0, desIndex);
    }

    /**
     * Add a float data by destination index, endian and format, 4-bytes.
     * (Add float data for processing of special EByteBuffFormat)
     *
     * @param src          a float data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @param format       EByteBuffFormat format
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putFloat(float src, int desIndex, boolean littleEndian, EByteBuffFormat format) {
        return this.putBytes(format.formatIn4Bytes(FloatUtil.toByteArray(src, littleEndian)), 0, desIndex);
    }

    /**
     * Add a double data by destination index and endian, 8-bytes.
     * (Add double data)
     *
     * @param src          a long data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putDouble(double src, int desIndex, boolean littleEndian) {
        return this.putBytes(this.format.formatIn8Bytes(FloatUtil.toByteArray(src, littleEndian)), 0, desIndex);
    }

    /**
     * Add a double data by destination index, endian and format, 8-bytes.
     * (Add double data for special EByteBuffFormat processing)
     *
     * @param src          a long data
     * @param desIndex     destination index
     * @param littleEndian is little endian
     * @param format       EByteBuffFormat format
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putDouble(double src, int desIndex, boolean littleEndian, EByteBuffFormat format) {
        return this.putBytes(format.formatIn8Bytes(FloatUtil.toByteArray(src, littleEndian)), 0, desIndex);
    }

    /**
     * Add a string data.
     * (Add string)
     *
     * @param src      a string data
     * @param charsets charsets
     * @param desIndex destination index
     * @return ByteWriteBuff itself
     */
    public ByteWriteBuff putString(String src, Charset charsets, int desIndex) {
        return this.putBytes(src.getBytes(charsets), 0, desIndex);
    }
}
