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
import java.util.ArrayList;
import java.util.List;


/**
 * ������Ϣ���ؽ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
 */
public class GetMessageResult {
    // ö�ٱ�����ȡ��Ϣ���
    private GetMessageStatus status;
    // �������˺󣬷�����һ�ο�ʼ��Offset
    private long nextBeginOffset;
    // �߼������е���СOffset
    private long minOffset;
    // �߼������е����Offset
    private long maxOffset;
    // �����������Ϣ����
    private final List<SelectMapedBufferResult> messageMapedList =
            new ArrayList<SelectMapedBufferResult>(100);
    // ������Consumer������Ϣ
    private final List<ByteBuffer> messageBufferList = new ArrayList<ByteBuffer>(100);
    // ByteBuffer ���ֽ���
    private int bufferTotalSize = 0;
    // �Ƿ����slave����Ϣ
    private boolean suggestPullingFromSlave = false;


    public GetMessageResult() {
    }


    public GetMessageStatus getStatus() {
        return status;
    }


    public void setStatus(GetMessageStatus status) {
        this.status = status;
    }


    public long getNextBeginOffset() {
        return nextBeginOffset;
    }


    public void setNextBeginOffset(long nextBeginOffset) {
        this.nextBeginOffset = nextBeginOffset;
    }


    public long getMinOffset() {
        return minOffset;
    }


    public void setMinOffset(long minOffset) {
        this.minOffset = minOffset;
    }


    public long getMaxOffset() {
        return maxOffset;
    }


    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }


    public List<SelectMapedBufferResult> getMessageMapedList() {
        return messageMapedList;
    }


    public List<ByteBuffer> getMessageBufferList() {
        return messageBufferList;
    }


    public void addMessage(final SelectMapedBufferResult mapedBuffer) {
        this.messageMapedList.add(mapedBuffer);
        this.messageBufferList.add(mapedBuffer.getByteBuffer());
        this.bufferTotalSize += mapedBuffer.getSize();
    }


    public void release() {
        for (SelectMapedBufferResult select : this.messageMapedList) {
            select.release();
        }
    }


    public int getBufferTotalSize() {
        return bufferTotalSize;
    }


    public int getMessageCount() {
        return this.messageMapedList.size();
    }


    public void setBufferTotalSize(int bufferTotalSize) {
        this.bufferTotalSize = bufferTotalSize;
    }


    public boolean isSuggestPullingFromSlave() {
        return suggestPullingFromSlave;
    }


    public void setSuggestPullingFromSlave(boolean suggestPullingFromSlave) {
        this.suggestPullingFromSlave = suggestPullingFromSlave;
    }
}
