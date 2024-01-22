package com.symc.plm.me.sdv.view.report;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;


public class SOAReportMessageView extends AbstractSDVViewPane {

	private IDataMap localDataMap0;
	
    /**
     * @wbp.parser.constructor
     */
    public SOAReportMessageView(Composite parent, int style, String id) {
        super(parent, style, id);
        
        System.out.println("ID = "+id);

    }

    @Override
    protected void initUI(Composite parent) {
		/////////////////////////////////////////////////////////////////////////////
		/*
		* ������ : 20200330
		* �������� : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
		* 			  ����� "�۾�����" �߰��� ����   CheckBox �߰�
		*/
    	localDataMap0 = new RawDataMap();
    	Composite composite0 = new Composite(parent, SWT.NONE);
    	composite0.setLayout(new RowLayout(SWT.VERTICAL));
    	////////////////////////////////////////////////////////////////////////////
    	
        Composite composite = new Composite(composite0, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Label OpenOrSave_label = new Label(composite, SWT.NONE);
        OpenOrSave_label.setAlignment(SWT.CENTER);
        OpenOrSave_label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
        OpenOrSave_label.setText("Report ���� ���α׷��� Server���� ����ǰ� �� ����� E-Mail�� �޾� ���� �� �ֽ��ϴ�.\n"
        		+ "Ȯ�� ��ư�� �����ø� ������ Report ������ ��û �մϴ�.");
        
        //////////////////////////////////////////////////////////////////////////////
        /*
		* ������ : 20200330
		* �������� : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
		* 			  ����� "�۾�����" �߰��� ����   CheckBox �߰�
		*/
        IDialog dialog = UIManager.getCurrentDialog();
        String description = ((DialogStubBean) dialog.getStub()).getDescription();
        if( description.equals("Process Master List")) {
        	Composite composite1 = new Composite(composite0, SWT.NONE);
        	composite1.setLayout(new GridLayout(3, false));
        	
        	final Button workInfo_CheckButton = new Button(composite1, SWT.CHECK);
        	workInfo_CheckButton.addSelectionListener(new SelectionAdapter() {
        		@Override
        		public void widgetSelected(SelectionEvent e) {
        			if(workInfo_CheckButton.getSelection()) {
//                    selectedValue[0] = 1;
        				localDataMap0.put("selectedValue", 1, IData.OBJECT_FIELD);
        			} else {
//                    selectedValue[0] = 0;
        				localDataMap0.put("selectedValue", 0, IData.OBJECT_FIELD);
        			}
        		}
        	});
        	workInfo_CheckButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        	workInfo_CheckButton.setText("�۾� ����");
        }
        //////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void uiLoadCompleted() {

    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    /*
	* ������ : 20200330
	* �������� : MPP -> Assembly BOP -> Reports ->  Process Master List(On Server)
	* 			  ����� "�۾�����" �߰��� ����   CheckBox �߰�
	*/
    public IDataMap getLocalDataMap0() {
    	return localDataMap0;
    }
    //////////////////////////////////////////////////////////////////////////////////////

}
