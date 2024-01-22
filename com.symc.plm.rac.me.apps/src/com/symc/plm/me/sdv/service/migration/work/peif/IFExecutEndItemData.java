package com.symc.plm.me.sdv.service.migration.work.peif;

import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EndItemData;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNodeType;
import com.teamcenter.rac.kernel.TCException;

public class IFExecutEndItemData extends IFExecutDefault {

	private EndItemData endItemData;

	private boolean isNeedToBOMLineAdd = false;
	private boolean isNeedToBOMLineReplace = false;

	private TCComponentBOPLine endItemBOPLine;
	private TCComponentItem endItemItem;
	private TCComponentItemRevision endItemItemRevision;
	private String endItemId;

	private TCComponentBOPLine oldEndItemBOPLine;
	private TCComponentItem oldEndItemItem;
	private TCComponentItemRevision oldEndItemItemRevision;

	public IFExecutEndItemData(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	public boolean createOrUpdate(TCData endItemData) {

		this.endItemData = (EndItemData) endItemData;

		endItemBOPLine = null;
		isNeedToBOMLineAdd = false;
		isNeedToBOMLineReplace = false;

		endItemItem = null;
		endItemItemRevision = null;

		super.createOrUpdate(endItemData);
		initTargetItem();

		boolean isUpdateTarget = false;

		int changeType = this.endItemData.getDecidedChagneType();
		boolean bomChanged = this.endItemData.getBOMAttributeChangeFlag();
		boolean attributeChanged = this.endItemData.getAttributeChangeFlag();
		
		if (changeType == TCData.DECIDED_NO_CHANGE && bomChanged==false) {
			// �߰����� ó�� ���� Return
			return true;
		} else if (changeType == TCData.DECIDED_REMOVE) {
			// ���� ó���� ���� �Ѵ�.
			boolean haveRemoveException = false;
			try {
				removeTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
				haveRemoveException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveRemoveException = true;
			}
			return !(haveRemoveException);
		} else if (changeType == TCData.DECIDED_ADD) {
			bomChanged=true;
			attributeChanged=true;
			// �Ʒ��� �߰����� Data Ȯ�ΰ� �ļ�ó���� �����Ѵ�.
		} else if (changeType == TCData.DECIDED_REVISE) {
			// �Ʒ��� �߰����� Data Ȯ�ΰ� �ļ�ó���� �����Ѵ�.
		} else if (changeType == TCData.DECIDED_REPLACE) {
			// �Ʒ��� �߰����� Data Ȯ�ΰ� �ļ�ó���� �����Ѵ�.
		}

		if (this.oldEndItemBOPLine == null) {
			if (endItemItemRevision != null) {
				isNeedToBOMLineAdd = true;
			}
		}

		if (changeType == TCData.DECIDED_ADD) {
			isNeedToBOMLineAdd = true;
			isUpdateTarget = true;
		} else if (changeType == TCData.DECIDED_REPLACE) {
			isNeedToBOMLineReplace = true;
			isUpdateTarget = true;
		} else if (changeType == TCData.DECIDED_REVISE) {
			// End Item�� ��� ū �ǹ̰� ����.
			isUpdateTarget = true;
		}
		
		if(bomChanged==true || attributeChanged==true){
			isUpdateTarget = true;
		}

		boolean isOk = true;
		if (this.endItemBOPLine == null && this.operationBOPLine != null) {
			try {
				addBOMLine();
				peIFExecution.waite();
			} catch (Exception e) {
				e.printStackTrace();
				isOk = false;
			}
		}

		if (isUpdateTarget == true) {
			try {
				updateTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return isOk;
	}

	/**
	 * Subsidiary�� ��� Tc�� ���°�쿡�� Subsidiary Item�� ���Ƿ� ���� ���� �ʴ´�. ����
	 * SubSidiary�� ��� Subsidiary�� �߰��ϴ� ��쿡�� Subsidiary�� �̵̹�ϵ� �͵� �߿� ã�Ƽ� �߰��ϴ� �͸�
	 * ������.
	 */
	public void createTargetObject() throws Exception, TCException {
		// End Item�� MBOM�� End Item�� ã�� �Ҵ� �ϴ� ���̹Ƿ� Item ������ BOP ��⿡�� �� �ʿ䰡 ����.
		// �Ҵ� ��� Data�� ������ Exception ó�� �ϸ� �ȴ�.
	}

	/**
	 * �������� ��� Revise�� Interface P/G���� ���� ���� �ʵ��� �Ѵ�.
	 */
	public void reviseTargetObject() throws Exception, TCException {
		// Tool/Equipment/Sub Sidiary/End Item Revise ���� �ʴ´�.
	}

	public void removeTargetObject() throws Exception, TCException {

		if (this.oldEndItemBOPLine != null) {
			if (this.operationBOPLine != null) {
				if (haveWriteAccessRight(this.operationBOPLine)) {
					this.oldEndItemBOPLine.cut();
				}
			}
		}

		this.operationBOPLine.save();
	}

	public void updateTargetObject() throws Exception, TCException {

		boolean isReleased = false;
		boolean isWriteAble = false;
		
		if(this.endItemBOPLine==null){
			return;
		}
		
		try {
			isReleased = SYMTcUtil.isReleased(this.endItemBOPLine);
			// ���� ������ �ִ��� Check �Ѵ�.
			if (isReleased == false) {
				isWriteAble = haveWriteAccessRight(this.endItemBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (isReleased == true) {
			System.out.println("[" + this.endItemData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;
		}else if(isWriteAble == false){
			System.out.println("[" + this.endItemData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;			
		}

		// BOMLine Attribute ����
		Element bomLineDataElement = (Element) this.endItemData
				.getBomLineNode();

		try {
			// Qty = 1
			endItemBOPLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
		} catch (Exception e) {
			String message = "End item quantity set error : "+operationItemId + " -> "+endItemId+" "+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.endItemData.setStatus(TCData.STATUS_ERROR, message);
		}

		// Find No. ���
		String sequenceStr = null;
		if (bomLineDataElement.getElementsByTagName("O") != null) {
			if (bomLineDataElement.getElementsByTagName("O").getLength() > 0) {
				sequenceStr = bomLineDataElement.getElementsByTagName("O")
						.item(0).getTextContent();
			}
		}
		if (sequenceStr == null
				|| (sequenceStr != null && sequenceStr.trim().length() < 1)) {
			sequenceStr = "";
		}
		
		try {
			endItemBOPLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO,
					getPropertyString(sequenceStr) );
		} catch (Exception e) {
			String message = "End item seq set error : "+operationItemId + " -> "+endItemId+" "+e.getMessage();
			this.peIFExecution.writeLogTextLine(message);
			this.endItemData.setStatus(TCData.STATUS_ERROR, message);

		}

		endItemBOPLine.save();
		endItemData.setBopBomLine(endItemBOPLine);

	}

	private void addBOMLine() throws Exception {

		int changeType = this.endItemData.getDecidedChagneType();
		if (changeType == TCData.DECIDED_REMOVE) {
			return;
		}

		boolean isReplaceTarget = false;
		if (this.oldEndItemBOPLine != null) {
			String oldItemId = this.oldEndItemItem.getProperty("item_id");
			String targetItemId = this.endItemData.getItemId();

			if (oldItemId.equalsIgnoreCase(targetItemId) == false) {
				isReplaceTarget = true;
			}
		}

		if (isReplaceTarget == true) {
			// Do Replace
			return;
		} else if (this.oldEndItemBOPLine != null) {
			this.endItemBOPLine = this.oldEndItemBOPLine;
			this.endItemItem = this.endItemBOPLine.getItem();
			this.endItemItemRevision = this.endItemBOPLine.getItemRevision();

			if (this.endItemBOPLine != null) {
				this.endItemData.setBopBomLine(this.endItemBOPLine);
			}
			return;
		}

		// ---------------------------
		// ���� ���ǿ� ���� ���� �ʰ� ������� ���� � ���� ������ ����
		// End Item BOMLine�� ���� ����̹Ƿ�
		// End Item BOMLine�� �߰��� �־�� �Ѵ�.
		// ---------------------------

		// Validation�� ��ģ ��� Product BOMLine�� �����Ǿ� �ִ� ������.
		TCComponentBOMLine productBOMLine = this.endItemData.getProductBomLine();

		String productABSOccId = null;
		if (productBOMLine != null) {
			productABSOccId = productBOMLine
					.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
		} else {
			this.peIFExecution.writeLogTextLine("Product BOMLine Not Found : "+this.operationItemId+" -> "+endItemId +"("+this.endItemData.getAbsOccPuids()+")");
			this.endItemData.setHaveMajorError(true);
			return;
			//throw new Exception("Can't find product BOM Line ..");
		}

		boolean isProcess = true;
		TCComponentBOPLine targetBOMLine = operationBOPLine;
		TCComponentBOMLine[] processBOMLines = getCurrentBOPLine(
				productABSOccId, isProcess, null);
		if (processBOMLines != null && processBOMLines.length > 0) {
			this.endItemBOPLine = (TCComponentBOPLine) processBOMLines[0];
			return;
		}

		if (this.operationBOPLine != null) {

			boolean haveWriteAccess = haveWriteAccessRight(this.operationBOPLine);

			if (haveWriteAccess == true) {
				try {
					this.endItemBOPLine = (TCComponentBOPLine) this.operationBOPLine
							.assignAsChild(productBOMLine, "MEConsumed");
					this.endItemBOPLine.setProperty(
							SDVPropertyConstant.BL_ABS_OCC_ID, productABSOccId);
					this.endItemBOPLine.save();
				} catch (TCException e) {
					e.printStackTrace();
				}
			} else {
				throw new Exception("You need write permissions : "
						+ this.operationBOPLine);
			}
		}

		if (this.endItemBOPLine != null) {
			this.endItemData.setBopBomLine(this.endItemBOPLine);
		}

	}

	private void initTargetItem() {

		this.oldEndItemBOPLine = (TCComponentBOPLine) this.endItemData
				.getBopBomLine();
		if (this.oldEndItemBOPLine == null) {

			// Validation�� ��ģ ��� Product BOMLine�� �����Ǿ� �ִ� ������.
			TCComponentBOMLine productBOMLine = this.endItemData
					.getProductBomLine();

			String productABSOccId = null;
			if (productBOMLine != null) {
				try {
					productABSOccId = productBOMLine
							.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}

			if (productABSOccId != null && productABSOccId.trim().length() > 0) {
				boolean isProcess = true;
				TCComponentBOPLine targetBOMLine = operationBOPLine;
				TCComponentBOMLine[] processBOMLines = getCurrentBOPLine(
						productABSOccId, isProcess, null);
				if (processBOMLines != null && processBOMLines.length > 0) {
					this.oldEndItemBOPLine = (TCComponentBOPLine) processBOMLines[0];
				}
			}
		}

		if (this.oldEndItemBOPLine != null) {
			// Old Data�� ã�Ƽ� �����Ѵ�.
			try {
				this.oldEndItemItemRevision = this.oldEndItemBOPLine
						.getItemRevision();
				this.oldEndItemItem = this.oldEndItemBOPLine.getItem();
				if (this.oldEndItemItem != null) {
					this.endItemId = this.oldEndItemItem
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		int changeType = this.endItemData.getDecidedChagneType();
		if (changeType == TCData.DECIDED_REMOVE) {
			// New�� ���ؼ��� ����� �ʿ� ����.
		} else {
			TCComponentBOMLine currentProductBOMLine = (TCComponentBOMLine) this.endItemData
					.getProductBomLine();
			if (currentProductBOMLine != null) {
				try {
					this.endItemItem = currentProductBOMLine.getItem();
					this.endItemItemRevision = currentProductBOMLine.getItemRevision();
				} catch (TCException e) {
					e.printStackTrace();
				}
				if (this.endItemItem != null) {
					try {
						this.endItemId = this.endItemItem.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
					} catch (TCException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * BOP Line�� Assign�� Part�� BOM Line�߿� ã�Ƽ� ���� Assign�Ѵ�. �̰��� Process������
	 * Product Part�� ã�� �� �ִµ� Product������ Process�� Assign �� Part�� ã�� �� ���°�� ���ȴ�.
	 * -> �̷���찡 �߻��ϴ� ������ ���� �𸥴�.
	 * 
	 * @param operationBOPLine
	 * @param partBOMLinesInProduct
	 * @param assignedBOMLine
	 * @throws TCException
	 */
	private void refreshAssignedPartBOMLine(
			TCComponentBOPLine operationBOPLine,
			TCComponentBOMLine[] partBOMLinesInProduct,
			TCComponentBOMLine assignedBOMLine) throws TCException {

		// --------------------------------------------
		// Test ������ �����غ��� �Լ��� ���� ��뿩�δ� ���� ���� ���� ����
		// ���� ��� ���ΰ� �����Ǹ� �׿� �°� ������ �� Remark ���� �� ����.
		// [2017 01 17] Taeku.Jeong
		// --------------------------------------------

		// IMANComponentBOMWindow productWindow, IMANComponentBOPWindow
		// processWindow
		if (operationBOPLine == null || assignedBOMLine == null
				|| partBOMLinesInProduct == null) {
			return;
		}

		for (int i = 0; i < partBOMLinesInProduct.length; i++) {
			TCComponentBOMLine productBOMLine = partBOMLinesInProduct[i];

			TCComponent tempComp = assignedBOMLine
					.getReferenceProperty("bl_me_refline");
			if (tempComp == null) {
				String idInContextTopLine = "####"; // ���Ƿ� ��������. �����δ� Unique��
													               // Id���� ������ �ʿ���.
				String processIdInContextTopLine = "####"; 	// ���Ƿ� ��������. �����δ�
															                 	// Unique�� Id���� ������
															    				// �ʿ���.

				TCComponentBOPLine newAttachedBOPLine = null;
				
				//---------------------------------
				// Function Ussage (S)
				//---------------------------------
				// newAttachedBOPLine = (TCComponentBOPLine) operationBOPLine.add(productBOMLine, false, "MEConsumed",false);

				// This allows a current BOMLine to be "copied" to a new line as a child or substitute of this line, with an occurrence type.
				// add(TCComponentBOMLine bomLine, boolean asSubstitute, java.lang.String occType)

				// This allows a current BOMLine to be "copied" to a new line as a child or substitute of this line
				// add(TCComponentBOMLine bomLine, boolean asSubstitute)

				// Add a new line as a child of this line
				// add(TCComponentItem item, TCComponentItemRevision rev, TCComponent bv, boolean asSubstitute)

				// Add a new line as a child of this line, specifying the occurrence type.
				// add(TCComponentItem item, TCComponentItemRevision rev, TCComponent bv, boolean asSubstitute, java.lang.String occType)
				//---------------------------------
				// Function Ussage (E)
				//---------------------------------

				newAttachedBOPLine = (TCComponentBOPLine) operationBOPLine
						.assignAsChild(productBOMLine, "MEConsumed");

				newAttachedBOPLine.save();

				TCComponentMEAppearancePathNodeType appPathNodeType = 
						(TCComponentMEAppearancePathNodeType) operationBOPLine
						.getSession().getTypeComponent("MEAppearancePathNode");
				TCComponentMEAppearancePathNode bomLineAppPathNode = appPathNodeType
						.findOrCreateMEAppearancePathNode(productBOMLine);
				newAttachedBOPLine.linkToAppearance(bomLineAppPathNode, false);

				if (newAttachedBOPLine != null) {
					newAttachedBOPLine.setStringProperty("bl_abs_occ_id", processIdInContextTopLine);
					String partInstance = productBOMLine.getProperty("bl_occurrence_name");
					
					if (partInstance != null && partInstance.trim().length() > 0) {
						newAttachedBOPLine.setStringProperty( "bl_occurrence_name", partInstance);
					}
					assignedBOMLine.cut();
				}
			}

			break;
		}

	}

}
