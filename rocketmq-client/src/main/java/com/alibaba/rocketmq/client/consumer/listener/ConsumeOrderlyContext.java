/**
 * $Id: ConsumeOrderlyContext.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.listener;

import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * ������Ϣ�����ģ�ͬһ���е���Ϣͬһʱ��ֻ��һ���߳����ѣ��ɱ�֤ͬһ������Ϣ˳������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class ConsumeOrderlyContext {
    /**
     * Ҫ���ѵ���Ϣ�����ĸ�����
     */
    private final MessageQueue messageQueue;
    /**
     * ��ϢOffset�Ƿ��Զ��ύ
     */
    private boolean autoCommit = true;
    /**
     * ����ǰ���й���ʱ�䣬��λ����
     */
    private long suspendCurrentQueueTimeMillis = 1000;
    /**
     * �����������ѣ�ack��������Ϣ��Ĭ��ȫ��ack�������һ����Ϣ
     */
    private int ackIndex = Integer.MAX_VALUE;


    public ConsumeOrderlyContext(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }


    public boolean isAutoCommit() {
        return autoCommit;
    }


    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }


    public MessageQueue getMessageQueue() {
        return messageQueue;
    }


    public long getSuspendCurrentQueueTimeMillis() {
        return suspendCurrentQueueTimeMillis;
    }


    public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
        this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
    }


    public int getAckIndex() {
        return ackIndex;
    }


    public void setAckIndex(int ackIndex) {
        this.ackIndex = ackIndex;
    }
}
