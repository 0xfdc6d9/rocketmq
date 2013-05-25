package com.alibaba.rocketmq.remoting.protocol;

import com.alibaba.fastjson.JSON;


/**
 * ���Ӷ�������л�������gson��ʵ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public abstract class RemotingSerializable {
    public String toJson() {
        return JSON.toJSONString(this);
    }


    public static <T> T fromJson(String json, Class<T> classOfT) {
        return JSON.parseObject(json, classOfT);
    }


    public byte[] encode() {
        final String json = this.toJson();
        if (json != null) {
            return json.getBytes();
        }
        return null;
    }


    public static <T> T decode(final byte[] data, Class<T> classOfT) {
        final String json = new String(data);
        return fromJson(json, classOfT);
    }
}
