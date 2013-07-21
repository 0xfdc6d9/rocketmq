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

import java.nio.ByteBuffer;


/**
 * ��ѯPagecache���ؽ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
 */
public class SelectMapedBufferResult {
    // �Ӷ������ĸ�����Offset��ʼ
    private final long startOffset;
    // position��0��ʼ
    private final ByteBuffer byteBuffer;
    // ��Ч���ݴ�С
    private int size;
    // �����ͷ��ڴ�
    private MapedFile mapedFile;


    public SelectMapedBufferResult(long startOffset, ByteBuffer byteBuffer, int size, MapedFile mapedFile) {
        this.startOffset = startOffset;
        this.byteBuffer = byteBuffer;
        this.size = size;
        this.mapedFile = mapedFile;
    }


    public void setSize(final int s) {
        this.size = s;
        this.byteBuffer.limit(this.size);
    }


    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }


    public int getSize() {
        return size;
    }


    public MapedFile getMapedFile() {
        return mapedFile;
    }


    /**
     * �˷���ֻ�ܱ�����һ�Σ��ظ�������Ч
     */
    public synchronized void release() {
        if (this.mapedFile != null) {
            this.mapedFile.release();
            this.mapedFile = null;
        }
    }


    @Override
    protected void finalize() {
        if (this.mapedFile != null) {
            this.release();
        }
    }


    public long getStartOffset() {
        return startOffset;
    }
}
