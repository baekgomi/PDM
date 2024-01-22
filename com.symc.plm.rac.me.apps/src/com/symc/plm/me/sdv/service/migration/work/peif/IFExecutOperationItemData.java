package com.symc.plm.me.sdv.service.migration.work.peif;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Element;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.migration.job.peif.NewPEIFExecution;
import com.symc.plm.me.sdv.service.migration.job.peif.OperationValidationUtil;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEOP;
import com.teamcenter.rac.kernel.TCComponentMEOPRevision;
import com.teamcenter.rac.kernel.TCException;

public class IFExecutOperationItemData extends IFExecutDefault {

//	private IDataSet dataSet;

	private TCComponentBOMLine oldOperationBOPLine;
	private TCComponentItem oldOperationItem;
	private TCComponentItemRevision oldMEOperationRevision;
	

	private TCComponentChangeItemRevision oldOperationChangeItemRevision;
	private String oldOperationMecoNo;

	boolean isNeedToBOMLineAdd = false;
	boolean isNeedToBOMLineReplace = false;

	public IFExecutOperationItemData(NewPEIFExecution peIFExecution) {
		super(peIFExecution);
	}

	private static String DEFAULT_REV_ID = "000";

	@Override
	public boolean createOrUpdate(TCData tcData) {

		this.meOperationItem = null;
		this.meOperationRevision = null;
		this.operationBOPLine = null;

		this.operationItemData = (OperationItemData) tcData;

		isNeedToBOMLineAdd = false;
		isNeedToBOMLineReplace = false;

		operationItemId = null;
		oldOperationItem = null;
		oldMEOperationRevision = null;
		oldOperationChangeItemRevision = null;
		oldOperationMecoNo = null;
		
		// Reader Class�� ���� Operation ��������� Data�� �о� dataSet�� �����Ѵ�.
		Element operationBOMLineDataElement = (Element) this.operationItemData
				.getBomLineNode();
		Element operationNodeMasterData = (Element) this.operationItemData
				.getMasterDataNode();
		
//		this.dataSet = peIFExecution.getPeImportDataReaderUtil().
//				getOperationNodeIDataSet(
//					operationBOMLineDataElement, 
//					operationNodeMasterData
//				);

		initTargetItem();

		boolean isCreateTarget = false;
		boolean isReviseTarget = false;
		boolean isUpdateTarget = false;

		int changeType = this.operationItemData.getDecidedChagneType();
		boolean bomChanged = this.operationItemData.getBOMAttributeChangeFlag();
		boolean attributeChanged = this.operationItemData.getAttributeChangeFlag();
		
		if (changeType == TCData.DECIDED_NO_CHANGE && bomChanged==false) {
			
			System.out.println("Change Type : No Change");
			
			// �߰����� ó�� ���� Return
			return true;
		} else if (changeType == TCData.DECIDED_REMOVE) {
			
			boolean haveRemoveException = false;
			
			// ���� ó���� ���� �Ѵ�.
			try {
				removeTargetObject();
			} catch (TCException e) {
				e.printStackTrace();
				haveRemoveException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveRemoveException = true;
			}
			return !(haveRemoveException);
		} else if (changeType == TCData.DECIDED_ADD) {
			
			// �Ʒ��� �߰����� Data Ȯ�ΰ� �ļ�ó���� �����Ѵ�.
			isNeedToBOMLineAdd = true;
			isUpdateTarget = true;
			bomChanged=true;
			attributeChanged=true;
		} else if (changeType == TCData.DECIDED_REVISE) {
			
			// �Ʒ��� �߰����� Data Ȯ�ΰ� �ļ�ó���� �����Ѵ�.
			isReviseTarget = true;
			isUpdateTarget = true;
		} else if (changeType == TCData.DECIDED_REPLACE) {
			
			// �Ʒ��� �߰����� Data Ȯ�ΰ� �ļ�ó���� �����Ѵ�.
			isNeedToBOMLineReplace = true;
			isUpdateTarget = true;
		}
		
		if(bomChanged==true || attributeChanged==true ){
			isUpdateTarget = true;
		}
		
		if (this.oldOperationItem == null) {
			isCreateTarget = true;
		} else {
			if (this.oldOperationChangeItemRevision != null) {
				if (peIFExecution.getMecoRevision() != null
						&& peIFExecution.getMecoRevision().equals(
									this.oldOperationChangeItemRevision
								) == true) {
					// ������ MECO No
					this.meOperationItem = this.oldOperationItem;
					this.meOperationRevision = this.oldMEOperationRevision;
					if (this.oldOperationBOPLine != null) {
						this.operationBOPLine = (TCComponentBOPLine) this.oldOperationBOPLine;
					}
					isUpdateTarget = true;
				} else {
					// �ٸ� MECO NO
					isReviseTarget = true;
					isUpdateTarget = true;
				}
			} else {
				// MECO ���� �׳� Update �ϸ� ��.
				isUpdateTarget = true;
			}
		}
		
		boolean haveCreateException = false;
		
		if (isCreateTarget == true) {
			try {
				createTargetObject();
				peIFExecution.waite();
				isUpdateTarget = true;
			} catch (TCException e) {
				
				haveCreateException = true;
				String message = "Operation create error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
			} catch (Exception e) {
				
				haveCreateException = true;
				String message = "Operation create error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
			}
		}
		
		boolean haveReviseException = false;
		if (isCreateTarget == false && isReviseTarget == true) {
			try {
				reviseTargetObject();
				peIFExecution.waite();
				isUpdateTarget = true;
			} catch (TCException e) {
				haveReviseException = true;

				String message = "Operation revise error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
				
			} catch (Exception e) {
				haveReviseException = true;
				
				String message = "Operation revise error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
			}
		}
		
		if ((haveCreateException== false && haveReviseException==false) && isNeedToBOMLineReplace == true) {
			// Operation�� ���� �ϴ� ��찡 ������?
			if (this.operationBOPLine != null) {
				try {
					this.operationBOPLine.replace(this.meOperationItem,
							this.meOperationRevision, null);
					this.operationBOPLine.save();
					peIFExecution.waite();
				} catch (TCException e) {
					String message = "BOPLine replace Error ["+operationItemId+"] : "+e.getMessage();
					this.peIFExecution.writeLogTextLine(message);
					this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
				}
			}
		}

		boolean haveUpdateException = false;
		boolean isUpdate = true;
		
		if (isUpdateTarget == true) {
			
			if(isCreateTarget && haveCreateException== true){
				isUpdate = false;
			}
			if(isReviseTarget == true && haveReviseException==true){
				isUpdate = false;
			}
			
		}
		
		if(isUpdate==true){
			try {
				updateTargetObject();
				peIFExecution.waite();
			} catch (TCException e) {
				e.printStackTrace();
				haveUpdateException = true;
			} catch (Exception e) {
				e.printStackTrace();
				haveUpdateException = true;
			}
		}

		if (this.operationBOPLine != null) {
			this.operationItemData.setBopBomLine(this.operationBOPLine);
		}
		
		boolean isOk = true;
		if(isCreateTarget == true && haveCreateException == true){
			isOk = false;
		}
		if(isReviseTarget == true && haveReviseException == true){
			isOk = false;
		}
		if(isUpdateTarget==true && haveUpdateException == true){
			isOk = false;
		}

		return isOk;
	}

