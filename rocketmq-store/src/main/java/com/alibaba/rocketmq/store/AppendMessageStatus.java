/**
 * $Id: AppendMessageStatus.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

/**
 * ���������д��Ϣ���ؽ����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public enum AppendMessageStatus {
    // �ɹ�׷����Ϣ
    PUT_OK,
    // �ߵ��ļ�ĩβ
    END_OF_FILE,
    // ��Ϣ��С����
    MESSAGE_SIZE_EXCEEDED,
    // δ֪����
    UNKNOWN_ERROR,
}
