/**
 * $Id: ConsumeFromWhereOffset.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

/**
 * Consumer�����￪ʼ����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum ConsumeFromWhereOffset {
    /**
     * ÿ�����������ϴμ�¼��λ�㿪ʼ���ѣ�����ǵ�һ������������λ�㿪ʼ���ѣ���������������ʹ��
     */
    CONSUME_FROM_LAST_OFFSET,
    /**
     * ÿ�����������ϴμ�¼��λ�㿪ʼ���ѣ�����ǵ�һ�����������Сλ�㿪ʼ���ѣ��������ʱʹ��
     */
    CONSUME_FROM_LAST_OFFSET_AND_FROM_MIN_WHEN_BOOT_FIRST,
    /**
     * ÿ������������Сλ�㿪ʼ���ѣ��������ʱʹ��
     */
    CONSUME_FROM_MIN_OFFSET,
    /**
     * ÿ�������������λ�㿪ʼ���ѣ��������ʱʹ��
     */
    CONSUME_FROM_MAX_OFFSET,
}
