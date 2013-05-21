/**
 * $Id: TransactionStateService.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store.transaction;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.Message;
import com.alibaba.rocketmq.common.MessageExt;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.sysflag.MessageSysFlag;
import com.alibaba.rocketmq.store.ConsumeQueue;
import com.alibaba.rocketmq.store.DefaultMessageStore;
import com.alibaba.rocketmq.store.MapedFile;
import com.alibaba.rocketmq.store.MapedFileQueue;
import com.alibaba.rocketmq.store.SelectMapedBufferResult;


/**
 * ������񣬴洢ÿ�������״̬��Prepared��Commited��Rollbacked��<br>
 * ���ʽ��ͣ�<br>
 * clOffset - Commit Log Offset<br>
 * tsOffset - Transaction State Table Offset
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 * 
 */
public class TransactionStateService {
    private static final Logger log = LoggerFactory.getLogger(MixAll.StoreLoggerName);
    // �洢��Ԫ��С
    public static final int TSStoreUnitSize = 24;
    // �����ָ�����״̬���redolog
    public static final String TRANSACTION_REDOLOG_TOPIC = "TRANSACTION_REDOLOG_TOPIC_XXXX";
    public static final int TRANSACTION_REDOLOG_TOPIC_QUEUEID = 0;
    // �洢�������
    private final DefaultMessageStore defaultMessageStore;
    // �洢����״̬�ı��
    private MapedFileQueue tranStateTable;
    // �ظ������ڴ�Buffer
    private final ByteBuffer byteBufferAppend = ByteBuffer.allocate(TSStoreUnitSize);
    // ����״̬��Redolog������������崵�����ͨ��redolog�ָ����������״̬
    // Redolog��ʵ�����������Ѷ��У���ҪΪ�˻ָ�����
    private final ConsumeQueue tranRedoLog;
    public final static long PreparedMessageTagsCode = -1;

    // ��������״̬���������λ��
    private final static int TS_STATE_POS = 20;

    // State Table Offset������ʱ���������
    private final AtomicLong tranStateTableOffset = new AtomicLong(0);

    // ��ʱ�ز��߳�
    private final Timer timer = new Timer("CheckTransactionMessageTimer", true);


    public TransactionStateService(final DefaultMessageStore defaultMessageStore) {
        this.defaultMessageStore = defaultMessageStore;
        this.tranStateTable =
                new MapedFileQueue(defaultMessageStore.getMessageStoreConfig().getTranStateTableStorePath(),
                    defaultMessageStore.getMessageStoreConfig().getTranStateTableMapedFileSize(), null);

        this.tranRedoLog = new ConsumeQueue(//
            TRANSACTION_REDOLOG_TOPIC,//
            TRANSACTION_REDOLOG_TOPIC_QUEUEID,//
            defaultMessageStore.getMessageStoreConfig().getTranRedoLogStorePath(),//
            defaultMessageStore.getMessageStoreConfig().getTranRedoLogMapedFileSize(),//
            defaultMessageStore);
    }


    public boolean load() {
        boolean result = this.tranRedoLog.load();
        result = result && this.tranStateTable.load();

        return result;
    }


    private void initTimerTask() {
        final List<MapedFile> mapedFiles = this.tranStateTable.getMapedFiles();
        for (MapedFile mf : mapedFiles) {
            this.addTimerTask(mf);
        }
    }


    public void start() {
        this.initTimerTask();
    }


    public void shutdown() {
    }


    public int deleteExpiredStateFile(long offset) {
        int cnt = this.tranStateTable.deleteExpiredFileByOffset(offset, TSStoreUnitSize);
        return cnt;
    }


    public void recoverStateTable(final boolean lastExitOK) {
        if (lastExitOK) {
            this.recoverStateTableNormal();
        }
        else {
            // ��һ����ɾ��State Table
            this.tranStateTable.destroy();
            // �ڶ�����ͨ��RedoLogȫ���ָ�StateTable
            this.recreateStateTable();
        }
    }


