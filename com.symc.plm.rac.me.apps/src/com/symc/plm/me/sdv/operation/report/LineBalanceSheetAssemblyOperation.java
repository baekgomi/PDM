/**
 * 
 */
package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : LineBalanceSheetAssemblyOperation
 * Class Description : ���� ���� ��ǥ ������ �����ϴ� Ŭ����
 * [SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
 * 
 * @date 2013. 12. 16.
 * 
 */
public class LineBalanceSheetAssemblyOperation extends SimpleSDVExcelOperation {

    private Registry registry = null;
    private boolean isWorkMaxTime = false;
    private TCComponentBOMLine selectedBOMLine = null; // ���õ� BOMLine

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation#getData()
     */
    @Override
    protected IDataSet getData() throws Exception {

        int mainStationCount = 0, subStationCount = 0; // ���� ���� ����, ���� ���� ����
        int mainStationWorkerCount = 0, subStationWorkerCount = 0; // ���� ���� �۾��� ����, ���� ���� �۾��� ����
        double mainStationTotalTime = 0, mainStationTotalRate = 0; // ���� ���� �۾��ð� �հ�, ���� ���� ���� �հ�
        double subStationTotalTime = 0, subStationTotalRate = 0; // ���� ���� �۾��ð� �հ�, ���� ���� ���� �հ�
        ArrayList<String> stationDupCheck = new ArrayList<String>(); // �ߺ��� ���� üũ
        ArrayList<String> workCodeDupCheck = new ArrayList<String>(); // �ߺ��� �۾��� �ڵ� üũ

        TCComponentBOMLine shopBOPLine = selectedBOMLine.window().getTopBOMLine();
        TCComponentItemRevision shopItemRevision = shopBOPLine.getItemRevision();
        String lineName = selectedBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
        String lineRevId = selectedBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);

        String productCode = shopItemRevision.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
        TCComponentPerson personComp = (TCComponentPerson) SDVBOPUtilities.getTCSession().getUser().getReferenceProperty("person");
        String loginGroup = personComp.getProperty(TCComponentPerson.PROP_PA6);

        //[SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
        double jph = shopItemRevision.getTCProperty(SDVPropertyConstant.SHOP_REV_JPH).getDoubleValue();
        double tackTime = 3600 / jph; // Tack Time

        String baseOnPrint = isWorkMaxTime ? "�ִ� �۾� �ð� ����" : "��ǥ ���� ����";

        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();

        // ���� ��ǥ Row Data ����
        LinkedHashMap<String, RowDataBean> rowDataMap = new LinkedHashMap<String, RowDataBean>();
        // ������ �������� Row Data ����Ʈ�� ����
        getOperationList(selectedBOMLine, rowDataMap);
        // ���� ���� Row ����Ʈ ���·� ����
        ArrayList<RowDataBean> assyLineRowList = new ArrayList<RowDataBean>(rowDataMap.values());

        // ���� �հ� ������ ������
        for (String key : rowDataMap.keySet()) {
            RowDataBean rowData = rowDataMap.get(key);
            String stationNo = rowData.getStationNo();
            String workCode = rowData.getWorkerCode();
            double workTime = rowData.getMaxTime();
            double rate = (workTime / tackTime) * 100;
            // ����,���� �� ���� ��, �����ð� �ջ�, ���� �� �ð� �ջ�
            if (!stationDupCheck.contains(stationNo)) {
                stationDupCheck.add(stationNo);
                // Sub ���� �� ���
                if (stationNo.substring(1, 1).equals("S")) {
                    subStationCount++;
                    subStationTotalTime += workTime;
                    subStationTotalRate += rate;
                } else {
                    mainStationCount++;
                    mainStationTotalTime += workTime;
                    mainStationTotalRate += rate;
                }
            }
            // �۾��� ��
            if (!workCodeDupCheck.contains(workCode)) {
                if (stationNo.substring(1, 1).equals("S"))
                    subStationWorkerCount++;
                else
                    mainStationWorkerCount++;
                workCodeDupCheck.add(workCode);
            }
        }

        // �۾��׷� ������ Sort
        Collections.sort(assyLineRowList, new RowDataComparator());

        //[SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
        dataMap.put(SDVPropertyConstant.SHOP_REV_JPH, jph, IData.STRING_FIELD); // JPH
        dataMap.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, productCode); // Product Code
        dataMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, lineName); // Line ��
        dataMap.put(SDVPropertyConstant.ITEM_REVISION_ID, lineRevId); // Line Revision ID

        dataMap.put("MAIN_STAION_COUNT", mainStationCount, IData.INTEGER_FIELD); // ���� ���� ����
        dataMap.put("MAIN_STATION_TOTAL_TIME", mainStationTotalTime, IData.OBJECT_FIELD); // ���� ���� �ð� �հ�
        dataMap.put("MAIN_STATION_TOTAL_RATE", mainStationTotalRate, IData.OBJECT_FIELD); // ���� ���� ���� �հ�

        dataMap.put("SUB_STAION_COUNT", subStationCount, IData.INTEGER_FIELD); // ���� ���� ����
        dataMap.put("SUB_STATION_TOTAL_TIME", subStationTotalTime, IData.OBJECT_FIELD); // ���� ���� �ð� �հ�
        dataMap.put("SUB_STATION_TOTAL_RATE", subStationTotalRate, IData.OBJECT_FIELD); // ���� ���� ���� �հ�

        dataMap.put("MAIN_STAION_WORKER_COUNT", mainStationWorkerCount, IData.INTEGER_FIELD); // ���� �۾��� ��
        dataMap.put("SUB_STAION_WORKER_COUNT", subStationWorkerCount, IData.INTEGER_FIELD); // ���� �۾��� ��

        dataMap.put("STATION_ROW_LIST", assyLineRowList, IData.OBJECT_FIELD); // Row �� �������� ����Ʈ

        dataMap.put("BASED_ON_PRINT", baseOnPrint); // ��� ����
        dataMap.put("LOGIN_GROUP", loginGroup); // �ۼ� �μ�

        dataSet.addDataMap("ASSY_LINE_DATA", dataMap);
        return dataSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        registry = Registry.getRegistry(this);

        isWorkMaxTime = (Boolean) localDataMap.get(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK).getValue();

        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        selectedBOMLine = mfgApp.getSelectedBOMLines()[0]; // ���õ� BOMLINE

        IDataSet dataSet = getData();

        String defaultFileName = registry.getString("exportLineBalancingAssembly.FileName") + "_" + ExcelTemplateHelper.getToday("yyMMdd");
        transformer.print(mode, templatePreference, defaultFileName, dataSet);

    }

    /**
     * ������ ���� ������ ������
     * 
     * @method getOperationList
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getOperationList(TCComponentBOMLine parentBOMLine, LinkedHashMap<String, RowDataBean> rowDataMap) throws Exception {

        AIFComponentContext[] aifContexts = parentBOMLine.getChildren();
        for (AIFComponentContext aifcomp : aifContexts) {
            TCComponentBOMLine childBOMLine = (TCComponentBOMLine) aifcomp.getComponent();
            String childItemType = childBOMLine.getItem().getType();
            if (!childItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM))
                continue;
            getOpRowData(childBOMLine, rowDataMap);
        }
    }

    /**
     * Operation ������ ������
     * 
     * @method getOpRowData
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getOpRowData(TCComponentBOMLine opBOMLine, LinkedHashMap<String, RowDataBean> rowDataMap) throws Exception {
        TCComponentItem opItem = opBOMLine.getItem();
        TCComponentItemRevision opRevision = opBOMLine.getItemRevision();

        boolean isMaxWorkTime = opItem.getLogicalProperty(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK);// �ִ��۾��ð� ����
        boolean isRepVehicle = opItem.getLogicalProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK); // ��ǥ ���� ����

        String opName = opItem.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME); // ������
        String workerCode = opItem.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE); // �۾��� �ڵ�
        String stationNo = opRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);// ���� ��ȣ

        // �ִ� �۾� �ð�,
        if (isWorkMaxTime) {
            if (!isMaxWorkTime)
                return;
        } else {
            if (!isRepVehicle)
                return;
        }

        // �۾��� �ڵ�, ���� �ڵ� �� ������ Pass
        if (workerCode.isEmpty() || stationNo.isEmpty())
            return;

        // �۾��ð� ���� ������
        double workTime = getSumWorkTime(opBOMLine);

        RowDataBean rowDataValue = new RowDataBean(stationNo, workerCode, opName, workTime);
        if (!rowDataMap.containsKey(rowDataValue.toString())) {
            rowDataMap.put(rowDataValue.toString(), rowDataValue);
            // �ߺ��� �����ڵ�, �۾��� �ڵ� ������ ������
        } else {
            rowDataValue = rowDataMap.get(rowDataValue.toString());
            double maxTime = rowDataValue.getMaxTime();
            double currentTime = rowDataValue.getCurrentTime();

            // �ִ� �ð��� ��� ��ǥ �������� ������
            if (workTime > currentTime) {
                rowDataValue.setOpName(opName);
                rowDataValue.setCurrentTime(workTime);
            }
            // (�����ڵ� ,�۾��� �ڵ�) ���� �ð��� ���Ͽ� ������
            workTime += maxTime;
            rowDataValue.setMaxTime(workTime);
        }

    }

    // ������ȣ, �۾��ο�, �۾��ð�, ��ǥ �۾���

    /**
     * ������ �۾��ð��� ������
     * 
     * @method getWorkTime
     * @date 2013. 12. 17.
     * @param
     * @return double
     * @exception
     * @throws
     * @see
     */
    private double getSumWorkTime(TCComponentBOMLine opBOMLine) throws Exception {

        double sumWorkTime = 0;
        TCComponent actRootComp = opBOMLine.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);

        if (actRootComp == null || !(actRootComp instanceof TCComponentCfgActivityLine))
            return sumWorkTime;

        TCComponentMEActivity rootActivity = (TCComponentMEActivity) actRootComp.getUnderlyingComponent();
        TCComponent[] activities = ActivityUtils.getSortedActivityChildren(rootActivity);
        for (TCComponent activity : activities) {
            activity.refresh();
            double systemUnitTime = activity.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
            double frequency = activity.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
            sumWorkTime += (systemUnitTime * frequency);
        }

        return sumWorkTime;
    }

    /**
     * �����ڵ�, �۾����ڵ庰 Row Data �� �����ϴ� Data Ŭ����
     * Class Name : RowDataBean
     * Class Description :
     * 
     * @date 2013. 12. 17.
     * 
     */
    public class RowDataBean {
        private String opName = "";
        private String stationNo = "";
        private String workerCode = "";
        private double maxTime = 0;
        private double currentTime = 0;

        public RowDataBean(String stationNo, String workerCode, String opName, double currentTime) {
            this.stationNo = stationNo;
            this.workerCode = workerCode;
            this.opName = opName;
            this.currentTime = currentTime;
            this.maxTime = currentTime;
        }

        /**
         * @return the opName
         */
        public String getOpName() {
            return opName;
        }

        /**
         * @param opName
         *            the opName to set
         */
        public void setOpName(String opName) {
            this.opName = opName;
        }

        /**
         * @return the stationNo
         */
        public String getStationNo() {
            return stationNo;
        }

        /**
         * @param stationNo
         *            the stationNo to set
         */
        public void setStationNo(String stationNo) {
            this.stationNo = stationNo;
        }

        /**
         * @return the workerCode
         */
        public String getWorkerCode() {
            return workerCode;
        }

        /**
         * @param workerCode
         *            the workerCode to set
         */
        public void setWorkerCode(String workerCode) {
            this.workerCode = workerCode;
        }

        /**
         * @return the maxTime
         */
        public double getMaxTime() {
            return maxTime;
        }

        /**
         * @param maxTime
         *            the maxTime to set
         */
        public void setMaxTime(double maxTime) {
            this.maxTime = maxTime;
        }

        /**
         * @return the currentTime
         */
        public double getCurrentTime() {
            return currentTime;
        }

        /**
         * @param currentTime
         *            the currentTime to set
         */
        public void setCurrentTime(double currentTime) {
            this.currentTime = currentTime;
        }

        public String toString() {
            return stationNo + ";" + workerCode;
        }
    }

    /**
     * Row Data �۾��� �ڵ� ������ Sort
     * Class Name : RowDataComparator
     * Class Description :
     * 
     * @date 2013. 12. 23.
     * 
     */
    public class RowDataComparator implements Comparator<RowDataBean> {
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(RowDataBean paramT1, RowDataBean paramT2) {
            String workerCode1 = "", workerCode2 = "";
            workerCode1 = paramT1.getWorkerCode();
            workerCode2 = paramT2.getWorkerCode();
            return workerCode1.compareTo(workerCode2);
        }
    }
}
