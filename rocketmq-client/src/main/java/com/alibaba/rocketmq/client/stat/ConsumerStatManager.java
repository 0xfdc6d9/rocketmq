package com.alibaba.rocketmq.client.stat;

import java.util.LinkedList;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.log.ClientLogger;


/**
 * ����ͳ��Consumer����״̬
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-7
 */
public class ConsumerStatManager {
    private final Logger log = ClientLogger.getLog();
    private final ConsumerStat consumertat = new ConsumerStat();
    private final LinkedList<ConsumerStat> snapshotList = new LinkedList<ConsumerStat>();


    public ConsumerStat getConsumertat() {
        return consumertat;
    }


    public LinkedList<ConsumerStat> getSnapshotList() {
        return snapshotList;
    }


    /**
     * ÿ��1���¼һ��
     */
    public void recordSnapshotPeriodically() {
        snapshotList.addLast(consumertat.createSnapshot());
        if (snapshotList.size() > 60) {
            snapshotList.removeFirst();
        }
    }


    /**
     * ÿ��1���Ӽ�¼һ��
     */
    public void logStatsPeriodically(final String group, final String clientId) {
        if (this.snapshotList.size() >= 60) {
            ConsumerStat first = this.snapshotList.getFirst();
            ConsumerStat last = this.snapshotList.getLast();

            double avgRT = (last.getConsumeMsgRTTotal().get() - first.getConsumeMsgRTTotal().get()) //
                    / //
                    (double) ((last.getConsumeMsgOKTotal().get() + last.getConsumeMsgFailedTotal().get()) //
                    - //
                    (first.getConsumeMsgOKTotal().get() + first.getConsumeMsgFailedTotal().get()));

            double tps = ((last.getConsumeMsgOKTotal().get() + last.getConsumeMsgFailedTotal().get()) //
                    - //
                    (first.getConsumeMsgOKTotal().get() + first.getConsumeMsgFailedTotal().get()))//
                    / //
                    (double) (last.getCreateTimestamp() - first.getCreateTimestamp());

            tps *= 1000;

            log.info("Consumer, {} {}, AvgRT: {} MaxRT: {} TotalOKMsg: {} TotalFailedMsg: {} consumeTPS: {}",//
                group, //
                clientId, //
                avgRT, //
                last.getConsumeMsgRTMax(), //
                last.getConsumeMsgOKTotal(), //
                last.getConsumeMsgFailedTotal(), //
                tps//
            );
        }
    }
}
