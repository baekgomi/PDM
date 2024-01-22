/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;

/**
 * [SR150415-005][20150518] shcho, ������ �Ҵ� ���� ���� ��� �߰��� ���� Class �ű� ����
 * (�������� �Ҵ���� �ʾƾ� �� ���� �Ҵ� �Ǿ��� ���, ���� �޽����� ����.)
 * 
 * Class Name : WeldPointAssignValidation2
 * Class Description :
 *
 *
 */
public class WeldPointAssignValidation2 extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.WeldPointAssignValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {
        
        String itemId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        AIFComponentContext[] comps = target.getChildren();
        for (AIFComponentContext comp : comps) {
            TCComponentBOPLine childBOMline = (TCComponentBOPLine) comp.getComponent();
            if (childBOMline.getItem().getType().equals(SDVTypeConstant.BOP_BODY_WELD_POINT_ITEM)) {
                result = getMessage(ERROR_TYPE_WP_ASSIGNED, itemId);
            }
        }
    }

}
