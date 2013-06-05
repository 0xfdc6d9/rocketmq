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


    public static String suggestTodo(final String url) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("For more infomation, please acccess this url, ");
        sb.append(url);
        return sb.toString();
    }
}
