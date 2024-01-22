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
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;

public class SpecialProcessMasterListExcelTransformer extends AbstractExcelTransformer {

    private final int HEADER_CELL_COUNT = 30;
    private final int DATA_START_INDEX = 9;

    private int currentRow = 9;

    ArrayList<Integer> underlineRowList = new ArrayList<Integer>();

    public SpecialProcessMasterListExcelTransformer() {
        super();
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) {
        // PreferenceName���� ���ø� ���� ��������
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            if(dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                IDataMap dataMap = dataSet.getDataMap("additionalInfo");
                printHeaderInfo(currentSheet, dataMap.getStringValue("processType_kor_name"));
                int rowSize = dataList.size();
                for(int i = 0; i < rowSize; i++) {
                    printRow(currentSheet, i, dataList.get(i));
                }
                setCellStyleOfContents(workbook, currentSheet);
                currentRow = 9;
                underlineRowList.clear();
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
        if(dataMap != null) {
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
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap) {
        Row row = getRow(currentSheet, 0);

        // NO
        Cell cell = row.getCell(0);
        cell.setCellValue(num + 1);

        // ���� NO
        cell = row.getCell(1);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_LINE) + "-" + (String) dataMap.get(SDVPropertyConstant.STATION_STATION_CODE));

        // ������
        cell = row.getCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.ITEM_OBJECT_NAME));

        // D ���� �� ���� ǥ��
        cell = row.getCell(3);
        cell.setCellValue("");

        // �۾� ����
        row = printEndItemList(currentSheet, (ArrayList<String>) dataMap.get("endItemList"));
        int underlineRow = 0;
        underlineRow = row.getRowNum();

        // ����/���
        row = printEquipmentList(currentSheet, (ArrayList<String>) dataMap.get("equipmentList"));
        if(underlineRow < row.getRowNum()) {
            underlineRow = row.getRowNum();
        }

        underlineRowList.add(underlineRow);
        currentRow = underlineRow + 1;
    }

    /**
     * �۾� ���� List ���
     *
     * @method printEndItemList
     * @date 2014. 1. 24.
     * @param
     * @return Row
     * @exception
     * @throws
     * @see
     */
    private Row printEndItemList(Sheet currentSheet, ArrayList<String> endItemList) {
        Row row = null;

        for(int i = 0; i <= endItemList.size(); i++) {
            if(i == endItemList.size()) {
                row = getRow(currentSheet, i);
                break;
            }

            row = getRow(currentSheet, i);
            Cell cell = row.getCell(4);
            cell.setCellValue(endItemList.get(i));
        }

        return row;
    }

    /**
     * ����/���(Robot, Gun) List ���
     *
     * @method printEquipmentList
     * @date 2014. 1. 24.
     * @param
     * @return Row
     * @exception
     * @throws
     * @see
     */
    private Row printEquipmentList(Sheet currentSheet, ArrayList<String> equipmentList) {
        Row row = null;

        for(int i = 0; i <= equipmentList.size(); i++) {
            if(i == equipmentList.size()) {
                row = getRow(currentSheet, i);
                break;
            }

            row = getRow(currentSheet, i);
            Cell cell = row.getCell(5);
            cell.setCellValue(equipmentList.get(i));
        }

        return row;
    }

    /**
     * Header ����
     * (��ü/����) Ư������ ��������
     *
     * @method printHeaderInfo
     * @date 2013. 11. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printHeaderInfo(Sheet currentSheet, String processType_kor_name) {
        Row row = currentSheet.getRow(2);
        Cell cell = row.getCell(0);
        cell.setCellValue(processType_kor_name + cell.getStringCellValue());
    }

    /**
     *
     *
     * @method getRow
     * @date 2014. 1. 24.
     * @param
     * @return Row
     * @exception
     * @throws
     * @see
     */
    private Row getRow(Sheet currentSheet, int addRow) {
        Row row = currentSheet.getRow(currentRow + addRow);
        if(row == null) {
            row = currentSheet.createRow(currentRow + addRow);
            for(int i = 0; i < HEADER_CELL_COUNT; i++) {
                if(row.getCell(i) == null) {
                    row.createCell(i);
                }
            }
        }

        return row;
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
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short)10);

        int lastRowNum = currentSheet.getLastRowNum();
        short rowHeight = currentSheet.getRow(DATA_START_INDEX).getHeight();
        for(int i = DATA_START_INDEX; i <= lastRowNum; i++) {
            Row row = currentSheet.getRow(i);
            row.setHeight(rowHeight);
            for(int j = 0; j < HEADER_CELL_COUNT; j++) {
                XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
                style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
                style.setBorderRight(XSSFCellStyle.BORDER_THIN);
                style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
                style.setFont(font);

                // NO, ���� NO - ��� ����
                if(j == 0 || j == 1) {
                    style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
                }

                // D ���� border(left, right)�� �׸��� �ʴ´�.
                if(j == 3) {
                    style.setBorderLeft(XSSFCellStyle.BORDER_NONE);
                    style.setBorderRight(XSSFCellStyle.BORDER_NONE);
                }

                // E ���� border(left)�� �׸��� �ʴ´�.
                if(j == 4) {
                    style.setBorderLeft(XSSFCellStyle.BORDER_NONE);
                }

                // E, F�� �� ���� - ���� ����
                if(j == 4 || j == 5) {
                    style.setAlignment(XSSFCellStyle.ALIGN_LEFT);

                    if(style.getCoreXf().getAlignment() == null) {
                        style.getCoreXf().setAlignment(CTCellAlignment.Factory.newInstance());
                    }
                    style.getCoreXf().getAlignment().setShrinkToFit(true);
                }

                // ���� �� ���ٷ� ����
                if(underlineRowList.contains(i)) {
                    if(i != underlineRowList.get(underlineRowList.size() - 1)) {
                        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
                    }
                }

                if(i == lastRowNum) {
                    style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
                }

                Cell cell = row.getCell(j);
                cell.setCellStyle(style);
            }

            // �� ����
            currentSheet.addMergedRegion(new CellRangeAddress(i, i, 6, 10));
            currentSheet.addMergedRegion(new CellRangeAddress(i, i, 11, 13));
            currentSheet.addMergedRegion(new CellRangeAddress(i, i, 14, 17));
            currentSheet.addMergedRegion(new CellRangeAddress(i, i, 18, 21));
            currentSheet.addMergedRegion(new CellRangeAddress(i, i, 22, 25));
            currentSheet.addMergedRegion(new CellRangeAddress(i, i, 26, 29));
        }
    }

}