    private void recreateStateTable() {
        this.tranStateTable =
                new MapedFileQueue(defaultMessageStore.getMessageStoreConfig().getTranStateTableStorePath(),
                    defaultMessageStore.getMessageStoreConfig().getTranStateTableMapedFileSize(), null);

        final TreeSet<Long> preparedItemSet = new TreeSet<Long>();

        // ��һ������ͷɨ��RedoLog
        final long minOffset = this.tranRedoLog.getMinOffsetInQuque();
        long processOffset = minOffset;
        while (true) {
            SelectMapedBufferResult bufferConsumeQueue = this.tranRedoLog.getIndexBuffer(processOffset);
            if (bufferConsumeQueue != null) {
                try {
                    long i = 0;
                    for (; i < bufferConsumeQueue.getSize(); i += ConsumeQueue.CQStoreUnitSize) {
                        long offsetMsg = bufferConsumeQueue.getByteBuffer().getLong();
                        int sizeMsg = bufferConsumeQueue.getByteBuffer().getInt();
                        long tagsCode = bufferConsumeQueue.getByteBuffer().getLong();

                        // Prepared
                        if (TransactionStateService.PreparedMessageTagsCode == tagsCode) {
                            preparedItemSet.add(offsetMsg);
                        }
                        // Commit/Rollback
                        else {
                            preparedItemSet.remove(tagsCode);
                        }
                    }

                    processOffset += i;
                }
                finally {
                    // �����ͷ���Դ
                    bufferConsumeQueue.release();
                }
            }
            else {
                break;
            }
        }

        log.info("scan transaction redolog over, End offset: {},  Prepared Transaction Count: {}", processOffset,
            preparedItemSet.size());
        // �ڶ������ؽ�StateTable
        Iterator<Long> it = preparedItemSet.iterator();
        while (it.hasNext()) {
            Long offset = it.next();
            MessageExt msgExt = this.defaultMessageStore.lookMessageByOffset(offset);
            if (msgExt != null) {
                this.appendPreparedTransaction(msgExt.getCommitLogOffset(), msgExt.getStoreSize(), (int) (msgExt
                    .getStoreTimestamp() / 1000), msgExt.getProperty(Message.PROPERTY_PRODUCER_GROUP).hashCode());
            }
        }
    }


    private void recoverStateTableNormal() {
        final List<MapedFile> mapedFiles = this.tranStateTable.getMapedFiles();
        if (!mapedFiles.isEmpty()) {
            // �ӵ����������ļ���ʼ�ָ�
            int index = mapedFiles.size() - 3;
            if (index < 0)
                index = 0;

            int mapedFileSizeLogics = this.tranStateTable.getMapedFileSize();
            MapedFile mapedFile = mapedFiles.get(index);
            ByteBuffer byteBuffer = mapedFile.sliceByteBuffer();
            long processOffset = mapedFile.getFileFromOffset();
            long mapedFileOffset = 0;
            while (true) {
                for (int i = 0; i < mapedFileSizeLogics; i += TSStoreUnitSize) {
                    long offset = byteBuffer.getLong();
                    int size = byteBuffer.getInt();
                    long tagsCode = byteBuffer.getLong();

                    // ˵����ǰ�洢��Ԫ��Ч
                    // TODO �����ж���Ч�Ƿ����
                    if (offset >= 0 && size > 0) {
                        mapedFileOffset = i + TSStoreUnitSize;
                    }
                    else {
                        log.info("recover current logics file over,  " + mapedFile.getFileName() + " " + offset
                                + " " + size + " " + tagsCode);
                        break;
                    }
                }

                // �ߵ��ļ�ĩβ���л�����һ���ļ�
                if (mapedFileOffset == mapedFileSizeLogics) {
                    index++;
                    if (index >= mapedFiles.size()) {
                        // ��ǰ������֧�����ܷ���
                        log.info("recover last logics file over, last maped file " + mapedFile.getFileName());
                        break;
                    }
                    else {
                        mapedFile = mapedFiles.get(index);
                        byteBuffer = mapedFile.sliceByteBuffer();
                        processOffset = mapedFile.getFileFromOffset();
                        mapedFileOffset = 0;
                        log.info("recover next logics file, " + mapedFile.getFileName());
                    }
                }
                else {
                    log.info("recover current logics queue over " + mapedFile.getFileName() + " "
                            + (processOffset + mapedFileOffset));
                    break;
                }
            }

            processOffset += mapedFileOffset;
            this.tranStateTable.truncateDirtyFiles(processOffset);
        }
    }


