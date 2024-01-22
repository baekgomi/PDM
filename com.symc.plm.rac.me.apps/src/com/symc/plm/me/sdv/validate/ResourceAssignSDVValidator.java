package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;
import org.sdv.core.util.SDVSpringContextUtil;

import com.symc.plm.me.sdv.service.resource.ResourceUtilities;
import com.symc.plm.me.sdv.viewpart.resource.ResourceSearchViewPart;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

public class ResourceAssignSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ResourceAssignSDVValidator.class);
    private ArrayList<InterfaceAIFComponent> resourceItemsList = null;
    private TCComponentBOMLine targetComponent;

    public ResourceAssignSDVValidator() {
    }

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        registry = Registry.getRegistry(this);

        // ResourceViewPart
        ResourceSearchViewPart resourceSearchViewPart = ResourceUtilities.getResourceSearchViewPart();
        if (resourceSearchViewPart == null) {
            throw new ValidateSDVException(registry.getString("ResourceAssign.Viewpart.MESSAGE"));
        }

        // ResourceViewPart�� ���õ� �ڿ�
        InterfaceAIFComponent[] targetResourceItems = resourceSearchViewPart.getCurrentTable().getSelectedItems();
        resourceItemsList = new ArrayList<InterfaceAIFComponent>(Arrays.asList(targetResourceItems));

        // MPPAppication BOP�� ���õ� Target
        // AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
        // targetComponent = (TCComponentBOMLine) application.getTargetComponent();
        // InterfaceAIFComponent ainterfaceaifcomponent[] = AifrcpPlugin.getSelectionMediatorService().getTargetComponents();
        // InterfaceAIFComponent ainterfaceaifcomponent[] = AIFUtility.getCurrentApplication().getTargetComponents();
        AIFComponentContext[] targetContexts = AIFUtility.getCurrentApplication().getTargetContexts();

        // �ڿ� ���� ����, target BOP ���� ���� üũ Validation
        if (resourceItemsList == null || resourceItemsList.size() <= 0) {
            throw new ValidateSDVException(registry.getString("ResourceAssign.Resource.MESSAGE"));
        }
        if (targetContexts == null || targetContexts.length <= 0) {
            throw new ValidateSDVException(registry.getString("ResourceAssign.BOMLine.MESSAGE"));
        }

        // �ڿ��� target BOP�� ���õǾ� ������ Validation ����
        targetComponent = (TCComponentBOMLine) targetContexts[0].getComponent();
        parameter.put("RESOURCE", resourceItemsList);
        parameter.put("BOPTARGET", targetComponent);

        // �Ҵ� ��� Validation 1 (�ڿ��� �ڿ��� �Ҵ����� �ʴ´�.)
        SelectedResourceSDVValidator selectedResourceSDVValidator = (SelectedResourceSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.SelectedResourceSDVValidator");
        selectedResourceSDVValidator.validate(commandId, parameter, applicationCtx);

        // �Ҵ� ��� Validation 2 (��ü ����� Plant���� �Ҵ�ǵ��� �Ѵ�.)
        ResourceAssignToPlantSDVValidator resourceAssignToPlantSDVValidator = (ResourceAssignToPlantSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceAssignToPlantSDVValidator");
        resourceAssignToPlantSDVValidator.validate(commandId, parameter, applicationCtx);

        // ���� �ڿ�(ID) �ߺ� �Ҵ� �Ұ� Validation
        ResourceDuplicatedAssignSDVValidator resourceDuplicatedAssignSDVValidator = (ResourceDuplicatedAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceDuplicatedAssignSDVValidator");
        resourceDuplicatedAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // ���� Validation (BOPOperation���� �Ҵ�)
        ResourceToolAssignSDVValidator resourceToolAssignSDVValidator = (ResourceToolAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceToolAssignSDVValidator");
        resourceToolAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // ������ Validation (BOPOperation���� �Ҵ�)
        ResourceSubsidiaryAssignSDVValidator resourceSubsidiaryAssignSDVValidator = (ResourceSubsidiaryAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceSubsidiaryAssignSDVValidator");
        resourceSubsidiaryAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // ��ü Robot Validation (Plant WorkArea���� �Ҵ�, Robot�� Plant WorkArea�� �ϳ��� ����)
        ResourceRobotAssignSDVValidator resourceRobotAssignSDVValidator = (ResourceRobotAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceRobotAssignSDVValidator");
        resourceRobotAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // ��ü Gun Validation (Plant WorkArea���� �Ҵ�)
        ResourceGunAssignSDVValidator resourceGunAssignSDVValidator = (ResourceGunAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceGunAssignSDVValidator");
        resourceGunAssignSDVValidator.validate(commandId, parameter, applicationCtx);

        // ��ü JIG Validation (Plant Station���� ��ü Jig �Ҵ�)
        ResourceJigAssignSDVValidator resourceJigAssignSDVValidator = (ResourceJigAssignSDVValidator) SDVSpringContextUtil.getBean("com.symc.plm.me.sdv.validate.ResourceJigAssignSDVValidator");
        resourceJigAssignSDVValidator.validate(commandId, parameter, applicationCtx);
    }

}
