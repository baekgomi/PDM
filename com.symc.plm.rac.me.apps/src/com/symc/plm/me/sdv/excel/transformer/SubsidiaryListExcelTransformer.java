package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.ibm.icu.text.DecimalFormat;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.util.Registry;

public class SubsidiaryListExcelTransformer extends AbstractExcelTransformer {

    private Registry registry;

    private final int dataStartIndex = 5;
    public static final String DEFAULT_QUANTITY_FORMAT = "###,##0.00000";

    protected DecimalFormat dformat;
    private final int headerCellCount = 21;
    private int rowSize;

    public SubsidiaryListExcelTransformer() {
        super();
        this.registry = Registry.getRegistry(this);
        this.dformat = new DecimalFormat(DEFAULT_QUANTITY_FORMAT);
    }

    /**
     * 
     * @method print
     * @date 2013. 10. 30.
     * @param mode
     *            , templatePreference, defaultFileName, dataSet
     * @return void
     * @exception
     * @throws
     * @see
     */
    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) {
        // PreferenceName���� ���ø� ���� ��������
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            Map<String, XSSFCellStyle> cellStyles = ExcelTemplateHelper.getCellStyles(workbook);
            List<HashMap<String, Object>> dataList = getTableData(dataSet);

            if (dataList != null) {
                Sheet currentSheet = workbook.getSheetAt(0);

                IDataMap dataMap = dataSet.getDataMap("additionalInfo");
                printHeaderInfo(workbook, currentSheet, dataMap);

                int cnt = 0;
                rowSize = dataList.size();
                Cell firstCell = null;

                for (int i = 0; i < rowSize; i++) {
                    Row row = currentSheet.createRow(this.dataStartIndex + i);
                    printRow(currentSheet, i, row, dataList.get(i), cellStyles);
                    double firstValue = 0.0;
                    // �ҿ䷮ �հ�
                    if (i == 0) {
                        firstCell = row.createCell(7); // ���� row�� firstCell ����
                        if (dataList.get(i).get("quantity") == null) {
                            firstValue = 0.0;
                        } else {
                            firstValue = (Double) dataList.get(i).get("quantity");

                        }
                        firstCell.setCellValue(firstValue); // ���� row�� �� �־���

                    } else {
                        String previousItemId = ((String) dataList.get(i - 1).get(SDVPropertyConstant.BL_ITEM_ID)).trim();
                        String currentItemId = ((String) dataList.get(i).get(SDVPropertyConstant.BL_ITEM_ID)).trim();
                        if (previousItemId.equals(currentItemId)) {
                            double cCellValue = 0.0;
                            if (dataList.get(i).get("quantity") != null) {
                                cCellValue = (Double) dataList.get(i).get("quantity"); // ���� ���� ���ڰ�
                            }
                            row.createCell(7);
                            double sum1 = firstCell.getNumericCellValue() + cCellValue;
                            firstCell.setCellValue(sum1);
                            cnt++;

                        } else {

                            double sum2 = 0.0;
                            currentSheet.addMergedRegion(new CellRangeAddress(i + dataStartIndex - cnt - 1, i + dataStartIndex - 1, 7, 7)); // �� ����
                            firstCell = row.createCell(7);
                            if (dataList.get(i).get("quantity") != null) {
                                sum2 = (Double) dataList.get(i).get("quantity");
                            }
                            firstCell.setCellValue(sum2);
                            cnt = 0;
                        }
                    }

                }

                if (cnt > 0) {
                    currentSheet.addMergedRegion(new CellRangeAddress(rowSize + dataStartIndex - cnt - 1, rowSize + dataStartIndex - 1, 7, 7));

                }

                setBorderAndAlign(workbook, currentSheet, rowSize, cellStyles);

                XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook) workbook);
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

    /**
     * 
     * @method getTableData
     * @date 2013. 10. 30.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    private List<HashMap<String, Object>> getTableData(IDataSet dataSet) {
        Collection<HashMap<String, Object>> data = null;

        IDataMap dataMap = dataSet.getDataMap("operationList");
        if (dataMap != null) {
            data = dataMap.getTableValue("operationList");
        }

        return (List<HashMap<String, Object>>) data;
    }

    /**
     * 
     * @method printHeaderInfo
     * @date 2013. 11. 18.
     * @param currentSheet
     *            , dataMap
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printHeaderInfo(Workbook workbook, Sheet currentSheet, IDataMap dataMap) {
        // 2��
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(0);
        cell.setCellValue(dataMap.getStringValue("productCode") + " - " + dataMap.getStringValue("compID"));

        // 3��
        row = currentSheet.getRow(2);
        cell = row.getCell(0);
        cell.setCellValue(dataMap.getStringValue("revisionRule") + "(" + dataMap.getStringValue("revRuleStandardDate") + ")");

        cell = row.getCell(2);
        if (!dataMap.getStringValue("variantRule").isEmpty()) {
            cell.setCellValue(dataMap.getStringValue("variantRule"));
        }

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

        cell = row.getCell(20);
        cell.setCellValue(String.format(registry.getString("report.ExcelExportDate", "��� �Ͻ� : %s"), dataMap.getStringValue("excelExportDate")));
        cell.setCellStyle(style);
    }

    /**
     * �� Row�� ����Ѵ�.
     * 
     * @method printRow
     * @date 2013. 10. 30.
     * @param currentSheet
     *            , num, row, dataMap, cellStyles
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printRow(Sheet currentSheet, int num, Row row, HashMap<String, Object> dataMap, Map<String, XSSFCellStyle> cellStyles) {

        // NO
        Cell cell = row.createCell(0);
        cell.setCellValue(num + 1);

        // ������ ǰ��
        cell = row.createCell(1);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_ID));

        // ������ ǰ��
        cell = row.createCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_REV_OBJECT_NAME));

        // �԰�
        cell = row.createCell(3);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR));

        //20201113 seho buy unit�� unit amount�� ����
        // ���Ŵ���
        // �ҿ䷮����
        cell = row.createCell(4);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT));
//        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT));

        // ��ü��
        cell = row.createCell(5);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_MAKER));

        // �ҿ䷮
        cell = row.createCell(6);

        if (dataMap.get("quantity") == null) {
            cell.setCellValue(0.0);
        } else {
            cell.setCellValue((Double) dataMap.get("quantity"));
        }

        // ������ �ɼ��ڵ�
        cell = row.createCell(8);
        cell.setCellValue(((String) dataMap.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION)));

        // ������ �ɼ� Description
        cell = row.createCell(9);
        cell.setCellValue((String) dataMap.get("description"));

        // ��ǰ�� No.
        cell = row.createCell(10);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_OLDPART));

        // Line
        cell = row.createCell(11);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.LINE_REV_CODE));

        // ������
        cell = row.createCell(12);
        cell.setCellValue(((String) dataMap.get("line_id")));

        // Line Rev
        cell = row.createCell(13);
        cell.setCellValue((String) dataMap.get("line_revision"));

        // ���� NO
        cell = row.createCell(14);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_REV_CODE));

        // ���� Rev
        cell = row.createCell(15);
        cell.setCellValue((String) dataMap.get("station_revision"));

        // ����(�۾�ǥ�ؼ�) NO
        cell = row.createCell(16);
        cell.setCellValue((String) dataMap.get("operation_id"));

        // ���� Rev
        cell = row.createCell(17);
        cell.setCellValue((String) dataMap.get("operation_revision"));

        // MECO
        cell = row.createCell(18);
        cell.setCellValue((String) dataMap.get("mecoNo"));

        // ���� Release Date
        cell = row.createCell(19);
        cell.setCellValue((String) dataMap.get("afterDate"));

        // ���� Effectivity
        cell = row.createCell(20);
        cell.setCellValue((String) dataMap.get("effectDate"));

    }

    /**
     * ���� ���� �׸���
     * 
     * @method setBorderAndAlign
     * @date 2013. 10. 30.
     * @param currentSheet
     *            , cellStyles
     * @param rowSize
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setBorderAndAlign(Workbook workbook, Sheet currentSheet, int rowSize, Map<String, XSSFCellStyle> cellStyles) {
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

        style1.setFont(font);
        style1.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        style1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style1.setAlignment(XSSFCellStyle.ALIGN_CENTER);

        // ��� ���� column
        int[] center_columnArray = new int[] { 0, 1, 7, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
        ArrayList<Integer> center_columnList = new ArrayList<Integer>();
        for (int i = 0; i < center_columnArray.length; i++) {
            center_columnList.add(center_columnArray[i]);
        }

        Row row = null;

        int lastRowNum = dataStartIndex + rowSize;
        for (int i = dataStartIndex; i < lastRowNum; i++) {
            row = currentSheet.getRow(i);
            for (int j = 0; j < headerCellCount; j++) {
                if (center_columnList.contains(j)) {
                    row.getCell(j).setCellStyle(style1);
                } else {
                    row.getCell(j).setCellStyle(style);
                }
            }
        }
    }
}
