package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

public class ResourceGunAssignSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ResourceGunAssignSDVValidator.class);

    public ResourceGunAssignSDVValidator() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);

        ArrayList<InterfaceAIFComponent> resourceItemsList = (ArrayList<InterfaceAIFComponent>) parameter.get("RESOURCE");
        TCComponentBOMLine targetComponent = (TCComponentBOMLine) parameter.get("BOPTARGET");

        if (resourceItemsList != null && targetComponent != null) {
            try {
                // ��ü Gun Validation (Plant WorkArea���� �Ҵ�)
                String targetType = targetComponent.getItem().getType();

                // Plant WorkArea �� �ƴϸ�...
                if (!targetType.equals(SDVTypeConstant.PLANT_OPAREA_ITEM)) {
                    for (InterfaceAIFComponent aifComponent : resourceItemsList) {
                        TCComponent resourceComponent = (TCComponent) aifComponent;
                        String resourceItemId = resourceComponent.getProperty("item_id");
                        String resourceType = resourceComponent.getType();

                        // Gun �̸� ���� ó��
                        if (resourceType.equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM_REV)) {
                            throw new ValidateSDVException(registry.getString("ResourceGunAssign.WorkArea.MESSAGE").replace("%0", resourceItemId));
                        }
                    }
                }
            } catch (Exception ex) {
                throw new ValidateSDVException(ex.getMessage(), ex);
            }
        }
    }

}
