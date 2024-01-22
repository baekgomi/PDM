/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.work.peif;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.ValidateSDVException;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.exception.SkipException;
import com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob;
import com.symc.plm.me.sdv.service.migration.job.peif.PEIFTCDataExecuteJob;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.basic.ItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivitySubData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EndItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.EquipmentData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.LineItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OccurrenceData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SheetDatasetData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.SubsidiaryData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ToolData;
import com.symc.plm.me.sdv.service.migration.util.PEExcelConstants;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

/**
 * Class Name : PEValidation
 * Class Description :
 * 
 * 
 * �������� �Ҵ� FindNo ��Ģ :
 * 1 ~ 9 : �Ϲ�����(END-ITEM)
 * 10 ~ 200 : ����(TOOL)
 * 210 ~ 500 : ����(EQUIPMENT)
 * 510 ~ 800 : ������(SUBSIDIARY)
 * 
 * @date 2013. 11. 25.
 * 
 */
public class PEValidation extends PEDataWork {

    /**
     * @param shell
     * @param tcDataMigrationJob
     * @param processLine
     * @param mecoNo
     * @param isOverride
     */
    public PEValidation(Shell shell, TCDataMigrationJob tcDataMigrationJob, TCComponentMfgBvrProcess processLine, String mecoNo, boolean isOverride) {
        super(shell, tcDataMigrationJob, processLine, mecoNo, isOverride);
    }

    public void rowValidate(final int index, final TCData tcData) throws Exception {
    	
        final ArrayList<Exception> exception = new ArrayList<Exception>();
        
        Runnable aValidationRun = new Runnable() {
            public void run() {
                String message = "";
                String logMessage = "";
                String differLog = "";
                try {
                    // Progress �޼���
                    tcData.setStatus(TCData.STATUS_INPROGRESS);
                    getTcDataMigrationJob().getTree().setSelection(tcData);
                    // LINE validate
                    if (tcData instanceof LineItemData) {
                        validateLine(processLine, (LineItemData) tcData);
                    } else
                    // OPERATION Validate
                    if (tcData instanceof OperationItemData) {
                        // check Operation
                        differLog = printDifferenceResult(tcData, validateOperation((OperationItemData) tcData));
                    }
                    // Activity ó��
                    else if (tcData instanceof ActivityMasterData) {
                        // check Activity
                        differLog = validateActivity((ActivityMasterData) tcData);
                    }
                    // Resource - EquipmentData ó��
                    else if (tcData instanceof EquipmentData) {
                        // check EquipmentData
                        differLog = printDifferenceResult(tcData, validateEquipmentData((EquipmentData) tcData));
                    }
                    // Resource - ToolData ó��
                    else if (tcData instanceof ToolData) {
                        // check ToolData
                        differLog = printDifferenceResult(tcData, validateToolData((ToolData) tcData));
                    }
                    // Resource - EndItemData ó��
                    else if (tcData instanceof EndItemData) {
                        // check EndItemData
                        differLog = printDifferenceResult(tcData, validateEndItemData((EndItemData) tcData));
                    }
                    // Resource - SubsidiaryData ó��
                    else if (tcData instanceof SubsidiaryData) {
                        // check SubsidiaryData
                        differLog = printDifferenceResult(tcData, validateSubsidiaryData((SubsidiaryData) tcData));
                    }
                    // �۾�ǥ�ؼ� DatasetData ó��
                    else if (tcData instanceof SheetDatasetData) {
                        // SheetDatasetData
                        differLog = printDifferenceResult(tcData, validateSheetDatasetData((SheetDatasetData) tcData));
                    }
                } catch (Exception e) {
                	if((e instanceof SkipException)==false){
                		e.printStackTrace();
                	}
                    exception.add(e);
                } finally {
                    // Error �޼��� ó��
                    if (exception.size() > 0) {
                        message = exception.get(0).getMessage();
                        // Skip Exception ó��
                        if (exception.get(0) instanceof SkipException) {
                            SkipException skipException = (SkipException) exception.get(0);
                            tcData.setStatus(skipException.getStatus(), message);
                        } else {
                            tcData.setStatus(TCData.STATUS_ERROR, message);
                        }
                        logMessage = "{" + tcData.getText() + "} : " + tcData.getStatusMessage();
                    }
                    // �޼��� ó��
                    else {
                        logMessage = processingMessage(tcData);
                        if (!StringUtils.isEmpty(differLog)) {
                            logMessage += ("\n" + differLog);
                        }
                    }
                    // Log ó��
                    saveLog(tcData, exception, logMessage);
                }
            }

            /**
             * CLASS Type�� ����ó��(Status ����), �޼��� ó��
             * 
             * @method processingMessage
             * @date 2013. 11. 27.
             * @param
             * @return String
             * @exception
             * @throws
             * @see
             */
            private String processingMessage(final TCData tcData) {
                String message;
                String logMessage;
                int rowStatus = TCData.STATUS_VALIDATE_COMPLETED;
                message = COMPLETED_MESSAGE;
                // ����(Operation) DataType ó��
                if (tcData instanceof OperationItemData) {
                    // Operation ������ ��� �޼��� ó��
                    if (!((OperationItemData) tcData).isExistItem()) {
                        message = "Createable Operation";
                    }
                }
                // Activity DataType ó��
                else if (tcData instanceof ActivityMasterData) {
                    if (((ActivityMasterData) tcData).isCreateable()) {
                        message = "Activity�� ����Ǿ� ���� ����Դϴ�.";
                        // //���� ������ VALIDATE_COMPLETED �����̸� STATUS_WARNING�� �����Ѵ�.
                        if (TCData.STATUS_VALIDATE_COMPLETED == ((TCData) tcData.getParentItem()).getStatus()) {
                            ((TCData) tcData.getParentItem()).setStatus(TCData.STATUS_WARNING, message);
                        }
                    } else {
                        message = "Activity ���� ������ ���� Skip ó���մϴ�.";
                        rowStatus = TCData.STATUS_SKIP;
                    }
                }
                // Activity DataType ó��
                else if (tcData instanceof ActivitySubData) {
                    // Master�� �����̸� ���� Sub�� ���� ���
                    if (((ActivitySubData) tcData).isCreateable()) {
                        message = "�ű� �Ǵ� �����׸�";
                    } else {
                        rowStatus = TCData.STATUS_SKIP;
                    }
                }
                // OccurrenceData - EquipmentData, ToolData, EndItemData, SubsidiaryData ó��
                else if (tcData instanceof OccurrenceData) {
                    if (((OccurrenceData) tcData).getResourceItem() == null) {
                        message = "Resource Item�� ���������ʾ� Master ���� ��� �� ó���մϴ�.";
                    }
                    if (((OccurrenceData) tcData).getBopBomLine() != null) {
                        // BOM ������Ʈ�� ������ Skip
                        if (!((OccurrenceData) tcData).isBOMLineModifiable()) {
                            rowStatus = TCData.STATUS_SKIP;
                        }
                    }
                    // Resource - EquipmentData ó��
                    if (tcData instanceof EquipmentData) {

                    }
                    // Resource - ToolData ó��
                    if (tcData instanceof ToolData) {

                    }
                    // Resource - EndItemData ó��
                    if (tcData instanceof EndItemData) {

                    }
                    // Resource - SubsidiaryData ó��
                    if (tcData instanceof SubsidiaryData) {

                    }
                }
                tcData.setStatus(rowStatus, message);
                logMessage = "{" + tcData.getText() + "} : " + tcData.getStatusMessage();
                return logMessage;
            }
        };

        // [NON-SR][2016.01.07] taeku.jeong PE->TC Interface ������ Down�Ǵ� ������ �ذ��� ���� Thread�� ��������� �Ҹ� �� �� �ִ¹������ ������ 
//        Thread aValidationThread = new Thread(aValidationRun);
        shell.getDisplay().syncExec(aValidationRun);
//        aValidationThread.stop();
//        aValidationThread = null;
        
        if (exception.size() > 0) {
            // SKIP Exception �̸� ������ Throw���� �ʴ´�.
            if (!(exception.get(0) instanceof SkipException)) {
                throw exception.get(0);
            }
        }
    }

