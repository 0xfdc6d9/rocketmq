/**
 * $Id: GetMessageStatus.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

/**
 * ������Ϣ���ص�״̬��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public enum GetMessageStatus {
    // �ҵ���Ϣ
    FOUND,
    // offset��ȷ�����ǹ��˺�û��ƥ�����Ϣ
    NO_MATCHED_MESSAGE,
    // offset��ȷ���������������Ϣ���ڱ�ɾ��
    MESSAGE_WAS_REMOVING,
    // offset��ȷ�����Ǵ��߼�����û���ҵ����������ڱ�ɾ��
    OFFSET_FOUND_NULL,
    // offset�����������
    OFFSET_OVERFLOW_BADLY,
    // offset�������1��
    OFFSET_OVERFLOW_ONE,
    // offset����̫С��
    OFFSET_TOO_SMALL,
    // û�ж�Ӧ���߼�����
    NO_MATCHED_LOGIC_QUEUE,
    // ������һ����Ϣ��û��
    NO_MESSAGE_IN_QUEUE,
}
