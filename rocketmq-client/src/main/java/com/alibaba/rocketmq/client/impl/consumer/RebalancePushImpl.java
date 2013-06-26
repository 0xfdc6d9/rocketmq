package com.alibaba.rocketmq.client.impl.consumer;

import java.util.List;
import java.util.Set;

import com.alibaba.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.alibaba.rocketmq.client.consumer.store.OffsetStore;
import com.alibaba.rocketmq.client.impl.factory.MQClientFactory;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-6-22
 */
public class RebalancePushImpl extends RebalanceImpl {
    private final DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;


    public RebalancePushImpl(DefaultMQPushConsumerImpl defaultMQPushConsumerImpl) {
        this(null, null, null, null, defaultMQPushConsumerImpl);
    }


    public RebalancePushImpl(String consumerGroup, MessageModel messageModel,
            AllocateMessageQueueStrategy allocateMessageQueueStrategy, MQClientFactory mQClientFactory,
            DefaultMQPushConsumerImpl defaultMQPushConsumerImpl) {
        super(consumerGroup, messageModel, allocateMessageQueueStrategy, mQClientFactory);
        this.defaultMQPushConsumerImpl = defaultMQPushConsumerImpl;
    }


    @Override
    public void dispatchPullRequest(List<PullRequest> pullRequestList) {
        // �ɷ�PullRequest
        for (PullRequest pullRequest : pullRequestList) {
            this.defaultMQPushConsumerImpl.executePullRequestImmediately(pullRequest);
            log.info("doRebalance, {}, add a new pull request {}", consumerGroup, pullRequest);
        }
    }


    @Override
    public long computePullFromWhere(MessageQueue mq) {
        long result = -1;
        final ConsumeFromWhere consumeFromWhere =
                this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getConsumeFromWhere();
        final OffsetStore offsetStore = this.defaultMQPushConsumerImpl.getOffsetStore();
        switch (consumeFromWhere) {
        case CONSUME_FROM_LAST_OFFSET: {
            long lastOffset = offsetStore.readOffset(mq, true);
            if (lastOffset >= 0) {
                result = lastOffset;
            }
            // ��ǰ�������ڷ�����û�ж�Ӧ��Offset
            // ˵���ǵ�һ������
            else if (-1 == lastOffset) {
                result = Long.MAX_VALUE;
            }
            // ������������
            else {
                result = -1;
            }
            break;
        }
        case CONSUME_FROM_LAST_OFFSET_AND_FROM_MIN_WHEN_BOOT_FIRST: {
            long lastOffset = offsetStore.readOffset(mq, true);
            if (lastOffset >= 0) {
                result = lastOffset;
            }
            // ��ǰ�������ڷ�����û�ж�Ӧ��Offset
            // ˵���ǵ�һ������
            else if (-1 == lastOffset) {
                result = 0L;
            }
            // ������������
            else {
                result = -1;
            }
            break;
        }
        case CONSUME_FROM_MAX_OFFSET:
            result = Long.MAX_VALUE;
            break;
        case CONSUME_FROM_MIN_OFFSET:
            result = 0L;
            break;
        default:
            break;
        }

        return result;
    }


    @Override
    public void messageQueueChanged(String topic, Set<MessageQueue> mqAll, Set<MessageQueue> mqDivided) {
    }


    @Override
    public void removeUnnecessaryMessageQueue(MessageQueue mq, ProcessQueue pq) {
        this.unlock(mq, true);
    }
}
