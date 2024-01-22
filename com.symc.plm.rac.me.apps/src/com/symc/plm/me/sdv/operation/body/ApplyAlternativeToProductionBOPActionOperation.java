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
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
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
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
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
public class ApplyAlternativeToProductionBOPActionOperation extends AbstractSDVActionOperation {
	private TCSession session;
	private Registry registry = Registry.getRegistry(ApplyAlternativeToProductionBOPActionOperation.class);
	private TCComponentBOPLine bop_shop_bopline = null;
	private TCComponentBOMLine plantLine = null;
	private final int ADD = 1;
    private final int DELETE = 0;

	/**
	 * @param actionId
	 * @param ownerId
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(int actionId, String ownerId, IDataSet dataset) {
		super(actionId, ownerId, dataset);
	}

	/**
	 * @param actionId
	 * @param operationId
	 * @param ownerId
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(int actionId, String operationId, String ownerId, IDataSet dataset) {
		super(actionId, operationId, ownerId, dataset);
	}

	/**
	 * @param actionId
	 * @param ownerId
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(String actionId, String ownerId, IDataSet dataset) {
		super(actionId, ownerId, dataset);
	}

	/**
	 * @param actionId
	 * @param ownerId
	 * @param parameters
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
		super(actionId, ownerId, parameters, dataset);
	}

	/**
	 * @param actionId
	 * @param ownerId
	 * @param parameters
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
		super(actionId, ownerId, parameters, dataset);
	}

	/**
	 * @param operationId
	 * @param actionId
	 * @param ownerId
	 * @param parameters
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(String operationId, String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
		super(operationId, actionId, ownerId, parameters, dataset);
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
	 */
	@Override
	public void startOperation(String commandId) {
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVOperation#endOperation()
	 */
	@Override
	public void endOperation() {
	}

	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
		session = CustomUtil.getTCSession();

		IDataSet dataSet = getDataSet();

		try
		{
			Object alt_bop_obj = dataSet.getValue("applyAlternativeToProductionBOPView", SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
			Object target_mproduct_obj = dataSet.getValue("applyAlternativeToProductionBOPView", SDVTypeConstant.EBOM_MPRODUCT);
			Object meco_obj = dataSet.getValue("applyAlternativeToProductionBOPView", SDVTypeConstant.MECO_ITEM);

	    	if (alt_bop_obj == null || (! (alt_bop_obj instanceof TCComponentBOPLine)))
	    		throw new Exception("[" + registry.getString("ApplyAltToProduct.AltShopRequired.MESSAGE", "Alternative BOP") + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."));

	    	if (meco_obj == null || meco_obj.toString().trim().length() == 0)
	    		throw new Exception("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."));

	    	if (target_mproduct_obj == null || (! (target_mproduct_obj instanceof TCComponentItemRevision)))
	    		throw new Exception("[" + registry.getString("ApplyAltToProduct.ProductShopRequired.MESSAGE", "M-Product") + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."));

			// ��������� Alternative BOP�� Shop�� �����´�.
			TCComponentItemRevision alt_shop_rev = null;


			String target_obj_type = ((TCComponentBOPLine) alt_bop_obj).getItem().getType();
			if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
				alt_shop_rev = ((TCComponentBOPLine) alt_bop_obj).getItemRevision();
			else if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
				alt_shop_rev = ((TCComponentBOPLine) alt_bop_obj).parent().getItemRevision();
			else if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
				alt_shop_rev = ((TCComponentBOPLine) alt_bop_obj).parent().parent().getItemRevision();
			else
				throw new Exception("Not support item type.");

            String alt_prefix = alt_shop_rev.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
	    	TCComponent workarea_rev = alt_shop_rev.getRelatedComponent(SDVTypeConstant.MFG_WORKAREA);
			plantLine = CustomUtil.getBomline((TCComponentItemRevision) workarea_rev, session);

			// ��������� Shop�� �����ϴ��� ���� üũ�Ѵ�.
			String alt_shop_code = alt_shop_rev.getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
			String product_code = "P".concat(((TCComponentItemRevision) target_mproduct_obj).getProperty(SDVPropertyConstant.ITEM_ITEM_ID).substring(1));
			String vehicle_code = null;
			String bop_shop_code = SDVPropertyConstant.ITEM_ID_PREFIX + "-"  + alt_shop_code + "-"  + product_code;//alt_shop_code.substring(alt_shop_prefix.length() + 1);
			TCComponentItem bop_shop_item = SDVBOPUtilities.FindItem(bop_shop_code, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
			boolean is_shop_created = false;

			if (target_mproduct_obj != null)
			{
				TCComponentItem sproductItem = SDVBOPUtilities.FindItem(product_code, SDVTypeConstant.EBOM_PRODUCT_ITEM);
				if (sproductItem == null)
					throw new NullPointerException(registry.getString("ProjectItemIsNull.MESSAGE", "Project Item not found of Product Item[%s].").replace("%s", product_code));

				TCComponentItem projectItem = SDVBOPUtilities.FindItem(sproductItem.getLatestItemRevision().getProperty(SDVPropertyConstant.S7_PROJECT_CODE), SDVTypeConstant.EBOM_PROJECT_ITEM);
				if (projectItem == null)
					throw new NullPointerException(registry.getString("ProjectItemIsNull.MESSAGE", "Project Item not found of Product Item[%s].").replace("%s", product_code));

				vehicle_code = projectItem.getLatestItemRevision().getProperty(SDVPropertyConstant.S7_VEHICLE_NO);
			}

			if (bop_shop_item == null && (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)))
				throw new NullPointerException(registry.getString("ApplyProductBOPItemNotFound.MESSAGE", "Apply Shop Item can not find. contact to BOP Admin."));
			else if (bop_shop_item == null)
			{
				// ������ �������� �� ������ ǥ���Ѵ�.
				if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
					throw new Exception("Can not find BOP Shop Item.[" + bop_shop_code + "]");

				// Shop�� �����ϰ� �ؾ� �Ѵ�.
				bop_shop_item = SDVBOPUtilities.createItem(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, bop_shop_code, SDVPropertyConstant.ITEM_REV_ID_ROOT, alt_shop_rev.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), alt_shop_rev.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
//				bop_shop_item = alt_shop_rev.saveAsItem(bop_shop_code, SDVPropertyConstant.ITEM_REV_ID_ROOT);
				is_shop_created = true;
			}

			TCComponentItemRevision bop_shop_revision = bop_shop_item.getLatestItemRevision();
			TCComponentItemRevision shopPreRevision = null;
			if (! target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
			{
				if (CustomUtil.isReleased(bop_shop_revision))
				{
					// ���� ������ ��ŷ�� �ƴϸ� ������ ǥ���ؾ� �ϴ°� �ƴ��� Ȯ���� ��.
					String new_rev_id = bop_shop_item.getNewRev();
					shopPreRevision = bop_shop_item.getLatestItemRevision();
					bop_shop_revision = bop_shop_item.revise(new_rev_id, bop_shop_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), bop_shop_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
				}
			}

			TCComponent revMECO = null;
			if (! target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM) && ! target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
			{
				revMECO = bop_shop_revision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
				if (revMECO == null)
				{
					// ���࿡ �������� �̹� �����Ǿ� ������ MECO������ ��� ���� �����ؾ� ��.
					bop_shop_revision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, (TCComponent) meco_obj);

					if (shopPreRevision != null)
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_PROBLEM_ITEM, shopPreRevision);

					if (! ((TCComponentChangeItemRevision) meco_obj).isRelationSet(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_shop_revision))
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_shop_revision);
				}

				// Shop ������ �����Ѵ�.
				setProperties(bop_shop_revision, alt_shop_rev, product_code, vehicle_code);
			}

			// ��� Shop�� BOPLine�� �����.
			bop_shop_bopline = CustomUtil.getBopline(bop_shop_revision, session);

			// ������ Shop�� �ɼ��� ��������.
			if (is_shop_created)
				copyTopOptions((TCComponentBOPLine) alt_bop_obj, bop_shop_bopline);

			if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
			{
				// Alternative BOP���� ������ �׸��� Shop�̸� ���� ����
				if (((TCComponentBOPLine) alt_bop_obj).hasChildren())
					makeChildBOPLine(alt_shop_code, (TCComponentBOPLine) alt_bop_obj, bop_shop_bopline, product_code, vehicle_code, (TCComponent) meco_obj, alt_prefix);
			}

			// Alternative BOP���� ������ �׸��� Line�� ��
			TCComponentBOPLine bop_line_bopline = null;
			if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
			{
				// Alternative BOP���� ������ �׸��� �����̸� ��� ������ �����ϴ��� üũ
				String line_id = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
				String bop_line_code = SDVPropertyConstant.ITEM_ID_PREFIX  + "-" + alt_shop_code + "-"  + line_id + "-"  + product_code + "-00";
				TCComponentItem bop_line_item = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, bop_line_code);

				// �������� ������ ����
				if (bop_line_item == null)
				{
					if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
						throw new Exception("Can not find Line Item.[" + bop_line_code + "]");

					bop_line_item = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().saveAsItem(bop_line_code, SDVPropertyConstant.ITEM_REV_ID_ROOT);
				}

				TCComponentItemRevision bop_line_revision = bop_line_item.getLatestItemRevision();
				TCComponent linePreRevision = null;
				if (CustomUtil.isReleased(bop_line_revision))
				{
					if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
						throw new Exception("BOP Line Revision is not modify status. Please first Line Revise.[" + bop_line_code + "]");

					String new_rev_id = bop_line_item.getNewRev();
					linePreRevision = bop_line_item.getLatestItemRevision();
					bop_line_revision = bop_line_item.revise(new_rev_id, bop_line_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), bop_line_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
				}

				revMECO = bop_line_revision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
				if (revMECO == null)
				{
					bop_line_revision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, (TCComponent) meco_obj);

					if (linePreRevision != null)
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_PROBLEM_ITEM, linePreRevision);

					if (! ((TCComponentChangeItemRevision) meco_obj).isRelationSet(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_line_revision))
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_line_revision);
				}

				// ������ ���� ����
				setProperties(bop_line_revision, ((TCComponentBOPLine) alt_bop_obj).getItemRevision(), product_code, vehicle_code);

				bop_line_item.refresh();
				bop_shop_bopline.refresh();
				bop_shop_bopline.window().refresh();

				// ��� BOPShop ������ �����ϴ��� üũ�ؼ� �������� ������ ����.
				for(AIFComponentContext alt_child_line : bop_shop_bopline.getChildren())
				{
					if (alt_child_line.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					TCComponentBOPLine child_bopline = (TCComponentBOPLine) alt_child_line.getComponent();
					String child_id = child_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
					if (child_id.equals(bop_line_code))
					{
						bop_line_bopline = child_bopline;
						break;
					}
				}

				// ��� BOPShop ������ ������ ������ ����
				if (bop_line_bopline == null)
					bop_line_bopline = (TCComponentBOPLine) bop_shop_bopline.add(bop_line_item, null);

				if (bop_line_bopline != null)
					makeChildBOPLine(alt_shop_code, (TCComponentBOPLine) alt_bop_obj, bop_line_bopline, product_code, vehicle_code, (TCComponent) meco_obj, alt_prefix);
			}
			else
			{
				// ������ �׸��� �����̸� BOPShop�������� BOPLine�� ã�´�.
				bop_shop_bopline.refresh();
				bop_shop_bopline.window().refresh();

				if (bop_shop_bopline.hasChildren())
				{
					String line_id = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
					for(AIFComponentContext shop_child_line : bop_shop_bopline.getChildren())
					{
						if (shop_child_line.getComponent() instanceof TCComponentAppGroupBOPLine)
							continue;

						TCComponentBOPLine child_bopline = (TCComponentBOPLine) shop_child_line.getComponent();
						String child_id = child_bopline.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
						if (child_id.equals(line_id))
						{
							bop_line_bopline = child_bopline;
							break;
						}
					}
				}
			}

			// ������ �����ؼ� ������ �ǵ�, ������ ������ ����
			if (bop_line_bopline == null)
				throw new Exception("Apply BOPLine Item can not find. contact to BOP Admin.");

			if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
			{
				// Alternative BOP���� ������ �׸��� �����̸� ��� ������ �����ϴ��� üũ
				String line_id = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
				String station_id = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().getProperty(SDVPropertyConstant.STATION_REV_CODE);
				String bop_station_code = SDVPropertyConstant.ITEM_ID_PREFIX  + "-" + alt_shop_code + "-"  + line_id + "-" + station_id + "-" + product_code + "-00";
				TCComponentItem bop_station_item = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, bop_station_code);
				TCComponentBOPLine bop_station_bopline = null;

				// �������� ������ ����
				if (bop_station_item == null)
				{
					bop_station_item = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().saveAsItem(bop_station_code, SDVPropertyConstant.ITEM_REV_ID_ROOT);
				}

				TCComponentItemRevision bop_station_revision = bop_station_item.getLatestItemRevision();
				TCComponent stationPreRevision = null;
				if (CustomUtil.isReleased(bop_station_revision))
				{
					String new_rev_id = bop_station_item.getNewRev();
					stationPreRevision = bop_station_item.getLatestItemRevision();
					bop_station_revision = bop_station_item.revise(new_rev_id, bop_station_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), bop_station_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
				}

				// ������ MECO�� ����
