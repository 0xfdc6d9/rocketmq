package com.alibaba.rocketmq.broker.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.broker.BrokerController;
import com.alibaba.rocketmq.broker.client.ClientChannelInfo;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.store.SelectMapedBufferResult;
import com.alibaba.rocketmq.store.transaction.TransactionCheckExecuter;


/**
 * �洢��ص��˽ӿڣ����������ز�Producer������״̬
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public class DefaultTransactionCheckExecuter implements TransactionCheckExecuter {
    private static final Logger log = LoggerFactory.getLogger(MixAll.TransactionLoggerName);
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
        // ����������Producer�����첽RPC����

        // ���Ĳ����յ��첽Ӧ��󣬿�ʼ����Ӧ����
    }
}
