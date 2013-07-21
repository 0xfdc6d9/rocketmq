/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.store;

import java.util.HashMap;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.SubscriptionData;


/**
 * �洢������ṩ�Ľӿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
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
