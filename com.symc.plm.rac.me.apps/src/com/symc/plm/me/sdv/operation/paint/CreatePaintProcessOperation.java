/**
 * 
 */
package com.symc.plm.me.sdv.operation.paint;

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
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Class Name : CreatePaintProcessOperation
 * Class Description :
 * 
 * @date 2013. 12. 2.
 * 
 */
public class CreatePaintProcessOperation extends AbstractSDVActionOperation {
    private IDataSet dataSet = null;
    private TCComponentItem createdItem = null;
    private static String DEFAULT_REV_ID = "000";

    public CreatePaintProcessOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

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

        String shopCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_SHOP);
        String lineCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_LINE);
        String stationCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_STATION_CODE);
        String productCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_PRODUCT_CODE);
        String parallelStationNo = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_PARALLEL_STATION_NO);

        // Item Id ����
        String itemId = "PTP" + "-" + shopCode + "-" + lineCode + "-" + stationCode + "-" + productCode + "-" + parallelStationNo;
        String korName = dataSet.getStringValue("processInform", SDVPropertyConstant.ITEM_OBJECT_NAME);

        /**
         * Station Item ����
         */
        String selectedItemType = targetBOMLine.getItem().getType();
        if (selectedItemType.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            createdItem = SDVBOPUtilities.createItem(SDVTypeConstant.BOP_PROCESS_STATION_ITEM, itemId, DEFAULT_REV_ID, korName, "");
        }

        /**
         * Station �Ӽ� Update
         */
        setProperties();

        /**
         * ���õ� Station BOP Line�� Station BOP Line�� �߰�
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

        //String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        String shopCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_SHOP);
        String lineCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_LINE);
        String stationCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_STATION_CODE);
        String productCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_PRODUCT_CODE);
        String ParallelStationNo = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_PARALLEL_STATION_NO);
        String stationKorName = dataSet.getStringValue("processInform", SDVPropertyConstant.ITEM_OBJECT_NAME);
        String stationEngName = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_ENG_NAME);
        String vehicleCode = dataSet.getStringValue("processInform", SDVPropertyConstant.STATION_VEHICLE_CODE);
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision)dataSet.getValue("mecoSelect", "mecoRev");
        
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();

        //createdItemRevision.setProperty(SDVPropertyConstant.STATION_MECO_NO, mecoNo);
        createdItemRevision.setReferenceProperty(SDVPropertyConstant.STATION_MECO_NO, mecoRevision);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_SHOP, shopCode);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_LINE, lineCode);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_STATION_CODE, stationCode);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_PRODUCT_CODE, productCode);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_PARALLEL_STATION_NO, ParallelStationNo);
        createdItem.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, stationKorName);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_ENG_NAME, stationEngName);
        createdItemRevision.setProperty(SDVPropertyConstant.STATION_VEHICLE_CODE, vehicleCode);
        
        createdItemRevision.setProperty(SDVPropertyConstant.LINE_REV_PROCESS_TYPE, "P");
        
    }

    /**
     * Station BOPLine �߰�
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

        // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
    	BOPLineUtility.updateLineToOperationAbsOccId(newOpBOMLine);
    	
        return newOpBOMLine;

    }

    /**
     * 
     * @method AddRevisionToMecoRevision
     * @date 2013. 12. 3.
     * @param
     * @return void
     * @throws Exception
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