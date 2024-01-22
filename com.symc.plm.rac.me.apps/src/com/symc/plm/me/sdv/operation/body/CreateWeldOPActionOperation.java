package com.symc.plm.me.sdv.operation.body;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;


/**
 * [SR140702-044][20140702] jwLee �������� IDü�� ���� (1. LOV�߰�, 2. Serial No.ü�� ����, 3. �������� �ߺ� �˻� �ҽ� �̵�)
 * [SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
 * [NON-SR][20150729] shcho, �ɼ� �� �������� ��ġ�� BL_VARIANT_CONDITION ���� BL_OCC_MVL_CONDITION���� ����
 * 
*/

public class CreateWeldOPActionOperation extends AbstractSDVActionOperation {

    private IDataSet dataSet = null;

    private final String firstRevision = SDVPropertyConstant.ITEM_REV_ID_ROOT;

    private TCSession session;


    /**
     * @param operationId
     * @param ownerId
     * @param dataSet
     */
    public CreateWeldOPActionOperation(int operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
        this.dataSet = dataSet;
    }

    public CreateWeldOPActionOperation(String operationId, String ownerId, IDataSet dataSet) {
        super(operationId, ownerId, dataSet);
        this.dataSet = dataSet;
    }

    public CreateWeldOPActionOperation(int operationId, String ownerId,  Map<String, Object> parameters, IDataSet dataSet) {
        super(operationId, ownerId, parameters, dataSet);
        this.dataSet = dataSet;
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeOperation() throws Exception
    {
        session = CustomUtil.getTCSession();
        Markpoint mp = new Markpoint(session);
        try
        {
            dataSet = getDataSet();

            // MECO �˻� �信�� �˻��� ��� �����͸� �����´�
            Object meco_no = dataSet.getValue("searchMECO", SDVPropertyConstant.SHOP_REV_MECO_NO);
            
            // [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
            if(meco_no!=null){
            	if(meco_no instanceof TCComponentItemRevision){
            		TCComponentItemRevision mecoRevision = isOwnedMECO((TCComponentItemRevision)meco_no);
            		if(mecoRevision==null){
            			throw new Exception("Check MECO owning user");
            		}
            	}
            }

            // createWeldOP �信�� �����͸� �����´�
            IDataMap createWeldOP = dataSet.getDataMap("createWeldOP");
            TCComponentBOPLine targetOP = (TCComponentBOPLine) createWeldOP.getValue("targetOP");
            TCComponentBOPLine gunItem = (TCComponentBOPLine) createWeldOP.getValue("gunID");

            String serialNO = createWeldOP.getStringValue("serialNO");
            String weldOpOption = createWeldOP.getStringValue("weldOpOption");
            Object isAltObj = createWeldOP.getValue(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP);
            String altPrefix = createWeldOP.getStringValue(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);


            // �űԻ����� WeldOP �� ID �� �����

            //String id = (((Boolean) isAltObj) ? altPrefix + "-" : "") + targetOP.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "-WEOP-" + serialNO;
            String id = targetOP.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID) + "-WEOP-" + serialNO + "-" + weldOpOption;
            //���������� ���ԵǴ� Gun / Robot �� �ִ� Plant Window ������ �����´�
            TCComponentBOMLine plantLine = getPlantBOMLine(targetOP);
            TCComponentBOMWindow plantWindow = plantLine.window();

            // WeldOP �� �����Ѵ�
            TCComponentItem item = CustomUtil.createItem(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM, id, firstRevision, id, "WeldOP");
            TCComponentItemRevision itemRevision = item.getLatestItemRevision();
            // ������ WeldOP �� Reference �Ӽ��� �߰��Ѵ� (TargetOperation)
            itemRevision.setReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP, targetOP.getItemRevision());
            // ������ WeldOP�� �ش� TargetOP�� ���� ������ ���δ�
            TCComponentBOPLine station = (TCComponentBOPLine)targetOP.parent();
            TCComponentBOPLine weldOP = (TCComponentBOPLine)station.add(null, itemRevision, null, false);
            
            // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
        	BOPLineUtility.updateLineToOperationAbsOccId(weldOP);

            // ���� ������ WeldOP �� ÷���� Gun�� Robot �������� �����´�
//            TCComponentBOPLine plantItem = (TCComponentBOPLine) gunItem.parent();
//
//            AIFComponentContext[] plantChildItem = plantItem.getChildren();

            //MECO �� �����Ѵ�
            if (meco_no != null && !meco_no.equals(""))
            {
                itemRevision.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, (TCComponent) meco_no);

                // MECO Solution�� ����
                ((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_SOLUTION_ITEM, itemRevision);
            }
            // TargetOperation �� �ɼ��� �����ͼ� ���� ������ WeldOP �� �߰��Ѵ�
            //[NON-SR][20150729] shcho, �ɼ� �� �������� ��ġ�� BL_VARIANT_CONDITION ���� BL_OCC_MVL_CONDITION���� ����
            String option = targetOP.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);

