package com.symc.plm.me.sdv.view.ps;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetUtils;
import com.symc.plm.me.sdv.view.excel.ExcelView;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

/**
 * [P0087] [20150130] ymjang TC Save ��ư�� ������ �ʰ�, ���� �ִ� Excel���α׷����� Save�ÿ��� �ٷ� TC�� Save �� �� �ֵ��� ��� ���� ��û
 */
public class ProcessSheetExcelView extends ExcelView {
	
	// ����, ���� �۾�ǥ�ؼ��� Preview �� �����ٶ� ���Ǵ� Excelview
	
    public ProcessSheetExcelView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    public ProcessSheetExcelView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }

	@SuppressWarnings("unchecked")
    @Override
    public TCComponentDataset getDataset() {
        HashMap<String, Object> paramMap = null;

        String id = UIManager.getCurrentDialog().getId();
        if(id.endsWith("EN")) {
            langConfigId = 1;
        } else {
            langConfigId = 0;
        }

        IDialog dialog = UIManager.getAvailableDialog(id);
        Map<String, Object> parameters = dialog.getParameters();

        TCComponentDataset dataset = null;
        TCComponentBOPLine operationLine = null;
        try {
            if(parameters != null && parameters.containsKey("targetOperaion")) {
                paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");

                if(paramMap != null) {
                    AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
                    if(application instanceof MFGLegacyApplication) {
                        TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
                        TCComponentBOPLine bopLine = (TCComponentBOPLine) bopWindow.getTopBOMLine();
                        if(bopLine != null) {
                            operationLine = getChildBopLine(bopLine, (String) paramMap.get(SDVPropertyConstant.ITEM_ITEM_ID));
                        }
                    }
                }
            } else {
                operationLine = (TCComponentBOPLine) AIFUtility.getCurrentApplication().getTargetComponent();
            }

            if(operationLine != null) {
                String[] propertyNames = new String[] {
                        SDVPropertyConstant.ITEM_ITEM_ID,
                        SDVPropertyConstant.ITEM_REVISION_ID
                };

                TCComponentItemRevision revision = operationLine.getItemRevision();
                String[] values = revision.getProperties(propertyNames);

                TCComponent[] comps = null;
                if(langConfigId == 0) {
                    comps = revision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                } else {
                    comps = revision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
                }

                if(comps != null && comps.length > 0) {
                    for(TCComponent comp : comps) {
                        if (comp instanceof TCComponentDataset) {
                            dataset = (TCComponentDataset) comp;
                            break;
                        }
                    }
                } else {
                    if(langConfigId == 0) {
                        dataset = SDVBOPUtilities.getTemplateDataset("M7_TEM_DocItemID_ProcessSheet_Kor", values[0] + "/" + values[1], values[0]);
                        revision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, dataset);
                    } else {
                        dataset = ProcessSheetUtils.translateProcessSheet(revision, ProcessSheetUtils.getProcessType(revision));
                        revision.add(SDVTypeConstant.PROCESS_SHEET_EN_RELATION, dataset);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Excel", MessageBox.ERROR);
        }

        return dataset;
    }

    private TCComponentBOPLine getChildBopLine(TCComponentBOPLine bopLine, String targetItemId) throws TCException {
        TCComponentBOPLine childLine = null;

        if(ProcessSheetUtils.isOperation(bopLine)) {
            String itemId = bopLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
            if (targetItemId.equals(itemId)) {
                childLine = bopLine;
            }
        } else {
            if(bopLine.getChildrenCount() > 0) {
                AIFComponentContext[] contexts = bopLine.getChildren();
                for(AIFComponentContext context : contexts) {
                    TCComponentBOPLine contextComp = (TCComponentBOPLine) context.getComponent();
                    if(ProcessSheetUtils.isLine(contextComp) || ProcessSheetUtils.isStation(contextComp) || ProcessSheetUtils.isOperation(contextComp)) {
                        childLine = getChildBopLine(contextComp, targetItemId);
                        if(childLine != null) {
                            break;
                        }
                    }
                }
            }
        }

        return childLine;
    }

    /**
     * [SR��ȣ����][20150105] shcho, Preview ȭ�鿡�� addSheet ��ư Ŭ���Ͽ� ���� �߰��� ������ǥ�� �߰� �� �� �ֵ��� ���� ���� 
     */
    @Override
    public void addSheet() {
        OleAutomation targetSheet = null;
        OleAutomation aSheetLast = null;
        OleAutomation bSheetLast = null;
        int bSheetCnt = 0;

        OleAutomation workbook = new OleAutomation(clientSite);
        OleAutomation sheets = getAutoProperty(workbook, "Sheets");
        if(sheets != null) {
            int count = (int) getLongProperty(sheets, "Count");
            if(count > 0) {
                for(int i = 1; i <= count; i++) {
                    Variant var = new Variant(i);
                    OleAutomation sheet = getAutoProperty(sheets, "Item", new Variant[] {var});
                    if(sheet != null) {
                        String name = getStringProperty(sheet, "Name");
                        if(name.startsWith("��") ||(name.startsWith("A") && !name.endsWith("SHEET"))) {
                            aSheetLast = sheet;
                        } else if(name.startsWith("��") ||(name.startsWith("B") && !name.endsWith("SHEET"))) {
                            bSheetCnt++;
                            bSheetLast = sheet;
                        } else if("BSHEET".equals(name)) {
                            targetSheet = sheet;
                        }
                    }
                }

                Variant[] params = new Variant[1];
                if(bSheetLast == null) {
                    params[0] = new Variant(aSheetLast);
                } else {
                    params[0] = new Variant(bSheetLast);
                }

                if(unprotectWorkbook(workbook)) {
                    Variant varResult = invokeMethod("BSHEET", targetSheet, "Copy", params, new String[] {"Copy", "After"});
                    if(varResult != null) {
                        System.out.println(" copy invoke result of BSHEET = " + varResult);

                        bSheetLast = getAutoProperty(sheets, "Item", new Variant[] {new Variant(bSheetCnt + 2)});

                        if(bSheetLast.setProperty(property(bSheetLast, "Name"), new Variant((UIManager.getCurrentDialog().getId().endsWith("EN") ? "B" : "��") + (++bSheetCnt)))) {
                            bSheetLast.setProperty(property(bSheetLast, "Visible"), new Variant(true));
                            bSheetLast.setProperty(property(bSheetLast, "Activate"), new Variant(true));
                        }

                        varResult.dispose();
                    } else {
                        System.out.println("=====failed invoke copySheet method ====");
                    }

                    protectWorkbook(workbook);
                }
            }
        }

        if(!frame.isEnabled()) {
            setEnable(frame.getShell());
            clientSite.setFocus();
        }
    }

    @Override
    public void removeSheet() {
        OleAutomation workbook = new OleAutomation(clientSite);
        OleAutomation activeSheet = getAutoProperty(workbook, "ActiveSheet");
        String sheetName = getStringProperty(activeSheet, "Name");

        if(sheetName.startsWith("��")) {
            MessageDialog.openError(AIFUtility.getActiveDesktop().getShell(), "Error", "������ ������ �� �����ϴ�.");
            return;
        }

        if(unprotectWorkbook(workbook)) {
            super.removeSheet();

            OleAutomation sheets = getAutoProperty(workbook, "Sheets");
            int bSheetCnt = 1;
            if(sheets != null) {
                int count = (int) getLongProperty(sheets, "Count");
                if(count > 0) {
                    for(int i = 1; i <= count; i++) {
                        Variant var = new Variant(i);
                        OleAutomation sheet = getAutoProperty(sheets, "Item", new Variant[] {var});
                        if(sheet != null) {
                            String name = getStringProperty(sheet, "Name");
                            if(name.startsWith("��")) {
                                sheet.setProperty(property(sheet, "Name"), new Variant("��" + bSheetCnt++));
                            }
                        }
                    }
                }
            }

            protectWorkbook(workbook);
        }

        if(!frame.isEnabled()) {
            setEnable(frame.getShell());
            clientSite.setFocus();
        }
    }

    @Override
    protected void validateConfig(int configId) {
        if(configId != 0 && configId != 1) {
            throw new SDVRuntimeException("View[" + getId() + " not supported config Id :" + configId);
        }
    }
    @Override
    public void openExcel() {
        if(closeExcelView()) {
            if(openExcelApplication()) {
                HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();
                for(String key : actionToolButtons.keySet()) {
                    ((Button) actionToolButtons.get(key)).setEnabled(false);
                }
            }
        }
    }
    
    /**
     * [P0087] [20150130] ymjang TC Save ��ư�� ������ �ʰ�, ���� �ִ� Excel���α׷����� Save�ÿ��� �ٷ� TC�� Save �� �� �ֵ��� ��� ���� ��û
     * ���� ����� ����� ���� ���ε�
     * @return
     */
    @Override
    public boolean uploladFile() {
    	
    	boolean isUploaded = false;
    	
        try 
        {
        	TCComponentDataset dataset = this.getDataset() ;
        	Vector<File> importFiles = new Vector<File>();
        	File tmpFile = new File(this.filePath);
        	//File tmpOldFile = new File(this.oldFilePath);
            importFiles.add(tmpFile);          
            
            if (dataset != null)
            //if (tmpFile.length() != tmpOldFile.length() && dataset != null)
            {
            	System.out.println("File Save!!!");
	            SYMTcUtil.removeAllNamedReference(dataset);
	            SYMTcUtil.importFiles(dataset, importFiles);
            }
            else
            {
            	System.out.println("Save Skip!!!");
            }
            isUploaded = true;
            		
		} catch (Exception e) {
			isUploaded = false;
			e.printStackTrace();
		}
        
        return isUploaded;
    }
    
    /**
     * [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
     * [SR150317-021] [20150323] ymjang, ���� �۾�ǥ�ؼ� Republish ������� ����
     * ������ ���, ������ ���¿� ���� ���� View�� ��ư Ȱ��/��Ȱ�� ó����.
     */
    @Override
    public void enabledButton() {

    	String id = UIManager.getCurrentDialog().getId();
    	
    	// ��ư(�۾� �׸�/������ �̹����� ���÷��� ���⸦ Ŭ���ϼ���)
		btnOpen.setEnabled(true);
        if(id.endsWith("EN")) 
        {
        	if (!isReleasedOP())
        		btnOpen.setEnabled(false);
        }

//        if(id.endsWith("KO")) 
//        {
//        	if (isReleasedOP())
//        		btnOpen.setEnabled(false);
//        }
        
        // ActionToolbar
        HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();
        
        for(String key : actionToolButtons.keySet()) {
            Button button = actionToolButtons.get(key).getButton();
            if(key.equals("OpenExcel")) {
            	if(id.endsWith("EN"))
            		if (!isReleasedOP())
            			button.setEnabled(false);
            		else 
            			button.setEnabled(true);

//            	if(id.endsWith("KO"))
//            		if (isReleasedOP())
//            			button.setEnabled(false);
//            		else 
//            			button.setEnabled(true);
            } else {
                button.setEnabled(false);
            }
        }
    }
 
}
