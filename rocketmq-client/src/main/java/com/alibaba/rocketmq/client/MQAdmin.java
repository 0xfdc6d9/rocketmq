/**
 * $Id: MQAdmin.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * MQ������ӿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface MQAdmin {
    /**
     * ����topic
     * 
     * @param key
     *            ������ά��Ա����
     * @param newTopic
     *            Ҫ��������topic
     * @param queueNum
     *            ��topic������
     * @param order
     *            �Ƿ����ϸ��˳����Ϣ
     * @throws MQClientException
     */
    public void createTopic(final String key, final String newTopic, final int queueNum, final boolean order)
            throws MQClientException;


    /**
     * ����ʱ���ѯ��Ӧ��offset����ȷ������<br>
     * P.S. ��ǰ�ӿ��н϶�IO����������Ƶ������
     * 
     * @param mq
     *            ����
     * @param timestamp
     *            ������ʽʱ���
     * @return ָ��ʱ���Ӧ��offset
     * @throws MQClientException
     */
    public long searchOffset(final MessageQueue mq, final long timestamp) throws MQClientException;


    /**
     * ���������ѯ�������Offset PS: ���Offset�޶�Ӧ��Ϣ����1����Ϣ
     * 
     * @param mq
     *            ����
     * @return ���е����Offset
     * @throws MQClientException
     */
    public long maxOffset(final MessageQueue mq) throws MQClientException;


    /**
     * ���������ѯ������СOffset PS: ��СOffset�ж�Ӧ��Ϣ
     * 
     * @param mq
     *            ����
     * @return ���е���СOffset
     * @throws MQClientException
     */
    public long minOffset(final MessageQueue mq) throws MQClientException;


    /**
     * ���������ѯ���б����������Ϣ��Ӧ�Ĵ洢ʱ��
     * 
     * @param mq
     *            ����
     * @return ������Ϣ��Ӧ�Ĵ洢ʱ�䣬��ȷ������
     * @throws MQClientException
     */
    public long earliestMsgStoreTime(final MessageQueue mq) throws MQClientException;


    /**
     * ������ϢID���ӷ�������ȡ��������Ϣ
     * 
     * @param msgId
     * @return ������Ϣ
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     * @throws MQClientException
     */
    public MessageExt viewMessage(final String msgId) throws RemotingException, MQBrokerException,
            InterruptedException, MQClientException;


    /**
     * ������ϢKey��ѯ��Ϣ
     * 
     * @param topic
     *            ��Ϣ����
     * @param key
     *            ��Ϣ�ؼ���
     * @param maxNum
     *            ��ѯ�������
     * @param begin
     *            ��ʼʱ���
     * @param end
     *            ����ʱ���
     * @return ��ѯ���
     * @throws MQClientException
     * @throws InterruptedException
     */
    public QueryResult queryMessage(final String topic, final String key, final int maxNum, final long begin,
            final long end) throws MQClientException, InterruptedException;
}
