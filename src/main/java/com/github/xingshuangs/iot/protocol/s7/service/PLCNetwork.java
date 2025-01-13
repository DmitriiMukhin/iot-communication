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
import com.github.xingshuangs.iot.common.buff.ByteWriteBuff;
import com.github.xingshuangs.iot.common.constant.GeneralConst;
import com.github.xingshuangs.iot.exceptions.S7CommException;
import com.github.xingshuangs.iot.net.client.TcpClientBasic;
import com.github.xingshuangs.iot.protocol.s7.algorithm.S7ComGroup;
import com.github.xingshuangs.iot.protocol.s7.algorithm.S7ComItem;
import com.github.xingshuangs.iot.protocol.s7.algorithm.S7SequentialGroupAlg;
import com.github.xingshuangs.iot.protocol.s7.constant.ErrorCode;
import com.github.xingshuangs.iot.protocol.s7.enums.*;
import com.github.xingshuangs.iot.protocol.s7.model.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Network communication of plc
 * The maximum read byte array size is 240-18=222, 480-18=462, 960-18=942
 * According to test S1200 [CPU 1214C], read multiple bytes at a time
 * Send: The maximum byte read length is 216 = 240 - 24, 24 (PDU of the request message) = 10 (header) + 14 (parameter)
 * Receiving: The maximum byte read length is 222 = 240 - 18, 18 (PDU of response message) = 12 (header) + 2 (parameter) + 4 (dataItem)
 * According to test S1200 [CPU 1214C], write multiple bytes at a time
 * Send: The maximum byte write length is 212 = 240 - 28, 28 (PDU of the request message) = 10 (header) + 14 (parameter) + 4 (dataItem)
 * Receiving: The maximum byte write length is 225 = 240 - 15, 15 (PDU of response message) = 12 (header) + 2 (parameter) + 1 (dataItem)
 *
 * @author xingshuang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("DuplicatedCode")
@Slf4j
public class PLCNetwork extends TcpClientBasic {

    /**
     * locker.
     */
    private final Object objLock = new Object();

    /**
     * PLC type.
     * (Type of PLC)
     */
    protected EPlcType plcType = EPlcType.S1200;

    /**
     * PLC rack.
     * (PLC rack number)
     */
    protected int rack = 0;

    /**
     * PLC Slot.
     * (PLC slot number, S7-300 = 2)
     */
    protected int slot = 1;

    /**
     * PDU length, different PLC corresponding to different values, there are 240,480,960.
     * (The maximum PDU length, different PLC corresponds to different values, including 240, 480, 960, the current default is 240)
     */
    protected int pduLength;

    /**
     * Persistence, true: long connection, false: short connection.
     * (Whether to persist, the default is persistence, corresponding to long connection, true: long connection, false: short connection)
     */
    private boolean persistence = true;

    /**
     * Communication callback, first parameter is tag, second is package content.
     * (Communication callback, the first parameter is the tag label, indicating the meaning of the message; the second parameter is the specific message content)
     */
    private BiConsumer<String, byte[]> comCallback;

    public PLCNetwork() {
        super();
    }

    public PLCNetwork(String host, int port) {
        super(host, port);
        this.tag = "S7";
    }

    @Override
    public void connect() {
        try {
            super.connect();
        } finally {
            if (!this.persistence) {
                this.close();
            }
        }
    }

    //region socket handshake operation after connection

    /**
     * Do after connected.
     * (What to do after the connection is successful)
     */
    @Override
    protected void doAfterConnected() {
        this.connectionRequest();
        // The PDULength set exists! = the actual PDULength of the PLC, so the PLC's PDULength shall prevail.
        this.pduLength = this.connectDtData();
        log.debug("PLC[{}] handshake success, rack[{}]，slot[{}]，PDULength[{}]", this.plcType, this.rack, this.slot, this.pduLength);
    }

