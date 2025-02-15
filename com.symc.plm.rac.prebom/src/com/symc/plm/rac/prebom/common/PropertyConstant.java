package com.symc.plm.rac.prebom.common;

/**
 * [20160907][ymjang] 컬럼명 오류 수정
 * [SR170703-020][LJG]Proto Tooling 컬럼 추가
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 */
public class PropertyConstant {

	public static final String ACTIONTYPE_NEW = "New";
	public static final String CCNITEM = "ccnitem";

	public static final String ATTR_NAME_ITEMTYPE = "object_type";
	public static final String ATTR_NAME_ITEMNAME = "object_name";
	public static final String ATTR_NAME_ITEMDESC = "object_desc";
	public static final String ATTR_NAME_ITEMID = "item_id";
	public static final String ATTR_NAME_ITEMREVID = "item_revision_id";
	public static final String ATTR_NAME_UOMTAG = "uom_tag";
	public static final String ATTR_NAME_USERID = "user_id";
	public static final String ATTR_NAME_USERNAME = "user_name";
	public static final String ATTR_NAME_NAME = "name";
	public static final String ATTR_NAME_OWNINGUSER = "owning_user";
	public static final String ATTR_NAME_OWNINGGROUP = "owning_group";
	public static final String ATTR_NAME_LOGINGROUP = "login_group";
	public static final String ATTR_NAME_CREATIONDATE = "creation_date";
	public static final String ATTR_NAME_LASTMODUSER = "last_mod_user";
	public static final String ATTR_NAME_LASTMODDATE = "last_mod_date";
	public static final String ATTR_NAME_DATERELEASED = "date_released";
	public static final String ATTR_NAME_ORIGINALFILENAME = "original_file_name";
	public static final String ATTR_NAME_PERSON = "person";
	public static final String ATTR_NAME_MATURITY = "s7_MATURITY";

	public static final String ATTR_NAME_PROCESSSTAGELIST = "process_stage_list";
	public static final String ATTR_NAME_RELEASESTATUSLIST = "release_status_list";

	public static final String ATTR_NAME_PRODUCTTYPE = "s7_PRODUCT_TYPE";
	public static final String ATTR_NAME_FUNCTIONTYPE = "s7_FUNCTION_TYPE";
	public static final String ATTR_NAME_PROJCODE = "s7_PROJECT_CODE";
	public static final String ATTR_NAME_GMODELCODE = "s7_GMODEL_CODE";
	public static final String ATTR_NAME_CCNNO = "s7_CCN_NO";
	public static final String ATTR_NAME_PARTTYPE = "s7_PART_TYPE";
	public static final String ATTR_NAME_STAGE = "s7_STAGE";
	public static final String ATTR_NAME_REGULARPART = "s7_REGULAR_PART";
	public static final String ATTR_NAME_DISPLAYPARTNO = "s7_DISPLAY_PART_NO";
	public static final String ATTR_NAME_KORNAME = "s7_KOR_NAME";
	public static final String ATTR_NAME_UNIT = "s7_UNIT";
	public static final String ATTR_NAME_BUDGETCODE = "s7_BUDGET_CODE"; //[20180213][LJG] BOMLine으로 이동 ->사용 안함
	public static final String ATTR_NAME_COLORID = "s7_COLOR";
	public static final String ATTR_NAME_SELECTIVEPART = "s7_SELECTIVE_PART";
	public static final String ATTR_NAME_REGULATION = "s7_REGULATION";
	public static final String ATTR_NAME_ESTWEIGHT = "s7_EST_WEIGHT";
	public static final String ATTR_NAME_ACTWEIGHT = "s7_ACT_WEIGHT";
	public static final String ATTR_NAME_CALWEIGHT = "s7_CAL_WEIGHT";
	public static final String ATTR_NAME_CHANGE_DESCRIPTION = "object_desc";

	// PreVehPart 속성 추가
	public static final String ATTR_NAME_UNIQUE_ID = "s7_UNIQUE_ID";