    /**
     * ����(Operation)���� Child Item�� �˻��Ѵ�.
     * 
     * @method findOperationUnderData
     * @date 2013. 11. 26.
     * @param
     * @return ArrayList<TCComponentItem>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<TCComponentBOMLine> findOperationUnderDataItem(OccurrenceData occurrenceData) throws Exception {
        ArrayList<TCComponentBOMLine> findBOMLineList = new ArrayList<TCComponentBOMLine>();
        TCComponentBOMLine[] childBOMLine = ((OperationItemData) occurrenceData.getParentItem()).getOperationChildComponent();
        for (int i = 0; i < childBOMLine.length; i++) {
            String tcItemId = childBOMLine[i].getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            if (occurrenceData.getItemId().equals(tcItemId)) {
                findBOMLineList.add(childBOMLine[i]);
            }
        }
        return findBOMLineList;
    }

    /**
     * ���� Activity ��ȿ�� ��
     * 
     * m7_CONTROL_BASIS=ü��
     * m7_ENG_NAME=,
     * time_system_category=03,
     * m7_WORK_OVERLAP_TYPE=,
     * time_system_frequency=1.0,
     * m7_CONTROL_POINT=ü��,
     * object_name=����,
     * SEQ=60,
     * time_system_unit_time=10.0,
     * time_system_code=Test 03
     * 
     * @method validateActivity
     * @date 2013. 11. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private String validateActivity(ActivityMasterData activityMasterData) throws Exception {
        StringBuffer log = new StringBuffer();
        boolean creteable = false;
        // ActivityMasterData�� Parent���� TC���� ��ȸ�� Activity List�� �����´�.
        List<HashMap<String, Object>> activityList = ((OperationItemData) activityMasterData.getParentItem()).getOperationActivityList();
        TreeItem[] activitySubDatas = activityMasterData.getItems();
        for (int i = 0; i < activitySubDatas.length; i++) {
            // TC-PE �� Activity �Ӽ��� Ʋ���� ����
            HashMap<String, Object> tcActivityData = findActivity(activityList, (ActivitySubData) activitySubDatas[i]);
            if (tcActivityData == null) {
                // ActivityMasterData ���� ���� ��� - Activity �׸��� 1���� ����Ǹ� ��ü ����
                creteable = setActivityModify(creteable, (ActivitySubData) activitySubDatas[i]);
            } else {
                ArrayList<String> activitySubRowData = (ArrayList<String>) ((ActivitySubData) activitySubDatas[i]).getData();
                // ���� ���� Log
                LinkedHashMap<String, String[]> differenceResult = validateActivitySubAttributes(tcActivityData, activitySubRowData);
                if (differenceResult.size() > 0) {
                    // ActivityMasterData ���� ���� ��� - Activity �׸��� 1���� ����Ǹ� ��ü ����
                    creteable = setActivityModify(creteable, (ActivitySubData) activitySubDatas[i]);
                    log.append(printDifferenceResult((TCData) activitySubDatas[i], differenceResult));
                } else {
                    ((ActivitySubData) activitySubDatas[i]).setCreateable(false);
                }
                // ���� ���� Activity�̸� �������� TC���� ������ PE�� �ٽ� �����Ѵ�. (PE I/F �������� null�� ��� ����, �������� ������ ��� PE �������� ���)
                // ������ PE I/F���� ���� �������� ���� ������ �ٽ� ������Ʈ �� �������� ������� ������ �߻��ϴ� ���� ��.
                // if (StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_ENG_NAME_COLUMN_INDEX))) {
                // Activity �������� ���� ������ ������ (differenceResult�� SDVPropertyConstant.ACTIVITY_OBJECT_NAME(Activity ������) �� ����� 'null' -> ������ ������ �����Ƿ� �������� �״�� ���)
                // ���� TC�� �������� �״�� ����Ѵ�.
                if (!differenceResult.containsKey(SDVPropertyConstant.ACTIVITY_OBJECT_NAME)) {
                    activitySubRowData.set(PEExcelConstants.ACTIVITY_MASTER_ENG_NAME_COLUMN_INDEX, BundleUtil.nullToString((String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_ENG_NAME)));
                }
            }
        }
        // Activity�� ���� ����
        activityMasterData.setCreateable(creteable);
        // TC Activity ���� PE I/F Activity���� ���������� ������ ����
        if (activitySubDatas.length != activityList.size()) {
            log.append("Activity�� ���� TC-PE ���� ��ġ���� �ʾ� ��ü �����մϴ�. \n");
            activityMasterData.setCreateable(true);
        }
        return log.toString();
    }

    /**
     * ActivityMasterData ���� ���� ��� - Activity �׸��� 1���� ����Ǹ� ��ü ����
     * 
     * @method setActivityModify
     * @date 2013. 12. 11.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private boolean setActivityModify(boolean creteable, ActivitySubData activitySubData) {
        // ActivityMasterData ���� ���� ��� - Activity �׸��� 1���� ����Ǹ� ��ü ����
        if (creteable == false) {
            creteable = true;
        }
        // ActivitySubData ���� ���� ���
        activitySubData.setCreateable(true);
        return creteable;
    }

    /**
     * TC Activity List ���� PE���� ������ Activity �˻��Ѵ�.
     * 
     * @method findActivity
     * @date 2013. 11. 27.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private HashMap<String, Object> findActivity(List<HashMap<String, Object>> activityList, ActivitySubData activitySubData) throws Exception {
        for (int i = 0; i < activityList.size(); i++) {
            // SEQ=60 --> TC-PE������ ����
            ArrayList<String> activitySubRowData = (ArrayList<String>) activitySubData.getData();
            String seq = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_SEQ_COLUMN_INDEX);
            if (seq.equals(activityList.get(i).get("SEQ"))) {
                return activityList.get(i);
            }
        }
        return null;
    }

    /**
     * Activity �۾������� �Ӽ� �񱳸� �Ѵ�.
     * 
     * @method validateActivitySubAttributes
     * @date 2013. 12. 10.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    private LinkedHashMap<String, String[]> validateActivitySubAttributes(HashMap<String, Object> tcActivityData, ArrayList<String> activitySubRowData) throws Exception {
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        String tcActivityKorName = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_OBJECT_NAME);
        String peActivityKorName = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KOR_NAME_COLUMN_INDEX);
        String[] defferActivityKorName = getDefferenceData(tcActivityKorName, peActivityKorName);
        if (defferActivityKorName != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_OBJECT_NAME, defferActivityKorName);
        }
        String tcCategory = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
        String peCategory = ImportCoreService.getPEActivityCategolyLOV(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_CATEGORY_COLUMN_INDEX)); // LOV value�� ��ȯ
        // PE LOV�� TC String���� �纯ȯ(����(PE) -> 01 -> �۾�������(TC))
        peCategory = ImportCoreService.getActivityCategolyLOVToString(peCategory);
        String[] defferCategory = getDefferenceData(tcCategory, peCategory);
        if (defferCategory != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY, defferCategory);
        }
        String tcWorkCode = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
        String peWorkCode = (String) activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_MAIN_COLUMN_INDEX);
        if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX))) {
            peWorkCode = peWorkCode + "-" + activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX).replace(",", "-");
        }
        String[] defferWorkCode = getDefferenceData(tcWorkCode, peWorkCode);
        if (defferWorkCode != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE, defferWorkCode);
        }
        double tcTimeSystemUnitTime = (Double) tcActivityData.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
        double peTimeSystemUnitTime = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX));
        String[] defferTimeSystemUnitTime = getDefferenceData(tcTimeSystemUnitTime, peTimeSystemUnitTime);
        if (defferTimeSystemUnitTime != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME, defferTimeSystemUnitTime);
        }
        double tcTimeSystemFrequency = (Double) tcActivityData.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
        double peTimeSystemFrequency = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX));
        String[] defferTimeSystemFrequency = getDefferenceData(tcTimeSystemFrequency, peTimeSystemFrequency);
        if (defferTimeSystemFrequency != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY, defferTimeSystemFrequency);
        }
        String tcControlPoint = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);
        String peControlPoint = (String) activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_COLUMN_INDEX);
        String[] defferControlPoint = getDefferenceData(tcControlPoint, peControlPoint);
        if (defferControlPoint != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_CONTROL_POINT, defferControlPoint);
        }
        String tcControlBasis = (String) tcActivityData.get(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS);
        String peControlBasis = (String) activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_BASIS_COLUMN_INDEX);
        String[] defferControlBasis = getDefferenceData(tcControlBasis, peControlBasis);
        if (defferControlBasis != null) {
            differenceResult.put(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS, defferControlBasis);
        }
        // checkActivityTool
        checkActivityTool(tcActivityData, activitySubRowData, differenceResult);
        return differenceResult;
    }

    /**
     * checkActivityTool
     * 
     * @method checkActivityTool
     * @date 2013. 12. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void checkActivityTool(HashMap<String, Object> tcActivityData, ArrayList<String> activitySubRowData, LinkedHashMap<String, String[]> differenceResult) throws TCException {
        String peToolIds = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TOOL_ID_COLUMN_INDEX);
        peToolIds = BundleUtil.nullToString(peToolIds);
        String[] peToolIdList = peToolIds.split(",");
        // split �� [0]�����Ͱ� empty("") �̸� String[0] �迭 ó��
        if (peToolIdList != null && peToolIdList.length == 1) {
            if ("".equals(BundleUtil.nullToString(peToolIdList[0]).trim())) {
                peToolIdList = new String[0];
            }
        }
        TCComponentBOMLine[] tcToolList = (TCComponentBOMLine[]) tcActivityData.get(SDVPropertyConstant.ACTIVITY_TOOL_LIST);
        if (tcToolList == null) {
            tcToolList = new TCComponentBOMLine[0];
        }
        String tcToolIds = "";
        for (int i = 0; i < tcToolList.length; i++) {
            String itemId = tcToolList[i].getProperty(SDVPropertyConstant.BL_ITEM_ID);
            tcToolIds += (i == 0) ? itemId : "," + itemId;
        }
        if (peToolIdList.length != tcToolList.length) {
            differenceResult.put("Activity Tool mismatch", new String[] { tcToolIds, peToolIds });
        } else {
            HashMap<String, TCComponentBOMLine> peMappingList = new HashMap<String, TCComponentBOMLine>();
            for (int i = 0; i < peToolIdList.length; i++) {
                String peToolItemId = peToolIdList[i];
                peMappingList.put(peToolItemId, null);
                for (int j = 0; j < tcToolList.length; j++) {
                    String tcToolItemId = tcToolList[j].getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    if (peToolItemId.equals(tcToolItemId)) {
                        peMappingList.put(peToolItemId, tcToolList[j]);
                        break;
                    }
                }
            }
            if (peMappingList.containsValue(null)) {
                differenceResult.put("Activity Tool mismatch", new String[] { tcToolIds, peToolIds });
            }
        }
    }

    /**
     * Validate Line
     * 
     * @method validateLine
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void validateLine(TCComponentMfgBvrProcess processLine, LineItemData lineItemData) throws TCException, Exception, ValidateSDVException {
        String tcLineItemId = processLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String dataLineItemId = lineItemData.getItemId();
        // TC���� ������ Line������ Import ����� Line������ �´��� �����Ѵ�.
        if (!tcLineItemId.equals(dataLineItemId)) {
            throw new ValidateSDVException("Line������ �����ʽ��ϴ�. (TC = '" + tcLineItemId + "' / IMP_DATE = '" + dataLineItemId + "')");
        }
        // ����, Released üũ
        checkPermissions(lineItemData);
    }

    /**
     * Validate Operation
     * 
     * @method validateOperation
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> validateOperation(OperationItemData operationItemData) throws Exception {
        // BOP Line���� �������� �������� ������ �ٸ� Line������ �����ϴ� ���
        if (operationItemData.getBopBomLine() == null && operationItemData.isExistItem() == true) {
            operationPaste(operationItemData);
        }
        if (!isOverride) {
            // �ʱ������ �ƴ� ��� �������üũ Release
            checkPermissions(operationItemData);
        }
        // TC�� ��� �� DataSet ���� - ������Ʈ, Revise�� �����ͼ�
        operationItemData.setData(PEExcelConstants.DATASET, generateOperationCreateDataset(operationItemData));
        // ���� �ɼ������ TC�� �°� ������
        String condition = ((ArrayList<String>) operationItemData.getData(PEExcelConstants.BOM)).get(PEExcelConstants.OPERATION_BOM_OPTION_COLUMN_INDEX);
        operationItemData.setConversionOptionCondition(getConversionOptionCondition(condition));
        // TC ���� Master(Item, ItemRevision) ������ PE I/F ���� Master ������ ���Ͽ� ������Ʈ ������� Ȯ���Ѵ�.
        LinkedHashMap<String, String[]> differenceResult = compareOperationData(operationItemData);
        return differenceResult;
    }

    /**
     * BOP Line���� �������� �������� ������ �ٸ� Line������ �����ϴ� ���
     * 
     * @method operationPaste
     * @date 2014. 1. 27.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void operationPaste(OperationItemData operationItemData) throws Exception {
        // throw new ValidateSDVException(operationItemData.getItemId() + " - ����(Operation)�� �ٸ� Line�� �����մϴ�. �ش� Line�� �۾� �� �ٽ� ������ �ּ���.");

        // Line�� OperationItem BOMLine Paste
        TCComponentBOMLine operationBOMLine = processLine.add(null, SYMTcUtil.getLatestedRevItem(operationItemData.getItemId()), null, false);
        operationItemData.setBopBomLine(operationBOMLine);
        // �α� ���
        saveLog(operationItemData, operationItemData.getItemId() + " - Line�� ������ ���� ����(Operation) Item�� �����մϴ�. �ش� Line�� �ٿ��ֱ�(Paste) �Ͽ����ϴ�.", true);
        // ���Ҵ� ���� �Ʒ��� ������ �����ϸ� cut
        TCComponentBOMLine shopBOMLine = processLine.parent();
        TCComponentBOMLine[] lineBOMLines = SDVBOPUtilities.getUnpackChildrenBOMLine(shopBOMLine);
        String tempBOPLineId = getTempBOPLineId(shopBOMLine);
        if (StringUtils.isEmpty(tempBOPLineId)) {
            throw new ValidateSDVException("SHOP���� ���Ҵ� Line�� ã�� �� �����ϴ�.");
        }
        // ���Ҵ� ���� �˻�
        for (TCComponentBOMLine lineBOMLine : lineBOMLines) {
            if (tempBOPLineId.equals(lineBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                TCComponentBOMLine[] tempOperationBOMLines = SDVBOPUtilities.getUnpackChildrenBOMLine(lineBOMLine);
                ArrayList<TCComponentBOMLine> removeList = new ArrayList<TCComponentBOMLine>();
                String cutTempOperationMsg = "";
                // Paste�� ���Ҵ� ���� �˻�
                for (TCComponentBOMLine tempOperationBOMLine : tempOperationBOMLines) {
                    // PE Operation Item ID�� ���Ҵ� ���ο� �����ϸ� cut.
                    if (operationItemData.getItemId().equals(tempOperationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                        removeList.add(tempOperationBOMLine);
                        cutTempOperationMsg = tempOperationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    }
                }
                // ���Ҵ� ���ο��� �ش� ������ Cut
                if (removeList.size() > 0) {
                    SDVBOPUtilities.disconnectObjects(lineBOMLine, removeList);
                    saveLog(operationItemData, cutTempOperationMsg + " - ���Ҵ� Line�� ������ �ִ� ������ Cut �Ͽ����ϴ�.", true);
                }
            }
        }
    }

    /**
     * SHOP BOMLine���� ���Ҵ� Line �˻�
     * 
     * @method getTempBOPLineId
     * @date 2014. 1. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getTempBOPLineId(TCComponentBOMLine topBOMLine) throws Exception {
        String tempLineId = "";
        String shopId = topBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String[] idSplit = shopId.split("-");
        // ���Ҵ� LINE ID
        tempLineId = idSplit[0] + "-" + idSplit[1] + "-TEMP-" + idSplit[2];
        return tempLineId;
    }

    /**
     * Create / Update Operation Dataset
     * 
     * @method generateOperationCreateDataset
     * @date 2013. 12. 10.
     * @param
     * @return IDataSet
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private IDataSet generateOperationCreateDataset(OperationItemData operationItemData) throws Exception {
        // (34) - (1D) - (862-015R) - (00)
        IDataSet dataSet = new DataSet();
        IDataMap opInformDataMap = new RawDataMap();
        dataSet.addDataMap("opInform", opInformDataMap);
        ArrayList<String> masterData = (ArrayList<String>) operationItemData.getData(PEExcelConstants.MASTER);
        ArrayList<String> bomData = (ArrayList<String>) operationItemData.getData(PEExcelConstants.BOM);
        if (masterData == null) {
            throw new ValidateSDVException("����(Operation) Master ������ �����ϴ�.");
        }
        // [, 34, 1D, 862-015R, 00, 10R, ���� �γ��ǳڿ� ���� ��Ʈ��Ʈ ��Ʈ���� ����2,��, , T1-240, , , null, null, null, null, null]
        // ��������
        // MASTER
        String vechicleCode = masterData.get(PEExcelConstants.COMMON_MASTER_PROJECT_NO_COLUMN_INDEX);
        String lineCode = masterData.get(PEExcelConstants.COMMON_MASTER_SHOP_LINE_COLUMN_INDEX);
        String sheetNo = masterData.get(PEExcelConstants.COMMON_MASTER_SHEET_NO_COLUMN_INDEX);
        String functionCode = sheetNo.split("-")[0];
        String opCode = sheetNo.split("-")[1];
        String bopVersion = masterData.get(PEExcelConstants.COMMON_MASTER_PLANNING_VERSION_COLUMN_INDEX);
        // BOM
        String productNo = bomData.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX);
        // Item Id ����
        // String itemId = vechicleCode + "-" + lineCode + "-" + functionCode + "-" + opCode + "-" + bopVersion;
        String korName = masterData.get(PEExcelConstants.OPERATION_MASTER_KOR_NAME_COLUMN_INDEX);
        String opEngName = masterData.get(PEExcelConstants.OPERATION_MASTER_ENG_NAME_COLUMN_INDEX);
        String workerCode = masterData.get(PEExcelConstants.OPERATION_MASTER_WORKER_CODE_COLUMN_INDEX);// �۾��ڱ����ڵ�
        String processSeq = masterData.get(PEExcelConstants.OPERATION_MASTER_PROCESS_SEQ_COLUMN_INDEX);// Sequence No.
        String workArea = masterData.get(PEExcelConstants.OPERATION_MASTER_WORK_AREA_COLUMN_INDEX);
        String itemUL = masterData.get(PEExcelConstants.OPERATION_MASTER_ITEM_UL_COLUMN_INDEX); // ����������ġ
        String stationNo = masterData.get(PEExcelConstants.OPERATION_MASTER_STATION_NO_COLUMN_INDEX);// Station No.
        String dr = masterData.get(PEExcelConstants.OPERATION_MASTER_DR_COLUMN_INDEX); // ����
        String assySystem = masterData.get(PEExcelConstants.OPERATION_MASTER_ASSEMBLY_SYSTEM_COLUMN_INDEX);// �����ý���
        String workUbody = masterData.get(PEExcelConstants.OPERATION_MASTER_WORK_UBODY_COLUMN_INDEX); // ���ϱ���
        String vehicleCheckStr = masterData.get(PEExcelConstants.OPERATION_MASTER_REP_VHICLE_CHECK_COLUMN_INDEX); // ��ǥ���� ���� (N/Y)
        /*
         *  Ư��Ư�� �Ӽ� �߰� 
         */
        String specialCharic = masterData.get(PEExcelConstants.OPERATION_MASTER_SHEET_SPECIAL_CHARICTORISTIC);
        
