package com.symc.plm.me.sdv.service.migration.job.peif;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;

public class SubsidiaryValidationUtil extends DefaultValidationUtil {

	public SubsidiaryValidationUtil(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean isValide(TCComponentBOMLine tcSubsidiaryBOMLine,
			Node subsidiaryBOMLineDataNode) {

		boolean isValide = true;

		clearStatusValues();

		if (tcSubsidiaryBOMLine == null) {
			bomLineNotFound = true;
		}
		if (subsidiaryBOMLineDataNode == null) {
			bomDataNotFoundFlag = true;
		}

		// ----------------------------------------------------
		// �� ���ؿ� ���� �ϳ��� �� �� ������.
		// ----------------------------------------------------

		// BOMLine Data�� �д´�.
		String tcItemId = "";
		String tcItemRevId = "";
		String tcOptionCondStr = "";
		String tcDayShiftSortatoin = "";

		String tcQuantityStr = "";
		String tcSequenceStr = "";

		if (tcSubsidiaryBOMLine != null) {
			try {
				tcItemId = tcSubsidiaryBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tcItemRevId = tcSubsidiaryBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
				// ������ Option�� Parent�� Option�� �����´�.
				tcOptionCondStr = tcSubsidiaryBOMLine.parent().getProperty(
						SDVPropertyConstant.BL_OCC_MVL_CONDITION);
				tcDayShiftSortatoin = tcSubsidiaryBOMLine
						.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
				tcQuantityStr = tcSubsidiaryBOMLine
						.getProperty(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY);
				tcSequenceStr = tcSubsidiaryBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}

		}

		// N/F Data�� �д´�.
		String nfSubsidiaryItemId = getNodeFirstChildElementText(
				subsidiaryBOMLineDataNode, "K");
		String nfOptionCondStr = getNodeFirstChildElementText(
				subsidiaryBOMLineDataNode, "L");

		if (tcSubsidiaryBOMLine != null && nfOptionCondStr != null
				&& nfOptionCondStr.trim().length() > 0) {
			try {
				TCComponentBOMWindow window = tcSubsidiaryBOMLine.window();
				nfOptionCondStr = ImportCoreService.conversionOptionCondition(
						window, nfOptionCondStr);
			} catch (TCException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String nfQuantityStr = getNodeFirstChildElementText(
				subsidiaryBOMLineDataNode, "M");
		String nfDayShiftSortatoinStr = getNodeFirstChildElementText(
				subsidiaryBOMLineDataNode, "N");
		String nfSubsidiarySequenceStr = getNodeFirstChildElementText(
				subsidiaryBOMLineDataNode, "O");

		// Validation ����� ����ϴµ� �ʿ��� Data ����
		String operationItemId = null;
		if (subsidiaryBOMLineDataNode != null) {
			operationItemId = ((Element) subsidiaryBOMLineDataNode)
					.getAttribute("OperationItemId");
		}
		String targetType = "Subsidiary";
		String targetItemId = null;
		if (tcItemId != null) {
			targetItemId = tcItemId;
		}
		if (targetItemId == null) {
			targetItemId = nfSubsidiaryItemId;
		}

		// -------------------------------------
		// Node �߰����� ���θ� Ȯ��
		// -------------------------------------
		if (tcSubsidiaryBOMLine == null) {
			// �߰��� �����
			validationResultChangeType = TCData.DECIDED_ADD;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		} else {
			if (subsidiaryBOMLineDataNode != null) {
				// ���泻�� �� ���.
			} else {
				// ������ �����.
				validationResultChangeType = TCData.DECIDED_REMOVE;
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
				isValide = false;
			}
		}

		// �߰� �Ǵ� ������ Activity�� ��� ���̻� �񱳴� ���ǹ���.
		if (validationResultChangeType == TCData.DECIDED_REMOVE
				|| validationResultChangeType == TCData.DECIDED_ADD) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			return isValide;
		}

		// ----------------------------------------------------
		// �� ���ؿ� ���� �ϳ��� �� �� ������.
		// ----------------------------------------------------

		// 1. �̸�
		boolean isSameId = true;
		if (isSame(tcItemId, nfSubsidiaryItemId) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameId = false;
			isValide = false;
			isBOMAttributeChanged = true;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Item Id", isSameId);

		// 2. Sequence (���ڷ� �����ؼ� �� �Ѵ�.)
		boolean isSameSeq = true;
		double tcSeqValue = -1;
		double nfSeqValue = -1;

		if (tcSequenceStr != null && tcSequenceStr.trim().length() > 0
				&& tcSequenceStr.trim().equalsIgnoreCase("NULL") == false) {
			try {
				tcSeqValue = Double.parseDouble(tcSequenceStr.trim());
			} catch (java.lang.NumberFormatException e) {
			}
		}

		if (nfSubsidiarySequenceStr != null
				&& nfSubsidiarySequenceStr.trim().length() > 0
				&& nfSubsidiarySequenceStr.trim().equalsIgnoreCase("NULL") == false) {

			String tempValueStr = ImportCoreService
					.conversionSubsidiaryFindNo(nfSubsidiarySequenceStr);
			try {
				nfSeqValue = Double.parseDouble(tempValueStr);
			} catch (java.lang.NumberFormatException e) {
			}
		}

		if (tcSeqValue > -1 && tcSeqValue != nfSeqValue) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameSeq = false;
			isValide = false;
			isBOMAttributeChanged = true;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Sequence", isSameSeq);

		if (isSameSeq == true && isSameId == false) {
			validationResultChangeType = TCData.DECIDED_REPLACE;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
			isReplaced = true;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Subsidiary Replace", !(isReplaced));

		// 3. Option Compare
		boolean isSameOption = true;
		if (isSame(tcOptionCondStr, nfOptionCondStr) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameOption = false;
			isValide = false;
			isBOMAttributeChanged = true;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Option Condition", isSameOption);

		// 4. Quantity (���ڷ� �����ؼ� �� �Ѵ�.)
		double tcQuantityValue = 0.0;
		double nfQuantityValue = 0.0;
		boolean isSameQuantity = true;
		if (tcQuantityStr != null && tcQuantityStr.trim().length() > 0
				&& tcQuantityStr.trim().equalsIgnoreCase("NULL") == false) {
			tcQuantityValue = Double.parseDouble(tcQuantityStr.trim());
		}
		if (nfQuantityStr != null && nfQuantityStr.trim().length() > 0
				&& nfQuantityStr.trim().equalsIgnoreCase("NULL") == false) {
			nfQuantityValue = Double.parseDouble(nfQuantityStr.trim());
		}
		if (tcQuantityValue != nfQuantityValue) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameQuantity = false;
			isValide = false;
			isBOMAttributeChanged = true;

			String kkMessage = "tcQuantityValue=" + tcQuantityValue
					+ ", nfQuantityValue=" + nfQuantityValue;
			peIFExecution.writeLogTextLine(kkMessage);
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Quantity", isSameQuantity);

		// 5. �� ���� Compare
		boolean isSameDayShift = true;
		if (isSame(tcDayShiftSortatoin, nfDayShiftSortatoinStr) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isSameDayShift = false;
			isValide = false;
			isBOMAttributeChanged = true;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Day Shift", isSameDayShift);

		if(isValide==true){
			if (getCompareResult() != DefaultValidationUtil.COMPARE_RESULT_DIFFERENT) {
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_EQUAL);
				validationResultChangeType = TCData.DECIDED_NO_CHANGE;
			}
		}

		return isValide;
	}

}
