/**
 * $Id: DefaultMQPushConsumerImpl.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.impl.consumer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.QueryResult;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.PullCallback;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.consumer.store.LocalFileOffsetStore;
import com.alibaba.rocketmq.client.consumer.store.OffsetStore;
import com.alibaba.rocketmq.client.consumer.store.RemoteBrokerOffsetStore;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.CommunicationMode;
import com.alibaba.rocketmq.client.impl.MQClientManager;
import com.alibaba.rocketmq.client.impl.factory.MQClientFactory;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.stat.ConsumerStatManager;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.ServiceState;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.filter.FilterAPI;
import com.alibaba.rocketmq.common.help.FAQUrl;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.ConsumeType;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.common.protocol.heartbeat.SubscriptionData;
import com.alibaba.rocketmq.common.sysflag.PullSysFlag;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-6-15
 */
public class DefaultMQPushConsumerImpl implements MQConsumerInner {
    private final Logger log = ClientLogger.getLog();
    private final DefaultMQPushConsumer defaultMQPushConsumer;
    private ServiceState serviceState = ServiceState.CREATE_JUST;
    private MQClientFactory mQClientFactory;
    private PullAPIWrapper pullAPIWrapper;

    // �Ƿ���ͣ������Ϣ suspend/resume
    private volatile boolean pause = false;

    // �Ƿ�˳��������Ϣ
    private boolean consumeOrderly = false;

    // ������Ϣ������
    private MessageListener messageListenerInner;

    // ���ѽ��ȴ洢
    private OffsetStore offsetStore;

    // ������Ϣ����
    private ConsumeMessageService consumeMessageService;

    // Rebalanceʵ��
    private final RebalanceImpl rebalanceImpl = new RebalancePushImpl(this);

    private final ConsumerStatManager consumerStatManager = new ConsumerStatManager();

    // ����Ϣ�쳣ʱ���ӳ�һ��ʱ������
    private static final long PullTimeDelayMillsWhenException = 3000;
    private static final long PullTimeDelayMillsWhenFlowControl = 100;
    private static final long PullTimeDelayMillsWhenSuspend = 1000;

    // ����ѯģʽ��Consumer������Broker�����ʱ��
    private static final long BrokerSuspendMaxTimeMillis = 1000 * 15;

    // ����ѯģʽ��Consumer��ʱʱ�䣨����Ҫ����brokerSuspendMaxTimeMillis��
    private static final long ConsumerTimeoutMillisWhenSuspend = 1000 * 30;


    public DefaultMQPushConsumerImpl(DefaultMQPushConsumer defaultMQPushConsumer) {
        this.defaultMQPushConsumer = defaultMQPushConsumer;
    }