	private void initTargetItem() {

		this.oldOperationBOPLine = (TCComponentBOPLine) this.operationItemData
				.getBopBomLine();

		if (this.oldOperationBOPLine != null) {
			try {
				this.oldOperationItem = (TCComponentMEOP) this.oldOperationBOPLine
						.getItem();
				if (this.oldOperationItem != null) {
					this.operationItemId = this.oldOperationItem
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
				this.oldMEOperationRevision = (TCComponentMEOPRevision) this.oldOperationBOPLine
						.getItemRevision();
			} catch (TCException e) {
				e.printStackTrace();
			}
		} else {

			Element operationBOMLineDataElement = (Element) this.operationItemData
					.getBomLineNode();
			this.operationItemId = operationBOMLineDataElement
					.getAttribute("OperationItemId");

			// ���� �ϴ� Item �ΰ�� ������ Item Revision�� ã�´�.
			if (this.operationItemId != null) {
				try {
					this.oldMEOperationRevision = (TCComponentMEOPRevision) SYMTcUtil
							.getLatestedRevItem(operationItemId);
					if (this.oldMEOperationRevision != null) {
						this.oldOperationItem = (TCComponentMEOP) this.oldMEOperationRevision
								.getItem();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// �̹� �����ϴ� Item Revision�� MECO ������ Ȯ��.
		if (this.oldMEOperationRevision != null) {
			try {
				this.oldOperationChangeItemRevision = (TCComponentChangeItemRevision) oldMEOperationRevision
						.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
				if (oldOperationChangeItemRevision != null) {
					this.oldOperationMecoNo = oldOperationChangeItemRevision
							.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

	public void createTargetObject() throws Exception, TCException {

		// Reader Class�� ���� Operation ��������� Data�� �о� dataSet�� �����Ѵ�.
		Element operationBOMLineDataElement = (Element) this.operationItemData
				.getBomLineNode();
		Element operationNodeMasterData = (Element) this.operationItemData
				.getMasterDataNode();

		// Operation Item�� ���Ѵ�.
		if (this.meOperationItem == null && this.oldOperationItem == null) {
			
			String korName = null;
			
			if (operationNodeMasterData.getElementsByTagName("G") != null) {
				if (operationNodeMasterData.getElementsByTagName("G")
						.getLength() > 0) {
					korName = operationNodeMasterData
							.getElementsByTagName("G").item(0)
							.getTextContent();
				}
			}
			
			try {
				this.meOperationItem = null;
				this.meOperationItem = (TCComponentMEOP) SDVBOPUtilities
						.createItem(
								SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM,
								this.operationItemId, DEFAULT_REV_ID, korName,
								"");
			} catch (Exception e) {
				this.peIFExecution.writeLogTextLine("Operation Creation Error ["+operationItemId+"] : "+e.getMessage());
			}
			
			if (this.meOperationItem != null) {
				try {
					this.meOperationRevision = (TCComponentMEOPRevision) this.meOperationItem
							.getLatestItemRevision();
				} catch (TCException e) {
					this.peIFExecution.writeLogTextLine("Operation Revision Find Error ["+operationItemId+"] : "+e.getMessage());
				}
			}
			if (this.meOperationRevision != null) {
				try {
					this.meOperationRevision.setReferenceProperty(
							SDVPropertyConstant.OPERATION_REV_MECO_NO,
							peIFExecution.getMecoRevision());
				} catch (TCException e) {
					this.peIFExecution.writeLogTextLine("MECO Set Error ["+operationItemId+"] : "+e.getMessage());
				}
				
				// BOPLine�� �������� ���� �����̸� BOPLine�� �����Ѵ�.
				addBOMLine();
			}
		}

		if (this.meOperationRevision != null) {

			// �Ӽ� Update
			try {
				updateOperationItemAndItemRevisionDataUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (this.meOperationRevision
					.isValidPropertyName(SDVPropertyConstant.S7_MATURITY)) {
				try {
					this.meOperationRevision.setProperty(
							SDVPropertyConstant.S7_MATURITY, "In Work");
				} catch (TCException e) {
					e.printStackTrace();
				}
			}

			// ����Revision�� �۾�ǥ�ؼ��� ����
			try {
				attachProcessExcelToOP(meOperationRevision, operationNodeMasterData);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/**
			 * MECO�� ������ Item Revision�� ����
			 */
			try {
				addRevisionToMecoRevision();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public void reviseTargetObject() throws Exception, TCException {

		System.out.println("this.oldMEOperationRevision = "
				+ this.oldMEOperationRevision);

		boolean isReleased = false;
		if (this.oldMEOperationRevision != null) {
			isReleased = SYMTcUtil.isReleased(this.oldMEOperationRevision);
		}

		if (isReleased == true) {

			// ���� ������ �ִ��� Check �Ѵ�.
			boolean isChangeAble = haveChangeAccessRight(this.oldMEOperationRevision);
			boolean haveVolumeError = this.operationItemData.isHaveWorkInstructionVolumeFileError();
			
			
			// Test �������� Test ����� Dataset�� ������ Volume�� ���� ��� �����Ҷ� Exception�� �߻��ȴ�.
			// �̰��� ������ �� �ִ� ����� �ִ��� ����غ��� �Ѵ�.
			// --> ���߼����� �ƴ� ���� Server������ �߻����� ���� ����
			// --> ���߼��������� � Server���� File�� Copy�ؼ� ����ϴ� Admin �޴��� �߰��ؼ� ����ϵ��� ��.
			// --> Validation���� �̷� ��� Erroró���Ǿ� ���������� ǥ��ǰ� Interface ���� �ǵ��� ó�� ����.
			
			if (isChangeAble == true && haveVolumeError==false) {
				
				String newRevId = null;
				try {
					newRevId = this.oldMEOperationRevision.getItem()
							.getNewRev();
					if (newRevId != null) {
						this.meOperationRevision = this.oldMEOperationRevision
								.saveAs(newRevId);
						if (this.meOperationRevision != null) {
							
							this.meOperationRevision.lock();
							
							this.meOperationRevision.setReferenceProperty(
									SDVPropertyConstant.OPERATION_REV_MECO_NO,
									peIFExecution.getMecoRevision());
							
							this.meOperationRevision.save();
							
							this.meOperationRevision.unlock();
						}
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			} else {
				throw new Exception("You need change permissions or work instruction file is exist : "
						+ this.oldMEOperationRevision);
			}

		} else {
			this.meOperationRevision = this.oldMEOperationRevision;
		}

		if (this.meOperationItem == null && this.meOperationRevision!=null) {
			this.meOperationItem = this.meOperationRevision.getItem();
		}

		isReleased = false;
		if (this.meOperationRevision != null) {
			isReleased = isReleased(this.meOperationRevision);
		}

		if (this.meOperationRevision != null && isReleased == false) {

			if (this.meOperationRevision
					.isValidPropertyName(SDVPropertyConstant.S7_MATURITY)) {
				
				this.meOperationRevision.lock();
				
				try {
					this.meOperationRevision.setProperty(
							SDVPropertyConstant.S7_MATURITY, "In Work");
					this.meOperationRevision.save();
				} catch (TCException e) {
					e.printStackTrace();
				}
				
				this.meOperationRevision.unlock();
			}

			/**
			 * MECO�� ������ Item Revision�� ����
			 */
			try {
				addRevisionToMecoRevision();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public void removeTargetObject() throws Exception, TCException {
		if (this.operationBOPLine == null) {
			return;
		}

		TCComponentBOPLine lineBOPLine = (TCComponentBOPLine) this.operationBOPLine
				.parent();
		if (lineBOPLine != null) {
			if (haveWriteAccessRight(lineBOPLine) == true) {
				this.operationBOPLine.cut();
			} else {
				throw new Exception("You need write permissions. : "
						+ this.operationBOPLine);
			}
			lineBOPLine.save();
		}
	}

	private void updateOperationItemData(Element nfMasterDataElement) throws Exception {
		if (nfMasterDataElement == null) {
			return;
		}
		if (meOperationItem == null) {
			return;
		}

		try {
			this.meOperationItem.lock();
		} catch (Exception e) {

		}

		// ��ü�۾� ���� (U/Body Work)
		String isUnderBodyWork = null;
		if (nfMasterDataElement.getElementsByTagName("Q") != null) {
			if (nfMasterDataElement.getElementsByTagName("Q").getLength() > 0) {
				isUnderBodyWork = nfMasterDataElement.getElementsByTagName("Q")
						.item(0).getTextContent();
			}
		}
		if ((isUnderBodyWork != null && isUnderBodyWork.equalsIgnoreCase("Y")) == false) {
			isUnderBodyWork = "N";
		}

		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_WORK_UBODY, getPropertyString(isUnderBodyWork) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Ubody Error : "+isUnderBodyWork);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Ubody Error : "+e.getMessage());
		}

		// �۾���ġ (Working Position)
		String operationWorkArea = null;
		if (nfMasterDataElement.getElementsByTagName("F") != null) {
			if (nfMasterDataElement.getElementsByTagName("F").getLength() > 0) {
				operationWorkArea = nfMasterDataElement
						.getElementsByTagName("F").item(0).getTextContent();
			}
		}

		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_WORKAREA, getPropertyString(operationWorkArea) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Worker area Error : "+operationWorkArea);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Worker area Error : "+e.getMessage());
			//e.printStackTrace();
		}

		// �۾��� �����ڵ� (Worker Code)
		String operationWorkerCode = null;
		if (nfMasterDataElement.getElementsByTagName("I") != null) {
			if (nfMasterDataElement.getElementsByTagName("I").getLength() > 0) {
				operationWorkerCode = nfMasterDataElement
						.getElementsByTagName("I").item(0).getTextContent();
			}
		}
		
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_WORKER_CODE,
					 getPropertyString(operationWorkerCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Worker code Error : "+operationWorkerCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Worker code Error : "+e.getMessage());
		}

		// ����������ġ ���� (Item Location-U/L)
		String operationItemUL = null;
		if (nfMasterDataElement.getElementsByTagName("J") != null) {
			if (nfMasterDataElement.getElementsByTagName("J").getLength() > 0) {
				operationItemUL = nfMasterDataElement.getElementsByTagName("J")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_ITEM_UL, getPropertyString(operationItemUL) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set UL Error : "+operationItemUL);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set UL Error : "+e.getMessage());
		}

		// Process Sequence
		String operationProcessSeq = null;
		if (nfMasterDataElement.getElementsByTagName("P") != null) {
			if (nfMasterDataElement.getElementsByTagName("P").getLength() > 0) {
				operationProcessSeq = nfMasterDataElement
						.getElementsByTagName("P").item(0).getTextContent();
			}
		}
		if ((operationProcessSeq != null && operationProcessSeq.trim().length() > 0) == false) {
			operationProcessSeq = "";
		}
		// Process Sequence ���ڸ� �Է½� �տ� 0 �� ���δ�.
		if (operationProcessSeq.length() == 2) {
			operationProcessSeq = "0".concat(operationProcessSeq);
		}
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_PROCESS_SEQ,
					 getPropertyString(operationProcessSeq) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Op Seq Error : "+operationProcessSeq);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Op Seq Error : "+e.getMessage());
		}

		// ��ǥ���� ���� (Is Representative Vehicle)
		String operationRepVehicleCheck = null;
		if (nfMasterDataElement.getElementsByTagName("R") != null) {
			if (nfMasterDataElement.getElementsByTagName("R").getLength() > 0) {
				operationRepVehicleCheck = nfMasterDataElement
						.getElementsByTagName("R").item(0).getTextContent();
			}
		}
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK,
					getPropertyString(operationRepVehicleCheck) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set m7_REP_VHICLE_CHECK Error : "+operationRepVehicleCheck);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set m7_REP_VHICLE_CHECK Error : "+e.getMessage());
		}

		// Name (object_name)
		String operationName = null;
		if (nfMasterDataElement.getElementsByTagName("G") != null) {
			if (nfMasterDataElement.getElementsByTagName("G").getLength() > 0) {
				operationName = nfMasterDataElement.getElementsByTagName("G")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationItem.setProperty(
					SDVPropertyConstant.ITEM_OBJECT_NAME, 
					getPropertyString(operationName) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Name Error : "+operationName);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Name Error : "+e.getMessage());
		}

		// Max Working Time ���� (Is Representative Vehicle)
		// SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK

		try {
			this.meOperationItem.save();
			System.out.println("" + this.operationItemId
					+ " Item Data Save --- ");
		} catch (TCException e) {
			e.printStackTrace();
		}

		try {
			this.meOperationItem.unlock();
		} catch (Exception e) {

		}

	}

	private void updateOperationItemRevisionData(Element nfBOMLineDataElement,
			Element nfMasterDataElement) throws Exception {

		if (nfMasterDataElement == null) {
			return;
		}
		if (meOperationRevision == null) {
			return;
		}
		
		boolean isReleased = false;
		if (this.meOperationRevision != null) {
			try {
				isReleased = SYMTcUtil.isReleased(this.meOperationRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		if(isReleased==true){
			String exceptionMessage = "Error : "+this.meOperationRevision+" is released. (Can't property update!!)"; 
			System.out.println(exceptionMessage);
			return;
			//throw new Exception(exceptionMessage);
		}

		try {
			this.meOperationRevision.lock();
		} catch (Exception e) {

		}

		// Name (object_name)
		String operationName = null;
		if (nfMasterDataElement.getElementsByTagName("G") != null) {
			if (nfMasterDataElement.getElementsByTagName("G").getLength() > 0) {
				operationName = nfMasterDataElement.getElementsByTagName("G")
						.item(0).getTextContent();
			}
		}

		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.ITEM_OBJECT_NAME, 
					getPropertyString(operationName) );
			// this.meOperationRevision.setProperty(SDVPropertyConstant.OPERATION_REV_KOR_NAME,
			// operationName);
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Name Error : "+operationName);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Name Error : "+e.getMessage());
		}

		// ������ (English Name)
		String englishName = null;
		if (nfMasterDataElement.getElementsByTagName("H") != null) {
			if (nfMasterDataElement.getElementsByTagName("H").getLength() > 0) {
				englishName = nfMasterDataElement.getElementsByTagName("H")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_ENG_NAME, 
					getPropertyString(englishName) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Eng Name Error : "+englishName);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Eng Name Error : "+e.getMessage());
		}

		// vehicleCode
		String vehicleCode = null;
		if (nfMasterDataElement.getElementsByTagName("B") != null) {
			if (nfMasterDataElement.getElementsByTagName("B").getLength() > 0) {
				vehicleCode = nfMasterDataElement.getElementsByTagName("B")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision
					.setProperty(
							SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE,
							getPropertyString(vehicleCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Vehicle Code Error : "+vehicleCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Vehicle Code Error : "+e.getMessage());
		}

		// Shop Code
		String shopCode = null;
		if (nfMasterDataElement.getElementsByTagName("C") != null) {
			if (nfMasterDataElement.getElementsByTagName("C").getLength() > 0) {
				shopCode = nfMasterDataElement.getElementsByTagName("C")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_SHOP, 
					getPropertyString(shopCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Shop Error : "+shopCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Shop Error : "+e.getMessage());
		}

		// Function Code / Operation Code
		String functionCode = null;
		String operationCode = null;
		if (nfMasterDataElement.getElementsByTagName("D") != null) {
			if (nfMasterDataElement.getElementsByTagName("D").getLength() > 0) {
				operationCode = nfMasterDataElement.getElementsByTagName("D")
						.item(0).getTextContent();
			}
		}

		if ((operationCode != null && operationCode.trim().length() > 0) == false) {
			functionCode = "";
			operationCode = "";
		} else {
			functionCode = operationCode.split("-")[0];
			operationCode = operationCode.split("-")[1];
		}

		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE,
					getPropertyString(functionCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Function Code Error : "+functionCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Function Code Error : "+e.getMessage());
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_OPERATION_CODE,
					getPropertyString(operationCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Operation Code Error : "+operationCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Operation Code Error : "+e.getMessage());
		}

		// Assembly System
		String assySystem = null;
		if (nfMasterDataElement.getElementsByTagName("N") != null) {
			if (nfMasterDataElement.getElementsByTagName("N").getLength() > 0) {
				assySystem = nfMasterDataElement.getElementsByTagName("N")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, 
					getPropertyString(assySystem) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Assy System Error : "+assySystem);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Assy System Error : "+e.getMessage());
		}

		// ���� (DR)
		String drCode = null;
		if (nfMasterDataElement.getElementsByTagName("M") != null) {
			if (nfMasterDataElement.getElementsByTagName("M").getLength() > 0) {
				drCode = nfMasterDataElement.getElementsByTagName("M").item(0)
						.getTextContent();
			}
		}
		
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_DR, 
					getPropertyString(drCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set DR Code Error : "+drCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set DR Code Error : "+e.getMessage());
		}

		// Station No
		String stationNo = null;
		if (nfMasterDataElement.getElementsByTagName("L") != null) {
			if (nfMasterDataElement.getElementsByTagName("L").getLength() > 0) {
				stationNo = nfMasterDataElement.getElementsByTagName("L")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision.setProperty(
					SDVPropertyConstant.OPERATION_REV_STATION_NO, 
					getPropertyString(stationNo) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Station No Error : "+stationNo);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Station No Error : "+e.getMessage());
		}

		// Install Drawing No
		String installDrawingNo = null;
		if (nfMasterDataElement.getElementsByTagName("K") != null) {
			if (nfMasterDataElement.getElementsByTagName("K").getLength() > 0) {
				installDrawingNo = nfMasterDataElement
						.getElementsByTagName("K").item(0).getTextContent();
			}
		}
		if ((installDrawingNo != null && installDrawingNo.trim().length() > 0) == false) {
			installDrawingNo = "";
		}

		String[] dwgNoArray = installDrawingNo.split("/");
		// OPERATION_REV_INSTALL_DRW_NO
		ArrayList<String> dwgNoList = new ArrayList<String>();
		if (dwgNoArray != null && dwgNoArray.length > 0) {
			for (String dwg : dwgNoList) {
				dwg = BundleUtil.nullToString(dwg).trim();
				if (!"".equals(dwg)) {
					dwgNoList.add(dwg);
				}
			}
		}

		dwgNoArray = dwgNoList.toArray(new String[dwgNoList.size()]);
		if (dwgNoList == null || (dwgNoList != null && dwgNoList.size() < 1)) {
			dwgNoArray = new String[] { "" };
		} else {
			for (int i = 0; dwgNoArray != null && i < dwgNoArray.length; i++) {
				String tempStr = dwgNoArray[i];

				if (tempStr == null
						|| (tempStr != null && tempStr.trim().length() < 1)) {
					tempStr = "";
				} else {
					if (tempStr.trim().equalsIgnoreCase("NULL")) {
						tempStr = "";
					} else {
						tempStr = tempStr.trim();
					}
				}
				dwgNoArray[i] = tempStr;
			}
		}

		try {
			this.meOperationRevision.getTCProperty(
					SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO)
					.setStringValueArray(dwgNoArray);
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Install Drawing No Error : "+dwgNoArray);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Install Drawing No Error : "+e.getMessage());
		}

		// Product Code
		String productCode = null;
		if (nfBOMLineDataElement.getElementsByTagName("D") != null) {
			if (nfBOMLineDataElement.getElementsByTagName("D").getLength() > 0) {
				productCode = nfBOMLineDataElement.getElementsByTagName("D")
						.item(0).getTextContent();
			}
		}
		try {
			this.meOperationRevision
					.setProperty(
							SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE,
							getPropertyString(productCode) );
		} catch (TCException e) {
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Product Code Error : "+productCode);
			this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set Product Code Error : "+e.getMessage());
		}

		// // Line Code
		// String lineCode = null;
		// if( nfBOMLineDataElement.getElementsByTagName("E") != null){
		// if( nfBOMLineDataElement.getElementsByTagName("E").getLength()>0 ){
		// lineCode =
		// nfBOMLineDataElement.getElementsByTagName("E").item(0).getTextContent();
		// }
		// }
		// if((lineCode!=null && lineCode.trim().length()>0)==false){
		// lineCode = "";
		// }
		// try {
		// this.meOperationRevision.setProperty(SDVPropertyConstant.OPERATION_REV_LINE,
		// lineCode);
		// } catch (TCException e) {
		// e.printStackTrace();
		// }

		// MECO No
		if(peIFExecution.getMecoRevision()!=null){
			try {
				this.meOperationRevision.setReferenceProperty(
						SDVPropertyConstant.OPERATION_REV_MECO_NO,
						peIFExecution.getMecoRevision());
			} catch (TCException e) {
				this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set MECO No Error : "+peIFExecution.getMecoRevision());
				this.peIFExecution.writeLogTextLine("["+operationItemId+"] Set MECO No Error : "+e.getMessage());
			}
		}

		try {
			this.meOperationRevision.save();
			//System.out.println("" + this.operationItemId+ " Item Revision Data Save --- ");
		} catch (TCException e1) {
			//e1.printStackTrace();
		}

		try {
			this.meOperationRevision.unlock();
		} catch (Exception e) {

		}
		
		// Operation�� Work Instruction�� Update ����� ��� Excel�� Update �Ѵ�.
		if(this.operationItemData.isWorkInstructionUpdateTarget()==true){
			try {
				attachProcessExcelToOP(this.meOperationRevision, nfMasterDataElement);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// SDVPropertyConstant.OPERATION_REV_ALT_PREFIX
		// SDVPropertyConstant.OPERATION_REV_BOP_VERSION
		// SDVPropertyConstant.OPERATION_REV_IS_ALTBOP
		// SDVPropertyConstant.OPERATION_REV_KPC
		// SDVPropertyConstant.OPERATION_REV_MAX_WORK_TIME_CHECK
		// SDVPropertyConstant.OPERATION_REV_REP_VHICLE_CHECK
		// SDVPropertyConstant.OPERATION_REV_STATION_CODE

	}

	private void updateOperationItemAndItemRevisionDataUpdate() throws Exception {
		// -----------------------------------------------------------------------------
		// Operation Item �Ǵ� Operation Item Revision�� Property ����
		// -----------------------------------------------------------------------------
		Element nfBOMLineDataElement = (Element) operationItemData
				.getBomLineNode();
		Element nfMasterDataElement = (Element) operationItemData
				.getMasterDataNode();

		boolean isReleased = false;

		// Item Property Update
		if (this.meOperationRevision != null) {
			try {
				isReleased = SYMTcUtil.isReleased(this.meOperationRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
			if (isReleased == false) {
				// ���� ������ �ִ��� Check �Ѵ�.
				boolean isWriteAble = haveWriteAccessRight(this.meOperationItem);
				if (isWriteAble == true) {
					try {
						updateOperationItemData(nfMasterDataElement);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		// Item Revision Property Update
		isReleased = false;
		if (this.meOperationRevision != null) {
			try {
				isReleased = SYMTcUtil.isReleased(this.meOperationRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
			if (isReleased == false) {
				// ���� ������ �ִ��� Check �Ѵ�.
				boolean isWriteAble = haveWriteAccessRight(this.meOperationRevision);
				if (isWriteAble == true) {
					try {
						updateOperationItemRevisionData(nfBOMLineDataElement,
								nfMasterDataElement);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	public void updateTargetObject() throws Exception, TCException {

		System.out.println("Operatoin updateTargetObject() : " + this.operationItemId);

		// BOPLine�� �������� ���� �����̸� BOPLine�� �����Ѵ�.
		addBOMLine();

		// �Ӽ� Update
		updateOperationItemAndItemRevisionDataUpdate();
		
		if(this.operationBOPLine==null){
			return;
		}

		boolean isReleased = false;
		boolean isWriteAble = false;
		try {
			isReleased = SYMTcUtil.isReleased(this.operationBOPLine);
			// ���� ������ �ִ��� Check �Ѵ�.
			if (isReleased == false) {
				isWriteAble = haveWriteAccessRight(this.operationBOPLine);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (isReleased == true) {
			System.out.println("[" + this.operationItemData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;
		}else if(isWriteAble == false){
			System.out.println("[" + this.operationItemData.getText() + "] "
					+ "BOMLine Change Fail : " + "isReleased=" + isReleased
					+ ", isChangeAble=" + isWriteAble);
			return;			
		}

		// --------------------------------------------------------------------------------------------------------
		// BOMLine Attribute Update �Ѵ�.
		// --------------------------------------------------------------------------------------------------------
		String stationNo = meOperationRevision.getProperty(
				SDVPropertyConstant.OPERATION_REV_STATION_NO).replace("-", "");// ������ȣ
		stationNo = getPropertyString(stationNo);

		String workerCode = meOperationItem.getProperty(
				SDVPropertyConstant.OPERATION_WORKER_CODE).replace("-", "");// �۾����ڵ�
		workerCode = getPropertyString(workerCode);

		String seq = meOperationItem
				.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);// �۾���
																		// ����
		seq = getPropertyString(seq);

		// ���� �Է�
		this.operationBOPLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");

		// ��������ȣ �Է�
		boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty()
				|| seq.isEmpty(); // �ϳ��� ���� ������ �ݿ�����
		String findNo = stationNo.concat("|").concat(workerCode).concat("|")
				.concat(seq);
		findNo = getPropertyString(findNo);
		if (findNo.length() > 15 || isExistEmptyValue) {
			;
		} else {
			this.operationBOPLine.setProperty(
					SDVPropertyConstant.BL_SEQUENCE_NO,
					getPropertyString(findNo) );
		}
		
		// Option Condition ����
		Element nfBOMLineDataElement = (Element) operationItemData
				.getBomLineNode();
		String optionCode = null;
		if (nfBOMLineDataElement.getElementsByTagName("K") != null) {
			if (nfBOMLineDataElement.getElementsByTagName("K").getLength() > 0) {
				optionCode = nfBOMLineDataElement.getElementsByTagName("K")
						.item(0).getTextContent();
			}
		}
		if ((optionCode != null && optionCode.trim().length() > 0) == false) {
			optionCode = "";
		}
		
		System.out.println("optionCode = "+optionCode);
		
		String kk = getConversionOptionCondition(optionCode);

		System.out.println("optionCondition = "+kk);
		TCException optionConditionTCException = null;
		Exception optionConditionException = null;
		int exceptionType = 0;
		try {
			System.out.println("Update Operation ["+operationItemId+"] -> Option Condition");
			SDVBOPUtilities.updateOptionCondition(this.operationBOPLine,kk);
		} catch (TCException e) {
			optionConditionTCException = e;
			exceptionType = 1;
			//e.printStackTrace();
		} catch (Exception e) {
			optionConditionException = e;
			exceptionType = 2;
			//e.printStackTrace();
		}
		
//		System.out.println("KKKKKKKK-- 3 -- : optionConditionTCException = "+optionConditionTCException);
//		System.out.println("KKKKKKKK-- 3 -- : optionConditionException = "+optionConditionException);

		if(exceptionType==1){
			this.peIFExecution.writeLogTextLine(operationItemId+" have Major Exception [Option Condition Update] -> :\n"+optionConditionTCException.getDetailsMessage());
			this.operationItemData.setHaveMajorError(true);
		}
		if(exceptionType==2){
			this.peIFExecution.writeLogTextLine(operationItemId+" have Major Exception [Option Condition Update] -> :\n"+optionConditionException.getMessage());
			this.operationItemData.setHaveMajorError(true);
		}
		
		// [NON-SR][20160113] taeku.jeong Line, Station, Operation,
		// Operation�� bl_abs_occ_id ���� �����Ѵ�.
		try {
			BOPLineUtility.updateLineToOperationAbsOccId(this.operationBOPLine);
		} catch (TCException e) {
			e.printStackTrace();
		}

		try {
			this.operationBOPLine.save();
			this.operationBOPLine.window().save();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		System.out.println("Operation Update End.............");

		this.operationItemData.setBopBomLine(this.operationBOPLine);
	}

	private void addBOMLine() {
		
		if(this.operationItemData.getDecidedChagneType()==TCData.DECIDED_REMOVE){
			return;
		}

		LineItemData lineItemData = (LineItemData) this.operationItemData
				.getParentItem();
		TCComponentBOPLine lineBOPLine = (TCComponentBOPLine) lineItemData
				.getBopBomLine();

		if (lineBOPLine == null) {
			return;
		}

		TCComponentBOMLine[] findedBOMLines = 
				getCurrentBOPLine(this.operationItemId, true, lineBOPLine);
		
		if (findedBOMLines != null && findedBOMLines.length > 0) {
			// Find Current Operation BOP Line
			this.operationBOPLine = (TCComponentBOPLine) findedBOMLines[0];
		} else {
			// OperationBOPLine�� ��ϵǾ� ���� ���� ���
			try {
				this.operationBOPLine = (TCComponentBOPLine) lineBOPLine.add(
						this.meOperationItem, this.oldMEOperationRevision,
						null, false);
				if (this.operationBOPLine != null) {
					this.operationBOPLine.setProperty(
							SDVPropertyConstant.BL_ABS_OCC_ID,
							this.operationItemId);
				}
				this.operationBOPLine.save();
			} catch (TCException e) {
				String message = "Operation BOMLine attach error ["+operationItemId+"] : "+e.getMessage();
				this.peIFExecution.writeLogTextLine(message);
				this.operationItemData.setStatus(TCData.STATUS_ERROR, message);
			}
		}

		if (lineBOPLine!=null && this.operationBOPLine != null) {
			try {
				lineBOPLine.save();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		if(this.operationBOPLine!=null){
			this.operationItemData.setBopBomLine(this.operationBOPLine);
		}
		
	}

	/**
	 * �۾�ǥ�ؼ� Excel Template ������ �����Ʒ��� ����
	 * 
	 * @method attachProcessExcelToOP
	 * @date 2013. 11. 21.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void attachProcessExcelToOP(TCComponentItemRevision opRevision, Element nfMasterDataElement)
			throws Exception {
		
		// �̺κ��� ������ ����Ǹ� �ȵȴ�.
		// Work Instruction ���� ����� �ƴ� ���� �ֱ� �����̴�.

		// Work Instruction I/F ������� Ȯ�� �Ѵ�.
		boolean isIFTarget = false;
		String isIFTargetString = null;
		if (nfMasterDataElement.getElementsByTagName("T") != null) {
			if (nfMasterDataElement.getElementsByTagName("T").getLength() > 0) {
				isIFTargetString = nfMasterDataElement.getElementsByTagName("T")
						.item(0).getTextContent();
			}
		}
		if ((isIFTargetString != null && isIFTargetString.equalsIgnoreCase("TRUE")) == true) {
			isIFTarget = true;
		}
		
		String message = "#### W/I Update target : "+operationItemId+" -> "+isIFTargetString +"("+isIFTarget+")";
		System.out.println(message);
		peIFExecution.writeLogTextLine(message);
		
		String nfFilePath = null;
		if (nfMasterDataElement.getElementsByTagName("S") != null) {
			if (nfMasterDataElement.getElementsByTagName("S").getLength() > 0) {
				nfFilePath = nfMasterDataElement.getElementsByTagName("S")
						.item(0).getTextContent();
			}
		}

		if(isIFTarget==false){
			return;
		}
		
		File nfWorkInstructionFile = null;
		if(nfFilePath!=null && nfFilePath.trim().length()>0){
			nfWorkInstructionFile = new File(nfFilePath.trim());
		}

		
		// ������ �ִ� DataSet�� ������ ���� �ϰ� �ٽ� �߰� �Ѵ�.
		boolean haveWorkInstructionUpdateTaget = false;
		TCComponentDataset dataSet = null;
		TCComponentDataset[] dataSets = OperationValidationUtil.findKorWorkSheetTcDataset(opRevision);
		if(dataSets!=null && dataSets.length>0){
			dataSet = dataSets[0];
		}
		
		if(dataSet!=null){
			File workInstructionFile = OperationValidationUtil.getExcelFile((String)null, dataSet);
			Date tcmodifiedDate = OperationValidationUtil.getFileLastModifiedDate(dataSet);
			
			if(workInstructionFile!=null && workInstructionFile.exists()==true){

				if(nfWorkInstructionFile!=null && nfWorkInstructionFile.exists()==true){
					
					
					// ������ ������ Upload �����.
					Long newFileLastModified = nfWorkInstructionFile.lastModified();
					Date newFileLastModifiedDate = new Date(newFileLastModified);
					
					// TC�� ��ϵ� Reference�� ���� �������� N/F ��ο� �ִ� work instruction ���Ϻ���
					// ���� ��� Update ����� �Ǵ°���.
					if(tcmodifiedDate.before(newFileLastModifiedDate)==true){
						haveWorkInstructionUpdateTaget = true;
					}
				}
			}else{
				haveWorkInstructionUpdateTaget = true;
			}
		}else{
			haveWorkInstructionUpdateTaget = true;
		}
		
		message = "#### W/I Update target : "+operationItemId+" -> haveWorkInstructionUpdateTaget :  "+haveWorkInstructionUpdateTaget;
		System.out.println(message);
		peIFExecution.writeLogTextLine(message);
		
		// WorkInstruction�� Update �� �ʿ䰡 ���°�� ���̻� ����  ���� �ʴ´�.
		if(haveWorkInstructionUpdateTaget==false){
			return;
		}
		
		// Work Instruction�� �ٽ� Attache �ؾ� �ϹǷ� ������ Work Instruction�� ������ ���� ó���Ѵ�.
		if (dataSets != null && dataSets.length > 0) {
			try {
				opRevision.remove(SDVTypeConstant.PROCESS_SHEET_KO_RELATION,
						dataSets);
			} catch (TCException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < dataSets.length; i++) {
				try {
					dataSets[i].delete();
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
		
		String message2 = "#### W/I Update : "+operationItemId+" -> "+nfFilePath;
		System.out.println(message2);
		peIFExecution.writeLogTextLine(message2);

		if(nfWorkInstructionFile==null || (nfWorkInstructionFile!=null && nfWorkInstructionFile.exists()==false)){
			// Work Instruction Update ������� �Ǿ� ������ N/F Data�� ������ File�� ���� ��� �켱�� Template File��
			// Attache �Ѵ�.
			
			String itemId = opRevision
					.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			String revision = opRevision
					.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
			TCComponentDataset procDataSet = SDVBOPUtilities.getTemplateDataset(
					"M7_TEM_DocItemID_ProcessSheet_Kor", itemId + "/" + revision,
					itemId);
			opRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, procDataSet);
		}else if(nfWorkInstructionFile!=null && nfWorkInstructionFile.exists()==true){
			// N/F�� �ִ� File�� ã�Ƽ� Work Instruction Dataset�� �����ϰ� Attach �Ѵ�. 
			TCComponentDataset procDataSet = SDVBOPUtilities.createDataset(nfFilePath.trim());
			opRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, procDataSet);
		}
	}

	/**
	 * MECO�� ������ Item Revision�� Solution Item�� ���δ�.
	 * 
	 * @method AddRevisionToMecoRevision
	 * @date 2013. 11. 22.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void addRevisionToMecoRevision() throws Exception {
		
		TCComponentItemRevision mecoItemRevision = peIFExecution.getMecoRevision();

		if (this.meOperationRevision
				.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_MECO_NO)) {
			try {
				this.meOperationRevision.getTCProperty(
						SDVPropertyConstant.OPERATION_REV_MECO_NO)
						.setReferenceValue(mecoItemRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		// MECO�� �����ϱ�
		// [NON-SR][2016.01.07] taeku.jeong PE->TC Migration Test ������
		// Exception�߻����� Problem, Solution Items�� �̹� �����ϴ��� Check �ϵ��� ������.
		if (this.oldMEOperationRevision!=null && 
				CustomUtil.isExistInProblemItems( 
							(TCComponentChangeItemRevision) peIFExecution.getMecoRevision(),
							this.oldMEOperationRevision
						) == false
				) {

			TCComponentItemRevision tempItemRevision = 
					(TCComponentItemRevision) this.meOperationRevision.getReferenceProperty(
				SDVPropertyConstant.OPERATION_REV_MECO_NO
					);
			if(tempItemRevision!=null && 
					tempItemRevision.equals(mecoItemRevision)==false){
				try {
					((TCComponentChangeItemRevision) mecoItemRevision).add(
							SDVTypeConstant.MECO_PROBLEM_ITEM,
							this.oldMEOperationRevision);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		if (this.meOperationRevision!=null && 
				CustomUtil.isExistInSolutionItems(
						(TCComponentChangeItemRevision) mecoItemRevision,
						this.meOperationRevision) == false) {
			try {
				((TCComponentChangeItemRevision) mecoItemRevision).add(
						SDVTypeConstant.MECO_SOLUTION_ITEM,
						this.meOperationRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

}
