/**
 * $Id: EndTransactionProcessor.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.broker.processor;

import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.broker.BrokerController;
import com.alibaba.rocketmq.common.TopicFilterType;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageDecoder;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.MQProtos.MQResponseCode;
import com.alibaba.rocketmq.common.protocol.header.EndTransactionRequestHeader;
import com.alibaba.rocketmq.common.sysflag.MessageSysFlag;
import com.alibaba.rocketmq.remoting.common.RemotingHelper;
import com.alibaba.rocketmq.remoting.exception.RemotingCommandException;
import com.alibaba.rocketmq.remoting.netty.NettyRequestProcessor;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;
import com.alibaba.rocketmq.remoting.protocol.RemotingProtos.ResponseCode;
import com.alibaba.rocketmq.store.MessageExtBrokerInner;
import com.alibaba.rocketmq.store.MessageStore;
import com.alibaba.rocketmq.store.PutMessageResult;


/**
 * Commit��Rollback����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public class EndTransactionProcessor implements NettyRequestProcessor {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BrokerLoggerName);

    private final BrokerController brokerController;


    public EndTransactionProcessor(final BrokerController brokerController) {
        this.brokerController = brokerController;
    }


    private MessageExtBrokerInner endMessageTransaction(MessageExt msgExt) {
        MessageExtBrokerInner msgInner = new MessageExtBrokerInner();
        msgInner.setBody(msgExt.getBody());
        msgInner.setFlag(msgExt.getFlag());
        msgInner.setProperties(msgExt.getProperties());

        TopicFilterType topicFilterType =
                (msgInner.getSysFlag() & MessageSysFlag.MultiTagsFlag) == MessageSysFlag.MultiTagsFlag ? TopicFilterType.MULTI_TAG
                        : TopicFilterType.SINGLE_TAG;
        long tagsCodeValue = MessageExtBrokerInner.tagsString2tagsCode(topicFilterType, msgInner.getTags());
        msgInner.setTagsCode(tagsCodeValue);
        msgInner.setPropertiesString(MessageDecoder.messageProperties2String(msgExt.getProperties()));

        msgInner.setSysFlag(msgExt.getSysFlag());
        msgInner.setBornTimestamp(msgExt.getBornTimestamp());
        msgInner.setBornHost(msgExt.getBornHost());
        msgInner.setStoreHost(msgExt.getStoreHost());
        msgInner.setReconsumeTimes(msgExt.getReconsumeTimes());

        msgInner.setWaitStoreMsgOK(false);
        msgInner.clearProperty(Message.PROPERTY_DELAY_TIME_LEVEL);

        msgInner.setTopic(msgExt.getTopic());
        msgInner.setQueueId(msgExt.getQueueId());

        return msgInner;
    }

    private static final Logger logTransaction = LoggerFactory.getLogger(LoggerName.BrokerLoggerName);


    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
            throws RemotingCommandException {
        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        final EndTransactionRequestHeader requestHeader =
                (EndTransactionRequestHeader) request
                    .decodeCommandCustomHeader(EndTransactionRequestHeader.class);

        // �ز�Ӧ��
        if (requestHeader.getFromTransactionCheck()) {
            switch (requestHeader.getCommitOrRollback()) {
            // ���ύҲ���ع�
            case MessageSysFlag.TransactionNotType: {
                logTransaction.warn("check producer[{}] transaction state, but it's pending status.\n"//
                        + "RequestHeader: {} Remark: {}",//
                    RemotingHelper.parseChannelRemoteAddr(ctx.channel()), //
                    requestHeader.toString(),//
                    request.getRemark());
                return null;
            }
            // �ύ
            case MessageSysFlag.TransactionCommitType: {
                logTransaction.warn(
                    "check producer[{}] transaction state, the producer commit the message.\n"//
                            + "RequestHeader: {} Remark: {}",//
                    RemotingHelper.parseChannelRemoteAddr(ctx.channel()), //
                    requestHeader.toString(),//
                    request.getRemark());

                break;
            }
            // �ع�
            case MessageSysFlag.TransactionRollbackType: {
                logTransaction.warn(
                    "check producer[{}] transaction state, the producer rollback the message.\n"//
                            + "RequestHeader: {} Remark: {}",//
                    RemotingHelper.parseChannelRemoteAddr(ctx.channel()), //
                    requestHeader.toString(),//
                    request.getRemark());
                break;
            }
            default:
                return null;
            }
        }
        // �����ύ�ع�
        else {
            switch (requestHeader.getCommitOrRollback()) {
            // ���ύҲ���ع�
            case MessageSysFlag.TransactionNotType: {
                logTransaction.warn(
                    "the producer[{}] end transaction in sending message,  and it's pending status.\n"//
                            + "RequestHeader: {} Remark: {}",//
                    RemotingHelper.parseChannelRemoteAddr(ctx.channel()), //
                    requestHeader.toString(),//
                    request.getRemark());
                return null;
            }
            // �ύ
            case MessageSysFlag.TransactionCommitType: {
                break;
            }
            // �ع�
            case MessageSysFlag.TransactionRollbackType: {
                logTransaction.warn(
                    "the producer[{}] end transaction in sending message, rollback the message.\n"//
                            + "RequestHeader: {} Remark: {}",//
                    RemotingHelper.parseChannelRemoteAddr(ctx.channel()), //
                    requestHeader.toString(),//
                    request.getRemark());
                break;
            }
            default:
                return null;
            }
        }

        final MessageExt msgExt =
                this.brokerController.getMessageStore().lookMessageByOffset(
                    requestHeader.getCommitLogOffset());
        if (msgExt != null) {
            // У��Producer Group
            final String pgroupRead = msgExt.getProperty(Message.PROPERTY_PRODUCER_GROUP);
            if (!pgroupRead.equals(requestHeader.getProducerGroup())) {
                response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
                response.setRemark("the producer group wrong");
                return response;
            }

            // У��Transaction State Table Offset
            if (msgExt.getQueueOffset() != requestHeader.getTranStateTableOffset()) {
                response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
                response.setRemark("the transaction state table offset wrong");
                return response;
            }

            // У��Commit Log Offset
            if (msgExt.getCommitLogOffset() != requestHeader.getCommitLogOffset()) {
                response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
                response.setRemark("the commit log offset wrong");
                return response;
            }

            MessageExtBrokerInner msgInner = this.endMessageTransaction(msgExt);
            msgInner.setSysFlag(MessageSysFlag.resetTransactionValue(msgInner.getSysFlag(),
                requestHeader.getCommitOrRollback()));

            msgInner.setQueueOffset(requestHeader.getTranStateTableOffset());
            msgInner.setPreparedTransactionOffset(requestHeader.getCommitLogOffset());
            msgInner.setStoreTimestamp(msgExt.getStoreTimestamp());
            if (MessageSysFlag.TransactionRollbackType == requestHeader.getCommitOrRollback()) {
                msgInner.setBody(null);
            }

            final MessageStore messageStore = this.brokerController.getMessageStore();
            final PutMessageResult putMessageResult = messageStore.putMessage(msgInner);
            if (putMessageResult != null) {
                switch (putMessageResult.getPutMessageStatus()) {
                // Success
                case PUT_OK:
                case FLUSH_DISK_TIMEOUT:
                case FLUSH_SLAVE_TIMEOUT:
                case SLAVE_NOT_AVAILABLE:
                    response.setCode(ResponseCode.SUCCESS_VALUE);
                    response.setRemark(null);
                    break;

                // Failed
                case CREATE_MAPEDFILE_FAILED:
                    response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
                    response.setRemark("create maped file failed.");
                    break;
                case MESSAGE_ILLEGAL:
                    response.setCode(MQResponseCode.MESSAGE_ILLEGAL_VALUE);
                    response.setRemark("the message is illegal, maybe length not matched.");
                    break;
                case SERVICE_NOT_AVAILABLE:
                    response.setCode(MQResponseCode.SERVICE_NOT_AVAILABLE_VALUE);
                    response.setRemark("service not available now.");
                    break;
                case UNKNOWN_ERROR:
                    response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
                    response.setRemark("UNKNOWN_ERROR");
                    break;
                default:
                    response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
                    response.setRemark("UNKNOWN_ERROR DEFAULT");
                    break;
                }

                return response;
            }
            else {
                response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
                response.setRemark("store putMessage return null");
            }
        }
        else {
            response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
            response.setRemark("find prepared transaction message failed");
            return response;
        }

        return response;
    }
}
