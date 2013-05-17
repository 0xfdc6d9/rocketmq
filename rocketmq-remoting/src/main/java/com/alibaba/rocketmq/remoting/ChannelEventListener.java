package com.alibaba.rocketmq.remoting;

import io.netty.channel.Channel;


/**
 * ����Channel���¼����������ӶϿ������ӽ���
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public interface ChannelEventListener {
    public void onChannelConnect(final String remoteAddr, final Channel channel);


    public void onChannelClose(final String remoteAddr, final Channel channel);


    public void onChannelException(final String remoteAddr, final Channel channel);
}