        boolean vehicleCheck = ("Y".equals(vehicleCheckStr)) ? true : false;
        boolean maxWorkTimeCheck = false;
        String dwgNo = masterData.get(PEExcelConstants.OPERATION_MASTER_DWG_NO_COLUMN_INDEX);
        String[] dwgNoArray = dwgNo.split("/");
        ArrayList<String> dwgNoList = new ArrayList<String>();
        if (dwgNoArray != null && dwgNoArray.length > 0) {
            for (String dwg : dwgNoArray) {
                dwg = BundleUtil.nullToString(dwg).trim();
                if (!"".equals(dwg)) {
                    dwgNoList.add(dwgNo);
                }
            }
        }
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vechicleCode);// ����
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_SHOP, lineCode);// ����
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, functionCode); //
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opCode);// ���� �ڵ�
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bopVersion);// ���� ������
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, korName);// ���� �̸� (�ѱ�)
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, opEngName); // ���� �̸� (����)
        opInformDataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE, workerCode);// �۾��� �����ڵ�
        opInformDataMap.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ, processSeq); // Sequence No.
        opInformDataMap.put(SDVPropertyConstant.OPERATION_WORKAREA, workArea); // �۾���ġ
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, stationNo); // Station No.
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_DR, dr); // ����
        opInformDataMap.put(SDVPropertyConstant.OPERATION_WORK_UBODY, workUbody); // WORK_UBODY ��ü�۾�����(N/Y)
        opInformDataMap.put(SDVPropertyConstant.OPERATION_ITEM_UL, itemUL); // ����������ġ
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, dwgNoList, IData.LIST_FIELD); // ���������ȣ ����Ʈ
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, assySystem); // �����ý���
        opInformDataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, maxWorkTimeCheck, IData.BOOLEAN_FIELD);
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, vehicleCheck, IData.BOOLEAN_FIELD); // ��ǥ���� ����(N/Y)
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productNo); // Product No.
        /*
         * Ư�� Ư�� �Ӽ� �߰�
         */
        opInformDataMap.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialCharic);// Ư�� Ư�� 
        
        // MECO ����
        IDataMap mecoSelectDataMap = new RawDataMap();
        dataSet.addDataMap("mecoSelect", mecoSelectDataMap);
        mecoSelectDataMap.put("mecoNo", mecoNo);
        mecoSelectDataMap.put("mecoRev", ImportCoreService.getMecoRevision(mecoNo), IData.OBJECT_FIELD);

        return dataSet;
    }

    /**
     * TC ���� Master(Item, ItemRevision) ������ PE I/F ���� Master, BOMLine ������ ���Ͽ� ������Ʈ ������� Ȯ���Ѵ�.
     * 
     * @method compareOperationData
     * @date 2013. 12. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> compareOperationData(OperationItemData operationItemData) throws Exception {
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        // �ʱ������ ������ Master���� Create
        if (operationItemData.getBopBomLine() == null) {
            operationItemData.setMasterModifiable(true);
            return differenceResult;
        }
        // Update ��� �� - PEExecution.updateOperationItem ������Ʈ dataset ���� ��
        // Excel ���� ��� ��� Dataset ��������
        IDataSet dataSet = (IDataSet) operationItemData.getData(PEExcelConstants.DATASET);
        IDataMap opInformDataMap = dataSet.getDataMap("opInform");
        // �񱳴�� TC Operation Item
        TCComponentItem item = operationItemData.getBopBomLine().getItem();

        String tcOperationWorkerCode = BundleUtil.nullToString(item.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE));
        String peOperationWorkerCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKER_CODE).getStringValue());
        String[] defferOperationWorkerCode = getDefferenceData(tcOperationWorkerCode, peOperationWorkerCode);
        if (defferOperationWorkerCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_WORKER_CODE, defferOperationWorkerCode);
        }

        String tcOperationProcessSeq = BundleUtil.nullToString(item.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ));
        String peOperationProcessSeq = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_PROCESS_SEQ).getStringValue());
        String[] defferOperationProcessSeq = getDefferenceData(tcOperationProcessSeq, peOperationProcessSeq);
        if (defferOperationProcessSeq != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ, defferOperationProcessSeq);
        }

        String tcOperationWorkArea = BundleUtil.nullToString(item.getProperty(SDVPropertyConstant.OPERATION_WORKAREA));
        String peOperationWorkArea = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKAREA).getStringValue());
        String[] defferOperationWorkArea = getDefferenceData(tcOperationWorkArea, peOperationWorkArea);
        if (defferOperationWorkArea != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_WORKAREA, defferOperationWorkArea);
        }

        // �񱳴�� TC Operation ItemRevision
        TCComponentItemRevision itemRevision = operationItemData.getBopBomLine().getItemRevision();

        String tcOperationKorName = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
        String peOperationKorName = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME).getStringValue());
        String[] defferOperationKorName = getDefferenceData(tcOperationKorName, peOperationKorName);
        if (defferOperationKorName != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, defferOperationKorName);
        }

        String tcOperationEngName = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));
        String peOperationEngName = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_ENG_NAME).getStringValue());
        // PE�� �������� null�̸� ������Ʈ ó������ �ʴ´�.
        if (!StringUtils.isEmpty(peOperationEngName)) {
            String[] defferOperationEngName = getDefferenceData(tcOperationEngName, peOperationEngName);
            if (defferOperationEngName != null) {
                differenceResult.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, defferOperationEngName);
            }
        }

        String tcOperationrevVehicleCode = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE));
        String peOperationrevVehicleCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE).getStringValue());
        String[] defferOperationrevVehicleCode = getDefferenceData(tcOperationrevVehicleCode, peOperationrevVehicleCode);
        if (defferOperationrevVehicleCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, defferOperationrevVehicleCode);
        }

        String tcOperationRevShop = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SHOP));
        String peOperationRevShop = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_SHOP).getStringValue());
        String[] defferOperationRevShop = getDefferenceData(tcOperationRevShop, peOperationRevShop);
        if (defferOperationRevShop != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_SHOP, defferOperationRevShop);
        }

        String tcOperationRevFunctionCode = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE));
        String peOperationRevFunctionCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE).getStringValue());
        String[] defferOperationRevFunctionCode = getDefferenceData(tcOperationRevFunctionCode, peOperationRevFunctionCode);
        if (defferOperationRevFunctionCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, defferOperationRevFunctionCode);
        }

        String tcOperationRevOperationCode = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE));
        String peOperationRevOperationCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE).getStringValue());
        String[] defferOperationRevOperationCode = getDefferenceData(tcOperationRevOperationCode, peOperationRevOperationCode);
        if (defferOperationRevOperationCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, defferOperationRevOperationCode);
        }

        itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_BOP_VERSION).getStringValue();
        String tcOperationRevBopVersion = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION));
        String peOperationRevBopVersion = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_BOP_VERSION).getStringValue());
        String[] defferOperationRevBopVersion = getDefferenceData(tcOperationRevBopVersion, peOperationRevBopVersion);
        if (defferOperationRevBopVersion != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, defferOperationRevBopVersion);
        }

        itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
        opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO).getStringValue();
        String tcOperationRevStationNo = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
        String peOperationRevStationNo = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO).getStringValue());
        String[] defferOperationRevStationNo = getDefferenceData(tcOperationRevStationNo, peOperationRevStationNo);
        if (defferOperationRevStationNo != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_STATION_NO, defferOperationRevStationNo);
        }

        String tcOperationRevProductCode = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE));
        String peOperationRevProductCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE).getStringValue());
        String[] defferOperationRevProductCode = getDefferenceData(tcOperationRevProductCode, peOperationRevProductCode);
        if (defferOperationRevProductCode != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, defferOperationRevProductCode);
        }
        /*
         * Ư�� Ư�� �Ӽ� �߰�
         */
        String tcOperationRevSpecialCharic = BundleUtil.nullToString(itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC));
        String peOperationRevSpecialCharic = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC).getStringValue());
        String[] defferOperationRevSpecialCharic = getDefferenceData(tcOperationRevSpecialCharic, peOperationRevSpecialCharic);
        if (defferOperationRevSpecialCharic != null) {
        	differenceResult.put(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, defferOperationRevSpecialCharic);
        }
        
        // OPERATION_REV_INSTALL_DRW_NO �� TC���� �迭 Ÿ������ ����, PE������ �ܰ����� �����ϹǷ� 1������ TC���� �����Ͽ� PE�� ���Ѵ�.
        String tcOperationRevInstallDwgNo[] = null;
        if (itemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO) != null) {
            tcOperationRevInstallDwgNo = itemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getStringValueArray();
        }
        if (tcOperationRevInstallDwgNo == null) {
            tcOperationRevInstallDwgNo = new String[0];
        }
        String[] peOperationRevInstallDwgNo = null;
        if (opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getValue() != null) {
            ArrayList<String> dwgList = (ArrayList<String>) opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getValue();
            if (dwgList != null && dwgList.size() > 0) {
                peOperationRevInstallDwgNo = dwgList.toArray(new String[dwgList.size()]);
            }
        }
        if (peOperationRevInstallDwgNo == null) {
            peOperationRevInstallDwgNo = new String[0];
        }
        String[] defferOperationRevInstallDwgNo = getDefferenceData(tcOperationRevInstallDwgNo, peOperationRevInstallDwgNo);
        if (defferOperationRevInstallDwgNo != null) {
            differenceResult.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, defferOperationRevInstallDwgNo);
        }

        // MASTER ���� ������ �����Ƿ� ������Ʈ
        if (differenceResult.size() > 0) {
            operationItemData.setMasterModifiable(true);
        }
        // �񱳴�� TC Operation BOMLine
        // Option Condition
        String tcOpertationBOMLineMVLCondition = BundleUtil.nullToString(operationItemData.getBopBomLine().getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).trim();
        String peOpertationBOMLineMVLCondition = BundleUtil.nullToString(operationItemData.getConversionOptionCondition()).trim();
        String[] defferBOMLineMVLCondition = getDefferenceData(tcOpertationBOMLineMVLCondition, peOpertationBOMLineMVLCondition);
        if (defferBOMLineMVLCondition != null) {
            differenceResult.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, defferBOMLineMVLCondition);
            operationItemData.setBOMLineModifiable(true);
        }
        // Find No. ��
        // ��������ȣ �Է�
        String tcOpertationSeqNo = BundleUtil.nullToString(operationItemData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO)).trim();
        String peOpertationSeqNo = "";
        String stationNo = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO).getStringValue()).replace("-", "");// ������ȣ
        String workerCode = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKER_CODE).getStringValue()).replace("-", "");// �۾����ڵ�
        String seq = BundleUtil.nullToString(opInformDataMap.get(SDVPropertyConstant.OPERATION_PROCESS_SEQ).getStringValue()); // Process Seq
        boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty() || seq.isEmpty(); // �ϳ��� ���� ������ �ݿ�����
        String findNo = stationNo.concat("|").concat(workerCode).concat("|").concat(seq);
        if (!(findNo.length() > 15 || isExistEmptyValue)) {
            peOpertationSeqNo = findNo;
            opInformDataMap.put(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
        }
        String[] defferBOMLineOpertationSeqNo = getDefferenceData(tcOpertationSeqNo, peOpertationSeqNo);
        if (defferBOMLineOpertationSeqNo != null) {
            differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferBOMLineOpertationSeqNo);
            operationItemData.setBOMLineModifiable(true);
        }
        return differenceResult;
    }

    /**
     * ������ ��
     * 
     * ���� : double�� ��� �Ҽ��� 10�ڸ� ���� ����
     * 
     * @method getDefferenceData
     * @date 2013. 12. 11.
     * @param
     * @return String[]
     * @exception
     * @throws
     * @see
     */
    private String[] getDefferenceData(Object tcData, Object peData) {
        // double�� ��� �Ҽ��� 10�ڸ� ���� ����
        if (tcData instanceof Double) {
            tcData = (new Double(Double.parseDouble(longDouble2String(10, (Double) tcData)))).toString();
            peData = (new Double(Double.parseDouble(longDouble2String(10, (Double) peData)))).toString();
            if (!(tcData.equals(peData))) {
                String[] defferData = new String[2];
                defferData[0] = tcData.toString();
                defferData[1] = peData.toString();
                return defferData;
            }
        } else if (tcData instanceof String[]) {
            String[] tcStrings = (String[]) tcData;
            String[] peStrings = (String[]) peData;
            if (tcStrings.length != peStrings.length) {
                String[] defferData = new String[2];
                defferData[0] = Arrays.toString(tcStrings);
                defferData[1] = Arrays.toString(peStrings);
                return defferData;
            } else {
                for (int i = 0; i < tcStrings.length; i++) {
                    String tcValue = BundleUtil.nullToString(tcStrings[i]);
                    boolean isSameData = false;
                    for (int j = 0; j < peStrings.length; j++) {
                        String peValue = BundleUtil.nullToString(peStrings[j]);
                        if (tcValue.equals(peValue)) {
                            isSameData = true;
                            break;
                        }
                    }
                    if (isSameData == false) {
                        String[] defferData = new String[2];
                        defferData[0] = Arrays.toString(tcStrings);
                        defferData[1] = Arrays.toString(peStrings);
                        return defferData;
                    }
                }
                return null;
            }
        } else {
            // null �ʱ�ȭ
            if (tcData == null) {
                tcData = "";
            }
            if (peData == null) {
                peData = "";
            }
            if (!(tcData.equals(peData))) {
                String[] defferData = new String[2];
                defferData[0] = tcData.toString();
                defferData[1] = peData.toString();
                return defferData;
            }
        }
        return null;
    }

    /**
     * ������ ���̷� �ڸ���.
     * 
     * @param size
     * @param value
     * @return
     */
    private String longDouble2String(int size, double value) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(size);
        nf.setGroupingUsed(false);
        return nf.format(value);
    }

    /**
     * ����, Released üũ
     * 
     * @method checkPermissions
     * @date 2013. 11. 29.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void checkPermissions(ItemData itemData) throws Exception {
        try {
            if (itemData.isExistItem()) {
                boolean isReleased = SYMTcUtil.isLatestedRevItemReleased(itemData.getBopBomLine().getItemRevision());
                if (isReleased) {
                    throw new ValidateSDVException("'" + itemData.getClassType() + "' is Released", null);
                }
                boolean isWritable = SYMTcUtil.isBOMWritable(itemData.getBopBomLine());
                if (!isWritable) {
                    throw new ValidateSDVException("'" + itemData.getClassType() + "' BOMLine ���� ������ �����ϴ�.", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidateSDVException(e.getMessage(), e);
        }

    }

    /**
     * Validate EquipmentData
     * 
     * @method validateEquipmentData
     * @date 2013. 12. 12.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private LinkedHashMap<String, String[]> validateEquipmentData(EquipmentData equipmentData) throws Exception {
        // �ڿ�(Resource) ������ ��ȸ
        equipmentData.setResourceItem(SYMTcUtil.getItem(equipmentData.getItemId()));
        setBOMLineMappingOccurrenceData(equipmentData);
        return compareEquipmentData(equipmentData);
    }

    /**
     * Equipment BOMLine ������ ���Ͽ� BOMLine ������Ʈ ������� Ȯ���Ѵ�.
     * 
     * @method compareEquipmentData
     * @date 2013. 12. 23.
     * @param
     * @return LinkedHashMap<String,String[]>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> compareEquipmentData(EquipmentData equipmentData) throws Exception {
        ArrayList<String> equipmentBOMData = (ArrayList<String>) equipmentData.getData(PEExcelConstants.BOM);
        equipmentBOMData.set(PEExcelConstants.EQUIPMENT_BOM_SEQ_COLUMN_INDEX, ImportCoreService.getFindNoFromSeq(equipmentBOMData.get(PEExcelConstants.EQUIPMENT_BOM_SEQ_COLUMN_INDEX))); // �������� Find No.�� �ٽ� Dataset�� ���
        equipmentData.setFindNo(equipmentBOMData.get(PEExcelConstants.EQUIPMENT_BOM_SEQ_COLUMN_INDEX)); // FindNo ���
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        if (equipmentData.getResourceItem() == null) {
            return differenceResult;
        }
        if (equipmentData.getBopBomLine() == null) {
            return differenceResult;
        }
        // Quantity üũ
        Double tcEquipmentQuantity = Double.parseDouble(BundleUtil.nullToString(equipmentData.getBopBomLine().getProperty(SDVPropertyConstant.BL_QUANTITY)));
        Double peEquipmentQuantity = Double.parseDouble(BundleUtil.nullToString(equipmentBOMData.get(PEExcelConstants.EQUIPMENT_BOM_QUANTITY_COLUMN_INDEX)));
        String[] defferEquipmentQuantity = getDefferenceData(tcEquipmentQuantity, peEquipmentQuantity);
        if (defferEquipmentQuantity != null) {
            differenceResult.put(SDVPropertyConstant.BL_QUANTITY, defferEquipmentQuantity);
        }
        // Seq No. üũ
        String tcEquipmentSeqNo = BundleUtil.nullToString(equipmentData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
        String peEquipmentSeqNo = equipmentBOMData.get(PEExcelConstants.EQUIPMENT_BOM_SEQ_COLUMN_INDEX); // Find No.
        String[] defferEquipmentSeqNo = getDefferenceData(tcEquipmentSeqNo, peEquipmentSeqNo);
        if (defferEquipmentSeqNo != null) {
            differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferEquipmentSeqNo);
        }
        // BOMLine ���� ������ �����Ƿ� ������Ʈ
        if (differenceResult.size() > 0) {
            equipmentData.setBOMLineModifiable(true);
        }
        return differenceResult;
    }

    /**
     * Validate ToolData
     * 
     * @method validateToolData
     * @date 2013. 12. 12.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private LinkedHashMap<String, String[]> validateToolData(ToolData toolData) throws Exception {
        // �ڿ�(Resource) ������ ��ȸ
        toolData.setResourceItem(SYMTcUtil.getItem(toolData.getItemId()));
        setBOMLineMappingOccurrenceData(toolData);
        return compareToolData(toolData);
    }

    /**
     * TOOL BOMLine ������ ���Ͽ� BOMLine ������Ʈ ������� Ȯ���Ѵ�.
     * 
     * @method compareToolData
     * @date 2013. 12. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> compareToolData(ToolData toolData) throws Exception {
        ArrayList<String> toolBOMData = (ArrayList<String>) toolData.getData(PEExcelConstants.BOM);
        toolBOMData.set(PEExcelConstants.TOOL_BOM_SEQ_COLUMN_INDEX, ImportCoreService.getFindNoFromSeq(toolBOMData.get(PEExcelConstants.TOOL_BOM_SEQ_COLUMN_INDEX))); // �������� Find No.�� �ٽ� Dataset�� ���
        toolData.setFindNo(toolBOMData.get(PEExcelConstants.TOOL_BOM_SEQ_COLUMN_INDEX)); // FindNo ���
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        if (toolData.getResourceItem() == null) {
            return differenceResult;
        }
        if (toolData.getBopBomLine() == null) {
            return differenceResult;
        }
        // Quantity üũ
        Double tcToolQuantity = Double.parseDouble(BundleUtil.nullToString(toolData.getBopBomLine().getProperty(SDVPropertyConstant.BL_QUANTITY)));
        Double peToolQuantity = Double.parseDouble(BundleUtil.nullToString(toolBOMData.get(PEExcelConstants.TOOL_BOM_QUANTITY_INDEX)));
        String[] defferToolQuantity = getDefferenceData(tcToolQuantity, peToolQuantity);
        if (defferToolQuantity != null) {
            differenceResult.put(SDVPropertyConstant.BL_QUANTITY, defferToolQuantity);
        }
        // Torque üũ
        String tcToolTorQueType = BundleUtil.nullToString(toolData.getBopBomLine().getProperty(SDVPropertyConstant.BL_NOTE_TORQUE));
        String tcToolTorQueValue = BundleUtil.nullToString(toolData.getBopBomLine().getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE));
        String tcToolTorQue = "";
        if (!StringUtils.isEmpty(tcToolTorQueValue)) {
            tcToolTorQue = tcToolTorQueType + " " + tcToolTorQueValue;
        }
        String peToolTorQue = BundleUtil.nullToString(toolBOMData.get(PEExcelConstants.TOOL_BOM_TORQUE_COLUMN_INDEX));
        String[] defferToolTorQue = getDefferenceData(tcToolTorQue, peToolTorQue);
        if (defferToolTorQue != null) {
            differenceResult.put(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, defferToolTorQue);
        }
        // Seq No. üũ
        String tcToolSeqNo = BundleUtil.nullToString(toolData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
        String peToolSeqNo = toolBOMData.get(PEExcelConstants.TOOL_BOM_SEQ_COLUMN_INDEX); // Find No.
        String[] defferToolSeqNo = getDefferenceData(tcToolSeqNo, peToolSeqNo);
        if (defferToolSeqNo != null) {
            differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferToolSeqNo);
        }
        // BOMLine ���� ������ �����Ƿ� ������Ʈ
        if (differenceResult.size() > 0) {
            toolData.setBOMLineModifiable(true);
        }
        return differenceResult;
    }

    /**
     * Validate OccurrenceData
     * 
     * @method validateOccurrenceData
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private LinkedHashMap<String, String[]> validateEndItemData(EndItemData endItemData) throws Exception {
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        // �ڿ�(Resource) ������ ��ȸ
        try {
            endItemData.setResourceItem(SYMTcUtil.getItem(endItemData.getItemId()));
        } catch (Exception e) {
            SkipException skipException = new SkipException("E-BOM END-ITEM�� �������� �ʽ��ϴ�.", e);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        // END-ITEM ó��
        try {
            OperationItemData operationItemData = (OperationItemData) endItemData.getParentItem();
            // PE �������� M-BOM������ �����Ѵ�.
            TCComponentBOMLine findPEmBOMEndItemBOMLine = findEndItemBOMLine(endItemData.getFunctionItemId(), endItemData.getAbsOccPuids());
            if (findPEmBOMEndItemBOMLine == null) {
                SkipException skipException = new SkipException("E-BOM END-ITEM�� M-BOM���� ã�� �� �����ϴ�.");
                skipException.setStatus(TCData.STATUS_ERROR);
                throw skipException;
            }
            // M-BOM END-ITEM BOMLine������ ����Ѵ�.
            endItemData.setEndItemMBOMLine(findPEmBOMEndItemBOMLine);
            // M-BOM END-ITEM BOMLine������ ������ BOP END-ITEM BOMLine������ ã�´�.
            TCComponentBOMLine[] findBOPEndItemBOMLineList = SDVBOPUtilities.getAssignSrcBomLineList(processLine.window(), findPEmBOMEndItemBOMLine);
            for (int i = 0; i < findBOPEndItemBOMLineList.length; i++) {
                TCComponentBOMLine parentBOMLine = findBOPEndItemBOMLineList[i].parent();
                if (operationItemData.getBopBomLine() == parentBOMLine) {
                    // BOP END-ITEM BOMLine������ ����Ѵ�.
                    endItemData.setBopBomLine(findBOPEndItemBOMLineList[i]);
                    // Find No. üũ
                    // Find No. ����
                    endItemData.setFindNo(ImportCoreService.getFindNoFromSeq(endItemData.getFindNo()));
                    String tcEndItemFindNo = BundleUtil.nullToString(endItemData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
                    String peEndItemFindNo = BundleUtil.nullToString(endItemData.getFindNo());
                    String[] defferEndItemFindNo = getDefferenceData(tcEndItemFindNo, peEndItemFindNo);
                    if (defferEndItemFindNo != null) {
                        differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferEndItemFindNo);
                        endItemData.setBOMLineModifiable(true);
                    }
                }
            }
        } catch (Exception e) {
            // END-ITEM Exception�� �ߴ������ʰ� Skipó���Ѵ�.
            SkipException skipException = new SkipException(e.getMessage(), e);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        return differenceResult;
    }

    /**
     * Validate SubsidiaryData
     * 
     * @method validateSubsidiaryData
     * @date 2013. 12. 12.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> validateSubsidiaryData(SubsidiaryData subsidiaryData) throws Exception {
        LinkedHashMap<String, String[]> differenceResult = new LinkedHashMap<String, String[]>();
        // �ڿ�(Resource) ������ ��ȸ
        subsidiaryData.setResourceItem(SYMTcUtil.getItem(subsidiaryData.getItemId()));
        if (subsidiaryData.getResourceItem() == null) {
            SkipException skipException = new SkipException("������ Item�� ���������ʽ��ϴ�. - " + subsidiaryData.getItemId());
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        setBOMLineMappingOccurrenceData(subsidiaryData);
        ArrayList<String> subsidiaryBomData = (ArrayList<String>) subsidiaryData.getData(PEExcelConstants.BOM);
        // ������ �ɼ������ ��
        // ������ �ɼ� ������� PE->TC�� �°� ������
        subsidiaryData.setConversionOptionCondition(getConversionOptionCondition(subsidiaryBomData.get(PEExcelConstants.SUBSIDIARY_BOM_OPTION_COLUMN_INDEX)));
        String tcSubsidiaryMVLCondition = "";
        if (subsidiaryData.getBopBomLine() != null) {
            tcSubsidiaryMVLCondition = BundleUtil.nullToString(subsidiaryData.getBopBomLine().getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).trim();
        }
        String peSubsidiaryMVLCondition = BundleUtil.nullToString(subsidiaryData.getConversionOptionCondition()).trim();
        if (subsidiaryData.getBopBomLine() != null) {
            // ������ �ɼ� ������� ���� ���
            if (StringUtils.isEmpty(peSubsidiaryMVLCondition)) {
                // ������ �ɼ� ������� Parent(����)���� �����´�.
                peSubsidiaryMVLCondition = BundleUtil.nullToString(subsidiaryData.getBopBomLine().parent().getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION)).trim();
                subsidiaryData.setConversionOptionCondition(peSubsidiaryMVLCondition);
            }
        }
        String[] defferSubsidiaryMVLCondition = getDefferenceData(tcSubsidiaryMVLCondition, peSubsidiaryMVLCondition);
        if (defferSubsidiaryMVLCondition != null) {
            differenceResult.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, defferSubsidiaryMVLCondition);
        }
        // Find No. üũ
        // Find No. ����
        subsidiaryData.setFindNo(ImportCoreService.conversionSubsidiaryFindNo(subsidiaryData.getFindNo())); // ������ Seq No. -> Find No.�� ��ȯ
        String tcSubsidiaryFindNo = "";
        if (subsidiaryData.getBopBomLine() != null) {
            tcSubsidiaryFindNo = BundleUtil.nullToString(subsidiaryData.getBopBomLine().getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
        }
        String peSubsidiaryFindNo = BundleUtil.nullToString(subsidiaryData.getFindNo());
        String[] defferSubsidiaryFindNo = getDefferenceData(tcSubsidiaryFindNo, peSubsidiaryFindNo);
        if (defferSubsidiaryFindNo != null) {
            differenceResult.put(SDVPropertyConstant.BL_SEQUENCE_NO, defferSubsidiaryFindNo);
        }
        // �ҿ䷮ üũ
        String tcSubsidiaryDemeanQuantity = "";
        if (subsidiaryData.getBopBomLine() != null) {
            tcSubsidiaryDemeanQuantity = BundleUtil.nullToString(subsidiaryData.getBopBomLine().getProperty(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY));
        }
        String peSubsidiaryDemeanQuantity = BundleUtil.nullToString(subsidiaryBomData.get(PEExcelConstants.SUBSIDIARY_BOM_DEMAND_QUANTITY_COLUMN_INDEX));
        String[] defferSubsidiaryDemeanQuantity = getDefferenceData(tcSubsidiaryDemeanQuantity, peSubsidiaryDemeanQuantity);
        if (defferSubsidiaryDemeanQuantity != null) {
            differenceResult.put(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY, defferSubsidiaryDemeanQuantity);
        }
        // ������(Day or Night - LOV ==> �ְ�:A �߰�:B)
        String tcSubsidiaryDayOrNight = "";
        if (subsidiaryData.getBopBomLine() != null) {
            tcSubsidiaryDayOrNight = BundleUtil.nullToString(subsidiaryData.getBopBomLine().getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT));
        }
        String peSubsidiaryDayOrNight = BundleUtil.nullToString(subsidiaryBomData.get(PEExcelConstants.SUBSIDIARY_BOM_DEMAND_DIVIDE_GROUP_COLUMN_INDEX));
        String[] defferSubsidiaryDayOrNight = getDefferenceData(tcSubsidiaryDayOrNight, peSubsidiaryDayOrNight);
        if (defferSubsidiaryDayOrNight != null) {
            differenceResult.put(SDVPropertyConstant.BL_NOTE_DAYORNIGHT, defferSubsidiaryDayOrNight);
        }
        // ���� ������ �����Ƿ� ������Ʈ
        if (differenceResult.size() > 0) {
            subsidiaryData.setBOMLineModifiable(true);
        }
        return differenceResult;
    }

    /**
     * validateSheetDatasetData
     * 
     * @method validateDatasetData
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, String[]> validateSheetDatasetData(SheetDatasetData tcData) throws Exception {
        File uploadSheetExcelFile = ImportCoreService.getPathFile((String) tcData.getData());
        if (uploadSheetExcelFile == null) {
            SkipException skipException = new SkipException("���ε� ��� �۾�ǥ�ؼ� ������ �������� �ʽ��ϴ�.");
            skipException.setStatus(TCData.STATUS_SKIP);
            throw skipException;
        }
        OperationItemData operationItemData = (OperationItemData) tcData.getParentItem();
        if (operationItemData.isExistItem()) {
            ArrayList<String> operationMasterRowData = (ArrayList<String>) operationItemData.getData(PEExcelConstants.MASTER);
            // I/F ���� ����
            String isIf = BundleUtil.nullToString(operationMasterRowData.get(PEExcelConstants.OPERATION_MASTER_SHEET_KO_YN_COLUMN_INDEX)).toUpperCase();
            if (isIf!=null && isIf.trim().toUpperCase().indexOf("TRUE")>=0) {
                tcData.setIf(true);
            } else {
                tcData.setIf(false);
                SkipException skipException = new SkipException("�۾�ǥ�ؼ� I/F ����� �ƴմϴ�.");
                skipException.setStatus(TCData.STATUS_SKIP);
                throw skipException;
            }
        }
        // ���� ������ ���� ������ �۾�ǥ�ؼ� ���
        else {
            tcData.setIf(true);
        }
        return null;
    }

    /**
     * Option Condition Conversion
     * 
     * @method setConversionOptionCondition
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private String getConversionOptionCondition(String condition) throws TCException, Exception {
        TCComponentBOMWindow window = processLine.window();
        return ImportCoreService.conversionOptionCondition(window, condition);
    }

    /**
     * OccurrenceData�� BOPBOMLine Mapping
     * 
     * @method setBOMLineMappingOccurrenceData
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setBOMLineMappingOccurrenceData(OccurrenceData occurrenceData) throws Exception {
        ArrayList<TCComponentBOMLine> findBOPBOMLineList = findOperationUnderDataItem(occurrenceData);
        if (findBOPBOMLineList.size() > 0) {
            occurrenceData.setBopBomLine(findBOPBOMLineList.get(0));
        }
    }

    /**
     * M-BOM PRODUCT �������� PE I/F EndItem�� TCComponentMEAppearancePathNode PUID�� �˻��Ѵ�.
     * 
     * @method findEndItemBOMLine
     * @date 2013. 12. 3.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOMLine findEndItemBOMLine(String peFunctionItemId, String absOccPuids) throws Exception {
        if (((PEIFTCDataExecuteJob) tcDataMigrationJob).getPeIFJobWork().functionContexts == null) {
            throw new ValidateSDVException("M-BOM BOMWindow�� �ʱ�ȭ�����ʾҽ��ϴ�.");
        }
        if (StringUtils.isEmpty(peFunctionItemId)) {
            SkipException skipException = new SkipException("E-BOM�� Function Item ������ 'NULL' �Դϴ�.");
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }

        // PRODUCT ���� FUNCTION BOMLINE�� �˻�
        TCComponentBOMLine findedFunctionBOMLine = null;
        for (AIFComponentContext functionContext : ((PEIFTCDataExecuteJob) tcDataMigrationJob).getPeIFJobWork().functionContexts) {
            TCComponentBOMLine functionBOMLine = (TCComponentBOMLine) functionContext.getComponent();
            String functionItemId = (functionBOMLine.getItem() != null) ? functionBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) : null;
            if (peFunctionItemId.equals(functionItemId)) {
                findedFunctionBOMLine = functionBOMLine;
                break;
            }
        }
        if (findedFunctionBOMLine == null) {
            SkipException skipException = new SkipException("E-BOM�� ��ġ�Ǵ� Function ������ �����ϴ�. - I/F PE FN ID : " + peFunctionItemId);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        String[] absOccPuidList = splitAbsOccPuids(absOccPuids);
        if (absOccPuidList.length == 0) {
            SkipException skipException = new SkipException("E-BOM ABS Occthread puid ������ 'NULL' �Դϴ�. - I/F PE FN ID : " + peFunctionItemId);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        TCComponentBOMLine endItemBOMLine = findEndItemBOMLine(absOccPuidList, findedFunctionBOMLine, 0);
        if (endItemBOMLine == null) {
            SkipException skipException = new SkipException("E-BOM�� ��ġ�Ǵ� END-ITEM ������ �����ϴ�. - I/F PE FN ID : " + peFunctionItemId);
            skipException.setStatus(TCData.STATUS_ERROR);
            throw skipException;
        }
        // Pack�� ��� Unpack�Ѵ�.
        /*
         * if (endItemBOMLine.isPacked()) {
         * TCComponentBOMLine unpackEndItemBOMLine = null;
         * TCComponentBOMLine[] unpackLines = SDVBOPUtilities.getUnpackBOMLines(endItemBOMLine);
         * if (unpackLines != null) {
         * for (TCComponentBOMLine unpackLine : unpackLines) {
         * String occpuid = unpackLine.getProperty("bl_occurrence_uid");
         * if (absOccPuidList[absOccPuidList.length - 1].equals(occpuid)) {
         * unpackEndItemBOMLine = unpackLine;
         * }
         * }
         * }
         * if (unpackEndItemBOMLine == null) {
         * SkipException skipException = new SkipException("END-ITEM unpack ������ �����ϴ�. - I/F PE FN ID : " + peFunctionItemId + " @@ OCCPUID : " + absOccPuidList[absOccPuidList.length - 1]);
         * skipException.setStatus(TCData.STATUS_ERROR);
         * throw skipException;
         * }
         * return unpackEndItemBOMLine;
         * } else {
         * return endItemBOMLine;
         * }
         */
        return endItemBOMLine;
    }

    /**
     * M-BOM PRODUCT �������� Function�� PE I/F EndItem�� �˻��Ѵ�.
     * 
     * @method findEndItemBOMLine
     * @date 2014. 3. 2.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOMLine findEndItemBOMLine(String[] absOccPuidList, TCComponentBOMLine parentBOMLine, int level) throws Exception {
        // pack�� ��� unpack�Ͽ� unpack ���� ��ȸ�Ѵ�.
        try {
            if (parentBOMLine.isPacked()) {
                return parentFindEndItemBOMLine(absOccPuidList, parentBOMLine, level);
            }
            ArrayList<TCComponentBOMLine> unpackChidrenList = unpackChidren(parentBOMLine);
            for (int i = 0; i < unpackChidrenList.size(); i++) {
                String occpuid = unpackChidrenList.get(i).getProperty("bl_occurrence_uid");
                if (absOccPuidList[level].equals(occpuid)) {
                    if (absOccPuidList.length - 1 == level) {
                        return unpackChidrenList.get(i);
                    } else {
                        return findEndItemBOMLine(absOccPuidList, unpackChidrenList.get(i), level + 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    /**
     * Target BOMLine�� packed�� �����̸� unpack�� unpack BOMLine �� ��ŭ loop�� �Ͽ� endItem�� �˻��Ѵ�.
     * 
     * @method parentFindEndItemBOMLine
     * @date 2014. 3. 2.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine parentFindEndItemBOMLine(String[] absOccPuidList, TCComponentBOMLine parentBOMLine, int level) throws Exception {
        TCComponentBOMLine[] unpackLines = SDVBOPUtilities.getUnpackBOMLines(parentBOMLine);
        if (unpackLines != null) {
            for (TCComponentBOMLine unpackLine : unpackLines) {
                return findEndItemBOMLine(absOccPuidList, unpackLine, level);
            }
        }
        return null;
    }

    /**
     * endItem getChidren�� unpack�Ͽ� �����´�.
     * 
     * @method unpackChidren
     * @date 2014. 3. 2.
     * @param
     * @return ArrayList<TCComponentBOMLine>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<TCComponentBOMLine> unpackChidren(TCComponentBOMLine parentBOMLine) throws Exception {
        ArrayList<TCComponentBOMLine> unpackChidrenList = new ArrayList<TCComponentBOMLine>();
        AIFComponentContext contexts[] = parentBOMLine.getChildren();
        for (int i = 0; i < contexts.length; i++) {
            TCComponentBOMLine childLine = (TCComponentBOMLine) contexts[i].getComponent();
            if (childLine == null) {
                continue;
            }
            if (childLine.isPacked()) {
                TCComponentBOMLine[] unpackLines = SDVBOPUtilities.getUnpackBOMLines(childLine);
                if (unpackLines != null) {
                    for (TCComponentBOMLine unpackLine : unpackLines) {
                        unpackChidrenList.add(unpackLine);
                    }
                }
            } else {
                unpackChidrenList.add(childLine);
            }
        }
        return unpackChidrenList;
    }

    /**
     * ABS OCC PUID�� "+" delimit�� �����ڷ� �迭�� ��ȯ�Ѵ�.
     * 
     * @method splitAbsOccPuids
     * @date 2013. 12. 3.
     * @param
     * @return String[]
     * @exception
     * @throws
     * @see
     */
    public String[] splitAbsOccPuids(String absOccPuids) {
        if (StringUtils.isEmpty(absOccPuids)) {
            return new String[0];
        }
        return absOccPuids.split("\\+");
    }

    /**
     * Row Expand ���� ó��
     * 
     * @method expandAllTCDataItemPre
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void expandAllTCDataItemPre() throws Exception {

    }

    /**
     * Row Expand ���� ó��
     * 
     * @method expandAllTCDataItemPost
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void expandAllTCDataItemPost(ArrayList<TCData> expandAllItems) throws Exception {

    }

    /**
     * �Ӽ� ���������� Log�� ����Ѵ�.
     * 
     * @method printOperationDifferenceResult
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private String printDifferenceResult(TCData tcData, LinkedHashMap<String, String[]> differenceResult) throws Exception {
        StringBuffer logString = new StringBuffer();
        if (differenceResult == null || differenceResult.size() == 0) {
            return "";
        }
        String[] attrIds = differenceResult.keySet().toArray(new String[differenceResult.size()]);
        logString.append("\t��  <" + tcData.getText() + "> ���� �Ӽ� \n");
        for (String attrId : attrIds) {
            String[] difference = differenceResult.get(attrId);
            logString.append("\t\t[" + attrId + "] > TC: '" + difference[0] + "'   @@   PE: '" + difference[1] + "' \n");
        }
        return logString.toString();
    }

    /**
     * �α� ó��..
     * 
     * @method saveLog
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void saveLog(final TCData tcData, final ArrayList<Exception> exception, String logMessage) {
        // Log ���� ó��..
        try {
            saveLog(tcData, logMessage, true);
        } catch (Exception e) {
            exception.add(new ValidateSDVException(e.getMessage(), e));
        }
    }

    /**
     * �α� ó��..
     * 
     * @method saveLog
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void saveLog(final TCData tcData, String logMessage, boolean outputClassType) throws Exception {
        Text logText = getTcDataMigrationJob().getLogText();
        String logFilePath = ((PEIFTCDataExecuteJob) getTcDataMigrationJob()).getPeIFJobWork().getLogFilePath();
        ImportCoreService.saveLog(shell, tcData, logText, logFilePath, logMessage, outputClassType);
    }

}
