/**
 * 
 */
package com.symc.plm.me.sdv.dialog.common;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.view.common.ReviseView;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : ReviseDialog
 * Class Description : 
 * @date 2013. 11. 20.
 *
 */
public class ReviseDialog extends SimpleSDVDialog {
	Registry registry = Registry.getRegistry(ReviseDialog.class);

    /**
     * @param shell
     * @param dialogStub
     */
    public ReviseDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck()
    {
    	try
    	{
    	    /* [CF-3537] [20230131]	[��������]MECO ���� �ź� �� ���� ���� �Ұ�
    	     *  ���� �˻� ȭ�鿡�� �ݷ��� MECO�� �˻� �ȵǴ� ������ �־ �Ʒ� �������� ���� 
    	    isWorkingStatus�� �ݷ��� MECO�� ���� �� �ְ� ���� ���� SearchTypeItemView���� MecoSearchView �˻�â���� ����   �Ʒ� getValue�κп� ȭ���� key���� �Ӽ��� ����*/
//    		Object mecoID = getSelectDataSet("reviseMecoView").getValue(SDVPropertyConstant.SHOP_REV_MECO_NO);
    		Object mecoID = getSelectDataSet("reviseMecoView").getValue(SDVPropertyConstant.MECO_NO);
    		Object skipMECO = getSelectDataSet("reviseView").getValue("SkipMECO");
    		if (! ((Boolean) skipMECO) && mecoID == null)
    		{
    			showErrorMessage("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."), null);
    			return false;
    		}
    		

    		if (getSelectDataSet("reviseView").getValue("reviseView") == null || getSelectDataSet("reviseView").getListValue("reviseView", "reviseView").size() == 0)
    		{
    			showErrorMessage(registry.getString("ReviseTargetIsNull.MESSAGE", "Revise target list is null."), null);
    			return false;
    		}

    		ReviseView reviseView = (ReviseView) getView("reviseView");
    		
    		
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Non-SR ������ ���� ��û 
			// Plant �������� ���� �ϱ�� ���� ���μ����� �ٲ�
			// Plant �����ÿ��� MECO �� �ʿ� ���� 
			// MECO ��ȣ�� ���� Skip MECO �� True �ϰ�� ���� Validation ���
			if (((Boolean) skipMECO) && mecoID == null )
			{
				return true;
			}
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    		
    		// [SR141208-036] [20150122] ymjang �������� �߰� ������ ���� �Ϲݰ��� ���� ���� �߻� �� MEP�� MEW�� MECO�� ������ �����ؾ߸� ��.
    		// MEW MECO �� ���� ������ �����ϵ��� ������.
    		if (mecoID.toString().startsWith("MEW") && !reviseView.isWeldOPOnlyMECOMEW())
    		{
    			showErrorMessage(registry.getString("SelectMEWMECOType.MESSAGE", "MEW type MECO is only available to WeldPoint Operation."), null);
    			return false;
    		}
    		
    		// ��� �׸��� �����Ͻðڽ��ϱ�?
    		int ret = ConfirmDialog.prompt(getShell(), registry.getString("ReviseAllConfirm.TITLE", "Confirmation"), registry.getString("ReviseAllConfirm.MESSAGE", "��� �׸��� �����Ͻðڽ��ϱ�?"));

    		if (ret != 2)
    			return false;
    	}
    	catch (Exception ex)
    	{
    		showErrorMessage(ex.getMessage(), ex);
    		return false;
    	}

    	return true;
    }
}
