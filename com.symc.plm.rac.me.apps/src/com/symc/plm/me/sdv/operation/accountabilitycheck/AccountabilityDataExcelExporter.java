package com.symc.plm.me.sdv.operation.accountabilitycheck;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.graphics.RGB;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Accountability Check ����� Excel�� ��� �� �� �ֵ��� ����� �߰����ּ���.
 * [SR150106-015] 20151028 taeku.jeong	����ȭ ������� �䱸�� �߰��� SR�� ���� ����
 *  
 * @author Taeku
 *
 */
public class AccountabilityDataExcelExporter {

	private TCSession session;
	private String exportTemplateName = "M7_TEM_DocItemId_AccountabilityReport";
	
	private String targetPath;
	private String exportTargetDir;
	
	private XSSFWorkbook excelWorkbook = null;
	private XSSFSheet excelWorkSheet = null;
	private XSSFRow beforMadeExcelRow = null;
	private XSSFRow createdExcelRow = null;
	
	private int currentExcelRowIndex = 0;

	// Template���� Data�� ä���ֱ� ���� �ؾ��ϴ� Row�� Index
	private int dataStartRowIndex = 6;
	// Template�� �ܰ����� �׷����ִ� ������ Row�� Index
	private int templateBottomRowIndex = 50;
	// Template���� Data�� ����ϱ� �����ؾ� �ϴ�Column�� Index
	private int startColumnIndex = 1;
	
