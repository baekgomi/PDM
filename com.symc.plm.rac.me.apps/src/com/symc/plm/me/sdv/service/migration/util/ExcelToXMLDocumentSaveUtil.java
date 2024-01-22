package com.symc.plm.me.sdv.service.migration.util;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExcelToXMLDocumentSaveUtil {
	
	//LineItemId='PTP-A2-I1-PVXA2015-00'�� OperationBOMLine Element �˻�
    // result : //OperationBOMLine[@LineItemId='PTP-A2-I1-PVXA2015-00']
	
	/**
	 * ������ Excel ������ Sheet�� �а� XMLDocument�� XML���Ϸ� �����ϴ� ����� �����Ѵ�.
	 * @param srcExcelFilePath �о� ���� Excel ������ Path
	 * @param targetSheetName Excel ���Ͽ��� XML�� ���� Data�� �о���� Sheet�� �̸�
	 * @param rootElementName XMLDocument�� Root Element Name
	 * @param rowElementName Excel�� Row Data�� �����ϴ� Element Name 
	 * @param xmlFileSavePath	������ XML ������ ������ ���
	 * @param tcColumnNames Excel�� Column �̸��� �����ϴ� Teamcenter�� Property �̸��� �����Ѵ�.
	 * @param excelColumnHeadText Excel�� Header�� �ش��ϴ� Row�� Column Text�� Row Element�� Attribute�� �����Ѵ�.
	 * @param targetColumnTypes
	 * @return ������ XMLDocument ��ü�� Return �Ѵ�.
	 */
	public static Document getXMLDocumentFromExcel(String srcExcelFilePath, String targetSheetName, int leftBlankRowCount,
			String rootElementName, String rowElementName, String xmlFileSavePath,
			String[] tcColumnNames, String[] excelColumnHeadText, String[] targetColumnTypes,
			InterfaceSpecialAttributeDefineData[] specialAttributeDef){

		HSSFWorkbook  workbook = null;
		HSSFSheet activitySheet = null;
		try {
			workbook = new HSSFWorkbook(new FileInputStream(srcExcelFilePath));
			if(workbook!=null){
				activitySheet = workbook.getSheet(targetSheetName);
			}
		} catch (Exception err) {
			workbook = null;
		}
		
		if(activitySheet==null){
			return (Document)null;
		}
		
		// XML Document ��ü�� �����Ѵ�.
		Document xmlDoc = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			if(docBuilder!=null){
				xmlDoc = docBuilder.newDocument();
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		
		if(xmlDoc==null){
			return xmlDoc;
		}
		
		Element rootElement = xmlDoc.createElement(rootElementName);
		xmlDoc.appendChild(rootElement);
		
		// ��ü �о� ���� Row�� ������ �ľ��ϱ� ����.
		int rowCount = activitySheet.getPhysicalNumberOfRows();
		
		// Excel���� ���� Row�� ���������� Index�� ����Ѵ�.
		int dataCount = 1;
		for (int rowIndex = 2; activitySheet!=null && rowIndex < rowCount; rowIndex++) {
			HSSFRow currentRow = activitySheet.getRow(rowIndex);
			
			// ���࿡ ù��° Column�� ��ϵ� ���ڿ��� "EOF"�̸� Excel���� Data �б⸦ �ߴ� �Ѵ�.
			HSSFCell firstColumnCell = currentRow.getCell(0);
			if(firstColumnCell!=null){
				String tempString = firstColumnCell.getStringCellValue();
				if(tempString!=null && tempString.trim().equalsIgnoreCase("EOF")==true){
					break;
				}
			}
			
			// OperationItem Elements
			Element elementForRow = xmlDoc.createElement(rowElementName);
			rootElement.appendChild(elementForRow);
			
		
			// set attribute to OperationItem Elements
			Attr rowIndexAttr = xmlDoc.createAttribute("rowIndex");
			rowIndexAttr.setValue((""+dataCount));
			elementForRow.setAttributeNode(rowIndexAttr);
			
			String[] columnStringValues = new String[excelColumnHeadText.length];
			
			for (int targetDataIndex = 0; targetDataIndex < excelColumnHeadText.length; targetDataIndex++) {
				
				// Template�� ���� 
				int excelColumnIndex = targetDataIndex+leftBlankRowCount;
				
				HSSFCell columnCell = currentRow.getCell(excelColumnIndex);
				
				String tcColumnNameString = tcColumnNames[targetDataIndex];
				String columnNameString = excelColumnHeadText[targetDataIndex];
				String columnTypeDefString = targetColumnTypes[targetDataIndex];
				String columnStrValue = "";
				
				if(columnCell==null){
					continue;
				}
				
				switch (columnCell.getCellType()) {
				case HSSFCell.CELL_TYPE_FORMULA:
					columnStrValue = columnCell.getCellFormula();
					break;

				case HSSFCell.CELL_TYPE_NUMERIC:
					columnStrValue = columnCell.getNumericCellValue()+"";
					break;

				case HSSFCell.CELL_TYPE_STRING:
					columnStrValue = columnCell.getStringCellValue();
					break;
					
				case HSSFCell.CELL_TYPE_BOOLEAN:
					columnStrValue = columnCell.getBooleanCellValue()+"";
					break;

				case HSSFCell.CELL_TYPE_BLANK:
					columnStrValue = "";
					break;

				case HSSFCell.CELL_TYPE_ERROR:
					columnStrValue = columnCell.getErrorCellValue()+"";
					break;
					
				default:
					columnStrValue = "";
					break;
				}
				
				columnStringValues[targetDataIndex] = columnStrValue;
				
				// Element ����
				Element elementForColumn = xmlDoc.createElement(tcColumnNameString);
				
				// Column�� �ϴ��ϴ� Element�� Attribute�� PropertyDesc�� ����
				Attr descAttr = xmlDoc.createAttribute("PropertyDesc");
				descAttr.setValue(columnNameString);
				elementForColumn.setAttributeNode(descAttr);
				
				// Column�� �ϴ��ϴ� Element�� Attribute�� PropertyType�� ����
				Attr typeAttr = xmlDoc.createAttribute("PropertyType");
				typeAttr.setValue(columnTypeDefString);
				elementForColumn.setAttributeNode(typeAttr);
				
				// Column�� �ش��ϴ� Element�� Text�� �����Ѵ�.
				elementForColumn.appendChild(xmlDoc.createTextNode(columnStrValue));

				// Row Element�� ������ Column Element�� �߰��Ѵ�.
				elementForRow.appendChild(elementForColumn);

			}
			
			// Ư���� ���ǵ� RowElement�� ���� �߰� Attribute ����
			for (int i = 0; specialAttributeDef!=null && i < specialAttributeDef.length; i++) {
				
				String specialAttributeName = specialAttributeDef[i].getAttributeName();
				int[] valueIndexList = specialAttributeDef[i].getStringConstructionColumns();

				String madeAttributeValueStr = "";
				int strCount = 0;
				for (int j = 0; j < valueIndexList.length; j++) {
					// �Է��Ҷ� Excel�� Column�� 0���� ���� �ϴ°����� �ϰ� ���̴� Index���� �Է��߱� ������ 
					// �̰Ϳ� ���� Data�� Index�� ������ �ش�.
					// ���� ��� Column B ���� Data�� ���۵Ǹ� Data�� �����Ҷ� Index������ 1�� �Է��Ѵ�.
					// �׷��� ���� Data�� ���鿡���� ���������� Data ������ ����� Column�� ���� �ݿ��ϸ�
					// ���� Data������ 0�� Index�� �ȴ�.
					int targetDataIndex = valueIndexList[j] - leftBlankRowCount;
					
					String tempString = columnStringValues[targetDataIndex];
					if(tempString==null || (tempString!=null && tempString.trim().length()<1)){
						tempString = "";
					}
					
					if(tempString==null || (tempString!=null && tempString.trim().length()<1)){
						continue;
					}else{
						tempString = tempString.trim();
					}
					
					if(strCount==0){
						madeAttributeValueStr = tempString;
					}else{
						madeAttributeValueStr = madeAttributeValueStr+"-"+tempString;
					}
					
					strCount++;
				}
				
				if(madeAttributeValueStr==null || (madeAttributeValueStr!=null && madeAttributeValueStr.trim().length()<1)){
					continue;
				}

				// ��� Column ���� ���� �ϳ��� Attribute�� �����.
				// �ַ� Ư������ ������ Item Id�� ���鶧 ����Ѵ�.
				Attr specialAttribute = xmlDoc.createAttribute(specialAttributeName.trim());
				specialAttribute.setValue(madeAttributeValueStr);
				elementForRow.setAttributeNode(specialAttribute);
				
			}
			
			dataCount++;
		}
		
		if(xmlDoc!=null){
			ExcelToXMLDocumentSaveUtil.saveXMLDocumentFile(xmlDoc, xmlFileSavePath);
		}else{
			System.out.println("XML creation error!!");
		}
		
		return xmlDoc;
	}

	public static void saveXMLDocumentFile(Document doc, String xmlFileSavePath){
		
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(xmlFileSavePath));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		if(transformer!=null){
			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}

		System.out.println("File saved!");

	}
	
}