    /**
     * Connection request.
     * (Connection Request)
     * <p>
     * TSAP contains two bytes. The remote TSAP address is the address set by the connected remote PC Access.
     * The first byte identifies the resource being accessed. 01 is PG, 02 is OP, 03 is S7 unilateral (server mode), and 10 (hexadecimal) and above are S7 bilateral communication.
     * The second byte is the access point, which is the CPU slot number + CP slot number
     * First byte: 0x01+connection number (S7-200) or 0x03+connection number (S7-300/400)
     * Second byte: module position (S7-200) or rack and slot (S7-300/400)
     * 1500 1200 300 400 200 200Smart
     * 0x0102 0x0102 0x0102 0x0102 0x4d57 0x1000
     * 0x0100 0x0100 0x0102 0x0103 0x4d57 0x0300
     * ------------------------------------------------
     * 0x0100 0x0100 0x0100 0x4d57 0x1000
     * 0x0300 0x0300 0x0302 0x0303 0x4d57 0x0300
     */    private void connectionRequest() {
        // Corresponds to 0xC1
        int local = 0x0100;
        // Corresponds to 0xC2 | Tested: S1200 supports 0x0100, 0x0200, 0x0300, S200Smart supports 0x0200, 0x0300
        int remote = 0x0300;
        switch (this.plcType) {
            case S200:
                // What is written in S7net is 0x1000, 0x1001
                local = 0x4D57;
                remote = 0x4D57;
                break;
            case S200_SMART:
                local = 0x1000;
                // Remote can only be set to 0x0200, 0x0201, 0x0300, 0x0301
                remote = 0x0300;
                break;
            case S300:
            case S400:
            case S1200:
            case S1500:
                remote += 0x20 * this.rack + this.slot;
                break;
            case SINUMERIK_828D:
                local = 0x0400;
                remote = 0x0D04;
                break;
        }
        S7Data req = S7Data.createConnectRequest(local, remote);
        S7Data ack = this.readFromServer(req);
        if (ack.getCotp().getPduType() != EPduType.CONNECT_CONFIRM) {
            // Connection request refused
            throw new S7CommException("The connection request was denied");
        }
    }

    /**
     * Connection setup.
     * (Connect setup)
     *
     * @return pduLength pdu长度
     */
    private int connectDtData() {
        S7Data req = S7Data.createConnectDtData(this.pduLength);
        S7Data ack = this.readFromServer(req);
        if (ack.getCotp().getPduType() != EPduType.DT_DATA) {
            // Connection Setup response error
            throw new S7CommException("Connection Setup response error");
        }
        if (ack.getHeader() == null || ack.getHeader().byteArrayLength() != AckHeader.BYTE_LENGTH) {
            // Connection Setup response error, missing response header or insufficient response header length [12]
            throw new S7CommException("Connection Setup response error, missing response header or insufficient response header length [12]");
        }
        int length = ((SetupComParameter) ack.getParameter()).getPduLength();
        if (length <= 0) {
            // The maximum length of PDU is less than 0
            throw new S7CommException("The maximum length of a PDU is less than 0");
        }
        return length;
    }
    //endregion

    //region underlying data communication part

    /**
     * Read data from server, core interaction.
     * (Read data from server)
     *
     * @param req req data
     * @return ack data
     */
    private S7Data readFromServer(S7Data req) {
        byte[] sendData = req.toByteArray();
        byte[] total = this.readFromServer(sendData);
        S7Data ack = S7Data.fromBytes(total);
        this.checkPostedCom(req, ack);
        return ack;
    }

    /**
     * Data interaction with the server as byte array
     * (Interact data with the server in the form of byte arrays)
     *
     * @param sendData byte array of request
     * @return byte array of response
     */
    private byte[] readFromServer(byte[] sendData) {
        if (this.comCallback != null) {
            this.comCallback.accept(GeneralConst.PACKAGE_REQ, sendData);
        }

        // Subtract TPKT and COTP from the message, leaving the content of PDU, 7=4(tpkt)+3(cotp)
        if (this.pduLength > 0 && sendData.length - 7 > this.pduLength) {
            // The number of bytes sent in the request is too long [%d] and is greater than the maximum PDU length [%d]
            throw new S7CommException(String.format("The number of bytes sent for the request is too long [%d], which is larger than the maximum PDU length [%d].", sendData.length, this.pduLength));
        }

        TPKT tpkt;
        int len;
        byte[] total;
        synchronized (this.objLock) {
            this.write(sendData);

            byte[] data = new byte[TPKT.BYTE_LENGTH];
            len = this.read(data);
            if (len < TPKT.BYTE_LENGTH) {
                // Invalid TPKT, inconsistent length
                throw new S7CommException("The TPKT is invalid and the length is inconsistent");
            }
            tpkt = TPKT.fromBytes(data);
            total = new byte[tpkt.getLength()];
            System.arraycopy(data, 0, total, 0, data.length);
            len = this.read(total, TPKT.BYTE_LENGTH, tpkt.getLength() - TPKT.BYTE_LENGTH);
        }
        if (len < total.length - TPKT.BYTE_LENGTH) {
            // The data length behind TPKT is inconsistent.
            throw new S7CommException("The length of the data after TPKT is inconsistent");
        }
        if (this.comCallback != null) {
            this.comCallback.accept(GeneralConst.PACKAGE_ACK, total);
        }
        return total;
    }

