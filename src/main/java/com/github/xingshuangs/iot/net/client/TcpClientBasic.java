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

package com.github.xingshuangs.iot.net.client;


import com.github.xingshuangs.iot.exceptions.SocketRuntimeException;
import com.github.xingshuangs.iot.net.ICommunicable;
import com.github.xingshuangs.iot.net.SocketUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.xingshuangs.iot.common.constant.GeneralConst.LOCALHOST;

/**
 * TCP client basic class
 *
 * @author xingshuang
 */
@Slf4j
public class TcpClientBasic implements ICommunicable {

    // region Private objects

    /**
     * TAG name.
     * (TAG name)
     */
    protected String tag = "";

    /**
     * socket object.
     * (socket object)
     */
    protected Socket socket;

    /**
     * Connect timeout in millisecond, 10_000ms default.
     * (Connection timeout period, default is 10s)
     */
    protected int connectTimeout = 10_000;

    /**
     * Receive timeout in millisecond, 10_000ms default.
     * (The timeout period for receiving data is 10s by default.)
     */
    protected int receiveTimeout = 10_000;

    /**
     * Socket address.
     * (Socket address)
     */
    protected final InetSocketAddress socketAddress;

    /**
     * Flag, is socket has error.
     * (Socket error)
     */
    protected final AtomicBoolean socketError;

    /**
     * Auto-reconnect, true: auto-reconnect, false: does not auto-reconnect, auto-reconnects by default
     */
    protected boolean enableReconnect = true;

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public boolean isEnableReconnect() {
        return enableReconnect;
    }

