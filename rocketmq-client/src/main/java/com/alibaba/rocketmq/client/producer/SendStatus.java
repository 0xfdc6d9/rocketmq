/**
 * $Id: SendStatus.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.producer;

/**
 * ��4��״̬����ʾ��Ϣ�Ѿ��ɹ�����Master
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum SendStatus {
    SEND_OK,
    FLUSH_DISK_TIMEOUT,
    FLUSH_SLAVE_TIMEOUT,
    SLAVE_NOT_AVAILABLE
}
