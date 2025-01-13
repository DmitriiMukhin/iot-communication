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

package com.github.xingshuangs.iot.net;


import com.github.xingshuangs.iot.exceptions.SocketRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Socket communication util.
 * Socket communication tools
 *
 * @author xingshuang
 */
public class SocketUtils {

    private SocketUtils() {
        // NOOP
    }

    /**
     * Close socket.
     * (Socket Off)
     *
     * @param socket socket object
     * @throws IOException IO exception
     */
    public static void close(final Socket socket) throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * Get connected status, true：connected，false：disconnected.
     * (Connection status)
     *
     * @param socket socket object
     * @return true：connected，false：disconnected
     */
    public static boolean isConnected(final Socket socket) {
        return socket != null && (socket.isConnected() && !socket.isClosed());
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param socket socket object
     * @param data   byte array
     * @return the total number of bytes read into the data
     * @throws IOException IO Exception
     */
    public static int read(final Socket socket, final byte[] data) throws IOException {
        return read(socket, data, 0, data.length, -1, 0);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param socket socket object
     * @param data   byte array
     * @param offset the start offset in the data.
     * @param length the number of bytes to read.
     * @return the total number of bytes read into the data
     * @throws IOException IO Exception
     */
    public static int read(final Socket socket, final byte[] data, int offset, final int length) throws IOException {
        return read(socket, data, offset, length, -1, 0);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param socket    socket object
     * @param data      byte array
     * @param offset    the start offset in the data.
     * @param length    the number of bytes to read.
     * @param maxLength the maximum length allowed for a single communication,if litter than 0, then ignore. (单次通信允许的对最大长度，若小于等于0则不考虑)
     * @return the total number of bytes read into the data
     * @throws IOException IO Exception
     */
    public static int read(final Socket socket, final byte[] data, int offset, final int length, final int maxLength) throws IOException {
        return read(socket, data, offset, length, maxLength, 0);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param socket    socket object
     * @param data      byte array
     * @param offset    the start offset in the data.
     * @param length    the number of bytes to read.
     * @param maxLength the maximum length allowed for a single communication,if litter than 0, then ignore. (单次通信允许的对最大长度，若小于等于0则不考虑)
     * @param timeout   timeout with ms, 0: no timeout
     * @return the total number of bytes read into the data
     * @throws IOException IO Exception
     */
    public static int read(final Socket socket, final byte[] data, final int offset, final int length,
                           final int maxLength, final int timeout) throws IOException {
        return read(socket, data, offset, length, maxLength, timeout, false);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param socket      socket object
     * @param data        byte array
     * @param offset      the start offset in the data.
     * @param length      the number of bytes to read.
     * @param maxLength   the maximum length allowed for a single communication,if litter than 0, then ignore. (单次通信允许的对最大长度，若小于等于0则不考虑)
     * @param timeout     timeout with ms, 0: no timeout
     * @param waitForMore If the data is not enough, whether to wait for more data, most of them are not waiting, waiting is suitable for subcontracting sticky packages(若数据不够，是否等待，等待更多数据，大部分都是不等待的，等待都适用于分包粘包的情况)
     * @return the total number of bytes read into the data
     * @throws IOException IO Exception
     */
    public static int read(final Socket socket, final byte[] data, final int offset, final int length,
                           final int maxLength, final int timeout, final boolean waitForMore) throws IOException {
        if (offset + length > data.length) {
            throw new IllegalArgumentException("offset+length");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout>=0");
        }
        // Blocking does not refer to the length of time it takes to read,
        // it can be understood that there is no data to read, and the thread is waiting here
        socket.setSoTimeout(timeout);
        // Number of reads
        int count = 0;
        // The amount of translation in the read position will change
        int off = offset;
        InputStream in = socket.getInputStream();
        while (count < length) {
            int len = maxLength <= 0 ? length - count : Math.min(maxLength, length - count);
            int num = in.read(data, off, len);
            if (num < 0) {
                // The read data is abnormal, the data is not read, and the connection is disconnected
                throw new SocketRuntimeException("The end of the stream has been reached, and disconnected");
            }
            count += num;
            off += num;
            // If the actual length of the data read is smaller than expected, it proves that the read has been completed
            if (!waitForMore && num < len) {
                break;
            }
        }
        return count;
    }

    /**
     * Write data by byte array.
     * (Write Data)
     *
     * @param socket socket object
     * @param data   byte array
     * @throws IOException IO Exception
     */
    public static void write(final Socket socket, final byte[] data) throws IOException {
        write(socket, data, 0, data.length, -1);
    }

    /**
     * Write data by byte array.
     * (Write Data)
     *
     * @param socket socket object
     * @param data   byte array
     * @param offset the start offset in the data.
     * @param length the number of bytes to write.
     * @throws IOException IO Exception
     */
    public static void write(final Socket socket, final byte[] data, int offset, final int length) throws IOException {
        write(socket, data, offset, length, -1);
    }

    /**
     * Write data by byte array.
     * (Write Data)
     *
     * @param socket    socket object
     * @param data      byte array
     * @param offset    the start offset in the data.
     * @param length    the number of bytes to write.
     * @param maxLength the maximum length allowed for a single communication,if litter than 0, then ignore. (单次通信允许的对最大长度，若小于等于0则不考虑)
     * @throws IOException IO Exception
     */
    public static void write(final Socket socket, final byte[] data, final int offset, final int length, final int maxLength) throws IOException {
        if (offset + length > data.length) {
            throw new IllegalArgumentException("offset+length");
        }
        // Number of writes
        int count = 0;
        // The amount of translation of the write position, will change
        int off = offset;
        OutputStream out = socket.getOutputStream();
        while (count < length) {
            int len = maxLength <= 0 ? length - count : Math.min(maxLength, length - count);
            out.write(data, off, len);
            off += len;
            count += len;
        }
    }
}
