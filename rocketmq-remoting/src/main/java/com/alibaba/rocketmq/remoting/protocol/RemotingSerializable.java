package com.alibaba.rocketmq.remoting.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * ���Ӷ�������л�������json��ʵ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public abstract class RemotingSerializable {
    protected final static GsonBuilder builder = new GsonBuilder();
    protected final static Gson gson = builder.create();


    public String toJson() {
        return gson.toJson(this);
    }


    public static String toJson(final Object obj) {
        return gson.toJson(obj);
    }


    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }


    public byte[] encode() {
        final String json = this.toJson();
        if (json != null) {
            return json.getBytes();
        }
        return null;
    }


    public static byte[] encode(final Object obj) {
        final String json = toJson(obj);
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
