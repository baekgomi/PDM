/**
 *
 */
package com.symc.plm.me.sdv.operation.body;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCComponentMECfgLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.kernel.VariantCondition;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

/**
 *
 */
public class CopyToAlternativeBOPActionOperation extends AbstractSDVActionOperation {
    private Registry registry = Registry.getRegistry(CopyToAlternativeBOPActionOperation.class);
    private TCSession session;
    private TCComponentBOPLine altBOPRootLine = null;
    private TCComponentBOMLine plantRootLine = null;
    private TCVariantService variantService = null;
    private final int ADD = 1;
    private final int DELETE = 0;

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public CopyToAlternativeBOPActionOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public CopyToAlternativeBOPActionOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    /**
     * @param actionId
     * @param ownerId
     * @param parameters
     * @param dataSet
     */
    public CopyToAlternativeBOPActionOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataSet) {
        super(actionId, ownerId, parameters, dataSet);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        session = CustomUtil.getTCSession();

        IDataSet dataSet = getDataSet();

        try {
            variantService = session.getVariantService();

            Object target_bop = dataSet.getValue("copyToAltView", SDVPropertyConstant.WELDOP_REV_TARGET_OP);
            Object alt_prefix = dataSet.getValue("copyToAltView", SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
            Object with_enditem = dataSet.getValue("copyToAltView", "ContainedEI");

            if (target_bop == null || target_bop.toString().length() == 0)
                throw new NullPointerException(registry.getString("TargetBOPIsNull.MESSAGE", "��� BOP ���� �����ϴ�. �����ڿ��� �����ϼ���."));

            if (!(target_bop instanceof TCComponentBOPLine))
                throw new Exception(registry.getString("TargetBOPIsNotBOPLine.MESSAGE", "��� BOP ������ �߸� �Ǿ����ϴ�."));

            if (alt_prefix == null || alt_prefix.toString().length() == 0)
                throw new NullPointerException(registry.getString("AltPrefixParameterNull.MESSAGE", "�ӽ� BOP�� ��� ���� �����ϴ�. �����ڿ��� �����ϼ���."));

            // ������ ��� BOP�� Shop�� �����´�.
            TCComponentItemRevision shop_rev;
            String target_item_type = ((TCComponentBOPLine) target_bop).getItem().getType();
            if (target_item_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
                shop_rev = ((TCComponentBOPLine) target_bop).getItemRevision();
            else
                shop_rev = ((TCComponentBOPLine) target_bop).parent().getItemRevision();

            TCComponent workarea_rev = shop_rev.getRelatedComponent(SDVTypeConstant.MFG_WORKAREA);
            if (workarea_rev == null)
                throw new NullPointerException("BOP Shop was not contained Plant. Please first contain to Plant Shop.");
            plantRootLine = CustomUtil.getBomline((TCComponentItemRevision) workarea_rev, session);

            // Alternative Shop�� �����ϴ��� ���� üũ�Ѵ�.
            String shop_code = shop_rev.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            boolean is_source_alt = shop_rev.getLogicalProperty(SDVPropertyConstant.SHOP_REV_IS_ALTBOP);
            String source_alt_prefix = shop_rev.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
            String alt_shop_code = is_source_alt ? shop_code.replace(source_alt_prefix, alt_prefix.toString()) : alt_prefix + "-" + shop_code;
            TCComponentItem alt_shop_item = SDVBOPUtilities.FindItem(alt_shop_code, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
            boolean bshop_created = false;

            if (alt_shop_item == null) {
                if (target_item_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
                    throw new Exception("Alternative Shop is not find[" + alt_shop_code + "]. Please contact to BOP Admin.");

                // Alternative Shop�� ������ �����Ѵ�.
                alt_shop_item = CustomUtil.createItem(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, alt_shop_code, SDVPropertyConstant.ITEM_REV_ID_ROOT, shop_rev.getItem().getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), shop_rev.getItem().getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));

                bshop_created = true;
            } else {
                if (CustomUtil.isReleased(alt_shop_item.getLatestItemRevision())) {
                    throw new Exception("Alternative Shop is released.[" + alt_shop_code + "]. Please contact to BOP Admin.");
                    // String new_rev_id = alt_shop_item.getNewRev();
                    // alt_shop_item.getLatestItemRevision().saveAs(new_rev_id);
                    // // alt_shop_item.refresh();
                }
            }

            // Shop ������ �����Ѵ�.
            if (target_item_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
                setProperties(alt_shop_item.getLatestItemRevision(), shop_rev, alt_prefix.toString());

            // Alternative Shop�� BOPLine�� �����.
            altBOPRootLine = CustomUtil.getBopline(alt_shop_item.getLatestItemRevision(), session);

            // �ɼ��� ������ ���� �ʾƵ� �ǳ�?
            if (target_item_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
                copyTopOptions((TCComponentBOPLine) target_bop, altBOPRootLine);

            if (target_item_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)) {
                if (!bshop_created) {
                    // Shop�� �ű� ������ �ƴϹǷ� ���� ��� ������ �����Ѵ�.
                    cutAllChildren(altBOPRootLine);
                }

                // ��� BOP���� ������ �׸��� Shop�̸� ���� ����
                if (((TCComponentBOPLine) target_bop).hasChildren())
                    makeChildBOPLine((TCComponentBOPLine) target_bop, altBOPRootLine, alt_prefix.toString(), (Boolean) with_enditem);
            } else {
                // ��� BOP���� ������ �׸��� �����̸� Alternative ������ �����Ѵ��� üũ
                String line_id = ((TCComponentBOPLine) target_bop).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                String alt_line_id = is_source_alt ? line_id.replace(source_alt_prefix, alt_prefix.toString()) : alt_prefix + "-" + line_id;
                TCComponentItem alt_line_item = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, alt_line_id);
                TCComponentBOPLine alt_line_bopline = null;

                // �������� ������ ����
                if (alt_line_item == null) {
                    throw new NullPointerException("Alternative Line Item is not found.[" + alt_line_id + "] Please contact to BOP Admin.");
                    // alt_line_item = ((TCComponentBOPLine) target_bop).getItemRevision().saveAsItem(alt_line_id, SDVPropertyConstant.ITEM_REV_ID_ROOT);
                } else {
                    if (CustomUtil.isReleased(alt_line_item.getLatestItemRevision())) {
                        throw new NullPointerException("Alternative Line Item is Released.[" + alt_line_id + "] Please contact to BOP Admin.");

                        // String new_rev_id = alt_line_item.getNewRev();
                        // alt_line_item.getLatestItemRevision().saveAs(new_rev_id);
                        // alt_line_item.refresh();
                    }
                }

                // ������ ���� ����
                // setProperties(alt_line_item.getLatestItemRevision(), ((TCComponentBOPLine) target_bop).getItemRevision(), alt_prefix.toString());

                // alt_shop_line ������ �����ϴ��� üũ�ؼ� �������� ������ ����.
                // if (altBOPLine.hasChildren())
                // {
                AIFComponentContext[] alt_child_lines = altBOPRootLine.getChildren();
                for (AIFComponentContext alt_child_line : alt_child_lines) {
                    if (alt_child_line.getComponent() instanceof TCComponentAppGroupBOPLine)
                        continue;

                    TCComponentBOPLine child_bopline = (TCComponentBOPLine) alt_child_line.getComponent();
                    String child_id = child_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    if (child_id.equals(alt_line_id)) {
                        alt_line_bopline = child_bopline;
                        break;
                    }
                }

                // Alternative Shop ������ ������ ������ ����
                if (alt_line_bopline == null) {
                    alt_line_bopline = (TCComponentBOPLine) altBOPRootLine.add(alt_line_item, null);
                    alt_line_bopline.save();
                    altBOPRootLine.save();
                }
                // }
                // else
                // {
                // // Alternative Shop ���� �������� ������ ���� ����.
                // alt_line_bopline = (TCComponentBOPLine) altBOPLine.add(alt_line_item, null);
                // }

                // ������������ üũ�ؼ� �������� �����Ѵ�.
                // if (alt_line_bopline.isModifiable("bl_plmxml_occ_xform"))
                // setBOPLineProperties((TCComponentBOPLine) target_bop, alt_line_bopline);

                if (alt_line_bopline != null) {
                    // alt_line_bopline.refresh();
                    // alt_line_bopline.window().refresh();
                    makeChildBOPLine((TCComponentBOPLine) target_bop, alt_line_bopline, alt_prefix.toString(), (Boolean) with_enditem);
                }
            }

            // 2014/03/20 PERT ���� �ִ� �κ��� ��� �߰�
            updatePertInfo((TCComponentBOPLine) target_bop, altBOPRootLine, alt_prefix.toString());

            altBOPRootLine.window().save();
            refreshBOMLine(altBOPRootLine);
            altBOPRootLine.window().clearCache();
            altBOPRootLine.window().refresh();
            altBOPRootLine.window().newIrfWhereConfigured(altBOPRootLine.getItemRevision());
            altBOPRootLine.window().fireComponentChangeEvent();
            // altBOPRootLine.window().close();

            /* [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link������ ���̻� ������.
                AIFComponentContext[] rev_metarget_contexts = shop_rev.getRelated(SDVTypeConstant.MFG_TARGETS);
            */
            
            final TCComponentItemRevision openRevision = alt_shop_item.getLatestItemRevision();
            /* [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link������ ���̻� ������.
                final TCComponent prodItem = rev_metarget_contexts == null || rev_metarget_contexts.length == 0 ? null : (TCComponentItemRevision) rev_metarget_contexts[0].getComponent();
            */
            AbstractAIFOperation openOperation = new AbstractAIFOperation() {

                @Override
                public void executeOperation() throws Exception {
                    try {
                        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                        AbstractViewableTreeTable[] openTreeTables = mfgApp.getViewableTreeTables();
                        boolean isOpened = false;
                        for (AbstractViewableTreeTable openTreeTable : openTreeTables) {
                            if (openTreeTable.getBOMRoot().getItem().equals(openRevision.getItem())) {
                                isOpened = true;
                            }
                        }

                        if (!isOpened)
                            mfgApp.open(openRevision.getItem());
                        storeOperationResult(IStatus.OK);
                    } catch (Exception e) {
                        e.printStackTrace();
                        storeOperationResult(IStatus.ERROR);
                        return;
                    }
                }
            };
            openOperation.addOperationListener(new InterfaceAIFOperationListener() {
                @Override
                public void startOperation(String arg0) {
                }

                @Override
                public void endOperation() {
                    try {
                        Thread.sleep(2000);

                        // if (getOperationResult().equals(IStatus.OK))
                        {
                            /* [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link������ ���̻� ������.
                            AIFComponentContext[] targetRelated = openRevision.getRelated(SDVTypeConstant.MFG_TARGETS);
                            if (prodItem != null && (targetRelated == null || targetRelated.length == 0))
                                openRevision.add(SDVTypeConstant.MFG_TARGETS, prodItem);
                            */
                            AIFComponentContext[] workareaRelated = openRevision.getRelated(SDVTypeConstant.MFG_WORKAREA);
                            if (plantRootLine != null && (workareaRelated == null || workareaRelated.length == 0))
                                openRevision.add(SDVTypeConstant.MFG_WORKAREA, plantRootLine.getItem());
                        }
                        session.setStatus("");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            });
            session.queueOperation(openOperation);
        } catch (Exception ex) {
            setErrorMessage(ex.getMessage());
            setExecuteError(ex);
            throw ex;
        } finally {
            if (altBOPRootLine != null)
                altBOPRootLine.window().close();
            if (plantRootLine != null)
                plantRootLine.window().close();
        }
    }

    /**
     * �ɼ��� �����ϴ� �Լ�
     *
     * @param option_from_bopline
     * @param option_copyto_bopline
     * @throws Exception
     */
    private void copyTopOptions(TCComponentBOPLine option_from_bopline, TCComponentBOPLine option_copyto_bopline) throws Exception {
        try {
            TCVariantService variantService = session.getVariantService();
            ModularOption[] srcOptions = SDVBOPUtilities.getModularOptions(option_from_bopline);
            ModularOption[] copyToOptions = SDVBOPUtilities.getModularOptions(option_copyto_bopline);

            // �����ϱ� ���� ��� �ɼǵ��� ������.
            if (copyToOptions != null && copyToOptions.length > 0) {
                // Option ���� ���� ���� ����
                variantService.setLineMvl(option_copyto_bopline, "");
                option_copyto_bopline.save();
                // �� �ɼ� ����
                for (ModularOption copyToOption : copyToOptions) {
                    variantService.lineDeleteOption(option_copyto_bopline, copyToOption.optionId);
                    option_copyto_bopline.save();
                }

                // refreshBOMLine(option_copyto_bopline);
            }

            // �ɼǵ��� ���
//            String[] corpIds = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, "PSM_global_option_item_ids");
            String[] corpIds = session.getPreferenceService().getStringValuesAtLocation("PSM_global_option_item_ids", TCPreferenceLocation.OVERLAY_LOCATION);
            for (ModularOption srcOption : srcOptions) {
                HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                OVEOption oveOption = CustomMVPanel.getOveOption(option_from_bopline, options, srcOption);
                // ����� �ɼ� ��
                String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                // (�ɼ� �߰�) �ɼ��� ��� BOMLINE�� �߰���
                variantService.lineDefineOption(option_copyto_bopline, optionValue);
                option_copyto_bopline.save();
            }

            // refreshBOMLine(option_copyto_bopline);

            // �ɼ��� ���� ����
            String srcOptItemId = MVLLexer.mvlQuoteId(option_from_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);
            String targetOptItemId = MVLLexer.mvlQuoteId(option_copyto_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);

            // ���� �ɼ� ��ȿ���˻� ������ ������
            String lineMvl = variantService.askLineMvl(option_from_bopline).replace(srcOptItemId, targetOptItemId);

            // ��� BOMLINE�� �ɼ� ��ȿ���˻� ������ ������
            variantService.setLineMvl(option_copyto_bopline, lineMvl);

            option_copyto_bopline.save();
            // refreshBOMLine(option_copyto_bopline);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * ���� ��� �ڽĵ��� BOMLine���� �����Ѵ�.
     *
     * @param alt_shop_bopline
     */
    private void cutAllChildren(TCComponentBOPLine bopLine) throws Exception {
        try {
            if (bopLine == null)
                return;
            // long startTime = System.currentTimeMillis();

            // bopLine.refresh();

            AIFComponentContext[] child_boplines = bopLine.getChildren();
            ArrayList<TCComponentBOMLine> to_delete_lines = new ArrayList<TCComponentBOMLine>();

            for (AIFComponentContext child_context : child_boplines) {
                if (!(child_context.getComponent() instanceof TCComponentAppGroupBOPLine))
                    to_delete_lines.add((TCComponentBOMLine) child_context.getComponent());
            }

            if (to_delete_lines.size() > 0) {
                SDVBOPUtilities.disconnectObjects(bopLine, to_delete_lines);
            }

            bopLine.save();
            // bopLine.clearCache();
            // bopLine.refresh();
            // bopLine.window().newIrfWhereConfigured(bopLine.getItemRevision());
            // bopLine.window().fireComponentChangeEvent();
            // System.out.println("cutChildren time =>" + (System.currentTimeMillis() - startTime));
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * ������ BOP Line���� �������� Alternative BOP�� �����ϰ� ������ �����ϴ� �Լ�
     *
     * @param targetBopLine
     * @param altBopLine
     * @param altPrefix
     * @param withEndItem
     * @throws Exception
     */
    private void makeChildBOPLine(TCComponentBOPLine targetBopLine, TCComponentBOPLine altBopLine, String altPrefix, boolean withEndItem) throws Exception {
        try {
            AIFComponentContext[] target_child_boplines = targetBopLine.getChildren();

            if (targetBopLine == null || altBopLine == null)
                return;

            // Alternative BOP ������ �ڽ��� �����ϸ� ��� �߶󳻰� ���� �ٿ�����.
            cutAllChildren(altBopLine);

            // ��� BOP ���� �ڽĵ��� Alternative �׸��� �����ϴ��� üũ
            if (target_child_boplines != null && target_child_boplines.length > 0) {
                for (AIFComponentContext target_child_context : target_child_boplines) {
                    if (target_child_context.getComponent() instanceof TCComponentAppGroupBOPLine)
                        continue;

                    TCComponentBOPLine added_new_altbopline = null;
                    TCComponentBOPLine target_child_bopline = (TCComponentBOPLine) target_child_context.getComponent();

                    String target_child_type = target_child_bopline.getItem().getType();
                    String occ_type = target_child_bopline.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE);

                    // BOP �������̸� ������ ��ü�� �����ؾ� �Ѵ�. ������ �������� �ʰ� �Ӽ��� ����
                    if (target_child_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM)
                            || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM)) {
                        TCComponentItemRevision target_revision = target_child_bopline.getItemRevision();
                        // target_revision.refresh();
                        String target_child_id = target_child_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                        boolean is_alt_bop = target_revision.getLogicalProperty(SDVPropertyConstant.SHOP_REV_IS_ALTBOP);
                        String alt_prefix = target_revision.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);

                        String bop_child_id = is_alt_bop ? target_child_id.replace(alt_prefix, altPrefix) : altPrefix + "-" + target_child_id;
                        TCComponentItem alt_child_item = CustomUtil.findItem(target_child_type, bop_child_id);

                        // �ӽ� BOP�� �������� ������ �����Ѵ�.
                        if (alt_child_item == null) {
                            alt_child_item = target_revision.saveAsItem(bop_child_id, SDVPropertyConstant.ITEM_REV_ID_ROOT);
                            // alt_child_item.save();
                        } else {
                            if (CustomUtil.isReleased(alt_child_item.getLatestItemRevision())) {
                                String newRevisionID = alt_child_item.getNewRev();
                                // TCComponentItemRevision newRevision =
                                alt_child_item.getLatestItemRevision().saveAs(newRevisionID);
                                // newRevision.save();
                                // refreshBOMLine(newRevision);
                                // refreshBOMLine(alt_child_item);
                            }
                        }

                        // �������� �Ӽ��� ��������.
                        setProperties(alt_child_item.getLatestItemRevision(), target_revision, altPrefix);

                        // ����� ���� ����Ÿ���� ��� �����ϴ� �κ�
                        AIFComponentContext[] rev_under_items = alt_child_item.getLatestItemRevision().getChildren(new String[] { SDVTypeConstant.PROCESS_SHEET_KO_RELATION, SDVTypeConstant.PROCESS_SHEET_EN_RELATION, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION });
                        for (AIFComponentContext rev_under_item : rev_under_items) {
                            if (rev_under_item.getComponent() instanceof TCComponentDataset) {
                                try {
                                    ((TCComponentDataset) rev_under_item.getComponent()).delete();
                                } catch (TCException ex) {
                                    alt_child_item.getLatestItemRevision().cutOperation(rev_under_item.getContext().toString(), new TCComponent[] { (TCComponent) rev_under_item.getComponent() });
                                    try {
                                        ((TCComponentDataset) rev_under_item.getComponent()).delete();
                                    } catch (TCException ex2) {
                                        ex2.printStackTrace();
                                    }
                                }
                                // alt_child_item.getLatestItemRevision().save();
                                // refreshBOMLine(alt_child_item);
                            }
                        }

                        if (alt_child_item != null) {
                            for (AIFComponentContext childContext : altBopLine.getChildren()) {
                                if (childContext.getComponent() instanceof TCComponentAppGroupBOPLine)
                                    continue;

                                if (bop_child_id.equals(childContext.getComponent().getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                                    added_new_altbopline = (TCComponentBOPLine) childContext.getComponent();
                                    break;
                                }
                            }
                            if (added_new_altbopline == null) {
                                ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                addToChild.add(alt_child_item);
                                TCComponent[] addedChild = SDVBOPUtilities.connectObject(altBopLine, addToChild, null);
                                if (addedChild == null || addedChild.length == 0)
                                    throw new Exception("Can not add to BOP Line.");
                                added_new_altbopline = (TCComponentBOPLine) addedChild[0];
                                added_new_altbopline.save();
                                altBopLine.save();
                                // refreshBOMLine(added_new_altbopline);
                                // refreshBOMLine(altBopLine);
                            }
                        }

                        // BOP line information copy
                        if (added_new_altbopline != null) {
                            setBOPLineProperties(target_child_bopline, added_new_altbopline);
                        }

                        // ���� ������ �켱 ���� �����ϰ� �ٽ� ���δ�.
                        if ((target_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM)) && added_new_altbopline != null) {
                            // EndItem �� �������� ���� ��� ������ ������ ��� �����Ѵ�.
                            cutAllChildren(added_new_altbopline);

                            // long startTime = System.currentTimeMillis();
                            if (withEndItem) {
                                // EndItem/������/Plant �� ������ ���� PathNode ���ᵵ ���� �����Ͽ��� �Ѵ�.
                                copyOperationChild(target_child_bopline, added_new_altbopline, altPrefix);
                                // System.out.println("copyOperationChild time =>" + (System.currentTimeMillis() - startTime));
                            }
                        }

                        if (target_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM)) {
                            // TODOS �������� Activity ����� ��� �ǳ�?
                            copyMEActivitiesOfOperation(target_child_bopline, added_new_altbopline);
                        }

                        // �����̳� ������ �ٽ� ������ �����ϵ��� ���ȣ�� �Ѵ�.
                        if (target_child_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || target_child_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
                            makeChildBOPLine(target_child_bopline, added_new_altbopline, altPrefix, withEndItem);
                    } else if (occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE)) {
                        // Ÿ���� �����̰�, ���������� Plant�� MEResource�� �����Ѵ�.
                        TCComponentMEAppearancePathNode[] linkedPaths = target_child_bopline.askLinkedAppearances(false);

                        if (linkedPaths == null || linkedPaths.length == 0) {
                            ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                            addToChild.add(target_child_bopline.getItem());
                            TCComponent[] addedChild = SDVBOPUtilities.connectObject(altBopLine, addToChild, occ_type);
                            if (addedChild == null || addedChild.length == 0)
                                throw new Exception("Can not add to BOP Line.");
                            added_new_altbopline = (TCComponentBOPLine) addedChild[0];
                            added_new_altbopline.save();
                            altBopLine.save();
                            // refreshBOMLine(altBopLine);
                            // refreshBOMLine(added_new_altbopline);

                            setBOPLineProperties(target_child_bopline, added_new_altbopline);
                        } else {
                            TCComponentBOMLine plantBOMLine = plantRootLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantRootLine);
                            TCComponentBOMLine productPlantBOMLine = null;
                            boolean is_alt_plant = false;
                            String alt_prefix = null;

                            // MEResource�� ������ Alternative���� üũ�ϰ� �� ������ �������� ������ Product Plant������ �˻�����.
                            if (plantBOMLine.parent().getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_IS_ALTBOP))
                                is_alt_plant = plantBOMLine.parent().getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
                            if (plantBOMLine.parent().getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_ALT_PREFIX))
                                alt_prefix = plantBOMLine.parent().getItemRevision().getStringProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX);
                            if (!is_alt_plant || (is_alt_plant && alt_prefix != null && !alt_prefix.equals(altPrefix))) {
                                // ������ Alternative�� �ƴϸ� �� �������� Alternative�� ã�´�.
                                productPlantBOMLine = getAlternativeBOPLineInParent(plantBOMLine.parent().parent(), plantBOMLine.parent(), altPrefix);
                                if (productPlantBOMLine != null) {
                                    // ������ Alternative Plant ���� �ڱ� Plant������ ã�´�.
                                    productPlantBOMLine = getAlternativeBOPLineInParent(productPlantBOMLine, plantBOMLine, "");

                                    if (productPlantBOMLine != null) {
                                        ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                        addToChild.add(productPlantBOMLine);
                                        TCComponent[] addedChild = SDVBOPUtilities.connectObject(altBopLine, addToChild, occ_type);
                                        if (addedChild == null || addedChild.length == 0)
                                            throw new Exception("Can not add to BOP Line.");
                                        added_new_altbopline = (TCComponentBOPLine) addedChild[0];
                                        added_new_altbopline.save();
                                        altBopLine.save();
                                        // refreshBOMLine(altBopLine);
                                        // refreshBOMLine(added_new_altbopline);

                                        setBOPLineProperties(target_child_bopline, added_new_altbopline);
                                    }
                                    // added_new_altbopline = (TCComponentBOPLine) altBopLine.assignAsChild(productPlantBOMLine, occ_type);
                                    // added_new_altbopline.save();
                                    // altBopLine.save();
                                    //
                                    // setBOPLineProperties(target_child_bopline, added_new_altbopline);
                                }
                            } else {
                                ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                addToChild.add(target_child_bopline.getItem());
                                TCComponent[] addedChild = SDVBOPUtilities.connectObject(altBopLine, addToChild, occ_type);
                                if (addedChild == null || addedChild.length == 0)
                                    throw new Exception("Can not add to BOP Line.");
                                added_new_altbopline = (TCComponentBOPLine) addedChild[0];

                                added_new_altbopline.linkToAppearance(linkedPaths[0], false);
                                added_new_altbopline.save();
                                altBopLine.save();
                                // refreshBOMLine(altBopLine);
                                // refreshBOMLine(added_new_altbopline);

                                setBOPLineProperties(target_child_bopline, added_new_altbopline);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * ���� ���� Activity ���� �����ϴ� �Լ�
     *
     * @param copyFromOpLine
     * @param copyToOpLine
     * @throws Exception
     */
    private void copyMEActivitiesOfOperation(TCComponentBOPLine copyFromOpLine, TCComponentBOPLine copyToOpLine) throws Exception {
        try {
            // long startTime = System.currentTimeMillis();
            String[] timeProperties = registry.getStringArray("CopyActivityProperties.BODY");
            TCComponentMEActivity copyfrom_root_activity = (TCComponentMEActivity) copyFromOpLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
            TCComponent[] copyfrom_child_activities = ActivityUtils.getSortedActivityChildren(copyfrom_root_activity);

            TCComponent activityRootLine = copyToOpLine.getReferenceProperty("bl_me_activity_lines");
            if (activityRootLine != null && activityRootLine instanceof TCComponentCfgActivityLine) {
                TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) activityRootLine);
                for (TCComponent childActivityLine : childLines) {
                    TCComponentMECfgLine parentLine = ((TCComponentCfgActivityLine) childActivityLine).parent();
                    ActivityUtils.removeActivity((TCComponentCfgActivityLine) childActivityLine);
                    parentLine.save();
                }
            }
            ((TCComponentCfgActivityLine) activityRootLine).save();
            // refreshBOMLine(activityRootLine);
            // refreshBOMLine(copyToOpLine);
            // System.out.println("copyMEActivitiesOfOperation Delete time =>" + (System.currentTimeMillis() - startTime));
            // startTime = System.currentTimeMillis();

            HashMap<String, TCComponentBOPLine> toolBOMLineList = getAssignedToolBOMLine(copyToOpLine);

            for (TCComponent copyfrom_child_activity : copyfrom_child_activities) {
                // ������ ��Ƽ��Ƽ�� �����ؾ� �Ѵ�. ��� �Ӽ���� �Բ�.
                TCComponent[] copyto_activities = ActivityUtils.createActivitiesBelow(new TCComponent[] { activityRootLine }, copyfrom_child_activity.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
                TCComponentCfgActivityLine copyto_activity_line = (TCComponentCfgActivityLine) copyto_activities[0];
                TCComponentMEActivity copyto_activity = (TCComponentMEActivity) copyto_activity_line.getUnderlyingComponent();
                // System.out.println("copyMEActivitiesOfOperation createActivity time =>" + (System.currentTimeMillis() - startTime));
                // startTime = System.currentTimeMillis();

                // Activity Time
                HashMap<String, String> propertyMap = new HashMap<String, String>();
                for (String property : timeProperties) {
                    String[] propValue = SoaUtil.marshallTCProperty(copyfrom_child_activity.getTCProperty(property));
                    if (propValue != null && propValue.length > 0 && propValue[0].trim().length() > 0) {
                        if (!propertyMap.containsKey(property))
                            propertyMap.put(property, propValue[0]);
                    }
                }
                // System.out.println("copyMEActivitiesOfOperation ggggetProperty time =>" + (System.currentTimeMillis() - startTime));
                // startTime = System.currentTimeMillis();
                if (propertyMap.size() > 0)
                    copyto_activity.setProperties(propertyMap);
                // System.out.println("copyMEActivitiesOfOperation ssssetProperty time =>" + (System.currentTimeMillis() - startTime));
                // startTime = System.currentTimeMillis();

                // Workers -- (Array)
                String[] workerList = copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORKER).getStringValueArray();
                ArrayList<String> workerArray = new ArrayList<String>();
                String propWorker = "";
                for (String worker : workerList)
                    if (worker != null && worker.trim().length() > 0) {
                        workerArray.add(worker.trim());
                        propWorker += worker + ";";
                    }
                if (workerArray.size() > 0)
                    copyto_activity.setProperty(SDVPropertyConstant.ACTIVITY_WORKER, propWorker.substring(0, propWorker.length() - 1));
                // copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORKER).setStringValueArray(workerArray.toArray(new String[0]));
                else
                    copyto_activity.setProperty(SDVPropertyConstant.ACTIVITY_WORKER, null);
                // Overlay Type
                // copyto_activity.setStringProperty(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE, copyfrom_child_activity.getStringProperty(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE));
                // MECO
                // copyto_activity.setStringProperty(SDVPropertyConstant.ACTIVITY_MECO_NO, copyfrom_child_activity.getStringProperty(SDVPropertyConstant.ACTIVITY_MECO_NO));
                // System.out.println("copyMEActivitiesOfOperation setWorkerProperty time =>" + (System.currentTimeMillis() - startTime));
                // startTime = System.currentTimeMillis();

                // Activity �����ڿ� �Ҵ�
                // ���� ���� METool occtype�� �ش��ϴ� BOMLine�� ��� �����´�. �׸���, �Ҵ�� ���� ����Ʈ�� ID�� ���� ���� ������ ���Ͽ� �� BOMLine�� Tool�� �Ҵ��Ѵ�.
                if (toolBOMLineList != null) {
                    String[] tools = ((TCComponentMEActivity) copyfrom_child_activity).getReferenceToolList(copyFromOpLine);
                    ArrayList<TCComponentBOPLine> reference_tool_list = new ArrayList<TCComponentBOPLine>();
                    for (String tool : tools) {
                        if (toolBOMLineList != null && toolBOMLineList.containsKey(tool))
                            reference_tool_list.add(toolBOMLineList.get(tool));
                    }
                    if (reference_tool_list.size() > 0)
                        copyto_activity.addReferenceTools(copyToOpLine, reference_tool_list.toArray(new TCComponentBOPLine[0]));
                }
                // System.out.println("copyMEActivitiesOfOperation setTOOL time =>" + (System.currentTimeMillis() - startTime));
                // startTime = System.currentTimeMillis();

                copyto_activity.save();
                activityRootLine.save();
                // System.out.println("copyMEActivitiesOfOperation Save time =>" + (System.currentTimeMillis() - startTime));
                // startTime = System.currentTimeMillis();
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * ���� ���� ��� Tool ����Ʈ�� ã�� �����ϴ� �Լ�
     *
     * @param copyToOpLine
     * @return
     * @throws Exception
     */
    private HashMap<String, TCComponentBOPLine> getAssignedToolBOMLine(TCComponentBOPLine copyToOpLine) throws Exception {

        try {
            HashMap<String, TCComponentBOPLine> childToolList = null;
            TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(copyToOpLine);
            for (TCComponentBOMLine operationUnderBOMLine : childs) {
                if (operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_TOOL) || operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE)
                        || operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_SUBSIDIARY) ||
                        // operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_GROUP) ||
                        operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA)) {
                    String itemId = operationUnderBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
                    if (childToolList == null)
                        childToolList = new HashMap<String, TCComponentBOPLine>();
                    if (!childToolList.containsKey(itemId))
                        childToolList.put(itemId, (TCComponentBOPLine) operationUnderBOMLine);
                }
            }

            return childToolList;
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * ���� Structure ���� �ڽĵ��� �����ϴ� �Լ�
     *
     * @param fromBopLine
     * @param copyToBopLine
     */
    private void copyOperationChild(TCComponentBOPLine fromBopLine, TCComponentBOPLine copyToBopLine, String altPrefix) throws Exception {
        try {
            // ���� ���� �ڽĵ��� ��� �����Ѵ�.
            // BOP Line �Ӽ����� �����Ѵ�.
            // ���� �ڽĵ� �� MEConsumed�� ������ PathNode�� �ξ��ش�.
            // ���� �ڽĵ� �� MEWorkArea�� ����(Alternative Plant�� ������ �װ����� ����)�� Plant PathNode�� �ξ��ش�.
            if (fromBopLine.hasChildren()) {
                for (AIFComponentContext childContext : fromBopLine.getChildren()) {
                    if (childContext.getComponent() instanceof TCComponentAppGroupBOPLine)
                        continue;

                    TCComponentBOPLine child_line = (TCComponentBOPLine) childContext.getComponent();
                    String occ_type = child_line.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE);

                    if (occ_type != null && (occ_type.equals(SDVTypeConstant.OCC_TYPE_MECONSUMED) || occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWELDPOINT) || occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA) || occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE))) {
                        TCComponentMEAppearancePathNode[] linkedPaths = child_line.askLinkedAppearances(false);

                        // MEResource�� ���ΰ� Plant���� ���� �� �ƴϱ� ������ ó����.
                        // if (linkedPaths == null || linkedPaths.length == 0)
                        // throw new Exception("Can not find BOM Line information from Product BOM or Plant BOM.");

                        if (occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA) || occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE)) {
                            boolean isLinkValid = false;
                            // WorkArea�� Alternative�� �����ϱ� ������ Alternative�� �ƴ� �Ϲ� WorkArea�� Pathnode ������ ������ ��� �Ѵ�.
                            TCComponentBOMLine plantBOMLine = (TCComponentBOMLine) child_line.getReferenceProperty("bl_me_refline");

                            if (plantBOMLine == null) {
                                try {
                                    plantBOMLine = plantRootLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantRootLine);
                                } catch (TCException ex) {
                                    if (ex.getErrorCode() == 43068) {
                                        isLinkValid = true;
                                    }
                                }
                            }

                            boolean is_alt_plant = false;
                            String alt_prefix = null;
                            TCComponentBOMLine productPlantBOMLine = null;

                            if (occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA) && !isLinkValid) {
                                plantBOMLine = plantRootLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantRootLine);
                                String plant_item_type = plantBOMLine.getItem().getType();

                                // �Ϲݰ��� �� �������� ������ ����
                                if (plant_item_type.equals(SDVTypeConstant.PLANT_LINE_ITEM) || plant_item_type.equals(SDVTypeConstant.PLANT_OPAREA_ITEM) || plant_item_type.equals(SDVTypeConstant.PLANT_STATION_ITEM)) {
                                    if (plantBOMLine.getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_IS_ALTBOP))
                                        is_alt_plant = plantBOMLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
                                    if (plantBOMLine.getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_ALT_PREFIX))
                                        alt_prefix = plantBOMLine.getItemRevision().getStringProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX);
                                    if (!is_alt_plant || (is_alt_plant && alt_prefix != null && !alt_prefix.equals(altPrefix))) {
                                        // ������������ Alternative OPArea ã��
                                        productPlantBOMLine = getAlternativeBOPLineInParent(plantBOMLine.parent(), plantBOMLine, altPrefix);
                                        if (productPlantBOMLine == null) {
                                            // �������� ��ã���� ���ο��� Alternative ���� ã��
                                            productPlantBOMLine = getAlternativeBOPLineInParent(plantBOMLine.parent().parent(), plantBOMLine.parent(), altPrefix);

                                            // Alternative ������������ Alternative OPArea ã��
                                            if (productPlantBOMLine != null)
                                                productPlantBOMLine = getAlternativeBOPLineInParent(productPlantBOMLine, plantBOMLine, altPrefix);
                                        }

                                        if (productPlantBOMLine != null) {
                                            ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                            addToChild.add(productPlantBOMLine);
                                            TCComponent[] addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
                                            if (addedChild == null || addedChild.length == 0)
                                                throw new Exception("Can not add to BOP Line.");
                                            TCComponentBOPLine added_new_altbopline = (TCComponentBOPLine) addedChild[0];
                                            added_new_altbopline.save();
                                            copyToBopLine.save();
                                            // refreshBOMLine(copyToBopLine);
                                            // refreshBOMLine(added_new_altbopline);

                                            setBOPLineProperties(child_line, (TCComponentBOPLine) added_new_altbopline);

                                            // // productPlantBOMLine = plantBOMLine;
                                            // TCComponentBOPLine plantBOPLine = (TCComponentBOPLine) copyToBopLine.assignAsChild(productPlantBOMLine, SDVTypeConstant.OCC_TYPE_MEWORKAREA);
                                            // plantBOPLine.save();
                                            // setBOPLineProperties(child_line, plantBOPLine);
                                        }
                                    } else {
                                        ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                        addToChild.add(child_line.getItem());
                                        TCComponent[] addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
                                        if (addedChild == null || addedChild.length == 0)
                                            throw new Exception("Can not add to BOP Line.");
                                        TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
                                        // TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);

                                        added_bop_line.linkToAppearance(linkedPaths[0], false);
                                        added_bop_line.save();
                                        copyToBopLine.save();
                                        // refreshBOMLine(copyToBopLine);
                                        // refreshBOMLine(added_bop_line);

                                        setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
                                    }
                                } else {
                                    // �������� ������ Plant������ �ƴ� Plant���� ���� ������ ���� ������ ������ üũ�ؼ� ó���ؾ� �Ѵ�.
                                    // ������ Alternative���� üũ�ϰ� �� ������ �������� ������ Product Plant������ �˻�����.

                                    // OPArea�� Alt���� üũ
                                    // ������ Alternative���� üũ�ϰ� �� ������ �������� ������ Product Plant������ �˻�����.
                                    if (plantBOMLine.parent().getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_IS_ALTBOP))
                                        is_alt_plant = plantBOMLine.parent().getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
                                    if (plantBOMLine.getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_ALT_PREFIX))
                                        alt_prefix = plantBOMLine.getItemRevision().getStringProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX);
                                    if (!is_alt_plant || (is_alt_plant && alt_prefix != null && !alt_prefix.equals(altPrefix))) {
                                        // ������ Alternative�� �ƴϸ� �� �������� Alternative�� ã�´�.
                                        productPlantBOMLine = getAlternativeBOPLineInParent(plantBOMLine.parent().parent(), plantBOMLine.parent(), altPrefix);
                                        if (productPlantBOMLine == null) {
                                            productPlantBOMLine = getAlternativeBOPLineInParent(plantBOMLine.parent().parent().parent(), plantBOMLine.parent().parent(), altPrefix);

                                            if (productPlantBOMLine != null)
                                                productPlantBOMLine = getAlternativeBOPLineInParent(productPlantBOMLine, plantBOMLine.parent(), altPrefix);
                                        }
                                        if (productPlantBOMLine != null) {
                                            // ������ Alternative Plant ���� �ڱ� Plant������ ã�´�.
                                            productPlantBOMLine = getAlternativeBOPLineInParent(productPlantBOMLine, plantBOMLine, "");

                                            if (productPlantBOMLine != null) {
                                                ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                                addToChild.add(productPlantBOMLine);
                                                TCComponent[] addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
                                                if (addedChild == null || addedChild.length == 0)
                                                    throw new Exception("Can not add to BOP Line.");
                                                TCComponentBOPLine added_new_altbopline = (TCComponentBOPLine) addedChild[0];
                                                added_new_altbopline.save();
                                                copyToBopLine.save();
                                                // refreshBOMLine(copyToBopLine);
                                                // refreshBOMLine(added_new_altbopline);

                                                setBOPLineProperties(child_line, added_new_altbopline);
                                            }
                                            // TCComponentBOPLine plantBOPLine = (TCComponentBOPLine) copyToBopLine.assignAsChild(productPlantBOMLine, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
                                            // plantBOPLine.save();
                                            //
                                            // setBOPLineProperties(child_line, plantBOPLine);
                                        }
                                    } else {
                                        ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                        addToChild.add(child_line.getItem());
                                        TCComponent[] addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
                                        if (addedChild == null || addedChild.length == 0)
                                            throw new Exception("Can not add to BOP Line.");
                                        TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];

                                        added_bop_line.linkToAppearance(linkedPaths[0], false);
                                        added_bop_line.save();
                                        copyToBopLine.save();
                                        // refreshBOMLine(copyToBopLine);
                                        // refreshBOMLine(added_bop_line);

                                        setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
                                    }
                                }
                            } else {
                                if (plantBOMLine != null) {
                                    // MEResource�� Plant�� ����� �� �� ���
                                    plantBOMLine = plantRootLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantRootLine);

                                    // MEResource�� ������ Alternative���� üũ�ϰ� �� ������ �������� ������ Product Plant������ �˻�����.
                                    if (plantBOMLine.parent().getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_IS_ALTBOP))
                                        is_alt_plant = plantBOMLine.parent().getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
                                    if (plantBOMLine.getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_ALT_PREFIX))
                                        alt_prefix = plantBOMLine.getItemRevision().getStringProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX);
                                    if (!is_alt_plant || (is_alt_plant && alt_prefix != null && !alt_prefix.equals(altPrefix))) {
                                        // ������ Alternative�� �ƴϸ� �� �������� Alternative�� ã�´�.
                                        productPlantBOMLine = getAlternativeBOPLineInParent(plantBOMLine.parent().parent(), plantBOMLine.parent(), altPrefix);
                                        if (productPlantBOMLine == null) {
                                            productPlantBOMLine = getAlternativeBOPLineInParent(plantBOMLine.parent().parent().parent(), plantBOMLine.parent().parent(), altPrefix);

                                            if (productPlantBOMLine != null)
                                                productPlantBOMLine = getAlternativeBOPLineInParent(productPlantBOMLine, plantBOMLine.parent(), altPrefix);
                                        }
                                        if (productPlantBOMLine != null) {
                                            // ������ Alternative Plant ���� �ڱ� Plant������ ã�´�.
                                            productPlantBOMLine = getAlternativeBOPLineInParent(productPlantBOMLine, plantBOMLine, "");

                                            if (productPlantBOMLine != null) {
                                                ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                                addToChild.add(productPlantBOMLine);
                                                TCComponent[] addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
                                                if (addedChild == null || addedChild.length == 0)
                                                    throw new Exception("Can not add to BOP Line.");
                                                TCComponentBOPLine added_new_altbopline = (TCComponentBOPLine) addedChild[0];
                                                added_new_altbopline.save();
                                                copyToBopLine.save();
                                                // refreshBOMLine(copyToBopLine);
                                                // refreshBOMLine(added_new_altbopline);

                                                setBOPLineProperties(child_line, added_new_altbopline);
                                            }
                                            // TCComponentBOPLine plantBOPLine = (TCComponentBOPLine) copyToBopLine.assignAsChild(productPlantBOMLine, SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE);
                                            // plantBOPLine.save();
                                            //
                                            // setBOPLineProperties(child_line, plantBOPLine);
                                        }
                                    }
                                } else {
                                    // �Ϲ� MEResource�� �׳� �߰��Ѵ�.
                                    ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                                    addToChild.add(child_line.getItem());
                                    TCComponent[] addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
                                    if (addedChild == null || addedChild.length == 0)
                                        throw new Exception("Can not add to BOP Line.");
                                    TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];

                                    added_bop_line.save();
                                    copyToBopLine.save();
                                    // refreshBOMLine(copyToBopLine);
                                    // refreshBOMLine(added_bop_line);

                                    if (linkedPaths != null && linkedPaths.length > 0)
                                        added_bop_line.linkToAppearance(linkedPaths[0], false);
                                    setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
                                }
                            }
                        } else {
                            ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                            addToChild.add(child_line.getItem());
                            TCComponent[] addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
                            if (addedChild == null || addedChild.length == 0)
                                throw new Exception("Can not add to BOP Line.");
                            TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];

                            added_bop_line.linkToAppearance(linkedPaths[0], false);
                            added_bop_line.save();
                            copyToBopLine.save();
                            // refreshBOMLine(copyToBopLine);
                            // refreshBOMLine(added_bop_line);

                            setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
                        }
                    } else {
                        // �Ϲ� ���ҽ���..
                        ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
                        addToChild.add(child_line.getItem());
                        TCComponent[] addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
                        if (addedChild == null || addedChild.length == 0)
                            throw new Exception("Can not add to BOP Line.");
                        TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];

                        added_bop_line.save();
                        copyToBopLine.save();
                        // refreshBOMLine(copyToBopLine);
                        // refreshBOMLine(added_bop_line);

                        setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
                    }
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * �־��� �����ο��� Prefix�� �����ϴ� Alternative BOP�� ã�� �����ϴ� �Լ�
     *
     * @param parentLine
     * @param targetLine
     * @param altPrefix
     * @return
     * @throws Exception
     */
    private TCComponentBOMLine getAlternativeBOPLineInParent(TCComponentBOMLine parentLine, TCComponentBOMLine targetLine, String altPrefix) throws Exception {
        try {
            String item_id = targetLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            String check_id = (altPrefix != null && altPrefix.length() > 0 ? altPrefix + "-" : "") + item_id;

            for (AIFComponentContext childContext : parentLine.getChildren()) {
                if (childContext.getComponent() instanceof TCComponentAppGroupBOPLine)
                    continue;

                TCComponentBOMLine childLine = (TCComponentBOMLine) childContext.getComponent();
                String child_id = childLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                if (child_id.equals(check_id))
                    return childLine;
            }

            return null;
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * BOP Line�� ������ �����ϴ� �Լ�
     *
     * @param target_bopline
     * @param alt_bopline
     * @throws Exception
     */
    private void setBOPLineProperties(TCComponentBOPLine targetBopLine, TCComponentBOPLine altBopLine) throws Exception {
        try {
            // long startTime = System.currentTimeMillis();
            String[] bopLineProperties = registry.getStringArray("CopyBOPLineProperties");
            // refreshBOMLine(altBopLine);

            if (bopLineProperties == null) {
                System.out.println("CopyToAlternativeBOPActionOperation.setBOPLineProperties() = Copy Property is null.");
                return;
            }
            
            if(altBopLine!=null){
                // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
            	BOPLineUtility.updateLineToOperationAbsOccId(altBopLine);
            }

            HashMap<String, String> linePropertyMap = new HashMap<String, String>();
            for (String bopLineProperty : bopLineProperties) {
                if ((bopLineProperty.equals("bl_variant_condition")) || (bopLineProperty.equals("bl_condition_tag")) || (bopLineProperty.equals("bl_formula"))) {
                    // long cstartTime = System.currentTimeMillis();
                    try {
                        if (!targetBopLine.getProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.OCC_TYPE_MECONSUMED)) {
                            TCComponent localTCComponent = targetBopLine.getReferenceProperty("bl_condition_tag");
                            Object localObject1;
                            if (localTCComponent == null) {
                                localObject1 = targetBopLine.getProperty("bl_variant_condition");
                                Object setToObject = altBopLine.getProperty("bl_variant_condition");
                                if (localObject1 != null && setToObject != null && !setToObject.equals(localObject1))
                                    variantService.setLineMvlCondition(altBopLine, (String) localObject1);
                            } else {
                                TCComponent toTCComponent = altBopLine.getReferenceProperty("bl_condition_tag");
                                if (toTCComponent != null && !localTCComponent.equals(toTCComponent)) {
                                    localObject1 = VariantCondition.create(localTCComponent, altBopLine.window());
                                    altBopLine.setReferenceProperty("bl_condition_tag", ((VariantCondition) localObject1).toCondition());
                                }
                            }
                            altBopLine.save();
                        }
                    } catch (Exception ex) {
                        throw ex;
                    }
                    // System.out.println("setBOPLineProperty condition set time =>" + (System.currentTimeMillis() - cstartTime));
                } else {
                    if (targetBopLine.isValidPropertyName(bopLineProperty) && altBopLine.isValidPropertyName(bopLineProperty)) {
                        String[] targetValue = SoaUtil.marshallTCProperty(targetBopLine.getTCProperty(bopLineProperty));
                        String[] altValue = SoaUtil.marshallTCProperty(altBopLine.getTCProperty(bopLineProperty));
                        if (targetValue == null || altValue == null || targetValue.length == 0 || altValue.length == 0 || !targetValue[0].equals(altValue[0]))
                            if (!linePropertyMap.containsKey(bopLineProperty))
                                linePropertyMap.put(bopLineProperty, (targetValue == null || targetValue.length == 0 ? null : targetValue[0]));
                    }
                }
            }
            // System.out.println("setBOPLineProperty check property time =>" + (System.currentTimeMillis() - startTime));
            // startTime = System.currentTimeMillis();

            if (linePropertyMap.size() > 0)
                altBopLine.setProperties(linePropertyMap);
            // System.out.println("setBOPLineProperty set time =>" + (System.currentTimeMillis() - startTime));
            // startTime = System.currentTimeMillis();

            altBopLine.save();
            // refreshBOMLine(altBopLine);
            // System.out.println("setBOPLineProperty save time =>" + (System.currentTimeMillis() - startTime));
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * BOP �׸���� ������ �����ϴ� �Լ�
     *
     * @param setToRevision
     * @param fromRevision
     * @param altPrefix
     * @throws Exception
     */
    private void setProperties(TCComponentItemRevision setToRevision, TCComponentItemRevision fromRevision, String altPrefix) throws Exception {
        try
        {
//long startTime = System.currentTimeMillis();
            String []itemProperties = null;
            String []revProperties = null;

//          setToRevision.refresh();
            if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV))
            {
                itemProperties = registry.getStringArray("CopyShopItemProperties.BODY");
                revProperties = registry.getStringArray("CopyShopRevisionProperties.BODY");
            }
            else if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV))
            {
                revProperties = registry.getStringArray("CopyLineRevisionProperties.BODY");
            }
            else if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV))
            {
//              revProperties = registry.getStringArray("CopyStationRevisionProperties.BODY");

                if (setToRevision.isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) && fromRevision.isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) &&
                        ! setToRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME).equals(fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME)))
                        setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

                    if (setToRevision.isValidPropertyName(SDVPropertyConstant.STATION_ALT_PREFIX) && fromRevision.isValidPropertyName(SDVPropertyConstant.STATION_ALT_PREFIX))
                        setToRevision.setProperty(SDVPropertyConstant.STATION_ALT_PREFIX, altPrefix);
                    if (setToRevision.isValidPropertyName(SDVPropertyConstant.STATION_IS_ALTBOP) && fromRevision.isValidPropertyName(SDVPropertyConstant.STATION_IS_ALTBOP))
                        setToRevision.setLogicalProperty(SDVPropertyConstant.STATION_IS_ALTBOP, true);

                    if (setToRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS) && fromRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS))
                    {
                        // ���� EXT_DECESSORS �� ���� �Ǿ� ������ ����Ʈ�� �����Ѵ�
                        TCComponent[] altDecessorsStations = setToRevision.getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
                        for (TCComponent altDecessorsStation : altDecessorsStations) {
                            updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, (TCComponentItemRevision)altDecessorsStation, DELETE);
                        }
                        // ���� EXT_DECESSORS �� ���� �Ѵ�
                        TCComponent[] decessorsStations = fromRevision.getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
                        if (decessorsStations != null && decessorsStations.length > 0)
                        {
                            for (TCComponent decessorsStation : decessorsStations) {
                                String extDecessorsID = decessorsStation.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                                TCComponentItem extDecessorsItem = SDVBOPUtilities.FindItem(altPrefix + "-" + extDecessorsID, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                                if (extDecessorsItem != null) {
                                    TCComponentItemRevision extDecessorsItemrev = extDecessorsItem.getLatestItemRevision();
                                    updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, extDecessorsItemrev, ADD);
                                }
                            }
                        }
                    }

            }
            else if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV) ||
                      fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV) ||
                      fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV))
            {
                itemProperties = registry.getStringArray("CopyOperationItemProperties.BODY");
                revProperties = registry.getStringArray("CopyOperationRevisionProperties.BODY");
            }
            else if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV))
            {
                if (setToRevision.isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) && fromRevision.isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) &&
                    ! setToRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME).equals(fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME)))
                    setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

                if (setToRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX) && fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX))
                    setToRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, altPrefix);
                if (setToRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP) && fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP))
                    setToRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, true);

                if (setToRevision.isValidPropertyName(SDVPropertyConstant.WELDOP_REV_TARGET_OP) && fromRevision.isValidPropertyName(SDVPropertyConstant.WELDOP_REV_TARGET_OP))
                {
                    TCComponent targetOpRevision = fromRevision.getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
                    if (targetOpRevision != null)
                    {
                        String item_id = targetOpRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

                        TCComponentItem targetAltOP = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM, altPrefix + "-" + item_id);
                        if (targetAltOP == null)
                            throw new Exception("WeldOperation's target Operation was not found[" + item_id + "].");

                        setToRevision.setReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP, targetAltOP.getLatestItemRevision());
                    }
                }

                // MECO ������ ������.
                if (setToRevision.isValidPropertyName(SDVPropertyConstant.ITEM_REV_MECO_NO) && fromRevision.isValidPropertyName(SDVPropertyConstant.ITEM_REV_MECO_NO))
                    if (setToRevision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO) != null)
                        setToRevision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, null);

