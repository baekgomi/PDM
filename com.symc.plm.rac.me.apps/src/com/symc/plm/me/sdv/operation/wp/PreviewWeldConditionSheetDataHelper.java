package com.symc.plm.me.sdv.operation.wp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVProcessUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;

/**
 * [SR140611-027][20140611] jwlee ��������ǥ ���� �߰�
 * [SR140702-044][20140702] shcho ���� ���� IDü�� ���濡 ���� ��������ǥ ��Ʈ ����
 * [SR140709-043][20140709] jwlee, MECO �Ϸ� �� �������� ������ ���� �������� �������ڰ�  ��������ǥ���� �����Ǵ� ���� ����
 * [SR140902-070][201408011] shcho, ��������ǥ ��Ʈ�� �߰��� �ɼ� ���� ����, SYSTEM���� �ڵ����� �Է� �ϵ��� ��� �߰�      
 */
public class PreviewWeldConditionSheetDataHelper {

    private Map<String, Integer> propertyMap = new HashMap<String, Integer>();
    private String weldItemType = "";
    // BOMLine�� ���� �ؾ� �Ѵ�.
    private InterfaceAIFComponent userDefineTarget = null;
    
    private String weldConditionSheetType;

	public PreviewWeldConditionSheetDataHelper(String weldConditionSheetType) {
		this.weldConditionSheetType = weldConditionSheetType;
    }

    public IDataSet getDataSet() throws Exception{
        IDataSet dataSet = null;
        if(weldConditionSheetType!=null && weldConditionSheetType.equalsIgnoreCase("SPOT_TYPE")==true){
        	dataSet = getDataSpot();
        }else if(weldConditionSheetType!=null && weldConditionSheetType.equalsIgnoreCase("CO2_TYPE")==true){
        	dataSet = getDataCo2();
        }
        return dataSet;
    }

    protected IDataSet getDataSpot() throws Exception
    {
        IDataMap dataMap = new RawDataMap();

        propertyMap.put(SDVPropertyConstant.BL_OCCURRENCE_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_CONNECTED_PARTS, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_LINE, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_PRESSURIZATION, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_ETC, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.WELD_NUMBER_OF_SHEETS, SDVPropertyConstant.TYPE_STRING);

        List<HashMap<String, Object>> weldDataList = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> mecoDataList = new ArrayList<HashMap<String, Object>>();
        String compID = "";
        String revID = "";
        String productCode = "";
        String lineCode = "";
        String stationCode = "";
        String robotWorkArea = "";
        String gunNO = "";
        String occ_mvll = "";
        String option = "";
        String weldOptionCodeDescription = "";

        InterfaceAIFComponent component =  null;
        
        // ���������� Target�� ������ ���� ���ǵ� Target�� �켱 �����Ѵ�.
        // [NON-SR][20160217] taeku.jeong MECO�� ���Ե� ���� ����ǥ�� ���� Update�ϴ�
        // ����� �����ϵ��� �߰���.
        if(this.userDefineTarget!=null){
        	component = this.userDefineTarget;  
        }else{
        	component =  AIFUtility.getCurrentApplication().getTargetComponent();
        }
        
        if(component != null && component instanceof TCComponentBOPLine)
        {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOPLine comp = (TCComponentBOPLine) bomWindow.getTopBOMLine();

            // WeldOP ID
            compID = ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            String[] tempCompID = compID.split("-");
            compID = tempCompID[2] + "-" + tempCompID[4] + "-" + tempCompID[5];
            // WeldOP Rev.
            if (tempCompID.length > 6) {
                revID = "-" + tempCompID[6] + " / " + ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            }else{
                revID =  " / " + ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            }
            // Product Code (Product �ڵ�� �ΰ��� BOP �� �������� �ִ°�찡 �߻��Ҽ� �ֱ� ������ �ΰ��� BOP ���� �����;� �Ѵ�)
            productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            // Line Code
            lineCode = ((TCComponentBOPLine)component).parent().parent().getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
            // ���� Code
            stationCode = ((TCComponentBOPLine)component).parent().getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
            // �������� ID �� �������� ����Ǿ� �ִ� �Ϲ� ������ ID �� �����´�
            TCComponent opItemRevision = ((TCComponentBOMLine)component).getItemRevision().getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
            String oPID = opItemRevision.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID);;
            // for (int i = 0; i < (weldOP_IDs.length - 2); i++)
            // {
            // oPID += weldOP_IDs[i];
            // if (i == (weldOP_IDs.length - 3))
            // break;
            // oPID += "-";
            // }

            // weldItem type
            weldItemType = SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM;

            // GunID
            TCComponentBOPLine gunBopLine = getGunNO((TCComponentBOPLine)component);
            if (gunBopLine != null)
                gunNO = gunBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);

