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

package com.github.xingshuangs.iot.common.serializer;


import com.github.xingshuangs.iot.common.buff.ByteReadBuff;
import com.github.xingshuangs.iot.common.buff.ByteWriteBuff;
import com.github.xingshuangs.iot.exceptions.ByteArrayParseException;
import com.github.xingshuangs.iot.utils.BooleanUtil;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.xingshuangs.iot.common.enums.EDataType.BOOL;
import static com.github.xingshuangs.iot.common.enums.EDataType.STRING;

/**
 * Tool of byte array serialize class.
 * (byte array serialization tool)
 *
 * @author xingshuang
 */
public class ByteArraySerializer implements IByteArraySerializable {

    public static ByteArraySerializer newInstance() {
        return new ByteArraySerializer();
    }

    @Override
    public <T> T toObject(final Class<T> targetClass, final byte[] src) {
        try {
            final T bean = targetClass.newInstance();
            for (final Field field : targetClass.getDeclaredFields()) {
                final ByteArrayVariable variable = field.getAnnotation(ByteArrayVariable.class);
                if (variable == null) {
                    continue;
                }
                ByteArrayParameter parameter = new ByteArrayParameter(variable.byteOffset(), variable.bitOffset(),
                        variable.count(), variable.type(), variable.littleEndian(), variable.format());
                this.checkByteArrayVariable(parameter);
                this.extractData(src, bean, field, parameter);
            }
            return bean;
        } catch (Exception e) {
            // Parse into object error, reason:
            throw new ByteArrayParseException("parsing to object error, cause:" + e.getMessage(), e);
        }
    }

    /**
     * To object and extract parameter.
     * (Convert to object, extract parameter data)
     *
     * @param parameter byte array parameter
     * @param src       byte array source
     * @return parameter with value
     */
    public ByteArrayParameter extractParameter(final ByteArrayParameter parameter, final byte[] src) {
        return this.extractParameter(Collections.singletonList(parameter), src).get(0);
    }

    /**
     * To list, and extract parameter.
     * (Convert to list object and extract parameter data)
     *
     * @param parameters list parameter.
     * @param src        byte array data.
     * @return list parameter with value.
     */
    public List<ByteArrayParameter> extractParameter(final List<ByteArrayParameter> parameters, final byte[] src) {
        try {
            for (final ByteArrayParameter parameter : parameters) {
                if (parameter == null) {
                    throw new ByteArrayParseException("null exists in the list of ByteArrayParameter");
                }

                this.checkByteArrayVariable(parameter);
                Field field = parameter.getClass().getDeclaredField("value");
                this.extractData(src, parameter, field, parameter);
            }
            return parameters;
        } catch (Exception e) {
            throw new ByteArrayParseException("parsing to object error, cause:" + e.getMessage(), e);
        }
    }

    @Override
    public <T> byte[] toByteArray(final T targetBean) {
        try {
            // Assemble data while calculating maximum byte length
            int buffSize = 0;
            List<ByteArrayParseData> parseDataList = new ArrayList<>();
            for (final Field field : targetBean.getClass().getDeclaredFields()) {
                final ByteArrayVariable variable = field.getAnnotation(ByteArrayVariable.class);
                if (variable == null) {
                    continue;
                }
                ByteArrayParameter parameter = new ByteArrayParameter(variable.byteOffset(), variable.bitOffset(),
                        variable.count(), variable.type(), variable.littleEndian(), variable.format());
                this.checkByteArrayVariable(parameter);
                parseDataList.add(new ByteArrayParseData(variable, field));
                int maxPos = variable.byteOffset() + variable.count() * variable.type().getByteLength();
                if (maxPos > buffSize) {
                    buffSize = maxPos;
                }
            }
            if (buffSize == 0 || parseDataList.isEmpty()) {
                return new byte[0];
            }
            // Fill the contents of the byte array
            ByteWriteBuff buff = ByteWriteBuff.newInstance(buffSize);
            for (ByteArrayParseData item : parseDataList) {
                item.getField().setAccessible(true);
                Object data = item.getField().get(targetBean);
                if (data == null) {
                    continue;
                }
                if (item.getVariable().count() == 1) {
                    this.fillOneData(item.getVariable(), data, buff, 0);
                } else {
                    this.fillListData(item.getVariable(), data, buff);
                }
            }
            return buff.getData();
        } catch (Exception e) {
            throw new ByteArrayParseException("parsing to object error, cause:" + e.getMessage(), e);
        }
    }

