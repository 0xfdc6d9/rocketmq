/**
 * 
 */
package com.alibaba.rocketmq.client.impl;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.impl.factory.MQClientFactory;
import com.alibaba.rocketmq.remoting.exception.RemotingCommandException;
import com.alibaba.rocketmq.remoting.netty.NettyRequestProcessor;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;


/**
 * Client����Broker�Ļص���������������ص���������������������ص�
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public class ClientRemotingProcessor implements NettyRequestProcessor {
    private final Logger log;
    private final MQClientFactory mqClientFactory;


    public ClientRemotingProcessor(final MQClientFactory mqClientFactory, final Logger log) {
        this.log = log;
        this.mqClientFactory = mqClientFactory;
    }


    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
            throws RemotingCommandException {
        // TODO Auto-generated method stub
        return null;
    }
}