//				revMECO = bop_station_revision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
//				if (revMECO == null)
				{
					bop_station_revision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, (TCComponent) meco_obj);

					if (stationPreRevision != null)
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_PROBLEM_ITEM, stationPreRevision);

					if (! ((TCComponentChangeItemRevision) meco_obj).isRelationSet(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_station_revision))
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_station_revision);
				}

				// ������ ���� ����
				setProperties(bop_station_revision, ((TCComponentBOPLine) alt_bop_obj).getItemRevision(), product_code, vehicle_code);

				bop_station_item.refresh();
				bop_line_bopline.refresh();
				bop_line_bopline.window().refresh();

				// ���� ������ �����ϴ��� üũ�ؼ� �������� ������ ����.
				AIFComponentContext[] alt_child_lines = bop_line_bopline.getChildren();
				for(AIFComponentContext alt_child_line : alt_child_lines)
				{
					if (alt_child_line.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					TCComponentBOPLine child_bopline = (TCComponentBOPLine) alt_child_line.getComponent();
					String child_id = child_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
					if (child_id.equals(bop_station_code))
					{
						bop_station_bopline = child_bopline;
						break;
					}
				}

				// ���� ������ ������ ������ ����
				if (bop_station_bopline == null)
					bop_station_bopline = (TCComponentBOPLine) bop_shop_bopline.add(bop_station_item, null);

				if (bop_station_bopline != null)
					makeChildBOPLine(alt_shop_code, (TCComponentBOPLine) alt_bop_obj, bop_station_bopline, product_code, vehicle_code, (TCComponent) meco_obj, alt_prefix);
			}

			updatePertInfo((TCComponentBOPLine) alt_bop_obj, bop_shop_bopline);

			bop_shop_bopline.window().save();
			bop_shop_bopline.window().refresh();
