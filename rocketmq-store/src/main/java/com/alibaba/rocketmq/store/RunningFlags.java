/**
 * $Id: RunningFlags.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

/**
 * �洢ģ�����й��̵�״̬λ
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public class RunningFlags {
    // ��ֹ��Ȩ��
    private static final int NotReadableBit = 1;
    // ��ֹдȨ��
    private static final int NotWriteableBit = 1 << 1;
    // �߼������Ƿ�������
    private static final int WriteLogicsQueueErrorBit = 1 << 2;
    // �����ļ��Ƿ�������
    private static final int WriteIndexFileErrorBit = 1 << 3;
    // ���̿ռ䲻��
    private static final int DiskFullBit = 1 << 4;

    private volatile int flagBits = 0;


    public int getFlagBits() {
        return flagBits;
    }


    public RunningFlags() {
    }


    public boolean isReadable() {
        if ((this.flagBits & NotReadableBit) == 0) {
            return true;
        }

        return false;
    }


    public boolean isWriteable() {
        if ((this.flagBits & (NotWriteableBit | WriteLogicsQueueErrorBit | DiskFullBit | WriteIndexFileErrorBit)) == 0) {
            return true;
        }

        return false;
    }


    public boolean getAndMakeReadable() {
        boolean result = this.isReadable();
        if (!result) {
            this.flagBits &= ~NotReadableBit;
        }
        return result;
    }


    public boolean getAndMakeNotReadable() {
        boolean result = this.isReadable();
        if (result) {
            this.flagBits |= NotReadableBit;
        }
        return result;
    }


    public boolean getAndMakeWriteable() {
        boolean result = this.isWriteable();
        if (!result) {
            this.flagBits &= ~NotWriteableBit;
        }
        return result;
    }


    public boolean getAndMakeNotWriteable() {
        boolean result = this.isWriteable();
        if (result) {
            this.flagBits |= NotWriteableBit;
        }
        return result;
    }


    public void makeLogicsQueueError() {
        this.flagBits |= WriteLogicsQueueErrorBit;
    }


    public boolean isLogicsQueueError() {
        if ((this.flagBits & WriteLogicsQueueErrorBit) == WriteLogicsQueueErrorBit) {
            return true;
        }

        return false;
    }


    public void makeIndexFileError() {
        this.flagBits |= WriteIndexFileErrorBit;
    }


    public boolean isIndexFileError() {
        if ((this.flagBits & WriteIndexFileErrorBit) == WriteIndexFileErrorBit) {
            return true;
        }

        return false;
    }


    /**
     * ����Disk�Ƿ�����
     */
    public boolean getAndMakeDiskFull() {
        boolean result = !((this.flagBits & DiskFullBit) == DiskFullBit);
        this.flagBits |= DiskFullBit;
        return result;
    }


    /**
     * ����Disk�Ƿ�����
     */
    public boolean getAndMakeDiskOK() {
        boolean result = !((this.flagBits & DiskFullBit) == DiskFullBit);
        this.flagBits &= ~DiskFullBit;
        return result;
    }
}
