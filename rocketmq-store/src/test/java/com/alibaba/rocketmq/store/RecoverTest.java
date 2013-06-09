/**
 * $Id: RecoverTest.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.rocketmq.common.message.MessageDecoder;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.store.config.MessageStoreConfig;


public class RecoverTest {
    // ���и���
    private static int QUEUE_TOTAL = 10;
    // �����ĸ�����
    private static AtomicInteger QueueId = new AtomicInteger(0);
    // ����������ַ
    private static SocketAddress BornHost;
    // �洢������ַ
    private static SocketAddress StoreHost;
    // ��Ϣ��
    private static byte[] MessageBody;

    private static final String StoreMessage = "Once, there was a chance for me!aaaaaaaaaaaaaaaaaaaaaaaa";


    public MessageExtBrokerInner buildMessage() {
        MessageExtBrokerInner msg = new MessageExtBrokerInner();
        msg.setTopic("TOPIC_A");
        msg.setTags("TAG1");
        msg.setKeys("Hello");
        msg.setBody(MessageBody);
        msg.setKeys(String.valueOf(System.currentTimeMillis()));
        msg.setQueueId(Math.abs(QueueId.getAndIncrement()) % QUEUE_TOTAL);
        msg.setSysFlag(4);
        msg.setBornTimestamp(System.currentTimeMillis());
        msg.setStoreHost(StoreHost);
        msg.setBornHost(BornHost);

        return msg;
    }


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        StoreHost = new InetSocketAddress(InetAddress.getLocalHost(), 8123);
        BornHost = new InetSocketAddress(InetAddress.getByName("10.232.102.184"), 0);
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private MessageStore storeWrite1;
    private MessageStore storeWrite2;
    private MessageStore storeRead;


    private void destroy() {
        if (storeWrite1 != null) {
            // �رմ洢����
            storeWrite1.shutdown();
            // ɾ���ļ�
            storeWrite1.destroy();
        }

        if (storeWrite2 != null) {
            // �رմ洢����
            storeWrite2.shutdown();
            // ɾ���ļ�
            storeWrite2.destroy();
        }

        if (storeRead != null) {
            // �رմ洢����
            storeRead.shutdown();
            // ɾ���ļ�
            storeRead.destroy();
        }
    }


    public void writeMessage(boolean normal, boolean first) throws Exception {
        System.out.println("================================================================");
        long totalMsgs = 1000;
        QUEUE_TOTAL = 3;

        // ������Ϣ��
        MessageBody = StoreMessage.getBytes();

        MessageStoreConfig messageStoreConfig = new MessageStoreConfig();
        // ÿ������ӳ���ļ�
        messageStoreConfig.setMapedFileSizeCommitLog(1024 * 32);
        // ÿ���߼�ӳ���ļ�
        messageStoreConfig.setMapedFileSizeConsumeQueue(100 * 20);
        messageStoreConfig.setMessageIndexEnable(false);

        MessageStore messageStore = new DefaultMessageStore(messageStoreConfig);
        if (first) {
            this.storeWrite1 = messageStore;
        }
        else {
            this.storeWrite2 = messageStore;
        }

        // ��һ����load��������
        boolean loadResult = messageStore.load();
        assertTrue(loadResult);

        // �ڶ�������������
        messageStore.start();

        // ������������Ϣ
        for (long i = 0; i < totalMsgs; i++) {

            PutMessageResult result = messageStore.putMessage(buildMessage());

            System.out.println(i + "\t" + result.getAppendMessageResult().getMsgId());
        }

        if (normal) {
            // �رմ洢����
            messageStore.shutdown();
        }

        System.out.println("========================writeMessage OK========================================");
    }


    private void veryReadMessage(int queueId, long queueOffset, List<ByteBuffer> byteBuffers) {
        for (ByteBuffer byteBuffer : byteBuffers) {
            MessageExt msg = MessageDecoder.decode(byteBuffer);
            System.out.println("request queueId " + queueId + ", request queueOffset " + queueOffset
                    + " msg queue offset " + msg.getQueueOffset());

            assertTrue(msg.getQueueOffset() == queueOffset);

            queueOffset++;
        }
    }


    public void readMessage(final long msgCnt) throws Exception {
        System.out.println("================================================================");
        QUEUE_TOTAL = 3;

        // ������Ϣ��
        MessageBody = StoreMessage.getBytes();

        MessageStoreConfig messageStoreConfig = new MessageStoreConfig();
        // ÿ������ӳ���ļ�
        messageStoreConfig.setMapedFileSizeCommitLog(1024 * 32);
        // ÿ���߼�ӳ���ļ�
        messageStoreConfig.setMapedFileSizeConsumeQueue(100 * 20);
        messageStoreConfig.setMessageIndexEnable(false);

        storeRead = new DefaultMessageStore(messageStoreConfig);
        // ��һ����load��������
        boolean loadResult = storeRead.load();
        assertTrue(loadResult);

        // �ڶ�������������
        storeRead.start();

        // ������������Ϣ
        long readCnt = 0;
        for (int queueId = 0; queueId < QUEUE_TOTAL; queueId++) {
            for (long offset = 0;;) {
                GetMessageResult result = storeRead.getMessage("TOPIC_A", queueId, offset, 1024 * 1024, null);
                if (result.getStatus() == GetMessageStatus.FOUND) {
                    System.out.println(queueId + "\t" + result.getMessageCount());
                    this.veryReadMessage(queueId, offset, result.getMessageBufferList());
                    offset += result.getMessageCount();
                    readCnt += result.getMessageCount();
                    result.release();
                }
                else {
                    break;
                }
            }
        }

        System.out.println("readCnt = " + readCnt);
        assertTrue(readCnt == msgCnt);

        System.out.println("========================readMessage OK========================================");
    }


    /**
     * �����رպ������ָ���Ϣ����֤�Ƿ�����Ϣ��ʧ
     */
    @Test
    public void test_recover_normally() throws Exception {
        this.writeMessage(true, true);
        Thread.sleep(1000 * 3);
        this.readMessage(1000);
        this.destroy();
    }


    /**
     * �����رպ������ָ���Ϣ�����ٴ�д����Ϣ����֤�Ƿ�����Ϣ��ʧ
     */
    @Test
    public void test_recover_normally_write() throws Exception {
        this.writeMessage(true, true);
        Thread.sleep(1000 * 3);
        this.writeMessage(true, false);
        Thread.sleep(1000 * 3);
        this.readMessage(2000);
        this.destroy();
    }


    /**
     * �쳣�رպ������ָ���Ϣ����֤�Ƿ�����Ϣ��ʧ
     */
    @Test
    public void test_recover_abnormally() throws Exception {
        this.writeMessage(false, true);
        Thread.sleep(1000 * 3);
        this.readMessage(1000);
        this.destroy();
    }


    /**
     * �쳣�رպ������ָ���Ϣ�����ٴ�д����Ϣ����֤�Ƿ�����Ϣ��ʧ
     */
    @Test
    public void test_recover_abnormally_write() throws Exception {
        this.writeMessage(false, true);
        Thread.sleep(1000 * 3);
        this.writeMessage(false, false);
        Thread.sleep(1000 * 3);
        this.readMessage(2000);
        this.destroy();
    }
}
