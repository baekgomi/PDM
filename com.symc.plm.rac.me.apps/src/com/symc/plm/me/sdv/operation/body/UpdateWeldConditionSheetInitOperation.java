/**
 * 
 */
package com.symc.plm.me.sdv.operation.body;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * Class Name : UpdateWeldConditionSheetInitOperation
 * Class Description :
 * [NON-SR][20160217] taeku.jeong, ��������ǥ�� ������ �ϴ°�� �����̷��� ��ϵ��� �ʴ� ��찡 �־� �̸� �ذ��ϱ����� �߰��� Operation
 * @date 2016. 02. 17.
 * 
 */
public class UpdateWeldConditionSheetInitOperation extends AbstractSDVInitOperation {

    /**
     * 
     */
    public UpdateWeldConditionSheetInitOperation() {
        super();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        try {
            if (!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
                // MPPApplication Check
                throw new Exception("MPP Application���� �۾��ؾ� �մϴ�.");
            }
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            if (selectedTargets.length > 1)
                throw new Exception("��� SHOP�� �ϳ��� ������ �ּ���.");

            if ((!(selectedTargets[0] instanceof TCComponentBOMLine)) || (!((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))) {
                throw new Exception("��� SHOP�� ������ �ּ���.");
            }

            RawDataMap targetDataMap = new RawDataMap();
            targetDataMap.put(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, selectedTargets[0], IData.OBJECT_FIELD);
            
            DataSet targetDataset = new DataSet();
            targetDataset.addDataMap(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, targetDataMap);

            setData(targetDataset);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
