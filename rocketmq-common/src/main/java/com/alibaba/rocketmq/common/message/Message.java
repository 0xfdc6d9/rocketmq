/**
 * $Id: Message.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.common.message;

import java.util.HashMap;
import java.util.Map;


/**
 * ��Ϣ��Producer��Consumerʹ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class Message {
    /**
     * ��Ϣ����
     */
    private String topic;
    /**
     * ��Ϣ��־��ϵͳ������Ԥ����ȫ��Ӧ�þ������ʹ��
     */
    private int flag;
    /**
     * ��Ϣ���ԣ�ϵͳ�б������ԣ�Ӧ��Ҳ�����Զ�������
     */
    private Map<String, String> properties;
    /**
     * ��Ϣ��
     */
    private byte[] body;

    /**
     * ��Ϣ�ؼ��ʣ����Key��KEY_SEPARATOR��������ѯ��Ϣʹ�ã�
     */
    public static final String PROPERTY_KEYS = "KEYS";
    /**
     * ��Ϣ��ǩ��ֻ֧������һ��Tag���������Ϣ����ʹ�ã�
     */
    public static final String PROPERTY_TAGS = "TAGS";
    /**
     * �Ƿ�ȴ�����������Ϣ�洢����ٷ��أ������ǵȴ�ˢ����ɻ��ߵȴ�ͬ�����Ƶ�������������
     */
    public static final String PROPERTY_WAIT_STORE_MSG_OK = "WAIT";
    /**
     * ��Ϣ��ʱͶ��ʱ�伶��0��ʾ����ʱ������0��ʾ�ض���ʱ���𣨾��弶���ڷ������˶��壩
     */
    public static final String PROPERTY_DELAY_TIME_LEVEL = "DELAY";

    /**
     * �ڲ�ʹ��
     */
    public static final String PROPERTY_REAL_TOPIC = "REAL_TOPIC";
    public static final String PROPERTY_REAL_QUEUE_ID = "REAL_QID";
    public static final String PROPERTY_TRANSACTION_PREPARED = "TRAN_MSG";
    public static final String PROPERTY_PRODUCER_GROUP = "PGROUP";

    public static final String KEY_SEPARATOR = " ";


    public Message() {
    }


    public Message(String topic, byte[] body) {
        this(topic, "", "", 0, body, true);
    }


    public Message(String topic, String tags, byte[] body) {
        this(topic, tags, "", 0, body, true);
    }


    public Message(String topic, String tags, String keys, byte[] body) {
        this(topic, tags, keys, 0, body, true);
    }


    public Message(String topic, String tags, String keys, int flag, byte[] body, boolean waitStoreMsgOK) {
        this.topic = topic;
        this.flag = flag;
        this.body = body;

        if (tags != null && tags.length() > 0)
            this.setTags(tags);

        if (keys != null && keys.length() > 0)
            this.setKeys(keys);

        this.setWaitStoreMsgOK(waitStoreMsgOK);
    }


    public void clearProperty(final String name) {
        if (null != this.properties) {
            this.properties.remove(name);
        }
    }


    public void putProperty(final String name, final String value) {
        if (null == this.properties) {
            this.properties = new HashMap<String, String>();
        }

        this.properties.put(name, value);
    }


    public String getProperty(final String name) {
        if (null == this.properties) {
            this.properties = new HashMap<String, String>();
        }

        return this.properties.get(name);
    }


    public String getTopic() {
        return topic;
    }


    public void setTopic(String topic) {
        this.topic = topic;
    }


    public String getTags() {
        return this.getProperty(PROPERTY_TAGS);
    }


    public void setTags(String tags) {
        this.putProperty(PROPERTY_TAGS, tags);
    }


    public String getKeys() {
        return this.getProperty(PROPERTY_KEYS);
    }


    public void setKeys(String keys) {
        this.putProperty(PROPERTY_KEYS, keys);
    }


    public int getDelayTimeLevel() {
        String t = this.getProperty(PROPERTY_DELAY_TIME_LEVEL);
        if (t != null) {
            return Integer.parseInt(t);
        }

        return 0;
    }


    public void setDelayTimeLevel(int level) {
        this.putProperty(PROPERTY_DELAY_TIME_LEVEL, String.valueOf(level));
    }


    public boolean isWaitStoreMsgOK() {
        String result = this.getProperty(PROPERTY_WAIT_STORE_MSG_OK);
        if (null == result)
            return true;

        return Boolean.parseBoolean(result);
    }


    public void setWaitStoreMsgOK(boolean waitStoreMsgOK) {
        this.putProperty(PROPERTY_WAIT_STORE_MSG_OK, Boolean.toString(waitStoreMsgOK));
    }


    public int getFlag() {
        return flag;
    }


    public void setFlag(int flag) {
        this.flag = flag;
    }


    public byte[] getBody() {
        return body;
    }


    public void setBody(byte[] body) {
        this.body = body;
    }


    public Map<String, String> getProperties() {
        return properties;
    }


    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }


    @Override
    public String toString() {
        return "Message [topic=" + topic + ", flag=" + flag + ", properties=" + properties + ", body="
                + (body != null ? body.length : 0) + "]";
    }
}
