/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.work.export.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.migration.ImportCoreService;
import com.symc.plm.me.sdv.service.migration.ImportExcelServce;
import com.symc.plm.me.sdv.service.migration.util.PEExcelConstants;
import com.symc.plm.me.sdv.service.migration.work.peif.PEIFJobWork;
import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrProcess;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

/**
 * [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
 * 
 * Class Name : ExportProgressDialog
 * Class Description :
 * 
 * @date 2013. 12. 24.
 * 
 */
public class ExportProgressDialog extends ProgressBarDialog {
    private TCComponentMfgBvrProcess processLine;
    TCComponentBOMLine[] operations;
    String exportFolderPath;
    TCComponentBOMWindow mBOMWindow;
    // Operation
    ArrayList<ArrayList<String>> operationRowBOMList;
    ArrayList<ArrayList<String>> operationRowMasterList;
    ArrayList<TCComponentBOMLine> operationBOMLineList;
    // Activity
    ArrayList<ArrayList<String>> activityRowBOMList;
    ArrayList<ArrayList<String>> activityRowMasterList;
    // EndItem
    ArrayList<ArrayList<String>> endItemRowBOMList;
    // Subsidiary
    ArrayList<ArrayList<String>> subsidiaryRowBOMList;
    // Tool
    ArrayList<ArrayList<String>> toolRowBOMList;
    ArrayList<ArrayList<String>> toolRowMasterList;
    // Equipment
    ArrayList<ArrayList<String>> equipmentRowBOMList;
    ArrayList<ArrayList<String>> equipmentRowMasterList;
    // PW WorkSheet Path
//    private static final String PE_WORKSHEET_PATH = "Z:\\TcM_Interface\\WorkSheet";
    private static final String PE_WORKSHEET_PATH = "X:\\TcM_Interface\\WorkSheet";
    // Excel ���� Ȯ����
    private static final String EXCEL_FILE_EXT = ".xlsx";

    /**
     * @param parent
     */
    public ExportProgressDialog(Shell parent, TCComponentMfgBvrProcess processLine, String exportFolderPath) {
        super(parent);
        this.processLine = processLine;
        this.exportFolderPath = exportFolderPath;
        // Export Folder üũ �� ����
        checkExportFolderPath(exportFolderPath);
    }

    /**
     * Export Excel ������ �ʱ�ȭ
     * 
     * @method initExcelExport
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void initExport() throws Exception {
        // Line ���� ���� ����Ʈ
        operations = getChildBOMLines(processLine);
        operationRowBOMList = new ArrayList<ArrayList<String>>();
        operationBOMLineList = new ArrayList<TCComponentBOMLine>();
        operationRowMasterList = new ArrayList<ArrayList<String>>();
        activityRowBOMList = new ArrayList<ArrayList<String>>();
        activityRowMasterList = new ArrayList<ArrayList<String>>();
        endItemRowBOMList = new ArrayList<ArrayList<String>>();
        subsidiaryRowBOMList = new ArrayList<ArrayList<String>>();
        toolRowBOMList = new ArrayList<ArrayList<String>>();
        toolRowMasterList = new ArrayList<ArrayList<String>>();
        equipmentRowBOMList = new ArrayList<ArrayList<String>>();
        equipmentRowMasterList = new ArrayList<ArrayList<String>>();
        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // ���� BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();
        // M Product ������
        // [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
        mBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(bomWindow.getTopBOMLine().getItemRevision());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.work.export.ui.dialog.ProgressBarDialog#initGuage()
     */
    @Override
    public void initGuage() {
        try {
            initExport();
            this.setExecuteTime(operations.length);
            this.setMayCancel(true);
            this.setProcessMessage("please waiting....");
            this.setShellTitle("BOP Excel Export");
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "Export", MessageBox.ERROR);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.service.migration.work.export.ui.dialog.ProgressBarDialog#process(int)
     */
    @Override
    protected String process(int times) {
        String message = "";
        try {
            message = printMessage(times);
            exportRowOperation(operations[times - 1]);
        } catch (Exception e) {
            e.printStackTrace();
            if (mBOMWindow != null) {
                try {
                    mBOMWindow.close();
                } catch (TCException te) {
                    te.printStackTrace();
                }
            }
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "Export", MessageBox.ERROR);
            // process �ߴ�
            isClosed = true;
        } finally {

        }
        return message;
    }

