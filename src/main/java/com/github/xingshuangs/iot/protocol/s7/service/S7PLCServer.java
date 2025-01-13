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


import com.github.xingshuangs.iot.common.buff.ByteReadBuff;
import com.github.xingshuangs.iot.exceptions.SocketRuntimeException;
import com.github.xingshuangs.iot.net.SocketUtils;
import com.github.xingshuangs.iot.net.server.TcpServerBasic;
import com.github.xingshuangs.iot.protocol.s7.enums.*;
import com.github.xingshuangs.iot.protocol.s7.model.*;
import com.github.xingshuangs.iot.protocol.s7.utils.AddressUtil;
import com.github.xingshuangs.iot.utils.BooleanUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * S7 plc server class.
 * S7 PLC server
 *
 * @author xingshuang
 */
@Slf4j
public class S7PLCServer extends TcpServerBasic {

    /**
     * Locker.
     * (Data Map operation lock)
     */
    private final Object objLock = new Object();

    /**
     * Write and read lock.
     * (read-write lock)
     */
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * All data for operating.
     * (all data)
     */
    protected final HashMap<String, byte[]> dataMap = new HashMap<>();

    public S7PLCServer() {
        this(102);
    }

    public S7PLCServer(int port) {
        this.port = port;
        this.dataMap.put("DB1", new byte[65536]);
        this.dataMap.put("M", new byte[65536]);
        this.dataMap.put("I", new byte[65536]);
        this.dataMap.put("Q", new byte[65536]);
        this.dataMap.put("T", new byte[65536]);
        this.dataMap.put("C", new byte[65536]);
    }

    /**
     * Gets the currently available area.
     * (Get currently available regions)
     *
     * @return data areas
     */
    public Set<String> getAvailableAreas() {
        synchronized (this.objLock) {
            return this.dataMap.keySet();
        }
    }

    /**
     * Add DB area when needed.
     * (Add required DB blocks)
     *
     * @param dbNumbers db number
     */
    public void addDBArea(int...dbNumbers) {
        log.debug("Add DB{} to server data area", dbNumbers);
        synchronized (this.objLock) {
            for (int x : dbNumbers) {
                String name = String.format("DB%s", x);
                this.dataMap.computeIfAbsent(name, key -> new byte[65536]);
            }
        }
    }

    @Override
    protected boolean checkHandshake(Socket socket) {
        // Verify the connection request
        S7Data s7Data = this.readS7DataFromClient(socket);
        if (!(s7Data.getCotp() instanceof COTPConnection)
                || s7Data.getCotp().getPduType() != EPduType.CONNECT_REQUEST) {
            // Client [{}] handshake failed, not a connection request
            log.error("Client [{}] Handshake failed, not connection request", socket.getRemoteSocketAddress());
            return false;
        }
        S7Data connectConfirm = S7Data.createConnectConfirm(s7Data);
        this.write(socket, connectConfirm.toByteArray());

        // Verify setup
        s7Data = this.readS7DataFromClient(socket);
        if (!(s7Data.getCotp() instanceof COTPData)
                || s7Data.getCotp().getPduType() != EPduType.DT_DATA) {
            // Client [{}] handshake failed, not parameter setting
            log.error("Client [{}] handshake failed, not parameter setting", socket.getRemoteSocketAddress());
            return false;
        }
        S7Data connectAckDtData = S7Data.createConnectAckDtData(s7Data);
        this.write(socket, connectAckDtData.toByteArray());
        log.debug("The client [{}] handshake succeeded", socket.getRemoteSocketAddress());
        return true;
    }

    @Override
    protected void doClientHandle(Socket socket) {
        S7Data req = this.readS7DataFromClient(socket);
        if (!(req.getCotp() instanceof COTPData)
                || req.getCotp().getPduType() != EPduType.DT_DATA
                || req.getHeader().getMessageType() != EMessageType.JOB) {
            S7Data response = S7Data.createErrorResponse(req, EErrorClass.ERROR_ON_SUPPLIES, 0x8500);
            this.write(socket, response.toByteArray());
        }

        try {
            switch (req.getParameter().getFunctionCode()) {
                case READ_VARIABLE:
                    this.readVariableHandle(socket, req);
                    return;
                case WRITE_VARIABLE:
                    this.writeVariableHandle(socket, req);
                    return;
                default:
                    S7Data response = S7Data.createErrorResponse(req, EErrorClass.ERROR_ON_SUPPLIES, 0x8500);
                    this.write(socket, response.toByteArray());
            }
        } catch (Exception e) {
            S7Data response = S7Data.createErrorResponse(req, EErrorClass.ERROR_ON_SERVICE_PROCESSING, 0x8404);
            this.write(socket, response.toByteArray());
        }
    }

