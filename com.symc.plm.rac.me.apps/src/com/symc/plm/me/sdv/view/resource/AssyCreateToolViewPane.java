/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.soa.client.model.LovValue;

/**
 * Class Name : Equipment_Creation_Properties
 * Class Description :
 * 
 * [SR141016-058][20141027] shcho, ���� ���� Sub Class �� 2�� �߰�
 * 
 * @date 2013. 10. 24.
 * 
 */
public class AssyCreateToolViewPane extends CreateResourceViewPane {

    private HashMap<String, List<LovValue>> lovListMap;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public AssyCreateToolViewPane(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /**
     * Initiation ID Field (ID ǥ�� ���� ����)
     */
    @Override
    protected void initIdField(Composite topComposite) {
        super.initIdField(topComposite);
        if (resourceCategory.equals(registry.getString("Resource.Category.SOC"))) {
            drawIdField(7); // ���� ���� ���� (7ĭ)
        } else {
            drawIdField(5); // ���� �Ϲ� ���� (5ĭ)
        }
    }

    /**
     * Initiation Property Field (Property ���� ����)
     */
    @Override
    protected void initPropertyField(Composite topComposite) {
        super.initPropertyField(topComposite);
    }

    /**
     * Set Mandatory Properties
     * 
     * @param composite
     * @param bopType
     *            A : ASSY, B : BODY, P : PAINT
     * @param resourceType
     *            Value is "Equip" or "Tool"
     * @param resourceCategory
     *            EXT : �Ϲݼ��� �Ǵ� ����, JIG : Jig & Fixture, ROB : Robot, GUN : Gun, SOC : Socket���� ...
     */
    @Override
    protected void initMandatoryProperties(Composite composite, String bopType, String resourceType, String resourceCategory) {
        resource_category = drawPropCombo(composite, registry.getString("ResourceCategory.NAME"), "resource_category", getDefaultLovName(), true);
        resource_category.setEnabled(false);
        resource_category.getTextField().setBackground(getBackground());
        resource_category.setSelectedItem(resourceCategory);

        drawBlankLabel(composite, registry.getString("Label.Blank.NAME")); // ������ ���߱� ���� �� Label �߰���
        final String mainClassLovName = getChildLovName(resourceCategory);
        main_class = drawPropCombo(composite, registry.getString("MainClass.NAME"), "main_class", mainClassLovName, true);
        sub_class = drawPropCombo(composite, registry.getString("SubClass.NAME"), "sub_class", true);
        // �Ϲݰ��� ����
        if (resourceCategory.equals(registry.getString("Resource.Category.EXT"))) {
            spec_code = drawPropCombo(composite, registry.getString("SpecCode.NAME"), "spec_code", true);
            drawBlankLabel(composite, registry.getString("Label.Blank.NAME")); // ������ ���߱� ���� �� Label �߰���
            maker_af_code = drawPropCombo(composite, registry.getString("MakerAfCode.NAME"), "maker_af_code", getChildLovName("MAKER_AF", mainClassLovName), true);
            maker_torque_code = drawPropCombo(composite, registry.getString("MakerTorqueCode.NAME"), "maker_torque_code", getChildLovName("MAKER_TORQUE", mainClassLovName), true, false);

            lovListMap = new HashMap<String, List<LovValue>>();
            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    lovListMap.clear();
                    sub_class.removeAllItems();
                    spec_code.removeAllItems();
                    String seletedValue = main_class.getSelectedItem().toString();
                    try {
                        setSubClassLov(mainClassLovName, seletedValue);
                        // sub_class.setSelectedIndex(0);
                        sub_class.setText("");
                        spec_code.setText("");

                        idMap.get(1).setText(seletedValue);
                        idMap.get(2).setText("");
                        idMap.get(3).setText("");

                    } catch (TCException e) {
                        e.printStackTrace();
                    }
                }
            });

            sub_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    String seletedValue = sub_class.getSelectedItem().toString();
                    String seletedSubLovName = getSelectedSubClassLovName(seletedValue);
                    spec_code.removeAllItems();
                    final String specCodeLovName = getChildLovName("COM", seletedSubLovName);
                    spec_code = SDVLOVUtils.comboValueSetting(spec_code, specCodeLovName);
                    spec_code.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                    // spec_code.setSelectedIndex(0);
                    spec_code.setText("");

