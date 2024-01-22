package com.symc.plm.me.sdv.excel.transformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.ShapeTypes;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFConnector;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.util.BundleUtility;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookProtection;
import org.osgi.framework.Bundle;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.sdv.excel.common.ProcessSheetExcelHelper;
import com.symc.plm.me.sdv.operation.common.AISInstructionDatasetCopyUtil;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetUtils;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

public class ProcessSheetPublishTransformer {

    private final int RESOURCE_MAX_COUNT_PER_SHEET = 6;
    private final int ACTIVITY_MAX_COUNT_PER_SHEET = 26;

    // [NON-SR][20160224] taeku.jeong �����۾�ǥ�ؼ��� Cell�� ǥ�õǴ� ���ڿ���
    // �ʹ� �� ��쿡 �ٹٲ��� �ؾ� �ϴµ� �̶� ���� �ٹٲ� �����̴�.
    // ������ ������ �����ؼ� ��� �Ѵ�.
    private int maxCharCountPerRow = 0;
    private final int MAX_CHAR_COUNT_PER_ROW_KOR = 44;
    private final int MAX_CHAR_COUNT_PER_ROW_ENG = 55;

    private final int ACTIVITY_START_ROW = 6;
    private final int ACTIVITY_END_ROW = 33;

    private String processType;
    private double workerNetSum = 0.0;
    private double addtionalSum = 0.0;
    private double autoSum = 0.0;
    private StringBuffer kpcSpecial;
    private int configId;

    private Workbook workbook;

    private Registry registry = Registry.getRegistry(this);

    private DataFormat dataFormat;
    
    ////////////////////////////////////////////////////////////////////
    private ArrayList<String> specialCharList;
    ////////////////////////////////////////////////////////////////////

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public void print(File file, IDataSet dataSet, int configId) throws Exception {
        try {
            this.configId = configId;
            this.kpcSpecial = new StringBuffer();
            
            //////////////////////////////////////////////////////////////////////////
            this.specialCharList = new ArrayList<String>();
            //////////////////////////////////////////////////////////////////////////

            workbook = new XSSFWorkbook(new FileInputStream(file));
            this.dataFormat = workbook.createDataFormat();

            // �۾�ǥ�ؼ� ���� Sheet �� ���
            int sheetACnt = getASheetCount(dataSet);
            if (sheetACnt > 1) {
                workbook = ProcessSheetExcelHelper.addASheets(workbook, sheetACnt - 1, configId);
            }
            
            if (configId == 0) {
                // [NON-SR][20160224] taeku.jeong  ������ �ٹٲޱ����� �Ǵ� ���ڿ��� ���� ����
            	maxCharCountPerRow = MAX_CHAR_COUNT_PER_ROW_KOR;
            } else {
                // [NON-SR][20160224] taeku.jeong  ������ �ٹٲޱ����� �Ǵ� ���ڿ��� ���� ����
            	maxCharCountPerRow = MAX_CHAR_COUNT_PER_ROW_ENG;
            }

            IDataMap dataMap = dataSet.getDataMap("HeaderInfo");
            printHeaderInfo(dataMap);
            printResourceList((List<HashMap<String, Object>>) dataSet.getDataMap("ResourceList").getTableValue("ResourceList"));
            printMECOList((List<HashMap<String, Object>>) dataSet.getDataMap("MECOList").getTableValue("MECOList"));
            printOperationInfo(dataSet.getDataMap("OperationInfo"));
            printOthers(dataSet);

            // ������ ��� Sheet�� Workbook ��ȣ, ������ ��� Password ����
            if (configId == 0) {
                String password = null;
                if (dataSet.containsKey("Password")) {
                    password = dataSet.getDataMap("Password").getStringValue("Password");
                } else {
                    password = registry.getString("ProcessSheetDefaultPassword");
                }
                
                AISInstructionDatasetCopyUtil.changePassword(workbook, password);

            } else {
                releasePassword();
                
            }

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TCException e) {
            e.printStackTrace();
        }

    }

//    private void changePassword(String password) {
//    	AISInstructionDatasetCopyUtil.changePassword(workbook, password);
//    }

    private void releasePassword() {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
            CTSheetProtection sheetProtection = sheet.getCTWorksheet().getSheetProtection();
            if (sheetProtection != null) {
                sheet.getCTWorksheet().unsetSheetProtection();
            }
        }

