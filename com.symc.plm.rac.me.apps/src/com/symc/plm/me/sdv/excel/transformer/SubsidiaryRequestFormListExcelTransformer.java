package com.symc.plm.me.sdv.excel.transformer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.util.Registry;

import common.Logger;

public class SubsidiaryRequestFormListExcelTransformer extends AbstractExcelTransformer {

    private static Registry registry = Registry.getRegistry(SubsidiaryRequestFormListExcelTransformer.class);
    private static final Logger logger = Logger.getLogger(SubsidiaryRequestFormListExcelTransformer.class);
    public static final int DEFAULT_OPTION_DESC_ROW_NO = 5;
    public static final int DEFAULT_START_ROW_NO = 6;
    public static final int DEFAULT_COMMON_OPTION_COLUMN_NO = 8;
    public static final int DEFAULT_REMARK_COLUMN_NO = 17;
    public static final int DEFAULT_SUM_QUANTITY_ROW_NO = 18;

    public static final String DEFAULT_SUBSIDIARY_TYPE_FORMAT = "������ ���� : ( %s ����  %s ����  %s �Ҹ�)";
    public static final String DEFAULT_SUBSIDIARY_TYPE_SELECT = "��";
    public static final Object[] DEFAULT_SUBSIDIARY_TYPE_NORMAL = { "��", "��", "��" };

    private static final String DEFAULT_COMMON_OPTION_DESCRIPTION = "";

    private int dataStartIndex = DEFAULT_START_ROW_NO;
    private int commonOptionColIndex = DEFAULT_COMMON_OPTION_COLUMN_NO;
    private String operationType = "";

    public SubsidiaryRequestFormListExcelTransformer() {
        super();
    }

    /**
     * 
     * @method print
     * @date 2013. 11. 26.
     * @param mode
     *            , templatePreference, defaultFileName, dataSet
     * @return void
     * @throws Exception
     * @see
     */

