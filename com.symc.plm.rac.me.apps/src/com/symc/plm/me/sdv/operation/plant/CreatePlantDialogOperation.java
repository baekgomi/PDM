/**
 *
 */
package com.symc.plm.me.sdv.operation.plant;

import java.awt.Frame;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IDialogOpertation;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.service.Plant.PlantUtilities;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : AbstractTCSDVExecuteOperation
 * Class Description :
 * 
 * @date 2013. 9. 17.
 * 
 */
public class CreatePlantDialogOperation extends AbstractTCSDVOperation implements IDialogOpertation {

    private static final Logger logger = Logger.getLogger(CreatePlantDialogOperation.class);
    public String dialogId;

    protected Frame parentFrame;
    private Map<String, Object> parameter;
    private boolean isValidOK;
    // private Registry registry;
    private String opareaMessage;

    @SuppressWarnings("unused")
    @Override
    public void startOperation(String commandId) {
        // registry = Registry.getRegistry(this);
        setParentFrame();
        isValidOK = true;
        opareaMessage = null;

        // MPPAppication
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        // ���� BOM WINDOW
        TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

        try {
            // // (üũ)1. BOP Load ���� : Plant Shop ����
            // if (bomWindow == null) {
            // dialogId = "symc.me.bop.CreateShopItemDialog";
            // return;
            // }
            //
            // // (üũ)2. Plant BOP Load ���� : Plant�� �ƴϸ� Shop ����
            // MFGStructureType mfgType = MFGStructureTypeUtil.getStructureType(bomWindow.getTopBOMLine());
            // if (mfgType != MFGStructureType.Plant) {
            // dialogId = "symc.me.bop.CreateShopItemDialog";
            // return;
            // }

            // // (üũ)3. Top�� Shop���� ���� : Shop�� �ƴϸ� ���� ó��
            // String topItemType = bomWindow.getTopBOMLine().getItem().getType();
            // boolean isEnableType = topItemType.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            // if (!isEnableType) {
            // MessageBox.post(AIFUtility.getActiveDesktop().getShell(), registry.getString("noBopLoad.MSG"), "Warning", MessageBox.WARNING);
            // isValidOK = false;
            // return;
            // } else {
            // }

            // (üũ)3. interfaceAIFComponent �� : Mfg0BvrWorkarea�� �ƴϸ� Shop ����
            InterfaceAIFComponent interfaceAIFComponent = mfgApp.getTargetComponent();
            TCComponentItemRevision itemRevision = PlantUtilities.getItemRevision(interfaceAIFComponent);
            String itemRevisionType = itemRevision.getType();

            // ������ targetItem��
            // Shop�ΰ�� : Line ���� ȭ��
            if (itemRevisionType != null && itemRevisionType.equals(SDVTypeConstant.PLANT_SHOP_ITEM_REVISION)) {
                opareaMessage = itemRevision.toString() + " is Shop Item.";
                isValidOK = false;
                return;
            } else if (itemRevisionType != null && itemRevisionType.equals(SDVTypeConstant.PLANT_LINE_ITEM_REVISION)) {
                // Line�ΰ�� : Station ���� ȭ��
                dialogId = "symc.me.bop.CreateStationItemDialog";
            } else if (itemRevisionType != null && itemRevisionType.equals(SDVTypeConstant.PLANT_STATION_ITEM_REVISION)) {
                // Station�ΰ�� : OPArea ���� ȭ��
                dialogId = "symc.me.bop.CreateWorkareaItemDialog";
            } else if (itemRevisionType != null && itemRevisionType.equals(SDVTypeConstant.PLANT_OPAREA_ITEM_REV)) {
                // OPArea�ΰ�� : �˸��� ����.
                isValidOK = false;
                opareaMessage = "Plant WorkArea child can not be added.";
                return;
            } else {
                isValidOK = false;
            }

            // Released�� �ƴϸ� ���� ó��
            if (CustomUtil.isReleased(itemRevision)) {
                opareaMessage = itemRevision.toString() + " is Released.";
                isValidOK = false;
                return;
            }

            // ItemRevision�� View�� Parameter�� ������.
            parameter = getParamters();
            parameter.put("targetItemRevision", itemRevision);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // }
        // isValidOK = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            if (!isValidOK) {
                throw new SDVException((opareaMessage == null) ? "Failed to initialize the Dialog" : opareaMessage);
            }

            Shell shell = AIFUtility.getActiveDesktop().getShell();
            IDialog dialog = UIManager.getDialog(shell, dialogId);
            dialog.setParameters(parameter);
            dialog.open();
        } catch (Exception exception) {
            logger.error(exception.getClass().getName(), exception);
            MessageBox messagebox = new MessageBox(parentFrame, exception);
            messagebox.setModal(true);
            messagebox.setVisible(true);
        }
    }

    protected void setParentFrame() {
        this.parentFrame = AIFDesktop.getActiveDesktop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#afterExecuteSDVOperation()
     */
    @Override
    public void endOperation() {
        // IDialog dialog = UIManager.getActiveDialog(dialogId);
    }

    /**
     * @return the dialogId
     */
    public String getDialogId() {
        return dialogId;
    }

    /**
     * @param dialogId
     *            the dialogId to set
     */
    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

}
