/**
 * $Id: DefaultMQProducerImpl.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.impl.producer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.rocketmq.client.QueryResult;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.CommunicationMode;
import com.alibaba.rocketmq.client.impl.MQClientManager;
import com.alibaba.rocketmq.client.impl.factory.MQClientFactory;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.LocalTransactionExecuter;
import com.alibaba.rocketmq.client.producer.LocalTransactionState;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.Message;
import com.alibaba.rocketmq.common.MessageDecoder;
import com.alibaba.rocketmq.common.MessageExt;
import com.alibaba.rocketmq.common.MessageId;
import com.alibaba.rocketmq.common.MessageQueue;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.ServiceState;
import com.alibaba.rocketmq.common.TopicFilterType;
import com.alibaba.rocketmq.common.UtilALl;
import com.alibaba.rocketmq.common.protocol.MQProtos.MQResponseCode;
import com.alibaba.rocketmq.common.protocol.header.EndTransactionRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.SendMessageRequestHeader;
import com.alibaba.rocketmq.common.sysflag.MessageSysFlag;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.alibaba.rocketmq.remoting.protocol.RemotingProtos.ResponseCode;


/**
 * ������Ĭ��ʵ��
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public class DefaultMQProducerImpl implements MQProducerInner {
    private ServiceState serviceState = ServiceState.CREATE_JUST;

    private final DefaultMQProducer defaultMQProducer;

    private final ConcurrentHashMap<String/* topic */, TopicPublishInfo> topicPublishInfoTable =
            new ConcurrentHashMap<String, TopicPublishInfo>();

    private MQClientFactory mQClientFactory;


    public DefaultMQProducerImpl(final DefaultMQProducer defaultMQProducer) {
        this.defaultMQProducer = defaultMQProducer;
    }


    private void checkConfig() throws MQClientException {
        if (null == this.defaultMQProducer.getProducerGroup()) {
            throw new MQClientException("producerGroup is null", null);
        }

        if (this.defaultMQProducer.getProducerGroup().equals(MixAll.DEFAULT_PRODUCER_GROUP)) {
            throw new MQClientException("producerGroup can not equal " + MixAll.DEFAULT_PRODUCER_GROUP
                    + ", please specify another one.", null);
        }
    }


    public void start() throws MQClientException {
        switch (this.serviceState) {
        case CREATE_JUST:
            this.checkConfig();

            this.serviceState = ServiceState.RUNNING;

            this.mQClientFactory =
                    MQClientManager.getInstance().getAndCreateMQClientFactory(
                        this.defaultMQProducer.getMQClientConfig());

            boolean registerOK = mQClientFactory.registerProducer(this.defaultMQProducer.getProducerGroup(), this);
            if (!registerOK) {
                this.serviceState = ServiceState.CREATE_JUST;
                throw new MQClientException("The producer group[" + this.defaultMQProducer.getProducerGroup()
                        + "] has created already, specifed another name please.", null);
            }

            // Ĭ��Topicע��
            this.topicPublishInfoTable.put(this.defaultMQProducer.getCreateTopicKey(), new TopicPublishInfo());

            mQClientFactory.start();
            break;
        case RUNNING:
            break;
        case SHUTDOWN_ALREADY:
            break;
        default:
            break;
        }
    }


    public void shutdown() {
        switch (this.serviceState) {
        case CREATE_JUST:
            break;
        case RUNNING:
            this.serviceState = ServiceState.SHUTDOWN_ALREADY;
            this.mQClientFactory.unregisterProducer(this.defaultMQProducer.getProducerGroup());
            this.mQClientFactory.shutdown();
            break;
        case SHUTDOWN_ALREADY:
            break;
        default:
            break;
        }
    }


    private void makeSureStateOK() throws MQClientException {
        if (this.serviceState != ServiceState.RUNNING) {
            throw new MQClientException("The producer service state not OK", null);
        }
    }


    @Override
    public void updateTopicPublishInfo(final String topic, final TopicPublishInfo info) {
        if (info != null && topic != null) {
            TopicPublishInfo prev = this.topicPublishInfoTable.put(topic, info);
            if (prev != null) {
                // TODO log
            }
        }
    }


    @Override
    public Set<String> getPublishTopicList() {
        Set<String> topicList = new HashSet<String>();
        for (String key : this.topicPublishInfoTable.keySet()) {
            topicList.add(key);
        }

        return topicList;
    }


    public void createTopic(String key, String newTopic, int queueNum, TopicFilterType topicFilterType,
            boolean order) throws MQClientException {
        this.makeSureStateOK();
        this.mQClientFactory.getMQAdminImpl().createTopic(key, newTopic, queueNum, topicFilterType, order);
    }


    public List<MessageQueue> fetchPublishMessageQueues(String topic) throws MQClientException {
        this.makeSureStateOK();
        return this.mQClientFactory.getMQAdminImpl().fetchPublishMessageQueues(topic);
    }


    public long searchOffset(MessageQueue mq, long timestamp) throws MQClientException {
        this.makeSureStateOK();
        return this.mQClientFactory.getMQAdminImpl().searchOffset(mq, timestamp);
    }


    public long getMaxOffset(MessageQueue mq) throws MQClientException {
        this.makeSureStateOK();
        return this.mQClientFactory.getMQAdminImpl().getMaxOffset(mq);
    }


    public long getMinOffset(MessageQueue mq) throws MQClientException {
        this.makeSureStateOK();
        return this.mQClientFactory.getMQAdminImpl().getMinOffset(mq);
    }


    public long getEarliestMsgStoreTime(MessageQueue mq) throws MQClientException {
        this.makeSureStateOK();
        return this.mQClientFactory.getMQAdminImpl().getEarliestMsgStoreTime(mq);
    }


    public MessageExt viewMessage(String msgId) throws RemotingException, MQBrokerException, InterruptedException,
            MQClientException {
        this.makeSureStateOK();

        return this.mQClientFactory.getMQAdminImpl().viewMessage(msgId);
    }


    public QueryResult queryMessage(String topic, String key, int maxNum, long begin, long end)
            throws MQClientException, InterruptedException {
        this.makeSureStateOK();
        return this.mQClientFactory.getMQAdminImpl().queryMessage(topic, key, maxNum, begin, end);
    }


    private void checkMessage(Message msg) throws MQClientException {
        // topic TODO
        // tags TODO
        // keys TODO
        // body TODO
        if (null == msg.getBody()) {
            throw new MQClientException("the message body is null", null);
        }

        if (msg.getBody().length > this.defaultMQProducer.getMaxMessageSize()) {
            throw new MQClientException("the message body size over max value, MAX: "
                    + this.defaultMQProducer.getMaxMessageSize(), null);
        }
    }


    private boolean tryToCompressMessage(final Message msg) {
        byte[] body = msg.getBody();
        if (body != null) {
            if (body.length >= this.defaultMQProducer.getCompressMsgBodyOverHowmuch()) {
                try {
                    byte[] data = UtilALl.compress(body, 9);
                    if (data != null) {
                        msg.setBody(data);
                        return true;
                    }
                }
                catch (IOException e) {
                    // TODO log
                }
            }
        }

        return false;
    }


    private SendResult sendDefaultImpl(//
            Message msg,//
            final CommunicationMode communicationMode,//
            final SendCallback sendCallback//
    ) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        final long beginTimestamp = System.currentTimeMillis();
        long endTimestamp = beginTimestamp;
        TopicPublishInfo topicPublishInfo = this.tryToFindTopicPublishInfo(msg.getTopic());
        if (topicPublishInfo != null && topicPublishInfo.ok()) {
            MessageQueue mq = null;
            Exception exception = null;
            SendResult sendResult = null;
            for (int times = 0; times < 3
                    && (endTimestamp - beginTimestamp) < this.defaultMQProducer.getSendMsgTimeout(); times++) {
                String lastBrokerName = null == mq ? null : mq.getBrokerName();
                mq = topicPublishInfo.selectOneMessageQueue(lastBrokerName);
                if (mq != null) {
                    try {
                        sendResult = this.sendKernelImpl(msg, mq, communicationMode, sendCallback);
                        endTimestamp = System.currentTimeMillis();
                        switch (communicationMode) {
                        case ASYNC:
                            return null;
                        case ONEWAY:
                            return null;
                        case SYNC:
                            if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                                if (this.defaultMQProducer.isRetryAnotherBrokerWhenNotStoreOK()) {
                                    continue;
                                }
                            }

                            return sendResult;
                        default:
                            break;
                        }
                    }
                    catch (RemotingException e) {
                        exception = e;
                        endTimestamp = System.currentTimeMillis();
                        continue;
                    }
                    catch (MQClientException e) {
                        exception = e;
                        endTimestamp = System.currentTimeMillis();
                        continue;
                    }
                    catch (MQBrokerException e) {
                        exception = e;
                        endTimestamp = System.currentTimeMillis();
                        switch (e.getResponseCode()) {
                        case MQResponseCode.TOPIC_NOT_EXIST_VALUE:
                        case MQResponseCode.SERVICE_NOT_AVAILABLE_VALUE:
                        case ResponseCode.SYSTEM_ERROR_VALUE:
                        case MQResponseCode.NO_PERMISSION_VALUE:
                            continue;
                        default:
                            if (sendResult != null) {
                                return sendResult;
                            }

                            throw e;
                        }
                    }
                    catch (InterruptedException e) {
                        throw e;
                    }
                }
                else {
                    break;
                }
            } // end of for

            if (sendResult != null) {
                return sendResult;
            }

            throw new MQClientException("Retry many times, still failed", exception);
        }

        throw new MQClientException("No route info of this topic, " + msg.getTopic(), null);
    }


    private SendResult sendKernelImpl(final Message msg,//
            final MessageQueue mq,//
            final CommunicationMode communicationMode,//
            final SendCallback sendCallback//
    ) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        String brokerAddr = this.mQClientFactory.findBrokerAddressInPublish(mq.getBrokerName());
        if (null == brokerAddr) {
            // TODO �˴����ܶ�Name Serverѹ��������Ҫ����
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(mq.getTopic());
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(this.defaultMQProducer.getCreateTopicKey());
            brokerAddr = this.mQClientFactory.findBrokerAddressInPublish(mq.getBrokerName());
        }

        if (brokerAddr != null) {
            byte[] prevBody = msg.getBody();
            try {
                int sysFlag = 0;
                if (this.tryToCompressMessage(msg)) {
                    sysFlag |= MessageSysFlag.CompressedFlag;
                }

                final String tranMsg = msg.getProperty(Message.PROPERTY_TRANSACTION_PREPARED);
                if (tranMsg != null && Boolean.parseBoolean(tranMsg)) {
                    sysFlag |= MessageSysFlag.TransactionPreparedType;
                }

                SendMessageRequestHeader requestHeader = new SendMessageRequestHeader();
                requestHeader.setProducerGroup(this.defaultMQProducer.getProducerGroup());
                requestHeader.setTopic(msg.getTopic());
                requestHeader.setDefaultTopic(this.defaultMQProducer.getCreateTopicKey());
                requestHeader.setDefaultTopicQueueNums(this.defaultMQProducer.getDefaultTopicQueueNums());
                requestHeader.setQueueId(mq.getQueueId());
                requestHeader.setSysFlag(sysFlag);
                requestHeader.setBornTimestamp(System.currentTimeMillis());
                requestHeader.setFlag(msg.getFlag());
                requestHeader.setProperties(MessageDecoder.messageProperties2String(msg.getProperties()));

                SendResult sendResult = this.mQClientFactory.getMQClientAPIImpl().sendMessage(//
                    brokerAddr,// 1
                    mq.getBrokerName(),// 2
                    msg,// 3
                    requestHeader,// 4
                    this.defaultMQProducer.getSendMsgTimeout(),// 5
                    communicationMode,// 6
                    sendCallback// 7
                    );

                return sendResult;
            }
            finally {
                msg.setBody(prevBody);
            }
        }

        throw new MQClientException("The broker[" + mq.getBrokerName() + "] not exist", null);
    }


    private SendResult sendSelectImpl(//
            Message msg,//
            MessageQueueSelector selector,//
            Object arg,//
            final CommunicationMode communicationMode,//
            final SendCallback sendCallback//
    ) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        TopicPublishInfo topicPublishInfo = this.tryToFindTopicPublishInfo(msg.getTopic());
        if (topicPublishInfo != null && topicPublishInfo.ok()) {
            MessageQueue mq = null;
            try {
                mq = selector.select(topicPublishInfo.getMessageQueueList(), msg, arg);
            }
            catch (Throwable e) {
                throw new MQClientException("select message queue throwed exception.", e);
            }

            if (mq != null) {
                return this.sendKernelImpl(msg, mq, communicationMode, sendCallback);
            }
            else {
                throw new MQClientException("select message queue return null.", null);
            }
        }

        throw new MQClientException("No route info for this topic, " + msg.getTopic(), null);
    }


    /**
     * ����Ѱ��Topic·����Ϣ�����û����Name Server���ң���û�У���ȡĬ��Topic
     */
    private TopicPublishInfo tryToFindTopicPublishInfo(final String topic) {
        TopicPublishInfo topicPublishInfo = this.topicPublishInfoTable.get(topic);
        if (null == topicPublishInfo) {
            this.topicPublishInfoTable.putIfAbsent(topic, new TopicPublishInfo());
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(topic);
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(this.defaultMQProducer.getCreateTopicKey());
            topicPublishInfo = this.topicPublishInfoTable.get(topic);
        }

        if (topicPublishInfo != null && topicPublishInfo.ok()) {
            return topicPublishInfo;
        }

        return this.topicPublishInfoTable.get(this.defaultMQProducer.getCreateTopicKey());
    }


    /**
     * DEFAULT SYNC -------------------------------------------------------
     */
    public SendResult send(Message msg) throws MQClientException, RemotingException, MQBrokerException,
            InterruptedException {
        this.makeSureStateOK();

        this.checkMessage(msg);

        return this.sendDefaultImpl(msg, CommunicationMode.SYNC, null);
    }


    /**
     * DEFAULT ASYNC -------------------------------------------------------
     */
    public void send(Message msg, SendCallback sendCallback) throws MQClientException, RemotingException,
            InterruptedException {
        this.makeSureStateOK();

        this.checkMessage(msg);

        try {
            this.sendDefaultImpl(msg, CommunicationMode.ASYNC, sendCallback);
        }
        catch (MQBrokerException e) {
            throw new MQClientException("unknow exception", e);
        }
    }


    /**
     * DEFAULT ONEWAY -------------------------------------------------------
     */
    public void sendOneway(Message msg) throws MQClientException, RemotingException, InterruptedException {
        this.makeSureStateOK();

        this.checkMessage(msg);

        try {
            this.sendDefaultImpl(msg, CommunicationMode.ONEWAY, null);
        }
        catch (MQBrokerException e) {
            throw new MQClientException("unknow exception", e);
        }
    }


    /**
     * KERNEL SYNC -------------------------------------------------------
     */
    public SendResult send(Message msg, MessageQueue mq) throws MQClientException, RemotingException,
            MQBrokerException, InterruptedException {
        this.makeSureStateOK();

        this.checkMessage(msg);

        return this.sendKernelImpl(msg, mq, CommunicationMode.SYNC, null);
    }


    /**
     * KERNEL ASYNC -------------------------------------------------------
     */
    public void send(Message msg, MessageQueue mq, SendCallback sendCallback) throws MQClientException,
            RemotingException, InterruptedException {
        this.makeSureStateOK();

        this.checkMessage(msg);

        try {
            this.sendKernelImpl(msg, mq, CommunicationMode.ASYNC, sendCallback);
        }
        catch (MQBrokerException e) {
            throw new MQClientException("unknow exception", e);
        }
    }


    /**
     * KERNEL ONEWAY -------------------------------------------------------
     */
    public void sendOneway(Message msg, MessageQueue mq) throws MQClientException, RemotingException,
            InterruptedException {
        this.makeSureStateOK();

        this.checkMessage(msg);

        try {
            this.sendKernelImpl(msg, mq, CommunicationMode.ONEWAY, null);
        }
        catch (MQBrokerException e) {
            throw new MQClientException("unknow exception", e);
        }
    }


    /**
     * SELECT SYNC -------------------------------------------------------
     */
    public SendResult send(Message msg, MessageQueueSelector selector, Object arg) throws MQClientException,
            RemotingException, MQBrokerException, InterruptedException {
        return this.sendSelectImpl(msg, selector, arg, CommunicationMode.SYNC, null);
    }


    /**
     * SELECT ASYNC -------------------------------------------------------
     */
    public void send(Message msg, MessageQueueSelector selector, Object arg, SendCallback sendCallback)
            throws MQClientException, RemotingException, InterruptedException {
        try {
            this.sendSelectImpl(msg, selector, arg, CommunicationMode.ASYNC, sendCallback);
        }
        catch (MQBrokerException e) {
            throw new MQClientException("unknow exception", e);
        }
    }


    /**
     * SELECT ONEWAY -------------------------------------------------------
     */
    public void sendOneway(Message msg, MessageQueueSelector selector, Object arg) throws MQClientException,
            RemotingException, InterruptedException {
        try {
            this.sendSelectImpl(msg, selector, arg, CommunicationMode.ONEWAY, null);
        }
        catch (MQBrokerException e) {
            throw new MQClientException("unknow exception", e);
        }
    }


    private void endTransaction(final SendResult sendResult, final LocalTransactionState localTransactionState)
            throws RemotingException, MQBrokerException, InterruptedException, UnknownHostException {
        final MessageId id = MessageDecoder.decodeMessageId(sendResult.getMsgId());
        final String addr = RemotingUtil.socketAddress2String(id.getAddress());
        EndTransactionRequestHeader requestHeader = new EndTransactionRequestHeader();
        requestHeader.setCommitLogOffset(id.getOffset());
        switch (localTransactionState) {
        case COMMIT_MESSAGE:
            requestHeader.setCommitOrRollback(MessageSysFlag.TransactionCommitType);
            break;
        case ROLLBACK_MESSAGE:
            requestHeader.setCommitOrRollback(MessageSysFlag.TransactionRollbackType);
            break;
        case UNKNOW:
            break;
        default:
            break;
        }

        requestHeader.setProducerGroup(this.defaultMQProducer.getProducerGroup());
        requestHeader.setTranStateTableOffset(sendResult.getQueueOffset());
        this.mQClientFactory.getMQClientAPIImpl().endTransaction(addr, requestHeader,
            this.defaultMQProducer.getSendMsgTimeout());
    }


    public SendResult sendMessageInTransaction(final Message msg, final LocalTransactionExecuter tranExecuter)
            throws MQClientException {
        if (null == msg) {
            throw new MQClientException("msg is null", null);
        }

        if (null == tranExecuter) {
            throw new MQClientException("tranExecuter is null", null);
        }

        // ��һ������Broker����һ��Prepared��Ϣ
        SendResult sendResult = null;
        msg.putProperty(Message.PROPERTY_TRANSACTION_PREPARED, "true");
        msg.putProperty(Message.PROPERTY_PRODUCER_GROUP, this.defaultMQProducer.getProducerGroup());
        try {
            sendResult = this.send(msg);
        }
        catch (Exception e) {
            throw new MQClientException("send message Exception", e);
        }

        // �ڶ������ص���������
        LocalTransactionState localTransactionState = LocalTransactionState.UNKNOW;
        MQClientException exception = null;
        try {
            localTransactionState = tranExecuter.executeLocalTransactionBranch(msg);
            if (null == localTransactionState) {
                localTransactionState = LocalTransactionState.UNKNOW;
            }
        }
        catch (Throwable e) {
            exception =
                    new MQClientException("send message OK, but execute local transaction branch Exception", e);
        }

        // ���������ύ���߻ع�Broker����Ϣ
        switch (localTransactionState) {
        case COMMIT_MESSAGE:
        case ROLLBACK_MESSAGE:
            try {
                this.endTransaction(sendResult, localTransactionState);
            }
            catch (Exception e) {
                throw new MQClientException("local transaction execute " + localTransactionState
                        + ", but end broker transaction failed", e);
            }
            break;
        // ���������׳��쳣���߷���δ֪״̬��������ʱ���ύҲ���ع����ȴ�����������check
        case UNKNOW:
            // TODO log
        default:
            break;

        }

        return sendResult;
    }
}
