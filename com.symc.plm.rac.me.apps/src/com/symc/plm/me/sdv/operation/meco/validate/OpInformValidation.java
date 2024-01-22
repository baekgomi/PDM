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
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Class Name : OperationValidator
 * Class Description :
 * [SR141008-012,SR141008-025][20141013] shcho, Activity �۾��ڵ� �� ������ �Ҵ�� ���� ������, ����� �Ǵ� ���� �� ���� ���� �߰�
 *  1) TT1, TT2, PT3 �� ��쿡 üũ �� ������� �߰� �� ���� üũ ���� �߰�
 *  2) TT1, TT2, PT3, CP1 �� �ƴ� ��쿡 üũ �� ������� �߰� �� ���� üũ ���� �߰�
 *  3) TC�� ��쿡 üũ �� ���� ��� �߰�
 *  4) �񱳴�� �۾��ڵ��� ACTIVITY_CODE_TYPE3�� PT1,PT2,CP1 �߰�
 * 
 * [SR141014-037][20141014] shcho,  Activity �۾��ڵ� �� ������ �Ҵ�� ���� ���� ��� ����
 *  1) �۾��ڵ尡 TC�� �ƴ� ����� ó�� ������ Validation���� ���� ó��
 * 
 * [SR141016-010][20141016] shcho, Activity �۾��ڵ� �� ������ �Ҵ�� ���� ���� ��� ����
 *  1) �Ҵ�� ������ ���� ��쿡 üũ�ϴ� ���� ����
 *  2)  TT1, TT2, PT3�� ��� üũ ���� ������ �׸� �߰�
 *  3) �񱳴�� �۾��ڵ��� ACTIVITY_CODE_TYPE3���� TT4����
 * 
 * [SR141022-012][20141022] shcho, Activity �۾��ڵ� �� ������ �Ҵ�� ���� ���� ��� ����
 *  1) ACTIVITY_CODE_TYPE1���� DA1,RV1,RV2 �߰�
 *  2) ACTIVITY_CODE_TYPE3���� DA1,RV1,RV2,AJ1,AJ2 �߰�
 *  3) ACTIVITY_CODE_TYPE4���� DA1,RV1,RV2,AJ1,AJ2 �߰�
 *  4) ACTIVITY_CODE_TYPE1�� ��� '�Ҵ�Ǵ� ���� A-C-CH or A-E-21�� ���۵Ǿ�� �Ѵ�' �� ����
 * 
 * 
 * [SR150107-038][20140403] shcho, Reference E/Item Find No. �Ӽ� �� Sync ��� �߰�
 * 
 * 
 * @date 2013. 12. 10.
 * 
 */
public class OpInformValidation extends OperationValidation<TCComponentBOMLine, String> {

    public static String ACTIVITY_CODE_TYPE1 = "TT1,TT2,PT3,DA1,RV1,RV2";
    public static String ACTIVITY_CODE_TYPE2 = "TC";
    public static String ACTIVITY_CODE_TYPE3 = "TT1,TT2,PT1,PT2,PT3,TC,CP1,DA1,RV1,RV2,AJ1,AJ2";
    public static String ACTIVITY_CODE_TYPE4 = "TT1,TT2,PT3,CP1,DA1,RV1,RV2,AJ1,AJ2";

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
        TCComponentBOMWindow mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(target.getItemRevision());
        
        /**
         * Line Code ��ġ üũ
         */
        validateLineCode(resultsByItem);

        TCComponentBOMLine[] childBOMLineList = SDVBOPUtilities.getUnpackChildrenBOMLine(target);
        ArrayList<String> dupCheckList = new ArrayList<String>(); // �ߺ� üũ List
        ArrayList<TCComponentBOMLine> toolCheckList = new ArrayList<TCComponentBOMLine>(); // �Ҵ�� ���� üũ List
        ArrayList<TCComponentBOMLine> equipCheckList = new ArrayList<TCComponentBOMLine>(); // �Ҵ�� ���� üũ List
        for (TCComponentBOMLine childBOMLine : childBOMLineList) {
            boolean isExistError = false;
            String errorMsg = ""; // ���� �޼���
            childBOMLine.refresh();
            
            String itemId = childBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String itemName = childBOMLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME);
            String itemType = childBOMLine.getItem().getType();
            String quantity = childBOMLine.getProperty(SDVPropertyConstant.BL_QUANTITY);
            String findNo = childBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
            int sequence = 0;

