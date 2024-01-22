/**
 *
 */
package com.symc.plm.me.sdv.operation.meco.validate;

import com.teamcenter.rac.util.Registry;

/**
 * Class Name : OperationValidation
 * Class Description :
 *
 *[SR150415-005][20150518] shcho, ������ �Ҵ� ���� ���� ��� �߰� (���������� �ƴ� ���� �������� �����ϴ��� ����)
 *
 * @date 2013. 12. 10.
 *
 */
public abstract class OperationValidation<T, R> {

    public T target;
    public R result;

    public static Registry registry = Registry.getRegistry(OperationValidation.class);

    public final static int MSG_TYPE_OP_DUPASSIGN = 0; // ���� �ߺ� �Ҵ� �޼���
    public final static int MSG_TYPE_OP_LINE_MATCH = 1; // ���� ���� ����ġ �޼���
    public final static int MSG_TYPE_ENDITEM = 2; // END ITEM �޼���
    public final static int MSG_TYPE_TOOL = 3; // ���� �޼���
    public final static int MSG_TYPE_SUBPART = 4; // ������ �޼���
    public final static int MSG_TYPE_EQUIPMENT = 5; // ������ �޼���
    public final static int MSG_TYPE_ACTIVITY = 6; // ACTIVITY �޼���

    public final static int ERROR_TYPE_ENITEM_QTY_EMPTY = 10; // End Item ����
    public final static int ERROR_TYPE_ENDITEM_SEQ_EMPTY = 11; // End Item Sequence

    public final static int ERROR_TYPE_SUBPART_OPTION_EMPTY = 12; // ������ �ɼ�
    public final static int ERROR_TYPE_SUBPART_QTY_EMPTY = 13; // ������ ����
    public final static int ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY = 14; // ������ ��,�߰� ����
    public final static int ERROR_TYPE_TOOL_QTY_EMPTY = 15; // ���� ����
    public final static int ERROR_TYPE_TOOL_QTY_INVALID = 16; // ���� ���� �߸��� ��
    public final static int ERROR_TYPE_TOOL_TORQUE_EMPTY = 17; // ���� ��ũ
    public final static int ERROR_TYPE_TOOL_NOTASSIGNED = 18; // ���� ���Ҵ�
    public final static int ERROR_TYPE_PS_NOT_EXIST = 19; // �۾� ǥ�ؼ� ������
    public final static int ERROR_TYPE_ACTIVITY_FREQUENCY = 20; // ���̵� ���
    public final static int ERROR_TYPE_OP_STATION_NOT_INVALID = 21; // ���� ���� �ڵ�
    public final static int ERROR_TYPE_OP_WORKERCODE_NOT_INVALID = 22; // ���� �۾��� �ڵ�
    public final static int ERROR_TYPE_OP_DUPLICATE_ASSIGNED = 23; // ���� �ߺ��Ҵ�
    public final static int ERROR_TYPE_WP_NOT_EXIST = 24; // ���� ����ǥ ������
    public final static int ERROR_TYPE_WP_NOTASSIGNED = 25; // ������ ���Ҵ�
    public final static int ERROR_TYPE_CHECK_UNLINK = 26; // �Ҵ��� ������
    public final static int ERROR_TYPE_WP_CONDITION_SHEET_UPDATE = 27; // ��������ǥ Update �ʿ���
    public final static int ERROR_TYPE_GUN_DUPLICATE_ASSIGNED = 28;  // ���� ������ Gun �ߺ� �Ҵ�

    public final static int ERROR_TYPE_ENDITEM_SEQ_INVALID = 31;  //End Item Sequence (1~9)
    public final static int ERROR_TYPE_TOOL_SEQ_INVALID = 32;  //���� Sequence (10~200)
    public final static int ERROR_TYPE_EQUIPMENT_SEQ_INVALID = 33;  //���� Sequence (210~500)
    public final static int ERROR_TYPE_SUBPART_SEQ_INVALID = 34;  //������ Sequence (510~800)

    public final static int ERROR_TYPE_TOOL_TORGUE_VALUE_INVALID = 35;  //��ũ �� üũ
    public final static int ERROR_TYPE_ACTIVITY_CATEGOLRY_EMPTY = 36;  //Category ���� ���Է� �Ǿ��� ��� üũ
    public final static int ERROR_TYPE_WP_MERESOURCE_CHECK = 37;  //���������� MEResource �Ҵ� üũ
    public final static int ERROR_TYPE_OP_NOT_EXIST = 38;  // ������ ���� ���翩�� üũ
    public final static int ERROR_TYPE_ACTIVITY_WORKER_EMPTY = 39;  // Activity Category ���� �۾��� ������ ��� �۾��� �ڵ尡 ��� �ִ��� üũ
    public final static int ERROR_TYPE_ACTIVITY_UNITTIME_EMPTY = 40; // Activity �� UnitTime �� ��� �ִ��� üũ
   
    public final static int ERROR_TYPE_BOMLINE_STATION_LINE_INVALID = 41; //���õ� BOMLine�� Line �Ǵ� Station ���� üũ
    public final static int ERROR_TYPE_TOOL_ASSIGN_INVALID = 42;  //�Ҵ�� ���� ���� (�ʼ� �Ҵ� üũ)
    public final static int ERROR_TYPE_TOOL_NOTASSIGN_INVALID = 43;  //�Ҵ�� ���� ���� (�Ҵ� ���� üũ)

