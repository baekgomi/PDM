package com.symc.plm.me.sdv.service.migration.work.peif;

import org.eclipse.swt.widgets.TreeItem;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.ItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivitySubData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EndItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EquipmentData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SubsidiaryData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ToolData;
import com.teamcenter.rac.eintegrator.ExternalDsAdapter.TCDsAdapter;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.soa.exceptions.NotLoadedException;

public class NEWPEImportDataCreator {

	NewPEIFExecution peIFExecution;

	IFExecutOperationItemData ifExecutOperationItemData;
	IFExecutEndItemData ifExecutEndItemData;
	IFExecutSubsidiaryData ifExecutSubsidiaryData;
	IFExecutEquipmentData ifExecutEquipmentData;
	IFExecutToolData ifExecutToolData;
	IFExecutActivityMasterData ifExecutActivityMasterData;

	/**
	 * 
	 * @param processWindow
	 * @param productWindow
	 * @param mecoItemRev
	 * @param itemLineData
	 * @param peImportDataReaderUtil
	 * @param peIFMigrationViewControll
	 */
	public NEWPEImportDataCreator( NewPEIFExecution peIFExecution ) {
		this.peIFExecution = peIFExecution;
//		this.processWindow = peIFExecution.getProcessWindow();
//		this.productWindow = peIFExecution.getProductWindow();
//		this.mecoItemRev = peIFExecution.getMecoRevision();
//		this.lineItemData = peIFExecution.getItemLineData();
//		this.peImportDataReaderUtil = peIFExecution.getPeImportDataReaderUtil();
//		this.peIFMigrationViewControll = peIFExecution.getPeIFMigrationViewControll();
//		this.mecoNo = peIFExecution.getMecoNo();
	}
	
	public boolean isChangeInterfaceTarget(){
	
		LineItemData lineItemData = peIFExecution.getItemLineData();
		boolean haveChildeRevise = lineItemData.getChildNodeRevisedFlag();
		boolean haveChildeAdded = lineItemData.getChildNodeAddedFlag();
		boolean haveChildeRemoved = lineItemData.getChildNodeRemovedFlag();
		boolean haveChildeReplaced = lineItemData.getChildNodeReplacedFlag();
		
		System.out.println("haveChildeRevise = "+haveChildeRevise);
		System.out.println("haveChildeAdded = "+haveChildeAdded);
		System.out.println("haveChildeRemoved = "+haveChildeRemoved);
		System.out.println("haveChildeReplaced = "+haveChildeReplaced);
		
		boolean isChangeTarget = false;
		if(haveChildeRevise || haveChildeAdded || haveChildeRemoved || haveChildeReplaced){
			isChangeTarget = true;
		}
		
		return isChangeTarget;
	}
	
