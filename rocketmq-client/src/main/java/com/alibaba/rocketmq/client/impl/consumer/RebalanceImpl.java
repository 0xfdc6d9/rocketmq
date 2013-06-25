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

    // ����õĶ��У���Ϣ�洢Ҳ������
    protected final ConcurrentHashMap<MessageQueue, ProcessQueue> processQueueTable =
            new ConcurrentHashMap<MessageQueue, ProcessQueue>(64);

    // ���Զ��ĵ����ж��У���ʱ��Name Server�������°汾��
    protected final ConcurrentHashMap<String/* topic */, Set<MessageQueue>> topicSubscribeInfoTable =
            new ConcurrentHashMap<String, Set<MessageQueue>>();

    // ���Ĺ�ϵ���û����õ�ԭʼ����
    protected final ConcurrentHashMap<String /* topic */, SubscriptionData> subscriptionInner =
            new ConcurrentHashMap<String, SubscriptionData>();

    protected String consumerGroup;
    protected MessageModel messageModel;
    protected AllocateMessageQueueStrategy allocateMessageQueueStrategy;
    protected MQClientFactory mQClientFactory;


    public RebalanceImpl(String consumerGroup, MessageModel messageModel,
            AllocateMessageQueueStrategy allocateMessageQueueStrategy, MQClientFactory mQClientFactory) {
        this.consumerGroup = consumerGroup;
        this.messageModel = messageModel;
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
        this.mQClientFactory = mQClientFactory;
    }


    public abstract void removeUnnecessaryMessageQueue(final MessageQueue mq, final ProcessQueue pq);


    public abstract void dispatchPullRequest(final List<PullRequest> pullRequestList);


    public abstract long computePullFromWhere(final MessageQueue mq);


    public abstract void messageQueueChanged(final String topic, final Set<MessageQueue> mqAll,
            final Set<MessageQueue> mqDivided);


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
                boolean changed = this.updateProcessQueueTableInRebalance(topic, mqSet);
                if (changed) {
                    this.messageQueueChanged(topic, mqSet, mqSet);
                    log.info("messageQueueChanged {} {} {} {}",//
                        consumerGroup,//
                        topic,//
                        mqSet,//
                        mqSet);
                }
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
                boolean changed = this.updateProcessQueueTableInRebalance(topic, allocateResultSet);
                if (changed) {
                    this.messageQueueChanged(topic, mqSet, allocateResultSet);
                    log.info("messageQueueChanged {} {} {} {}",//
                        consumerGroup,//
                        topic,//
                        mqSet,//
                        allocateResultSet);
                }
            }
            break;
        }
        default:
            break;
        }
    }


    private boolean updateProcessQueueTableInRebalance(final String topic, final Set<MessageQueue> mqSet) {
        boolean changed = false;

        // ������Ķ���ɾ��
        for (MessageQueue mq : this.processQueueTable.keySet()) {
            if (mq.getTopic().equals(topic)) {
                if (!mqSet.contains(mq)) {
                    changed = true;
                    ProcessQueue pq = this.processQueueTable.remove(mq);
                    if (pq != null) {
                        pq.setDroped(true);
                        log.info("doRebalance, {}, remove unnecessary mq, {}", consumerGroup, mq);
                        this.removeUnnecessaryMessageQueue(mq, pq);
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
                if (nextOffset >= 0) {
                    pullRequest.setNextOffset(nextOffset);
                    pullRequestList.add(pullRequest);
                    changed = true;
                    this.processQueueTable.put(mq, pullRequest.getProcessQueue());
                    log.info("doRebalance, {}, add a new mq, {}", consumerGroup, mq);
                }
                else {
                    // �ȴ��˴�Rebalance������
                    log.warn("doRebalance, {}, add new mq failed, {}", consumerGroup, mq);
                }
            }
        }

        this.dispatchPullRequest(pullRequestList);

        return changed;
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


    public String getConsumerGroup() {
        return consumerGroup;
    }


    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }


    public MessageModel getMessageModel() {
        return messageModel;
    }


    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }


    public AllocateMessageQueueStrategy getAllocateMessageQueueStrategy() {
        return allocateMessageQueueStrategy;
    }


    public void setAllocateMessageQueueStrategy(AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
    }


    public MQClientFactory getmQClientFactory() {
        return mQClientFactory;
    }


    public void setmQClientFactory(MQClientFactory mQClientFactory) {
        this.mQClientFactory = mQClientFactory;
    }
}
