/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.activator.Activator;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVProcessUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.framework.services.IMFGContextService;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.ICreateInstanceInput;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.services.IOpenService;
import com.teamcenter.rac.util.OSGIUtil;

/**
 * Class Name : CreateAssemblyShopOperation
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
 * [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link���� 
 * 
 * @date 2013. 11. 7.
 * 
 */
public class CreateAssemblyShopOperation extends AbstractSDVActionOperation {
    private IDataSet dataSet = null;
    private TCSession tcSession = null;
    private TCComponentItem createdItem = null; // ������ Item
    private TCComponentBOMLine createdBOPLine = null; // ������ Item BOPLine
    private static String DEFAULT_REV_ID = "000";
    private static String M_RELEASE_WORKFLOW_TEMPLATE = "SYMC_ANY_Self_M_Release";

    public CreateAssemblyShopOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        dataSet = getDataSet();

        tcSession = (TCSession) getSession();
        
        // [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
    	TCComponentItemRevision mecoRevision = isOwnedMECO();
    	if(mecoRevision==null){
    		throw new Exception("Check MECO owning user");
    	}

        String itemId = makeItemId();

        String shopKorName = dataSet.getStringValue("shopInform", SDVPropertyConstant.ITEM_OBJECT_NAME);

        /**
         * Item ����
         */
        createRootItem(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, itemId, shopKorName);

        /**
         * �Ӽ����� �Է�
         */
        setProperties();