//              refreshBOMLine(setToRevision);
            }

            if (itemProperties != null)
            {
//              setToRevision.getItem().setTCProperties(fromRevision.getItem().getTCProperties(itemProperties));
                HashMap<String, String> propertyMap = new HashMap<String, String>();
                for (String property : itemProperties)
                {
                    if (setToRevision.getItem().isValidPropertyName(property) && fromRevision.getItem().isValidPropertyName(property))
                    {
                        String []propValue = SoaUtil.marshallTCProperty(fromRevision.getItem().getTCProperty(property));
                        String []setToValue = SoaUtil.marshallTCProperty(setToRevision.getItem().getTCProperty(property));
                        if (propValue == null || propValue.length == 0 || setToValue == null || setToValue.length == 0 || ! propValue[0].equals(setToValue[0]))
                        {
                            if (! propertyMap.containsKey(property))
                                propertyMap.put(property, (propValue == null || propValue.length == 0 ? null : propValue[0]));
                        }
                    }
                }
                if (propertyMap.size() > 0)
                    setToRevision.getItem().setProperties(propertyMap);

//              refreshBOMLine(setToRevision.getItem());
//System.out.println("Item setProperty time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();
            }
            if (revProperties != null)
            {
                HashMap<String, String> setPropertyList = new HashMap<String, String>();
                for (String property : revProperties)
                {
                    if (setToRevision.isValidPropertyName(property) && fromRevision.isValidPropertyName(property))
                    {
                        if (property.equals(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO))
                        {
                            ArrayList<String> dwgList = new ArrayList<String>();
                            String[] dwgNos = fromRevision.getTCProperty(property).getStringArrayValue();
                            for (String dwgNo : dwgNos)
                            {
                                if (dwgNo != null && dwgNo.trim().length() > 0)
                                    dwgList.add(dwgNo);
                            }
                            if (dwgList.size() > 0)
                                setToRevision.getTCProperty(property).setStringValueArray(dwgList.toArray(new String[0]));
                            else
                                setToRevision.setProperty(property, null);
                        }
                        else
                        {
                            String[] propValue = SoaUtil.marshallTCProperty(fromRevision.getTCProperty(property));
                            String[] setToValue = SoaUtil.marshallTCProperty(setToRevision.getTCProperty(property));
                            if (propValue == null || propValue.length == 0 || setToValue == null || setToValue.length == 0 || ! propValue[0].equals(setToValue[0]))
                            {
                                if (! setPropertyList.containsKey(property))
                                    setPropertyList.put(property, (propValue == null || propValue.length == 0 ? null : propValue[0]));
                            }
//                          setToRevision.setTCProperty(fromRevision.getTCProperty(property));
                        }
                    }
                }
//System.out.println("Revision getProperty time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

                if (setPropertyList.size() > 0)
                    setToRevision.setProperties(setPropertyList);
//System.out.println("Revision setProperty time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

                if (setToRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX) && fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX))
                    setToRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, altPrefix);
                if (setToRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP) && fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP))
                    setToRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, true);
