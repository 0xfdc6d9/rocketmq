/**
 * $Id: AllocateMessageQueueByConfig.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.loadbalance;

import java.util.List;

import com.alibaba.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.alibaba.rocketmq.common.MessageQueue;


/**
 * ����������������У�����Ӧ��ʹ��Spring����ʼ��
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public class AllocateMessageQueueByConfig implements AllocateMessageQueueStrategy {
    private List<MessageQueue> messageQueueList;


    @Override
    public List<MessageQueue> allocate(String group, String topic, String currentCID, List<MessageQueue> mqAll,
            List<String> cidAll) {
        return this.messageQueueList;
    }


    public List<MessageQueue> getMessageQueueList() {
        return messageQueueList;
    }


    public void setMessageQueueList(List<MessageQueue> messageQueueList) {
        this.messageQueueList = messageQueueList;
    }
}
