/**
 * $Id: SelectMapedBufferResult.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

import java.nio.ByteBuffer;


/**
 * ��ѯPagecache���ؽ��
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
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
