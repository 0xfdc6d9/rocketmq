package com.alibaba.rocketmq.namesrv.sync;

/**
 * ��������
 * 
 * @author lansheng.zj@taobao.com
 */
public abstract class TaskType {

    /**
     * ע��broker������
     */
    public static final int REG_BROKER = 1;

    /**
     * ע��topic������
     */
    public static final int REG_TOPIC = 2;

    /**
     * ע��broker������
     */
    public static final int UNREG_BROKER = 3;

}
