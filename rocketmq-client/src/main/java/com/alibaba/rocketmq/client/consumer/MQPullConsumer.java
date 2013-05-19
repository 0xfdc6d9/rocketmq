/**
 * $Id: MQPullConsumer.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer;

import java.util.List;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * �����ߣ�������ʽ����
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
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


    public void updateConsumeOffset(final MessageQueue mq, final long offset) throws RemotingException,
            MQBrokerException, InterruptedException, MQClientException;


    public long fetchConsumeOffset(final MessageQueue mq) throws RemotingException, MQBrokerException,
            InterruptedException, MQClientException;


    /**
     * ����topic��ȡMessageQueue���Ծ��ⷽʽ�����ڶ����Ա֮�����
     * 
     * @param topic
     *            ��ϢTopic
     * @return �ɹ� ���ض��м��� ʧ�� ����null
     * 
     */
    public List<MessageQueue> fetchMessageQueuesInBalance(final String topic);
}