        CTWorkbookProtection workbookProtection = ((XSSFWorkbook) workbook).getCTWorkbook().getWorkbookProtection();
        if (workbookProtection != null) {
            ((XSSFWorkbook) workbook).getCTWorkbook().unsetWorkbookProtection();
        }
    }


    public int getASheetCount(IDataSet dataSet) {
        int sheetCnt = 1;

        int resourceCnt = 0, activityCnt = 0, endItemCnt = 0, subsidiaryCnt = 0, totalCnt = 0;

        List<HashMap<String, Object>> resourceList = (List<HashMap<String, Object>>) dataSet.getDataMap("ResourceList").getTableValue("ResourceList");
        List<HashMap<String, Object>> activityList = (List<HashMap<String, Object>>) dataSet.getDataMap("ActivityList").getTableValue("ActivityList");
        List<HashMap<String, Object>> endItemList = (List<HashMap<String, Object>>) dataSet.getDataMap("EndItemList").getTableValue("EndItemList");
        List<HashMap<String, Object>> subsidiaryList = (List<HashMap<String, Object>>) dataSet.getDataMap("SubsidiaryList").getTableValue("SubsidiaryList");

        if (resourceList != null)
            resourceCnt = resourceList.size();
        if (activityList != null)
            activityCnt = getActivityCount(activityList);
        if (endItemList != null)
            endItemCnt = endItemList.size();
        if (subsidiaryList != null)
            subsidiaryCnt = subsidiaryList.size();

        int tempCnt = 0;
        tempCnt = resourceCnt / (RESOURCE_MAX_COUNT_PER_SHEET + 1) + 1;
        if (tempCnt > sheetCnt)
            sheetCnt = tempCnt;

        // endItem�� �ְ�, activity ������ 26�� ������ �ƴ� ��쿡�� activity�� endItem ���̿� ������ ���� ���� activityCnt�� 1�� �߰�
        if (endItemCnt > 0 && (activityCnt % ACTIVITY_MAX_COUNT_PER_SHEET > 0)) {
            activityCnt = activityCnt + 1;
        }

        // [SR140917-022][20140918] bykim
        // activityCnt + endItemCnt + subsidiaryCnt �� �հ�(������)�� �� �鰳�� ������ ��Ʈ Count�� ���� �����Ǿ�� �� ���� ���� �۰� �����Ǿ� �����Ͱ� ©���� ������ �Ʒ��� ���� ����
        totalCnt = activityCnt + endItemCnt + subsidiaryCnt;
        tempCnt = (totalCnt / ACTIVITY_MAX_COUNT_PER_SHEET);
        if ((totalCnt % ACTIVITY_MAX_COUNT_PER_SHEET) != 0) {
            tempCnt = tempCnt + 1;
        }

        if (tempCnt > sheetCnt)
            sheetCnt = tempCnt;

        return sheetCnt;
    }

    private int getActivityCount(List<HashMap<String, Object>> activityList) {
        int activityRowCnt = 0;
        
        if (configId == 0) {
            // [NON-SR][20160224] taeku.jeong  ������ �ٹٲޱ����� �Ǵ� ���ڿ��� ���� ����
        	maxCharCountPerRow = MAX_CHAR_COUNT_PER_ROW_KOR;
        } else {
            // [NON-SR][20160224] taeku.jeong  ������ �ٹٲޱ����� �Ǵ� ���ڿ��� ���� ����
        	maxCharCountPerRow = MAX_CHAR_COUNT_PER_ROW_ENG;
        }

        for (int i = 0; i < activityList.size(); i++) {
            String activity = (String) activityList.get(i).get(SDVPropertyConstant.ITEM_OBJECT_NAME);
            activityRowCnt += (activity.length() / maxCharCountPerRow + 1);
        }

        return activityRowCnt;
    }

    private void printHeaderInfo(IDataMap dataMap) throws TCException {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet currentSheet = workbook.getSheetAt(i);
            if (!workbook.isSheetHidden(i)) {
                Row row = currentSheet.getRow(1);
                String processType = dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE);
                // ����
                row.getCell(42).setCellValue(dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_SHOP_CODE));
                // ����
                row.getCell(48).setCellValue(dataMap.getStringValue(SDVPropertyConstant.MECO_REV_PROJECT_CODE));
                // ����
                row.getCell(57).setCellValue(dataMap.getStringValue(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
                // ���θ�
                if ("P".equals(processType)) {
                    row.getCell(73).setCellValue(dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME));
                } else {
                    row.getCell(73).setCellValue(dataMap.getStringValue(SDVPropertyConstant.LINE_REV_CODE));
                }
                // �ۼ�
                String user = dataMap.getStringValue(SDVPropertyConstant.ITEM_OWNING_USER);
                if (user == null) {
                    TCComponentUser tcUser = ((TCSession) AIFUtility.getDefaultSession()).getUser();
                    user = ProcessSheetUtils.getUserName(configId, tcUser);
                }
                row.getCell(94).setCellValue(user);
                // ����
                user = dataMap.getStringValue(SDVPropertyConstant.WORKFLOW_SIGNOFF);
                row.getCell(99).setCellValue(user);
                // �ۼ���
                Date date = (Date) dataMap.getValue(SDVPropertyConstant.ITEM_CREATION_DATE);
                if (date == null) {
                    date = new Date();
                }
                String strDate = SDVStringUtiles.dateToString(date, "yyyy-MM-dd");
                row.getCell(89).setCellValue(strDate.substring(0, 4));

                row = currentSheet.getRow(2);
                row.getCell(89).setCellValue(strDate.substring(5));
                // ����
                String vehicleName = dataMap.getStringValue(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME);
                if (vehicleName != null && !"".equals(vehicleName)) {
                    row.getCell(48).setCellValue(vehicleName);
                }
                
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                // Ư�� Ư�� �Ӽ��� �߰�
                ArrayList<HashMap> sheetArray = new ArrayList<HashMap>();
                String currentSheetName = "";
                
                if(configId == 0) {
                	currentSheetName = "��";
                } else {
                	currentSheetName = "A";
                }
                
                
                if( configId != 0 ) {
    				if( currentSheet.getSheetName().equals("ASHEET")) {
    					continue;
    				}
    			}
                
                if( !currentSheet.getSheetName().startsWith(currentSheetName)) {
                	continue;
                }
                
                int mergeIndex = 0;
        		int firstRow = 0;
    		    int lastRow = 0;
    		    int firstColumn = 0;
    		    int lastColumn = 0;
    		    
    		    String specialCharacteristicsName = "";
    		    
    		    String oldToolName = "";
        		String oldToolNo = "";
        		String newToolName = "";
    			String newToolNo = "";
					
				if( configId == 0 ) {
					specialCharacteristicsName = "Ư�� Ư��";
					
					oldToolName = "����";
	    			oldToolNo = "��ȣ";
	    			newToolName = "R/C,";
	    			newToolNo = "����";
				}  else {
					specialCharacteristicsName = "S.C";
					oldToolName = "TOOL";
	    			oldToolNo = "NO.";
	    			newToolName = "R/C,";
	    			newToolNo = "TOOL";
				}
                
                for( int j = 0; j < currentSheet.getNumMergedRegions(); j ++ ) {
   				 CellRangeAddress range = currentSheet.getMergedRegion(j);
   				 String message = "";
   				 Row currentRow = currentSheet.getRow(range.getFirstRow());
   				 Cell cell = currentRow.getCell(range.getFirstColumn());
   				 //[SR181005-008] Cell Ÿ���� �ٸ� �Ϳ��� ������ ���� ���� Ÿ�� ��� 
   				 if( cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING ) {
   					 message = cell.getStringCellValue();
   				 }
   				 if( message.startsWith("KPC/")) {
   					 mergeIndex = j;
   					 firstRow = range.getFirstRow();
   					 lastRow = range.getLastRow();
   					 firstColumn = range.getFirstColumn();
   					 lastColumn = range.getLastColumn();
   					 HashMap <String, String> sheetHash = new HashMap<String, String>();
   					 sheetHash.put(currentSheet.getSheetName() + "_" + j, firstRow + ":"+ lastRow + ":" + firstColumn+ ":" + lastColumn );
					 sheetArray.add(sheetHash);
   					 
   				 }  else if( (message.equals(oldToolName) && range.getFirstRow() ==4) || (message.equals(oldToolNo) && range.getFirstRow() == 5) ) {
   					 mergeIndex = j;
   					 firstRow = range.getFirstRow();
   					 lastRow = range.getLastRow();
   					 firstColumn = range.getFirstColumn();
   					 lastColumn = range.getLastColumn();
   					 HashMap <String, String> sheetHash = new HashMap<String, String>();
   					 sheetHash.put(currentSheet.getSheetName() + "_" + j, firstRow + ":"+ lastRow + ":" + firstColumn+ ":" + lastColumn );
					 sheetArray.add(sheetHash);
   				 }
   				 
                }//for�� ����
                
                 if(sheetArray.size() >0) {
   					CellStyle leftCellStyle = workbook.createCellStyle();  // �� ��Ÿ�� ����
   		    		Font font = workbook.createFont(); // ��Ʈ ��Ÿ�� ����
   		    		
   		    		
   		    		font.setFontHeightInPoints((short)10);
   		    		font.setBoldweight(Font.BOLDWEIGHT_BOLD); // ��Ʈ �β� ����
   		    		leftCellStyle.setFont(font);  // ��Ʈ �� ��Ÿ�Ͽ� ����
   		    		leftCellStyle.setWrapText(true); // �� ũ�⿡ �°� �ؽ�Ʈ ����
   		    		leftCellStyle.setAlignment(CellStyle.ALIGN_CENTER); // �� ����
   		    		leftCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // �� ���� ����
   		    		leftCellStyle.setBorderRight(CellStyle.BORDER_THIN); // �� �׵θ�(������) ���±���
   		    		leftCellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM); // �� �׵θ� (����) �߰� ����
   		    		
   		    		if(configId == 0) {
   		    			font.setFontName("����"); // ��Ʈ �۲�
   		    			font.setFontHeightInPoints((short)11);
   		    			
   		    		} else {
   		    			font.setFontName("Tahoma"); // ��Ʈ �۲�
   		    			font.setFontHeightInPoints((short)12);
   		    		}
   		    		
   		    		CellStyle rightCellStyle = workbook.createCellStyle();  // �� ��Ÿ�� ����
   		    		Font font_rightCell = workbook.createFont();
   		    		font_rightCell.setFontName("HY�߰��");
   		    		font_rightCell.setFontHeightInPoints((short)16);
   		    		font_rightCell.setBoldweight(Font.BOLDWEIGHT_BOLD);
   		    		font_rightCell.setColor(HSSFColor.BLUE.index);
   		    		rightCellStyle.setFont(font_rightCell);
   		    		rightCellStyle.setBorderLeft(CellStyle.BORDER_THIN); // �� �׵θ� ����
   		    		rightCellStyle.setAlignment(CellStyle.ALIGN_CENTER); // �� ����
   		    		rightCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // �� ���� ����
   					 
   		    		
   		    		//��Ʈ ��ȣ ����
     				  CTSheetProtection sheetProtection = ((XSSFSheet)currentSheet).getCTWorksheet().getSheetProtection();
     		         if (sheetProtection != null) {
     		       	  ((XSSFSheet)currentSheet).getCTWorksheet().unsetSheetProtection();
     		         }
     		     
     		         CTWorkbookProtection workbookProtection =  ((XSSFWorkbook)currentSheet.getWorkbook()).getCTWorkbook().getWorkbookProtection();
	       		     if (workbookProtection != null) {
	       		   	  		((XSSFWorkbook)currentSheet.getWorkbook()).getCTWorkbook().unsetWorkbookProtection();
	       		     }
   					//////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
		       		  for( int j = 0; j < sheetArray.size(); j ++ ) {
		    				HashMap sheetHashMap = sheetArray.get(j);
		    				Iterator iterator = sheetHashMap.keySet().iterator();
		    			   while(iterator.hasNext()) {
		    			    String key = (String)iterator.next();
		    				String[] keySplit = key.split("_");
		    					String placeInform = (String)sheetHashMap.get(key);
		    					String[] placeInformSplit = placeInform.split(":"); //[0] : firstRow, [1] : lastRow, [2] : firstColumn, [3] : lastColumn
		    					int firstR = Integer.parseInt(placeInformSplit[0]);
		    					int lastR  = Integer.parseInt(placeInformSplit[1]);
		    					int firstC = Integer.parseInt(placeInformSplit[2]);
		    					int lastC  = Integer.parseInt(placeInformSplit[3]);
		    					int mergeI = Integer.parseInt(keySplit[1]);
		    					
		    					if(firstC == 42 && lastC == 47) {
		    						currentSheet.removeMergedRegion(mergeI); // �� ���� ���  
		    						currentSheet.addMergedRegion(new CellRangeAddress(firstR, firstR, firstC, firstC + 2)); // ������  
		    						currentSheet.addMergedRegion(new CellRangeAddress(firstR, firstR, firstC + 3, lastC));
		    						currentSheet.getRow(firstR).getCell(firstC).setCellStyle(leftCellStyle);
		    						currentSheet.getRow(firstR).getCell(firstC).setCellValue(specialCharacteristicsName);
		    						currentSheet.getRow(firstR).getCell(firstC + 3).setCellStyle(rightCellStyle);
		    					} else if( firstC == 99 &&  lastC == 103 ) {
		    						currentSheet.getRow(4).getCell(firstC).setCellValue(newToolName);
		    						currentSheet.getRow(5).getCell(firstC).setCellValue(newToolNo);
		    						currentSheet.getRow(4).getCell(99).setCellStyle(leftCellStyle);
		    						currentSheet.getRow(5).getCell(99).setCellStyle(leftCellStyle);
		    					}
		    			   	}
		       		  	}
   					////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   					// Sheet ��ȣ
   					CTSheetProtection sheetProtection1 = ((XSSFSheet)currentSheet).getCTWorksheet().getSheetProtection();
    			    if (sheetProtection1 == null) {
    	                sheetProtection1 = ((XSSFSheet)currentSheet).getCTWorksheet().addNewSheetProtection();
    	            }
    	            sheetProtection1.setSheet(true);
    	            sheetProtection1.setScenarios(true);
    	            sheetProtection1.setObjects(false);
    	            CTWorkbookProtection workbookProtection1 =  ((XSSFWorkbook)currentSheet.getWorkbook()).getCTWorkbook().getWorkbookProtection();
    	            if (workbookProtection1 == null) {
    	                workbookProtection1 = ((XSSFWorkbook) workbook).getCTWorkbook().addNewWorkbookProtection();
    	                workbookProtection1.setLockStructure(true);
    	                workbookProtection1.setLockWindows(true);
    	            }
   					
                 } // sheetArray ������ if��
                	Row row_specialChar = currentSheet.getRow(3);
					 String specialChar = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
	                if (specialChar != null && !"".equals(specialChar)) {
	                	row_specialChar.getCell(45).setCellValue(specialChar);  // 45�ڸ��� �߰�
	                }
               
                //////////////////////////////////////////////////////////////////////////////////////////////////////
            }
        }
    }

    private void printResourceList(List<HashMap<String, Object>> dataList) {
        if (dataList != null) {
            int sheetCnt = dataList.size() / (RESOURCE_MAX_COUNT_PER_SHEET + 1) + 1;
            HashMap<String, Object> dataMap = null;
            for (int i = 0; i < sheetCnt; i++) {
                Sheet currentSheet = workbook.getSheetAt(i);
                int size = dataList.size() - (i * RESOURCE_MAX_COUNT_PER_SHEET);
                if (size > RESOURCE_MAX_COUNT_PER_SHEET) {
                    size = RESOURCE_MAX_COUNT_PER_SHEET;
                }
                for (int j = 1; j <= size; j++) {
                    dataMap = dataList.get(i * RESOURCE_MAX_COUNT_PER_SHEET + j - 1);
                    Row currentRow = currentSheet.getRow(40 - j);
                    // ����
                    if (dataMap.get("SYMBOL") != null) {
                        currentRow.getCell(0).setCellValue(dataMap.get("SYMBOL").toString());
                    }
                    // ��ȣ
                    currentRow.getCell(2).setCellValue((Integer) dataMap.get("SEQ"));
                    // ��� �� ������
                    currentRow.getCell(4).setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OBJECT_NAME));
                    // ����
                    currentRow.getCell(16).setCellValue((String) dataMap.get(SDVPropertyConstant.BL_QUANTITY));
                    // ���, ���
                    currentRow.getCell(11).setCellValue((String) dataMap.get(SDVPropertyConstant.EQUIP_SPEC_KOR));
                    currentRow.getCell(18).setCellValue((String) dataMap.get(SDVPropertyConstant.EQUIP_PURPOSE_KOR));
                }
            }
        }
    }

    private void printMECOList(List<HashMap<String, Object>> dataList) {
        if (dataList != null) {
            Sheet currentSheet = workbook.getSheetAt(0);
            int size = dataList.size();
            if (size > 6) {
                // �ʵ� MECO
                printMECORow(currentSheet.getRow(39), dataList.get(0), 0);
                // �ֽ� 5��
                for (int i = 1; i <= RESOURCE_MAX_COUNT_PER_SHEET - 1; i++) {
                    printMECORow(currentSheet.getRow(39 - i), dataList.get(size - i), i);
                }

                // ��ü ����Ʈ ���
                addAllMECOSheet(dataList);
            } else {
                for (int i = 0; i < size; i++) {
                    printMECORow(currentSheet.getRow(39 - i), dataList.get(i), i);
                }
            }
        }
    }

    private void printMECORow(Row row, HashMap<String, Object> dataMap, int index) {
        // ��ȣ
        row.getCell(22).setCellValue(dataMap.get("SYMBOL").toString());
        // ����
        Date date = (Date) dataMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED);
        if (date != null) {
            row.getCell(24).setCellValue(SDVStringUtiles.dateToString(date, "yyyy-MM-dd"));
        }
        // ���� ����
        String description = null;
        if (configId == 0) {
            description = (String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID) + "," + (String) dataMap.get(SDVPropertyConstant.ITEM_OBJECT_DESC);
        } else {
            if (index == 0) {
                description = registry.getString("ProcessSheetEn.InitialMECODescPrefix");
            } else {
                description = registry.getString("ProcessSheetEn.MECODescPrefix");
            }
            description = description + " " + (String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
        }
        row.getCell(28).setCellValue(description);
        // ���
        row.getCell(38).setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OWNING_USER));
        // ����
        row.getCell(40).setCellValue((String) dataMap.get("APPR"));
    }

    private void addAllMECOSheet(List<HashMap<String, Object>> dataList) {
        Sheet mecoSheet = workbook.getSheet("MECO");
        workbook.setSheetHidden(workbook.getSheetIndex(mecoSheet), false);
        for (int i = 0; i < dataList.size(); i++) {
            Row row = mecoSheet.getRow(4 + i);
            // ��ȣ
            row.getCell(0).setCellValue(dataList.get(i).get("SYMBOL").toString());
            // ����
            String releasedDateStr = "";
            if(dataList.get(i)!=null){
            	Object k = (Object)dataList.get(i).get(SDVPropertyConstant.ITEM_DATE_RELEASED);
            	if(k!=null && k instanceof String){
            		releasedDateStr = (String)k;
            	}else if(k !=null && k instanceof Date){
            		releasedDateStr = SDVStringUtiles.dateToString((Date)k, "yyyy-MM-dd");
            	}
            }
            row.getCell(5).setCellValue(releasedDateStr);
            // ���� ����
            String description = null;
            if (configId == 0) {
                description = (String) dataList.get(i).get(SDVPropertyConstant.ITEM_ITEM_ID) + "," + (String) dataList.get(i).get(SDVPropertyConstant.ITEM_OBJECT_DESC);
            } else {
                if (i == 0) {
                    description = registry.getString("ProcessSheetEn.InitialMECODescPrefix");
                } else {
                    description = registry.getString("ProcessSheetEn.MECODescPrefix");
                }
                description = description + " " + (String) dataList.get(i).get(SDVPropertyConstant.ITEM_ITEM_ID);
            }
            row.getCell(11).setCellValue(description);
            // ���
            row.getCell(32).setCellValue((String) dataList.get(i).get(SDVPropertyConstant.ITEM_OWNING_USER));
            // ����
            row.getCell(37).setCellValue((String) dataList.get(i).get("APPR"));
        }
    }

    private void printOthers(IDataSet dataSet) throws Exception {
        List<HashMap<String, Object>> activityList = (List<HashMap<String, Object>>) dataSet.getDataMap("ActivityList").getTableValue("ActivityList");
        List<HashMap<String, Object>> resourceList = (List<HashMap<String, Object>>) dataSet.getDataMap("ResourceList").getTableValue("ResourceList");
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        IDataMap bomLine = dataSet.getDataMap("BOMLINE_OBJECT");
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        activityList = getConvertedDataList(activityList);

        int sheetIndex = 0;
        int rowIndex = ACTIVITY_START_ROW;
        Sheet currentSheet = workbook.getSheetAt(sheetIndex);

        // Print Activity List
        int size = activityList.size();
        for (int i = 0; i < size; i++) {
            printActivityRow(currentSheet, rowIndex++, activityList.get(i), resourceList, bomLine);
            if (rowIndex == 32) {
                setHeader(currentSheet, rowIndex);
                currentSheet = workbook.getSheetAt(++sheetIndex);
                rowIndex = ACTIVITY_START_ROW;
            }
        }
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        
    	 /**
        * Ư�� Ư�� �Ӽ��� �߰� ����
        */
        
        Row row_specialChar = currentSheet.getRow(3);
    	TCComponentBOPLine bopLine = (TCComponentBOPLine)bomLine.getValue("BOMLINE_OBJECT");
    	TCComponentItemRevision bopbomRevision = bopLine.getItemRevision();
    	String scCellValue = bopbomRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
    	if( scCellValue == null ) {
    		scCellValue = "";
    	}
        
//        if ("A".equals(processType)) {
        
		       	boolean ctcode = false;
		       	boolean recode = false;
		       	boolean trcode = false;
		       	
		       		if( this.specialCharList.size() > 0 ) {
		       		for( String specialChar : this.specialCharList ) {
		       			if( specialChar.startsWith("CT")  ) {
		       				ctcode = true;
		       			} else if( specialChar.startsWith("RE")) {
		       				recode = true;
		       			} else if( specialChar.startsWith("TR")) {
		       				trcode = true;
		       			} 
		       		}// for�� ��
		       		
		       		if( ctcode && trcode && !recode ) {
		       			row_specialChar.getCell(45).setCellValue("C,T");
		       		} else if(  recode && trcode && !ctcode ) {
		       			row_specialChar.getCell(45).setCellValue("R,T");
		       		} else if( ctcode && !trcode && !recode ) {
		       			row_specialChar.getCell(45).setCellValue("C");
		       		} else if( recode && !trcode && !ctcode ) {
		       			row_specialChar.getCell(45).setCellValue("R");
		       		} else if ( ctcode && recode ) {
		       			row_specialChar.getCell(45).setCellValue("R");
		       		} else if( !ctcode && !recode && trcode ) {
	          			row_specialChar.getCell(45).setCellValue("T");
	          		} else  {
		       			row_specialChar.getCell(45).setCellValue("");
		       		}
		       	} else {
		       		row_specialChar.getCell(45).setCellValue("");
		       	}
//		 } else {
//		     row_specialChar.getCell(45).setCellValue(scCellValue);
//		 }
			
			// BOMLine Ư�� Ư�� �Ӽ� ����
		 
		 if( !scCellValue.equals(row_specialChar.getCell(45).getStringCellValue() == null ? "" : row_specialChar.getCell(45).getStringCellValue())) {
		 	bopbomRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, row_specialChar.getCell(45).getStringCellValue() == null ? "" : row_specialChar.getCell(45).getStringCellValue());
		 }
        
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (kpcSpecial.length() > 0 && kpcSpecial.toString().endsWith("\n")) {
            kpcSpecial.deleteCharAt(kpcSpecial.length() - 1);
        }

        // SUM
        CellStyle cs = workbook.createCellStyle();
        cs.setWrapText(true);
        cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        if (!"A".equals(processType)) {
            cs.setAlignment(CellStyle.ALIGN_CENTER);
        }
        Font font = workbook.createFont();
        font.setFontName("����");
        font.setColor(IndexedColors.BLUE.index);
        cs.setFont(font);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (!workbook.isSheetHidden(i)) {
                String sheetName = sheet.getSheetName();
                if (sheetName.startsWith("��") || sheetName.startsWith("A")) {
                    Row row = sheet.getRow(3);
                    row.getCell(48).setCellStyle(cs);
                    row.getCell(48).setCellValue(kpcSpecial.toString());
                    row.getCell(62).setCellValue((double) Math.ceil(workerNetSum * 10) / 10);
                    row.getCell(73).setCellValue((double) Math.ceil(addtionalSum * 10) / 10);
                    row.getCell(83).setCellValue((double) Math.ceil(autoSum * 10) / 10);
                    if (!"A".equals(processType)) {
                        row.getCell(99).setCellValue((double) Math.ceil((workerNetSum + autoSum) * 10) / 10);
                    }
                }
            }
        }

        // Print Subsidiary & EndItem List
        List<HashMap<String, Object>> subList = (List<HashMap<String, Object>>) dataSet.getDataMap("SubsidiaryList").getTableValue("SubsidiaryList");
        List<HashMap<String, Object>> endItemList = (List<HashMap<String, Object>>) dataSet.getDataMap("EndItemList").getTableValue("EndItemList");

        int subListCnt = subList.size();
        int endItemListCnt = endItemList.size();
        if (subListCnt + endItemListCnt < ACTIVITY_END_ROW - 1 - rowIndex) {
            int subRowIndex = ACTIVITY_END_ROW;
            for (int i = subListCnt - 1; i >= 0; i--) {
                printSubsidiaryRow(currentSheet, subRowIndex--, subList.get(i));
            }

            subRowIndex = setHeader(currentSheet, subRowIndex - 1);

            for (int i = 0; i < endItemListCnt; i++) {
                printEndItemRow(currentSheet, subRowIndex--, endItemList.get(i));
            }
        } else {
            int subRowIndex = ACTIVITY_END_ROW;
            int lastIndex = subListCnt - 1;
            boolean flag = false;
            if (subListCnt > ACTIVITY_END_ROW - 1 - rowIndex) {  
                lastIndex = ACTIVITY_END_ROW - 1 - rowIndex - 1;
                flag = true;
            }

            for (int i = lastIndex; i >= 0; i--) {
                printSubsidiaryRow(currentSheet, subRowIndex--, subList.get(i));
            }

            subRowIndex = setHeader(currentSheet, subRowIndex - 1);
            
            if (flag) {
                int sheetCnt = (subListCnt - lastIndex) / ACTIVITY_MAX_COUNT_PER_SHEET + 1;
                for (int i = 0; i < sheetCnt; i++) {
                    subRowIndex = ACTIVITY_END_ROW;
                    currentSheet = workbook.getSheetAt(++sheetIndex);
                    int first = lastIndex + 1 + (i * ACTIVITY_MAX_COUNT_PER_SHEET);
                    int last = (first + ACTIVITY_MAX_COUNT_PER_SHEET - 1 > subListCnt - 1 ? subListCnt - 1 : first + ACTIVITY_MAX_COUNT_PER_SHEET - 1);
                    for (int j = last; j >= first; j--) {
                        printSubsidiaryRow(currentSheet, subRowIndex--, subList.get(j));
                    }
                    subRowIndex = setHeader(currentSheet, subRowIndex - 1);
                }
            }

            int checkRowIndex = ACTIVITY_START_ROW;
            for (int i = 0; i < endItemListCnt; i++) {
                if (!flag) {
                    checkRowIndex = rowIndex; // ���� Activity ��� �� Index
                }
                // ���� �κ� 
                // [SR181108-064] �۾�ǥ�ؼ� Publish�� ���� ��ħ ���� ����
                // subRowIndex -> �����縦 ��� �ϰ� �� �ٷ� �� Header �κ��� ��� �� Index
                if (subRowIndex <= checkRowIndex ) {
                    subRowIndex = ACTIVITY_END_ROW;
                    currentSheet = workbook.getSheetAt(++sheetIndex);
                    subRowIndex = setHeader(currentSheet, subRowIndex - 1);
                    flag = true;
                    checkRowIndex = ACTIVITY_START_ROW - 1;
                }
                printEndItemRow(currentSheet, subRowIndex--, endItemList.get(i));
            }
        }
    }

    private int setHeader(Sheet sheet, int headerRowIndex) {
        Font font = workbook.createFont();
        if (configId == 0) {
            font.setFontName("����");
        } else {
            font.setFontName("Tahoma");
        }
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
        cellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        cellStyle.setWrapText(true);
        cellStyle.setFont(font);

        CellStyle cellStyle2 = workbook.createCellStyle();
        cellStyle2.setBorderTop(CellStyle.BORDER_MEDIUM);
        cellStyle2.setBorderBottom(CellStyle.BORDER_MEDIUM);
        cellStyle2.setBorderLeft(CellStyle.BORDER_MEDIUM);
        cellStyle2.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle2.setAlignment(CellStyle.ALIGN_CENTER);
        cellStyle2.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        cellStyle2.setWrapText(true);
        cellStyle2.setFont(font);

        CellStyle cellStyle3 = workbook.createCellStyle();
        cellStyle3.setBorderTop(CellStyle.BORDER_MEDIUM);
        cellStyle3.setBorderBottom(CellStyle.BORDER_MEDIUM);
        cellStyle3.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle3.setBorderRight(CellStyle.BORDER_THICK);
        cellStyle3.setAlignment(CellStyle.ALIGN_CENTER);
        cellStyle3.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        cellStyle3.setWrapText(true);
        cellStyle3.setFont(font);

        for (int i = headerRowIndex; i <= headerRowIndex + 1; i++) {
            Row row = sheet.getRow(i);
            for (int j = 43; j <= 102; j++) {
                row.getCell(j).setCellStyle(cellStyle);
            }
        }

        for (int i = headerRowIndex; i <= headerRowIndex + 1; i++) {
            sheet.getRow(i).getCell(42).setCellStyle(cellStyle2);
        }

        for (int i = headerRowIndex; i <= headerRowIndex + 1; i++) {
            sheet.getRow(i).getCell(103).setCellStyle(cellStyle3);
        }

        Row headerRow = sheet.getRow(headerRowIndex);

        sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex + 1, 42, 44));
        headerRow.getCell(42).setCellValue(registry.getString("ProcessSheetEndItemHeaderSymbol." + configId));

        sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex + 1, 45, 47));
        headerRow.getCell(45).setCellValue(registry.getString("ProcessSheetEndItemHeaderSeq." + configId));

        sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex + 1, 48, 56));
        headerRow.getCell(48).setCellValue(registry.getString("ProcessSheetEndItemHeaderPartNo." + configId));

        sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex + 1, 57, 74));
        headerRow.getCell(57).setCellValue(registry.getString("ProcessSheetEndItemHeaderPartName." + configId));

        sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex + 1, 75, 93));
        headerRow.getCell(75).setCellValue(registry.getString("ProcessSheetEndItemHeaderApplication." + configId));

        sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex + 1, 94, 98));
        headerRow.getCell(94).setCellValue(registry.getString("ProcessSheetEndItemHeaderQuantity." + configId));

        sheet.addMergedRegion(new CellRangeAddress(headerRowIndex, headerRowIndex + 1, 99, 103));
        headerRow.getCell(99).setCellValue(registry.getString("ProcessSheetEndItemHeaderUnit." + configId));

        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
        List<XSSFShape> shapeList = drawing.getShapes();

        if (shapeList != null) {
            for (int i = 0; i < shapeList.size(); i++) {
                if (shapeList.get(i) instanceof XSSFConnector) {
                    XSSFConnector connector = (XSSFConnector) shapeList.get(i);
                    XSSFClientAnchor anchor = (XSSFClientAnchor) connector.getAnchor();
                    // FIXME: Connector ����
                    // Connector ������ �ȵǾ� �� ���ٷ� �̵�
                    if (anchor.getRow1() == headerRowIndex || anchor.getRow1() == headerRowIndex + 1) {
                        anchor.setRow1(6);
                        anchor.setRow2(7);
                    }
                }
            }
        }

        return headerRowIndex - 1;
    }

    private List<HashMap<String, Object>> getConvertedDataList(List<HashMap<String, Object>> dataList) {
        List<HashMap<String, Object>> newDataList = new ArrayList<HashMap<String, Object>>();

        if (configId == 0) {
            // [NON-SR][20160224] taeku.jeong  ������ �ٹٲޱ����� �Ǵ� ���ڿ��� ���� ����
        	maxCharCountPerRow = MAX_CHAR_COUNT_PER_ROW_KOR;
        } else {
            // [NON-SR][20160224] taeku.jeong  ������ �ٹٲޱ����� �Ǵ� ���ڿ��� ���� ����
        	maxCharCountPerRow = MAX_CHAR_COUNT_PER_ROW_ENG;
        }
        
        HashMap<String, Object> dataMap = null;
        for (int i = 0; i < dataList.size(); i++) {
            String content = (String) dataList.get(i).get(SDVPropertyConstant.ITEM_OBJECT_NAME);
            int contentLength = content.length();
            if (contentLength > maxCharCountPerRow) {
                dataMap = dataList.get(i);
                dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, content.substring(0, maxCharCountPerRow));
                newDataList.add(dataMap);

                int rowCnt = 0;
                if (contentLength % maxCharCountPerRow == 0) {
                    rowCnt = contentLength / maxCharCountPerRow - 1;
                } else {
                    rowCnt = contentLength / maxCharCountPerRow;
                }

                for (int j = 1; j <= rowCnt; j++) {
                    dataMap = new HashMap<String, Object>();
                    int startIndex = j * maxCharCountPerRow;
                    int endIndex = startIndex + maxCharCountPerRow;
                    dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, content.substring(startIndex, (endIndex > contentLength - 1 ? contentLength : endIndex)));
                    newDataList.add(dataMap);
                }
            } else {
                newDataList.add(dataList.get(i));
            }
        }

        return newDataList;
    }

    private void printActivityRow(Sheet sheet, int rowIndex, HashMap<String, Object> dataMap, List<HashMap<String, Object>> resourceList, IDataMap bomLine) throws TCException {
        Row row = sheet.getRow(rowIndex);
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 42, 44));
        if (dataMap.get("SYMBOL") != null) {
            row.getCell(42).getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
            row.getCell(42).setCellValue(dataMap.get("SYMBOL").toString());
        }
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 45, 47));
        if (dataMap.containsKey("SEQ")) {
            row.getCell(45).getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
            row.getCell(45).setCellValue((Integer) dataMap.get("SEQ"));
        }
        // �۾�����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 57, 93));
        row.getCell(57).setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OBJECT_NAME));
        // �۾� �ð�
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 94, 98));
        if (dataMap.containsKey(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME) && dataMap.containsKey(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY)) {
            double frequency = (Double) dataMap.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
            double workTime = frequency * (Double) dataMap.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
            workTime = (double) Math.round(workTime * 100) / 100;

            String code = "";
            if (dataMap.containsKey(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE)) {
                code = (String) dataMap.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
            }
            String category = (String) dataMap.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
            // [SR��ȣ����][20140514] shcho, �۾��ð� �ڸ��� �Ҽ��� 2�ڸ����� ǥ��, �۾� �ð� ���� �Ҽ��� 1�ڸ����� ǥ�� ���� -----
            // cellType�� CELL_TYPE_STRING ���� ������. (CELL_TYPE_NUMERIC ���δ� �Ҽ��� 2��° �ڸ����� �����ϴ� �Լ��� ����)
            Cell activityTimeCell = row.getCell(94);

            if ("A".equals(processType)) {
                // �۾��� ����
                if ("01".equals(category)) {
                    workerNetSum += workTime;
                    activityTimeCell.setCellValue(workTime);
                    if (frequency != 1) {
                        code = code + " X " + frequency;
                    }
                } else if ("02".equals(category)) {
                    autoSum += workTime;
                    activityTimeCell.setCellValue("[" + workTime + "]");
                } else if ("03".equals(category)) {
                    addtionalSum += workTime;
                    activityTimeCell.setCellValue("(" + workTime + ")");
                }
            } else {
                String overlapType = (String) dataMap.get(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE);
                if ("DUPLICATE".equals(overlapType)) {
                    activityTimeCell.setCellValue("(" + workTime + ")");
                } else if ("STANDBY".equals(overlapType)) {
                    addtionalSum += workTime;
                    activityTimeCell.setCellValue("<" + workTime + ">");
                } else {
                    if ("01".equals(category)) {
                        workerNetSum += workTime;
                        activityTimeCell.setCellValue(workTime);
                    } else if ("02".equals(category)) {
                        autoSum += workTime;
                        activityTimeCell.setCellValue(workTime);
                    }
                }
            }

            activityTimeCell.setCellType(Cell.CELL_TYPE_STRING);
            CellStyle cellStyle = activityTimeCell.getCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            activityTimeCell.setCellStyle(cellStyle);
            // ---------------------------------------------------------------------------------------------------------------------

            // ���/�ڵ�
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 48, 56));
            row.getCell(48).setCellValue(code);
        }

        // KPC, ������ȣ
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 99, 103));
        String controlPoint = "";
        TCComponentBOMLine[] toolList = null;
        String strTool = "";
        String activityTool = "";
        if (dataMap.containsKey(SDVPropertyConstant.ACTIVITY_CONTROL_POINT)) {
            controlPoint = (String) dataMap.get(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);
        }
        if (dataMap.containsKey(SDVPropertyConstant.ACTIVITY_TOOL_LIST)) {
            toolList = (TCComponentBOMLine[]) dataMap.get(SDVPropertyConstant.ACTIVITY_TOOL_LIST);
        }
        