    public final static int MSG_VALID_START = 30;

    // [SR150415-005][20150518] shcho, ������ �Ҵ� ���� ���� ��� �߰� (���������� �ƴ� ���� �������� �����ϴ��� ����)
    public final static int ERROR_TYPE_WP_ASSIGNED = 44; // ������ �߸� �� �Ҵ�

    public R execute(T target) {
        try {

            this.target = target;
            executeValidation();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    protected abstract void executeValidation() throws Exception;

    public static String getMessage(int type, String param1) {
        return getMessage(type, param1, "", true);
    }

    public static String getMessage(int type, String param1, String param2) {
        return getMessage(type, param1, param2, true);
    }

    /**
     * ������ Error Message �� ������
     * FIXME: Registry ���� �ʿ�
     *
     * @method getErrMsg
     * @date 2013. 12. 11.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getMessage(int type, String param1, String param2, boolean isNewLine) {
        String errorMsg = "";
        switch (type) {
        case ERROR_TYPE_ENITEM_QTY_EMPTY:
            errorMsg = "     " + registry.getString("EndItemQtyEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_ENDITEM_SEQ_EMPTY:
            errorMsg = "     " + registry.getString("EndItemSeqEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_SUBPART_QTY_EMPTY:
            errorMsg = "     " + registry.getString("SubPartQtyEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_SUBPART_OPTION_EMPTY:
            errorMsg = "     " + registry.getString("SubPartOptionEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_SUBPART_DAYORNIGHT_EMPTY:
            errorMsg = "     " + registry.getString("SubPartDayOrNightEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_QTY_EMPTY:
            errorMsg = "     " + registry.getString("ToolQtyEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_QTY_INVALID:
            errorMsg = "     " + registry.getString("ToolQtyInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_TORQUE_EMPTY:
            errorMsg = "     " + registry.getString("TorqueEmty.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_NOTASSIGNED:
            errorMsg = "     " + registry.getString("TorqueNotAssigned.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_PS_NOT_EXIST:
            errorMsg = "     " + registry.getString("PSNotExist.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_ACTIVITY_FREQUENCY:
            errorMsg = "     " + registry.getString("FrequencyOverOne.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_OP_STATION_NOT_INVALID:
            errorMsg = "     " + registry.getString("StationCodeInvalid.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_OP_WORKERCODE_NOT_INVALID:
            errorMsg = "     " + registry.getString("WorkerCodeInvalid.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_OP_DUPLICATE_ASSIGNED:
            errorMsg = "     " + registry.getString("OPDuplicated.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_WP_NOT_EXIST:
            errorMsg = "     " + registry.getString("WPNotExist.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_WP_NOTASSIGNED:
            errorMsg = "     " + registry.getString("WPNotAssigned.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_CHECK_UNLINK:
            errorMsg = "     " + registry.getString("CheckUnLink.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_WP_CONDITION_SHEET_UPDATE:
            errorMsg = "     " + registry.getString("WPConditionSheetUpdate.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_GUN_DUPLICATE_ASSIGNED:
            errorMsg = "     " + registry.getString("GunDuplicated.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case MSG_VALID_START:
            errorMsg = " " + registry.getString("StartVerify.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_ENDITEM_SEQ_INVALID:
            errorMsg = "     " + registry.getString("EndItemSeqInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_SEQ_INVALID:
            errorMsg = "     " + registry.getString("ToolSeqInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_EQUIPMENT_SEQ_INVALID:
            errorMsg = "     " + registry.getString("EquipmentSeqInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_SUBPART_SEQ_INVALID:
            errorMsg = "     " + registry.getString("SubPartSeqInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_TORGUE_VALUE_INVALID:
            errorMsg = "     " + registry.getString("ToolTorgueValueInvalid.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_ACTIVITY_CATEGOLRY_EMPTY:
            errorMsg = "     " + registry.getString("CategoryEmpty.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_WP_MERESOURCE_CHECK:
            errorMsg = "     " + registry.getString("MERsourceAssigned.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_OP_NOT_EXIST:
            errorMsg = "     " + registry.getString("OperationNotExist.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_ACTIVITY_WORKER_EMPTY:
            errorMsg = "     " + registry.getString("WorkerCodeEmpty.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_ACTIVITY_UNITTIME_EMPTY:
            errorMsg = "     " + registry.getString("UnitTimeEmpty.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_BOMLINE_STATION_LINE_INVALID:
            errorMsg = "     " + registry.getString("NotValidSelectedType2.MSG").replace("%0", param1).replace("%1", param2);
            break;
        case ERROR_TYPE_TOOL_ASSIGN_INVALID:
            errorMsg = "     " + registry.getString("ToolAssignInvalid.MSG").replace("%0", param1);
            break;
        case ERROR_TYPE_TOOL_NOTASSIGN_INVALID:
            errorMsg = "     " + registry.getString("ToolNotAssignInvalid.MSG").replace("%0", param1);
            break;
        // [SR150415-005][20150518] shcho, ������ �Ҵ� ���� ���� ��� �߰� (���������� �ƴ� ���� �������� �����ϴ��� ����)
        case ERROR_TYPE_WP_ASSIGNED:
            errorMsg = "     " + registry.getString("WPAssigned.MSG").replace("%0", param1);
            break;
        }
        errorMsg += (isNewLine ? "\r\n" : "");
        return errorMsg;
    }
}
