/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.store;

/**
 * ���������д����Ϣ���ؽ��
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
 */
public class AppendMessageResult {
    // ������
    private AppendMessageStatus status;
    // �����￪ʼд��
    private long wroteOffset;
    // д���ֽ���
    private int wroteBytes;
    // ��ϢID
    private String msgId;
    // ��Ϣ�洢ʱ��
    private long storeTimestamp;
    // д���߼����е�offset���ݽ�1��
    private long logicsOffset;


    public AppendMessageResult(AppendMessageStatus status, long wroteOffset, int wroteBytes, String msgId,
            long storeTimestamp, long logicsOffset) {
        this.status = status;
        this.wroteOffset = wroteOffset;
        this.wroteBytes = wroteBytes;
        this.msgId = msgId;
        this.storeTimestamp = storeTimestamp;
        this.logicsOffset = logicsOffset;
    }


    public boolean isOk() {
        return this.status == AppendMessageStatus.PUT_OK;
    }


    public AppendMessageResult(AppendMessageStatus status) {
        this(status, 0, 0, "", 0, 0);
    }


    public AppendMessageStatus getStatus() {
        return status;
    }


    public void setStatus(AppendMessageStatus status) {
        this.status = status;
    }


    public long getWroteOffset() {
        return wroteOffset;
    }


    public void setWroteOffset(long wroteOffset) {
        this.wroteOffset = wroteOffset;
    }


    public int getWroteBytes() {
        return wroteBytes;
    }


    public void setWroteBytes(int wroteBytes) {
        this.wroteBytes = wroteBytes;
    }


    public String getMsgId() {
        return msgId;
    }


    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }


    public long getStoreTimestamp() {
        return storeTimestamp;
    }


    public void setStoreTimestamp(long storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }


    public long getLogicsOffset() {
        return logicsOffset;
    }


    public void setLogicsOffset(long logicsOffset) {
        this.logicsOffset = logicsOffset;
    }
}
