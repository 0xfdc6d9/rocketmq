/**
 * $Id: MessageStore.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

import java.util.HashMap;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.SubscriptionData;


/**
 * �洢������ṩ�Ľӿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface MessageStore {

    /**
     * ����ʱ����������
     */
    public boolean load();


    /**
     * ��������
     */
    public void start() throws Exception;


    /**
     * �رշ���
     */
    public void shutdown();


    /**
     * ɾ�������ļ�����Ԫ���Ի�ʹ��
     */
    public void destroy();


    /**
     * �洢��Ϣ
     */
    public PutMessageResult putMessage(final MessageExtBrokerInner msg);


    /**
     * ��ȡ��Ϣ�����typesΪnull����������
     */
    public GetMessageResult getMessage(final String topic, final int queueId, final long offset,
            final int maxMsgNums, final SubscriptionData subscriptionData);


    /**
     * ��ȡָ���������Offset ������в����ڣ�����-1
     */
    public long getMaxOffsetInQuque(final String topic, final int queueId);


    /**
     * ��ȡָ��������СOffset ������в����ڣ�����-1
     */
    public long getMinOffsetInQuque(final String topic, final int queueId);


    /**
     * ������Ϣʱ���ȡĳ�������ж�Ӧ��offset 1�����ָ��ʱ�䣨����֮ǰ֮���ж�Ӧ����Ϣ�����ȡ�����ʱ�������offset������ѡ��֮ǰ��
     * 2�����ָ��ʱ���޶�Ӧ��Ϣ���򷵻�0
     */
    public long getOffsetInQueueByTime(final String topic, final int queueId, final long timestamp);


    /**
     * ͨ���������Offset����ѯ��Ϣ�� ������������򷵻�null
     */
    public MessageExt lookMessageByOffset(final long commitLogOffset);


    /**
     * ͨ���������Offset����ѯ��Ϣ�� ������������򷵻�null
     */
    public SelectMapedBufferResult selectOneMessageByOffset(final long commitLogOffset);


    public SelectMapedBufferResult selectOneMessageByOffset(final long commitLogOffset, final int msgSize);


    /**
     * ��ȡ����ʱͳ������
     */
    public String getRunningDataInfo();


    /**
     * ��ȡ����ʱͳ������
     */
    public HashMap<String, String> getRuntimeInfo();


    /**
     * ��ȡ����������offset
     */
    public long getMaxPhyOffset();


    /**
     * ��ȡ�������������Ϣʱ��
     */
    public long getEarliestMessageTime(final String topic, final int queueId);


    /**
     * ��ȡ�����е���Ϣ����
     */
    public long getMessageTotalInQueue(final String topic, final int queueId);


    /**
     * ���ݸ���ʹ�ã���ȡCommitLog����
     */
    public SelectMapedBufferResult getCommitLogData(final long offset);


    /**
     * ���ݸ���ʹ�ã���CommitLog׷�����ݣ����ַ�������Consume Queue
     */
    public boolean appendToCommitLog(final long startOffset, final byte[] data);


    /**
     * �ֶ�����ɾ���ļ�
     */
    public void excuteDeleteFilesManualy();


    /**
     * ������ϢKey��ѯ��Ϣ
     */
    public QueryMessageResult queryMessage(final String topic, final String key, final int maxNum,
            final long begin, final long end);


    public void updateHaMasterAddress(final String newAddr);


    public long now();
}
