/**
 * $Id: ConsumerManager.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.broker.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.rocketmq.common.protocol.heartbeat.ConsumeType;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.common.protocol.heartbeat.SubscriptionData;


/**
 * Consumer���ӡ����Ĺ�ϵ����
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public class ConsumerManager {
    private final ConcurrentHashMap<String/* Group */, ConsumerGroupInfo> consumerTable =
            new ConcurrentHashMap<String, ConsumerGroupInfo>(1024);


    public ConsumerGroupInfo getConsumerGroupInfo(final String group) {
        return this.consumerTable.get(group);
    }


    /**
     * �����Ƿ��б仯
     */
    public boolean registerConsumer(final String group, final ClientChannelInfo clientChannelInfo,
            ConsumeType consumeType, MessageModel messageModel, final Set<SubscriptionData> subList) {
        ConsumerGroupInfo consumerGroupInfo = this.consumerTable.get(group);
        if (null == consumerGroupInfo) {
            ConsumerGroupInfo tmp = new ConsumerGroupInfo(group, consumeType, messageModel);
            ConsumerGroupInfo prev = this.consumerTable.putIfAbsent(group, tmp);
            consumerGroupInfo = prev != null ? prev : tmp;
        }

        boolean r1 = consumerGroupInfo.updateChannel(clientChannelInfo, consumeType, messageModel);
        boolean r2 = consumerGroupInfo.updateSubscription(subList);
        return r1 || r2;
    }
}