//			bop_shop_bopline.window().close();


			final TCComponentItemRevision openRevision = bop_shop_revision;
			AbstractAIFOperation openOperation = new AbstractAIFOperation() {

				@Override
				public void executeOperation() throws Exception {
                    try {
                    	MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                    	AbstractViewableTreeTable[] openTreeTables = mfgApp.getViewableTreeTables();
                    	boolean isOpened = false;
                    	for (AbstractViewableTreeTable openTreeTable : openTreeTables)
                    	{
                    		if (openTreeTable.getBOMRoot().getItem().equals(openRevision.getItem()))
                    		{
                    			isOpened = true;
                    		}
                    	}

                    	if (! isOpened)
                    		mfgApp.open(openRevision.getItem());
                    	storeOperationResult(IStatus.OK);
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.storeOperationResult(IStatus.ERROR);
                        return;
                    }
				}
			};

			session.queueOperation(openOperation);
		}
		catch (Exception ex)
		{
			setErrorMessage(ex.getMessage());
			setExecuteError(ex);
			throw ex;
		}
		finally
		{
			if (bop_shop_bopline != null)
				bop_shop_bopline.window().close();
			if (plantLine != null)
				plantLine.window().close();
		}
	}


    /**
	 * �ɼ��� �����ϴ� �Լ�
	 * @param option_from_bopline
	 * @param option_copyto_bopline
	 * @throws Exception
	 */
	private void copyTopOptions(TCComponentBOPLine option_from_bopline, TCComponentBOPLine option_copyto_bopline) throws Exception
	{
		try
		{
			TCVariantService variantService = session.getVariantService();
			ModularOption[] srcOptions = SDVBOPUtilities.getModularOptions(option_from_bopline);
			ModularOption[] copyToOptions = SDVBOPUtilities.getModularOptions(option_copyto_bopline);

			// �����ϱ� ���� ��� �ɼǵ��� ������.
			if (copyToOptions != null && copyToOptions.length > 0)
			{
				// Option ���� ���� ���� ����
				variantService.setLineMvl(option_copyto_bopline, "");
				option_copyto_bopline.save();
				// �� �ɼ� ����
				for (ModularOption copyToOption : copyToOptions)
				{
					variantService.lineDeleteOption(option_copyto_bopline, copyToOption.optionId);
					option_copyto_bopline.save();
				}
			}

			// �ɼǵ��� ���
//			String[] corpIds = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, "PSM_global_option_item_ids");
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

			// �ɼ��� ���� ����
            String srcOptItemId = MVLLexer.mvlQuoteId(option_from_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);
            String targetOptItemId = MVLLexer.mvlQuoteId(option_copyto_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);

            // ���� �ɼ� ��ȿ���˻� ������ ������
            String lineMvl = variantService.askLineMvl(option_from_bopline).replace(srcOptItemId, targetOptItemId);

            // ��� BOMLINE�� �ɼ� ��ȿ���˻� ������ ������
            variantService.setLineMvl(option_copyto_bopline, lineMvl);

            option_copyto_bopline.save();
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * ���� ��� �ڽĵ��� BOMLine���� �����Ѵ�.
	 * @param alt_shop_bopline
	 */
	private void cutAllChildren(TCComponentBOPLine bopLine) throws Exception {
		try
		{
			if (bopLine == null)
				return;

			AIFComponentContext[] child_boplines = bopLine.getChildren();
			ArrayList<TCComponentBOMLine> to_delete_lines = new ArrayList<TCComponentBOMLine>();

			for (AIFComponentContext child_context : child_boplines)
			{
				if (! (child_context.getComponent() instanceof TCComponentAppGroupBOPLine))
					to_delete_lines.add((TCComponentBOMLine) child_context.getComponent());
			}

			if (to_delete_lines.size() > 0)
			{
				SDVBOPUtilities.disconnectObjects(bopLine, to_delete_lines);
			}

			bopLine.save();
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * ������ Alternative BOP ���� �������� ��� BOP�� �����ϰ� ������ �����ϴ� �Լ�
	 *
	 * @param altBopLine
	 * @param targetBopLine
	 * @param altPrefix
	 * @param withEndItem
	 * @throws Exception
	 */
	private void makeChildBOPLine(String shop_code, TCComponentBOPLine altBopLine, TCComponentBOPLine targetBopLine, String product_code, String vehicle_code, TCComponent mecoComponent, String alt_prefix) throws Exception {
		try
		{
			AIFComponentContext[] alt_child_boplines = altBopLine.getChildren();

			// ��� BOP ������ �ڽ��� �����ϸ� ��� �߶󳻰� ���� �ٿ�����.
			cutAllChildren(targetBopLine);

			// Alternative BOP ���� �ڽĵ��� ��� BOP �׸��� �����ϴ��� üũ
			if (alt_child_boplines != null && alt_child_boplines.length > 0)
			{
				for (AIFComponentContext alt_child_context : alt_child_boplines)
				{
					if (alt_child_context.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					TCComponentBOPLine added_new_bopline = null;
					TCComponentBOPLine alt_child_bopline = (TCComponentBOPLine) alt_child_context.getComponent();

					String alt_child_type = alt_child_bopline.getItem().getType();
					String alt_occ_type = alt_child_bopline.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE);
					TCComponentItemRevision alt_bop_revision = alt_child_bopline.getItemRevision();
					alt_bop_revision.refresh();

					// BOP �������̸� ������ ��ü�� �����ؾ� �Ѵ�. ������ �������� �ʰ� �Ӽ��� ����
					if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
					{
						String bop_child_id = null;
//						String alt_prefix = alt_bop_revision.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
						String alt_item_id = alt_bop_revision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
						String line_code = alt_bop_revision.getProperty(SDVPropertyConstant.LINE_REV_CODE);
						if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
							bop_child_id = SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shop_code + "-" + line_code + "-" + product_code + "-00";
						else if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
							bop_child_id = SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shop_code + "-" + line_code + "-" + alt_bop_revision.getProperty(SDVPropertyConstant.STATION_STATION_CODE) + "-" + product_code + "-00";
						else
							bop_child_id = alt_item_id.replace(alt_prefix + "-", "");
							//bop_child_id = vehicle_code + "-" + shop_code + "-" + alt_child_bopline.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE) + "-" + "00";

						TCComponentItem bop_child_item = CustomUtil.findItem(alt_child_type, bop_child_id);
						TCComponentItemRevision bop_child_revision = null;

						// �ӽ� BOP�� �������� ������ �����Ѵ�.
						if (bop_child_item == null)
						{
//							alt_child_item = CustomUtil.createItem(alt_child_type, alt_child_id, SDVPropertyConstant.ITEM_REV_ID_ROOT, target_child_bopline.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), target_child_bopline.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
							bop_child_item = alt_bop_revision.saveAsItem(bop_child_id, SDVPropertyConstant.ITEM_REV_ID_ROOT);
						}

						bop_child_revision = bop_child_item.getLatestItemRevision();
						TCComponent preItemRevision = null;
						if (CustomUtil.isReleased(bop_child_revision))
						{
							String new_rev_id = bop_child_item.getNewRev();
							preItemRevision = bop_child_item.getLatestItemRevision();
							bop_child_revision = bop_child_item.getLatestItemRevision().saveAs(new_rev_id);
//							bop_child_revision = bop_child_item.revise(new_rev_id, bop_child_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), bop_child_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));

						}

						// ������ �ϰ� ������ �ϰ� MECO�� �����Ѵ�.
//						TCComponent revMECO = bop_child_revision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
//						if (revMECO == null)
						{
							bop_child_revision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, mecoComponent);

							if (preItemRevision != null)
								((TCComponentChangeItemRevision) mecoComponent).add(SDVTypeConstant.MECO_PROBLEM_ITEM, preItemRevision);

							if (! ((TCComponentChangeItemRevision) mecoComponent).isRelationSet(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_child_revision))
								((TCComponentChangeItemRevision) mecoComponent).add(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_child_revision);
						}

						// �������� �Ӽ��� ��������.
						setProperties(bop_child_revision, alt_bop_revision, product_code, vehicle_code);

						// ����� ���� ����Ÿ���� ��� �����ϴ� �κ�
						AIFComponentContext []rev_under_items = bop_child_revision.getChildren(new String[]{SDVTypeConstant.PROCESS_SHEET_KO_RELATION, SDVTypeConstant.PROCESS_SHEET_EN_RELATION, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION});
						for (AIFComponentContext rev_under_item : rev_under_items)
						{
							if (rev_under_item.getComponent() instanceof TCComponentDataset)
							{
								try
								{
									((TCComponentDataset) rev_under_item.getComponent()).delete();
								}
								catch (TCException ex)
								{
									bop_child_revision.cutOperation(rev_under_item.getContext().toString(), new TCComponent[]{(TCComponent) rev_under_item.getComponent()});
									try
									{
										((TCComponentDataset) rev_under_item.getComponent()).delete();
									}
									catch (TCException ex2)
									{
										ex2.printStackTrace();
									}
								}
							}
						}

						// �����۾�ǥ�ؼ� ���ø� ����
						if (bop_child_item.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
							bop_child_item.getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ||
							bop_child_item.getType().equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM))
						{
							bop_child_revision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, SDVBOPUtilities.getTemplateDataset(SDVTypeConstant.PROCESS_SHEET_TEMPLATE_PREF_NAME, null));
						}

						bop_child_item.refresh();
						if (bop_child_item != null)
						{
							for (AIFComponentContext childContext : targetBopLine.getChildren())
							{
								if (childContext.getComponent() instanceof TCComponentAppGroupBOPLine)
									continue;

								if (bop_child_id.equals(childContext.getComponent().getProperty(SDVPropertyConstant.BL_ITEM_ID)))
								{
									added_new_bopline = (TCComponentBOPLine) childContext.getComponent();
									break;
								}
							}
							if (added_new_bopline == null)
							{
								ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
								addToChild.add(bop_child_item);
								TCComponent []addedChild = SDVBOPUtilities.connectObject(targetBopLine, addToChild, null);
								if (addedChild == null || addedChild.length == 0)
									throw new Exception("Can not add to BOP Line.");
								added_new_bopline = (TCComponentBOPLine) addedChild[0];
								added_new_bopline.save();
								targetBopLine.save();
//								added_new_bopline = (TCComponentBOPLine) targetBopLine.add(bop_child_item, null);
							}
						}

						if (added_new_bopline != null)
						{
							setBOPLineProperties(alt_child_bopline, added_new_bopline);
						}

						// ���� �� ������ ������ �켱 ���� �����ϰ� �ٽ� ���δ�.
						if ((alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
							 alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ||
							 alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) ||
							 alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM)) && added_new_bopline != null)
						{
							// EndItem �� �������� ���� ��� ������ ������ ��� �����Ѵ�.
							cutAllChildren(added_new_bopline);

//long startTime = System.currentTimeMillis();
							// EndItem/������/Plant �� ������ ���� PathNode ���ᵵ ���� �����Ͽ��� �Ѵ�.
							copyOperationChild(alt_child_bopline, added_new_bopline);
//System.out.println("copyOperationChild time =>" + (System.currentTimeMillis() - startTime));
						}

						if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
							alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ||
							alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM))
						{
							// �������� Activity ����� ��� �ǳ�?
							copyMEActivitiesOfOperation(alt_child_bopline, added_new_bopline);
						}

						// �����̳� ������ �ٽ� ������ �����ϵ��� ���ȣ�� �Ѵ�.
						if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
							makeChildBOPLine(shop_code, alt_child_bopline, added_new_bopline, product_code, vehicle_code, mecoComponent, alt_prefix);
					}
					else if (alt_occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE))
					{
						// Ÿ���� �����̰�, ���������� Plant�� MEResource�� �����Ѵ�.
						TCComponentMEAppearancePathNode[] linkedPaths = alt_child_bopline.askLinkedAppearances(false);

						if (linkedPaths != null && linkedPaths.length > 0)
						{
							TCComponentBOMLine plantBOMLine = plantLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantLine);
							TCComponentBOMLine productPlantBOMLine = null;
							boolean is_alt_plant = false;

							// MEResource�� ������ Alternative���� üũ�ϰ� �� ������ �������� ������ Product Plant������ �˻�����.
							if (plantBOMLine.parent().getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_IS_ALTBOP))
								is_alt_plant = plantBOMLine.parent().getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
							if (is_alt_plant)
							{
								// ������ Alternative�� �ƴϸ� �� �������� Alternative�� ã�´�.
								productPlantBOMLine = getProductBOPLineInParent(plantBOMLine.parent().parent(), plantBOMLine.parent(), true);
								if (productPlantBOMLine != null)
								{
									// ������ Alternative Plant ���� �ڱ� Plant������ ã�´�.
									productPlantBOMLine = getProductBOPLineInParent(productPlantBOMLine, plantBOMLine, false);
								}
								else
								{
									throw new NullPointerException("Can not find Product Plant Item of [" + plantBOMLine.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID) + "]");
								}

								ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
								addToChild.add(productPlantBOMLine);
								TCComponent []addedChild = SDVBOPUtilities.connectObject(targetBopLine, addToChild, alt_occ_type);
								if (addedChild == null || addedChild.length == 0)
									throw new Exception("Can not add to BOP Line.");
								added_new_bopline = (TCComponentBOPLine) addedChild[0];
								added_new_bopline.save();
								altBopLine.save();
