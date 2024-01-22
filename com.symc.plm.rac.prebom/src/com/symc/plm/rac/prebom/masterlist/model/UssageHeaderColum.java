package com.symc.plm.rac.prebom.masterlist.model;

/**
 * [SR160621-031][20160707] taeku.jeong
 * �ְ� ������ ������ Pre-BOM �����͸� Ȱ���Ͽ� ������ ����� �� �ִ� ��� ����
 */
public class UssageHeaderColum {
	
	public String area = null;
	public String passenger = null;
	public String engine = null;
	public String grade = null;
	public String trim = null;
	public String ussageKey = null;

	/**
	 * Ussage ������ ��Ÿ���� Colum�� ǥ�õ� Titl���� ����ϱ����� Data Class�� ����
	 * @param area
	 * @param passenger
	 * @param engine
	 * @param grade
	 * @param trim
	 * @param ussageKey
	 */
	public UssageHeaderColum(String area, String passenger, String engine, String grade, String trim, String ussageKey){
		
		this.area = area;
		this.passenger = passenger;
		this.engine = engine;
		this.grade = grade;
		this.trim = trim;
		this.ussageKey = ussageKey;
		
	}

	@Override
	public String toString() {
		return this.ussageKey;
	}
	
	
}
