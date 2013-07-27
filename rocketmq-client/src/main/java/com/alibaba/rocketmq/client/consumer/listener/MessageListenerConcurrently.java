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

import java.util.List;

import com.alibaba.rocketmq.common.message.MessageExt;


/**
 * ͬһ���е���Ϣ��������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-24
 */
public interface MessageListenerConcurrently extends MessageListener {
    /**
     * �����׳��쳣��ͬ�ڷ��� ConsumeConcurrentlyStatus.RECONSUME_LATER<br>
     * P.S: ����Ӧ�ò�Ҫ�׳��쳣
     * 
     * @param msgs
     *            msgs.size() >= 1<br>
     *            DefaultMQPushConsumer.consumeMessageBatchMaxSize=1��Ĭ����Ϣ��Ϊ1
     * @param context
     * @return
     */
    public ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs,
            final ConsumeConcurrentlyContext context);
}
