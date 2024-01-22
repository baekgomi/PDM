/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.util;

/**
 * Class Name : PEExcelConstants
 * Class Description :
 * 
 * @date 2013. 12. 5.
 * 
 */
public class PEExcelConstants {

    public static final String MASTER = "MASTER";
    public static final String BOM = "BOM";
    public static final String DATASET = "DATASET";

    // EXCEL ROW, CELL Contol
    // ROW
    public static final int START_ROW_INDEX = 2;
    // COLUMN
    public static final int END_CHAR_COLUMN_INDEX = 0; // EOF ���� �÷� INDEX
    public static final int START_COLUMN_INDEX = END_CHAR_COLUMN_INDEX;
    public static final String END_CHAR_ROW_STRING = "EOF";

    /*
     * #1 plant@Plant@
     * #2 nameshort@Shop Code
     * #3 productno@Product No.
     * #4 plant@Plant
     * #5 shopcode@Shop Code
     * #6 productno@Product No.
     * #7 nameshort@Line Code
     * #8 planningversion@Planning Version
     * #9 projectno@Project No
     * #10 Left(shopcode,2)@Shop/Line Code
     * #11 sheetno@Sheet No.
     * #12 planningversion@Planning Ver.
     * #13 carbodyposition@Work Location (for ALB)
     */

    // COMMON BOM COLUMN
    public static final int COMMON_BOM_PLANT_CODE_COLUMN_INDEX = 1; // Plant
    public static final int COMMON_BOM_SHOP_CODE_COLUMN_INDEX = 2; // Shop Code
    public static final int COMMON_BOM_PRODUCT_NO_COLUMN_INDEX = 3; // Product No.
    public static final int COMMON_BOM_LINE_CODE_COLUMN_INDEX = 4; // Line Code.
    public static final int COMMON_BOM_PLANNING_VERSION_COLUMN_INDEX = 5; // ����������
    public static final int COMMON_BOM_PROJECT_NO_COLUMN_INDEX = 6; // ������ȣ-����
    public static final int COMMON_BOM_SHOP_LINE_CODE_COLUMN_INDEX = 7; // ������ȣ-����
    public static final int COMMON_BOM_SHEET_NO_COLUMN_INDEX = 8; // ������ȣ
    // COMMON MASTER COLUMN
    public static final int COMMON_MASTER_PROJECT_NO_COLUMN_INDEX = 1; // ������ȣ-����
    public static final int COMMON_MASTER_SHOP_LINE_COLUMN_INDEX = 2; // ������ȣ-����
    public static final int COMMON_MASTER_SHEET_NO_COLUMN_INDEX = 3; // ������ȣ
    public static final int COMMON_MASTER_PLANNING_VERSION_COLUMN_INDEX = 4; // ����������

