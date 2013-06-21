package com.alibaba.rocketmq.namesrv.topic;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Map;

import com.alibaba.rocketmq.common.namesrv.TopicRuntimeData;
import com.alibaba.rocketmq.common.protocol.route.QueueData;
import com.alibaba.rocketmq.namesrv.common.MergeResult;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;


/**
 * @author lansheng.zj@taobao.com
 */
public interface TopicRuntimeDataManager {

    //
    // public void getBrokerList();
    // public void registerOrderTopic();
    // public void unRegisterOrderTopic();
    // public void getOrderTopicList();

    /**
     * ��ȡtopic��·����Ϣ
     * 
     * @param topic
     */
    public RemotingCommand getRouteInfoByTopic(String topic);


    /**
     * ��ȡTopicRuntimeData��һ��snapshot
     * 
     * @return
     */
    public RemotingCommand getTopicRuntimeData();


    /**
     * �ϲ�TopicRuntimeData����
     * 
     * @param newTopicData
     * @return �ϲ��Ľ�� {@link MergeResult}
     */
    public int merge(TopicRuntimeData newTopicData);


    /**
     * ע��broker��ַ�����أ�������ɢ������namesrv���
     * 
     * @param address
     *            broker��ַ
     * @return
     */
    public RemotingCommand registerBroker(String address);


    /**
     * ע��broker��ַ������
     * 
     * @param address
     * @return
     */
    public RemotingCommand registerBrokerSingle(String address);


    /**
     * ע��������ע���broker��������ɢ�������namesrv���
     * 
     * @param brokerName
     *            ��ע���brokername
     * @return
     */
    public RemotingCommand unRegisterBroker(String brokerName);


    /**
     * ע��������ע���broker
     * 
     * @param brokerName
     *            ��ע���brokername
     * @return
     */
    public RemotingCommand unRegisterBrokerSingle(String brokerName);


    /**
     * ע��˳����Ϣ�����õ����أ�������ɢ������namesrv���
     * 
     * @param topic
     * @param orderConf
     * @return
     */
    public RemotingCommand registerOrderTopic(String topic, String orderConf);


    /**
     * ע��˳����Ϣ�����õ�����
     * 
     * @param topic
     * @param orderConf
     * @return
     */
    public RemotingCommand registerOrderTopicSingle(String topic, String orderConf);


    /**
     * �ϲ�QueueData����
     * 
     * @param queueDataMap
     * @return
     */
    public boolean mergeQueueData(Map<String, QueueData> queueDataMap);


    /**
     * �ϲ�BrokerData����
     * 
     * @param brokerName
     * @param brokerId
     * @param address
     * @return
     */
    public boolean mergeBrokerData(String brokerName, long brokerId, String address);


    /**
     * ע����ַΪaddr��broker�������
     * 
     * @param addr
     * @return
     */
    public RemotingCommand unRegisterBrokerSingleByAddr(String addr);


    /**
     * ע����ַΪaddr��broker�������
     * 
     * @param addr
     * @return
     */
    public RemotingCommand unRegisterBrokerByAddr(String addr);


    /**
     * �رգ���Դ����
     */
    public void shutdown();


    /**
     * ��ȡbroker��ַ�б�
     * 
     * @return
     */
    public ArrayList<String> getBrokerList();


    /**
     * ��ʼ��
     * 
     * @return
     */
    public boolean init();


    /**
     * add property change listener
     * 
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener);


    /**
     * remove property change listener
     * 
     * @param listener
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener);


    /**
     * fire property change
     * 
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue);

}
