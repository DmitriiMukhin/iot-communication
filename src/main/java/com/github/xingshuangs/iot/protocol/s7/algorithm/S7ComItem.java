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

package com.github.xingshuangs.iot.protocol.s7.algorithm;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Group item.
 * (Consolidation)
 *
 * @author xingshuang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class S7ComItem {

    /**
     * Data index.
     * Data indexing
     */
    private int index;

    /**
     * Raw data size.
     * (Raw data size)
     */
    private int rawSize;

    /**
     * Split index offset.
     * (Split point, i.e., data offset index)
     */
    private int splitOffset;

    /**
     * Ripe data size after split.
     * (Split data size)
     */
    private int ripeSize;

    /**
     * Extra data size.
     * (Additional required data size)
     */
    private int extraSize;

    /**
     * Threshold.
     * (Threshold)
     */
    private int threshold = 0;

    /**
     * Get total length of item.
     * (Entire length)
     *
     * @return total length
     */
    public int getTotalLength() {
        return Math.max(this.ripeSize + this.extraSize, this.threshold);
    }
}
