/**
 * $Id: PullMessageService.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.impl.consumer;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.impl.factory.MQClientFactory;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.ServiceThread;


/**
 * ����ѯ����Ϣ���񣬵��߳��첽��ȡ
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class PullMessageService extends ServiceThread {
    private final Logger log = ClientLogger.getLog();
    private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<PullRequest>();
    private final MQClientFactory mQClientFactory;
    private final ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "PullMessageServiceScheduledThread");
            }
        });;


    public PullMessageService(MQClientFactory mQClientFactory) {
        this.mQClientFactory = mQClientFactory;
    }


    /**
     * ֻ��ʱһ��
     */
    public void executePullRequestLater(final PullRequest pullRequest, final long timeDelay) {
        this.scheduledExecutorService.schedule(new Runnable() {

            @Override
            public void run() {
                PullMessageService.this.executePullRequestImmediately(pullRequest);
            }
        }, timeDelay, TimeUnit.MILLISECONDS);
    }


    /**
     * ����ִ��PullRequest
     */
    public void executePullRequestImmediately(final PullRequest pullRequest) {
        try {
            this.pullRequestQueue.put(pullRequest);
        }
        catch (InterruptedException e) {
            log.error("executePullRequestImmediately pullRequestQueue.put", e);
        }
    }


    private void pullMessage(final PullRequest pullRequest) {
        final MQConsumerInner consumer = this.mQClientFactory.selectConsumer(pullRequest.getConsumerGroup());
        if (consumer != null) {
            DefaultMQPushConsumerImpl impl = (DefaultMQPushConsumerImpl) consumer;
            impl.pullMessage(pullRequest);
        }
        else {
            log.warn("No matched consumer for the PullRequest {}, drop it", pullRequest);
        }
    }


    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStoped()) {
            try {
                PullRequest pullRequest = this.pullRequestQueue.take();
                if (pullRequest != null) {
                    this.pullMessage(pullRequest);
                }
            }
            catch (InterruptedException e) {
            }
            catch (Exception e) {
                log.error("Pull Message Service Run Method exception", e);
            }
        }

        log.info(this.getServiceName() + " service end");
    }


    @Override
    public String getServiceName() {
        return PullMessageService.class.getSimpleName();
    }


    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }
}
