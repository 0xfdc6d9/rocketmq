package com.alibaba.rocketmq.remoting.protocol;

public final class RemotingProtos {
    private RemotingProtos() {
    }

    public enum ResponseCode {
        // �ɹ�
        SUCCESS(0, 0),
        // ������δ�����쳣
        SYSTEM_ERROR(1, 1),
        // �����̳߳�ӵ�£�ϵͳ��æ
        SYSTEM_BUSY(2, 2),
        // ������벻֧��
        REQUEST_CODE_NOT_SUPPORTED(3, 3), ;

        // /////////////////////////////////////////////////////////////////////

        // �ɹ�
        public static final int SUCCESS_VALUE = 0;
        // ������δ�����쳣
        public static final int SYSTEM_ERROR_VALUE = 1;
        // �����̳߳�ӵ�£�ϵͳ��æ
        public static final int SYSTEM_BUSY_VALUE = 2;
        // ������벻֧��
        public static final int REQUEST_CODE_NOT_SUPPORTED_VALUE = 3;


        public static ResponseCode valueOf(int value) {
            switch (value) {
            case 0:
                return SUCCESS;
            case 1:
                return SYSTEM_ERROR;
            case 2:
                return SYSTEM_BUSY;
            case 3:
                return REQUEST_CODE_NOT_SUPPORTED;
            default:
                return null;
            }
        }

        private final int index;
        private final int value;


        public final int getNumber() {
            return value;
        }


        public int getIndex() {
            return index;
        }


        private ResponseCode(int index, int value) {
            this.index = index;
            this.value = value;
        }

    }
}
