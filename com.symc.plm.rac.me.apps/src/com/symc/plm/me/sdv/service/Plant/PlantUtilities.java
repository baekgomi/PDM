package com.symc.plm.me.sdv.service.Plant;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrWorkarea;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class PlantUtilities {

    /**
     * Plant�� ItemRevision�� �������� �Լ�
     * 
     * @param interfaceAIFComponent
     * @param itemRevision
     * @return
     * @throws TCException
     */
    public static TCComponentItemRevision getItemRevision(InterfaceAIFComponent interfaceAIFComponent) throws TCException {
        TCComponentItemRevision itemRevision = null;
        if (interfaceAIFComponent instanceof TCComponentMfgBvrWorkarea) {
            TCComponentMfgBvrWorkarea MfgBvrWorkareaComponent = (TCComponentMfgBvrWorkarea) interfaceAIFComponent;
            itemRevision = MfgBvrWorkareaComponent.getItemRevision();
        }
        return itemRevision;
    }

    /**
     * Alternative BOMLine ���� �Լ�
     * 
     * @param altPrefix
     * @param targetBOMLine
     * @throws Exception
     */
    public static void createAlternativeBOMLine(TCComponentBOMLine targetBOMLine, TCComponentBOMLine parentBOMLine, String altPrefix) throws Exception {
        if (targetBOMLine != null && parentBOMLine != null) {
            /* Item ID ���� */
            String originItemID = targetBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            if (targetBOMLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP)) {
                originItemID = originItemID.substring(originItemID.indexOf("-") + 1);
            }
            String newItemID = altPrefix + "-" + originItemID;

            /* ALT Plant�� �ڱ��ڽ��� ���� �� ��� ���� ó�� */
            if (newItemID.equals(targetBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                throw new Exception(newItemID + " is current selected Alternative BOMLine.");
            }

            /* ���� ��� altStationBOMLine ã�� */
            TCComponentBOMLine altStationBOMLine = findBOMLine(parentBOMLine, newItemID);

            /* ���� ��� altStationBOMLine�� ���� ��� ���� */
            if (altStationBOMLine == null) {
                TCComponentItem altStationItem = createPlantItem(newItemID, targetBOMLine.getItem().getType(), altPrefix);

                if (altStationItem != null) {
                    // BOMLine�� �߰�
                    altStationBOMLine = parentBOMLine.add(null, altStationItem.getLatestItemRevision(), null, false);
                    altStationBOMLine.setProperty("bl_plmxml_occ_xform", targetBOMLine.getProperty("bl_plmxml_occ_xform"));
                } else {
                    throw new Exception(newItemID + " does not exist.");
                }
            }

            /* �ٿ����� ��� BOMLine ���� BOMLine ���� */
            deleteChildBOMLine(altStationBOMLine);

            /* targetBOMLine ������ BOMLine���� altStationBOMLine ������ �ٿ��ֱ� */
            addBOMLine(targetBOMLine, altStationBOMLine, altPrefix);
        }
    }

    /**
     * Plant Item ���� �Լ�
     * 
     * @throws TCException
     */
    public static TCComponentItem createPlantItem(String itemId, String itemType, String altPrefixProperty) throws TCException {
        // Plant Item ����
        TCComponentItem tcComponentItem = SDVBOPUtilities.createItem(itemType, itemId, "000", "", "");
        TCComponentItemRevision newItemRevision = tcComponentItem.getLatestItemRevision();

        // Plant ItemRevision �Ӽ� �Է�
        newItemRevision.setProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX, altPrefixProperty);
        newItemRevision.setLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP, (altPrefixProperty.length() > 0) ? true : false);

        return tcComponentItem;
    }

    /**
     * �ڿ� �Ҵ� �Լ�
     * 
     * @param targetBOMLine
     * @param parentBOMLine
     * @throws TCException
     * @throws Exception
     */
    public static TCComponent assignResource(TCComponentBOMLine targetBOMLine, TCComponentBOMLine parentBOMLine) throws TCException, Exception {
        ArrayList<InterfaceAIFComponent> resourceComponentList = new ArrayList<InterfaceAIFComponent>();
        resourceComponentList.add(targetBOMLine.getItemRevision());

        boolean isEquip = targetBOMLine.getItemRevision().isTypeOf("M7_EquipmentRevision");
        boolean isTool = targetBOMLine.getItemRevision().isTypeOf(SDVTypeConstant.BOP_PROCESS_TOOL_ITEM_REV);
        boolean isSubidiary = targetBOMLine.getItemRevision().isTypeOf(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM_REV);

        String occurrenceType = null;
        if (isEquip) {
            occurrenceType = SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE;
        } else if (isTool) {
            occurrenceType = SDVTypeConstant.BOP_PROCESS_OCCURRENCE_TOOL;
        } else if (isSubidiary) {
            occurrenceType = SDVTypeConstant.BOP_PROCESS_OCCURRENCE_SUBSIDIARY;
        }

        TCComponent[] tcComponents = SDVBOPUtilities.connectObject(parentBOMLine, resourceComponentList, occurrenceType);
        return tcComponents[0];
    }

    /**
     * Alternative���� Production���� BOMLine ���� (Station Only)
     * 
     * @param parentBOMLine
     * @param targetBOMLine
     * @throws TCException
     * @throws Exception
     */
    public static void applyProductionBOMLine(TCComponentBOMLine targetBOMLine, TCComponentBOMLine parentBOMLine) throws TCException, Exception {

        /* Item ID ���� */
        String alternativeItemID = targetBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String productionItemID = alternativeItemID.substring(alternativeItemID.indexOf("-") + 1);

        /* ���� ��� productionBOMLine ã�� */
        TCComponentBOMLine productionBOMLine = findBOMLine(parentBOMLine, productionItemID);

        /* ���� ��� productionBOMLine�� ���� ��� ���� */
        if (productionBOMLine == null) {
            TCComponentItem productionItem = SDVBOPUtilities.createItem(targetBOMLine.getItem().getType(), productionItemID, "000", "", "");
            if (productionItem != null) {
                productionBOMLine = parentBOMLine.add(null, productionItem.getLatestItemRevision(), null, false);
                productionBOMLine.setProperty("bl_plmxml_occ_xform", targetBOMLine.getProperty("bl_plmxml_occ_xform"));
            } else {
                throw new Exception(productionItemID + " does not exist.");
            }
        }

        /* productionBOMLine ���� BOMLine ���� */
        deleteChildBOMLine(productionBOMLine);

        /* Alternative ������ BOMLine���� productionBOMLine ������ �ٿ��ֱ� */
        addBOMLine(targetBOMLine, productionBOMLine, "");
    }

    /**
     * ItemID�� BOMLine ã��
     * 
     * @param parentBOMLine
     * @param targetItemID
     * @return
     * @throws TCException
     */
    public static TCComponentBOMLine findBOMLine(TCComponentBOMLine parentBOMLine, String targetItemID) throws TCException {
        /* Item ID�� Apply ��� productionBOMLine ã�� */
        TCComponentBOMLine targetBOMLine = null;
        AIFComponentContext[] arrAIFComponentContexts = parentBOMLine.getChildren();
        if (arrAIFComponentContexts != null) {
            for (AIFComponentContext aifComponentContext : arrAIFComponentContexts) {
                TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();
                if (targetItemID.equals(tcComponentBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                    targetBOMLine = tcComponentBOMLine;
                    break;
                }
            }
        }
        return targetBOMLine;
    }

    /**
     * Alternative ������ BOMLine���� targetBOMLine ������ �ٿ��ִ� �Լ�
     * 
     * @param alternativeBOMLine
     * @param productionBOMLine
     * @param altPrefix
     * @throws TCException
     * @throws Exception
     */
    public static void addBOMLine(TCComponentBOMLine alternativeBOMLine, TCComponentBOMLine productionBOMLine, String altPrefix) throws TCException, Exception {
        AIFComponentContext[] arrAifComponentContexts = alternativeBOMLine.getChildren();
        if (arrAifComponentContexts != null) {
            for (AIFComponentContext aifComponentContext : arrAifComponentContexts) {
                TCComponentBOMLine altChildBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();
                String altChildItemId = altChildBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                String altChildItemType = altChildBOMLine.getItem().getType();

                // Workarea �� ��� productionBOMLine Add
                if (altChildItemType.equals(SDVTypeConstant.PLANT_OPAREA_ITEM)) {
                    TCComponentItem productionChildItem = null;
                    String productionChildItemId = null;

                    // Alternative�� add�� ��� : ALT Prefix�� ������ ItemID ����
                    if (altPrefix.length() > 0) {
                        if (StringUtils.contains(altChildItemId, "ALT")) {
                            productionChildItemId = altPrefix + "-" + altChildItemId.substring(altChildItemId.indexOf("-") + 1);
                        } else {
                            productionChildItemId = altPrefix + "-" + altChildItemId;
                        }
                    }
                    // Production�� add�� ��� : ALT Prefix�� ������ ItemID ����
                    else {
                        if (StringUtils.contains(altChildItemId, "ALT")) {
                            productionChildItemId = altChildItemId.substring(altChildItemId.indexOf("-") + 1); // ��) "ALT1-"
                        } else {
                            productionChildItemId = altChildItemId;
                        }

                    }

                    // Workarea Item�� �����ϸ� ã�Ƽ� ���, ������ ����
                    productionChildItem = SDVBOPUtilities.FindItem(productionChildItemId, altChildItemType);
                    if (productionChildItem == null) {
                        productionChildItem = createPlantItem(productionChildItemId, altChildItemType, altPrefix);
                    }
                 
                    // productionBOMLine Add
                    TCComponentBOMLine newBOMLine = null;
                    AIFComponentContext[] arrAIFComponentContexts = productionBOMLine.getChildren();
                    if (arrAIFComponentContexts != null) {
                        for (AIFComponentContext aifComponentContext2 : arrAIFComponentContexts) {
                            TCComponentBOMLine productionChildBOMLine = (TCComponentBOMLine) aifComponentContext2.getComponent();
                            if (productionChildItemId.equals(productionChildBOMLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
                                newBOMLine = productionChildBOMLine;
                                break;
                            }
                        }
                    }

                    if (newBOMLine == null) {
                        newBOMLine = productionBOMLine.add(null, productionChildItem.getLatestItemRevision(), null, false);
                    }

                    // BOMLine �Ӽ� ����
                    if (newBOMLine != null) {
                        newBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, altChildBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
                        newBOMLine.setProperty("bl_plmxml_occ_xform", altChildBOMLine.getProperty("bl_plmxml_occ_xform"));
                    }

                    // BOMLine ���� ���ȣ��
                    addBOMLine(altChildBOMLine, newBOMLine, altPrefix);
                }

                // �ڿ��� ��� productionBOMLine�� �Ҵ�
                else {
                    TCComponentBOMLine resourceBOMLine = (TCComponentBOMLine) assignResource(altChildBOMLine, productionBOMLine);
                    resourceBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, altChildBOMLine.getProperty(SDVPropertyConstant.BL_SEQUENCE_NO));
                    resourceBOMLine.setProperty("bl_plmxml_occ_xform", altChildBOMLine.getProperty("bl_plmxml_occ_xform"));
                }
            }
        }
    }

    /**
     * ���� BOMLine�� ����
     * 
     * @param productionBOMLine
     * @throws TCException
     */
    public static void deleteChildBOMLine(TCComponentBOMLine targetBOMLine) throws TCException {
        if (targetBOMLine != null) {
            AIFComponentContext[] arrAIFComponentContexts2 = targetBOMLine.getChildren();
            if (arrAIFComponentContexts2 != null) {
                ArrayList<TCComponentBOMLine> childBOMLineList = new ArrayList<TCComponentBOMLine>();
                for (AIFComponentContext aifComponentContext : arrAIFComponentContexts2) {
                    TCComponentBOMLine childBOMLine = (TCComponentBOMLine) aifComponentContext.getComponent();

                    if (childBOMLine != null) {
                        // ��� ȣ���Ͽ� Child�� ���� BOMLine�� ã�Ƽ� ����
                        deleteChildBOMLine(childBOMLine);
                        childBOMLineList.add(childBOMLine);
                    }
                }

                if (childBOMLineList.size() > 0) {
                    SDVBOPUtilities.disconnectObjects(targetBOMLine, childBOMLineList);
                }
            }
        }
    }
}
