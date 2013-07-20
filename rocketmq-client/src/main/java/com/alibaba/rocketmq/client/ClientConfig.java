/**
 * $Id: MQClientConfig.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client;

import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;


/**
 * Producer��Consumer�Ĺ�������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class ClientConfig {
    private String namesrvAddr = System.getProperty(MixAll.NAMESRV_ADDR_PROPERTY,
        System.getenv(MixAll.NAMESRV_ADDR_ENV));
    private String clientIP = RemotingUtil.getLocalAddress();
    private String instanceName = System.getProperty("rocketmq.client.name", "DEFAULT");
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    private int pollNameServerInteval = 1000 * 30;
    private int heartbeatBrokerInterval = 1000 * 30;
    private int persistConsumerOffsetInterval = 1000 * 5;


    public String buildMQClientId() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClientIP());

        sb.append("@");
        sb.append(this.getInstanceName());

        return sb.toString();
    }


    public void resetClientConfig(final ClientConfig cc) {
        this.namesrvAddr = cc.namesrvAddr;
        this.clientIP = cc.clientIP;
        this.instanceName = cc.instanceName;
        this.clientCallbackExecutorThreads = cc.clientCallbackExecutorThreads;
        this.pollNameServerInteval = cc.pollNameServerInteval;
        this.heartbeatBrokerInterval = cc.heartbeatBrokerInterval;
        this.persistConsumerOffsetInterval = cc.persistConsumerOffsetInterval;
    }


    public ClientConfig cloneClientConfig() {
        ClientConfig cc = new ClientConfig();
        cc.namesrvAddr = namesrvAddr;
        cc.clientIP = clientIP;
        cc.instanceName = instanceName;
        cc.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
        cc.pollNameServerInteval = pollNameServerInteval;
        cc.heartbeatBrokerInterval = heartbeatBrokerInterval;
        cc.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
        return cc;
    }


    public String getNamesrvAddr() {
        return namesrvAddr;
    }


    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }


    public String getClientIP() {
        return clientIP;
    }


    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }


    public String getInstanceName() {
        return instanceName;
    }


    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }


    public int getClientCallbackExecutorThreads() {
        return clientCallbackExecutorThreads;
    }


    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }


    public int getPollNameServerInteval() {
        return pollNameServerInteval;
    }


    public void setPollNameServerInteval(int pollNameServerInteval) {
        this.pollNameServerInteval = pollNameServerInteval;
    }


    public int getHeartbeatBrokerInterval() {
        return heartbeatBrokerInterval;
    }


    public void setHeartbeatBrokerInterval(int heartbeatBrokerInterval) {
        this.heartbeatBrokerInterval = heartbeatBrokerInterval;
    }


    public int getPersistConsumerOffsetInterval() {
        return persistConsumerOffsetInterval;
    }


    public void setPersistConsumerOffsetInterval(int persistConsumerOffsetInterval) {
        this.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
    }


    @Override
    public String toString() {
        return "ClientConfig [namesrvAddr=" + namesrvAddr + ", clientIP=" + clientIP + ", instanceName="
                + instanceName + ", clientCallbackExecutorThreads=" + clientCallbackExecutorThreads
                + ", pollNameServerInteval=" + pollNameServerInteval + ", heartbeatBrokerInterval="
                + heartbeatBrokerInterval + ", persistConsumerOffsetInterval="
                + persistConsumerOffsetInterval + "]";
    }
}
