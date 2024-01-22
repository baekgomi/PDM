/**
 * 
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import java.util.ArrayList;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * Line ������ ������ �ߺ� �Ҵ�Ǿ� �ִ��� üũ��
 * Class Name : DuplcateOPValidation
 * Class Description :
 * 
 * @date 2013. 12. 12.
 * 
 */
public class DuplcateOPValidation extends OperationValidation<ArrayList<TCComponentBOMLine>, String> {

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.operation.meco.validate.OperationValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {
        ArrayList<String> operationIdList = new ArrayList<String>();
        StringBuilder errorMsgBuilder = new StringBuilder();
        for (TCComponentBOMLine operation : target) {
            String itemId = operation.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String itemName = operation.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
            String errorMsg = ""; // ���� �޼���
            if (operationIdList.contains(itemId)) {
                errorMsg = getMessage(ERROR_TYPE_OP_DUPLICATE_ASSIGNED, itemId, itemName);
                errorMsgBuilder.append(errorMsg);
            } else
                operationIdList.add(itemId);
        }
        
        if (errorMsgBuilder.length() > 0)
            result = errorMsgBuilder.toString();
    }

}