            if (!option.equals(""))
            {
                TCVariantService svc = session.getVariantService();
                svc.setLineMvlCondition(weldOP, option);
            }

            // ALT BOP �� ��� �߰��� �Ӽ��� �ִ´�
            if (((Boolean) isAltObj))
            {
                itemRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, true);
                itemRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, altPrefix);
            }

            // ������ WeldOP�� ������ Gun�� Robot �� �Ҵ��Ѵ�
            TCComponentMEAppearancePathNode[] linkedAppearances = gunItem.parent().askLinkedAppearances(false);
            TCComponentBOMLine linkedBOMLine = null;

            for (TCComponentMEAppearancePathNode linkedAppearance : linkedAppearances)
            {
                linkedBOMLine = plantWindow.getBOMLineFromAppearancePathNode(linkedAppearance, plantLine);
            }
            ArrayList<InterfaceAIFComponent> resourceList = new ArrayList<InterfaceAIFComponent>();
            AIFComponentContext[] plantChildItem = linkedBOMLine.getChildren();
            for (AIFComponentContext plantChild : plantChildItem)
            {
                if (resourceList.size() == 2)
                    break;

                TCComponentBOMLine plantResource = (TCComponentBOMLine) plantChild.getComponent();
                if (plantResource.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM))
                    resourceList.add(plantChild.getComponent());

                if (plantResource.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID).equals(gunItem.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID)))
                    resourceList.add(plantChild.getComponent());
            }

            //[SR140820-017][20150209]shcho, BOM Line ���� ǥ�� �ϰ��� ���� �ʿ��� ��ȯ���� Shop-Line-����-������ ���������� �������� �Ѵ�.
            //weldOP.setProperty(SDVPropertyConstant.BL_QUANTITY, "1");

            SDVBOPUtilities.connectObject(weldOP, resourceList, "MEWorkArea");
            weldOP.save();

            //SDVBOPUtilities.executeExpandOneLevel();
        }
        catch (Exception ex)
        {
            mp.rollBack();
            setErrorMessage(ex.getMessage());
            setExecuteError(ex);
            throw ex;
        }

        mp.forget();
    }
    
	/**
	 * [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
	 * MECO�� Owner �� ���� Login �� User�� �ٸ� ��� Operation�� ���̻� ���� �� �� ������ �Ѵ�.
	 * @return
	 */
	private TCComponentItemRevision isOwnedMECO(TCComponentItemRevision mecoRevision){
		
		TCComponentItemRevision ownMecoItemRevision = null;
	
    	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoRevision, (TCSession)this.getSession());
    	ownMecoItemRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
		
	    return ownMecoItemRevision;
	}

    /**
     *
     *
     * @method getPlantBOMLine
     * @date 2013. 12. 27.
     * @param
     * @return TCComponentBOMLine
     * @exception
     * @throws
     * @see
     */
    protected TCComponentBOMLine getPlantBOMLine(TCComponentBOMLine selectBOMLine) {
        TCComponentBOMLine rootBomline;
        String rootBomView = null;
        String targetPlant = null;
        try {
            TCComponentItemRevision itemRevision = selectBOMLine.window().getTopBOMLine().getItemRevision();
            targetPlant = getPlant(itemRevision);
        } catch (TCException e) {
            e.printStackTrace();
        }
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart == null)
                continue;
            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null)
                continue;

            rootBomline = cmeBOMTreeTable.getBOMRoot();
            try {
                rootBomView = rootBomline.getProperty("bl_item_item_id");
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (targetPlant != null && rootBomView != null)
            {
                if (targetPlant.equals(rootBomView))
                    return rootBomline;
            }

        }
        return null;
    }

    /**
     *
     *
     * @method getPlant
     * @date 2013. 12. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    protected static String getPlant(TCComponentItemRevision revision) throws TCException {
        String plantType = null;
        String plantId = null;
        TCComponent[] plant = revision.getRelatedComponents(SDVTypeConstant.MFG_WORKAREA);
        if (plant.length == 1)
        {
            plantType = plant[0].getProperty("object_type");
            if (plantType.equals("PlantShopRevision"))
                return plantId = plant[0].getProperty("item_id");
        }
        return plantId;
    }

}
