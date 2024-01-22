package com.symc.plm.me.sdv.operation.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawData;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.common.BOMTreeTableModel;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.soa.client.model.Property;

public class ReportBOPOperationMasterListOperation extends SimpleSDVExcelOperation {

    private Registry registry;

    private BOMTreeTableModel tableModel = null;

    private String productCode = "";
    private String processType = "";
    private String operationType = "";
    private int[] selectedValueFromDialog;

    @Override
    public void executeOperation() throws Exception {
        try {
            registry = Registry.getRegistry(this);

            selectedValueFromDialog = (int[]) localDataMap.get("selectedValue").getValue();

            IDataSet dataSet = getData();
            if (dataSet != null) {
                String defaultFileName = productCode + "_" + registry.getString("OperationMasterList.FileName", "OperationMasterList") + "_" + ExcelTemplateHelper.getToday("yyyyMMdd");
                transformer.print(mode, templatePreference, defaultFileName, dataSet);
            }
        } catch (Exception e) {
            setExecuteError(e);
            // MessageBox�� ������ �޽���
            // �������� ������ default �޽����� �����ش�.
            // setErrorMessage("");
        }
    }

    @Override
    protected IDataSet getData() throws Exception {
        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

        String compID = "";
        String revisionRule = "";
        String revRuleStandardDate = "";
        String variantRule = "";

        InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
        if (component != null && component instanceof TCComponentBOPLine) {
            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
            TCComponentBOMLine topBOMLine = bomWindow.getTopBOMLine();
            TCComponentBOPLine comp = (TCComponentBOPLine) component;

            // product Code
            productCode = topBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            // shop or line or station - id
            compID = comp.getItemRevision().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

            // process type
            processType = topBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE);
            if (processType.startsWith("B")) {
                operationType = SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM;
            } else if (processType.startsWith("P")) {
                operationType = SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM;
            } else if (processType.startsWith("A")) {
                operationType = SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM;
            }

            // Revision Rule
            TCComponentRevisionRule bomWindowRevisionRule = bomWindow.getRevisionRule();
            revisionRule = bomWindowRevisionRule.toString();

            // Revision Rule ������
            Date rule_date = bomWindowRevisionRule.getDateProperty("rule_date");
            if (rule_date != null) {
                revRuleStandardDate = new SimpleDateFormat("yyyy-MM-dd").format(rule_date);
            } else {
                revRuleStandardDate = ExcelTemplateHelper.getToday("yyyy-MM-dd");
            }

            // Variant
            variantRule = SDVBOPUtilities.getBOMConfiguredVariantSetToString(bomWindow);

            // BOMTreeTableModel
            MFGLegacyApplication application = SDVBOPUtilities.getMFGApplication();
            BOMTreeTable treeTable = application.getViewableTreeTable();
            tableModel = (BOMTreeTableModel) treeTable.getTreeTableModel();

            dataList = getChildrenList(dataList, (TCComponentBOPLine) component);
        }

        IDataSet dataSet = convertToDataSet("operationList", dataList);
        IDataMap dataMap = new RawDataMap();
        IData data = new RawData();

        data.setValue(selectedValueFromDialog);
        dataMap.put("selectedValueFromDialog", data);

        dataMap.put("productCode", productCode);
        dataMap.put("processType", processType);
        dataMap.put("compID", compID);
        dataMap.put("revisionRule", revisionRule);
        dataMap.put("revRuleStandardDate", revRuleStandardDate);
        dataMap.put("variantRule", variantRule);
        dataMap.put("excelExportDate", ExcelTemplateHelper.getToday("yyyy-MM-dd HH:mm"));
        dataSet.addDataMap("additionalInfo", dataMap);

