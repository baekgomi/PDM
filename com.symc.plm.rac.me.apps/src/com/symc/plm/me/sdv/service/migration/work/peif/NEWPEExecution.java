/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.work.peif;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.ExecuteSDVException;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.assembly.CreateAssemblyOPOperation;
import com.symc.plm.me.sdv.operation.common.ReviseActionOperation;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.exception.SkipException;
import com.symc.plm.me.sdv.service.migration.job.TCDataMigrationJob;
import com.symc.plm.me.sdv.service.migration.job.peif.PEIFTCDataExecuteJob;
import com.symc.plm.me.sdv.service.migration.model.tcdata.TCData;
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
import com.symc.plm.me.sdv.service.resource.service.create.CreateEquipmentItemService;
import com.symc.plm.me.sdv.service.resource.service.create.CreateToolItemService;
import com.symc.plm.me.sdv.view.resource.CreateResourceViewPane;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOPLine;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.cme.time.common.CommonUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentLine;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentWindow;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentWindowType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMECfgLine;
import com.teamcenter.rac.kernel.TCComponentMEOPRevision;
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : PEExecution
 * Class Description :
 * 
 * [SR����][20150224]shcho, Revise�� BOPLine���� newRevision�� �������� ���ϴ� ��찡 �幰�� �߻��Ͽ�, ��������� refresh ����
 * [SR����][20150224]shcho, activity ���� �� refresh �Ҷ� The specified tag  has been deleted, can not find requested object ������ �߻��Ͽ� ����BOMLine�� refresh���� ����
 * 
 * @date 2013. 11. 25.
 * 
 */
public class NEWPEExecution extends PEDataWork {

    private static String DEFAULT_REV_ID = "000";
    private static String EMPTY_DATA = "-";

    /**
     * @param shell
     * @param tcDataMigrationJob
     * @param processLine
     * @param mecoNo
     * @param isOverride
     */
    public NEWPEExecution(Shell shell, TCDataMigrationJob tcDataMigrationJob, TCComponentMfgBvrProcess processLine, String mecoNo, boolean isOverride) {
        super(shell, tcDataMigrationJob, processLine, mecoNo, isOverride);
    }

