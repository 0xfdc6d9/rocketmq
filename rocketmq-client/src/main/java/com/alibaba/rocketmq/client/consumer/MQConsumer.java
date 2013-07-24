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
package com.alibaba.rocketmq.client.consumer;

import java.util.Set;

import com.alibaba.rocketmq.client.MQAdmin;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;


/**
 * Consumer�ӿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-24
 */
public interface MQConsumer extends MQAdmin {
    /**
     * Consumer����ʧ�ܵ���Ϣ����ѡ�����·��ص��������ˣ�����ʱ����<br>
     * �����ȳ��Խ���Ϣ���ص���Ϣ֮ǰ�洢����������ʱֻ������ϢOffset����Ϣ�岻���ͣ�����ռ���������<br>
     * �������ʧ�ܣ����Զ����Է���������������ʱ��Ϣ��Ҳ�ᴫ��<br>
     * �ش���ȥ����Ϣֻ�ᱻ��ǰConsumer Group���ѡ�
     * 
     * @param msg
     * @param delayLevel
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws RemotingException
     * @throws MQClientException
     */
    public void sendMessageBack(final MessageExt msg, final int delayLevel) throws RemotingException,
            MQBrokerException, InterruptedException, MQClientException;


    /**
     * ����topic��ȡ��Ӧ��MessageQueue���ǿɱ����ĵĶ���<br>
     * P.S ��Consumer Cache�������ݣ�����Ƶ�����á�Cache�����ݴ�Լ30�����һ��
     * 
     * @param topic
     *            ��ϢTopic
     * @return ���ض��м���
     * @throws MQClientException
     */
    public Set<MessageQueue> fetchSubscribeMessageQueues(final String topic) throws MQClientException;
}