//								added_new_bopline = (TCComponentBOPLine) targetBopLine.assignAsChild(productPlantBOMLine, alt_occ_type);

								setBOPLineProperties(alt_child_bopline, added_new_bopline);
							}
							else
							{
								ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
								addToChild.add(alt_child_bopline.getItem());
								TCComponent []addedChild = SDVBOPUtilities.connectObject(targetBopLine, addToChild, alt_occ_type);
								if (addedChild == null || addedChild.length == 0)
									throw new Exception("Can not add to BOP Line.");
								added_new_bopline = (TCComponentBOPLine) addedChild[0];
								added_new_bopline.save();
								altBopLine.save();
//								added_new_bopline = (TCComponentBOPLine) targetBopLine.add(alt_child_bopline.getItem(), alt_occ_type);

								added_new_bopline.linkToAppearance(linkedPaths[0], false);

								setBOPLineProperties(alt_child_bopline, (TCComponentBOPLine) added_new_bopline);
							}
						}
						else
						{
							ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
							addToChild.add(alt_child_bopline.getItem());
							TCComponent []addedChild = SDVBOPUtilities.connectObject(targetBopLine, addToChild, alt_occ_type);
							if (addedChild == null || addedChild.length == 0)
								throw new Exception("Can not add to BOP Line.");
							added_new_bopline = (TCComponentBOPLine) addedChild[0];
							added_new_bopline.save();
							altBopLine.save();
//						added_new_bopline = (TCComponentBOPLine) targetBopLine.add(alt_child_bopline.getItem(), alt_occ_type);

							setBOPLineProperties(alt_child_bopline, (TCComponentBOPLine) added_new_bopline);
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * ���� BOM Line���� Alternative Plant�� �ش��ϴ� Product Plant�� ã�� �����ϴ� �Լ�
	 *
	 * @param parentLine
	 * @param targetLine
	 * @param altCheck
	 * @return
	 * @throws Exception
	 */
	private TCComponentBOMLine getProductBOPLineInParent(TCComponentBOMLine parentLine, TCComponentBOMLine targetLine, boolean altCheck) throws Exception
	{
		try
		{
			String item_id = targetLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			if (altCheck)
			{
				String alt_prefix = targetLine.getItemRevision().getProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX);
				boolean is_alt = targetLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);

				if (is_alt)
				{
					for (AIFComponentContext childLine : parentLine.getChildren())
					{
						if (childLine.getComponent() instanceof TCComponentAppGroupBOPLine)
							continue;

						String child_id = ((TCComponentBOMLine) childLine.getComponent()).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

						if (item_id.replace(alt_prefix + "-", "").equals(child_id))
							return (TCComponentBOMLine) childLine.getComponent();
					}

					if (parentLine.parent() != null)
					{
						TCComponentBOMLine findLine = getProductBOPLineInParent(parentLine.parent(), parentLine, altCheck);
						if (findLine != null)
						{
//							item_id = parentLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
							for (AIFComponentContext findChildLine : findLine.getChildren())
							{
								if (findChildLine.getComponent() instanceof TCComponentAppGroupBOPLine)
									continue;

								String child_id = ((TCComponentBOMLine) findChildLine.getComponent()).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

								if (item_id.replace(alt_prefix + "-", "").equals(child_id))
									return (TCComponentBOMLine) findChildLine.getComponent();
							}
						}
					}
				}
			}
			else
			{
				for (AIFComponentContext childLine : parentLine.getChildren())
				{
					if (childLine.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					String child_id = ((TCComponentBOMLine) childLine.getComponent()).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

					if (item_id.equals(child_id))
						return (TCComponentBOMLine) childLine.getComponent();
				}
			}

			return null;
		}
		catch (Exception ex)
		{
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
	private void copyMEActivitiesOfOperation(TCComponentBOMLine copyFromOpLine, TCComponentBOMLine copyToOpLine) throws Exception {
		try
		{
//long startTime = System.currentTimeMillis();
			String []timeProperties = registry.getStringArray("CopyActivityProperties.BODY");
			TCComponentMEActivity copyfrom_root_activity = (TCComponentMEActivity) copyFromOpLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
			TCComponent[] copyfrom_child_activities = ActivityUtils.getSortedActivityChildren(copyfrom_root_activity);

//			TCComponentMEActivity copyto_root_activity = (TCComponentMEActivity) copyToOpLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
//			TCComponent[] copyto_child_activities = ActivityUtils.getSortedActivityChildren(copyto_root_activity);

			TCComponent activityRootLine = copyToOpLine.getReferenceProperty("bl_me_activity_lines");
			if (activityRootLine != null && activityRootLine instanceof TCComponentCfgActivityLine)
			{
				TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) activityRootLine);
				for (TCComponent childActivityLine : childLines)
				{
					TCComponentMECfgLine parentLine = ((TCComponentCfgActivityLine) childActivityLine).parent();
					ActivityUtils.removeActivity((TCComponentCfgActivityLine) childActivityLine);
					parentLine.save();
				}
			}
			((TCComponentCfgActivityLine) activityRootLine).save();

//			for (TCComponent copyto_child_activity : copyto_child_activities)
//			{
//				// Alternative ������ ��� ��Ƽ��Ƽ �ϴ� ����
//				copyto_child_activity.delete();
//			}
//System.out.println("copyMEActivitiesOfOperation Delete time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

            HashMap<String, TCComponentBOPLine> toolBOMLineList = getAssignedToolBOMLine(copyToOpLine);
//System.out.println("copyMEActivitiesOfOperation getTool time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

            for (TCComponent copyfrom_child_activity : copyfrom_child_activities)
			{
				// ������ ��Ƽ��Ƽ�� �����ؾ� �Ѵ�. ��� �Ӽ���� �Բ�.
				TCComponent[] copyto_activities = ActivityUtils.createActivitiesBelow(new TCComponent[] { activityRootLine }, copyfrom_child_activity.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
                TCComponentCfgActivityLine copyto_activity_line = (TCComponentCfgActivityLine) copyto_activities[0];
                TCComponentMEActivity copyto_activity = (TCComponentMEActivity) copyto_activity_line.getUnderlyingComponent();
//System.out.println("copyMEActivitiesOfOperation CreateActivity time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

				HashMap<String, String> propertyMap = new HashMap<String, String>();
				for (String property : timeProperties)
				{
					String []propValue = SoaUtil.marshallTCProperty(copyfrom_child_activity.getTCProperty(property));
					if (propValue != null && propValue.length > 0 && propValue[0].trim().length() > 0)
					{
						if (! propertyMap.containsKey(property))
							propertyMap.put(property, propValue[0]);
					}
				}
				if (propertyMap.size() > 0)
					copyto_activity.setProperties(propertyMap);
//                // Activity Time
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).setDoubleValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).getDoubleValue());
//                // Category
//            	copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).getStringValue());
//                // Work Code (SYSTEM Code)
//                // �۾����
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE).getStringValue());
//                // Time System Frequency
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).setDoubleValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).getDoubleValue());
//                // KPC
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT).getStringValue());
//                // KPC ��������
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).getStringValue());
//                // Process Type
////                alt_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).setStringValue(target_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).getStringValue());
//                // English Name
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).getStringValue());
                // Workers -- (Array)
                String[] workerList = copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORKER).getStringValueArray();
                ArrayList<String> workerArray = new ArrayList<String>();
                for (String worker : workerList)
                	if (worker != null && worker.trim().length() > 0)
                		workerArray.add(worker.trim());
                if (workerArray.size() > 0)
                	copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORKER).setStringValueArray(workerArray.toArray(new String[0]));
                else
                	copyto_activity.setProperty(SDVPropertyConstant.ACTIVITY_WORKER, null);
