/**
 * 
 */
package com.alibaba.rocketmq.store.transaction;

/**
 * �洢����Producer�ز�����״̬
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface TransactionCheckExecuter {
    public void gotoCheck(//
            final int producerGroupHashCode,//
            final long tranStateTableOffset,//
            final long commitLogOffset,//
            final int msgSize);
}