    /**
     * Contains persistent reads from the server, external inheritance uses this method for interaction, not internal use.
     * (Contains persistence to read data from the server. External inheritance uses this method to interact, but it is not used internally.)
     *
     * @param req req data
     * @return ack data
     */
    public S7Data readFromServerByPersistence(S7Data req) {
        try {
            return this.readFromServer(req);
        } finally {
            if (!this.persistence) {
                this.close();
            }
        }
    }

    /**
     * Contains persistent reads from the server, external inheritance uses this method for interaction, not internal use.
     * (Contains persistence to read data from the server. External inheritance uses this method to interact, but it is not used internally.)
     *
     * @param req req data
     * @return ack data
     */
    public byte[] readFromServerByPersistence(byte[] req) {
        try {
            return this.readFromServer(req);
        } finally {
            if (!this.persistence) {
                this.close();
            }
        }
    }

    /**
     * Post-communication processing, once verifying the request and response data.
     * (Post-communication processing, verifying the request and response data once)
     *
     * @param req req data
     * @param ack ack data
     */
    private void checkPostedCom(S7Data req, S7Data ack) {
        if (ack.getHeader() == null) {
            return;
        }
        // Response headers are correct
        AckHeader ackHeader = (AckHeader) ack.getHeader();
        if (ackHeader.getErrorClass() == null) {
            // Response exception, unknown exception
            throw new S7CommException(String.format("Response exception, unknown exception：%s", ErrorCode.MAP.getOrDefault(ackHeader.getErrorCode(), "The error code does not exist")));
        }
        if (ackHeader.getErrorClass() != EErrorClass.NO_ERROR) {
            // Response exception, error type: %s, error reason
            throw new S7CommException(String.format("Response exception, error type: %s, error cause：%s",
                    ackHeader.getErrorClass().getDescription(), ErrorCode.MAP.getOrDefault(ackHeader.getErrorCode(), "The error code does not exist")));
        }
        // The PDU numbers sent and received are consistent
        if (ackHeader.getPduReference() != req.getHeader().getPduReference()) {
            // The pdu reference number is inconsistent and the data is incorrect.
            throw new S7CommException("The PDU references are inconsistent, causing incorrect data");
        }
        if (ack.getDatum() == null) {
            return;
        }
        if (!(ack.getDatum() instanceof ReadWriteDatum)) {
            return;
        }
        ReadWriteDatum datum = (ReadWriteDatum) ack.getDatum();
        // The requested data quantity is consistent
        List<ReturnItem> returnItems = datum.getReturnItems();
        ReadWriteParameter parameter = (ReadWriteParameter) req.getParameter();
        if (returnItems.size() != parameter.getItemCount()) {
            // The number of data returned is inconsistent with the number of data requested
            throw new S7CommException("The returned data quantity is different from the requested data quantity");
        }
        // Return result verification
        for (int i = 0; i < returnItems.size(); i++) {
            if (returnItems.get(i).getReturnCode() != EReturnCode.SUCCESS) {
                // The [%d]th result returned is abnormal, reason: %s
                throw new S7CommException(String.format("Return [%d] result exception, cause: %s", i + 1, returnItems.get(i).getReturnCode().getDescription()));
            }
        }
    }

    //endregion

    //region S7 data reading and writing part

