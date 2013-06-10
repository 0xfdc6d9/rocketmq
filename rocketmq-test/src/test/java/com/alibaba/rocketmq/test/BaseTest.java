package com.alibaba.rocketmq.test;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.alibaba.rocketmq.broker.BrokerController;
import com.alibaba.rocketmq.common.BrokerConfig;
import com.alibaba.rocketmq.common.MQVersion;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.remoting.netty.NettyServerConfig;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;
import com.alibaba.rocketmq.store.config.MessageStoreConfig;


public abstract class BaseTest extends TestCase {
    public static BrokerController brokerController;


    @BeforeClass
    // ���Է���֮ǰִ��
    public void testInit() throws Exception {
        System.out.println("before-----");
        // ���õ�ǰ����汾�ţ�ÿ�η����汾ʱ����Ҫ�޸�CurrentVersion
        System.setProperty(RemotingCommand.RemotingVersionKey, Integer.toString(MQVersion.CurrentVersion));

        try {

            // ��ʼ�������ļ�
            final BrokerConfig brokerConfig = new BrokerConfig();
            final NettyServerConfig nettyServerConfig = new NettyServerConfig();
            nettyServerConfig.setListenPort(10911);
            final MessageStoreConfig messageStoreConfig = new MessageStoreConfig();
            // ������
            // deleteDir(System.getProperty("user.home") + File.separator +
            // "store");

            if (null == brokerConfig.getRocketmqHome()) {
                System.out.println("Please set the " + MixAll.ROCKETMQ_HOME_ENV
                        + " variable in your environment to match the location of the RocketMQ installation");
                System.exit(-2);
            }

            // BrokerId�Ĵ���
            switch (messageStoreConfig.getBrokerRole()) {
            case ASYNC_MASTER:
            case SYNC_MASTER:
                // Master Id������0
                brokerConfig.setBrokerId(MixAll.MASTER_ID);
                break;
            case SLAVE:
                // Slave Id��Slave����IP���˿ھ���
                long id = MixAll.createBrokerId(brokerConfig.getBrokerIP1(), nettyServerConfig.getListenPort());
                brokerConfig.setBrokerId(id);
                break;
            default:
                break;
            }

            // Master����Slave����Ķ˿ڣ�Ĭ��Ϊ����˿�+1
            messageStoreConfig.setHaListenPort(nettyServerConfig.getListenPort() + 1);

            // ��ʼ��Logback
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure(brokerConfig.getRocketmqHome() + "/conf/log4j_broker.xml");
            final Logger log = LoggerFactory.getLogger(LoggerName.BrokerLoggerName);

            // ��ӡ��������
            MixAll.printObjectProperties(log, brokerConfig);
            MixAll.printObjectProperties(log, nettyServerConfig);
            MixAll.printObjectProperties(log, messageStoreConfig);

            // ��ʼ��������ƶ���
            brokerController = new BrokerController(brokerConfig, nettyServerConfig, messageStoreConfig);
            boolean initResult = brokerController.initialize();
            if (!initResult) {
                brokerController.shutdown();
                System.exit(-3);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                private volatile boolean hasShutdown = false;
                private AtomicInteger shutdownTimes = new AtomicInteger(0);


                @Override
                public void run() {
                    synchronized (this) {
                        log.info("shutdown hook was invoked, " + this.shutdownTimes.incrementAndGet());
                        if (!this.hasShutdown) {
                            this.hasShutdown = true;
                            long begineTime = System.currentTimeMillis();
                            brokerController.shutdown();
                            long consumingTimeTotal = System.currentTimeMillis() - begineTime;
                            log.info("shutdown hook over, consuming time total(ms): " + consumingTimeTotal);
                        }
                    }
                }
            }, "ShutdownHook"));

            // ����������ƶ���
            brokerController.start();
            String tip =
                    "The broker[" + brokerController.getBrokerConfig().getBrokerName() + ", "
                            + brokerController.getBrokerAddr() + "] boot success.";
            log.info(tip);
            System.out.println(tip);
        }
        catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    @AfterClass
    // ���Է���֮��ִ��
    public void testDown() throws Exception {
        System.out.println("after------");
        brokerController.shutdown();
    }


    public static void deleteDir(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File subFile : files) {
                    if (subFile.isDirectory())
                        deleteDir(subFile.getPath());
                    else
                        subFile.delete();
                }
            }
            file.delete();
        }
    }
}
