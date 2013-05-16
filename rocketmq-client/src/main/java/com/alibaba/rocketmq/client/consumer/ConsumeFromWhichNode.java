/**
 * $Id: ConsumeFromWhichNode.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

/**
 * Consumer��Master����Slave������Ϣ
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public enum ConsumeFromWhichNode {
    /**
     * ���ȴ�Master����Ϣ�����Master�����ڻ�����Ϣ�ѻ�����ת��Slave
     */
    CONSUME_FROM_MASTER_FIRST,
    /**
     * ���ȴ�Slave����Ϣ�����Slave�����ڣ���ת��Master
     */
    CONSUME_FROM_SLAVE_FIRST,
    /**
     * ֻ��Master����Ϣ
     */
    CONSUME_FROM_MASTER_ONLY,
    /**
     * ֻ��Slave����Ϣ
     */
    CONSUME_FROM_SLAVE_ONLY,
}
