/**
 * 
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;

/**
 * ���� Activity ����
 * Class Name : ActivitiyValidate
 * Class Description :
 * 
 * @date 2013. 12. 10.
 * 
 */
public class ActivitiyValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.operation.meco.validate.OperationValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        // Validation �׸� ���� �޼��� ����
        StringBuilder resultBuilders = new StringBuilder();

        AIFComponentContext[] activityParentComp = target.getRelated(SDVPropertyConstant.BL_ACTIVITY_LINES);
        TCComponent[] childComps = null;
        if(activityParentComp!=null && activityParentComp.length>0){
        	TCComponentCfgActivityLine rootActLine = (TCComponentCfgActivityLine) activityParentComp[0].getComponent();
        	TCComponentMEActivity rootActivity = (TCComponentMEActivity) rootActLine.getUnderlyingComponent();
        	childComps = ActivityUtils.getSortedActivityChildren(rootActivity);
        }

        for (int i = 0;childComps!=null && i < childComps.length; i++) {
        	
        	TCComponent childComp = childComps[i];
            TCComponentMEActivity childActivity = (TCComponentMEActivity) childComp;
            childActivity.refresh();
            String errorMsg = ""; // ���� �޼���
            String controlPoint = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT);
            String systemCode = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
            String category = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
            double frequency = childActivity.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY);

            /**
             * 1. �۾��ڵ尡 TT�� ��� ������ ��ũ ���� ������ ����
             * 2. TT �̰� KPC �� ��� ���� �Ҵ� ���� üũ
             * 3. �����ð��� ��� ���̵��� 1 �̻��� ��� ����
             * 4. Activity Category(NA) �� ���� �Է� �ȵɶ� üũ��
             */
            
            // (üũ) Activity Category("NA") �̸� ����
            if (category.equals("NA")) {
                errorMsg = getMessage(ERROR_TYPE_ACTIVITY_CATEGOLRY_EMPTY, systemCode);
                resultBuilders.append(errorMsg);
            }

            // (üũ) �����ð� �ϰ�� ���̵� 1�̻��� �� ����
            if (category.equals(registry.getString("Assistance.NAME", "����")) && frequency > 1) {
                errorMsg = getMessage(ERROR_TYPE_ACTIVITY_FREQUENCY, systemCode);
                resultBuilders.append(errorMsg);
            }

            if (!systemCode.startsWith("TT"))
                continue;

            String[] toolUidList = childActivity.getTCProperty(SDVPropertyConstant.ACTIVITY_TOOL_LIST).getStringArrayValue();

            // (üũ) ������ �Ҵ���� ����
            if (!controlPoint.isEmpty() && toolUidList.length == 0) {
                errorMsg = getMessage(ERROR_TYPE_TOOL_NOTASSIGNED, systemCode);
                resultBuilders.append(errorMsg);
            }

            if (toolUidList.length == 0)
                continue;

            for (String toolUid : toolUidList) {
                TCComponent comp = target.getSession().stringToComponent(toolUid);
                if (!(comp instanceof TCComponentMEAppearancePathNode))
                    continue;
                TCComponentMEAppearancePathNode pathNode = (TCComponentMEAppearancePathNode) comp;
                TCComponentBOMLine toolBOMLine = target.window().getBOMLineFromAppearancePathNode(pathNode, target);
                String toolId = toolBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                String torgueType = toolBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE);
                String torqueValue = toolBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE);

                if (torgueType.equals(registry.getString("NotYet.NAME", "����")))
                    continue;
                // (üũ) ��ũ�� �Է� ���� Ȯ��
                if (torgueType.isEmpty() || torqueValue.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_TOOL_TORQUE_EMPTY, systemCode, toolId);
                    resultBuilders.append(errorMsg);
                }
            }
        }

        if (resultBuilders.length() > 0)
            result = resultBuilders.toString();

    }

}
