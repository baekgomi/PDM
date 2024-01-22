package com.symc.plm.me.sdv.operation.plant;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.ValidationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.view.plant.CreatePlantView;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

public class CreatePlantOkOperation extends AbstractSDVActionOperation {

    private boolean isValidOk = true;
    private Registry registry;
    private String itemId;
    private String itemType;
    private String altPrefix = "";

    public CreatePlantOkOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public CreatePlantOkOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public CreatePlantOkOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void startOperation(String commandId) {
        IDataSet dataset = getDataSet();
        if (dataset.containsMap("CreatePlantView")) {
            if (dataset.getDataMap("CreatePlantView") != null) {
                RawDataMap rawDataMap = (RawDataMap) dataset.getDataMap("CreatePlantView");
                LinkedHashMap<String, String> idStrValueMap = (LinkedHashMap<String, String>) rawDataMap.getValue("id");
                LinkedHashMap<String, String> propertyStrValueMap = (LinkedHashMap<String, String>) rawDataMap.getValue("property");

                if (idStrValueMap != null && propertyStrValueMap != null) {
                    Set<String> keys = idStrValueMap.keySet();
                    itemType = getItemRevisionType(keys);
                    itemId = checkItemId(idStrValueMap);

                    // Alt Plant�� ��� itemId �տ� altPrefix �߰�
                    if (!propertyStrValueMap.isEmpty()) {
                        // Is Alt Plant �Ӽ��� ������� �ʼ����� altPrefix üũ (Station ������ ID validation)
                        String isAltPlant = propertyStrValueMap.get(registry.getString("Plant.IsAltPlant.Name", "Is Alt Plant"));
                        altPrefix = propertyStrValueMap.get(registry.getString("Plant.AltPrefix.NAME", "Alt Prefix"));
                        if (isAltPlant != null && isAltPlant.equals("true")) {
                            if (altPrefix == null || altPrefix.length() <= 0) {
                                isValidOk = false;
                            }
                        }

                        // altPrefix�� ���� ��� ItemID ���� �ϱ� (Station, Workarea ������)
                        if (altPrefix != null && altPrefix.length() > 0) {
                            if (altPrefix.length() < 4) {
                                isValidOk = false;
                            } else if (!StringUtils.contains(altPrefix, "ALT")) {
                                isValidOk = false;
                            }

                            itemId = altPrefix + "-" + itemId;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            if (!isValidOk) {
                throw new ValidationException(registry.getString("ItemID.Check.MSG"));
            }

            if (!itemId.equals("") && itemId != null && itemType != null && !itemType.equals("")) {
                createPlant(itemId, itemType, altPrefix);
            }

        } catch (Exception e) {
            this.setExecuteResult(FAIL);
            this.setExecuteError(e);
            this.setErrorMessage("������ �߻��߽��ϴ�.");
        }
    }

    @Override
    public void endOperation() {
        // Apply ��ư Ŭ���� ȭ���� ���ð� �ʱ�ȭ
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                CreatePlantView currentView = (CreatePlantView) UIManager.getCurrentDialog().getView("CreatePlantView");
                if (currentView.getShopCodeCombo() != null) {
                    if (currentView.getShopCodeCombo().getEnabled()) {
                        currentView.setShopCodeComboValue(-1);
                    }
                }

                if (currentView.getLineCodeCombo() != null) {
                    if (currentView.getLineCodeCombo().getEnabled()) {
                        currentView.setLineCodeComboValue(-1);
                    }
                }

                if (currentView.getStationCodeText() != null) {
                    if (currentView.getStationCodeText().getEditable()) {
                        currentView.setStationCodeTextValue("");
                    }
                }

                if (currentView.getWorkareaCodeText() != null) {
                    if (currentView.getWorkareaCodeText().getEditable()) {
                        currentView.setWorkareaCodeTextValue("");
                    }
                }
            }
        });
    }

    /**
     * ID Validation �Լ�
     * 
     * @param idMap
     */
    public String checkItemId(LinkedHashMap<String, String> idMap) {
        String itemId = "";
        int count = 0;

        for (String key : idMap.keySet()) {
            String idCode = idMap.get(key);
            if (idCode == null || idCode.equals("")) {
                isValidOk = false;
                return null;
            } else {
                itemId += idCode;

                if (count < (idMap.size() - 1)) {
                    itemId += "-";
                }
                count++;
            }
        }
        return itemId;
    }

    /**
     * ������ ItemRevision�� Type�� ã�� �����ϴ� �Լ�
     * 
     * @param idCodeKeys
     * @return String
     */
    public String getItemRevisionType(Set<String> idCodeKeys) {
        String itemRevType = null;
        if (idCodeKeys.contains(registry.getString("Plant.Workarea.NAME"))) {
            itemRevType = SDVTypeConstant.PLANT_OPAREA_ITEM;
        } else if (idCodeKeys.contains(registry.getString("Plant.StationCode.NAME"))) {
            itemRevType = SDVTypeConstant.PLANT_STATION_ITEM;
        } else if (idCodeKeys.contains(registry.getString("Plant.LineCode.NAME"))) {
            itemRevType = SDVTypeConstant.PLANT_LINE_ITEM;
        } else if (idCodeKeys.contains(registry.getString("Plant.ShopCode.NAME"))) {
            itemRevType = SDVTypeConstant.PLANT_SHOP_ITEM;
        }

        if (itemRevType == null) {
            isValidOk = false;
        }

        return itemRevType;
    }

    /**
     * Plant ���� �Լ�
     * 
     * @param itemId
     * @param altPrefixProperty
     * @throws Exception
     */
    private void createPlant(String itemId, String itemType, String altPrefixProperty) throws Exception {
        // Shop�� ��� Name�� "����"�߰� (��û���׿� ����)
        String itemName = null;
        if (itemType.equals(SDVTypeConstant.PLANT_SHOP_ITEM)) {
            itemName = itemId + " ����";
        }

        //����� Item�� �ߺ����� �ٴ°�찡 ������ ������ �����ؼ� ���̵��ϸ� �Ǿ�����. (�̷���� ������ ������ Item�� �����Ϸ��� �� ��� ���� �߻�)
        //�̸� �ذ��ϱ� ���ؼ� Item�� ã�Ƽ� ������ ���� ��� �̹� �پ��ִ°��� �� ���̴� ������ ��.
        //�׷��Ƿ� ��� �ڿ� ������ Item�� ã�Ƽ� �ٿ��� �ʿ䰡 ���� ��� �Ʒ� �ҽ��� �޽��� �ڽ� �߰��ؼ� ����ڰ� �Ǵ��ϵ��� �ݿ��ϸ� ��.
        // // Plant Item ���翩�� �˻�
        // TCComponentItem tcComponentItem = null;
        // TCComponentItem findedItem = SDVBOPUtilities.FindItem(itemId, itemType);
        // if (findedItem != null) {
        // tcComponentItem = findedItem;
        // }
        //
        // // Plant Item ����
        // if (tcComponentItem == null) {
        // tcComponentItem = SDVBOPUtilities.createItem(itemType, itemId, "000", (itemName != null) ? itemName : "", "");
        // }
        
        // Plant Item ����
        TCComponentItem tcComponentItem = SDVBOPUtilities.createItem(itemType, itemId, "000", (itemName != null) ? itemName : "", "");
        TCComponentItemRevision newItemRevision = tcComponentItem.getLatestItemRevision();

        // Plant ItemRevision �Ӽ� �Է�
        newItemRevision.setProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX, altPrefixProperty);
        newItemRevision.setLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP, (altPrefixProperty.length() > 0) ? true : false);

        // Shop ������ Application View Open
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        if (itemType.equals(SDVTypeConstant.PLANT_SHOP_ITEM)) {
            mfgApp.open(newItemRevision);
        }

        // Shop�� �ƴѰ��
        else {
            // (üũ) targetComponent �� : Mfg0BvrWorkarea�� �ƴϸ� ����ó��
            InterfaceAIFComponent interfaceAIFComponent = mfgApp.getTargetComponent();
            if (!interfaceAIFComponent.getType().equals("Mfg0BvrWorkarea")) {
                throw new Exception(itemType.substring(3) + " can only be added to the Workarea.");
            }

            // BOMLine�� ������ ItemRevision �߰�
            TCComponentBOMLine[] tcComponentBOMLines = mfgApp.getSelectedBOMLines();
            TCComponentBOMLine targetBOMLine = tcComponentBOMLines[0];
            targetBOMLine.add(null, newItemRevision);
            targetBOMLine.window().save();

            // �ٿ����� BOMLine ��ġ��
            SDVBOPUtilities.executeExpandOneLevel();
        }
    }
}
