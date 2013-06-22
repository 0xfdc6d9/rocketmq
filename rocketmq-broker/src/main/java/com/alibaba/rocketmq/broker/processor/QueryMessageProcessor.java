/**
 * $Id: QueryMessageProcessor.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.broker.processor;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.FileRegion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.broker.BrokerController;
import com.alibaba.rocketmq.broker.pagecache.OneMessageTransfer;
import com.alibaba.rocketmq.broker.pagecache.QueryMessageTransfer;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.protocol.MQProtos.MQRequestCode;
import com.alibaba.rocketmq.common.protocol.MQProtos.MQResponseCode;
import com.alibaba.rocketmq.common.protocol.header.QueryMessageRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.QueryMessageResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.ViewMessageRequestHeader;
import com.alibaba.rocketmq.remoting.exception.RemotingCommandException;
import com.alibaba.rocketmq.remoting.netty.NettyRequestProcessor;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;
import com.alibaba.rocketmq.remoting.protocol.RemotingProtos.ResponseCode;
import com.alibaba.rocketmq.store.QueryMessageResult;
import com.alibaba.rocketmq.store.SelectMapedBufferResult;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public class QueryMessageProcessor implements NettyRequestProcessor {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BrokerLoggerName);

    private final BrokerController brokerController;


    public QueryMessageProcessor(final BrokerController brokerController) {
        this.brokerController = brokerController;
    }


    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
            throws RemotingCommandException {
        MQRequestCode code = MQRequestCode.valueOf(request.getCode());
        switch (code) {
        case QUERY_MESSAGE:
            return this.queryMessage(ctx, request);
        case VIEW_MESSAGE_BY_ID:
            return this.viewMessageById(ctx, request);
        default:
            break;
        }

        return null;
    }


    public RemotingCommand queryMessage(ChannelHandlerContext ctx, RemotingCommand request)
            throws RemotingCommandException {
        final RemotingCommand response =
                RemotingCommand.createResponseCommand(QueryMessageResponseHeader.class);
        final QueryMessageResponseHeader responseHeader =
                (QueryMessageResponseHeader) response.getCustomHeader();
        final QueryMessageRequestHeader requestHeader =
                (QueryMessageRequestHeader) request
                    .decodeCommandCustomHeader(QueryMessageRequestHeader.class);

        // У���ѯʱ�䷶Χ
        long maxTimeSpan =
                this.brokerController.getBrokerConfig().getQueryMessageMaxTimeSpan() * 60 * 60 * 1000;
        long diff = requestHeader.getEndTimestamp() - requestHeader.getBeginTimestamp();
        if (diff > maxTimeSpan) {
            response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
            response.setRemark("the time range is too long, broker limits " + maxTimeSpan + "h");
            return response;
        }

        // ����ʹ��sendfile�����Ա���Ҫ����
        response.setOpaque(request.getOpaque());

        final QueryMessageResult queryMessageResult =
                this.brokerController.getMessageStore().queryMessage(requestHeader.getTopic(),
                    requestHeader.getKey(), requestHeader.getMaxNum(), requestHeader.getBeginTimestamp(),
                    requestHeader.getEndTimestamp());
        assert queryMessageResult != null;

        responseHeader.setIndexLastUpdatePhyoffset(queryMessageResult.getIndexLastUpdatePhyoffset());
        responseHeader.setIndexLastUpdateTimestamp(queryMessageResult.getIndexLastUpdateTimestamp());

        // ˵���ҵ���Ϣ
        if (queryMessageResult.getBufferTotalSize() > 0) {
            response.setCode(ResponseCode.SUCCESS_VALUE);
            response.setRemark(null);

            try {
                FileRegion fileRegion =
                        new QueryMessageTransfer(response.encodeHeader(queryMessageResult
                            .getBufferTotalSize()), queryMessageResult);
                ctx.channel().sendFile(fileRegion).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        queryMessageResult.release();
                        if (!future.isSuccess()) {
                            log.error("transfer query message by pagecache failed, ", future.cause());
                        }
                    }
                });
            }
            catch (Throwable e) {
                log.error("", e);
                queryMessageResult.release();
            }

            return null;
        }

        response.setCode(MQResponseCode.QUERY_NOT_FOUND_VALUE);
        response.setRemark("can not find message, maybe time range not correct");
        return response;
    }


    public RemotingCommand viewMessageById(ChannelHandlerContext ctx, RemotingCommand request)
            throws RemotingCommandException {
        final RemotingCommand response = RemotingCommand.createResponseCommand(null);
        final ViewMessageRequestHeader requestHeader =
                (ViewMessageRequestHeader) request.decodeCommandCustomHeader(ViewMessageRequestHeader.class);

        // ����ʹ��sendfile�����Ա���Ҫ����
        response.setOpaque(request.getOpaque());

        final SelectMapedBufferResult selectMapedBufferResult =
                this.brokerController.getMessageStore().selectOneMessageByOffset(requestHeader.getOffset());
        if (selectMapedBufferResult != null) {
            response.setCode(ResponseCode.SUCCESS_VALUE);
            response.setRemark(null);

            try {
                FileRegion fileRegion =
                        new OneMessageTransfer(response.encodeHeader(selectMapedBufferResult.getSize()),
                            selectMapedBufferResult);
                ctx.channel().sendFile(fileRegion).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        selectMapedBufferResult.release();
                        if (!future.isSuccess()) {
                            log.error("transfer one message by pagecache failed, ", future.cause());
                        }
                    }
                });
            }
            catch (Throwable e) {
                log.error("", e);
                selectMapedBufferResult.release();
            }

            return null;
        }
        else {
            response.setCode(ResponseCode.SYSTEM_ERROR_VALUE);
            response.setRemark("can not find message by the offset, " + requestHeader.getOffset());
        }

        return response;
    }
}