    @Override
    public void print(int mode, String templatePreference, String defaultFileName, IDataSet dataSet) throws Exception {

        // ���ø�Ʈ���� ROW�� ������ ���� �ε��� ��ġ�� ������ ȯ�漳������ �����´�.
        // ���� �������� �������� ���ϰų� ������ �� ��� �⺻ ������ DEFAULT_START_ROW_NO=6 �� ����Ѵ�.
        try {
            TCSession session = ExcelTemplateHelper.getTCSession();
            TCPreferenceService tcprefService = session.getPreferenceService();
//            int rowStartIndex = tcprefService.getInt(TCPreferenceService.TC_preference_site, "DEFAULT_START_ROW_NO", DEFAULT_START_ROW_NO);
            Integer rowStartIndex = tcprefService.getIntegerValueAtLocation("DEFAULT_START_ROW_NO", TCPreferenceLocation.OVERLAY_LOCATION);
            if(rowStartIndex == null) {
            	rowStartIndex = DEFAULT_START_ROW_NO;
            }
            dataStartIndex = rowStartIndex;

//            int commonColIndex = tcprefService.getInt(TCPreferenceService.TC_preference_site, "DEFAULT_COMMON_OPTION_COLUMN_NO", DEFAULT_COMMON_OPTION_COLUMN_NO);
            Integer commonColIndex = tcprefService.getIntegerValueAtLocation("DEFAULT_COMMON_OPTION_COLUMN_NO", TCPreferenceLocation.OVERLAY_LOCATION);
            if(commonColIndex == null) {
            	commonColIndex = DEFAULT_COMMON_OPTION_COLUMN_NO;
            }

            commonOptionColIndex = commonColIndex;

        } catch (Exception ex) {
            logger.equals("DEFAULT_START_ROW_NO preference is not defined !!!");
            logger.error(ex);
        }

        // PreferenceName���� ���ø� ���� ��������
        templateFile = getTemplateFile(mode, templatePreference, defaultFileName);

        try {
            Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
            Map<String, XSSFCellStyle> cellStyles = ExcelTemplateHelper.getCellStyles(workbook);
            IDataMap dataMap = dataSet.getDataMap("SubsidiaryListMap");
            IData data = dataMap.get("SubsidiaryListMap");
            //String targetName = dataMap.getStringValue("SubsidiaryListTargetName");
            operationType = dataMap.getStringValue("operationType");
            // Data Value�� ���� ������ ��Ȯ�� �˰� �����Ƿ� @SuppressWarnings ������
            @SuppressWarnings("unchecked")
            HashMap<String, ArrayList<HashMap<String, Object>>> subsidiaryListMap = (HashMap<String, ArrayList<HashMap<String, Object>>>) data.getValue();
            boolean isArrayOp = "M7_BOPAssyOp".equals(operationType);

            Sheet currentSheet = null;
            boolean foundSubsidiaryList = false;
            if (subsidiaryListMap.size() == 0) {
                throw new TCException(registry.getString("report.subsidiaryMaterialError"));
            }
            //������ ǰ��No ������ subsidiaryListMap �����Ͽ� treeMap ����
            Map<String, ArrayList<HashMap<String, Object>>> treeMap = new TreeMap<String, ArrayList<HashMap<String, Object>>>(subsidiaryListMap);


            for (String key : treeMap.keySet()) {
                if (!foundSubsidiaryList)
                    foundSubsidiaryList = true;
                // if(workbook.getSheetIndex(currentSheet) >= 0)

                // �����簡 ���� ���� ����� �����´�.
                ArrayList<HashMap<String, Object>> subList = treeMap.get(key);
                if (subList == null)
                    continue;

                // ��ü ������ �Ҵ�� ������ ��
                int listSize = subList.size();
                int page = 0;

                List<Sheet> sheetList = new ArrayList<Sheet>();
                List<OptionColumnInfo> optionInfos = new ArrayList<OptionColumnInfo>();

                // �����纰 ǥ�õ� ���� (���� 12������ ���������� �����Ѵ�.)
                HashMap<String, SheetRowInfo> opForSubsidiraryListIgnoreOptionCode = new HashMap<String, SheetRowInfo>();
                int rowIndex = 0;

                for (int j = 0; j < listSize; j++) {
                    Sheet targetSheet = null;
                    String opID = (String) subList.get(j).get(operationType + SDVPropertyConstant.BL_ITEM_ID);
                    double subQuantity = 0.0;
                    try {
                        if ((Double) subList.get(j).get("quantity") == null) {
                            subQuantity = 0.0;
                        } else {
                            subQuantity = (Double) subList.get(j).get("quantity");
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }

                    String description = (String) subList.get(j).get("description");
                    String rowKey = opID;
                    // ������ ��� �ɼ� ������ ���ϹǷ� �ɼǺ� ������ ��� �����ϵ��� Ű�� �������̵�� �ɼǼ����� ���ļ� ����Ѵ�.
                    if (isArrayOp) {
                        rowKey = opID + ":" + description;
                    }

                    // �̹� ó���� ������ ��� �ҿ䷮�� ǥ��
                    if (opForSubsidiraryListIgnoreOptionCode.containsKey(rowKey)) {
                        rowIndex = opForSubsidiraryListIgnoreOptionCode.get(rowKey).rowIndex;
                        targetSheet = opForSubsidiraryListIgnoreOptionCode.get(rowKey).sheet;
                    } else {
                        // ó�� ����ϴ� ������ ��쿡�� ���� �߰��Ͽ� ������ ���
                        if (opForSubsidiraryListIgnoreOptionCode.size() % 12 == 0) {

                            // ù��° ��Ʈ�� �����Ѵ�. �۾��� ������ ù��° ��Ʈ�� �������ְ� �ȴ�.
                            targetSheet = workbook.cloneSheet(0);
                            currentSheet = targetSheet;
                            rowIndex = 0;
                            page++;
                            sheetList.add(targetSheet);
                            changeSheetName(targetSheet, sheetList.size(), subList.get(0));

                            if (!operationType.equals("M7_BOPAssyOp")) {
                                updateDescription(sheetList, optionInfos);
                            }
                        } else {
                            // �������� ���� �������� �ʴ´ٸ� �ֱ� ������ ��Ʈ�� ����Ѵ�.
                            targetSheet = currentSheet;
                        }
                        printRow(currentSheet, rowIndex++, subList.get(j), opID, cellStyles);
                        opForSubsidiraryListIgnoreOptionCode.put(rowKey, new SheetRowInfo(currentSheet, rowIndex));
                    }
                    // �ɼ� description
                    setOptionNQuantity(optionInfos, isArrayOp, targetSheet, rowIndex, description, subQuantity, subQuantity);

                }
                // ������ �����ϰ� ������ �ι��� �����纰 �� ��Ʈ�� �ɼ� ������ �����Ͽ� �� ��Ʈ�� �ɼ� ���� ������ �����.
                if (!isArrayOp) {
                    updateDescription(sheetList, optionInfos);
                }
                // �����纰 �����Է��� ��Ʈ ��ü ���� �Է��� ó���Ѵ�.
                updateSheetSummarise(sheetList, optionInfos, subList.get(0), page);
            }

            if (foundSubsidiaryList) {
                workbook.removeSheetAt(0);
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
     * 
     * @method setOptionNQuantity
     * @date 2013. 11. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setOptionNQuantity(List<OptionColumnInfo> optionInfos, boolean isArrayOp, Sheet targetSheet, int rowIndex, String description, double subQuantityStr, double subQuantity) {
        rowIndex = dataStartIndex + rowIndex - 1;
        // ������ ���� ���ü� �ֵ��� �ɼ� ������ ó�� �Ѱ��� ��ϵ��� �ʾ����� ���� ù�� ������ ����ɼ��� �⺻���� �־��ش�.
        if (optionInfos.size() == 0) {
            optionInfos.add(new OptionColumnInfo(DEFAULT_COMMON_OPTION_DESCRIPTION, 0, 0));
        }
        // ����
        if (isArrayOp) {
            // �ҿ䷮�� ���� �ɼ��� ���� �÷�(ù��° �÷�)�� ������ ǥ���Ѵ�. - ����
            Row rowQuantity = targetSheet.getRow(rowIndex);
            Cell cellQuantity = rowQuantity.getCell(DEFAULT_COMMON_OPTION_COLUMN_NO);
            cellQuantity.setCellValue(subQuantity);

            // ���� �ɼ� �÷��� �ջ� ó��
            optionInfos.get(0).sumQuntity(subQuantity);

            // description - ����
            // ������ ��� ��� �ɼ��� �������� ó���Ͽ� ���� �ɼ� �÷��� ������ �Է��ϳ�, ����ɼ��� �ƴ� ��쿡�� ������ �ɼ��ڵ带 ǥ�����ش�.
            // ������ �ƴ� �ɼ� �ڵ带 �񱳶��� �Է��Ѵ�
            if (!DEFAULT_COMMON_OPTION_DESCRIPTION.equals(description)) {
                Row rowDesc = targetSheet.getRow(rowIndex);
                Cell cellDesc = rowDesc.getCell(DEFAULT_REMARK_COLUMN_NO);
                cellDesc.setCellValue(description);
            }
            // ��ü
        } else {
            int optionIndex = 0;
            // �ɼ��ڵ尡 ������ �ѹ� ���Ծ������� Ȯ���ϱ� ���� �ɼ� �ڵ� ����� Ȯ���Ѵ�.
            // ������ ��ϵ� �ɼ��ڵ�� �ɼ����� ����Ʈ�� ��ϵǾ� �����Ƿ� ������ �ջ��ϰ� ���� �÷���ġ�� �� Row�� ������ ǥ���Ѵ�.
            // �̹� ���� �ɼ�ó���� �Ǿ� �־� ����ɼ��� ������ �ڵ����� ���� �� �÷��� ����.
            if (!containOptionInfo(optionInfos, description)) {
                // ���Ӱ� �ɼ��ڵ带 ����ϱ� ���� ������ ��ϵ� �ɼ��ڵ��� ������ �ε����� �����´� (���� ����Ʈ�� ũ�Ⱑ �߰��� ������ �ε����� �ȴ�.)
                optionIndex = optionInfos.size();
                optionInfos.add(new OptionColumnInfo(description, optionIndex, subQuantity));
            } else {
                // ������ ��ϵ� ���� �ɼ��ڵ��� �÷� �ε����� ã�ƿ´�.
                optionIndex = indexOfOptionInfo(optionInfos, description);
                // ������ ��ϵ� �ɼ��� ������ �ջ�ó���Ѵ�.
                optionInfos.get(optionIndex).sumQuntity(subQuantity);
            }
            // �ɼ� �ε����� 0 �����̹Ƿ� ���ø�Ʈ ���Ͽ��� ǥ��� �ɼ��ڵ��� ������ġ�� �����ش�. �⺻���� ���� �÷��� ���� ������ ǥ��ǹǷ� �����÷� �ε����� �����ָ�
            // �ش� �ɼ��� �÷��ε����� ���´�.
            int optionColIndex = optionIndex + commonOptionColIndex;

            // ������ �ҿ䷮�� �ش� �ɼ��÷��� ���� ���� �Է����ش�. - ��ü/����
            Row row1 = targetSheet.getRow(rowIndex);
            Cell cell = row1.getCell(optionColIndex);
            cell.setCellValue(subQuantity);
        }
    }

    /**
     * 
     * @method updateSheetSummarise
     * @date 2013. 11. 26.
     * @param sheetList
     *            , optionInfos, subDatamap, subDatamap
     * @return void
     * @exception Exception
     * @throws
     * @see
     */
    private void updateSheetSummarise(List<Sheet> sheetList, List<OptionColumnInfo> optionInfos, HashMap<String, Object> subDatamap, int page) throws Exception {
        // ������ �⺻ ���� ����
        for (int l = 0; l < sheetList.size(); l++) {
            printQuantitySum(sheetList.get(l), optionInfos);
            printSubRow(sheetList.get(l), subDatamap, page, l + 1);
        }
    }

    /**
     * 
     * @method containOptionInfo
     * @date 2013. 11. 26.
     * @param optionInfos
     *            , description
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean containOptionInfo(List<OptionColumnInfo> optionInfos, String description) {
        if (optionInfos != null) {
            for (OptionColumnInfo oi : optionInfos) {
                if (oi.optionDescription.equals(description))
                    return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param rowIndex
     * @method indexOfOptionInfo
     * @date 2013. 11. 26.
     * @param optionInfos
     *            , description
     * @return int
     * @exception
     * @throws
     * @see
     */
    private int indexOfOptionInfo(List<OptionColumnInfo> optionInfos, String description) {
        if (optionInfos != null) {
            for (OptionColumnInfo oi : optionInfos) {
                if (oi.optionDescription.equals(description)) {
                    return oi.columnIndex;
                }
            }
        }
        return 0;
    }

    /**
     * 
     * @method changeSheetName
     * @date 2013. 11. 25.
     * @param targetSheet
     *            , size, subsidiraryDataMap
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    protected boolean changeSheetName(Sheet targetSheet, int size, HashMap<String, Object> subsidiraryDataMap) {
        // Part No�� ��Ʈ �̸����� �����
        String partNoForSheetname = (String) subsidiraryDataMap.get(SDVPropertyConstant.BL_ITEM_ID);
        // �־��� ��Ʈ�� �ε����� ã�´�.
        Workbook parentWB = targetSheet.getWorkbook();
        int currentSheetIndex = parentWB.getSheetIndex(targetSheet);

        // ó�� ��Ʈ�� ��� partno���� ����Ѵ�.
        if (size == 1) {
            parentWB.setSheetName(currentSheetIndex, partNoForSheetname);
        } else if (size == 2) {
            // �ι�° ��Ʈ���ʹ� PartNo�� ��ȣ�� ���δ�. (�� �ι�°���� ������ PartNo�� �� Sheet�� ã�Ƽ� �̸��� �������ش�.
            int previousSheetIndex = parentWB.getSheetIndex(partNoForSheetname);
            if (previousSheetIndex >= 0) {
                // Ȥ�� partno�� �� ��Ʈ �̸��� �ȳ��� ��� ������ ���ֱ� ���Ͽ�
                parentWB.setSheetName(previousSheetIndex, partNoForSheetname + "(1)");
                parentWB.setSheetName(currentSheetIndex, partNoForSheetname + "(2)");
            } else {
                parentWB.setSheetName(currentSheetIndex, partNoForSheetname + "(1)");
            }
        } else if (size > 2) {
            // ����° ���Ĵ� �ε��� ��ȣ�� ��Ʈ ��ȣ�� �ٿ� �ش�.
            parentWB.setSheetName(currentSheetIndex, partNoForSheetname + "(" + size + ")");
        } else {
            // ���� ���
        }
        return true;
    }

    /**
     * 
     * @method updateDescription
     * @date 2013. 11. 25.
     * @param sheetList
     *            , optionInfos
     * @return List<Sheet>
     * @exception
     * @throws
     * @see
     */
    // ���Ӱ� ��Ʈ�� �߰��ǰų� �ɼ��� �߰��ɶ����� ������ ������ ��Ʈ ��ο� ����� �ɼ� ������ ��� �������ش�.
    private List<Sheet> updateDescription(List<Sheet> sheetList, List<OptionColumnInfo> optionInfos) {
        for (Sheet sheet : sheetList) {
            Row row = sheet.getRow(DEFAULT_OPTION_DESC_ROW_NO);
            for (int i = 0; i < optionInfos.size(); i++) {
                Cell cell = row.getCell(this.commonOptionColIndex + i);
                if (optionInfos.get(i).optionDescription.equals("")) {
                    cell.setCellValue("����" + optionInfos.get(i).getOptionDescription());
                } else {
                    cell.setCellValue(optionInfos.get(i).getOptionDescription());
                }
            }
        }
        return sheetList;
    }

    /**
     * 
     * @method printQuantitySum
     * @date 2013. 11. 25.
     * @param sheetList
     *            , optionInfos
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printQuantitySum(Sheet sheet, List<OptionColumnInfo> optionInfos) {
        Row row = sheet.getRow(DEFAULT_SUM_QUANTITY_ROW_NO);
        for (int i = 0; i < optionInfos.size(); i++) {
            if (optionInfos.get(i).optionDescription.equals("")) {
                Cell cell = row.getCell(DEFAULT_COMMON_OPTION_COLUMN_NO + i);
                cell.setCellValue(optionInfos.get(i).quantitySum);
            } else {
                Cell cell = row.getCell(this.commonOptionColIndex + i);
                cell.setCellValue(optionInfos.get(i).quantitySum);
            }
        }
    }

    /**
     * 
     * @method printSubRow
     * @date 2013. 11. 12.
     * @param currentSheet
     *            , dataMap, mapSize, page
     * @return void
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    private void printSubRow(Sheet currentSheet, HashMap<String, Object> dataMap, int mapSize, int page) throws TCException {

        // ������ ����
        Row row = currentSheet.getRow(3);
        Cell cell = row.getCell(0);

        // public static final String DEFAULT_SUBSIDIARY_TYPE_SELECT = "��";

        Object[] subsidiaryTypes = DEFAULT_SUBSIDIARY_TYPE_NORMAL.clone();
        if (dataMap.containsKey(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP)) {
            int typeIndex = ((Integer) dataMap.get(SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP)).intValue();
            subsidiaryTypes[typeIndex] = DEFAULT_SUBSIDIARY_TYPE_SELECT;
        }
        String subsidiaryTypeStr = String.format(DEFAULT_SUBSIDIARY_TYPE_FORMAT, subsidiaryTypes);
        cell.setCellValue(subsidiaryTypeStr);

        // �������
        row = currentSheet.getRow(19);
        cell = row.getCell(7);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_OBJECT_NAME));

        // �԰�
        row = currentSheet.getRow(21);
        cell = row.getCell(7);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_SPEC_KOR));

        // ��� ����
        row = currentSheet.getRow(20);
        cell = row.getCell(11);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SUBSIDIARY_BUY_UNIT));

        // ǰ��
        row = currentSheet.getRow(25);
        cell = row.getCell(11);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.BL_ITEM_ID));

        // �ۼ��μ�
        row = currentSheet.getRow(22);
        cell = row.getCell(16);
        cell.setCellValue((String) dataMap.get("group"));

        // �ۼ���
        row = currentSheet.getRow(23);
        cell = row.getCell(16);
        cell.setCellValue((String) dataMap.get("name"));

        // shopCode
        row = currentSheet.getRow(25);
        cell = row.getCell(16);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.SHOP_REV_SHOP_CODE));

        // productCode
        row = currentSheet.getRow(24);
        cell = row.getCell(16);
        cell.setCellValue((String) dataMap.get("productCode"));

        // �ż�
        row = currentSheet.getRow(21);
        cell = row.getCell(16);
        cell.setCellValue(page + " / " + mapSize);

    }

    /**
     * �� Row�� ����Ѵ�.
     * 
     * @method printRow
     * @date 2013. 10. 28.
     * @param currentSheet
     * @param num
     * @param dataMap
     * @param opID
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void printRow(Sheet currentSheet, int num, HashMap<String, Object> dataMap, String opID, Map<String, XSSFCellStyle> cellStyles) {

        int rowIndex = dataStartIndex + num;

        Row row = currentSheet.getRow(rowIndex);

        // Line
        Cell cell = row.getCell(2);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.LINE_REV_CODE));

        // ���� NO
        cell = row.getCell(3);
        cell.setCellValue((String) dataMap.get(SDVPropertyConstant.STATION_STATION_CODE));

        // ����(�۾�ǥ�ؼ�) NO
        cell = row.getCell(4);
        cell.setCellValue(opID);

        // ������
        cell = row.getCell(5);
        cell.setCellValue((String) dataMap.get(operationType + SDVPropertyConstant.BL_OBJECT_NAME));

    }

    protected class SheetRowInfo {
        Sheet sheet;
        int rowIndex;

        SheetRowInfo(Sheet sheet, int rowIndex) {
            this.sheet = sheet;
            this.rowIndex = rowIndex;
        }
    }

    protected class OptionColumnInfo {
        String optionDescription;
        int columnIndex;
        double quantitySum;

        public OptionColumnInfo(String optionDescription, int columnIndex, double quantitySum) {
            this.optionDescription = optionDescription;
            this.columnIndex = columnIndex;
            this.quantitySum = quantitySum;
        }

        public void sumQuntity(double quantity) {
            this.quantitySum += quantity;
        }

        public String getOptionDescription() {
            return optionDescription;
        }

        public int getColIndex() {
            return this.columnIndex;
        }

        public int compareOption(String anotherOptionDesc) {
            return this.optionDescription.compareTo(anotherOptionDesc);
        }

    }

}
