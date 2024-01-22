package com.symc.plm.me.sdv.service.migration.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class PEIFMasterDataExcelToXML {

	// PE Interface�� ���� ����� Excel������ ����� ������ Path
	// ex ) D:\TcM_Interface\Interface
	//       ���� ��θ� �����ϸ� ���� Master Data�� D:\TcM_Interface\Interface\MASTER �������� ����.
	private String workFolderPath;
	
	// Operation ������ ���� XML Document ��ü
	private Document operationMasterXMLDoc = null;
	// Activity ������ ���� XML Document ��ü
	private Document activityMasterXMLDoc = null;
	// ���������� ���� XML Document ��ü
	private Document facilityMasterXMLDoc = null;
	// ���������� ���� XML Document ��ü
	private Document toolMasterXMLDoc = null;
	
	/**
	 * Class ������
	 * @param workFolderPath PE Interfac�� Data�� ����� �⺻ ������ ���
	 */
	public PEIFMasterDataExcelToXML(String workFolderPath){
		this.workFolderPath = workFolderPath;
	}
	
	/**
	 * Work Folder�� ����� Excel ���ϵ��� �о ������ Master Data�� ������ XML Document�� �����Ѵ�.
	 */
	public void readAndMakeMasterDataXML(){
		readAndMakeOperationMasterXMLDocument();
		readAndMakeActivityMasterXMLDocument();
		readAndMakeFacilityMasterXMLDocument();
		readAndMakeToolMasterXMLDocument();
	}
	
	public boolean isErrorExist(){
		
		boolean isErrorExist = false;
		
		if(this.activityMasterXMLDoc!=null){
			NodeList childNodeList = this.activityMasterXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}
		
		if(this.facilityMasterXMLDoc!=null){
			NodeList childNodeList = this.facilityMasterXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}
		
		if(this.operationMasterXMLDoc!=null){
			NodeList childNodeList = this.operationMasterXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}
		
		if(this.toolMasterXMLDoc!=null){
			NodeList childNodeList = this.toolMasterXMLDoc.getChildNodes();
			if(childNodeList==null || (childNodeList!=null && childNodeList.getLength()<1)){
				isErrorExist = true;	
			}
		}else{
			isErrorExist = true;
		}

		return isErrorExist;
	}
	
	/**
	 * Operation Master Data�� ���ǵ� Excel ������ ������ ���� XML Document�� Return �Ѵ�.
	 * @return
	 */
	public Document getOperationMasterXMLDocument(){
		return operationMasterXMLDoc;
	}
	
	/**
	 * Activity Master Data�� ���ǵ� Excel ������ ������ ���� XML Document�� Return �Ѵ�.
	 * @return
	 */
	public Document getActivityMasterXMLDocument(){
		return activityMasterXMLDoc;
	}
	
	/**
	 * Facility Master Data�� ���ǵ� Excel ������ ������ ���� XML Document�� Return �Ѵ�.
	 * @return
	 */
	public Document getFacilityMasterXMLDocument(){
		return facilityMasterXMLDoc;
	}
	
	/**
	 * Tool Master Data�� ���ǵ� Excel ������ ������ ���� XML Document�� Return �Ѵ�.
	 * @return
	 */
	public Document getToolMasterXMLDocument(){
		return toolMasterXMLDoc;
	}
	
	private Document readAndMakeOperationMasterXMLDocument(){
		
		String exclFilePath = workFolderPath+"\\MASTER\\Master-20����.xls";
		String targetSheetName = "����Master";
		int leftBlankColumnCount = 1;
		String rootElementName = "OperationMaster";
		String rowElementName = "OperationItem";
		String xmlFileSavePath = workFolderPath+"\\OperationMaster.xml";
		
		String[] operationMasterTcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T"};
		String[] operationMasterColumnNames = 
				new String[]{"������ȣ-����", "������ȣ-����", "������ȣ", "����������", "�۾���ġ", "������-����", "������-����", "�۾��ڱ����ڵ�", "����������ġ", "���������ȣ", "Station No.", "����", "�ý���", "������ȣ", "Sequence", "��ü�۾�����", "��ǥ���� ����", "�����۾�ǥ�ؼ� ���ϰ��", "�����۾�ǥ�ؼ� I/F ����"};
		String[] operationMasterColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[1];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{1,2,3,4});
		
		operationMasterXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				operationMasterTcColumnNames, operationMasterColumnNames, operationMasterColumnTypes,
				specialAttributeDefineData);
	
		return operationMasterXMLDoc;
	}
	
	private Document readAndMakeActivityMasterXMLDocument(){
		
		String exclFilePath = workFolderPath+"\\MASTER\\Master-20Activity.xls";
		String targetSheetName = "ActivityMaster";
		int leftBlankColumnCount = 1;
		String rootElementName = "ActivityMaster";
		String rowElementName = "ActivityItem";
		String xmlFileSavePath = workFolderPath+"\\ActivityMaster.xml";
		
		String[] activityMasterTcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};
		String[] activityMasterColumnNames = 
				new String[]{"������ȣ-����", "������ȣ-����", "������ȣ", "����������", "�۾�����", "�۾����", "����", "���̵�", "�۾�����(����)", "�۾�����(����)", "�۾��ð�", "�ڵ�/����/����", "����ID", "KPC", "KPC��������"};
		String[] activityMasterColumnTypes = 
				new String[]{"String", "String", "String","String", "String", "String", "String", "double", "String", "String", "double", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = new InterfaceSpecialAttributeDefineData[1];
		specialAttributeDefineData[0] = new InterfaceSpecialAttributeDefineData("OperationItemId",
				new int[]{1,2,3,4});
		
		activityMasterXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				activityMasterTcColumnNames, activityMasterColumnNames, activityMasterColumnTypes,
				specialAttributeDefineData);
	
		return activityMasterXMLDoc;
	}
	
	private Document readAndMakeFacilityMasterXMLDocument(){
		
		String exclFilePath = workFolderPath+"\\MASTER\\Master-30����.xls";
		String targetSheetName = "����Master";
		int leftBlankColumnCount = 1;
		String rootElementName = "FacilityMaster";
		String rowElementName = "FacilityItem";
		String xmlFileSavePath = workFolderPath+"\\FacilityMaster.xml";
		
		String[] facilityMasterTcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S"};
		String[] facilityMasterColumnNames = 
				new String[]{"����", "�����ȣ", "Main Name(����)", "Main Name(����)", "��� �뵵-����", "��� �뵵-����", "���� ���-����", "���� ���-����", "��з�", "�ߺз�", "ó���ɷ�", "���ۻ�", "���Ա���", "��ġ�⵵", "���泻������", "�����ڵ�(JIG)", "��з�(JIG)", "CAD���ϰ��"};
		String[] facilityMasterColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "int", "String", "String", "String", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = null;
		
		facilityMasterXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				facilityMasterTcColumnNames, facilityMasterColumnNames, facilityMasterColumnTypes,
				specialAttributeDefineData);
	
		return facilityMasterXMLDoc;
	}
	
	private Document readAndMakeToolMasterXMLDocument(){
		
		String exclFilePath = workFolderPath+"\\MASTER\\Master-40����.xls";
		String targetSheetName = "����Master";
		int leftBlankColumnCount = 1;
		String rootElementName = "ToolMaster";
		String rowElementName = "ToolItem";
		String xmlFileSavePath = workFolderPath+"\\ToolMaster.xml";
		
		String[] toolMasterTcColumnNames = 
				new String[]{"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U"};
		String[] toolMasterColumnNames = 
				new String[]{"������ȣ", "������-����", "������-����", "��з�", "�ߺз�", "���� �뵵", "����ڵ�", "��� ���-����", "��� ���-����", "�ҿ䷮ ����", "���� ����", "��ũ��", "���ۻ�", "��ü/AF", "����з�", "����", "����� Size", "�ڼ����Կ���", "Remark", "CAD���ϰ��"};
		String[] toolMasterColumnTypes = 
				new String[]{"String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "String", "double", "String", "String", "String", "String"};
		
		InterfaceSpecialAttributeDefineData[] specialAttributeDefineData = null;
		
		toolMasterXMLDoc = ExcelToXMLDocumentSaveUtil.getXMLDocumentFromExcel(exclFilePath, targetSheetName, leftBlankColumnCount, 
				rootElementName, rowElementName, xmlFileSavePath,
				toolMasterTcColumnNames, toolMasterColumnNames, toolMasterColumnTypes,
				specialAttributeDefineData);
	
		return toolMasterXMLDoc;
	}
	
}
