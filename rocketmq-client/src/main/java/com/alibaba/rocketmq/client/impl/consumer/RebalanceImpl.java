package com.alibaba.rocketmq.client.impl.consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.alibaba.rocketmq.client.impl.factory.MQClientFactory;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.common.protocol.heartbeat.SubscriptionData;


/**
 * Rebalance�ľ���ʵ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-6-22
 */
public abstract class RebalanceImpl {
    protected static final Logger log = ClientLogger.getLog();

    // ��Ϣ�洢���
    protected final ConcurrentHashMap<MessageQueue, ProcessQueue> processQueueTable =
            new ConcurrentHashMap<MessageQueue, ProcessQueue>(64);

    // �ɶ��ĵ���Ϣ����ʱ��Name Server�������°汾��
    protected final ConcurrentHashMap<String/* topic */, Set<MessageQueue>> topicSubscribeInfoTable =
            new ConcurrentHashMap<String, Set<MessageQueue>>();

    // ���Ĺ�ϵ���û����õ�ԭʼ����
    protected final ConcurrentHashMap<String /* topic */, SubscriptionData> subscriptionInner =
            new ConcurrentHashMap<String, SubscriptionData>();

    protected final String consumerGroup;
    protected final MessageModel messageModel;
    protected final AllocateMessageQueueStrategy allocateMessageQueueStrategy;
    protected final MQClientFactory mQClientFactory;


    public RebalanceImpl(String consumerGroup, MessageModel messageModel,
            AllocateMessageQueueStrategy allocateMessageQueueStrategy, MQClientFactory mQClientFactory) {
        this.consumerGroup = consumerGroup;
        this.messageModel = messageModel;
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
        this.mQClientFactory = mQClientFactory;
    }


    public abstract void dispatchPullRequest(final List<PullRequest> pullRequestList);


    public abstract long computePullFromWhere(final MessageQueue mq);


    public void doRebalance() {
        Map<String, SubscriptionData> subTable = this.getSubscriptionInner();
        if (subTable != null) {
            for (final Map.Entry<String, SubscriptionData> entry : subTable.entrySet()) {
                final String topic = entry.getKey();
                try {
                    this.rebalanceByTopic(topic);
                }
                catch (Exception e) {
                    log.warn("rebalanceByTopic Exception", e);
                }
            }
        }

        this.truncateMessageQueueNotMyTopic();
    }


    private void rebalanceByTopic(final String topic) {
        switch (messageModel) {
        case BROADCASTING: {
            Set<MessageQueue> mqSet = this.topicSubscribeInfoTable.get(topic);
            if (mqSet != null) {
                this.updateProcessQueueTableInRebalance(topic, mqSet);
            }
            else {
                log.warn("doRebalance, {}, but the topic[{}] not exist.", consumerGroup, topic);
            }
            break;
        }
        case CLUSTERING: {
            Set<MessageQueue> mqSet = this.topicSubscribeInfoTable.get(topic);
            List<String> cidAll = this.mQClientFactory.findConsumerIdList(topic, consumerGroup);
            if (null == mqSet) {
                if (!topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                    log.warn("doRebalance, {}, but the topic[{}] not exist.", consumerGroup, topic);
                }
            }

            if (null == cidAll) {
                log.warn("doRebalance, {}, get consumer id list failed", consumerGroup);
            }

            if (mqSet != null && cidAll != null) {
                List<MessageQueue> mqAll = new ArrayList<MessageQueue>();
                mqAll.addAll(mqSet);

                // ����
                Collections.sort(mqAll);
                Collections.sort(cidAll);

                AllocateMessageQueueStrategy strategy = this.allocateMessageQueueStrategy;

                // ִ�з����㷨
                List<MessageQueue> allocateResult = null;
                try {
                    allocateResult = strategy.allocate(this.mQClientFactory.getClientId(), mqAll, cidAll);
                }
                catch (Throwable e) {
                    log.error("AllocateMessageQueueStrategy.allocate Exception", e);
                }

                Set<MessageQueue> allocateResultSet = new HashSet<MessageQueue>();
                if (allocateResult != null) {
                    allocateResultSet.addAll(allocateResult);
                }

                // ���±��ض���
                this.updateProcessQueueTableInRebalance(topic, allocateResultSet);
            }
            break;
        }
        default:
            break;
        }
    }


    private void updateProcessQueueTableInRebalance(final String topic, final Set<MessageQueue> mqSet) {
        // ������Ķ���ɾ��
        for (MessageQueue mq : this.processQueueTable.keySet()) {
            if (mq.getTopic().equals(topic)) {
                if (!mqSet.contains(mq)) {
                    ProcessQueue pq = this.processQueueTable.remove(mq);
                    if (pq != null) {
                        pq.setDroped(true);
                        log.info("doRebalance, {}, remove unnecessary mq, {}", consumerGroup, mq);
                    }
                }
            }
        }

        // ���������Ķ���
        List<PullRequest> pullRequestList = new ArrayList<PullRequest>();
        for (MessageQueue mq : mqSet) {
            if (!this.processQueueTable.containsKey(mq)) {
                PullRequest pullRequest = new PullRequest();
                pullRequest.setConsumerGroup(consumerGroup);
                pullRequest.setMessageQueue(mq);
                pullRequest.setProcessQueue(new ProcessQueue());

                // �����Ҫ���ݲ���������
                long nextOffset = this.computePullFromWhere(mq);
                pullRequest.setNextOffset(nextOffset);
                pullRequestList.add(pullRequest);
                this.processQueueTable.put(mq, pullRequest.getProcessQueue());
                log.info("doRebalance, {}, add a new mq, {}", consumerGroup, mq);
            }
        }

        this.dispatchPullRequest(pullRequestList);
    }


    private void truncateMessageQueueNotMyTopic() {
        Map<String, SubscriptionData> subTable = this.getSubscriptionInner();

        for (MessageQueue mq : this.processQueueTable.keySet()) {
            if (!subTable.containsKey(mq.getTopic())) {
                ProcessQueue pq = this.processQueueTable.remove(mq);
                if (pq != null) {
                    pq.setDroped(true);
                    log.info("doRebalance, {}, truncateMessageQueueNotMyTopic remove unnecessary mq, {}",
                        consumerGroup, mq);
                }
            }
        }
    }


    public ConcurrentHashMap<MessageQueue, ProcessQueue> getProcessQueueTable() {
        return processQueueTable;
    }


    public ConcurrentHashMap<String, Set<MessageQueue>> getTopicSubscribeInfoTable() {
        return topicSubscribeInfoTable;
    }


    public ConcurrentHashMap<String, SubscriptionData> getSubscriptionInner() {
        return subscriptionInner;
    }


    public MQClientFactory getmQClientFactory() {
        return mQClientFactory;
    }


    public String getConsumerGroup() {
        return consumerGroup;
    }


    public MessageModel getMessageModel() {
        return messageModel;
    }


    public AllocateMessageQueueStrategy getAllocateMessageQueueStrategy() {
        return allocateMessageQueueStrategy;
    }
}
