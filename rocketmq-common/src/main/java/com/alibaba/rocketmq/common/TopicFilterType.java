/**
 * $Id: TopicFilterType.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.common;

/**
 * Topic���˷�ʽ��Ĭ��Ϊ��TAG����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum TopicFilterType {
    /**
     * ÿ����Ϣֻ����һ��Tag
     */
    SINGLE_TAG,
    /**
     * ÿ����Ϣ�����ж��Tag����ʱ��֧�֣����������֧�֣�<br>
     * Ϊʲô��ʱ��֧�֣�<br>
     * �˹��ܿ��ܻ���û�������ţ��ҷ������������������ݲ�֧��
     */
    MULTI_TAG
}
