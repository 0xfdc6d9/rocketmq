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
 * ������Ϣ�����ģ�ͬһ���е���Ϣͬһʱ��ֻ��һ���߳����ѣ��ɱ�֤ͬһ������Ϣ˳������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class ConsumeOrderlyContext {
    /**
     * Ҫ���ѵ���Ϣ�����ĸ�����
     */
    private final MessageQueue messageQueue;
    /**
     * ��ϢOffset�Ƿ��Զ��ύ
     */
    private boolean autoCommit = true;
    /**
     * ����ǰ���й���ʱ�䣬��λ����
     */
    private long suspendCurrentQueueTimeMillis = 1000;


    public ConsumeOrderlyContext(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }


    public boolean isAutoCommit() {
        return autoCommit;
    }


    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }


    public MessageQueue getMessageQueue() {
        return messageQueue;
    }


    public long getSuspendCurrentQueueTimeMillis() {
        return suspendCurrentQueueTimeMillis;
    }


    public void setSuspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
        this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
    }
}
