/**
 * $Id: BrokerRole.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store.config;

/**
 * Broker��ɫ
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public enum BrokerRole {
    // �첽����Master
    ASYNC_MASTER,
    // ͬ��˫дMaster
    SYNC_MASTER,
    // Slave
    SLAVE
}
