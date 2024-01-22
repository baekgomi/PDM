/**
 * 
 */
package com.symc.plm.me.sdv.operation.paint;

import java.util.ArrayList;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.BOPLineUtility;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Class Name : CreatePaintOPOperation
 * Class Description :
 * 
 * [SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
 * 
 * @date 2013. 12. 5.
 * 
 */
public class CreatePaintOPOperation extends AbstractSDVActionOperation {
    private IDataSet dataSet = null;
    private TCComponentItem createdItem = null;
    private static String DEFAULT_REV_ID = "000";

    public CreatePaintOPOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
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

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        dataSet = getDataSet();
        
        // [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
        TCComponentItemRevision mecoRevision = isOwnedMECO();
        if(mecoRevision==null){
        	throw new Exception("Check MECO owning user");
        }

        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();

        TCComponentBOMLine shopBOMLine = mfgApp.getBOMWindow().getTopBOMLine();
        TCComponentBOMLine targetBOMLine = mfgApp.getSelectedBOMLines()[0];

        String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
        String shopCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);
        String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
        String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        // Item Id ����
        String itemId = vechicleCode + "-" + shopCode + "-" + opCode + "-" + bopVersion;

        String korName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_KOR_NAME);

        /**
         * Item ����
         */
        createdItem = SDVBOPUtilities.createItem(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM, itemId, DEFAULT_REV_ID, korName, "");

        /**
         * �Ӽ� Update
         */
        setProperties();
        /**
         * ����Revision�� �۾�ǥ�ؼ��� ����
         */
        attachProcessExcelToOP(createdItem.getLatestItemRevision());
        /**
         * BOP Line�� �߰�
         */
        addOperationBOPLine(targetBOMLine, createdItem);

        /**
         * MECO�� ������ Item Revision�� ����
         */
        AddRevisionToMecoRevision(createdItem);
        /**
         * ������ ����
         */
        shopBOMLine.window().save();
        
        SDVBOPUtilities.executeExpandOneLevel();

    }

    /**
     * ���� BOPLine�� �߰� �� BOP �Ӽ� �߰�
     * 
     * [SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
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
        // ���� �Է�
        //newOpBOMLine.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");
        
        // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
    	BOPLineUtility.updateLineToOperationAbsOccId(newOpBOMLine);
    	
        return newOpBOMLine;

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
        String opEngName = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_ENG_NAME);

        
        //createdItem.setProperty("uom_tag", "EA");

        /**
         * Revision �Ӽ� Update
         */
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME, opEngName);
        /**
         * MECO
         */
        //String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision)dataSet.getValue("mecoSelect", "mecoRev");
        /**
         * ��������
         */
        String vechicleCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
        String shopCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SHOP);

        String opCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
        String productCode = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE);
        String bopVersion = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
        String dr = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_DR);

        String workerCount = dataSet.getStringValue("opInform", SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);

        String lineCode = dataSet.getStringValue("opInform", SDVPropertyConstant.STATION_LINE);
        String stationCode = dataSet.getStringValue("opInform", SDVPropertyConstant.STATION_STATION_CODE);
        
        /**
    	 * ����ȭ ����� ��û
    	 * ���� ���� ȭ�鿡�� Ư�� Ư�� �Ӽ��Է¶� �߰�
    	 */
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        String specialChar = dataSet.getStringValue("opInform", SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        @SuppressWarnings("unchecked")
        ArrayList<String> dwgNoList = (ArrayList<String>) dataSet.getListValue("opInform", SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO);
        boolean isKPC = (Boolean) dataSet.getValue(SDVPropertyConstant.OPERATION_REV_KPC);

        //createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoNo);
        createdItemRevision.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoRevision);

        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE, vechicleCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SHOP, shopCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE, opCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_PRODUCT_CODE, productCode);
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_BOP_VERSION, bopVersion);

        createdItemRevision.setProperty(SDVPropertyConstant.STATION_LINE, lineCode);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_STATION_CODE, stationCode);

        if (!workerCount.isEmpty())
            createdItemRevision.setIntProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT, Integer.parseInt(workerCount));

        if (!dr.isEmpty())
            createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_DR, dr);

        if (dwgNoList.size() > 0) {
            String[] dwgNoArray = dwgNoList.toArray(new String[dwgNoList.size()]);
            createdItemRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO).setStringValueArray(dwgNoArray);
        }

        createdItemRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_KPC, isKPC);
        
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        createdItemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC, specialChar);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        String revision  = opRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
        TCComponentDataset procDataSet = SDVBOPUtilities.getTemplateDataset("M7_TEM_DocItemID_ProcessSheet_Kor", itemId +"/"+revision, itemId);
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
        String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItem mecoItem = SDVBOPUtilities.FindItem(mecoNo, SDVTypeConstant.MECO_ITEM);
        if (mecoItem == null)
            return;
        TCComponentItemRevision mecoItemRevision = mecoItem.getLatestItemRevision();
        mecoItemRevision.add("CMHasSolutionItem", createdItem.getLatestItemRevision());

    }
    
	/**
     * [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
     * MECO�� Owner �� ���� Login �� User�� �ٸ� ��� Operation�� ���̻� ���� �� �� ������ �Ѵ�.
     * @return
     */
    private TCComponentItemRevision isOwnedMECO(){
    	
    	TCComponentItemRevision mecoItemRevision =  null;
        String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        if(mecoNo!=null && mecoNo.trim().length()>0){
        	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoNo, (TCSession)this.getSession());
        	mecoItemRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
        }
		
        return mecoItemRevision;
    }

}
