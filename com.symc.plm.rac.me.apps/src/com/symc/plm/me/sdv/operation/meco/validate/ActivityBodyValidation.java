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

/**
 * Class Name : ActivityCommonValidation
 * Class Description :
 *
 * @date 2014. 1. 28.
 *       Activity ���� Validation
 */
public class ActivityBodyValidation extends OperationValidation<TCComponentBOMLine, String> {

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
        TCComponentCfgActivityLine rootActLine = null;
        TCComponentMEActivity rootActivity = null;
        TCComponent[] childComps = null;

        if(activityParentComp.length > 0) {

        	rootActLine =(TCComponentCfgActivityLine) activityParentComp[0].getComponent();

        	if(null != rootActLine){

        		rootActivity = (TCComponentMEActivity) rootActLine.getUnderlyingComponent();

        		if(null != rootActivity) {

        			childComps = ActivityUtils.getSortedActivityChildren(rootActivity);


        			if(null != childComps) {

        		        for (TCComponent childComp : childComps) {
        		            TCComponentMEActivity childActivity = (TCComponentMEActivity) childComp;
        		            childActivity.refresh();
        		            String errorMsg = ""; // ���� �޼���
        		            String systemCode = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
        		            String category = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);
        		            String unitTime = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);
        		            String worker = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_WORKER);

        		            /**
        		             * 1. Activity Category(NA) �� ���� �Է� �ȵɶ� üũ��
        		             * 2. �߰� ���� : Category(�۾��� ����)�� ��쿡 �۾��� �ڵ尡 ���� ���� ��� üũ
        		             * 3. Activity �� ������ �ִµ� �ð�(unitTime <- �÷�)�� ���ų� 0 �ΰ�� üũ
        		             */

        		            // (üũ) Activity Category("NA") �̸� ����
        		            if (category.equals("NA")) {
        		                errorMsg = getMessage(ERROR_TYPE_ACTIVITY_CATEGOLRY_EMPTY, systemCode);
        		                resultBuilders.append(errorMsg);
        		            }else if(category.equals("")){
        		                errorMsg = getMessage(ERROR_TYPE_ACTIVITY_CATEGOLRY_EMPTY, systemCode);
                                resultBuilders.append(errorMsg);
        		            }else if(category.equals("�۾�������") && worker.equals("")){
        		                errorMsg = getMessage(ERROR_TYPE_ACTIVITY_WORKER_EMPTY, systemCode);
                                resultBuilders.append(errorMsg);
        		            }else if(unitTime.equals("") || unitTime.equals("0")){
                                errorMsg = getMessage(ERROR_TYPE_ACTIVITY_UNITTIME_EMPTY, systemCode);
                                resultBuilders.append(errorMsg);
        		            }
        		        }
        			}
        		}
        	}
        }



        if (resultBuilders.length() > 0)
            result = resultBuilders.toString();

    }

}
