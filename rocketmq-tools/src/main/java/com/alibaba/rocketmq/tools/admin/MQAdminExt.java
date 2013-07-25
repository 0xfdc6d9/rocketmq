/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.tools.admin;

import com.alibaba.rocketmq.client.MQAdmin;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.common.admin.ConsumerProgress;
import com.alibaba.rocketmq.common.admin.TopicOffsetTable;
import com.alibaba.rocketmq.common.subscription.SubscriptionGroupConfig;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * MQ������ӿڣ��漰������MQ������صĶ���ӿ�<br>
 * ����Topic�����������鴴���������޸ĵ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-14
 */
public interface MQAdminExt extends MQAdmin {
    public void start() throws MQClientException;


    public void shutdown();


    /**
     * ��ָ��Broker��Ⱥ�������߸���Topic����
     * 
     * @param cluster
     * @param config
     */
    public void createAndUpdateTopicConfigByCluster(final String cluster, final TopicConfig config);


    /**
     * ��ָ��Broker�������߸���Topic����
     * 
     * @param addr
     * @param config
     * @throws MQClientException
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     */
    public void createAndUpdateTopicConfigByAddr(final String addr, final TopicConfig config)
            throws RemotingException, MQBrokerException, InterruptedException, MQClientException;


    /**
     * ��ָ��Broker��Ⱥ�������߸��¶���������
     * 
     * @param cluster
     * @param config
     */
    public void createAndUpdateSubscriptionGroupConfigByCluster(final String cluster,
            final SubscriptionGroupConfig config);


    /**
     * ��ָ��Broker�������߸��¶���������
     * 
     * @param addr
     * @param config
     * @throws MQClientException
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     */
    public void createAndUpdateSubscriptionGroupConfigByAddr(final String addr,
            final SubscriptionGroupConfig config) throws RemotingException, MQBrokerException,
            InterruptedException, MQClientException;


    /**
     * ��ѯָ��Broker�Ķ���������
     * 
     * @param addr
     * @param group
     * @return
     */
    public SubscriptionGroupConfig examineSubscriptionGroupConfig(final String addr, final String group);


    /**
     * ��ѯָ��Broker��Topic����
     * 
     * @param addr
     * @param group
     * @return
     */
    public TopicConfig examineTopicConfig(final String addr, final String topic);


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