//                // Overlay Type
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE).getStringValue());
                // MECO
//                copyto_activity.setStringProperty(SDVPropertyConstant.ACTIVITY_MECO_NO, copyfrom_child_activity.getStringProperty(SDVPropertyConstant.ACTIVITY_MECO_NO));

                // Activity �����ڿ� �Ҵ�
                // ���� ���� METool occtype�� �ش��ϴ� BOMLine�� ��� �����´�. �׸���, �Ҵ�� ���� ����Ʈ�� ID�� ���� ���� ������ ���Ͽ� �� BOMLine�� Tool�� �Ҵ��Ѵ�.
                if (toolBOMLineList != null)
                {
	                String[] tools = ((TCComponentMEActivity) copyfrom_child_activity).getReferenceToolList(copyFromOpLine);
	                ArrayList<TCComponentBOPLine> reference_tool_list = new ArrayList<TCComponentBOPLine>();
	                for (String tool : tools)
	                {
	                	if (toolBOMLineList != null && toolBOMLineList.containsKey(tool))
	                		reference_tool_list.add(toolBOMLineList.get(tool));
	                }
	                if (reference_tool_list.size() > 0)
	                	copyto_activity.addReferenceTools(copyToOpLine, reference_tool_list.toArray(new TCComponentBOPLine[0]));
                }
