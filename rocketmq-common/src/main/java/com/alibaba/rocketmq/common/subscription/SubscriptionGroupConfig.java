package com.alibaba.rocketmq.common.subscription;

import com.alibaba.rocketmq.common.MixAll;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-6-18
 */
public class SubscriptionGroupConfig {
    // ��������
    private String groupName;
    // ���ѹ����Ƿ���
    private boolean consumeEnable = true;
    // ����ʧ�ܵ���Ϣ�ŵ�һ�����Զ��У�ÿ�����������ü������Զ���
    private int retryQueueNums = 1;
    // ���ĸ�Broker��ʼ����
    private long brokerId = MixAll.MASTER_ID;


    public String getGroupName() {
        return groupName;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    public boolean isConsumeEnable() {
        return consumeEnable;
    }


    public void setConsumeEnable(boolean consumeEnable) {
        this.consumeEnable = consumeEnable;
    }


    public int getRetryQueueNums() {
        return retryQueueNums;
    }


    public void setRetryQueueNums(int retryQueueNums) {
        this.retryQueueNums = retryQueueNums;
    }


    public long getBrokerId() {
        return brokerId;
    }


    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }


    @Override
    public String toString() {
        return "SubscriptionGroupConfig [groupName=" + groupName + ", consumeEnable=" + consumeEnable
                + ", retryQueueNums=" + retryQueueNums + ", brokerId=" + brokerId + "]";
    }
}
