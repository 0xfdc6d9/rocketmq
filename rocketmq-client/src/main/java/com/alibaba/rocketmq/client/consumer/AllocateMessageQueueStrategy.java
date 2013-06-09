/**
 * $Id: AllocateMessageQueueStrategy.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import java.util.List;

import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * Consumer�����Զ��������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public interface AllocateMessageQueueStrategy {
    /**
     * ���з����㷨
     * 
     * @param group
     * @param topic
     * @param currentCID
     *            ��ǰConsumerId
     * @param mqAll
     *            ��ǰTopic�����ж��м��ϣ����ظ����ݣ�������
     * @param cidAll
     *            ��ǰ�����������Consumer���ϣ����ظ����ݣ�������
     * @return �����������ظ�����
     */
    public List<MessageQueue> allocate(//
            final String group,//
            final String topic,//
            final String currentCID,//
            final List<MessageQueue> mqAll,//
            final List<String> cidAll//
    );
}
