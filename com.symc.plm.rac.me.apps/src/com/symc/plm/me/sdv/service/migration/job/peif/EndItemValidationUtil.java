package com.symc.plm.me.sdv.service.migration.job.peif;

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;

public class EndItemValidationUtil extends DefaultValidationUtil {
	
	private String operationItemId;
	private TCComponentBOPLine processEndItemBOPLine;

	private TCComponentBOMLine mBOMPartBOMLine;
	private String partCopyStableOccThreadUid;
	private String parentCopyStableOccThreadUid;
	private String combinedProductPartCopyStableOccThreadUid;
	private Element endItemNFBOMLineNodeData;

	public EndItemValidationUtil(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	private boolean isValide() {

		boolean isValide = true;

		if (this.processEndItemBOPLine == null) {
			bomLineNotFound = true;
		}
		if (endItemNFBOMLineNodeData == null) {
			bomDataNotFoundFlag = true;
		}

		// -------------------------------------
		// Node �߰����� ���θ� Ȯ��
		// -------------------------------------
		if (this.processEndItemBOPLine == null) {
			// �߰��� �����
			validationResultChangeType = TCData.DECIDED_ADD;
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		} else {
			if (endItemNFBOMLineNodeData == null) {
				// ������ �����.
				validationResultChangeType = TCData.DECIDED_REMOVE;
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
				isValide = false;
			}
		}

		// �߰� �Ǵ� ������ Activity�� ��� ���̻� �񱳴� ���ǹ���.
		if (isValide == false) {
			return isValide;
		}

		// ----------------------------------------------------
		// �� ���ؿ� ���� �ϳ��� �� �� ������.
		// ----------------------------------------------------

		// BOMLine Data�� �д´�.
		String tcItemId = "";
		String tcItemRevId = "";
		String tcSequenceStr = "";
		String tcQuantityStr = "";

		if (this.processEndItemBOPLine != null) {
			try {
				tcItemId = this.processEndItemBOPLine.getProperty(
						SDVPropertyConstant.BL_ITEM_ID);
				tcItemRevId = this.processEndItemBOPLine.getProperty(
						SDVPropertyConstant.BL_ITEM_REV_ID);
				tcSequenceStr = this.processEndItemBOPLine.getProperty(
						SDVPropertyConstant.BL_SEQUENCE_NO);
				tcQuantityStr = this.processEndItemBOPLine.getProperty(
						SDVPropertyConstant.BL_QUANTITY);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		// N/F Data�� �д´�.
		String nfEndItemId = getNodeFirstChildElementText(
				endItemNFBOMLineNodeData, "K");
		String nfEndItemSequenceStr = getNodeFirstChildElementText(
				endItemNFBOMLineNodeData, "O");

		String targetType = "End Item";
		String targetItemId = null;
		if (tcItemId != null) {
			targetItemId = tcItemId;
		}
		if (targetItemId == null) {
			targetItemId = nfEndItemId;
		}

		// ----------------------------------------------------
		// �� ���ؿ� ���� �ϳ��� �� �� ������.
		// ----------------------------------------------------

		// 1. �̸�
		boolean isSameId = true;
		if (isSame(tcItemId, nfEndItemId) == false) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameId = false;
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Part Id", isSameId);

		// 2. Sequence (���ڷ� �����ؼ� �� �Ѵ�.)
		boolean isSameSeq = true;
		double tcSeqValue = -1;
		double nfSeqValue = -1;

		if (tcSequenceStr != null && tcSequenceStr.trim().length() > 0
				&& tcSequenceStr.trim().equalsIgnoreCase("NULL") == false) {
			tcSeqValue = Double.parseDouble(tcSequenceStr.trim());
		}
		if (nfEndItemSequenceStr != null
				&& nfEndItemSequenceStr.trim().length() > 0
				&& nfEndItemSequenceStr.trim().equalsIgnoreCase("NULL") == false) {
			nfSeqValue = Double.parseDouble(nfEndItemSequenceStr.trim());
		}
		if (tcSeqValue > -1 && tcSeqValue != nfSeqValue) {
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isBOMAttributeChanged = true;
			isSameSeq = false;
			isValide = false;
			String kkMessage = "tcSeqValue=" + tcSeqValue + ", nfSeqValue="
					+ nfSeqValue;
			peIFExecution.writeLogTextLine(kkMessage);
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Sequence", isSameSeq);

		if (isSameSeq == true && isSameId == false) {
			isReplaced = true;
			validationResultChangeType = TCData.DECIDED_REPLACE;   // Tree�� "* * C ..." ���·� ǥ���.
			setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
			isValide = false;
		}
		printValidationMessageWhenFalse(operationItemId, targetType, targetItemId,
				"Part Replace", !(isReplaced));

		// //3. Quantity (���ڷ� �����ؼ� �� �Ѵ�.)
		// double tcQuantityValue = 0.0;
		// double nfQuantityValue = 0.0;
		// boolean isSameQuantity = true;
		// if(tcQuantityStr!=null && tcQuantityStr.trim().length()>0 &&
		// tcQuantityStr.trim().equalsIgnoreCase("NULL")==false ){
		// tcQuantityValue = Double.parseDouble(tcQuantityStr.trim());
		// }
		// if(nfEndItemQuantityStr!=null &&
		// nfEndItemQuantityStr.trim().length()>0 &&
		// nfEndItemQuantityStr.trim().equalsIgnoreCase("NULL")==false){
		// nfQuantityValue = Double.parseDouble(nfEndItemQuantityStr.trim());
		// }
		// if(tcQuantityValue!=nfQuantityValue){
		// setCompareResult(DefaultValidationUtil.COMPARE_RESULT_DIFFERENT);
		// isBOMAttributeChanged = true;
		// isSameQuantity = false;
		// }
		// printAbleValidationMessage(operationItemId, targetType, targetItemId,
		// "Quantity", isSameQuantity);

		if(isValide==true){
			if (getCompareResult() != DefaultValidationUtil.COMPARE_RESULT_DIFFERENT) {
				setCompareResult(DefaultValidationUtil.COMPARE_RESULT_EQUAL);
				validationResultChangeType = TCData.DECIDED_NO_CHANGE;
			}
		}

		return isValide;
	}
	
	/**
	 * Teamcenter�� �ִ� BOPLine�� �������� Validation �ϴ� ���
	 * @param processEndItemBOPLine
	 * @param operationItemId
	 * @return
	 */
	public boolean isValide(TCComponentBOPLine processEndItemBOPLine, String operationItemId){
		
		clearStatusValues();
		
		this.mBOMPartBOMLine = null;
		this.partCopyStableOccThreadUid = null;
		this.parentCopyStableOccThreadUid = null;
		this.combinedProductPartCopyStableOccThreadUid = null;
		this.endItemNFBOMLineNodeData = null;
		
		this.operationItemId = operationItemId;
		this.processEndItemBOPLine = processEndItemBOPLine;
		
		String endItemABSOccId = null;
		boolean searchAllContext = true;
		TCComponentBOMLine[] productBOMLines = null;
		
		if (peIFExecution.getProductWindow() != null) {
			try {
				endItemABSOccId = this.processEndItemBOPLine.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
				if(endItemABSOccId!=null && endItemABSOccId.trim().length()>0){
					productBOMLines = peIFExecution.getProductWindow().findConfigedBOMLinesForAbsOccID(
							endItemABSOccId,
							searchAllContext, 
							peIFExecution.getProductLine());
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		if(productBOMLines!=null && productBOMLines.length>0){
			this.mBOMPartBOMLine = productBOMLines[0];
		}
		
		// Product End Item�� CopyStableOccThreadUid �� �д´�.
		if(this.mBOMPartBOMLine!=null){
			
			this.partCopyStableOccThreadUid = NewPEIFExecution.getOccThreadUid(this.mBOMPartBOMLine);

			try {
				this.parentCopyStableOccThreadUid = NewPEIFExecution.getOccThreadUid(this.mBOMPartBOMLine.parent());
			} catch (TCException e) {
				e.printStackTrace();
			}
			
			if(this.parentCopyStableOccThreadUid!=null && partCopyStableOccThreadUid!=null){
				this.combinedProductPartCopyStableOccThreadUid = this.parentCopyStableOccThreadUid.trim()+"+"+partCopyStableOccThreadUid.trim();
			}
		}
		
		// ----------------------------------------------------------------------------------
		// N/F���� EndItemBOMLine Node ��ü�� XPATH�� �̿��� Query �Ѵ�.
		// ----------------------------------------------------------------------------------
		this.endItemNFBOMLineNodeData = (Element)
				peIFExecution.getPeImportDataReaderUtil().getOperationChildEndItemBOMLineNode(
						this.operationItemId,
						this.combinedProductPartCopyStableOccThreadUid, 
						this.partCopyStableOccThreadUid);
		
		return isValide();
	}
	
	/**
	 * Teamcenter�� �ִ� BOPLine�� �������� Validation �ϴ� ���
	 * @param processEndItemBOPLine
	 * @param operationItemId
	 * @return
	 */
	public boolean isValide(Element endItemNfBOMLineNode, String operationItemId){

		clearStatusValues();
		this.mBOMPartBOMLine = null;
		this.partCopyStableOccThreadUid = null;
		this.parentCopyStableOccThreadUid = null;
		this.combinedProductPartCopyStableOccThreadUid = null;
		this.endItemNFBOMLineNodeData = null;
		
		this.operationItemId = operationItemId;
		this.endItemNFBOMLineNodeData = endItemNfBOMLineNode;
		
		TCComponentBOMWindow productWindow = this.peIFExecution.getProductWindow();
		String productId = null;
		if(productWindow!=null){
			try {
				productId = productWindow.getTopBOMLine().getProperty(SDVPropertyConstant.BL_ITEM_ID);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		// Attribute�� �д´�.
		String productPartItemId = null;
		
		// OccThread Puid���� �д� �κ�
		if (this.endItemNFBOMLineNodeData.getElementsByTagName("K") != null) {
			productPartItemId = this.endItemNFBOMLineNodeData
					.getElementsByTagName("K").item(0).getTextContent();
		}
		// OccThread Puid���� �д� �κ�
		if (this.endItemNFBOMLineNodeData.getElementsByTagName("L") != null) {
			this.combinedProductPartCopyStableOccThreadUid = this.endItemNFBOMLineNodeData
					.getElementsByTagName("L").item(0).getTextContent();
		}
		if (this.endItemNFBOMLineNodeData.getElementsByTagName("M") != null) {
			this.partCopyStableOccThreadUid = this.endItemNFBOMLineNodeData
					.getElementsByTagName("M").item(0).getTextContent();
		}

		if (this.combinedProductPartCopyStableOccThreadUid != null) {
			
			String[] tempString = combinedProductPartCopyStableOccThreadUid.split("\\+");
			if(tempString!=null && tempString.length>1){
				this.parentCopyStableOccThreadUid = tempString[0];
			}else{
				this.parentCopyStableOccThreadUid = this.combinedProductPartCopyStableOccThreadUid.trim();
			}
		}
		
		// Nf Data�� ���� ���� �̿��� Product�� End Item BOMLine�� ã�´�.
		ArrayList<String> productEndItemABSOccPuidList = 
				peIFExecution.getPeImportDataReaderUtil().getABSOccPuidString(
						productId, 
						this.partCopyStableOccThreadUid,
						this.parentCopyStableOccThreadUid);
		if(productEndItemABSOccPuidList==null){
			this.peIFExecution.writeLogTextLine("1 st : null : "+this.partCopyStableOccThreadUid+", "+this.parentCopyStableOccThreadUid);
		}else{
			this.peIFExecution.writeLogTextLine("1 st : "+ productEndItemABSOccPuidList.size()+", "+this.partCopyStableOccThreadUid+", "+this.parentCopyStableOccThreadUid);
		}
		
		if(productEndItemABSOccPuidList==null || 
				(productEndItemABSOccPuidList!=null && productEndItemABSOccPuidList.size()<1)
				){
			productEndItemABSOccPuidList = 
					peIFExecution.getPeImportDataReaderUtil().getABSOccPuidString(
							productId, 
							this.partCopyStableOccThreadUid,
							(String)null);
			
			if(productEndItemABSOccPuidList==null){
				this.peIFExecution.writeLogTextLine("2 nd : null"+this.partCopyStableOccThreadUid+", "+this.parentCopyStableOccThreadUid);
			}else{
				this.peIFExecution.writeLogTextLine("2 nd : "+ productEndItemABSOccPuidList.size()+", "+this.partCopyStableOccThreadUid+", "+this.parentCopyStableOccThreadUid);
			}
		}

		String productABSOccId = null;
		if(productEndItemABSOccPuidList!=null && productEndItemABSOccPuidList.size()>0){
			this.mBOMPartBOMLine = getMatchedProductEndItemBOMLine(productEndItemABSOccPuidList);
		}
		
		if(this.mBOMPartBOMLine!=null){
			try {
				productABSOccId = this.mBOMPartBOMLine.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
//		else{
//			// Function�� �������� ������ BOMLine�� ã�ƾ� �Ѵ�.
//			// Function �������� ã�����ؼ��� Function�� Product �������� ã�� �� �־�� �Ѵ�.
//			// Function���� Part�� ã�ƾ�...
//			// �׷��� �̰� 
//		}
		
		TCComponentBOMLine[] findedProcessPartBOMLines = null;
		if(productABSOccId!=null){
			try {
				findedProcessPartBOMLines = peIFExecution.getProcessWindow().findConfigedBOMLinesForAbsOccID(
						productABSOccId, 
						true, 
						peIFExecution.getProcessLine());
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		for (int i = 0; findedProcessPartBOMLines!=null && i < findedProcessPartBOMLines.length; i++) {
			TCComponentBOMLine tempBOMLine = findedProcessPartBOMLines[i];
			if(tempBOMLine==null){
				continue;
			}
			
			TCComponentBOMLine tempParentBOMLine = null;
			try {
				tempParentBOMLine = tempBOMLine.parent();
			} catch (TCException e) {
				e.printStackTrace();
			}
			if(tempParentBOMLine==null){
				continue;
			}
			
			String tempItemId = null;
			try {
				tempItemId = tempParentBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			} catch (TCException e) {
				e.printStackTrace();
			}
			if(tempItemId!=null && operationItemId!=null && tempItemId.trim().equalsIgnoreCase(operationItemId)==true ){
				this.processEndItemBOPLine = (TCComponentBOPLine)tempBOMLine;
				break;
			}
		}
		
		// ������� �ϸ� �ϴ� Product BOMLIne�� ã�� ������.
		
		return isValide();
	}
	
	/**
	 * Part Parent Node CopyStableOccThreadUid �� Part CopyStableOccThreadUid�� �̿��� ã�� ABS OCC ID ���߿�
	 * ���� Product Structure�� �����ϴ� Occurrence�� �ش��ϴ� BOMLine�� �ϳ� ã�Ƽ� Return �Ѵ�.
	 * �̶� ã�� ��� BOMLine�� ������ �����ϴ��� ���� ó�� ã�� �� �ϳ��� Return �Ѵ�.   
	 * @param absOccPuidList
	 * @return
	 */
	private TCComponentBOMLine getMatchedProductEndItemBOMLine(
			ArrayList<String> absOccPuidList) {

		TCComponentBOMLine findedProductBOMLine = null;

		for (int i = 0; absOccPuidList != null && i < absOccPuidList.size(); i++) {
			String absOccId = absOccPuidList.get(i);

			// bl_occurrence_name (IdInContext TopLevel)�� �̿��ؼ� ã�¹��
			boolean searchAllContext = true;
			TCComponentBOMLine[] findedPartBOMLines = null;
			try {
				findedPartBOMLines = 
						peIFExecution.getProductWindow().findConfigedBOMLinesForAbsOccID(
							absOccId,
							searchAllContext, 
							peIFExecution.getProductLine()
						);

			} catch (TCException e) {
				e.printStackTrace();
			}

			if(findedPartBOMLines != null && findedPartBOMLines.length>0){
				findedProductBOMLine = findedPartBOMLines[0];
				break;
			}

		}

		return findedProductBOMLine;
	}
			
	public TCComponentBOPLine getProcessEndItemBOPLine() {
		return processEndItemBOPLine;
	}

	public TCComponentBOMLine getmBOMPartBOMLine() {
		return mBOMPartBOMLine;
	}

	public Element getEndItemNFBOMLineNodeData() {
		return endItemNFBOMLineNodeData;
	}
	
	public String getPartCopyStableOccThreadUid() {
		return partCopyStableOccThreadUid;
	}

	public String getParentCopyStableOccThreadUid() {
		return parentCopyStableOccThreadUid;
	}

}
