/**
 * $Id: AllocateMessageQueueAveragely.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.consumer.rebalance;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.alibaba.rocketmq.common.message.MessageQueue;


/**
 * ƽ����������㷨
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public class AllocateMessageQueueAveragely implements AllocateMessageQueueStrategy {
    @Override
    public List<MessageQueue> allocate(String currentCID, List<MessageQueue> mqAll, List<String> cidAll) {
        if(currentCID==null||currentCID.length()<1){
            throw new IllegalArgumentException("currentCID is empty");
        }
        if(mqAll==null||mqAll.size()<1){
            throw new IllegalArgumentException("mqAll is null or  mqAll'size  less one");
        }
        if(cidAll==null||cidAll.size()<1){
            throw new IllegalArgumentException("cidAll is null or  cidAll'size less one");
        }

        List<MessageQueue> result = new ArrayList<MessageQueue>();
        if (!cidAll.contains(currentCID)) { // �����ڴ�ConsumerId ,ֱ�ӷ���
            return result;
        }

        int index = cidAll.indexOf(currentCID);
        int averageSize = mqAll.size() / cidAll.size();
        int mod = mqAll.size() % cidAll.size();
        int startIndex = index * averageSize;
        int endIndex = (index + 1) * averageSize;
        for (int i = startIndex; i < endIndex; i++) {
            result.add(mqAll.get(i));
        }

        // �����ǰ��consumerId���һ���һ���ʣ�µĶ��У�Ӧ�ð������ж��ŵ���ǰconsumerId������
        boolean isAddRemainQueue = (index == cidAll.size()-1)&&mod > 0;
        if (isAddRemainQueue) {
            int messageQueueSize = mqAll.size();
            for (int i = endIndex; i < messageQueueSize; i++) {
                result.add(mqAll.get(i));
            }
        }
        return result;
    }
}
