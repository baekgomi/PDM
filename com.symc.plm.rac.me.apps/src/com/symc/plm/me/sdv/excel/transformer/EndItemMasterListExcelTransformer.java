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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;

/**
 * [SR150119-032,SR150122-027][20150210] shcho, End Item Master Report �ӵ� ���� �� replaced end item ���� ǥ�� (10�������� ���� �� �ҽ� 9���� �̽���)
 */
public class EndItemMasterListExcelTransformer extends AbstractExcelTransformer {

    private final int DATA_START_ROW_INDEX = 5;
    private final int CELL_COUNT = 45;

    public EndItemMasterListExcelTransformer() {
        super();
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) {
        // PreferenceName���� ���ø� ���� ��������
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            List<HashMap<String, Object>> dataList = getTableData(dataSet);
            IDataMap additionalInfoMap = dataSet.getDataMap("additionalInfo");

            if (dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                int rowSize = dataList.size();
                for (int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i), additionalInfoMap);
                }

                printHeaderInfo(currentSheet, additionalInfoMap);
                printBorder(workbook, currentSheet);
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
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap, IDataMap additionalInfoMap) {
        Row row = currentSheet.createRow(this.DATA_START_ROW_INDEX + num);

        int column = 0;
        // ����
        Cell cell = row.createCell(column++);
        cell.setCellValue(num + 1);

        // ����
        cell = row.createCell(column++);
        cell.setCellValue(additionalInfoMap.getStringValue("productCode"));

        // FUNC NO.
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("funcNo"));

        // SEQ
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_SEQUENCE_NO));

        // SEQ (Product)
        // [NONE_SR][20151116] taeku.jeong ��Ǽ�����(����1��)�� ��û���� �Ҵ�� M-BOM BOM Line�� Sequence No�� End Item�� �߰�
        // ME_DOCTEMP_01 Template�� ã�Ƽ� �����ؾ���. (4�� Cell �߰��ϰ� ���� Cell No �ϳ��� �����ð���.)
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("productSeqNo"));
        
        // ǰ��
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_ID));

        // ǥ�� ǰ��
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("DisplayPartNo"));

        // Rev
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_REV_ID));

        // ǰ��
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OBJECT_NAME));

        // Option Code
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("itemOptionCode"));

        // Option Description
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("itemOptionDescription"));

        // Module
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("module"));

        // Pos Description
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("posDesc"));

        // Reference
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("reference"));

        // S/MODE
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.S7_SUPPLY_MODE));

        // ����IN-ECO NO.
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.S7_ECO_NO));

        // ��������
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OWNING_USER));

        // SHOWNON
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("shown"));

        // Est. WGT
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("est_wgt"));

        // Cal. WGT
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("cal_wgt"));

        // Act. WGT
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("act_wgt"));

        // Thickness
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("thickness"));

        // MATERIAL
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("material"));

        // Release ����
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_RELEASE_STATUS));

        // Release Date
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_DATE_RELEASED));

        // BP Type (x)
        cell = row.createCell(column++);
        cell.setCellValue("");

        // BP Effectivity
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(""));

        // COLOR
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("color"));

        // COLOR Section No
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("colorSection"));

        // DR
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("dr"));

        // ����
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_QUANTITY));

        // Line
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("line_code"));

        // Line Rev
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("line_rev"));

        // ����
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("station_code"));

        // ���� Rev
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("station_rev"));

        // ����(�۾�ǥ�ؼ�)No
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("operation_id"));

        //20201110 seho ������ �߰�.
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("operation_name"));

        // ���� Rev
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("operation_rev"));

        // ���� Option Code
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("operation_optionCode"));

        // ���� Option Description
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("operation_optionDescription"));

        // ��ġ(L/R)
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("locLRProperty"));

        // ��ġ(U/R)
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("locUBProperty"));

        // MECO No.
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("mecoNo"));

        // E/Item ����-����
        cell = row.createCell(column++);
        cell.setCellValue("");

        // �ý���
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("assy_system"));

        // ��������
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("operation_owning_user"));

        // ���
        cell = row.createCell(column++);
        cell.setCellValue((String) dataMap.get("replacedEndItem"));
    }

    private void printHeaderInfo(Sheet currentSheet, IDataMap additionalInfoMap) {
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(0);
        cell.setCellValue(additionalInfoMap.getStringValue("productCode") + " - " + additionalInfoMap.getStringValue("compID"));

        row = currentSheet.getRow(2);
        cell = row.getCell(0);
        cell.setCellValue(additionalInfoMap.getStringValue("revisionRule") + "(" + additionalInfoMap.getStringValue("revRuleStandardDate") + ")");

        cell = row.getCell(3);
        cell.setCellValue(additionalInfoMap.getStringValue("variantRule"));

        cell = row.getCell(45);
        cell.setCellValue(additionalInfoMap.getStringValue("excelExportDate"));
    }

    private void printBorder(Workbook workbook, Sheet currentSheet) {
        CellStyle style_1 = workbook.createCellStyle();
        CellStyle style_2 = workbook.createCellStyle();

        // Font font = workbook.createFont();
        // font.setFontName("Arial");
        // font.setFontHeightInPoints((short) 10);

        // style_1.setFont(font);
        style_1.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style_1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style_1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style_1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style_1.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        // style_1.setWrapText(true);

        // style_2.setFont(font);
        style_2.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style_2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style_2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style_2.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style_2.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        style_2.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        // style_2.setWrapText(true);

        int[] center_columnArray = new int[] { 0, 1, 2, 3, 4, 6, 13, 14, 15, 22, 23, 24, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 38, 39, 40, 42, 43, 44, 45 };
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for (int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        int lastRowNum = currentSheet.getLastRowNum();
        for (int i = DATA_START_ROW_INDEX; i <= lastRowNum; i++) {
            Row row = currentSheet.getRow(i);
            for (int j = 0; j <= CELL_COUNT; j++) {
                if (center_columnList.contains(j)) {
                    row.getCell(j).setCellStyle(style_2);
                } else {
                    row.getCell(j).setCellStyle(style_1);
                }
            }
        }
    }
}
