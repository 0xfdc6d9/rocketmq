/**
 * $Id: ConsumeType.java 1835 2013-05-16 02:00:50Z shijia.wxr $
 */
package com.alibaba.rocketmq.common.protocol.heartbeat;

/**
 * ��������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum ConsumeType {
    /**
     * ������ʽ����
     */
    CONSUME_ACTIVELY,
    /**
     * ������ʽ����
     */
    CONSUME_PASSIVELY,
}