    public void setEnableReconnect(boolean enableReconnect) {
        this.enableReconnect = enableReconnect;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(int receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    // endregion

    // region constructor
    public TcpClientBasic() {
        this(LOCALHOST, 8088);
    }

    public TcpClientBasic(String host, int port) {
        this.socketError = new AtomicBoolean(false);
        this.socketAddress = new InetSocketAddress(host, port);
    }
    // endregion

    //region Public approach

    /**
     * Check connected state.
     * (Verify the connection status, true is connected, false is disconnected)
     *
     * @return connected state，true: connected，false: disconnected.
     */
    public boolean checkConnected() {
        return !this.socketError.get() && SocketUtils.isConnected(this.socket);
    }

    /**
     * Connect server.
     * (Connect)
     *
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public void connect() {
        this.close();
        this.getAvailableSocket();
    }

    /**
     * Get available socket object.
     * (Get a valid socket object)
     *
     * @return socket object
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public Socket getAvailableSocket() {
        // The socket has been connected, and it does not support automatic reconnection, so it is returned directly
        if (this.socket != null && !this.enableReconnect) {
            return this.socket;
        }

        // Connected ones are returned directly to the socket
        if (this.checkConnected()) {
            return this.socket;
        }
        // If it is not connected, it means that it has been disconnected, and you need to manually close the socket and create a new socket
        this.close();

        try {
            // Recreate the object and connect it
            this.socket = new Socket();
            this.socket.setSoTimeout(this.receiveTimeout);
            this.socket.connect(this.socketAddress, this.connectTimeout);
            this.socketError.set(false);
            // The {{} server [{}] is created and connected successfully
            log.debug("Create socket and connect to {} server [{}] succeed", this.tag, this.socketAddress);
            this.doAfterConnected();
            return socket;
        } catch (IOException e) {
            throw new SocketRuntimeException(e);
        }
    }

    /**
     * Close socket.
     * (Socket Off)
     *
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public void close() {
        try {
            SocketUtils.close(this.socket);
        } catch (IOException e) {
            throw new SocketRuntimeException(e);
        }
    }

    //endregion

    /**
     * Do after connected.
     * Connect (the action to be done after the connection is successful)
     */
    protected void doAfterConnected() {
        // NOOP
    }

    //region Reading and writing methods

    /**
     * Write data by byte array.
     * (Write Data)
     *
     * @param data byte array
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public void write(final byte[] data) {
        this.write(data, 0, data.length);
    }

    /**
     * Write data by byte array.
     * (Write Data)
     *
     * @param data   byte array
     * @param offset the start offset in the data.
     * @param length the number of bytes to write.
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public void write(final byte[] data, final int offset, final int length) {
        this.write(data, offset, length, -1);
    }

    /**
     * Write data by byte array.
     * (Write Data)
     *
     * @param data      byte array
     * @param offset    the start offset in the data.
     * @param length    the number of bytes to write.
     * @param maxLength the maximum length allowed for a single communication,if litter than 0, then ignore. (单次通信允许的对最大长度，若小于等于0则不考虑)
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public void write(final byte[] data, final int offset, final int length, final int maxLength) {
        try {
            Socket availableSocket = this.getAvailableSocket();
            SocketUtils.write(availableSocket, data, offset, length, maxLength);
        } catch (IOException e) {
            this.socketError.set(true);
            throw new SocketRuntimeException(e);
        }
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param data byte array
     * @return the total number of bytes read into the data
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public int read(final byte[] data) {
        return this.read(data, 0, data.length, this.receiveTimeout);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param data    byte array
     * @param timeout timeout with ms, 0: no timeout
     * @return the total number of bytes read into the data
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public int read(final byte[] data, final int timeout) {
        return this.read(data, 0, data.length, timeout);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param data   byte array
     * @param offset the start offset in the data.
     * @param length the number of bytes to read.
     * @return the total number of bytes read into the data
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public int read(final byte[] data, final int offset, final int length) {
        return this.read(data, offset, length, this.receiveTimeout);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param data        byte array
     * @param offset      the start offset in the data.
     * @param length      the number of bytes to read.
     * @param waitForMore If the data is not enough, whether to wait for more data, most of them are not waiting, waiting is suitable for subcontracting sticky packages(若数据不够，是否等待，等待更多数据，大部分都是不等待的，等待都适用于分包粘包的情况)
     * @return the total number of bytes read into the data
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public int read(final byte[] data, final int offset, final int length, final boolean waitForMore) {
        return this.read(data, offset, length, 1024, this.receiveTimeout, waitForMore);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param data    byte array
     * @param offset  the start offset in the data.
     * @param length  the number of bytes to read.
     * @param timeout timeout with ms, 0: no timeout
     * @return the total number of bytes read into the data
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public int read(final byte[] data, final int offset, final int length, final int timeout) {
        return this.read(data, offset, length, -1, timeout);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param data      byte array
     * @param offset    the start offset in the data.
     * @param length    the number of bytes to read.
     * @param maxLength the maximum length allowed for a single communication,if litter than 0, then ignore. (单次通信允许的对最大长度，若小于等于0则不考虑)
     * @param timeout   timeout with ms, 0: no timeout
     * @return the total number of bytes read into the data
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public int read(final byte[] data, final int offset, final int length, final int maxLength, final int timeout) {
        return this.read(data, offset, length, maxLength, timeout, false);
    }

    /**
     * Read data and store it in the position of the specified byte array.
     * (Read Data)
     *
     * @param data        byte array
     * @param offset      the start offset in the data.
     * @param length      the number of bytes to read.
     * @param maxLength   the maximum length allowed for a single communication,if litter than 0, then ignore. (单次通信允许的对最大长度，若小于等于0则不考虑)
     * @param timeout     timeout with ms, 0: no timeout
     * @param waitForMore If the data is not enough, whether to wait for more data, most of them are not waiting, waiting is suitable for subcontracting sticky packages(若数据不够，是否等待，等待更多数据，大部分都是不等待的，等待都适用于分包粘包的情况)
     * @return the total number of bytes read into the data
     * @throws SocketRuntimeException Socket Runtime Exception
     */
    public int read(final byte[] data, final int offset, final int length, final int maxLength,
                    final int timeout, final boolean waitForMore) {
        try {
            Socket availableSocket = this.getAvailableSocket();
            return SocketUtils.read(availableSocket, data, offset, length, maxLength, timeout, waitForMore);
        } catch (IOException e) {
            this.socketError.set(true);
            throw new SocketRuntimeException(e);
        }
    }

    //endregion
}
