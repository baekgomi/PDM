package com.symc.plm.me.sdv.view.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleEvent;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleListener;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

/**
 * [SR��ȣ][201411141530] shcho, Revise�� ������ �۾�ǥ�ؼ� ExcelView���� sheet ������ �ȵǴ� ���� ����
 * [SR��ȣ][20141215] shcho, ��������ǥ������ �۾�ǥ�ؼ��� ���������� LatestReleased DatasetUID�� �������� ���� �߰�
 * [SR150317-021][20150323] ymjang, ���� �۾�ǥ�ؼ� Republish ������� ����
 * [SR150312-024][20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
 * [NON-SR][20150410] shcho, langConfigId �� ���Ͽ� ��ư Ȱ��/��Ȱ�� �ϴ� ���� DialogID���� ���� �����ͼ� �������� �ĺ� ��  ��ư Ȱ��/��Ȱ�� �ϵ��� ����.
 * [SR150714-008][20150713] shcho, Excel Open�� ���� �ʴ� ���� ����
 * [NON-SR][20150714] shcho, Unlock ��ư Ȱ��ȭ (ExcelView Ȱ����)
 * 
 */
public abstract class ExcelView extends AbstractSDVViewPane {

    public final static String IID_AppEvents = "{00024413-0000-0000-C000-000000000046}";

    public final static int WindowDeactivate = 0x00000615;
    public final static int WorkbookBeforeSave = 0x00000623;
//    public final static int SelectionChange = 0x00000616;
//    public final static int SheetActivate = 0x00000619;

    protected OleControlSite clientSite;
    protected OleControlSite appControlSite;

    protected Composite oleComposite;
    protected OleFrame frame;
    protected File currentFile;
    protected String filePath;
    protected String oldFilePath;
    

    protected int openMode = 1;

    protected TCComponentDataset targetDataset;

    protected int langConfigId;

    protected boolean isWritable = true;

    public Button btnOpen;

    /** unprotectWorkbook ��õ� Ƚ��  **/
    protected int unprotectTryCount = 0;
    
    // [NON-SR][20160205] taeku.jeong Excel File�� �� Dataset�� ���Ե� Item Revision ������ Ȯ���ϱ����� �߰�
	private TCComponentItemRevision itemRevision;
	private String datasetRelationName;
	
	private DatasetCloseWaiter datasetCloseWaiter; 

    public ExcelView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    public ExcelView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public TCComponentDataset getTargetDataset() {
        return targetDataset;
    }

    @Override
    protected void initUI(Composite parent){
        parent.setLayout(new BorderLayout());

        this.oleComposite = new Composite(parent, SWT.NONE);
        this.oleComposite.setLayoutData(BorderLayout.CENTER);
        this.oleComposite.setLayout(new FillLayout());

        this.frame = new OleFrame(this.oleComposite, SWT.NONE);
        this.frame.setLayout(new FillLayout());

        createOpenButton();

        if(getConfigId() == 1) {
            this.targetDataset = getDataset();
            if(targetDataset != null) {
                setFile();
            }
        }
    }

    public void setPreviewWritable(boolean isPreviewWritable)
    {
    	isWritable = isPreviewWritable;
    }

