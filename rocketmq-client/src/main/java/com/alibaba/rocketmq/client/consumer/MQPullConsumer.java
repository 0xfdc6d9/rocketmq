/**
 * $Id: MQPullConsumer.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import java.util.Set;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * �����ߣ�������ʽ����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface MQPullConsumer extends MQConsumer {
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
     * ע��������б仯��listener����
     * 
     * @param topic
     * @param listener
     *            һ�������仯���ͻ��˻������ص�listener����
     */
    public void registerMessageQueueListener(final String topic, final MessageQueueListener listener);


    /**
     * ָ�����У�������ȡ��Ϣ����ʹû����Ϣ��Ҳ���̷���
     * 
     * @param mq
     *            ָ������Ҫ��ȡ�Ķ���
     * @param subExpression
     *            ���Ĺ��˱��ʽ�ַ�����broker���ݴ˱��ʽ���й��ˡ�<br>
     *            eg: "tag1 || tag2 || tag3"<br>
     *            "tag1 || (tag2 && tag3)"<br>
     *            ���subExpression=null, ���ʾȫ������
     * @param offset
     *            ��ָ�������ĸ�λ�ÿ�ʼ��ȡ
     * @param maxNums
     *            һ�������ȡ����
     * @return �μ�PullResult
     * @throws MQClientException
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     */
    public PullResult pull(final MessageQueue mq, final String subExpression, final long offset, final int maxNums)
            throws MQClientException, RemotingException, MQBrokerException, InterruptedException;


    public void pull(final MessageQueue mq, final String subExpression, final long offset, final int maxNums,
            final PullCallback pullCallback) throws MQClientException, RemotingException, InterruptedException;


    /**
     * ָ�����У�������ȡ��Ϣ�����û����Ϣ����broker����һ��ʱ���ٷ��أ�ʱ������ã�<br>
     * broker�����ڼ䣬�������Ϣ�������̽���Ϣ����
     * 
     * @param mq
     *            ָ������Ҫ��ȡ�Ķ���
     * @param subExpression
     *            ���Ĺ��˱��ʽ�ַ�����broker���ݴ˱��ʽ���й��ˡ�<br>
     *            eg: "tag1 || tag2 || tag3"<br>
     *            "tag1 || (tag2 && tag3)"<br>
     *            ���subExpression=null, ���ʾȫ������
     * @param offset
     *            ��ָ�������ĸ�λ�ÿ�ʼ��ȡ
     * @param maxNums
     *            һ�������ȡ����
     * @return �μ�PullResult
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     * @throws MQClientException
     */
    public PullResult pullBlockIfNotFound(final MessageQueue mq, final String subExpression, final long offset,
            final int maxNums) throws MQClientException, RemotingException, MQBrokerException,
            InterruptedException;


    public void pullBlockIfNotFound(final MessageQueue mq, final String subExpression, final long offset,
            final int maxNums, final PullCallback pullCallback) throws MQClientException, RemotingException,
            InterruptedException;


    /**
     * �������ѽ���<br>
     * ֻ�Ǹ���Consumer�����е����ݣ�����ǹ㲥ģʽ����ʱ���µ����ش洢<br>
     * ����Ǽ�Ⱥģʽ����ʱ���µ�Զ��Broker<br>
     * 
     * P.S. ��Ƶ�����ã������ܿ���
     * 
     * @param mq
     * @param offset
     * @throws MQClientException
     */
    public void updateConsumeOffset(final MessageQueue mq, final long offset) throws MQClientException;


    /**
     * ��ȡ���ѽ��ȣ�����-1��ʾ����
     * 
     * @param mq
     * @param fromStore
     * @return
     * @throws MQClientException
     */
    public long fetchConsumeOffset(final MessageQueue mq, final boolean fromStore) throws MQClientException;


    /**
     * ����topic��ȡMessageQueue���Ծ��ⷽʽ�����ڶ����Ա֮�����
     * 
     * @param topic
     *            ��ϢTopic
     * @return �ɹ� ���ض��м��� ʧ�� ����null
     * 
     */
    public Set<MessageQueue> fetchMessageQueuesInBalance(final String topic);
}
