package com.alibaba.rocketmq.namesrv.common;

/**
 * @author lansheng.zj@taobao.com
 */
public abstract class MergeResult {
	/**
	 * û�кϲ�����
	 */
	public static final int NOT_MERGE = 0; 
	/**
	 * �ϲ����ݳɹ�
	 */
	public static final int MERGE_SUCCESS = 1;
	/**
	 * �ϲ������г�����Ч����
	 */
	public static final int MERGE_INVALID = 2;
	/**
	 * ϵͳ����
	 */
	public static final int SYS_ERROR = 3;
}
