package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.util.Registry;

public class BOPOperationMasterListOPDataExcelTransformer extends AbstractExcelTransformer {

    private final int DATA_START_ROW_INDEX = 6;

    private HashMap<String, Object> add_columnInfo = new HashMap<String, Object>();
    private ArrayList<Integer> center_columnList = new ArrayList<Integer>();

    private Registry registry;

    public BOPOperationMasterListOPDataExcelTransformer() {
        super();
        this.registry = Registry.getRegistry(this);
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {
        // PreferenceName���� ���ø� ���� ��������
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            if (dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                IDataMap dataMap = dataSet.getDataMap("additionalInfo");
                add_columnInfo.put("selectedValueFromDialog", dataMap.getValue("selectedValueFromDialog"));

                printHeaderInfo(currentSheet, dataMap);
                int rowSize = dataList.size();
                for (int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i), dataMap);
                }
                setColumnInfo(currentSheet);
                setCellStyleOfContents(workbook, currentSheet);
                printExcelExportDate(workbook, currentSheet, dataMap);
            }

            FileOutputStream fos = new FileOutputStream(templateFile);
            workbook.write(fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<HashMap<String, Object>> getTableData(IDataSet dataSet) {
        Collection<HashMap<String, Object>> data = null;

        IDataMap dataMap = dataSet.getDataMap("operationList");
        if (dataMap != null) {
            data = dataMap.getTableValue("operationList");
        }

        return (List<HashMap<String, Object>>) data;
    }

    /**
     * �� Row�� ����Ѵ�.
     * 
     * @method printRow
     * @date 2013. 10. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap, IDataMap additionalInfoMap) {
        Row row = currentSheet.createRow(this.DATA_START_ROW_INDEX + num);

        // ����
        Cell cell = row.createCell(0);
        cell.setCellValue(num + 1);

        // ����(�۾�ǥ�ؼ�) NO
        cell = row.createCell(1);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_ID));

        // ���� REV
        cell = row.createCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_REV_ID));

        // ������
        cell = row.createCell(3);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OBJECT_NAME));

        // ���� ������
        cell = row.createCell(4);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_REV_ENG_NAME));

        // Option Code
        cell = row.createCell(5);
        cell.setCellValue((String) dataMap.get("optionCode"));

        // Option Description
        cell = row.createCell(6);
        cell.setCellValue((String) dataMap.get("optionDescription"));

        // DR
        cell = row.createCell(7);
        cell.setCellValue((String) dataMap.get("drProperty"));
        
        /////////////////////////////////////////////////////////////////////////////////
        // Report Excel ��¿� Ư�� Ư�� �Ӽ� �߰�
        // DR
        cell = row.createCell(8);
        cell.setCellValue((String) dataMap.get("specialCharacterristic"));
        
        /////////////////////////////////////////////////////////////////////////////////

        // KPC/Ư������
        cell = row.createCell(9);
        if (dataMap.get("isExistKPC") == null) {
            cell.setCellValue((String) dataMap.get("specialStation"));
        } else {
            cell.setCellValue((String) dataMap.get("isExistKPC"));
            
        }
        // KPC ���� �߰��� ���� ����
        cell = row.createCell(10);
        if(dataMap.get("isExistKPC") != null) {
        	cell.setCellValue((String) dataMap.get("kpcContents"));
        }
     // KPC ���� �߰��� ���� ���� 37 --> 38 �� Cell ���� ����
        // �ڵ� �ð�
        cell = row.createCell(11);
        cell.setCellValue((Double) dataMap.get("time2"));

        // �۾��� ���� �ð�
        cell = row.createCell(12);
        cell.setCellValue((Double) dataMap.get("time1"));

        // cycle time
        // [SR140905-044][20140924] shcho, ��ü/������ ��쿡�� ������ �Է�
        String processType = additionalInfoMap.getStringValue("processType");

        cell = row.createCell(13);
        if (processType.equals("ASSY")) {
            cell.setCellValue("");
        } else {
            cell.setCellValue((Double) dataMap.get("cycleTime"));
        }

        // �δ� ���
        cell = row.createCell(14);
        cell.setCellValue((Double) dataMap.get("allowance"));

        // ǥ�� �ð�
        cell = row.createCell(15);
        cell.setCellValue((Double) dataMap.get("time4"));

        // ���� �ð�
        cell = row.createCell(16);
        cell.setCellValue((Double) dataMap.get("time3"));

        // �۾� �ð�
        cell = row.createCell(17);
        cell.setCellValue((Double) dataMap.get("time5"));

        // �� �ð�(�ִ�)
        cell = row.createCell(18);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_REV_MAX_WORK_TIME_CHECK));

        // �� �ð�(��ǥ����)
        cell = row.createCell(19);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_REV_REP_VHICLE_CHECK));

        // Line Code
        cell = row.createCell(20);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.LINE_REV_CODE));

        // Line Revision
        cell = row.createCell(21);
        cell.setCellValue((String) dataMap.get("line_revision"));

        // ���� Code
        cell = row.createCell(22);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_STATION_CODE));

        // ���� Revision
        cell = row.createCell(23);
        cell.setCellValue((String) dataMap.get("station_revision"));

        // �۾���
        cell = row.createCell(24);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_WORKER_CODE));

        // �۾���
        cell = row.createCell(25);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_PROCESS_SEQ));

        // ��ġ
        cell = row.createCell(26);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_WORKAREA));

        // ���� List
        printToolList(row, (List<HashMap<String, Object>>) dataMap.get("toolList"));

        // ���� List
        printEquipmentList(row, (List<HashMap<String, Object>>) dataMap.get("equipmentList"));

        // ���� �ý���
        cell = row.createCell(36);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM));

        // ���� �����
        cell = row.createCell(37);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OWNING_USER));

        // INSTL DWG NO
        cell = row.createCell(38);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO));
        
        /*
         * ������ : 20200110
         * [CF196] �Ӽ� �߰� ��û "m7_P_FMEA_NO", "m7_CP_NO"
         * �Էµ� �����͸� Excel Report�� ���
         */
        // ������������ �Ӽ��� ����
        if (processType.equals("ASSY")) {
        	//P_MEFA_NO
        	cell = row.createCell(39);
        	cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO));
        	
