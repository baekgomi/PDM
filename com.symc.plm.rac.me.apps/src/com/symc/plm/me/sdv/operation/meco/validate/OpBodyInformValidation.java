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
 * [SR140924-021][20141006] shcho, ��ü ������ �ҿ䷮ üũ ��� �߰�(������ �����ϰ�)
 * [SR150107-038][20140403] shcho, Reference E/Item Find No. �Ӽ� �� Sync ��� �߰�
 * [NON-SR][20150605] shcho, Reference E/Item Find No. �Ӽ� �� Sync ��� ����� MECO ��� Validate ������ �ϰ� �Ǹ� target�� Parent�� �������� ���� null ���� ��. 
 *                              �� ������ �ذ� �� ������ �ӽ÷� Sync��� ����. (�ذ� ����� ����ó�� ��ü�� ��� �������� m7_PRODUCT_CODE �Ӽ����� �ִ� migration�۾� �ʿ�) 
 *
 * @date 2013. 12. 10.
 *
 */
@SuppressWarnings("unused")
public class OpBodyInformValidation extends OperationValidation<TCComponentBOMLine, String> {

    /*
     * (non-Javadoc)
     *
     * @see com.symc.plm.me.sdv.operation.meco.validate.Validator#executeValidation()
     */
    @Override
    protected void executeValidation() throws Exception {
        result = null;
        // Validation �׸� ���� �޼��� ����
        HashMap<Integer, StringBuilder> resultsByItem = new HashMap<Integer, StringBuilder>();
        
        /**
         * MProductBOMWindow ���� (�Ҵ�� End Item�� Source BOMWindow)
         */
//        TCComponentBOMWindow mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(target.parent().parent().parent().getItemRevision());
        
        /**
         * Line Code ��ġ üũ
         */
        //validateLineCode(resultsByItem);

        TCComponentBOMLine[] childBOMLineList = SDVBOPUtilities.getUnpackChildrenBOMLine(target);
        for (TCComponentBOMLine childBOMLine : childBOMLineList) {
            boolean isExistError = false;
            String errorMsg = ""; // ���� �޼���
            childBOMLine.refresh();
            
            String itemId = childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String itemName = childBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
            String itemType = childBOMLine.getItem().getType();
            String quantity = childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY);
            String findNo = childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
            ArrayList<String> dupCheckList = new ArrayList<String>();
            int sequence = 0;

            try {
                sequence = Integer.parseInt(findNo);
            } catch (NumberFormatException ex) {
            }

            /**
             * End Item üũ
             * 1. End Item Seq No. ���� üũ <- ������ üũ
             * 2. [SR150107-038][20140403] shcho, Reference E/Item Find No. �Ӽ� �� Sync ��� �߰�
             */
            if (itemType.equals(SDVTypeConstant.EBOM_STD_PART) || itemType.equals(SDVTypeConstant.EBOM_VEH_PART)) {
                // Reference E/Item Find No. �Ӽ� �� Sync
                /*TCComponentBOMLine assignSrcBOMLine = SDVBOPUtilities.getAssignSrcBomLine(mProductBOMWindow, childBOMLine);
                if (assignSrcBOMLine != null) {
                    String seqNo = assignSrcBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
                    childBOMLine.setProperty(SDVPropertyConstant.BL_NOTE_ENDITEM_SEQ_NO, seqNo);
                }*/

                if (dupCheckList.contains(itemId))
                    continue;
                // (üũ) Sequence �Է� ���� üũ
                if (sequence >= 1)
                    continue;

                errorMsg = getMessage(ERROR_TYPE_ENDITEM_SEQ_EMPTY, itemId, itemName);
                addErrorMsg(resultsByItem, MSG_TYPE_ENDITEM, errorMsg);
                dupCheckList.add(itemId);
            
                /**
                 * ������ Check
                 * 1. �ҿ䷮(����) �Է� ���� üũ �߰� [SR140924-021][20141006] shcho, ��ü ������ �ҿ䷮ üũ��� �߰�(������ �����ϰ�)
                 * 2. Paint Marker �� ���, �ְ� ��/�߰� �� �Է� ���� üũ  <- ����
                 * 3. ������ Option�� ���� ��� ������ Option�� �ִ��� üũ  <- ����
                 * 4. Sequence (510 ~ 800) <- ����
                 */
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {
//
                if (dupCheckList.contains(itemId))
                    continue;
//
//                String opOption = target.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
//                String subPartOption = childBOMLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                String subQuantity = childBOMLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
                // (üũ)�ҿ䷮ �Է� ����
                if (subQuantity.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_QTY_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

//                // (üũ) Paint Marker �� ���, �ְ�/�߰� �� �Է� ���� FIXME: Paint Marker ������ �ް� ������ <- ����
////                if (itemName.trim().toUpperCase().endsWith("PAINT MARKER") || itemName.trim().indexOf("��ī") > 0) {
////                    String dayOrNight = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
////                    if (dayOrNight.isEmpty()) {
////                        errorMsg = getMessage(ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY, itemId, itemName);
////                        addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
////                        isExistError = true;
////                    }
////
////                }
//
//                // (üũ) Option üũ <- ����
////                if (subPartOption.isEmpty() && !opOption.isEmpty()) {
////                    errorMsg = getMessage(ERROR_TYPE_SUBPART_OPTION_EMPTY, itemId, itemName);
////                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
////                    isExistError = true;
////                }
//
//                // (üũ) Sequence ���� üũ  <- ����
////                if (sequence < 510 || sequence > 800) {
////                    errorMsg = getMessage(ERROR_TYPE_SUBPART_SEQ_INVALID, itemId, itemName);
////                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
////                    isExistError = true;
////                }
//
                if (isExistError)
                    dupCheckList.add(itemId);
            }
//
//                /**
//                 * ���� Check
//                 * 1. ���� �Է� ����   <- ����
//                 * 2. ���� �Ҽ��� ����   <- ����
//                 * 3. Sequence (10 ~ 200)   <- ����
//                 */
//            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
//                if (dupCheckList.contains(itemId))
//                    continue;
//
//                // (üũ)���� �Է� ����
//                if (quantity.isEmpty()) {
//                    errorMsg = getMessage(ERROR_TYPE_TOOL_QTY_EMPTY, itemId, itemName);
//                    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
//                    isExistError = true;
//                } else {
//                    // (üũ)������ �Ҽ��� �Է� ����
//                    String regEx = "[0-9]+|[0-9]+\\.[0]+";
//                    boolean isMatch = quantity.matches(regEx);
//                    if (!isMatch) {
//                        errorMsg = getMessage(ERROR_TYPE_TOOL_QTY_INVALID, itemId, itemName);
//                        addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
//                        isExistError = true;
//                    }
//                }
//
//                // (üũ) Sequence ���� üũ
//                if (sequence < 10 || sequence > 200) {
//                    errorMsg = getMessage(ERROR_TYPE_TOOL_SEQ_INVALID, itemId, itemName);
//                    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
//                    isExistError = true;
//                }
//
//                if (isExistError)
//                    dupCheckList.add(itemId);
//                /**
//                 * ���� Find No(210 ~ 500)   <- ����
//                 */
//            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
//                if (dupCheckList.contains(itemId))
//                    continue;
//                // (üũ) Sequence ���� üũ
//                if (sequence >= 210 && sequence <= 500)
//                    continue;
//
//                errorMsg = getMessage(ERROR_TYPE_EQUIPMENT_SEQ_INVALID, itemId, itemName);
//                addErrorMsg(resultsByItem, MSG_TYPE_EQUIPMENT, errorMsg);
//
//                dupCheckList.add(itemId);
//            }
        }

        /**
         * ���� �޼����� ����
         */
        makeErrorMsg(resultsByItem);
      
        
        /**
         *  MProductBOMWindow close.
         */
//        if (mProductBOMWindow != null) {
//            mProductBOMWindow.clearCache();
//        }
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

    /**
     * Line �ڵ尡 �����ڵ�, �۾��� �ڵ��� ���ڸ��� ��ġ�ϴ��� üũ
     *
     * @method validLineOperation
     * @date 2013. 12. 12.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void validateLineCode(HashMap<Integer, StringBuilder> resultsByItem) throws Exception {

        TCComponentBOMLine lineBOMLine = target.parent();

        String opItemId = target.getProperty(SDVPropertyConstant.BL_ITEM_ID);

        // ���� BOM�� ���� ��쿡�� üũ��
        if (lineBOMLine == null)
            return;
        String lineCode = lineBOMLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
        // ���� �ڵ�   <- ����
        String stationNo = target.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
        // �۾��� �ڵ�   <- ����
        String workerCode = target.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE);

        if (!stationNo.startsWith(lineCode)) {
            addErrorMsg(resultsByItem, MSG_TYPE_OP_LINE_MATCH, getMessage(ERROR_TYPE_OP_STATION_NOT_INVALID, opItemId));
        }
        if (!workerCode.startsWith(lineCode)) {
            addErrorMsg(resultsByItem, MSG_TYPE_OP_LINE_MATCH, getMessage(ERROR_TYPE_OP_WORKERCODE_NOT_INVALID, opItemId));
        }
    }
}