        /**
         * MECO�� ������ Item Revision�� ����
         */
        AddRevisionToMecoRevision(createdItem);
        /**
         * BOP Window�� Open��
         */
        OpenCreatedItemBOMWindow();

    }

    /**
     * Root Item�� ������
     * 
     * @method createRootItem
     * @date 2013. 11. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createRootItem(String itemType, String itemId, String objectName) throws Exception {

        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(tcSession, itemType);
        IMFGContextService mfgContextService = (IMFGContextService) OSGIUtil.getService(Activator.getDefault(), IMFGContextService.class.getName());

        CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
        createInput.add(SDVPropertyConstant.ITEM_ITEM_ID, itemId);
        createInput.add(SDVPropertyConstant.ITEM_OBJECT_NAME, objectName);
        createInput.add(SDVPropertyConstant.ITEM_REVISION_ID, DEFAULT_REV_ID);

        List<ICreateInstanceInput> inPutList = new ArrayList<ICreateInstanceInput>();

        Map<?, ?> secondaryCreateDefinitions = createDefinition.getSecondaryCreateDefinitions();
        List<?> revList = (ArrayList<?>) secondaryCreateDefinitions.get("revision");
        IBOCreateDefinition createDefinitionRev = (IBOCreateDefinition) revList.get(0);
        CreateInstanceInput createInputRev = new CreateInstanceInput(createDefinitionRev);
        createInputRev.add(SDVPropertyConstant.ITEM_REVISION_ID, DEFAULT_REV_ID);
        inPutList.add(createInputRev);

        inPutList.add(createInput);

        try {
//            List<TCComponent> createCompList = mfgContextService.createContext(createDefinition, inPutList, null, null);
            List<TCComponent> createCompList = mfgContextService.createContext(createDefinition, inPutList, null, null, null, null);
            createdItem = (TCComponentItem) createCompList.get(0);
            createdBOPLine = (TCComponentBOMLine) createCompList.get(1);

        } catch (TCException e) {
            e.printStackTrace();
        }

    }

    /**
     * �Ӽ����� �Է�
     * 
     * @method SetProperties
     * @date 2013. 11. 8.
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
        String shopEngName = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_ENG_NAME);
        String vehicleKorName = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME);
        String vehicleEngName = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME);
        createdItem.setProperty(SDVPropertyConstant.SHOP_ENG_NAME, shopEngName);
        createdItem.setProperty(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME, vehicleKorName);
        createdItem.setProperty(SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME, vehicleEngName);
        // createdItem.setProperty("uom_tag", "EA");

        /**
         * Revision �Ӽ� Update
         */
        TCComponentItemRevision createdItemRevision = createdItem.getLatestItemRevision();

        // String mecoNo = dataSet.getStringValue("mecoSelect", "mecoNo");
        TCComponentItemRevision mecoRevision = (TCComponentItemRevision) dataSet.getValue("mecoSelect", "mecoRev");

        String shopCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_SHOP_CODE);
        String productCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);

        String jph = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_JPH);
        String allowance = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_ALLOWANCE);
        String vehicleCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_VEHICLE_CODE);

        createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE, shopCode);
        createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, productCode);
        createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE, vehicleCode);
        createdItemRevision.setReferenceProperty(SDVPropertyConstant.SHOP_REV_MECO_NO, mecoRevision);
        createdItemRevision.setProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE, "A");

        //[SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
        if (!jph.isEmpty())
            createdItemRevision.setDoubleProperty(SDVPropertyConstant.SHOP_REV_JPH, Double.parseDouble(jph));
        if (!allowance.isEmpty())
            createdItemRevision.setDoubleProperty(SDVPropertyConstant.SHOP_REV_ALLOWANCE, Double.parseDouble(allowance));
    }

    /**
     * 
     * ������ BOP�� Open��
     * 
     * @method OpenCreatedItemBOMWindow
     * @date 2013. 11. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void OpenCreatedItemBOMWindow() {

        OpenBomWindowOperation operation = new OpenBomWindowOperation();
        operation.addOperationListener(new InterfaceAIFOperationListener() {

            @Override
            public void endOperation() {
                try {
                    /**
                     * ���Ҵ� ���� ����
                     */
                    String shopCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_SHOP_CODE);
                    String productCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
                    TCComponentItemRevision mTopItemRevision = (TCComponentItemRevision) dataSet.getValue("shopInform", SDVTypeConstant.BOP_MPRODUCT_REVISION);

                    String tempLineId = SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shopCode + "-TEMP-" + productCode;
                    TCComponentItem tempLineItem = SDVBOPUtilities.createItem(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, tempLineId, DEFAULT_REV_ID, "���Ҵ� LINE", "");

                    // ���Ҵ� Line Shop BOP�� �߰���
                    TCComponentBOMLine tempLine = createdBOPLine.add(null, tempLineItem.getLatestItemRevision(), null, false);
                    tempLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, "-"); // ���Ҵ� ������ Find No - ���� ��
                    createdBOPLine.window().save();
                    /**
                     * Shop�� ��� ��ǰ���� ������
                     */
                    /* [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link���� 
                        createdBOPLine.getItemRevision().add("IMAN_METarget", mTopItemRevision);
                     */
                    /**
                     * �����׷� ����
                     */
                    createOccGroupCompoments();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void startOperation(String arg0) {

            }

        });
        tcSession.queueOperation(operation);
    }

    /**
     * BOM Window Open
     * Class Name : OpenBomWindowOperation
     * Class Description :
     * 
     * @date 2013. 11. 11.
     * 
     */
    public class OpenBomWindowOperation extends AbstractAIFOperation {

        /*
         * (non-Javadoc)
         * 
         * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
         */
        @Override
        public void executeOperation() throws Exception {
            IOpenService openService = AIFUtility.getCurrentOpenService();
            openService.open(createdBOPLine.window());

        }

    }

    /**
     * �����׷��� ������
     * 
     * @method createOccGroupCompoments
     * @date 2013. 11. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void createOccGroupCompoments() throws Exception {
        try {

            LinkedList<String> occGroupNameList = getOccGroupName();
            IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(tcSession, "AppearanceGroup");
            IMFGContextService mfgContextService = (IMFGContextService) OSGIUtil.getService(Activator.getDefault(), IMFGContextService.class.getName());
            for (String groupName : occGroupNameList) {
                // ������ �ʿ��� �Է°�
                CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
                createInput.add(SDVPropertyConstant.ITEM_OBJECT_NAME, groupName);
                createInput.add(SDVPropertyConstant.ITEM_OBJECT_TYPE, "AppearanceGroup");
                List<ICreateInstanceInput> inPutList = new ArrayList<ICreateInstanceInput>();
                inPutList.add(createInput);
                List<TCComponent> comps = mfgContextService.createView(createDefinition, inPutList, createdBOPLine);
                comps.get(0).save();
                createdBOPLine.getItem().refresh();
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * ���� �׷� ���� ������
     * 
     * @method getOccGroupName
     * @date 2013. 11. 11.
     * @param
     * @return LinkedList<String>
     * @exception
     * @throws
     * @see
     */
    private LinkedList<String> getOccGroupName() throws Exception {
        LinkedList<String> groupList = new LinkedList<String>();
        TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) tcSession.getTypeComponent("ListOfValues");
        TCComponentListOfValues[] listofvalues = listofvaluestype.find("M7_SYSTEM_CODE");
        TCComponentListOfValues listofvalue = listofvalues[0];
        String[] listValues = listofvalue.getListOfValues().getLOVDisplayValues();
        for (String value : listValues) {

            if (value.equals("."))
                continue;
            groupList.add(value);
        }
        groupList.add("���Ҵ�");
        return groupList;
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

    /**
     * 
     * Item Id�� ������
     * 
     * @method makeItemId
     * @date 2013. 12. 3.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String makeItemId() {
        String itemId = "";
        String shopCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_SHOP_CODE);
        String productCode = dataSet.getStringValue("shopInform", SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
        itemId = SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shopCode + "-" + productCode;
        return itemId;
    }

    /**
     * Item Revision�� Release ��Ŵ
     * 
     * @method setItemRevisionMReleased
     * @date 2013. 12. 30.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unused")
    private void setItemRevisionMReleased(TCComponentItemRevision itemRevision) throws Exception {
        SDVProcessUtils.createProcess(M_RELEASE_WORKFLOW_TEMPLATE, "", new TCComponent[] { itemRevision });
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

}
