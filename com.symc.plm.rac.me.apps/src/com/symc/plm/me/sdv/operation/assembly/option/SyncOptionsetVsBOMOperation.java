/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly.option;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

/**
 * Class Name : SyncOptionsetVsBOMOperation
 * Class Description : BOMLine�� ���ǵ� �ɼ��� Update �ϴ� Operation
 * 
 * @date 2013. 10. 28.
 * 
 */
public class SyncOptionsetVsBOMOperation extends AbstractSDVActionOperation {

    private TCSession tcSession = null;
    private TCComponentBOMLine srcTopBomLine = null; // �����Ǵ� �ɼ��� Top BomLine
    private TCComponentBOMLine targetTopBomLine = null; // ��� Top Bomline
    // �����Ǵ� ���� �ɼ� ����Ʈ
    private ModularOption[] srcOptions;
    // �ɼ� Service
    private TCVariantService tcVarServ = null;

    private Registry registry = null;

    private IDataSet dataSet = null;
    private boolean isValidOK = true;

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public SyncOptionsetVsBOMOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    /*
     * (non-Javadoc)
     * TODO: Validation , ȭ�鿡�� �����Ǿ�� �� �ڵ�. �̵��ؾ� ��
     * 
     * @see org.sdv.core.common.ISDVOperation#preExecuteSDVOperation()
     */
    @Override
    public void startOperation(String commandId) {

        try {
            dataSet = getDataSet();
            // updateOption
            TCComponentItemRevision srcItemRevision = (TCComponentItemRevision) dataSet.getValue("updateOption", "SRC_PRODUCT_REV");
            // �����Ǵ� Top BOM Window
            TCComponentBOMWindow srcTopBomWindow = SDVBOPUtilities.getBOMWindow(srcItemRevision, "Latest Working", "bom_view");
            // �����Ǵ� �ɼ��� ���� Top BOMLINE
            srcTopBomLine = srcTopBomWindow.getTopBOMLine();
            // �����Ǵ� ���� �ɼ� ����Ʈ
            srcOptions = SDVBOPUtilities.getModularOptions(srcTopBomLine);

            if (srcOptions == null || srcOptions.length == 0) {
                MessageBox.post(registry.getString("NotOptionDefine.MSG"), registry.getString("Warning.NAME"), MessageBox.WARNING);
                isValidOK = false;
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            setAbortRequested(true);
        }

        isValidOK = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#executeSDVOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        if (!isValidOK)
            return;
        try {
            tcSession = (TCSession) getSession();
            tcVarServ = tcSession.getVariantService();

            // MPPAppication
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            // ���� BOM WINDOW
            TCComponentBOMWindow bomWindow = mfgApp.getBOMWindow();

            // �ɼ��� ����� Corporate Option Id�� Preference���� ������
            TCPreferenceService preferenceService = tcSession.getPreferenceService();
//            String[] corpIds = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, "PSM_global_option_item_ids");
            String[] corpIds = preferenceService.getStringValuesAtLocation("PSM_global_option_item_ids", TCPreferenceLocation.OVERLAY_LOCATION);

            // ��� BOMLine
            targetTopBomLine = bomWindow.getTopBOMLine();

            // ��� BOP�� �ɼ� ����Ʈ
            ModularOption[] targetOptions = SDVBOPUtilities.getModularOptions(targetTopBomLine);

            // ��� BOP�� �ɼ��� ������ �ű� �����
            if (targetOptions == null || targetOptions.length == 0) {
                tcSession.setStatus(registry.getString("creatingOption.MSG"));
                // ���� �ɼ� ����Ʈ
                for (ModularOption srcOption : srcOptions) {
                    HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                    OVEOption oveOption = CustomMVPanel.getOveOption(srcTopBomLine, options, srcOption);
                    // ����� �ɼ� ��
                    String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                    // (�ɼ� �߰�) �ɼ��� ��� BOMLINE�� �߰���
                    tcVarServ.lineDefineOption(targetTopBomLine, optionValue);
                }

                // ��� BOMLINE�� �ɼ� ��ȿ���˻� ������ ������
                SetLineMvl(false);

                // �̹� �ɼ��� ���� ���
            } else {

                // ���� �ɼ� �� ����Ʈ(Key:�ɼ� Name, Value:�ɼ� ��)
                Hashtable<String, String> srcOptionsHash = new Hashtable<String, String>();
                // ��� BOMLINE �ɼ� �� ����Ʈ(Key:�ɼ� Name, Value:�ɼ� ��)
                Hashtable<String, String> targetOptionsHash = new Hashtable<String, String>();

                // ��� BOMLINE �ɼ� �޼��� �ʱ�ȭ
                SetLineMvl(true);

                // ���� �ɼ� ����Ʈ���� �ɼǰ��� ������
                for (ModularOption srcOption : srcOptions) {
                    HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                    OVEOption oveOption = CustomMVPanel.getOveOption(srcTopBomLine, options, srcOption);
                    // ����� �ɼ� ��
                    String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                    if (!srcOptionsHash.containsKey(oveOption.option.name))
                        srcOptionsHash.put(oveOption.option.name, optionValue);
                }

                // ����Sample(CustomMVPanel.deleteOption(tcvariantservice, targetTopBomLine, oveOption);)

                // ��� �ɼ� ����Ʈ���� �ɼǰ��� ������
                for (ModularOption targetOption : targetOptions) {

                    HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                    OVEOption oveOption = CustomMVPanel.getOveOption(targetTopBomLine, options, targetOption);
                    // ����� �ɼ� ��
                    String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                    if (!targetOptionsHash.containsKey(oveOption.option.name))
                        targetOptionsHash.put(oveOption.option.name, optionValue);

                }

                // (������� �ݿ�����)
                // 1. �ɼǺ��� Update
                // (����X)E-BOM������ �ɼ��� ������ �� ��찡 ��� �����Ǵ� ���� �ݿ� �� �ʿ䰡 ����.
                // (�ɼǰ� ����X) Corporate Option�� ����Ǹ� �ڵ�����Ǿ �ݿ� �� �ʿ䰡 ����.
                // (�ɼ� �߰� O) �߰��Ǵ� �ɼǿ� ���Ͽ� UPDATE �� ��
                // 2. �ɼ� ��ȿ���˻� ���� Update
                // Full ������Ʈ
                boolean isExistAddOption = false;
                // ��� BOMLINE�� �������� �ʴ� �ɼ��� �߰���
                for (Enumeration<String> enm = srcOptionsHash.keys(); enm.hasMoreElements();) {
                    String option = (String) enm.nextElement();
                    String srcOptionValue = srcOptionsHash.get(option);
                    if (targetOptionsHash.containsKey(option))
                        continue;
                    if (!isExistAddOption) {
                        tcSession.setStatus(registry.getString("addingOption.MSG"));
                        isExistAddOption = true;
                    }
                    // (�ɼ� �߰�) �ɼ��� ��� BOMLINE�� �߰���
                    tcVarServ.lineDefineOption(targetTopBomLine, srcOptionValue);
                }

                // ��� BOMLINE�� �ɼ� ��ȿ���˻� ������ ������
                SetLineMvl(false);
            }
        } catch (Exception ex) {
            setAbortRequested(true);
            isValidOK = false;
            MessageBox.post(UIManager.getCurrentDialog().getShell(), ex.getMessage(), registry.getString("Inform.NAME"), MessageBox.INFORMATION);
            throw ex;

        }

    }

    /**
     * 
     * ��� BOMLINE�� �ɼ� ��ȿ���˻� ������ ������
     * 
     * @method SetLineMvl
     * @date 2013. 10. 23.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void SetLineMvl(boolean isClear) throws Exception {
        try {
            if (isClear) {
                tcVarServ.setLineMvl(targetTopBomLine, "");
            } else {
                String srcOptItemId = MVLLexer.mvlQuoteId(srcTopBomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);
                String targetOptItemId = MVLLexer.mvlQuoteId(targetTopBomLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);
                // ���� �ɼ� ��ȿ���˻� ������ ������
                String lineMvl = tcVarServ.askLineMvl(srcTopBomLine).replace(srcOptItemId, targetOptItemId);
                tcSession.setStatus(registry.getString("creatingOptValidation.MSG"));
                // ��� BOMLINE�� �ɼ� ��ȿ���˻� ������ ������
                tcVarServ.setLineMvl(targetTopBomLine, lineMvl);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
        if (!isValidOK)
            return;
        MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), registry.getString("Complete.MSG"), registry.getString("Inform.NAME"), MessageBox.INFORMATION);
    }

}