        return dataSet;
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
    private List<HashMap<String, Object>> getChildrenList(List<HashMap<String, Object>> dataList, TCComponentBOPLine parentLine) throws Exception {
        String parent_type = parentLine.getItem().getType();
        if (parent_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || parent_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            BOMLineNode node = tableModel.getNode(parentLine);
            node.loadChildren();
        }

        if (parentLine.getChildrenCount() > 0) {
            AIFComponentContext[] context = parentLine.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();

                    // ���Ҵ� Line ����
                    if (type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
                        if (SDVBOPUtilities.isAssyTempLine(childLine)) {
                            continue;
                        }
                    }

                    if (SDVTypeConstant.EBOM_MPRODUCT.equals(type) || SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM.equals(type)) {
                        continue;
                    }

                    if (operationType.equals(type)) {
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();

                        // ����(�۾�ǥ�ؼ�)
                        dataMap = getOperationInfo(childLine, dataMap);

                        // �ð� ����
                        dataMap = getTimeInfo(childLine, dataMap);

                        // ���� ����
                        dataMap = getStationInfo(childLine, dataMap);

                        // ���� ����
                        dataMap = getToolList(childLine, dataMap);

                        // ���� ����
                        dataMap = getEquipmentList(childLine, dataMap);

                        // ��Ÿ ����
                        dataMap = getETCInfo(childLine, dataMap);

                        // User ���� �ɼ�
                        if (selectedValueFromDialog.length > 0) {
                            for (int num : selectedValueFromDialog) {
                                if (num == 1) {
                                    // �۾� ����
                                    dataMap = getWorkInfoList(childLine, dataMap);
                                } else if (num == 2) {
                                    // End Item
                                    dataMap = getEndItemList(childLine, dataMap);
                                } else if (num == 3) {
                                    // ������ ����
                                    dataMap = getSubsidiaryList(childLine, dataMap);
                                }
                            }
                        }

                        dataList.add(dataMap);
                    } else {
                        getChildrenList(dataList, (TCComponentBOPLine) context[i].getComponent());
                    }
                }
            }
        }

        return dataList;
    }

    /**
     * ����(�۾�ǥ�ؼ�)
     * 
     * @method getOperationInfo
     * @date 2014. 2. 5.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getOperationInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        // ����(�۾�ǥ�ؼ�) NO.
        dataMap.put(SDVPropertyConstant.BL_ITEM_ID, operation.getProperty(SDVPropertyConstant.BL_ITEM_ID));

        // ���� REV.
        dataMap.put(SDVPropertyConstant.BL_ITEM_REV_ID, operation.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));

        // ������
        dataMap.put(SDVPropertyConstant.BL_OBJECT_NAME, operation.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

        // ���� ������
        System.out.println("operation = "+operation);
        String a = operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
        
        dataMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));

        // option code, option description
        HashMap<String, Object> option = SDVBOPUtilities.getVariant(operation.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION));
        dataMap.put("optionCode", option.get("printValues"));
        dataMap.put("optionDescription", option.get("printDescriptions"));

        // DR
        dataMap = getDRProperty(operation, dataMap);

        // KPC/Ư������
        if (processType.startsWith("A")) {
            // KPC(����)
            dataMap = getKPCProperty(operation, dataMap);
        } else {
            // Ư������(��ü/����)
            dataMap.put("specialStation", operation.getItemRevision().getLogicalProperty(SDVPropertyConstant.OPERATION_REV_KPC) ? "Y" : "");
        }
        
        // Ư�� Ư�� �Ӽ� �߰�
        String specialCharacter = operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_SPECIAL_CHARACTERISTIC);
        dataMap.put("specialCharacterristic",  specialCharacter == null ? "" : specialCharacter );

        return dataMap;
    }

    /**
     * DR �Ӽ� End Item DR �Ӽ� �켱( DR1 > DR2 > DR3 ) End Item DR �Ӽ��� ������ ���� DR �Ӽ�
     * 
     * @method getDRProperty
     * @date 2013. 11. 11.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getDRProperty(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        // End Item DR �Ӽ� �켱( DR1 > DR2 > DR3 )
        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type)) {
                        String dr = childLine.getItemRevision().getProperty("s7_REGULATION");
                        if (!dr.equals(".") && !dr.equals("")) {
                            if (dataMap.get("drProperty") == null) {
                                dataMap.put("drProperty", dr);
                            } else {
                                if (dr.compareToIgnoreCase((String) dataMap.get("drProperty")) < 0) {
                                    dataMap.put("drProperty", dr);
                                }
                            }
                        }
                    }
                }
            }
        }

        // End Item DR �Ӽ��� ������ ���� DR �Ӽ�
        if (dataMap.get("drProperty") == null) {
            dataMap.put("drProperty", operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_DR));
        }

        return dataMap;
    }

    /**
     * ���� Activity �� KPC �Ӽ��� ���� ��� Y�� ǥ��(����)
     * 
     * @method getKPCProperty
     * @date 2013. 11. 11.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getKPCProperty(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operation;
        TCComponent root = bvrOperation.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);
        if (root != null) {
            if (root instanceof TCComponentCfgActivityLine) {
                TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                if (children != null) {
                    for (TCComponent child : children) {
                        String activityControlPoint = child.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);
                        // KPC ���� �߰��� ���� ����
                        String activityControlBasis = child.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS);
                        //////////////////////////////////////////////////////////////////////////////////////////////////
                        /*
                         * "KPC����" ��� ���� ����
                         *  AL_CONTROL_POINT ���� �ְ� "TR����" �� �ƴϸ� ���
                         */