                    idMap.get(2).setText(seletedValue);
                    idMap.get(3).setText("");
                }
            });

            spec_code.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(3).setText(spec_code.getSelectedItem().toString());
                }
            });

            maker_af_code.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    Object makerTorqueValue = maker_torque_code.getSelectedItem();
                    idMap.get(4).setText(maker_af_code.getSelectedItem().toString() + ((makerTorqueValue != null) ? makerTorqueValue.toString() : ""));
                }
            });

            maker_torque_code.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    Object makerAFValue = maker_af_code.getSelectedItem();
                    idMap.get(4).setText(((makerAFValue != null) ? makerAFValue.toString() : "") + maker_torque_code.getSelectedItem().toString());
                }
            });
        }
        // �Ϲݰ��� ��

        // ���ϰ��� ����
        if (resourceCategory.equals(registry.getString("Resource.Category.SOC"))) {
            tool_shape = drawPropCombo(composite, registry.getString("ToolShape.NAME"), "tool_shape", true);
            tool_length = drawPropCombo(composite, registry.getString("ToolLength.NAME"), "tool_length", getChildLovName("H_LENGTH", mainClassLovName), true);
            tool_size = drawPropCombo(composite, registry.getString("ToolSize.NAME"), "tool_size", getChildLovName("H_SIZE", mainClassLovName), true);
            tool_magnet = drawPropCombo(composite, registry.getString("ToolMagnet.NAME"), "tool_magnet", getChildLovName("H_MAGNET", mainClassLovName), true);

            lovListMap = new HashMap<String, List<LovValue>>();
            main_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    lovListMap.clear();
                    sub_class.removeAllItems();
                    tool_shape.removeAllItems();
                    String seletedValue = main_class.getSelectedItem().toString();
                    try {
                        setSubClassLov(mainClassLovName, seletedValue);
                        // sub_class.setSelectedIndex(0);
                        sub_class.setText("");
                        tool_shape.setText("");

                        idMap.get(1).setText(seletedValue);
                        idMap.get(2).setText("");
                        idMap.get(3).setText("");

                    } catch (TCException e) {
                        e.printStackTrace();
                    }
                }
            });

            sub_class.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    String seletedValue = sub_class.getSelectedItem().toString();
                    String seletedSubLovName = getSelectedSubClassLovName(seletedValue);
                    tool_shape.removeAllItems();
                    final String specCodeLovName = getChildLovName("COM", seletedSubLovName);
                    tool_shape = SDVLOVUtils.comboValueSetting(tool_shape, specCodeLovName);
                    tool_shape.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
                    // spec_code.setSelectedIndex(0);
                    tool_shape.setText("");

                    idMap.get(2).setText(seletedValue);
                    idMap.get(3).setText("");
                }
            });

            tool_shape.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(3).setText(tool_shape.getSelectedItem().toString());
                }
            });
            tool_length.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(4).setText(tool_length.getSelectedItem().toString());
                }
            });
            tool_size.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(5).setText(tool_size.getSelectedItem().toString());
                }
            });
            tool_magnet.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    idMap.get(6).setText(tool_magnet.getSelectedItem().toString());
                }
            });
        }
        // ���ϰ��� ��
    }

    /**
     * Set Additional Properties
     * 
     * @param composite
     * @param resourceType
     *            Value is "Equip" or "Tool"
     */
    @Override
    protected void initAdditionalProperties(Composite composite, String resourceType) {
        object_name = drawPropText(composite, registry.getString("KorName.Name"), "object_name", 1, true);
        eng_name = drawPropText(composite, registry.getString("EngName.NAME"), "eng_name", 1, true);
        spec_kor = drawPropText(composite, registry.getString("SpecKor.NAME"), "spec_kor", 1, true);
        spec_eng = drawPropText(composite, registry.getString("SpecEng.NAME"), "spec_eng", 1, true);
        torque_value = drawPropText(composite, registry.getString("TorqueValue.NAME"), "torque_value", 1, false);
        purpose = drawPropText(composite, registry.getString("Purpose.NAME"), "purpose", 1, true);
        unit_usage = drawPropCombo(composite, registry.getString("UnitUsage.NAME"), "unit_usage", registry.getString("UnitUsage.LOV.NAME"), false);
        material = drawPropText(composite, registry.getString("Material.NAME"), "material", 1, false);
        maker = drawPropText(composite, registry.getString("Maker.NAME"), "maker", 1, false);
        drawBlankLabel(composite, registry.getString("Label.Blank.NAME")); // ������ ���߱� ���� �� Label �߰���
        object_desc = drawPropText(composite, registry.getString("Description.NAME"), "object_desc", 3, false);
    }

    @Override
    public void uiLoadCompleted() {
        // Reviseȭ���� ��� ���õ� Item�� �Ӽ����� ȭ�� ����
        if (!createMode) {
            // ����
            for (int key : idMap.keySet()) {
                idMap.get(key).setEnabled(false);
                idMap.get(key).setBackground(getBackground());
            }
            resource_category.setEnabled(false);
            resource_category.getTextField().setBackground(getBackground());
            main_class.setEnabled(false);
            main_class.getTextField().setBackground(getBackground());
            sub_class.setEnabled(false);
            sub_class.getTextField().setBackground(getBackground());

            // SubClass LOV ����
            String mainClassValue = paramRevisionProperties.get(SDVPropertyConstant.TOOL_MAIN_CLASS);
            try {
                setSubClassLov(getChildLovName(resourceCategory), mainClassValue);
            } catch (TCException e) {
                e.printStackTrace();
            }

            // SubClass ���� �׸��� LOV Name ã��
            String subClassValue = paramRevisionProperties.get(SDVPropertyConstant.TOOL_SUB_CLASS);
            String seletedSubLovName = getSelectedSubClassLovName(subClassValue);
            String specCodeLovName = getChildLovName("COM", seletedSubLovName);

            // ���� �Ϲݰ��� Only
            if (resourceCategory.equalsIgnoreCase(registry.getString("Resource.Category.EXT"))) {

                SDVLOVUtils.comboValueSetting(spec_code, specCodeLovName);

                spec_code.setEnabled(false);
                spec_code.getTextField().setBackground(getBackground());
                maker_af_code.setEnabled(false);
                maker_af_code.getTextField().setBackground(getBackground());
                maker_torque_code.setEnabled(false);
                maker_torque_code.getTextField().setBackground(getBackground());
                /*
                 * maker_torque_code�� �Ӽ����� ���� ����. ID�� ��ü�ڵ�(3�ڸ�)�� ������3��° �ڸ����� ������ ����.
                 * (���� �Ϸ� �� �߰� ��û���׶� �ݿ��Ǿ���. 2014.01.07)
                 */
                String makerCode = paramRevisionProperties.get(SDVPropertyConstant.ITEM_ITEM_ID).split("-")[3];
                maker_torque_code.setSelectedItem(makerCode.substring(2));
            }

            // ���� Socket���� Only
            if (resourceCategory.equals(registry.getString("Resource.Category.SOC"))) {
                SDVLOVUtils.comboValueSetting(tool_shape, specCodeLovName);
                SDVLOVUtils.comboValueSetting(tool_length, getChildLovName("H_LENGTH", getChildLovName(resourceCategory)));
                SDVLOVUtils.comboValueSetting(tool_size, getChildLovName("H_SIZE", getChildLovName(resourceCategory)));
                SDVLOVUtils.comboValueSetting(tool_magnet, getChildLovName("H_MAGNET", getChildLovName(resourceCategory)));

                tool_shape.setEnabled(false);
                tool_shape.getTextField().setBackground(getBackground());
                tool_length.setEnabled(false);
                tool_length.getTextField().setBackground(getBackground());
                tool_size.setEnabled(false);
                tool_size.getTextField().setBackground(getBackground());
                tool_magnet.setEnabled(false);
                tool_magnet.getTextField().setBackground(getBackground());
            }

            // Value ����
            String itemId = paramRevisionProperties.get(SDVPropertyConstant.ITEM_ITEM_ID);
            String revisionId = paramRevisionProperties.get(SDVPropertyConstant.ITEM_REVISION_ID);
            String strNewRevisionId = ResourceUtilities.getNewRevisionId(revisionId);

            setItemId(itemId, strNewRevisionId);
            setControlValue(itemPropMap, paramItemProperties, resourceType);
            setControlValue(revisionPropMap, paramRevisionProperties, resourceType);
        }
    }

    /**
     * SubClass�� LOV�� �����ϴ� �Լ�
     * 
     * @param mainClassLovName
     * @param mainClassSeletedValue
     * @param createMode
     * @throws TCException
     */
    protected void setSubClassLov(final String mainClassLovName, String mainClassSeletedValue) throws TCException {
        int lovNum = 6; // MainClass�� A�� ��� Sub Class�� �� LOV �� 6�� (A1~A6)

        if (resourceCategory.equals(registry.getString("Resource.Category.EXT"))) {
            if (mainClassSeletedValue.equals("E"))
                lovNum = 1; // MainClass�� E�� ��� Sub Class�� �� LOV �� 1��
            if (mainClassSeletedValue.equals("H"))
                lovNum = 10; // MainClass�� H�� ��� Sub Class�� �� LOV �� 10��
        }

        if (resourceCategory.equals(registry.getString("Resource.Category.SOC"))) {
        				// ����[SR: ] ����D ��û H8(CF) �߰� 
            lovNum = 8; // Sub Class�� �� LOV �� 7�� (H1~h7)  // [SR141016-058][20141027] shcho, ���� ���� Sub Class �� 2�� �߰� �Ͽ� �� 7��
        }

        for (int i = 1; i <= lovNum; i++) {
            final String subClassLovName = getChildLovName((mainClassSeletedValue + Integer.toString(i)).toString(), mainClassLovName);
            sub_class = SDVLOVUtils.comboValueSetting(sub_class, subClassLovName);
            if (createMode) {
                sub_class.getTextField().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
            }
            List<LovValue> lovValues = SDVLOVUtils.getLOVValues(subClassLovName);
            lovListMap.put(subClassLovName, lovValues);
        }
    }

    /**
     * SubClass���� ������ ���� LOV Name�� �������� �Լ�
     * 
     * @param seletedValue
     * @return
     */
    protected String getSelectedSubClassLovName(String subClassSeletedValue) {
        String seletedSubLovName = "";
        Iterator<String> iterator = lovListMap.keySet().iterator();
        outerloop: while (iterator.hasNext()) {
            String subClassLovName = iterator.next();
            List<LovValue> lovList = lovListMap.get(subClassLovName);
            Iterator<LovValue> lovValue = lovList.iterator();
            while (lovValue.hasNext()) {
                String targetValue = lovValue.next().getValue().toString();
                if (targetValue.equals(subClassSeletedValue)) {
                    seletedSubLovName = subClassLovName;
                    break outerloop;
                }
            }
        }
        return seletedSubLovName;
    }
}