//System.out.println("copyMEActivitiesOfOperation setActivityProperty time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

                copyto_activity.save();
                activityRootLine.save();
//System.out.println("copyMEActivitiesOfOperation save Activity time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();
			}
//System.out.println("copyMEActivitiesOfOperation setProperty =>" + (System.currentTimeMillis() - startTime));
		}
		catch (Exception ex)
		{
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
	private HashMap<String, TCComponentBOPLine> getAssignedToolBOMLine(TCComponentBOMLine copyToOpLine) throws Exception {

		try
		{
			HashMap<String, TCComponentBOPLine> childToolList = null;
			TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(copyToOpLine);
			for (TCComponentBOMLine operationUnderBOMLine : childs) {
				if (operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_TOOL) ||
					operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE) ||
					operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_SUBSIDIARY) ||
//					operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_GROUP) ||
					operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA))
				{
					String itemId = operationUnderBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
					if (childToolList == null)
						childToolList = new HashMap<String, TCComponentBOPLine>();
					if (! childToolList.containsKey(itemId))
						childToolList.put(itemId, (TCComponentBOPLine) operationUnderBOMLine);
				}
			}

			return childToolList;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * ���� Structure ���� �ڽĵ��� �����ϴ� �Լ�
	 *
	 * @param fromBopLine
	 * @param copyToBopLine
	 */
	private void copyOperationChild(TCComponentBOPLine fromBopLine, TCComponentBOPLine copyToBopLine) throws Exception {
		try
		{
			// ���� ���� �ڽĵ��� ��� �����Ѵ�.
			// BOP Line �Ӽ����� �����Ѵ�.
			// ���� �ڽĵ� �� MEConsumed�� ������ PathNode�� �ξ��ش�.
			// ���� �ڽĵ� �� MEWorkArea�� ����(Alternative Plant�� ������ �װ����� ����)�� Plant PathNode�� �ξ��ش�.
			if (fromBopLine.hasChildren())
			{
				for (AIFComponentContext childContext : fromBopLine.getChildren())
				{
					if (childContext.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					TCComponentBOPLine child_line = (TCComponentBOPLine) childContext.getComponent();
					String occ_type = child_line.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE);

					if (occ_type != null && (occ_type.equals(SDVTypeConstant.OCC_TYPE_MECONSUMED) || occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWELDPOINT) ||
											  occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA) || occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE)))
					{
						TCComponentMEAppearancePathNode[] linkedPaths = child_line.askLinkedAppearances(false);

//						if (linkedPaths == null || linkedPaths.length == 0)
//							throw new Exception("Can not find BOM Line information from Product BOM.");

						TCComponentBOMLine product_plant_bom_line = null;
						if (occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA) || occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE))
						{
							if (occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA))
							{
								// WorkArea�� Alternative�� �����ϱ� ������ Alternative�� �ƴ� �Ϲ� WorkArea�� Pathnode ������ ������ ��� �Ѵ�.
								TCComponentBOMLine plant_bom_line = (TCComponentBOMLine) child_line.getReferenceProperty("bl_me_refline");

								if (plant_bom_line == null)
									throw new Exception("Please load Plant BOM.");

								plant_bom_line = plantLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantLine);

								String plant_item_type = plant_bom_line.getItem().getType();
								// �Ϲݰ��� ������ ����
								if (plant_item_type.equals(SDVTypeConstant.PLANT_OPAREA_ITEM) || plant_item_type.equals(SDVTypeConstant.PLANT_STATION_ITEM))
								{
//long startTime = System.currentTimeMillis();
									product_plant_bom_line = getProductBOPLineInParent(plant_bom_line.parent(), plant_bom_line, true);
//System.out.println("getProductBOPLineInParent time =>" + (System.currentTimeMillis() - startTime));

									if (product_plant_bom_line != null)
									{
										ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
										addToChild.add(product_plant_bom_line);
										TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, SDVTypeConstant.OCC_TYPE_MEWORKAREA);
										if (addedChild == null || addedChild.length == 0)
											throw new Exception("Can not add to BOP Line.");
										TCComponentBOPLine added_bop_line = (TCComponentBOPLine) addedChild[0];
										added_bop_line.save();
										copyToBopLine.save();
//										TCComponentBOPLine added_bop_line = (TCComponentBOPLine) copyToBopLine.assignAsChild(product_plant_bom_line, SDVTypeConstant.OCC_TYPE_MEWORKAREA);

										setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
									}
								}
								else
								{
									// �������� ������ Plant������ �ƴ� Plant���� ���� ������ ���� ������ ������ üũ�ؼ� ó���ؾ� �Ѵ�.
//long startTime = System.currentTimeMillis();
									product_plant_bom_line = getProductBOPLineInParent(plant_bom_line.parent().parent(), plant_bom_line.parent(), true);
//System.out.println("getProductBOPLineInParent time =>" + (System.currentTimeMillis() - startTime));

									if (product_plant_bom_line != null)
									{
										product_plant_bom_line = getProductBOPLineInParent(product_plant_bom_line, plant_bom_line, false);

										if (product_plant_bom_line != null)
										{
											ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
											addToChild.add(product_plant_bom_line);
											TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, SDVTypeConstant.OCC_TYPE_MEWORKAREA);
											if (addedChild == null || addedChild.length == 0)
												throw new Exception("Can not add to BOP Line.");
											TCComponentBOPLine added_bop_line = (TCComponentBOPLine) addedChild[0];
											added_bop_line.save();
											copyToBopLine.save();
//											TCComponentBOPLine added_bop_line = (TCComponentBOPLine) copyToBopLine.assignAsChild(product_plant_bom_line, SDVTypeConstant.OCC_TYPE_MEWORKAREA);

											setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
										}
									}
								}
							}
							else
							{
								// MEResource �� Plant���� �� �Ͱ� �Ϲ� ����� ���еȴ�.
								TCComponentBOMLine plant_bom_line = (TCComponentBOMLine) child_line.getReferenceProperty("bl_me_refline");

								if (plant_bom_line == null)
								{
									// �Ϲݼ����� ���� �׳� ��������.
									ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
									addToChild.add(child_line.getItem());
									TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
									if (addedChild == null || addedChild.length == 0)
										throw new Exception("Can not add to BOP Line.");
									TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
//									TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);

									setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);

									added_bop_line.save();
									child_line.save();
								}
								else
								{
									// Plant���� �� MEResource�� ������ üũ�ؼ� �����´�.
									plant_bom_line = plantLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantLine);

									// ���� Plant BOM Line���� Alternative �� �ƴ� Product Plant BOM Line�� ã�´�.
									product_plant_bom_line = getProductBOPLineInParent(plant_bom_line.parent().parent(), plant_bom_line.parent(), true);

									if (product_plant_bom_line != null)
									{
										// Product Plant BOM Line���� �ش� Resource�� ã�� �ٿ��ش�.
										product_plant_bom_line = getProductBOPLineInParent(product_plant_bom_line, plant_bom_line, false);

										if (product_plant_bom_line != null)
										{
											ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
											addToChild.add(product_plant_bom_line);
											TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, SDVTypeConstant.OCC_TYPE_MEWORKAREA);
											if (addedChild == null || addedChild.length == 0)
												throw new Exception("Can not add to BOP Line.");
											TCComponentBOPLine added_bop_line = (TCComponentBOPLine) addedChild[0];
											added_bop_line.save();
											copyToBopLine.save();
//											TCComponentBOPLine added_bop_line = (TCComponentBOPLine) copyToBopLine.assignAsChild(product_plant_bom_line, SDVTypeConstant.OCC_TYPE_MEWORKAREA);

											setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
										}
									}
									else
									{
										ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
										addToChild.add(child_line.getItem());
										TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
										if (addedChild == null || addedChild.length == 0)
											throw new Exception("Can not add to BOP Line.");
										TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
//										TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);
										setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);

										added_bop_line.linkToAppearance(linkedPaths[0], false);

										added_bop_line.save();
										copyToBopLine.save();
									}
								}
							}
						}
						else
						{
							ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
							addToChild.add(child_line.getItem());
							TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
							if (addedChild == null || addedChild.length == 0)
								throw new Exception("Can not add to BOP Line.");
							TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
//							TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);
							setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);

							added_bop_line.linkToAppearance(linkedPaths[0], false);

							added_bop_line.save();
							copyToBopLine.save();
						}
					}
					else
					{
						// �Ϲ� ���ҽ���..
						ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
						addToChild.add(child_line.getItem());
						TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
						if (addedChild == null || addedChild.length == 0)
							throw new Exception("Can not add to BOP Line.");
						TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
//						TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);
						setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
					}
				}
			}
		}
		catch (Exception ex)
		{
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
	private void setBOPLineProperties(TCComponentBOPLine from_bopline, TCComponentBOPLine setto_bopline) throws Exception
	{
		try
		{
//long startTime = System.currentTimeMillis();
			String []bopLineProperties = registry.getStringArray("CopyBOPLineProperties");

			setto_bopline.refresh();
//			setto_bopline.window().refresh();
			if (bopLineProperties == null)
			{
				System.out.println("ApplyAlternativeToProductionBOPActionOperation.setBOPLineProperties() = Copy Property is null.");
				return;
			}

			HashMap<String, String> lineProperties = new HashMap<String, String>();
			for (String bopLineProperty : bopLineProperties)
			{
				if ((bopLineProperty.equals("bl_variant_condition")) || (bopLineProperty.equals("bl_condition_tag")) || (bopLineProperty.equals("bl_formula")))
				{
				    try
				    {
				    	if (! from_bopline.getProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.OCC_TYPE_MECONSUMED))
				    	{
					        TCComponent localTCComponent = from_bopline.getReferenceProperty("bl_condition_tag");
					        Object localObject1;
					        if (localTCComponent == null)
					        {
					            localObject1 = from_bopline.getProperty("bl_variant_condition");
					            Object setToObject = setto_bopline.getProperty("bl_variant_condition");
					            if (localObject1 != null && setToObject != null && ! setToObject.equals(localObject1))
					            	from_bopline.getSession().getVariantService().setLineMvlCondition(setto_bopline, (String)localObject1);
					        }
					        else
					        {
					        	TCComponent toTCComponent = setto_bopline.getReferenceProperty("bl_condition_tag");
					        	if (toTCComponent != null && ! localTCComponent.equals(toTCComponent))
					        	{
						            localObject1 = VariantCondition.create(localTCComponent, setto_bopline.window());
						            setto_bopline.setReferenceProperty("bl_condition_tag", ((VariantCondition) localObject1).toCondition());
					        	}
					        }
					        setto_bopline.save();
				    	}
				    }
				    catch (Exception ex)
				    {
				        throw ex;
				    }
				}
				else
				{
					if (from_bopline.isValidPropertyName(bopLineProperty) && setto_bopline.isValidPropertyName(bopLineProperty))
					{
						String []targetValue = SoaUtil.marshallTCProperty(from_bopline.getTCProperty(bopLineProperty));
						String []altValue = SoaUtil.marshallTCProperty(setto_bopline.getTCProperty(bopLineProperty));
						if (targetValue == null || altValue == null || targetValue.length == 0 || altValue.length == 0 || ! targetValue[0].equals(altValue[0]))
							if (! lineProperties.containsKey(bopLineProperty))
								lineProperties.put(bopLineProperty, (targetValue == null || targetValue.length == 0 ? null : targetValue[0]));
					}
				}
			}

			if (lineProperties.size() > 0)
				setto_bopline.setProperties(lineProperties);

			setto_bopline.save();
//System.out.println("setBOPLineProperty time =>" + (System.currentTimeMillis() - startTime));
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * BOP �׸���� ������ �����ϴ� �Լ�
	 *
	 * @param setToRevision
	 * @param fromRevision
	 * @throws Exception
	 */
	private void setProperties(TCComponentItemRevision setToRevision, TCComponentItemRevision fromRevision, String product_code, String vehicle_code) throws Exception {
		try
		{
//long startTime = System.currentTimeMillis();
			String []itemProperties = null;
			String []revProperties = null;

			setToRevision.refresh();
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
//				revProperties = registry.getStringArray("CopyStationRevisionProperties.BODY");
                if (setToRevision.getItem().isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) && fromRevision.getItem().isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) &&
                    ! setToRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME).equals(fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME)))
                    setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

                if (fromRevision.isValidPropertyName(SDVPropertyConstant.STATION_ALT_PREFIX))
                    setToRevision.setProperty(SDVPropertyConstant.STATION_ALT_PREFIX, "");
                if (fromRevision.isValidPropertyName(SDVPropertyConstant.STATION_IS_ALTBOP))
                    setToRevision.setLogicalProperty(SDVPropertyConstant.STATION_IS_ALTBOP, false);

                if (setToRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS) && fromRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS))
                {
                    // ���� EXT_DECESSORS �� ���� �Ǿ� ������ ����Ʈ�� �����Ѵ�
                    TCComponent[] bopDecessorsStations = setToRevision.getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
                    for (TCComponent bopDecessorsStation : bopDecessorsStations) {
                        updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, (TCComponentItemRevision)bopDecessorsStation, DELETE);
                    }
                    // ���� EXT_DECESSORS �� ���� �Ѵ�
                    TCComponent[] decessorsStations = fromRevision.getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
                    if (decessorsStations != null && decessorsStations.length > 0)
                    {
                        for (TCComponent decessorsStation : decessorsStations) {
                            String altPrefix= fromRevision.getProperty(SDVPropertyConstant.STATION_ALT_PREFIX);
                            String extDecessorsID = decessorsStation.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                            extDecessorsID = extDecessorsID.replace(altPrefix + "-", "");
                            TCComponentItem extDecessorsItem = SDVBOPUtilities.FindItem(extDecessorsID, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                            if (extDecessorsItem != null) {
                                TCComponentItemRevision extDecessorsItemrev = extDecessorsItem.getLatestItemRevision();
                                updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, extDecessorsItemrev, ADD);
                            }
                        }
                    }
                }
