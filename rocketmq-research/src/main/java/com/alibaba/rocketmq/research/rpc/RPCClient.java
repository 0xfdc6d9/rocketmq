/**
 * $Id: RPCClient.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.research.rpc;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


/**
 * �ͻ��˽ӿ�
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public interface RPCClient {
    public void start();


    public void shutdown();


    public boolean connect(final InetSocketAddress remote, final int cnt);


    public ByteBuffer call(final byte[] req) throws InterruptedException;
}
