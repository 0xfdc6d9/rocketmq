/**
 * $Id: DefaultMQPushConsumer.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.rocketmq.client.ClientConfig;
import com.alibaba.rocketmq.client.QueryResult;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * ������Broker Push��Ϣ��Consumer��ʽ����ʵ����Ȼ��Consumer�ڲ���̨��Broker Pull��Ϣ<br>
 * ���ó���ѯ��ʽ����Ϣ��ʵʱ��ͬpush��ʽһ�£��Ҳ�����ν������Ϣ����Broker��Consumerѹ������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class DefaultMQPushConsumer extends ClientConfig implements MQPushConsumer {
    /**
     * ��ͬ�������Consumer��Ϊͬһ��Group��Ӧ�ñ������ã�����֤����Ψһ
     */
    private String consumerGroup = MixAll.DEFAULT_CONSUMER_GROUP;
    /**
     * ��Ⱥ����/�㲥����
     */
    private MessageModel messageModel = MessageModel.CLUSTERING;
    /**
     * Consumer����ʱ�������￪ʼ����
     */
    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
    /**
     * ���з����㷨��Ӧ�ÿ���д
     */
    private AllocateMessageQueueStrategy allocateMessageQueueStrategy = new AllocateMessageQueueAveragely();
    /**
     * ���Ĺ�ϵ
     */
    private Map<String /* topic */, String /* sub expression */> subscription = new HashMap<String, String>();
    /**
     * ��Ϣ������
     */
    private MessageListener messageListener;
    /**
     * ������Ϣ�̣߳���С��Ŀ
     */
    private int consumeThreadMin = 10;
    /**
     * ������Ϣ�̣߳������Ŀ
     */
    private int consumeThreadMax = 20;
    /**
     * ͬһ���в������ѵ�����ȣ�˳�����ѷ�ʽ����£��˲�����Ч
     */
    private int consumeConcurrentlyMaxSpan = 2000;
    /**
     * ���ض�����Ϣ�������˷�ֵ����ʼ����
     */
    private int pullThresholdForQueue = 1000;
    /**
     * ����Ϣ��������Ϊ�˽�����ȡ�ٶȣ��������ô���0��ֵ
     */
    private long pullInterval = 0;
    /**
     * ����һ����Ϣ�������
     */
    private int consumeMessageBatchMaxSize = 1;
    /**
     * ����Ϣ��һ����������
     */
    private int pullBatchSize = 32;

    private final transient DefaultMQPushConsumerImpl defaultMQPushConsumerImpl =
            new DefaultMQPushConsumerImpl(this);


    public DefaultMQPushConsumer() {

    }


    public DefaultMQPushConsumer(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }


    @Override
    public void createTopic(String key, String newTopic, int queueNum, boolean order)
            throws MQClientException {
        this.defaultMQPushConsumerImpl.createTopic(key, newTopic, queueNum, order);
    }


    @Override
    public long earliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.earliestMsgStoreTime(mq);
    }


    @Override
    public Set<MessageQueue> fetchSubscribeMessageQueues(String topic) throws MQClientException {
        return this.defaultMQPushConsumerImpl.fetchSubscribeMessageQueues(topic);
    }


    public AllocateMessageQueueStrategy getAllocateMessageQueueStrategy() {
        return allocateMessageQueueStrategy;
    }


    public int getConsumeConcurrentlyMaxSpan() {
        return consumeConcurrentlyMaxSpan;
    }


    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }


    public int getConsumeMessageBatchMaxSize() {
        return consumeMessageBatchMaxSize;
    }


    public String getConsumerGroup() {
        return consumerGroup;
    }


    public int getConsumeThreadMax() {
        return consumeThreadMax;
    }


    public int getConsumeThreadMin() {
        return consumeThreadMin;
    }


    public DefaultMQPushConsumerImpl getDefaultMQPushConsumerImpl() {
        return defaultMQPushConsumerImpl;
    }


    public MessageListener getMessageListener() {
        return messageListener;
    }


    public MessageModel getMessageModel() {
        return messageModel;
    }


    public int getPullBatchSize() {
        return pullBatchSize;
    }


    public long getPullInterval() {
        return pullInterval;
    }


    public int getPullThresholdForQueue() {
        return pullThresholdForQueue;
    }


    public Map<String, String> getSubscription() {
        return subscription;
    }


    @Override
    public long maxOffset(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.maxOffset(mq);
    }


    @Override
    public long minOffset(MessageQueue mq) throws MQClientException {
        return this.defaultMQPushConsumerImpl.minOffset(mq);
    }


    @Override
    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end)
            throws MQClientException, InterruptedException {
        return this.defaultMQPushConsumerImpl.queryMessage(topic, key, maxNum, begin, end);
    }


    @Override
    public void registerMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
        this.defaultMQPushConsumerImpl.registerMessageListener(messageListener);
    }


    @Override
    public void resume() {
        this.defaultMQPushConsumerImpl.resume();
    }


    @Override
    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        return this.defaultMQPushConsumerImpl.searchOffset(mq, timestamp);
    }


    @Override
    public void sendMessageBack(MessageExt msg, int delayLevel) throws RemotingException, MQBrokerException,
            InterruptedException {
        this.defaultMQPushConsumerImpl.sendMessageBack(msg, delayLevel);
    }


    public void setAllocateMessageQueueStrategy(AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
    }


    public void setConsumeConcurrentlyMaxSpan(int consumeConcurrentlyMaxSpan) {
        this.consumeConcurrentlyMaxSpan = consumeConcurrentlyMaxSpan;
    }


    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }


    public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
        this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
    }


    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }


    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }


    public void setConsumeThreadMin(int consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }


    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }


    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }


    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }


    public void setPullInterval(long pullInterval) {
        this.pullInterval = pullInterval;
    }


    public void setPullThresholdForQueue(int pullThresholdForQueue) {
        this.pullThresholdForQueue = pullThresholdForQueue;
    }


    public void setSubscription(Map<String, String> subscription) {
        this.subscription = subscription;
    }


    @Override
    public void shutdown() {
        this.defaultMQPushConsumerImpl.shutdown();
    }


    @Override
    public void start() throws MQClientException {
        this.defaultMQPushConsumerImpl.start();
    }


    @Override
    public void subscribe(String topic, String subExpression) throws MQClientException {
        this.defaultMQPushConsumerImpl.subscribe(topic, subExpression);
    }


    @Override
    public void suspend() {
        this.defaultMQPushConsumerImpl.suspend();
    }


    @Override
    public void unsubscribe(String topic) {
        this.defaultMQPushConsumerImpl.unsubscribe(topic);
    }


    @Override
    public MessageExt viewMessage(String msgId) throws RemotingException, MQBrokerException,
            InterruptedException, MQClientException {
        return this.defaultMQPushConsumerImpl.viewMessage(msgId);
    }
}
