package com.symc.plm.rac.prebom.masterlist.model;

/**
 * [SR160621-031][20160707] taeku.jeong
 * �ְ� ������ ������ Pre-BOM �����͸� Ȱ���Ͽ� ������ ����� �� �ִ� ��� ����
 */
public class MasterAndUssageFindKey {
	
	public String projectCode = null;
	public String masterEAICreateTime = null;
	public String ospecEAICreateTime = null;
	public String ussageEAICreateTime = null;
	public String eaiCreateTime = null;

	/**
	 * Project List Up�� ���� Data ǥ���� ���� Class
	 * @param projectCode
	 * @param eaiCreateTime
	 * @param masterEAICreateTime
	 * @param ospecEAICreateTime
	 * @param ussageEAICreateTime
	 */
	public MasterAndUssageFindKey(String projectCode, String eaiCreateTime, String masterEAICreateTime,  String ospecEAICreateTime, String ussageEAICreateTime){
		this.projectCode = projectCode;
		this.masterEAICreateTime = masterEAICreateTime;
		this.ospecEAICreateTime = ospecEAICreateTime;
		this.ussageEAICreateTime = ussageEAICreateTime;
		this.eaiCreateTime = eaiCreateTime;
	}
}
