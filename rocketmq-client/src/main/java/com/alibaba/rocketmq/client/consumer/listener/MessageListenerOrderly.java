/**
 * $Id: MessageListenerOrderly.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.listener;

import java.util.List;

import com.alibaba.rocketmq.common.message.MessageExt;


/**
 * ͬһ���е���Ϣͬһʱ��ֻ��һ���߳����ѣ��ɱ�֤��Ϣ��ͬһ�����ϸ���������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public interface MessageListenerOrderly extends MessageListener {
    /**
     * �����׳��쳣��ͬ�ڷ��� ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT
     * 
     * @param msgs
     *            msgs.size() >= 1
     * @param context
     * @return
     */
    public ConsumeOrderlyStatus consumeMessage(final List<MessageExt> msgs,
            final ConsumeOrderlyContext context);
}
