/**
 * $Id: SendCallback.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.producer;

/**
 * �첽������Ϣ�ص��ӿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public interface SendCallback {
    public void onSuccess(final SendResult sendResult);


    public void onException(final Throwable e);
}
