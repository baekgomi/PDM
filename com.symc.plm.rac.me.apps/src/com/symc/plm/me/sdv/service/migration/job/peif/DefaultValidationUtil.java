package com.symc.plm.me.sdv.service.migration.job.peif;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

public class DefaultValidationUtil {
	
	protected NewPEIFExecution peIFExecution;

	// Validation ��� ������ ������ COMPARE_RESULT_DIFFERENT�� ������ COMPARE_RESULT_EQUAL��
	// Return �Ѵ�.
	private int compareResultType = 0;
	// Add, Removed, Replaced, Revised ���� �������¸� ����ϴ� ����
	protected int validationResultChangeType = 0;

	// Validation ��� BOMLine Attritute�� ����Ǹ� True�� �����Ѵ�.
	protected boolean isBOMAttributeChanged = false;
	// Validation ��� Item. ItemRevision���� Attribute�� ����Ǹ� True�� �����Ѵ�.
	protected boolean isMasterDataChanged = false;
	// Validation��� �ش� BOMLine�� ItemRevision�� Replaced �Ȱ�� True�� �����Ѵ�.
	protected boolean isReplaced = false;
	
	// Validation ������ �ֿ��� Error�� �ִ°�� True�� �����Ѵ�.
	public boolean haveMajorError = false;

	static int COMPARE_UNPERFORMED = 0;
	static int COMPARE_RESULT_DIFFERENT = 1;
	static int COMPARE_RESULT_EQUAL = 2;

	protected boolean bomLineNotFound = false;
	protected boolean masterDataNotFoundFlag = false;
	protected boolean bomDataNotFoundFlag = false;
	
	/*
	 * �������� �Ҵ� FindNo ��Ģ :
	 * 1 ~ 9 : �Ϲ�����(END-ITEM)
	 * 10 ~ 200 : ����(TOOL)
	 * 210 ~ 500 : ����(EQUIPMENT)
	 * 510 ~ 800 : ������(SUBSIDIARY)
	 */

	public DefaultValidationUtil(NewPEIFExecution peIFExecution) {
		this.peIFExecution = peIFExecution;
	}
	
	/**
	 * Validation�� �����Ҷ� ȣ���ؼ� Validation �ʱⰪ�� �����ϴ� �Լ� �Ź� Validation�� �����Ҷ�����
	 * ȣ�����ش�.
	 */
	public void clearStatusValues() {
		
		compareResultType = COMPARE_UNPERFORMED;
		validationResultChangeType = TCData.DECIDED_NO_CHANGE;

		isBOMAttributeChanged = false;
		isMasterDataChanged = false;
		isReplaced = false;
		haveMajorError = false;
		
	}
	
	/**
	 * Validation ����� Ȯ�� �ϴ� Function
	 * @return
	 */
	public boolean getValidationResult(){
		boolean isValide = true;
		
		if(isBOMAttributeChanged==true || 
				isMasterDataChanged==true || 
				validationResultChangeType!=TCData.DECIDED_NO_CHANGE){
			isValide = false;
		}
		
		return isValide;
	}

	/**
	 * Validation ����� �����Ѵ�.
	 * [COMPARE_UNPERFORMED/COMPARE_RESULT_DIFFERENT/COMPARE_RESULT_EQUAL]
	 * 
	 * @param compareResultType
	 */
	public void setCompareResult(int compareResultType) {
		this.compareResultType = compareResultType;
	}

	/**
	 * Validation ��� ���� Tc�� ����� N/F Data�� ������ �������� �ٸ��� ���� ����� Return �Ѵ�.
	 * 
	 * @return 
	 *         [COMPARE_UNPERFORMED/COMPARE_RESULT_DIFFERENT/COMPARE_RESULT_EQUAL
	 *         ]
	 */
	public int getCompareResult() {
		return compareResultType;
	}

	/**
	 * Validation ������ ��ϵ� BOMLine Attribute ���� ���� ���� �о� Return �Ѵ�.
	 * 
	 * @return true �̸� BOMLine Attribute�� ����Ǿ����� �ǹ��Ѵ�.
	 */
	public boolean isBOMAttributeChanged() {
		return isBOMAttributeChanged;
	}

	/**
	 * Validation ������ ��ϵ� Master Data ���� ���� ���� �о� Return �Ѵ�.
	 * 
	 * @return true �̸� Master Data�� ����Ǿ����� �ǹ��Ѵ�.
	 */
	public boolean isMasterDataChanged() {
		return isMasterDataChanged;
	}

	/**
	 * Validation ��� Item�� ���� ���¸� Return �Ѵ�.
	 * 
	 * @return [DECIDED_NO_CHANGE/DECIDED_ADD/DECIDED_REMOVE/DECIDED_REVISE/
	 *         DECIDED_REPLACE]
	 */
	public int getValidationResultChangeType() {
		return validationResultChangeType;
	}

