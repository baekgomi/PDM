/**
 * 
 */
package com.symc.plm.me.sdv.dialog.body;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : CreateBodyShopDialog
 * Class Description : 
 * [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
 * 
 * @date 2013. 11. 11.
 *
 */
public class CreateBodyShopDialog extends SimpleSDVDialog {
	Registry registry = Registry.getRegistry(CreateBodyShopDialog.class);
	IDataMap mecoMap;
	IDataMap shopMap;

    /**
     * @param shell
     * @param dialogStub
     */
    public CreateBodyShopDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

	/**
	 * @param shell
	 * @param dialogStub
	 * @param configId
	 */
	public CreateBodyShopDialog(Shell shell, DialogStubBean dialogStub, int configId) {
		super(shell, dialogStub, configId);
	}

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck()
    {
    	try
    	{
    		IDataSet dataSet = getDataSetAll();
    	    /* [CF-3537] [20230131]	[개선과제]MECO 결재 거부 후 공법 개정 불가
    	     *  기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
    	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경  아래 getValue부분에 화면의 key값과 속성값 변경 */
//    		Object mecoID = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
    		Object mecoID = dataSet.getValue(SDVPropertyConstant.MECO_SELECT, SDVPropertyConstant.MECO_NO);
    		
    		Object shop_code = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_SHOP_CODE);
    		Object product_code = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
    		Object jph = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_JPH);
    		Object allowance = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_ALLOWANCE);
    		Object is_alt = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_IS_ALTBOP);
    		Object alt_prefix = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
    		Object kor_name = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_KOR_NAME);
    		Object eng_name = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_KOR_NAME);
//    		Object vehicle_kor_name = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME);
//    		Object vehicle_eng_name = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME);

            if (is_alt != null && ! is_alt.toString().toUpperCase().equals("TRUE") && (mecoID == null || mecoID.toString().trim().length() == 0))
    		{
    			showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
    			return false;
    		}

            if (is_alt != null && ! is_alt.toString().toUpperCase().equals("TRUE") && (mecoID != null && mecoID.toString().startsWith("MEW")))
            {
            	showErrorMessage(registry.getString("MECOTypeOnlyWeldOP.MESSAGE", "MECO Type is only for Weld Operation."), null);
            	return false;
            }

    		if (shop_code == null || shop_code.toString().trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_SHOP_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (is_alt != null && is_alt.toString().toUpperCase().equals("TRUE") && (alt_prefix == null || alt_prefix.toString().trim().length() == 0))
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_ALT_PREFIX) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

//    		product_code = "aaa";
	    	if (product_code == null || product_code.toString().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_PRODUCT_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}
//	    	else
//	    	{
//	    		if (! (product_code instanceof TCComponent))
//	    		{
//	    			try
//	    			{
//		    			if (SDVBOPUtilities.FindItem(product_code.toString(), SDVTypeConstant.EBOM_PRODUCT_ITEM) == null)
//			    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_PRODUCT_CODE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
//			    		return false;
//	    			}
//	    			catch (Exception ex)
//	    			{
//	    				showErrorMessage(ex.getMessage(), ex);
//	    				return false;
//	    			}
//	    		}
//	    	}

	    	// [SR140723-010][20140717] shcho, m7_JPH 속성의 타입을 정수에서 부동 소수점으로 변경. 소수점포함5자리까지 입력가능.
	    	if (jph == null || jph.toString().trim().length() == 0 || Double.valueOf(jph.toString()) <= 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_JPH) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (allowance == null || allowance.toString().trim().length() == 0 || Double.valueOf(allowance.toString()) <= 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_ALLOWANCE) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (kor_name == null || kor_name.toString().trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_KOR_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

	    	if (eng_name == null || eng_name.toString().trim().length() == 0)
	    	{
	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_ENG_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
	    		return false;
	    	}

//	    	if (vehicle_kor_name == null || vehicle_kor_name.toString().trim().length() == 0)
//	    	{
//	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
//	    		return false;
//	    	}
//
//	    	if (vehicle_eng_name == null || vehicle_eng_name.toString().trim().length() == 0)
//	    	{
//	    		showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
//	    		return false;
//	    	}
    	}
    	catch (Exception ex)
    	{
    		showErrorMessage(ex.getMessage(), ex);
    		return false;
    	}

    	return true;
    }
}
