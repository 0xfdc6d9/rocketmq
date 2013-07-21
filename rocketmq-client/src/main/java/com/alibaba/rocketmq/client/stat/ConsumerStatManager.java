/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

            // �������
            {
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

                log.info(
                    "Consumer, {} {}, ConsumeAvgRT: {} ConsumeMaxRT: {} TotalOKMsg: {} TotalFailedMsg: {} consumeTPS: {}",//
                    group, //
                    clientId, //
                    avgRT, //
                    last.getConsumeMsgRTMax(), //
                    last.getConsumeMsgOKTotal(), //
                    last.getConsumeMsgFailedTotal(), //
                    tps//
                );
            }

            // ����Ϣ���
            {
                double avgRT = (last.getPullRTTotal().get() - first.getPullRTTotal().get()) //
                        / //
                        (double) (last.getPullTimesTotal().get() - first.getPullTimesTotal().get());

                log.info("Consumer, {} {}, PullAvgRT: {}  PullTimesTotal: {}",//
                    group, //
                    clientId, //
                    avgRT, //
                    last.getPullTimesTotal() //
                );
            }
        }
    }
}