	/**
	 * Object�� Delete & Add �� �ƴ϶� Replace�� ��� Replace�� �������� Return�Ѵ�.
	 * 
	 * @return Replaced�Ȱ�� True�� Return �Ѵ�.
	 */
	public boolean isReplaced() {
		return isReplaced;
	}

	/**
	 * �־��� �� ���ڿ��� �������� ���� ����� Return �Ѵ�.
	 * 
	 * @param tcDataStr
	 * @param peDataStr
	 * @return
	 */
	public boolean isSame(String tcDataStr, String peDataStr) {
		boolean isSame = false;

		tcDataStr = checkValueNullOrBlank(tcDataStr);
		peDataStr = checkValueNullOrBlank(peDataStr);

		if ((tcDataStr == null || (tcDataStr != null && tcDataStr.trim()
				.length() < 1))
				|| (peDataStr == null || (peDataStr != null && peDataStr.trim()
						.length() < 1))) {
			isSame = true;
		}

		if ((tcDataStr != null && tcDataStr.trim().length() > 0)
				&& (peDataStr != null && peDataStr.trim().length() > 0)) {
			if (tcDataStr.trim().equalsIgnoreCase(peDataStr.trim()) == true) {
				isSame = true;
			}
		}

		return isSame;
	}

	/**
	 * ���ڿ��� ���� null�ΰ�� Blank���ڸ� �׷��� ���� ��� �ش� ���ڿ��� Trim�ؼ� Return �Ѵ�. ���ڿ� �� ���Ǽ���
	 * ���� �ʿ��� �Լ�
	 * 
	 * @param inputString
	 * @return
	 */
	public String checkValueNullOrBlank(String inputString) {
		String outputString = "";

		if (inputString == null) {
			return outputString;
		}

		if (inputString != null && inputString.trim().length() < 1) {
			return outputString;
		}

		if (inputString.trim().equalsIgnoreCase("NULL") == true) {
			return outputString;
		}

		outputString = inputString.trim();

		return outputString;
	}

	/**
	 * Element�� Child Node�� �־��� Element �̸��� ���� Node�� ã�� ���� ó������ �߰ߵ� Node�� Text��
	 * Return �Ѵ�.
	 * 
	 * @param dataNode
	 *            �������� XML Node
	 * @param elementName
	 *            ã���� �ϴ� Element �̸�
	 * @return ù��° ã�� Node�� Text ���� Return �Ѵ�.
	 */
	public String getNodeFirstChildElementText(Node dataNode, String elementName) {
		String nfDataStr = "";

		if (dataNode == null) {
			return "";
		}

		elementName = elementName.trim();

		Element targetElement = (Element) dataNode;
		if (targetElement.getElementsByTagName(elementName) != null) {
			if (targetElement.getElementsByTagName(elementName).getLength() > 0) {
				nfDataStr = ((Element) dataNode)
						.getElementsByTagName(elementName).item(0)
						.getTextContent();
			}
		}

		nfDataStr = checkValueNullOrBlank(nfDataStr);

		return nfDataStr;
	}

	/**
	 * �־��� TCComponent�� ���ڿ� Property ���� �о Return �Ѵ�. �̶� Property Value�� null
	 * �̸� Blank ���ڸ� Return �Ѵ�. �̰��� N/F Data�� ���� �񱳸� ���ϰ� �ϱ� �����̴�.
	 * 
	 * @param targetComponent
	 * @param propertyName
	 * @return
	 */
	public String getTCComponentStringPropertyValue(
			TCComponent targetComponent, String propertyName) {
		String propertyStrValue = "";

		if (targetComponent == null) {
			return propertyStrValue;
		}

		if (propertyName == null
				|| (propertyName != null && propertyName.trim().length() < 1)) {
			return propertyStrValue;
		}

		if (propertyName != null
				&& propertyName.trim().equalsIgnoreCase("NULL") == true) {
			return propertyStrValue;
		}

		try {
			propertyStrValue = targetComponent.getProperty(propertyName.trim());
		} catch (TCException e) {
			e.printStackTrace();
		}
		propertyStrValue = checkValueNullOrBlank(propertyStrValue);

		return propertyStrValue;
	}

	/**
	 * Validation ����� Validation Dialog�� Text ����� ���� UI�� ǥ�� ���ִ� �Լ�.
	 * 
	 * @param operationItemId
	 * @param targetType
	 * @param targetItemId
	 * @param validationName
	 * @param validationResult
	 */
	public void printValidationMessageWhenFalse(String operationItemId,
			String targetType, String targetItemId, String validationName,
			boolean validationResult) {

		if (validationResult == true) {
			return;
		}

		String message = "[" + operationItemId + "] " + targetItemId + "("
				+ targetType + ") : " + validationName + " => "
				+ validationResult;
		peIFExecution.writeLogTextLine(message);
	}

}