    private void checkConfig() throws MQClientException {
        // consumerGroup
        if (null == this.defaultMQPushConsumer.getConsumerGroup()) {
            throw new MQClientException("consumerGroup is null" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // consumerGroup
        if (this.defaultMQPushConsumer.getConsumerGroup().equals(MixAll.DEFAULT_CONSUMER_GROUP)) {
            throw new MQClientException("consumerGroup can not equal "//
                    + MixAll.DEFAULT_CONSUMER_GROUP //
                    + ", please specify another one."//
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // messageModel
        if (null == this.defaultMQPushConsumer.getMessageModel()) {
            throw new MQClientException("messageModel is null" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // consumeFromWhereOffset
        if (null == this.defaultMQPushConsumer.getConsumeFromWhere()) {
            throw new MQClientException("consumeFromWhere is null" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // allocateMessageQueueStrategy
        if (null == this.defaultMQPushConsumer.getAllocateMessageQueueStrategy()) {
            throw new MQClientException("allocateMessageQueueStrategy is null" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // subscription
        if (null == this.defaultMQPushConsumer.getSubscription()) {
            throw new MQClientException("subscription is null" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // messageListener
        if (null == this.defaultMQPushConsumer.getMessageListener()) {
            throw new MQClientException("messageListener is null" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        boolean orderly = this.defaultMQPushConsumer.getMessageListener() instanceof MessageListenerOrderly;
        boolean concurrently =
                this.defaultMQPushConsumer.getMessageListener() instanceof MessageListenerConcurrently;
        if (!orderly && !concurrently) {
            throw new MQClientException(
                "messageListener must be instanceof MessageListenerOrderly or MessageListenerConcurrently" //
                        + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // consumeThreadMin
        if (this.defaultMQPushConsumer.getConsumeThreadMin() < 1 //
                || this.defaultMQPushConsumer.getConsumeThreadMin() > 1000//
                || this.defaultMQPushConsumer.getConsumeThreadMin() > this.defaultMQPushConsumer
                    .getConsumeThreadMax()//
        ) {
            throw new MQClientException("consumeThreadMin Out of range [1, 1000]" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // consumeThreadMax
        if (this.defaultMQPushConsumer.getConsumeThreadMax() < 1
                || this.defaultMQPushConsumer.getConsumeThreadMax() > 1000) {
            throw new MQClientException("consumeThreadMax Out of range [1, 1000]" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // consumeConcurrentlyMaxSpan
        if (this.defaultMQPushConsumer.getConsumeConcurrentlyMaxSpan() < 1
                || this.defaultMQPushConsumer.getConsumeConcurrentlyMaxSpan() > 65535) {
            throw new MQClientException("consumeConcurrentlyMaxSpan Out of range [1, 65535]" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // pullThresholdForQueue
        if (this.defaultMQPushConsumer.getPullThresholdForQueue() < 1
                || this.defaultMQPushConsumer.getPullThresholdForQueue() > 65535) {
            throw new MQClientException("pullThresholdForQueue Out of range [1, 65535]" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // pullInterval
        if (this.defaultMQPushConsumer.getPullInterval() < 0
                || this.defaultMQPushConsumer.getPullInterval() > 65535) {
            throw new MQClientException("pullInterval Out of range [0, 65535]" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // consumeMessageBatchMaxSize
        if (this.defaultMQPushConsumer.getConsumeMessageBatchMaxSize() < 1
                || this.defaultMQPushConsumer.getConsumeMessageBatchMaxSize() > 1024) {
            throw new MQClientException("consumeMessageBatchMaxSize Out of range [1, 1024]" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }

        // pullBatchSize
        if (this.defaultMQPushConsumer.getPullBatchSize() < 1
                || this.defaultMQPushConsumer.getPullBatchSize() > 1024) {
            throw new MQClientException("pullBatchSize Out of range [1, 1024]" //
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_PARAMETER_CHECK_URL), //
                null);
        }
    }


    @Override
    public ConsumeFromWhere consumeFromWhere() {
        return this.defaultMQPushConsumer.getConsumeFromWhere();
    }


    @Override
    public ConsumeType consumeType() {
        return ConsumeType.CONSUME_PASSIVELY;
    }


    private void copySubscription() throws MQClientException {
        try {
            // �����û���ʼ���õĶ��Ĺ�ϵ
            Map<String, String> sub = this.defaultMQPushConsumer.getSubscription();
            if (sub != null) {
                for (final Map.Entry<String, String> entry : sub.entrySet()) {
                    final String topic = entry.getKey();
                    final String subString = entry.getValue();
                    SubscriptionData subscriptionData = FilterAPI.buildSubscriptionData(topic, subString);
                    this.rebalanceImpl.getSubscriptionInner().put(topic, subscriptionData);
                }
            }

            if (null == this.messageListenerInner) {
                this.messageListenerInner = this.defaultMQPushConsumer.getMessageListener();
            }

            switch (this.defaultMQPushConsumer.getMessageModel()) {
            case BROADCASTING:
                break;
            case CLUSTERING:
                // Ĭ�϶�����Ϣ����Topic
                final String retryTopic = MixAll.getRetryTopic(this.defaultMQPushConsumer.getConsumerGroup());
                SubscriptionData subscriptionData =
                        FilterAPI.buildSubscriptionData(retryTopic, SubscriptionData.SUB_ALL);
                this.rebalanceImpl.getSubscriptionInner().put(retryTopic, subscriptionData);
                break;
            default:
                break;
            }
        }
        catch (Exception e) {
            throw new MQClientException("subscription exception", e);
        }
    }


    public void createTopic(String key, String newTopic, int queueNum) throws MQClientException {
        this.mQClientFactory.getMQAdminImpl().createTopic(key, newTopic, queueNum);
    }


    @Override
    public void doRebalance() {
        if (this.rebalanceImpl != null) {
            this.rebalanceImpl.doRebalance();
        }
    }


    /**
     * ����ִ�����PullRequest
     */
    public void executePullRequestImmediately(final PullRequest pullRequest) {
        this.mQClientFactory.getPullMessageService().executePullRequestImmediately(pullRequest);
    }


    /**
     * �Ժ���ִ�����PullRequest
     */
    private void executePullRequestLater(final PullRequest pullRequest, final long timeDelay) {
        this.mQClientFactory.getPullMessageService().executePullRequestLater(pullRequest, timeDelay);
    }


    public Set<MessageQueue> fetchSubscribeMessageQueues(String topic) throws MQClientException {
        Set<MessageQueue> result = this.rebalanceImpl.getTopicSubscribeInfoTable().get(topic);
        if (null == result) {
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(topic);
            result = this.rebalanceImpl.getTopicSubscribeInfoTable().get(topic);
        }

        if (null == result) {
            throw new MQClientException("The topic[" + topic + "] not exist", null);
        }

        return result;
    }


    public DefaultMQPushConsumer getDefaultMQPushConsumer() {
        return defaultMQPushConsumer;
    }


    public long earliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        return this.mQClientFactory.getMQAdminImpl().earliestMsgStoreTime(mq);
    }


    public long maxOffset(MessageQueue mq) throws MQClientException {
        return this.mQClientFactory.getMQAdminImpl().maxOffset(mq);
    }


    public MessageListener getMessageListenerInner() {
        return messageListenerInner;
    }


    public long minOffset(MessageQueue mq) throws MQClientException {
        return this.mQClientFactory.getMQAdminImpl().minOffset(mq);
    }


    public OffsetStore getOffsetStore() {
        return offsetStore;
    }


    public ConcurrentHashMap<String, SubscriptionData> getSubscriptionInner() {
        return this.rebalanceImpl.getSubscriptionInner();
    }


    @Override
    public String groupName() {
        return this.defaultMQPushConsumer.getConsumerGroup();
    }


    public boolean isPause() {
        return pause;
    }


    private void makeSureStateOK() throws MQClientException {
        if (this.serviceState != ServiceState.RUNNING) {
            throw new MQClientException("The consumer service state not OK, " + this.serviceState, null);
        }
    }


    @Override
    public MessageModel messageModel() {
        return this.defaultMQPushConsumer.getMessageModel();
    }


    @Override
    public void persistConsumerOffset() {
        try {
            this.makeSureStateOK();
            Set<MessageQueue> mqs = new HashSet<MessageQueue>();
            Set<MessageQueue> allocateMq = this.rebalanceImpl.getProcessQueueTable().keySet();
            if (allocateMq != null) {
                mqs.addAll(allocateMq);
            }

            this.offsetStore.persistAll(mqs);
        }
        catch (Exception e) {
            log.error("group: " + this.defaultMQPushConsumer.getConsumerGroup()
                    + " persistConsumerOffset exception", e);
        }
    }


    public void pullMessage(final PullRequest pullRequest) {
        final ProcessQueue processQueue = pullRequest.getProcessQueue();
        if (processQueue.isDroped()) {
            log.info("the pull request[{}] is droped.", pullRequest.toString());
            return;
        }

        // ���Consumer�Ƿ�����
        try {
            this.makeSureStateOK();
        }
        catch (MQClientException e) {
            log.warn("pullMessage exception, consumer state not ok", e);
            this.executePullRequestLater(pullRequest, PullTimeDelayMillsWhenException);
            return;
        }

        // ���Consumer�Ƿ񱻹���
        if (this.isPause()) {
            this.executePullRequestLater(pullRequest, PullTimeDelayMillsWhenSuspend);
            return;
        }

        // �������ƣ���������Ϣ����
        long size = processQueue.getMsgCount().get();
        if (size > this.defaultMQPushConsumer.getPullThresholdForQueue()) {
            this.executePullRequestLater(pullRequest, PullTimeDelayMillsWhenFlowControl);
            log.warn("the consumer message buffer is full, so do flow control, {} {}", size, pullRequest);
            return;
        }

        // �������ƣ���������Ϣ�����
        if (!this.consumeOrderly) {
            if (processQueue.getMaxSpan() > this.defaultMQPushConsumer.getConsumeConcurrentlyMaxSpan()) {
                this.executePullRequestLater(pullRequest, PullTimeDelayMillsWhenFlowControl);
                log.warn("the queue's messages, span too long, so do flow control, {} {}", size, pullRequest);
                return;
            }
        }

        // ��ѯ���Ĺ�ϵ
        final SubscriptionData subscriptionData =
                this.rebalanceImpl.getSubscriptionInner().get(pullRequest.getMessageQueue().getTopic());
        if (null == subscriptionData) {
            // ���ڲ�����ϵ����ʹ�Ҳ������Ĺ�ϵ��ҲҪ�����£���ֹ��ʧPullRequest
            this.executePullRequestLater(pullRequest, PullTimeDelayMillsWhenException);
            log.warn("find the consumer's subscription failed, {}", pullRequest);
            return;
        }

        PullCallback pullCallback = new PullCallback() {
            @Override
            public void onException(Throwable e) {
                if (!pullRequest.getMessageQueue().getTopic().startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                    log.warn("execute the pull request exception", e);
                }

                DefaultMQPushConsumerImpl.this.executePullRequestLater(pullRequest,
                    PullTimeDelayMillsWhenException);
            }


            @Override
            public void onSuccess(PullResult pullResult) {
                if (pullResult != null) {
                    pullResult =
                            DefaultMQPushConsumerImpl.this.pullAPIWrapper.processPullResult(
                                pullRequest.getMessageQueue(), pullResult, subscriptionData);
                    pullRequest.setNextOffset(pullResult.getNextBeginOffset());

                    switch (pullResult.getPullStatus()) {
                    case FOUND:
                        boolean dispathToConsume = processQueue.putMessage(pullResult.getMsgFoundList());
                        DefaultMQPushConsumerImpl.this.consumeMessageService.submitConsumeRequest(//
                            pullResult.getMsgFoundList(), //
                            processQueue, //
                            pullRequest.getMessageQueue(), //
                            dispathToConsume);

                        // ����
                        if (DefaultMQPushConsumerImpl.this.defaultMQPushConsumer.getPullInterval() > 0) {
                            DefaultMQPushConsumerImpl.this.executePullRequestLater(pullRequest,
                                DefaultMQPushConsumerImpl.this.defaultMQPushConsumer.getPullInterval());
                        }
                        // ��������Ϣ
                        else {
                            DefaultMQPushConsumerImpl.this.executePullRequestImmediately(pullRequest);
                        }

                        break;
                    case NO_NEW_MSG:
                        DefaultMQPushConsumerImpl.this.executePullRequestImmediately(pullRequest);
                        break;
                    case NO_MATCHED_MSG:
                        pullRequest.setNextOffset(pullResult.getNextBeginOffset());
                        DefaultMQPushConsumerImpl.this.executePullRequestImmediately(pullRequest);
                        break;
                    case OFFSET_ILLEGAL:
                        log.warn("the pull request offset illegal, {} {}",//
                            pullRequest.toString(), pullResult.toString());
                        if (pullRequest.getNextOffset() < pullResult.getMinOffset()) {
                            pullRequest.setNextOffset(pullResult.getMinOffset());
                        }
                        else if (pullRequest.getNextOffset() > pullResult.getMaxOffset()) {
                            pullRequest.setNextOffset(pullResult.getMaxOffset());
                        }

                        DefaultMQPushConsumerImpl.this.offsetStore.updateOffset(
                            pullRequest.getMessageQueue(), pullRequest.getNextOffset(), false);

                        log.warn("fix the pull request offset, {}", pullRequest);
                        DefaultMQPushConsumerImpl.this.executePullRequestImmediately(pullRequest);
                        break;
                    default:
                        break;
                    }
                }
            }
        };

        int sysFlag = PullSysFlag.buildSysFlag(//
            false, // commitOffset
            true, // suspend
            false// subscription
            );
        try {
            this.pullAPIWrapper.pullKernelImpl(//
                pullRequest.getMessageQueue(), // 1
                null, // 2
                subscriptionData.getSubVersion(), // 3
                pullRequest.getNextOffset(), // 4
                this.defaultMQPushConsumer.getPullBatchSize(), // 5
                sysFlag, // 6
                0,// 7
                BrokerSuspendMaxTimeMillis, // 8
                ConsumerTimeoutMillisWhenSuspend, // 9
                CommunicationMode.ASYNC, // 10
                pullCallback// 11
                );
        }
        catch (Exception e) {
            log.error("pullKernelImpl exception", e);
            this.executePullRequestLater(pullRequest, PullTimeDelayMillsWhenException);
        }
    }


    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end)
            throws MQClientException, InterruptedException {
        return this.mQClientFactory.getMQAdminImpl().queryMessage(topic, key, maxNum, begin, end);
    }


    public void registerMessageListener(MessageListener messageListener) {
        this.messageListenerInner = messageListener;
    }


    public void resume() {
        this.pause = false;
        log.info("resume this consumer, {}", this.defaultMQPushConsumer.getConsumerGroup());
    }


    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return this.mQClientFactory.getMQAdminImpl().searchOffset(mq, timestamp);
    }


    public void sendMessageBack(MessageExt msg, int delayLevel) throws RemotingException, MQBrokerException,
            InterruptedException, MQClientException {
        try {
            this.mQClientFactory.getMQClientAPIImpl().consumerSendMessageBack(msg,
                this.defaultMQPushConsumer.getConsumerGroup(), delayLevel, 3000);
        }
        catch (Exception e) {
            log.error("sendMessageBack Exception, " + this.defaultMQPushConsumer.getConsumerGroup(), e);

            Message newMsg =
                    new Message(MixAll.getRetryTopic(this.defaultMQPushConsumer.getConsumerGroup()),
                        msg.getBody());

            newMsg.setFlag(msg.getFlag());
            newMsg.setProperties(msg.getProperties());
            newMsg.putProperty(Message.PROPERTY_RETRY_TOPIC, msg.getTopic());

            this.mQClientFactory.getDefaultMQProducer().send(newMsg);
        }
    }


    public void setOffsetStore(OffsetStore offsetStore) {
        this.offsetStore = offsetStore;
    }


    public void setPause(boolean pause) {
        this.pause = pause;
    }


    public void shutdown() {
        switch (this.serviceState) {
        case CREATE_JUST:
            break;
        case RUNNING:
            this.consumeMessageService.shutdown();
            this.persistConsumerOffset();
            this.mQClientFactory.unregisterConsumer(this.defaultMQPushConsumer.getConsumerGroup());
            this.mQClientFactory.shutdown();
            log.info("the consumer [{}] shutdown OK", this.defaultMQPushConsumer.getConsumerGroup());
            this.serviceState = ServiceState.SHUTDOWN_ALREADY;
            break;
        case SHUTDOWN_ALREADY:
            break;
        default:
            break;
        }
    }


    public void start() throws MQClientException {
        switch (this.serviceState) {
        case CREATE_JUST:
            this.checkConfig();

            // ���ƶ��Ĺ�ϵ
            this.copySubscription();

            this.serviceState = ServiceState.RUNNING;

            this.mQClientFactory =
                    MQClientManager.getInstance().getAndCreateMQClientFactory(this.defaultMQPushConsumer);

            // ��ʼ��Rebalance����
            this.rebalanceImpl.setConsumerGroup(this.defaultMQPushConsumer.getConsumerGroup());
            this.rebalanceImpl.setMessageModel(this.defaultMQPushConsumer.getMessageModel());
            this.rebalanceImpl.setAllocateMessageQueueStrategy(this.defaultMQPushConsumer
                .getAllocateMessageQueueStrategy());
            this.rebalanceImpl.setmQClientFactory(this.mQClientFactory);

            this.pullAPIWrapper = new PullAPIWrapper(//
                mQClientFactory,//
                this.defaultMQPushConsumer.getConsumerGroup());

            if (this.defaultMQPushConsumer.getOffsetStore() != null) {
                this.offsetStore = this.defaultMQPushConsumer.getOffsetStore();
            }
            else {
                // �㲥����/��Ⱥ����
                switch (this.defaultMQPushConsumer.getMessageModel()) {
                case BROADCASTING:
                    this.offsetStore =
                            new LocalFileOffsetStore(this.mQClientFactory,
                                this.defaultMQPushConsumer.getConsumerGroup());
                    break;
                case CLUSTERING:
                    this.offsetStore =
                            new RemoteBrokerOffsetStore(this.mQClientFactory,
                                this.defaultMQPushConsumer.getConsumerGroup());
                    break;
                default:
                    break;
                }
            }
            // �������ѽ���
            this.offsetStore.load();

            // ����������Ϣ����
            if (this.getMessageListenerInner() instanceof MessageListenerOrderly) {
                this.consumeOrderly = true;
                this.consumeMessageService =
                        new ConsumeMessageOrderlyService(this,
                            (MessageListenerOrderly) this.getMessageListenerInner());
            }
            else if (this.getMessageListenerInner() instanceof MessageListenerConcurrently) {
                this.consumeOrderly = false;
                this.consumeMessageService =
                        new ConsumeMessageConcurrentlyService(this,
                            (MessageListenerConcurrently) this.getMessageListenerInner());
            }

            this.consumeMessageService.start();

            boolean registerOK =
                    mQClientFactory.registerConsumer(this.defaultMQPushConsumer.getConsumerGroup(), this);
            if (!registerOK) {
                this.serviceState = ServiceState.CREATE_JUST;
                this.consumeMessageService.shutdown();
                throw new MQClientException("The consumer group["
                        + this.defaultMQPushConsumer.getConsumerGroup()
                        + "] has created already, specifed another name please."
                        + FAQUrl.suggestTodo(FAQUrl.GROUP_NAME_DUPLICATE_URL), null);
            }

            mQClientFactory.start();
            log.info("the consumer [{}] start OK", this.defaultMQPushConsumer.getConsumerGroup());
            break;
        case RUNNING:
            break;
        case SHUTDOWN_ALREADY:
            break;
        default:
            break;
        }

        this.updateTopicSubscribeInfoWhenSubscriptionChanged();

        this.mQClientFactory.sendHeartbeatToAllBrokerWithLock();

        this.mQClientFactory.rebalanceImmediately();
    }


    public void subscribe(String topic, String subExpression) throws MQClientException {
        try {
            SubscriptionData subscriptionData = FilterAPI.buildSubscriptionData(topic, subExpression);
            this.rebalanceImpl.getSubscriptionInner().put(topic, subscriptionData);
            // ����������������Ķ��Ĺ�ϵע����ȥ
            if (this.mQClientFactory != null) {
                this.mQClientFactory.sendHeartbeatToAllBrokerWithLock();
            }
        }
        catch (Exception e) {
            throw new MQClientException("subscription exception", e);
        }
    }


    @Override
    public Set<SubscriptionData> subscriptions() {
        Set<SubscriptionData> subSet = new HashSet<SubscriptionData>();

        subSet.addAll(this.rebalanceImpl.getSubscriptionInner().values());

        return subSet;
    }


    public void suspend() {
        this.pause = true;
        log.info("suspend this consumer, {}", this.defaultMQPushConsumer.getConsumerGroup());
    }


    public void unsubscribe(String topic) {
        this.rebalanceImpl.getSubscriptionInner().remove(topic);
    }


    public void updateConsumeOffset(MessageQueue mq, long offset) {
        this.offsetStore.updateOffset(mq, offset, false);
    }


    @Override
    public void updateTopicSubscribeInfo(String topic, Set<MessageQueue> info) {
        Map<String, SubscriptionData> subTable = this.getSubscriptionInner();
        if (subTable != null) {
            if (subTable.containsKey(topic)) {
                this.rebalanceImpl.topicSubscribeInfoTable.put(topic, info);
            }
        }
    }


    private void updateTopicSubscribeInfoWhenSubscriptionChanged() {
        Map<String, SubscriptionData> subTable = this.getSubscriptionInner();
        if (subTable != null) {
            for (final Map.Entry<String, SubscriptionData> entry : subTable.entrySet()) {
                final String topic = entry.getKey();
                this.mQClientFactory.updateTopicRouteInfoFromNameServer(topic);
            }
        }
    }


    public MessageExt viewMessage(String msgId) throws RemotingException, MQBrokerException,
            InterruptedException, MQClientException {
        return this.mQClientFactory.getMQAdminImpl().viewMessage(msgId);
    }


    public RebalanceImpl getRebalanceImpl() {
        return rebalanceImpl;
    }


    public boolean isConsumeOrderly() {
        return consumeOrderly;
    }


    public void setConsumeOrderly(boolean consumeOrderly) {
        this.consumeOrderly = consumeOrderly;
    }


    public ConsumerStatManager getConsumerStatManager() {
        return consumerStatManager;
    }
}
