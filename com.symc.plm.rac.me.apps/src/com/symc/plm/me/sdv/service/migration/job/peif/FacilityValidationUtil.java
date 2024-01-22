package com.symc.plm.me.sdv.service.migration.job.peif;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;

public class FacilityValidationUtil extends DefaultValidationUtil {

	public FacilityValidationUtil(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean isValide(TCComponentBOMLine tcFacilityBOMLine,
			Node facilityBOMLineDataNode, Node equipmentMasterDataNode) {

		boolean isValide = true;

		clearStatusValues();

		if (tcFacilityBOMLine == null) {
			bomLineNotFound = true;
		}
		if (facilityBOMLineDataNode == null) {
			bomDataNotFoundFlag = true;
		}
		if (equipmentMasterDataNode == null) {
			masterDataNotFoundFlag = true;
		}

		// ----------------------------------------------------
		// �� ���ؿ� ���� �ϳ��� �� �� ������.
		// ----------------------------------------------------

		// BOMLine Data�� �д´�.
		String tcItemId = "";
		String tcItemRevId = "";
		String tcSequenceStr = "";
		String tcQuantityStr = "";

		if (tcFacilityBOMLine != null) {
			try {
				tcItemId = tcFacilityBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				tcItemRevId = tcFacilityBOMLine
						.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
				tcSequenceStr = tcFacilityBOMLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
				tcQuantityStr = tcFacilityBOMLine
						.getProperty(SDVPropertyConstant.BL_QUANTITY);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		// N/F Data�� �д´�.
		String nfFacilityItemId = getNodeFirstChildElementText(
				facilityBOMLineDataNode, "K");
		String nfFacilityQuantityStr = getNodeFirstChildElementText(
				facilityBOMLineDataNode, "L");
		String nfFacilitySequenceStr = getNodeFirstChildElementText(
				facilityBOMLineDataNode, "M");

		String nfPlantCode = getNodeFirstChildElementText(
				equipmentMasterDataNode, "B");
		String nfFacilityMasterItemId = getNodeFirstChildElementText(
				equipmentMasterDataNode, "C");
		String nfFacilityKorName = getNodeFirstChildElementText(
				equipmentMasterDataNode, "D");
		String nfFacilityEngName = getNodeFirstChildElementText(
				equipmentMasterDataNode, "E");
		String nfFacilityKorUsage = getNodeFirstChildElementText(
				equipmentMasterDataNode, "F");
		String nfFacilityEngUsage = getNodeFirstChildElementText(
				equipmentMasterDataNode, "G");
		String nfFacilityKorSPEC = getNodeFirstChildElementText(
				equipmentMasterDataNode, "H");
		String nfFacilityEngSPEC = getNodeFirstChildElementText(
				equipmentMasterDataNode, "I");
		String nfLargeCategory = getNodeFirstChildElementText(
				equipmentMasterDataNode, "J");
		String nfMiddleCategory = getNodeFirstChildElementText(
				equipmentMasterDataNode, "K");
		String nfProcessingPower = getNodeFirstChildElementText(
				equipmentMasterDataNode, "L");
		String nfMakingCompany = getNodeFirstChildElementText(
				equipmentMasterDataNode, "M");
		String nfCountryOfMarketing = getNodeFirstChildElementText(
				equipmentMasterDataNode, "N");
		String nfDateOfInstallation = getNodeFirstChildElementText(
				equipmentMasterDataNode, "O");
		String nfRevisionHistoryText = getNodeFirstChildElementText(
				equipmentMasterDataNode, "P");
		String nfVehicleTypeCode_JIG = getNodeFirstChildElementText(
				equipmentMasterDataNode, "Q");
		String nfLargeCategory_JIG = getNodeFirstChildElementText(
				equipmentMasterDataNode, "R");
		String nfCADFilePath = getNodeFirstChildElementText(
				equipmentMasterDataNode, "K");

		// -------------------------------------
		// Node �߰����� ���θ� Ȯ��
		// -------------------------------------
		if (tcFacilityBOMLine == null) {
			// �߰��� �����
			validationResultChangeType = TCData.DECIDED_ADD;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		} else {
			if (facilityBOMLineDataNode != null) {
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

		// Validation ����� ����ϴµ� �ʿ��� Data ����
		String operationItemId = null;
		if (facilityBOMLineDataNode != null) {
			operationItemId = ((Element) facilityBOMLineDataNode)
					.getAttribute("OperationItemId");
		}
		if (operationItemId != null
				|| (operationItemId != null && operationItemId.trim().length() < 1)) {
			// if(equipmentMasterDataNode!=null){
			operationItemId = ((Element) equipmentMasterDataNode)
					.getAttribute("OperationItemId");
			// }
		}
		String targetType = "Facility";
		String targetItemId = null;
		if (tcItemId != null) {
			targetItemId = tcItemId;
		}
		if (targetItemId == null) {
			targetItemId = nfFacilityItemId;
		}

		// ----------------------------------------------------
		// �� ���ؿ� ���� �ϳ��� �� �� ������.
		// ----------------------------------------------------

		// 1. �̸�
		boolean isSameId = true;
		if (isSame(tcItemId, nfFacilityItemId) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameId = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Facility Id", isSameId);

		// 2. Sequence (���ڷ� �����ؼ� �� �Ѵ�.)
		double tcSeqValue = -1;
		double nfSeqValue = -1;
		boolean isSameSequence = true;
		if (tcSequenceStr != null && tcSequenceStr.trim().length() > 0
				&& tcSequenceStr.trim().equalsIgnoreCase("NULL") == false) {
			tcSeqValue = Double.parseDouble(tcSequenceStr.trim());
		}
		if (nfFacilitySequenceStr != null
				&& nfFacilitySequenceStr.trim().length() > 0
				&& nfFacilitySequenceStr.trim().equalsIgnoreCase("NULL") == false) {
			nfSeqValue = Double.parseDouble(nfFacilitySequenceStr.trim());
		}
		if (tcSeqValue > -1 && tcSeqValue != nfSeqValue) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameSequence = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Sequence", isSameSequence);

		if (isSameSequence == true && isSameId == false) {
			isReplaced = true;
			validationResultChangeType = TCData.DECIDED_REPLACE;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Facility Replace", !(isReplaced));

		// 3. Quantity (���ڷ� �����ؼ� �� �Ѵ�.)
		double tcQuantityValue = 0.0;
		double nfQuantityValue = 0.0;
		boolean isSameQuantity = true;
		if (tcQuantityStr != null && tcQuantityStr.trim().length() > 0
				&& tcQuantityStr.trim().equalsIgnoreCase("NULL") == false) {
			tcQuantityValue = Double.parseDouble(tcQuantityStr.trim());
		}
		if (nfFacilityQuantityStr != null
				&& nfFacilityQuantityStr.trim().length() > 0
				&& nfFacilityQuantityStr.trim().equalsIgnoreCase("NULL") == false) {
			nfQuantityValue = Double.parseDouble(nfFacilityQuantityStr.trim());
		}
		if (tcQuantityValue != nfQuantityValue) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameQuantity = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Quantity", isSameQuantity);

		if(isValide==true){
			if (getCompareResult() != DefaultValidationUtil.COMPARE_RESULT_DIFFERENT) {
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_EQUAL);
				validationResultChangeType = TCData.DECIDED_NO_CHANGE;
			}
		}

		return isValide;
	}
}