	public void readyFile(String targetPath, TCSession session)  throws Exception {
		
		if(this.exportTemplateName==null || (this.exportTemplateName!=null && this.exportTemplateName.trim().length()<1)){
			throw new Exception("The template used to create the document must be defined.");
		}
		
		if(targetPath==null || (targetPath!=null && targetPath.trim().length()<4)){
			throw new Exception("Must specify where the document is to be created.");
		}else{
			this.targetPath = targetPath;
		}
		
		if(session!=null){
			this.session = session; 
		}
		
		FilePathPars filepathpars = null;
		try {
			filepathpars = new FilePathPars(targetPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.exportTargetDir = filepathpars.getStrFileDir();

		//String xlsFileName = filepathpars.getStrFileName();
		//String xlsFileExt = filepathpars.getStrFileExt();
		
		if(this.exportTargetDir!=null && this.exportTargetDir.trim().length()>0){
			downloadTemplateAndRename();
		}
		
		this.currentExcelRowIndex = 0;
		
		openWorkSheet();
		
	}

	public void writeRow(Hashtable<String, String> rowDataHash, String[] keyIndexList,
			RGB matchedTypeRGB){
		
		boolean needToCreateRow = false;
		
		if(currentExcelRowIndex==0 || (currentExcelRowIndex<dataStartRowIndex)){
			// ���� �ѹ��� ��µ� ����� ����.
			currentExcelRowIndex = dataStartRowIndex;
		}
		
		// ���Ѽ����� ���� Row Index���� Ŀ���� ��� Row�� ���� �ϵ��� �Ѵ�.
		if(currentExcelRowIndex>=(templateBottomRowIndex-1)){
			needToCreateRow = true;
		}
		
		if(needToCreateRow==true){
			// Excel�� Row�� �߰��Ѵ�.
			// �̶� �����ؾ� �� ���� ������ Sheet�� �ִ� Row�� �����ֱ��ؾ� �ϹǷ� shiftRows()�� �̿��ؾ� �Ѵ�.
			excelWorkSheet.shiftRows(currentExcelRowIndex, excelWorkSheet.getLastRowNum(), 1);
			createdExcelRow = excelWorkSheet.createRow(currentExcelRowIndex);
		}else{
			createdExcelRow = excelWorkSheet.getRow(currentExcelRowIndex);
		}
		
		// Excel Column�� ǥ��Ǵ� Data�� ���� �ʱ� Template�� �������� 26���� ��������
		// Template�� ����Ǹ� ���� ���� �ǵ��� ó����.
		// com.myapp.boptest.AccountabilityCheckResultExport Class�� ���ǵ� �������� keyIndexList�� ����
		// �ϸ� �ȴ�.
		int columnLength = 26;
		if(keyIndexList!=null){
			columnLength = keyIndexList.length;
		}
		
		for (int dataIndex = 0; dataIndex < columnLength; dataIndex++) {
			
			// Hash Table�� Key ���� Index �� �� �ִ� ������ �ʿ���.
			String keyIndexValue = keyIndexList[dataIndex];
			String strValue = " ";
			if(keyIndexValue!=null && keyIndexValue.trim().length()>0){
				if(rowDataHash.get(keyIndexValue)!=null){
					strValue = rowDataHash.get(keyIndexValue);
				}
			}
			
			if(strValue==null || (strValue!=null && strValue.trim().length()<1)){
				strValue = " ";
			}
			
			int columnIndex = this.startColumnIndex + dataIndex;
	
			// Cell�� ����Ÿ �Է�.
			XSSFCell currentCell = null;
			try {
				currentCell = writeCell(createdExcelRow, columnIndex, strValue, needToCreateRow);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		
		// �߰��� Row�� Cell Style ����.
		if(beforMadeExcelRow!=null && needToCreateRow==true){
			for (int dataIndex = 0; dataIndex < columnLength; dataIndex++) {
				int columnIndex = this.startColumnIndex + dataIndex;
				
				String keyIndexValue = keyIndexList[dataIndex];
				if(keyIndexValue==null || (keyIndexValue!=null && keyIndexValue.trim().length()<1)){
					continue;
				}

				// ���� Row�� Cell�� �����´�.
				XSSFCell currentCell = createdExcelRow.getCell(columnIndex);
				XSSFCell beforRowCell = beforMadeExcelRow.getCell(currentCell.getColumnIndex());
				if(beforRowCell!=null && beforRowCell.getCellStyle()!=null){
					currentCell.setCellStyle(beforRowCell.getCellStyle());
				}
			}
		}
		
		// ������ ĥ�Ѵ�.
		java.awt.Color color = new Color(matchedTypeRGB.red, matchedTypeRGB.green, matchedTypeRGB.blue);
		XSSFColor aXSSFColor  = new XSSFColor(color);

		for (int dataIndex = 0; dataIndex < columnLength; dataIndex++) {
			int columnIndex = this.startColumnIndex + dataIndex;
			
			String keyIndexValue = keyIndexList[dataIndex];
			if(keyIndexValue==null || (keyIndexValue!=null && keyIndexValue.trim().length()<1)){
				continue;
			}

			XSSFCell currentCell = createdExcelRow.getCell(columnIndex);
			XSSFCellStyle cellStyle = currentCell.getCellStyle();
			XSSFCellStyle cellStyle2 = (XSSFCellStyle)cellStyle.clone();
			
			cellStyle2.setFillForegroundColor(aXSSFColor);
			cellStyle2.setFillPattern(CellStyle.SOLID_FOREGROUND);
			currentCell.setCellStyle(cellStyle2);
		}

		currentExcelRowIndex++;
		beforMadeExcelRow = createdExcelRow;
	}

	public void closeWorkSheet() throws Exception {
		
		if(excelWorkSheet==null){
			return;
		}
		if(excelWorkbook==null){
			return;
		}
		
		try {
		    // Excel File ����.
		    FileOutputStream fileOut = new FileOutputStream(targetPath);
		    excelWorkbook.write(fileOut);
		    fileOut.close();
		    
		} catch (Exception err) {
			throw err;
		} finally {
			
			if (excelWorkSheet != null) {
				excelWorkSheet = null;
			}
			
			if (excelWorkbook != null) {
				excelWorkbook = null;
			}
			
		}
	}


	/**
	 * Template Dataset���� ���� TcFile�� �����ͼ� 
	 * 			�����ϰ��� �ϴ� ���ϸ����� Rename.
	 * @throws Exception
	 */
	private void downloadTemplateAndRename() throws Exception{
		
		TCPreferenceService prefService = this.session.getPreferenceService();
		String itemID = prefService.getStringValueAtLocation(this.exportTemplateName, TCPreferenceLocation.OVERLAY_LOCATION);
		TCComponentItemType itemType = (TCComponentItemType) this.session.getTypeComponent("Item");

		TCComponentDataset excelTemplateDataset = null;
		if(itemType.findItems(itemID) != null) {
			TCComponentItem item = itemType.findItems(itemID)[0];
			if(item.getReleasedItemRevisions() != null) {
				
				TCComponentItemRevision revision = null;
				TCComponentItemRevision[] releasedRevisions =item.getReleasedItemRevisions();
				if(releasedRevisions!=null && releasedRevisions.length>0){
					
					TCComponentItemRevision maxRevision = null;
					for (int i = 0; i < releasedRevisions.length; i++) {
						
						if(maxRevision==null){
							maxRevision= releasedRevisions[i];
							continue;
						}

						String maxRevId = maxRevision.getProperty("item_revision_id");
						String currentRevId = releasedRevisions[i].getProperty("item_revision_id");
						
						if(maxRevId.compareToIgnoreCase(currentRevId)<0){
							maxRevision = releasedRevisions[i];
						}
					}
					
					revision = maxRevision;
				}else{
					revision = item.getLatestItemRevision();
				}
				
				if(revision!=null){
					
			        AIFComponentContext[] contextList = revision.getChildren();

		            for(AIFComponentContext context : contextList) {
		                InterfaceAIFComponent component = context.getComponent();
		                if(component instanceof TCComponentDataset){
		                	String compType = component.getType();
		                	if(compType!=null && compType.trim().indexOf("Excel")>=0){
		                		excelTemplateDataset = (TCComponentDataset) component;
		                		break;
		                	}
		                }
		            }
					System.out.println("excelTemplateDataset = "+excelTemplateDataset);
				}
			}
		}
		
		if(excelTemplateDataset!=null){
			try {
				// CheckSheet Template ���� �ٿ�ε�.
				TCComponentTcFile tcfile = excelTemplateDataset.getTcFiles()[0];
				File templateFile = tcfile.getFile(this.exportTargetDir);
				
				if(templateFile==null){
					throw new Exception("Can't find accountability report template file");
				}
				
				// Template ���ϸ��� ���ǵ� Export ��� ���� �̸����� Rename. 
				File xlsFile = new File(targetPath);
				
				templateFile.renameTo(xlsFile);
			}catch (Exception err) {
				throw err;
			}finally{
				
			}
		}else{
			throw new Exception("Can't find accountability report template data set");
		}
	}

	
	
	//-----------------------------------------
	// ����ϴ� �κ��� �Ʒ��� ���� �Ѵ�.
	//-----------------------------------------
	
	private void openWorkSheet() throws Exception {
		
		Exception err1 = null;
		try {
			// File ��ü ��������.
			excelWorkbook = new XSSFWorkbook(new FileInputStream(targetPath));
			
//			// Workbook ��ü�� �����´�.
//			workbook = new HSSFWorkbook(filesystem);
			
			// Worksheet ��ü�� �����´�.
			// Worksheet�� �������� ��� �ʿ��� WorkSheet�� �߰��� Ȱ���ϴ� �κп����ؼ���
			// ��ü���� ���ǰ� Ȯ���Ǹ� �ٽ� �߰� ���� �ϸ� �ɰ� ����.
			// ���� �����ϱ⿡�� ���ٸ� �߰� Sheet�� �ʿ������� ������ ����.
			//int intSheetNum = workbook.getNumberOfSheets();
			//String strSheetName = workbook.getSheetName(0);
			excelWorkSheet = excelWorkbook.getSheetAt(0);
		    
		} catch (Exception err) {
			err1 = err;
			throw err;
		} finally {
			if(err1!=null){
				if (excelWorkSheet != null) {
					excelWorkSheet = null;
				}
				
				if (excelWorkbook != null) {
					excelWorkbook = null;
				}
			}
		}
	}
	
	/**
	 * Excel File�� Column�� ����� Data�� cell ������ ����Ѵ�.
	 * 
	 * @param excelSheetRow
	 * @param columnIndex
	 * @param strValue
	 * @return
	 * @throws Exception
	 */
	private XSSFCell writeCell(XSSFRow excelSheetRow, int columnIndex, String strValue, boolean isNewRow) throws Exception {
		XSSFCell cell = null;
		try {
			if(isNewRow==true){
				cell = excelSheetRow.createCell(columnIndex);
			}else{
				cell = excelSheetRow.getCell(columnIndex);
			}
			cell.setCellValue(new XSSFRichTextString(strValue));
			
		}
		catch (Exception err) {
			err.printStackTrace();
			throw err;
		}	
		return cell;
	}		
	
	/**
	 * �ʿ信���� Cell�� Style�� �����ؾ� �ϴ� ��쿡 ����� ����������.
	 * @param ecxelSheetCell
	 * @throws Exception
	 */
	private void setCellStyle(XSSFCell ecxelSheetCell) throws Exception {
		
		// ��Ŀ��� Data �Է� ���� ��ġ ���� ���Ŀ� ���� Row�� ����� �����ؼ� ���� �ϵ���
		// ���� �ϸ� �� ������ �����ȴ�.
		// Template�� �������� Cell�� Style�� ���ǵȰ����� ���̴µ�...
		// ���Ŀ� Cell Style�� �����ؼ� ���̴� �������� ó���ϸ� ������ Cell Style��
		// �����ϰ� �����ϴ� ����� �ʿ� ���� ������ ���δ�.
		
		try {
			CellStyle style = excelWorkbook.createCellStyle();
			
			// CellStyle.BORDER_MEDIUM
			// CellStyle.BORDER_DOTTED
			// CellStyle.BORDER_THIN
			
			style.setBorderTop(CellStyle.BORDER_MEDIUM);
			style.setBorderLeft(CellStyle.BORDER_MEDIUM);				
			style.setBorderRight(CellStyle.BORDER_MEDIUM);
			style.setBorderBottom(CellStyle.BORDER_MEDIUM);
			
			ecxelSheetCell.setCellStyle(style);
		}
		catch (Exception err) {
			throw err;
		}	
	}

	/**
	 * Excel Template�� ����߿� M Product: �� Shop �̶�� ��ϵ� Cell�� ���� ä���ִ� �Լ�
	 * @param srcWindow M Product�� ��ϵ� Cell�� ���õ� Data�� ����Ѵ�.
	 * @param targetWindow Shop���� ��ϵ� Cell�� ���õ� Data�� ����Ѵ�. 
	 */
	public void printSrcAndTargetInfomation(TCComponentBOMWindow srcWindow,
			TCComponentBOMWindow targetWindow) {
		
		int title2Row = 1;
		int productColumnIndex = 2;
		int processColumnIndex = 22;
		
		System.out.println("srcWindow = "+srcWindow);
		System.out.println("targetWindow = "+targetWindow);
		
		String srcString = null;
		String srcItemId = null;
		String srcItemRevId = null;
		String srcItemRevName = null;
		TCComponentBOMLine srcTopBOMLine = null;
		try {
			srcTopBOMLine = srcWindow.getTopBOMLine();
			if(srcTopBOMLine!=null){
				srcItemId = srcTopBOMLine.getItem().getProperty("item_id");
				TCComponentItemRevision srcRevision = srcTopBOMLine.getItemRevision();
				if(srcRevision!=null){
					srcItemRevId = srcRevision.getProperty("item_revision_id");
					srcItemRevName = srcRevision.getProperty("object_name");
				}
				srcString = srcItemId+"/"+srcItemRevId+" "+srcItemRevName;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		String targetString = null;
		String targetItemId = null;
		String targetItemRevId = null;
		String targetItemRevName = null;
		TCComponentBOMLine targetTopBOMLine = null;
		try {
			targetTopBOMLine = targetWindow.getTopBOMLine();
			if(targetTopBOMLine!=null){
				targetItemId = targetTopBOMLine.getItem().getProperty("item_id");
				TCComponentItemRevision targetRevision = targetTopBOMLine.getItemRevision();
				if(targetRevision!=null){
					targetItemRevId = targetRevision.getProperty("item_revision_id");
					targetItemRevName = targetRevision.getProperty("object_name");
				}
				
				targetString = targetItemId+"/"+targetItemRevId+" "+targetItemRevName;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		XSSFRow title2InformationExcelRow = null;
		title2InformationExcelRow = excelWorkSheet.getRow(title2Row);
		
		System.out.println("srcString = "+srcString);
		System.out.println("targetString = "+targetString);
		
		try {
			XSSFCell productCell = title2InformationExcelRow.getCell(productColumnIndex);
			if(productCell==null){
				productCell = title2InformationExcelRow.createCell(productColumnIndex);			
			}
			productCell.setCellValue(new XSSFRichTextString(srcString));			

			XSSFCell shopCell = title2InformationExcelRow.getCell(processColumnIndex);
			if(shopCell==null){
				shopCell = title2InformationExcelRow.createCell(processColumnIndex);			
			}
			shopCell.setCellValue(new XSSFRichTextString(targetString));
		}
		catch (Exception err) {
			err.printStackTrace();
		}	
		
		
	}

}
