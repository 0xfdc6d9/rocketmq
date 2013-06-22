package com.alibaba.rocketmq.common.consumer;

/**
 * Consumer�����￪ʼ����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum ConsumeFromWhere {
    /**
     * ÿ�����������ϴμ�¼��λ�㿪ʼ���ѣ�����ǵ�һ������������λ�㿪ʼ���ѣ���������������ʹ��
     */
    CONSUME_FROM_LAST_OFFSET,
    /**
     * ÿ�����������ϴμ�¼��λ�㿪ʼ���ѣ�����ǵ�һ�����������Сλ�㿪ʼ���ѣ��������ʱʹ��<br>
     * ���ϻ����������������Ҫ��ˣ�������Ч
     */
    CONSUME_FROM_LAST_OFFSET_AND_FROM_MIN_WHEN_BOOT_FIRST,
    /**
     * ÿ������������Сλ�㿪ʼ���ѣ��������ʱʹ��<br>
     * ���ϻ����������������Ҫ��ˣ�������Ч
     */
    CONSUME_FROM_MIN_OFFSET,
    /**
     * ÿ�������������λ�㿪ʼ���ѣ��������ʱʹ��
     */
    CONSUME_FROM_MAX_OFFSET,
}