    /**
     * 
     * @method exportRowOperation
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void exportRowOperation(TCComponentBOMLine operationBOMLine) throws Exception {
        // Operation
        // BOM
        ArrayList<String> operationBOMInfo = getOperationBOMInfo(operationBOMLine);
        operationRowBOMList.add(operationBOMInfo);
        operationBOMLineList.add(operationBOMLine);
        // Master
        operationRowMasterList.add(getOperationMasterInfo(operationBOMLine, operationBOMInfo));
        // Activity
        setActivity(operationBOMLine, operationBOMInfo);
        // ���� ���� OCCURRENCE BOMLine ��ȸ
        TCComponentBOMLine[] childUnduerOperationBOMLines = getChildBOMLines(operationBOMLine);
        // EndItem
        setEndItem(operationBOMLine, operationBOMInfo, childUnduerOperationBOMLines);
        // Subsidiary
        setSubsidiary(operationBOMLine, operationBOMInfo, childUnduerOperationBOMLines);
        // Tool
        setTool(operationBOMLine, operationBOMInfo, childUnduerOperationBOMLines);
        // Equipment
        setEquipment(operationBOMLine, operationBOMInfo, childUnduerOperationBOMLines);
    }

    /**
     * Operation BOM
     * 
     * @method getOperationBOMInfo
     * @date 2013. 12. 24.
     * @param
     * @return ArrayList<String>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<String> getOperationBOMInfo(TCComponentBOMLine operationBOMLine) throws Exception {
        ArrayList<String> bomInfo = new ArrayList<String>();
        String lineItemId = processLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String operationItemId = operationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String[] splitIds = operationItemId.split("-");
        bomInfo.add("");// 0 : empty
        bomInfo.add(getPlantCode(lineItemId)); // 1 : Plant
        bomInfo.add(getShopCode(lineItemId)); // 2 : Shop Code
        bomInfo.add(getProductNo(lineItemId)); // 3 : Product No.
        bomInfo.add(getLineCode(lineItemId)); // 4 : Line Code
        // (6)-(7)-(8)-(8)-(5,9)
        bomInfo.add(splitIds[4]); // 5 : ����������
        bomInfo.add(splitIds[0]); // 6 : ������ȣ-����
        bomInfo.add(splitIds[1]); // 7 : ������ȣ-����
        bomInfo.add(splitIds[2] + "-" + splitIds[3]); // 8 : ������ȣ
        bomInfo.add(splitIds[4]); // 9 : ����������
        String peCondition = ImportCoreService.conversionOptionConditionFormTC(operationBOMLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
        bomInfo.add(peCondition); // 10 : OPTION
        return bomInfo;
    }

    /**
     * Operation Master
     * 
     * @method setOperationMasterInfo
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private ArrayList<String> getOperationMasterInfo(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo) throws Exception {
        ArrayList<String> masterInfo = new ArrayList<String>();
        masterInfo.add(""); // 0 : empty
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 1 : ������ȣ-����
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 2 : ������ȣ-����
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 3 : ������ȣ
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 4 : ����������
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKAREA));// 5 : �۾���ġ
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));// 6 : ������-����
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));// 7 : ������-����
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE));// 8 : �۾��ڱ����ڵ�
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_ITEM_UL));// 9 : ����������ġ-����
        String[] installDwgNoList = operationBOMLine.getItemRevision().getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).getStringArrayValue();
        String installDwgNo = "";
        if (installDwgNoList != null) {
            for (int i = 0; i < installDwgNoList.length; i++) {
                installDwgNo = installDwgNo + ((i == 0) ? installDwgNoList[i] : "/" + installDwgNoList[i]);
            }
        }
        masterInfo.add(installDwgNo);// 10 : ���������ȣ
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));// 11 : Station No.
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR));// 12 : ���� (DR1,DR2,DR3)
        masterInfo.add(operationBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM));// 13 : �ý���
        masterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 14 : ������ȣ
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ));// 15 : Sequence
        masterInfo.add(operationBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_WORK_UBODY));// 16 : ��ü�۾� ���� (N/Y)
        boolean repVhicleCheck = operationBOMLine.getItem().getTCProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK).getBoolValue();
        masterInfo.add((repVhicleCheck == true) ? "Y" : "N");// 17 : ��ǥ���� ���� (N/Y)
        masterInfo.add(PE_WORKSHEET_PATH + "\\" + operationBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID) + ".xlsx");// 18 : �����۾�ǥ�ؼ� ���ϰ��
        masterInfo.add("FALSE");// 19 : �����۾�ǥ�ؼ� I/F ����
        return masterInfo;
    }

    /**
     * 
     * @method setActivity
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setActivity(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo) throws Exception {
        List<HashMap<String, Object>> activityList = SYMTcUtil.getActivityList((TCComponentMfgBvrOperation) operationBOMLine);
        if (activityList == null || activityList.size() == 0) {
            return;
        }
        for (HashMap<String, Object> activity : activityList) {
            // Activity BOM
            ArrayList<String> activityBomInfo = new ArrayList<String>();
            activityBomInfo.add("");
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : ����������
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : ������ȣ-����
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : ������ȣ-����
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : ������ȣ
            activityBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : ����������
            activityBomInfo.add((String) activity.get("SEQ")); // 10 : �۾�����
            activityRowBOMList.add(activityBomInfo);
            // Activity MASTER
            ArrayList<String> activityMasterInfo = new ArrayList<String>();
            activityMasterInfo.add(""); // 0 : empty
            activityMasterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 1 : ������ȣ-����
            activityMasterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 2 : ������ȣ-����
            activityMasterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 3 : ������ȣ
            activityMasterInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 4 : ����������
            activityMasterInfo.add((String) activity.get("SEQ"));// 5 : �۾�����
            String activitysystemCode = (String) activity.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
            String activityMainSystemCode = "";
            String activitySubSystemCode = "";
            if (!StringUtils.isEmpty(activitysystemCode)) {
                String[] activitySplits = activitysystemCode.split("-");
                activityMainSystemCode = activitySplits[0].trim();
                if (activitySplits.length == 2) {
                    activitySubSystemCode = activitySplits[1].trim();
                } else if (activitySplits.length == 3) {
                    activitySubSystemCode = activitySplits[1].trim() + "," + activitySplits[2].trim();
                }
            }
            activityMasterInfo.add(activityMainSystemCode); // 6 : �۾����
            activityMasterInfo.add(activitySubSystemCode); // 7 : ����
            activityMasterInfo.add(activity.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).toString()); // 8 : ���̵�
            activityMasterInfo.add((String) activity.get(SDVPropertyConstant.ITEM_OBJECT_NAME)); // 9 : �۾�����(����)
            activityMasterInfo.add((String) activity.get(SDVPropertyConstant.ACTIVITY_ENG_NAME)); // 10 : �۾�����(����)
            activityMasterInfo.add(activity.get(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).toString()); // 11 : �۾��ð�
            String activitySystemCategory = (String) activity.get(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
            // '�۾�������' -> '����'�� ��ȯ (������ �ڵ�/������ TC - PE�� ����)
            if ("�۾�������".equals(activitySystemCategory)) {
                activitySystemCategory = "����";
            }
            activityMasterInfo.add(activitySystemCategory); // 12 : �ڵ�/����/����
            TCComponentBOMLine[] tools = (TCComponentBOMLine[]) activity.get(SDVPropertyConstant.ACTIVITY_TOOL_LIST);
            String tooIds = "";
            if (tools != null) {
                for (TCComponentBOMLine tcComponentBOMLine : tools) {
                    if ("".equals(tooIds)) {
                        tooIds = tcComponentBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    } else {
                        tooIds += "," + tcComponentBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    }
                }
            }
            activityMasterInfo.add(tooIds); // 13 : ����ID
            activityMasterInfo.add((String) activity.get(SDVPropertyConstant.ACTIVITY_CONTROL_POINT)); // 14 : KPC
            activityMasterInfo.add((String) activity.get(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS)); // 15 : KPC��������
            activityRowMasterList.add(activityMasterInfo);
        }
    }

    /**
     * 
     * @method setEndItem
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setEndItem(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo, TCComponentBOMLine[] childUnduerOperationBOMLines) throws Exception {
        for (TCComponentBOMLine childBOMLine : childUnduerOperationBOMLines) {
            if (SDVTypeConstant.EBOM_STD_PART.equals(childBOMLine.getItem().getType()) || SDVTypeConstant.EBOM_VEH_PART.equals(childBOMLine.getItem().getType())) {
                ArrayList<String> endItemBomInfo = new ArrayList<String>();
                endItemBomInfo.add("");
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : ����������
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : ������ȣ-����
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : ������ȣ-����
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : ������ȣ
                endItemBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : ����������
                endItemBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));// 10 : Part No.
                StringBuffer eBomAbsOoccUid = new StringBuffer();
                String eBomOccUid = "";
                String eBomFunctionItemId = "";
                TCComponentBOMLine[] findBOPEndItemBOMLineList = SDVBOPUtilities.getAssignSrcBomLineList(mBOMWindow, childBOMLine);
                // E-BOM ABS OCC PUID�� �˾Ƴ���.
                if (findBOPEndItemBOMLineList.length > 0) {
                    eBomOccUid = findBOPEndItemBOMLineList[0].getProperty("bl_occurrence_uid");
                    eBomAbsOoccUid.append(eBomOccUid);
                    TCComponentBOMLine parentBOMLine = findBOPEndItemBOMLineList[0].parent();
                    while (true) {
                        if (parentBOMLine == null) {
                            // clear
                            eBomAbsOoccUid.delete(0, eBomAbsOoccUid.length());
                            break;
                        }
                        // E-BOMfunction Item�� �˾Ƴ���.
                        if (SDVTypeConstant.EBOM_FUNCTION.equals(parentBOMLine.getItem().getType())) {
                            eBomFunctionItemId = parentBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                            break;
                        }
                        String parentOccUid = parentBOMLine.getProperty("bl_occurrence_uid");
                        eBomAbsOoccUid.insert(0, "+");
                        eBomAbsOoccUid.insert(0, parentOccUid);
                        // set parent
                        parentBOMLine = parentBOMLine.parent();
                    }
                }
                endItemBomInfo.add(eBomAbsOoccUid.toString()); // 11 : EBOM ABS Occurrence PUID
                endItemBomInfo.add(eBomOccUid);// 12 : EBOM Occurrence PUID
                endItemBomInfo.add(eBomFunctionItemId); // 13 : Function Part Number
                endItemBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));// 14 : ����SEQ
                endItemRowBOMList.add(endItemBomInfo);
            }
        }
    }

    /**
     * 
     * @method setSubsidiary
     * @date 2013. 12. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setSubsidiary(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo, TCComponentBOMLine[] childUnduerOperationBOMLines) throws Exception {
        for (TCComponentBOMLine childBOMLine : childUnduerOperationBOMLines) {
            if (SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equals(childBOMLine.getItem().getType())) {
                ArrayList<String> subsidiaryBomInfo = new ArrayList<String>();
                subsidiaryBomInfo.add("");
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : ����������
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : ������ȣ-����
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : ������ȣ-����
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : ������ȣ
                subsidiaryBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : ����������
                subsidiaryBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));// 10 : ��ǰ��ȣ
                String peCondition = ImportCoreService.conversionOptionConditionFormTC(childBOMLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
                subsidiaryBomInfo.add(peCondition);// 11 : OPTION
                subsidiaryBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY).toString());// 12 : �ҿ䷮
                subsidiaryBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT));// 13 : ������
                subsidiaryBomInfo.add(ImportCoreService.conversionSubsidiaryFindNoToTc(childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO)));// 14 : ����SEQ
                subsidiaryRowBOMList.add(subsidiaryBomInfo);
            }
        }
    }

    /**
     * 
     * @method setTool
     * @date 2013. 12. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setTool(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo, TCComponentBOMLine[] childUnduerOperationBOMLines) throws Exception {
        for (TCComponentBOMLine childBOMLine : childUnduerOperationBOMLines) {
            if (SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(childBOMLine.getItem().getType())) {
                // BOM
                ArrayList<String> toolBomInfo = new ArrayList<String>();
                toolBomInfo.add("");
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : ����������
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : ������ȣ-����
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : ������ȣ-����
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : ������ȣ
                toolBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : ����������
                toolBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));// 10 : ������ȣ
                toolBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY).toString());// 11 : ����
                // 12 : Torque = TorqueType + TorqueValue
                String torque = "";
                String torqueType = BundleUtil.nullToString(childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE));
                String torqueValue = BundleUtil.nullToString(childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE));
                torque = torqueType + " " + torqueValue;
                torque = torque.trim();
                toolBomInfo.add(torque);// 12 : Torque
                toolBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));// 13 : ����SEQ
                toolRowBOMList.add(toolBomInfo);
                // MASTER
                // TODO : MASTER ����...
            }
        }
    }

    /**
     * 
     * @method setEquipment
     * @date 2013. 12. 25.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setEquipment(TCComponentBOMLine operationBOMLine, ArrayList<String> operationBOMInfo, TCComponentBOMLine[] childUnduerOperationBOMLines) throws Exception {
        for (TCComponentBOMLine childBOMLine : childUnduerOperationBOMLines) {
            if (SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM.equals(childBOMLine.getItem().getType()) || SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(childBOMLine.getItem().getType())) {
                // BOM
                ArrayList<String> equipmentBomInfo = new ArrayList<String>();
                equipmentBomInfo.add("");
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANT_CODE_COLUMN_INDEX)); // 1 : Plant
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_CODE_COLUMN_INDEX)); // 2 : Shop Code
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PRODUCT_NO_COLUMN_INDEX));// 3 : Product No.
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_LINE_CODE_COLUMN_INDEX));// 4 : Line Code
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 5 : ����������
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PROJECT_NO_COLUMN_INDEX));// 6 : ������ȣ-����
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX));// 7 : ������ȣ-����
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_SHEET_NO_COLUMN_INDEX));// 8 : ������ȣ
                equipmentBomInfo.add(operationBOMInfo.get(PEExcelConstants.COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX));// 9 : ����������
                equipmentBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));// 10 : �����ȣ
                equipmentBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY).toString());// 11 : ���� ����
                equipmentBomInfo.add(childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));// 12 : SEQ
                equipmentRowBOMList.add(equipmentBomInfo);
                // MASTER
                // TODO : MASTER ����...
            }
        }
    }

    /**
     * 
     * @method printMessage
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String printMessage(int times) throws Exception {
        String message = "[" + times + "/" + operations.length + "] ";
        message += operations[times - 1].getProperty(SDVPropertyConstant.BL_ITEM_ID) + "\t" + operations[times - 1].getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
        return message;
    }

    /**
     * 
     * @method createExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createExcel() throws Exception {
        createOperationBOMExcel();
        createOperationMasterExcel();
        createActivityBOMExcel();
        createActivityMasterExcel();
        createEndItemBOMExcel();
        createSubsidiaryBOMExcel();
        createToolBOMExcel();
        createToolMasterExcel();
        createEquipmentBOMExcel();
        createEquipmentMasterExcel();
    }

    /**
     * 
     * @method createOperationBOMExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createOperationBOMExcel() throws Exception {
        ArrayList<String> operationBOMHeader = new ArrayList<String>();
        operationBOMHeader.add("");
        operationBOMHeader.add("Plant");
        operationBOMHeader.add("Shop Code");
        operationBOMHeader.add("Product No.");
        operationBOMHeader.add("Line Code.");
        operationBOMHeader.add("����������");
        operationBOMHeader.add("������ȣ-����");
        operationBOMHeader.add("������ȣ-����");
        operationBOMHeader.add("������ȣ");
        operationBOMHeader.add("����������\n");
        operationBOMHeader.add("OPTION");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_OPERATION[2] + EXCEL_FILE_EXT, operationBOMHeader, operationRowBOMList);
    }

    /**
     * 
     * @method createOperationMasterExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createOperationMasterExcel() throws Exception {
        ArrayList<String> operationMasterHeader = new ArrayList<String>();
        operationMasterHeader.add("");
        operationMasterHeader.add("������ȣ-����");
        operationMasterHeader.add("������ȣ-����");
        operationMasterHeader.add("������ȣ");
        operationMasterHeader.add("����������");
        operationMasterHeader.add("�۾���ġ");
        operationMasterHeader.add("������-����");
        operationMasterHeader.add("������-����");
        operationMasterHeader.add("�۾��ڱ����ڵ�");
        operationMasterHeader.add("����������ġ-����");
        operationMasterHeader.add("���������ȣ");
        operationMasterHeader.add("Station No.");
        operationMasterHeader.add("���� (DR1,DR2,DR3)");
        operationMasterHeader.add("�ý���");
        operationMasterHeader.add("������ȣ");
        operationMasterHeader.add("Sequence");
        operationMasterHeader.add("��ü�۾� ���� (N/Y)");
        operationMasterHeader.add("��ǥ���� ���� (N/Y)");
        operationMasterHeader.add("�����۾�ǥ�ؼ� ���ϰ��");
        operationMasterHeader.add("�����۾�ǥ�ؼ� I/F ����");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.MASTER + "\\" + PEIFJobWork.TC_TYPE_OPERATION[1] + EXCEL_FILE_EXT, operationMasterHeader, operationRowMasterList);
    }

    /**
     * 
     * @method createActivityBOMExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createActivityBOMExcel() throws Exception {
        ArrayList<String> activityBOMHeader = new ArrayList<String>();
        activityBOMHeader.add("");
        activityBOMHeader.add("Plant");
        activityBOMHeader.add("Shop Code");
        activityBOMHeader.add("Product No.");
        activityBOMHeader.add("Line Code");
        activityBOMHeader.add("����������");
        activityBOMHeader.add("������ȣ-����");
        activityBOMHeader.add("������ȣ-����");
        activityBOMHeader.add("������ȣ");
        activityBOMHeader.add("����������");
        activityBOMHeader.add("�۾�����");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_ACTIVITY[2] + EXCEL_FILE_EXT, activityBOMHeader, activityRowBOMList);
    }

    /**
     * 
     * @method createActivityMasterExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createActivityMasterExcel() throws Exception {
        ArrayList<String> activityMasterHeader = new ArrayList<String>();
        activityMasterHeader.add("");
        activityMasterHeader.add("������ȣ-����");
        activityMasterHeader.add("������ȣ-����");
        activityMasterHeader.add("������ȣ");
        activityMasterHeader.add("����������");
        activityMasterHeader.add("�۾�����");
        activityMasterHeader.add("�۾����");
        activityMasterHeader.add("����");
        activityMasterHeader.add("���̵�");
        activityMasterHeader.add("�۾�����(����)");
        activityMasterHeader.add("�۾�����(����)");
        activityMasterHeader.add("�۾��ð�");
        activityMasterHeader.add("�ڵ�/����/����");
        activityMasterHeader.add("����ID");
        activityMasterHeader.add("KPC");
        activityMasterHeader.add("KPC��������");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.MASTER + "\\" + PEIFJobWork.TC_TYPE_ACTIVITY[1] + EXCEL_FILE_EXT, activityMasterHeader, activityRowMasterList);
    }

    /**
     * 
     * @method createEndItemBOMExcel
     * @date 2013. 12. 24.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createEndItemBOMExcel() throws Exception {
        ArrayList<String> endItemBOMHeader = new ArrayList<String>();
        endItemBOMHeader.add("");
        endItemBOMHeader.add("Plant");
        endItemBOMHeader.add("Shop Code");
        endItemBOMHeader.add("Product No.");
        endItemBOMHeader.add("Line Code");
        endItemBOMHeader.add("����������");
        endItemBOMHeader.add("������ȣ-����");
        endItemBOMHeader.add("������ȣ-����");
        endItemBOMHeader.add("������ȣ");
        endItemBOMHeader.add("����������");
        endItemBOMHeader.add("Part NO.");
        endItemBOMHeader.add("EBOM ABS Occurrence PUID");
        endItemBOMHeader.add("EBOM Occurrence PUID");
        endItemBOMHeader.add("Function Part Number");
        endItemBOMHeader.add("����SEQ");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_END_ITEM[2] + EXCEL_FILE_EXT, endItemBOMHeader, endItemRowBOMList);
    }

    /**
     * 
     * @method createSubsidiaryBOMExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createSubsidiaryBOMExcel() throws Exception {
        ArrayList<String> subsidiaryBOMHeader = new ArrayList<String>();
        subsidiaryBOMHeader.add("");
        subsidiaryBOMHeader.add("Plant");
        subsidiaryBOMHeader.add("Shop Code");
        subsidiaryBOMHeader.add("Product No.");
        subsidiaryBOMHeader.add("Line Code");
        subsidiaryBOMHeader.add("����������");
        subsidiaryBOMHeader.add("������ȣ-����");
        subsidiaryBOMHeader.add("������ȣ-����");
        subsidiaryBOMHeader.add("������ȣ");
        subsidiaryBOMHeader.add("����������");
        subsidiaryBOMHeader.add("��ǰ��ȣ");
        subsidiaryBOMHeader.add("OPTION");
        subsidiaryBOMHeader.add("�ҿ䷮");
        subsidiaryBOMHeader.add("������");
        subsidiaryBOMHeader.add("����SEQ");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_SUBSIDIARY[2] + EXCEL_FILE_EXT, subsidiaryBOMHeader, subsidiaryRowBOMList);
    }

    /**
     * 
     * @method createToolBOMExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createToolBOMExcel() throws Exception {
        ArrayList<String> toolBOMHeader = new ArrayList<String>();
        toolBOMHeader.add("");
        toolBOMHeader.add("Plant");
        toolBOMHeader.add("Shop Code");
        toolBOMHeader.add("Product No.");
        toolBOMHeader.add("Line Code");
        toolBOMHeader.add("����������");
        toolBOMHeader.add("������ȣ-����");
        toolBOMHeader.add("������ȣ-����");
        toolBOMHeader.add("������ȣ");
        toolBOMHeader.add("����������");
        toolBOMHeader.add("������ȣ");
        toolBOMHeader.add("����");
        toolBOMHeader.add("Torque");
        toolBOMHeader.add("����SEQ");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_TOOL[2] + EXCEL_FILE_EXT, toolBOMHeader, toolRowBOMList);
    }

    /**
     * 
     * @method createToolMasterExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createToolMasterExcel() throws Exception {
        ArrayList<String> toolMasterHeader = new ArrayList<String>();
        toolMasterHeader.add("");
        toolMasterHeader.add("������ȣ");
        toolMasterHeader.add("������-����");
        toolMasterHeader.add("������-����");
        toolMasterHeader.add("��з�");
        toolMasterHeader.add("�ߺз�");
        toolMasterHeader.add("���� �뵵");
        toolMasterHeader.add("����ڵ�");
        toolMasterHeader.add("��� ���-����");
        toolMasterHeader.add("��� ���-����");
        toolMasterHeader.add("�ҿ䷮ ����");
        toolMasterHeader.add("���� ����");
        toolMasterHeader.add("��ũ��");
        toolMasterHeader.add("���ۻ�");
        toolMasterHeader.add("��ü/AF");
        toolMasterHeader.add("����з�");
        toolMasterHeader.add("����");
        toolMasterHeader.add("����� Size");
        toolMasterHeader.add("�ڼ����Կ���");
        toolMasterHeader.add("Remark");
        toolMasterHeader.add("CAD���ϰ��");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.MASTER + "\\" + PEIFJobWork.TC_TYPE_TOOL[1] + EXCEL_FILE_EXT, toolMasterHeader, toolRowMasterList);
    }

    /**
     * 
     * @method createEquipmentBOMExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createEquipmentBOMExcel() throws Exception {
        ArrayList<String> equipmentBOMHeader = new ArrayList<String>();
        equipmentBOMHeader.add("");
        equipmentBOMHeader.add("Plant");
        equipmentBOMHeader.add("Shop Code");
        equipmentBOMHeader.add("Product No.");
        equipmentBOMHeader.add("Line Code");
        equipmentBOMHeader.add("����������");
        equipmentBOMHeader.add("������ȣ-����");
        equipmentBOMHeader.add("������ȣ-����");
        equipmentBOMHeader.add("������ȣ");
        equipmentBOMHeader.add("����������");
        equipmentBOMHeader.add("�����ȣ");
        equipmentBOMHeader.add("���� ����");
        equipmentBOMHeader.add("SEQ");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.BOM + "\\" + PEIFJobWork.TC_TYPE_EQUIPMENT[2] + EXCEL_FILE_EXT, equipmentBOMHeader, equipmentRowBOMList);
    }

    /**
     * 
     * @method createEquipmentMasterExcel
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createEquipmentMasterExcel() throws Exception {
        ArrayList<String> equipmentMasterHeader = new ArrayList<String>();
        equipmentMasterHeader.add("");
        equipmentMasterHeader.add("����");
        equipmentMasterHeader.add("�����ȣ");
        equipmentMasterHeader.add("��з�");
        equipmentMasterHeader.add("�ߺз�");
        equipmentMasterHeader.add("���� ���-����");
        equipmentMasterHeader.add("���� ���-����");
        equipmentMasterHeader.add("Main Name(����)");
        equipmentMasterHeader.add("Main Name(����)");
        equipmentMasterHeader.add("ó���ɷ�");
        equipmentMasterHeader.add("���ۻ�");
        equipmentMasterHeader.add("���Ա���");
        equipmentMasterHeader.add("��ġ�⵵");
        equipmentMasterHeader.add("��� �뵵-����");
        equipmentMasterHeader.add("��� �뵵-����");
        equipmentMasterHeader.add("���泻������");
        equipmentMasterHeader.add("�����ڵ�");
        equipmentMasterHeader.add("CAD���ϰ��");
        ImportExcelServce.writeExportExcel(exportFolderPath + "\\" + PEExcelConstants.MASTER + "\\" + PEIFJobWork.TC_TYPE_EQUIPMENT[1] + EXCEL_FILE_EXT, equipmentMasterHeader, equipmentRowMasterList);
    }

    /**
     * ���� ó��
     */
    @Override
    protected void doAfter() {
        try {
            createExcel();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), e.getMessage(), "Export", MessageBox.ERROR);
            // process �ߴ�
            isClosed = true;
        } finally {
            if (mBOMWindow != null) {
                try {
                    mBOMWindow.close();
                } catch (TCException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Cancel Cleanup
     * 
     * @see com.symc.plm.me.sdv.service.migration.work.export.ui.dialog.ProgressBarDialog#cleanUp()
     */
    @Override
    protected void cleanUp() {
        if (mBOMWindow != null) {
            try {
                mBOMWindow.close();
            } catch (TCException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Find BOM Child
     * 
     * @method getChildBOMLines
     * @date 2013. 12. 26.
     * @param
     * @return TCComponentBOMLine[]
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine[] getChildBOMLines(TCComponentBOMLine bomLine) throws Exception {
        AIFComponentContext contexts[] = bomLine.getChildren();
        TCComponentBOMLine childLines[] = new TCComponentBOMLine[contexts.length];
        for (int i = 0; i < childLines.length; i++) {
            childLines[i] = (TCComponentBOMLine) contexts[i].getComponent();
        }
        return childLines;
    }

    /**
     * LINE Item ID�� ������ PLANT Code�� �˾Ƴ���.
     * 
     * @method getPlantCode
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getPlantCode(String lineItemId) {
        return lineItemId.split("-")[0];
    }

    /**
     * LINE Item ID�� ������ SHOP Code�� �˾Ƴ���.
     * 
     * @method getShopCode
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getShopCode(String lineItemId) {
        return lineItemId.split("-")[1];
    }

    /**
     * LINE Item ID�� ������ LINE Code�� �˾Ƴ���.
     * 
     * @method getLineCode
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getLineCode(String lineItemId) {
        return lineItemId.split("-")[2];
    }

    /**
     * LINE Item ID�� ������ ProductNo�� �˾Ƴ���.
     * 
     * @method getProductNo
     * @date 2013. 12. 24.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getProductNo(String lineItemId) {
        return lineItemId.split("-")[3];
    }

    /**
     * Export Folder üũ - Folder�� �������������� ����
     * 
     * @method checkExportFolderPath
     * @date 2014. 1. 13.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void checkExportFolderPath(String folderPath) {
        File folderFile = new File(folderPath);
        // Folder�� ������ �����Ѵ�.
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
    }
}
