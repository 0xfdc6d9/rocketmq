/**
 * $Id: AppendMessageCallback.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

import java.nio.ByteBuffer;


/**
 * д����Ϣ�Ļص��ӿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface AppendMessageCallback {

    /**
     * ���л���Ϣ��д��MapedByteBuffer
     * 
     * @param byteBuffer
     *            Ҫд���target
     * @param maxBlank
     *            Ҫд���target���հ���
     * @param msg
     *            Ҫд���message
     * @return д������ֽ�
     */
    public AppendMessageResult doAppend(final long fileFromOffset, final ByteBuffer byteBuffer,
            final int maxBlank, final Object msg);
}
