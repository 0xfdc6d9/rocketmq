package com.alibaba.rocketmq.client.consumer.listener;

/**
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum ConsumeOrderlyStatus {
    // ��Ϣ����ɹ�
    SUCCESS,
    // �ع���Ϣ
    ROLLBACK,
    // �ύ��Ϣ
    COMMIT,
    // ����ǰ���й���һС���
    SUSPEND_CURRENT_QUEUE_A_MOMENT,
}
