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
package com.alibaba.rocketmq.client.consumer.listener;

import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * ������Ϣ�����ģ�ͬһ���е���Ϣ�Ტ�����ѣ���Ϣ��˳����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-24
 */
public class ConsumeConcurrentlyContext {
    /**
     * Ҫ���ѵ���Ϣ�����ĸ�����
     */
    private final MessageQueue messageQueue;
    /**
     * �´���Ϣ������ʱʱ��<br>
     * -1����ʾ�����ԣ�ֱ�ӽ������Ŷ���<br>
     * 0����ʾ�ɷ������������Դ����Զ�����<br>
     * >0����ʾ�ͻ���ǿ��ָ����ʱLevel
     */
    private int delayLevelWhenNextConsume = 0;
    /**
     * �����������ѣ�ack��������Ϣ��Ĭ��ȫ��ack�������һ����Ϣ
     */
    private int ackIndex = Integer.MAX_VALUE;


    public ConsumeConcurrentlyContext(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }


    public int getDelayLevelWhenNextConsume() {
        return delayLevelWhenNextConsume;
    }


    public void setDelayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
        this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    }


    public MessageQueue getMessageQueue() {
        return messageQueue;
    }


    public int getAckIndex() {
        return ackIndex;
    }


    public void setAckIndex(int ackIndex) {
        this.ackIndex = ackIndex;
    }
}
