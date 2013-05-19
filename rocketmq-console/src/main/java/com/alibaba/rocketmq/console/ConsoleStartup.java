/**
 * $Id: ConsoleStartup.java 1839 2013-05-16 02:12:02Z shijia.wxr $
 */
package com.alibaba.rocketmq.console;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;

import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.MQVersion;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;


/**
 * Broker�������
 * 
 * @author vintage.wang@gmail.com shijia.wxr@taobao.com
 */
public class ConsoleStartup {

    public static Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("c", "configFile", true, "Console config properties file");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("p", "printConfigItem", false, "Print all config item");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }


    public static void main(String[] args) {
        // ���õ�ǰ����汾�ţ�ÿ�η����汾ʱ����Ҫ�޸�CurrentVersion
        System.setProperty(RemotingCommand.RemotingVersionKey, Integer.toString(MQVersion.CurrentVersion));

        try {
            // ����������
            Options options = MixAll.buildCommandlineOptions(new Options());
            final CommandLine commandLine =
                    MixAll.parseCmdLine("mqconsole", args, buildCommandlineOptions(options), new PosixParser());
            if (null == commandLine) {
                System.exit(-1);
                return;
            }

            // ��ʼ�������ļ�
            final ConsoleConfig consoleConfig = new ConsoleConfig();

            // ��ӡĬ������
            if (commandLine.hasOption('p')) {
                MixAll.printObjectProperties(null, consoleConfig);
                System.exit(0);
            }

            // ָ�������ļ�
            if (commandLine.hasOption('c')) {
                String file = commandLine.getOptionValue('c');
                if (file != null) {
                    InputStream in = new BufferedInputStream(new FileInputStream(file));
                    Properties properties = new Properties();
                    properties.load(in);
                    MixAll.properties2Object(properties, consoleConfig);

                    System.out.println("load config properties file OK, " + file);
                }
            }

            MixAll.properties2Object(MixAll.commandLine2Properties(commandLine), consoleConfig);

            if (null == consoleConfig.getRocketmqHome()) {
                System.out.println("Please set the " + MixAll.ROCKETMQ_HOME_ENV
                        + " variable in your environment to match the location of the RocketMQ installation");
                System.exit(-2);
            }

            // ���ò���
            consoleConfig.setWebRootPath(consoleConfig.getRocketmqHome() + File.separator + "webroot");

            final Logger log = null;

            // ��ӡ��������
            MixAll.printObjectProperties(log, consoleConfig);

            // ��ʼ��������ƶ���
            final ConsoleController controller = new ConsoleController(consoleConfig);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                private volatile boolean hasShutdown = false;
                private AtomicInteger shutdownTimes = new AtomicInteger(0);


                @Override
                public void run() {
                    synchronized (this) {
                        log.info("shutdown hook was invoked, " + this.shutdownTimes.incrementAndGet());
                        if (!this.hasShutdown) {
                            this.hasShutdown = true;
                            long begineTime = System.currentTimeMillis();
                            controller.shutdown();
                            long consumingTimeTotal = System.currentTimeMillis() - begineTime;
                            log.info("shutdown hook over, consuming time total(ms): " + consumingTimeTotal);
                        }
                    }
                }
            }, "ShutdownHook"));

            // ����������ƶ���
            controller.start();
        }
        catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