    /**
     * Extract target data by condition.
     * (extract data)
     *
     * @param src      byte array
     * @param bean     bean object
     * @param field    field
     * @param variable byte array parameter
     * @param <T>      type
     * @throws IllegalAccessException illegal access exception
     */
    private <T> void extractData(byte[] src, T bean, Field field, ByteArrayParameter variable) throws IllegalAccessException {
        ByteReadBuff buff = new ByteReadBuff(src, 0, variable.isLittleEndian(), variable.getFormat());
        field.setAccessible(true);
        switch (variable.getType()) {
            case BOOL:
                List<Boolean> booleans = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> {
                            int byteAdd = variable.getByteOffset() + (variable.getBitOffset() + x) / 8;
                            int bitAdd = (variable.getBitOffset() + x) % 8;
                            return buff.getBoolean(byteAdd, bitAdd);
                        }).collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? booleans.get(0) : booleans);
                break;
            case BYTE:
                List<Byte> bytes = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> buff.getByte(variable.getByteOffset() + x * variable.getType().getByteLength()))
                        .collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? bytes.get(0) : bytes);
                break;
            case UINT16:
                List<Integer> uint16s = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> buff.getUInt16(variable.getByteOffset() + x * variable.getType().getByteLength()))
                        .collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? uint16s.get(0) : uint16s);
                break;
            case INT16:
                List<Short> int16s = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> buff.getInt16(variable.getByteOffset() + x * variable.getType().getByteLength()))
                        .collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? int16s.get(0) : int16s);
                break;
            case UINT32:
                List<Long> uint32s = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> buff.getUInt32(variable.getByteOffset() + x * variable.getType().getByteLength()))
                        .collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? uint32s.get(0) : uint32s);
                break;
            case INT32:
                List<Integer> int32s = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> buff.getInt32(variable.getByteOffset() + x * variable.getType().getByteLength()))
                        .collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? int32s.get(0) : int32s);
                break;
            case INT64:
                List<Long> int64s = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> buff.getInt64(variable.getByteOffset() + x * variable.getType().getByteLength()))
                        .collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? int64s.get(0) : int64s);
                break;
            case FLOAT32:
                List<Float> float32s = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> buff.getFloat32(variable.getByteOffset() + x * variable.getType().getByteLength()))
                        .collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? float32s.get(0) : float32s);
                break;
            case FLOAT64:
                List<Double> float64s = IntStream.range(0, variable.getCount())
                        .mapToObj(x -> buff.getFloat64(variable.getByteOffset() + x * variable.getType().getByteLength()))
                        .collect(Collectors.toList());
                field.set(bean, variable.getCount() == 1 ? float64s.get(0) : float64s);
                break;
            case STRING:
                field.set(bean, buff.getString(variable.getByteOffset(), variable.getCount()));
                break;
            default:
                // Unable to identify data type when extracting data
                throw new ByteArrayParseException("The data type can not be recognized when extracting the data");
        }
    }

    /**
     * Check byte array variable.
     * (Verify parameters of byte array annotation)
     *
     * @param variable variable
     */
    private void checkByteArrayVariable(ByteArrayParameter variable) {
        if (variable.getByteOffset() < 0) {
            // The byte offset cannot be negative
            throw new ByteArrayParseException("The byte offset can't be negative");
        }
        if (variable.getCount() < 0) {
            // The number of data cannot be negative
            throw new ByteArrayParseException("The number of data can't be negative");
        }
        if (variable.getType() == BOOL && (variable.getBitOffset() > 7 || variable.getBitOffset() < 0)) {
            // When the data type is bool, the offset can only be [0,7]
            throw new ByteArrayParseException("When the data type is bool, the bit offset can only be [0,7].");
        }
    }

    /**
     * Fill with one data.
     * (Populate a data)
     *
     * @param variable annotation of byte array variable
     * @param data     data object
     * @param buff     write byte buffer
     * @param index    target index
     */
    private void fillOneData(ByteArrayVariable variable, Object data, ByteWriteBuff buff, int index) {
        switch (variable.type()) {
            case BOOL:
                int byteAdd = variable.byteOffset() + (variable.bitOffset() + index) / 8;
                int bitAdd = (variable.bitOffset() + index) % 8;
                byte newByte = BooleanUtil.setBit(buff.getByte(byteAdd), bitAdd, (Boolean) data);
                buff.putByte(newByte, byteAdd);
                break;
            case BYTE:
                buff.putByte((Byte) data, variable.byteOffset() + index * variable.type().getByteLength());
                break;
            case UINT16:
                buff.putShort((Integer) data, variable.byteOffset() + index * variable.type().getByteLength(), variable.littleEndian());
                break;
            case INT16:
                buff.putShort((Short) data, variable.byteOffset() + index * variable.type().getByteLength(), variable.littleEndian());
                break;
            case UINT32:
                buff.putInteger((Long) data, variable.byteOffset() + index * variable.type().getByteLength(), variable.littleEndian(), variable.format());
                break;
            case INT32:
                buff.putInteger((Integer) data, variable.byteOffset() + index * variable.type().getByteLength(), variable.littleEndian(), variable.format());
                break;
            case INT64:
                buff.putLong((Long) data, variable.byteOffset() + index * variable.type().getByteLength(), variable.littleEndian(), variable.format());
                break;
            case FLOAT32:
                buff.putFloat((Float) data, variable.byteOffset() + index * variable.type().getByteLength(), variable.littleEndian(), variable.format());
                break;
            case FLOAT64:
                buff.putDouble((Double) data, variable.byteOffset() + index * variable.type().getByteLength(), variable.littleEndian(), variable.format());
                break;
            case STRING:
                buff.putString((String) data, StandardCharsets.UTF_8, variable.byteOffset());
                break;
            default:
                // The data type is not recognized when the data is populated
                throw new ByteArrayParseException("The data type can not be recognized when populating the data");
        }
    }

    /**
     * Fill with list data.
     * (Populated with multiple data, must be list data)
     *
     * @param variable annotation of byte array variable
     * @param data     data object
     * @param buff     write byte buffer
     */
    private void fillListData(ByteArrayVariable variable, Object data, ByteWriteBuff buff) {
        if (variable.type() == STRING) {
            buff.putString((String) data, StandardCharsets.UTF_8, variable.byteOffset());
        } else {
            List<Object> list = (List<Object>) data;
            for (int i = 0; i < list.size(); i++) {
                this.fillOneData(variable, list.get(i), buff, i);
            }
        }
    }
}
