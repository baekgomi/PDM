package com.ssangyong.soa.bop.reports;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.ssangyong.CommonConstants;
import com.ssangyong.common.remote.SendMailEAIUtil;
import com.ssangyong.soa.bop.util.BasicSoaUtil;
import com.ssangyong.soa.bop.util.LogFileUtility;
import com.ssangyong.soa.bop.util.MPPTopLines;
import com.ssangyong.soa.bop.util.MppUtil;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;
import com.teamcenter.soa.client.model.strong.AppearanceGroup;
import com.teamcenter.soa.client.model.strong.BOMLine;
import com.teamcenter.soa.client.model.strong.BOMWindow;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.Item;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.MECollaborationContext;
import com.teamcenter.soa.client.model.strong.RevisionRule;
import com.teamcenter.soa.exceptions.NotLoadedException;

/**
 * 부자재 목록 Report
 * Structure를 Operatoin Level 까지 전개 한다.
 * Operation의 Child Node로 부자재들이 있다.
 * @author tj
 *
 */
public class ToolListReport {
	
	Connection connection;
	String targetCCName;

	BasicSoaUtil basicSoaUtil;
	MppUtil mppUtil;
	LogFileUtility logFileUtility; 
	MPPTopLines mppTopLines;

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	Vector<String> reportTargeItemType  = null;
	BOMLine productTopBOMLine = null;
	
	String plantCode = null;
	String korCarName = null;
	String engCarName = null;
	String vechileCode = null;
	String productCode = null;
	String mProductItemId = null;
	String mProductItemPUID = null;
	
	String processTopItemId = null;
	String processTopItemPuid = null;
	BOMLine processTopBOMLine = null;
	String processType = null;
	
	//-----------------------------------
	
	BOMLine currentLineBOMLine = null;
	String currentLineItemId = null;
	String currentLineCode = null;
	String currentLineItemRevId = null;
	
	BOMLine currentStationBOMLine = null;
	String currentStationItemId = null;
	String currentStationCode = null;
	String currentStationItemRevId = null;
	
	String currentOperationId = "";
	String beforeOperationId = "Empty";
	int seq = 0;
	
	int indexNo = 0;
	int limitIndexCount = 0;
	int templateDataStartRowIndex = 4;
	
	String reportDestinationFolderPath = CommonConstants.REPORT_FILE_PATH;
	File reportTemplateFile = null;
	String reportFilePath = null;
	
	ExcelWorkBookWriter excelWorkBookWriter;
	
	boolean isSuccess = false;
	
	boolean isAllList = false;
	
	public ToolListReport(Connection connection, String ccName){
		
		this.connection = connection;
		this.targetCCName = ccName;
		
		this.basicSoaUtil = new BasicSoaUtil(this.connection);
		String userId = this.basicSoaUtil.getLoginUserId();
		DateFormat fileNameDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStr = fileNameDf.format(new Date());
		this.logFileUtility = new LogFileUtility("ToolReport_"+ccName+"["+timeStr+"].txt");
		this.logFileUtility.setOutUseSystemOut(true);
		
		this.mppUtil = new MppUtil(connection);
		
		this.reportTargeItemType  = new Vector<String>();
		this.reportTargeItemType.add("M7_BOPBodyOp");
		this.reportTargeItemType.add("M7_BOPPaintOp");
		this.reportTargeItemType.add("M7_BOPAssyOp");
		
	}
	
	public ToolListReport(Connection connection, String ccName, boolean isAllList){
		
		this.connection = connection;
		this.targetCCName = ccName;
		
		this.basicSoaUtil = new BasicSoaUtil(this.connection);
		String userId = this.basicSoaUtil.getLoginUserId();
		DateFormat fileNameDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeStr = fileNameDf.format(new Date());
		this.logFileUtility = new LogFileUtility("ToolReport_"+ccName+"["+timeStr+"].txt");
		this.logFileUtility.setOutUseSystemOut(true);
		
		this.mppUtil = new MppUtil(connection);
		
		this.reportTargeItemType  = new Vector<String>();
		this.reportTargeItemType.add("M7_BOPBodyOp");
		this.reportTargeItemType.add("M7_BOPPaintOp");
		this.reportTargeItemType.add("M7_BOPAssyOp");
		
		this.isAllList = isAllList;
		
	}
	
