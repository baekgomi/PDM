package com.symc.plm.me.sdv.validate;

import java.util.Map;


import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;
import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

public class ProcessLoadedSDVValidator implements ISDVValidator {

    private Registry registry = Registry.getRegistry(ProcessLoadedSDVValidator.class);

    public ProcessLoadedSDVValidator() {
    }

    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        if(!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
            throw new ValidateSDVException(registry.getString("Common.SelectedMPPApp.MESSAGE"));
        }

        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            // (üũ)1. BOP Load ����
            if(bomWindow == null) {
                throw new ValidateSDVException(registry.getString("Common.NoBOPLoad.MESSAGE", "BOP Structure is not Loaded."));
            }

            // (üũ)2. Product BOP Load ����
//            MFGStructureType mfgType = MFGStructureTypeUtil.getStructureType(bomWindow.getTopBOMLine());
//            if(mfgType != MFGStructureType.Process) {
//                throw new ValidateSDVException(registry.getString("Common.NoProcessBOPLoad.MESSAGE", "Process BOP Structure is not Loaded."));
//            }
            boolean isProcess = MFGStructureTypeUtil.isProcess(bomWindow.getTopBOMLine());
            if(!isProcess) {
            	throw new ValidateSDVException(registry.getString("Common.NoProcessBOPLoad.MESSAGE", "Process BOP Structure is not Loaded."));
            }
        } catch(TCException e) {
            e.printStackTrace();
        }
    }

}
