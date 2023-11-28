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

package com.github.xingshuangs.iot.protocol.mp4.model;


import com.github.xingshuangs.iot.protocol.common.buff.ByteWriteBuff;
import com.github.xingshuangs.iot.protocol.mp4.enums.EMp4Type;

/**
 * Track Fragment Base Media Decode Time Box(tfdt), 主要是用来存放相关sample编码的绝对时间的,
 * 因为FMP4是流式的格式，所以不像MP4一样可以直接根据sample直接seek到具体位置。
 *
 * @author xingshuang
 */
public class Mp4TfdtBox extends Mp4Box {

    /**
     * 1字节，版本
     */
    private final int version;

    /**
     * 3字节为flags
     */
    private final byte[] flags;

    /**
     * 4字节Base media decode time，基准解码时间，从0开始，主要计算每个分片的时间偏移，所以其等于前一个基准时间+前一个分片所有sample
     * 持续时间的总和，第一个分片的基准时间为0，例如每个分片包含一个sample，每个sample持续时间为3600，那么第一个分片的基准解码时间为0.
     * 第二个分片为0+3600=3600，第三个分片等于3600+3600=7200，如此类推
     */
    private final long baseMediaDecodeTime;

    public Mp4TfdtBox(long baseMediaDecodeTime) {
        this.mp4Type = EMp4Type.TFDT;
        this.version = 0;
        this.flags = new byte[3];
        this.baseMediaDecodeTime = baseMediaDecodeTime;
    }

    @Override
    public int byteArrayLength() {
        return 16;
    }

    @Override
    public byte[] toByteArray() {
        int size = this.byteArrayLength();
        return ByteWriteBuff.newInstance(size)
                .putInteger(size)
                .putBytes(this.mp4Type.getByteArray())
                .putByte(this.version)
                .putBytes(this.flags)
                .putInteger(this.baseMediaDecodeTime)
                .getData();
    }
}
