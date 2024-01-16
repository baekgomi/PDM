package com.ssangyong.commands.ospec.op;

import java.io.Serializable;
import java.util.ArrayList;

public class OpGroup implements Comparable<OpGroup>, Serializable{
	private String opGroupName = null;
	private boolean isChanged = false;
	private String owner = null;
	private String desciption = null;
	private String condition = null; // OR Condition �߰�
	private ArrayList<OpValueName> optionList = null;
	
	public OpGroup(String opGroupName, String owner){
		this.opGroupName = opGroupName;
		this.owner = owner;
		this.isChanged = true;
	}
	
	/**
	 * 
	 * @param opGroupName
	 * @param owner
	 * @param desciption
	 */
	public OpGroup(String opGroupName, String owner, String desciption, String condition){
		this(opGroupName,owner);
		this.desciption = desciption;
		this.condition = condition;
	}
	
	public String getOpGroupName() {
		return opGroupName;
	}
	public void setOpGroupName(String opGroupName) {
		this.opGroupName = opGroupName;
	}
	public boolean isChanged() {
		return isChanged;
	}
	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public ArrayList<OpValueName> getOptionList() {
		return optionList;
	}
	public void setOptionList(ArrayList<OpValueName> optionList) {
		this.optionList = optionList;
	}
	@Override
	public int compareTo(OpGroup o) {
		return opGroupName.compareTo(o.getOpGroupName());
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return opGroupName;
	}
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof OpGroup){
			OpGroup opGroup = (OpGroup)obj;
			return (owner + "_" + opGroupName).equals( opGroup.getOwner() + "_" + opGroup.getOpGroupName());
		}
		return super.equals(obj);
	}

	/**
	 * @return the desciption
	 */
	public String getDesciption() {
		return desciption;
	}

	/**
	 * @param desciption the desciption to set
	 */
	public void setDesciption(String desciption) {
		this.desciption = desciption;
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	
}
