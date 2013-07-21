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

import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.exception.MQClientException;


/**
 * �����ߣ�������ʽ����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface MQPushConsumer extends MQConsumer {
    /**
     * �������񣬵���֮ǰȷ��registerMessageListener��subscribe���Ѿ�����<br>
     * �����Ѿ�ͨ��Springע�����������
     * 
     * @throws MQClientException
     */
    public void start() throws MQClientException;


    /**
     * �رշ���һ���رգ��˶��󽫲�����
     */
    public void shutdown();


    /**
     * ע����Ϣ��������һ��Consumerֻ����һ��������
     * 
     * @param messageListener
     */
    public void registerMessageListener(final MessageListener messageListener);


    /**
     * ������Ϣ���������Ե��ö�������Ĳ�ͬ��Topic��Ҳ�ɸ���֮ǰTopic�Ķ��Ĺ��˱��ʽ
     * 
     * @param topic
     *            ��Ϣ����
     * @param subExpression
     *            ���Ĺ��˱��ʽ�ַ�����broker���ݴ˱��ʽ���й��ˡ�Ŀǰֻ֧�ֻ�����<br>
     *            eg: "tag1 || tag2 || tag3"<br>
     *            ���subExpression����null����*�����ʾȫ������
     * @param listener
     *            ��Ϣ�ص�������
     * @throws MQClientException
     */
    public void subscribe(final String topic, final String subExpression) throws MQClientException;


    /**
     * ȡ�����ģ��ӵ�ǰ��������ע������Ϣ�ᱻ�����������������߶���
     * 
     * @param topic
     *            ��Ϣ����
     */
    public void unsubscribe(final String topic);


    /**
     * ��̬���������̳߳��߳�����
     * 
     * @param corePoolSize
     */
    public void updateCorePoolSize(int corePoolSize);


    /**
     * �����̹߳�����ͣ����
     */
    public void suspend();


    /**
     * �����ָ̻߳�����������
     */
    public void resume();
}