    public void rowExecute(final int index, final TCData tcData) throws Exception {
        final ArrayList<Exception> exception = new ArrayList<Exception>();
        
        Runnable aExecutionRun = new Runnable() {
            public void run() {
                String message = "";
                String logMessage = "";
                String differLog = "";
                try {
                    getTcDataMigrationJob().getTree().setSelection(tcData);
                    // Master ���� ������ ������ SubActivity�� ��ü �����̹Ƿ� ����üũ�� 'STATUS_VALIDATE_COMPLETED' ���·� �����Ѵ�.
                    if (tcData instanceof ActivityMasterData) {
                        if (((ActivityMasterData) tcData).isCreateable()) {
                            setSubActivityStatus((ActivityMasterData) tcData);
                        }
                    }
                    // Validate ���� üũ
                    if (TCData.STATUS_ERROR == tcData.getStatus() && (tcData instanceof SheetDatasetData)==false) {
                        SkipException errorSkipException = new SkipException("Validate�� Error�߻����� ���� ������ Skip �մϴ�. - " + tcData.getStatusMessage());
                        errorSkipException.setStatus(TCData.STATUS_ERROR);
                        throw errorSkipException;
                    } else if (TCData.STATUS_SKIP == tcData.getStatus() && (tcData instanceof SheetDatasetData)==false) {
                        SkipException skipSkipException = new SkipException("Validate�� Skip�߻����� ���� ������ Skip �մϴ�. - " + tcData.getStatusMessage());
                        skipSkipException.setStatus(TCData.STATUS_SKIP);
                        throw skipSkipException;
                    }
                    // Progress �޼���
                    tcData.setStatus(TCData.STATUS_INPROGRESS);
                    // LINE Execute
                    if (tcData instanceof LineItemData) {
                        executeLine(processLine, (LineItemData) tcData);
                    }
                    // OPERATION ó��
                    else if (tcData instanceof OperationItemData) {
                        // Operation
                        executeOperation((OperationItemData) tcData);
                    }
                    // Activity ó��
                    else if (tcData instanceof ActivityMasterData) {
                        // Activity
                        executeActivity((ActivityMasterData) tcData);
                    }
                    // Resource - OccurrenceData ó��
                    else if (tcData instanceof OccurrenceData) {
                        // Resource - EquipmentData ó��
                        if (tcData instanceof EquipmentData) {
                            differLog = executeEquipmentData((EquipmentData) tcData);
                        }
                        // Resource - ToolData ó��
                        if (tcData instanceof ToolData) {
                            differLog = executeToolData((ToolData) tcData);
                        }
                        // Resource - EndItemData ó��
                        if (tcData instanceof EndItemData) {
                            differLog = executeEndItemData((EndItemData) tcData);
                        }
                        // Resource - SubsidiaryData ó��
                        if (tcData instanceof SubsidiaryData) {
                            differLog = executeSubsidiaryData((SubsidiaryData) tcData);
                        }
                    } // SheetDatasetData ó��
                    else if (tcData instanceof SheetDatasetData) {
                        // DatasetData
                        differLog = executeSheetDatasetData((SheetDatasetData) tcData);
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
             * Master ���� ������ ������ SubActivity�� ��ü �����̹Ƿ� ����üũ�� 'STATUS_VALIDATE_COMPLETED' ���·� �����Ѵ�.
             * 
             * @method setSubActivityStatus
             * @date 2013. 12. 18.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void setSubActivityStatus(ActivityMasterData activityMasterData) {
                if (!activityMasterData.isCreateable()) {
                    return;
                }
                TreeItem[] subActivityItems = activityMasterData.getItems();
                for (TreeItem treeItem : subActivityItems) {
                    ((ActivitySubData) treeItem).setStatus(TCData.STATUS_VALIDATE_COMPLETED, "");
                }
            }

            /**
             * Line Execute
             * 
             * @method executeLine
             * @date 2013. 12. 5.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void executeLine(TCComponentMfgBvrProcess processLine, LineItemData tcData) throws Exception {
            }

            /**
             * Revise Operation Dataset
             * 
             * @method generateOperationReviseDataset
             * @date 2013. 12. 17.
             * @param
             * @return IDataSet
             * @exception
             * @throws
             * @see
             */
            private IDataSet generateOperationReviseDataset(OperationItemData operationItemData) throws Exception {
                IDataSet dataSet = new DataSet();
                // MECO ����
                IDataMap reviseMecoViewDataMap = new RawDataMap();
                reviseMecoViewDataMap.put(SDVPropertyConstant.SHOP_REV_MECO_NO, ImportCoreService.getMecoRevision(mecoNo), IData.OBJECT_FIELD);
                dataSet.addDataMap("reviseMecoView", reviseMecoViewDataMap);
                // Revise BOMLine ����
                ArrayList<TCComponentBOMLine> operationBOMLineList = new ArrayList<TCComponentBOMLine>();
                operationBOMLineList.add(operationItemData.getBopBomLine());
                IDataMap reviseViewDataMap = new RawDataMap();
                reviseViewDataMap.put("reviseView", operationBOMLineList, IData.LIST_FIELD);
                dataSet.addDataMap("reviseView", reviseViewDataMap);
                return dataSet;
            }

            /**
             * Operation Execute
             * 
             * @method executeOperation
             * @date 2013. 12. 5.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void executeOperation(OperationItemData operationItemData) throws Exception {
                // ���� ����
                if (!operationItemData.isExistItem()) {
                    createOperationItem(operationItemData);
                } else {
                    // 1. isOverride = true�� ��� Released �����̸� ������ Revise�Ѵ�.
                    // 2. ���� �����Ͱ� ���� ������ �ִ� ��� or ���� ������ ���� ������ �ִ� ���

                    // 1. isOverride = true�� ��� Released �����̸� ������ Revise�Ѵ�.
                    if (isOverride) {
                        // 2. ���� �����Ͱ� ���� ������ �ִ� ��� or ���� ������ ���� ������ �ִ� ���
                        if (operationItemData.isMasterModifiable() || checkOperationUnderModify(operationItemData)) {
                            if (SYMTcUtil.isLatestedRevItemReleased(operationItemData.getBopBomLine().getItemRevision())) {
                                // Revise ����
                                IDataSet dataSet = generateOperationReviseDataset(operationItemData);
                                ReviseActionOperation ReviseActionOperation = new ReviseActionOperation(100, this.getClass().getName(), dataSet, true);
                                ReviseActionOperation.revise(dataSet);
                                // �α� ���
                                saveLog(operationItemData, "����(Operation) ITEM�� Released �����̹Ƿ� Revise �Ͽ����ϴ�. \n");
                                
                                //[SR����][20150224]shcho, Revise�� BOPLine���� newRevision�� �������� ���ϴ� ��찡 �幰�� �߻��Ͽ�, ��������� refresh ���� 
                                refreshBOMLine(operationItemData.getBopBomLine());
                                refreshBOMLine(processLine);
                            }
                        }
                    }
                    // Master �Ӽ� �������� üũ
                    if (operationItemData.isMasterModifiable()) {
                        updateOperationItem(operationItemData);
                    }
                }
                // BOMLine �Ӽ� �������� üũ
                if (operationItemData.isBOMLineModifiable()) {
                    // 1. Find No. update
                    IDataSet dataSet = (IDataSet) operationItemData.getData(PEExcelConstants.DATASET);
                    IDataMap opInformDataMap = dataSet.getDataMap("opInform");
                    if (!StringUtils.isEmpty(opInformDataMap.getStringValue(SDVPropertyConstant.BL_SEQUENCE_NO))) {
                        operationItemData.getBopBomLine().getTCProperty(SDVPropertyConstant.BL_SEQUENCE_NO).setStringValue(opInformDataMap.getStringValue(SDVPropertyConstant.BL_SEQUENCE_NO));
                    }
                    // 2. ����(QUANTITY) update
                    //[SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
                    //operationItemData.getBopBomLine().setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
                    // 3. Option condition update (Exception �߻��� ���� �ߴ� ó���� �����ʴ´�.)
                    try {
                        SDVBOPUtilities.updateOptionCondition(operationItemData.getBopBomLine(), operationItemData.getConversionOptionCondition());
                    } catch (Exception e) {
                        SkipException skipException = new SkipException(e.getMessage(), e);
                        throw skipException;
                    }
                }
            }

            /**
             * �������� BOMLine�� ���� ������ �ִ��� üũ�Ѵ�.
             * 
             * @method checkOperationUnderModify
             * @date 2014. 1. 14.
             * @param
             * @return boolean
             * @exception
             * @throws
             * @see
             */
            private boolean checkOperationUnderModify(OperationItemData operationItemData) {
                TreeItem[] items = operationItemData.getItems();
                for (TreeItem item : items) {
                    // Skip�� �ƴѰ�찡 1���� ������ ���������� �ִ� �ɷ� ����
                    if (TCData.STATUS_SKIP != ((TCData) item).getStatus()) {
                        return true;
                    }
                }
                return false;
            }

            /**
             * ���� ����
             * 
             * @method createOperationItem
             * @date 2013. 12. 5.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void createOperationItem(OperationItemData operationItemData) throws Exception {
                // Dataset ��������
                IDataSet dataSet = (IDataSet) operationItemData.getData(PEExcelConstants.DATASET);
                CreateAssemblyOPOperation createOperation = new CreateAssemblyOPOperation(99, this.getClass().getName(), dataSet, true);
                // ���� �� Operation BOMLine�� Set
                operationItemData.setBopBomLine(createOperation.createOperation());
                // BOMLine Modifiable = true
                operationItemData.setBOMLineModifiable(true);
            }

            /**
             * ���� ������Ʈ
             * 
             * @method updateOperationItem
             * @date 2013. 12. 10.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void updateOperationItem(OperationItemData operationItemData) throws Exception {
                // Dataset ��������
                IDataSet dataSet = (IDataSet) operationItemData.getData(PEExcelConstants.DATASET);
                IDataMap opInformDataMap = dataSet.getDataMap("opInform");
                TCComponentItem item = operationItemData.getBopBomLine().getItem();
                // item.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME).getStringValue());
                // item.setProperty(SDVPropertyConstant.OPERATION_ENG_NAME, opInformDataMap.get(SDVPropertyConstant.OPERATION_ENG_NAME).getStringValue());
                item.setProperty(SDVPropertyConstant.OPERATION_WORKER_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKER_CODE).getStringValue());
                item.setProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ, opInformDataMap.get(SDVPropertyConstant.OPERATION_PROCESS_SEQ).getStringValue());
                item.setProperty(SDVPropertyConstant.OPERATION_WORKAREA, opInformDataMap.get(SDVPropertyConstant.OPERATION_WORKAREA).getStringValue());
                TCComponentItemRevision itemRevision = operationItemData.getBopBomLine().getItemRevision();
                // REV object_name�� Item�� �����ϰ� �����.
                itemRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_ENG_NAME).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SHOP, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_SHOP).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_BOP_VERSION).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_STATION_NO).getStringValue());
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, opInformDataMap.get(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE).getStringValue());
            }

            /**
             * Activity ����
             * 
             * @method executeActivity
             * @date 2013. 12. 9.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void executeActivity(ActivityMasterData tcData) throws Exception {
                // ���� Flag�� �̴ϸ� return
                if (!tcData.isCreateable()) {
                    return;
                }
                OperationItemData operationItemData = (OperationItemData) tcData.getParentItem();
                // 1, 2 ��������� ���� �����ϳ� Window ���� ���Ϸ� ���� BOMLine Reference�˻� �Ͽ� �����ϴ� 1���� ���
                //
                // 1. <���� �����> Activity ���� (BOMWindow ���������ʰ� ActivityLine ����)
                createActivities(tcData, operationItemData);
                // 2. <���� ������� ����> Activity ���� (BOMWindow ���� �� ActivityLine ����)
                // createActivitysWithWindow(tcData, operationItemData);
            }

            /**
             * <���� ������� ����> Activity ���� (BOMWindow ���� �� ActivityLine ����)
             * 
             * @deprecated
             * @method createActivitysWithWindow
             * @date 2014. 1. 8.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings({ "unchecked", "unused" })
            private void createActivitysWithWindow(ActivityMasterData tcData, OperationItemData operationItemData) throws Exception {
                TCComponentCfgAttachmentWindow activityWindow = null;
                try {
                    // Activity Window ����
                    activityWindow = createActivityWindow(operationItemData.getBopBomLine());
                    // Activity Window�� Activity Root Top Line ����
                    TCComponentCfgAttachmentLine rootTCComponentCfgAttachmentLine = setActivityWindowTopLine(operationItemData, activityWindow);
                    // Activity ��ü ����
                    removeAllActivity(rootTCComponentCfgAttachmentLine);
                    // activity root refresh
                    refreshBOMLine(rootTCComponentCfgAttachmentLine);
                    // Activity ����
                    TreeItem[] activitySubDatas = tcData.getItems();
                    for (TreeItem activitySubData : activitySubDatas) {
                        ArrayList<String> activitySubRowData = (ArrayList<String>) activitySubData.getData();
                        TCComponent[] afterTCComponents = ActivityUtils.createActivitiesBelow(new TCComponent[] { rootTCComponentCfgAttachmentLine }, activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KOR_NAME_COLUMN_INDEX));
                        TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) afterTCComponents[0];
                        TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();
                        // Activity Time
                        double timeSystemUnitTime = 0.0;
                        if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX))) {
                            timeSystemUnitTime = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX));
                        }
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).setDoubleValue(timeSystemUnitTime);
                        // Category
                        String category = ImportCoreService.getPEActivityCategolyLOV(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_CATEGORY_COLUMN_INDEX));
                        if (category != null) {
                            activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).setStringValue(category);
                        }
                        // Work Code (SYSTEM Code)
                        // �۾����
                        String workCode = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_MAIN_COLUMN_INDEX);
                        // ����
                        if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX))) {
                            workCode = workCode + "-" + activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX).replace(",", "-");
                        }
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE).setStringValue(workCode);
                        // Time System Frequency
                        double timeSystemFrequency = 0.0;
                        if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX))) {
                            timeSystemFrequency = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX));
                        }
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).setDoubleValue(timeSystemFrequency);
                        // Activity �����ڿ� �Ҵ�
                        addActivityTools(operationItemData.getBopBomLine(), activityLine, (ActivitySubData) activitySubData);
                        // KPC
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT).setStringValue(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_COLUMN_INDEX));
                        // KPC ��������
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).setStringValue(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_BASIS_COLUMN_INDEX));
                        // activity sub refresh
                        refreshBOMLine(activityLine);
                    }
                    // activity root refresh
                    refreshBOMLine(rootTCComponentCfgAttachmentLine);
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (activityWindow != null) {
                        activityWindow.closeWindow();
                    }
                }
            }

            /**
             * <���� �����> Activity ���� (BOMWindow ���������ʰ� ActivityLine ����)
             * 
             * @method createActivities
             * @date 2014. 1. 8.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            public void createActivities(ActivityMasterData tcData, OperationItemData operationItemData) throws Exception, TCException {
                TCComponentCfgActivityLine[] childActivityList = getChildActivityList(operationItemData.getBopBomLine());
                for (TCComponentCfgActivityLine meActivityLine : childActivityList) {
                    TCComponentMECfgLine parentLine = meActivityLine.parent();
                    ActivityUtils.removeActivity(meActivityLine);
                    parentLine.save();
                }
                TCComponent root = operationItemData.getBopBomLine().getReferenceProperty("bl_me_activity_lines");
                // activity refresh
                refreshBOMLine(root);
                //[SR����][20150224]shcho, activity ���� �� refresh �Ҷ� The specified tag  has been deleted, can not find requested object ������ �߻��Ͽ� ����BOMLine�� refresh���� ����
                //refreshBOMLine(operationItemData.getBopBomLine());

                // Activity ����
                TreeItem[] activitySubDatas = tcData.getItems();
                for (TreeItem activitySubData : activitySubDatas) {
                    ArrayList<String> activitySubRowData = (ArrayList<String>) activitySubData.getData();
                    TCComponent[] afterTCComponents = ActivityUtils.createActivitiesBelow(new TCComponent[] { root }, activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KOR_NAME_COLUMN_INDEX));
                    TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) afterTCComponents[0];
                    TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();
                    // Activity Time
                    double timeSystemUnitTime = 0.0;
                    if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX))) {
                        timeSystemUnitTime = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TIME_COLUMN_INDEX));
                    }
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).setDoubleValue(timeSystemUnitTime);
                    // Category
                    String category = ImportCoreService.getPEActivityCategolyLOV(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_CATEGORY_COLUMN_INDEX));
                    if (category != null) {
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).setStringValue(category);
                    }
                    // Work Code (SYSTEM Code)
                    // �۾����
                    String workCode = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_MAIN_COLUMN_INDEX);
                    // ����
                    if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX))) {
                        workCode = workCode + "-" + activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX).replace(",", "-");
                    }
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE).setStringValue(workCode);
                    // Time System Frequency
                    double timeSystemFrequency = 0.0;
                    if (!StringUtils.isEmpty(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX))) {
                        timeSystemFrequency = Double.parseDouble(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX));
                    }
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).setDoubleValue(timeSystemFrequency);
                    // Activity �����ڿ� �Ҵ�
                    addActivityTools(operationItemData.getBopBomLine(), activityLine, (ActivitySubData) activitySubData);
                    // KPC
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT).setStringValue(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_COLUMN_INDEX));
                    // KPC ��������
                    activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).setStringValue(activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_KPC_BASIS_COLUMN_INDEX));
                    activity.save();
                    root.save();
                }
            }

            private TCComponentCfgActivityLine[] getChildActivityList(TCComponentBOMLine bomLine) throws Exception {
                ArrayList<TCComponentCfgActivityLine> childActivityList = new ArrayList<TCComponentCfgActivityLine>();
                TCComponent root = bomLine.getReferenceProperty("bl_me_activity_lines");
                if (root != null) {
                    if (root instanceof TCComponentCfgActivityLine) {
                        TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) root);
                        for (TCComponent childLine : childLines) {
                            if (childLine instanceof TCComponentCfgActivityLine) {
                                childActivityList.add((TCComponentCfgActivityLine) childLine);
                            }
                        }
                    }
                }
                return childActivityList.toArray(new TCComponentCfgActivityLine[childActivityList.size()]);
            }

            /**
             * Activity �����ڿ� �Ҵ�
             * 
             * @method addActivityTools
             * @date 2013. 12. 19.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private void addActivityTools(TCComponentBOMLine operationBOMLine, TCComponentCfgActivityLine activityLine, ActivitySubData activitySubData) throws Exception {
                TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();
                ArrayList<String> activitySubRowData = (ArrayList<String>) activitySubData.getData();
                String toolId = activitySubRowData.get(PEExcelConstants.ACTIVITY_MASTER_TOOL_ID_COLUMN_INDEX);
                if (StringUtils.isEmpty(toolId)) {
                    return;
                }
                String[] toolIds = toolId.split(",");
                HashMap<String, TCComponentBOMLine> findedAssignToolBOMLine = findAssignToolBOMLine(operationBOMLine, toolIds);
                for (String itemId : findedAssignToolBOMLine.keySet()) {
                    TCComponentBOMLine bomLine = findedAssignToolBOMLine.get(itemId);
                    if (bomLine != null) {
                        activity.addReferenceTools(operationBOMLine, new TCComponentBOMLine[] { bomLine });
                    }
                    // Activity �����Ҵ� �α�
                    saveLogAcvivityAssingTool(itemId, activitySubData, bomLine);
                }
            }

            /**
             * Activity �����Ҵ� �α�
             * 
             * @method saveLogAcvivityAssingTool
             * @date 2013. 12. 19.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void saveLogAcvivityAssingTool(String toolItemId, ActivitySubData activitySubData, TCComponentBOMLine bomLine) throws Exception {
                StringBuffer log = new StringBuffer();
                if (bomLine == null) {
                    String errorMsg = "'" + toolItemId + "' �� ����(Operation)�� �������� �ʾ� Activity �Ҵ��� Skip �Ͽ����ϴ�.";
                    log.append("~ assign Activity : [" + activitySubData.getText() + "] " + errorMsg);
                    activitySubData.setStatus(TCData.STATUS_ERROR, errorMsg);
                } else {
                    log.append("~ assign Activity : [" + activitySubData.getText() + "] '" + toolItemId + "' ��(��) ����(Operation) Activity�� �Ҵ� �Ͽ����ϴ�.");
                }
                saveLog(activitySubData, log.toString());
            }

            /**
             * �Ҵ� ��� ����(Tool) ����(Operation)���� �˻�
             * 
             * @method findAssignToolBOMLine
             * @date 2013. 12. 19.
             * @param
             * @return HashMap<String,TCComponentBOMLine>
             * @exception
             * @throws
             * @see
             */
            private HashMap<String, TCComponentBOMLine> findAssignToolBOMLine(TCComponentBOMLine operationBOMLine, String[] toolIds) throws Exception {
                HashMap<String, TCComponentBOMLine> findedAssignToolBOMLine = new HashMap<String, TCComponentBOMLine>();
                if (toolIds == null || toolIds.length == 0) {
                    return findedAssignToolBOMLine;
                }
                // �ʱ�ȭ
                for (int i = 0; i < toolIds.length; i++) {
                    toolIds[i] = (toolIds[i] == null) ? "" : toolIds[i].trim();
                }
                for (String toolId : toolIds) {
                    findedAssignToolBOMLine.put(toolId, null);
                }
                TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(operationBOMLine);
                for (TCComponentBOMLine operationUnderBOMLine : childs) {
                    String itemId = operationUnderBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    // PE I/F ����ID�� ������ �������� ���� �˻�
                    if (findedAssignToolBOMLine.containsKey(itemId)) {
                        findedAssignToolBOMLine.put(itemId, operationUnderBOMLine);
                    }
                }
                return findedAssignToolBOMLine;
            }

            /**
             * Activity Window�� Activity Root Top Line ����
             * 
             * @method setActivityWindowTopLine
             * @date 2013. 12. 13.
             * @param
             * @return TCComponentCfgAttachmentLine
             * @exception
             * @throws
             * @see
             */
            private TCComponentCfgAttachmentLine setActivityWindowTopLine(OperationItemData operationItemData, TCComponentCfgAttachmentWindow activityWindow) throws Exception {
                // TOP Line ����
                TCComponentItemRevision localTCComponentItemRevision = operationItemData.getBopBomLine().getItemRevision();
                if (!(localTCComponentItemRevision instanceof TCComponentMEOPRevision)) {
                    throw new ExecuteSDVException("����(Operation)��  ItemRevision�� �����ϴ�.");
                }
                TCComponentMEOPRevision localTCComponentMEOPRevision = (TCComponentMEOPRevision) localTCComponentItemRevision;
                TCComponentMEActivity localTCComponentMEActivity = (TCComponentMEActivity) localTCComponentMEOPRevision.getRelatedComponent("root_activity");
                if (localTCComponentMEActivity == null) {
                    throw new ExecuteSDVException("����(Operation)�� Root Activity ������ �����ϴ�.");
                }
                TCComponentCfgAttachmentLine rootTCComponentCfgAttachmentLine = (TCComponentCfgAttachmentLine) activityWindow.createTopLine(localTCComponentMEActivity);
                rootTCComponentCfgAttachmentLine.setReferenceProperty("al_activity_oper_bl", operationItemData.getBopBomLine());
                return rootTCComponentCfgAttachmentLine;
            }

            /**
             * Activity ��ü ����
             * 
             * @method removeAllActivity
             * @date 2013. 12. 13.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private void removeAllActivity(TCComponentCfgAttachmentLine rootTCComponentCfgAttachmentLine) throws TCException {
                TCComponent[] childComps = ActivityUtils.getSortedActivityChildren(rootTCComponentCfgAttachmentLine);
                for (TCComponent activityChildComp : childComps) {
                    TCComponentCfgAttachmentLine activityBOMLine = (TCComponentCfgAttachmentLine) activityChildComp;
                    // rootTCComponentCfgAttachmentLine.deleteChild(activityBOMLine, "me_cl_child_lines");
                    ActivityUtils.removeActivity(activityBOMLine);
                }
            }

            /**
             * �ڿ��Ҵ� ���� - EquipmentData
             * 
             * @method executeEquipmentData
             * @date 2013. 12. 12.
             * @param
             * @return String
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private String executeEquipmentData(EquipmentData equipmentData) throws Exception {
                StringBuffer log = new StringBuffer();
                TCComponentBOMLine bopOperationBOMLine = ((OperationItemData) equipmentData.getParentItem()).getBopBomLine();
                if (equipmentData.getBopBomLine() == null) {
                    // ����(Equipment) Item ����
                    if (equipmentData.getResourceItem() == null) {
                        equipmentData.setResourceItem(createResourceItem(log, equipmentData));
                    }
                    ArrayList<InterfaceAIFComponent> toolDataList = new ArrayList<InterfaceAIFComponent>();
                    toolDataList.add(equipmentData.getResourceItem());
                    TCComponent[] resultBOMLineList = SDVBOPUtilities.connectObject(bopOperationBOMLine, toolDataList, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
                    // BOMLine ����
                    equipmentData.setBopBomLine((TCComponentBOMLine) resultBOMLineList[0]);
                    // BOMLine Modifiable = true
                    equipmentData.setBOMLineModifiable(true);
                    log.append("\t ~ ����(EQUIPMENT) �Ҵ� �Ϸ� - " + equipmentData.getItemId() + "\n");
                }
                if (equipmentData.isBOMLineModifiable()) {
                    ArrayList<String> bomRowData = (ArrayList<String>) equipmentData.getData(PEExcelConstants.BOM);
                    equipmentData.getBopBomLine().setProperty(SDVPropertyConstant.BL_QUANTITY, bomRowData.get(PEExcelConstants.EQUIPMENT_BOM_QUANTITY_COLUMN_INDEX));
                    equipmentData.getBopBomLine().setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, equipmentData.getFindNo());
                    log.append("\t ~ ����(EQUIPMENT) BOMLine ������Ʈ �Ϸ� - " + equipmentData.getItemId());
                }
                return log.toString();
            }

            /**
             * �ڿ��Ҵ� ���� - ToolData
             * 
             * @method executeToolData
             * @date 2013. 12. 10.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private String executeToolData(ToolData toolData) throws Exception {
                StringBuffer log = new StringBuffer();
                TCComponentBOMLine bopOperationBOMLine = ((OperationItemData) toolData.getParentItem()).getBopBomLine();
                if (toolData.getBopBomLine() == null) {
                    // ����(Tool) Item ����
                    if (toolData.getResourceItem() == null) {
                        toolData.setResourceItem(createResourceItem(log, toolData));
                    }
                    ArrayList<InterfaceAIFComponent> toolDataList = new ArrayList<InterfaceAIFComponent>();
                    toolDataList.add(toolData.getResourceItem());
                    TCComponent[] resultBOMLineList = SDVBOPUtilities.connectObject(bopOperationBOMLine, toolDataList, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_TOOL);
                    // BOMLine ����
                    toolData.setBopBomLine((TCComponentBOMLine) resultBOMLineList[0]);
                    // BOMLine Modifiable = true
                    toolData.setBOMLineModifiable(true);
                    log.append("\t ~ ����(TOOL) �Ҵ� �Ϸ� - " + toolData.getItemId() + "\n");
                }
                if (toolData.isBOMLineModifiable()) {
                    ArrayList<String> bomRowData = (ArrayList<String>) toolData.getData(PEExcelConstants.BOM);
                    toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_QUANTITY, bomRowData.get(PEExcelConstants.TOOL_BOM_QUANTITY_INDEX));
                    String torqueType = "";
                    String torqueValue = "";
                    if (StringUtils.isEmpty(bomRowData.get(PEExcelConstants.TOOL_BOM_TORQUE_COLUMN_INDEX))) {
                        toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_TORQUE, torqueType);
                        toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, torqueValue);
                    } else {
                        // String[] torqueDelimits = bomRowData.get(PEExcelConstants.TOOL_BOM_TORQUE_COLUMN_INDEX).split(" ");
                        // torqueType = torqueDelimits[0].trim();
                        // torqueValue = torqueDelimits[1].trim();
                        String peTorque = bomRowData.get(PEExcelConstants.TOOL_BOM_TORQUE_COLUMN_INDEX).trim();
                        // Torque Type : ���ڸ� 2�ڸ� ������ substring 
                        torqueType = peTorque.substring(0, 2);
                        // Torque Value : 2�ڸ� ���� ������ �ڸ���
                        torqueValue = peTorque.substring(2, peTorque.length());
                        torqueValue = torqueValue.trim();
                        toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_TORQUE, torqueType);
                        toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, torqueValue);
                    }
                    toolData.getBopBomLine().setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, toolData.getFindNo());
                    log.append("\t ~ ����(TOOL) BOMLine ������Ʈ �Ϸ� - " + toolData.getItemId());
                }
                return log.toString();
            }

            /**
             * ����(Equipment), ����(Tool) Item ����
             * 
             * (���� �� �����߻��� Skip ó���Ѵ�.)
             * 
             * @method createResourceEquipmentItem
             * @date 2013. 12. 13.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private TCComponentItem createResourceItem(StringBuffer log, OccurrenceData occurrenceData) throws Exception {
                TCComponentItem resourceItem = null;
                try {
                    if (occurrenceData.getResourceItem() == null) {
                        occurrenceData.setResourceItem(createResourceItem(occurrenceData));
                        log.append("\t ~ [" + occurrenceData.getClassType() + "] Resource�� ���������ʾ� �����Ͽ����ϴ�. - " + occurrenceData.getItemId() + "\n");
                    }
                    if (occurrenceData.getData(PEExcelConstants.MASTER) == null) {
                        throw new ExecuteSDVException("[" + occurrenceData.getClassType() + "] PE I/F ��MASTER ������ �����ϴ�. - " + occurrenceData.getItemId());
                    }
                    resourceItem = occurrenceData.getResourceItem();
                } catch (Exception e) {
                    SkipException skipException = new SkipException(e.getMessage(), e);
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
                return resourceItem;
            }

            /**
             * Resource Type�� Item ����
             * 
             * @method createResourceItem
             * @date 2013. 12. 13.
             * @param
             * @return TCComponentItem
             * @exception
             * @throws
             * @see
             */
            private TCComponentItem createResourceItem(OccurrenceData occurrenceData) throws Exception {
                if (occurrenceData instanceof ToolData) {
                    return createResourceToolItem((ToolData) occurrenceData);
                } else if (occurrenceData instanceof EquipmentData) {
                    return createResourceEquipmentItem((EquipmentData) occurrenceData);
                } else {
                    SkipException skipException = new SkipException("���� Resource Item Type�� �ƴմϴ�. - " + occurrenceData.getClassType());
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
            }

            /**
             * ����(Tool) Resource Item ����
             * 
             * @method createResourceToolItem
             * @date 2013. 12. 26.
             * @param
             * @return TCComponentItem
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            public TCComponentItem createResourceToolItem(ToolData toolData) throws Exception {
                ArrayList<String> masterRowData = (ArrayList<String>) toolData.getData(PEExcelConstants.MASTER);
                IDataMap datamap = new RawDataMap();
                datamap.put("createMode", true, IData.BOOLEAN_FIELD);
                datamap.put("itemTCCompType", SDVTypeConstant.BOP_PROCESS_TOOL_ITEM, IData.STRING_FIELD);
                // Map<String, String> itemProperties
                // public static final String TOOL_ENG_NAME = "m7_ENG_NAME";
                Map<String, String> itemProperties = new HashMap<String, String>();
                itemProperties.put(SDVPropertyConstant.ITEM_ITEM_ID, masterRowData.get(PEExcelConstants.TOOL_MASTER_ITEM_ID_COLUMN_INDEX));
                itemProperties.put(SDVPropertyConstant.TOOL_ENG_NAME, masterRowData.get(PEExcelConstants.TOOL_MASTER_ENG_NAME_COLUMN_INDEX));
                datamap.put("itemProperties", itemProperties, IData.OBJECT_FIELD);
                // Map<String, String> revisionProperties
                // public static final String TOOL_RESOURCE_CATEGORY = "m7_RESOURCE_CATEGORY";
                // public static final String TOOL_MAIN_CLASS = "m7_MAIN_CLASS";
                // public static final String TOOL_SUB_CLASS = "m7_SUB_CLASS";
                // public static final String TOOL_PURPOSE = "m7_PURPOSE_KOR";
                // public static final String TOOL_SPEC_CODE = "m7_SPEC_CODE";
                // public static final String TOOL_SPEC_KOR = "m7_SPEC_KOR";
                // public static final String TOOL_SPEC_ENG = "m7_SPEC_ENG";
                // public static final String TOOL_TORQUE_VALUE = "m7_TORQUE_VALUE";
                // public static final String TOOL_UNIT_USAGE = "m7_UNIT_USAGE";
                // public static final String TOOL_MATERIAL = "m7_MATERIAL";
                // public static final String TOOL_MAKER = "m7_MAKER";
                // public static final String TOOL_MAKER_AF_CODE = "m7_MAKER_AF_CODE";
                // public static final String TOOL_TOOL_SHAPE = "m7_TOOL_SHAPE";
                // public static final String TOOL_TOOL_LENGTH = "m7_TOOL_LENGTH";
                // public static final String TOOL_TOOL_SIZE = "m7_TOOL_SIZE";
                // public static final String TOOL_TOOL_MAGNET = "m7_TOOL_MAGNET";
                // public static final String TOOL_VEHICLE_CODE = "m7_VEHICLE_CODE";
                // public static final String TOOL_STAY_TYPE = "m7_STAY_TYPE";
                // public static final String TOOL_STAY_AREA = "m7_STAY_AREA";
                Map<String, String> revisionProperties = new HashMap<String, String>();
                revisionProperties.put(SDVPropertyConstant.ITEM_REVISION_ID, DEFAULT_REV_ID);
                revisionProperties.put(SDVPropertyConstant.ITEM_OBJECT_NAME, masterRowData.get(PEExcelConstants.TOOL_MASTER_KOR_NAME_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY, getToolResourceCateGory(masterRowData.get(PEExcelConstants.TOOL_MASTER_ITEM_ID_COLUMN_INDEX)));
                revisionProperties.put(SDVPropertyConstant.TOOL_MAIN_CLASS, masterRowData.get(PEExcelConstants.TOOL_MASTER_MAIN_CLASS_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_SUB_CLASS, masterRowData.get(PEExcelConstants.TOOL_MASTER_SUB_CLASS_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_PURPOSE, masterRowData.get(PEExcelConstants.TOOL_MASTER_PURPOSE_KOR_COLUMN_INDEX));
                // �������� ���� �ʱ�ȭ ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.TOOL_PURPOSE))) {
                    revisionProperties.put(SDVPropertyConstant.TOOL_PURPOSE, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_KOR, masterRowData.get(PEExcelConstants.TOOL_MASTER_SPEC_KOR_COLUMN_INDEX));
                // �������� ���� �ʱ�ȭ ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.TOOL_SPEC_KOR))) {
                    revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_KOR, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_ENG, masterRowData.get(PEExcelConstants.TOOL_MASTER_SPEC_ENG_COLUMN_INDEX));
                // �������� ���� �ʱ�ȭ ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.TOOL_SPEC_ENG))) {
                    revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_ENG, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.TOOL_TORQUE_VALUE, masterRowData.get(PEExcelConstants.TOOL_MASTER_PURPOSE_KOR_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_UNIT_USAGE, masterRowData.get(PEExcelConstants.TOOL_MASTER_TORQUE_VALUE_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_MATERIAL, masterRowData.get(PEExcelConstants.TOOL_MASTER_MATERIAL_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.TOOL_MAKER, masterRowData.get(PEExcelConstants.TOOL_MASTER_MAKER_COLUMN_INDEX));
                // Socket ���� ó��
                if (isToolSocket(revisionProperties.get(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY))) {
                    revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_SHAPE, masterRowData.get(PEExcelConstants.TOOL_MASTER_TOOL_SHAPE_COLUMN_INDEX));
                    revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_LENGTH, masterRowData.get(PEExcelConstants.TOOL_MASTER_TOOL_LENGTH_COLUMN_INDEX));
                    revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_SIZE, masterRowData.get(PEExcelConstants.TOOL_MASTER_TOOL_SIZE_COLUMN_INDEX));
                    revisionProperties.put(SDVPropertyConstant.TOOL_TOOL_MAGNET, masterRowData.get(PEExcelConstants.TOOL_MASTER_TOOL_MAGNET_COLUMN_INDEX));
                }
                // Socket ������ �ƴ� ��쿡�� ó��
                else {
                    revisionProperties.put(SDVPropertyConstant.TOOL_SPEC_CODE, masterRowData.get(PEExcelConstants.TOOL_MASTER_SPEC_CODE_COLUMN_INDEX));
                    String makerAf = BundleUtil.nullToString(masterRowData.get(PEExcelConstants.TOOL_MASTER_MAKER_AF_CODE_COLUMN_INDEX)).trim();
                    // empty -> 00
                    if (StringUtils.isEmpty(makerAf)) {
                        revisionProperties.put(SDVPropertyConstant.TOOL_MAKER_AF_CODE, "00");
                    }
                    // 3�ڸ����� ���� ���ڸ��� ó�� - 000 -> 00
                    else if (makerAf.length() == 3) {
                        revisionProperties.put(SDVPropertyConstant.TOOL_MAKER_AF_CODE, makerAf.substring(0, 1));
                    }
                }

                // ����
                // revisionProperties.put(SDVPropertyConstant.TOOL_VEHICLE_CODE, masterRowData.get(0));
                // revisionProperties.put(SDVPropertyConstant.TOOL_STAY_TYPE, masterRowData.get(0));
                // revisionProperties.put(SDVPropertyConstant.TOOL_STAY_AREA, masterRowData.get(0));
                datamap.put("revisionProperties", revisionProperties, IData.OBJECT_FIELD);
                // CAD File List
                RawDataMap fileDataMap = new RawDataMap();
                if (!StringUtils.isEmpty(masterRowData.get(PEExcelConstants.TOOL_MASTER_CAD_FILE_PATH_COLUMN_INDEX))) {
                    fileDataMap.put("isModified", true, IData.BOOLEAN_FIELD);
                    fileDataMap.put("CATPart", masterRowData.get(PEExcelConstants.TOOL_MASTER_CAD_FILE_PATH_COLUMN_INDEX), IData.STRING_FIELD);
                } else {
                    fileDataMap.put("isModified", false, IData.BOOLEAN_FIELD);
                }
                datamap.put("File", fileDataMap, IData.OBJECT_FIELD);

                CreateToolItemService createItemService = new CreateToolItemService(datamap);
                return createItemService.create().getItem();
            }

            /**
             * �Ϲݰ���, ���ϰ��� Ȯ��
             * 
             * @method getToolResourceCateGory
             * @date 2013. 12. 31.
             * @param
             * @return String
             * @exception
             * @throws
             * @see
             */
            private String getToolResourceCateGory(String tooId) throws Exception {
                if (StringUtils.isEmpty(tooId)) {
                    SkipException skipException = new SkipException("TOOL ITEM ID�� ���� ���� �ʽ��ϴ�.");
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
                Registry registry = Registry.getRegistry(CreateResourceViewPane.class);
                if (tooId.split("-").length == 6) {
                    return registry.getString("Resource.Category.SOC");
                } else {
                    return registry.getString("Resource.Category.EXT");
                }
            }

            /**
             * �Ϲݰ���, ���ϰ��� Ȯ��
             * 
             * @method isToolSocket
             * @date 2014. 1. 24.
             * @param
             * @return boolean
             * @exception
             * @throws
             * @see
             */
            private boolean isToolSocket(String category) {
                Registry registry = Registry.getRegistry(CreateResourceViewPane.class);
                if (category.equals(registry.getString("Resource.Category.SOC"))) {
                    return true;
                } else {
                    return false;
                }
            }

            /**
             * ����(Equipment) Resource Item ����
             * 
             * @method createResourceEquipmentItem
             * @date 2013. 12. 26.
             * @param
             * @return TCComponentItem
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private TCComponentItem createResourceEquipmentItem(EquipmentData equipmentData) throws Exception {
                ArrayList<String> masterRowData = (ArrayList<String>) equipmentData.getData(PEExcelConstants.MASTER);
                IDataMap datamap = new RawDataMap();
                datamap.put("createMode", true, IData.BOOLEAN_FIELD);
                boolean isJIG = false;
                if (!StringUtils.isEmpty(masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_JIG_VEHICLE_CODE_COLUMN_INDEX))) {
                    isJIG = true;
                    datamap.put("itemTCCompType", SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM, IData.STRING_FIELD);
                } else {
                    datamap.put("itemTCCompType", SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM, IData.STRING_FIELD);
                }
                // Map<String, String> itemProperties
                // public static final String EQUIP_ENG_NAME = "m7_ENG_NAME";
                Map<String, String> itemProperties = new HashMap<String, String>();
                itemProperties.put(SDVPropertyConstant.ITEM_ITEM_ID, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_ITEM_ID_COLUMN_INDEX));
                itemProperties.put(SDVPropertyConstant.EQUIP_ENG_NAME, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_ENG_NAME_COLUMN_INDEX));
                datamap.put("itemProperties", itemProperties, IData.OBJECT_FIELD);
                // Map<String, String> revisionProperties
                // public static final String EQUIP_SHOP_CODE = "m7_SHOP";
                // public static final String EQUIP_RESOURCE_CATEGORY = "m7_RESOURCE_CATEGORY";
                // public static final String EQUIP_MAIN_CLASS = "m7_MAIN_CLASS";
                // public static final String EQUIP_SUB_CLASS = "m7_SUB_CLASS";
                // public static final String EQUIP_SPEC_KOR = "m7_SPEC_KOR";
                // public static final String EQUIP_SPEC_ENG = "m7_SPEC_ENG";
                // public static final String EQUIP_CAPACITY = "m7_CAPACITY";
                // public static final String EQUIP_MAKER = "m7_MAKER";
                // public static final String EQUIP_NATION = "m7_NATION";
                // public static final String EQUIP_INSTALL_YEAR = "m7_INSTALL_YEAR";
                // public static final String EQUIP_PURPOSE_KOR = "m7_PURPOSE_KOR";
                // public static final String EQUIP_PURPOSE_ENG = "m7_PURPOSE_ENG";
                // public static final String EQUIP_REV_DESC = "m7_REV_DESC";
                //
                // public static final String EQUIP_VEHICLE_CODE= "m7_VEHICLE_CODE";
                // public static final String EQUIP_STATION_CODE = "m7_STATION_CODE";
                // public static final String EQUIP_POSITION_CODE = "m7_POSITION_CODE";
                // public static final String EQUIP_LINE_CODE = "m7_LINE";
                //
                // public static final String EQUIP_AXIS= "m7_AXIS";
                // public static final String EQUIP_SERVO = "m7_SERVO";
                // public static final String EQUIP_ROBOT_TYPE = "m7_ROBOT_TYPE";
                // public static final String EQUIP_MAKER_NO = "m7_MAKER_NO";
                Map<String, String> revisionProperties = new HashMap<String, String>();
                revisionProperties.put(SDVPropertyConstant.ITEM_REVISION_ID, DEFAULT_REV_ID);
                revisionProperties.put(SDVPropertyConstant.ITEM_OBJECT_NAME, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_KOR_NAME_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_SHOP_CODE, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_SHOP_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_MAIN_CLASS, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_MAIN_CLASS_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_SUB_CLASS, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_SUB_CLASS_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_KOR, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_SPEC_KOR_COLUMN_INDEX));
                // �������� ���� �ʱ�ȭ ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.EQUIP_SPEC_KOR))) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_KOR, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_ENG, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_SPEC_ENG_COLUMN_INDEX));
                // �������� ���� �ʱ�ȭ ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.EQUIP_SPEC_ENG))) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_SPEC_ENG, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.EQUIP_CAPACITY, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_CAPACITY_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_MAKER, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_MAKER_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_NATION, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_NATION_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_INSTALL_YEAR, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_INSTALL_YEAR_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_KOR, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_PURPOSE_KOR_COLUMN_INDEX));
                // �������� ���� �ʱ�ȭ ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.EQUIP_PURPOSE_KOR))) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_KOR, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_PURPOSE_ENG_COLUMN_INDEX));
                // �������� ���� �ʱ�ȭ ("-")
                if (StringUtils.isEmpty(revisionProperties.get(SDVPropertyConstant.EQUIP_PURPOSE_ENG))) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG, EMPTY_DATA);
                }
                revisionProperties.put(SDVPropertyConstant.EQUIP_REV_DESC, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_REV_DESC_COLUMN_INDEX));
                revisionProperties.put(SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY, getEquipmentResourceCateGory(masterRowData));
                // JIG
                if (isJIG) {
                    revisionProperties.put(SDVPropertyConstant.EQUIP_VEHICLE_CODE, masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_JIG_VEHICLE_CODE_COLUMN_INDEX));
                }
                // ��ü JIG
                // revisionProperties.put(SDVPropertyConstant.EQUIP_STATION_CODE, masterRowData.get(0));
                // revisionProperties.put(SDVPropertyConstant.EQUIP_POSITION_CODE, masterRowData.get(0));
                // ����
                // revisionProperties.put(SDVPropertyConstant.EQUIP_LINE_CODE, masterRowData.get(0));

                datamap.put("revisionProperties", revisionProperties, IData.OBJECT_FIELD);
                // CAD File List
                RawDataMap fileDataMap = new RawDataMap();
                if (!StringUtils.isEmpty(masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_CAD_FILE_PATH_COLUMN_INDEX))) {
                    fileDataMap.put("isModified", true, IData.BOOLEAN_FIELD);
                    fileDataMap.put("CATPart", masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_CAD_FILE_PATH_COLUMN_INDEX), IData.STRING_FIELD);
                } else {
                    fileDataMap.put("isModified", false, IData.BOOLEAN_FIELD);
                }
                datamap.put("File", fileDataMap, IData.OBJECT_FIELD);

                CreateEquipmentItemService createEquipmentItemService = new CreateEquipmentItemService(datamap);
                return createEquipmentItemService.create().getItem();
            }

            /**
             * �Ϲݼ���, JIG���� Ȯ��
             * 
             * @method getEquipmentResourceCateGory
             * @date 2013. 12. 31.
             * @param
             * @return String
             * @exception
             * @throws
             * @see
             */
            private String getEquipmentResourceCateGory(ArrayList<String> masterRowData) throws Exception {
                if (StringUtils.isEmpty(masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_ITEM_ID_COLUMN_INDEX))) {
                    SkipException skipException = new SkipException("EQUIPMENT ITEM ID�� ���� ���� �ʽ��ϴ�.");
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
                Registry registry = Registry.getRegistry(CreateResourceViewPane.class);
                if (StringUtils.isEmpty(masterRowData.get(PEExcelConstants.EQUIPMENT_MASTER_JIG_VEHICLE_CODE_COLUMN_INDEX))) {
                    return registry.getString("Resource.Category.EXT");
                } else {
                    return registry.getString("Resource.Category.JIG");
                }
            }

            /**
             * �ڿ��Ҵ� ���� - EndItemData
             * 
             * @method executeEndItemData
             * @date 2013. 12. 10.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private String executeEndItemData(EndItemData endItemData) throws Exception {
                StringBuffer log = new StringBuffer();
                TCComponentBOMLine bopOperationBOMLine = ((OperationItemData) endItemData.getParentItem()).getBopBomLine();
                if (endItemData.getEndItemMBOMLine() == null) {
                    SkipException skipException = new SkipException("�Ҵ����� END-ITEM BOMLine������ �����ϴ�.");
                    skipException.setStatus(TCData.STATUS_ERROR);
                    throw skipException;
                }
                if (endItemData.getBopBomLine() == null) {
                    try {
                        ArrayList<InterfaceAIFComponent> endItemBOMLineList = new ArrayList<InterfaceAIFComponent>();
                        endItemBOMLineList.add(endItemData.getEndItemMBOMLine());
                        TCComponent[] resultBOMLineList = SDVBOPUtilities.connectObject(bopOperationBOMLine, endItemBOMLineList, null);
                        endItemData.setBopBomLine((TCComponentBOMLine) resultBOMLineList[0]);
                        endItemData.setBOMLineModifiable(true);
                    } catch (Exception e) {
                        throw new ExecuteSDVException(e.getMessage(), e);
                    }
                }
                if (endItemData.isBOMLineModifiable()) {
                    updateEndItemBOMLine(endItemData, log);
                }
                return log.toString();
            }

            /**
             * �Ϲ����� BOMLine ������ ������Ʈ�Ѵ�.
             * 
             * @method updateEndItemBOMLine
             * @date 2014. 1. 6.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            public void updateEndItemBOMLine(EndItemData endItemData, StringBuffer log) throws TCException {
                // SEQ No.�� ������ ��ȯ�� Find No.�� ��� (1 -> 001)
                endItemData.getBopBomLine().setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, endItemData.getFindNo());
                log.append("\t ~ �Ϲ�����(END-ITEM) �Ҵ� �Ϸ� - " + endItemData.getItemId());
            }

            /**
             * �ڿ��Ҵ� ���� - SubsidiaryData
             * 
             * @method executeSubsidiaryData
             * @date 2013. 12. 12.
             * @param
             * @return String
             * @exception
             * @throws
             * @see
             */
            @SuppressWarnings("unchecked")
            private String executeSubsidiaryData(SubsidiaryData subsidiaryData) throws Exception {
                StringBuffer log = new StringBuffer();
                TCComponentBOMLine bopOperationBOMLine = ((OperationItemData) subsidiaryData.getParentItem()).getBopBomLine();
                if (subsidiaryData.getBopBomLine() == null) {
                    try {
                        if (subsidiaryData.getResourceItem() == null) {
                            SkipException skipException = new SkipException("�Ҵ����� ������ Item�� �������� �ʽ��ϴ�.");
                            skipException.setStatus(TCData.STATUS_ERROR);
                            throw skipException;
                        }
                        ArrayList<InterfaceAIFComponent> toolDataList = new ArrayList<InterfaceAIFComponent>();
                        toolDataList.add(subsidiaryData.getResourceItem());
                        TCComponent[] resultBOMLineList = SDVBOPUtilities.connectObject(bopOperationBOMLine, toolDataList, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_SUBSIDIARY);
                        subsidiaryData.setBopBomLine((TCComponentBOMLine) resultBOMLineList[0]);
                        log.append("\t ~ ������(Subsidiary) �Ҵ� �Ϸ� - " + subsidiaryData.getItemId() + "\n");
                    } catch (Exception e) {
                        throw new ExecuteSDVException(e.getMessage(), e);
                    }
                }
                if (subsidiaryData.isBOMLineModifiable()) {
                    ArrayList<String> bomRowData = (ArrayList<String>) subsidiaryData.getData(PEExcelConstants.BOM);
                    // Qty = 1
                    subsidiaryData.getBopBomLine().setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
                    // �ҿ䷮ ���
                    subsidiaryData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY, bomRowData.get(PEExcelConstants.SUBSIDIARY_BOM_DEMAND_QUANTITY_COLUMN_INDEX));
                    // ������ Option condition ���
                    SDVBOPUtilities.updateAssiginOptionCondition(subsidiaryData.getBopBomLine(), subsidiaryData.getConversionOptionCondition());
                    // Find No. ���
                    subsidiaryData.getBopBomLine().setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, subsidiaryData.getFindNo());
                    // ������ ���
                    subsidiaryData.getBopBomLine().setProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT, bomRowData.get(PEExcelConstants.SUBSIDIARY_BOM_DEMAND_DIVIDE_GROUP_COLUMN_INDEX));
                    log.append("\t ~ ������(Subsidiary) BOMLine ������Ʈ �Ϸ� - " + subsidiaryData.getItemId());
                }
                return log.toString();
            }

            /**
             * �۾�ǥ�ؼ� SheetDatasetData ó��
             * 
             * @method executeSheetDatasetData
             * @date 2013. 12. 11.
             * @param
             * @return void
             * @exception
             * @throws
             * @see
             */
            private String executeSheetDatasetData(SheetDatasetData tcData) throws Exception {
                StringBuffer log = new StringBuffer();
                OperationItemData operationItemData = (OperationItemData) tcData.getParentItem();
                TCComponentBOMLine bopOperationBOMLine = operationItemData.getBopBomLine();
                TCComponentDataset korSheetDataset = null;
                Vector<File> uploadFiles = new Vector<File>();
                if (tcData instanceof SheetDatasetData) {
                    SheetDatasetData sheetDatasetData = (SheetDatasetData) tcData;
                    
//                    System.out.println("# ---------------------------------------------------------------------------");
//                    System.out.println("# operationItemData.getItemId()="+operationItemData.getItemId());
//                    System.out.println("# operationItemData.getRevId()="+operationItemData.getRevId());
//                    System.out.println("# operationItemData.isReleased() = "+operationItemData.isReleased());
//                    System.out.println("# operationItemData.isExistItem() = "+operationItemData.isExistItem());
//                    System.out.println("# sheetDatasetData.isIf() = "+sheetDatasetData.isIf());
                    boolean isReleased = false;
                    if(bopOperationBOMLine!=null){
                    	TCComponentReleaseStatus status=(TCComponentReleaseStatus)bopOperationBOMLine.getItemRevision().getRelatedComponent("release_status_list");
                    	if(status!=null){
                    		isReleased = true;
                    	}
                    	
                        // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
                    	BOPLineUtility.updateLineToOperationAbsOccId(bopOperationBOMLine);
                    	
                    }
                    
                    // �۾�ǥ�ؼ� I/F ����� ��� Update - �� �����ʵ� ������ ���� ������ �۾�ǥ�ؼ� ���
                    //if(operationItemData.isExistItem()==false && sheetDatasetData.isIf()==false){
                    if(isReleased==true || sheetDatasetData.isIf()==false){
                        SkipException skipException = new SkipException("�۾�ǥ�ؼ� I/F ����� �ƴմϴ�.");
                        skipException.setStatus(TCData.STATUS_SKIP);
                        throw skipException;
                    }
                    
                    // Dataset upload��� File
                    String uploadFilePath = (String) sheetDatasetData.getData();
                    if (StringUtils.isEmpty(uploadFilePath)) {
                        SkipException skipException = new SkipException("�۾�ǥ�ؼ� ���� ��ΰ� ���� Skip �մϴ�.");
                        skipException.setStatus(TCData.STATUS_ERROR);
                        throw skipException;
                    }
                    File uploadSheetExcelFile = ImportCoreService.getPathFile(uploadFilePath);
                    if (uploadSheetExcelFile == null) {
                        SkipException skipException = new SkipException("���ε� ��� �۾�ǥ�ؼ� ������ �������� �ʽ��ϴ�.");
                        skipException.setStatus(TCData.STATUS_ERROR);
                        throw skipException;
                    }
                    uploadFiles.add(uploadSheetExcelFile);
                    ArrayList<TCComponentDataset> korSheetDatastList = getKorSheetDatastList(bopOperationBOMLine);
                    if (korSheetDatastList.size() > 0) {
                        korSheetDataset = korSheetDatastList.get(0);
                        String oldDatasetName = korSheetDataset.toDisplayString();
                        // SYMTcUtil.removeAllNamedReference(korSheetDataset);
                        bopOperationBOMLine.getItemRevision().remove(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, korSheetDataset);
                        korSheetDataset.delete();
                        // SYMTcUtil.importFiles(korSheetDataset, uploadFiles);
                        bopOperationBOMLine.clearCache();
                        bopOperationBOMLine.refresh();
                        log.append("\t ~ ���� �۾�ǥ�ؼ� Dataset�� �����Ͽ����ϴ�. - " + oldDatasetName + "\n");
                    }
                    SDVBOPUtilities.createService(bopOperationBOMLine.getSession());
                    String datasetName = bopOperationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "/" + bopOperationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                    korSheetDataset = SDVBOPUtilities.createDataset(uploadFilePath);
                    // Dataset �̸� ����
                    korSheetDataset.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, datasetName);
                    bopOperationBOMLine.getItemRevision().add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, korSheetDataset);
                    // SYMTcUtil.importFiles(korSheetDataset, uploadFiles);
                    log.append("\t ~ �۾�ǥ�ؼ� Dataset�� ���Ͼ��ε带 �Ϸ��Ͽ����ϴ�. - " + korSheetDataset.toDisplayString() + "\n");
                }
                return log.toString();
            }

            /**
             * ���� �۾�ǥ�ؼ� Dataset�� �˻��Ѵ�.
             * 
             * @method getKorSheetDatastList
             * @date 2013. 12. 11.
             * @param
             * @return ArrayList<TCComponentDataset>
             * @exception
             * @throws
             * @see
             */
            private ArrayList<TCComponentDataset> getKorSheetDatastList(TCComponentBOMLine operationBOMLine) throws Exception {
                ArrayList<TCComponentDataset> korSheetDatastList = new ArrayList<TCComponentDataset>();
                TCComponent[] comps = operationBOMLine.getItemRevision().getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                if (comps != null) {
                    for (TCComponent comp : comps) {
                        if (comp instanceof TCComponentDataset) {
                            korSheetDatastList.add((TCComponentDataset) comp);
                        }
                    }
                }
                return korSheetDatastList;
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
                int rowStatus = TCData.STATUS_EXECUTE_COMPLETED;
                message = COMPLETED_MESSAGE;
                tcData.setStatus(rowStatus, message);
                logMessage = "{" + tcData.getText() + "} : " + tcData.getStatusMessage();
                return logMessage;
            }
        };

        // [NON-SR][2016.01.07] taeku.jeong PE->TC Interface ������ Down�Ǵ� ������ �ذ��� ���� Thread�� ��������� �Ҹ� �� �� �ִ¹������ ������ 
        //Thread aExecutionThread = new Thread(aExecutionRun);
        shell.getDisplay().syncExec(aExecutionRun);
        //aExecutionThread.stop();
        //aExecutionThread = null;
        aExecutionRun = null;
        
        if (exception.size() > 0) {
            // SKIP Exception �̸� ������ Throw���� �ʴ´�.
            if (!(exception.get(0) instanceof SkipException)) {
                throw exception.get(0);
            }
        }
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
        // Class Type log ���
        logMessage = "<" + tcData.getClassType() + ">\t\t\t" + logMessage;
        // Log ���� ó��..
        try {
            saveLog(logMessage);
        } catch (Exception e) {
            exception.add(new ExecuteSDVException(e.getMessage(), e));
        }
    }

