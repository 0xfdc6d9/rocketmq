package com.alibaba.rocketmq.broker.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.broker.BrokerController;
import com.alibaba.rocketmq.common.MixAll;
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
        // �ڶ�������ѯ��Ϣ
        // ����������Producer�����첽RPC����
        // ���Ĳ����յ��첽Ӧ��󣬿�ʼ����Ӧ����
    }
}
