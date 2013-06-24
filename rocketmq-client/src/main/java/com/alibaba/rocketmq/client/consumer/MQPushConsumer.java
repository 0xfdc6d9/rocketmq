/**
 * $Id: MQPushConsumer.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.exception.MQClientException;


/**
 * �����ߣ�������ʽ����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface MQPushConsumer extends MQConsumer {
    /**
     * �������񣬵���֮ǰȷ��registerMessageListener��subscribe���Ѿ�����<br>
     * �����Ѿ�ͨ��Springע�����������
     * 
     * @throws MQClientException
     */
    public void start() throws MQClientException;


    /**
     * �رշ���һ���رգ��˶��󽫲�����
     */
    public void shutdown();


    /**
     * ע����Ϣ��������һ��Consumerֻ����һ��������
     * 
     * @param messageListener
     */
    public void registerMessageListener(final MessageListener messageListener);


    /**
     * ������Ϣ���������Ե��ö�������Ĳ�ͬ��Topic��Ҳ�ɸ���֮ǰTopic�Ķ��Ĺ��˱��ʽ
     * 
     * @param topic
     *            ��Ϣ����
     * @param subExpression
     *            ���Ĺ��˱��ʽ�ַ�����broker���ݴ˱��ʽ���й��ˡ�Ŀǰֻ֧�ֻ�����<br>
     *            eg: "tag1 || tag2 || tag3"<br>
     *            ���subExpression����null����*�����ʾȫ������
     * @param listener
     *            ��Ϣ�ص�������
     * @throws MQClientException
     */
    public void subscribe(final String topic, final String subExpression) throws MQClientException;


    /**
     * ȡ�����ģ��ӵ�ǰ��������ע������Ϣ�ᱻ�����������������߶���
     * 
     * @param topic
     *            ��Ϣ����
     */
    public void unsubscribe(final String topic);


    /**
     * �����̹߳�����ͣ����
     */
    public void suspend();


    /**
     * �����ָ̻߳�����������
     */
    public void resume();
}
