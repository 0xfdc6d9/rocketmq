/**
 * $Id: ServiceThread.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.remoting.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ��̨�����̻߳���
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public abstract class ServiceThread implements Runnable {
    private static final Logger stlog = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);
    // ִ���߳�
    protected final Thread thread;
    // �̻߳���ʱ�䣬Ĭ��90S
    private static final long JoinTime = 90 * 1000;
    // �Ƿ��Ѿ���Notify��
    protected volatile boolean hasNotified = false;
    // �߳��Ƿ��Ѿ�ֹͣ
    protected volatile boolean stoped = false;


    public ServiceThread() {
        this.thread = new Thread(this, this.getServiceName());
    }


    public abstract String getServiceName();


    public void start() {
        this.thread.start();
    }


    public void shutdown() {
        this.shutdown(false);
    }


    public void stop() {
        this.stop(false);
    }


    public void makeStop() {
        this.stoped = true;
        stlog.info("makestop thead " + this.getServiceName());
    }


    public void stop(final boolean interrupt) {
        this.stoped = true;
        stlog.info("stop thead " + this.getServiceName() + " interrupt " + interrupt);
        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }

        if (interrupt) {
            this.thread.interrupt();
        }
    }


    public void shutdown(final boolean interrupt) {
        this.stoped = true;
        stlog.info("shutdown thead " + this.getServiceName() + " interrupt " + interrupt);
        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }

        try {
            if (interrupt) {
                this.thread.interrupt();
            }

            long beginTime = System.currentTimeMillis();
            this.thread.join(this.getJointime());
            long eclipseTime = System.currentTimeMillis() - beginTime;
            stlog.info("join thead " + this.getServiceName() + " eclipse time(ms) " + eclipseTime + " "
                    + this.getJointime());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void wakeup() {
        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }
    }


    protected void waitForRunning(long interval) {
        synchronized (this) {
            if (this.hasNotified) {
                this.hasNotified = false;
                this.onWaitEnd();
                return;
            }

            try {
                this.wait(interval);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                this.hasNotified = false;
                this.onWaitEnd();
            }
        }
    }


    protected void onWaitEnd() {
    }


    public boolean isStoped() {
        return stoped;
    }


    public long getJointime() {
        return JoinTime;
    }
}