	// PreVehPart Revision 속성 추가
	public static final String ATTR_NAME_TARGET_WEIGHT = "s7_TGT_WEIGHT";
	public static final String ATTR_NAME_SOURCING = "s7_SOURCING";
	public static final String ATTR_NAME_DR = "s7_REGULATION";
	public static final String ATTR_NAME_BOX = "s7_RESPONSIBILITY";
	public static final String ATTR_NAME_CONTENTS = "s7_CONTENTS";
	public static final String ATTR_NAME_OLD_PART_NO = "s7_OLD_PART_NO";
	// public static final String ATTR_NAME_CHG_TYPE_ENGCONCEPT = "s7_CHG_TYPE_ENGCONCEPT"; // N, M, C, D
	public static final String ATTR_NAME_CHG_TYPE_NM = "s7_CHG_TYPE_NM"; // N, M
	public static final String ATTR_NAME_ORIGIN_PROJECT = "s7_ORIGIN_PROJECT"; // 양산차종
	public static final String ATTR_NAME_DESIGN_CONCEPT = "s7_DESIGN_CONCEPT_U"; // 설계 구상서
	public static final String ATTR_NAME_EST_COST_MATERIAL = "s7_EST_COST_MATERIAL"; // 추정 재료비
	public static final String ATTR_NAME_TARGET_COST_MATERIAL = "s7_TARGET_COST_MATERIAL"; // 목표 재료비
	public static final String ATTR_NAME_SELECTED_COMPANY = "s7_SELECTED_COMPANY"; // 업체명
	public static final String ATTR_NAME_CON_DWG_PLAN = "s7_CON_DWG_PLAN"; // Concept Dwg - 계획
	public static final String ATTR_NAME_CON_DWG_PERFORMANCE = "s7_CON_DWG_PERFORMANCE"; // Concept Dwg 실적
	public static final String ATTR_NAME_CON_DWG_TYPE = "s7_CON_DWG_TYPE"; // Concept Dwg 2D/3D
	public static final String ATTR_NAME_DWG_DEPLOYABLE_DATE = "s7_DWG_DEPLOYABLE_DATE"; // 도면 배포 예정일
	public static final String ATTR_NAME_PRD_DWG_PLAN = "s7_PRD_DWG_PLAN"; // 도면 작성(양산) 계획
	public static final String ATTR_NAME_PRD_DWG_PERFORMANCE = "s7_PRD_DWG_PERFORMANCE"; // 도면 작성(양산) 실적
	// public static final String ATTR_NAME_SELECTED_COMPANY = "s7__SELECTED_COMPANY"; // 도면 작성(양산) ECO/No
	
	/**
	 * [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동
	 */
//	public static final String ATTR_NAME_DVP_NEEDED_QTY = "s7_DVP_NEEDED_QTY"; // DVP SAMPLE 필요수량
//	public static final String ATTR_NAME_DVP_USE = "s7_DVP_USE"; // DVP SAMPLE 용도
//	public static final String ATTR_NAME_DVP_REQ_DEPT = "s7_DVP_REQ_DEPT"; // DVP SAMPLE 요청부서
	
	/**
	 * [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동
	 */
//	public static final String ATTR_NAME_ENG_DEPT_NM = "s7_ENG_DEPT_NM"; // 설계담당 팀
//	public static final String ATTR_NAME_ENG_RESPONSIBLITY = "s7_ENG_RESPONSIBLITY"; // 설계 담당 담당
	
	public static final String ATTR_NAME_CIC_DEPT_NM = "s7_CIC_DEPT_NM"; // CIC 팀
	public static final String ATTR_NAME_PRT_TOOLG_INVESTMENT = "s7_PRT_TOOLG_INVESTMENT"; // 예상투자비 PRO TOOL'G
	public static final String ATTR_NAME_PRD_TOOL_COST = "s7_PRD_TOOL_COST"; // 예상투자비 PRO TOOL'G
	public static final String ATTR_NAME_PRD_SERVICE_COST = "s7_PRD_SERVICE_COST"; // 예상투자비 PRO TOOL'G
	public static final String ATTR_NAME_PRD_SAMPLE_COST = "s7_PRD_SAMPLE_COST"; // 예상투자비 PRO TOOL'G
	public static final String ATTR_NAME_TOTAL = "s7_TOTAL"; // 예상투자비 PRO TOOL'G
	//[20160907][ymjang] 컬럼명 오류 수정
	public static final String ATTR_NAME_PUR_DEPT_NM = "s7_PUR_DEPT_NM"; // 예상투자비 PRO TOOL'G
	//public static final String ATTR_NAME_PUR_TEAM = "s7_PUR_TEAM"; // 예상투자비 PRO TOOL'G
	public static final String ATTR_NAME_PUR_RESPONSIBILITY = "s7_PUR_RESPONSIBILITY"; // 예상투자비 PRO TOOL'G
	public static final String ATTR_NAME_EMPLOYEE_NO = "s7_EMPLOYEE_NO"; // 예상투자비 PRO TOOL'G
	public static final String ATTR_NAME_ECO_NO = "s7_ECO"; // Key In ECO NO
	public static final String ATTR_NAME_PRD_PROJ_CODE = "s7_PRD_PROJECT_CODE"; // Key In Project Code
	public static final String ATTR_NAME_PRE_VEH_TYPE_REF = "s7_PreVeh_TypedReference"; 

