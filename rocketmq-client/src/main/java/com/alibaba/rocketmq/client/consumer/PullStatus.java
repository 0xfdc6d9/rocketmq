/**
 * $Id: PullStatus.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum PullStatus {
    /**
     * �ҵ���Ϣ
     */
    FOUND,
    /**
     * û���µ���Ϣ���Ա���ȡ
     */
    NO_NEW_MSG,
    /**
     * �������˺�û��ƥ�����Ϣ
     */
    NO_MATCHED_MSG,
    /**
     * Offset���Ϸ������ܹ�����߹�С
     */
    OFFSET_ILLEGAL
}
