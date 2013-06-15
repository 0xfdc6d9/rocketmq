package com.alibaba.rocketmq.common.help;

/**
 * ��¼һЩ�����Ӧ�Ľ�����������ٴ��ɹ�����
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class FAQUrl {
    // FAQ: Topic��������ν��
    public static final String APPLY_TOPIC_URL = //
            "https://github.com/alibaba/RocketMQ/issues/55";

    // FAQ: ͬһ̨�����޷��������ʵ�����ڶ��JVM�����У�
    public static final String CLIENT_INSTACNCE_NAME_DUPLICATE_URL = //
            "https://github.com/alibaba/RocketMQ/issues/56";

    // FAQ: Name Server��ַ������
    public static final String NAME_SERVER_ADDR_NOT_EXIST_URL = //
            "https://github.com/alibaba/RocketMQ/issues/57";

    // FAQ: ����Producer��Consumerʧ�ܣ�Group Name�ظ�
    public static final String GROUP_NAME_DUPLICATE_URL = //
            "https://github.com/alibaba/RocketMQ/issues/63";

    // FAQ: �ͻ��˶������У��Ϸ���
    public static final String CLIENT_PARAMETER_CHECK_URL = //
            "https://github.com/alibaba/RocketMQ/issues/73";

    //
    // FAQ: δ��¼�쳣����취
    //
    public static final String UNEXPECTED_EXCEPTION_URL = //
            "https://github.com/alibaba/RocketMQ/issues/64";

    private static final String TipString = "\nFor more infomation, please visit the url, ";


    public static String suggestTodo(final String url) {
        StringBuilder sb = new StringBuilder();
        sb.append(TipString);
        sb.append(url);
        return sb.toString();
    }


    /**
     * ����û��δ�쳣ԭ��ָ��FAQ�������׷��Ĭ��FAQ
     */
    public static String attachDefaultURL(final String errorMessage) {
        if (errorMessage != null) {
            int index = errorMessage.indexOf(TipString);
            if (-1 == index) {
                StringBuilder sb = new StringBuilder();
                sb.append(errorMessage);
                sb.append("\n");
                sb.append("For inquiries, please visit the url, ");
                sb.append(UNEXPECTED_EXCEPTION_URL);
                return sb.toString();
            }
        }

        return errorMessage;
    }
}