            try {
                sequence = Integer.parseInt(findNo);

            } catch (NumberFormatException ex) {
            }

            sequence = findNo.startsWith("0") ? 0 : sequence;

            /**
             * End Item Seq No. ���� üũ( 1 ~ 9 )
             */
            if (itemType.equals(SDVTypeConstant.EBOM_STD_PART) || itemType.equals(SDVTypeConstant.EBOM_VEH_PART)) {
                // Reference E/Item Find No. �Ӽ� �� Sync
                TCComponentBOMLine assignSrcBOMLine = SDVBOPUtilities.getAssignSrcBomLine(mProductBOMWindow, childBOMLine);
                if (assignSrcBOMLine != null) {
                    String seqNo = assignSrcBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO);
                    childBOMLine.setProperty(SDVPropertyConstant.BL_NOTE_ENDITEM_SEQ_NO, seqNo);
                }
                
                if (dupCheckList.contains(itemId))
                    continue;
                // (üũ) Sequence ���� üũ
                if (sequence >= 1 && sequence <= 9)
                    continue;

                errorMsg = getMessage(ERROR_TYPE_ENDITEM_SEQ_INVALID, itemId, itemName);
                addErrorMsg(resultsByItem, MSG_TYPE_ENDITEM, errorMsg);
                dupCheckList.add(itemId);
                /**
                 * ������ Check
                 * 1. �ҿ䷮(����) �Է� ����
                 * 2. Paint Marker �� ���, �ְ� ��/�߰� �� �Է� ���� üũ
                 * 3. ������ Option�� ���� ��� ������ Option�� �ִ��� üũ
                 * 4. Sequence (510 ~ 700)
                 */
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {

                if (dupCheckList.contains(itemId))
                    continue;

                String opOption = target.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                String subPartOption = childBOMLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                String subQuantity = childBOMLine.getProperty(SDVPropertyConstant.SUB_SUBSIDIARY_QTY);
                // (üũ)�ҿ䷮ �Է� ����
                if (subQuantity.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_QTY_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

                // (üũ) Paint Marker �� ���, �ְ�/�߰� �� �Է� ���� FIXME: Paint Marker ������ �ް� ������
                if (itemName.trim().toUpperCase().endsWith("PAINT MARKER") || itemName.trim().indexOf("��ī") > 0) {
                    String dayOrNight = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_DAYORNIGHT);
                    if (dayOrNight.isEmpty()) {
                        errorMsg = getMessage(ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY, itemId, itemName);
                        addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                        isExistError = true;
                    }

                }

                // (üũ) Option üũ
                if (subPartOption.isEmpty() && !opOption.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_OPTION_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

                // (üũ) Sequence ���� üũ
                if (sequence < 510 || sequence > 700) {
                    errorMsg = getMessage(ERROR_TYPE_SUBPART_SEQ_INVALID, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_SUBPART, errorMsg);
                    isExistError = true;
                }

                if (isExistError)
                    dupCheckList.add(itemId);

                /**
                 * ���� Check
                 * 1. ���� �Է� ����
                 * 2. ���� �Ҽ��� ����
                 * 3. Sequence (10 ~ 200)
                 * 4. ��ũ���� �߸� �Էµ�
                 * 5. Activity �۾��ڵ� �� ������ �Ҵ�� ���� ����
                 */
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
                if (dupCheckList.contains(itemId))
                    continue;

                String torgueType = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE);
                String torqueValue = childBOMLine.getProperty(SDVPropertyConstant.BL_NOTE_TORQUE_VALUE);

                // (üũ1)���� ���� �Է� ����
                if (quantity.isEmpty()) {
                    errorMsg = getMessage(ERROR_TYPE_TOOL_QTY_EMPTY, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
                    isExistError = true;
                } else {
                    // (üũ2)������ �Ҽ��� �Է� ����
                    String regEx = "[0-9]+|[0-9]+\\.[0]+";
                    boolean isMatch = quantity.matches(regEx);
                    if (!isMatch) {
                        errorMsg = getMessage(ERROR_TYPE_TOOL_QTY_INVALID, itemId, itemName);
                        addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
                        isExistError = true;
                    }
                }

                // (üũ3) Sequence ����(10 ~ 200) üũ
                if (sequence < 10 || sequence > 200) {
                    errorMsg = getMessage(ERROR_TYPE_TOOL_SEQ_INVALID, itemId, itemName);
                    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
                    isExistError = true;
                }

                // (üũ4) ��ũ Ÿ���� "����" �� �ƴϰ�, ���� ���� ���
                if (!torgueType.equals("") && !torgueType.equals(registry.getString("NotYetValue.NAME", "����"))) {
                    // ��ũ ���� "~" �Ǵ� "MAX" ���� �ִ� �� üũ
                    if (torqueValue.indexOf("~") < 0 && torqueValue.toUpperCase().indexOf("MAX") < 0 && torqueValue.toUpperCase().indexOf("MIN") < 0) {
                        errorMsg = getMessage(ERROR_TYPE_TOOL_TORGUE_VALUE_INVALID, itemId, itemName);
                        addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
                        isExistError = true;
                    }
                }

                // (üũ5) Activity �۾��ڵ� �� ������ �Ҵ�� ���� ���� (toolCheckList�� Tool�� ��Ƽ� �ϴܿ��� ���� ó��)
                toolCheckList.add(childBOMLine);

                if (isExistError)
                    dupCheckList.add(itemId);

                /**
                 * ���� Find No(210 ~ 500)
                 */
            } else if (itemType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
                if (dupCheckList.contains(itemId))
                    continue;

                // 'Activity �۾��ڵ� �� ������ �Ҵ�� ���� ����' �� ����� ���� ����Ʈ
                equipCheckList.add(childBOMLine);
                
                // (üũ) Sequence ���� üũ
                if (sequence >= 210 && sequence <= 500)
                    continue;

                errorMsg = getMessage(ERROR_TYPE_EQUIPMENT_SEQ_INVALID, itemId, itemName);
                addErrorMsg(resultsByItem, MSG_TYPE_EQUIPMENT, errorMsg);

                dupCheckList.add(itemId);
            }
        }

