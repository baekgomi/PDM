/**
 * 
 */
package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.ibm.icu.util.Calendar;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.operation.report.LineBalanceSheetAssemblyOperation.RowDataBean;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : AssemblyLineBalanceSheetExcelTransformer
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
 * 
 * @date 2013. 12. 16.
 * 
 */
public class AssemblyLineBalanceSheetExcelTransformer extends AbstractExcelTransformer {
    private final int dataStartIndex = 4;
    private final int rowCnt = 29;
    private Registry registy = null;

    public AssemblyLineBalanceSheetExcelTransformer() {
        super();
        registy = Registry.getRegistry(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.IExcelTransformer#print(int, java.lang.String, java.lang.String, org.sdv.core.common.data.IDataSet)
     */
    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {

        try {
            templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

            IDataMap dataMap = dataSet.getDataMap("ASSY_LINE_DATA");

            @SuppressWarnings("unchecked")
            ArrayList<RowDataBean> rowDataList = (ArrayList<RowDataBean>) dataMap.getValue("STATION_ROW_LIST");

            int mainStationCount = dataMap.getIntValue("MAIN_STAION_COUNT");// ���� ���� ����
            int mainStationWorkerCount = dataMap.getIntValue("MAIN_STAION_WORKER_COUNT");// ���� �۾��� ����
            double mainStationTotalTime = (Double) dataMap.getValue("MAIN_STATION_TOTAL_TIME");// ���� ���� �ð� �հ�
            double mainStationTotalRate = (Double) dataMap.getValue("MAIN_STATION_TOTAL_RATE");// ���� ���� ���� �հ�

            int subStationCount = dataMap.getIntValue("SUB_STAION_COUNT"); // ���� ���� ����
            int subStationWorkerCount = dataMap.getIntValue("SUB_STAION_WORKER_COUNT");// ���� �۾��� ����
            double subStationTotalTime = (Double) dataMap.getValue("SUB_STATION_TOTAL_TIME");// ���� ���� �ð� �հ�
            double subStationTotalRate = (Double) dataMap.getValue("SUB_STATION_TOTAL_RATE");// ���� ���� ���� �հ�

            //[SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
            double jph = ((Double) dataMap.getValue(SDVPropertyConstant.SHOP_REV_JPH)).doubleValue();
            String productCode = dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            String lineName = dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME);

            String baseOnPrint = dataMap.getStringValue("BASED_ON_PRINT"); // ��� ����
            Registry clientReg = Registry.getRegistry("client_specific");

            // FIXME: Copy Sheet ����
            if (rowDataList.size() / rowCnt > 0 && !(rowDataList.size() / rowCnt == 1 && rowDataList.size() % rowCnt == 0)) {
                int count = (rowDataList.size() % rowCnt != 0) ? rowDataList.size() / rowCnt : rowDataList.size() / rowCnt - 1;
                String exePath = clientReg.getString("runnerCommand").replace("runner", "");
                String cmd = "cmd /C " + exePath + "ExcelUtil.exe  /copySheet file=" + templateFile.getAbsolutePath() + " src=Sheet1 count=" + count;
                Process process = Runtime.getRuntime().exec(cmd);
                process.waitFor();
            }

            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            workbook.setForceFormulaRecalculation(true);

            if (rowDataList.size() != 0) {
                Sheet currentSheet = workbook.getSheetAt(0);

                int rowSize = rowDataList.size();
                int index = 0;

                for (index = 0; index < (rowSize / rowCnt) + 1; index++) {
                    if (index > 0)
                        currentSheet = copySheet(workbook);
                    // FIXME: Copy Sheet ����
                    currentSheet = workbook.getSheetAt(index);

                    if (currentSheet == null)
                        continue;
                    // Header ���
                    printHeader(currentSheet, dataMap);
                    for (int i = 0; i < rowDataList.size(); i++) {
                        if (i == rowCnt)
                            break;
                        // FIXME: Copy Sheet ����
                        if (index == rowSize / rowCnt) {
                            if (i == (rowSize % rowCnt))
                                break;
                        }
                        RowDataBean rowData = rowDataList.get(i + (rowCnt * index));
                        printRow(currentSheet, i, i + (rowCnt * index) + 1, rowData);
                    }

                    workbook.getSheetAt(index).getRow(34).getCell(3).setCellValue(mainStationCount);
                    workbook.getSheetAt(index).getRow(34).getCell(5).setCellValue(mainStationWorkerCount);
                    workbook.getSheetAt(index).getRow(34).getCell(6).setCellValue(mainStationTotalTime);
                    workbook.getSheetAt(index).getRow(34).getCell(7).setCellValue(mainStationTotalRate);

                    workbook.getSheetAt(index).getRow(37).getCell(3).setCellValue(subStationCount);
                    workbook.getSheetAt(index).getRow(37).getCell(5).setCellValue(subStationWorkerCount);
                    workbook.getSheetAt(index).getRow(37).getCell(6).setCellValue(subStationTotalTime);
                    workbook.getSheetAt(index).getRow(37).getCell(7).setCellValue(subStationTotalRate);

                    workbook.getSheetAt(index).getRow(40).getCell(10).setCellValue(productCode);
                    workbook.getSheetAt(index).getRow(40).getCell(15).setCellValue(lineName);
                    workbook.getSheetAt(index).getRow(41).getCell(15).setCellValue(jph);

                    workbook.getSheetAt(index).getRow(39).getCell(2).setCellValue(baseOnPrint);
                }

                // workbook.getSheetAt(index - 1).getRow(34).getCell(3).setCellValue(mainStationCount);
                // workbook.getSheetAt(index - 1).getRow(34).getCell(5).setCellValue(mainStationWorkerCount);
                // workbook.getSheetAt(index - 1).getRow(34).getCell(6).setCellValue(mainStationTotalTime);
                // workbook.getSheetAt(index - 1).getRow(34).getCell(7).setCellValue(mainStationTotalRate);
                //
                // workbook.getSheetAt(index - 1).getRow(37).getCell(3).setCellValue(subStationCount);
                // workbook.getSheetAt(index - 1).getRow(37).getCell(5).setCellValue(subStationWorkerCount);
                // workbook.getSheetAt(index - 1).getRow(37).getCell(6).setCellValue(subStationTotalTime);
                // workbook.getSheetAt(index - 1).getRow(37).getCell(7).setCellValue(subStationTotalRate);
                //
                // workbook.getSheetAt(index - 1).getRow(40).getCell(10).setCellValue(productCode);
                // workbook.getSheetAt(index - 1).getRow(40).getCell(15).setCellValue(lineName);
                // workbook.getSheetAt(index - 1).getRow(41).getCell(15).setCellValue(jph);
            }

            FileOutputStream fos = new FileOutputStream(templateFile);
            workbook.write(fos);
            fos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private void printHeader(Sheet currentSheet, IDataMap dataMap) throws Exception {
        String productCode = dataMap.getStringValue(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
        String lineName = dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME);
        String lineRevId = dataMap.getStringValue(SDVPropertyConstant.ITEM_REVISION_ID);
        String loginGroup = dataMap.getStringValue("LOGIN_GROUP");

        String header = productCode + " " + lineName + " " + registy.getString("PaintLineBalanceSheetTitle");
        Row row = currentSheet.getRow(1);
        Cell cell = row.getCell(2);
        cell.setCellValue(header);

        // ���� ��ȣ
        Cell lineRevCell = row.getCell(14);
        lineRevCell.setCellValue(lineRevId);

        // �ۼ��μ�
        Row groupRow = currentSheet.getRow(0);
        Cell gorupCell = groupRow.getCell(14);
        gorupCell.setCellValue(loginGroup);

        // �ۼ���
        Row dateRow = currentSheet.getRow(2);
        Cell dateCell = dateRow.getCell(14);
        Calendar cal = Calendar.getInstance();

        String year = Integer.toString(cal.get(Calendar.YEAR));
        String month = Integer.toString(cal.get(Calendar.MONTH)+1);
        String date = Integer.toString(cal.get(Calendar.DATE));

        dateCell.setCellValue(year.concat("�� ").concat(month).concat("�� ").concat(date).concat("��"));
    }

    private void printRow(Sheet currentSheet, int rowNum, int seq, RowDataBean rowData) {
        Row row = currentSheet.getRow(dataStartIndex + rowNum);

        // NO
        Cell cell = row.getCell(0);
        // cell.setCellValue(seq + 1);
        cell.setCellValue(seq);

        // ���� ��ȣ
        cell = row.getCell(1);
        cell.setCellValue(rowData.getStationNo());

        // ��ǥ �۾�
        cell = row.getCell(2);
        cell.setCellValue(rowData.getOpName());

        // �۾��� �ڵ�
        cell = row.getCell(5);
        cell.setCellValue(rowData.getWorkerCode());

        // �۾��ð�
        cell = row.getCell(6);
        cell.setCellValue((Double) rowData.getMaxTime());
    }

    private Sheet copySheet(Workbook workbook) {
        return null;
    }
}
