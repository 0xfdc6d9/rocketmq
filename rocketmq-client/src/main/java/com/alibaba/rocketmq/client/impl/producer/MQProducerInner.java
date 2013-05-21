package com.alibaba.rocketmq.client.impl.producer;

import java.util.Set;


/**
 * Producer�ڲ��ӿ�
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public interface MQProducerInner {
    public Set<String> getPublishTopicList();


    public void updateTopicPublishInfo(final String topic, final TopicPublishInfo info);
}
