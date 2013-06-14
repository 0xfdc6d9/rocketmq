package com.alibaba.rocketmq.broker.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.broker.BrokerController;
import com.alibaba.rocketmq.broker.client.ClientChannelInfo;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.protocol.header.CheckTransactionStateRequestHeader;
import com.alibaba.rocketmq.store.SelectMapedBufferResult;
import com.alibaba.rocketmq.store.transaction.TransactionCheckExecuter;


/**
 * �洢��ص��˽ӿڣ����������ز�Producer������״̬
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class DefaultTransactionCheckExecuter implements TransactionCheckExecuter {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BrokerLoggerName);
    private final BrokerController brokerController;


    public DefaultTransactionCheckExecuter(final BrokerController brokerController) {
        this.brokerController = brokerController;
    }


    @Override
    public void gotoCheck(int producerGroupHashCode, long tranStateTableOffset, long commitLogOffset, int msgSize) {
        // ��һ������ѯProducer
        final ClientChannelInfo clientChannelInfo =
                this.brokerController.getProducerManager().pickProducerChannelRandomly(producerGroupHashCode);
        if (null == clientChannelInfo) {
            log.warn("check a producer transaction state, but not find any channel of this group[{}]",
                producerGroupHashCode);
            return;
        }

        // �ڶ�������ѯ��Ϣ
        SelectMapedBufferResult selectMapedBufferResult =
                this.brokerController.getMessageStore().selectOneMessageByOffset(commitLogOffset, msgSize);
        if (null == selectMapedBufferResult) {
            log.warn("check a producer transaction state, but not find message by commitLogOffset: {}, msgSize: ",
                commitLogOffset, msgSize);
            return;
        }

        // ����������Producer��������
        final CheckTransactionStateRequestHeader requestHeader = new CheckTransactionStateRequestHeader();
        requestHeader.setCommitLogOffset(commitLogOffset);
        requestHeader.setTranStateTableOffset(tranStateTableOffset);
        this.brokerController.getBroker2Client().checkProducerTransactionState(clientChannelInfo.getChannel(),
            requestHeader, selectMapedBufferResult);
    }
}