        /**
         * ���� Check(üũ5) Activity �۾��ڵ� �� ������ �Ҵ�� ���� ����
         */
         toolCheckWithActivityCode(resultsByItem, toolCheckList, equipCheckList);

        
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
     * [SR141008-012,SR141008-025][20141013] shcho, Activity �۾��ڵ� �� ������ �Ҵ�� ���� ������, ����� �Ǵ� ���� �� ���� ���� �߰�
     * [SR141014-037][20141014] shcho,  Activity �۾��ڵ� �� ������ �Ҵ�� ���� ���� ��� ����
     * [SR141016-010][20141016] shcho, Activity �۾��ڵ� �� ������ �Ҵ�� ���� ���� ��� ����
     * [����][20141017] shcho, Activity �۾��ڵ尡 TC�� ��� "���� A-E-21-010, A-E-21-030, A-E-21-040, A-E-21-051 �̾�� �Ѵ�." ��� �߰� 
     * [SR141022-012][20141022] shcho, Activity �۾��ڵ� �� ������ �Ҵ�� ���� ���� ��� ����
     *  1) ACTIVITY_CODE_TYPE1���� DA1,RV1,RV2 �߰�
     *  2) ACTIVITY_CODE_TYPE3���� DA1,RV1,RV2,AJ1,AJ2 �߰�
     *  3) ACTIVITY_CODE_TYPE4���� DA1,RV1,RV2,AJ1,AJ2 �߰�
     *  4) ACTIVITY_CODE_TYPE1�� ��� '�Ҵ�Ǵ� ���� A-C-CH or A-E-21�� ���۵Ǿ�� �Ѵ�' �� ����
     * 
     * 
     * 
     * 1. �۾��ڵ尡 TT1,TT2,PT3,DA1,RV1,RV2 �� ��� Ư�� ü����� �Ǵ� ���ϰ��� �Ǵ� ���� �Ҵ� �ʼ�.
     * 2. �۾��ڵ尡 TT1,TT2,PT3,CP1,DA1,RV1,RV2,AJ1,AJ2 �� �ƴ� ��� Ư������ �Ǵ� ���� �Ҵ�Ǿ� ������ �ȵ�.
     * 3. �۾��ڵ尡 TC�� ��� Ư�� ��ũ��ġ �Ǵ� ���� �Ҵ� �ʼ�.
     * 4. �۾��ڵ尡 TC�� �ƴ� ��� üũ ���� ����
     * 5. �۾��ڵ尡 TT1,TT2,PT1,PT2,PT3,TC,CP1,DA1,RV1,RV2,AJ1,AJ2 �� �ƴ� ��� ��� ���� ������ �Ҵ�Ǿ� ������ �ȵ�.
     * 
     * @param resultsByItem
     * @param toolCheckList
     * @param equipCheckList 
     * @throws Exception
     * @throws TCException
     */
    public void toolCheckWithActivityCode(HashMap<Integer, StringBuilder> resultsByItem, ArrayList<TCComponentBOMLine> toolCheckList, ArrayList<TCComponentBOMLine> equipCheckList) throws Exception, TCException {
        //üũ ����� �� ���� ����� TC Preference�� ���� ��������
        String errorMsg ="";
        String preferenceName = "SYMC_LIST_OF_TOOLS_FOR_VALIDATION";
        TCPreferenceService prefService = ((TCSession) AIFUtility.getDefaultSession()).getPreferenceService();
//        String[] arrToolCheckKeyList = prefService.getStringArray(TCPreferenceService.TC_preference_site, preferenceName);
        String[] arrToolCheckKeyList = prefService.getStringValuesAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
        if(arrToolCheckKeyList == null || arrToolCheckKeyList.length <= 0) {
            throw new Exception("Please Check Preference SYMC_LIST_OF_TOOLS_FOR_VALIDATION");
        }
        
        String[] COMBINATION_TOOL_INDEPENDENT = getToolCheckKey(arrToolCheckKeyList, "COMBINATION_TOOL_INDEPENDENT");
        String[] COMBINATION_TOOL_WITH_SOCKET = getToolCheckKey(arrToolCheckKeyList, "COMBINATION_TOOL_WITH_SOCKET");
        String[] SOCKET_TOOL_WITH_COMBINATION = getToolCheckKey(arrToolCheckKeyList, "SOCKET_TOOL_WITH_COMBINATION");
        String[] TORQUEWRENCH_TOOL_INDEPENDENT = getToolCheckKey(arrToolCheckKeyList, "TORQUEWRENCH_TOOL_INDEPENDENT");
        String[] TORQUEWRENCH_TOOL_WITH_SOCKET = getToolCheckKey(arrToolCheckKeyList, "TORQUEWRENCH_TOOL_WITH_SOCKET");
        String[] SOCKET_TOOL_WITH_TORQUEWRENCH = getToolCheckKey(arrToolCheckKeyList, "SOCKET_TOOL_WITH_TORQUEWRENCH");
        
        //Activity ��� ��������
        AIFComponentContext[] activityParentComp = target.getRelated(SDVPropertyConstant.BL_ACTIVITY_LINES);
        TCComponentCfgActivityLine rootActLine = null;
        if(activityParentComp!=null && activityParentComp.length>0){
        	rootActLine = (TCComponentCfgActivityLine) activityParentComp[0].getComponent();
        }
        TCComponentMEActivity rootActivity = null;
        if(rootActLine!=null){
        	rootActivity = (TCComponentMEActivity) rootActLine.getUnderlyingComponent();
        }
        TCComponent[] childComps = null;
        if(rootActivity!=null){
        	childComps = ActivityUtils.getSortedActivityChildren(rootActivity);
        }
        
        if(childComps==null){
        	return;
        }
        
            
        // (üũ1) �۾��ڵ尡 TT1,TT2,PT3,DA1,RV1,RV2 �� ���
        if(checkExistActivity(childComps, ACTIVITY_CODE_TYPE1.split(",")))
        {
         // [ü������� (A-IG or A-IS or A-IC or A-CG or A-CS or A-CC or A-OG or A-OS or A-OC or A-NG or A-NC or A-SG or A-SS or A-SC or E-SG or E-SS or E-SC or E-IG or E-IS or E-IC or E-CG or E-CS or E-CC or E-OG or E-OS or E-OC or E-NG or E-NS or E-NC or E-NH) AND ������ (H-IS or H-SB or H-SS or H-SP)]
            // �Ǵ� [ü������� A-RO or A-RC or A-NS or A-NH or A-RS or A-RM or A-PG or H-SO or H-SC or H-SM] �̾�� �Ѵ�. 
            // �Ǵ� [��ũ��ġ�� H-TS AND ������ H-IS or H-SS or H-SP] or  ��ũ��ġ�� H-TO or ��ũ��ġ�� H-TC] �̾�� �Ѵ�.
            // �Ǵ� [����  A-C-CH or A-E-21] �� ���۵Ǿ�� �Ѵ�. 
            boolean toolCheckResult = ((checkExistTool(toolCheckList, COMBINATION_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_COMBINATION))
                                                   || (checkExistTool(toolCheckList, COMBINATION_TOOL_INDEPENDENT) || checkExistTool(toolCheckList, new String[] {"H-SO", "H-SC", "H-SM"}))
                                                   || (checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_TORQUEWRENCH))
                                                   || checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_INDEPENDENT));
            boolean equipCheckResult = checkExistEquip(equipCheckList, new String[] {"A-C-CH", "A-E-21"});  
            if(!toolCheckResult && !equipCheckResult) 
            {
                errorMsg = getMessage(ERROR_TYPE_TOOL_ASSIGN_INVALID, ACTIVITY_CODE_TYPE1.toString());
                addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            }
        } 
        // (üũ2) �۾��ڵ尡 TT1,TT2,PT3,CP1,DA1,RV1,RV2,AJ1,AJ2 �� �ƴ� ���
        else if(!checkExistActivity(childComps, ACTIVITY_CODE_TYPE4.split(",")))
        {
            // [ü������� (A-IG or A-IS or A-IC or A-CG or A-CS or A-CC or A-OG or A-OS or A-OC or A-NG or A-NC or A-SG or A-SS or A-SC or E-SG or E-SS or E-SC or E-IG or E-IS or E-IC or E-CG or E-CS or E-CC or E-OG or E-OS or E-OC or E-NG or E-NS or E-NC or E-NH) AND ������ (H-IS or H-SB or H-SS or H-SP)]
            // �Ǵ�  [ü������� A-RO or A-RC or A-NS or A-NH or A-RS or A-RM or A-PG] �� �Ҵ� �Ǿ�� �ȵȴ�.
           // �Ǵ�  [���� A-C-CH ] �� ���۵Ǵ� ���� �Ҵ�Ǿ�� �ȵȴ�.
            boolean toolCheckResult = ((checkExistTool(toolCheckList, COMBINATION_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_COMBINATION))
                                                   || checkExistTool(toolCheckList, COMBINATION_TOOL_INDEPENDENT));
            boolean equipCheckResult = checkExistEquip(equipCheckList, new String[] {"A-C-CH"});
            if(toolCheckResult || equipCheckResult) 
            {
                errorMsg = getMessage(ERROR_TYPE_TOOL_NOTASSIGN_INVALID, ACTIVITY_CODE_TYPE4.toString());
                addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            }
        }
        
        // (üũ3) �۾��ڵ尡 TC�� ���
        if(checkExistActivity(childComps, ACTIVITY_CODE_TYPE2.split(",")))
        {
            // [��ũ��ġ�� H-TS AND ������ H-IS or H-SS or H-SP] or  ��ũ��ġ�� H-TO or ��ũ��ġ�� H-TC] �̾�� �Ѵ�.
            // �Ǵ�  [���� A-E-21-010, A-E-21-030, A-E-21-040, A-E-21-051 ] �̾�� �Ѵ�. 
            boolean toolCheckResult = ((checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_TORQUEWRENCH))
                                                   || checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_INDEPENDENT));
            boolean equipCheckResult = checkExistEquip(equipCheckList, new String[] {"A-E-21-010", "A-E-21-030", "A-E-21-040", "A-E-21-051"});
            if(!toolCheckResult && !equipCheckResult) 
            {
                errorMsg = getMessage(ERROR_TYPE_TOOL_ASSIGN_INVALID, ACTIVITY_CODE_TYPE2.toString());
                addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            }
        }
        // (üũ4) �۾��ڵ尡 TC�� �ƴ� ��� => [SR141014-037][20141014] shcho,  �۾��ڵ尡 TC�� �ƴ� ����� ó�� ������ Validation���� ���� ó�� 
        else 
        {
            // [��ũ��ġ�� H-TS AND ������ H-IS or H-SS or H-SP] or ��ũ��ġ�� H-TO or ��ũ��ġ�� H-TC]�� �Ҵ� �Ǿ�� �ȵȴ�.
            //boolean toolCheckResult = ((checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_WITH_SOCKET) && checkExistTool(toolCheckList, SOCKET_TOOL_WITH_TORQUEWRENCH)) || checkExistTool(toolCheckList, TORQUEWRENCH_TOOL_INDEPENDENT));
            //if(toolCheckResult) 
            //{
            //    errorMsg = getMessage(ERROR_TYPE_TOOL_NOTASSIGN_INVALID, ACTIVITY_CODE_TYPE2.toString());
            //    addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            //}
        }
        
        // (üũ5) �۾��ڵ尡 TT1,TT2,PT1,PT2,PT3,TC,CP1,DA1,RV1,RV2,AJ1,AJ2 �� �ƴ� ���
        if(!checkExistActivity(childComps, ACTIVITY_CODE_TYPE3.split(",")))
        {
            // ��� ���� ������ �Ҵ� �Ǿ�� �ȵȴ�.
            boolean toolCheckResult = checkSocketTool(toolCheckList);
            if(toolCheckResult) 
            {
                errorMsg = getMessage(ERROR_TYPE_TOOL_NOTASSIGN_INVALID, ACTIVITY_CODE_TYPE3.toString());
                addErrorMsg(resultsByItem, MSG_TYPE_TOOL, errorMsg);
            }
        }
        
    }


    /**
     * Preference�� ���� ������ ToolCkeckKeyList���� ��� Category �� �ش��ϴ� ToolCheckKey���� �̾Ƽ� �����ϴ� �Լ�  
     * @param arrToolCkeckKeyList, ToolCategoryName
     * @return String[]
     */
    public String[] getToolCheckKey(String[] arrToolCkeckKeyList, String ToolCategoryName) throws Exception{
        for (String toolCkeckKeyRow : arrToolCkeckKeyList) {
            if(toolCkeckKeyRow.startsWith(ToolCategoryName)) {
                String toolCkeckKeys = toolCkeckKeyRow.split(":")[1];
                return toolCkeckKeys.split(",");
            }
        }
        
        return null;
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
        // ���� �ڵ�
        String stationNo = target.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO);
        // �۾��� �ڵ�
        String workerCode = target.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE);

        if (!stationNo.startsWith(lineCode)) {
            addErrorMsg(resultsByItem, MSG_TYPE_OP_LINE_MATCH, getMessage(ERROR_TYPE_OP_STATION_NOT_INVALID, opItemId));
        }
        if (!workerCode.startsWith(lineCode)) {
            addErrorMsg(resultsByItem, MSG_TYPE_OP_LINE_MATCH, getMessage(ERROR_TYPE_OP_WORKERCODE_NOT_INVALID, opItemId));
        }
    }

    /**
     * ������ ID �� ���ڸ�(�Ӽ� MainClass, SubClass�� ����) �� üũ �Լ�
     * (�迭 arrToolCodes�� �ڵ尪�� �ش��ϴ� ������ toolList �ִ��� üũ�ϴ� �Լ�)
     * 
     * @param toolList, arrToolCodes
     * @return boolean
     * @throws TCException 
     */    
    private boolean checkExistTool(ArrayList<TCComponentBOMLine> toolList, String[] arrToolCodes) throws TCException {
        for (TCComponentBOMLine toolBOMLine : toolList) {
            TCComponentItemRevision toolItemRevision = toolBOMLine.getItemRevision();
            String propValue1 = toolItemRevision.getProperty(SDVPropertyConstant.TOOL_MAIN_CLASS);
            String propValue2 = toolItemRevision.getProperty(SDVPropertyConstant.TOOL_SUB_CLASS);
            
            for (String toolKeyCode : arrToolCodes) {
                if (toolKeyCode.equals(propValue1 + "-" + propValue2)) {
                    return true;
                }                
            }
        }
        return false;
    }
    
    
    /**
     * ������ ID �� 3�ڸ� �� üũ �Լ�
     * 
     * @param toolList, arrToolCodes
     * @return boolean
     * @throws TCException 
     */    
    private boolean checkExistEquip(ArrayList<TCComponentBOMLine> equipList, String[] arrEquipCodes) throws TCException {
        for (TCComponentBOMLine equipBOMLine : equipList) {
            TCComponentItemRevision equipItemRevision = equipBOMLine.getItemRevision();
            String equipId = equipItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            
            for (String equipKeyCode : arrEquipCodes) {
                if (equipId.startsWith(equipKeyCode)) {
                    return true;
                }                
            }
        }
        return false;
    }
    
    /**
     * ���� ���� üũ �Լ� (toolList�� ���ϰ����� �ִ��� üũ)
     * 
     * @param toolList
     * @return boolean
     * @throws TCException 
     */    
    private boolean checkSocketTool(ArrayList<TCComponentBOMLine> toolList) throws TCException {
        for (TCComponentBOMLine toolBOMLine : toolList) {
            TCComponentItemRevision toolItemRevision = toolBOMLine.getItemRevision();
            String toolCategory = toolItemRevision.getProperty(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY);          
            
            if (toolCategory.equals("SOC")) {
                return true;
            }                
        }
        return false;
    }

    /**
     * Activity�� SystemCode(�۾��ڵ�) üũ �Լ�
     * (�迭 arrActivityCodes�� �۾��ڵ尪�� �ش��ϴ� Activity�� arrActivityTCComps�� �ִ��� üũ�ϴ� �Լ�)
     * 
     * @param arrActivityTCComps, arrActivityCodes
     * @return boolean
     */
    private boolean checkExistActivity(TCComponent[] arrActivityTCComps, String[] arrActivityCodes) throws TCException {
        for (TCComponent childActivityTCComp : arrActivityTCComps) {
            TCComponentMEActivity childActivity = (TCComponentMEActivity) childActivityTCComp;
            childActivity.refresh();
            
            String systemCode = childActivity.getProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE);
            
            for(String activitySysCode : arrActivityCodes) {
                if(systemCode.startsWith(activitySysCode.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
