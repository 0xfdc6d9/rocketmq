package com.alibaba.rocketmq.client.producer.selector;

import java.util.List;
import java.util.Random;

import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.common.Message;
import com.alibaba.rocketmq.common.MessageQueue;


/**
 * ������Ϣ�����ѡ�����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class SelectMessageQueueByRandoom implements MessageQueueSelector {
    private Random random = new Random(System.currentTimeMillis());


    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        int value = random.nextInt();
        if (value < 0) {
            value = Math.abs(value);
        }

        value = value % mqs.size();
        return mqs.get(value);
    }
}
