/**
 * 
 */
package com.symc.plm.me.sdv.dialog.common;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.SimpleSDVDialog;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.dialog.body.CreateBodyOPDialog;
import com.symc.plm.me.sdv.view.common.ManageActivityView;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR141219-020][20150108] shcho, Open with Time â������ Activity �۾����� ����ġ �� ���� ���� �Ұ� ���� �ű� ȭ�� �߰�
 */
public class ManageActivityDialog extends SimpleSDVDialog {
    Registry registry = Registry.getRegistry(CreateBodyOPDialog.class);
    protected ManageActivityView manageActivityView;
    private ArrayList<HashMap<String, Object>> newTableList;

    /**
     * @param shell
     * @param dialogStub
     */
    public ManageActivityDialog(Shell shell, DialogStubBean dialogStub) {
        super(shell, dialogStub);
    }

    /**
     * @param shell
     * @param dialogStub
     * @param configId
     */
    public ManageActivityDialog(Shell shell, DialogStubBean dialogStub, int configId) {
        super(shell, dialogStub, configId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.ui.dialog.AbstractSDVSWTDialog#validationCheck()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean validationCheck() {
        try {
        	// [SR190104-050]Activity ���� �ð� ������ ���� �ҽ� ���� - ������
        	
            IDataSet dataSet = getDataSetAll();

            // Activity�� Release �Ǿ����� �������� �ʴ´�.
            String ReleaseFlag = (String) dataSet.getValue("activityView", "ReleaseFlag");
            if (ReleaseFlag.equals("true")) {
                MessageBox.post(getShell(), "The activity has already been released.", "WARNING", MessageBox.WARNING);
                return false;
            }

            boolean changeFlag = false;
            Object oldDataMap = dataSet.getValue("activityView", "oldTableList");
            Object newDataMap = dataSet.getValue("activityView", "newTableList");
            ArrayList<HashMap<String, Object>> oldTableList = ((ArrayList<HashMap<String, Object>>) oldDataMap);
            newTableList = ((ArrayList<HashMap<String, Object>>) newDataMap);

            // String[] propertyNames = registry.getStringArray("table.column.search.id.body");

            // ������� Check
            if (oldTableList.size() != newTableList.size()) {
                // Table Item ���� �ٸ� ��� ���� �� ����
                changeFlag = true;
            }
            
            for (int i = 0; i < newTableList.size(); i++) {
                HashMap<String, Object> newTableMap = newTableList.get(i);

                // ������� Check2
                if (oldTableList.size() == newTableList.size()) {
                    HashMap<String, Object> oldTableMap = oldTableList.get(i);
                    for (String key : newTableMap.keySet()) {
                        // seq�� ������� üũ���� ���� �Ѵ�.
                        if (key.equals("seq")) {
                            continue;
                        }

                        if (!newTableMap.get(key).equals(oldTableMap.get(key))) {
                            changeFlag = true;
                            break;
                        }
                    }
                }

                // Data ����
                for (String key : newTableMap.keySet()) {
                    
                    // �ʼ� �Է� �Ӽ� Check
                    if (key.equals(SDVPropertyConstant.ACTIVITY_OBJECT_NAME) || key.equals(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)) {
                        String value = newTableMap.get(key).toString();
                        if (value == null || value.length() <= 0 || value.equals("0")) {
                            MessageBox.post(getShell(), key + " �Ӽ� ���� �ʼ� �Դϴ�.", "INFORMATION", MessageBox.INFORMATION);
                            return false;
                        }
                    }
                    
                    // m7_WORKERS �ڸ��� 10�ڸ� üũ
                    if (key.equals(SDVPropertyConstant.ACTIVITY_WORKER)) {
                        if (newTableMap.get(key).toString().length() > 10) {
                            MessageBox.post(getShell(), "Workers(Body) ���� 10�ڸ��� ���� �� �����ϴ�.", "INFORMATION", MessageBox.INFORMATION);
                            return false;
                        }
                    }

                    // //���� Name�� ����� ��� ���� Name�� Reset�Ѵ�.(���� ��ǥ ��)
                    // boolean objNameChange = true; //�̸��� ����Ǹ� true, �̸� ������ ������ false
                    // if (key.equals(SDVPropertyConstant.ACTIVITY_OBJECT_NAME)) {
                    // String newObjectName = newTableMap.get(key).toString();
                    // for(HashMap<String, Object> oldTableMap2 : oldTableList) {
                    // for(String key2 : oldTableMap2.keySet()) {
                    // if(key2.equals(SDVPropertyConstant.ACTIVITY_OBJECT_NAME)) {
                    // if(newObjectName.equals(oldTableMap2.get(key2).toString())) {
                    // objNameChange = false;
                    // break;
                    // };
                    // }
                    // }
                    // if(!objNameChange) {
                    // break;
                    // }
                    // }
                    // if(objNameChange) {
                    // newTableMap.put(SDVPropertyConstant.ACTIVITY_ENG_NAME, "");
                    // }
                    // }
                }
            }
            
            if (!changeFlag) {
                MessageBox.post(getShell(), "��������� �����ϴ�.", "INFORMATION", MessageBox.INFORMATION);
                return false;
            }
        } catch (Exception ex) {
            showErrorMessage(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    protected void applyPressed() {
        super.applyPressed();

        AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
        manageActivityView = (ManageActivityView) dialog.getView("activityView");
        manageActivityView.setOldTableList(newTableList);
    }

}
