/**
 * $Id: HAConnection.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store.ha;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.ServiceThread;
import com.alibaba.rocketmq.common.logger.LoggerName;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;
import com.alibaba.rocketmq.store.SelectMapedBufferResult;


/**
 * HA����Master������Slave Push���ݣ�������SlaveӦ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class HAConnection {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.StoreLoggerName);

    private final HAService haService;
    private final SocketChannel socketChannel;
    private final String clientAddr;

    private WriteSocketService writeSocketService;
    private ReadSocketService readSocketService;

    // Slave��������￪ʼ������
    private volatile long slaveRequestOffset = -1;
    // Slave�յ����ݺ�Ӧ��Offset
    private volatile long slaveAckOffset = -1;

    /**
     * ��ȡSlave����һ��Ϊpush ack
     * 
     * @author shijia.wxr<vintage.wang@gmail.com>
     */
    class ReadSocketService extends ServiceThread {
        private static final int ReadMaxBufferSize = 1024 * 1024;
        private final Selector selector;
        private final SocketChannel socketChannel;
        private final ByteBuffer byteBufferRead = ByteBuffer.allocate(ReadMaxBufferSize);
        private int processPostion = 0;
        private volatile long lastReadTimestamp = System.currentTimeMillis();


        public ReadSocketService(final SocketChannel socketChannel) throws IOException {
            this.selector = RemotingUtil.openSelector();
            this.socketChannel = socketChannel;
            this.socketChannel.register(this.selector, SelectionKey.OP_READ);
            // �߳��Զ����գ�����Ҫ�������߳�join
            this.thread.setDaemon(true);
        }


        private boolean processReadEvent() {
            int readSizeZeroTimes = 0;

            if (!this.byteBufferRead.hasRemaining()) {
                this.byteBufferRead.flip();
                this.processPostion = 0;
            }

            while (this.byteBufferRead.hasRemaining()) {
                try {
                    int readSize = this.socketChannel.read(this.byteBufferRead);
                    if (readSize > 0) {
                        readSizeZeroTimes = 0;
                        this.lastReadTimestamp =
                                HAConnection.this.haService.getDefaultMessageStore().getSystemClock().now();
                        // ����Slave�ϴ���offset
                        if ((this.byteBufferRead.position() - this.processPostion) >= 8) {
                            int pos = this.byteBufferRead.position() - (this.byteBufferRead.position() % 8);
                            long readOffset = this.byteBufferRead.getLong(pos - 8);
                            this.processPostion = pos;

                            // ����Slave������
                            HAConnection.this.slaveAckOffset = readOffset;
                            if (HAConnection.this.slaveRequestOffset < 0) {
                                HAConnection.this.slaveRequestOffset = readOffset;
                                log.info("slave[" + HAConnection.this.clientAddr + "] request offset "
                                        + readOffset);
                            }

                            // ֪ͨǰ���߳�
                            HAConnection.this.haService.notifyTransferSome(HAConnection.this.slaveAckOffset);
                        }
                    }
                    else if (readSize == 0) {
                        if (++readSizeZeroTimes >= 3) {
                            break;
                        }
                    }
                    else {
                        log.error("read socket[" + HAConnection.this.clientAddr + "] < 0");
                        return false;
                    }
                }
                catch (IOException e) {
                    log.error("processReadEvent exception", e);
                    return false;
                }
            }

            return true;
        }


        @Override
        public void run() {
            HAConnection.log.info(this.getServiceName() + " service started");

            while (!this.isStoped()) {
                try {
                    this.selector.select(1000);
                    boolean ok = this.processReadEvent();
                    if (!ok) {
                        HAConnection.log.error("processReadEvent error");
                        break;
                    }

                    // ����������ʱ�䣬������ǿ�ƶϿ�
                    long interval =
                            HAConnection.this.haService.getDefaultMessageStore().getSystemClock().now()
                                    - this.lastReadTimestamp;
                    if (interval > HAConnection.this.haService.getDefaultMessageStore().getMessageStoreConfig()
                        .getHaHousekeepingInterval()) {
                        log.warn("ha housekeeping, found this connection[" + HAConnection.this.clientAddr
                                + "] expired, " + interval);
                        break;
                    }
                }
                catch (Exception e) {
                    HAConnection.log.error(this.getServiceName() + " service has exception.", e);
                    break;
                }
            }

            this.makeStop();

            // ֻ�ж��߳���Ҫִ��
            HAConnection.this.haService.getConnectionCount().decrementAndGet();

            SelectionKey sk = this.socketChannel.keyFor(this.selector);
            if (sk != null) {
                sk.cancel();
            }

            try {
                this.selector.close();
                this.socketChannel.close();
            }
            catch (IOException e) {
                HAConnection.log.error("", e);
            }

            HAConnection.log.info(this.getServiceName() + " service end");
        }


        @Override
        public String getServiceName() {
            return ReadSocketService.class.getSimpleName();
        }
    }

    /**
     * ��Slave��������Э�� <Phy Offset> <Body Size> <Body Data><br>
     * ��Slave��������Э�� <Phy Offset>
     */
    /**
     * ��Slaveд������
     * 
     * @author shijia.wxr<vintage.wang@gmail.com>
     */
    class WriteSocketService extends ServiceThread {
        private final Selector selector;
        private final SocketChannel socketChannel;
        private long nextTransferFromWhere = -1;

        // Ҫ���������
        private final int HEADER_SIZE = 8 + 4;
        private final ByteBuffer byteBufferHeader = ByteBuffer.allocate(HEADER_SIZE);
        private SelectMapedBufferResult selectMapedBufferResult;

        private boolean lastWriteOver = true;
        private long lastWriteTimestamp = System.currentTimeMillis();


        public WriteSocketService(final SocketChannel socketChannel) throws IOException {
            this.selector = RemotingUtil.openSelector();
            this.socketChannel = socketChannel;
            this.socketChannel.register(this.selector, SelectionKey.OP_WRITE);
            this.thread.setDaemon(true);
        }


        @Override
        public void run() {
            HAConnection.log.info(this.getServiceName() + " service started");

            while (!this.isStoped()) {
                try {
                    this.selector.select(1000);

                    if (-1 == HAConnection.this.slaveRequestOffset) {
                        Thread.sleep(10);
                        continue;
                    }

                    // ��һ�δ��䣬��Ҫ��������￪ʼ
                    // Slave�������û�����ݣ������OffsetΪ0����ômaster��������ļ����һ���ļ���ʼ��������
                    if (-1 == this.nextTransferFromWhere) {
                        if (0 == HAConnection.this.slaveRequestOffset) {
                            long masterOffset =
                                    HAConnection.this.haService.getDefaultMessageStore().getCommitLog()
                                        .getMaxOffset();
                            masterOffset =
                                    masterOffset
                                            - (masterOffset % HAConnection.this.haService.getDefaultMessageStore()
                                                .getMessageStoreConfig().getMapedFileSizeCommitLog());

                            if (masterOffset < 0) {
                                masterOffset = 0;
                            }

                            this.nextTransferFromWhere = masterOffset;
                        }
                        else {
                            this.nextTransferFromWhere = HAConnection.this.slaveRequestOffset;
                        }

                        log.info("master transfer data from " + this.nextTransferFromWhere + " to slave["
                                + HAConnection.this.clientAddr + "], and slave request "
                                + HAConnection.this.slaveRequestOffset);
                    }

                    if (this.lastWriteOver) {
                        // �����ʱ��û�з���Ϣ���Է�����
                        long interval =
                                HAConnection.this.haService.getDefaultMessageStore().getSystemClock().now()
                                        - this.lastWriteTimestamp;

                        if (interval > HAConnection.this.haService.getDefaultMessageStore()
                            .getMessageStoreConfig().getHaSendHeartbeatInterval()) {
                            // ��Slave��������
                            // Build Header
                            this.byteBufferHeader.position(0);
                            this.byteBufferHeader.limit(HEADER_SIZE);
                            this.byteBufferHeader.putLong(this.nextTransferFromWhere);
                            this.byteBufferHeader.putInt(0);
                            this.byteBufferHeader.flip();

                            this.lastWriteOver = this.transferData();
                            if (!this.lastWriteOver)
                                continue;
                        }
                    }
                    // ��������
                    else {
                        this.lastWriteOver = this.transferData();
                        if (!this.lastWriteOver)
                            continue;
                    }

                    // ��������,
                    // selectResult�ḳֵ��this.selectMapedBufferResult�������쳣Ҳ�������
                    SelectMapedBufferResult selectResult =
                            HAConnection.this.haService.getDefaultMessageStore().getCommitLogData(
                                this.nextTransferFromWhere);
                    if (selectResult != null) {
                        int size = selectResult.getSize();
                        if (size > HAConnection.this.haService.getDefaultMessageStore().getMessageStoreConfig()
                            .getHaTransferBatchSize()) {
                            size =
                                    HAConnection.this.haService.getDefaultMessageStore().getMessageStoreConfig()
                                        .getHaTransferBatchSize();
                        }

                        long thisOffset = this.nextTransferFromWhere;
                        this.nextTransferFromWhere += size;

                        selectResult.getByteBuffer().limit(size);
                        this.selectMapedBufferResult = selectResult;

                        // Build Header
                        this.byteBufferHeader.position(0);
                        this.byteBufferHeader.limit(HEADER_SIZE);
                        this.byteBufferHeader.putLong(thisOffset);
                        this.byteBufferHeader.putInt(size);
                        this.byteBufferHeader.flip();

                        this.lastWriteOver = this.transferData();
                    }
                    else {
                        // û�����ݣ��ȴ�֪ͨ
                        HAConnection.this.haService.getWaitNotifyObject().allWaitForRunning(100);
                    }
                }
                catch (Exception e) {
                    // ֻҪ�׳��쳣��һ�������緢���������ӱ���Ͽ�����������Դ
                    HAConnection.log.error(this.getServiceName() + " service has exception.", e);
                    break;
                }
            }

            // ������Դ
            if (this.selectMapedBufferResult != null) {
                this.selectMapedBufferResult.release();
            }

            this.makeStop();

            SelectionKey sk = this.socketChannel.keyFor(this.selector);
            if (sk != null) {
                sk.cancel();
            }

            try {
                this.selector.close();
                this.socketChannel.close();
            }
            catch (IOException e) {
                HAConnection.log.error("", e);
            }

            HAConnection.log.info(this.getServiceName() + " service end");
        }


        /**
         * ��ʾ�Ƿ������
         */
        private boolean transferData() throws Exception {
            int writeSizeZeroTimes = 0;
            // Write Header
            while (this.byteBufferHeader.hasRemaining()) {
                int writeSize = this.socketChannel.write(this.byteBufferHeader);
                if (writeSize > 0) {
                    writeSizeZeroTimes = 0;
                    this.lastWriteTimestamp =
                            HAConnection.this.haService.getDefaultMessageStore().getSystemClock().now();
                }
                else if (writeSize == 0) {
                    if (++writeSizeZeroTimes >= 3) {
                        break;
                    }
                }
                else {
                    throw new Exception("ha master write header error < 0");
                }
            }

            if (null == this.selectMapedBufferResult) {
                return !this.byteBufferHeader.hasRemaining();
            }

            writeSizeZeroTimes = 0;

            // Write Body
            if (!this.byteBufferHeader.hasRemaining()) {
                while (this.selectMapedBufferResult.getByteBuffer().hasRemaining()) {
                    int writeSize = this.socketChannel.write(this.selectMapedBufferResult.getByteBuffer());
                    if (writeSize > 0) {
                        writeSizeZeroTimes = 0;
                        this.lastWriteTimestamp =
                                HAConnection.this.haService.getDefaultMessageStore().getSystemClock().now();
                    }
                    else if (writeSize == 0) {
                        if (++writeSizeZeroTimes >= 3) {
                            break;
                        }
                    }
                    else {
                        throw new Exception("ha master write body error < 0");
                    }
                }
            }

            boolean result =
                    !this.byteBufferHeader.hasRemaining()
                            && !this.selectMapedBufferResult.getByteBuffer().hasRemaining();

            if (!this.selectMapedBufferResult.getByteBuffer().hasRemaining()) {
                this.selectMapedBufferResult.release();
                this.selectMapedBufferResult = null;
            }

            return result;
        }


        @Override
        public String getServiceName() {
            return WriteSocketService.class.getSimpleName();
        }


        @Override
        public void shutdown() {
            super.shutdown();
        }
    }


    public HAConnection(final HAService haService, final SocketChannel socketChannel) throws IOException {
        this.haService = haService;
        this.socketChannel = socketChannel;
        this.clientAddr = this.socketChannel.socket().getRemoteSocketAddress().toString();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.socket().setSoLinger(false, -1);
        this.socketChannel.socket().setTcpNoDelay(true);
        this.socketChannel.socket().setReceiveBufferSize(1024 * 64);
        this.socketChannel.socket().setSendBufferSize(1024 * 64);
        this.writeSocketService = new WriteSocketService(this.socketChannel);
        this.readSocketService = new ReadSocketService(this.socketChannel);
        this.haService.getConnectionCount().incrementAndGet();
    }


    public void start() {
        this.readSocketService.start();
        this.writeSocketService.start();
    }


    public void shutdown() {
        this.writeSocketService.shutdown(true);
        this.readSocketService.shutdown(true);
        this.close();
    }


    public SocketChannel getSocketChannel() {
        return socketChannel;
    }


    public void close() {
        if (this.socketChannel != null) {
            try {
                this.socketChannel.close();
            }
            catch (IOException e) {
                HAConnection.log.error("", e);
            }
        }
    }
}
