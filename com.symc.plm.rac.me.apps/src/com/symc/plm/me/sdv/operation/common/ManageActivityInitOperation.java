/**
 * 
 */
package com.symc.plm.me.sdv.operation.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrOperation;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.util.Registry;

/**
 *[SR141219-020][20150108] shcho, Open with Time â������ Activity �۾����� ����ġ �� ���� ���� �Ұ� ���� �ű� ȭ�� �߰�
 */
public class ManageActivityInitOperation extends AbstractSDVInitOperation {
    private Registry registry = Registry.getRegistry(ManageActivityInitOperation.class);

    /**
	 * 
	 */
    public ManageActivityInitOperation() {
        super();
    }

    @Override
    public void executeOperation() throws Exception {
        try {

            /** Validation (�Ѱ��� ������ ���õǾ� �ִ��� üũ�Ѵ�) **/
            if (!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
                // MPPApplication Check
                throw new Exception(registry.getString("WorkInMPPApplication.MESSAGE", "MPP Application���� �۾��ؾ� �մϴ�."));
            }
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            if (selectedTargets.length != 1)
                throw new Exception(registry.getString("SelectOneTargetOperation.MESSAGE", "��� ������ �ϳ��� ������ �ּ���."));

            if ((!(selectedTargets[0] instanceof TCComponentBOMLine))) {
                throw new Exception(registry.getString("SelectTargetOperation.MESSAGE", "������ ������ �ּ���."));
            } else {
                String type = ((TCComponentBOMLine) selectedTargets[0]).getItem().getType();
                if (!(type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) || type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) || type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM))) {
                    throw new Exception(registry.getString("SelectTargetOperation.MESSAGE", "������ ������ �ּ���."));
                }
            }

            /** Activity List ��ȸ **/
            TCComponentMfgBvrOperation bvrOperation = (TCComponentMfgBvrOperation) selectedTargets[0];
            TCComponent root = bvrOperation.getReferenceProperty("bl_me_activity_lines");
            // [SR190104-050]���� MECO ID ��ȸ 
            TCComponent meco = bvrOperation.getReferenceProperty("m7_MECO_NO");
            String mecoNo = meco.getStringProperty("item_id");
            List<HashMap<String, Object>> activityList = new ArrayList<HashMap<String, Object>>();
            TCComponentMEActivity rootActivity = null;
            boolean releaseFlag = false;

            if (root != null) {
                if (root instanceof TCComponentCfgActivityLine) {
                    rootActivity = (TCComponentMEActivity) root.getUnderlyingComponent();
                    Date releasedDate = rootActivity.getDateProperty(SDVPropertyConstant.ACTIVITY_DATE_RELEASED);
                    if (releasedDate != null) {
                        releaseFlag = true;
                    }
                
                    TCComponent[] children = ActivityUtils.getSortedActivityChildren(rootActivity);
//                    String[] propertyNames = registry.getStringArray("table.column.search.id.body");
                    String propertyName = "seq,object_name,time_system_category,m7_WORK_OVERLAP_TYPE,time_system_unit_time,m7_CONTROL_POINT,m7_CONTROL_BASIS,m7_WORKERS,m7_MECO_NO,m7_ENG_NAME";
                    String[] propertyNames = propertyName.split(",");
//                    String[] values = child.getProperties(propertyNames);
                    HashMap<String, Object> activityMap = null;
                    if (children != null) {
                        for (int i = 0; i < children.length; i++) {
                            if (children[i] instanceof TCComponentMEActivity) {
                                TCComponentMEActivity child = (TCComponentMEActivity) children[i];
                                child.refresh();
                                // String[] propertyNames = new String[] {
                                // SDVPropertyConstant.ITEM_OBJECT_NAME,
                                // SDVPropertyConstant.ACTIVITY_ENG_NAME,
                                // SDVPropertyConstant.ACTIVITY_SYSTEM_CODE,
                                // SDVPropertyConstant.ACTIVITY_CONTROL_POINT,
                                // SDVPropertyConstant.ACTIVITY_CONTROL_BASIS,
                                // SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE
                                // };
//                                String[] propertyNames = registry.getStringArray("table.column.search.id.body");
                                String[] values = child.getProperties(propertyNames);

                                activityMap = new HashMap<String, Object>();
                                activityMap.put(propertyNames[0], String.valueOf((i + 1) * 10));
                                for (int j = 1; j < propertyNames.length; j++) {
                                    activityMap.put(propertyNames[j], values[j]);
                                }
                                

                                // activityMap.put("SEQ", (i + 1) * 10);
                                // String activityName = values[0];
                                // activityMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, activityName);
                                // activityMap.put(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE, values[2]);
                                // activityMap.put(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY, child.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).getStringValue());
                                // activityMap.put(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY, child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY));
                                // activityMap.put(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME, child.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME));
                                // activityMap.put(SDVPropertyConstant.ACTIVITY_CONTROL_POINT, values[3]);
                                // activityMap.put(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS, values[4]);
                                // activityMap.put(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE, values[5]);

                                // if(configId == 1) {
                                // String engActivityName = values[1];
                                // if(engActivityName != null && engActivityName.length() > 0) {
                                // activityName = engActivityName;
                                // }
                                // }

                                // TCComponentBOMLine[] tools = child.getReferenceTools(bopLine);
                                // if(tools != null && tools.length > 0) {
                                // activityMap.put(SDVPropertyConstant.ACTIVITY_TOOL_LIST, tools);
                                // }

                                // MECONO setting
                                // String activityMecoId = getActivityMECOId(bopLine.getItemRevision(), child, i);
                                // activityMap.put("SYMBOL", getMecoSymbol(activityMecoId));
                                // if(!releaseFlag) {
                                // child.setProperty(SDVPropertyConstant.ACTIVITY_MECO_NO, activityMecoId);
                                // }

                                activityList.add(activityMap);
                            }
                        }
                    }
                }
            }
            
            
            RawDataMap targetDataMap = new RawDataMap();
            targetDataMap.put("ReleaseFlag", releaseFlag, IData.BOOLEAN_FIELD);
            targetDataMap.put("OperationLine", bvrOperation, IData.OBJECT_FIELD);
            targetDataMap.put("ActivityList", activityList, IData.TABLE_FIELD);
            targetDataMap.put("Activity Category", SDVLOVUtils.getLOVValues("Activity Category"), IData.LIST_FIELD);
            targetDataMap.put("M7_WORK_OVERLAP_TYPE", SDVLOVUtils.getLOVValues("M7_WORK_OVERLAP_TYPE"), IData.LIST_FIELD);
            /*
             * Lov����Ʈ �߰� 
             * Control_Point �߰� 
             */
            targetDataMap.put("M7_MANAGEMENT_POINT", SDVLOVUtils.getLOVValues("M7_MANAGEMENT_POINT"), IData.LIST_FIELD);
            // [SR190104-050] ������ ���� ��û 
            // Activity ���� �� ��û ����
            // Add ��ư Ŭ���� Row �߰� �� MECO_NO ���� ǥ��
            // Copy �� Paste �ҽ� ���� �Ӽ����� �� ���� 
            // Activity ������ �ӵ� ����
            targetDataMap.put("MECO_NO", mecoNo, IData.STRING_FIELD);

            DataSet targetDataset = new DataSet();
            targetDataset.addDataMap("ActivityList", targetDataMap);

            setData(targetDataset);
        } catch (Exception ex) {
            throw ex;
        }

    }

  
}
