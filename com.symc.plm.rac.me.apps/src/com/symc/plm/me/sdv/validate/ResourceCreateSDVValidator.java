/**
 * 
 */
package com.symc.plm.me.sdv.validate;

import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.resource.ResourceIDConstants;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ResourceCreateSDVValidator
 * Class Description :
 * 
 * @date 2013. 12. 18.
 * 
 */
public class ResourceCreateSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(ResourceCreateSDVValidator.class);

    /**
     * Description :
     * 
     * @method :
     * @date : 2013. 12. 18.
     * @param :
     * @return :
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        IDataMap datamap = (IDataMap) parameter.get("datamap");

        Map<String, String> itemProperties = (Map<String, String>) datamap.getValue("itemProperties");
        Map<String, String> revisionProperties = (Map<String, String>) datamap.getValue("revisionProperties");
        String itemId = itemProperties.get(SDVPropertyConstant.ITEM_ITEM_ID);
        String itemTCCompType = datamap.getStringValue("itemTCCompType");

        try {
            // Item ID Validation
            if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM)) {
                checkEquipItemId(itemId, itemTCCompType);
            } else if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
                checkToolItemId(itemId);
            } else if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {
                checkSubsidiaryItemId(itemId);
            }

            // �ʼ� �Ӽ� Validation
            checkMandatoryProperties(itemTCCompType, itemProperties, revisionProperties);

        } catch (Exception e) {
            throw new SDVException(e.getMessage(), e);
        }
    }

    /**
     * ���� Item ID üũ
     * 
     * @method checkItemId
     * @date 2013. 12. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void checkEquipItemId(String itemId, String itemTCCompType) throws Exception {
        if (StringUtils.isEmpty(itemId)) {
            throw new ValidateSDVException("Resource Item ID is empty");
        }

        String[] splitItemIds = itemId.split("-");

        // �ڸ��� üũ (4 : ���� �Ϲݼ���, ���� JIG����, ��ü �κ�, ��ü ��
        if (splitItemIds.length == 4) {
            // B : BODY
            if (splitItemIds[0].equals(registry.getString("BOP.Type.Body"))) {
                if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM)) {
                    ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.BODY_EQUIP_ROBOT);
                }
                if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM)) {
                    ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.BODY_EQUIP_GUN);
                }
                // P : PAINT
            } else if (splitItemIds[0].equals(registry.getString("BOP.Type.Paint"))) {
                // ��� ����
                // else : ASSY
            } else {
                if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM)) {
                    ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.ASSY_EQUIP_GENERAL);
                }

                if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
                    ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.ASSY_EQUIP_JIG);
                }
            }
            // �ڸ��� üũ (5 : ��ü �Ϲݼ���,��ü�κ��δ뼳��,��üJIG����, ���� �Ϲݼ���)
        } else if (splitItemIds.length == 5) {
            if (splitItemIds[0].equals(registry.getString("BOP.Type.Body"))) {
                ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.BODY_EQUIP_GENERAL);
                // P : PAINT
            } else if (splitItemIds[0].equals(registry.getString("BOP.Type.Paint"))) {
                ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.PAINT_EQUIP_GENERAL);
                // else : ASSY
            } else {
                // ������
            }
        } else {
            throw ResourceUtilities.checkIdErrorMessage(itemId);
        }
    }

    /**
     * ���� Item ID üũ
     * 
     * @method checkItemId
     * @date 2013. 12. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void checkToolItemId(String itemId) throws Exception {
        if (StringUtils.isEmpty(itemId)) {
            throw new ValidateSDVException("Resource Item ID is empty");
        }

        String[] splitItemIds = itemId.split("-");

        // �ڸ��� üũ (3 : ��ü �Ϲݰ���, ���� �Ϲݰ���)
        if (splitItemIds.length == 3) {
            // B : BODY
            if (splitItemIds[0].equals(registry.getString("BOP.Type.Body"))) {
                ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.BODY_TOOL_GENERAL);
                // P : PAINT
            } else if (splitItemIds[0].equals(registry.getString("BOP.Type.Paint"))) {
                ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.PAINT_TOOL_GENERAL);
            }
            // �ڸ��� üũ (4 : ���� �Ϲݰ���, ���� STAY����)
        } else if (splitItemIds.length == 4) {
            // B : BODY
            if (splitItemIds[0].equals(registry.getString("BOP.Type.Body"))) {
                // ��� ����
                // P : PAINT
            } else if (splitItemIds[0].equals(registry.getString("BOP.Type.Paint"))) {
                ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.PAINT_TOOL_STAY);
                // else : ASSY
            } else {
                ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.ASSY_TOOL_GENERAL);
            }
            // �ڸ��� üũ (6 : ���� ���ϰ���)
        } else if (splitItemIds.length == 6) {
            ResourceUtilities.checkResourceId(itemId, ResourceIDConstants.ASSY_TOOL_SOCKET);
        } else {
            throw ResourceUtilities.checkIdErrorMessage(itemId);
        }
    }

    /**
     * ������ Item ID üũ
     * 
     * @method checkItemId
     * @date 2013. 12. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void checkSubsidiaryItemId(String itemId) throws Exception {
        if (StringUtils.isEmpty(itemId)) {
            throw new ValidateSDVException("Resource Item ID is empty");
        }
        if (SYMTcUtil.getLatestedRevItem(itemId) != null) {
            throw new ValidateSDVException("Resource Item is exist");
        }
    }

    /**
     * �ʼ��Ӽ� �� ���翩�� üũ
     * 
     * @throws Exception
     */
    private void checkMandatoryProperties(String itemTCCompType, Map<String, String> itemProperties, Map<String, String> revisionProperties) throws Exception {
        StringBuffer errorMsg = new StringBuffer();
        String itemId = itemProperties.get(SDVPropertyConstant.ITEM_ITEM_ID);
        String resourceCategory = revisionProperties.get(SDVPropertyConstant.TOOL_RESOURCE_CATEGORY);
        String bopType = ResourceUtilities.getBOPType(itemId);

        /* ��ü ���� */
        checkProperty(errorMsg, itemProperties, SDVPropertyConstant.ITEM_OBJECT_NAME, registry.getString("KorName.Name"));
        checkProperty(errorMsg, itemProperties, SDVPropertyConstant.EQUIP_ENG_NAME, registry.getString("EngName.NAME"));

        /* ������ */
        if (bopType.equals("Subsidiary")) {
            if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM)) {
                checkSubsidiaryProperty(revisionProperties, errorMsg);
            }
        }

        /* ��ü */
        else if (bopType.equals("Body")) {
            checkBodyProperty(itemTCCompType, revisionProperties, resourceCategory, errorMsg);
        }

        /* ���� */
        else if (bopType.equals("Paint")) {
            checkPaintProperty(itemTCCompType, revisionProperties, resourceCategory, errorMsg);
        }

        /* ���� */
        else if (bopType.equals("Assy")) {
            checkAssyProperty(itemTCCompType, revisionProperties, resourceCategory, errorMsg);
        }

        if (errorMsg.length() > 0) {
            throw new SDVException(errorMsg.toString());
        }
    }

    /**
     * ������ �Ӽ� Validate
     * 
     * @param revisionProperties
     * @param errorMsg
     * @throws Exception
     */
    private void checkSubsidiaryProperty(Map<String, String> revisionProperties, StringBuffer errorMsg) throws Exception {
        checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.SUBSIDIARY_MATERIAL_TYPE, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_MATERIAL_TYPE));
        checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SUBSIDIARY_GROUP));
        checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.SUBSIDIARY_SPEC_KOR, SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV, SDVPropertyConstant.SUBSIDIARY_SPEC_KOR));
    }

    /**
     * ��ü �Ӽ� Validate
     * 
     * @param itemTCCompType
     * @param revisionProperties
     * @param errorMsg
     * @param resourceCategory
     */
    private void checkBodyProperty(String itemTCCompType, Map<String, String> revisionProperties, String resourceCategory, StringBuffer errorMsg) {
        // ��ü ����
        if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
            // ��ü ���� ����
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY, registry.getString("ResourceCategory.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SHOP_CODE, registry.getString("ShopCode.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_MAIN_CLASS, registry.getString("MainClass.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SUB_CLASS, registry.getString("SubClass.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SPEC_KOR, registry.getString("SpecKor.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SPEC_ENG, registry.getString("SpecEng.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_PURPOSE_KOR, registry.getString("PurposeKor.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_PURPOSE_ENG, registry.getString("PurposeEng.NAME"));

            // ��ü �Ϲ� ����, ��ü �κ� �δ� ����, ��ü JIG
            if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_VEHICLE_CODE, registry.getString("VehicleCode.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_STATION_CODE, registry.getString("StationCode.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_POSITION_CODE, registry.getString("PositionCode.NAME"));

            }

            // ��ü �κ� ��ü
            else if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM)) {
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_AXIS, registry.getString("Axis.NAME"));
                // checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SERVO, registry.getString("Servo.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_ROBOT_TYPE, registry.getString("RobotType.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_MAKER_NO, registry.getString("MakerNo.NAME"));
            }

            // ��ü GUN
            else if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM)) {
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_MAKER_NO, registry.getString("MakerNo.NAME"));
            }
        }

        // ��ü ����
        else if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_RESOURCE_CATEGORY, registry.getString("ResourceCategory.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_MAIN_CLASS, registry.getString("MainClass.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SUB_CLASS, registry.getString("SubClass.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SPEC_CODE, registry.getString("VehicleCode.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SPEC_KOR, registry.getString("SpecKor.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SPEC_ENG, registry.getString("SpecEng.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_PURPOSE, registry.getString("PurposeKor.NAME"));
        }
    }

    /**
     * ���� �Ӽ� Validate
     * 
     * @param itemTCCompType
     * @param revisionProperties
     * @param resourceCategory
     * @param errorMsg
     */
    private void checkPaintProperty(String itemTCCompType, Map<String, String> revisionProperties, String resourceCategory, StringBuffer errorMsg) {
        // ���� ����
        if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM)) {
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY, registry.getString("ResourceCategory.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SHOP_CODE, registry.getString("ShopCode.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_LINE_CODE, registry.getString("LineCode.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_STATION_CODE, registry.getString("StationCode.NAME"));

            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SPEC_KOR, registry.getString("SpecKor.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SPEC_ENG, registry.getString("SpecEng.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_PURPOSE_KOR, registry.getString("PurposeKor.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_PURPOSE_ENG, registry.getString("PurposeEng.NAME"));
        }

        // ���� ����
        else if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
            // ���� ���� ����
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_RESOURCE_CATEGORY, registry.getString("ResourceCategory.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_MAIN_CLASS, registry.getString("MainClass.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SUB_CLASS, registry.getString("SubClass.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SPEC_KOR, registry.getString("SpecKor.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SPEC_ENG, registry.getString("SpecEng.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_PURPOSE, registry.getString("PurposeKor.NAME"));

            // �Ϲ� ����
            if (resourceCategory.equals(registry.getString("Resource.Category.EXT"))) {
                // �Ϲ� ���� ���� �Ӽ� ����
            }

            // ���� ����
            else if (resourceCategory.equals(registry.getString("Resource.Category.STY"))) {
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_VEHICLE_CODE, registry.getString("VehicleCode.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SPEC_CODE, registry.getString("VehicleCode.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_STAY_TYPE, registry.getString("VehicleCode.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_STAY_AREA, registry.getString("VehicleCode.NAME"));
            }
        }
    }

    /**
     * ���� �Ӽ� Validate
     * 
     * @param itemTCCompType
     * @param revisionProperties
     * @param resourceCategory
     * @param errorMsg
     */
    private void checkAssyProperty(String itemTCCompType, Map<String, String> revisionProperties, String resourceCategory, StringBuffer errorMsg) {
        // ����, ���� ����
        checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_RESOURCE_CATEGORY, registry.getString("ResourceCategory.NAME"));
        checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_MAIN_CLASS, registry.getString("MainClass.NAME"));
        checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SPEC_KOR, registry.getString("SpecKor.NAME"));
        checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SPEC_ENG, registry.getString("SpecEng.NAME"));
        checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_PURPOSE_KOR, registry.getString("PurposeKor.NAME"));

        // ���� �Ϲݼ���, JIG����
        if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SHOP_CODE, registry.getString("ShopCode.NAME"));
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_PURPOSE_ENG, registry.getString("PurposeEng.NAME"));
        }

        // ���� �Ϲݼ���, ���� �Ϲݰ���, ���� ���ϰ���
        if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_GENERALEQUIP_ITEM) || itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_SUB_CLASS, registry.getString("SubClass.NAME"));
        }

        // ���� JIG����
        if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM)) {
            checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.EQUIP_VEHICLE_CODE, registry.getString("VehicleCode.NAME"));
        }

        // ���� ����
        if (itemTCCompType.equals(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM)) {
            // ���� �Ϲݰ���
            if (resourceCategory.equals(registry.getString("Resource.Category.EXT"))) {
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_SPEC_CODE, registry.getString("SpecCode.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_MAKER_AF_CODE, registry.getString("MakerAfCode.NAME"));
            }

            // ���� ���ϰ���
            else if (resourceCategory.equals(registry.getString("Resource.Category.SOC"))) {
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_TOOL_SHAPE, registry.getString("ToolShape.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_TOOL_LENGTH, registry.getString("ToolLength.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_TOOL_SIZE, registry.getString("ToolSize.NAME"));
                checkProperty(errorMsg, revisionProperties, SDVPropertyConstant.TOOL_TOOL_MAGNET, registry.getString("ToolMagnet.NAME"));
            }
        }
    }

    /**
     * checkProperty : Property�� �ʼ����� �����ϴ��� �˻�
     * 
     * @param propMap
     * @param errorMsg
     */
    private void checkProperty(StringBuffer errorMsg, Map<String, String> propMap, String propertyName, String propertyDisplayName) {
        if (propMap.containsKey(propertyName)) {
            String propValue = propMap.get(propertyName);
            if (propValue.length() == 0 || propValue == null) {
                errorMsg.append(registry.getString("RequiredCheck.MSG").replace("%0", propertyDisplayName).concat("\n"));
            }
        }
    }

}
