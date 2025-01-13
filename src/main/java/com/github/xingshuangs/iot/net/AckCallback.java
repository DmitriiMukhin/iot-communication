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

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Response callback class.
 * (Callback in response)
 *
 * @author xingshuang
 */
@Getter
public class AckCallback<T> {

    /**
     * Response data.
     * (Response Data)
     */
    protected T ackData;

    /**
     * Data listener for callback.
     * (Data Response Listener)
     */
    protected final IAckListener<T> listener;

    /**
     * Flag, is completed.
     * (Completed or not)
     */
    protected boolean completed;

    /**
     * Error message for callback.
     * (Error Message)
     */
    protected String errorMessage;

    /**
     * Timeout in millisecond.
     * (Timeout ms)
     */
    protected final long timeoutMs;

    /**
     * Lock object.
     * (Lock Object)
     */
    protected final Object lock = new Object();

    /**
     * Object create time.
     * (Created)
     */
    protected final LocalDateTime createTime = LocalDateTime.now();


    public AckCallback() {
        this(null);
    }

    public AckCallback(IAckListener<T> listener) {
        this.ackData = null;
        this.completed = false;
        this.timeoutMs = 10_000;
        this.listener = listener;
    }

    /**
     * Response success.
     * (Successful Response)
     *
     * @param ackPackage response package data
     */
    public void responseOk(T ackPackage) {
        this.ackData = ackPackage;
        this.completed = true;
        this.errorMessage = "";
        synchronized (this.lock) {
            this.lock.notifyAll();
        }
        if (this.listener != null) {
            CompletableFuture.runAsync(() -> this.listener.ok(ackPackage));
        }
    }

    /**
     * Response failed.
     * (Failed Response)
     *
     * @param message error message
     */
    public void responseFail(String message) {
        this.ackData = null;
        this.completed = true;
        this.errorMessage = message;
        synchronized (this.lock) {
            this.lock.notifyAll();
        }
        if (this.listener != null) {
            CompletableFuture.runAsync(() -> this.listener.fail(message));
        }
    }

    /**
     * Wait for more data.
     * (Waiting for data)
     */
    public void waitData() {
        try {
            synchronized (this.lock) {
                this.lock.wait(this.timeoutMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Wait for more data.
     * (Waiting for data)
     *
     * @param timeout timeout in millisecond
     */
    public void waitData(long timeout) {
        try {
            synchronized (this.lock) {
                this.lock.wait(timeout);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
