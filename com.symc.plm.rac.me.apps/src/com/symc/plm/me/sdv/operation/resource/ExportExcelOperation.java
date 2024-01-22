/**
 * 
 */
package com.symc.plm.me.sdv.operation.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.teamcenter.rac.aif.AIFDesktop;

/**
 * [20140430][SR140507-042] shcho, ������ �˻� ��� Excel�� �������� ��� �߰��� ���� Class ����
 * 
 * @author shcho
 * 
 */
public class ExportExcelOperation {

    public static final String FILE_DEFAULT_NAME = "Resource_Search_Result_List";

//    private StringBuilder strData;              // [20140430][SR140507-042]
    private List<List<String>> tableValue;  // [20140430][SR140507-042]
    private String highlightKey;                // [20140430][SR140507-042]
    private int highlightPosition;              // [20140430][SR140507-042]

    // 20201118 seho table �����͸� StringBuilder�� ������������ ����Ʈ�� �޾� ó��.... �����ڸ� ����ϴ°�...xxxxx
    public ExportExcelOperation(List<List<String>> _allDataList) {
    	tableValue = _allDataList;
    }

    // [20140430][SR140507-042]
    public ExportExcelOperation(List<List<String>> rowValueList, String highlightKey, int highlightPosition) {
        this.tableValue = rowValueList;
        this.highlightKey = highlightKey;
        this.highlightPosition = highlightPosition;
    }

    @SuppressWarnings("unused")
    public void executeOperation() throws Exception {

        // ����� ���� ���
        String fileName = openFileDialog(FILE_DEFAULT_NAME, "xlsx");

        if (fileName != null) {
            File file = new File(fileName);

            if (file.exists()) {
                org.eclipse.swt.widgets.MessageBox confirmBox = new org.eclipse.swt.widgets.MessageBox(AIFDesktop.getActiveDesktop().getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
                confirmBox.setMessage("A file named " + file.getName() + " already exists. Are you sure you want to overwrite it?");

                if (confirmBox.open() != SWT.OK) {
                    return;
                }
            }

            // ���� ���� �ۼ�
            XSSFWorkbook workbook = createExcel();

            // ���� ���� ����
            FileOutputStream fileOutput = new FileOutputStream(file);
            workbook.write(fileOutput);
            fileOutput.close();

            /* ���� ���� ���� */
            Runtime runtime = Runtime.getRuntime();
            String strCmd = "cmd /c \"" + file.getAbsolutePath();
            Process p = runtime.exec(strCmd);
        }
    }

    /**
     * Excel ���� �ۼ� �Լ�
     * 
     * @return
     */
    public XSSFWorkbook createExcel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        // Font ����
        XSSFFont font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);

        // ������ ��Ÿ�� ����
        XSSFCellStyle titlestyle = workbook.createCellStyle();
        titlestyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        titlestyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        titlestyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        titlestyle.setBorderLeft(BorderStyle.THIN);
        titlestyle.setBorderRight(BorderStyle.THIN);
        titlestyle.setBorderTop(BorderStyle.THIN);
        titlestyle.setBorderBottom(BorderStyle.DOUBLE);
        titlestyle.setFont(font);


        // Original �ҽ� ([20140430][SR140507-042] shcho, ������ �˻� ��� Excel�� �������� ��� �߰��� ���� Class ���� ��)
        // int headColumnCount = 0;
        // String arrRowValue[] = strData.toString().split("\n");
        // for (int i = 0; i < arrRowValue.length; i++) {
        // XSSFRow row = sheet.createRow(i);
        //
        // String arrCellValue[] = arrRowValue[i].split("%%");
        // int columnCount = arrCellValue.length;
        // if (i == 0)
        // headColumnCount = columnCount;
        // for (int j = 0; j < headColumnCount; j++) {
        // XSSFCell cell = row.createCell(j);
        // cell.setCellValue((j >= columnCount) ? "" : arrCellValue[j]);
        //
        // // ��Ÿ�� ����
        // if (i == 0) {
        // cell.setCellStyle(titlestyle);
        // } else {
        // cell.setCellStyle(style);
        // }
        // }
        // }

