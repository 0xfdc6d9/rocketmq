/**
 * $Id: SelectMessageQueueByMachineRoom.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.producer.selector;

import java.util.List;
import java.util.Set;

import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * ���ݻ�����ѡ�����ĸ����У�֧�����߼�����ʹ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public class SelectMessageQueueByMachineRoom implements MessageQueueSelector {
    private Set<String> consumeridcs;


    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        // TODO Auto-generated method stub
        return null;
    }


    public Set<String> getConsumeridcs() {
        return consumeridcs;
    }


    public void setConsumeridcs(Set<String> consumeridcs) {
        this.consumeridcs = consumeridcs;
    }
}
