/**
 * 
 */
package com.symc.plm.me.sdv.dialog.body;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyOPDialog
 * Class Description : 
 * @date 2013. 11. 20.
 *
 */
public class CreateBodyOPDialog extends SimpleSDVDialog {
	Registry registry = Registry.getRegistry(CreateBodyOPDialog.class);

    /**
     * @param shell
     * @param dialogStub
     */
    public CreateBodyOPDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

	/**
	 * @param shell
	 * @param dialogStub
	 * @param configId
	 */
	public CreateBodyOPDialog(Shell shell, DialogStubBean dialogStub, int configId) {
		super(shell, dialogStub, configId);
	}

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
    	try
    	{
    		IDataSet dataSet = getDataSetAll();
    	    /* [CF-3537] [20230131]	[��������]MECO ���� �ź� �� ���� ���� �Ұ� 
    	     * ���� �˻� ȭ�鿡�� �ݷ��� MECO�� �˻� �ȵǴ� ������ �־ �Ʒ� �������� ���� 
    	    isWorkingStatus�� �ݷ��� MECO�� ���� �� �ְ� ���� ���� SearchTypeItemView���� MecoSearchView �˻�â���� ����  �Ʒ� getValue�κп� ȭ���� key���� �Ӽ��� ����*/
//    		Object mecoID = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
    		Object mecoID = dataSet.getValue(SDVPropertyConstant.MECO_SELECT, SDVPropertyConstant.MECO_NO);
    		
    		String vehicle_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE);
    		String shop = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_SHOP);
    		String station_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_STATION_CODE);
    		String op_code = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_OPERATION_CODE);
    		String bop_version = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_BOP_VERSION);
    		String kor_name = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_KOR_NAME);
    		String eng_name = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_ENG_NAME);
    		Object is_alt = dataSet.getValue("createOPView", SDVPropertyConstant.OPERATION_REV_IS_ALTBOP);
//    		String alt_prefix = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
//    		String dr = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_DR);
//    		String kpc = dataSet.getStringValue("createOPView", SDVPropertyConstant.OPERATION_REV_KPC);
//    		String worker_cnt = dataSet.getStringValue("createOPView", SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);

    		if (is_alt != null && ! is_alt.toString().toUpperCase().equals("TRUE") && (mecoID == null || mecoID.toString().trim().length() == 0))
    		{
    			showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
    			return false;
    		}

            if (is_alt != null && ! is_alt.toString().toUpperCase().equals("TRUE") && (mecoID != null && mecoID.toString().startsWith("MEW")))
            {
            	showErrorMessage(registry.getString("MECOTypeOnlyWeldOP.MESSAGE", "MECO Type is only for Weld Operation."), null);
            	return false;
            }

	    	if (vehicle_code == null || vehicle_code.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_VEHICLE_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (shop == null || shop.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_SHOP) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (station_code == null || station_code.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_STATION_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

    		if (op_code == null || op_code.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_OPERATION_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (kor_name == null || kor_name.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_KOR_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

    		if (eng_name == null || eng_name.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_ENG_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (bop_version == null || bop_version.trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, SDVPropertyConstant.OPERATION_REV_BOP_VERSION) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}
    	}
    	catch (Exception ex)
    	{
    		showErrorMessage(ex.getMessage(), ex);
    		return false;
    	}

    	return true;
    }
}