    /**
     * �α� ó��..
     * 
     * @method saveLog
     * @date 2013. 12. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void saveLog(final TCData tcData, String logMessage) throws Exception {
        // Class Type log ���
        logMessage = "<" + tcData.getClassType() + ">\t\t\t" + logMessage;
        saveLog(logMessage);
    }

    /**
     * �α� ó��
     * 
     * @method saveLog
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void saveLog(String logMessage) throws Exception {
        // Log ���� ó��..
        try {
            //ImportCoreService.syncSetItemTextField(shell, getTcDataMigrationJob().getLogText(), logMessage);
        	getTcDataMigrationJob().getLogText().append(logMessage + "\n");
            ImportCoreService.saveLogFile(((PEIFTCDataExecuteJob) getTcDataMigrationJob()).getPeIFJobWork().getLogFilePath(), logMessage);
        } catch (Exception e) {
            throw e;
        }
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
        OperationItemData operationItemData = null;
        for (TCData tcData : expandAllItems) {
            if (tcData instanceof OperationItemData) {
                operationItemData = (OperationItemData) tcData;
            }
        }
        removeNotPeIfBOMLine(operationItemData);
    }

    /**
     * ����(Operation) ���� �Ҵ������� IF ��� ���� ������ �����Ѵ�.
     * 
     * @method removeNotPeIfBOMLine
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void removeNotPeIfBOMLine(OperationItemData operationItemData) throws Exception, TCException {
        HashMap<TCComponentBOMLine, TCComponentBOMLine> deleteBOMList = findNotIFBOMLines(operationItemData);
        if (deleteBOMList.size() == 0) {
            return;
        }
        // Log ���
        StringBuffer log = new StringBuffer();
        log.append("\n ��  I/F ��� ���� ���� �����׸� ����\n");
        TCComponentMfgBvrBOPLine[] deleteLines = deleteBOMList.keySet().toArray(new TCComponentMfgBvrBOPLine[deleteBOMList.size()]);
        int count = 0;
        for (TCComponentMfgBvrBOPLine bopBOMLine : deleteLines) {
            // I/F ���ܴ�� BOP BOMLine ����
            ArrayList<TCComponentBOMLine> deleteBopLines = new ArrayList<TCComponentBOMLine>();
            deleteBopLines.add(bopBOMLine);
            SDVBOPUtilities.disconnectObjects(bopBOMLine.parent(), deleteBopLines);
            log.append(++count + " : '" + bopBOMLine.toDisplayString() + "' is removed\n");
        }
        // Log ���
        saveLog(log.toString());
        // BOMLine Refresh
        refreshBOMLine(operationItemData.getBopBomLine());
    }

    /**
     * ����(Operation) ���� �Ҵ������� IF ��� ���� ������ �����Ѵ�. (���ʿ� �Ҵ��ڿ� ��ȸ)
     * 
     * @method findNotIFBOMLines
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private HashMap<TCComponentBOMLine, TCComponentBOMLine> findNotIFBOMLines(final OperationItemData operationItemData) throws Exception {
        final ArrayList<Exception> exception = new ArrayList<Exception>();
        final HashMap<TCComponentBOMLine, TCComponentBOMLine> notIFBOMLines = new HashMap<TCComponentBOMLine, TCComponentBOMLine>();
        Runnable operationMigratoinThread = new Runnable() {
        	public void run() {
                try {
                    TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(operationItemData.getBopBomLine());
                    for (TCComponentBOMLine tcComponentBOMLine : childs) {
                        notIFBOMLines.put(tcComponentBOMLine, tcComponentBOMLine);
                        TreeItem[] items = operationItemData.getItems();
                        for (TreeItem item : items) {
                            if (item instanceof OccurrenceData) {
                                // �����ϸ� ����
                                if (notIFBOMLines.containsKey((((OccurrenceData) item).getBopBomLine()))) {
                                    notIFBOMLines.remove(((OccurrenceData) item).getBopBomLine());
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    exception.add(e);
                }
        	}
        };
        
        //Thread aThread = new Thread(operationMigratoinThread);
        shell.getDisplay().syncExec(operationMigratoinThread);
        //aThread.stop();
        //aThread = null;
        operationMigratoinThread = null;
        
//        shell.getDisplay().syncExec(new Runnable() {
//            public void run() {
//                try {
//                    TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(operationItemData.getBopBomLine());
//                    for (TCComponentBOMLine tcComponentBOMLine : childs) {
//                        notIFBOMLines.put(tcComponentBOMLine, tcComponentBOMLine);
//                        TreeItem[] items = operationItemData.getItems();
//                        for (TreeItem item : items) {
//                            if (item instanceof OccurrenceData) {
//                                // �����ϸ� ����
//                                if (notIFBOMLines.containsKey((((OccurrenceData) item).getBopBomLine()))) {
//                                    notIFBOMLines.remove(((OccurrenceData) item).getBopBomLine());
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    exception.add(e);
//                }
//            }
//        });
        
        if (exception.size() > 0) {
            // SKIP Exception �̸� ������ Throw���� �ʴ´�.
            if (!(exception.get(0) instanceof SkipException)) {
                throw exception.get(0);
            }
        }
        return notIFBOMLines;
    }

    /**
     * BOMLine refresh
     * 
     * @method refreshBOMLine
     * @date 2013. 12. 12.
     * @param
     * @exception
     * @return void
     * @throws
     * @see
     */
    private void refreshBOMLine(Object tcComponent) throws Exception {
        // Activity Refresh
        if (tcComponent instanceof TCComponentMECfgLine) {
            TCComponentMECfgLine tcComponentMECfgLine = (TCComponentMECfgLine) tcComponent;
            tcComponentMECfgLine.clearCache();
            tcComponentMECfgLine.window().fireChangeEvent();
            tcComponentMECfgLine.refresh();
        } else if (tcComponent instanceof TCComponentCfgAttachmentLine) {
            TCComponentCfgAttachmentLine tcComponentCfgAttachmentLine = (TCComponentCfgAttachmentLine) tcComponent;
            tcComponentCfgAttachmentLine.clearCache();
            tcComponentCfgAttachmentLine.window().fireChangeEvent();
            tcComponentCfgAttachmentLine.refresh();
        }
        // BOMLine Refresh
        else if (tcComponent instanceof TCComponentBOMLine) {
            TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) tcComponent;
            tcComponentBOMLine.clearCache();
            tcComponentBOMLine.refresh();
            tcComponentBOMLine.window().newIrfWhereConfigured(tcComponentBOMLine.getItemRevision());
            tcComponentBOMLine.window().fireComponentChangeEvent();
        }
    }

    /**
     * Activity Window ����
     * 
     * @method createActivityWindow
     * @date 2013. 12. 31.
     * @param
     * @return TCComponentCfgAttachmentWindow
     * @exception
     * @throws
     * @see
     */
    private TCComponentCfgAttachmentWindow createActivityWindow(TCComponentBOMLine operationBOMLine) throws Exception {
        TCComponentBOMWindow bopBOMWindow = operationBOMLine.window();
        TCComponentCfgAttachmentWindow attachmentWindow = null;
        TCComponentRevisionRule localTCComponentRevisionRule = (TCComponentRevisionRule) (bopBOMWindow).getReferenceProperty("revision_rule");
        TCComponentCfgAttachmentWindowType attachmentWindowType = ((TCComponentCfgAttachmentWindowType) CommonUtils.getSession().getTypeComponent("CfgAttachmentWindow"));
        attachmentWindow = attachmentWindowType.createAttachmentWindow(localTCComponentRevisionRule, bopBOMWindow, true);
        TCPreferenceService localObject2 = CommonUtils.getSession().getPreferenceService();
        TCProperty localObject3 = attachmentWindow.getTCProperty("me_cfg_icm_mode");
        // ((TCProperty) localObject3).setLogicalValue(((TCPreferenceService) localObject2).isTrue(4, "Incremental_Change_Management"));
        Boolean flag = ((TCPreferenceService) localObject2).getLogicalValueAtLocation("Incremental_Change_Management", TCPreferenceLocation.OVERLAY_LOCATION);
        if(flag == null) {
        	flag = false;
        }
        
        ((TCProperty) localObject3).setLogicalValue(flag);
        
        return attachmentWindow;
    }

}
