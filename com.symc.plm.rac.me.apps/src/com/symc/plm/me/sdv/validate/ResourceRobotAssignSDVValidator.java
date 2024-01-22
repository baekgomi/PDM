package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

public class ResourceRobotAssignSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ResourceRobotAssignSDVValidator.class);

    public ResourceRobotAssignSDVValidator() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);

        ArrayList<InterfaceAIFComponent> resourceItemsList = (ArrayList<InterfaceAIFComponent>) parameter.get("RESOURCE");
        TCComponentBOMLine targetComponent = (TCComponentBOMLine) parameter.get("BOPTARGET");

        if (resourceItemsList != null && targetComponent != null) {
            try {
                // ��ü Robot Validation (Plant WorkArea���� �Ҵ�, Robot�� Plant WorkArea�� �ϳ��� ����)
                String targetType = targetComponent.getItem().getType();

                for (InterfaceAIFComponent aifComponent : resourceItemsList) {
                    TCComponent resourceComponent = (TCComponent) aifComponent;
                    String resourceItemId = resourceComponent.getProperty("item_id");
                    String resourceType = resourceComponent.getType();
                    //Robot�̸�...
                    if (resourceType.equals(SDVTypeConstant.BOP_PROCESS_ROBOT_ITEM_REV)) {
                        //Plant WorkArea �� �ƴϸ� ���� ó��
                        if (!targetType.equals(SDVTypeConstant.PLANT_OPAREA_ITEM)) {
                            throw new ValidateSDVException(registry.getString("ResourceRobotAssign.WorkArea.MESSAGE").replace("%0", resourceItemId));
                        }
                        
                        //Robot�� �̹� �Ҵ� �Ǿ� ������ ���� ó��
                        if(compareResourceItemRevisionType(resourceType, targetComponent)) {
                            throw new ValidateSDVException(registry.getString("ResourceRobotAssign.Robot.MESSAGE"));
                        }
                    }
                }
            } catch (Exception ex) {
                throw new ValidateSDVException(ex.getMessage(), ex);
            }
        }
    }

    /**
     * �Ҵ��� �ڿ�(Robot)������, ��� �̹� �Ҵ� �Ǿ��ִ��� Type �� �ϴ� �Լ�
     * 
     * @param resourceItemId
     * @throws TCException
     */
    public boolean compareResourceItemRevisionType(String resourceItemRevisionType, TCComponentBOMLine targetComponent) throws TCException {
        boolean isDuplicate = false;
        AIFComponentContext[] aifComponentContexts = targetComponent.getChildren();

        for (AIFComponentContext aifComponentContext : aifComponentContexts) {
            TCComponentBOMLine childComponent = (TCComponentBOMLine) aifComponentContext.getComponent();
            String childItemRevisionType = childComponent.getItemRevision().getType();

            if (childItemRevisionType.equals(resourceItemRevisionType)) {
                isDuplicate = true;
                break;
            }
        }

        return isDuplicate;
    }

}
