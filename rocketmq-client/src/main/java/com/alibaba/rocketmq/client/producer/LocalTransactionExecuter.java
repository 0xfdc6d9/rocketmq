/**
 * $Id: LocalTransactionExecuter.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.producer;

import com.alibaba.rocketmq.common.Message;


/**
 * ִ�б��������ɿͻ��˻ص�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public interface LocalTransactionExecuter {
    public LocalTransactionState executeLocalTransactionBranch(final Message msg);
}
