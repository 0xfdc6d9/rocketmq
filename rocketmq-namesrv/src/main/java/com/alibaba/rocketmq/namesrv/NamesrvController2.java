package com.alibaba.rocketmq.namesrv;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.namesrv.NamesrvConfig;
import com.alibaba.rocketmq.namesrv.kvconfig.KVConfigManager;
import com.alibaba.rocketmq.namesrv.processor.DefaultRequestProcessor;
import com.alibaba.rocketmq.remoting.RemotingServer;
import com.alibaba.rocketmq.remoting.netty.NettyRemotingServer;
import com.alibaba.rocketmq.remoting.netty.NettyServerConfig;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @author lansheng.zj@taobao.com
 * @since 2013-7-1
 */
public class NamesrvController2 {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NamesrvLoggerName);
    // Name Server����
    private final NamesrvConfig namesrvConfig;
    // ͨ�Ų�����
    private final NettyServerConfig nettyServerConfig;
    // �����ͨ�Ų����
    private RemotingServer remotingServer;
    // ����������������̳߳�
    private ExecutorService remotingExecutor;

    /**
     * �������ݽṹ
     */
    private final KVConfigManager kvConfigManager;


    public NamesrvController2(NamesrvConfig namesrvConfig, NettyServerConfig nettyServerConfig) {
        this.namesrvConfig = namesrvConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.kvConfigManager = new KVConfigManager(this);
    }


    public boolean initialize() {
        // ��ӡ���������ò���
        MixAll.printObjectProperties(log, this.namesrvConfig);

        // ����KV����
        this.kvConfigManager.load();

        // ��ʼ��ͨ�Ų�
        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig);

        // ��ʼ���̳߳�
        this.remotingExecutor =
                Executors.newFixedThreadPool(nettyServerConfig.getServerWorkerThreads(), new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);


                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "RemotingExecutorThread_" + threadIndex.incrementAndGet());
                    }
                });

        this.registerProcessor();

        return true;
    }


    private void registerProcessor() {
        this.remotingServer
            .registerDefaultProcessor(new DefaultRequestProcessor(this), this.remotingExecutor);
    }


    public void start() throws Exception {
        this.remotingServer.start();
    }


    public void shutdown() {
        this.remotingServer.shutdown();
    }


    public NamesrvConfig getNamesrvConfig() {
        return namesrvConfig;
    }


    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }


    public KVConfigManager getKvConfigManager() {
        return kvConfigManager;
    }
}
