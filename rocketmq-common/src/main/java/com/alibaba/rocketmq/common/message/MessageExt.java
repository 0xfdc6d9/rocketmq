/**
 * $Id: MessageExt.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.common.message;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.alibaba.rocketmq.common.TopicFilterType;
import com.alibaba.rocketmq.common.sysflag.MessageSysFlag;


/**
 * ��Ϣ��չ���ԣ��ڷ������ϲ����˶���
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class MessageExt extends Message implements Serializable {
    private static final long serialVersionUID = 8319216613151969646L;

    // ����ID <PUT>
    private int queueId;
    // �洢��¼��С
    private int storeSize;
    // ����ƫ����
    private long queueOffset;
    // ��Ϣ��־λ <PUT>
    private int sysFlag;
    // ��Ϣ�ڿͻ��˴���ʱ��� <PUT>
    private long bornTimestamp;
    // ��Ϣ�������� <PUT>
    private SocketAddress bornHost;
    // ��Ϣ�ڷ������洢ʱ���
    private long storeTimestamp;
    // ��Ϣ�洢���ĸ������� <PUT>
    private SocketAddress storeHost;
    // ��ϢID
    private String msgId;
    // ��Ϣ��Ӧ��Commit Log Offset
    private long commitLogOffset;
    // ��Ϣ��CRC
    private int bodyCRC;
    // ��ǰ��Ϣ��ĳ�����������������˼��Σ�������֮�����������
    private int reconsumeTimes;

    private long preparedTransactionOffset;


    public MessageExt() {
    }


    public MessageExt(int queueId, long bornTimestamp, SocketAddress bornHost, long storeTimestamp,
            SocketAddress storeHost, String msgId) {
        this.queueId = queueId;
        this.bornTimestamp = bornTimestamp;
        this.bornHost = bornHost;
        this.storeTimestamp = storeTimestamp;
        this.storeHost = storeHost;
        this.msgId = msgId;
    }


    /**
     * SocketAddress ----> ByteBuffer ת����8���ֽ�
     */
    public static ByteBuffer SocketAddress2ByteBuffer(SocketAddress socketAddress) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        byteBuffer.put(inetSocketAddress.getAddress().getAddress());
        byteBuffer.putInt(inetSocketAddress.getPort());
        byteBuffer.flip();
        return byteBuffer;
    }


    /**
     * ��ȡbornHost�ֽ���ʽ��8���ֽ� HOST + PORT
     */
    public ByteBuffer getBornHostBytes() {
        return SocketAddress2ByteBuffer(this.bornHost);
    }


    /**
     * ��ȡstorehost�ֽ���ʽ��8���ֽ� HOST + PORT
     */
    public ByteBuffer getStoreHostBytes() {
        return SocketAddress2ByteBuffer(this.storeHost);
    }


    public int getQueueId() {
        return queueId;
    }


    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }


    public long getBornTimestamp() {
        return bornTimestamp;
    }


    public void setBornTimestamp(long bornTimestamp) {
        this.bornTimestamp = bornTimestamp;
    }


    public SocketAddress getBornHost() {
        return bornHost;
    }


    public String getBornHostString() {
        if (this.bornHost != null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) this.bornHost;
            return inetSocketAddress.getAddress().getHostAddress();
        }

        return null;
    }


    public String getBornHostNameString() {
        if (this.bornHost != null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) this.bornHost;
            return inetSocketAddress.getAddress().getHostName();
        }

        return null;
    }


    public void setBornHost(SocketAddress bornHost) {
        this.bornHost = bornHost;
    }


    public long getStoreTimestamp() {
        return storeTimestamp;
    }


    public void setStoreTimestamp(long storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }


    public SocketAddress getStoreHost() {
        return storeHost;
    }


    public void setStoreHost(SocketAddress storeHost) {
        this.storeHost = storeHost;
    }


    public String getMsgId() {
        return msgId;
    }


    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }


    public int getSysFlag() {
        return sysFlag;
    }


    public void setSysFlag(int sysFlag) {
        this.sysFlag = sysFlag;
    }


    public int getBodyCRC() {
        return bodyCRC;
    }


    public void setBodyCRC(int bodyCRC) {
        this.bodyCRC = bodyCRC;
    }


    public long getQueueOffset() {
        return queueOffset;
    }


    public void setQueueOffset(long queueOffset) {
        this.queueOffset = queueOffset;
    }


    public long getCommitLogOffset() {
        return commitLogOffset;
    }


    public void setCommitLogOffset(long physicOffset) {
        this.commitLogOffset = physicOffset;
    }


    public int getStoreSize() {
        return storeSize;
    }


    public void setStoreSize(int storeSize) {
        this.storeSize = storeSize;
    }


    public static TopicFilterType parseTopicFilterType(final int sysFlag) {
        if ((sysFlag & MessageSysFlag.MultiTagsFlag) == MessageSysFlag.MultiTagsFlag) {
            return TopicFilterType.MULTI_TAG;
        }

        return TopicFilterType.SINGLE_TAG;
    }


    public int getReconsumeTimes() {
        return reconsumeTimes;
    }


    public void setReconsumeTimes(int reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }


    public long getPreparedTransactionOffset() {
        return preparedTransactionOffset;
    }


    public void setPreparedTransactionOffset(long preparedTransactionOffset) {
        this.preparedTransactionOffset = preparedTransactionOffset;
    }


    @Override
    public String toString() {
        return "MessageExt [queueId=" + queueId + ", storeSize=" + storeSize + ", queueOffset=" + queueOffset
                + ", sysFlag=" + sysFlag + ", bornTimestamp=" + bornTimestamp + ", bornHost=" + bornHost
                + ", storeTimestamp=" + storeTimestamp + ", storeHost=" + storeHost + ", msgId=" + msgId
                + ", commitLogOffset=" + commitLogOffset + ", bodyCRC=" + bodyCRC + ", reconsumeTimes="
                + reconsumeTimes + ", preparedTransactionOffset=" + preparedTransactionOffset
                + ", toString()=" + super.toString() + "]";
    }
}
