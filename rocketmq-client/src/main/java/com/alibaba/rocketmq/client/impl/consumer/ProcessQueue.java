package com.alibaba.rocketmq.client.impl.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.MessageExt;


/**
 * ���ڱ����ѵĶ��У�����Ϣ
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class ProcessQueue {
    private final Logger log = ClientLogger.getLog();
    private final ReadWriteLock lockTreeMap = new ReentrantReadWriteLock();
    private final TreeMap<Long, MessageExt> msgTreeMap = new TreeMap<Long, MessageExt>();
    private final AtomicLong msgCount = new AtomicLong();

    // ��ǰQ�Ƿ�rebalance����
    private volatile boolean droped = false;

    /**
     * ˳����Ϣר��
     */
    // �Ƿ��Broker����
    private volatile boolean locked = false;
    // �Ƿ����ڱ�����
    private volatile boolean consuming = false;
    // ����ʽ���ѣ�δ�ύ����Ϣ
    private final TreeMap<Long, MessageExt> msgTreeMapTemp = new TreeMap<Long, MessageExt>();


    /**
     * @return �Ƿ���Ҫ�ַ���ǰ���е������̳߳�
     */
    public boolean putMessage(final List<MessageExt> msgs) {
        boolean dispathToConsume = false;
        try {
            this.lockTreeMap.writeLock().lockInterruptibly();
            try {
                for (MessageExt msg : msgs) {
                    msgTreeMap.put(msg.getQueueOffset(), msg);
                }
                msgCount.addAndGet(msgs.size());

                if (!msgTreeMap.isEmpty() && !this.consuming) {
                    dispathToConsume = true;
                    this.consuming = true;
                }
            }
            finally {
                this.lockTreeMap.writeLock().unlock();
            }
        }
        catch (InterruptedException e) {
            log.error("putMessage exception", e);
        }

        return dispathToConsume;
    }


    /**
     * ��ȡ��ǰ���е������
     */
    public long getMaxSpan() {
        try {
            this.lockTreeMap.readLock().lockInterruptibly();
            try {
                if (!this.msgTreeMap.isEmpty()) {
                    return this.msgTreeMap.lastKey() - this.msgTreeMap.firstKey();
                }
            }
            finally {
                this.lockTreeMap.readLock().unlock();
            }
        }
        catch (InterruptedException e) {
            log.error("getMaxSpan exception", e);
        }

        return 0;
    }


    /**
     * ɾ���Ѿ����ѹ�����Ϣ��������СOffset�����Offset��Ӧ����Ϣδ����
     * 
     * @param msgs
     * @return
     */
    public long removeMessage(final List<MessageExt> msgs) {
        long result = -1;

        try {
            this.lockTreeMap.writeLock().lockInterruptibly();
            try {
                if (!msgTreeMap.isEmpty()) {
                    result = msgTreeMap.lastKey() + 1;
                    for (MessageExt msg : msgs) {
                        msgTreeMap.remove(msg.getQueueOffset());
                    }
                    msgCount.addAndGet(msgs.size() * (-1));

                    if (!msgTreeMap.isEmpty()) {
                        result = msgTreeMap.firstKey();
                    }
                }
            }
            finally {
                this.lockTreeMap.writeLock().unlock();
            }
        }
        catch (InterruptedException e) {
            log.error("removeMessage exception", e);
        }

        return result;
    }


    public TreeMap<Long, MessageExt> getMsgTreeMap() {
        return msgTreeMap;
    }


    public AtomicLong getMsgCount() {
        return msgCount;
    }


    public boolean isDroped() {
        return droped;
    }


    public void setDroped(boolean droped) {
        this.droped = droped;
    }


    /**
     * ========================================================================
     * ���²���Ϊ˳����Ϣר�в���
     */

    public void setLocked(boolean locked) {
        this.locked = locked;
    }


    public boolean isLocked() {
        return locked;
    }


    public void rollback() {
        try {
            this.lockTreeMap.writeLock().lockInterruptibly();
            try {
                this.msgTreeMap.putAll(this.msgTreeMapTemp);
                this.msgTreeMapTemp.clear();
            }
            finally {
                this.lockTreeMap.writeLock().unlock();
            }
        }
        catch (InterruptedException e) {
            log.error("rollback exception", e);
        }
    }


    public long commit() {
        try {
            this.lockTreeMap.writeLock().lockInterruptibly();
            try {
                Long offset = this.msgTreeMapTemp.lastKey();
                msgCount.addAndGet(this.msgTreeMapTemp.size() * (-1));
                this.msgTreeMapTemp.clear();
                if (offset != null) {
                    return offset + 1;
                }
            }
            finally {
                this.lockTreeMap.writeLock().unlock();
            }
        }
        catch (InterruptedException e) {
            log.error("commit exception", e);
        }

        return -1;
    }


    /**
     * ���ȡ������Ϣ������������״̬��Ϊfalse
     * 
     * @param batchSize
     * @return
     */
    public List<MessageExt> takeMessags(final int batchSize) {
        List<MessageExt> result = new ArrayList<MessageExt>(batchSize);
        try {
            this.lockTreeMap.writeLock().lockInterruptibly();
            try {
                if (!this.msgTreeMap.isEmpty()) {
                    for (int i = 0; i < batchSize; i++) {
                        Map.Entry<Long, MessageExt> entry = this.msgTreeMap.pollFirstEntry();
                        if (entry != null) {
                            result.add(entry.getValue());
                            msgTreeMapTemp.put(entry.getKey(), entry.getValue());
                        }
                        else {
                            break;
                        }
                    }

                    if (result.isEmpty()) {
                        consuming = false;
                    }
                }
            }
            finally {
                this.lockTreeMap.writeLock().unlock();
            }
        }
        catch (InterruptedException e) {
            log.error("takeMessags exception", e);
        }

        return result;
    }
}
