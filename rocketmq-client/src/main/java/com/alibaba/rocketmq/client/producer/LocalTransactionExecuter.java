/**
 * $Id: LocalTransactionExecuter.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.producer;

/**
 * ִ�б���������Metaq�ͻ��˻ص�
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public interface LocalTransactionExecuter {
    public boolean executeLocalTransactionBranch();
}
