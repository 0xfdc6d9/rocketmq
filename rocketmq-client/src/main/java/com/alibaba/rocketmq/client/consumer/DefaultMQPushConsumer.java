/**
 * $Id: DefaultMQPushConsumer.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.rocketmq.client.MQClientConfig;
import com.alibaba.rocketmq.client.QueryResult;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.consumer.loadbalance.AllocateMessageQueueAveragely;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import com.alibaba.rocketmq.common.MessageExt;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.MessageQueue;
import com.alibaba.rocketmq.common.TopicFilterType;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * ������Broker Push��Ϣ��Consumer��ʽ����ʵ����Ȼ��Consumer�ڲ���̨��Broker Pull��Ϣ
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public class DefaultMQPushConsumer implements MQPushConsumer {
    /**
     * �ͻ��˹������ã��������޸�
     */
    private MQClientConfig mQClientConfig = new MQClientConfig();
    /**
     * ��ͬ�������Consumer��Ϊͬһ��Group��Ӧ�ñ������ã�����֤����Ψһ
     */
    private String consumerGroup = MixAll.DEFAULT_CONSUMER_GROUP;
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
     * ������Ϣ�߳���
     */
    private int consumeThreadCount = 10;
    /**
     * ͬһ���в������ѵ�����ȣ�˳�����ѷ�ʽ����£��˲�����Ч
     */
    private int consumeConcurrentlyMaxSpan = 1000;
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


    // ////////////////////////////////////////////////////////////////////////

    public int getPullBatchSize() {
        return pullBatchSize;
    }


    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
    }

}