    private void addTimerTask(final MapedFile mf) {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            private final MapedFile mapedFile = mf;
            private final TransactionCheckExecuter transactionCheckExecuter =
                    TransactionStateService.this.defaultMessageStore.getTransactionCheckExecuter();


            private long getTranStateOffset(final long currentIndex) {
                long offset = this.mapedFile.getFileFromOffset() / TransactionStateService.TSStoreUnitSize;
                offset += currentIndex;
                return offset;
            }


            @Override
            public void run() {
                try {
                    SelectMapedBufferResult selectMapedBufferResult = mapedFile.selectMapedBuffer(0);
                    // TODO �ز�������ʱʱ����Ҫ�Ż�
                    if (selectMapedBufferResult != null) {
                        try {
                            for (long i = 0; i < selectMapedBufferResult.getSize(); i += TSStoreUnitSize) {
                                // Commit Log Offset
                                long clOffset = selectMapedBufferResult.getByteBuffer().getLong();
                                // Message Size
                                int msgSize = selectMapedBufferResult.getByteBuffer().getInt();
                                // Timestamp
                                int timestamp = selectMapedBufferResult.getByteBuffer().getInt();
                                // Producer Group Hashcode
                                int groupHashCode = selectMapedBufferResult.getByteBuffer().getInt();
                                // Transaction State
                                int tranType = selectMapedBufferResult.getByteBuffer().getInt();

                                long timestampLong = timestamp * 1000;
                                // TODO �жϼ��ָ��ʱ����ٻز�
                                if (tranType != MessageSysFlag.TransactionPreparedType) {
                                    continue;
                                }

                                try {
                                    this.transactionCheckExecuter.gotoCheck(//
                                        groupHashCode,//
                                        getTranStateOffset(i),//
                                        clOffset,//
                                        msgSize);
                                }
                                catch (Exception e) {
                                    log.warn("", e);
                                }
                            }
                        }
                        finally {
                            selectMapedBufferResult.release();
                        }
                    }

                    if (mapedFile.isFull()) {
                        // ������Prepared����

                        // this.cancel();
                    }
                }
                catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 1000 * 60, 1000 * 60 * 3);
    }


    /**
     * ���̵߳���
     */
    public boolean appendPreparedTransaction(//
            final long clOffset,//
            final int size,//
            final int timestamp,//
            final int groupHashCode//
    ) {
        MapedFile mapedFile = this.tranStateTable.getLastMapedFile();
        if (null == mapedFile) {
            log.error("appendPreparedTransaction: create mapedfile error.");
            return false;
        }

        // �״δ��������붨ʱ������
        if (0 == mapedFile.getWrotePostion()) {
            this.addTimerTask(mapedFile);
        }

        this.byteBufferAppend.position(0);
        this.byteBufferAppend.limit(TSStoreUnitSize);

        // Commit Log Offset
        this.byteBufferAppend.putLong(clOffset);
        // Message Size
        this.byteBufferAppend.putInt(size);
        // Timestamp
        this.byteBufferAppend.putInt(timestamp);
        // Producer Group Hashcode
        this.byteBufferAppend.putInt(groupHashCode);
        // Transaction State
        this.byteBufferAppend.putInt(MessageSysFlag.TransactionPreparedType);

        return mapedFile.appendMessage(this.byteBufferAppend.array());
    }


    /**
     * ���̵߳���
     */
    public boolean updateTransactionState(//
            final long tsOffset,//
            final long clOffset,//
            final int groupHashCode,//
            final int state//
    ) {
        SelectMapedBufferResult selectMapedBufferResult = this.findTransactionBuffer(tsOffset);
        if (selectMapedBufferResult != null) {
            try {
                final long clOffset_read = selectMapedBufferResult.getByteBuffer().getLong();
                final int size_read = selectMapedBufferResult.getByteBuffer().getInt();
                final int timestamp_read = selectMapedBufferResult.getByteBuffer().getInt();
                final int groupHashCode_read = selectMapedBufferResult.getByteBuffer().getInt();
                final int state_read = selectMapedBufferResult.getByteBuffer().getInt();

                // У��������ȷ��
                if (clOffset != clOffset_read) {
                    log.error("updateTransactionState error clOffset: {} clOffset_read: {}", clOffset,
                        clOffset_read);
                    return false;
                }

                // У��������ȷ��
                if (groupHashCode != groupHashCode_read) {
                    log.error("updateTransactionState error groupHashCode: {} groupHashCode_read: {}",
                        groupHashCode, groupHashCode_read);
                    return false;
                }

                // �ж��Ƿ��Ѿ����¹�
                if (MessageSysFlag.TransactionPreparedType != state_read) {
                    log.warn("updateTransactionState error, the transaction is updated before.");
                    return true;
                }

                // ��������״̬
                selectMapedBufferResult.getByteBuffer().putInt(TS_STATE_POS, state);
            }
            catch (Exception e) {
                log.error("updateTransactionState exception", e);
            }
            finally {
                selectMapedBufferResult.release();
            }
        }

        return false;
    }


    private SelectMapedBufferResult findTransactionBuffer(final long tsOffset) {
        final int mapedFileSize =
                this.defaultMessageStore.getMessageStoreConfig().getTranStateTableMapedFileSize();
        final long offset = tsOffset * TSStoreUnitSize;
        MapedFile mapedFile = this.tranStateTable.findMapedFileByOffset(offset);
        if (mapedFile != null) {
            SelectMapedBufferResult result = mapedFile.selectMapedBuffer((int) (offset % mapedFileSize));
            return result;
        }

        return null;
    }


    public AtomicLong getTranStateTableOffset() {
        return tranStateTableOffset;
    }


    public ConsumeQueue getTranRedoLog() {
        return tranRedoLog;
    }
}