	/**
	 * ���� Inteface�� ���� �ϱ����� Line�� �����Ǿ� Interface ������ �������� Check �ϴ� �Լ�.
	 * @return
	 */
	public boolean isLineHaveChangeAccessRight(){
		
		boolean isChangeAble = false;
		
		LineItemData lineItemData = peIFExecution.getItemLineData();
		
		TCComponentBOPLine lineBOPLine = (TCComponentBOPLine)lineItemData.getBopBomLine();
		
		System.out.println("lineBOPLine = "+lineBOPLine);
		
		if(lineBOPLine!=null){
			
			boolean isReleased = IFExecutDefault.isReleased(lineBOPLine);
			System.out.println("isReleased = "+isReleased);	
			if(isReleased==true){
				return isChangeAble;
			}
			
			boolean haveWriteAccess = IFExecutDefault.haveWriteAccessRight(lineBOPLine);
			System.out.println("haveWriteAccess = "+haveWriteAccess);	
			if(haveWriteAccess==false){
				return isChangeAble;
			}

			boolean isSameWithTargetMeco = false;
			TCComponentChangeItemRevision changeRevision = (TCComponentChangeItemRevision)peIFExecution.getMecoRevision();
			TCComponentChangeItemRevision[] referencedChangeRevisions = IFExecutDefault.getReferencedChangeItemRevision(lineBOPLine);
			if(referencedChangeRevisions!=null){
				for (int i = 0; referencedChangeRevisions!=null && i < referencedChangeRevisions.length; i++) {
					if(referencedChangeRevisions[i]!=null && referencedChangeRevisions[i].equals(changeRevision)==true){
						isSameWithTargetMeco = true;
						break;
					}
				}
			}else{
				try {
					TCComponentItemRevision lineItemRevision = lineBOPLine.getItemRevision();
					TCComponentChangeItemRevision lineMECORev = 
							(TCComponentChangeItemRevision)lineItemRevision.getReferenceProperty(SDVPropertyConstant.LINE_REV_MECO_NO);
					if(lineMECORev!=null && lineMECORev.equals(changeRevision)==true){
						isSameWithTargetMeco = true;
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			System.out.println("isSameWithTargetMeco = "+isSameWithTargetMeco);
			
			if(isSameWithTargetMeco==true && haveWriteAccess==true && isReleased==false){
				isChangeAble = true;
			}
		}
		
		return isChangeAble;
	}

	public void expandAndUpdateLineItemData() {

		TreeItem[] childNodeTreeItems = peIFExecution.getItemLineData().getItems();

		OperationItemData operationItemData = null;

		ifExecutOperationItemData = new IFExecutOperationItemData(peIFExecution);
		ifExecutEndItemData = new IFExecutEndItemData(peIFExecution);
		ifExecutSubsidiaryData = new IFExecutSubsidiaryData(peIFExecution);
		ifExecutEquipmentData = new IFExecutEquipmentData(peIFExecution);
		ifExecutToolData = new IFExecutToolData(peIFExecution);
		ifExecutActivityMasterData = new IFExecutActivityMasterData(peIFExecution);

		for (int i = 0; childNodeTreeItems != null
				&& i < childNodeTreeItems.length; i++) {
			
			this.peIFExecution.redrawUI();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			TreeItem currentTreeItem = childNodeTreeItems[i];
			
			if (currentTreeItem == null) {
				peIFExecution.waite();
				continue;
			}

			if (currentTreeItem instanceof OperationItemData) {
				operationItemData = (OperationItemData) currentTreeItem;
				
				peIFExecution.getPeIFMigrationViewControll().writeLogTextLine(
						"Execut : "+operationItemData.getItemId());
				
				if(operationItemData.isHaveMajorError()==true){
					peIFExecution.writeLogTextLine(operationItemData.getItemId() +" have Major Exception -> Exception out");
				}else{
					expandOperatoinItemData((OperationItemData) currentTreeItem);
				}
				currentTreeItem.setExpanded(false);
			}
			
			peIFExecution.waite();
			System.gc();
		}

	}

	/**
	 * Operation�� Operation�� �����ϴ� Child Node �鿡 ���� Update �� �߰�/���ŵ��� ���� ������ �ݿ��ϴ�
	 * �Լ�
	 * 
	 * @param operationItemData
	 * @return
	 */
	private OperationItemData expandOperatoinItemData(
			OperationItemData operationItemData) {

		boolean isOk = ifExecutOperationItemData.createOrUpdate(operationItemData);
		if(isOk==false){
			peIFExecution.writeLogTextLine("Error -> "+operationItemData.getItemId()+" [create or update]");
			return operationItemData;
		}
		
		TCComponentBOPLine operationBOPLine = ifExecutOperationItemData.operationBOPLine;
		if(operationBOPLine==null){
			String message = "Operation BOMLine is null : "+operationItemData.getItemId();
			this.peIFExecution.writeLogTextLine(message);
			operationItemData.setStatus(TCData.STATUS_ERROR, message);
			return operationItemData;
		}

		// Operation ��ü�� Data�� Update �Ǿ����� Child Node�� Data�� Update �ؾ� �Ѵ�.
		TreeItem[] childNodeTreeItems = operationItemData.getItems();
		for (int i = 0; childNodeTreeItems != null
				&& i < childNodeTreeItems.length; i++) {

			this.peIFExecution.redrawUI();
			peIFExecution.waite();
			
			TreeItem currentTreeItem = childNodeTreeItems[i];

			if (currentTreeItem instanceof EndItemData) {
				ifExecutEndItemData.createOrUpdate((TCData) currentTreeItem);
			} else if (currentTreeItem instanceof SubsidiaryData) {
				ifExecutSubsidiaryData.createOrUpdate((TCData) currentTreeItem);
			} else if (currentTreeItem instanceof ToolData) {
				ifExecutToolData.createOrUpdate((TCData) currentTreeItem);
			} else if (currentTreeItem instanceof EquipmentData) {
				ifExecutEquipmentData.createOrUpdate((TCData) currentTreeItem);
			} else if (currentTreeItem instanceof ActivityMasterData) {
				ifExecutActivityMasterData
						.createOrUpdate((TCData) currentTreeItem);
			}
			
			peIFExecution.waite();
		}
		
		try {
			peIFExecution.getProcessWindow().save();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		peIFExecution.waite();
		
		return operationItemData;
	}

	/**
	 * Tree Node�� Tree���� �����Ѵ�. ���� �����ϴ� ���� ������ �ƴϸ� ���ŵǾ����� ǥ���ϴ� ���������� ǥ���ϴ°��� ������
	 * ���� ����� ���� �ǰڴ�.
	 * 
	 * @param targetTreeNodeItem
	 */
	private void removeTreeNode(TreeItem targetTreeNodeItem) {
		TreeItem parentTreeNodeItem = targetTreeNodeItem.getParentItem();
		if (parentTreeNodeItem != null) {
			int treeNodeIndex = parentTreeNodeItem.indexOf(targetTreeNodeItem);
			if (treeNodeIndex > -1) {
				parentTreeNodeItem.clear(treeNodeIndex, true);
			}
		}
	}

	private boolean isSameMECONo(TreeItem targetTreeNodeItem) {

		String dataClassType = null;
		TCComponentBOPLine bopLine = null;
		TCComponentItemRevision currentItemRevision = null;

		if (targetTreeNodeItem instanceof ItemData) {
			ItemData tempItemData = (ItemData) targetTreeNodeItem;

			dataClassType = tempItemData.getClassType();
			if (dataClassType != null) {
				if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_OPERATION)) {
					bopLine = (TCComponentBOPLine) ((OperationItemData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_END_ITEM)) {
					bopLine = (TCComponentBOPLine) ((EndItemData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_EQUIPMENT)) {
					bopLine = (TCComponentBOPLine) ((EquipmentData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_SUBSIDIARY)) {
					bopLine = (TCComponentBOPLine) ((SubsidiaryData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_TOOL)) {
					bopLine = (TCComponentBOPLine) ((ToolData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_ACTIVITY)) {
					bopLine = (TCComponentBOPLine) ((ActivityMasterData) targetTreeNodeItem)
							.getBopBomLine();
				} else if (dataClassType.trim().equalsIgnoreCase(
						TCData.TC_TYPE_CLASS_NAME_ACTIVITY_SUB)) {
					bopLine = (TCComponentBOPLine) ((ActivitySubData) targetTreeNodeItem)
							.getBopBomLine();
				}
			}
		}

		boolean isSameMECONo = false;

		TCComponentChangeItemRevision changeItemRevision = null;
		String currentItemMecoNo = null;
		if (bopLine != null) {
			try {
				currentItemRevision = bopLine.getItemRevision();
				if (currentItemRevision != null) {
					changeItemRevision = (TCComponentChangeItemRevision) currentItemRevision
							.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
				}
				if (changeItemRevision != null) {
					currentItemMecoNo = changeItemRevision
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}

		}
		if (currentItemMecoNo == null
				|| (currentItemMecoNo != null && currentItemMecoNo.trim()
						.length() < 1)
				|| (currentItemMecoNo != null && currentItemMecoNo.trim()
						.equalsIgnoreCase("NULL"))) {
			currentItemMecoNo = "";
		}

		// ���� Current Node Item Revision�� MECO No Ȯ��
		if (peIFExecution.getMecoNo().equalsIgnoreCase(currentItemMecoNo)
				&& currentItemMecoNo.trim().equalsIgnoreCase("") == false) {
			// blank�� �ƴ� ������ MECO No�� ����.
			isSameMECONo = true;
		}

		return isSameMECONo;
	}

	/**
	 * Validation ����� ������ Data�� ���� �ؾ� �ϴ°�� True�� �׷��� ���� ��� False�� Return �Ѵ�.
	 * 
	 * @param targetTreeNodeItem
	 * @return
	 */
	private boolean compareResultIsRevise(TreeItem targetTreeNodeItem) {

		boolean isReviseAble = false;

		// Validation ��� Ȯ��
		boolean isAttributeChanged = false;
		boolean isBOMLineAttributeChanged = false;
		int changeType = TCData.DECIDED_NO_CHANGE;
		boolean haveChildNodeAdded = false;
		boolean haveChildNodeRemoved = false;
		boolean haveChildNodeReplaced = false;
		boolean haveChildNodeRevised = false;
		boolean haveChildNodeAttributeChanged = false;
		boolean haveChildNodeBOMLineAttributeChanged = false;
		String dataClassType = null;

		if (targetTreeNodeItem instanceof ItemData) {
			ItemData tempItemData = (ItemData) targetTreeNodeItem;

			isAttributeChanged = tempItemData.getAttributeChangeFlag();
			isBOMLineAttributeChanged = tempItemData
					.getBOMAttributeChangeFlag();
			changeType = tempItemData.getDecidedChagneType();
			haveChildNodeAdded = tempItemData.getChildNodeAddedFlag();
			haveChildNodeRemoved = tempItemData.getChildNodeRemovedFlag();
			haveChildNodeReplaced = tempItemData.getChildNodeReplacedFlag();
			haveChildNodeRevised = tempItemData.getChildNodeRevisedFlag();
			haveChildNodeAttributeChanged = tempItemData
					.getChildAttributeChangedFlag();
			haveChildNodeBOMLineAttributeChanged = tempItemData
					.getChildBOMLineChangedFlag();
			dataClassType = tempItemData.getClassType();
		}

		boolean isReviseTargetCondition1 = false;
		if (isAttributeChanged || haveChildNodeAdded || haveChildNodeRemoved
				|| haveChildNodeReplaced
				|| haveChildNodeBOMLineAttributeChanged) {
			isReviseTargetCondition1 = true;
		}

		boolean isReviseTargetCondition2 = true;
		if (dataClassType.trim().equalsIgnoreCase(
				TCData.TC_TYPE_CLASS_NAME_OPERATION)) {
			if (isBOMLineAttributeChanged == false
					&& haveChildNodeAttributeChanged == false) {
				isReviseTargetCondition2 = false;
			}
		}

		boolean isReviseTargetCondition3 = false;
		if (changeType == TCData.DECIDED_REVISE) {
			isReviseTargetCondition3 = true;
		}

		if ((isReviseTargetCondition1 && isReviseTargetCondition2)
				|| isReviseTargetCondition3) {
			isReviseAble = true;
		}

		return isReviseAble;
	}

	private EndItemData updateEndItemData(EndItemData endItemData) {

		TCComponentBOPLine endItemBOPLine = null;

		// ���� Type�� ���� �߰� �Ǵ� ������ bopLine�� �޾� �´�.
		int changeType = endItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("End Item Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("End Item Remove");
		} else if (changeType == ItemData.DECIDED_REVISE) {
			System.out.println("End Item Revise");
		} else if (changeType == ItemData.DECIDED_REPLACE) {
			System.out.println("End Item Replace");
		}

		if (endItemBOPLine == null) {
			// Operation�� ���� �� Case�� ���� �Ѵ�.
		}

		return endItemData;
	}

	private SubsidiaryData updateSubsidiaryItemData(
			SubsidiaryData subsidiaryItemData) {

		TCComponentBOPLine subsidiaryItemBOPLine = null;

		// ���� Type�� ���� �߰� �Ǵ� ������ bopLine�� �޾� �´�.
		int changeType = subsidiaryItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Subsidiary Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("Subsidiary Remove");
		} else if (changeType == ItemData.DECIDED_REVISE) {
			System.out.println("Subsidiary Revise");
		} else if (changeType == ItemData.DECIDED_REPLACE) {
			System.out.println("Subsidiary Relplace");
		}

		if (subsidiaryItemBOPLine == null) {
			// Operation�� ���� �� Case�� ���� �Ѵ�.
		}
		return subsidiaryItemData;
	}

	private void createFacility(EquipmentData equipmentItemData) {

	}

	private EquipmentData updateFacilityItemData(EquipmentData equipmentItemData) {

		TCComponentBOPLine equipmentItemBOPLine = null;

		// ���� Type�� ���� �߰� �Ǵ� ������ bopLine�� �޾� �´�.
		int changeType = equipmentItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Equipment Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("Equipment Remove");
		} else if (changeType == ItemData.DECIDED_REVISE) {
			System.out.println("Equipment Revise");
		} else if (changeType == ItemData.DECIDED_REPLACE) {
			System.out.println("Equipment Replace");
		}

		if (equipmentItemBOPLine == null) {
			// Operation�� ���� �� Case�� ���� �Ѵ�.
		}

		return equipmentItemData;
	}

	private void createTool(ToolData toolItemData) {

	}

	private ToolData updateToolItemData(ToolData toolItemData) {

		TCComponentBOPLine toolItemBOPLine = null;

		// ���� Type�� ���� �߰� �Ǵ� ������ bopLine�� �޾� �´�.
		int changeType = toolItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Tool Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("Tool Remove");
		} else if (changeType == ItemData.DECIDED_REVISE) {
			System.out.println("Tool Revise");
		} else if (changeType == ItemData.DECIDED_REPLACE) {
			System.out.println("Tool Replace");
		}

		if (toolItemBOPLine == null) {
			// Operation�� ���� �� Case�� ���� �Ѵ�.
		}

		return toolItemData;
	}

	private void createActivity(ActivityMasterData activityMasterDat) {

	}

	private ActivityMasterData updateActivityMasterItemData(
			ActivityMasterData activityMasterItemData) {

		TCComponentMEActivity rootActivity = null;

		// ���� Type�� ���� �߰� �Ǵ� ������ bopLine�� �޾� �´�.
		int changeType = activityMasterItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Activity Maseter Add");
		} else if (changeType == ItemData.DECIDED_REMOVE) {
			System.out.println("Activity Maseter Remove");
		} else {
			System.out.println("Activity Maseter No Change");
		}

		if (rootActivity == null) {
			// Operation�� ���� �� Case�� ���� �Ѵ�.
		}

		if (changeType != ItemData.DECIDED_ADD) {
			return activityMasterItemData;
		}
		// Activity�� Sub Activity Data�� Update �ؾ� �Ѵ�.
		TreeItem[] childNodeTreeItems = activityMasterItemData.getItems();
		for (int i = 0; childNodeTreeItems != null
				&& i < childNodeTreeItems.length; i++) {
			TreeItem currentTreeItem = childNodeTreeItems[i];
			if (currentTreeItem == null) {
				continue;
			}

			if (currentTreeItem instanceof ActivitySubData) {
				currentTreeItem = updateSubActivityItemData((ActivitySubData) currentTreeItem);
			}
		}

		return activityMasterItemData;
	}

	private void createSubActivity(ActivityMasterData activityMasterItemData,
			ActivitySubData activitySubData) {

	}

	private ActivitySubData updateSubActivityItemData(
			ActivitySubData subActivityItemData) {

		TCComponentMEActivity subActivity = null;

		// ���� Type�� ���� �߰� �Ǵ� ������ bopLine�� �޾� �´�.
		int changeType = subActivityItemData.getDecidedChagneType();
		if (changeType == ItemData.DECIDED_ADD) {
			System.out.println("Sub Activity Add");
		}

		if (subActivity == null) {
			// Operation�� ���� �� Case�� ���� �Ѵ�.
		}

		return subActivityItemData;
	}

}
