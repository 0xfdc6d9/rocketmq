package com.alibaba.rocketmq.tools.admin;

import com.alibaba.rocketmq.client.MQAdmin;
import com.alibaba.rocketmq.common.admin.ConsumerProgress;
import com.alibaba.rocketmq.common.admin.TopicOffsetTable;
import com.alibaba.rocketmq.common.subscription.SubscriptionGroupConfig;


/**
 * MQ������ӿڣ��漰������MQ������صĶ���ӿ�<br>
 * ����Topic�����������鴴���������޸ĵ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-14
 */
public interface MQAdminExt extends MQAdmin {
    public void start();


    public void shutdown();


    /**
     * ��ָ��Broker��Ⱥ�������߸��¶���������
     * 
     * @param cluster
     * @param config
     */
    public void createAndUpdateSubscriptionGroupConfig(final String cluster,
            final SubscriptionGroupConfig config);


    /**
     * ��ѯָ����Ⱥ�Ķ���������
     * 
     * @param cluster
     * @param group
     * @return
     */
    public SubscriptionGroupConfig examineSubscriptionGroupConfig(final String cluster, final String group);


    /**
     * ��ѯTopic Offset��Ϣ
     * 
     * @param topic
     * @return
     */
    public TopicOffsetTable examineTopicOffset(final String topic);


    /**
     * ��ѯ���ѽ���
     * 
     * @param consumerGroup
     * @param topic
     * @return
     */
    public ConsumerProgress examineConsumerProgress(final String consumerGroup, final String topic);


    /**
     * ��Name Server����һ��������
     * 
     * @param namespace
     * @param key
     * @param value
     */
    public void putKVConfig(final String namespace, final String key, final String value);


    /**
     * ��Name Server��ȡһ��������
     * 
     * @param namespace
     * @param key
     * @return
     */
    public String getKVConfig(final String namespace, final String key);
}
