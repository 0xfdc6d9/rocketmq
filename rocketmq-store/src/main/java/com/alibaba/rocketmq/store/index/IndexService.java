/**
 * $Id: IndexService.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.Message;
import com.alibaba.rocketmq.common.ServiceThread;
import com.alibaba.rocketmq.common.UtilALl;
import com.alibaba.rocketmq.common.logger.LoggerName;
import com.alibaba.rocketmq.common.sysflag.MessageSysFlag;
import com.alibaba.rocketmq.store.DefaultMessageStore;
import com.alibaba.rocketmq.store.DispatchRequest;


/**
 * ��Ϣ��������
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class IndexService extends ServiceThread {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.StoreLoggerName);

    private LinkedBlockingQueue<Object[]> requestQueue = new LinkedBlockingQueue<Object[]>();
    private AtomicInteger requestCount = new AtomicInteger(0);

    private final DefaultMessageStore defaultMessageStore;

    // ��������
    private final int hashSlotNum;
    private final int indexNum;
    private final String storePath;

    // �����ļ�����
    private final ArrayList<IndexFile> indexFileList = new ArrayList<IndexFile>();
    // ��д�������indexFileList��
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    public IndexService(final DefaultMessageStore store) {
        this.defaultMessageStore = store;
        this.hashSlotNum = store.getMessageStoreConfig().getMaxHashSlotNum();
        this.indexNum = store.getMessageStoreConfig().getMaxIndexNum();
        this.storePath = store.getMessageStoreConfig().getStorePathIndex();
    }


    public boolean load(final boolean lastExitOK) {
        File dir = new File(this.storePath);
        File[] files = dir.listFiles();
        if (files != null) {
            // ascending order
            Arrays.sort(files);
            for (File file : files) {
                try {
                    IndexFile f = new IndexFile(file.getPath(), this.hashSlotNum, this.indexNum, 0, 0);
                    f.load();

                    if (!lastExitOK) {
                        if (f.getEndTimestamp() > this.defaultMessageStore.getStoreCheckpoint()
                            .getIndexMsgTimestamp()) {
                            f.destroy(0);
                            continue;
                        }
                    }

                    log.info("load index file OK, " + f.getFileName());
                    this.indexFileList.add(f);
                }
                catch (IOException e) {
                    log.error("load file " + file + " error", e);
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * ��ȡ���һ�������ļ����������Ϊ�ջ������һ���ļ�д���ˣ����½�һ���ļ�<br>
     * ֻ��һ���̵߳��ã����Բ�����д��������
     */
    public IndexFile getAndCreateLastIndexFile() {
        IndexFile indexFile = null;
        IndexFile prevIndexFile = null;
        long lastUpdateEndPhyOffset = 0;
        long lastUpdateIndexTimestamp = 0;
        // �ȳ���ʹ�ö���
        {
            this.readWriteLock.readLock().lock();
            if (!this.indexFileList.isEmpty()) {
                IndexFile tmp = this.indexFileList.get(this.indexFileList.size() - 1);
                if (!tmp.isWriteFull()) {
                    indexFile = tmp;
                }
                else {
                    lastUpdateEndPhyOffset = tmp.getEndPhyOffset();
                    lastUpdateIndexTimestamp = tmp.getEndTimestamp();
                    prevIndexFile = tmp;
                }
            }

            this.readWriteLock.readLock().unlock();
        }

        // ���û�ҵ���ʹ��д�������ļ�
        if (indexFile == null) {
            try {
                String fileName =
                        this.storePath + File.separator
                                + UtilALl.timeMillisToHumanString(System.currentTimeMillis());
                indexFile =
                        new IndexFile(fileName, this.hashSlotNum, this.indexNum, lastUpdateEndPhyOffset,
                            lastUpdateIndexTimestamp);
                this.readWriteLock.writeLock().lock();
                this.indexFileList.add(indexFile);
            }
            catch (Exception e) {
                log.error("getLastIndexFile exception ", e);
            }
            finally {
                this.readWriteLock.writeLock().unlock();
            }

            // ÿ����һ�����ļ���֮ǰ�ļ�Ҫˢ��
            if (indexFile != null) {
                final IndexFile flushThisFile = prevIndexFile;
                Thread flushThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        IndexService.this.flush(flushThisFile);
                    }
                }, "FlushIndexFileThread");

                flushThread.setDaemon(true);
                flushThread.start();
            }
        }

        return indexFile;
    }


    /**
     * ɾ���ļ�ֻ�ܴ�ͷ��ʼɾ
     */
    private void deleteExpiredFile(List<IndexFile> files) {
        if (!files.isEmpty()) {
            try {
                this.readWriteLock.writeLock().lock();
                for (IndexFile file : files) {
                    if (!this.indexFileList.remove(file)) {
                        log.error("deleteExpiredFile remove failed.");
                        break;
                    }
                }
            }
            catch (Exception e) {
                log.error("deleteExpiredFile has exception.", e);
            }
            finally {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }


    /**
     * ɾ�������ļ�
     */
    public void deleteExpiredFile(long offset) {
        Object[] files = null;
        try {
            this.readWriteLock.readLock().lock();
            if (this.indexFileList.isEmpty()) {
                return;
            }

            long endPhyOffset = this.indexFileList.get(0).getEndPhyOffset();
            if (endPhyOffset < offset) {
                files = this.indexFileList.toArray();
            }
        }
        catch (Exception e) {
            log.error("destroy exception", e);
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }

        if (files != null) {
            List<IndexFile> fileList = new ArrayList<IndexFile>();
            for (int i = 0; i < (files.length - 1); i++) {
                IndexFile f = (IndexFile) files[i];
                if (f.getEndPhyOffset() < offset) {
                    fileList.add(f);
                }
                else {
                    break;
                }
            }

            this.deleteExpiredFile(fileList);
        }
    }


    public void destroy() {
        try {
            this.readWriteLock.readLock().lock();
            for (IndexFile f : this.indexFileList) {
                f.destroy(1000 * 3);
            }
            this.indexFileList.clear();
        }
        catch (Exception e) {
            log.error("destroy exception", e);
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }
    }


    public void flush(final IndexFile f) {
        if (null == f)
            return;

        long indexMsgTimestamp = 0;

        if (f.isWriteFull()) {
            indexMsgTimestamp = f.getEndTimestamp();
        }

        f.flush();

        this.defaultMessageStore.getStoreCheckpoint().setIndexMsgTimestamp(indexMsgTimestamp);
        this.defaultMessageStore.getStoreCheckpoint().flush();
    }


    // public void flush() {
    // ArrayList<IndexFile> indexFileListClone = null;
    // try {
    // this.readWriteLock.readLock().lock();
    // indexFileListClone = (ArrayList<IndexFile>) this.indexFileList.clone();
    // }
    // catch (Exception e) {
    // log.error("flush exception", e);
    // }
    // finally {
    // this.readWriteLock.readLock().unlock();
    // }
    //
    // long indexMsgTimestamp = 0;
    //
    // if (indexFileListClone != null) {
    // for (IndexFile f : indexFileListClone) {
    // if (f.isWriteFull()) {
    // indexMsgTimestamp = f.getEndTimestamp();
    // }
    //
    // f.flush();
    // }
    // }
    //
    // this.defaultMessageStore.getStoreCheckpoint().setIndexMsgTimestamp(indexMsgTimestamp);
    // this.defaultMessageStore.getStoreCheckpoint().flush();
    // }

    public QueryOffsetResult queryOffset(String topic, String key, int maxNum, long begin, long end) {
        List<Long> phyOffsets = new ArrayList<Long>(maxNum);
        // TODO ������Ҫ���ظ������û�
        long indexLastUpdateTimestamp = 0;
        long indexLastUpdatePhyoffset = 0;
        maxNum = Math.min(maxNum, this.defaultMessageStore.getMessageStoreConfig().getMaxMsgsNumBatch());
        try {
            this.readWriteLock.readLock().lock();
            if (!this.indexFileList.isEmpty()) {
                for (int i = this.indexFileList.size(); i > 0; i--) {
                    IndexFile f = this.indexFileList.get(i - 1);
                    boolean lastFile = i == this.indexFileList.size();
                    if (lastFile) {
                        indexLastUpdateTimestamp = f.getEndTimestamp();
                        indexLastUpdatePhyoffset = f.getEndPhyOffset();
                    }

                    if (f.isTimeMatched(begin, end)) {
                        // ���һ���ļ���Ҫ����
                        f.selectPhyOffset(phyOffsets, this.buildKey(topic, key), maxNum, begin, end, lastFile);
                    }

                    // ����ǰ����ʱ���������
                    if (f.getBeginTimestamp() > end) {
                        break;
                    }

                    if (phyOffsets.size() >= maxNum) {
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("queryMsg exception", e);
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }

        return new QueryOffsetResult(phyOffsets, indexLastUpdateTimestamp, indexLastUpdatePhyoffset);
    }


    /**
     * ׷�����󣬷��ض����жѻ���������
     */
    public int putRequest(final Object[] reqs) {
        this.requestQueue.add(reqs);
        return this.requestCount.addAndGet(reqs.length);
    }


    private String buildKey(final String topic, final String key) {
        return topic + "#" + key;
    }


    public IndexFile retryGetAndCreateIndexFile() {
        IndexFile indexFile = null;

        // �������ʧ�ܣ������ؽ�3��
        for (int times = 0; null == indexFile && times < 3; times++) {
            indexFile = this.getAndCreateLastIndexFile();
            if (null != indexFile)
                break;

            try {
                log.error("try to create index file, " + times + " times");
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // ���Զ�Σ���Ȼ�޷����������ļ�
        if (null == indexFile) {
            this.defaultMessageStore.getAccessRights().makeIndexFileError();
            log.error("mark index file can not build flag");
        }

        return indexFile;
    }


    public void buildIndex(Object[] req) {
        boolean breakdown = false;
        IndexFile indexFile = retryGetAndCreateIndexFile();
        if (indexFile != null) {
            long endPhyOffset = indexFile.getEndPhyOffset();
            MSG_WHILE: for (Object o : req) {
                DispatchRequest msg = (DispatchRequest) o;
                String topic = msg.getTopic();
                String keys = msg.getKeys();
                if (msg.getCommitLogOffset() < endPhyOffset) {
                    continue;
                }

                final int tranType = MessageSysFlag.getTransactionValue(msg.getSysFlag());
                switch (tranType) {
                case MessageSysFlag.TransactionNotType:
                case MessageSysFlag.TransactionPreparedType:
                    break;
                case MessageSysFlag.TransactionCommitType:
                case MessageSysFlag.TransactionRollbackType:
                    continue;
                }

                if (keys != null && keys.length() > 0) {
                    String[] keyset = keys.split(Message.KEY_SEPARATOR);
                    for (String key : keyset) {
                        // TODO �Ƿ���ҪTRIM
                        if (key.length() > 0) {
                            for (boolean ok =
                                    indexFile.putKey(buildKey(topic, key), msg.getCommitLogOffset(),
                                        msg.getStoreTimestamp()); !ok;) {
                                log.warn("index file full, so create another one, " + indexFile.getFileName());
                                indexFile = retryGetAndCreateIndexFile();
                                if (null == indexFile) {
                                    breakdown = true;
                                    break MSG_WHILE;
                                }

                                ok =
                                        indexFile.putKey(buildKey(topic, key), msg.getCommitLogOffset(),
                                            msg.getStoreTimestamp());
                            }
                        }
                    }
                }
            }
        }
        // IO�������ϣ�build���������жϣ���Ҫ�˹����봦��
        else {
            breakdown = true;
        }

        if (breakdown) {
            log.error("build index error, stop building index");
            // TODO
        }

        this.requestCount.addAndGet(req.length * (-1));
    }


    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStoped()) {
            try {
                // Object[] req = this.requestQueue.take();
                Object[] req = this.requestQueue.poll(3000, TimeUnit.MILLISECONDS);

                if (req != null) {
                    this.buildIndex(req);
                }
            }
            catch (Exception e) {
                log.warn(this.getServiceName() + " service has exception. ", e);
            }
        }

        log.info(this.getServiceName() + " service end");
    }


    @Override
    public String getServiceName() {
        return IndexService.class.getSimpleName();
    }
}