//System.out.println("Revision set Alt Property time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

                // MECO ������ ������.
                if (setToRevision.isValidPropertyName(SDVPropertyConstant.ITEM_REV_MECO_NO) && fromRevision.isValidPropertyName(SDVPropertyConstant.ITEM_REV_MECO_NO))
                {
                    if (setToRevision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO) != null)
                        setToRevision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, null);
                }

//              refreshBOMLine(setToRevision);
//System.out.println("Revision set MECO Property time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /**
     * BOMLine refresh
     *
     * @method refreshBOMLine
     * @date 2013. 12. 12.
     * @param
     * @exception
     * @return void
     * @throws
     * @see
     */
    private void refreshBOMLine(Object tcComponent) throws Exception {
        // Activity Refresh
        if (tcComponent instanceof TCComponentMECfgLine) {
            TCComponentMECfgLine tcComponentMECfgLine = (TCComponentMECfgLine) tcComponent;
            tcComponentMECfgLine.clearCache();
            tcComponentMECfgLine.window().fireChangeEvent();
            tcComponentMECfgLine.refresh();
        } else if (tcComponent instanceof TCComponentCfgAttachmentLine) {
            TCComponentCfgAttachmentLine tcComponentCfgAttachmentLine = (TCComponentCfgAttachmentLine) tcComponent;
            tcComponentCfgAttachmentLine.clearCache();
            tcComponentCfgAttachmentLine.window().fireChangeEvent();
            tcComponentCfgAttachmentLine.refresh();
        }
        // BOMLine Refresh
        else if (tcComponent instanceof TCComponentBOMLine) {
            TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) tcComponent;
            tcComponentBOMLine.clearCache();
            tcComponentBOMLine.refresh();
            // tcComponentBOMLine.window().clearCache();
            // tcComponentBOMLine.window().refresh();
            // tcComponentBOMLine.window().newIrfWhereConfigured(tcComponentBOMLine.getItemRevision());
            // tcComponentBOMLine.window().fireComponentChangeEvent();
        } else if (tcComponent instanceof TCComponentItemRevision) {
            ((TCComponentItemRevision) tcComponent).clearCache();
            ((TCComponentItemRevision) tcComponent).refresh();
            // ((TCComponentItemRevision) tcComponent).fireComponentChangeEvent();
        } else if (tcComponent instanceof TCComponentItem) {
            ((TCComponentItem) tcComponent).clearCache();
            ((TCComponentItem) tcComponent).refresh();
            // ((TCComponentItem) tcComponent).fireComponentChangeEvent();
        }
    }

    private void updateReferenceArrayProperty(TCComponentItemRevision decessorItemRev, String propertyName, TCComponentItemRevision sucessorItemRev, int mode) throws TCException {
        if(decessorItemRev != null){
            TCProperty property = decessorItemRev.getTCProperty(propertyName);
            if(property == null) return;
            //���������� �ִ��� ����Ȯ��
            if(decessorItemRev.isModifiable(propertyName)){
                TCComponent [] values = property.getReferenceValueArray();
                if(values == null) values = new TCComponent[0];
                switch (mode) {
                    case ADD:       values = (TCComponent[]) ArrayUtils.add(values, sucessorItemRev);
                        break;
                    case DELETE :   values = (TCComponent[]) ArrayUtils.removeElement(values, sucessorItemRev);
                        break;
                }
                property.setReferenceValueArray(values);

                //Save�� �ϸ� Lock�� �߻��Ͽ� save()�� ���� ����
                //decessorItemRev.save();
            }
        }
    }

    /**
     *  ALTBOP�� Decessors �� �����Ѵ�
     * @param bopLine
     * @param altBopShopLine
     * @param altPrefix
     * @throws Exception
     */
    private void updatePertInfo(TCComponentBOPLine bopLine, TCComponentBOPLine altBopShopLine, String altPrefix) throws Exception{

        TCComponent[] decessors = bopLine.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);

        // ��� BOP �� ���ǵ� PreDecessors �� ALTBOP �� �����Ѵ�
        if (decessors != null && decessors.length > 0) {
            // ALTBOP(Line)�� ã�´�
            String altBopID = altPrefix + "-" + bopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            TCComponentBOPLine altLine = findAltBopLine(altBopID, altBopShopLine);

            // ALTBOP(Line) decessors �� ã�´�
            List<TCComponentBOMLine> altDecessors = findAltDecessors(decessors, altPrefix, altBopShopLine);
            if (altDecessors.size() > 0) {
                removeAltBopLineDecessors(altLine);
                altLine.addPredecessors(altDecessors);
            }
        }

        // ������ Decessors �� ���� �Ѵ�
       AIFComponentContext[] stationList = bopLine.getChildren();
       for (AIFComponentContext station : stationList) {
           TCComponentBOPLine stationBopLine = (TCComponentBOPLine)station.getComponent();
           TCComponent[] stationDecessors = stationBopLine.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);

           // ��� BOP �� ���ǵ� PreDecessors �� ALTBOP �� �����Ѵ�
           if (stationDecessors != null && stationDecessors.length > 0) {
               // ALTBOP(Station)�� ã�´�
               String altBopID = altPrefix + "-" + stationBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
               TCComponentBOPLine altStationLine = findAltBopLine(altBopID, altBopShopLine);

               // ALTBOP(Line) stationDecessors �� ã�´�
               List<TCComponentBOMLine> altDecessors = findAltDecessors(stationDecessors, altPrefix, altBopShopLine);
               if (altDecessors.size() > 0) {
                   removeAltBopLineDecessors(altStationLine);
                   altStationLine.addPredecessors(altDecessors);
               }
           }
       }
    }

    /**
     * Decessors �� ���ǵ� BOPLine �� ALT �׸����� �ٲ�ġ�� �Ѵ�.
     * @param decessors
     * @param prefix
     * @param altBopLine
     * @return
     * @throws TCException
     */
    private List<TCComponentBOMLine> findAltDecessors( TCComponent[] decessors, String prefix, TCComponentBOPLine altBopLine) throws TCException{
        List<TCComponentBOMLine> decessorsList = new ArrayList<TCComponentBOMLine>();
        for (TCComponent decessor : decessors) {
            String altID = prefix + "-" + decessor.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            decessorsList.add((TCComponentBOMLine)findAltBopLine(altID, altBopLine));
        }
        return decessorsList;
    }

    /**
     *   altBop�� ���ǵ� PreDecessors �� �����Ѵ�
     * @param altBopLine
     * @throws TCException
     */
    private void removeAltBopLineDecessors(TCComponentBOPLine altBopLine) throws TCException{
        TCComponent[] altDecessors = altBopLine.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);
        // ���� ALTBOP�� ���ǵ� PreDecessors �� �����Ѵ�
        if (altDecessors != null && altDecessors.length > 0) {
            for (TCComponent altDecessor : altDecessors) {
                altBopLine.removePredecessor((TCComponentBOMLine)altDecessor);
            }
        }
    }

    /**
     *  ���  BOPLine �� ��Ī�Ǵ� ALTBOPLine �� ã�ƿ´�
     * @param prefix
     * @param bopLine
     * @param altBopLine
     * @return
     * @throws TCException
     */
    private TCComponentBOPLine findAltBopLine(String altBopID, TCComponentBOPLine altBopLine) throws TCException{
        TCComponentBOPLine altBop = null;
        if (altBopLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || altBopLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            AIFComponentContext[] altBopChilds = altBopLine.getChildren();
            for (AIFComponentContext altBopChild : altBopChilds) {
                altBop = (TCComponentBOPLine)altBopChild.getComponent();
                if (altBop.getProperty(SDVPropertyConstant.BL_ITEM_ID).equals(altBopID)) {
                    return altBop;
                }
                altBop = findAltBopLine(altBopID, altBop);
                if (altBop != null) {
                    return altBop;
                }
            }
        }
        return altBop;
    }

}
