/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import java.util.Date;
import java.util.Vector;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.TcDefinition;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * ���� ����ǥ ���� ���� üũ / ��������ǥ ���� �ʿ� ���� üũ
 * Class Name : WeldConditionSheetValidation
 * Class Description :
 *
 * @date 2013. 12. 18.
 * @author jwlee
 *
 */
public class WeldConditionSheetValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.WeldConditionSheetValidation#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {

        TCComponent[] comps = target.getItemRevision().getRelatedComponents(SDVTypeConstant.WELD_CONDITION_SHEET_RELATION);
        String itemId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);
        if (comps.length == 0){
            result = getMessage(ERROR_TYPE_WP_NOT_EXIST, itemId);
            return;
        }

        if (checkWeldSheetData(target.getItemRevision()))
            result = getMessage(ERROR_TYPE_WP_CONDITION_SHEET_UPDATE, itemId);

    }

    /**
     * BOMView �� ��������ǥ �����ͼ¿� ���� �������� ���Ͽ� BOMView �����ϰ� ���ų� ��ũ��
     * true �� ��ȯ �Ѵ�
     * 
     * [SR140704-002][20140703] shcho, ��������ǥ Update ���� üũ�� �ð����� ���� (BOMView�������� 5���̳� ������ŭ �ʰ� ����Ǵ°� ���)
     *
     * @method checkWeldSheetData
     * @date 2013. 12. 19.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unused")
    private boolean checkWeldSheetData(TCComponentItemRevision selectedTarget) throws Exception
    {
        // ������ WeldOP �� BOMView Revision Ÿ���� �����´�
        TCComponent[] bomViewTypes = selectedTarget.getReferenceListProperty(SDVTypeConstant.BOMLINE_RELATION);
        // ������ WeldOP �� �������� Dataset �� �����´�
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        datasets = CustomUtil.getDatasets(selectedTarget, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);

        for (TCComponent bomViewType : bomViewTypes)
        {
            if (bomViewType.getType().equals(SDVTypeConstant.BOMLINE_ITEM_REVISION))
            {
                Date bomViewLastDate = bomViewType.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
                Date dataSetLastDate = datasets.get(0).getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE);
                Long compare = (long) bomViewLastDate.compareTo(dataSetLastDate);

                String bomViewStringDate = SDVStringUtiles.dateToString(bomViewLastDate, "yyyyMMddHHmmss");
                String dataSetStringDate = SDVStringUtiles.dateToString(dataSetLastDate, "yyyyMMddHHmmss");
                Long bomViewLongDate = Long.parseLong(bomViewStringDate);
                Long dataSetLongDate = Long.parseLong(dataSetStringDate);
                Long compareLongResult = dataSetLongDate - bomViewLongDate;
                if (compareLongResult < Long.parseLong("-5")){
                    return true;
                }
            }
        }
        return false;
    }

}
