package com.alibaba.rocketmq.client.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.alibaba.rocketmq.common.constant.LoggerName;


public class ClientLogger {
    private static Logger log;

    static {
        String logConfigFilePath =
                System.getProperty("rocketmq.client.log.configFile",
                    System.getenv("ROCKETMQ_CLIENT_LOG_CONFIGFILE"));
        if (null == logConfigFilePath) {
            // ���Ӧ��û�����ã���ʹ��jar����������
            logConfigFilePath = "logback_rocketmq_client.xml";
        }

        // ��ʼ��Logger
        log = createLogger(LoggerName.ClientLoggerName, logConfigFilePath);
    }


    private static Logger createLogger(final String loggerName, final String logConfigFile) {
        try {
            LoggerContext lc = new LoggerContext();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            // �����ļ��Ѿ������Client Jar��
            configurator.doConfigure(logConfigFile);
            return lc.getLogger(LoggerName.ClientLoggerName);
        }
        catch (Exception e) {
            System.err.println(e);
        }

        return LoggerFactory.getLogger(LoggerName.ClientLoggerName);
    }


    public static Logger getLog() {
        return log;
    }


    public static void setLog(Logger log) {
        ClientLogger.log = log;
    }
}