    /**
     * Read data handler.
     * (Read data processing)
     *
     * @param socket socket object
     * @param req    request data
     */
    private void readVariableHandle(Socket socket, S7Data req) {
        ReadWriteParameter parameter = (ReadWriteParameter) req.getParameter();
        List<ReturnItem> returnItems = new ArrayList<>();
        try {
            this.rwLock.readLock().lock();
            parameter.getRequestItems().forEach(p1 -> {
                RequestItem p = (RequestItem) p1;
                // Determine whether data exists in the region
                String area = AddressUtil.parseArea(p);
                if (!this.dataMap.containsKey(area)) {
                    // Client [{}] reads [{}] data, region [{}], byte index [{}], bit index [{}], length [{}], no address data for this region
                    log.error("Client[{}] read [{}] data, area[{}], byte index[{}], bit index[{}], length[{}], no the address data",
                            socket.getRemoteSocketAddress(), p.getVariableType(), area, p.getByteAddress(), p.getBitAddress(), p.getCount());
                    returnItems.add(ReturnItem.createDefault(EReturnCode.OBJECT_DOES_NOT_EXIST));
                    return;
                }
                // Extract byte data for a specified address
                byte[] bytes = this.dataMap.get(area);
                ByteReadBuff buff = new ByteReadBuff(bytes);
                byte[] data;
                if (p.getVariableType() == EParamVariableType.BYTE) {
                    data = buff.getBytes(p.getByteAddress(), p.getCount());
                } else {
                    byte oldData = buff.getByte(p.getByteAddress());
                    data = BooleanUtil.getValue(oldData, p.getBitAddress()) ? new byte[]{(byte) 0x01} : new byte[]{(byte) 0x00};
                }
                // Client [{}] reads [{}] data, region [{}], byte index [{}], bit index [{}], length [{}], region address data {}
                log.debug("Client[{}] read [{}] data, area[{}], byte index[{}], bit index[{}], length[{}], address data{}",
                        socket.getRemoteSocketAddress(), p.getVariableType(), area, p.getByteAddress(), p.getBitAddress(), p.getCount(), data);
                DataItem dataItem = DataItem.createAckBy(data, p.getVariableType() == EParamVariableType.BYTE ? EDataVariableType.BYTE_WORD_DWORD : EDataVariableType.BIT);
                returnItems.add(dataItem);
            });
        } finally {
            this.rwLock.readLock().unlock();
        }
        S7Data ack = S7Data.createReadWriteResponse(req, returnItems);
        this.write(socket, ack.toByteArray());
    }

    /**
     * Write data handler.
     * (Write data processing)
     *
     * @param socket socket object
     * @param req    request data
     */
    private void writeVariableHandle(Socket socket, S7Data req) {
        ReadWriteParameter parameter = (ReadWriteParameter) req.getParameter();
        ReadWriteDatum datum = (ReadWriteDatum) req.getDatum();
        List<DataItem> dataItems = datum.getReturnItems().stream().map(DataItem.class::cast).collect(Collectors.toList());
        List<ReturnItem> returnItems = new ArrayList<>();
        try {
            this.rwLock.writeLock().lock();
            for (int i = 0; i < parameter.getItemCount(); i++) {
                RequestItem p = (RequestItem) (parameter.getRequestItems().get(i));
                DataItem d = dataItems.get(i);
                // Determine whether data exists in the region
                String area = AddressUtil.parseArea(p);
                if (!this.dataMap.containsKey(area)) {
                    // Client [{}] writes [{}] data, region [{}], byte index [{}], bit index [{}], length [{}], and no region address
                    log.error("Client[{}] write [{}] data, area[{}], byte index[{}], bit index[{}], length[{}], no the address data",
                            socket.getRemoteSocketAddress(), p.getVariableType(), area, p.getByteAddress(), p.getBitAddress(), p.getCount());
                    returnItems.add(ReturnItem.createDefault(EReturnCode.OBJECT_DOES_NOT_EXIST));
                    continue;
                }
                // Write data to the specified address
                byte[] bytes = this.dataMap.get(area);
                if (p.getVariableType() == EParamVariableType.BYTE) {
                    System.arraycopy(d.getData(), 0, bytes, p.getByteAddress(), d.getData().length);
                } else {
                    byte newData = BooleanUtil.setBit(bytes[p.getByteAddress()], p.getBitAddress(), d.getData()[0] == 1);
                    System.arraycopy(new byte[]{newData}, 0, bytes, p.getByteAddress(), 1);
                }
                // Client[{}] writes [{}] data, region [{}], byte index [{}], bit index [{}], length [{}], region address data {}
                log.debug("Client[{}] write [{}] data, area[{}], byte index[{}], bit index[{}], length[{}], address data{}",
                        socket.getRemoteSocketAddress(), p.getVariableType(), area, p.getByteAddress(), p.getBitAddress(), p.getCount(), d.getData());
                returnItems.add(ReturnItem.createDefault(EReturnCode.SUCCESS));
            }
        } finally {
            this.rwLock.writeLock().unlock();
        }

        S7Data ack = S7Data.createReadWriteResponse(req, returnItems);
        this.write(socket, ack.toByteArray());
    }

    /**
     * Read S7 data from client.
     * (Read data from the S7 protocol)
     *
     * @param socket socket object
     * @return S7Data
     */
    private S7Data readS7DataFromClient(Socket socket) {
        byte[] data = this.readClientData(socket);
        return S7Data.fromBytes(data);
    }

    /**
     * Rewrite read client data, sticky packet data processing
     * (Rewrite and read client data, data processing for sticky packets)
     *
     * @param socket client socket object
     * @return byte array
     */
    @Override
    protected byte[] readClientData(Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            int firstByte = in.read();
            if (firstByte == -1) {
                SocketUtils.close(socket);
                throw new SocketRuntimeException("The client is disconnected.");
            }
            // Get TPKT first
            byte[] tpktData = new byte[4];
            tpktData[0] = (byte) firstByte;
            this.read(socket, tpktData, 1, 3, 1024, 0, true);
            TPKT tpkt = TPKT.fromBytes(tpktData);
            // Fetch the rest of the content based on the length of the interior
            byte[] data = new byte[tpkt.getLength()];
            System.arraycopy(tpktData, 0, data, 0, tpktData.length);
            this.read(socket, data, 4, data.length - 4, 1024, 0, true);
            return data;
        } catch (IOException e) {
            throw new SocketRuntimeException(e);
        }
    }
}
