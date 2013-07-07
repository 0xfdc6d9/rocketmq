package com.alibaba.rocketmq.client.stat;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Consumer�ڲ�����ʱͳ����Ϣ
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-7
 */
public class ConsumerStat {
    // һ��������Ϣ�����RT
    private final AtomicLong consumeMsgRTMax = new AtomicLong(0);
    // ÿ��������ϢRT�����ܺ�
    private final AtomicLong consumeMsgRTTotal = new AtomicLong(0);
    // ������Ϣ�ɹ������ܺ�
    private final AtomicLong consumeMsgOKTotal = new AtomicLong(0);
    // ������Ϣʧ�ܴ����ܺ�
    private final AtomicLong consumeMsgFailedTotal = new AtomicLong(0);
    // ���ʱ���
    private long createTimestamp;


    public ConsumerStat createSnapshot() {
        ConsumerStat consumerStat = new ConsumerStat();

        consumerStat.getConsumeMsgRTMax().set(this.consumeMsgRTMax.get());
        consumerStat.getConsumeMsgRTTotal().set(this.consumeMsgRTTotal.get());
        consumerStat.getConsumeMsgOKTotal().set(this.consumeMsgOKTotal.get());
        consumerStat.getConsumeMsgFailedTotal().set(this.consumeMsgFailedTotal.get());
        consumerStat.createTimestamp = System.currentTimeMillis();
        return consumerStat;
    }


    public AtomicLong getConsumeMsgRTMax() {
        return consumeMsgRTMax;
    }


    public AtomicLong getConsumeMsgRTTotal() {
        return consumeMsgRTTotal;
    }


    public AtomicLong getConsumeMsgOKTotal() {
        return consumeMsgOKTotal;
    }


    public AtomicLong getConsumeMsgFailedTotal() {
        return consumeMsgFailedTotal;
    }


    public long getCreateTimestamp() {
        return createTimestamp;
    }


    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
}
