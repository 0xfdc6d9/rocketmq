/**
 * $Id: PullAPIWrapper.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.impl.consumer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.rocketmq.client.consumer.PullCallback;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.consumer.PullStatus;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.CommunicationMode;
import com.alibaba.rocketmq.client.impl.FindBrokerResult;
import com.alibaba.rocketmq.client.impl.factory.MQClientFactory;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageDecoder;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.header.PullMessageRequestHeader;
import com.alibaba.rocketmq.common.protocol.heartbeat.SubscriptionData;
import com.alibaba.rocketmq.common.sysflag.PullSysFlag;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * ��Pull�ӿڽ��н�һ���ķ�װ
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public class PullAPIWrapper {
    private ConcurrentHashMap<MessageQueue, AtomicLong/* brokerId */> pullFromWhichNodeTable =
            new ConcurrentHashMap<MessageQueue, AtomicLong>(32);

    private final MQClientFactory mQClientFactory;
    private final String consumerGroup;


    public PullAPIWrapper(MQClientFactory mQClientFactory, String consumerGroup) {
        this.mQClientFactory = mQClientFactory;
        this.consumerGroup = consumerGroup;
    }


    public void updatePullFromWhichNode(final MessageQueue mq, final long brokerId) {
        AtomicLong suggest = this.pullFromWhichNodeTable.get(mq);
        if (null == suggest) {
            this.pullFromWhichNodeTable.put(mq, new AtomicLong(brokerId));
        }
        else {
            suggest.set(brokerId);
        }
    }


    /**
     * ����ȡ������д�����Ҫ����Ϣ�����л�
     */
    public PullResult processPullResult(final MessageQueue mq, final PullResult pullResult,
            final SubscriptionData subscriptionData) {
        PullResultExt pullResultExt = (PullResultExt) pullResult;

        this.updatePullFromWhichNode(mq, pullResultExt.getSuggestWhichBrokerId());
        if (PullStatus.FOUND == pullResult.getPullStatus()) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(pullResultExt.getMessageBinary());
            List<MessageExt> msgList = MessageDecoder.decodes(byteBuffer);

            // ��Ϣ�ٴι���
            List<MessageExt> msgListFilterAgain = msgList;
            if (!subscriptionData.getTagsSet().isEmpty()) {
                msgListFilterAgain = new ArrayList<MessageExt>(msgList.size());

                for (MessageExt msg : msgList) {
                    if (msg.getTags() != null) {
                        if (subscriptionData.getTagsSet().contains(msg.getTags())) {
                            msgListFilterAgain.add(msg);
                        }
                    }
                }
            }

            // ��Ϣ�з�����е������СOffset������Ӧ������֪��Ϣ�ѻ��̶�
            for (MessageExt msg : msgListFilterAgain) {
                msg.putProperty(Message.PROPERTY_MIN_OFFSET, Long.toString(pullResult.getMinOffset()));
                msg.putProperty(Message.PROPERTY_MAX_OFFSET, Long.toString(pullResult.getMaxOffset()));
            }

            pullResultExt.setMsgFoundList(msgListFilterAgain);
        }

        // ��GC�ͷ��ڴ�
        pullResultExt.setMessageBinary(null);

        return pullResult;
    }


    /**
     * ÿ�����ж�Ӧ������Ӧ�ı�����������ĸ���������
     */
    public long recalculatePullFromWhichNode(final MessageQueue mq) {
        AtomicLong suggest = this.pullFromWhichNodeTable.get(mq);
        if (suggest != null) {
            return suggest.get();
        }

        return MixAll.MASTER_ID;
    }


    public PullResult pullKernelImpl(//
            final MessageQueue mq,// 1
            final String subExpression,// 2
            final long subVersion,// 3
            final long offset,// 4
            final int maxNums,// 5
            final int sysFlag,// 6
            final long commitOffset,// 7
            final long brokerSuspendMaxTimeMillis,// 8
            final long timeoutMillis,// 9
            final CommunicationMode communicationMode,// 10
            final PullCallback pullCallback// 11
    ) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        FindBrokerResult findBrokerResult =
                this.mQClientFactory.findBrokerAddressInSubscribe(mq.getBrokerName(),
                    this.recalculatePullFromWhichNode(mq), false);
        if (null == findBrokerResult) {
            // TODO �˴����ܶ�Name Serverѹ��������Ҫ����
            this.mQClientFactory.updateTopicRouteInfoFromNameServer(mq.getTopic());
            findBrokerResult =
                    this.mQClientFactory.findBrokerAddressInSubscribe(mq.getBrokerName(),
                        this.recalculatePullFromWhichNode(mq), false);
        }

        if (findBrokerResult != null) {
            int sysFlagInner = sysFlag;

            // Slave������ʵʱ�ύ���ѽ��ȣ����Զ�ʱ�ύ
            if (findBrokerResult.isSlave()) {
                sysFlagInner = PullSysFlag.clearCommitOffsetFlag(sysFlagInner);
            }

            PullMessageRequestHeader requestHeader = new PullMessageRequestHeader();
            requestHeader.setConsumerGroup(this.consumerGroup);
            requestHeader.setTopic(mq.getTopic());
            requestHeader.setQueueId(mq.getQueueId());
            requestHeader.setQueueOffset(offset);
            requestHeader.setMaxMsgNums(maxNums);
            requestHeader.setSysFlag(sysFlagInner);
            requestHeader.setCommitOffset(commitOffset);
            requestHeader.setSuspendTimeoutMillis(brokerSuspendMaxTimeMillis);
            requestHeader.setSubscription(subExpression);
            requestHeader.setSubVersion(subVersion);

            PullResult pullResult = this.mQClientFactory.getMQClientAPIImpl().pullMessage(//
                findBrokerResult.getBrokerAddr(),//
                requestHeader,//
                timeoutMillis,//
                communicationMode,//
                pullCallback);

            return pullResult;
        }

        throw new MQClientException("The broker[" + mq.getBrokerName() + "] not exist", null);
    }
}
