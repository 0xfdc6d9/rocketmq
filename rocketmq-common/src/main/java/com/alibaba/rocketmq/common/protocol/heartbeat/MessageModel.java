/**
 * $Id: MessageModel.java 1835 2013-05-16 02:00:50Z shijia.wxr $
 */
package com.alibaba.rocketmq.common.protocol.heartbeat;

/**
 * ��Ϣģ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum MessageModel {
    /**
     * �㲥ģ��
     */
    BROADCASTING,
    /**
     * ��Ⱥģ��
     */
    CLUSTERING,
//    /**
//     * δ֪��������������ѣ�����ȷ��Ӧ�õ���Ϣģ��
//     */
//    UNKNOWNS,
}
