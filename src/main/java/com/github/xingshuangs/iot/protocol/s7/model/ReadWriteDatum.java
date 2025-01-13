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


import com.github.xingshuangs.iot.common.buff.ByteWriteBuff;
import com.github.xingshuangs.iot.protocol.s7.enums.EFunctionCode;
import com.github.xingshuangs.iot.protocol.s7.enums.EMessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Read write data.
 * Read and write data
 *
 * @author xingshuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReadWriteDatum extends Datum {

    /**
     * Return items.
     * (data item)
     */
    private final List<ReturnItem> returnItems = new ArrayList<>();

    @Override
    public int byteArrayLength() {
        if (this.returnItems.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < this.returnItems.size(); i++) {
            int length = this.returnItems.get(i).byteArrayLength();
            sum += length;
            // When the data is not the last one, if the data length is an odd number, the S7 protocol will fill one more byte to keep its number to an even number (the last odd-length data does not need to be filled)
            if (i != this.returnItems.size() - 1
                    && length % 2 == 1
                    && this.returnItems.get(i) instanceof DataItem) {
                sum++;
            }
        }
        return sum;
    }

    @Override
    public byte[] toByteArray() {
        if (this.returnItems.isEmpty()) {
            return new byte[0];
        }
        ByteWriteBuff buff = ByteWriteBuff.newInstance(this.byteArrayLength());
        for (int i = 0; i < this.returnItems.size(); i++) {
            int length = this.returnItems.get(i).byteArrayLength();
            buff.putBytes(this.returnItems.get(i).toByteArray());
            // When the data is not the last, if the data length is odd, the S7 protocol will pad one more byte
            // to keep the number even (the last odd-length data does not need to be filled)
            if (i != this.returnItems.size() - 1
                    && length % 2 == 1
                    && this.returnItems.get(i) instanceof DataItem) {
                buff.putByte(0x00);
            }
        }
        return buff.getData();
    }

    /**
     * Add item.
     * (Add Data Item)
     *
     * @param item item
     */
    public void addItem(ReturnItem item) {
        this.returnItems.add(item);
    }

    /**
     * Add items.
     * (Add data items in bulk)
     *
     * @param items item list
     */
    public void addItem(Collection<? extends ReturnItem> items) {
        this.returnItems.addAll(items);
    }

    /**
     * Parses byte array and converts it to object.
     * (Byte array data is parsed according to message type and function code)
     *
     * @param data         byte array Byte array data
     * @param messageType  message type The type of message in the header
     * @param functionCode function code The function code of the parameter section
     * @return ReadWriteDatum
     */
    public static ReadWriteDatum fromBytes(final byte[] data, EMessageType messageType, EFunctionCode functionCode) {
        ReadWriteDatum datum = new ReadWriteDatum();
        if (data.length == 0) {
            return datum;
        }
        int offset = 0;
        byte[] remain = data;
        while (true) {
            ReturnItem dataItem;
            // Special processing is performed on the response results of write operations
            if (EMessageType.ACK_DATA == messageType && EFunctionCode.WRITE_VARIABLE == functionCode) {
                dataItem = ReturnItem.fromBytes(remain);
                datum.returnItems.add(dataItem);
                offset += dataItem.byteArrayLength();
            } else {
                dataItem = DataItem.fromBytes(remain);
                datum.returnItems.add(dataItem);
                offset += dataItem.byteArrayLength();
                // When the data is not the last, if the data length is odd, the S7 protocol will pad one more byte
                // to keep the number even (the last odd-length data does not need to be filled)
                if (dataItem.byteArrayLength() % 2 == 1) {
                    offset++;
                }
            }
            if (offset >= data.length) {
                break;
            }
            remain = Arrays.copyOfRange(data, offset, data.length);
        }
        return datum;
    }

    /**
     * Create read and write data.
     * (Create Data Datum)
     *
     * @param dataItems data items
     * @return Datum
     */
    public static ReadWriteDatum createDatum(Collection<? extends ReturnItem> dataItems) {
        ReadWriteDatum datum = new ReadWriteDatum();
        datum.addItem(dataItems);
        return datum;
    }
}
