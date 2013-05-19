/**
 * $Id: TransactionCheckListener.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.producer;

import com.alibaba.rocketmq.common.MessageExt;


/**
 * �������ص�Producer����鱾�������֧�ɹ�����ʧ��
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public interface TransactionCheckListener {
    public LocalTransactionState checkLocalTransactionState(final MessageExt msg);
}