        // [20140430][SR140507-042] shcho, ������ �˻� ��� Excel�� �������� ��� �߰��� ���� �ҽ� ����
        // 20201118 seho �̺κ��� �ʿ����.
//        if (tableValue == null) {
//            tableValue = setDataToArrayList(allDataList);
//        }

        int rowCount = tableValue.size();
        int headColumnCount = 0;
        for (int i = 0; i < rowCount; i++) {
            XSSFRow row = sheet.createRow(i);

            List<String> rowValue = tableValue.get(i);
            int columnCount = rowValue.size();
            if (i == 0)
                headColumnCount = columnCount;

            for (int j = 0; j < headColumnCount; j++) {
                XSSFCell cell = row.createCell(j);
                String columnValue = (j >= columnCount) ? "" : rowValue.get(j);
                cell.setCellValue(columnValue);

                // ��Ÿ�� ����
                if (i == 0) {
                    cell.setCellStyle(titlestyle);
                } else {
                    // �߰� ����//
                    // [20140430][SR140507-042] shcho, TC�� ���ϵ� ������ ȸ������ ���� ǥ��
                    if (ishighlight(rowValue)) {
                        cell.setCellStyle(setCellStyle(workbook, font, HSSFColor.GREY_25_PERCENT.index));
                    } else {
                        cell.setCellStyle(setCellStyle(workbook, font, HSSFColor.WHITE.index));
                    }
                    // �߰� ����//
                }
            }
        }

        // �� ũ�� �ڵ� ����
        for (int i = 0; i <= headColumnCount; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    /**
     * Cell�� Style ���� �Լ�
     * 
     * [20140430][SR140507-042] shcho, �Լ��űԻ���
     * 
     * @param workbook
     * @param font
     * @param colorIndex
     * @return XSSFCellStyle
     */
    public XSSFCellStyle setCellStyle(XSSFWorkbook workbook, XSSFFont font, short colorIndex) {
        // ���� ��Ÿ�� ����
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(colorIndex);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND); // [20140430][SR140507-042] shcho, TC�� ���ϵ� ������ ȸ������ ���� ǥ��
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setFont(font);
        return style;
    }

    /**
     * Excel Row�� ����ǥ��(����) �Ҷ� ������ �Ǵ� Ű �� ���� �Լ� (highlightPosition���� ���� highlightKey�� ��ġ�ϴ��� ��)
     * 
     * [20140430][SR140507-042] shcho, �Լ��űԻ���
     * 
     * @param arrCellValue
     * @return boolean
     */
    public boolean ishighlight(List<String> arrCellValue) {
        if (highlightKey != null && highlightKey.length() > 0) {

            // System.out.println(highlightPosition + " >> " + arrCellValue.length + "  >> " + arrCellValue);
            if (highlightKey.equals(arrCellValue.get(highlightPosition))) {
                return true;
            }
        }
        return false;
    }

    /**
     * ������ ���� ��ġ ���� �Լ�
     * 
     * @return String
     */
    public String openFileDialog(final String defaultFileName, final String extention) {
        FileDialog fileDialog = new FileDialog(AIFDesktop.getActiveDesktop().getShell(), SWT.SAVE);
        fileDialog.setFileName(defaultFileName);
        fileDialog.setFilterExtensions(new String[] { "*." + extention });
        String selectedFile = fileDialog.open();

        return selectedFile;
    }

    // 20201118 seho �����ڸ� ����ϴ°� ���� ����.  xx xxxxxx
//    /**
//     * StringBuilderŸ������ �����ϴ� Data�� ArrayList<ArrayList<String>>Ÿ������ ��ȯ
//     * 
//     * [20140430][SR140507-042] shcho, �Լ��űԻ���
//     * 
//     * @param strData
//     * @return ArrayList<ArrayList<String>>
//     */
//    public List<List<String>> setDataToArrayList(StringBuilder strData) {
//        List<List<String>> arrListRowValues = new ArrayList<List<String>>();
//        List<String> arrListColumnValues = new ArrayList<String>();
//
//        String arrRowValue[] = strData.toString().split("\n");
//        for (int i = 0; i < arrRowValue.length; i++) {
//            String arrCellValue[] = arrRowValue[i].split("%%");
//            arrListColumnValues = Arrays.asList(arrCellValue);
//            arrListRowValues.add(arrListColumnValues);
//        }
//        return arrListRowValues;
//    }

}
