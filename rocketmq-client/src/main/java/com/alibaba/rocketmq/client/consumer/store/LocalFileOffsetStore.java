package com.alibaba.rocketmq.client.consumer.store;

import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * ���ѽ��ȴ洢��Consumer���أ����Ǻܿɿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class LocalFileOffsetStore implements OffsetStore {
    @Override
    public void load() {
    }


    @Override
    public void updateOffset(MessageQueue mq, long offset) {
    }


    @Override
    public long readOffset(MessageQueue mq) {
        return 0;
    }


    @Override
    public void persistAll() {
    }
}