//                if (setToRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS) && fromRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS))
//                {
//                    TCComponent[] extDecessorsLines = fromRevision.getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
//                    if (extDecessorsLines != null && extDecessorsLines.length > 0)
//                      {
//                          for (TCComponent extDecessorsLine : extDecessorsLines) {
//                              updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, (TCComponentItemRevision)extDecessorsLine, DELETE);
//                              String altPrefix= fromRevision.getProperty(SDVPropertyConstant.STATION_ALT_PREFIX);
//                              String extDecessorsID = extDecessorsLine.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
//                              extDecessorsID = extDecessorsID.replace(altPrefix + "-", "");
//                              TCComponentItem extDecessorsItem = SDVBOPUtilities.FindItem(extDecessorsID, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
//                              if (extDecessorsItem != null) {
//                                  TCComponentItemRevision extDecessorsItemrev = extDecessorsItem.getLatestItemRevision();
//                                  updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, extDecessorsItemrev, ADD);
//                              }
//                          }
//                      }
//                }

                setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
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
				if (setToRevision.getItem().isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) && fromRevision.getItem().isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) &&
					! setToRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME).equals(fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME)))
					setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

				if (fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX))
					setToRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, "");
				if (fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP))
					setToRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, false);

				if (setToRevision.isValidPropertyName(SDVPropertyConstant.WELDOP_REV_TARGET_OP) && fromRevision.isValidPropertyName(SDVPropertyConstant.WELDOP_REV_TARGET_OP))
				{
					TCComponent targetOpRevision = fromRevision.getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
					if (targetOpRevision != null)
					{
						String altPrefix = targetOpRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
						String altItemID = targetOpRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
						String item_id = altItemID.replace(altPrefix + "-", "");

						TCComponentItem targetOPItem = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM, item_id);
						if (targetOPItem == null)
							throw new Exception("WeldOperation's target Operation was not found[" + item_id + "].");

						setToRevision.setReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP, targetOPItem.getLatestItemRevision());
					}
				}

				setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
			}

			if (itemProperties != null)
			{
//				setToRevision.getItem().setTCProperties(fromRevision.getItem().getTCProperties(itemProperties));
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
			}
			if (revProperties != null)
			{
				HashMap<String, String> setPropertyList = new HashMap<String, String>();
				for (String property : revProperties)
				{
					if (property.equals(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE))
					{
						if (setToRevision.isValidPropertyName(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE))
							setToRevision.setProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, product_code);
					}
					else if (property.equals(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE))
					{
						if (setToRevision.isValidPropertyName(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE))
							setToRevision.setProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE, vehicle_code);
					}
					else
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
//								setToRevision.setTCProperty(fromRevision.getTCProperty(property));
							}
						}
					}
				}

				if (setPropertyList.size() > 0)
					setToRevision.setProperties(setPropertyList);

				if (fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX))
					setToRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, "");
				if (fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP))
					setToRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, false);
			}
