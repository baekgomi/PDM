package com.symc.plm.me.sdv.service.resource;

public class ResourceIDConstants {

    /* ID Validation�� ���� �ڸ��� ���� �迭 */
    public static final int[] ASSY_TOOL_GENERAL = { 1, 2, 3, 3 }; // ���� �Ϲݰ���
    public static final int[] ASSY_TOOL_SOCKET = { 1, 2, 3, 3, 3, 1 }; // ���� ���ϰ���
    public static final int[] BODY_TOOL_GENERAL = { 1, 2, 2 }; // ��ü �Ϲݰ���
    public static final int[] PAINT_TOOL_GENERAL = { 1, 3, 2 }; // ���� �Ϲݰ���
    public static final int[] PAINT_TOOL_STAY = { 1, 2, 3, 3 }; // ���� STAY����

    public static final int[] ASSY_EQUIP_GENERAL = { 1, 1, 2, 3 }; // ���� �Ϲݼ���
    public static final int[] ASSY_EQUIP_JIG = { 1, 2, 1, 3 }; // ���� JIG����
    public static final int[] BODY_EQUIP_ROBOT = { 1, 2, 3, 0 }; // ��ü �κ�
    public static final int[] BODY_EQUIP_GUN = { 1, 2, 2, 0 }; // ��ü ��
    //[SR140512-016][20140512] shcho, ��ü Resource ID ü�� ���� (ID 4��° �ʵ� 5�ڸ����� 6�ڸ��� ����) -----------
    public static final int[] BODY_EQUIP_GENERAL = { 1, 2, 2, 6, 2 }; // ��ü �Ϲݼ���,�κ��δ뼳��,JIG����
    //----------------------------------------------------------------------------------------------------------
    public static final int[] PAINT_EQUIP_GENERAL = { 1, 1, 2, 2, 2 }; // ���� �Ϲݼ���
}
