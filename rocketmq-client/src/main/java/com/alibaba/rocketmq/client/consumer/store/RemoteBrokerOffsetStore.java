package com.alibaba.rocketmq.client.consumer.store;

import com.alibaba.rocketmq.common.MessageQueue;


/**
 * ���ѽ��ȴ洢��Զ��Broker���ȽϿɿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class RemoteBrokerOffsetStore implements OffsetStore {
    @Override
    public void load() {
        // TODO Auto-generated method stub

    }


    @Override
    public void updateOffset(MessageQueue mq, long offset) {
        // TODO Auto-generated method stub

    }


    @Override
    public long readOffset(MessageQueue mq) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public void persistAll() {
        // TODO Auto-generated method stub

    }
}
