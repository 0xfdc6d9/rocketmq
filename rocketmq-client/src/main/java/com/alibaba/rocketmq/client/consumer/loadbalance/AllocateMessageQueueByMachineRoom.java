/**
 * $Id: AllocateMessageQueueByMachineRoom.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.loadbalance;

import java.util.List;

import com.alibaba.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.alibaba.rocketmq.common.MessageQueue;


/**
 * ���ջ�����������У�����֧�����߼�����
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public class AllocateMessageQueueByMachineRoom implements AllocateMessageQueueStrategy {

    @Override
    public List<MessageQueue> allocate(String group, String topic, String currentCID, List<MessageQueue> mqAll,
            List<String> cidAll) {
        // TODO Auto-generated method stub
        return mqAll;
    }
}