    // OPERATION COLUMN
    // LINE ITEM ID:
    // COMMON_BOM_PLANT_CODE_COLOUMN_INDEX + "-" + COMMON_BOM_SHOP_CODE_COLOUMN_INDEX + "-" + COMMON_BOM_LINE_CODE_COLOUMN_INDEX
    // + "-" + COMMON_BOM_PRODUCT_NO_COLOUMN_INDEX + "-" + COMMON_BOM_PLANNING_VERSION_COLOUMN_INDEX
    //
    // OPERATION ITEM ID :
    // COMMON_BOM_PROJECT_NO_COLOUMN_INDEX + "-" + COMMON_BOM_LINE_CODE_COLOUMN_INDEX
    // + "-" + COMMON_BOM_SHEET_NO_COLOUMN_INDEX + "-" + COMMON_BOM_PLANNING_VERSION_COLOUMN_INDEX
    // BOM
    public static final int OPERATION_BOM_END_COLUMN_INDEX = 10;
    public static final int OPERATION_BOM_OPTION_COLUMN_INDEX = 10;
    // MASTER
    /*
     * Ư��Ư�� �Ӽ� �߰��� ���� End Column Index �÷� ��ȣ Ȯ��
     */
//    public static final int OPERATION_MASTER_END_COLUMN_INDEX = 19;
    public static final int OPERATION_MASTER_END_COLUMN_INDEX = 20;
    public static final int OPERATION_MASTER_WORK_AREA_COLUMN_INDEX = 5; // �۾���ġ
    public static final int OPERATION_MASTER_KOR_NAME_COLUMN_INDEX = 6; // ������ - ����
    public static final int OPERATION_MASTER_ENG_NAME_COLUMN_INDEX = 7; // ������ - ����
    public static final int OPERATION_MASTER_WORKER_CODE_COLUMN_INDEX = 8; // �۾��ڱ����ڵ�
    public static final int OPERATION_MASTER_ITEM_UL_COLUMN_INDEX = 9; // ����������ġ
    public static final int OPERATION_MASTER_DWG_NO_COLUMN_INDEX = 10; // ���������ȣ ����Ʈ
    public static final int OPERATION_MASTER_STATION_NO_COLUMN_INDEX = 11; // Station No.
    public static final int OPERATION_MASTER_DR_COLUMN_INDEX = 12; // ����
    public static final int OPERATION_MASTER_ASSEMBLY_SYSTEM_COLUMN_INDEX = 13; // �����ý���
    // 14. ������ȣ
    public static final int OPERATION_MASTER_PROCESS_SEQ_COLUMN_INDEX = 15; // Sequence No.
    public static final int OPERATION_MASTER_WORK_UBODY_COLUMN_INDEX = 16; // WORK_UBODY ��ü�۾�����(N/Y)
    public static final int OPERATION_MASTER_REP_VHICLE_CHECK_COLUMN_INDEX = 17; // ��ǥ���� ����(N/Y)
    public static final int OPERATION_MASTER_SHEET_KO_FILE_PATH_COLUMN_INDEX = 18; // �����۾�ǥ�ؼ� ���ϰ��
    public static final int OPERATION_MASTER_SHEET_KO_YN_COLUMN_INDEX = 19; // �����۾�ǥ�ؼ� I/F ����
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Ư�� Ư�� �÷� �߰�
    public static final int OPERATION_MASTER_SHEET_SPECIAL_CHARICTORISTIC = 20;
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    // ACTIVITY COLUMN
    // BOM
    public static final int ACTIVITY_BOM_END_COLUMN_INDEX = 10;
    public static final int ACTIVITY_BOM_SEQ_COLUMN_INDEX = 10; // �۾�����
    // MASTER
    public static final int ACTIVITY_MASTER_END_COLUMN_INDEX = 15;
    public static final int ACTIVITY_MASTER_SEQ_COLUMN_INDEX = 5; // �۾�����
    public static final int ACTIVITY_MASTER_WORK_CODE_MAIN_COLUMN_INDEX = 6; // �۾����
    public static final int ACTIVITY_MASTER_WORK_CODE_SUB_COLUMN_INDEX = 7; // ����
    public static final int ACTIVITY_MASTER_FREQUENCY_COLUMN_INDEX = 8; // ���̵�(FREQUENCY)
    public static final int ACTIVITY_MASTER_KOR_NAME_COLUMN_INDEX = 9; // �۾�����(����)
    public static final int ACTIVITY_MASTER_ENG_NAME_COLUMN_INDEX = 10; // �۾�����(����)
    public static final int ACTIVITY_MASTER_TIME_COLUMN_INDEX = 11; // �۾��ð�
    public static final int ACTIVITY_MASTER_CATEGORY_COLUMN_INDEX = 12; // �ڵ�/����/���� 
    public static final int ACTIVITY_MASTER_TOOL_ID_COLUMN_INDEX = 13; // ���� ID
    public static final int ACTIVITY_MASTER_KPC_COLUMN_INDEX = 14; // KPC
    public static final int ACTIVITY_MASTER_KPC_BASIS_COLUMN_INDEX = 15; // KPC ��������

    // TOOL COLUMN
    // BOM
    public static final int TOOL_BOM_END_COLUMN_INDEX = 13;
    public static final int TOOL_BOM_TOOL_NO_COLUMN_INDEX = 10;// ������ȣ
    public static final int TOOL_BOM_QUANTITY_INDEX = 11;// ����
    public static final int TOOL_BOM_TORQUE_COLUMN_INDEX = 12;// Torque
    public static final int TOOL_BOM_SEQ_COLUMN_INDEX = 13;// ����SEQ
    // MASTER
    public static final int TOOL_MASTER_END_COLUMN_INDEX = 20;
    public static final int TOOL_MASTER_ITEM_ID_COLUMN_INDEX = 1;// ������ȣ
    public static final int TOOL_MASTER_KOR_NAME_COLUMN_INDEX = 2;// ������-����
    public static final int TOOL_MASTER_ENG_NAME_COLUMN_INDEX = 3;// ������-����
    public static final int TOOL_MASTER_MAIN_CLASS_COLUMN_INDEX = 4;// ��з�
    public static final int TOOL_MASTER_SUB_CLASS_COLUMN_INDEX = 5;// �ߺз�
    public static final int TOOL_MASTER_PURPOSE_KOR_COLUMN_INDEX = 6;// ���� �뵵
    public static final int TOOL_MASTER_SPEC_CODE_COLUMN_INDEX = 7;// ����ڵ�
    public static final int TOOL_MASTER_SPEC_KOR_COLUMN_INDEX = 8;// ��� ���-����
    public static final int TOOL_MASTER_SPEC_ENG_COLUMN_INDEX = 9;// ��� ���-����
    public static final int TOOL_MASTER_UNIT_USAGE_COLUMN_INDEX = 10;// �ҿ䷮ ����
    public static final int TOOL_MASTER_MATERIAL_COLUMN_INDEX = 11;// ���� ����
    public static final int TOOL_MASTER_TORQUE_VALUE_COLUMN_INDEX = 12;// ��ũ��
    public static final int TOOL_MASTER_MAKER_COLUMN_INDEX = 13;// ���ۻ�
    public static final int TOOL_MASTER_MAKER_AF_CODE_COLUMN_INDEX = 14;// ��ü/AF
    public static final int TOOL_MASTER_TOOL_SHAPE_COLUMN_INDEX = 15;// ����з�
    public static final int TOOL_MASTER_TOOL_LENGTH_COLUMN_INDEX = 16;// ����
    public static final int TOOL_MASTER_TOOL_SIZE_COLUMN_INDEX = 17;// ����� Size
    public static final int TOOL_MASTER_TOOL_MAGNET_COLUMN_INDEX = 18;// �ڼ����Կ���
    public static final int TOOL_MASTER_DESC_COLUMN_INDEX = 19;// Remark
    public static final int TOOL_MASTER_CAD_FILE_PATH_COLUMN_INDEX = 20;// CAD���ϰ��

    // EQUIPMENT COLUMN
    // BOM
    public static final int EQUIPMENT_BOM_END_COLUMN_INDEX = 12;
    public static final int EQUIPMENT_BOM_TEM_ID_COLUMN_INDEX = 10; // �����ȣ
    public static final int EQUIPMENT_BOM_QUANTITY_COLUMN_INDEX = 11; // ���� ����
    public static final int EQUIPMENT_BOM_SEQ_COLUMN_INDEX = 12; // SEQ

    // MASTER
    public static final int EQUIPMENT_MASTER_END_COLUMN_INDEX = 18;
    public static final int EQUIPMENT_MASTER_SHOP_COLUMN_INDEX = 1;// ����
    public static final int EQUIPMENT_MASTER_ITEM_ID_COLUMN_INDEX = 2;// �����ȣ
    public static final int EQUIPMENT_MASTER_KOR_NAME_COLUMN_INDEX = 3;// Main Name(����)
    public static final int EQUIPMENT_MASTER_ENG_NAME_COLUMN_INDEX = 4;// Main Name(����)    
    public static final int EQUIPMENT_MASTER_PURPOSE_KOR_COLUMN_INDEX = 5;// ��� �뵵-����
    public static final int EQUIPMENT_MASTER_PURPOSE_ENG_COLUMN_INDEX = 6;// ��� �뵵-����
    public static final int EQUIPMENT_MASTER_SPEC_KOR_COLUMN_INDEX = 7;// ���� ���-����
    public static final int EQUIPMENT_MASTER_SPEC_ENG_COLUMN_INDEX = 8;// ���� ���-����
    public static final int EQUIPMENT_MASTER_MAIN_CLASS_COLUMN_INDEX = 9;// ��з�
    public static final int EQUIPMENT_MASTER_SUB_CLASS_COLUMN_INDEX = 10;// �ߺз�  
    public static final int EQUIPMENT_MASTER_CAPACITY_COLUMN_INDEX = 11;// ó���ɷ�
    public static final int EQUIPMENT_MASTER_MAKER_COLUMN_INDEX = 12;// ���ۻ�
    public static final int EQUIPMENT_MASTER_NATION_COLUMN_INDEX = 13;// ���Ա���
    public static final int EQUIPMENT_MASTER_INSTALL_YEAR_COLUMN_INDEX = 14;// ��ġ�⵵
    public static final int EQUIPMENT_MASTER_REV_DESC_COLUMN_INDEX = 15;// ���泻������
    public static final int EQUIPMENT_MASTER_JIG_VEHICLE_CODE_COLUMN_INDEX = 16;// JIG-�����ڵ�
    public static final int EQUIPMENT_MASTER_JIG_MAIN_CLASS_COLUMN_INDEX = 17;// JIG-��з�
    public static final int EQUIPMENT_MASTER_CAD_FILE_PATH_COLUMN_INDEX = 18; // CAD���ϰ��

    // END_ITEM COLUMN
    // BOM
    public static final int END_ITEM_BOM_END_COLUMN_INDEX = 14;
    public static final int END_ITEM_BOM_PART_NO_COLUMN_INDEX = 10; // Part No.
    public static final int END_ITEM_BOM_ABS_OCCPUIDS_COLUMN_INDEX = 11; // EBOM ABS Occurrence PUID
    public static final int END_ITEM_BOM_OCCPUID_COLUMN_INDEX = 12;// EBOM Occurrence PUID
    public static final int END_ITEM_BOM_FUNCTION_PART_NO_COLUMN_INDEX = 13;// Function Part Number
    public static final int END_ITEM_BOM_SEQ_COLUMN_INDEX = 14;// ����SEQ

    // SUBSIDIARY COLUMN
    // BOM
    public static final int SUBSIDIARY_BOM_END_COLUMN_INDEX = 14;
    public static final int SUBSIDIARY_BOM_ITEM_ID_COLUMN_INDEX = 10; // ��ǰ��ȣ
    public static final int SUBSIDIARY_BOM_OPTION_COLUMN_INDEX = 11;// OPTION
    public static final int SUBSIDIARY_BOM_DEMAND_QUANTITY_COLUMN_INDEX = 12;// �ҿ䷮
    public static final int SUBSIDIARY_BOM_DEMAND_DIVIDE_GROUP_COLUMN_INDEX = 13;// ������
    public static final int SUBSIDIARY_BOM_SEQ_COLUMN_INDEX = 14;// ����SEQ

}