    /**
     * Read S7 data.
     * (Read S7 protocol data)
     *
     * @param requestItems request items
     * @return ack data items
     */
    public List<DataItem> readS7Data(List<RequestItem> requestItems) {
        if (requestItems == null || requestItems.isEmpty()) {
            // The request item is missing and the data cannot be obtained
            throw new S7CommException("The request item is missing and the data cannot be retrieved");
        }
        // Extract each request data size based on the original request list
        List<Integer> rawNumbers = requestItems.stream().map(RequestItem::getCount).collect(Collectors.toList());
        // Build the final result list based on the original request list
        List<DataItem> resultList = requestItems.stream().map(x -> DataItem.createReq(new byte[x.getCount()],
                        x.getVariableType() == EParamVariableType.BIT ? EDataVariableType.BIT : EDataVariableType.BYTE_WORD_DWORD))
                .collect(Collectors.toList());

        // Get the grouping result according to the sequential grouping algorithm,
        // Send: 12=10(header)+2(before parameter),12(after parameter)
        // Receive: 14=12(header)+2(parameter),5(DataItem), dataItem may be 4 or 5, 5 is used uniformly
        List<S7ComGroup> s7ComGroups = S7SequentialGroupAlg.readRecombination(rawNumbers, this.pduLength - 14, 5, 12);
        try {
            s7ComGroups.forEach(x -> {
                // Build the corresponding request list based on the grouping
                List<S7ComItem> comItemList = x.getItems();
                List<RequestItem> newRequestItems = comItemList.stream().map(i -> {
                    RequestItem item = requestItems.get(i.getIndex()).copy();
                    item.setCount(i.getRipeSize());
                    item.setByteAddress(item.getByteAddress() + i.getSplitOffset());
                    return item;
                }).collect(Collectors.toList());

                // S7 data request
                S7Data req = S7Data.createReadRequest(newRequestItems);
                S7Data ack = this.readFromServer(req);
                ReadWriteDatum datum = (ReadWriteDatum) ack.getDatum();
                List<DataItem> dataItems = datum.getReturnItems().stream().map(DataItem.class::cast).collect(Collectors.toList());

                // Reload the obtained data into the actual result list
                for (int i = 0; i < comItemList.size(); i++) {
                    S7ComItem comItem = comItemList.get(i);
                    byte[] src = dataItems.get(i).getData();
                    byte[] des = resultList.get(comItem.getIndex()).getData();
                    System.arraycopy(src, 0, des, comItem.getSplitOffset(), src.length);
                }
            });
            return resultList;
        } finally {
            if (!this.persistence) {
                this.close();
            }
        }
    }

    /**
     * Read S7 data.
     * (Read S7 protocol data)
     *
     * @param requestItem request item
     * @return ack data item
     */
    public DataItem readS7Data(RequestItem requestItem) {
        return this.readS7Data(Collections.singletonList(requestItem)).get(0);
    }

    /**
     * Write S7 data.
     * (Write S7 protocol data)
     *
     * @param requestItem request item
     * @param dataItem    data item
     */
    public void writeS7Data(RequestItem requestItem, DataItem dataItem) {
        this.writeS7Data(Collections.singletonList(requestItem), Collections.singletonList(dataItem));
    }

    /**
     * Write S7 data.
     * (Write S7 protocol)
     *
     * @param requestItems request items
     * @param dataItems    data items
     */
    public void writeS7Data(List<RequestItem> requestItems, List<DataItem> dataItems) {
        if (requestItems.size() != dataItems.size()) {
            // During the write operation, the number of requestItems and dataItems data is inconsistent.
            throw new S7CommException("During the write operation, the number of requestItems and dataItems is inconsistent. Procedure");
        }

        // Extract each request data size based on the original request list
        List<Integer> rawNumbers = requestItems.stream().map(RequestItem::getCount).collect(Collectors.toList());

        // Get the grouping result according to the sequential grouping algorithm
        // Send: 12=10(header)+2(before parameter), 17=12(after parameter)+5(dataItem), dataItem may be 4 or 5, 5 is used uniformly
        // Receive: 14=12(header)+2(parameter),1(DataItem)
        List<S7ComGroup> s7ComGroups = S7SequentialGroupAlg.writeRecombination(rawNumbers, this.pduLength - 12, 17);
        try {
            s7ComGroups.forEach(x -> {
                // Build the corresponding request list based on the grouping
                List<S7ComItem> comItemList = x.getItems();
                List<RequestItem> newRequestItems = comItemList.stream().map(i -> {
                    RequestItem item = requestItems.get(i.getIndex()).copy();
                    item.setCount(i.getRipeSize());
                    item.setByteAddress(item.getByteAddress() + i.getSplitOffset());
                    return item;
                }).collect(Collectors.toList());
                // Build the corresponding data list based on the grouping
                List<DataItem> newDataItems = comItemList.stream().map(i -> {
                    DataItem item = dataItems.get(i.getIndex()).copy();
                    item.setCount(i.getRipeSize());
                    item.setData(ByteReadBuff.newInstance(item.getData()).getBytes(i.getSplitOffset(), i.getRipeSize()));
                    return item;
                }).collect(Collectors.toList());

                // S7 data request
                S7Data req = S7Data.createWriteRequest(newRequestItems, newDataItems);
                this.readFromServer(req);
            });
        } finally {
            if (!this.persistence) {
                this.close();
            }
        }
    }

