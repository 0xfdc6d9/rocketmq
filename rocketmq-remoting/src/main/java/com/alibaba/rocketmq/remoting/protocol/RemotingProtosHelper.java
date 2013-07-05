/**
 * $Id: RemotingProtosHelper.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.remoting.protocol;

import java.util.List;

import com.alibaba.rocketmq.remoting.protocol.RemotingProtos.StringList;
import com.google.protobuf.InvalidProtocolBufferException;


/**
 * Э�鸨����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public class RemotingProtosHelper {
    /**
     * ���л��ַ����б�
     */
    public static byte[] stringList2Bytes(final List<String> strs) {
        if (null == strs || strs.isEmpty()) {
            return null;
        }

        StringList.Builder builder = StringList.newBuilder();

        for (String str : strs) {
            builder.addName(str);
        }

        return builder.build().toByteArray();
    }


    /**
     * �����л��ַ����б�
     */
    public static List<String> bytes2StringList(final byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return null;
        }
        StringList stringList = StringList.parseFrom(data);
        return stringList.getNameList();
    }

}