//System.out.println("setProperty time =>" + (System.currentTimeMillis() - startTime));
		}
		catch (Exception ex)
		{
			throw ex;
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
     *  ���BOP�� Decessors �� �����Ѵ�
     * @param alt_bop_obj
     * @param bop_shop_bopline
     * @param alt_prefix
     * @throws Exception
     */
    private void updatePertInfo(TCComponentBOPLine alt_bop_obj, TCComponentBOPLine bop_shop_bopline) throws Exception{

        TCComponent[] altDecessors = alt_bop_obj.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);

        TCComponentItemRevision alt_bop_revision = alt_bop_obj.getItemRevision();
        String alt_prefix = alt_bop_revision.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
        String alt_item_id = alt_bop_revision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

        // ALTBOP �� ���ǵ� PreDecessors �� ��� BOP�� �����Ѵ�
        if (altDecessors != null && altDecessors.length > 0) {
            // ���BOP(Line)�� ã�´�
            String bopID = alt_item_id.replace(alt_prefix + "-", "");
            TCComponentBOPLine bopLine = findBopLine(bopID, bop_shop_bopline);

            // ���BOP(Line) decessors �� ã�´�
            List<TCComponentBOMLine> decessors = findDecessors(altDecessors, alt_prefix, bop_shop_bopline);
            if (decessors.size() > 0) {
                removeBopLineDecessors(bopLine);
                bopLine.addPredecessors(decessors);
            }
        }

        // ������ Decessors �� ���� �Ѵ�
       AIFComponentContext[] stationList = alt_bop_obj.getChildren();
       for (AIFComponentContext station : stationList) {
           TCComponentBOPLine stationBopLine = (TCComponentBOPLine)station.getComponent();
           TCComponent[] stationDecessors = stationBopLine.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);

           // ALTBOP �� ���ǵ� PreDecessors �� ��� BOP�� �����Ѵ�
           if (stationDecessors != null && stationDecessors.length > 0) {
               // ���BOP(Station)�� ã�´�
               String stationLineProp = stationBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
               String bopID = stationLineProp.replace(alt_prefix + "-", "");
               TCComponentBOPLine stationLine = findBopLine(bopID, bop_shop_bopline);

               // ���BOP(Line) stationDecessors �� ã�´�
               List<TCComponentBOMLine> decessors = findDecessors(stationDecessors, alt_prefix, bop_shop_bopline);
               if (decessors.size() > 0) {
                   removeBopLineDecessors(stationLine);
                   stationLine.addPredecessors(decessors);
               }
           }
       }
    }

    /**
     * Decessors �� ���ǵ� ALTBOPLine ��  ���BOPLine���� �ٲ۴�.
     * @param decessors
     * @param prefix
     * @param altBopLine
     * @return
     * @throws TCException
     */
    private List<TCComponentBOMLine> findDecessors( TCComponent[] decessors, String prefix, TCComponentBOPLine bopShopLine) throws TCException{
        List<TCComponentBOMLine> decessorsList = new ArrayList<TCComponentBOMLine>();
        for (TCComponent decessor : decessors) {
//            String bopID = prefix + "-" + decessor.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String bopID = decessor.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            bopID = bopID.replace(prefix+ "-", "");
            decessorsList.add((TCComponentBOMLine)findBopLine(bopID, bopShopLine));
        }
        return decessorsList;
    }

    /**
     *  ���BOP�� ���ǵ� PreDecessors �� �����Ѵ�
     * @param bopLine
     * @throws TCException
     */
    private void removeBopLineDecessors(TCComponentBOPLine bopLine) throws TCException{
        TCComponent[] decessors = bopLine.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);
        // ���� ���BOP�� ���ǵ� PreDecessors �� �����Ѵ�
        if (decessors != null && decessors.length > 0) {
            for (TCComponent decessor : decessors) {
                bopLine.removePredecessor((TCComponentBOMLine)decessor);
            }
        }
    }

    /**
     *  ALTBOPLine �� ��Ī�Ǵ� ���BOPLine �� ã�ƿ´�
     * @param prefix
     * @param bopID
     * @param bopLine
     * @return
     * @throws TCException
     */
    private TCComponentBOPLine findBopLine(String bopID, TCComponentBOPLine bopLine) throws TCException{
        TCComponentBOPLine bop = null;
        if (bopLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || bopLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            AIFComponentContext[] bopChilds = bopLine.getChildren();
            for (AIFComponentContext bopChild : bopChilds) {
                bop = (TCComponentBOPLine)bopChild.getComponent();
                if (bop.getProperty(SDVPropertyConstant.BL_ITEM_ID).equals(bopID)) {
                    return bop;
                }
                bop = findBopLine(bopID, bop);
                if (bop != null) {
                    return bop;
                }
            }
        }
        return bop;
    }
}
