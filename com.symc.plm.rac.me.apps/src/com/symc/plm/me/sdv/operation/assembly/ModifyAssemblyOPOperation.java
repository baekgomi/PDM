/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.tree.TreePath;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.psebase.common.AbstractViewableNode;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.treetable.TreeTableNode;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * ���� ���� Operation
 * Class Name : CreateAssemblyOPOperation
 * Class Description :
 * 
 * [SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
 * 
 * 
 * @date 2013. 11. 20.
 * 
 */
public class ModifyAssemblyOPOperation extends AbstractSDVActionOperation {
    private IDataSet dataSet = null;
    private TCComponentItem createdItem = null;
    private TCComponentBOMLine tempLineBOMLine = null;
    private Registry registry = null;
    private static String DEFAULT_REV_ID = "000";
    private int actionId = 0;
    private boolean isIFExcution = false; // PE I/F ���� ȣ�� �ϴ� �� ����

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public ModifyAssemblyOPOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        this.actionId = actionId;
    }

    /**
     * I/F �� ������
     * 
     * @param actionId
     * @param ownerId
     * @param dataSet
     * @param isIFExcution
     */
    public ModifyAssemblyOPOperation(int actionId, String ownerId, IDataSet dataSet, boolean isIFExcution) {
        super(actionId, ownerId, dataSet);
        this.isIFExcution = isIFExcution;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {

        if (isIFExcution)
            return;


        try {
            Shell shell = null;
            // OK �̸�
            if (actionId == 1)
                shell = AIFDesktop.getActiveDesktop().getShell();
            else {
                Dialog dialog = (Dialog) UIManager.getAvailableDialog("symc.me.bop.ModifyAssemblyOPDialog");
                shell = dialog.getShell();
            }

            MessageBox.post(shell, "������ �Ϸ� �Ǿ����ϴ�. ", "Information", MessageBox.INFORMATION);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            registry = Registry.getRegistry(this);
            modifyOperation();
        } catch (Exception ex) {
            setAbortRequested(true);
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Create Operation
     * 
     * @method executeCreateOperation
     * @date 2013. 12. 5.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    public void modifyOperation() throws Exception {
        dataSet = getDataSet();
        
        // [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
//    	TCComponentItemRevision mecoRevision = isOwnedMECO();
//    	if(mecoRevision==null){
//    		throw new Exception("Check MECO owning user");
//    	}

        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();

        TCComponentBOMLine shopBOMLine = mfgApp.getBOMWindow().getTopBOMLine();
        TCComponentBOMLine targetBOMLine = mfgApp.getSelectedBOMLines()[0];

        String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
        String lineCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
        String functionCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
        String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
        String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        // Item Id ����
        String itemId = vechicleCode + "-" + lineCode + "-" + functionCode + "-" + opCode + "-" + bopVersion;
        // String itemId = dataSet.getStringValue("opInform", SDVPropertyConstant.ITEM_ITEM_ID);
        String korName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);

        String selectedItemType = targetBOMLine.getItem().getType();

        // ���õ� BOMLine�� Line�� ���
//        if (selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
//            String tempBOPLineId = getTempBOPLineId(shopBOMLine);
//            // ���Ҵ� Line�̸�
//            if (tempBOPLineId.equals(targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
//                // ���Ҵ� Line�� Release �Ǿ��ٸ� �ڵ�������
//                ReviseBOMLine(targetBOMLine);
//            }
//        } else {
//            // ���õ� BOMLine�� Line�� �ƴ� ��� ���Ҵ� Line�� ������ �����Ѵ�.
//            targetBOMLine = getTempBOPLine(shopBOMLine);
//            // ���Ҵ� Line�� Release �Ǿ��ٸ� �ڵ�������
//            ReviseBOMLine(targetBOMLine);
//            tempLineBOMLine = targetBOMLine;
//
//        }
//        createdItem = SDVBOPUtilities.createItem(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM, itemId, DEFAULT_REV_ID, korName, "");
        createdItem = targetBOMLine.getItem();
        /**
         * �Ӽ� Update
         */
        setProperties();
        /**
         * ����Revision�� �۾�ǥ�ؼ��� ����
         */
//        attachProcessExcelToOP(createdItem.getLatestItemRevision());
        /**
         * BOP Line�� �߰�
         */
//        TCComponentBOMLine operationBOMLine = addOperationBOPLine(targetBOMLine, createdItem);
        
        // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
//    	BOPLineUtility.updateLineToOperationAbsOccId(operationBOMLine);

        /**
         * MECO�� ������ Item Revision�� ����
         */
//        AddRevisionToMecoRevision(createdItem);
        /**
         * ������ ����
         */
//        shopBOMLine.window().save();

        /**
         * �߰��� Expand ��
         */
//        targetBelowExpand(mfgApp);

//        return operationBOMLine;
    }

    /**
     * ���� BOPLine�� �߰� �� BOP �Ӽ� �߰�
     * 
     * [SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
     * 
     * 
     * @method addOperationBOPLine
     * @date 2013. 11. 8.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine addOperationBOPLine(TCComponentBOMLine targetBOMLine, TCComponentItem createdItem) throws Exception {

        TCComponentBOMLine newOpBOMLine = targetBOMLine.add(null, createdItem.getLatestItemRevision(), null, false);
        TCComponentItemRevision opRevision = newOpBOMLine.getItemRevision();

        String stationNo = opRevision.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO).replace("-", "");// ������ȣ
        String workerCode = createdItem.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE).replace("-", "");// �۾����ڵ�
        String seq = createdItem.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ);// �۾��� ����
        // ���� �Է�
        //newOpBOMLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
        // ��������ȣ �Է�
        boolean isExistEmptyValue = stationNo.isEmpty() || workerCode.isEmpty() || seq.isEmpty(); // �ϳ��� ���� ������ �ݿ�����
        String findNo = stationNo.concat("|").concat(workerCode).concat("|").concat(seq);
        if (findNo.length() > 15 || isExistEmptyValue)
            return newOpBOMLine;
        newOpBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
        return newOpBOMLine;

    }

    /**
     * Shop ������ ������ ���Ҵ� Line�� BOMLine�� ������
     * 
     * @method getTempBOPLine
     * @date 2013. 11. 21.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOMLine getTempBOPLine(TCComponentBOMLine topBOMLine) throws Exception {
        // ���Ҵ� LINE ID
        String tempLineId = getTempBOPLineId(topBOMLine);

        AIFComponentContext[] aifComps = topBOMLine.getChildren();
        for (AIFComponentContext aifComp : aifComps) {
            TCComponentBOMLine lineBOMLine = (TCComponentBOMLine) aifComp.getComponent();
            String lineId = lineBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            if (tempLineId.equals(lineId))
                return lineBOMLine;
        }
        return null;
    }

    /**
     * ���Ҵ� Line Id�� ������
     * 
     * @method getTempBOPLineId
     * @date 2013. 12. 31.
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
     * �Ӽ����� �Է�
     * 
     * @method setProperties
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setProperties() throws Exception {

        /**
         * Item �Ӽ� Update
         */
    	
        String workerCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORKER_CODE);
        String processSeq = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_PROCESS_SEQ);
        String workArea = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORKAREA);
        String workUbody = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_WORK_UBODY);
        String itemUL = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_ITEM_UL);
        boolean maxWorkTimeCheck = (Boolean) dataSet.getValue(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK);
        boolean vehicleCheck = (Boolean) dataSet.getValue(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK);

        // Process Sequence ���ڸ� �Է½� �տ� 0 �� ���δ�.
        if (processSeq.length() == 2)
            processSeq = "0".concat(processSeq);

        createdItem.setProperty(SDVPropertyConstant.OPERATION_WORKER_CODE, workerCode);
        createdItem.setProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ, processSeq);
        if (!workArea.isEmpty())
            createdItem.setProperty(SDVPropertyConstant.OPERATION_WORKAREA, workArea);
        if (!workUbody.isEmpty())
            createdItem.setProperty(SDVPropertyConstant.OPERATION_WORK_UBODY, workUbody);
        if (!itemUL.isEmpty())
            createdItem.setProperty(SDVPropertyConstant.OPERATION_ITEM_UL, itemUL);

        createdItem.setLogicalProperty(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, maxWorkTimeCheck);
        createdItem.setLogicalProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, vehicleCheck);

        /**
         * Revision �Ӽ� Update
         */
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
        
        
        /**
         * MECO
         */
        // String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");

        TCComponentItemRevision mecoRevision = (TCComponentItemRevision) dataSet.getValue("mecoSelect", "mecoRev");
        /**
         * ��������
         */
        String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
        String shopCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
        String fcCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
        String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
        String productCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
        String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        String dr = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_DR);
        
        ////////////////////////////////////////////////////////////////////////////////////////
        //Ư�� Ư�� �Ӽ� �߰�
        String specialChar = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        
        ////////////////////////////////////////////////////////////////////////////////////////

        // ��������
        String stationNo = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_STATION_NO);
        /**
         * ��Ÿ ����
         */
        @SuppressWarnings("unchecked")
        ArrayList<String> dwgNoList = (ArrayList<String>) dataSet.getListValue("opInform", SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
        String assySystem = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM);

