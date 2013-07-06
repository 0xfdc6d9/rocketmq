/**
 * $Id: BrokerController.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.broker;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.broker.client.ClientHousekeepingService;
import com.alibaba.rocketmq.broker.client.ConsumerIdsChangeListener;
import com.alibaba.rocketmq.broker.client.ConsumerManager;
import com.alibaba.rocketmq.broker.client.DefaultConsumerIdsChangeListener;
import com.alibaba.rocketmq.broker.client.ProducerManager;
import com.alibaba.rocketmq.broker.client.net.Broker2Client;
import com.alibaba.rocketmq.broker.client.rebalance.RebalanceLockManager;
import com.alibaba.rocketmq.broker.digestlog.DigestLogManager;
import com.alibaba.rocketmq.broker.longpolling.PullRequestHoldService;
import com.alibaba.rocketmq.broker.offset.ConsumerOffsetManager;
import com.alibaba.rocketmq.broker.out.BrokerOuterAPI;
import com.alibaba.rocketmq.broker.processor.AdminBrokerProcessor;
import com.alibaba.rocketmq.broker.processor.ClientManageProcessor;
import com.alibaba.rocketmq.broker.processor.EndTransactionProcessor;
import com.alibaba.rocketmq.broker.processor.PullMessageProcessor;
import com.alibaba.rocketmq.broker.processor.QueryMessageProcessor;
import com.alibaba.rocketmq.broker.processor.SendMessageProcessor;
import com.alibaba.rocketmq.broker.subscription.SubscriptionGroupManager;
import com.alibaba.rocketmq.broker.topic.TopicConfigManager;
import com.alibaba.rocketmq.broker.transaction.DefaultTransactionCheckExecuter;
import com.alibaba.rocketmq.common.BrokerConfig;
import com.alibaba.rocketmq.common.DataVersion;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.protocol.MQProtos;
import com.alibaba.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import com.alibaba.rocketmq.remoting.RemotingServer;
import com.alibaba.rocketmq.remoting.netty.NettyClientConfig;
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
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BrokerLoggerName);
    // ����������
    private final BrokerConfig brokerConfig;
    // ͨ�Ų�����
    private final NettyServerConfig nettyServerConfig;
    private final NettyClientConfig nettyClientConfig;
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

    // Broker��������Client
    private final Broker2Client broker2Client;

    // ���������ù���
    private final SubscriptionGroupManager subscriptionGroupManager;

    // �������ڳ�Ա�����仯������֪ͨ���г�Ա
    private final ConsumerIdsChangeListener consumerIdsChangeListener;

    // ������е�������
    private final RebalanceLockManager rebalanceLockManager = new RebalanceLockManager();

    // Broker��ͨ�Ų�ͻ���
    private final BrokerOuterAPI brokerOuterAPI;

    private final ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "BrokerControllerScheduledThread");
            }
        });

    // �Ƿ���Ҫ���ڸ���HA Master��ַ
    private boolean updateMasterHAServerAddrPeriodically = false;

    private final DigestLogManager digestLogManager;


    public BrokerController(//
            final BrokerConfig brokerConfig, //
            final NettyServerConfig nettyServerConfig, //
            final NettyClientConfig nettyClientConfig, //
            final MessageStoreConfig messageStoreConfig //
    ) {
        this.brokerConfig = brokerConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.nettyClientConfig = nettyClientConfig;
        this.messageStoreConfig = messageStoreConfig;
        this.consumerOffsetManager = new ConsumerOffsetManager(this);
        this.topicConfigManager = new TopicConfigManager(this);
        this.pullMessageProcessor = new PullMessageProcessor(this);
        this.pullRequestHoldService = new PullRequestHoldService(this);
        this.consumerIdsChangeListener = new DefaultConsumerIdsChangeListener(this);
        this.consumerManager = new ConsumerManager(this.consumerIdsChangeListener);
        this.producerManager = new ProducerManager();
        this.clientHousekeepingService = new ClientHousekeepingService(this);
        this.defaultTransactionCheckExecuter = new DefaultTransactionCheckExecuter(this);
        this.broker2Client = new Broker2Client(this);
        this.subscriptionGroupManager = new SubscriptionGroupManager(this);
        this.brokerOuterAPI = new BrokerOuterAPI(nettyClientConfig);

        if (this.brokerConfig.getNamesrvAddr() != null) {
            this.brokerOuterAPI.updateNameServerAddressList(this.brokerConfig.getNamesrvAddr());
            log.info("user specfied name server address: {}", this.brokerConfig.getNamesrvAddr());
        }
        this.digestLogManager = new DigestLogManager();
    }


    public boolean initialize() {
        boolean result = true;

        // ��ӡ���������ò���
        MixAll.printObjectProperties(log, this.brokerConfig);
        MixAll.printObjectProperties(log, this.nettyServerConfig);
        MixAll.printObjectProperties(log, this.messageStoreConfig);

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
            this.remotingServer =
                    new NettyRemotingServer(this.nettyServerConfig, this.clientHousekeepingService);

            // ��ʼ���̳߳�
            this.sendMessageExecutor =
                    Executors.newFixedThreadPool(this.brokerConfig.getSendMessageThreadPoolNums(),
                        new ThreadFactory() {

                            private AtomicInteger threadIndex = new AtomicInteger(0);


                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r, "SendMessageThread_"
                                        + this.threadIndex.incrementAndGet());
                            }
                        });

            this.pullMessageExecutor =
                    Executors.newFixedThreadPool(this.brokerConfig.getPullMessageThreadPoolNums(),
                        new ThreadFactory() {

                            private AtomicInteger threadIndex = new AtomicInteger(0);


                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r, "PullMessageThread_"
                                        + this.threadIndex.incrementAndGet());
                            }
                        });

            this.adminBrokerExecutor =
                    Executors.newFixedThreadPool(this.brokerConfig.getAdminBrokerThreadPoolNums(),
                        new ThreadFactory() {

                            private AtomicInteger threadIndex = new AtomicInteger(0);


                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r, "AdminBrokerThread_"
                                        + this.threadIndex.incrementAndGet());
                            }
                        });

            this.registerProcessor();

            // ��ʱˢ���ѽ���
            this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        BrokerController.this.consumerOffsetManager.persist();
                    }
                    catch (Exception e) {
                        log.error("", e);
                    }
                }
            }, 1000 * 10, this.brokerConfig.getFlushConsumerOffsetInterval(), TimeUnit.MILLISECONDS);

            // ��ʱ��ӡ����������������ٶ�
            this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        BrokerController.this.consumerOffsetManager.recordPullTPS();
                    }
                    catch (Exception e) {
                        log.error("", e);
                    }
                }
            }, 1000 * 10, this.brokerConfig.getFlushConsumerOffsetHistoryInterval(), TimeUnit.MILLISECONDS);

            // �Ȼ�ȡName Server��ַ
            if (null == this.brokerConfig.getNamesrvAddr()) {
                this.brokerConfig.setNamesrvAddr(this.brokerOuterAPI.fetchNameServerAddr());
            }

            // ��ʱ��ȡName Server��ַ
            if (null == this.brokerConfig.getNamesrvAddr()) {
                this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            BrokerController.this.brokerOuterAPI.fetchNameServerAddr();
                        }
                        catch (Exception e) {
                            log.error("ScheduledTask fetchNameServerAddr exception", e);
                        }
                    }
                }, 1000 * 10, 1000 * 60 * 2, TimeUnit.MILLISECONDS);
            }

            // �����slave
            if (BrokerRole.SLAVE == this.messageStoreConfig.getBrokerRole()) {
                if (this.messageStoreConfig.getMasterAddress() != null
                        && this.messageStoreConfig.getMasterAddress().length() >= 6) {
                    this.messageStore.updateMasterAddress(this.messageStoreConfig.getMasterAddress());
                    this.updateMasterHAServerAddrPeriodically = false;
                }
                else {
                    this.updateMasterHAServerAddrPeriodically = true;
                }
            }

        }

        return result;
    }


    public synchronized void registerBrokerAll() {
        TopicConfigSerializeWrapper topicConfigWrapper =
                this.getTopicConfigManager().buildTopicConfigSerializeWrapper();

        String haServerAddr = this.brokerOuterAPI.registerBrokerAll(//
            this.brokerConfig.getBrokerClusterName(), //
            this.getBrokerAddr(), //
            this.brokerConfig.getBrokerName(), //
            this.brokerConfig.getBrokerId(), //
            this.getHAServerAddr(), topicConfigWrapper);

        if (this.updateMasterHAServerAddrPeriodically && haServerAddr != null) {
            this.messageStore.updateMasterAddress(haServerAddr);
        }
    }


    private void unregisterBrokerAll() {
        this.brokerOuterAPI.unregisterBrokerAll(//
            this.brokerConfig.getBrokerClusterName(), //
            this.getBrokerAddr(), //
            this.brokerConfig.getBrokerName(), //
            this.brokerConfig.getBrokerId());
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

        {
            Properties properties = MixAll.object2Properties(this.nettyClientConfig);
            if (properties != null) {
                sb.append(MixAll.properties2String(properties));
            }
            else {
                log.error("encodeAllConfig object2Properties error");
            }
        }
        return sb.toString();
    }


    private void flushAllConfig() {
        String allConfig = this.encodeAllConfig();
        try {
            MixAll.string2File(allConfig, this.brokerConfig.getBrokerConfigPath());
            log.info("flush broker config, {} OK", this.brokerConfig.getBrokerConfigPath());
        }
        catch (IOException e) {
            log.info("flush broker config Exception, " + this.brokerConfig.getBrokerConfigPath(), e);
        }
    }


    public Broker2Client getBroker2Client() {
        return broker2Client;
    }


    public String getBrokerAddr() {
        String addr = this.brokerConfig.getBrokerIP1() + ":" + this.nettyServerConfig.getListenPort();
        return addr;
    }


    public String getHAServerAddr() {
        String addr = this.brokerConfig.getBrokerIP2() + ":" + this.messageStoreConfig.getHaListenPort();
        return addr;
    }


    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }


    public String getConfigDataVersion() {
        return this.configDataVersion.toJson();
    }


    public ConsumerManager getConsumerManager() {
        return consumerManager;
    }


    public ConsumerOffsetManager getConsumerOffsetManager() {
        return consumerOffsetManager;
    }


    public DefaultTransactionCheckExecuter getDefaultTransactionCheckExecuter() {
        return defaultTransactionCheckExecuter;
    }


    public MessageStore getMessageStore() {
        return messageStore;
    }


    public MessageStoreConfig getMessageStoreConfig() {
        return messageStoreConfig;
    }


    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }


    public ProducerManager getProducerManager() {
        return producerManager;
    }


    public PullMessageProcessor getPullMessageProcessor() {
        return pullMessageProcessor;
    }


    public PullRequestHoldService getPullRequestHoldService() {
        return pullRequestHoldService;
    }


    public RemotingServer getRemotingServer() {
        return remotingServer;
    }


    public SubscriptionGroupManager getSubscriptionGroupManager() {
        return subscriptionGroupManager;
    }


    public TopicConfigManager getTopicConfigManager() {
        return topicConfigManager;
    }


    public DigestLogManager getDigestLogManager() {
        return digestLogManager;
    }


    public void registerProcessor() {
        /**
         * SendMessageProcessor
         */
        NettyRequestProcessor sendProcessor = new SendMessageProcessor(this);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.SEND_MESSAGE_VALUE, sendProcessor,
            this.sendMessageExecutor);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.CONSUMER_SEND_MSG_BACK_VALUE,
            sendProcessor, this.sendMessageExecutor);

        /**
         * PullMessageProcessor
         */
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.PULL_MESSAGE_VALUE,
            this.pullMessageProcessor, this.pullMessageExecutor);

        /**
         * QueryMessageProcessor
         */
        NettyRequestProcessor queryProcessor = new QueryMessageProcessor(this);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.QUERY_MESSAGE_VALUE, queryProcessor,
            this.pullMessageExecutor);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.VIEW_MESSAGE_BY_ID_VALUE,
            queryProcessor, this.pullMessageExecutor);

        /**
         * ClientManageProcessor
         */
        NettyRequestProcessor clientProcessor = new ClientManageProcessor(this);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.HEART_BEAT_VALUE, clientProcessor,
            this.adminBrokerExecutor);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.UNREGISTER_CLIENT_VALUE,
            clientProcessor, this.adminBrokerExecutor);
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.GET_CONSUMER_LIST_BY_GROUP_VALUE,
            clientProcessor, this.adminBrokerExecutor);

        /**
         * EndTransactionProcessor
         */
        this.remotingServer.registerProcessor(MQProtos.MQRequestCode.END_TRANSACTION_VALUE,
            new EndTransactionProcessor(this), this.sendMessageExecutor);

        /**
         * Default
         */
        this.remotingServer
            .registerDefaultProcessor(new AdminBrokerProcessor(this), this.adminBrokerExecutor);
    }


    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }


    public void setRemotingServer(RemotingServer remotingServer) {
        this.remotingServer = remotingServer;
    }


    public void setTopicConfigManager(TopicConfigManager topicConfigManager) {
        this.topicConfigManager = topicConfigManager;
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
        try {
            this.scheduledExecutorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
        }

        this.unregisterBrokerAll();

        if (this.sendMessageExecutor != null) {
            this.sendMessageExecutor.shutdown();
        }

        if (this.pullMessageExecutor != null) {
            this.pullMessageExecutor.shutdown();
        }

        if (this.adminBrokerExecutor != null) {
            this.adminBrokerExecutor.shutdown();
        }

        if (this.brokerOuterAPI != null) {
            this.brokerOuterAPI.shutdown();
        }
        this.digestLogManager.dispose();

        this.consumerOffsetManager.persist();
    }


    public void start() throws Exception {
        if (this.messageStore != null) {
            this.messageStore.start();
        }

        if (this.remotingServer != null) {
            this.remotingServer.start();
        }

        if (this.brokerOuterAPI != null) {
            this.brokerOuterAPI.start();
        }

        if (this.pullRequestHoldService != null) {
            this.pullRequestHoldService.start();
        }

        if (this.clientHousekeepingService != null) {
            this.clientHousekeepingService.start();
        }

        // ����ͳ����־
        this.digestLogManager.start();
        // ����ʱ��ǿ��ע��
        this.registerBrokerAll();

        // ��ʱע��Broker��Name Server
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    BrokerController.this.registerBrokerAll();
                }
                catch (Exception e) {
                    log.error("registerBrokerAll Exception", e);
                }
            }
        }, 1000 * 10, 1000 * 30, TimeUnit.MILLISECONDS);
    }


    public void updateAllConfig(Properties properties) {
        MixAll.properties2Object(properties, brokerConfig);
        MixAll.properties2Object(properties, nettyServerConfig);
        MixAll.properties2Object(properties, nettyClientConfig);
        MixAll.properties2Object(properties, messageStoreConfig);
        this.configDataVersion.nextVersion();
        this.flushAllConfig();
    }


    public RebalanceLockManager getRebalanceLockManager() {
        return rebalanceLockManager;
    }
}