        	//P_MEFA_NO
        	cell = row.createCell(40);
        	cell.setCellValue((String) dataMap.get(SDVPropertyConstant.OPERATION_ITEM_CP_NO));
        }

        // �۾� ���� List
        if (dataMap.get("workInfoList") != null) {
            printWorkInfoList(row, (List<HashMap<String, Object>>) dataMap.get("workInfoList"));
        }

        // End Item List
        if (dataMap.get("endItemList") != null) {
            printEndItemList(row, (List<HashMap<String, Object>>) dataMap.get("endItemList"));
        }

        // ������ List
        if (dataMap.get("subsidiaryList") != null) {
            printSubsidiaryList(row, (List<HashMap<String, Object>>) dataMap.get("subsidiaryList"));
        }
    }

    /**
     * ���� List ���
     * 
     * @method printToolList
     * @date 2014. 1. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printToolList(Row row, List<HashMap<String, Object>> toolList) {
        String toolID = "";
        String toolEngName = "";
        String toolSpec = "";
        String toolTorqueValue = "";
        String toolQuantity = "";

        for (int i = 0; i < toolList.size(); i++) {
            if (i != 0) {
                toolID += "\n";
                toolEngName += "\n";
                toolSpec += "\n";
                toolTorqueValue += "\n";
                toolQuantity += "\n";
            }

            toolID += toolList.get(i).get(SDVPropertyConstant.BL_ITEM_ID);
            toolEngName += toolList.get(i).get(SDVPropertyConstant.TOOL_ENG_NAME);
            toolSpec += toolList.get(i).get(SDVPropertyConstant.TOOL_SPEC_ENG);
            toolTorqueValue += toolList.get(i).get(SDVPropertyConstant.BL_NOTE_TORQUE) + " " + toolList.get(i).get(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE);
            toolQuantity += toolList.get(i).get(SDVPropertyConstant.BL_QUANTITY);
        }
        //  KPC ���� �߰��� ���� Cell ���� ���� 37 --> 38
        // ���� ID
        row.createCell(27).setCellValue(toolID);

        // ���� ������
        row.createCell(28).setCellValue(toolEngName);

        // ���� ����
        row.createCell(29).setCellValue(toolSpec);

        // TORQUE
        row.createCell(30).setCellValue(toolTorqueValue);

        // ���� ����
        row.createCell(31).setCellValue(toolQuantity);
    }

    /**
     * ���� List ���
     * 
     * @method printEquipmentList
     * @date 2014. 1. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printEquipmentList(Row row, List<HashMap<String, Object>> equipmentList) {
        String equipmentID = "";
        String equipmentName = "";
        String equipmentSpecAndPurpose = "";
        String equipmentQuantity = "";

        for (int i = 0; i < equipmentList.size(); i++) {
            if (i != 0) {
                equipmentID += "\n";
                equipmentName += "\n";
                equipmentSpecAndPurpose += "\n";
                equipmentQuantity += "\n";
            }

            equipmentID += equipmentList.get(i).get(SDVPropertyConstant.BL_ITEM_ID);
            equipmentName += equipmentList.get(i).get(SDVPropertyConstant.BL_OBJECT_NAME);
            equipmentSpecAndPurpose += equipmentList.get(i).get(SDVPropertyConstant.EQUIP_SPEC_ENG) + "/" + equipmentList.get(i).get(SDVPropertyConstant.EQUIP_PURPOSE_ENG);
            equipmentQuantity += equipmentList.get(i).get(SDVPropertyConstant.BL_QUANTITY);
        }
    //  KPC ���� �߰��� ���� Cell ���� ���� 37 --> 38
        // ���� ID
        row.createCell(32).setCellValue(equipmentID);

        // �����
        row.createCell(33).setCellValue(equipmentName);

        // ���� ����/����
        row.createCell(34).setCellValue(equipmentSpecAndPurpose);

        // ���� ����
        row.createCell(35).setCellValue(equipmentQuantity);
    }

    /**
     * �۾� ���� List ���
     * 
     * @method printSubsidiaryList
     * @date 2014. 1. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printWorkInfoList(Row row, List<HashMap<String, Object>> workInfoList) {
        String workCode = "";
        String workInfo = "";

        for (int i = 0; i < workInfoList.size(); i++) {
            if (i != 0) {
                workCode += "\n";
                workInfo += "\n";
            }

            workCode += workInfoList.get(i).get("workCode");
            workInfo += workInfoList.get(i).get("workInfo");
        }

        int cellNum = row.getLastCellNum();

        // �۾� �ڵ�
        row.createCell(cellNum).setCellValue(workCode);

        // �۾� ����
        row.createCell(cellNum + 1).setCellValue(workInfo);

        if (!add_columnInfo.containsKey("workInfo")) {
            HashMap<String, Object> columnIndex = new HashMap<String, Object>();
            columnIndex.put("startIndex", cellNum);
            columnIndex.put("lastIndex", cellNum + 1);
            add_columnInfo.put("workInfo", columnIndex);
        }
    }

    /**
     * End Item List ���
     * 
     * @method printEndItemList
     * @date 2014. 1. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printEndItemList(Row row, List<HashMap<String, Object>> endItemList) {
        String endItemID = "";
        String endItemName = "";
        String endItemOption = "";
        String endItemQuantity = "";

        for (int i = 0; i < endItemList.size(); i++) {
            if (i != 0) {
                endItemID += "\n";
                endItemName += "\n";
                endItemOption += "\n";
                endItemQuantity += "\n";
            }

            endItemID += endItemList.get(i).get(SDVPropertyConstant.BL_ITEM_ID);
            endItemName += endItemList.get(i).get(SDVPropertyConstant.BL_OBJECT_NAME);
            endItemOption += endItemList.get(i).get(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
            endItemQuantity += endItemList.get(i).get(SDVPropertyConstant.BL_QUANTITY);
        }

        int cellNum = row.getLastCellNum();

        // PART NO
        row.createCell(cellNum).setCellValue(endItemID);

        // PART ��
        row.createCell(cellNum + 1).setCellValue(endItemName);

        // �ɼ�
        row.createCell(cellNum + 2).setCellValue(endItemOption);

        // ����
        row.createCell(cellNum + 3).setCellValue(endItemQuantity);
        center_columnList.add(cellNum + 3);

        if (!add_columnInfo.containsKey("endItemInfo")) {
            HashMap<String, Object> columnIndex = new HashMap<String, Object>();
            columnIndex.put("startIndex", cellNum);
            columnIndex.put("lastIndex", cellNum + 3);
            add_columnInfo.put("endItemInfo", columnIndex);
        }
    }

    /**
     * ������ List ���
     * 
     * @method printEndItemList
     * @date 2014. 1. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printSubsidiaryList(Row row, List<HashMap<String, Object>> subsidiaryList) {
        String subsidiaryID = "";
        String subsidiaryName = "";
        String subsidiaryEngName = "";
        String subsidiaryOption = "";
        String subsidiaryUnitAmount = "";
        String subsidiaryQuantity = "";
        for (int i = 0; i < subsidiaryList.size(); i++) {
            if (i != 0) {
                subsidiaryID += "\n";
                subsidiaryName += "\n";
                subsidiaryEngName += "\n";
                subsidiaryOption += "\n";
                subsidiaryUnitAmount += "\n";
                subsidiaryQuantity += "\n";
            }
            subsidiaryID += subsidiaryList.get(i).get(SDVPropertyConstant.BL_ITEM_ID);
            subsidiaryName += subsidiaryList.get(i).get(SDVPropertyConstant.BL_OBJECT_NAME);
            subsidiaryEngName += subsidiaryList.get(i).get(SDVPropertyConstant.SUBSIDIARY_ENG_NAME);
            subsidiaryOption += subsidiaryList.get(i).get(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
            subsidiaryUnitAmount += subsidiaryList.get(i).get(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT);
            subsidiaryQuantity += subsidiaryList.get(i).get(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
        }

        int cellNum = row.getLastCellNum();

        // ������ NO
        row.createCell(cellNum).setCellValue(subsidiaryID);

        // ������ ��
        row.createCell(cellNum + 1).setCellValue(subsidiaryName);

        // ������ ������
        row.createCell(cellNum + 2).setCellValue(subsidiaryEngName);

        // ���� �ɼ�
        row.createCell(cellNum + 3).setCellValue(subsidiaryOption);

        // ����
        row.createCell(cellNum + 4).setCellValue(subsidiaryUnitAmount);
        center_columnList.add(cellNum + 4);

        // �ҿ䷮
        row.createCell(cellNum + 5).setCellValue(subsidiaryQuantity);
        center_columnList.add(cellNum + 5);

        if (!add_columnInfo.containsKey("subsidiaryInfo")) {
            HashMap<String, Object> columnIndex = new HashMap<String, Object>();
            columnIndex.put("startIndex", cellNum);
            columnIndex.put("lastIndex", cellNum + 5);
            add_columnInfo.put("subsidiaryInfo", columnIndex);
        }
    }

    /**
     * Header ����
     * 
     * @method printHeaderInfo
     * @date 2013. 11. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printHeaderInfo(Sheet currentSheet, IDataMap dataMap) {
        // product code, shop or line or station - id
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(0);
        cell.setCellValue(dataMap.getStringValue("productCode") + " - " + dataMap.getStringValue("compID"));

        // revision rule, revision rule ������
        row = currentSheet.getRow(2);
        cell = row.getCell(0);
        cell.setCellValue(dataMap.getStringValue("revisionRule") + "(" + dataMap.getStringValue("revRuleStandardDate") + ")");

        // �ɼ�
        cell = row.getCell(2);
        if (!dataMap.getStringValue("variantRule").isEmpty()) {
            cell.setCellValue(dataMap.getStringValue("variantRule"));
        }
    }

    /**
     * Header ���� ����� ���� ����(�۾� ����, E/Item, ������ ����)�� ���� ������ �÷� ��ġ�� ����Ǳ� ������ Excel ��� �Ͻø� ���� �з���.
     * 
     * @method printExcelExportDate
     * @date 2013. 11. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printExcelExportDate(Workbook workbook, Sheet currentSheet, IDataMap dataMap) {
        // ��� �Ͻ�
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 11);

        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        style.setFont(font);
        style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(new XSSFColor(new byte[] { 0, (byte) 204, (byte) 255 }));

        Row row = currentSheet.getRow(2);
        Cell cell = row.getCell(row.getLastCellNum() - 1);
        cell.setCellValue(String.format(registry.getString("report.ExcelExportDate", "��� �Ͻ� : %s"), dataMap.getStringValue("excelExportDate")));
        cell.setCellStyle(style);
    }

    /**
     * ����� ���� ����(�۾� ����, E/Item, ������ ����)�� ���� Header, Column �߰�
     * 
     * @method setCellStyleOfHeaderInfo
     * @date 2013. 11. 14.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setCellStyleOfHeaderInfo(Sheet currentSheet, int startIndex, int lastIndex, String subject, String[] columnName) {
        XSSFCellStyle cellStyle;
        Row row;
        Cell cell;

        // Excel 1, 2, 3�� �Ķ��� ��� ä���
        for (int i = 0; i < 3; i++) {
            row = currentSheet.getRow(i);
            cellStyle = (XSSFCellStyle) row.getCell(0).getCellStyle();
            for (int j = startIndex; j <= lastIndex; j++) {
                cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
            }
        }

        // Excel 5�� column ���� �ֱ�
        row = currentSheet.getRow(4);
        cell = row.getCell(0);
        cellStyle = (XSSFCellStyle) cell.getCellStyle();

        for (int i = startIndex; i <= lastIndex; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
        }
        cell = row.getCell(startIndex);
        cell.setCellValue(subject);

        // Excel 6�� column�� �ֱ�
        row = currentSheet.getRow(5);
        cell = row.getCell(0);
        cellStyle = (XSSFCellStyle) cell.getCellStyle();

        int index = 0;
        for (int j = startIndex; j <= lastIndex; j++) {
            cell = row.createCell(j);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(columnName[index]);
            index++;
        }
    }

    /**
     * Contents Cell Style ����
     * 
     * @method setCellStyleOfContents
     * @date 2013. 11. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setCellStyleOfContents(Workbook workbook, Sheet currentSheet) {
        // ��Ÿ��
        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);

        style.setFont(font);
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style.setWrapText(true);

        style1.setFont(font);
        style1.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style1.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        style1.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style1.setWrapText(true);

        // ��� ���� column
        int[] center_columnArray = new int[] { 0, 1, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 28, 29, 33, 34, 35 };
        for (int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        Row row;

        int lastRowNum = currentSheet.getLastRowNum();
        int lastCellNum = currentSheet.getRow(0).getLastCellNum();
        for (int i = DATA_START_ROW_INDEX; i <= lastRowNum; i++) {
            row = currentSheet.getRow(i);
            for (int j = 0; j < lastCellNum; j++) {
                Cell cell;
                if (row.getCell(j) == null) {
                    cell = row.createCell(j);
                } else {
                    cell = row.getCell(j);
                }

                if (center_columnList.contains(j)) {
                    cell.setCellStyle(style1);
                } else {
                    cell.setCellStyle(style);
                }
            }
        }
    }

    /**
     * Column ����
     * 
     * @method setColumnInfo
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void setColumnInfo(Sheet currentSheet) {
        HashMap<String, Object> columnIndex = new HashMap<String, Object>();

        // Column Width
        int optionCode_width = currentSheet.getColumnWidth(5);
        int id_width = currentSheet.getColumnWidth(25);
        int name_width = currentSheet.getColumnWidth(26);
        int quantity_width = currentSheet.getColumnWidth(29);

        int[] selectedValueFromDialog = (int[]) add_columnInfo.get("selectedValueFromDialog");

        for (int value : selectedValueFromDialog) {
            // value
            // 1 : �۾�����, 2 : End Item ����, 3 : ������ ����
            if (value == 1) {
                columnIndex = (HashMap<String, Object>) add_columnInfo.get("workInfo");
                int startIndex = (Integer) columnIndex.get("startIndex");
                int lastIndex = (Integer) columnIndex.get("lastIndex");

                // Column Name ����
                String subject = registry.getString("OperationMasterList.WorkInfo", "�۾� ����");
                String[] columnName = { registry.getString("OperationMasterList.WorkCode", "�۾� �ڵ�"), registry.getString("OperationMasterList.WorkContents", "�۾� ����") };

                // Column Width ����
                int[] columnWidthArray = new int[] { id_width, name_width };
                setColumnWidth(currentSheet, startIndex, lastIndex, columnWidthArray);

                setCellStyleOfHeaderInfo(currentSheet, startIndex, lastIndex, subject, columnName);
                currentSheet.addMergedRegion(new CellRangeAddress(4, 4, startIndex, lastIndex));
            } else if (value == 2) {
                columnIndex = (HashMap<String, Object>) add_columnInfo.get("endItemInfo");
                int startIndex = (Integer) columnIndex.get("startIndex");
                int lastIndex = (Integer) columnIndex.get("lastIndex");

                // Column Name ����
                String subject = "E/Item";
                String[] columnName = { "PART NO", registry.getString("OperationMasterList.EItemPartName", "PART ��"), registry.getString("OperationMasterList.EItemOption", "�ɼ�"), registry.getString("OperationMasterList.EItemQuantity", "����") };

                // Column Width ����
                int[] columnWidthArray = new int[] { id_width, name_width, optionCode_width, quantity_width };
                setColumnWidth(currentSheet, startIndex, startIndex + 3, columnWidthArray);

                setCellStyleOfHeaderInfo(currentSheet, startIndex, lastIndex, subject, columnName);
                currentSheet.addMergedRegion(new CellRangeAddress(4, 4, startIndex, lastIndex));
            } else if (value == 3) {
                columnIndex = (HashMap<String, Object>) add_columnInfo.get("subsidiaryInfo");
                int startIndex = (Integer) columnIndex.get("startIndex");
                int lastIndex = (Integer) columnIndex.get("lastIndex");

                // Column Name ����
                String subject = registry.getString("OperationMasterList.SubsidiaryInfo", "������ ����");
                String[] columnName = { registry.getString("OperationMasterList.SubsidiaryNo", "������ NO"), registry.getString("OperationMasterList.SubsidiaryName", "������ ��"), registry.getString("OperationMasterList.SubsidiaryEngName", "������ ������"), registry.getString("OperationMasterList.SubsidiaryOption", "���� �ɼ�"), registry.getString("OperationMasterList.SubsidiaryUnit", "����"),
                        registry.getString("OperationMasterList.SubsidiaryQuantity", "�ҿ䷮") };

                // Column Width ����
                int[] columnWidthArray = new int[] { id_width, name_width - 1000, name_width - 1000, optionCode_width, quantity_width, quantity_width + 500 };
                setColumnWidth(currentSheet, startIndex, startIndex + 5, columnWidthArray);

                setCellStyleOfHeaderInfo(currentSheet, startIndex, lastIndex, subject, columnName);
                currentSheet.addMergedRegion(new CellRangeAddress(4, 4, startIndex, lastIndex));
            }
        }

        add_columnInfo.clear();
    }

    /**
     * Column Width ����
     * 
     * @method setColumnWidth
     * @date 2013. 11. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setColumnWidth(Sheet currentSheet, int startIndex, int lastIndex, int[] columnWidthArray) {
        for (int i = startIndex; i <= lastIndex; i++) {
            currentSheet.autoSizeColumn(i);
            currentSheet.setColumnWidth(i, columnWidthArray[i - startIndex]);
        }
    }

}
