/**
 * 
 */
package com.alibaba.rocketmq.client.producer;

/**
 * Producer��������ִ��״̬
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public enum LocalTransactionState {
    COMMIT_MESSAGE,
    ROLLBACK_MESSAGE,
    UNKNOW,
}
