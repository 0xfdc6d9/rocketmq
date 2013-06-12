/**
 * $Id: DefaultMQPushConsumer.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.rocketmq.client.ClientConfig;
import com.alibaba.rocketmq.client.QueryResult;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.TopicFilterType;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * ������Broker Push��Ϣ��Consumer��ʽ����ʵ����Ȼ��Consumer�ڲ���̨��Broker Pull��Ϣ<br>
 * ���ó���ѯ��ʽ����Ϣ��ʵʱ��ͬpush��ʽһ�£��Ҳ�����ν������Ϣ����Brokerѹ������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
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
     * Consumer��Master����Slave����Ϣ
     */
    private ConsumeFromWhichNode consumeFromWhichNode = ConsumeFromWhichNode.CONSUME_FROM_MASTER_FIRST;
    /**
     * Consumer����ʱ�������￪ʼ����
     */
    private ConsumeFromWhereOffset consumeFromWhereOffset = ConsumeFromWhereOffset.CONSUME_FROM_LAST_OFFSET;
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

    private final transient DefaultMQPushConsumerImpl defaultMQPushConsumerImpl = new DefaultMQPushConsumerImpl(
        this);


    public DefaultMQPushConsumer() {

    }


    public DefaultMQPushConsumer(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }


    @Override
    public void sendMessageBack(MessageExt msg, MessageQueue mq, int delayLevel) {
        // TODO Auto-generated method stub
    }


    @Override
    public void createTopic(String key, String newTopic, int queueNum, TopicFilterType topicFilterType,
            boolean order) throws MQClientException {
        // TODO Auto-generated method stub

    }


    @Override
    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public long getMaxOffset(MessageQueue mq) throws MQClientException {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public long getMinOffset(MessageQueue mq) throws MQClientException {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public long getEarliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public MessageExt viewMessage(String msgId) throws RemotingException, MQBrokerException, InterruptedException,
            MQClientException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end)
            throws MQClientException, InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void start() {
        // TODO Auto-generated method stub

    }


    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }


    @Override
    public void registerMessageListener(MessageListener messageListener) {
        // TODO Auto-generated method stub

    }


    @Override
    public void subscribe(String topic, String subExpression) {
        // TODO Auto-generated method stub

    }


    @Override
    public void unsubscribe(String topic) {
        // TODO Auto-generated method stub

    }


    @Override
    public void suspend() {
        // TODO Auto-generated method stub

    }


    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }


    @Override
    public List<MessageQueue> fetchSubscribeMessageQueues(String topic) throws MQClientException {
        // TODO Auto-generated method stub
        return null;
    }


    public String getConsumerGroup() {
        return consumerGroup;
    }


    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }


    public ConsumeFromWhichNode getConsumeFromWhichNode() {
        return consumeFromWhichNode;
    }


    public void setConsumeFromWhichNode(ConsumeFromWhichNode consumeFromWhichNode) {
        this.consumeFromWhichNode = consumeFromWhichNode;
    }


    public ConsumeFromWhereOffset getConsumeFromWhereOffset() {
        return consumeFromWhereOffset;
    }


    public void setConsumeFromWhereOffset(ConsumeFromWhereOffset consumeFromWhereOffset) {
        this.consumeFromWhereOffset = consumeFromWhereOffset;
    }


    public AllocateMessageQueueStrategy getAllocateMessageQueueStrategy() {
        return allocateMessageQueueStrategy;
    }


    public void setAllocateMessageQueueStrategy(AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
        this.allocateMessageQueueStrategy = allocateMessageQueueStrategy;
    }


    public Map<String, String> getSubscription() {
        return subscription;
    }


    public void setSubscription(Map<String, String> subscription) {
        this.subscription = subscription;
    }


    public MessageListener getMessageListener() {
        return messageListener;
    }


    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }


    public int getConsumeThreadMin() {
        return consumeThreadMin;
    }


    public void setConsumeThreadMin(int consumeThreadMin) {
        this.consumeThreadMin = consumeThreadMin;
    }


    public int getConsumeConcurrentlyMaxSpan() {
        return consumeConcurrentlyMaxSpan;
    }


    public void setConsumeConcurrentlyMaxSpan(int consumeConcurrentlyMaxSpan) {
        this.consumeConcurrentlyMaxSpan = consumeConcurrentlyMaxSpan;
    }


    public int getConsumeMessageBatchMaxSize() {
        return consumeMessageBatchMaxSize;
    }


    public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
        this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
    }


    public int getPullBatchSize() {
        return pullBatchSize;
    }


    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }


    public DefaultMQPushConsumerImpl getDefaultMQPushConsumerImpl() {
        return defaultMQPushConsumerImpl;
    }


    public MessageModel getMessageModel() {
        return messageModel;
    }


    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }


    public int getConsumeThreadMax() {
        return consumeThreadMax;
    }


    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }


    public int getPullThresholdForQueue() {
        return pullThresholdForQueue;
    }


    public void setPullThresholdForQueue(int pullThresholdForQueue) {
        this.pullThresholdForQueue = pullThresholdForQueue;
    }


    public long getPullInterval() {
        return pullInterval;
    }


    public void setPullInterval(long pullInterval) {
        this.pullInterval = pullInterval;
    }
}
