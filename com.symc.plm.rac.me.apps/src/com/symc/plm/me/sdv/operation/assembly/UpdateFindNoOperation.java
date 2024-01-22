/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.workflow.commands.assign.AssignOperation;

/**
 * Class Name : UpdateFindNoOperation
 * Class Description : ��������ȣ Update
 * 
 * @date 2013. 10. 29.
 * 
 */
public class UpdateFindNoOperation extends AbstractTCSDVOperation {

    private TCSession tcSession = null;
    private boolean isValidOK = true;
    private TCComponentBOMLine[] selectedBOMLines = null;
    private TCAccessControlService aclService = null;
    private Registry registry = null;
    private static String WORKFLOW_TEMPLATE_GRANT_PRIVILEGE = "SYMC_GRANT_PRIVILEGE_PROCESS";

    /*
     * (non-Javadoc)
     * TODO: Validation ��� �� ����
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void startOperation(String commandId) {

        registry = Registry.getRegistry(this);
        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // ���� BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            // (üũ)1. BOP Load ����
            if (bomWindow == null) {
                com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), registry.getString("Warning.NAME"), com.teamcenter.rac.util.MessageBox.WARNING);
                isValidOK = false;
                return;
            }

            selectedBOMLines = mfgApp.getSelectedBOMLines();

            for (TCComponentBOMLine selectedBOMLine : selectedBOMLines) {
                String selectedItemType = selectedBOMLine.getItem().getType();
                boolean isEnableType = selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM);
                // (üũ)3. Process BOP ���� ����
                if (!isEnableType) {
                    com.teamcenter.rac.util.MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("selectBOP.MSG"), registry.getString("Warning.NAME"), com.teamcenter.rac.util.MessageBox.WARNING);
                    isValidOK = false;
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        isValidOK = true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        // ������ �� ���� Line BOMLine ����Ʈ(Key:ItemId,OrderNo List, Value:TCComponentBOMViewRevision)
        Hashtable<List<String>, TCComponentBOMViewRevision> releasedLineBOMViewRevHash = new Hashtable<List<String>, TCComponentBOMViewRevision>();
        // ��� Operation ����Ʈ
        ArrayList<TCComponentBOMLine> targetOperationList = new ArrayList<TCComponentBOMLine>();
        // Line�� ���� ���ɻ��¸� �ߺ� üũ ����Ȯ��
        ArrayList<List<String>> isLineDupCheckedList = new ArrayList<List<String>>();
        TCComponentProcess grantProcess = null;

        if (!isValidOK)
            return;

        try {
            tcSession = (TCSession) getSession();
            aclService = tcSession.getTCAccessControlService();

            /**
             * 1. ������ ��� Operation�� �߰���
             */
            for (TCComponentBOMLine selectedBomline : selectedBOMLines) {
                String selectedItemType = selectedBomline.getItem().getType();
                // ������ ���
                if (selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM)) {

                    TCComponentBOMLine parentBOMLine = selectedBomline.parent();
                    if (parentBOMLine == null)
                        continue;
                    String parentItemType = parentBOMLine.getItem().getType();
                    if (!parentItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
                        continue;
                    /** ������ �� ���� Line BOM View �߰� **/
                    addLineBomViewRevision(selectedBomline.parent(), releasedLineBOMViewRevHash, isLineDupCheckedList);
                    /** ������ ��� �߰� **/
                    targetOperationList.add(selectedBomline);

                } else
                    addTargetBOMLine(selectedBomline, releasedLineBOMViewRevHash, targetOperationList, isLineDupCheckedList);
            }

            /**
             * 2. ���������� ���� Line BOM View Revision�� ���������� ��
             */
            if (releasedLineBOMViewRevHash.size() != 0)
                grantProcess = grantLineBomViewRevision(releasedLineBOMViewRevHash);

            /**
             * 3. ������ FindNo �Ӽ��� Update��
             */
            updateFindNo(targetOperationList);

            selectedBOMLines[0].window().save();

            for (TCComponentBOMViewRevision lineBOMViewRevision : releasedLineBOMViewRevHash.values()) {
                lineBOMViewRevision.refresh();
            }