	public boolean makeReport(){
		
		this.logFileUtility.writeReport("Make Report");
	
		logFileUtility.setTimmerStarat();
		logFileUtility.writeReport("Find CC...");
		
		// CC를 찾는다
		MECollaborationContext aMECollaborationContext = mppUtil.findMECollaborationContext(targetCCName);
		if(aMECollaborationContext==null){
			logFileUtility.writeReport("Return ["+logFileUtility.getElapsedTime()+"] : CC is null!!");
			return isSuccess;
		}

		// MECollaborationContext의 Structure Context Object를 찾아 온다.
		try {
			String[] propertyNames = new String[]{"object_name","structure_contexts"};
			aMECollaborationContext = (MECollaborationContext)basicSoaUtil.readProperties(aMECollaborationContext, propertyNames);
			this.logFileUtility.writeReport("aMECollaborationContext.get_object_name() = "+aMECollaborationContext.get_object_name());
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		logFileUtility.writeReport("Open BOP Window...");
		
    	// Product, Process, Plant의 Top BOMLine을 가져온다.
		// 아래의 Function 실행을 하지 않으면 mppTopLines의 값들이 초기화 되지 않은
		// 상태로 남아 있음.
    	try {
			mppTopLines = mppUtil.openCollaborationContext(aMECollaborationContext);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
    	if(mppTopLines==null){
    		logFileUtility.writeReport("Return ["+logFileUtility.getElapsedTime()+"] : mppTopLines is null!!");
    		return isSuccess;
    	}

    	unpack();
    	
    	initProductBasicInformation();
    	
    	String structureDataInitTime = logFileUtility.getElapsedTime();

		boolean isReadyOk = false;
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_ss");
		String newFileName = "ToolReport_"+this.productCode+"_"+this.plantCode+"["+simpleDateFormat.format(new Date())+"]";
		reportFilePath = reportDestinationFolderPath + File.separator + newFileName+".xlsx";

		this.reportTemplateFile = SDVBOPUtilities.getReportExcelTemplateFile(connection, reportDestinationFolderPath, "ME_DOCTEMP_06", reportFilePath);
		
		if(this.reportTemplateFile!=null && this.reportTemplateFile.exists()==true){
			
			//this.reportTemplateFile.renameTo(new File(reportFilePath));
			
			excelWorkBookWriter = new ExcelWorkBookWriter(reportFilePath);

			int templateDataStartColumnIndex = 0;
			int templateReadyLastRowIndex = templateDataStartRowIndex+3;
			
			try {
				excelWorkBookWriter.readyFile(templateDataStartRowIndex, templateDataStartColumnIndex, templateReadyLastRowIndex);
				isReadyOk = true;
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}
		
		if(isReadyOk==false){
			this.logFileUtility.writeReport("It failed ready to output to a file.");
			
	    	// Window를 닫는다.
	    	try {
				mppUtil.closeCollaborationContext(mppTopLines);
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
	    	
			return isSuccess;
		}
		
		// 자동변경된 Item List를 전역변수에 저장한다.
		printTitleInformation();
		
		String replacedPartInitTime = logFileUtility.getElapsedTime();
    	
    	// BOP Process Structure를 전개해서 Report 생성을 시작 한다.
    	try {
			basicSoaUtil.readProperties(mppTopLines.processLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = mppTopLines.processLine.get_bl_all_child_lines();
			
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				
				if(limitIndexCount!=0 && this.indexNo>limitIndexCount){
					break;
				}
				
				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
					expandAllChildLine((BOMLine)chilBOMLineObjects[i]);
				}
			}
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
    	// Excle 파일을 구성이 완료된 상태임.
    	// 여기서 Sub Total을 표현하기위한 처리를 추가한다.
    	int keyColumnIndex = 1;
    	int sortAreasStartRowIndex = this.templateDataStartRowIndex;
    	boolean isAscendingOrder = true;
    	excelWorkBookWriter.rowSortingForMakeSubTotalData("Sheet1", 1, sortAreasStartRowIndex, isAscendingOrder);

    	// Data의 Index번호를 다시 부여 한다.
    	excelWorkBookWriter.updateSortedDataIndex("Sheet1", 0, sortAreasStartRowIndex);
    	
    	try {
			excelWorkBookWriter.saveWorkBook();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	
    	// 소요량 Sub Total 을 계산하고 같은 Part끼리 Cell을 Merge한다.
//    	int partIdColumnIndex = 1;
//    	int quantityColumnIndex = 6;
//    	int firstDataRowIndex = this.templateDataStartRowIndex + 1;
//    	try {
//			excelWorkBookWriter.subsidiaryReportSubTotalExpressionUpdate(
//					"Sheet1", partIdColumnIndex, quantityColumnIndex, firstDataRowIndex);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
    	
    	// Window를 닫는다.
    	try {
    		
			mppUtil.closeCollaborationContext(mppTopLines);
			excelWorkBookWriter.closeWorkBook();
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
    	File tempFile = new File(this.reportFilePath);
    	if(this.reportFilePath!=null && tempFile.exists()==true && indexNo>0){
    		isSuccess = true;
    	}
    	isSuccess = true;
        
    	this.logFileUtility.writeReport("\n\n----------------------------------");
		this.logFileUtility.writeReport("korCarName = "+korCarName);
		this.logFileUtility.writeReport("engCarName = "+engCarName);
		this.logFileUtility.writeReport("vechileCode = "+vechileCode);
		this.logFileUtility.writeReport("productCode = "+productCode);
		this.logFileUtility.writeReport("mProductItemId = "+mProductItemId);
    	
    	logFileUtility.writeReport("Structure Init Time :  ["+structureDataInitTime+"]");
    	logFileUtility.writeReport("Replaced Part Init Time :  ["+replacedPartInitTime+"]");
    	logFileUtility.writeReport("End ["+logFileUtility.getElapsedTime()+"]");
    	
    	return isSuccess;
	}
	
	private void printTitleInformation(){
		
		String productInfoString = this.productCode + "_" + this.processTopItemId;
		
		try {
			processTopBOMLine = (BOMLine) basicSoaUtil.readProperties(processTopBOMLine, new String[]{"bl_window"});
			
			BOMWindow window = (BOMWindow)processTopBOMLine.get_bl_window();
			
			window = (BOMWindow) basicSoaUtil.readProperties(window, new String[]{"revision_rule"});
			RevisionRule revisionRule = (RevisionRule)window.get_revision_rule();
			revisionRule = (RevisionRule) basicSoaUtil.readProperties(revisionRule, new String[]{"object_name", "rule_date"});
			String revisionRuleName = revisionRule.get_object_name();
			// Revision Rule 기준일
			String revRuleStandardDate = null;
			Calendar ruleDateCalendar  = revisionRule.get_rule_date();
			Date rule_date = null;
			if(ruleDateCalendar!=null){
				rule_date = ruleDateCalendar.getTime();
			}
			if (rule_date != null) {
				revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(rule_date);
			} else {
				revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			}
			String revisionRuleInfo = revisionRuleName+"("+revRuleStandardDate+")";
			String reportDate = "출력 일시 : "+new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
			
			this.logFileUtility.writeReport("productInfoString = "+productInfoString);
			this.logFileUtility.writeReport("revisionRuleInfo = "+revisionRuleInfo);
			this.logFileUtility.writeReport("reportDate = "+reportDate);
			
			excelWorkBookWriter.writeRow("Sheet1", 1, 0, productInfoString);
			excelWorkBookWriter.writeRow("Sheet1", 2, 0, revisionRuleInfo);
			//excelWorkBookWriter.writeRow("Sheet1", 2, 3, variantRule);
			excelWorkBookWriter.writeRow("Sheet1", 2, 19, reportDate);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

	}
	
	public void sendResultMail(String typeOfReport, String userId){
		
		SendMailEAIUtil asendfile = new SendMailEAIUtil();
    	asendfile.sendMailEAI(typeOfReport, processTopItemId+" ["+korCarName+"/"+engCarName+"]", logFileUtility, reportFilePath, isSuccess, userId);
	}
	
	private void initProductBasicInformation(){
		
    	// productTopBOMLine 을 초기화 한다.
    	productTopBOMLine = mppTopLines.productLine;
		
		try {
			processTopBOMLine = (BOMLine)this.mppTopLines.processLine;
			processTopBOMLine = (BOMLine) basicSoaUtil.readProperties(processTopBOMLine, new String[]{"bl_item", "bl_revision"});
			Item processItem = (Item) processTopBOMLine.get_bl_item();
			if(processItem!=null){
				processItem = (Item) basicSoaUtil.readProperties(processItem, new String[]{"m7_VEHICLE_KOR_NAME", "m7_VEHICLE_ENG_NAME", "item_id"});
				korCarName = processItem.getPropertyDisplayableValue("m7_VEHICLE_KOR_NAME");
				engCarName = processItem.getPropertyDisplayableValue("m7_VEHICLE_ENG_NAME");
				
				engCarName = engCarName.replaceAll("/", "_");
				engCarName = engCarName.replaceAll("\\\\", "_");
				
				this.logFileUtility.writeReport("korCarName = "+korCarName);
				this.logFileUtility.writeReport("engCarName = "+engCarName);
				
				this.processTopItemId = processItem.get_item_id();
				this.processTopItemPuid = processItem.getUid();
				
			}

			ItemRevision processItemRevision = (ItemRevision) processTopBOMLine.get_bl_revision();
			if(processItemRevision!=null){
				
				this.logFileUtility.writeReport("Process Item Revision is not null");
				
				processItemRevision = (ItemRevision) basicSoaUtil.readProperties(processItemRevision, new String[]{"m7_VEHICLE_CODE", "m7_PRODUCT_CODE", "m7_SHOP", "m7_PROCESS_TYPE"});
				vechileCode = processItemRevision.getPropertyDisplayableValue("m7_VEHICLE_CODE");
				productCode = processItemRevision.getPropertyDisplayableValue("m7_PRODUCT_CODE");
				plantCode = processItemRevision.getPropertyDisplayableValue("m7_SHOP");
				plantCode = processItemRevision.getPropertyDisplayableValue("m7_PROCESS_TYPE");
				// process type
				processType = processItemRevision.getPropertyDisplayableValue("m7_PROCESS_TYPE");
				// operation type(차체(B), 도장(P), 조립(A))
				if (processType!=null && processType.startsWith("B")) {
				} else if (processType!=null && processType.startsWith("P")) {
				} else if (processType!=null && processType.startsWith("A")) {
				}
				
				this.logFileUtility.writeReport("vechileCode = "+vechileCode);
				this.logFileUtility.writeReport("productCode = "+productCode);
				this.logFileUtility.writeReport("plantCode = "+plantCode);
				
			}else{
				this.logFileUtility.writeReport("Process Item Revision is null");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			productTopBOMLine = (BOMLine) basicSoaUtil.readProperties(productTopBOMLine, new String[]{"bl_item_item_id", "bl_item"});
			mProductItemId = productTopBOMLine.get_bl_item_item_id();
			mProductItemPUID = productTopBOMLine.get_bl_item().getUid();
			this.logFileUtility.writeReport("mProductItemId = "+mProductItemId);
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

	}
	
	private void unpack(){
		
		int flag = 3;    // 0:pack the lines 
		// 1:unpack the lines 
		// 2:pack all lines 
		// 3:unpack all lines

	BOMLine[] srcBOMLines = new BOMLine[]{this.mppTopLines.processLine};
	com.teamcenter.soa.client.model.ServiceData serviceData = 
	com.teamcenter.services.strong.structuremanagement.StructureService.getService(connection).packOrUnpack(srcBOMLines, flag);
	
	this.logFileUtility.writeReport("++++++++++++");
	this.logFileUtility.writeReport("Packed");
	this.logFileUtility.writeReport("++++++++++++");
	int plainObjectsize = serviceData.sizeOfPlainObjects();
	int createObjectsize = serviceData.sizeOfCreatedObjects();
	int updateObjectsize = serviceData.sizeOfUpdatedObjects();

	this.logFileUtility.writeReport("plainObjectsize = "+plainObjectsize);
	this.logFileUtility.writeReport("createObjectsize = "+createObjectsize);
	this.logFileUtility.writeReport("updateObjectsize = "+updateObjectsize);
		
	}

	/**
	 * Child Node를 전개하는 Function
	 * @param bomLine
	 */
	private void expandAllChildLine(BOMLine bomLine){
		
		if(limitIndexCount!=0 && this.indexNo>limitIndexCount){
			return;
		}
		
		String[] properteis = new String[]{"bl_item_item_id", "bl_rev_item_revision_id", "bl_item_object_type", "bl_line_object", "bl_revision"};
		
		String itemId = null;
		String itemRevId = null;
		String itemType = null;
		ModelObject  lineObject = null;
		ItemRevision itemRevision = null;
		try {
			bomLine = (BOMLine)basicSoaUtil.readProperties(bomLine, properteis);
			itemId = bomLine.get_bl_item_item_id();
			itemRevId = bomLine.get_bl_rev_item_revision_id();
			itemType = bomLine.get_bl_item_object_type();
			lineObject = bomLine.get_bl_line_object();
			itemRevision = (ItemRevision)bomLine.get_bl_revision();
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

		// ------------------------------------------------
		// 전개 제외 조건 (Start)
		// ------------------------------------------------
		if(lineObject != null && lineObject instanceof AppearanceGroup){
			//itemType="M7_MfgProduct"
			return;
		}
		
		if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPLine")){
			if(this.currentLineBOMLine==null || (this.currentLineBOMLine!=null && this.currentLineBOMLine.equals(bomLine)==false)){
				this.currentLineBOMLine = bomLine;
				this.currentLineItemId = itemId;
//				this.currentLineItemRevId = itemRevId;
				
				try {
					itemRevision = (ItemRevision)basicSoaUtil.readProperties(itemRevision, new String[]{"m7_LINE", "item_revision_id"});
					this.currentLineCode = itemRevision.getPropertyDisplayableValue("m7_LINE");
					this.currentLineItemRevId = itemRevision.get_item_revision_id();
				} catch (Exception e) {
					this.logFileUtility.writeExceptionTrace(e);
				}
				
				// 조립의 경우 미할당 Line을 Report 출력에서 제외 한다.
				if(this.currentLineCode==null || (this.currentLineCode!=null && this.currentLineCode.trim().length()<1)){
					return;
				}
			}
		}

		if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPStation")){
			if(this.currentStationBOMLine==null || (this.currentStationBOMLine!=null && this.currentStationBOMLine.equals(bomLine)==false)){
				this.currentStationBOMLine = bomLine;
//				this.currentStationItemId = itemId;
				try {
					itemRevision = (ItemRevision)basicSoaUtil.readProperties(itemRevision, new String[]{"m7_STATION_CODE", "item_revision_id"});
					this.currentStationCode = itemRevision.getPropertyDisplayableValue("m7_STATION_CODE");
					this.currentStationItemRevId = itemRevision.get_item_revision_id();
				} catch (Exception e) {
					this.logFileUtility.writeExceptionTrace(e);
				}
				
				this.currentStationItemRevId = itemRevId;
				
			}
		}
		
		if(itemType!=null && itemType.trim().equalsIgnoreCase("M7_BOPWeldOP")){
			return;
		}
		
		// ------------------------------------------------
		// 전개 제외 조건 (End)
		// ------------------------------------------------
		
		// MEProcessRevision, MEOPRevision, ItemRevision, Mfg0MEResourceRevision, MEWorkareaRevision, Mfg0MEDiscreteOPRevision
		
		// ----------------------------------------------------------------------------------
		// Operation에 대한 End Item을 전개한 결과를 Report 해야 하므로
		// Operation의 경우 Report를 만들기 위한 Data를 생성 하도록 한다.
		// ----------------------------------------------------------------------------------
		if(itemType!=null && this.reportTargeItemType.contains(itemType)==true ){
			makeOperatonReportData(bomLine);
			return;
		}
		
		// ----------------------------------------------------------------------------------
		// 제귀 호출을 이용해 Child Node를 계속 전개 한다.
		// ----------------------------------------------------------------------------------
    	try {
			basicSoaUtil.readProperties(bomLine, new String[]{"bl_all_child_lines"});
			ModelObject[] chilBOMLineObjects = bomLine.get_bl_all_child_lines();
			
			for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
				// Child Node를 전개하는 Function을 재귀호출 한다.
				if( chilBOMLineObjects[i]!=null && chilBOMLineObjects[i] instanceof BOMLine){
					expandAllChildLine((BOMLine)chilBOMLineObjects[i]);
				}
			}
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
    	
	}
	
	/**
	 * Operation의 Child Node 정보를 Report에 List 할 형태로 만든다.
	 * @param operationBOMLine
	 */
	private void makeOperatonReportData(BOMLine operationBOMLine){

		HashMap<String, Object> columnDataHash = new HashMap<String, Object>();
		
		columnDataHash.put("Col12", (Object) this.currentLineCode);
		columnDataHash.put("Col13", (Object) currentLineItemRevId);
		
		if (!processType.startsWith("A")) {
			columnDataHash.put("Col14", (Object) currentLineCode + "-" + currentStationCode);   // 공정 
			columnDataHash.put("Col15", (Object) currentStationItemRevId);
			
		} else {
			columnDataHash.put("Col14", "");   // 공정 
			columnDataHash.put("Col15", "");
		}
		
		String operationItemId = null;
		String operationItemRevId = null;
		String operationItemType = null;
		Item operationItem = null;
		ItemRevision operationItemRevision  = null;
		String operationReleasStatusDate = null;
		String operationObjectName = null;
		String operationOwningUser = null;
		String workerCode = null;

		
		// "bl_item_object_name", "bl_variant_condition",
		String[] operationBOMLineProperteis = new String[]{"bl_item", "bl_rev_object_name", "bl_item_item_id", 
				"bl_rev_item_revision_id", "bl_item_object_type", "bl_rev_owning_user", "m7_WORKER_CODE",
				"bl_revision", "bl_all_child_lines", "bl_occ_mvl_condition"
				};
		
		try {
			operationBOMLine = (BOMLine)basicSoaUtil.readProperties(operationBOMLine, operationBOMLineProperteis);
			
			operationItemId = operationBOMLine.get_bl_item_item_id();
			/////////////////////////////////////////////////////////////////////////////////////////////////////
			//35-1D-150-0210-00
//			35-1D-150-0220-00
//			35-1D-150-0220-00
//			35-1D-150-0230-00
//			35-1D-150-0230-00
			
//			if( operationItemId.equals("35-1D-150-0210-00") ||  operationItemId.equals("35-1D-150-0220-00") || operationItemId.equals("35-1D-150-0230-00")) {
//				System.out.println("추출 안되는 공법 번호 ");
//			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////
			
			operationItemRevId = operationBOMLine.get_bl_rev_item_revision_id();
			operationItemType = operationBOMLine.get_bl_item_object_type();
			operationItem = (Item)operationBOMLine.get_bl_item();
			operationItemRevision  = (ItemRevision)operationBOMLine.get_bl_revision();
			operationObjectName = operationBOMLine.get_bl_rev_object_name();
			operationOwningUser = operationBOMLine.get_bl_rev_owning_user();
			workerCode = operationBOMLine.getPropertyDisplayableValue("m7_WORKER_CODE");
		} catch (Exception e) {
			this.logFileUtility.writeExceptionTrace(e);
		}
		
		
		columnDataHash.put("Col01", (Object) operationItemId);
		columnDataHash.put("Col02", (Object) operationItemRevId);
		columnDataHash.put("Col16", (Object) workerCode);
		columnDataHash.put("Col17", (Object) operationObjectName);
		columnDataHash.put("Col19", (Object) operationOwningUser.substring(0, operationOwningUser.indexOf("(")));
		
		
		if(operationItemType!=null && operationItemType.trim().equalsIgnoreCase("M7_BOPAssyOp")==true){
			
			String stationNo = null;
			String staionRevId = null;
			
			String[] assyOpItemRevProperties = new String[]{"m7_STATION_NO"};
			try {
				operationItemRevision = (ItemRevision)basicSoaUtil.readProperties(operationItemRevision, assyOpItemRevProperties);
				stationNo = operationItemRevision.getPropertyDisplayableValue("m7_STATION_NO");
				staionRevId = "";
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			// 조립의 경우 Station BOMLine이 없음.
//			columnDataHash.put("Col12", (Object) stationNo);
//			columnDataHash.put("Col13", (Object) staionRevId);
			columnDataHash.put("Col14", (Object) stationNo);
			columnDataHash.put("Col15", (Object) staionRevId);
			
		}else if(operationItemType!=null && operationItemType.trim().equalsIgnoreCase("M7_BOPPaintOp")==true){
			
		}else if(operationItemType!=null && operationItemType.trim().equalsIgnoreCase("M7_BOPBodyOp")==true){
			
		}else{
			return;
		}
		
		ModelObject[] chilBOMLineObjects = null;
		try {
			chilBOMLineObjects = operationBOMLine.get_bl_all_child_lines();
		} catch (NotLoadedException e) {
			this.logFileUtility.writeExceptionTrace(e);
		}

		// Pack 된 BOMLine이 있으면 UnPack 한다.
		// 전개되는 자동으로 Pack 되도록 설정 되어 있으므로 Unpack 하도록 한다.
		// Rich Client에 구동된 Report 모듈과 동일한 방식으로 구현 한다.
		Vector<BOMLine> childBOMLineVector = new Vector<BOMLine>();
		for (int i = 0; chilBOMLineObjects!=null && i < chilBOMLineObjects.length; i++) {
			
			if(chilBOMLineObjects[i]==null || (chilBOMLineObjects[i]!=null && (chilBOMLineObjects[i] instanceof BOMLine)==false)){
				continue;
			}
			
			ModelObject  lineObject = null;
			BOMLine currentOperationChildBOMLine = (BOMLine)chilBOMLineObjects[i];
			try {
				currentOperationChildBOMLine = (BOMLine)basicSoaUtil.readProperties(currentOperationChildBOMLine, 
						new String[]{"bl_pack_count","bl_packed_lines", "bl_is_packed", "bl_item_object_type","bl_line_object", "bl_item_item_id", "bl_rev_owning_user"});
				
				lineObject = currentOperationChildBOMLine.get_bl_line_object();
				// ------------------------------------------------
				// 전개 제외 조건 (Start)
				// ------------------------------------------------
				if(lineObject != null && lineObject instanceof AppearanceGroup){
					//itemType="M7_MfgProduct"
					return;
				}
				
				String itemType = currentOperationChildBOMLine.get_bl_item_object_type();
				if(itemType==null || (itemType!=null && itemType.trim().equalsIgnoreCase("M7_Tool")==false) ){
					continue;
				}
					
				if (currentOperationChildBOMLine.get_bl_is_packed()) {
					
					int packCount = currentOperationChildBOMLine.get_bl_pack_count();
					ModelObject[] packedLines = currentOperationChildBOMLine.get_bl_packed_lines();
					
					// Pack 된것의 Data를 가져오지 못하는 것 같아 관련된 소스를 아래와 같이 참고 함.
					packedLines = new ModelObject[currentOperationChildBOMLine.get_bl_packed_lines().length + 1];
					packedLines[0] = currentOperationChildBOMLine;
					System.arraycopy(currentOperationChildBOMLine.get_bl_packed_lines(), 0, packedLines, 1, currentOperationChildBOMLine.get_bl_packed_lines().length);
					
					for (int packIndex = 0; packedLines!=null && packIndex < packedLines.length; packIndex++) {
						BOMLine packedChildBOMLine = (BOMLine)packedLines[packIndex];
						childBOMLineVector.add(packedChildBOMLine);
					}
				}else{
					childBOMLineVector.add(currentOperationChildBOMLine);
				}
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
		}
		
		// BOMLie을 Item Id 순으로 Sort 한다.
		Collections.sort(childBOMLineVector, new BOMLineItemIdAscCompare());
		
		// M7_Tool Type의 Child Node만 검토대상으로 List 됨.
		for (int toolIndex = 0; childBOMLineVector!=null && toolIndex < childBOMLineVector.size(); toolIndex++) {
			
			BOMLine toolBOMLine = (BOMLine)childBOMLineVector.get(toolIndex);
			
			
			// ----------------------------------------------				
			// 부자재 BOMLine 특성값을 읽는다.
			// ----------------------------------------------
			String[] toolBOMLineProperteis = new String[]{"bl_item_item_id", 
				"bl_revision", "bl_rev_item_revision_id", "bl_rev_object_name",
				"bl_variant_condition", "bl_occ_mvl_condition", "M7_SUBSIDIARY_QTY", "bl_quantity", "M7_TORQUE", "M7_TORQUE_VALUE"};
			
			String[] toolRevisionProperties = new String[] {"m7_ENG_NAME", "m7_SPEC_ENG" };
			
			String itemId = null;
			String itemRevId = null;
			String itemName = null;
			ItemRevision toolItemRevision = null;
			String engName = null;
			String engSpec = null;
			String torque = null;
			String torqueValue = null;
			String quantity = null;
			try {
				toolBOMLine = (BOMLine)basicSoaUtil.readProperties(toolBOMLine, toolBOMLineProperteis);
				itemId = toolBOMLine.get_bl_item_item_id();
				itemRevId = toolBOMLine.get_bl_rev_item_revision_id();
				itemName = toolBOMLine.get_bl_rev_object_name();
				toolItemRevision = (ItemRevision)toolBOMLine.get_bl_revision();
				toolItemRevision = (ItemRevision)basicSoaUtil.readProperties(toolItemRevision, toolRevisionProperties);
				
				torque = toolBOMLine.getPropertyDisplayableValue("M7_TORQUE");
				torqueValue = toolBOMLine.getPropertyDisplayableValue("M7_TORQUE_VALUE");
				
				engName = toolItemRevision.getPropertyDisplayableValue("m7_ENG_NAME");
				engSpec = toolItemRevision.getPropertyDisplayableValue("m7_SPEC_ENG");
				quantity = toolBOMLine.get_bl_quantity();
				
				
				columnDataHash.put("Col03", (Object) itemId);
				columnDataHash.put("Col04", (Object) itemName);
				columnDataHash.put("Col09", (Object) engSpec);
				columnDataHash.put("Col10", (Object) torque + " " + torqueValue);
				columnDataHash.put("Col11", (Object) quantity);
				
				currentOperationId = operationBOMLine.get_bl_item_item_id();
				 if (this.currentOperationId.equals(this.beforeOperationId)){
			            seq += 10;
			        } else {
			            seq = 10;
			        }
				 this.beforeOperationId = this.currentOperationId;
				 columnDataHash.put("Col18", (Object) String.valueOf(seq));
				
				
			} catch (Exception e) {
				this.logFileUtility.writeExceptionTrace(e);
			}
			
			
			if(toolItemRevision!=null){
				
				indexNo++;
				columnDataHash.put("Col00", (Object) (""+indexNo));
				
				getAdditionalProperty(toolItemRevision, columnDataHash);
				
				String rowDataString = "";
				rowDataString = rowDataString+ columnDataHash.get("Col00");       // 순번 
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col01"); // 공법 No. : bl_item_item_id
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col02"); // 공법 Rev : bl_item_item_id
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col03"); // 공구 Code   
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col04"); // 공구 Name(English)  : m7_SPEC_ENG
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col05"); // Cad
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col06"); // JT
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col07"); // CGR
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col08"); // 기타
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col09"); // Tech Spec
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col10"); // Torque
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col11"); // 수량
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col12"); // Line
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col13"); // Line Rev
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col14"); // 공정
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col15"); // 공정 Rev
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col16"); // 작업자 Code
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col17"); // 공법 명
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col18"); // Find No.
				rowDataString = rowDataString + "\t"+columnDataHash.get("Col19"); // 담당자 
				
				
				Object[] rowData = new Object[20];
				
				for (int j = 0; j < rowData.length; j++) {
					
					DecimalFormat formatter = new DecimalFormat("00");
					String indexStr = "Col"+formatter.format(j);
					Object valueObject = columnDataHash.get(indexStr);
					if(valueObject==null){
						valueObject = new String("");
					}
					this.logFileUtility.writeReport("indexStr = "+indexStr+", valueObject = "+valueObject);
					
					rowData[j] = valueObject;
				}
				
				excelWorkBookWriter.writeRow("Sheet1", indexNo, rowData);
			}
		}
		
	}
	
	 /**
	  * BOM Lie의 Attribute 중 Item Id를 읽어 비교하는 Comparator 구현 Class
	  */
	static class BOMLineItemIdAscCompare implements Comparator<BOMLine> {

		/**
		 * 오름차순(ASC)
		 */
		@Override
		public int compare(BOMLine arg0, BOMLine arg1) {

			String arg0Str = null;
			try {
				arg0Str = arg0.get_bl_item_item_id();
			} catch (Exception e) {
			}
			String arg1Str = null;
			try {
				arg1Str = arg1.get_bl_item_item_id();
			} catch (Exception e) {
			}
			
			return arg0Str.compareTo(arg1Str);
		}

	}

	/**
	 * BOM Lie의 Attribute 중 Item Id를 읽어 비교하는 Comparator 구현 Class
	 *
	 */
	static class BOMLineItemIdDescCompare implements Comparator<BOMLine> {

		/**
		 * 내림차순(DESC)
		 */
		@Override
		public int compare(BOMLine arg0, BOMLine arg1) {

			String arg0Str = null;
			try {
				arg0Str = arg0.get_bl_item_item_id();
			} catch (Exception e) {
			}
			String arg1Str = null;
			try {
				arg1Str = arg1.get_bl_item_item_id();
			} catch (Exception e) {
			}
			
			return arg1Str.compareTo(arg0Str);
		}

	}
	
	
	 // CAD Data 속성
    private void getAdditionalProperty(ItemRevision eqipmentRevision, HashMap<String, Object> columnDataHash)  {

    	try {
    		
    		basicSoaUtil.readProperties(eqipmentRevision, new String[]{"IMAN_specification", "IMAN_Rendering", "IMAN_reference"});
    		ModelObject[] referenceModel = eqipmentRevision.get_IMAN_reference();
    		ModelObject[] specifiModel = eqipmentRevision.get_IMAN_specification();
    		ModelObject[] renderingModel = eqipmentRevision.get_IMAN_Rendering();
    		Vector<ModelObject> modelVector = new Vector<ModelObject>();
    		for (int i = 0;referenceModel!=null && i < referenceModel.length; i++) {
    			if (referenceModel[i] instanceof Dataset) {
    					modelVector.add(referenceModel[i]);
    				}
    				
    			}
    		
    		for (int i = 0;specifiModel!=null && i < specifiModel.length; i++) {
    			if (specifiModel[i] instanceof Dataset) {
    				modelVector.add(specifiModel[i]);
    			}
    			
    		}
    		
    		for (int i = 0;renderingModel!=null && i < renderingModel.length; i++) {
    			if (renderingModel[i] instanceof Dataset) {
    				modelVector.add(renderingModel[i]);
    			}
    			
    		}
    		
    		
    		for (int i = 0;modelVector!=null && i < modelVector.size(); i++) {
    			Dataset aDataset = (Dataset)modelVector.get(i);
    			basicSoaUtil.readProperties(aDataset, new String[]{"object_type"});
				String type = aDataset.get_object_type();
				
				if (type.equals("CATPart") || type.equals("CATDrawing")) {
					columnDataHash.put("Col05","●" );
					
	              } else if(type.equals("DirectModel"))  {
	            	  columnDataHash.put("Col06","●" );
	            	  
	              }  else if ( type.equals("CATCache")) {
	            	  columnDataHash.put("Col07","●" );
						
	              } else {
	            	  columnDataHash.put("Col08","●" );
	              }
    			
    		}
    		
    		
    	} catch ( Exception e ) {
    		e.getStackTrace();
    	}
    }
    
}