//        ArrayList<String> specialCharList = new ArrayList<String>();
        if (!"".equals(controlPoint.trim())) {
        	
//        	if("A".equals(processType)) {
            	if(controlPoint.startsWith("CT") || controlPoint.startsWith("RE") || controlPoint.startsWith("TR")) {
            		kpcSpecial.append(controlPoint.substring(2));
            	} else {
            		kpcSpecial.append(controlPoint);
            	}
                kpcSpecial.append(":");
                kpcSpecial.append((String) dataMap.get(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS));
                kpcSpecial.append("\n");
                /////////////////////////////////////////////////////////////////////////
                // KPC �Ӽ��� �߰��� ���� ���� �κ�
                // 
                specialCharList.add(controlPoint);
                /////////////////////////////////////////////////////////////////////////
//            } else {
//            	strTool = "KPC";
//            }
        }

        if (toolList != null && toolList.length > 0) {
            for (int i = 0; i < toolList.length; i++) {
                String toolId = toolList[i].getProperty(SDVPropertyConstant.BL_ITEM_ID);
                for (int j = 0; j < resourceList.size(); j++) {
                    if (toolId.equals(resourceList.get(j).get(SDVPropertyConstant.ITEM_ITEM_ID))) {
                        if (strTool.length() > 0) {
                            strTool += ",";
                            activityTool += ",";
                        }
                        strTool += resourceList.get(j).get("SEQ");
                        activityTool += resourceList.get(j).get("SEQ");
                        break;
                    }
                }
            }
        }
        
		/////////////////////////////////////////////////////////////////////////
		// KPC �Ӽ��� �߰��� ���� ���� �κ�
        try {
        	if (!"".equals(controlPoint)) {
		        	  Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		        	  URL fileURL = null;
		        	  InputStream inputStream = null;
		        	  byte[] bytes = null;
		        	  CreationHelper helper = null;
		        	  Drawing drawing = null;
		        	  ClientAnchor anchor = null;
		        	  Picture pict = null;
		        	  
		        	  
		        	  if( controlPoint.startsWith("CT")) {
		        		  fileURL = BundleUtility.find(bundle, "icons/delta_c.png");
		        		  inputStream = fileURL.openStream();
		        		  bytes = IOUtils.toByteArray(inputStream);
		        		  int pictureIdx = sheet.getWorkbook().addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		        		  inputStream.close();
		        		  helper = sheet.getWorkbook().getCreationHelper();
		        		  drawing = sheet.createDrawingPatriarch();
		        		  anchor = helper.createClientAnchor();
		        		  if(activityTool.contains(",") || activityTool.length() > 0) {
		        			  CellStyle style = sheet.getWorkbook().createCellStyle();
		        			  style.setAlignment(CellStyle.ALIGN_RIGHT);
		        			  row.getCell(99).setCellStyle(style);
		        			  row.getCell(99).setCellValue(activityTool);
		        			  anchor.setCol1(99);
		        		  } else {
		        			  anchor.setCol1(100);
		        		  }
		        		  anchor.setRow1(row.getRowNum());
		        		  pict = drawing.createPicture(anchor, pictureIdx);
		        		  pict.resize();
		        		  
		        	  } else if (controlPoint.startsWith("RE")) {
		        		  fileURL = BundleUtility.find(bundle, "icons/delta_r.png");
		        		  inputStream = fileURL.openStream();
		        		  bytes = IOUtils.toByteArray(inputStream);
		        		  int pictureIdx = sheet.getWorkbook().addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		        		  inputStream.close();
		        		  helper = sheet.getWorkbook().getCreationHelper();
		        		  drawing = sheet.createDrawingPatriarch();
		        		  anchor = helper.createClientAnchor();
		        		  if(activityTool.contains(",") || activityTool.length() > 0) {
		        			  anchor.setCol1(99);
		        			  
		        		  } else {
		        			  anchor.setCol1(100);
		        		  }
		        		  anchor.setRow1(row.getRowNum());
		        		  pict = drawing.createPicture(anchor, pictureIdx);
		        		  pict.resize();
		        		  
		        		  if(activityTool.contains(",") || activityTool.length() > 0 ) {
		        			  CellStyle style = sheet.getWorkbook().createCellStyle();
		        			  style.setAlignment(CellStyle.ALIGN_RIGHT);
		        			  row.getCell(99).setCellStyle(style);
		        			  row.getCell(99).setCellValue(activityTool);
		        		  } 
		        		  
		        	  } else if(Pattern.matches("^[��-�R]*$", controlPoint)) {
		        		  
		        		  strTool = "KPC";
		        		  row.getCell(99).setCellValue(strTool);
		        		  if(activityTool.contains(",") || activityTool.length() > 0 ) {
		        			  row.getCell(99).setCellValue(strTool + " " + activityTool);
		        		  } else {
		        			  row.getCell(99).setCellValue(strTool);
		        		  }
		        	  }
           }
        }catch(Exception e) {
        	e.printStackTrace();
        }
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    private void printEndItemRow(Sheet sheet, int rowIndex, HashMap<String, Object> dataMap) throws Exception  {
        Row row = sheet.getRow(rowIndex);
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 42, 44));
        if (dataMap.get("SYMBOL") != null) {
            row.getCell(42).getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
            row.getCell(42).setCellValue(dataMap.get("SYMBOL").toString());
        }
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 45, 47));
        
        /* 
         * 20220419[CF-2917]�����۾� ǥ�ؼ� ���� ��� ���� �Ƿڰ�(������������ ���ؿ� ���� ��û)
         * 20220509 �۾�ǥ�ؼ� ������ ������ ���� �������� �����ڷ� ���� ��û�Ͽ� ���� �Ͽ���.
         * createEllipse ���� ���� ����� �ڵ� �ּ�ó�� �Ͽ���.
         * uniCharNo�� �� ���� ���� �����ڵ�
         * �� ~ �������� ��� �Ҽ� �־ 16�̻��� ����ó�� �Ͽ���
         * order = uniCharNo �� ���� �� �����ڵ�� uniCharNo�� seq�� 1�̿����� ���ϸ� ������ �� �Ǽ� �ٽ� 1���� ���־���
         * order�� char�� ���� �Ͽ� �� ���ڷ� �����.
         * ������ 12���� 16���� ����  
         * 20230627[CF-4156] (��������ȹ�� ����ȭ ���� ��û)
         * �� ���ؿ� ���� ��û���� �����ڷ� ���� �Ͽ����� �����ڴ� 15���� �����Ǽ� 16�̻��� ���� �ȵǰ� ���� ������
         * ����ȭ ������ �۾��� 16�� �Ѿ�� �����簡 �־ 16���ʹ� ���� ���� ������� ó���ش޶�� �Ͽ� 16���ʹ� ���� ������� �����ǰ� ���� ó��  
         * */
        int uniCharNo = 9312; 
        if((Integer) dataMap.get("SEQ") > 15){ 
        	//������ ���� ��ȣ�� 16�̻��� ��� �������� ó��
        	createEllipse(sheet, rowIndex, (String.valueOf(dataMap.get("SEQ"))));
        }else{
            int order = uniCharNo + (Integer) dataMap.get("SEQ") - 1;
            char uniChar = (char) order;
            
            CellStyle cellStyle = workbook.createCellStyle(); // �� ��Ÿ�� ����
            Font font = workbook.createFont();
    		font.setFontName("Arial");
    		font.setFontHeightInPoints((short)16);
    		font.setColor(HSSFColor.BLUE.index);
    		cellStyle.setFont(font);
    		cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM); //�� �׵θ� ����
    		cellStyle.setBorderBottom(CellStyle.BORDER_THIN); 
    		cellStyle.setAlignment(CellStyle.ALIGN_CENTER); //�� ����
    		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // �� ���� ����
    		sheet.getRow(rowIndex).getCell(45).setCellStyle(cellStyle);
    		row.getCell(45).setCellValue(String.valueOf(uniChar));
        }


        
        // ǰ��
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 48, 56));
        row.getCell(48).setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_ID) + " " + (String) dataMap.get(SDVPropertyConstant.BL_ITEM_REV_ID));
        // ǰ��
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 57, 74));
        row.getCell(57).setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OBJECT_NAME));
        // ������
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 75, 93));
        row.getCell(75).setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
        row.getCell(75).setCellStyle(row.getCell(57).getCellStyle());
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 94, 98));
        row.getCell(94).getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
        String quantity = (String) dataMap.get(SDVPropertyConstant.BL_QUANTITY);
        row.getCell(94).setCellValue(Double.parseDouble(quantity));
        row.getCell(94).getCellStyle().setDataFormat(dataFormat.getFormat("0"));
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 99, 103));
        row.getCell(99).getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
        row.getCell(99).setCellValue((String) dataMap.get(SDVPropertyConstant.BL_UNIT_OF_MEASURES));
    }

    private void printSubsidiaryRow(Sheet sheet, int rowIndex, HashMap<String, Object> dataMap) throws Exception {
        Row row = sheet.getRow(rowIndex);
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 42, 44));
        if (dataMap.get("SYMBOL") != null) {
            row.getCell(42).getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
            row.getCell(42).setCellValue(dataMap.get("SYMBOL").toString());
        }
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 45, 47));
        /*
         * 20220419[CF-2917]�����۾� ǥ�ؼ� ���� ��� ���� �Ƿڰ�
         * �۾�ǥ�ؼ� ������ ������ ���� �������� �����ڷ� ���� ��û�Ͽ� ���� �Ͽ���.
         * createEllipse ���� ���� ����� �ڵ� �ּ�ó�� �Ͽ���.
         * uniCharNo�� �� ���� ���� �����ڵ�
         * �� ~ ����� ��� 
         * order = uniCharNo �� ���� �� �����ڵ�� uniCharNo�� seq�� 97�̿����� ���ϸ� ������ �ΰ��Ǽ� �ٽ� 97���� ���־���
         * order�� char�� ���� �Ͽ� �� ���ڷ� �����.  
         * ������ 12���� 16���� ����
         * 20230627[CF-4156] (��������ȹ�� ����ȭ ���� ��û)
         * �� ���ؿ� ���� ��û���� �����ڷ� ���� �Ͽ����� �����ڴ� 15���� �����Ǽ� 16�̻��� ���� �ȵǰ� ���� ������
         * ����ȭ ������ �۾��� 16�� �Ѿ�� �����簡 �־ 16���ʹ� ���� ���� ������� ó���ش޶�� �Ͽ� 16���ʹ� ���� ������� �����ǰ� ���� ó��  
         * �����絵 ������ó�� Z�� �Ѿ�� ������ ����������� �����ǰ� ���� ó��
         * */
        int uniCharNo = 9424; 
        char seq = (Character) dataMap.get("SEQ");
        int seqNo = (int) seq;
        int order = uniCharNo + seqNo - 97;
        char uniChar = (char) order;
        if(seqNo > 122){ 
        	char[] seqs = { (Character) dataMap.get("SEQ") };
        	createEllipse(sheet, rowIndex, new String(seqs));
        }else{
            row.getCell(45).setCellValue(String.valueOf(uniChar));
            
            CellStyle cellStyle = workbook.createCellStyle(); // �� ��Ÿ�� ����
            Font font = workbook.createFont();
    		font.setFontName("Arial");
    		font.setFontHeightInPoints((short)16);
    		font.setColor(HSSFColor.BLUE.index);
    		cellStyle.setFont(font);
    		cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM); //�� �׵θ� ����
    		cellStyle.setBorderBottom(CellStyle.BORDER_THIN); 
    		cellStyle.setAlignment(CellStyle.ALIGN_CENTER); //�� ����
    		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // �� ���� ����
    		sheet.getRow(rowIndex).getCell(45).setCellStyle(cellStyle);
        }


        // ǰ��
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 48, 56));
        row.getCell(48).setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID) + " " + (String) dataMap.get(SDVPropertyConstant.ITEM_REVISION_ID));
        // ǰ��
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 57, 74));
        row.getCell(57).setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OBJECT_NAME));
        // ������
        String spec = (String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR);
        String option = (String) dataMap.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
        String dayOrNight = (String) dataMap.get(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
        if (spec != null && !"".equals(spec)) {
            spec = spec + "/" + option;
        } else {
            spec = option;
        }
        if (dayOrNight != null && !"".equals(dayOrNight)) {
            spec = spec + "/" + dayOrNight;
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 75, 93));
        row.getCell(75).setCellValue(spec);
        row.getCell(75).setCellStyle(row.getCell(57).getCellStyle());
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 94, 98));
        row.getCell(94).getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
        row.getCell(94).setCellValue((String) dataMap.get(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY));
        // ����
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 99, 103));
        row.getCell(99).getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
        row.getCell(99).setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT));
    }

    private void printOperationInfo(IDataMap dataMap) {
        if (!"A".equals(processType)) {
            String special = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_KPC);
            if (special != null && special.length() > 0) {
                kpcSpecial.append(registry.getString("SpecialStation." + configId));
                kpcSpecial.append("\n");
            } else {
                kpcSpecial.append("-");
            }
        }

        int totalSheetCnt = 0;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetHidden(i)) {
                totalSheetCnt++;
            }
        }

        int sheetIndex = 1;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetHidden(i)) {
                Sheet currentSheet = workbook.getSheetAt(i);
                Row row = currentSheet.getRow(35);
                // �۾���
                String operationName = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_KOR_NAME);
                if (configId == 1) {
                    String engOperationName = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
                    if (engOperationName != null && engOperationName.length() > 0) {
                        operationName = engOperationName;
                    }
                }
                row.getCell(42).setCellValue(operationName);
                // ������ȣ
                String operationCode;
                if ("A".equals(processType)) {
                    operationCode = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_STATION_NO);
                    operationCode = operationCode + "\n(" + dataMap.getStringValue(SDVPropertyConstant.OPERATION_WORKER_CODE) + ")";
                } else {
                    operationCode = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_STATION_CODE);
                }

                row.getCell(94).getCellStyle().setWrapText(true);
                row.getCell(94).setCellValue(operationCode);

                // ���������ȣ
                String installDrwNo = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
                if (installDrwNo != null && !"".equals(installDrwNo)) {
                    String[] nos = installDrwNo.split(",");
                    for (int j = 0; j < nos.length; j++) {
                        row = currentSheet.getRow(35 + j);
                        row.getCell(84).setCellValue(nos[j]);
                    }
                }

                // ������ȣ
                
                row = currentSheet.getRow(39);
                String itemId = dataMap.getStringValue(SDVPropertyConstant.ITEM_ITEM_ID);
                row.getCell(42).setCellValue(itemId);
                /* 20220419[CF-2917]�����۾� ǥ�ؼ� ���� ��� ���� �Ƿڰ�
                 * ������ȣ ��ĭ�� �ٿ� ��� �ϴ��� �ٿ��� ����ϱ�� ��û�Ͽ� �ּ� ó��
                 * ������ȣ ũ�� Ȯ�� ��û �Ͽ� ������ȣ CellStyle �߰��� 24���� 34�� ����*/  
