/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.store;

import java.util.concurrent.atomic.AtomicLong;


/**
 * ���ü������࣬������C++����ָ��ʵ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
 */
public abstract class ReferenceResource {
    protected final AtomicLong refCount = new AtomicLong(1);
    protected volatile boolean available = true;
    protected volatile boolean cleanupOver = false;
    private volatile long firstShutdownTimestamp = 0;


    /**
     * ��Դ�Ƿ���HOLDס
     */
    public synchronized boolean hold() {
        if (this.isAvailable()) {
            if (this.refCount.getAndIncrement() > 0) {
                return true;
            }
            else {
                this.refCount.getAndDecrement();
            }
        }

        return false;
    }


    /**
     * �ͷ���Դ
     */
    public void release() {
        long value = this.refCount.decrementAndGet();
        if (value > 0)
            return;

        synchronized (this) {
            // cleanup�ڲ�Ҫ���Ƿ�clean������
            this.cleanupOver = this.cleanup(value);
        }
    }


    /**
     * ��ֹ��Դ������ shutdown��������ö�Σ�������ɹ����̵߳���
     */
    public void shutdown(final long intervalForcibly) {
        if (this.available) {
            this.available = false;
            this.firstShutdownTimestamp = System.currentTimeMillis();
            this.release();
        }
        // ǿ��shutdown
        else if (this.getRefCount() > 0) {
            if ((System.currentTimeMillis() - this.firstShutdownTimestamp) >= intervalForcibly) {
                this.refCount.set(-1000 - this.getRefCount());
                this.release();
            }
        }
    }


    /**
     * ��Դ�Ƿ���ã����Ƿ�ɱ�HOLD
     */
    public boolean isAvailable() {
        return this.available;
    }


    /**
     * ��Դ�Ƿ��������
     */
    public boolean isCleanupOver() {
        return this.refCount.get() <= 0 && this.cleanupOver;
    }


    public long getRefCount() {
        return this.refCount.get();
    }


    public abstract boolean cleanup(final long currentRef);
}
