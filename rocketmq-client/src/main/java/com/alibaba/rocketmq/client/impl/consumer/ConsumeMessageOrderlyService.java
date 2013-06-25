package com.alibaba.rocketmq.client.impl.consumer;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * ����������Ϣ����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class ConsumeMessageOrderlyService implements ConsumeMessageService {
    private static final Logger log = ClientLogger.getLog();
    private final DefaultMQPushConsumerImpl defaultMQPushConsumerImpl;
    private final DefaultMQPushConsumer defaultMQPushConsumer;
    private final MessageListenerOrderly messageListener;
    private final BlockingQueue<Runnable> consumeRequestQueue;
    private final ExecutorService consumeExecutor;
    private final String consumerGroup;
    private final MessageQueueLock messageQueueLock = new MessageQueueLock();

    // ��ʱ�߳�
    private final ScheduledExecutorService scheduledExecutorService;


    public ConsumeMessageOrderlyService(DefaultMQPushConsumerImpl defaultMQPushConsumerImpl,
            MessageListenerOrderly messageListener) {
        this.defaultMQPushConsumerImpl = defaultMQPushConsumerImpl;
        this.messageListener = messageListener;

        this.defaultMQPushConsumer = this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer();
        this.consumerGroup = this.defaultMQPushConsumer.getConsumerGroup();
        this.consumeRequestQueue = new LinkedBlockingQueue<Runnable>();

        this.consumeExecutor = new ThreadPoolExecutor(//
            this.defaultMQPushConsumer.getConsumeThreadMin(),//
            this.defaultMQPushConsumer.getConsumeThreadMax(),//
            1000 * 60,//
            TimeUnit.MILLISECONDS,//
            this.consumeRequestQueue,//
            new ThreadFactory() {
                private AtomicLong threadIndex = new AtomicLong(0);


                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "ConsumeMessageThread-" //
                            + ConsumeMessageOrderlyService.this.consumerGroup//
                            + "-" + this.threadIndex.incrementAndGet());
                }
            });

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "ConsumeMessageScheduledThread-" + consumerGroup);
            }
        });
    }


    public void start() {
    }


    public void shutdown() {
        this.scheduledExecutorService.shutdown();
        this.consumeExecutor.shutdown();
    }

    class ConsumeRequest implements Runnable {
        private final ProcessQueue processQueue;
        private final MessageQueue messageQueue;


        public ConsumeRequest(ProcessQueue processQueue, MessageQueue messageQueue) {
            this.processQueue = processQueue;
            this.messageQueue = messageQueue;
        }


        @Override
        public void run() {
            // ��֤�ڵ�ǰConsumer�ڣ�ͬһ���д�������
            final Object objLock = messageQueueLock.fetchLockObject(this.messageQueue);
            synchronized (objLock) {
                // ��֤��Consumer��Ⱥ��ͬһ���д�������
                if (this.processQueue.isLocked()) {
                    for (boolean continueConsume = true; continueConsume;) {
                        if (this.processQueue.isDroped()) {
                            log.info("the message queue not be able to consume, because it's droped {}",
                                this.messageQueue);
                            break;
                        }

                        final int consumeBatchSize =
                                ConsumeMessageOrderlyService.this.defaultMQPushConsumer
                                    .getConsumeMessageBatchMaxSize();

                        List<MessageExt> msgs = this.processQueue.takeMessags(consumeBatchSize);
                        if (!msgs.isEmpty()) {
                            final ConsumeOrderlyContext context =
                                    new ConsumeOrderlyContext(this.messageQueue);

                            ConsumeOrderlyStatus status = null;
                            try {
                                status = messageListener.consumeMessage(msgs, context);
                            }
                            catch (Throwable e) {
                                log.warn("consumeMessage exception, Group: "
                                        + ConsumeMessageOrderlyService.this.consumerGroup//
                                        + " " + msgs//
                                        + " " + messageQueue, e);
                            }

                            // �û��׳��쳣���߷���null�����������
                            if (null == status) {
                                status = ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                            }

                            continueConsume =
                                    ConsumeMessageOrderlyService.this.processConsumeResult(msgs, status,
                                        context, this);
                        }
                        else {
                            continueConsume = false;
                        }
                    }
                }
                // û���õ���ǰ���е������Ժ�������
                else {
                    // TODO
                }
            }
        }
    }


    public boolean processConsumeResult(//
            final List<MessageExt> msgs, //
            final ConsumeOrderlyStatus status, //
            final ConsumeOrderlyContext context, //
            final ConsumeRequest consumeRequest//
    ) {
        boolean continueConsume = false;

        return continueConsume;
    }


    /**
     * ��Consumer���ض�ʱ�߳��ж�ʱ����
     */
    private void submitConsumeRequestLater(//
            final ProcessQueue processQueue, //
            final MessageQueue messageQueue//
    ) {

        this.scheduledExecutorService.schedule(new Runnable() {

            @Override
            public void run() {
                ConsumeMessageOrderlyService.this
                    .submitConsumeRequest(null, processQueue, messageQueue, true);
            }
        }, 5000, TimeUnit.MILLISECONDS);
    }


    @Override
    public void submitConsumeRequest(//
            final List<MessageExt> msgs, //
            final ProcessQueue processQueue, //
            final MessageQueue messageQueue, //
            final boolean isEmptyBefore) {
        if (isEmptyBefore) {
            ConsumeRequest consumeRequest = new ConsumeRequest(processQueue, messageQueue);
            this.consumeExecutor.submit(consumeRequest);
        }
    }
}
