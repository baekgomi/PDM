/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.job.peif;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivitySubData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EndItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EquipmentData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SubsidiaryData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ToolData;
import com.symc.plm.me.sdv.service.migration.work.peif.NEWPEImportDataCreator;
import com.symc.plm.me.sdv.service.migration.work.peif.NEWPEImportDataReader;
import com.symc.plm.me.sdv.view.migration.NEWPEIFMigrationViewControll;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentCCObject;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentStructureContext;
import com.teamcenter.rac.kernel.TCException;

/**
 * Class Name : PEIFTCDataExecuteJob Class Description :
 * 
 * @date 2013. 11. 20.
 * 
 */
public class NewPEIFExecution {
	
	private NEWPEIFMigrationViewControll peIFMigrationViewControll;

	private String mecoNo;
	private TCComponentItemRevision mecoRevision;
	private String workFolderPath;
	private boolean isOverride = false;

	private TCComponentBOPWindow processWindow;
	private TCComponentMfgBvrProcess processLine;
	private String lineProcessId;

	private TCComponentBOMWindow productWindow;
	private TCComponentBOMLine productLine;
	private String productItemId;

	private boolean isReInterface = false;

	private boolean forcedStopFlag = false;
	private boolean isEndOfProcess = false;

	private Tree tree;
	private LineItemData itemLineData;

	private NEWPEImportDataReader peImportDataReaderUtil;
	// ���� P/G ���� �ִ��ǵ� ��ü���� �ʿ伺�� �ð������� ���� �ɵ�.
	// ����ν�� �߰����� �۾��� �ϱ�� �ð������� ...
	private NEWPEImportDataCreator peImportDataCreator;

	// Validation Utility ����
	OperationValidationUtil operationValidationUtil;
	SubActivityValidationUtil subActivityValidationUtil;
	EndItemValidationUtil endItemValidationUtil;
	FacilityValidationUtil facilityValidationUtil;
	ToolValidationUtil toolValidationUtil;
	SubsidiaryValidationUtil subsidiaryValidationUtil;
	
	private long started; // ���� �ð�

	public NewPEIFExecution(NEWPEIFMigrationViewControll peIFMigrationViewControll) {

		this.peIFMigrationViewControll = peIFMigrationViewControll;

		this.processLine = this.peIFMigrationViewControll
				.getPeIFMigrationViewPane().getProcessLine();
		if (this.processLine != null) {
			try {
				this.processWindow = (TCComponentBOPWindow) this.processLine
						.window();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

		this.mecoNo = this.peIFMigrationViewControll.getPeIFMigrationViewPane()
				.getMecoTextValue();
		try {
			this.mecoRevision = ImportCoreService.getMecoRevision(this.mecoNo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.workFolderPath = this.peIFMigrationViewControll
				.getPeIFMigrationViewPane().getWorkPath();
		this.isOverride = this.peIFMigrationViewControll
				.getPeIFMigrationViewPane().getOverride();

		this.productWindow = getProductWindow();

		try {
			this.productLine = this.productWindow.getTopBOMLine();
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (this.productLine != null) {
			try {
				productItemId = this.productLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * ���� Interface�� �ǽ��ϱ����� Validation�� �����ϸ鼭 Validation ����� Tree Node ������
	 * ��� �ϴ� ��Ȱ�� ���� �ϵ��� �Ѵ�.
	 */
	public boolean runTcDataValidation() {
		
		boolean isOk = false;
		
		started = Calendar.getInstance().getTimeInMillis();

		peImportDataReaderUtil = new NEWPEImportDataReader(workFolderPath);
		Exception readDataException = null;
		try {
			peImportDataReaderUtil.initMigrationInputData(this.mecoNo);
		} catch (Exception e1) {
			readDataException = e1;
		}
		
		if(readDataException==null){
			peImportDataReaderUtil.setProcessWindow(this.processWindow);

			try {
				lineProcessId = processLine.getItem().getProperty("item_id");
			} catch (TCException e) {
				e.printStackTrace();
			}

			writeLogTextLine("Product Item Id  : " + this.productItemId);
			System.out.println("Product Item Id  : " + this.productItemId);
			writeLogTextLine("Line Process Id  : " + this.lineProcessId);
			System.out.println("Line Process Id  : " + this.lineProcessId);
			writeLogTextLine("********** Start of Validation **********");
			System.out.println("********** Start of Validation **********");

			expandTcLineData();

			writeLogTextLine("********** End of Validation **********");
			System.out.println("********** End of Validation **********");
			
			isOk = true;
		}else{
			writeLogTextLine("Data Read Exception!!");
			String message = readDataException.getMessage();
			writeLogTextLine(message);
			System.out.println(message);
		}
		
		writeLogTextLine("\n Elapsed Time : "+getElapsedTime());
		System.out.println("\n Elapsed Time : "+getElapsedTime());
		
		System.gc();
		
		System.out.println("making log file");
		
		peImportDataCreator = new NEWPEImportDataCreator(this);
		if(this.workFolderPath!=null && this.workFolderPath.trim().length()>0){
			DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
			String time = df.format(new Date());
			String logFilePath = this.workFolderPath+"\\ValidationLog["+this.lineProcessId+"]"+time+".txt";
			peIFMigrationViewControll.updateLogTextToFile(logFilePath);
		}
		
		System.out.println("Log file was made");
		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return isOk;

	}

	/**
	 * Validation ���Ŀ� ���� Interface�� ���� �ϵ��� �Ѵ�.
	 */
	public void runTcDataInterface() {

		peIFMigrationViewControll.clearLogText();
		writeLogTextLine("Start of Execution");
		
		started = Calendar.getInstance().getTimeInMillis();
		
		boolean isChangeInterfaceTarget = peImportDataCreator.isChangeInterfaceTarget();
		if(isChangeInterfaceTarget==true){
			boolean haveAccessWrite = peImportDataCreator.isLineHaveChangeAccessRight();
			if(haveAccessWrite==true){
				peImportDataCreator.expandAndUpdateLineItemData();
			}else{
				writeLogTextLine("\nYou must ensure that you have permission to modify the target line.\n");
			}
		}else{
			writeLogTextLine("\nThere is no data to modify validation results.\n");
		}
		

		writeLogTextLine("End of Execution");
		System.out.println("End of Execution");

		String elapsedTime = "\n Elapsed Time : "+getElapsedTime();
		writeLogTextLine(elapsedTime);
		System.out.println(elapsedTime);
		
		System.gc();
		
		System.out.println("making log file");
		
		peImportDataCreator = new NEWPEImportDataCreator(this);
		if(this.workFolderPath!=null && this.workFolderPath.trim().length()>0){
			DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm");
			String time = df.format(new Date());
			String logFilePath = this.workFolderPath+"\\ExecuteLog["+this.lineProcessId+"]"+time+".txt";
			peIFMigrationViewControll.updateLogTextToFile(logFilePath);
		}
		
		System.out.println("Log file was made");
		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void writeLogTextLine(String logMessage){
		peIFMigrationViewControll.writeLogTextLine(logMessage);
	}
	
	public void redrawUI(){
		peIFMigrationViewControll.redrawUI();
	}

	/**
	 * Job �������� ���� Ȯ��
	 * 
	 * @return
	 */
	public boolean isForcedStopFlag() {
		return this.forcedStopFlag;
	}

	/**
	 * Job �������� ���� ����
	 * 
	 * @param forcedStopFlag
	 */
	public void setForcedStopFlag(boolean forcedStopFlag) {
		this.forcedStopFlag = forcedStopFlag;
	}

	private void expandTcLineData() {

		NodeList childOperationNodeList = peImportDataReaderUtil
				.getAllChildOperationBOMLineNodeList(lineProcessId);
		
		ArrayList<String> childOperationIdList = peImportDataReaderUtil
				.getOperationItemIdListFromOperationBOMLineNodeList(childOperationNodeList);

		this.peIFMigrationViewControll.createTreeColumn();
		this.peIFMigrationViewControll.treeRemoveAll();
		this.itemLineData = this.peIFMigrationViewControll.createLineItemData(
				lineProcessId, processLine);

		// ---------------------------------------------------------
		// Tc�� BOMLine�� ���� �Ѵ�.
		// ---------------------------------------------------------
		AIFComponentContext[] childNodes = null;
		try {
			childNodes = processLine.getChildren();
		} catch (TCException e) {
			e.printStackTrace();
		}

		operationValidationUtil = new OperationValidationUtil(this);

		for (int i = 0; childNodes != null && i < childNodes.length; i++) {
			
			//waite();
			
			TCComponentBOMLine tempOperationBOMLine = (TCComponentBOMLine) childNodes[i]
					.getComponent();
			if (tempOperationBOMLine != null) {

				String bomLineType = null;
				try {
					bomLineType = tempOperationBOMLine
							.getProperty(SDVPropertyConstant.BL_OBJECT_TYPE);
				} catch (TCException e) {
					e.printStackTrace();
				}

				if (bomLineType == null
						|| (bomLineType != null && bomLineType
								.trim()
								.equalsIgnoreCase(
										SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) == false)) {
					continue;
				}

				String operationItemId = null;
				try {
					operationItemId = tempOperationBOMLine
							.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				} catch (TCException e) {
					e.printStackTrace();
				}

				if (operationItemId == null
						|| (operationItemId != null && operationItemId.trim()
								.length() < 1)) {
					continue;
				}

				// Operation BOMLine�� �����ϴ� N/F Data�� BOMLIne ���� �� Operation
				// Master ������ ã�� �´�.
				Node currentOperationLineNode = peImportDataReaderUtil
						.getOperationBOMLineNode(this.lineProcessId,
								operationItemId);
				Node operationMasterNode = peImportDataReaderUtil
						.getOperationMasterNode(operationItemId);

				// Operation Validation ���� ���Ȯ��
				// Operation Master Data Validation �κ� �߰� ���� �ؾ���.
				boolean isValideOperation = operationValidationUtil.isValide(
						tempOperationBOMLine, currentOperationLineNode,
						operationMasterNode);

				// Tc�� BOMLine�� N/F�� Operatoin ������ �̿��� Tree Table�� Operation
				// Tree Node�� �߰� �Ѵ�.
				OperationItemData tempOperationItemData = this.peIFMigrationViewControll
						.addOperationItemData(tempOperationBOMLine,
								currentOperationLineNode, operationMasterNode);

				if (tempOperationItemData == null) {
					continue;
				}

				tempOperationItemData
						.setValidationResult(operationValidationUtil);
				
				// ���� Server�� ��� ��� Volume�� Copy���� �ʱ� ������ ��������� �����ϴ� �����۾� ǥ�ؼ���
				// ���� Server�� Volumn���� �������� �ʴ� ��찡 �ִ�
				// �̷���� Operation�� �����ϸ鼭 ������ �߻� �� �� �ִµ� �̰��� �����ϱ� ���� ��ġ�� ������.
				tempOperationItemData.setHaveWorkInstructionVolumeFileError(operationValidationUtil.isFileNotFoundInVolume());
				
				if(operationValidationUtil.isSameInstruction()==false){
					tempOperationItemData.setWorkInstructionUpdateTarget(true);
				}else{
					tempOperationItemData.setWorkInstructionUpdateTarget(false);
				}

				expandTcOperationChildNodeData(tempOperationItemData);
				expandTcActivityData(tempOperationItemData);

				// Tc���� �������� ���� N/F���� �ִ� Operation Id�� ���� ���´�.
				if (childOperationIdList != null
						&& childOperationIdList.contains(operationItemId) == true) {
					childOperationIdList.remove(operationItemId);
				}

				// Operatoin�� Validation �Ϸ� ǥ�� �ϴ� �κ�
				if(operationValidationUtil.haveMajorError==true){
					tempOperationItemData.setStatus(TCData.STATUS_ERROR);
				}else if(tempOperationItemData.getMasterDataNode()==null){
						tempOperationItemData.setStatus(TCData.STATUS_ERROR, "Operation Master Data Not Found!!");
				}else{
					tempOperationItemData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
				}
				
			}

		}

		// ---------------------------------------------------------
		// TC�� Line�� ���Ե������� Interface N/F���� �ִ� Operation ������ ���� �Ѵ�.
		// ---------------------------------------------------------
		for (int i = 0; childOperationIdList != null
				&& i < childOperationIdList.size(); i++) {

			// operatoinItemId
			String operationItemId = childOperationIdList.get(i);
			if (operationItemId == null
					|| (operationItemId != null && operationItemId.trim()
							.length() < 1)) {
				continue;
			}

			Node currentOperationLineNode = peImportDataReaderUtil
					.getOperationBOMLineNode(this.lineProcessId,
							operationItemId);
			Node operationMasterNode = peImportDataReaderUtil
					.getOperationMasterNode(operationItemId);

			// N/F�������� ������ �̿��� Operation�� �߰��Ѵ�.
			TCComponentBOMLine tempBOMLine = null;

			boolean isValideOperation = operationValidationUtil.isValide(
					tempBOMLine, currentOperationLineNode, operationMasterNode);

			OperationItemData tempOperationItemData = this.peIFMigrationViewControll
					.addOperationItemData(tempBOMLine,
							currentOperationLineNode, operationMasterNode);

			tempOperationItemData.setValidationResult(operationValidationUtil);

			if (tempOperationItemData != null) {

				// Parent Node�� Child Node�� �߰��� ���� ��� �Ѵ�.
				TCData tempTCData = (TCData) tempOperationItemData;
				if (tempTCData != null) {
					TCData tempParentTCData = (TCData) tempTCData
							.getParentItem();
					if (tempParentTCData != null) {
						tempParentTCData.setChildNodeAddedFlag(true);
					}
				}

				// Attribute�� �д� ���1
				// String tempOperationItemId = null;
				// tempOperationItemId =
				// ((Element)currentOperationNode).getAttribute("OperationItemId");
				// String elementText =
				// ((Element)currentOperationNode).getTextContent();

				// BOMLine Node Data�� ���� ���� ���� �ϴ� ����� ��� �غ���.
				expandNFOperationChildNodeData(tempOperationItemData);
				expandNFActivityData(tempOperationItemData);

				// Operatoin�� Validation �Ϸ� ǥ�� �ϴ� �κ�
				if(operationValidationUtil.haveMajorError==true){
					tempOperationItemData.setStatus(TCData.STATUS_ERROR);
				}else{
					tempOperationItemData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
				}

			}

		}

		// ----------------------------------------------------------
		// ������� ���� �Ǹ� Tc�� �����鼭 �����ɰ�, ����ɰ�, ��ȭ ���°�, N/F���� �־ �߰��ɰͿ� ����
		// �񱳹� ���̿����� ǥ�ð� Tree�� ǥ��ȴ�.
		// Activity Root�� ��� ������ Activity�� Property �Ǵ� Activity�� ���� �ٸ����
		// ������ Activity Root ���� Cut �ϰ� ���ο� Activity Root�� �߰��ϰ� ���� Activity�� �߰�
		// �ؾ� �Ѵ�.
		// ----------------------------------------------------------
		this.peIFMigrationViewControll.setLineItemDataStatus(
				TCData.STATUS_COMPLETED, "Complete");

		if (this.itemLineData != null) {
			itemLineData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
			peIFMigrationViewControll.showTreeItem(itemLineData);
		}

	}

	/**
	 * Tc ������ Operation�� �����Ѵ�.
	 * 
	 * @param tempOperationItemData
	 * @param operationBOMLine
	 */
	private void expandTcOperationChildNodeData(
			OperationItemData tempOperationItemData) {

		String operationItemId = tempOperationItemData.getItemId();
		TCComponentBOMLine currentOperationBOPLine = tempOperationItemData
				.getBopBomLine();

		AIFComponentContext[] currentOperationBOPLineChildNodes = null;
		try {
			currentOperationBOPLineChildNodes = currentOperationBOPLine
					.getChildren();
		} catch (TCException e) {
			e.printStackTrace();
		}

		if (endItemValidationUtil == null) {
			endItemValidationUtil = new EndItemValidationUtil(this);
		}
		if (facilityValidationUtil == null) {
			facilityValidationUtil = new FacilityValidationUtil(this);
		}
		if (toolValidationUtil == null) {
			toolValidationUtil = new ToolValidationUtil(this);
		}
		if (subsidiaryValidationUtil == null) {
			subsidiaryValidationUtil = new SubsidiaryValidationUtil(this);
		}
		if (subActivityValidationUtil == null) {
			subActivityValidationUtil = new SubActivityValidationUtil(this);
		}

		Vector<String> endItemValidateVector = new Vector<String>();
		Vector<String> toolValidateVector = new Vector<String>();
		Vector<String> subsidiaryValidateVector = new Vector<String>();
		Vector<String> facilityValidateVector = new Vector<String>();

		for (int i = 0; currentOperationBOPLineChildNodes != null
				&& i < currentOperationBOPLineChildNodes.length; i++) {

			TCComponentBOMLine currentOperationChildBOPLine = (TCComponentBOMLine) currentOperationBOPLineChildNodes[i]
					.getComponent();
			if (currentOperationChildBOPLine == null) {
				continue;
			}

			String currentChildBOPLineType = null;
			String currentChildBOPLineItemId = null;
			String currentChildBOPLineAbsOccId = null;
			String currentChildBOPLineSeqNo = null;

			try {
				currentChildBOPLineType = currentOperationChildBOPLine
						.getProperty(SDVPropertyConstant.BL_OBJECT_TYPE);
				currentChildBOPLineItemId = currentOperationChildBOPLine
						.getProperty(SDVPropertyConstant.BL_ITEM_ID);
				currentChildBOPLineAbsOccId = currentOperationChildBOPLine
						.getProperty(SDVPropertyConstant.BL_ABS_OCC_ID);
				currentChildBOPLineSeqNo = currentOperationChildBOPLine
						.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
			} catch (TCException e) {
				e.printStackTrace();
			}

			String formatedSeqStr = null;
			double doubleSeqNo = 0.0;
			if (currentChildBOPLineSeqNo != null
					&& currentChildBOPLineSeqNo.trim().length() > 0
					&& currentChildBOPLineSeqNo.trim().equalsIgnoreCase("NULL") == false) {
				try {
					doubleSeqNo = Double.parseDouble(currentChildBOPLineSeqNo
							.trim());
					formatedSeqStr = "" + doubleSeqNo;
				} catch (java.lang.NumberFormatException e) {
					formatedSeqStr = currentChildBOPLineSeqNo;
				}
			}
			
			if(formatedSeqStr==null || (formatedSeqStr!=null && formatedSeqStr.trim().length()<1)){
				formatedSeqStr = "";
			}

			String childNodeKeyStr = operationItemId.trim() + "_"
					+ currentChildBOPLineItemId.trim() + "_"
					+ formatedSeqStr.trim();
			System.out.println("childNodeKeyStr = " + childNodeKeyStr);

			if (currentChildBOPLineType != null
					&& (currentChildBOPLineType.trim().equalsIgnoreCase(
							SDVTypeConstant.EBOM_STD_PART) || currentChildBOPLineType
							.trim().equalsIgnoreCase(
									SDVTypeConstant.EBOM_VEH_PART))) {

				// Vialidation�� �ǽ��Ѵ�.
				boolean isValideEndItem = endItemValidationUtil.isValide(
						(TCComponentBOPLine)currentOperationChildBOPLine, 
						operationItemId);

				String occThreadUid = endItemValidationUtil.getPartCopyStableOccThreadUid();
				String parentOccThreadUid = endItemValidationUtil.getParentCopyStableOccThreadUid();
				
				waite();
				
				// -----------------------------------
				// EndItemData�� ����
				// -----------------------------------
				EndItemData endItemData = this.peIFMigrationViewControll
						.addEndItemItemData(tempOperationItemData,
								endItemValidationUtil.getProcessEndItemBOPLine(),
								endItemValidationUtil.getmBOMPartBOMLine(), 
								endItemValidationUtil.getEndItemNFBOMLineNodeData());
				
				endItemData.setValidationResult(endItemValidationUtil);
				endItemData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);

				String endItemValidateKeyStr = null;
				if(parentOccThreadUid!=null && occThreadUid!=null){
					endItemValidateKeyStr = operationItemId.trim() + "_"
							+ parentOccThreadUid.trim() + "_" + 
							occThreadUid.trim();
				}else{
					endItemValidateKeyStr = operationItemId.trim() + "_NULL_NULL";
				}
				
				endItemValidateVector.add(endItemValidateKeyStr.trim());

			} else if (currentChildBOPLineType != null
					&& currentChildBOPLineType.trim().equalsIgnoreCase(
							SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {

				System.out.println("operationItemId = " + operationItemId
						+ ", currentChildBOPLineSeqNo = "
						+ currentChildBOPLineSeqNo);

				// Tool
				Node toolBOMLineNode = peImportDataReaderUtil
						.getToolBOMLineNode(operationItemId,
								currentChildBOPLineSeqNo);

				System.out.println("toolBOMLineNode = " + toolBOMLineNode);

				String toolId = null;
				if (toolBOMLineNode != null) {
					if (((Element) toolBOMLineNode).getElementsByTagName("K") != null) {
						if (((Element) toolBOMLineNode).getElementsByTagName(
								"K").getLength() > 0) {
							toolId = ((Element) toolBOMLineNode)
									.getElementsByTagName("K").item(0)
									.getTextContent();
						}
					}
				} else {
					toolId = currentChildBOPLineItemId;
				}

				Node toolMasterNode = peImportDataReaderUtil
						.getToolMasterNode(toolId);

				// Validation ���� ���Ȯ��
				boolean isValideTool = toolValidationUtil.isValide(
						currentOperationChildBOPLine, toolBOMLineNode,
						toolMasterNode);
				waite();

				ToolData toolData = this.peIFMigrationViewControll
						.addToolItemData(tempOperationItemData,
								currentOperationChildBOPLine, toolBOMLineNode,
								toolMasterNode);

				toolData.setValidationResult(toolValidationUtil);
				if(toolData.getMasterDataNode()==null){
					toolData.setStatus(TCData.STATUS_ERROR, "Tool Master Data Not Found!!");
				}else{
					toolData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
				}
				toolValidateVector.add(childNodeKeyStr.trim());

			} else if (currentChildBOPLineType != null
					&& currentChildBOPLineType.trim().equalsIgnoreCase(
							SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {

				// Subsidiary
				String nfSeqNo = NewPEIFExecution
						.getNfFindNoFromTcData(currentChildBOPLineSeqNo);
				Node subsidiaryBOMLineNode = peImportDataReaderUtil
						.getSubsidiaryBOMLineNode(operationItemId, nfSeqNo);

				// Validation ���� ���Ȯ��
				boolean isValideSubsidiary = subsidiaryValidationUtil.isValide(
						currentOperationChildBOPLine, subsidiaryBOMLineNode);
				waite();

				SubsidiaryData subsidiaryData = this.peIFMigrationViewControll
						.addSubsidiaryItemData(tempOperationItemData,
								currentOperationChildBOPLine,
								subsidiaryBOMLineNode);

				subsidiaryData.setValidationResult(subsidiaryValidationUtil);

				// ������� Master Data�� �����Ƿ� Master Data�� ã�� �ʿ䰡 ����.
//				if(subsidiaryData.getMasterDataNode()==null){
//					subsidiaryData.setStatus(TCData.STATUS_ERROR, "Subsidiary Master Data Not Found!!");
//				}else{
					subsidiaryData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
//				}
				subsidiaryValidateVector.add(childNodeKeyStr.trim());

			} else if (currentChildBOPLineType != null
					&& (currentChildBOPLineType.trim().equalsIgnoreCase(
							SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM)
							|| currentChildBOPLineType.trim().equalsIgnoreCase(
									SDVTypeConstant.BOP_PROCESS_GUN_ITEM)
							|| currentChildBOPLineType
									.trim()
									.equalsIgnoreCase(
											SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM) || currentChildBOPLineType
							.trim().equalsIgnoreCase(
									SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM))) {

				// Facility
				Node facilityBOMLineNode = peImportDataReaderUtil
						.getFacilityBOMLineNode(operationItemId,
								currentChildBOPLineSeqNo);
				
				Node equipmentMasterNode = peImportDataReaderUtil
						.getFacilityMasterNode(currentChildBOPLineItemId);

				// Validation ���� ���Ȯ��
				boolean isValideFacility = facilityValidationUtil.isValide(
						currentOperationChildBOPLine, facilityBOMLineNode,
						equipmentMasterNode);
				waite();

				EquipmentData equipmentData = this.peIFMigrationViewControll
						.addEquipmentItemData(tempOperationItemData,
								currentOperationChildBOPLine,
								facilityBOMLineNode, equipmentMasterNode);

				equipmentData.setValidationResult(facilityValidationUtil);
				if(equipmentData.getMasterDataNode()==null){
					equipmentData.setStatus(TCData.STATUS_ERROR, "Equipment Master Data Not Found!!");
				}else{
					equipmentData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
				}
				facilityValidateVector.add(childNodeKeyStr.trim());
			}

		}

		// ----------------------------------------------------------------------------------------------------------
		// N/F���� �ִµ� Tc�� ������ Data���� ���� EndItem Node ������ �ִ���Ȯ���Ѵ�.
		// ----------------------------------------------------------------------------------------------------------
		int nfEndItemBOMLineNodeCount = 0;
		int endItemValidateVectorCount = 0;
		NodeList nfEndItemBOMLineNodeList = peImportDataReaderUtil
				.getOperationChildEndItemBOMLineNodeList(operationItemId);
		if (nfEndItemBOMLineNodeList != null
				&& nfEndItemBOMLineNodeList.getLength() > 0) {
			nfEndItemBOMLineNodeCount = nfEndItemBOMLineNodeList.getLength();
		}
		if (endItemValidateVector != null && endItemValidateVector.size() > 0) {
			endItemValidateVectorCount = endItemValidateVector.size();
		}
		if (endItemValidateVectorCount != nfEndItemBOMLineNodeCount) {
			for (int i = 0; nfEndItemBOMLineNodeList != null
					&& i < nfEndItemBOMLineNodeList.getLength(); i++) {
				
				Element endItemNfBOMLineNode = (Element) nfEndItemBOMLineNodeList.item(i);

				boolean isValideEndItem = endItemValidationUtil.isValide(
						endItemNfBOMLineNode, 
						operationItemId);
				waite();

				// Attribute�� �д´�.
				String parentOccThreadPuid = endItemValidationUtil.getParentCopyStableOccThreadUid();
				String endItemOccThreadPuid = endItemValidationUtil.getPartCopyStableOccThreadUid();
				
				String findKey = operationItemId.trim() + "_" + 
						parentOccThreadPuid.trim() +"_"+
						endItemOccThreadPuid.trim();
				
				if (endItemValidateVector.contains(findKey) == true) {
					continue;
				}

				EndItemData endItemData = this.peIFMigrationViewControll
						.addEndItemItemData(tempOperationItemData,
								endItemValidationUtil.getProcessEndItemBOPLine(),
								endItemValidationUtil.getmBOMPartBOMLine(),
								endItemNfBOMLineNode);

				endItemData.setValidationResult(endItemValidationUtil);
				TCComponentBOMLine prodBOMLine = endItemData.getProductBomLine();
				if(prodBOMLine==null){
					peIFMigrationViewControll.writeLogTextLine("Product BOMLine Not Found : "+operationItemId+"->"+endItemData.getItemId()+"("+endItemData.getAbsOccPuids()+")");
					endItemData.setHaveMajorError(true);
					endItemData.setStatus(TCData.STATUS_ERROR, "Validation Complete -> Product part not found");
				}else{
					endItemData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
				}
			}
		}

		// ----------------------------------------------------------------------------------------------------------
		// N/F���� �ִµ� Tc�� ������ Data���� ���� Tool Node ������ �ִ���Ȯ���Ѵ�.
		// ----------------------------------------------------------------------------------------------------------
		int nfToolBOMLineNodeCount = 0;
		int toolValidateVectorCount = 0;
		NodeList nfToolBOMLineNodeList = 
				peImportDataReaderUtil.getOperationChildToolBOMLineNodeList(operationItemId);
		
		if (nfToolBOMLineNodeList != null
				&& nfToolBOMLineNodeList.getLength() > 0) {
			nfToolBOMLineNodeCount = nfToolBOMLineNodeList.getLength();
		}
		if (endItemValidateVector != null && endItemValidateVector.size() > 0) {
			toolValidateVectorCount = endItemValidateVector.size();
		}
		if (toolValidateVectorCount != nfToolBOMLineNodeCount) {
			for (int i = 0; nfToolBOMLineNodeList != null
					&& i < nfToolBOMLineNodeList.getLength(); i++) {

				// Tool
				TCComponentBOMLine tempToolBOMLine = null;

				Element toolBOMLineNode = (Element) nfToolBOMLineNodeList
						.item(i);

				String toolItemId = null;
				if (toolBOMLineNode.getElementsByTagName("K") != null) {
					toolItemId = toolBOMLineNode.getElementsByTagName("K")
							.item(0).getTextContent();
				}

				String nfToolSequenceStr = null;
				if (toolBOMLineNode.getElementsByTagName("N") != null) {
					nfToolSequenceStr = toolBOMLineNode
							.getElementsByTagName("N").item(0).getTextContent();
				}

				String formatedSeqStr = null;
				double doubleSeqNo = 0.0;
				if (nfToolSequenceStr != null
						&& nfToolSequenceStr.trim().length() > 0
						&& nfToolSequenceStr.trim().equalsIgnoreCase("NULL") == false) {
					try {
						doubleSeqNo = Double.parseDouble(nfToolSequenceStr
								.trim());
						formatedSeqStr = "" + doubleSeqNo;
					} catch (java.lang.NumberFormatException e) {
						formatedSeqStr = nfToolSequenceStr;
					}
				}

				String findKey = operationItemId + "_" + toolItemId + "_"
						+ formatedSeqStr;
				if (toolValidateVector.contains(findKey) == true) {
					continue;
				}

				Node toolMasterNode = peImportDataReaderUtil
						.getToolMasterNode(toolItemId);

				// Validation ���� ���Ȯ��
				boolean isValideTool = toolValidationUtil.isValide(
						tempToolBOMLine, toolBOMLineNode, toolMasterNode);
				waite();

				ToolData toolData = this.peIFMigrationViewControll
						.addToolItemData(tempOperationItemData,
								tempToolBOMLine, toolBOMLineNode,
								toolMasterNode);

				toolData.setValidationResult(toolValidationUtil);
				toolData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);

			}
		}

		// ----------------------------------------------------------------------------------------------------------
		// N/F���� �ִµ� Tc�� ������ Data���� ���� Subsisiary Node ������ �ִ���Ȯ���Ѵ�.
		// ----------------------------------------------------------------------------------------------------------
		int nfSubsidiaryBOMLineNodeCount = 0;
		int subsidiaryValidateVectorCount = 0;
		NodeList nfSubsidiaryBOMLineNodeList = peImportDataReaderUtil
				.getOperationChildSubsidiaryBOMLineNodeList(operationItemId);
		if (nfSubsidiaryBOMLineNodeList != null
				&& nfSubsidiaryBOMLineNodeList.getLength() > 0) {
			nfSubsidiaryBOMLineNodeCount = nfSubsidiaryBOMLineNodeList
					.getLength();
		}
		if (endItemValidateVector != null && endItemValidateVector.size() > 0) {
			subsidiaryValidateVectorCount = endItemValidateVector.size();
		}
		if (subsidiaryValidateVectorCount != nfSubsidiaryBOMLineNodeCount) {
			for (int i = 0; nfSubsidiaryBOMLineNodeList != null
					&& i < nfSubsidiaryBOMLineNodeList.getLength(); i++) {

				// Subsidiary
				TCComponentBOMLine tempSubsidiaryBOMLine = null;
				Element subsidiaryBOMLineNode = (Element) nfSubsidiaryBOMLineNodeList
						.item(i);

				String nfSubsidiaryItemId = null;
				if (subsidiaryBOMLineNode.getElementsByTagName("K") != null) {
					nfSubsidiaryItemId = subsidiaryBOMLineNode
							.getElementsByTagName("K").item(0).getTextContent();
				}

				String nfSubsidiarySequenceStr = null;
				if (subsidiaryBOMLineNode.getElementsByTagName("O") != null) {
					nfSubsidiarySequenceStr = subsidiaryBOMLineNode
							.getElementsByTagName("O").item(0).getTextContent();
				}

				String formatedSeqStr = null;
				double doubleSeqNo = 0.0;
				System.out.println("nfSubsidiarySequenceStr = "
						+ nfSubsidiarySequenceStr);
				if (nfSubsidiarySequenceStr != null
						&& nfSubsidiarySequenceStr.trim().length() > 0
						&& nfSubsidiarySequenceStr.trim().equalsIgnoreCase(
								"NULL") == false) {
					String tempValueStr = ImportCoreService
							.conversionSubsidiaryFindNo(nfSubsidiarySequenceStr);
					System.out.println("tempValueStr = " + tempValueStr);
					try {
						doubleSeqNo = Double.parseDouble(tempValueStr);
						formatedSeqStr = "" + doubleSeqNo;
						System.out.println("K1");
					} catch (java.lang.NumberFormatException e) {
						formatedSeqStr = tempValueStr;
						System.out.println("K2");
					}
				}

				System.out.println("formatedSeqStr = " + formatedSeqStr);

				String findKey = operationItemId + "_" + nfSubsidiaryItemId
						+ "_" + formatedSeqStr;
				if (subsidiaryValidateVector.contains(findKey) == true) {
					continue;
				}

				System.out.println("findKey(Sub) = " + findKey);

				// Validation ���� ���Ȯ��
				boolean isValideSubsidiary = subsidiaryValidationUtil.isValide(
						tempSubsidiaryBOMLine, subsidiaryBOMLineNode);
				waite();

				SubsidiaryData subsidiaryData = this.peIFMigrationViewControll
						.addSubsidiaryItemData(tempOperationItemData,
								tempSubsidiaryBOMLine, subsidiaryBOMLineNode);

				subsidiaryData.setValidationResult(subsidiaryValidationUtil);
				subsidiaryData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);

			}
		}

		// ----------------------------------------------------------------------------------------------------------
		// N/F���� �ִµ� Tc�� ������ Data���� ���� Facility Node ������ �ִ���Ȯ���Ѵ�.
		// ----------------------------------------------------------------------------------------------------------
		int nfFacilityBOMLineNodeCount = 0;
		int facilityValidateVectorCount = 0;
		NodeList nfFacilityBOMLineNodeList = peImportDataReaderUtil
				.getOperationChildFacilityBOMLineNodeList(operationItemId);
		if (nfFacilityBOMLineNodeList != null
				&& nfFacilityBOMLineNodeList.getLength() > 0) {
			nfFacilityBOMLineNodeCount = nfFacilityBOMLineNodeList.getLength();
		}
		if (endItemValidateVector != null && endItemValidateVector.size() > 0) {
			facilityValidateVectorCount = endItemValidateVector.size();
		}
		if (facilityValidateVectorCount != nfFacilityBOMLineNodeCount) {
			for (int i = 0; nfFacilityBOMLineNodeList != null
					&& i < nfFacilityBOMLineNodeList.getLength(); i++) {

				// Facility
				TCComponentBOMLine tempFacilityBOMLine = null;

				Element nfFacilityBOMLineNode = (Element) nfFacilityBOMLineNodeList
						.item(i);

				String equitmentItemId = null;
				if (nfFacilityBOMLineNode.getElementsByTagName("K") != null) {
					equitmentItemId = nfFacilityBOMLineNode
							.getElementsByTagName("K").item(0).getTextContent();
				}

				String nfFacilitySequenceStr = null;
				if (nfFacilityBOMLineNode.getElementsByTagName("M") != null) {
					nfFacilitySequenceStr = nfFacilityBOMLineNode
							.getElementsByTagName("M").item(0).getTextContent();
				}

				String formatedSeqStr = null;
				double doubleSeqNo = 0.0;
				if (nfFacilitySequenceStr != null
						&& nfFacilitySequenceStr.trim().length() > 0
						&& nfFacilitySequenceStr.trim()
								.equalsIgnoreCase("NULL") == false) {
					try {
						doubleSeqNo = Double.parseDouble(nfFacilitySequenceStr
								.trim());
						formatedSeqStr = "" + doubleSeqNo;
					} catch (java.lang.NumberFormatException e) {
						formatedSeqStr = nfFacilitySequenceStr;
					}
				}

				String findKey = operationItemId + "_" + equitmentItemId + "_"
						+ formatedSeqStr;
				if (facilityValidateVector.contains(findKey) == true) {
					continue;
				}

				Node nfFacilityBOMLineMasterNode = peImportDataReaderUtil
						.getFacilityMasterNode(equitmentItemId);

				// Validation ���� ���Ȯ��
				boolean isValideFacility = facilityValidationUtil.isValide(
						tempFacilityBOMLine, nfFacilityBOMLineNode,
						nfFacilityBOMLineMasterNode);
				waite();

				EquipmentData equipmentData = this.peIFMigrationViewControll
						.addEquipmentItemData(tempOperationItemData,
								tempFacilityBOMLine, nfFacilityBOMLineNode,
								nfFacilityBOMLineMasterNode);

				equipmentData.setValidationResult(facilityValidationUtil);
				equipmentData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);

			}
		}

	}

	/**
	 * Tc �������������Ǵ� Action ������ �����Ѵ�.
	 * 
	 * @param operationItemId
	 * @param tempOperationItemData
	 * @param operationBOMLine
	 */
	private void expandTcActivityData(OperationItemData operationItemData) {

		/**
		 * Activity Data�� Operation�� �ٷ� ÷�εǴ°��� �ƴ϶� Activity Root Tree Node�� �����ϰ�
		 * �� ���Ͽ� Activity Node�� List �ϴ� �����̴�. ���� Tc�� Activity �������� ���� �Ǵ� ��쿡��
		 * ������ ���ؿ� ���� �ϳ��� �׸��̶� ���̰� ������ ��ü Activity Root Node�� ���� �ϰ� ���ο�
		 * Activity Root Node ���� �ٿ� �ִ� �������� ���� �ؾ� �Ѵ�. 1. Root activity Node�� ������
		 * Child Node�� Activity�� ������ �ٸ� ��� 2. Child Activity���� �̸��� �ٸ� ���� �ִ°�� 3.
		 * Activity�� Property�� �ٸ� ���� �ִ°��
		 * 
		 * �ᱹ ������ Activity �׸��� Validation�� �ʿ��ϰڴ�. ������ Activity�� Validation �����
		 * �ٸ��� �ִ°�� ���ο� Activity Root Node�� �߰��ϰ� ������ Activity Root Node�� ���ؼ��� ����
		 * ó���� ���� �ϴ� ������ ������ �ʿ��ϴ�.
		 */

		String operationItemId = operationItemData.getItemId();

		TCComponent activityRootComponent = null;
		TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operationItemData
				.getBopBomLine();
		try {
			activityRootComponent = bvrOperation
					.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);
		} catch (TCException e) {
			e.printStackTrace();
		}

		boolean activityChanged = false;

		// Operation Id�� �������� N/F�� �ִ� Activity List�� ã�ƿ´�.
		List<Element> activityLineNodeList = peImportDataReaderUtil
				.getActivityLineNodeList(operationItemId);
		int nfActivityNodeCount = activityLineNodeList.size();

		// --------------------------------------
		// Sub Activity ����
		// --------------------------------------
		ActivityMasterData currentActivityMasterData = null;
		TCComponent[] tcCurrentSubActivityLines = null;
		int tcActivityLineCount = 0;

		// Root Activity�� ã�� ������ Child Node�� Activity�� ������ ���� tcCurrentSubActivityLines�� ��´�.
		if (activityRootComponent instanceof TCComponentCfgActivityLine) {
			
			TCComponentMEActivity rootActivity = null;
			try {
				rootActivity = (TCComponentMEActivity) activityRootComponent
						.getUnderlyingComponent();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
			
			if (rootActivity != null) {
				// Tree Table�� ǥ���� Activity Master Data�� ã�� �´�.
				currentActivityMasterData = this.peIFMigrationViewControll
						.addActivityMasterData(operationItemData);
				
				// Tc�� ��ϵ� Root Activity�� Child Node�� Activity�� ���������� List �Ѵ�. 
				try {
					tcCurrentSubActivityLines = ActivityUtils
							.getSortedActivityChildren(rootActivity);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}

			if ((tcCurrentSubActivityLines != null && tcCurrentSubActivityLines.length > 0)) {
				tcActivityLineCount = tcCurrentSubActivityLines.length;
			}
		}


		if (nfActivityNodeCount == tcActivityLineCount) {

			// Operation�� ���� ��ϵ� Activity List�� N/F�� ��ϵ� Activity List�� �� �Ѵ�.
			for (int activityListIndex = 0; 
					tcCurrentSubActivityLines != null && activityListIndex < tcCurrentSubActivityLines.length; 
					activityListIndex++) {

				TCComponentMEActivity currentActivity = null;
				
				TCComponent tempComponent = tcCurrentSubActivityLines[activityListIndex];
				if(tempComponent!=null && tempComponent instanceof TCComponentMEActivity){
					currentActivity = (TCComponentMEActivity) tempComponent;
				}else{
					continue;
				}

				// ���⼭ ���� �񱳸� ���� ����� �߿��ѵ� ���� Test ������ 
				// ������ ���ϴ� ����� ���� Activity�� �����ϴ� �����̱� �����̴�.
				// ���� ������ ���� Activity �̸��� �켱 �� �ϴ� �������� ������ Activity�� ã���� ���ڴ�.
				
				String tcActivityObjectName = null;
				try {
					tcActivityObjectName = currentActivity.getProperty(SDVPropertyConstant.ACTIVITY_OBJECT_NAME);
				} catch (TCException e) {
					e.printStackTrace();
				}
			
				Element activityLineNode = (Element)activityLineNodeList.get(activityListIndex);
				String tempActivitySeqStr = null;
				if (activityLineNode!=null && activityLineNode.getElementsByTagName("K") != null) {
					if (activityLineNode.getElementsByTagName("K").getLength() > 0) {
						tempActivitySeqStr = activityLineNode.getElementsByTagName("K").item(0).getTextContent();
					}
				}
				Element activityMasterDataNode = (Element)peImportDataReaderUtil.getActivityMasterNode(operationItemId, tempActivitySeqStr);
				String tempActivityNameStr = null;
				if (activityMasterDataNode!=null && activityMasterDataNode.getElementsByTagName("J") != null) {
					if (activityMasterDataNode.getElementsByTagName("J").getLength() > 0) {
						tempActivityNameStr = activityMasterDataNode.getElementsByTagName("J").item(0).getTextContent();
					}
				}
				
				// �̸��� �� �Ѵ�.
				boolean isSameName = false;
				if(tcActivityObjectName!=null && tempActivityNameStr!=null){
					if(tcActivityObjectName.trim().equalsIgnoreCase(tempActivityNameStr.trim())==true){
						isSameName = true;
					}
				}
				
				// �̸��� �ٸ� ��� Tc�� Activity�� �ش��ϴ� Data�� N/F���� ã�� ���� ������ ó�� �Ѵ�.
				if(isSameName!=true){
					activityLineNode = null;
					activityMasterDataNode = null;
					activityChanged = true;
				}
				
				 // Sub ActivityNode Data�� �д´�.
				boolean isValideActivity = subActivityValidationUtil.isValide(
						bvrOperation, currentActivity, (Node) activityLineNode,
						activityMasterDataNode, tempActivitySeqStr);

				ActivitySubData activitySubData = this.peIFMigrationViewControll
						.addActivitySubData(operationItemId,
								currentActivityMasterData, currentActivity,
								activityLineNode,
								activityMasterDataNode, tempActivitySeqStr);

				activitySubData.setValidationResult(subActivityValidationUtil);

				if (isValideActivity == false) {
					// Sub Activity�� ����Ȱ��� ������ Activity Master�� ���� �ϰ� ���� �߰� �ؾ�
					// �Ѵ�.
					if (currentActivityMasterData.getDecidedChagneType() == TCData.DECIDED_NO_CHANGE) {
						currentActivityMasterData
								.setDecidedChagneType(TCData.DECIDED_REMOVE);
					}
					activityChanged = true;
				}
				activitySubData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);

			}
		} else if ((nfActivityNodeCount > 0 || tcActivityLineCount > 0)
				&& (nfActivityNodeCount != tcActivityLineCount)) {
			if(currentActivityMasterData!=null){
				currentActivityMasterData.setDecidedChagneType(TCData.DECIDED_REPLACE);
			}
			activityChanged = true;
		}

		if(currentActivityMasterData!=null){
			currentActivityMasterData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
		}

		// Activity�� ������ �ִ°�� ���ο� Activity Root Node�� �߰��ϰ� Activity�� �߰� �Ѵ�.
		if (activityChanged == true && nfActivityNodeCount > 0) {

			if (currentActivityMasterData != null) {
				currentActivityMasterData.setDecidedChagneType(TCData.DECIDED_REMOVE);
			}

			ActivityMasterData newActivityMasterData = this.peIFMigrationViewControll
					.addActivityMasterData(operationItemData);
			newActivityMasterData.setDecidedChagneType(TCData.DECIDED_ADD);
			for (int i = 0; activityLineNodeList != null
					&& i < activityLineNodeList.size(); i++) {
				// Sub ActivityNode Data�� �д´�.
				TCComponentMEActivity currentActivity = null;
				
				Element activityLineNode = activityLineNodeList.get(i);
				String activitySeqString = activityLineNode
						.getElementsByTagName("K").item(0).getTextContent();
				Node activityMasterDataNode = peImportDataReaderUtil
						.getActivityMasterNode(operationItemId,
								activitySeqString);

				System.out.println("@@#2 activitySeqString = "+activitySeqString);
				System.out.println("@@#2 activityLineNode = "+activityLineNode);
				System.out.println("@@#2 activityMasterDataNode = "+activityMasterDataNode);
				
				
				boolean isValideActivity = subActivityValidationUtil.isValide(
						bvrOperation, currentActivity, (Node) activityLineNode,
						activityMasterDataNode, activitySeqString);
				
				ActivitySubData activitySubData = this.peIFMigrationViewControll
						.addActivitySubData(operationItemId,
								newActivityMasterData, currentActivity,
								activityLineNode, activityMasterDataNode,
								activitySeqString);

				activitySubData.setValidationResult(subActivityValidationUtil);

				if (isValideActivity == false) {
					int validationCheckResult = subActivityValidationUtil
							.getCompareResult();
					if (validationCheckResult == SubActivityValidationUtil.COMPARE_RESULT_DIFFERENT) {
						activityChanged = true;
					}
				}

				activitySubData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
			}

			newActivityMasterData.setDecidedChagneType(TCData.DECIDED_ADD);
			newActivityMasterData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);

		}
	}

	/**
	 * N/F �������� �����Ǵ� Operation�� Child Node Data�� Tree�� �߰� �ϰ� ǥ���Ѵ�.
	 * 
	 * @param tempOperationItemData
	 * @param operationNode
	 */
	private void expandNFOperationChildNodeData(
			OperationItemData tempOperationItemData) {

		String operationItemId = tempOperationItemData.getItemId();
		
		if (endItemValidationUtil == null) {
			endItemValidationUtil = new EndItemValidationUtil(this);
		}
		if (subsidiaryValidationUtil == null) {
			subsidiaryValidationUtil = new SubsidiaryValidationUtil(this);
		}
		if (facilityValidationUtil == null) {
			facilityValidationUtil = new FacilityValidationUtil(this);
		}
		if (toolValidationUtil == null) {
			toolValidationUtil = new ToolValidationUtil(this);
		}
		
		NodeList endItemBOMLineNodeList = peImportDataReaderUtil
				.getOperationChildEndItemBOMLineNodeList(operationItemId);
		for (int i = 0; endItemBOMLineNodeList != null
				&& i < endItemBOMLineNodeList.getLength(); i++) {
			// EndItem

			Element nfEndItemBOMLineNodeElement = (Element) endItemBOMLineNodeList
					.item(i);
			
			boolean isValideEndItem = endItemValidationUtil.isValide(
					nfEndItemBOMLineNodeElement, 
					operationItemId);
			waite();

			EndItemData endItemData = this.peIFMigrationViewControll
					.addEndItemItemData(tempOperationItemData,
							endItemValidationUtil.getProcessEndItemBOPLine(),
							endItemValidationUtil.getmBOMPartBOMLine(),
							nfEndItemBOMLineNodeElement);

			endItemData.setValidationResult(endItemValidationUtil);
			TCComponentBOMLine prodBOMLine = endItemData.getProductBomLine();
			if(prodBOMLine==null){
				peIFMigrationViewControll.writeLogTextLine("Product BOMLine Not Found : "+operationItemId+"->"+endItemData.getItemId()+"("+endItemData.getAbsOccPuids()+")");
				endItemData.setHaveMajorError(true);
				endItemData.setStatus(TCData.STATUS_ERROR, "Validation Complete -> Product part not found");
			}else{
				endItemData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
			}
		}

		NodeList subsidiaryNodeList = peImportDataReaderUtil
				.getOperationChildSubsidiaryBOMLineNodeList(operationItemId);
		for (int i = 0; subsidiaryNodeList != null
				&& i < subsidiaryNodeList.getLength(); i++) {
			// Subsidiary
			TCComponentBOMLine tempSubsidiaryBOMLine = null;
			Element subsidiaryBOMLineNode = (Element) subsidiaryNodeList
					.item(i);
			
			// Validation ���� ���Ȯ��
			boolean isValideSubsidiary = subsidiaryValidationUtil.isValide(
					tempSubsidiaryBOMLine, subsidiaryBOMLineNode);
			waite();

			SubsidiaryData subsidiaryData = this.peIFMigrationViewControll
					.addSubsidiaryItemData(tempOperationItemData,
							tempSubsidiaryBOMLine, subsidiaryBOMLineNode);

			subsidiaryData.setValidationResult(subsidiaryValidationUtil);
			
			// ������� Master Data�� �����Ƿ� Master Data�� ã�� �ʿ䰡 ����.
//			if(subsidiaryData.getMasterDataNode()==null){
//				subsidiaryData.setStatus(TCData.STATUS_ERROR, "Subsidiary Master Data Not Found!!");
//			}else{
				subsidiaryData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
//			}
		}

		NodeList facilityNodeList = peImportDataReaderUtil
				.getOperationChildFacilityBOMLineNodeList(operationItemId);
		
		for (int i = 0; facilityNodeList != null
				&& i < facilityNodeList.getLength(); i++) {
			// Facility
			TCComponentBOMLine tempFacilityBOMLine = null;

			Element nfFacilityBOMLineNode = (Element) facilityNodeList.item(i);

			String equitmentItemId = null;
			if (nfFacilityBOMLineNode.getElementsByTagName("K") != null) {
				equitmentItemId = nfFacilityBOMLineNode
						.getElementsByTagName("K").item(0).getTextContent();
			}
			
			Node nfFacilityBOMLineMasterNode = peImportDataReaderUtil
					.getFacilityMasterNode(equitmentItemId);
			
			// Validation ���� ���Ȯ��
			boolean isValideFacility = facilityValidationUtil.isValide(
					tempFacilityBOMLine, nfFacilityBOMLineNode,
					nfFacilityBOMLineMasterNode);
			waite();

			EquipmentData equipmentData = this.peIFMigrationViewControll
					.addEquipmentItemData(tempOperationItemData,
							tempFacilityBOMLine, nfFacilityBOMLineNode,
							nfFacilityBOMLineMasterNode);

			equipmentData.setValidationResult(facilityValidationUtil);
			if(equipmentData.getMasterDataNode()==null){
				equipmentData.setStatus(TCData.STATUS_ERROR, "Equipment Master Data Not Found!!");
			}else{
				equipmentData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
			}
		}

		NodeList toolNodeList = peImportDataReaderUtil
				.getOperationChildToolBOMLineNodeList(operationItemId);
		for (int i = 0; toolNodeList != null && i < toolNodeList.getLength(); i++) {
			// Tool
			TCComponentBOMLine tempToolBOMLine = null;

			Element toolBOMLineNode = (Element) toolNodeList.item(i);

			String toolItemId = null;
			if (toolBOMLineNode.getElementsByTagName("K") != null) {
				toolItemId = toolBOMLineNode.getElementsByTagName("K").item(0)
						.getTextContent();
			}

			Node toolMasterNode = peImportDataReaderUtil
					.getToolMasterNode(toolItemId);

			// Validation ���� ���Ȯ��
			boolean isValideTool = toolValidationUtil.isValide(tempToolBOMLine,
					toolBOMLineNode, toolMasterNode);
			waite();

			ToolData toolData = this.peIFMigrationViewControll.addToolItemData(
					tempOperationItemData, tempToolBOMLine, toolBOMLineNode,
					toolMasterNode);

			toolData.setValidationResult(toolValidationUtil);
			if(toolData.getMasterDataNode()==null){
				toolData.setStatus(TCData.STATUS_ERROR, "Tool Master Data Not Found!!");
			}else{
				toolData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
			}
		}

	}

	/**
	 * N/F �������������Ǵ� Action ������ �����Ѵ�.
	 * 
	 * @param operationItemId
	 * @param tempOperationItemData
	 * @param operationNode
	 */
	private void expandNFActivityData(OperationItemData operationItemData) {

		String operationItemId = operationItemData.getItemId();
		TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operationItemData
				.getBopBomLine();

		if (endItemValidationUtil == null) {
			endItemValidationUtil = new EndItemValidationUtil(this);
		}
		if (facilityValidationUtil == null) {
			facilityValidationUtil = new FacilityValidationUtil(this);
		}
		if (toolValidationUtil == null) {
			toolValidationUtil = new ToolValidationUtil(this);
		}
		if (subsidiaryValidationUtil == null) {
			subsidiaryValidationUtil = new SubsidiaryValidationUtil(this);
		}
		if (subActivityValidationUtil == null) {
			subActivityValidationUtil = new SubActivityValidationUtil(this);
		}

		ActivityMasterData activityMasterData = this.peIFMigrationViewControll
				.addActivityMasterData(operationItemData);

		boolean activityChanged = false;
		// Activity�� ���� TC-PE ���� ��ġ���� �ʾ� ��ü ���� �ؾ� �Ѵ�.
		NodeList activitySubNodeList = peImportDataReaderUtil
				.getOperationChildActivityLineNodeList(operationItemId);
		for (int i = 0; activitySubNodeList != null
				&& i < activitySubNodeList.getLength(); i++) {

			TCComponentMEActivity tempActivityComponent = null;

			Element activityLineNode = (Element) activitySubNodeList.item(i);

			String activitySeq = null;
			if (activityLineNode.getElementsByTagName("K") != null) {
				activitySeq = activityLineNode.getElementsByTagName("K")
						.item(0).getTextContent();
			}

			Node activityLineMasterNode = peImportDataReaderUtil
					.getActivityMasterNode(operationItemId, activitySeq);

			boolean isValideActivity = subActivityValidationUtil.isValide(
					bvrOperation, tempActivityComponent,
					(Node) activityLineNode, activityLineMasterNode,
					activitySeq);

			ActivitySubData activitySubData = this.peIFMigrationViewControll
					.addActivitySubData(operationItemId, activityMasterData,
							tempActivityComponent, activityLineNode,
							activityLineMasterNode, activitySeq);

			activitySubData.setValidationResult(subActivityValidationUtil);
			activitySubData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
		}

		activityMasterData.setDecidedChagneType(TCData.DECIDED_ADD);
		activityMasterData.setStatus(TCData.STATUS_VALIDATE_COMPLETED);
	}

	/**
	 * �־��� Product BOMLine�� Parent Node�� Ȯ���ؼ� Function BOMLine�� ã�� Return �Ѵ�.
	 * 
	 * @param productBOMLine
	 * @return
	 */
	public static TCComponentBOMLine getFunctionBOMLine(
			TCComponentBOMLine productBOMLine) {

		TCComponentItem item = null;
		try {
			item = productBOMLine.getItem();
			String itemType = item.getType();
			if (itemType != null
					&& itemType.trim().equalsIgnoreCase(
							SDVTypeConstant.EBOM_FUNCTION) == true) {

				return productBOMLine;

			} else {
				TCComponentBOMLine parentLine = productBOMLine.parent();
				String parentLineType = productBOMLine.getItem().getType();

				if (parentLine == null
						|| (parentLine != null && (parentLineType
								.trim()
								.equalsIgnoreCase(SDVTypeConstant.EBOM_MPRODUCT) == true || parentLineType
								.trim().equalsIgnoreCase(
										SDVTypeConstant.EBOM_PRODUCT_ITEM) == true))) {
					return (TCComponentBOMLine) null;
				} else {
					return getFunctionBOMLine(parentLine);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return (TCComponentBOMLine) null;

	}

	public static String getOccThreadUid(TCComponentBOMLine bomLine) {
		
		String occThreadUid = null;
		try {
			occThreadUid = bomLine.getProperty("bl_clone_stable_occurrence_id");
			// occurrenceUid = bomLine.getProperty("bl_occurrence_uid");
			// absOccId = bomLine.getProperty("bl_abs_occ_id");
			// absOccUidInTopLineContext =bomLine.getProperty("bl_absocc_uid_in_topline_context");
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		return occThreadUid;
	}

	/**
	 * N/F�� seq No�� Tc�� Find No. ���·� ��ȯ�ؼ� Return �Ѵ�. (Import) (a=>510 ~ z=>760)
	 * (a=510 ~ z=760)
	 * 
	 * @param seqNo
	 * @return
	 */
	public static String getTcFindNoFromNfData(String seqNo) {

		if (StringUtils.isEmpty(seqNo)) {
			return "";
		}
		// trim, lowerCase
		seqNo = seqNo.trim().toLowerCase();
		if (seqNo.length() != 1) {
			return seqNo;
		}
		int charCode = seqNo.charAt(0);
		// a=97 ~ z=122 ������ SeqNo �� Find No.�� ����
		if (charCode >= 97 && charCode <= 122) {
			int findNo = charCode - 97;// a=0 ~ z=25
			findNo = findNo + 1;// a=1 ~ z=26
			findNo = findNo * 10; // a=10 ~ z=260
			findNo = findNo + 500;// a=510 ~ z=760
			return ImportCoreService.getFindNoFromSeq(findNo + "");
		}
		return seqNo;
	}

	/**
	 * Teamcenter�� 510, 760 ���� ���·� �Ǿ��ִ� Find No�� N/F�� a,b,z ���� ���·� ��ȯ�ؼ� Return
	 * �Ѵ�. Ex : (510=>a ~ 760=>z)
	 * 
	 * @param findNo
	 * @return
	 */
	public static String getNfFindNoFromTcData(String findNo) {

		if (StringUtils.isEmpty(findNo)) {
			return "";
		}
		// trim, lowerCase
		findNo = findNo.trim();
		try {
			int number = Integer.parseInt(findNo);
			if (number >= 510 && number <= 760) {
				if (number % 10 != 0) {
					return findNo;
				}
				number = number / 10; // 510(a) -> 51
				number = number - 51; // 51(a) -> 0
				number = number + 97;// a=97 ~ z=122
				return new String(Character.toChars(number));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return findNo;
		}
		return findNo;
	}

	public static void waite() {

//		try {
//			Thread.sleep(50);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		// OS�� ����ϰ� ���� ������ Sleep �ϵ��� �Ѵ�.
		// �̷��� �ϸ� UI�� ������� �̶�� �޽����� ������ �ʴ´�.
		// if(!Display.getCurrent().readAndDispatch()){
		// Display.getCurrent().sleep();
		// }
		if (!Display.getDefault().readAndDispatch()) {
			Display.getDefault().sleep();
		}

	}
	
	public NEWPEImportDataReader getPeImportDataReaderUtil() {
		return peImportDataReaderUtil;
	}

	public void setPeImportDataReaderUtil(NEWPEImportDataReader peImportDataReaderUtil) {
		this.peImportDataReaderUtil = peImportDataReaderUtil;
	}

	public NEWPEIFMigrationViewControll getPeIFMigrationViewControll() {
		return peIFMigrationViewControll;
	}

	public TCComponentItemRevision getMecoRevision() {
		return mecoRevision;
	}

	public String getMecoNo() {
		return mecoNo;
	}

	public String getProductItemId() {
		return productItemId;
	}

	/**
	 * Program�� ���۵Ǹ鼭 ����Ǵ� Function���� ���Ǵ� �Լ���. Process
	 * 
	 * @return
	 */
	public TCComponentBOMWindow getProductWindow() {
	
		TCComponentBOMWindow window = null;
	
		TCComponentCCObject ccObject = null;
	
		if (this.processWindow != null) {
			ccObject = this.processWindow.getCC();
		}
	
		if (ccObject == null) {
			return window;
		}
	
		TCComponent[] structureContexts = null;
		try {
			structureContexts = ccObject.getStructureContexts();
		} catch (TCException e) {
			e.printStackTrace();
		}
	
		for (int i = 0; structureContexts != null
				&& i < structureContexts.length; i++) {
			if (structureContexts[i] != null
					&& structureContexts[i] instanceof TCComponentStructureContext) {
				TCComponentStructureContext aStructureContext = (TCComponentStructureContext) structureContexts[i];
	
				String structureContextType = aStructureContext.getType();
	
				// MEProcessContext, MEProductContext
				if (structureContextType == null
						|| (structureContextType != null && structureContextType
								.trim().equalsIgnoreCase("MEProductContext") == false)) {
					continue;
				}
	
				List<TCComponentBOMWindow> windowList = aStructureContext
						.getWindows();
				for (int j = 0; windowList != null && j < windowList.size(); j++) {
					window = windowList.get(j);
				}
			}
	
		}
	
		return window;
	}

	public TCComponentMfgBvrProcess getProcessLine() {
		return processLine;
	}

	public TCComponentBOPWindow getProcessWindow() {
		return processWindow;
	}

	public TCComponentBOMLine getProductLine() {
		return productLine;
	}

	public LineItemData getItemLineData() {
		return itemLineData;
	}
	
	/**
	 * started �ð����� ���� �ҿ�� �ð��� ���ڿ��� ���� �ش�.
	 * @return �ҿ�ð� ���ڿ�
	 */
	public String getElapsedTime() {
		String elapseStrig = null;

		float elapsedTime = ((System.currentTimeMillis() - started) / 1000);
		float elapsedMinut = elapsedTime / 60;
		float elapsedSec = elapsedTime % 60;
		int sec = 0;
		int minut = 0;
		int houre = 0;
		Format formatter = new DecimalFormat("##");
		Format formatter2 = new DecimalFormat("######");
		sec = Integer.parseInt(formatter.format(new Double(elapsedSec)));
		minut = Integer.parseInt(formatter2.format(new Double(elapsedMinut)));
		if (elapsedMinut > 60) {
			houre = minut / 60;
			minut = minut % 60;
		}
		houre = Integer.parseInt(formatter2.format(new Double(houre)));
		minut = Integer.parseInt(formatter.format(new Double(minut)));

		elapseStrig = houre + ":" + minut + ":" + sec;

		return elapseStrig;
	}
}
