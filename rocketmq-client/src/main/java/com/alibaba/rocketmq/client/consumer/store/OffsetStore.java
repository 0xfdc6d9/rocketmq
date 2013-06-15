package com.alibaba.rocketmq.client.consumer.store;

import java.util.Set;

import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface OffsetStore {
    /**
     * ����Offset
     */
    public void load();


    /**
     * �������ѽ��ȣ��洢���ڴ�
     */
    public void updateOffset(final MessageQueue mq, final long offset);


    /**
     * �ӱ��ػ����ȡ���ѽ���
     */
    public long readOffset(final MessageQueue mq, final boolean fromStore);


    /**
     * �־û�ȫ�����ѽ��ȣ����ܳ־û����ػ���Զ��Broker
     */
    public void persistAll(final Set<MessageQueue> mqs);
}
