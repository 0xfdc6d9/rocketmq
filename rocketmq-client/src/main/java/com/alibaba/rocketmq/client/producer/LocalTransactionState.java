/**
 * 
 */
package com.alibaba.rocketmq.client.producer;

/**
 * Producer��������ִ��״̬
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public enum LocalTransactionState {
    COMMIT_MESSAGE,
    ROLLBACK_MESSAGE,
    UNKNOW,
}
