/**
 * $Id: MQProducer.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.producer;

import java.util.List;

import com.alibaba.rocketmq.client.MQAdmin;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.Message;
import com.alibaba.rocketmq.common.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * ��Ϣ������
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public interface MQProducer extends MQAdmin {
    /**
     * ��������
     * 
     * @throws MQClientException
     */
    public void start() throws MQClientException;


    /**
     * �رշ���
     */
    public void shutdown();


    /**
     * ����topic��ȡ��Ӧ��MessageQueue�������˳����Ϣ������˳����Ϣ���÷���
     * 
     * @param topic
     *            ��ϢTopic
     * @return ���ض��м���
     * @throws MQClientException
     */
    public List<MessageQueue> fetchPublishMessageQueues(final String topic) throws MQClientException;


    /**
     * ������Ϣ��ͬ������
     * 
     * @param msg
     *            ��Ϣ
     * @return ���ͽ��
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     * @throws MQClientException
     */
    public SendResult send(final Message msg) throws MQClientException, RemotingException, MQBrokerException,
            InterruptedException;


    /**
     * ������Ϣ���첽����
     * 
     * @param msg
     *            ��Ϣ
     * @param sendCallback
     *            ���ͽ��ͨ���˽ӿڻص�
     * @throws MQClientException
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void send(final Message msg, final SendCallback sendCallback) throws MQClientException,
            RemotingException, InterruptedException;


    /**
     * ������Ϣ��Oneway��ʽ����������Ӧ���޷���֤��Ϣ�Ƿ�ɹ����������
     * 
     * @param msg
     *            ��Ϣ
     * @throws MQClientException
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void sendOneway(final Message msg) throws MQClientException, RemotingException, InterruptedException;


    /**
     * ��ָ�����з�����Ϣ��ͬ������
     * 
     * @param msg
     *            ��Ϣ
     * @param mq
     *            ����
     * @return ���ͽ��
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     * @throws MQClientException
     */
    public SendResult send(final Message msg, final MessageQueue mq) throws MQClientException, RemotingException,
            MQBrokerException, InterruptedException;


    /**
     * ��ָ�����з�����Ϣ���첽����
     * 
     * @param msg
     *            ��Ϣ
     * @param mq
     *            ����
     * @param sendCallback
     *            ���ͽ��ͨ���˽ӿڻص�
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     */
    public void send(final Message msg, final MessageQueue mq, final SendCallback sendCallback)
            throws MQClientException, RemotingException, InterruptedException;


    /**
     * ��ָ�����з�����Ϣ��Oneway��ʽ����������Ӧ���޷���֤��Ϣ�Ƿ�ɹ����������
     * 
     * @param msg
     *            ��Ϣ
     * @param mq
     *            ����
     * @throws MQClientException
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void sendOneway(final Message msg, final MessageQueue mq) throws MQClientException, RemotingException,
            InterruptedException;


    /**
     * ������Ϣ�������Զ���ѡ����У����е��������ܻ�����Broker����ͣ�仯<br>
     * ���Ҫ��֤��Ϣ�ϸ���������Metaq��ά��Ա����Topicʱ����Ҫ�ر�˵��<br>
     * ͬ������
     * 
     * @param msg
     *            ��Ϣ
     * @param selector
     *            ����ѡ����������ʱ��ص�
     * @param arg
     *            �ص�����ѡ����ʱ���˲����ᴫ�����ѡ�񷽷�
     * @return ���ͽ��
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     * @throws MQClientException
     */
    public SendResult send(final Message msg, final MessageQueueSelector selector, final Object arg)
            throws MQClientException, RemotingException, MQBrokerException, InterruptedException;


    /**
     * ������Ϣ�������Զ���ѡ����У����е��������ܻ�����Broker����ͣ�仯<br>
     * ���Ҫ��֤��Ϣ�ϸ���������Metaq��ά��Ա����Topicʱ����Ҫ�ر�˵��<br>
     * �첽����
     * 
     * @param msg
     *            ��Ϣ
     * @param selector
     *            ����ѡ����������ʱ��ص�
     * @param arg
     *            �ص�����ѡ����ʱ���˲����ᴫ�����ѡ�񷽷�
     * @param sendCallback
     *            ���ͽ��ͨ���˽ӿڻص�
     * @throws MQClientException
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void send(final Message msg, final MessageQueueSelector selector, final Object arg,
            final SendCallback sendCallback) throws MQClientException, RemotingException, InterruptedException;


    /**
     * ������Ϣ�������Զ���ѡ����У����е��������ܻ�����Broker����ͣ�仯<br>
     * ���Ҫ��֤��Ϣ�ϸ���������Metaq��ά��Ա����Topicʱ����Ҫ�ر�˵��<br>
     * Oneway��ʽ����������Ӧ���޷���֤��Ϣ�Ƿ�ɹ����������
     * 
     * @param msg
     *            ��Ϣ
     * @param selector
     *            ����ѡ����������ʱ��ص�
     * @param arg
     *            �ص�����ѡ����ʱ���˲����ᴫ�����ѡ�񷽷�
     * @throws MQClientException
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void sendOneway(final Message msg, final MessageQueueSelector selector, final Object arg)
            throws MQClientException, RemotingException, InterruptedException;


    public void sendMessageInTransaction(final Message msg, final LocalTransactionExecuter tranExecuter)
            throws MQClientException;
}
