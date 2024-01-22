/**
 * 
 */
package com.symc.plm.me.sdv.operation.swm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.excel.transformer.AbstractExcelTransformer;

/**
 * Class Name : RegisterSWMDocExcelTransformer
 * Class Description :
 * 
 * @date 2013. 11. 17.
 * 
 */
public class RegisterSWMDocExcelTransformer extends AbstractExcelTransformer {

    public RegisterSWMDocExcelTransformer() {
        super();
    }

    @Override
    public void print(int mode, String templatePreference, String defaultFileNameObject, IDataSet dataSet) throws Exception {

        // PreferenceName���� ���ø� ���� ��������
        templateFile = getTemplateFile(mode, templatePreference, defaultFileNameObject);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            workbook.setForceFormulaRecalculation(true);
            Map<String, XSSFCellStyle> cellStyles = ExcelTemplateHelper.getCellStyles(workbook);
            IDataMap dataMap = dataSet.getDataMap("registerSWMDocView");
            if (dataMap != null) {
                Sheet currentSheet = workbook.getSheetAt(0);
                printRow(currentSheet, dataMap, cellStyles);

                FileOutputStream fos = new FileOutputStream(templateFile);
                workbook.write(fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ���� ���ø��� ������ �Ӽ����� ��������
     * 
     * @method printRow
     * @date 2013. 10. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printRow(Sheet currentSheet, IDataMap dataMap, Map<String, XSSFCellStyle> cellStyles) {

        // �Ϸù�ȣ (item ID)
        Row row = currentSheet.getRow(2);
        row.getCell(20).setCellValue(dataMap.getStringValue(SDVPropertyConstant.ITEM_ITEM_ID));

        // �ۼ������(������ ������)
        row = currentSheet.getRow(4);
        row.getCell(20).setCellValue(SDVStringUtiles.dateToString((Date) dataMap.getValue(SDVPropertyConstant.ITEM_CREATION_DATE), "yyyy-MM-dd"));

        // ��� (������ ������) owning_user
        row = currentSheet.getRow(3);
        row.getCell(27).setCellValue(dataMap.getStringValue(SDVPropertyConstant.ITEM_OWNING_USER).split(" ")[0]);

        // ����(LOV value)
        row = currentSheet.getRow(6);
        row.getCell(0).setCellValue(dataMap.getStringValue(SDVPropertyConstant.SWM_CATEGORY));

        // ����(LOV Desc)
        row = currentSheet.getRow(11);
        row.getCell(13).setCellValue(dataMap.getStringValue(SDVPropertyConstant.SWM_VEHICLE_CODE));

        // ���ñٰ�(���� �� item ID)
        row = currentSheet.getRow(11);
        row.getCell(24).setCellValue(dataMap.getStringValue(SDVPropertyConstant.ITEM_M7_REFERENCE_INFO));

        // �۾���(���� �� ������)
        row = currentSheet.getRow(12);
        row.getCell(13).setCellValue(dataMap.getStringValue(SDVPropertyConstant.ITEM_OBJECT_NAME));

    }

}
