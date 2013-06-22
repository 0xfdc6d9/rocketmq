/**
 * $Id: MessageQueueListener.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import java.util.Set;

import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * ���б仯������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface MessageQueueListener {
    public void messageQueueChanged(final String topic, final Set<MessageQueue> mqAll,
            final Set<MessageQueue> mqDivided);
}