//                StringBuffer sb = new StringBuffer();
//                for (int j = 0; j < itemId.length(); j++) {
//                    sb.append(itemId.charAt(j));
//                    if (j < itemId.length() - 1) {
//                        sb.append(" ");
//                    }
//                }
//              row.getCell(42).setCellValue(sb.toString());

                //������ȣ CellStyle
                CellStyle cellStyle = workbook.createCellStyle(); // �� ��Ÿ�� ����
                Font font = workbook.createFont();
				font.setFontName("HY�߰��");
				font.setFontHeightInPoints((short)30);
				font.setBoldweight(Font.BOLDWEIGHT_BOLD);
				font.setColor(HSSFColor.BLUE.index);
				cellStyle.setFont(font);
				cellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM); //�� �׵θ� ����
				cellStyle.setAlignment(CellStyle.ALIGN_CENTER); //�� ����
				cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // �� ���� ����
				currentSheet.getRow(39).getCell(42).setCellStyle(cellStyle);
//				-----------------------------------------------------------
				
                // �ż�
                row.getCell(84).setCellValue(sheetIndex++ + " / " + totalSheetCnt);
                // ����
                row.getCell(94).setCellValue(dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_DR));
            }
        }
    }

    private void createEllipse(Sheet sheet, int row, String text) {
        XSSFClientAnchor anchor = (XSSFClientAnchor) workbook.getCreationHelper().createClientAnchor();
        anchor.setDx1(7 * XSSFShape.EMU_PER_PIXEL);
        anchor.setDy1(0);
        anchor.setDx2(7 * XSSFShape.EMU_PER_PIXEL);
        anchor.setDy2(0);
        anchor.setCol1(45);
        anchor.setRow1(row);
        anchor.setCol2(47);
        anchor.setRow2(row + 1);
        
        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
        XSSFSimpleShape simpleShape = drawing.createSimpleShape(anchor);
        simpleShape.setShapeType(ShapeTypes.ELLIPSE);
        simpleShape.setNoFill(true);
        simpleShape.setLineStyleColor(0, 0, 0);
        simpleShape.setLineWidth(0.5);

        CTTextBodyProperties properties = simpleShape.getCTShape().getTxBody().getBodyPr();
        properties.setRIns(0);
        properties.setTIns(0);
        properties.setBIns(0);
        // Align ������ �ȵǾ� �������� ����
        if (text.length() == 1) {
            properties.setLIns(Units.toEMU(4.5));
        } else {
            properties.setLIns(0);
        }

        Font font = workbook.createFont();
        font.setFontName("����");
        if (text.length() > 2) {
            font.setFontHeightInPoints((short) 9);
        } else {
            font.setFontHeightInPoints((short) 12);
        }
        font.setColor(IndexedColors.BLUE.index);

        XSSFRichTextString textString = new XSSFRichTextString(text);
        textString.applyFont(font);

        simpleShape.setText(textString);
    }

}