//                        if (!activityControlPoint.equals("") || !activityControlBasis.equals("")) {
//                            dataMap.put("isExistKPC", "Y");
//                        // KPC ���� �߰��� ���� ����
//                            dataMap.put("kpcContents", activityControlPoint + ":" + activityControlBasis);
//                            break;
//                        }
                        if (!activityControlPoint.equals("") || !activityControlPoint.equals("TR����")) {
                        	dataMap.put("isExistKPC", "Y");
                        	// KPC ���� �߰��� ���� ����
                        	 if(activityControlPoint.length() > 2) {
                       		  activityControlPoint = activityControlPoint.substring(2);
                       	  	}
                        	dataMap.put("kpcContents", activityControlPoint + ":" + activityControlBasis);
                        	break;
                        }
                        ///////////////////////////////////////////////////////////////////////////////////////////////////
                    }
                }
            }
        }

        return dataMap;
    }

    /**
     * �ð� ����
     * 
     * @method getTimeInfo
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getTimeInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        Double time1 = 0.0;
        Double time2 = 0.0;
        Double time3 = 0.0;
        Double time4 = 0.0;
        Double time5 = 0.0;
        Double cycleTime = 0.0;
        Double allowance = 0.0;

        // �δ� ��� - Line �켱
        TCComponentBOPLine shop = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
        TCComponentBOPLine line = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);

        if (shop != null) {
            String shop_allowance = shop.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_ALLOWANCE);
            if (!shop_allowance.trim().equals("")) {
                allowance = Double.valueOf(shop_allowance);
            }
        }

        if (line != null) {
            String line_allowance = line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_ALLOWANCE);
            if (!line_allowance.trim().equals("")) {
                allowance = Double.valueOf(line_allowance);
            }
        }

        // Activity �ð�
        TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operation;
        TCComponent root = bvrOperation.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);
        if (root != null) {
            if (root instanceof TCComponentCfgActivityLine) {
                TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                if (children != null) {
                    for (TCComponent child : children) {
                        String activity_system_category = child.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).getStringValue();
                        // �۾��� ���� �ð�
                        if (activity_system_category.equals("01")) {
                            time1 += (child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME) * child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY));
                        }
                        // �ڵ� �ð�
                        else if (activity_system_category.equals("02")) {
                            time2 += (child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME) * child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY));
                        }
                        // ���� �ð�
                        else if (activity_system_category.equals("03")) {
                            time3 += (child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME) * child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY));
                        }
                    }

                    // [SR140905-044][20140918] bykim, �ð� ������ �߸��Ǿ� ����
                    // ǥ�ؽð�(�۾��� ���� �ð� * �δ� ���)
                    time4 = time1 * allowance;

                    // �۾� �ð�(ǥ�� �ð� + ���� �ð�)
                    time5 = time3 + time4;

                    // cycle time(�ڵ� �ð� + �۾��� ���� �ð�)
                    cycleTime = time1 + time2;
                }
            }
        }

        // �� �ð�(�ִ�)
        if (operation.getItem().getLogicalProperty(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK)) {
            dataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, ((Double) (Math.ceil(time5 * 10.0) / 10.0)).toString());
        } else {
            dataMap.put(SDVPropertyConstant.OPERATION_MAX_WORK_TIME_CHECK, "");
        }

        // �� �ð�(��ǥ����)
        if (operation.getItem().getLogicalProperty(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK)) {
            dataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, ((Double) (Math.ceil(time5 * 10.0) / 10.0)).toString());
        } else {
            dataMap.put(SDVPropertyConstant.OPERATION_REP_VEHICLE_CHECK, "");
        }

        dataMap.put("time1", Math.ceil(time1 * 10.0) / 10.0);
        dataMap.put("time2", Math.ceil(time2 * 10.0) / 10.0);
        dataMap.put("time3", Math.ceil(time3 * 10.0) / 10.0);
        dataMap.put("time4", Math.ceil(time4 * 10.0) / 10.0);
        dataMap.put("time5", Math.ceil(time5 * 10.0) / 10.0);
        dataMap.put("cycleTime", Math.ceil(cycleTime * 10.0) / 10.0);
        dataMap.put("allowance", allowance);

        return dataMap;
    }

    /**
     * ���� ����
     * 
     * @method getStationInfo
     * @date 2014. 2. 5.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getStationInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        // Line Code, Line Revision
        TCComponentBOPLine line = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
        if (line != null) {
            dataMap.put(SDVPropertyConstant.LINE_REV_CODE, line.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
            dataMap.put("line_revision", line.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
        }

        if (processType.startsWith("B") || processType.startsWith("P")) {
            // Station Code, Station Revision
            TCComponentBOPLine station = getParentBOPLine(operation, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
            if (station != null) {
                dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, station.getItemRevision().getProperty(SDVPropertyConstant.STATION_LINE) + "-" + station.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE));
                dataMap.put("station_revision", station.getProperty(SDVPropertyConstant.BL_ITEM_REV_ID));
            }
        } else {
            // Station No(����)
            dataMap.put(SDVPropertyConstant.STATION_STATION_CODE, operation.getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO));
        }

        // �۾���, �۾���, ��ġ
        TCComponentItem operation_item = operation.getItem();
        dataMap.put(SDVPropertyConstant.OPERATION_WORKER_CODE, operation_item.getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE));
        dataMap.put(SDVPropertyConstant.OPERATION_PROCESS_SEQ, operation_item.getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ));
        dataMap.put(SDVPropertyConstant.OPERATION_WORKAREA, operation_item.getProperty(SDVPropertyConstant.OPERATION_WORKAREA));

        return dataMap;
    }

    /**
     * ���� ����
     * 
     * @method getToolList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getToolList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> toolList = new ArrayList<HashMap<String, Object>>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    if (SDVTypeConstant.BOP_PROCESS_TOOL_ITEM.equals(childLine.getItem().getType())) {
                        HashMap<String, Object> toolMap = new HashMap<String, Object>();

                        // ���� ID
                        toolMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // ���� ������
                        toolMap.put(SDVPropertyConstant.TOOL_ENG_NAME, childLine.getItemRevision().getProperty(SDVPropertyConstant.TOOL_ENG_NAME));

                        // ���� ����
                        toolMap.put(SDVPropertyConstant.TOOL_SPEC_ENG, childLine.getItemRevision().getProperty(SDVPropertyConstant.TOOL_SPEC_ENG));

                        // TORQUE
                        toolMap.put(SDVPropertyConstant.BL_NOTE_TORQUE, childLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE));
                        toolMap.put(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE, childLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE));

                        // ���� ���� - ������ ǥ��
                        String quantity = childLine.getProperty(SDVPropertyConstant.BL_QUANTITY).split("\\.")[0];
                        toolMap.put(SDVPropertyConstant.BL_QUANTITY, quantity);

                        toolList.add(toolMap);
                    }
                }
            }
        }
        dataMap.put("toolList", toolList);

        return dataMap;
    }

    /**
     * ���� ����
     * 
     * @method getEquipmentList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getEquipmentList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> equipmentList = new ArrayList<HashMap<String, Object>>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if (SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM.equals(type) || SDVTypeConstant.BOP_PROCESS_GUN_ITEM.equals(type)) {
                        HashMap<String, Object> equipmentMap = new HashMap<String, Object>();

                        // ���� ID
                        equipmentMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // �����
                        equipmentMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

                        // ���� ����/����
                        equipmentMap.put(SDVPropertyConstant.EQUIP_SPEC_ENG, childLine.getItemRevision().getProperty(SDVPropertyConstant.EQUIP_SPEC_ENG));
                        equipmentMap.put(SDVPropertyConstant.EQUIP_PURPOSE_ENG, childLine.getItemRevision().getProperty(SDVPropertyConstant.EQUIP_PURPOSE_ENG));

                        // ���� ���� - ������ ǥ��
                        String quantity = childLine.getProperty(SDVPropertyConstant.BL_QUANTITY).split("\\.")[0];
                        equipmentMap.put(SDVPropertyConstant.BL_QUANTITY, quantity);

                        equipmentList.add(equipmentMap);
                    }
                }
            }
        }
        dataMap.put("equipmentList", equipmentList);

        return dataMap;
    }

    /**
     * ��Ÿ ����
     * 
     * @method getETCInfo
     * @date 2014. 2. 5.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getETCInfo(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        // ���� �ý���
        dataMap.put(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_ASSY_SYSTEM));

        // ���� �����(���� owner �̸�)
        String owning_user = operation.getProperty(SDVPropertyConstant.BL_OWNING_USER);
        dataMap.put(SDVPropertyConstant.BL_OWNING_USER, getNameProperty(owning_user));

        // INSTL DWG NO
        dataMap.put(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO, operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO));
        
        /*
         * ������ : 20200110
         * [CF196] �Ӽ� �߰� ��û "m7_P_FMEA_NO", "m7_CP_NO"
         * ������ �Ӽ� ���� �� dataMap �� �Է�
         */
        // �� �Ӽ��� Item �Ӽ��̶� BOMLine �� �������� ������
        
        if( this.processType.startsWith("A")) {
        	TCComponentItem opItem = (TCComponentItem)operation.getItem();
        	String pFmeaNo = "";
        	String cpNo = "";
        	
        	pFmeaNo = opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO) == null ? "" : opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO) ;
        	cpNo = opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_CP_NO) == null ? "" :opItem.getProperty(SDVPropertyConstant.OPERATION_ITEM_CP_NO);
        	
	        	if( pFmeaNo.equals("Y") || cpNo.equals("Y") ) {
	        		String projectCode = "";
					String functionCode = "";
					String shopCode = "";
					InterfaceAIFComponent component = AIFUtility.getCurrentApplication().getTargetComponent();
			        if (component != null && component instanceof TCComponentBOPLine) {
			            TCComponentBOMWindow bomWindow = ((TCComponentBOPLine) component).window();
			            TCComponentBOMLine topBOMLine = bomWindow.getTopBOMLine();
			            // shop Code
			            shopCode = topBOMLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
			            TCComponentItemRevision  mecoRevision = (TCComponentItemRevision)operation.getItemRevision().getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
			            projectCode = mecoRevision.getProperty(SDVPropertyConstant.MECO_REV_PROJECT_CODE);
			            projectCode = reNameProjectCode(projectCode);
			            functionCode =operation.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_FUNCTION_CODE);
					
					  if(((String)pFmeaNo).equals("Y")) {
						  dataMap.put(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO,  "PF" + "-" + projectCode + "-" + shopCode + "-" + "F" + functionCode + "-" + "XX");
					  } else {
						  dataMap.put(SDVPropertyConstant.OPERATION_ITEM_P_MEFA_NO, "");
					  } 
					  
					  if(((String)cpNo).equals("Y")) {
						  dataMap.put(SDVPropertyConstant.OPERATION_ITEM_CP_NO,  "CP" + "-" + projectCode + "-" + shopCode + "-" + "F" + functionCode + "-" + "XX");
					  } else {
						  dataMap.put(SDVPropertyConstant.OPERATION_ITEM_CP_NO, "");
					  }
	        	}
	        	
	        }
        }
        

        return dataMap;
    }

    /**
     * �۾� ����
     * 
     * @method getWorkInfoList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getWorkInfoList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> workInfoList = new ArrayList<HashMap<String, Object>>();

        TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) operation;
        TCComponent root = bvrOperation.getReferenceProperty(SDVPropertyConstant.BL_ACTIVITY_LINES);
        if (root != null) {
            if (root instanceof TCComponentCfgActivityLine) {
                TCComponentMEActivity rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
                if (children != null) {
                    for (TCComponent child : children) {
                        HashMap<String, Object> workInfoMap = new HashMap<String, Object>();

                        workInfoMap.put("workCode", child.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE));
                        workInfoMap.put("workInfo", child.getProperty(SDVPropertyConstant.ACTIVITY_OBJECT_NAME));

                        if (processType.startsWith("A")) {
                            String time_system_frequency = child.getProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);
                            if (!"1".equals(time_system_frequency)) {
                                workInfoMap.put("workCode", child.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE) + "X" + time_system_frequency);
                            }
                        }

                        workInfoList.add(workInfoMap);
                    }
                    dataMap.put("workInfoList", workInfoList);
                }
            }
        }

        return dataMap;
    }

    /**
     * End Item
     * 
     * [SR140828-014][20140827] shcho, BOPLine�� Option ������ �������� �Ӽ��� BL_OCC_MVL_CONDITION ���� BL_VARIANT_CONDITION���� ���� (���� : Copy&Paste�� �Ҵ�� End Item�� ��� BL_OCC_MVL_CONDITION�� ���� ����.)
     * 
     * @method getEndItemList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getEndItemList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> endItemList = new ArrayList<HashMap<String, Object>>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    String type = childLine.getItem().getType();
                    if (SDVTypeConstant.EBOM_VEH_PART.equals(type) || SDVTypeConstant.EBOM_STD_PART.equals(type)) {
                        HashMap<String, Object> endItemMap = new HashMap<String, Object>();

                        // PART NO
                        endItemMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // PART ��
                        endItemMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

                        // ���� - ������ ǥ��
                        String quantity = childLine.getProperty(SDVPropertyConstant.BL_QUANTITY).split("\\.")[0];
                        endItemMap.put(SDVPropertyConstant.BL_QUANTITY, quantity);

                        // End Item �ɼ��� ������ ������ �ɼ� ����
                        String endItem_option = childLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                        if ("".equals(endItem_option)) {
                            String operation_option = operation.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
                            if ("".equals(operation_option)) {
                                endItemMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, "����");
                            } else {
                                endItemMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(operation_option).get("printValues"));
                            }
                        } else {
                            endItemMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(endItem_option).get("printValues"));
                        }

                        endItemList.add(endItemMap);

                        // Wiring Harness�� �ִ� 20���� ���(���輭)
                        // Wiring Harness�� ������� End Item�� �ִ� 20���� ���(2013-12-12)
                        if (endItemList.size() == 20) {
                            break;
                        }
                    }
                }
            }
        }
        dataMap.put("endItemList", endItemList);

        return dataMap;
    }

    /**
     * ������
     * 
     * @method getSubsidiaryList
     * @date 2013. 11. 20.
     * @param
     * @return HashMap<String,Object>
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, Object> getSubsidiaryList(TCComponentBOPLine operation, HashMap<String, Object> dataMap) throws TCException {
        List<HashMap<String, Object>> subsidiaryList = new ArrayList<HashMap<String, Object>>();

        if (operation.getChildrenCount() > 0) {
            AIFComponentContext[] context = operation.getChildren();
            for (int i = 0; i < context.length; i++) {
                if (context[i].getComponent() instanceof TCComponentBOPLine) {
                    TCComponentBOPLine childLine = (TCComponentBOPLine) context[i].getComponent();
                    if (SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equals(childLine.getItem().getType())) {
                        HashMap<String, Object> subsidiaryMap = new HashMap<String, Object>();

                        // ������ NO
                        subsidiaryMap.put(SDVPropertyConstant.BL_ITEM_ID, childLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

                        // ������ ��
                        subsidiaryMap.put(SDVPropertyConstant.BL_OBJECT_NAME, childLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));

                        // ������ ������
                        subsidiaryMap.put(SDVPropertyConstant.SUBSIDIARY_ENG_NAME, childLine.getItemRevision().getProperty(SDVPropertyConstant.SUBSIDIARY_ENG_NAME));

                        // ����
                        subsidiaryMap.put(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT, childLine.getProperty(SDVPropertyConstant.SUBSIDIARY_UNIT_AMOUNT));

                        // �ҿ䷮
                        subsidiaryMap.put(SDVPropertyConstant.SUB_SUBSIDIARY_QTY, childLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY));

                        // ������ �ɼ��� ������ ������ �ɼ� ����
                        String subsidiary_option = childLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
                        if ("".equals(subsidiary_option)) {
                            String operation_option = operation.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION).trim();
                            if ("".equals(operation_option)) {
                                subsidiaryMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, "����");
                            } else {
                                subsidiaryMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(operation_option).get("printValues"));
                            }
                        } else {
                            subsidiaryMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, SDVBOPUtilities.getVariant(subsidiary_option).get("printValues"));
                        }

                        subsidiaryList.add(subsidiaryMap);
                    }
                }
            }
        }
        dataMap.put("subsidiaryList", subsidiaryList);

        return dataMap;
    }

    /**
     * �θ� TCComponentBOPLine return
     * 
     * @method getParentBOPLine
     * @date 2014. 2. 5.
     * @param
     * @return TCComponentBOPLine
     * @exception
     * @throws
     * @see
     */
    private TCComponentBOPLine getParentBOPLine(TCComponentBOPLine bopLine, String itemType) throws TCException {
        TCComponentBOPLine parentBOPLine = null;

        if (bopLine.parent() != null) {
            parentBOPLine = (TCComponentBOPLine) bopLine.parent();
            if (!parentBOPLine.getItem().getType().equals(itemType)) {
                return getParentBOPLine(parentBOPLine, itemType);
            }
        }

        return parentBOPLine;
    }

    /**
     * owning_user ������ �̸��� �������� ex) BOPADM (bopadm) -> BOPADM
     * 
     * @method getNameProperty
     * @date 2013. 11. 18.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getNameProperty(String owning_user) {
        String name = owning_user.split(" ")[0];

        return name;
    }

    private IDataSet convertToDataSet(String dataName, List<HashMap<String, Object>> dataList) {
        IDataSet dataSet = new DataSet();
        IDataMap dataMap = new RawDataMap();
        dataMap.put(dataName, dataList, IData.TABLE_FIELD);
        dataSet.addDataMap(dataName, dataMap);

        return dataSet;
    }
    
	/**
	 * ������ : [CF-196]20200114
	 * ProjectCode Name ���� ���� 
	 * Ex) X100, X150, X151 -> X150
	 * 	   C300, C301 		-> C300  ���� ���� �߰��� ���� �׸� ����
	 * @param projectCode
	 * @return
	 */
	private String reNameProjectCode(String projectCode) {
		String projectCodeRename = "";
		if( projectCode.startsWith("X1")) {
			projectCodeRename = "X150";
		}
		
		if( projectCode.startsWith("C3")) {
			projectCodeRename = "C300";
		}
		
		return projectCodeRename;
	}

}
