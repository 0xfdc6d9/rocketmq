/**
 * $Id: TopicRouteData.java 1835 2013-05-16 02:00:50Z shijia.wxr $
 */
package com.alibaba.rocketmq.common.protocol.route;

import java.util.List;

import com.alibaba.rocketmq.common.protocol.MetaProtos.TopicRouteInfo;
import com.google.protobuf.InvalidProtocolBufferException;


/**
 * Topic·�����ݣ���Name Server��ȡ
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public class TopicRouteData {
    public static final String SEP = ",";
    private List<QueueData> queueDatas;
    private List<BrokerData> brokerDatas;

    private String orderTopicConf;


    public byte[] encode() {
        TopicRouteInfo topicRouteInfo = ObjectConverter.topicRouteData2TopicRouteInfo(this);
        return topicRouteInfo.toByteArray();
    }


    public static TopicRouteData decode(byte[] data) throws InvalidProtocolBufferException {
        TopicRouteInfo topicRouteInfo = TopicRouteInfo.parseFrom(data);
        return ObjectConverter.topicRouteInfo2TopicRouteData(topicRouteInfo);
    }


    public List<QueueData> getQueueDatas() {
        return queueDatas;
    }


    public void setQueueDatas(List<QueueData> queueDatas) {
        this.queueDatas = queueDatas;
    }


    public List<BrokerData> getBrokerDatas() {
        return brokerDatas;
    }


    public void setBrokerDatas(List<BrokerData> brokerDatas) {
        this.brokerDatas = brokerDatas;
    }


    public String getOrderTopicConf() {
        return orderTopicConf;
    }


    public void setOrderTopicConf(String orderTopicConf) {
        this.orderTopicConf = orderTopicConf;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brokerDatas == null) ? 0 : brokerDatas.hashCode());
        result = prime * result + ((orderTopicConf == null) ? 0 : orderTopicConf.hashCode());
        result = prime * result + ((queueDatas == null) ? 0 : queueDatas.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TopicRouteData other = (TopicRouteData) obj;
        if (brokerDatas == null) {
            if (other.brokerDatas != null)
                return false;
        }
        else if (!brokerDatas.equals(other.brokerDatas))
            return false;
        if (orderTopicConf == null) {
            if (other.orderTopicConf != null)
                return false;
        }
        else if (!orderTopicConf.equals(other.orderTopicConf))
            return false;
        if (queueDatas == null) {
            if (other.queueDatas != null)
                return false;
        }
        else if (!queueDatas.equals(other.queueDatas))
            return false;
        return true;
    }
}
