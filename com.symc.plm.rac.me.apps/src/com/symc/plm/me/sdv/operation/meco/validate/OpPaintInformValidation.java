/**
 * 
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;

/**
 * Class Name : OperationValidator
 * Class Description :
 * 
 * [SR140924-021][20141006] shcho, ���� ������ �ҿ䷮ üũ ��� �߰�(������ �����ϰ�)
 * [SR150107-038][20140403] shcho, Reference E/Item Find No. �Ӽ� �� Sync ��� �߰�
 * 
 * @date 2014. 10. 06.
 * 
 */
public class OpPaintInformValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     * 
     * @see com.symc.plm.me.sdv.operation.meco.validate.Validator#executeValidation()
     */
    @SuppressWarnings("unused")
    @Override
    protected void executeValidation() throws Exception {
        result = null;
        // Validation �׸� ���� �޼��� ����
        HashMap<Integer, StringBuilder> resultsByItem = new HashMap<Integer, StringBuilder>();
        
        /**
         * MProductBOMWindow ���� (�Ҵ�� End Item�� Source BOMWindow)
         */
        TCComponentBOMWindow mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(target.getItemRevision());
        
        TCComponentBOMLine[] childBOMLineList = SDVBOPUtilities.getUnpackChildrenBOMLine(target);
        ArrayList<String> dupCheckList = new ArrayList<String>(); // �ߺ� üũ List
        for (TCComponentBOMLine childBOMLine : childBOMLineList) {
            boolean isExistError = false;
            String errorMsg = ""; // ���� �޼���
            childBOMLine.refresh();
            
            String itemId = childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String itemName = childBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
            String itemType = childBOMLine.getItem().getType();
            String quantity = childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY);
            String findNo = childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
            // int sequence = 0;
            //
            // try {
            // sequence = Integer.parseInt(findNo);
            //
            // } catch (NumberFormatException ex) {
            // }
            //
            // sequence = findNo.startsWith("0") ? 0 : sequence;

            /**
             * ������ Check
             * 1. �ҿ䷮(����) �Է� ����
             */
            if (itemType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {

                if (dupCheckList.contains(itemId))
                    continue;

                // String opOption = target.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                // String subPartOption = childBOMLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                String subQuantity = childBOMLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
                // (üũ)�ҿ䷮ �Է� ����
                if (subQuantity.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_QTY_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

                // // (üũ) Paint Marker �� ���, �ְ�/�߰� �� �Է� ���� FIXME: Paint Marker ������ �ް� ������
                // if (itemName.trim().toUpperCase().endsWith("PAINT MARKER") || itemName.trim().indexOf("��ī") > 0) {
                // String dayOrNight = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
                // if (dayOrNight.isEmpty()) {
                // errorMsg = getMessage(ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY, itemId, itemName);
                // addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                // isExistError = true;
                // }
                // }
                //
                // // (üũ) Option üũ
                // if (subPartOption.isEmpty() && !opOption.isEmpty()) {
                // errorMsg = getMessage(ERROR_TYPE_SUBPART_OPTION_EMPTY, itemId, itemName);
                // addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                // isExistError = true;
                // }
                //
                // // (üũ) Sequence ���� üũ
                // if (sequence < 510 || sequence > 700) {
                // errorMsg = getMessage(ERROR_TYPE_SUBPART_SEQ_INVALID, itemId, itemName);
                // addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                // isExistError = true;
                // }

                if (isExistError)
                    dupCheckList.add(itemId);
                
                /**
                 * End Item üũ
                 * 1. [SR150107-038][20140403] shcho, Reference E/Item Find No. �Ӽ� �� Sync ��� �߰�
                 */
            } else if (itemType.equals(SDVTypeConstant.EBOM_STD_PART) || itemType.equals(SDVTypeConstant.EBOM_VEH_PART)) {
                // Reference E/Item Find No. �Ӽ� �� Sync
                TCComponentBOMLine assignSrcBOMLine = SDVBOPUtilities.getAssignSrcBomLine(mProductBOMWindow, childBOMLine);
                if (assignSrcBOMLine != null) {
                    String seqNo = assignSrcBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
                    childBOMLine.setProperty(SDVPropertyConstant.BL_NOTE_ENDITEM_SEQ_NO, seqNo);
                }
            }
        }

        /**
         * ���� �޼����� ����
         */
        makeErrorMsg(resultsByItem);
      
        
        /**
         *  MProductBOMWindow close.
         */
        if (mProductBOMWindow != null) {
            mProductBOMWindow.clearCache();
        }
    }

    /**
     * ���� �׸񺰷� Error ����Ʈ�� �߰��Ѵ�.
     * 
     * @method addErroMsg
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addErrorMsg(HashMap<Integer, StringBuilder> allMap, int key, String value) {
        StringBuilder sb = new StringBuilder();
        if (!allMap.containsKey(key)) {
            sb.append(value);
            allMap.put(key, sb);
        } else {
            sb = allMap.get(key);
            sb.append(value);
        }
    }

    /**
     * �׸� Error ����Ʈ�� �ϳ��� ���� �޼����� �����
     * 
     * @method makeErrorMsg
     * @date 2013. 12. 11.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void makeErrorMsg(HashMap<Integer, StringBuilder> resultsByItem) throws Exception {
        SortedSet<Integer> keys = new TreeSet<Integer>(resultsByItem.keySet());
        StringBuilder allMsg = new StringBuilder();

        for (int key : keys) {
            StringBuilder sb = resultsByItem.get(key);
            allMsg.append(sb.toString());
        }
        if (allMsg.length() > 0)
            result = allMsg.toString();
    }


}
