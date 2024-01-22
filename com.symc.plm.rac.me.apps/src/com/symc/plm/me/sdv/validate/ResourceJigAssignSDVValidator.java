package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

public class ResourceJigAssignSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ResourceJigAssignSDVValidator.class);

    public ResourceJigAssignSDVValidator() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);

        ArrayList<InterfaceAIFComponent> resourceItemsList = (ArrayList<InterfaceAIFComponent>) parameter.get("RESOURCE");
        TCComponentBOMLine targetComponent = (TCComponentBOMLine) parameter.get("BOPTARGET");

        if (resourceItemsList != null && targetComponent != null) {
            try {
                // ��ü JIG Validation (Plant Station���� �Ҵ�)
                String targetType = targetComponent.getItem().getType();
                // Plant WorkArea �� �ƴϸ�...
                if (!targetType.equals(SDVTypeConstant.PLANT_STATION_ITEM)) {
                    for (InterfaceAIFComponent aifComponent : resourceItemsList) {
                        TCComponent resourceComponent = (TCComponent) aifComponent;
                        String resourceItemId = resourceComponent.getProperty("item_id");
                        String resourceType = resourceComponent.getType();

                        //��ü �̸�...
                        if (ResourceUtilities.getBOPType(resourceItemId).equals("Body")) {
                            // JIG �̸� ���� ó��
                            if (resourceType.equals(SDVTypeConstant.BOP_PROCESS_JIGFIXTURE_ITEM_REV)) {
                                throw new ValidateSDVException(registry.getString("ResourceJigAssign.Station.MESSAGE").replace("%0", resourceItemId));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                throw new ValidateSDVException(ex.getMessage(), ex);
            }
        }
    }

}