    //endregion

    //region read NCK data

    /**
     * Read S7 nck data.
     * (Read S7 protocol NCK data)
     *
     * @param requestItem request item.
     * @return ack data item
     */
    public DataItem readS7NckData(RequestNckItem requestItem) {
        return this.readS7NckData(Collections.singletonList(requestItem)).get(0);
    }

    /**
     * Read S7 nck data. It is not possible to limit the number of requests precisely because the content length of the response varies
     * (Reading S7 protocol NCK data cannot accurately limit the number of requests because the length of the response content is variable.)
     *
     * @param requestItems request items
     * @return data items
     */
    public List<DataItem> readS7NckData(List<RequestNckItem> requestItems) {
        try {
            S7Data s7Data = NckRequestBuilder.creatNckRequest(requestItems);
            S7Data ack = this.readFromServer(s7Data);
            ReadWriteDatum datum = (ReadWriteDatum) ack.getDatum();
            return datum.getReturnItems().stream().map(DataItem.class::cast).collect(Collectors.toList());
        } finally {
            if (!this.persistence) {
                this.close();
            }
        }
    }

    //endregion

    //region upload download

    /**
     * Downloading files has been successfully tested on the s200smart.
     * (Download the file and successfully tested it in s200smart)
     *
     * @param mc7 Mc7File file object
     */
    public void downloadFile(Mc7File mc7) {
        try {
            // start download
            EDestinationFileSystem destinationFileSystem = EDestinationFileSystem.P;
            S7Data reqStartDownload = S7Data.createStartDownload(mc7.getBlockType(), mc7.getBlockNumber(), destinationFileSystem,
                    mc7.getLoadMemoryLength(), mc7.getMC7CodeLength());
            this.readFromServer(reqStartDownload);

            // downloading
            ByteReadBuff buff = new ByteReadBuff(mc7.getData());
            while (buff.getRemainSize() > 0) {
                boolean moreDataFollowing = buff.getRemainSize() > this.pduLength - 32;
                byte[] tmpData = buff.getBytes(Math.min(buff.getRemainSize(), this.pduLength - 32));
                S7Data reqDownload = S7Data.createDownload(mc7.getBlockType(), mc7.getBlockNumber(), destinationFileSystem, moreDataFollowing, tmpData);
                this.readFromServer(reqDownload);
            }

            // download ends
            S7Data reqEndDownload = S7Data.createEndDownload(mc7.getBlockType(), mc7.getBlockNumber(), destinationFileSystem);
            this.readFromServer(reqEndDownload);
        } finally {
            if (!this.persistence) {
                this.close();
            }
        }
    }

    /**
     * Uploading file content from PLC to PC has been successfully tested in s200smart
     * (Uploading file content from PLC to PC has been successfully tested in s200smart)
     *
     * @param blockType   block type 数据块类型
     * @param blockNumber block number 数据块编号
     * @return byte array
     */
    public byte[] uploadFile(EFileBlockType blockType, int blockNumber) {
        try {
            // start upload
            S7Data reqStartDownload = S7Data.createStartUpload(blockType, blockNumber, EDestinationFileSystem.A);
            S7Data ackStartDownload = this.readFromServer(reqStartDownload);
            StartUploadAckParameter startUploadAckParameter = (StartUploadAckParameter) ackStartDownload.getParameter();

            // uploading
            ByteWriteBuff buff = new ByteWriteBuff(startUploadAckParameter.getBlockLength());
            UploadAckParameter uploadAckParameter = new UploadAckParameter();
            uploadAckParameter.setMoreDataFollowing(true);
            while (uploadAckParameter.isMoreDataFollowing()) {
                S7Data reqUpload = S7Data.createUpload(startUploadAckParameter.getId());
                S7Data ackUpload = this.readFromServer(reqUpload);
                uploadAckParameter = (UploadAckParameter) ackUpload.getParameter();
                if (uploadAckParameter.isErrorStatus()) {
                    throw new S7CommException("Upload error occurred");
                }
                UpDownloadDatum datum = (UpDownloadDatum) ackUpload.getDatum();
                buff.putBytes(datum.getData());
            }

            // upload ends
            S7Data reqEndUpload = S7Data.createEndUpload(startUploadAckParameter.getId());
            this.readFromServer(reqEndUpload);
            return buff.getData();
        } finally {
            if (!this.persistence) {
                this.close();
            }
        }
    }
    //endregion
}
