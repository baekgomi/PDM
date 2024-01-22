/**
 * 
 */
package com.symc.plm.me.sdv.operation.body;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class CopyToAlternativeBOPInitOperation extends AbstractSDVInitOperation {
	private Registry registry = Registry.getRegistry(CopyToAlternativeBOPInitOperation.class);

	/**
	 * 
	 */
	public CopyToAlternativeBOPInitOperation() {
	}

	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
		try
		{
			if (! (AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication))
			{
				// MPPApplication Check
				throw new Exception(registry.getString("WorkInMPPApplication.MESSAGE", "MPP Application���� �۾��ؾ� �մϴ�."));
			}
			InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
			if (selectedTargets == null || selectedTargets.length != 1)
				throw new Exception(registry.getString("SelectOneBOP.MESSAGE", "��� BOP Shop �Ǵ� Line�� �ϳ��� ������ �ּ���."));

			if ((! (selectedTargets[0] instanceof TCComponentBOMLine)) ||
					((! ((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM)) &&
					(! ((TCComponentBOMLine) selectedTargets[0]).getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))))
			{
				throw new Exception(registry.getString("SelectionBOPInvalid.MESSAGE", "������ ��� BOP(Shop, Line)�� ������ �ּ���."));
			}

			RawDataMap targetDataMap = new RawDataMap();
			targetDataMap.put("CopyAltTargetItem", selectedTargets[0], IData.OBJECT_FIELD);

			DataSet targetDataset = new DataSet();
			targetDataset.addDataMap("CopyAltTargetItem", targetDataMap);

			setData(targetDataset);
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

}
