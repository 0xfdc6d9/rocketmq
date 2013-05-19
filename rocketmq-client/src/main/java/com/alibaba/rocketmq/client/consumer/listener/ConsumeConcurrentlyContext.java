/**
 * $Id: ConsumeConcurrentlyContext.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.listener;

import com.alibaba.rocketmq.common.MessageQueue;


/**
 * ������Ϣ�����ģ�ͬһ���е���Ϣ�Ტ�����ѣ���Ϣ��˳����
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
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
}
