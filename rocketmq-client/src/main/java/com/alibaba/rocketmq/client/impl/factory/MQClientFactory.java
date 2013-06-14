/**
 * $Id: MQClientFactory.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.client.impl.factory;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.ClientConfig;
import com.alibaba.rocketmq.client.consumer.ConsumeFromWhichNode;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.ClientRemotingProcessor;
import com.alibaba.rocketmq.client.impl.FindBrokerResult;
import com.alibaba.rocketmq.client.impl.MQAdminImpl;
import com.alibaba.rocketmq.client.impl.MQClientAPIImpl;
import com.alibaba.rocketmq.client.impl.MQClientManager;
import com.alibaba.rocketmq.client.impl.consumer.MQConsumerInner;
import com.alibaba.rocketmq.client.impl.consumer.PullMessageService;
import com.alibaba.rocketmq.client.impl.consumer.RebalanceService;
import com.alibaba.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import com.alibaba.rocketmq.client.impl.producer.MQProducerInner;
import com.alibaba.rocketmq.client.impl.producer.TopicPublishInfo;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.ServiceState;
import com.alibaba.rocketmq.common.constant.PermName;
import com.alibaba.rocketmq.common.help.FAQUrl;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.ConsumerData;
import com.alibaba.rocketmq.common.protocol.heartbeat.HeartbeatData;
import com.alibaba.rocketmq.common.protocol.heartbeat.ProducerData;
import com.alibaba.rocketmq.common.protocol.heartbeat.SubscriptionData;
import com.alibaba.rocketmq.common.protocol.route.BrokerData;
import com.alibaba.rocketmq.common.protocol.route.QueueData;
import com.alibaba.rocketmq.common.protocol.route.TopicRouteData;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.alibaba.rocketmq.remoting.netty.NettyClientConfig;


/**
 * �ͻ���Factory�࣬��������Producer��Consumer
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class MQClientFactory {
    private ServiceState serviceState = ServiceState.CREATE_JUST;
    private final Logger log = ClientLogger.getLog();
    private final ClientConfig mQClientConfig;
    private final int factoryIndex;
    private final String clientId;
    private final long bootTimestamp = System.currentTimeMillis();

    // Producer����
    private final ConcurrentHashMap<String/* group */, MQProducerInner> producerTable =
            new ConcurrentHashMap<String, MQProducerInner>();
    // Consumer����
    private final ConcurrentHashMap<String/* group */, MQConsumerInner> consumerTable =
            new ConcurrentHashMap<String, MQConsumerInner>();
    // Netty�ͻ�������
    private final NettyClientConfig nettyClientConfig;
    // RPC���õķ�װ��
    private final MQClientAPIImpl mQClientAPIImpl;
    private final MQAdminImpl mQAdminImpl;

    // �洢��Name Server�õ���Topic·����Ϣ
    private final ConcurrentHashMap<String/* Topic */, TopicRouteData> topicRouteTable =
            new ConcurrentHashMap<String, TopicRouteData>();
    // ����Name Server��ȡTopic·����Ϣʱ������
    private final Lock lockNamesrv = new ReentrantLock();
    private final static long LockTimeoutMillis = 3000;

    // ������ע����������
    private final Lock lockHeartbeat = new ReentrantLock();

    // �洢Broker Name ��Broker Address�Ķ�Ӧ��ϵ
    private final ConcurrentHashMap<String/* Broker Name */, HashMap<Long/* brokerId */, String/* address */>> brokerAddrTable =
            new ConcurrentHashMap<String, HashMap<Long, String>>();

    // ��ʱ�߳�
    private final ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "MQClientFactoryScheduledThread");
            }
        });

    // �����������������������
    private final ClientRemotingProcessor clientRemotingProcessor;

    // ����һ��UDP�˿ڣ�������ֹͬһ��Factory������ݣ��п��ֲܷ��ڶ��JVM�У�
    private DatagramSocket datagramSocket;

    // ����Ϣ����
    private final PullMessageService pullMessageService;

    // Rebalance����
    private final RebalanceService rebalanceService;


    public MQClientFactory(ClientConfig mQClientConfig, int factoryIndex, String clientId) {
        this.mQClientConfig = mQClientConfig;
        this.factoryIndex = factoryIndex;
        this.nettyClientConfig = new NettyClientConfig();
        this.nettyClientConfig.setClientCallbackExecutorThreads(mQClientConfig.getClientCallbackExecutorThreads());
        this.clientRemotingProcessor = new ClientRemotingProcessor(this);
        this.mQClientAPIImpl = new MQClientAPIImpl(this.nettyClientConfig, this.clientRemotingProcessor);

        if (this.mQClientConfig.getNamesrvAddr() != null) {
            this.mQClientAPIImpl.updateNameServerAddressList(this.mQClientConfig.getNamesrvAddr());
            log.info("user specfied name server address: {}", this.mQClientConfig.getNamesrvAddr());
        }

        this.clientId = clientId;

        this.mQAdminImpl = new MQAdminImpl(this);

        this.pullMessageService = new PullMessageService(this);

        this.rebalanceService = new RebalanceService(this);

        log.info("created a new client fatory, FactoryIndex: {} ClinetID: {}", this.factoryIndex, this.clientId);
    }


    private void makesureInstanceNameIsOnly(final String instanceName) throws MQClientException {
        int udpPort = 33333;

        int value = instanceName.hashCode();
        if (value < 0) {
            value = Math.abs(value);
        }

        udpPort += value % 10000;

        try {
            this.datagramSocket = new DatagramSocket(udpPort);
        }
        catch (SocketException e) {
            throw new MQClientException("instance name is a duplicate one[" + udpPort + "], please set a new name"
                    + FAQUrl.suggestTodo(FAQUrl.CLIENT_INSTACNCE_NAME_DUPLICATE_URL), e);
        }
    }


    private void startScheduledTask() {
        // ��ʱ��ȡName Server��ַ
        if (null == this.mQClientConfig.getNamesrvAddr()) {
            this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    try {
                        MQClientFactory.this.mQClientAPIImpl.fetchNameServerAddr();
                    }
                    catch (Exception e) {
                        log.error("ScheduledTask fetchNameServerAddr exception", e);
                    }
                }
            }, 1000 * 10, 1000 * 60 * 2, TimeUnit.MILLISECONDS);
        }

        // ��ʱ��Name Server��ȡTopic·����Ϣ
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    MQClientFactory.this.updateTopicRouteInfoFromNameServer();
                }
                catch (Exception e) {
                    log.error("ScheduledTask updateTopicRouteInfoFromNameServer exception", e);
                }
            }
        }, 0, this.mQClientConfig.getPollNameServerInteval(), TimeUnit.MILLISECONDS);

        // ������Broker����������Ϣ���������Ĺ�ϵ�ȣ�
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    MQClientFactory.this.sendHeartbeatToAllBrokerWithLock();
                }
                catch (Exception e) {
                    log.error("ScheduledTask sendHeartbeatToAllBroker exception", e);
                }
            }
        }, 1000, this.mQClientConfig.getHeartbeatBrokerInterval(), TimeUnit.MILLISECONDS);

        // ��ʱ�־û�Consumer���ѽ��ȣ��㲥�洢�����أ���Ⱥ�洢��Broker��
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    MQClientFactory.this.persistAllConsumerOffset();
                }
                catch (Exception e) {
                    log.error("ScheduledTask uploadConsumerOffsets exception", e);
                }
            }
        }, 1000 * 10, this.mQClientConfig.getUploadConsumerOffsetInterval(), TimeUnit.MILLISECONDS);
    }


    public void start() throws MQClientException {
        synchronized (this) {
            switch (this.serviceState) {
            case CREATE_JUST:
                this.makesureInstanceNameIsOnly(this.mQClientConfig.getInstanceName());

                this.serviceState = ServiceState.RUNNING;
                if (null == this.mQClientConfig.getNamesrvAddr()) {
                    this.mQClientConfig.setNamesrvAddr(this.mQClientAPIImpl.fetchNameServerAddr());
                }

                this.startScheduledTask();
                this.mQClientAPIImpl.start();
                this.pullMessageService.start();
                this.rebalanceService.start();
                log.info("the client factory [{}] start OK", this.clientId);
                break;
            case RUNNING:
                break;
            case SHUTDOWN_ALREADY:
                break;
            default:
                break;
            }
        }
    }


    public void shutdown() {
        // Producer
        if (!this.producerTable.isEmpty())
            return;

        // Consumer
        if (!this.consumerTable.isEmpty())
            return;

        synchronized (this) {
            switch (this.serviceState) {
            case CREATE_JUST:
                break;
            case RUNNING:
                this.serviceState = ServiceState.SHUTDOWN_ALREADY;
                this.pullMessageService.shutdown(true);
                this.scheduledExecutorService.shutdown();
                this.mQClientAPIImpl.shutdown();
                this.rebalanceService.shutdown();

                if (this.datagramSocket != null) {
                    this.datagramSocket.close();
                    this.datagramSocket = null;
                }
                MQClientManager.getInstance().removeClientFactory(this.clientId);
                log.info("the client factory [{}] shutdown OK", this.clientId);
                break;
            case SHUTDOWN_ALREADY:
                break;
            default:
                break;
            }
        }
    }


    private void persistAllConsumerOffset() {
        for (MQConsumerInner consumer : this.consumerTable.values()) {
            consumer.persistConsumerOffset();
        }
    }


    private void unregisterClientWithLock(final String producerGroup, final String consumerGroup) {
        try {
            if (this.lockHeartbeat.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
                try {
                    this.unregisterClient(producerGroup, consumerGroup);
                }
                catch (Exception e) {
                    log.error("unregisterClient exception", e);
                }
                finally {
                    this.lockHeartbeat.unlock();
                }
            }
            else {
                log.warn("lock heartBeat, but failed.");
            }
        }
        catch (InterruptedException e) {
            log.warn("unregisterClientWithLock exception", e);
        }
    }


    private void unregisterClient(final String producerGroup, final String consumerGroup) {
        for (String name : this.brokerAddrTable.keySet()) {
            final HashMap<Long, String> oneTable = this.brokerAddrTable.get(name);
            if (oneTable != null) {
                for (Long id : oneTable.keySet()) {
                    String addr = oneTable.get(id);
                    if (addr != null) {
                        try {
                            this.mQClientAPIImpl.unregisterClient(addr, this.clientId, producerGroup,
                                consumerGroup, 3000);
                            log.info("unregister client[Producer: {} Consumer: {}] from broker[{} {} {}] success",
                                producerGroup, consumerGroup, name, id, addr);
                        }
                        catch (RemotingException e) {
                            log.error("unregister client exception from broker: " + addr, e);
                        }
                        catch (MQBrokerException e) {
                            log.error("unregister client exception from broker: " + addr, e);
                        }
                        catch (InterruptedException e) {
                            log.error("unregister client exception from broker: " + addr, e);
                        }
                    }
                }
            }
        }
    }


    private HeartbeatData prepareHeartbeatData() {
        HeartbeatData heartbeatData = new HeartbeatData();

        // clientID
        heartbeatData.setClientID(this.clientId);

        // Consumer
        for (String group : this.consumerTable.keySet()) {
            MQConsumerInner impl = this.consumerTable.get(group);
            if (impl != null) {
                ConsumerData consumerData = new ConsumerData();
                consumerData.setGroupName(impl.getGroupName());
                consumerData.setConsumeType(impl.getConsumeType());
                consumerData.setMessageModel(impl.getMessageModel());
                if (impl.getMQSubscriptions() != null) {
                    consumerData.getSubscriptionDataSet().addAll(impl.getMQSubscriptions());
                }

                heartbeatData.getConsumerDataSet().add(consumerData);
            }
        }

        // Producer
        for (String group : this.producerTable.keySet()) {
            MQProducerInner impl = this.producerTable.get(group);
            if (impl != null) {
                ProducerData producerData = new ProducerData();
                producerData.setGroupName(group);

                heartbeatData.getProducerDataSet().add(producerData);
            }
        }

        return heartbeatData;
    }


    public void sendHeartbeatToAllBrokerWithLock() {
        if (this.lockHeartbeat.tryLock()) {
            try {
                this.sendHeartbeatToAllBroker();
            }
            catch (final Exception e) {
                log.error("sendHeartbeatToAllBroker exception", e);
            }
            finally {
                this.lockHeartbeat.unlock();
            }
        }
        else {
            log.warn("lock heartBeat, but failed.");
        }
    }


    private void sendHeartbeatToAllBroker() {
        final HeartbeatData heartbeatData = this.prepareHeartbeatData();
        final boolean producerEmpty = heartbeatData.getProducerDataSet().isEmpty();
        final boolean consumerEmpty = heartbeatData.getConsumerDataSet().isEmpty();
        if (producerEmpty && consumerEmpty) {
            log.warn("sending hearbeat, but no consumer and no producer");
            return;
        }

        for (String name : this.brokerAddrTable.keySet()) {
            final HashMap<Long, String> oneTable = this.brokerAddrTable.get(name);
            if (oneTable != null) {
                for (Long id : oneTable.keySet()) {
                    String addr = oneTable.get(id);
                    if (addr != null) {
                        // ˵��ֻ��Producer������Slave������
                        if (consumerEmpty) {
                            if (id != MixAll.MASTER_ID)
                                continue;
                        }

                        try {
                            this.mQClientAPIImpl.sendHearbeat(addr, heartbeatData, 3000);
                            log.debug("send heart beat to broker[{} {} {}] success", name, id, addr);
                            log.debug(heartbeatData.toString());
                        }
                        catch (Exception e) {
                            log.error("send heart beat to broker exception", e);
                        }
                    }
                }
            }
        }
    }


    public boolean registerProducer(final String group, final DefaultMQProducerImpl producer) {
        if (null == group || null == producer) {
            return false;
        }

        MQProducerInner prev = this.producerTable.putIfAbsent(group, producer);
        if (prev != null) {
            log.warn("the producer group[{}] exist already.", group);
            return false;
        }

        this.sendHeartbeatToAllBrokerWithLock();

        return true;
    }


    public void rebalanceImmediately() {
        this.rebalanceService.wakeup();
    }


    public void doRebalance() {
        for (String group : this.consumerTable.keySet()) {
            MQConsumerInner impl = this.consumerTable.get(group);
            if (impl != null) {
                try {
                    impl.doRebalance();
                }
                catch (Exception e) {
                    log.error("doRebalance exception", e);
                }
            }
        }
    }


    public MQProducerInner selectProducer(final String group) {
        return this.producerTable.get(group);
    }


    public MQConsumerInner selectConsumer(final String group) {
        return this.consumerTable.get(group);
    }


    public void unregisterProducer(final String group) {
        this.producerTable.remove(group);
        this.unregisterClientWithLock(group, null);
    }


    public boolean registerConsumer(final String group, final MQConsumerInner consumer) {
        if (null == group || null == consumer) {
            return false;
        }

        MQConsumerInner prev = this.consumerTable.putIfAbsent(group, consumer);
        if (prev != null) {
            log.warn("the consumer group[" + group + "] exist already.");
            return false;
        }

        this.sendHeartbeatToAllBrokerWithLock();

        return true;
    }


    public void unregisterConsumer(final String group) {
        this.consumerTable.remove(group);
        this.unregisterClientWithLock(null, group);
    }


    /**
     * ������Ľӿڲ�ѯBroker��ַ��Master����
     * 
     * @param brokerName
     * @return
     */
    public FindBrokerResult findBrokerAddressInAdmin(final String brokerName) {
        String brokerAddr = null;
        boolean slave = false;
        boolean found = false;

        HashMap<Long/* brokerId */, String/* address */> map = this.brokerAddrTable.get(brokerName);
        if (map != null && !map.isEmpty()) {
            // TODO ����ж��Slave�����ܻ�ÿ�ζ�ѡ����ͬ��Slave��������Ҫ�Ż�
            FOR_SEG: for (Map.Entry<Long, String> entry : map.entrySet()) {
                Long id = entry.getKey();
                brokerAddr = entry.getValue();
                if (brokerAddr != null) {
                    found = true;
                    if (MixAll.MASTER_ID == id) {
                        slave = false;
                        break FOR_SEG;
                    }
                    else {
                        slave = true;
                    }
                    break;

                }
            } // end of for
        }

        if (found) {
            return new FindBrokerResult(brokerAddr, slave);
        }

        return null;
    }


    /**
     * ������Ϣ�����У�Ѱ��Broker��ַ��һ������Master
     */
    public String findBrokerAddressInPublish(final String brokerName) {
        HashMap<Long/* brokerId */, String/* address */> map = this.brokerAddrTable.get(brokerName);
        if (map != null && !map.isEmpty()) {
            return map.get(MixAll.MASTER_ID);
        }

        return null;
    }


    /**
     * ������Ϣ�����У�Ѱ��Broker��ַ��ȡMaster����Slave�ɲ�������
     */
    public FindBrokerResult findBrokerAddressInSubscribe(//
            final String brokerName,//
            final ConsumeFromWhichNode consumeFromWhichNode//
    ) {
        String brokerAddr = null;
        boolean slave = false;
        boolean found = false;

        HashMap<Long/* brokerId */, String/* address */> map = this.brokerAddrTable.get(brokerName);
        if (map != null && !map.isEmpty()) {
            // TODO ����ж��Slave�����ܻ�ÿ�ζ�ѡ����ͬ��Slave��������Ҫ�Ż�
            FOR_SEG: for (Map.Entry<Long, String> entry : map.entrySet()) {
                Long id = entry.getKey();
                brokerAddr = entry.getValue();
                if (brokerAddr != null) {
                    switch (consumeFromWhichNode) {
                    case CONSUME_FROM_MASTER_FIRST:
                        found = true;
                        if (MixAll.MASTER_ID == id) {
                            slave = false;
                            break FOR_SEG;
                        }
                        else {
                            slave = true;
                        }
                        break;
                    case CONSUME_FROM_SLAVE_FIRST:
                        found = true;
                        if (MixAll.MASTER_ID != id) {
                            slave = true;
                            break FOR_SEG;
                        }
                        else {
                            slave = false;
                        }
                        break;
                    case CONSUME_FROM_MASTER_ONLY:
                        if (MixAll.MASTER_ID == id) {
                            found = true;
                            slave = false;
                            break FOR_SEG;
                        }
                        break;
                    case CONSUME_FROM_SLAVE_ONLY:
                        if (MixAll.MASTER_ID != id) {
                            found = true;
                            slave = true;
                            break FOR_SEG;
                        }
                        break;
                    default:
                        break;
                    }
                }
            } // end of for
        }

        if (found) {
            return new FindBrokerResult(brokerAddr, slave);
        }

        return null;
    }


    public String findBrokerAddrByTopic(final String topic) {
        TopicRouteData topicRouteData = this.topicRouteTable.get(topic);
        if (topicRouteData != null) {
            List<BrokerData> brokers = topicRouteData.getBrokerDatas();
            if (!brokers.isEmpty()) {
                BrokerData bd = brokers.get(0);
                return bd.getOneBrokerAddr();
            }
        }

        return null;
    }


    public List<String> findConsumerIdList(final String topic, final String group) {
        String brokerAddr = this.findBrokerAddrByTopic(topic);
        if (null == brokerAddr) {
            this.updateTopicRouteInfoFromNameServer(topic);
            brokerAddr = this.findBrokerAddrByTopic(topic);
        }

        if (null != brokerAddr) {
            try {
                return this.mQClientAPIImpl.getConsumerIdListByGroup(brokerAddr, group, 3000);
            }
            catch (Exception e) {
                log.warn("getConsumerIdListByGroup exception, " + brokerAddr + " " + group, e);
            }
        }

        return null;
    }


    public static TopicPublishInfo topicRouteData2TopicPublishInfo(final String topic, final TopicRouteData route) {
        TopicPublishInfo info = new TopicPublishInfo();
        // ˳����Ϣ
        if (route.getOrderTopicConf() != null && route.getOrderTopicConf().length() > 0) {
            String[] brokers = route.getOrderTopicConf().split(";");
            for (String broker : brokers) {
                String[] item = broker.split(":");
                int nums = Integer.parseInt(item[1]);
                for (int i = 0; i < nums; i++) {
                    MessageQueue mq = new MessageQueue(topic, item[0], i);
                    info.getMessageQueueList().add(mq);
                }
            }

            info.setOrderTopic(true);
        }
        // ��˳����Ϣ
        else {
            List<QueueData> qds = route.getQueueDatas();
            // ����ԭ�򣺼�ʹû������˳����Ϣģʽ��Ĭ�϶��е�˳��ͬ���õ�һ�¡�
            Collections.sort(qds);
            for (QueueData qd : qds) {
                if (PermName.isWriteable(qd.getPerm())) {
                    for (int i = 0; i < qd.getWriteQueueNums(); i++) {
                        MessageQueue mq = new MessageQueue(topic, qd.getBrokerName(), i);
                        info.getMessageQueueList().add(mq);
                    }
                }
            }

            info.setOrderTopic(false);
        }

        return info;
    }


    public static Set<MessageQueue> topicRouteData2TopicSubscribeInfo(final String topic,
            final TopicRouteData route) {
        Set<MessageQueue> mqList = new HashSet<MessageQueue>();
        List<QueueData> qds = route.getQueueDatas();
        for (QueueData qd : qds) {
            if (PermName.isReadable(qd.getPerm())) {
                for (int i = 0; i < qd.getReadQueueNums(); i++) {
                    MessageQueue mq = new MessageQueue(topic, qd.getBrokerName(), i);
                    mqList.add(mq);
                }
            }
        }

        return mqList;
    }


    private void updateTopicRouteInfoFromNameServer() {
        Set<String> topicList = new HashSet<String>();

        // Consumer
        for (String g : this.consumerTable.keySet()) {
            MQConsumerInner impl = this.consumerTable.get(g);
            if (impl != null) {
                Set<SubscriptionData> subList = impl.getMQSubscriptions();
                if (subList != null) {
                    for (SubscriptionData subData : subList) {
                        topicList.add(subData.getTopic());
                    }
                }
            }
        }

        // Producer
        for (String g : this.producerTable.keySet()) {
            MQProducerInner impl = this.producerTable.get(g);
            if (impl != null) {
                Set<String> lst = impl.getPublishTopicList();
                topicList.addAll(lst);
            }
        }

        for (String topic : topicList) {
            this.updateTopicRouteInfoFromNameServer(topic);
        }
    }


    public TopicRouteData getAnExistTopicRouteData(final String topic) {
        return this.topicRouteTable.get(topic);
    }


    /**
     * ����Name Server�ӿڣ�����Topic��ȡ·����Ϣ
     */
    public boolean updateTopicRouteInfoFromNameServer(final String topic) {
        try {
            if (this.lockNamesrv.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
                try {
                    TopicRouteData topicRouteData =
                            this.mQClientAPIImpl.getTopicRouteInfoFromNameServer(topic, 1000 * 3);
                    if (topicRouteData != null) {
                        TopicRouteData old = this.topicRouteTable.get(topic);
                        if (null == old || !old.equals(topicRouteData)) {
                            log.info("the topic[" + topic + "] route info changed, " + topicRouteData);
                            // ����Broker��ַ��Ϣ
                            for (BrokerData bd : topicRouteData.getBrokerDatas()) {
                                this.brokerAddrTable.put(bd.getBrokerName(), bd.getBrokerAddrs());
                            }

                            // ���·���������Ϣ
                            TopicPublishInfo publishInfo = topicRouteData2TopicPublishInfo(topic, topicRouteData);
                            for (String g : this.producerTable.keySet()) {
                                MQProducerInner impl = this.producerTable.get(g);
                                if (impl != null) {
                                    impl.updateTopicPublishInfo(topic, publishInfo);
                                }
                            }

                            // ���¶��Ķ�����Ϣ
                            Set<MessageQueue> subscribeInfo =
                                    topicRouteData2TopicSubscribeInfo(topic, topicRouteData);
                            for (String g : this.consumerTable.keySet()) {
                                MQConsumerInner impl = this.consumerTable.get(g);
                                if (impl != null) {
                                    impl.updateTopicSubscribeInfo(topic, subscribeInfo);
                                }
                            }

                            this.topicRouteTable.put(topic, topicRouteData);
                            return true;
                        }
                    }
                }
                catch (Exception e) {
                    log.warn("updateTopicRouteInfoFromNameServer Exception", e);
                }
                finally {
                    this.lockNamesrv.unlock();
                }
            }
        }
        catch (InterruptedException e) {
            log.warn("updateTopicRouteInfoFromNameServer Exception", e);
        }

        return false;
    }


    public MQClientAPIImpl getMQClientAPIImpl() {
        return mQClientAPIImpl;
    }


    public MQAdminImpl getMQAdminImpl() {
        return mQAdminImpl;
    }


    public String getClientId() {
        return clientId;
    }


    public long getBootTimestamp() {
        return bootTimestamp;
    }


    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }


    public PullMessageService getPullMessageService() {
        return pullMessageService;
    }
}