            // �κ� BOPLine ������ �����´�
            TCComponentBOPLine robotLine = getRobotNO((TCComponentBOPLine)component);
            // �κ��� ������ ��������� �Ǵ� WorkArea ������ �������� �ʴ´�
            if (robotLine != null)
            {
                // Robot�� WorkArea ���� �����´�
                TCComponentBOMLine robotBopLine = getHavePathNodeBopLine(gunBopLine, comp);
                if (robotBopLine != null)
                {
                    String workAreaName = robotBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    String[] waName = workAreaName.split("-");
                    if (waName.length > 3)
                        robotWorkArea = waName[2] + "-" + waName[3];
                }

                if (robotWorkArea == null || robotWorkArea.equals(""))
                    robotWorkArea = getRobotWorkArea((TCComponentBOPLine)component, oPID, gunNO);
            }

            // GunID �� Display ������ �ٲ۴�
            String[] gunName = gunNO.split("-");
            if (gunName.length > 3){
                gunNO = "";
                for (int i = 3; i < gunName.length; i++) {
                    gunNO += gunName[i];
                    if (gunName.length == (i + 1)) {
                        break;
                    }
                    gunNO += "-";
                }
            }

            // Variant
            occ_mvll = ((TCComponentBOPLine)component).getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);

            weldDataList = getChildrenList(weldDataList, (TCComponentBOPLine) component);

            // Variant Description
            HashMap<String, Object> variantMap = SDVBOPUtilities.getVariant(((TCComponentBOPLine)component).getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));

            option = (String) variantMap.get("printDescriptions");

            // ���õ� �������� ���� ���� Revision ������ MECOList �� �����´�
            List<String> mecoList = new ArrayList<String>();
            TCComponent mecoComponent = ((TCComponentBOPLine)component).getItemRevision().getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            String mecoNO = "";
            if (mecoComponent != null)
            {
                mecoNO = mecoComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                mecoList.add(mecoNO);
            }
            else
            {
                mecoList.add(mecoNO);
            }

            // ������ MECOList �� ������ MECO �� ��µ� ������ �����Ѵ�
            if (!mecoNO.equals(""))
            {
                mecoList = getMecoList(((TCComponentBOPLine)component).getItemRevision(), mecoList);
                mecoDataList = getMecoInfoList(mecoList);
            }
            
            //M7_BOPB_WELD_OPTION_CODE�� Description
            // [SR140902-070][201408011] shcho, ��������ǥ ��Ʈ�� �߰��� �ɼ� ���� ����, SYSTEM���� �ڵ����� �Է� �ϵ��� ��� �߰�   
            if (tempCompID.length > 6) {
                weldOptionCodeDescription = SDVLOVUtils.getLovValueDesciption("M7_BOPB_WELD_OPTION_CODE", tempCompID[6]);
            }
        }

        // ������ ����Ʈ
        IDataSet dataSet = convertToDataSet("weldList", weldDataList);
        // MECO List
        dataSet.addDataSet(convertToDataSet("mecoList", mecoDataList));

        dataMap.put("compID", compID);
        dataMap.put("revID", revID);

        dataMap.put("productCode", productCode);
        dataMap.put("lineCode", lineCode);
        dataMap.put("stationCode", stationCode);
        dataMap.put("gunNO", gunNO);
        dataMap.put("robotWorkArea", robotWorkArea);
        dataMap.put("occ_mvll", occ_mvll);
        // option code
        //dataMap.put("optionCode", variantMap.get("printValues"));
        // option description
        dataMap.put("optionDescription", option);
        // [SR140902-070][201408011] shcho, ��������ǥ ��Ʈ�� �߰��� �ɼ� ���� ����, SYSTEM���� �ڵ����� �Է� �ϵ��� ��� �߰�      
        dataMap.put("weldOptionCodeDescription", weldOptionCodeDescription);

        dataSet.addDataMap("weldCondSheetInfo", dataMap);

        return dataSet;
    }
    
    protected IDataSet getDataCo2() throws Exception
    {
        IDataMap dataMap = new RawDataMap();

        propertyMap.put(SDVPropertyConstant.BL_OCCURRENCE_NAME, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_CONNECTED_PARTS, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_LINE, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_PRESSURIZATION, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.BL_WELD_NOTE_ETC, SDVPropertyConstant.TYPE_STRING);
        propertyMap.put(SDVPropertyConstant.WELD_NUMBER_OF_SHEETS, SDVPropertyConstant.TYPE_STRING);

        List<HashMap<String, Object>> weldDataList = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> mecoDataList = new ArrayList<HashMap<String, Object>>();
        String compID = "";
        String revID = "";
        String productCode = "";
        String lineCode = "";
        String stationCode = "";
        String robotWorkArea = "";
        String gunNO = "";
        String occ_mvll = "";
        String option = "";
        String weldOptionCodeDescription = "";

        InterfaceAIFComponent component =  null;
        
        if(this.userDefineTarget==null){
        	System.out.println("this.userDefineTarget = null");
        }
        
        // ���������� Target�� ������ ���� ���ǵ� Target�� �켱 �����Ѵ�.
        // [NON-SR][20160217] taeku.jeong MECO�� ���Ե� ���� ����ǥ�� ���� Update�ϴ�
        // ����� �����ϵ��� �߰���.
        if(this.userDefineTarget!=null){
        	component = this.userDefineTarget;
        	System.out.println("A");
        }else{
        	component =  AIFUtility.getCurrentApplication().getTargetComponent();
        	System.out.println("B");
        }
        
    	System.out.println("component.getClass().getName() = "+component.getClass().getName());
        
        if(component != null && component instanceof TCComponentBOPLine)
        {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOPLine comp = (TCComponentBOPLine) bomWindow.getTopBOMLine();

            // WeldOP ID
            compID = ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            String[] tempCompID = compID.split("-");
            compID = tempCompID[2] + "-" + tempCompID[4] + "-" + tempCompID[5];
            // WeldOP Rev.
            if (tempCompID.length > 6) {
                revID = "-" + tempCompID[6] + " / " + ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            }else{
                revID =  " / " + ((TCComponentBOPLine)component).getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            }
            // Product Code (Product �ڵ�� �ΰ��� BOP �� �������� �ִ°�찡 �߻��Ҽ� �ֱ� ������ �ΰ��� BOP ���� �����;� �Ѵ�)
            productCode = comp.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            // Line Code
            lineCode = ((TCComponentBOPLine)component).parent().parent().getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
            // ���� Code
            stationCode = ((TCComponentBOPLine)component).parent().getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
            // �������� ID �� �������� ����Ǿ� �ִ� �Ϲ� ������ ID �� �����´�
            TCComponent opItemRevision = ((TCComponentBOMLine)component).getItemRevision().getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
            String oPID = opItemRevision.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID);;
            // for (int i = 0; i < (weldOP_IDs.length - 2); i++)
            // {
            // oPID += weldOP_IDs[i];
            // if (i == (weldOP_IDs.length - 3))
            // break;
            // oPID += "-";
            // }

            // weldItem type
            weldItemType = SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM;

            // GunID
            TCComponentBOPLine gunBopLine = getGunNO((TCComponentBOPLine)component);
            if (gunBopLine != null)
                gunNO = gunBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);

            // �κ� BOPLine ������ �����´�
            TCComponentBOPLine robotLine = getRobotNO((TCComponentBOPLine)component);
            // �κ��� ������ ��������� �Ǵ� WorkArea ������ �������� �ʴ´�
            if (robotLine != null)
            {
                // Robot�� WorkArea ���� �����´�
                TCComponentBOMLine robotBopLine = getHavePathNodeBopLine(gunBopLine, comp);
                if (robotBopLine != null)
                {
                    String workAreaName = robotBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    String[] waName = workAreaName.split("-");
                    if (waName.length > 3)
                        robotWorkArea = waName[2] + "-" + waName[3];
                }

                if (robotWorkArea == null || robotWorkArea.equals(""))
                    robotWorkArea = getRobotWorkArea((TCComponentBOPLine)component, oPID, gunNO);
            }

            // GunID �� Display ������ �ٲ۴�
            String[] gunName = gunNO.split("-");
            if (gunName.length > 3){
                gunNO = "";
                for (int i = 3; i < gunName.length; i++) {
                    gunNO += gunName[i];
                    if (gunName.length == (i + 1)) {
                        break;
                    }
                    gunNO += "-";
                }
            }

            // Variant
            occ_mvll = ((TCComponentBOPLine)component).getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);

            weldDataList = getChildrenList(weldDataList, (TCComponentBOPLine) component);

            // Variant Description
            HashMap<String, Object> variantMap = SDVBOPUtilities.getVariant(((TCComponentBOPLine)component).getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));

            option = (String) variantMap.get("printDescriptions");

            // ���õ� �������� ���� ���� Revision ������ MECOList �� �����´�
            List<String> mecoList = new ArrayList<String>();
            TCComponent mecoComponent = ((TCComponentBOPLine)component).getItemRevision().getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            String mecoNO = "";
            if (mecoComponent != null)
            {
                mecoNO = mecoComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                mecoList.add(mecoNO);
            }
            else
            {
                mecoList.add(mecoNO);
            }

            // ������ MECOList �� ������ MECO �� ��µ� ������ �����Ѵ�
            if (!mecoNO.equals(""))
            {
                mecoList = getMecoList(((TCComponentBOPLine)component).getItemRevision(), mecoList);
                mecoDataList = getMecoInfoList(mecoList);
            }
            
            //M7_BOPB_WELD_OPTION_CODE�� Description
            // [SR140902-070][201408011] shcho, ��������ǥ ��Ʈ�� �߰��� �ɼ� ���� ����, SYSTEM���� �ڵ����� �Է� �ϵ��� ��� �߰�   
            if (tempCompID.length > 6) {
                weldOptionCodeDescription = SDVLOVUtils.getLovValueDesciption("M7_BOPB_WELD_OPTION_CODE", tempCompID[6]);
            }
        }

        // ������ ����Ʈ
        IDataSet dataSet = convertToDataSet("weldList", weldDataList);
        // MECO List
        dataSet.addDataSet(convertToDataSet("mecoList", mecoDataList));

        dataMap.put("compID", compID);
        dataMap.put("revID", revID);

        dataMap.put("productCode", productCode);
        dataMap.put("lineCode", lineCode);
        dataMap.put("stationCode", stationCode);
        dataMap.put("gunNO", gunNO);
        dataMap.put("robotWorkArea", robotWorkArea);
        dataMap.put("occ_mvll", occ_mvll);
        // option code
        //dataMap.put("optionCode", variantMap.get("printValues"));
        // option description
        dataMap.put("optionDescription", option);
        // [SR140902-070][201408011] shcho, ��������ǥ ��Ʈ�� �߰��� �ɼ� ���� ����, SYSTEM���� �ڵ����� �Է� �ϵ��� ��� �߰�      
        dataMap.put("weldOptionCodeDescription", weldOptionCodeDescription);

        dataSet.addDataMap("weldCondSheetInfo", dataMap);

        return dataSet;
    }

    /**
     * ���������� ����Ǿ� �ִ� TargetOP �� Plant ������ �����´�
     *
     *
     * @method getRobotWorkArea
     * @date 2013. 12. 2.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getRobotWorkArea(TCComponentBOPLine weldOP, String opID, String gunID) throws TCException
    {
        String robotWorkID = "";
//        boolean plantCheck = false;
        TCComponentBOPLine stationOP = (TCComponentBOPLine) weldOP.parent();
        TCComponentBOPLine normalOP = null;
        AIFComponentContext[] stationChilds = stationOP.getChildren();
        for (AIFComponentContext stationChild : stationChilds)
        {
            TCComponentBOPLine child = (TCComponentBOPLine) stationChild.getComponent();
            if (child.getProperty(SDVPropertyConstant.BL_ITEM_ID).equals(opID))
                normalOP = child;
//            if (child.getType().equals(SDVTypeConstant.PLANT_OPAREA_ITEM))
//                plantCheck = true;
        }
        robotWorkID = getPlantResource(normalOP, gunID);
//        if (plantCheck)
//            robotWorkID = getPlantResource(stationOP, gunID);
//        else
//            robotWorkID = getPlantResource(normalOP, gunID);

        return robotWorkID;
    }

    private TCComponentBOMLine getHavePathNodeBopLine(TCComponentBOPLine gunItem, TCComponentBOPLine targetOP) throws TCException
    {
        if (gunItem == null || targetOP == null) return null;

        TCComponentBOMLine plantLine = getPlantBOMLine(targetOP);
        TCComponentBOMWindow plantWindow = plantLine.window();

        TCComponentMEAppearancePathNode[] linkedAppearances = gunItem.askLinkedAppearances(false);
        if (linkedAppearances == null) return null;

        TCComponentBOMLine linkedBOMLine = null;

        for (TCComponentMEAppearancePathNode linkedAppearance : linkedAppearances)
        {
        	try {
        		
        		linkedBOMLine = plantWindow.getBOMLineFromAppearancePathNode(linkedAppearance, plantLine);
        	} catch (Exception e) {
        		e.getStackTrace();
        	}
        }
        if (linkedBOMLine == null) return null;
        return linkedBOMLine.parent();
    }

    private TCComponentBOMLine getPlantBOMLine(TCComponentBOMLine selectBOMLine) {
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

    private String getPlant(TCComponentItemRevision revision) throws TCException {
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

    /**
     *  Target OP ������ �ִ� Plant ������ �����´�
     *
     * @method getPlantResource
     * @date 2013. 12. 2.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getPlantResource(TCComponentBOPLine targetComp, String gunID) throws TCException
    {
        String plantID = "";

        AIFComponentContext[] targetChilds = targetComp.getChildren();
        for (AIFComponentContext targetChild : targetChilds)
        {
            TCComponentBOPLine tempOPchild = (TCComponentBOPLine)targetChild.getComponent();
            if (tempOPchild.getItem().getType().equals(SDVTypeConstant.PLANT_OPAREA_ITEM))
            {
                AIFComponentContext[] plantChilds = tempOPchild.getChildren();
                for (AIFComponentContext plantChild : plantChilds)
                {
                    TCComponentBOPLine tempPlantchild = (TCComponentBOPLine)plantChild.getComponent();
                    if (tempPlantchild.getProperty(SDVPropertyConstant.BL_ITEM_ID).equals(gunID))
                    {
                        String workAreaName = tempOPchild.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                        String[] waName = workAreaName.split("-");
                        plantID = waName[2] + "-" + waName[3];
                        break;
                    }
                }
            }
        }
        return plantID;
    }

    /**
     *  ������ ���������� Gun ID �� �����´�
     *
     * @method getGunNO
     * @date 2013. 12. 2.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOPLine getGunNO(TCComponentBOPLine weldOP) throws TCException
    {
        AIFComponentContext[] weldOPChilds = weldOP.getChildren();
        for (AIFComponentContext weldOPChild : weldOPChilds)
        {
            TCComponentBOPLine gunComponent = (TCComponentBOPLine) weldOPChild.getComponent();
            if (gunComponent.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM))
                return gunComponent;
        }
        return null;
    }

    /**
     *  ������ ���������� Robot ID �� �����´�
     *
     * @method getRobotNO
     * @date 2014. 1. 29.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOPLine getRobotNO(TCComponentBOPLine weldOP) throws TCException
    {
        AIFComponentContext[] weldOPChilds = weldOP.getChildren();
        for (AIFComponentContext weldOPChild : weldOPChilds)
        {
            TCComponentBOPLine gunComponent = (TCComponentBOPLine) weldOPChild.getComponent();
            if (gunComponent.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM))
                return gunComponent;
        }
        return null;
    }

    /**
     * ����ڰ� ������ Component ������ �ڽ� Component���� ������ �����´�.
     *
     * @method getChildrenList
     * @date 2013. 10. 28.
     * @param
     * @return List<HashMap<String,Object>>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    private List<HashMap<String, Object>> getChildrenList(List<HashMap<String, Object>> dataList, TCComponentBOPLine parentLine) throws Exception
    {
    	AIFComponentContext[] context = parentLine.getChildren();
//        if(parentLine.getChildrenCount() > 0)
          if(context.length > 0)
        {
            for(int i = 0; i < context.length; i++)
            {
                if(context[i].getComponent() instanceof TCComponentBOPLine)
                {
                    TCComponentBOPLine childLine = (TCComponentBOPLine)context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if(weldItemType.equals(type))
                    {
                        HashMap<String, Object> dataMap = convertComponent(childLine);
                        // �������� bl_connected_lines �� �ִ� Item �� �Ӽ� ���� �����´�
                        dataMap = getEndItemProperties(dataMap);

                        dataList.add(dataMap);
                    }
                    else
                    {
                        getChildrenList(dataList, (TCComponentBOPLine)context[i].getComponent());
                    }
                }
            }
        }

        return dataList;
    }

    /**
     * �θ� ���� ��������
     *
     * @method getParentInfo
     * @date 2013. 11. 11.
     * @param
     * @return HashMap<String,Object>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public IDataMap getParentInfo(TCComponentBOPLine weldOP, IDataMap dataMap, String type) throws Exception
    {
        if (weldOP.parent() != null) {
            TCComponentBOPLine parent = (TCComponentBOPLine) weldOP.parent();
            if (parent.getItem().getType().equals(type))
            {
                if(type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
                {
                    String shop_code = parent.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_ID, shop_code);
                }
                else if(type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
                {
                    String line_code = parent.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
                    dataMap.put(type + SDVPropertyConstant.LINE_REV_CODE, line_code);
                }
                else if(type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
                {
                    String station_code = parent.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);
                    dataMap.put(type + SDVPropertyConstant.STATION_STATION_CODE, station_code);
                }
                else if(type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
                {
                    String weldOp_ID = parent.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    String weldOp_rev = parent.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_ID, weldOp_ID);
                    dataMap.put(type + SDVPropertyConstant.BL_ITEM_REV_ID, weldOp_rev);
                }
            }
            else
            {
                return getParentInfo(parent, dataMap, type);
            }
        }
        return dataMap;
    }

    /**
    *
    *
    * @method convertToDataSet
    * @date 2013. 11. 27.
    * @param
    * @return IDataSet
    * @exception
    * @throws
    * @see
    */
   private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList)
   {
       IDataSet dataSet = new DataSet();
       IDataMap dataMap = new RawDataMap();
       dataMap.put(dataName, dataList, IData.TABLE_FIELD);
       dataSet.addDataMap(dataName, dataMap);

       return dataSet;
   }


    /**
     * Component�� �Ӽ��� ������ HashMap���� �����Ѵ�.
     *
     * @method convertComponent
     * @date 2013. 10. 28.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> convertComponent(TCComponentBOPLine component) throws TCException
    {
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        Iterator<String> iterator = propertyMap.keySet().iterator();
        while(iterator.hasNext())
        {
            String key = iterator.next();
            int value = (int) propertyMap.get(key);

            switch(value)
            {
                case 0x01 : dataMap.put(key, component.getProperty(key)); break;
                case 0x02 : dataMap.put(key, component.getIntProperty(key)); break;
                case 0x03 : dataMap.put(key, component.getDoubleProperty(key)); break;
                case 0x04 : dataMap.put(key, component.getProperty(key)); break;
                case 0x05 : dataMap.put(key, component.getReferenceProperty(key)); break;
                default : break;
            }
        }

        return dataMap;
    }

    /**
     *  End �����ۿ� bl_connected_lines �� ��� �ִ� ������ �������� �˻��Ͽ�
     *  s7_material(����) / s7_thickness(�β�) ������ �����´�
     *
     * @method getEndItemProperties
     * @date 2013. 12. 4.
     * @param
     * @return HashMap<String,Object>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getEndItemProperties(HashMap<String, Object> dataMap) throws Exception
    {
        String[] connectedItems = ((String) dataMap.get(SDVPropertyConstant.BL_CONNECTED_PARTS)).split(",");
        String materialThickness = "";
        int MfgNumber = 0;
        if (!connectedItems[0].isEmpty())
        {
            String material;
            String thickness;
            MfgNumber = connectedItems.length;

            for (int i = 0; i < connectedItems.length; i++)
            {
                String endItemID = connectedItems[i].substring(0, connectedItems[i].indexOf("/"));  //[SR140611-027][20140611] jwlee ��������ǥ ���� �߰�
                String endItemRev = connectedItems[i].substring(connectedItems[i].indexOf("/") + 1);    //[SR140611-027][20140611] jwlee ��������ǥ ���� �߰�
                endItemID = endItemID.trim();
                endItemRev = endItemRev.trim();
                TCComponentItemRevision findEndItemRevision = CustomUtil.findItemRevision(SDVTypeConstant.EBOM_VEH_PART_REV, endItemID, endItemRev);
                //material = findEndItemRevision.getProperty(SDVPropertyConstant.S7_MATERIAL);
                TCComponent referenceItem = findEndItemRevision.getReferenceProperty(SDVPropertyConstant.S7_MATERIAL);
                if (referenceItem != null)
                {
                    material = referenceItem.getProperty(SDVPropertyConstant.S7_SES_CODE);
                    thickness = findEndItemRevision.getProperty(SDVPropertyConstant.S7_THICKNESS);
                    materialThickness += thickness + "t(" + material + ")";
                    if((i+1) != connectedItems.length)
                        materialThickness += "+";
                }
            }
        }
        dataMap.put("MaterialThickness", materialThickness);
        dataMap.put("MfgNumber", MfgNumber);
        return dataMap;
    }


    /**
     * Ÿ�� ������ ������ ���� ���� ������ ������ ��� �ִ� MECO List �� �����´�
     *
     * @method getMecoList
     * @date 2013. 12. 10.
     * @param
     * @return List<String>
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public static List<String> getMecoList(TCComponentItemRevision target, List<String> mecoList) throws Exception
    {
        TCComponentItemRevision revision = CustomUtil.getPreviousRevision(target);

        if (revision != null)
        {
            TCComponent mecoComponent = revision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            String mecoNo = "";
            if (mecoComponent != null)
            {
                mecoNo = mecoComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                mecoList.add(mecoNo);
            }
            else
            {
                mecoList.add(mecoNo);
            }
            getMecoList(revision, mecoList);
        }
        return mecoList;
    }


    /**
     * MECO Item ID �� �޾Ƽ� MECO �������� ���� �Ѵ�
     *
     * @method getMecoInfoList
     * @date 2013. 12. 11.
     * @param
     * @return List<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    public static List<HashMap<String, Object>> getMecoInfoList(List<String> list) throws Exception
    {
        List<HashMap<String, Object>> DataList = new ArrayList<HashMap<String, Object>>();
        char lowerAlphabat = 97;
        char upperAlphabat = 65;
        int number = 0;

        for (int i = list.size(); i > 0; i--)
        {
            HashMap<String, Object> mecoInfo = new HashMap<String, Object>();
            TCComponentItemRevision mecoRev = CustomUtil.findItemRevision(SDVTypeConstant.MECO_ITEM_REV, list.get(i - 1), "000");
            //TCComponentItemRevision mecoRev = CustomUtil.findLatestItemRevision(SDVTypeConstant.MECO_ITEM_REV, list.get(i - 1));
            if (mecoRev == null) continue;

            // MECO Type �� ���� ������ ���� �ο��Ѵ�
            if (mecoRev.getProperty(SDVPropertyConstant.MECO_TYPE).equals("PBI"))
            {
                mecoInfo.put("changeNo", Integer.toString(number));
                number++;
            }
            else if (mecoRev.getProperty(SDVPropertyConstant.MECO_TYPE).equals("MEW"))
            {
                mecoInfo.put("changeNo", Character.toString(upperAlphabat));
                upperAlphabat++;
            }
            else
            {
                mecoInfo.put("changeNo", Character.toString(lowerAlphabat));
                lowerAlphabat++;
            }

            // ���� (MECO ������)
            // [SR140709-043][20140709] jwlee, MECO �Ϸ� �� �������� ������ ���� �������� �������ڰ�  ��������ǥ���� �����Ǵ� ���� ���� (���� PROCESS_STAGE_LIST���� �������� ��¥�� MECO ���� Task signoff ��¥�� ���������� ����) 
            Date releaseDate = null;
            HashMap<String, TCComponent[]> signoffs = new HashMap<String, TCComponent[]>();
            AIFComponentContext[] ctx = mecoRev.whereReferenced();
            for (int j = 0; j < ctx.length; j++) {
                TCComponent component = (TCComponent) ctx[j].getComponent();
                if(component instanceof TCComponentTask) {
                    TCComponentTask task = (TCComponentTask) component;
                    signoffs = SDVProcessUtils.getSignOffs(signoffs, task);
                    if(signoffs.containsKey("Team Leader")){
                        TCComponentSignoff reader = (TCComponentSignoff) signoffs.get("Team Leader")[0];
                        releaseDate = reader.getDecisionDate();
                        break;
                    }
                }
            }
            if (releaseDate != null)
                mecoInfo.put(SDVPropertyConstant.ITEM_DATE_RELEASED, SDVStringUtiles.dateToString(releaseDate, "yyyy-MM-dd"));

            // MECO ID (MECO ID)
            mecoInfo.put(SDVPropertyConstant.ITEM_ITEM_ID, mecoRev.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

            // ���泻��
            mecoInfo.put(SDVPropertyConstant.ITEM_OBJECT_DESC, mecoRev.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));

            // ���
            TCComponentUser owningUser = (TCComponentUser) mecoRev.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
            mecoInfo.put(SDVPropertyConstant.ITEM_OWNING_USER, getUserName(owningUser));

            // ����
            mecoInfo.put("APPR", getMECOTeamLeaderSignoff(mecoRev));

            DataList.add(mecoInfo);
        }
        return DataList;
    }

    /**
     * MECO ������ �� ���� �̸��� ��ȸ�Ѵ�.
     *
     * @method getMECOTeamLeaderSignoff
     * @date 2013. 11. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getMECOTeamLeaderSignoff(TCComponentItemRevision mecoRev) throws TCException
    {
        HashMap<String, TCComponent[]> signoffs = new HashMap<String, TCComponent[]>();
        AIFComponentContext[] ctx = mecoRev.whereReferenced();
        if(ctx != null)
        {
            for(int i = 0; i < ctx.length; i++)
            {
                TCComponent component = (TCComponent) ctx[i].getComponent();
                if(component instanceof TCComponentTask)
                {
                    TCComponentTask task = (TCComponentTask) component;
                    signoffs = SDVProcessUtils.getSignOffs(signoffs, task);
                    if(signoffs.containsKey("Team Leader"))
                    {
                        TCComponentSignoff reader = (TCComponentSignoff) signoffs.get("Team Leader")[0];
                        TCComponentUser user = reader.getGroupMember().getUser();
                        return getUserName(user);
                    }
                }
            }
        }
        return "";
    }

    /**
     * User �̸��� �����´�
     *
     * @method getUserName
     * @date 2013. 12. 11.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getUserName(TCComponentUser user) throws TCException
    {
        String userName = "";
        TCComponentPerson person = (TCComponentPerson) user.getUserInformation().get(0);
        userName = person.getProperty("user_name");
        /*if(configId == 0) {
            if(person != null) {
            }
        } else {
            userName = user.getOSUserName();
        }*/

        return userName;
    }

    /**
     * MECO ������ �����´�
     *
     * @method getMECO
     * @date 2013. 12. 16.
     * @param
     * @return TCComponentItem
     * @exception
     * @throws
     * @see
     */
    public static TCComponentItem getMECO(String mecoID) throws Exception
    {
        TCComponentItem mecoItem = null;
        if (mecoID != null && !"".equals(mecoID))
            mecoItem = SDVBOPUtilities.FindItem(mecoID, SDVTypeConstant.MECO_ITEM);

        return mecoItem;
    }

   /**
    * MECOItemRevision ���� ���ϴ� �������� Target Item�� �����´�
    *
    * @method getMecoTargetList
    * @date 2013. 12. 16.
    * @param
    * @return List<TCComponentItemRevision>
    * @exception
    * @throws
    * @see
    */
    @SuppressWarnings("unused")
    public static List<TCComponentItemRevision> getMecoTargetList(TCComponentItemRevision mecoItemRevision, String folderType, String targetType) throws TCException
    {
        List<TCComponentItemRevision> targetList = new ArrayList<TCComponentItemRevision>();
        TCComponentFolder targetFolder = null;
        //TCComponent[] tmpComponent = mecoItemRevision.getRelatedComponents(TcDefinition.TC_SPECIFICATION_RELATION);
        //TCComponent[] adsd = mecoItemRevision.getClassificationObjects();
        TCComponentContextList tmpComponent = mecoItemRevision.getRelatedList();
        TCComponent[] solutionFolderChilds = mecoItemRevision.getRelatedComponents(folderType);//getRelatedComponents(TcDefinition.TC_SPECIFICATION_RELATION);
        //CustomUtil.getDatasets(itemRevision, relationType, dataType)

        for (TCComponent Child : solutionFolderChilds)
        {
            if (Child.getType().equals(targetType))
            {
                targetList.add((TCComponentItemRevision) Child);
            }
        }

        return targetList;
    }

    /**
     * [NON-SR][20160217] taeku.jeong ��������ǥ�� ���� Update �ϱ����� ��������ǥ �˻� ����� ������ �����Ѵ�.
     * ��������ǥ ���� Update ����� �Ǵ� Object�� �о��.
     * @return
     */
    public InterfaceAIFComponent getUserDefineTarget() {
		return userDefineTarget;
	}

    /**
     * [NON-SR][20160217] taeku.jeong ��������ǥ�� ���� Update �ϱ����� ��������ǥ �˻� ����� ������ �����Ѵ�.
     * ��������ǥ ���� Update ����� �Ǵ� weld Operation�� BOMLine�� Target���� ���� �����Ѵ�.
     * @param targetBOMLine
     */
	public void setUserDefineTarget( TCComponentBOPLine targetBOMLine) {
		this.userDefineTarget = (InterfaceAIFComponent)targetBOMLine;
	}

}
