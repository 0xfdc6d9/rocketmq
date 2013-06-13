/**
 * $Id: MQConsumer.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import java.util.Set;

import com.alibaba.rocketmq.client.MQAdmin;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public interface MQConsumer extends MQAdmin {
    /**
     * Consumer����ʧ�ܵ���Ϣ����ѡ�����·��ص��������ˣ�����ʱ����<br>
     * �����ȳ��Խ���Ϣ���ص���Ϣ֮ǰ�洢����������ʱֻ������ϢOffset����Ϣ�岻���ͣ�����ռ���������<br>
     * �������ʧ�ܣ����Զ����Է���������������ʱ��Ϣ��Ҳ�ᴫ��<br>
     * �ش���ȥ����Ϣֻ�ᱻ��ǰConsumer Group���ѡ�
     * 
     * @param msg
     * @param mq
     * @param delayLevel
     */
    public void sendMessageBack(final MessageExt msg, final MessageQueue mq, final int delayLevel);


    /**
     * ����topic��ȡ��Ӧ��MessageQueue���ǿɱ����ĵĶ���
     * 
     * @param topic
     *            ��ϢTopic
     * @return ���ض��м���
     * @throws MQClientException
     */
    public Set<MessageQueue> fetchSubscribeMessageQueues(final String topic) throws MQClientException;

}
