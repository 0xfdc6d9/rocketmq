/**
 * $Id: ConsumeOrderlyStatus.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.listener;

/**
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public enum ConsumeOrderlyStatus {
    // ��Ϣ����ɹ�
    SUCCESS,
    // �ع���Ϣ
    ROLLBACK,
    // �ύ��Ϣ
    COMMIT,
    // ��������
    RETRY_IMMEDIATELY,
    // ����ǰ���й���һС���
    SUSPEND_CURRENT_QUEUE_A_MOMENT,
}
