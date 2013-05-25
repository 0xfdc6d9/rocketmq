/**
 * $Id: GetMessageResult.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * ������Ϣ���ؽ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
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
    private final List<SelectMapedBufferResult> messageMapedList = new ArrayList<SelectMapedBufferResult>(100);
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
