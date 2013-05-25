/**
 * $Id: WaitNotifyObject.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store.ha;

import java.util.HashMap;


/**
 * �������߳�֮���첽֪ͨ
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class WaitNotifyObject {
    // �Ƿ��Ѿ���Notify��
    protected volatile boolean hasNotified = false;
    // �Ƿ��Ѿ���Notify�����㲥ģʽ
    protected final HashMap<Long/* thread id */, Boolean/* notified */> waitingThreadTable =
            new HashMap<Long, Boolean>(16);


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


    /**
     * �㲥��ʽ����
     */
    public void wakeupAll() {
        synchronized (this) {
            boolean needNotify = false;

            for (Boolean value : this.waitingThreadTable.values()) {
                needNotify = needNotify || !value;
                value = true;
            }

            if (needNotify) {
                this.notifyAll();
            }
        }
    }


    /**
     * ����̵߳���wait
     */
    public void allWaitForRunning(long interval) {
        long currentThreadId = Thread.currentThread().getId();
        synchronized (this) {
            Boolean notified = this.waitingThreadTable.get(currentThreadId);
            if (notified != null && notified) {
                this.waitingThreadTable.put(currentThreadId, false);
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
                this.waitingThreadTable.put(currentThreadId, false);
                this.onWaitEnd();
            }
        }
    }


    protected void onWaitEnd() {
    }
}
