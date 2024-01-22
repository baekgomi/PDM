/**
 * 
 */
package com.symc.plm.me.sdv.view.resource;

import java.awt.Frame;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Class Name : Resource_View_Table
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public class ResourceTableViewPane extends Composite {
    private TCTable table;
    private String queryName;
    private Label resultCountLabel;
    private static TCSession session;

    /*
     * QRY_dataset_display_option Value
     * Queries only the latest version of a dataset. Valid values are 1 to query all versions of a dataset, 2 to query only the latest version.
     */
    private static final String PREFERENCE_SERVICE_VALUE = "2";
    private static final String PREFERENCE_SERVICE_NAME = "QRY_dataset_display_option";

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     * @param tableHead
     */
    public ResourceTableViewPane(Composite parent, int style, String queryName) {
        super(parent, style);
        session = SDVBOPUtilities.getTCSession();
        this.queryName = queryName;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        Composite composite = new Composite(this, SWT.EMBEDDED);
        composite.setLayoutData(BorderLayout.CENTER);

        Frame frame = SWT_AWT.new_Frame(composite);

        Panel panel = new Panel();
        frame.add(panel);
        panel.setLayout(new java.awt.BorderLayout(0, 0));

        JRootPane rootPane = new JRootPane();
        panel.add(rootPane);
        JScrollPane scrollPane = new JScrollPane();

        try {

            TCComponentQuery queryComponent = getQueryComponent(queryName);
            // String[] columnNames = new String[]{SDVPropertyConstant.ITEM_ITEM_ID, SDVPropertyConstant.ITEM_REVISION_ID, SDVPropertyConstant.ITEM_OBJECT_TYPE, SDVPropertyConstant.ITEM_OBJECT_NAME, SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST};
            // table = new TCTable(session, headColumn);
            // table = new TCTable(arg0, arg1, arg2)
            // SYMC_Search_Equipment_Revision_ColumnPreferences
            // SYMC_Search_Subsidiary_Revision_ColumnPreferences
            String s = (new StringBuilder()).append(queryComponent.toString()).append("_").append("ColumnPreferences").toString();
            String s1 = (new StringBuilder()).append(queryComponent.toString()).append("_").append("ColumnWidthPreferences").toString();
            boolean isEnableDnDrop = false;

            table = new TCTable(session, s, s1, queryComponent.getQueryResultType().getType(), isEnableDnDrop);

            rootPane.getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
            scrollPane.getViewport().add(table);
            table.setEditable(false);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } catch (TCException e) {
            e.printStackTrace();
        }

        Composite composite2 = new Composite(this, SWT.NONE);
        composite2.setLayoutData(BorderLayout.SOUTH);
        composite2.setLayout(new FormLayout());
        composite2.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

        resultCountLabel = new Label(composite2, SWT.NONE);
        FormData fbresultCount = new FormData();
        fbresultCount.bottom = new FormAttachment(100);
        fbresultCount.right = new FormAttachment(100, -5);
        fbresultCount.width = 50;
        resultCountLabel.setLayoutData(fbresultCount);
        resultCountLabel.setAlignment(SWT.RIGHT);
        resultCountLabel.setText("0");
    }

    /**
     * @return
     * @throws TCException
     */
    protected TCComponentQuery getQueryComponent(String queryName) throws TCException {
        TCComponentQueryType queryType = (TCComponentQueryType) SDVBOPUtilities.getTCSession().getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(queryName);
        return query;
    }

    /**
     * �ڿ� �˻� ���� (SavedQuery�� ���)
     * 
     * @param queryName
     * @param searchConditionMap
     */
    public void searchResource(String queryName, HashMap<String, String> searchConditionMap) {
        // ���� �˻� ��� ����
        table.removeAllRows();

        // queryEntries, entryValues ����
        ArrayList<String> queryEntries = new ArrayList<String>();
        ArrayList<String> entryValues = new ArrayList<String>();
        for (String key : searchConditionMap.keySet()) {
            String value = searchConditionMap.get(key);
            if (value.length() > 0 && value != null) {
                queryEntries.add(key);
                entryValues.add(value);
            }
        }

        /*
         * queryEntries, entryValues ����
         * String[] queryEntries = {"Korean Name", "Spec Korean"};
         * String[] entryValues= {"�Ƿ�", ""};
         * String[] queryEntries = {"OwningUser", "OwningGroup"};
         * String[] entryValues= {"shcho", "MFG"};
         * TCComponent[] tcComponents = queryComponent(queryName, queryEntries, entryValues);
         */

        try {
            // ���� �˻� ���� (queryComponent)
            TCComponent[] tcComponents = queryComponent(queryName, queryEntries.toArray(new String[queryEntries.size()]), entryValues.toArray(new String[entryValues.size()]));

            // �˻� ��� table�� ����
            int searchedItemCount = 0;
            for (TCComponent component : tcComponents) {
                if (component instanceof TCComponentItemRevision) {
                    TCComponentItemRevision compItemRevision = (TCComponentItemRevision) component;
                    // ���� Revision�� �˻� ����� ���
                    if (compItemRevision.getProperty("item_revision_id").equals(compItemRevision.getItem().getLatestItemRevision().getProperty("item_revision_id"))) {
                        table.addRows(compItemRevision);
                        searchedItemCount++;
                    }
                }
            }

            // ��� Count
            resultCountLabel.setText(String.valueOf(searchedItemCount));

            // �˻� ����� ������ �޽��� ǥ��
            if (searchedItemCount == 0) {
                MessageBox.post(getShell(), "No Result.", "INFORMATION", MessageBox.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * �̹� ������� �ִ� Saved query�� �̿��Ͽ� imanComponent�� �˻��ϴ� method�̴�.
     * 
     * @param savedQueryName
     *            String ����� query name
     * @param entryName
     *            String[] �˻� ���� name(�������� name)
     * @param entryValue
     *            String[] �˻� ���� value
     * @return TCComponent[] �˻� ���
     * @throws Exception
     * 
     */
    public static TCComponent[] queryComponent(String savedQueryName, String[] entryName, String[] entryValue) throws Exception {

        int scope = TCPreferenceService.TC_preference_user;
//        session.getPreferenceService().setString(scope, PREFERENCE_SERVICE_NAME, PREFERENCE_SERVICE_VALUE);
        session.getPreferenceService().setStringValueAtLocation(PREFERENCE_SERVICE_NAME, PREFERENCE_SERVICE_VALUE, TCPreferenceLocation.convertLocationFromLegacy(scope));

        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(savedQueryName);
        String[] queryEntries = session.getTextService().getTextValues(entryName);
        for (int i = 0; queryEntries != null && i < queryEntries.length; i++) {
            if (queryEntries[i] == null || queryEntries[i].equals("")) {
                queryEntries[i] = entryName[i];
            }
        }
        return query.execute(queryEntries, entryValue);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    /**
     * TCTable�� ���õ� Item�� return �ϴ� �Լ�
     * 
     * @return InterfaceAIFComponent[]
     */
    public InterfaceAIFComponent[] getSelectedItems() {
        return table.getSelectedComponents();
    }

    /**
     * TCTable�� �ִ� ��� Component return�ϴ� �Լ�
     * 
     * @return AIFComponentContext[]
     */
    public AIFComponentContext[] getComponentList() {
        return table.getRows(0, table.getRowCount() - 1);
    }

    /**
     * TCTable�� ǥ�õ� ��� ���� return �ϴ� �Լ�
     * 20201118 seho table�� ���� �����ö� ������ %%�� ����ϸ� ���� ������ %%�� ������ ��� ��ĭ�� �и��� ���� �߻�. �����ڸ� %%�� �ϸ� �ȵȴ�.
     * 
     * @return List<List<String>>
     */
    public List<List<String>> getAllRowValues() {
//        String separator = "%%";
//        int[] columnIndexes = new int[table.getColumnCount()];
//        for (int i = 0; i < columnIndexes.length; i++) {
//            columnIndexes[i] = i;
//        }

//        return table.getRowData(separator, false, columnIndexes);
    	List<List<String>> allDataList = new ArrayList<List<String>>();
		List<String> columnList = new ArrayList<String>();
    	for(int c=0;c<table.getColumnCount();c++)
    	{
    		columnList.add(table.getColumnName(c));
    	}
		allDataList.add(columnList);
    	for(int i = 0;i<table.getRowCount();i++)
    	{
    		List<String> rowDataList = new ArrayList<String>();
    		Object[] rowObject = table.getRowData(i);
    		for(int j=0;j<rowObject.length;j++)
    		{
    			rowDataList.add(rowObject[j].toString());
    		}
    		allDataList.add(rowDataList);
    	}
    	return allDataList;
    }

}
