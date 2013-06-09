/**
 * $Id: ConsumeConcurrentlyContext.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.listener;

import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * ������Ϣ�����ģ�ͬһ���е���Ϣ�Ტ�����ѣ���Ϣ��˳����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class ConsumeConcurrentlyContext {
    /**
     * Ҫ���ѵ���Ϣ�����ĸ�����
     */
    private final MessageQueue messageQueue;
    /**
     * 0����ʾ�ɿͻ��˾���
     */
    private int delayLevelWhenNextConsume = 0;
    /**
     * �����������ѣ�ack��������Ϣ��Ĭ��ȫ��ack�������һ����Ϣ
     */
    private int ackIndex = Integer.MAX_VALUE;


    public ConsumeConcurrentlyContext(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }


    public int getDelayLevelWhenNextConsume() {
        return delayLevelWhenNextConsume;
    }


    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
        this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    }


    public MessageQueue getMessageQueue() {
        return messageQueue;
    }


    public int getAckIndex() {
        return ackIndex;
    }


    public void setAckIndex(int ackIndex) {
        this.ackIndex = ackIndex;
    }
}