    public void setFile() {
        try {
            TCComponentTcFile[] files = targetDataset.getTcFiles();
            if(files != null && files.length > 0) {
                File file = files[0].getFile(null);
                if(file != null) {
                    this.filePath = file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openExcel() {
    	
    	System.out.println("View Case1");
    	
        if(closeExcelView()) {
            if(openExcelApplication()) {
            	// Excel Open ��ư�� ���� Excel�� ���� �ִµ��� ��� Button�� �� Ȱ��ȭ �Ѵ�.
                HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();
                for(String key : actionToolButtons.keySet()) {
                    ((Button) actionToolButtons.get(key)).setEnabled(false);
                }
            }
        }
    }

    /**
     * OEL Open�� Excel  View�� �ݴ´�.
     * @return
     */
    public boolean closeExcelView() {
        if(closeWorkbook()) {
            if(btnOpen == null) {
                createOpenButton();
            }

            HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();

            for(String key : actionToolButtons.keySet()) {
                Button button = actionToolButtons.get(key).getButton();
                if(key.equals("OpenExcel")) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }

            return true;
        }

        return false;
    }

    public void openExcelView() {
        if(openWorkbook()) {
            HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();
            for(String key : actionToolButtons.keySet()) {
                Button button = actionToolButtons.get(key).getButton();
                //[NON-SR][20150714] shcho, Unlock ��ư Ȱ��ȭ (ExcelView Ȱ����)
                if("Unlock".equals(key)) {
                    button.setEnabled(true);
                    continue;
                }
                
                if(!isWritable) {
                    if("ShowHideRibbon".equals(key) || "Close".equals(key)) {
                        button.setEnabled(true);
                    }
                } else {
                    //����
                	//[NON-SR][20150410] shcho, langConfigId �� ���Ͽ� ��ư Ȱ��/��Ȱ�� �ϴ� ���� DialogID���� ���� �����ͼ� �������� �ĺ� ��  ��ư Ȱ��/��Ȱ�� �ϵ��� ����.
                	String id = UIManager.getCurrentDialog().getId();
                    if(id.endsWith("EN")) {
                        if("ShowHideRibbon".equals(key) || "Save".equals(key) || "Close".equals(key)) {
                            button.setEnabled(true);
                        }
                        //����
                    } else {
                        //Released�� ��� ���� �� ���� ��Ȱ��
                        if(isReleasedOP()) {
                            if("ShowHideRibbon".equals(key) || "Close".equals(key)) {
                                button.setEnabled(true);
                            } else {
                                button.setEnabled(false);
                            }                            
                        } else {
                            button.setEnabled(true);
                        }
                    }
                }
            }
        }
    }

    public boolean closeWorkbook() {
        if(clientSite != null) {
            OleAutomation workbook = new OleAutomation(clientSite);
            unprotectWorkbook(workbook);

            workbook.dispose();

            this.clientSite.dispose();
            this.clientSite = null;
        }

        return true;
    }
    
    public boolean openExcelApplication() {
    	
    	System.out.println("ExcelView.openExcelApplication()");
    	
        boolean retVal = false;
        
        LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
        for(String key : buttons.keySet()) {
            buttons.get(key).getButton().setEnabled(false);
        }

        if(this.btnOpen != null) {
            this.btnOpen.dispose();
            this.btnOpen = null;
        }
        
        if(appControlSite != null) {
        	
        	if (appControlSite.isDirty()){ 
            	uploladFile();
        	}
        	
            appControlSite.dispose();
            appControlSite = null;
        }
        
		DatasetOpenForEditManager aDatasetOpenForEditManager = new DatasetOpenForEditManager(targetDataset, this.frame, isWritable);
		// ���������� �������� ������� Ȯ�� �Ѵ�. (�����۾�ǥ�ؼ� Preview �ΰ�� Message ������ �ʵ��� ó�� �Ѵ�.)
		if(aDatasetOpenForEditManager.isCanOpenEditAble()==false && getConfigId() != 1){
			MessageBox.post(this.frame.getShell(), "Excel������ �б� �������� �����ϴ�.", "Warning", MessageBox.WARNING);
		}
		
		// ���� �۾� ǥ�ؼ��� ��� ȭ���� Open-Ko ��ư�� Ȱ��ȭ �ǵ��� ���־���Ѵ�.......
		// �̰� �ذ�Ǿ�� ���������۾�ǥ�ؼ� �۾��� ������.
		
		datasetCloseWaiter = new DatasetCloseWaiter(aDatasetOpenForEditManager, this);
		
		// Thread ������ ������ ���� ��ٸ�.
		try {
			aDatasetOpenForEditManager.start();
			Thread.sleep(10);
			datasetCloseWaiter.start();
			// Thread�� �Ϸ� �ɶ� ���� ��ٸ���.
			//aDatasetOpenForEditManager.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        return false;
    }
    
    public void clossExcelApplicatoin(){
    	// Close ������ �ϸ�ȴ�.
    	
		try {
			// ����� ������ view ȭ�鿡 ǥ���ϱ����� �߿��� �κ���.
			this.currentFile = null;
			this.targetDataset.refresh();
			setFile();
		} catch (TCException e) {
			e.printStackTrace();
		}

		// [NON-SR][20161004] taeku.jeong UI ����ȭ�� ���� ""Problem Occurred" Dialog �����Ǵ� ��찡 ����.
		try {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					// Excel�� ���� ��ư�� Ȱ��ȭ �ǵ��� �Ѵ�.
					LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
					for(String key : buttons.keySet()) {
						if("OpenExcel".equals(key)) {
							buttons.get(key).getButton().setEnabled(true);
						}
					}
					
					// kbc ���� : btnOpen �� ���� �Ǿ������� �ٽ� ������ ����
			        // OLE Openȭ�鿡 ���̴� "�۾��׸�/������ �̹����� ���÷��� ���⸦ Ŭ�����ּ���" ��ư�� ����
					if(btnOpen == null) {
		                createOpenButton();
		            }
				}
	    	});
			
		} catch (Exception e) {
				e.printStackTrace();
		}

    }
    
    public boolean openExcelApplication_OLD() {
    	
        boolean retVal = false;
        
        LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
        for(String key : buttons.keySet()) {
            buttons.get(key).getButton().setEnabled(false);
        }

        if(this.btnOpen != null) {
            this.btnOpen.dispose();
            this.btnOpen = null;
        }

        if(appControlSite == null) {
            appControlSite = new OleControlSite(this.frame, SWT.NONE, "Excel.Application");
        }
        appControlSite.doVerb(OLE.OLEIVERB_OPEN);
        
        OleListener beforeSaveListener = new OleListener() {

            @Override
            public void handleEvent(OleEvent event) {
            	System.out.println("Ev1");
                isProcessSheetFile();
            }
        };

        OleListener windowDeactivateListener = new OleListener() {

            @Override
            public void handleEvent(OleEvent event) {
            	
            	System.out.println("Ev2 : ");
            	
                if(isProcessSheetFile()) {
                	
                	// [P0087] [20150130] ymjang TC Save ��ư�� ������ �ʰ�, ���� �ִ� Excel���α׷����� Save�ÿ��� �ٷ� TC�� Save �� �� �ֵ��� ��� ���� ��û
                	// ���� ����� ����� ���� ���ε� 
                	if (appControlSite.isDirty()) 
                    	uploladFile();
                	
                    if(appControlSite != null) {
                        appControlSite.dispose();
                        appControlSite = null;
                    }

                    // Excel�� ���� ��ư�� Ȱ��ȭ �ǵ��� �Ѵ�.
                    LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
                    for(String key : buttons.keySet()) {
                        if("OpenExcel".equals(key)) {
                            buttons.get(key).getButton().setEnabled(true);
                        }
                    }
                    
                    // OLE Openȭ�鿡 ���̴� "�۾��׸�/������ �̹����� ���÷��� ���⸦ Ŭ�����ּ���" ��ư�� ����
                    createOpenButton();
                }
            }
        };

        OleAutomation application = new OleAutomation(appControlSite);
        appControlSite.addEventListener(application, IID_AppEvents, WorkbookBeforeSave, beforeSaveListener);
        appControlSite.addEventListener(application, IID_AppEvents, WindowDeactivate, windowDeactivateListener);
        
        application.setProperty(application.getIDsOfNames(new String[] {"Visible"})[0], new Variant(true));

        OleAutomation workbooks = application.getProperty(application.getIDsOfNames(new String[] {"Workbooks"})[0]).getAutomation();
        Variant varResult = workbooks.invoke(workbooks.getIDsOfNames(new String[] {"Open"})[0], new Variant[] {new Variant(filePath)});
        
        if(varResult != null) {
            System.out.println(" copy invoke result of BSHEET = " + varResult);
            varResult.dispose();
            retVal = true;
            OleAutomation workbook = getAutoProperty(application, "ActiveWorkbook");
            protectWorkbook(workbook);
            workbook.dispose();
        } else {
            System.out.println("=====failed invoke copySheet method ====");
            retVal = false;
        }
        
        workbooks.dispose();
        application.dispose();
        
        return retVal;
    }  // Open Excel Application

    private boolean isProcessSheetFile() {
        boolean retVal = false;
        OleAutomation application = new OleAutomation(appControlSite);
        OleAutomation workbook = getAutoProperty(application, "ActiveWorkbook");
        String name = workbook.getProperty(workbook.getIDsOfNames(new String[] {"Name"})[0]).getString();

        System.out.println("filePath = "+filePath);
        System.out.println("name = "+name);
        
        if(filePath.endsWith(name)) {
            retVal = true;

            if(appControlSite != null) {
                unprotectWorkbook(workbook);
            }
        }

        workbook.dispose();
        application.dispose();

        return retVal;
    }

    public void openInViewer() {
        if(openWorkbook()) {
            HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();
            openMode = 0;

            for(String key : actionToolButtons.keySet()) {
                if(key.equals("OpenExcel")) {
                    actionToolButtons.get(key).getButton().setEnabled(false);
                } else {
                    if(key.equals("Close")) {
                        actionToolButtons.get(key).getButton().setToolTipText("Close");
                    }
                    actionToolButtons.get(key).getButton().setEnabled(true);
                }
            }
        }
    }

    public boolean openWorkbook() {
        if(this.btnOpen != null) {
            this.btnOpen.dispose();
            this.btnOpen = null;
        }

        try {
            if(this.currentFile == null || (currentFile!=null && currentFile.exists()==false)) {
            	
            	// [bc.kim] filePath�� null�ΰ�� ����ǥ�� �߰�
            	if( filePath != null ) {
            		currentFile = new File(filePath);
            		clientSite = null;
            	} else {
            		MessageBox.post( this.frame.getShell() , "�ش� ������ �۾�ǥ�ؼ� ������ �����ϴ�.", "Save", MessageBox.INFORMATION);
            		return false;
            	}
            }

            if(clientSite == null) {
                clientSite = new OleControlSite(frame, SWT.NONE, "Excel.Sheet", currentFile);
            }

            clientSite.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
            clientSite.setFocus();

            OleAutomation workbook = new OleAutomation(clientSite);

            // Workbook�� Dataset�� UID�� ���չ�����ȣ ����
            protectWorkbook(workbook);

            OleAutomation application = getAutoProperty(workbook, "Application");
            OleAutomation activeWindow = getAutoProperty(application, "ActiveWindow");
            activeWindow.setProperty(property(activeWindow, "Zoom"), new Variant(60));
            activeWindow.dispose();

            OleAutomation activeSheet = getAutoProperty(workbook, "ActiveSheet");
            if(hasProperty("activeSheet", activeSheet, "Range")) {
                OleAutomation range = getAutoProperty(activeSheet, "Range", new Variant[] { new Variant("A1") });
                if(hasProperty("Range", range, "Select")) {
                    invokeMethod("Range", range, "Select", null);
                }
                range.dispose();
            }

            showHideRibbonMenu(application, true);

            activeSheet.dispose();
            workbook.dispose();
            application.dispose();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    abstract public void addSheet();

    abstract public TCComponentDataset getDataset();

    public void removeSheet() {
        OleAutomation workbook = new OleAutomation(clientSite);
        OleAutomation activeSheet = getAutoProperty(workbook, "ActiveSheet");
        String sheetName = getStringProperty(activeSheet, "Name");

        Variant varResult = invokeMethod(sheetName, activeSheet, "Delete", null);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            System.out.println(" Delete invoke result of [" + sheetName + "] = " + varResult);
            boolean result = varResult.getBoolean();
            System.out.println("=====deleteSheet= result === " + result);
            varResult.dispose();
        } else {
            System.out.println("=====failed invoke deleteSheet method ====");
        }
        activeSheet.dispose();
        workbook.dispose();
    }

    public void showHideRibbon() {
        OleAutomation workbook = new OleAutomation(clientSite);
        String mainSheetName = getStringProperty(workbook, "Name");

        OleAutomation application = getAutoProperty(workbook, "Application");

        Variant varResult = null;
        Variant[] param = new Variant[1];

        boolean flag = true;
        param[0] = new Variant("Get.ToolBar(7," + Dquotation("Ribbon") +")");
        varResult = invokeMethod(mainSheetName, application, "ExecuteExcel4Macro", param);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            flag = varResult.getBoolean();
            showHideRibbonMenu(application, flag);
        } else {
            System.out.println("=====failed invoke copy method ====");
        }

        varResult.dispose();
        workbook.dispose();
        application.dispose();
    }

    protected void setEnable(Composite parent) {
        Control[] controls = parent.getChildren();
        if(controls != null && controls.length > 0) {
            for(Control control : controls) {
                if(control instanceof Composite) {
                    control.setEnabled(true);
                    setEnable((Composite) control);
                }
            }
        }
    }

    public void showHideRibbonMenu(OleAutomation application, boolean flag) {
        Variant varResult = null;
        Variant[] param = new Variant[1];

        if(flag) {
            param[0] = new Variant("SHOW.TOOLBAR(" + Dquotation("Ribbon") + ",False)");
            System.out.println("====flag " + flag + "=========");
        } else {
            param[0] = new Variant("SHOW.TOOLBAR(" + Dquotation("Ribbon") + ",True)");
            System.out.println("====flag " + flag + "=========");
        }

        varResult = invokeMethod("", application, "ExecuteExcel4Macro", param);

        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            varResult.dispose();
        } else {
            System.out.println("=====failed invoke copy method ====");
        }

        unlock();
    }

    public String Dquotation(String strData){
        return "\"" + strData + "\"";
    }

    /**
     * [SR��ȣ][20141217] shcho, NamedReference List�� ����� ���� imanFile�� (reference �Ǿ��־�) ���� ���ϴ� ��찡 �־ ���� �˸� �޽��� ���� ������.
     */
    public void saveWorkbook() {
        this.clientSite.save(this.currentFile, true);

        if(targetDataset != null) {
            try {
                Vector<File> files = new Vector<File>();
                files.add(currentFile);

                SYMTcUtil.removeAllNamedReference(targetDataset);
                SYMTcUtil.importFiles(targetDataset, files);
                MessageBox.post(this.frame.getShell(), "���� �Ǿ����ϴ�.", "Save", MessageBox.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.post(this.frame.getShell(), "������ ���� ���߽��ϴ�.\n System �����ڿ��� �����ϼ���.\n " + e.getMessage(), "Save", MessageBox.ERROR);
            }
        }

        if(this.clientSite != null) {
            this.clientSite.setFocus();
        }
    }

    protected Variant invokeMethod(String autoName, OleAutomation auto, String methodName, Variant[] varArgs, String[] rgVarDispNames) {
        Variant varResult = null;
        if (auto != null) {
            int[] methodDspIDs = auto.getIDsOfNames(new String[] { methodName });
            System.out.println("Invoke [" + methodName + "] Method of " + autoName);
            if (methodDspIDs != null) {
                System.out.println(autoName + " will invoked method [" + methodName + " | ID=" + methodDspIDs[0] + "] ");

                if (varArgs != null) {
                    int[] rgVarDispIDs = null;
                    if (rgVarDispNames != null) {
                        rgVarDispIDs = propertyIDs(auto, rgVarDispNames);
                        if (rgVarDispIDs != null) {
                            for (int id : rgVarDispIDs) {
                                System.out.println("variantArgName [" + id + "= " + auto.getName(id));
                            }
                        }
                    }

                    if (rgVarDispIDs != null) {
                    	// �����̸��� ���ϸ��� �޼�������� ã�Ƽ� �޼ҵ� ���̵� �׻� ���� ���´�.
                        int[] rgVarDispIDs2 = new int[rgVarDispIDs.length - 1];
                        for (int id = 1; id < rgVarDispIDs.length; id++) {
                            rgVarDispIDs2[id - 1] = id;
                        }
                        varResult = auto.invoke(methodDspIDs[0], varArgs, rgVarDispIDs2);
                    } else {
                        varResult = auto.invoke(methodDspIDs[0], varArgs);
                    }
                } else {
                    varResult = auto.invoke(methodDspIDs[0]);
                }
            } else {
                System.out.println(autoName + " has not this method [" + methodName + "] ");
            }
        }
        return varResult;
    }

    protected Variant invokeMethod(String autoName, OleAutomation auto, String methodName, Variant[] varArgs) {
        return invokeMethod(autoName, auto, methodName, varArgs, null);
    }

    protected OleAutomation getAutoProperty(OleAutomation auto, int dispId) {
        return getAutoProperty(auto, dispId, null);
    }

    protected OleAutomation getAutoProperty(OleAutomation auto, String name) {
        return getAutoProperty(auto, name, null);
    }

    protected OleAutomation getAutoProperty(OleAutomation auto, String name, Variant[] values) {
        return getAutoProperty(auto, property(auto, name), values);
    }

    /**
     *
     * @method getAutoProperty
     * @date 2013. 9. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected OleAutomation getAutoProperty(OleAutomation auto, int dispId, Variant[] values) {
        Variant varResult = null;
        varResult = (values != null) ? auto.getProperty(dispId, values) : auto.getProperty(dispId);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            OleAutomation result = varResult.getAutomation();
            varResult.dispose();
            return result;
        }
        return null;
    }

    protected int property(OleAutomation auto, String name) {
        return auto.getIDsOfNames(new String[] { name })[0];
    }

    protected int[] propertyIDs(OleAutomation auto, String[] names) {
        return auto.getIDsOfNames(names);
    }

    protected String getStringProperty(OleAutomation auto, String name) {
        return getStringProperty(auto, property(auto, name));
    }

    protected String getStringProperty(OleAutomation auto, int id) {
        String result = null;
        Variant varResult = auto.getProperty(id);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            result = varResult.getString();
            varResult.dispose();
        }
        return result;
    }

    protected long getLongProperty(OleAutomation auto, String name) {
        return getLongProperty(auto, property(auto, name));
    }

    protected long getLongProperty(OleAutomation auto, int id) {
        long result = 0;
        Variant varResult = auto.getProperty(id);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            result = varResult.getLong();
            varResult.dispose();
        }
        return result;
    }

    /**
     *
     * @method hasProperty
     * @date 2013. 9. 26.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    protected boolean hasProperty(String parentName, OleAutomation auto, String propertyName) {
        return hasProperty(parentName, auto, propertyName, true);
    }

    /**
     *
     * @method hasProperty
     * @date 2013. 9. 29.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    protected boolean hasProperty(String parentName, OleAutomation auto, String propertyName, boolean showInfo) {
        boolean result = false;
        if (auto != null) {

            int[] ids = propertyIDs(auto, new String[] { propertyName });
            result = (ids != null && ids.length > 0);
            if(!showInfo) return result;
            if (result) {
                System.out.println(" [ " + parentName + " ] has property [" + propertyName + "] ID = " + ids[0]);
            } else {
                System.out.println(" [ " + parentName + " ] has not property [" + propertyName + "] ============ ");
            }
        }
        return result;
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
        return getLocalDataMap();
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    public void setTargetDataset(TCComponentDataset dataset) {
        this.targetDataset = dataset;
        setItemRevsion(dataset);
        
        try {
            TCComponentTcFile[] files = dataset.getTcFiles();
            if(files != null && files.length > 0) {
                File file = files[0].getFile(null);
                if(file != null) {
                    this.filePath = file.getAbsolutePath();
                    this.currentFile = new File(this.filePath);

                    // [P0087] [20150130] ymjang TC Save ��ư�� ������ �ʰ�, ���� �ִ� Excel���α׷����� Save�ÿ��� �ٷ� TC�� Save �� �� �ֵ��� ��� ���� ��û
                    // File ���� ���θ� üũ�ϱ� ���Ͽ� ���� ������ tmp ������ ������.
                    /*
                    if (file != null)
                    {
                        this.oldFilePath = fileCopy(file);
                    }
                    */
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * �����۾�ǥ�ؼ�, ��������ǥ ���� Excel Sheet ��Ʈ��ȣ ��ȣ�� �����Ҷ� ��ȣ�� Item�� PUID�� ���Ҷ�
     * �ʿ��� ������. 
     * @param dataset
     */
    private void setItemRevsion(TCComponentDataset dataset){
		
		String[] targetTypes = new String[]{};
		String[] relationNames = new String[]{
					SDVTypeConstant.PROCESS_SHEET_KO_RELATION,
					SDVTypeConstant.PROCESS_SHEET_EN_RELATION,
					SDVTypeConstant.WELD_CONDITION_SHEET_RELATION
				};

		TCComponentItemRevision itemRevision = null;
		String relationName = null;
		try {
			AIFComponentContext[] referenced = dataset.whereReferencedByTypeRelation( null, relationNames);

			for (int i = 0;referenced!=null && i < referenced.length; i++) {
				TCComponent component = (TCComponent)referenced[i].getComponent();
				if(component!=null && component instanceof TCComponentItemRevision){
					itemRevision = (TCComponentItemRevision)component;
					relationName = referenced[i].getContext().toString();
					// ���࿡ �������� �˻��Ǵ� ��쿡 ���� ����� �ʿ��ϸ� �̺κ� ���Ͽ��� �߰� ���� �ؾ� �Ѵ�.
					break;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
    	
		if(itemRevision!=null){
			this.itemRevision = itemRevision;
			this.datasetRelationName = relationName;
		}
    }

   
    /**
     * [P0087] [20150130] ymjang TC Save ��ư�� ������ �ʰ�, ���� �ִ� Excel���α׷����� Save�ÿ��� �ٷ� TC�� Save �� �� �ֵ��� ��� ���� ��û
     * File ���� ���θ� üũ�ϱ� ���Ͽ� ���� ������ tmp ������ ������.
     * �̻��
     * @return
     */
	private String fileCopy(File inFile) {
		
		String outFilePath = null;
		
		try {
		   
			String inFilePath = inFile.getAbsolutePath();
			String inFileName = inFile.getName();
			
			int idx = inFilePath.lastIndexOf(File.separator);
			if (idx != -1) 
				outFilePath = inFilePath.substring(0, idx) + File.separator + "tmp";
			else
				outFilePath = inFilePath + File.separator + "tmp";
           
			File outFile = new File(outFilePath);
			if (!outFile.exists())
			{
				outFile.mkdir();
			}
			
			outFilePath = outFilePath + File.separator + inFileName;
			
			FileInputStream fis = new FileInputStream(inFilePath);
			FileOutputStream fos = new FileOutputStream(outFilePath);
	    
			int data = 0;
			while((data = fis.read())!=-1) {
				fos.write(data);
			}
			
			fis.close();
			fos.close();
	    
   		} catch (IOException e) {
    	    e.printStackTrace();
   		}
		
		return outFilePath;
	}
     	 
        
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if(result == SDVInitEvent.INIT_SUCCESS){
            
            /* [SR150714-008][20150713] shcho, Excel Open�� ���� �ʴ� ���� ����
             * Preview Open �Ϸ� �� ExcelView�� initalizeLocalData���� Excel Template ���� ������ �ϰ� �Ǵµ�, 
             * ����ڰ� Preview Open ���� (initalizeLocalDataȣ�� ����) Excel Open ��ư�� Ŭ���ϰ� �Ǹ� ������ ã�� �� ���� ������ �߻�
             * Excel Template ���� ������ Preview���� �ϵ��� ����
             * 
            if(getConfigId() == 0) {
                TCComponentDataset tcDataset = (TCComponentDataset)dataset.getData("exceltemplate");
                if(tcDataset != null){
                    setTargetDataset(tcDataset);
                }
            }
             */
            
        	/*
            * [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
            * [SR150714-008][20150713] shcho, Excel Open�� ���� �ʴ� ������ ���������� ExcelView�� ���ϴ� ��ư �鵵 �ʱ�ȭ �������� 
            *                                      ��, ����ڰ� Preview Open ���� (initalizeLocalDataȣ�� ����) Excel Open ��ư�� Ŭ���ϰ� �Ǹ� ���� ������ �߻�
            *                                      Excel Template ���� ������ ���������� Preview���� �ϵ��� ����
             * enabledButton();
             */
            
  
            
             /* ���� �ҽ� �ּ� ó����.
            HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();

            for(String key : actionToolButtons.keySet()) {
                Button button = actionToolButtons.get(key).getButton();
                if(key.equals("OpenExcel")) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }
            */
            
        }
    }

    @Override
    public void uiLoadCompleted() {

    }

    public boolean protectWorkbook(OleAutomation workbook) {
    	
    	System.out.println("Protection -----------");
    	System.out.println("Item Revision = "+getItemRevision());
    	boolean isItemRevisionMReleased = false;
    	String itemUid = null;
    	if(this.itemRevision!=null){
    		
    		try {
				itemUid = this.itemRevision.getItem().getUid();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
    		
    		System.out.println("Relation nam : "+this.datasetRelationName);
			try {
				String targetStatusName = "M Released";
				TCComponentReleaseStatus status = SYMTcUtil.getStatusObject(this.itemRevision, targetStatusName);
				if(status!=null){
					isItemRevisionMReleased = true;
					System.out.println("Status Name = "+targetStatusName);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
    	}
    	
    	boolean isEngWorkInsSheet = false;
    	if(this.datasetRelationName!=null){
    		if(this.datasetRelationName.trim().equalsIgnoreCase(SDVTypeConstant.PROCESS_SHEET_EN_RELATION)==true ){
    			isEngWorkInsSheet = true;
    		}
    	}
    	
		if(isItemRevisionMReleased == true && isEngWorkInsSheet==false){
			// Protection ���� �ص��˴ϴ�.
		}else{
			// Protection ���� �ϸ� �ȵ˴ϴ�.
		}
    	
		System.out.println("Item Uid" + itemUid );

        //Variant varResult = invokeMethod("workbook", workbook, "Protect", new Variant[] { new Variant(targetDataset.getUid()), new Variant(true)});
        Variant varResult = invokeMethod("workbook", workbook, "Protect", new Variant[] { new Variant(itemUid), new Variant(true), new Variant(false)});
        boolean result = false;
        if(varResult!=null){
        	System.out.println("varResult.toString() = "+varResult.toString());
        }
        
        if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            System.out.println("Protect invoke result = " + varResult);
            result = varResult.getBoolean();
            varResult.dispose();
        } else {
            System.out.println("=====failed invoke protectWorkbook method ====");
        }

        return result;
    }
    

    public boolean unprotectWorkbook(OleAutomation workbook) {
    	
    	String itemUid = null;
    	TCComponentItemRevision tempItemRevision = getItemRevision();
    	if(tempItemRevision!=null){
    		try {
				itemUid = tempItemRevision.getItem().getUid();
			} catch (TCException e) {
				e.printStackTrace();
			}
    	}
        return unprotectWorkbook(workbook, itemUid);
    }
    
    /**
     * [SR��ȣ][201411141530] shcho, Revise�� ������ �۾�ǥ�ؼ� ExcelView���� sheet ������ �ȵǴ� ���� ����
     *      �̹����������� �� workbook�� Ư�� ��ȣ(Dataset UID)�� ����ִ�. 
     *      ������ Revise �� ��� �̹������������� �״�� �����ϰ� �ǹǷ� ��ȣ�� ���� Revision �� �پ��ִ°Ͱ� �����ϴ�.
     *      �׷��� unprotectWorkbook�� �� Revision�� Dataset UID�� ������ üũ�ϹǷ� ��������� �� ���� ����. 
     *      �̸� �ذ��ϰ��� unprotectWorkbook�� ���� �� ���, ���� Revision�� Dataset UID�� ������ 1ȸ �� ���ȣ�� �ϵ��� �Ͽ���.
     *      
     * @param workbook
     * @param datasetUid
     * @return
     */
    public boolean unprotectWorkbook(OleAutomation workbook, String datasetUid) {
    	
    	System.out.println("Un Protection -----------");
    	System.out.println("Item Revision = "+getItemRevision());
    	boolean isItemRevisionMReleased = false;
    	String itemUid = null;
    	if(this.itemRevision!=null){
    		
    		try {
				itemUid = itemRevision.getItem().getUid();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
    		
    		System.out.println("Relation nam : "+this.datasetRelationName);
			try {
				String targetStatusName = "M Released";
				TCComponentReleaseStatus status = SYMTcUtil.getStatusObject(this.itemRevision, targetStatusName);
				if(status!=null){
					isItemRevisionMReleased = true;
					System.out.println("Status Name = "+targetStatusName);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
    	}
    	
    	boolean isEngWorkInsSheet = false;
    	if(this.datasetRelationName!=null){
    		if(this.datasetRelationName.trim().equalsIgnoreCase(SDVTypeConstant.PROCESS_SHEET_EN_RELATION)==true ){
    			isEngWorkInsSheet = true;
    		}
    	}
    	
    	boolean isUnProtectAble = true;
		if(isItemRevisionMReleased == true){
			if(isEngWorkInsSheet==false){
				isUnProtectAble = false;
			}
		}
		
		System.out.println("Item uid");
        Variant varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant(itemUid) });
        boolean result = false;
        if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            System.out.println("Unprotect invoke result = " + varResult);
            result = varResult.getBoolean();
            varResult.dispose();
        } else {
        	// Protection ��ȣ ���縦 ��� �õ� �غ����� ó�� �Ѵ�.
        	result = unProtectCaseByCase(varResult, workbook, getItemRevision());
        }
        return result;
    }
    
    /**
     * �ݺ��ؼ� ��� Protection ��ȣ�� �Է��� ����.
     * @param varResult
     * @param workbook
     * @param itemRevision
     * @return
     */
    public boolean unProtectCaseByCase(Variant varResult, OleAutomation workbook, TCComponentItemRevision itemRevision){
    	String datasetUid = getLatestReleasedDatasetUID();
    	String itemRevisionUid = itemRevision.getUid();
    	String baseOnRevisionUid = null;
    	try {
			TCComponentItemRevision baseOnRevision = itemRevision.basedOn();
			if(baseOnRevision!=null){
				baseOnRevisionUid = baseOnRevision.getUid();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
    	
    	boolean result = false;
    	
    	System.out.println("Item Revision uid = "+ itemRevisionUid);
    	varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant(itemRevisionUid) });
    	if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            System.out.println("Unprotect invoke result = " + varResult);
            result = varResult.getBoolean();
            varResult.dispose();
            unprotectTryCount = 0; //Count �ʱ�ȭ
    	}else{
    		System.out.println("Dataset uid = "+datasetUid);
    		unprotectTryCount++;
    		varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant(datasetUid) });
        	if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
                System.out.println("Unprotect invoke result = " + varResult);
                result = varResult.getBoolean();
                varResult.dispose();
                unprotectTryCount = 0; //Count �ʱ�ȭ
        	}else{
        		System.out.println("BaseonRevision uid = "+baseOnRevisionUid);
        		unprotectTryCount++;
        		varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant(baseOnRevisionUid) });
            	if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
                    System.out.println("Unprotect invoke result = " + varResult);
                    result = varResult.getBoolean();
                    varResult.dispose();
                    unprotectTryCount = 0; //Count �ʱ�ȭ
            	}else{
            		System.out.println("Basic uid = symc");
            		unprotectTryCount++;
            		varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant("symc") });
            		if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
                        System.out.println("Unprotect invoke result = " + varResult);
                        result = varResult.getBoolean();
                        varResult.dispose();
                        unprotectTryCount = 0; //Count �ʱ�ȭ
                	}else{
                   		System.out.println("=====failed invoke unprotectWorkbook method ====");
                	}
            	}
        	}
    	}
    	
    	return result;
    }

    public void unlock() {
        if(!frame.isEnabled()) {
            setEnable(getShell());
            clientSite.setFocus();
        }
    }

    private void createOpenButton() {
        btnOpen = new Button(this.frame, SWT.FLAT);
        btnOpen.setText("�۾��׸�/������ �̹����� ���÷��� ���⸦ Ŭ�����ּ���.");
        
        // [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
        // ������ ���, ������ ���¿� ���� ��ư Ȱ��/��Ȱ���� ������.
        enabledButton();
        
        btnOpen.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                openExcelView();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        frame.layout();
    }

    /**
     * [SR��ȣ][201411141530] shcho, LatestReleased ���� ��������  DatasetUID�� �������� �Լ� �ű� ����
     * [SR��ȣ][20141215] shcho, ��������ǥ������ �۾�ǥ�ؼ��� ���������� LatestReleased DatasetUID�� �������� ���� �߰�
     *      
     * @return
     */
    private String getLatestReleasedDatasetUID() {      
        try {
            AIFComponentContext[] arrAIFComponentContext = targetDataset.whereReferenced();
            for (AIFComponentContext aifComponentContext : arrAIFComponentContext) {
                InterfaceAIFComponent aifComponent = aifComponentContext.getComponent();
                if(aifComponent instanceof TCComponentItemRevision) {
                    TCComponentItemRevision itemRevision = (TCComponentItemRevision)aifComponent;
                    TCComponentItem item = itemRevision.getItem();
                    TCComponentItemRevision latestReleasedRevision = SYMTcUtil.getLatestReleasedRevision(item);
                    
                    TCComponent[] comps = null;

                    //���������� ���(��������ǥ)
                    if(itemRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV)) {
                    	if(latestReleasedRevision!=null){
                    		comps = latestReleasedRevision.getRelatedComponents(SDVTypeConstant.WELD_CONDITION_SHEET_RELATION);
                    	}
                    }
                    //�Ϲݰ����� ���(�۾�ǥ�ؼ�)
                    else {
                        if(langConfigId == 0) {
                        	if(latestReleasedRevision!=null){
                        		comps = latestReleasedRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                        	}
                        } else {
                        	if(latestReleasedRevision!=null){
                        		comps = latestReleasedRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
                        	}
                        }
                    }

                    if(comps != null && comps.length > 0) {
                        for(TCComponent comp : comps) {
                            if (comp instanceof TCComponentDataset) {
                                return ((TCComponentDataset) comp).getUid();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(this.frame.getShell(), e.getMessage(), "Excel", MessageBox.ERROR);
        } 
        
        return null;
    }

    /**
     * [P0087] [20150130] ymjang TC Save ��ư�� ������ �ʰ�, ���� �ִ� Excel���α׷����� Save�ÿ��� �ٷ� TC�� Save �� �� �ֵ��� ��� ���� ��û
     * ���� ����� ����� ���� ���ε�
     * @return
     */
    public boolean uploladFile() {
    	return true;
    }
    
    /**
     * [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
     */
    public void enabledButton() {
        return;
    }
    
    
    
    // [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
    // ������ ������ ���¸� üũ�Ѵ�.
    protected boolean isReleasedOP()
    {
        boolean isReleased = true;
        HashMap<String, Object> paramMap = null;
        String id = UIManager.getCurrentDialog().getId();
        IDialog dialog = UIManager.getAvailableDialog(id);
        Map<String, Object> parameters = dialog.getParameters();

        if(parameters == null)
            return false;
        
        // Search ȭ�鿡���� Preview �ÿ�
        if(parameters.containsKey("targetOperaion")) {
            paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");
            if(paramMap != null) {
                String date_released = (String) paramMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED);
                if (date_released.isEmpty())
                    isReleased = false;
                else
                    isReleased = true;
            }
        } 
        // BOP ������ Preview �ÿ�
        else {
            try {
                TCComponentBOPLine operationLine = (TCComponentBOPLine) AIFUtility.getCurrentApplication().getTargetComponent();
                Date pdate_released = operationLine.getItemRevision().getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
                if (pdate_released == null)
                    isReleased = false;
                else
                    isReleased = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return isReleased ;
        
    }    
    
    /**
     * [NON-SR][20160205] taeku.jeong Excel File�� �� Dataset�� ���Ե� Item Revision ������ Ȯ���ϱ����� �߰�
     * @return
     */
    public TCComponentItemRevision getItemRevision() {
		return itemRevision;
	}

    /**
     * [NON-SR][20160205] taeku.jeong Excel File�� �� Dataset�� ���Ե� Item Revision ������ Ȯ���ϱ����� �߰�
     * @param itemRevision
     */
	public void setItemRevision(TCComponentItemRevision itemRevision) {
		this.itemRevision = itemRevision;
	}

}
