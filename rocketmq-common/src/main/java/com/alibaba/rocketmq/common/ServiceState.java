/**
 * $Id: ServiceState.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.common;

/**
 * ��������״̬��ͨ����Ҫstart��shutdown
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * 
 */
public enum ServiceState {
    CREATE_JUST,
    RUNNING,
    SHUTDOWN_ALREADY
}