            if (releasedLineBOMViewRevHash.size() != 0)
                revokeProcess(grantProcess);

        } catch (Exception ex) {
            if (grantProcess != null)
                grantProcess.delete();
            isValidOK = false;
        }
    }

    /**
     * 
     * ���� ��� ������ ������
     * 
     * @method addTargetBOMLine
     * @date 2013. 11. 1.
     * @param bomline
     *            BOMLine
     * @param releasedLineBOMViewRevHash
     *            ���� �Ұ����� Line BOMView Revision Hash
     * @param targetOperationList
     *            ���� ��� ����
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addTargetBOMLine(TCComponentBOMLine bomline, Hashtable<List<String>, TCComponentBOMViewRevision> releasedLineBOMViewRevHash, ArrayList<TCComponentBOMLine> targetOperationList, ArrayList<List<String>> isLineDupCheckedList) throws Exception {
        try {

            // ����BOM Ÿ��
            String parentItemType = bomline.getItem().getType();

            AIFComponentContext[] aifComps = bomline.getChildren();
            tcSession.setStatus(bomline.toString() + " " + registry.getString("findingChildOp.MSG"));

            for (AIFComponentContext aifComp : aifComps) {
                TCComponentBOMLine childBomline = (TCComponentBOMLine) aifComp.getComponent();
                // ����BOM Ÿ��
                String childItemType = childBomline.getItem().getType();
                // ���� BOP�� Line�� ���
                if (parentItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) && childItemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM)) {
                    /** ������ �� ���� Line BOM View �߰� **/
                    addLineBomViewRevision(bomline, releasedLineBOMViewRevHash, isLineDupCheckedList);
                    /** ������ ��� �߰� **/
                    targetOperationList.add(childBomline);
                } else
                    addTargetBOMLine(childBomline, releasedLineBOMViewRevHash, targetOperationList, isLineDupCheckedList);
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * 
     * ������ �� ���� ���� BOMView Revision�� �߰�
     * 
     * @method addLineBomViewRevision
     * @date 2013. 11. 1.
     * @param bomline
     * @param releasedLineBOMViewRevHash
     *            ������ �� ���� Line BOMView Revision ����Ʈ
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addLineBomViewRevision(TCComponentBOMLine bomline, Hashtable<List<String>, TCComponentBOMViewRevision> releasedLineBOMViewRevHash, ArrayList<List<String>> isLineDupCheckedList) throws Exception {
        String itemType = bomline.getItem().getType();
        // ������ ��� ����
        if (!itemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
            return;

        String itemId = bomline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        String orderNo = bomline.getProperty(SDVPropertyConstant.BL_OCC_ORDER_NO);
        List<String> key = Arrays.asList(new String[] { itemId, orderNo });

        // ������ �������θ� üũ�Ͽ����� Pass
        if (isLineDupCheckedList.contains(key))
            return;
        isLineDupCheckedList.add(key);

        // �̹� �߰��� �����̸� Pass
        if (releasedLineBOMViewRevHash.containsKey(key))
            return;

        TCComponentBOMViewRevision lineViewRevision = SDVBOPUtilities.getBOMViewRevision(bomline.getItemRevision(), "view");
        if (lineViewRevision == null)
            return;
        boolean isWrite = aclService.checkPrivilege(lineViewRevision, TCAccessControlService.WRITE);
        if (isWrite)
            return;
        /** Released�� Line �� �߰� **/
        releasedLineBOMViewRevHash.put(key, lineViewRevision);
    }

    /**
     * 
     * ������ BOM View Revision �� ������ �� �ֵ��� ������ �ش�.
     * 
     * @method grantLineBomViewRevision
     * @date 2013. 11. 1.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private TCComponentProcess grantLineBomViewRevision(Hashtable<List<String>, TCComponentBOMViewRevision> releasedLineBOMViewRevHash) throws Exception {
        TCComponentProcess grantProcess = null;

        TCComponentTaskTemplate taskTemplate = getTaskTemplate(WORKFLOW_TEMPLATE_GRANT_PRIVILEGE);
        TCComponentProcessType newProcessType = (TCComponentProcessType) tcSession.getTypeComponent("Job");
        newProcessType.refresh();

        int cnt = 0;
        TCComponent[] targets = new TCComponent[releasedLineBOMViewRevHash.size()];
        int[] targetTypes = new int[releasedLineBOMViewRevHash.size()];
        for (TCComponentBOMViewRevision lineBOMViewRevision : releasedLineBOMViewRevHash.values()) {
            targets[cnt] = lineBOMViewRevision;
            targetTypes[cnt] = TCAttachmentType.TARGET;
            cnt++;
        }

        grantProcess = (TCComponentProcess) newProcessType.create(WORKFLOW_TEMPLATE_GRANT_PRIVILEGE, "", taskTemplate, targets, targetTypes);

        return grantProcess;
    }

    /**
     * ������ Find No�� �Ӽ��� Update��
     * 
     * @method updateFindNo
     * @date 2013. 11. 1.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void updateFindNo(ArrayList<TCComponentBOMLine> bomlineList) throws Exception {

        try {
            for (TCComponentBOMLine operation : bomlineList) {

                // ������ȣ
                String stationNo = operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO).replace("-", "");// ������ȣ
                String workerCode = operation.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE).replace("-", "");// �۾����ڵ�
                String seq = operation.getItem().getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);// �۾��� ����
                boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty() || seq.isEmpty(); // �ϳ��� ���� ������ �ݿ�����
                String findNo = stationNo.concat("|").concat(workerCode).concat("|").concat(seq);
                if (findNo.length() > 15 || isExistEmptyValue)
                    return;
                operation.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
            }

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * ���� Job Object�� ������
     * 
     * @method revokeProcess
     * @date 2013. 11. 1.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void revokeProcess(final TCComponentProcess grantProcess) throws Exception {

        if (grantProcess == null)
            return;
        TCComponentTask doTask = grantProcess.getRootTask().getSubtask("GrantTask");
        AssignOperation operation = new AssignOperation(tcSession, AIFDesktop.getActiveDesktop(), new TCComponentTask[] { doTask }, tcSession.getUser());
        operation.addOperationListener(new InterfaceAIFOperationListener() {
            public void startOperation(String arg0) {
            }

            public void endOperation() {
                try {
                    grantProcess.refresh();
                    grantProcess.delete();
                } catch (Exception e) {
//                    try {
//                        attachErrorJobToFolder(grantProcess);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
                }
            }
        });
        tcSession.queueOperation(operation);
    }

    /**
     * 
     * Task Name ���� ���� Template �� ������
     * 
     * @method getTaskTemplate
     * @date 2013. 10. 31.
     * @param
     * @return TCComponentTaskTemplate
     * @exception
     * @throws
     * @see
     */
    private TCComponentTaskTemplate getTaskTemplate(String templateName) throws Exception {
        TCComponentTaskTemplate taskTemplate = null;
        TCComponentTaskTemplate taskTemplateList[] = null;

        try {
            // EPM Task Template Ÿ���� ������
            TCComponentTaskTemplateType taskTemplateType = (TCComponentTaskTemplateType) tcSession.getTypeComponent("EPMTaskTemplate");

            if (taskTemplateType != null)
                taskTemplateList = taskTemplateType.extentTemplates(0);
            // ���� ���ø��� ������
            for (TCComponentTaskTemplate taskTemplateObj : taskTemplateList) {
                TCComponentTaskTemplate taskRootTemplate = taskTemplateObj.getRoot();
                if (taskRootTemplate.getName().equalsIgnoreCase(templateName)) {
                    taskTemplate = taskRootTemplate;
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return taskTemplate;
    }

    /**
     * ���� �� ������ �߻� �Ͽ��� ��� BOPADM Error Folder
     * TODO: BOPADM ������ġ����� ���� �ʿ�
     * 
     * @method attachErrorJobToFolder
     * @date 2013. 11. 4.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unused")
    private void attachErrorJobToFolder(TCComponentProcess grantProcess) throws Exception {
        TCComponentUserType userType = (TCComponentUserType) tcSession.getTypeComponent("User");
        TCComponentUser bopAdmUser = userType.find("BOPADM");
        TCComponentFolder bopAdmHome = bopAdmUser.getHomeFolder();
        TCComponentFolder errorFolder = getChildFolder(bopAdmHome, "ERROR UPDATE FINDNO");
        errorFolder.add("contents", grantProcess);
    }

    /**
     * 
     * ���� Component�� Folder �� ã��
     * 
     * @method getChildFolder
     * @date 2013. 11. 4.
     * @param
     * @return TCComponentFolder
     * @exception
     * @throws
     * @see
     */
    private TCComponentFolder getChildFolder(TCComponent parentFolder, String findFolderName) throws Exception {

        AIFComponentContext[] aifContexts = parentFolder.getChildren("contents");

        for (AIFComponentContext aifContext : aifContexts) {
            TCComponent comp = (TCComponent) aifContext.getComponent();
            if (!(comp instanceof TCComponentFolder))
                continue;
            if (comp.toString().equals(findFolderName))
                return (TCComponentFolder) comp;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#afterExecuteSDVOperation()
     */
    @Override
    public void endOperation() {
        if (isValidOK)
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("FindNoComplete.MSG"), "OK", MessageBox.INFORMATION);

    }
}
