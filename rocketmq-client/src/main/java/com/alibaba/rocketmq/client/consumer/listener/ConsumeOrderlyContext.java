/**
 * $Id: ConsumeOrderlyContext.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.listener;

import com.alibaba.rocketmq.common.MessageQueue;


/**
 * ������Ϣ�����ģ�ͬһ���е���Ϣͬһʱ��ֻ��һ���߳����ѣ��ɱ�֤ͬһ������Ϣ˳������
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
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


    public ConsumeOrderlyContext(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }


    public boolean isAutoCommit() {
        return autoCommit;
    }


    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }


    public MessageQueue getMetaQueue() {
        return messageQueue;
    }


    public long getSuspendCurrentQueueTimeMillis() {
        return suspendCurrentQueueTimeMillis;
    }


    public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
        this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
    }
}