	// System Concept, Design Concept
	public static final String ATTR_NAME_SYSTEMCODE = "s7_SYSTEM_CODE";
	public static final String ATTR_NAME_SUBSYSTEMCODE = "s7_SUB_SYSTEM_CODE";
	public static final String ATTR_NAME_LHDRHD = "s7_LHDRHD";
	public static final String ATTR_NAME_MATERIALCOST = "s7_MATERIAL_COST";
	public static final String ATTR_NAME_RELREGULATIONS = "s7_REL_REGULATIONS";
	public static final String ATTR_NAME_CLASSIFICATION = "ip_classification";
	public static final String ATTR_NAME_SECRET = "s7_SECRET";
	public static final String ATTR_NAME_FMEA = "s7_FMEA";
	public static final String ATTR_NAME_PRODUCTSPEC = "s7_PRODUCT_SPEC";
	public static final String ATTR_NAME_FUNCTIONSAFETY = "s7_FUNCTION_SAFETY";
	public static final String ATTR_NAME_JOINTWRITER = "s7_JOINT_WRITER";
	public static final String ATTR_NAME_CONSULTATIONDEPT = "s7_CONSULTATION_DEPARTMENT";
	public static final String ATTR_NAME_CONSULTATIONDEPTCODE = "s7_CONSULTATION_DEPT_CODE";
	public static final String ATTR_NAME_REFERENCEDEPT = "s7_REFERENCE_DEPARTMENT";
	public static final String ATTR_NAME_REFERENCEDEPTCODE = "s7_REFERENCE_DEPT_CODE";
	public static final String ATTR_NAME_TEAMLEADER = "s7_TEAMLEADER";
	public static final String ATTR_NAME_SUBTEAMLEADER = "s7_SUB_TEAMLEADER";
	public static final String ATTR_NAME_ISDCS = "s7_ISDCS";
	public static final String ATTR_NAME_ISMIG = "s7_ISMIG";
	public static final String ATTR_NAME_PSC_STATUS = "s7_PSC_STATUS";
	public static final String ATTR_NAME_DCS_STATUS = "s7_DCS_STATUS";
	public static final String ATTR_NAME_SUCCESSOR = "s7_SUCCESSOR";
	public static final String ATTR_NAME_DCSVIEWEDBY = "s7_DCS_VIEWED_BY";

	public static final String ATTR_NAME_VEHICLENO = "s7_VEHICLE_NO";
	public static final String ATTR_NAME_ISNEW = "s7_IS_NEW";
	public static final String ATTR_NAME_BASEPRJ = "s7_BASE_PRJ";
	public static final String ATTR_NAME_ISVEHICLEPRJ = "s7_IS_VEHICLE_PRJ";
	public static final String ATTR_NAME_CARCODE = "s7_CAR_CODE";
	public static final String ATTR_NAME_PHASE = "s7_PHASE";
	public static final String ATTR_NAME_GATE = "s7_GATE";

	public static final String ATTR_NAME_CHGREASON = "s7_CHANGEREASON";
	public static final String ATTR_NAME_PROJECTTYPE = "s7_PROJECT_TYPE";
	public static final String ATTR_NAME_OSPECNO = "s7_OSPEC_NO";
	public static final String ATTR_NAME_COSTDOWN = "s7_COST_DOWN";
	public static final String ATTR_NAME_ORDERINGSPEC = "s7_ORDERING_SPEC";
	public static final String ATTR_NAME_QUALITYIMPROVEMENT = "s7_QUALITY_IMPROVEMENT";
	public static final String ATTR_NAME_CORRECTIONOFEPL = "s7_CORRECTION_OF_EPL";
	public static final String ATTR_NAME_STYLINGUPDATE = "s7_STYLING_UPDATE";
	public static final String ATTR_NAME_WEIGHTCHANGE = "s7_WEIGHT_CHANGE";
	public static final String ATTR_NAME_MATERIALCOSTCHANGE = "s7_MATERIAL_COST_CHANGE";
	public static final String ATTR_NAME_THEOTHERS = "s7_THE_OTHERS";
	public static final String ATTR_NAME_DEPLOYMENTTARGET = "s7_DEPLOYMENT_TARGET";

	public static final String ATTR_NAME_GATENO = "s7_GATE_NO";
	public static final String ATTR_NAME_AFFECTEDSYSCODE = "s7_AFFECTED_SYSTEM_CODE";

