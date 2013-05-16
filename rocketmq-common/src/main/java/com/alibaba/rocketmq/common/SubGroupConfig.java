/**
 * $Id: SubGroupConfig.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.common;

/**
 * ����������
 * 
 * @author vintage.wang@gmail.com  shijia.wxr@taobao.com
 */
public class SubGroupConfig {
    private String groupName;
    // 0 ��ʾ master
    // > 0 ��ʾ ����slave
    private int subFromWhere = 0;
    private int perm = MixAll.PERM_READ;


    public SubGroupConfig(String groupName) {
        this.groupName = groupName;
    }


    public String getGroupName() {
        return groupName;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    public int getSubFromWhere() {
        return subFromWhere;
    }


    public void setSubFromWhere(int subFromWhere) {
        this.subFromWhere = subFromWhere;
    }


    public int getPerm() {
        return perm;
    }


    public void setPerm(int perm) {
        this.perm = perm;
    }
}
