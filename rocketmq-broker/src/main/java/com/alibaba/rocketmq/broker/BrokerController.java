/**
 * $Id: BrokerController.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.broker;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.broker.client.ClientHousekeepingService;
import com.alibaba.rocketmq.broker.client.ConsumerManager;
import com.alibaba.rocketmq.broker.client.ProducerManager;
import com.alibaba.rocketmq.broker.longpolling.PullRequestHoldService;
import com.alibaba.rocketmq.broker.offset.ConsumerOffsetManager;
import com.alibaba.rocketmq.broker.processor.AdminBrokerProcessor;
import com.alibaba.rocketmq.broker.processor.ClientManageProcessor;
import com.alibaba.rocketmq.broker.processor.EndTransactionProcessor;
import com.alibaba.rocketmq.broker.processor.PullMessageProcessor;
import com.alibaba.rocketmq.broker.processor.QueryMessageProcessor;
import com.alibaba.rocketmq.broker.processor.SendMessageProcessor;
import com.alibaba.rocketmq.broker.topic.TopicConfigManager;
import com.alibaba.rocketmq.broker.transaction.DefaultTransactionCheckExecuter;
import com.alibaba.rocketmq.common.BrokerConfig;
import com.alibaba.rocketmq.common.DataVersion;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.alibaba.rocketmq.common.protocol.MQProtos;
import com.alibaba.rocketmq.common.protocol.MQProtosHelper;
import com.alibaba.rocketmq.remoting.RemotingServer;
import com.alibaba.rocketmq.remoting.netty.NettyRemotingServer;
import com.alibaba.rocketmq.remoting.netty.NettyRequestProcessor;
import com.alibaba.rocketmq.remoting.netty.NettyServerConfig;
import com.alibaba.rocketmq.store.DefaultMessageStore;
import com.alibaba.rocketmq.store.MessageStore;
import com.alibaba.rocketmq.store.config.BrokerRole;
import com.alibaba.rocketmq.store.config.MessageStoreConfig;


/**
 * Broker�������������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class BrokerController {
    private static final Logger log = LoggerFactory.getLogger(MixAll.BrokerLoggerName);
    // ����������
    private final BrokerConfig brokerConfig;
    // ͨ�Ų�����
    private final NettyServerConfig nettyServerConfig;
    // �洢������
    private final MessageStoreConfig messageStoreConfig;
    // �����ļ��汾��
    private final DataVersion configDataVersion = new DataVersion();

    // �洢�����
    private MessageStore messageStore;
    // ͨ�Ų����
    private RemotingServer remotingServer;

    // ���ѽ��ȴ洢
    private final ConsumerOffsetManager consumerOffsetManager;
    // Consumer���ӡ����Ĺ�ϵ����
    private final ConsumerManager consumerManager;
    // Producer���ӹ���
    private final ProducerManager producerManager;
    // ������пͻ�������
    private final ClientHousekeepingService clientHousekeepingService;
    // Broker�����ز�Producer����״̬
    private final DefaultTransactionCheckExecuter defaultTransactionCheckExecuter;

    // Topic����
    private TopicConfigManager topicConfigManager;
    // ��������Ϣ�̳߳�
    private ExecutorService sendMessageExecutor;
    // ������ȡ��Ϣ�̳߳�
    private ExecutorService pullMessageExecutor;
    // �������Broker�̳߳�
    private ExecutorService adminBrokerExecutor;

    private final PullMessageProcessor pullMessageProcessor;
    private final PullRequestHoldService pullRequestHoldService;

    private ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "BrokerControllerScheduledThread");
            }
        });


    public BrokerController(final BrokerConfig brokerConfig, final NettyServerConfig nettyServerConfig,
            final MessageStoreConfig messageStoreConfig) {
        this.brokerConfig = brokerConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.messageStoreConfig = messageStoreConfig;
        this.consumerOffsetManager = new ConsumerOffsetManager(this);
        this.topicConfigManager = new TopicConfigManager(this);
        this.pullMessageProcessor = new PullMessageProcessor(this);
        this.pullRequestHoldService = new PullRequestHoldService(this);
        this.consumerManager = new ConsumerManager();
        this.producerManager = new ProducerManager();
        this.clientHousekeepingService = new ClientHousekeepingService(this);
        this.defaultTransactionCheckExecuter = new DefaultTransactionCheckExecuter(this);
    }


    public String getBrokerAddr() {
        String addr = this.brokerConfig.getBrokerIP1() + ":" + this.nettyServerConfig.getListenPort();
        return addr;
    }


    private boolean registerToNameServer() {
        TopAddressing topAddressing = new TopAddressing();
        String addrs =
                (null == this.brokerConfig.getNamesrvAddr()) ? topAddressing.fetchNSAddr() : this.brokerConfig
                    .getNamesrvAddr();
        if (addrs != null) {
            String[] addrArray = addrs.split(";");

            if (addrArray != null && addrArray.length > 0) {
                Random r = new Random();
                int begin = Math.abs(r.nextInt()) % 1000000;
                for (int i = 0; i < addrArray.length; i++) {
                    String addr = addrArray[begin++ % addrArray.length];
                    boolean result =
                            MQProtosHelper.registerBrokerToNameServer(addr, this.getBrokerAddr(), 1000 * 10);
                    final String info =
                            "register broker[" + this.getBrokerAddr() + "] to name server[" + addr + "] "
                                    + (result ? " success" : " failed");
                    log.info(info);
                    System.out.println(info);
                    if (result)
                        return true;
                }
            }
        }

        return false;
    }


    public boolean initialize() {
        boolean result = true;

        // ��ӡ���������ò���
        MixAll.printObjectProperties(log, this.brokerConfig);
        MixAll.printObjectProperties(log, this.nettyServerConfig);
        MixAll.printObjectProperties(log, this.messageStoreConfig);

        // ע�ᵽName Server
        // result = result && this.registerToNameServer();
//        registerToNameServer();
        // ����Topic����
        result = result && this.topicConfigManager.load();

        // ����Consumer Offset
        result = result && this.consumerOffsetManager.load();

        // ��ʼ���洢��
        if (result) {
            try {
                this.messageStore =
                        new DefaultMessageStore(this.messageStoreConfig, this.defaultTransactionCheckExecuter);
            }
            catch (IOException e) {
                result = false;
                e.printStackTrace();
            }
        }

        // ���ر�����Ϣ����
        result = result && this.messageStore.load();

        if (result) {
            // ��ʼ��ͨ�Ų�
            this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, this.clientHousekeepingService);

            // ��ʼ���̳߳�
            this.sendMessageExecutor =
                    Executors.newFixedThreadPool(this.brokerConfig.getSendMessageThreadPoolNums(),
                        new ThreadFactory() {

                            private AtomicInteger threadIndex = new AtomicInteger(0);


                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r, "SendMessageThread_" + this.threadIndex.incrementAndGet());
                            }
                        });

            this.pullMessageExecutor =
                    Executors.newFixedThreadPool(this.brokerConfig.getPullMessageThreadPoolNums(),
                        new ThreadFactory() {

                            private AtomicInteger threadIndex = new AtomicInteger(0);


                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r, "PullMessageThread_" + this.threadIndex.incrementAndGet());
                            }
                        });

            this.adminBrokerExecutor =
                    Executors.newFixedThreadPool(this.brokerConfig.getAdminBrokerThreadPoolNums(),
                        new ThreadFactory() {

                            private AtomicInteger threadIndex = new AtomicInteger(0);


                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r, "AdminBrokerThread_" + this.threadIndex.incrementAndGet());
                            }
                        });

            this.registerProcessor();

            // ��ʱˢ���ѽ���
            this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        BrokerController.this.consumerOffsetManager.flush();
                    }
                    catch (Exception e) {
                        log.error("", e);
                    }
                }
            }, 1000 * 10, this.brokerConfig.getFlushConsumerOffsetInterval(), TimeUnit.MILLISECONDS);

            // ��ʱˢ���ѽ��ȣ���ʷ��¼���������ɱ���
            this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        BrokerController.this.consumerOffsetManager.flushHistory();

                        BrokerController.this.consumerOffsetManager.recordPullTPS();
                    }
                    catch (Exception e) {
                        log.error("", e);
                    }
                }
            }, 1000 * 10, this.brokerConfig.getFlushConsumerOffsetHistoryInterval(), TimeUnit.MILLISECONDS);

            // �����slave
            if (BrokerRole.SLAVE == this.messageStoreConfig.getBrokerRole()) {
                if (this.messageStoreConfig.getMasterAddress() != null
                        && this.messageStoreConfig.getMasterAddress().length() >= 6) {
                    this.messageStore.updateMasterAddress(this.messageStoreConfig.getMasterAddress());
                }
                else {
                    // TODO ��ʱȥ���µ�ַ
                }
            }
        }

        return result;
    }


    public void registerProcessor() {
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.SEND_MESSAGE_VALUE, new SendMessageProcessor(
            this), this.sendMessageExecutor);

        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.PULL_MESSAGE_VALUE,
            this.pullMessageProcessor, this.pullMessageExecutor);

        NettyRequestProcessor queryProcessor = new QueryMessageProcessor(this);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.QUERY_MESSAGE_VALUE, queryProcessor,
            this.pullMessageExecutor);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.VIEW_MESSAGE_BY_ID_VALUE, queryProcessor,
            this.pullMessageExecutor);

        NettyRequestProcessor clientProcessor = new ClientManageProcessor(this);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.HEART_BEAT_VALUE, clientProcessor,
            this.adminBrokerExecutor);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.UNREGISTER_CLIENT_VALUE, clientProcessor,
            this.adminBrokerExecutor);

        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.END_TRANSACTION_VALUE,
            new EndTransactionProcessor(this), this.sendMessageExecutor);

        this.remotingServer.registerDefaultProcessor(new AdminBrokerProcessor(this), this.adminBrokerExecutor);
    }


    public void start() throws Exception {
        if (this.messageStore != null) {
            this.messageStore.start();
        }

        if (this.remotingServer != null) {
            this.remotingServer.start();
        }

        if (this.pullRequestHoldService != null) {
            this.pullRequestHoldService.start();
        }

        if (this.clientHousekeepingService != null) {
            this.clientHousekeepingService.start();
        }
    }


    public void shutdown() {
        if (this.clientHousekeepingService != null) {
            this.clientHousekeepingService.shutdown();
        }

        if (this.pullRequestHoldService != null) {
            this.pullRequestHoldService.shutdown();
        }

        if (this.remotingServer != null) {
            this.remotingServer.shutdown();
        }

        if (this.messageStore != null) {
            this.messageStore.shutdown();
        }

        this.scheduledExecutorService.shutdown();

        if (this.sendMessageExecutor != null) {
            this.sendMessageExecutor.shutdown();
        }

        if (this.pullMessageExecutor != null) {
            this.pullMessageExecutor.shutdown();
        }

        if (this.adminBrokerExecutor != null) {
            this.adminBrokerExecutor.shutdown();
        }

        this.consumerOffsetManager.flush();
    }


    public MessageStore getMessageStore() {
        return messageStore;
    }


    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }


    public RemotingServer getRemotingServer() {
        return remotingServer;
    }


    public void setRemotingServer(RemotingServer remotingServer) {
        this.remotingServer = remotingServer;
    }


    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }


    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }


    public MessageStoreConfig getMessageStoreConfig() {
        return messageStoreConfig;
    }


    public ConsumerOffsetManager getConsumerOffsetManager() {
        return consumerOffsetManager;
    }


    public TopicConfigManager getTopicConfigManager() {
        return topicConfigManager;
    }


    public void setTopicConfigManager(TopicConfigManager topicConfigManager) {
        this.topicConfigManager = topicConfigManager;
    }


    public void updateAllConfig(Properties properties) {
        MixAll.properties2Object(properties, brokerConfig);
        MixAll.properties2Object(properties, nettyServerConfig);
        MixAll.properties2Object(properties, messageStoreConfig);
        this.configDataVersion.nextVersion();
        this.flushAllConfig();
    }


    private void flushAllConfig() {
        String allConfig = this.encodeAllConfig();
        boolean result = MixAll.string2File(allConfig, this.brokerConfig.getConfigFilePath());
        log.info("flush topic config, " + this.brokerConfig.getConfigFilePath() + (result ? " OK" : " Failed"));
    }


    public String encodeAllConfig() {
        StringBuilder sb = new StringBuilder();
        {
            Properties properties = MixAll.object2Properties(this.brokerConfig);
            if (properties != null) {
                sb.append(MixAll.properties2String(properties));
            }
            else {
                log.error("encodeAllConfig object2Properties error");
            }
        }

        {
            Properties properties = MixAll.object2Properties(this.messageStoreConfig);
            if (properties != null) {
                sb.append(MixAll.properties2String(properties));
            }
            else {
                log.error("encodeAllConfig object2Properties error");
            }
        }

        {
            Properties properties = MixAll.object2Properties(this.nettyServerConfig);
            if (properties != null) {
                sb.append(MixAll.properties2String(properties));
            }
            else {
                log.error("encodeAllConfig object2Properties error");
            }
        }

        return sb.toString();
    }


    public String getConfigDataVersion() {
        return this.configDataVersion.currentVersion();
    }


    public PullMessageProcessor getPullMessageProcessor() {
        return pullMessageProcessor;
    }


    public PullRequestHoldService getPullRequestHoldService() {
        return pullRequestHoldService;
    }


    public ConsumerManager getConsumerManager() {
        return consumerManager;
    }


    public ProducerManager getProducerManager() {
        return producerManager;
    }


    public DefaultTransactionCheckExecuter getDefaultTransactionCheckExecuter() {
        return defaultTransactionCheckExecuter;
    }
}