//        createdItemRevision.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoRevision);

        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vechicleCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SHOP, shopCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE, fcCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bopVersion);

        if (!dr.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_DR, dr);

        if (!stationNo.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO, stationNo);

        if (dwgNoList.size() > 0) {
            String[] dwgNoArray = dwgNoList.toArray(new String[dwgNoList.size()]);
            createdItemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).setStringValueArray(dwgNoArray);
        }
        if (!assySystem.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, assySystem);
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (!specialChar.isEmpty())
        	createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialChar);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        
      //������ �ʵ� �ϰ�� ������ ������ �۾��� ������ ���� ������ ���ų� ���� �̸��� ��
//        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME,  dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME));
        

    }

    /**
     * �۾�ǥ�ؼ� Excel Template ������ �����Ʒ��� ����
     * 
     * @method attachProcessExcelToOP
     * @date 2013. 11. 21.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void attachProcessExcelToOP(TCComponentItemRevision opRevision) throws Exception {
        String itemId = opRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String revision = opRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
        TCComponentDataset procDataSet = SDVBOPUtilities.getTemplateDataset("M7_TEM_DocItemID_ProcessSheet_Kor", itemId + "/" + revision, itemId);
        opRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, procDataSet);
    }

    /**
     * MECO�� ������ Item Revision�� Solution Item�� ���δ�.
     * 
     * @method AddRevisionToMecoRevision
     * @date 2013. 11. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void AddRevisionToMecoRevision(TCComponentItem createdItem) throws Exception {
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision) dataSet.getValue("mecoSelect", "mecoRev");
        if(mecoRevision == null)
            return;
        mecoRevision.add("CMHasSolutionItem", createdItem.getLatestItemRevision());

    }
    
    /**
     * [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
     * MECO�� Owner �� ���� Login �� User�� �ٸ� ��� Operation�� ���̻� ���� �� �� ������ �Ѵ�.
     * @return
     */
    private TCComponentItemRevision isOwnedMECO(){
    	
    	TCComponentItemRevision ownedMecoRevision = null;
    	TCComponentItemRevision mecoRevision = (TCComponentItemRevision) dataSet.getValue("mecoSelect", "mecoRev");
        if(mecoRevision!=null){
        	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoRevision, (TCSession)this.getSession());
        	ownedMecoRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
        }
		
        return ownedMecoRevision;
    }

    /**
     * Release�� BOMLINE ������
     * 
     * @method ReviseBOMLine
     * @date 2013. 12. 30.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void ReviseBOMLine(TCComponentBOMLine targetBOMLine) throws Exception {

        if (!CustomUtil.isReleased(targetBOMLine.getItemRevision()))
            return;
        String newRev = targetBOMLine.getItem().getNewRev();
        TCComponentItemRevision newRevision = targetBOMLine.getItemRevision().saveAs(newRev);

        targetBOMLine.window().newIrfWhereConfigured(newRevision);
        targetBOMLine.window().fireChangeEvent();
    }

    /**
     * �߰��� ��� ������ Expand ��
     * 
     * @method targetBelowExpand
     * @date 2014. 1. 23.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void targetBelowExpand(MFGLegacyApplication mfgApp) throws Exception {
        if (isIFExcution)
            return;

        if (tempLineBOMLine == null) {
            SDVBOPUtilities.executeExpandOneLevel();
        } else {
            // ���Ҵ� ���ο� �߰��� ���
            AbstractViewableTreeTable treetable = mfgApp.getAbstractViewableTreeTable();
            AbstractViewableNode rootNode = treetable.getRootNode();
            // �ֻ����� Expand �� �Ǿ������� Expand
            if (!treetable.isExpanded(rootNode))
                SDVBOPUtilities.executeExpandOneLevel();
            Iterator<TreeTableNode> iterator = rootNode.allChildrenIterator(true);
            while (iterator != null && iterator.hasNext()) {
                AbstractViewableNode childNode = (AbstractViewableNode) iterator.next();
                // ���Ҵ� �����̸�
                if (childNode.getName().startsWith(tempLineBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                    // ���õǰ� �ϰ�
                    treetable.setSelectionPaths(new TreePath[] { childNode.getTreePath() });
                    // Expand ��
                    SDVBOPUtilities.executeExpandOneLevel();
                    break;
                }
            }
        }
    }

}