	// BOM line Property
	public static final String ATTR_NAME_BL_SEQUENCE_NO = "bl_sequence_no";
	public static final String ATTR_NAME_BL_SUPPLY_MODE = "S7_SUPPLY_MODE";
	public static final String ATTR_NAME_BL_MODULE_CODE = "S7_MODULE_CODE";
	public static final String ATTR_NAME_BL_ALTER_PART = "S7_PRE_ALTER_PART";
	public static final String ATTR_NAME_BL_CHG_CD = "S7_CHG_CD"; // C, D
	public static final String ATTR_NAME_BL_REQ_OPT = "S7_REQ_OPT";
	public static final String ATTR_NAME_BL_LEV_M = "S7_LEV_M";
	public static final String ATTR_NAME_BL_SPEC_DESC = "S7_SPECIFICATION";
	public static final String ATTR_NAME_BL_QUANTITY = "bl_quantity";
	public static final String ATTR_NAME_BL_SYSTEM_ROW_KEY = "S7_SYSTEM_ROW_KEY";
	public static final String ATTR_NAME_BL_OCC_THREAD = "bl_occurrence_uid";
	public static final String ATTR_NAME_BL_ITEM_ID = "bl_item_item_id";
	public static final String ATTR_NAME_BL_ITEM_REVISION_ID = "bl_rev_item_revision_id";
	public static final String ATTR_NAME_BL_REV_ITEM_NAME = "bl_rev_object_name";
	public static final String ATTR_NAME_BL_REV_RELEASELIST = "bl_rev_release_status_list";
	public static final String ATTR_NAME_BL_ITEM_REVISION = "bl_rev_item_revision";
	public static final String ATTR_NAME_BL_VARIANTCONDITION = "bl_occ_mvl_condition";
	public static final String ATTR_NAME_BL_OCC_ORDER_NO = "bl_occ_int_order_no";
	public static final String ATTR_NAME_BL_OBJECT_TYPE = "bl_item_object_type";
	public static final String ATTR_NAME_BL_REV_OBJECT_TYPE = "bl_rev_object_name";
	public static final String ATTR_NAME_BL_VARIANT_CONDITION = "bl_variant_condition";
	public static final String ATTR_NAME_BL_OCC_FND_OBJECT_ID = "bl_occ_fnd0objectId";
	public static final String ATTR_NAME_BL_ABS_OCC_ID = "bl_abs_occ_id";
	public static final String ATTR_NAME_BL_CONDITION = "bl_occ_mvl_condition";
	public static final String ATTR_NAME_BL_STRC_REVISION = "structure_revisions";
	// 20200914 seho EJS Column 추가. BOMLine 속성
	public static final String ATTR_NAME_BL_EJS = "S7_EJS";
	
	//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가. BOMLine 속성 by 전성용(20201223)	
	public static final String ATTR_NAME_BL_WEIGHT_MANAGEMENT = "S7_Weight_Management";
	
	/**
	 * [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동
	 */
	public static final String ATTR_NAME_BL_DVP_NEEDED_QTY = "S7_DVP_NEEDED_QTY"; // DVP SAMPLE 필요수량
	public static final String ATTR_NAME_BL_DVP_USE = "S7_DVP_USE"; // DVP SAMPLE 용도
	public static final String ATTR_NAME_BL_DVP_REQ_DEPT = "S7_DVP_REQ_DEPT"; // DVP SAMPLE 요청부서

	/**
	 * [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동
	 */
	public static final String ATTR_NAME_BL_ENG_DEPT_NM = "S7_ENG_DEPT_NM"; // 설계담당 팀
	public static final String ATTR_NAME_BL_ENG_RESPONSIBLITY = "S7_ENG_RESPONSIBLITY"; // 설계 담당 담당
	
	public static final String CONST_CCN_CHG_TYPE_ADD = "A";
	public static final String CONST_CCN_CHG_TYPE_CUT = "D";
	public static final String CONST_CCN_CHG_TYPE_REPLACE = "R";
	public static final String CONST_CCN_CHG_TYPE_CHANGE = "C";
	
	//[SR170703-020][LJG]Proto Tooling 컬럼 추가
	public static final String ATTR_NAME_BL_PROTO_TOOLING = "S7_PROTO_TOOLING"; // Proto Tooling
	
	// ATTRIBUTE - USER
	public static final String PROP_USER_NAME = "user_name";
	public static final String PROP_DEFAULT_GROUP = "default_group";
	public static final String PROP_DEPT_NAME = "PA6";
	public static final String PROP_TEL = "PA10";
    public static final String PROP_USER_ID = "user_id";

    public static final String ATTR_NAME_BL_BUDGETCODE = "S7_BUDGET_CODE"; //[20180213][LJG] BOMLine으로 이동
}
