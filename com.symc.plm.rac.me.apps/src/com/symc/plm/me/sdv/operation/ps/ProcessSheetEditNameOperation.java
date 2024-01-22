package com.symc.plm.me.sdv.operation.ps;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleEvent;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleListener;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomActivityDao;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.view.excel.DatasetCloseWaiter;
import com.symc.plm.me.sdv.view.excel.DatasetOpenForEditManager;
import com.symc.plm.me.sdv.view.excel.ExcelView;
import com.symc.plm.me.sdv.view.ps.PreviewView;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ProcessSheetEditNameOperation extends AbstractSDVActionOperation {

    private Registry registry;

    private boolean saveFlag = true;
    private String filePath;
    private OleControlSite appControlSite;

    public ProcessSheetEditNameOperation(String actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
        registry = Registry.getRegistry(this);
    }

    public ProcessSheetEditNameOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {

    }

    @Override
    public void executeOperation() throws Exception {
        IDataSet dataset = getDataSet();
        if(dataset != null) {
            Collection<IDataMap> dataMaps = dataset.getAllDataMaps();
            if(dataMaps != null) {
                for(IDataMap dataMap : dataMaps) {
                    if(dataMap.containsKey("targetOperationList")) {
                        List<HashMap<String, Object>> opList = dataMap.getTableValue("targetOperationList");
                        if(opList != null) {
                            String viewId = null;
                            if(dataMap.containsKey("viewId")) {
                                viewId = dataMap.getStringValue("viewId");
                            }
                            editOperationName(viewId, opList);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void editOperationName(final String viewId, List<HashMap<String, Object>> opList) {
        File templateFile = ExcelTemplateHelper.getTemplateFile(ExcelTemplateHelper.EXCEL_OPEN, registry.getString("EditEnglishOperationNamePreference.Name"), null);
        if(templateFile != null) {
            try {
                Workbook workbook = new XSSFWorkbook(new FileInputStream(templateFile));
                Sheet sheet = workbook.getSheetAt(0);
                int operationCnt = opList.size();
                for(int i = 0; i < operationCnt; i++) {
                    HashMap<String, Object> dataMap = opList.get(i);
                    Row row = sheet.createRow(i + 1);
                    String itemId = (String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
                    String itemRevId = (String) dataMap.get(SDVPropertyConstant.ITEM_REVISION_ID);
                    String objectName = (String) dataMap.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME);
                    row.createCell(0).setCellValue(itemId);
                    row.createCell(1).setCellValue(itemRevId);
                    row.createCell(2).setCellValue(objectName);
                    try {
                        // ���� �������� ��� ������ ���� �������� �״�� ������
                        TCComponentItemRevision itemRevision = CustomUtil.findItemRevision(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, itemId, itemRevId);
//                        TCComponentItem item = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM);
                        if(itemRevision == null) {
                            throw new NullPointerException("���� " + itemId + "/" + itemRevId + "�� �������� �ʽ��ϴ�.");
                        }

                        String objectEngName = itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
                        Cell cell = row.createCell(3);
                        CellStyle cellStyle = cell.getCellStyle();
                        cellStyle.setLocked(false);
                        cell.setCellStyle(cellStyle);
                        if(objectEngName != null && objectEngName.length() > 0) {
                            cell.setCellValue(objectEngName);
                        } else {
                            cell.setCellValue(objectName);
                        }

                        // ���� ���� �������� ������ Setting
                        TCComponent[] revisions = itemRevision.getItem().getRelatedComponents("revision_list");
                        for(int j = 0 ; j < revisions.length; j++) {
                            String revId = revisions[j].getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                            if(j > 0 && revId.equals(itemRevId)) {
                                row.createCell(4).setCellValue(revisions[j - 1].getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME));
                                break;
                            }
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                FileOutputStream fos = new FileOutputStream(templateFile);
                workbook.write(fos);
                fos.flush();
                fos.close();

                filePath = templateFile.getAbsolutePath();
                
                
                /*
                 * [20170525] bc.k ������ OLE ���̺귯���� ����� �ҽ��� ������ �־� 
                 * OLE�� ���� �ʴ� ������� ���� ��ȯ
                 * ExcelViewŬ������ OpenExcel �޼����� ������ �̿��Ͽ� �ۼ�
                 * ��� : Excel ���� ��ɰ� Excel ���� ������ ����� WorkSheet�� �ݿ� �������� ����
                 */
                final TCComponentDataset tempDataset  = SDVBOPUtilities.createDataset(templateFile.getAbsolutePath(), templateFile.getName());
                final DatasetOpenForEditManager aDatasetOpenForEditManager = new DatasetOpenForEditManager(tempDataset);
                 
                // ������ Activity�� ������ �ѱ۷� ������ Excel Sheet �� Open �ϴ� ����
                Display.getDefault().syncExec(new Runnable() {
                	
                	@Override
                    public void run() {
                		
                		DatasetCloseWaiter datasetCloseWaiter = null;
                		ExcelView excelView = ((ExcelView)(AbstractSDVViewPane) UIManager.getCurrentDialog().getView("excelView"));
                		if( excelView != null ) {
                			datasetCloseWaiter = new DatasetCloseWaiter(aDatasetOpenForEditManager, excelView);
                		}
                
		                try {
		        			aDatasetOpenForEditManager.start();
		        			Thread.sleep(500);
		        			if( excelView != null ) {
		        				datasetCloseWaiter.start();
		        			} else {
		        				
		        			}
		        			// Thread�� �Ϸ� �ɶ� ���� ��ٸ���.
//		        			aDatasetOpenForEditManager.join();
		        		} catch (InterruptedException e) {
		        			e.printStackTrace();
		        		}
		                
                	   }
                });
                
                
                // Open �� Excel�� �����ǰ� ���� �� �� Dataset�� CheckIn �Ǵ� ���� ����Ǹ�
                // Excel ������ Sheet�� �ݿ��ϴ� ����
                Display.getDefault().syncExec(new Runnable() {

                    @Override
                    public void run() {
                    	try {
                    		Thread.sleep(1500);
		                    while ( true ) {
//		                    	System.out.println("aaaaaaa=================>");
            				// Dataset�� CheckIn �Ǵ� ���� while�� ���� ����
            				if ( aDatasetOpenForEditManager.isExitFlag() ) {
//            					Thread.sleep(500);
            					break;
            				}
            				Thread.sleep(1500);
                    }
                    } catch (InterruptedException e) {
                    	e.printStackTrace();
                    }
                    	try {
                    	Thread.sleep(4000);
                    	// Dataset ���� ������ �����Ͽ� filePath ������ ������ �Է�
                    	setFile( tempDataset );
                    	
                    	// ���۵� Activity ������ DB�� �ݿ��ϴ� ����
                    	if(updateOperationName(viewId)) {
                    		
                    			Thread.sleep(1500);
                    			tempDataset.delete();
//                    		MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "Edit Activity", MessageBox.INFORMATION);
                    	}
                    	} catch (Exception e) {
                    		// TODO Auto-generated catch block
                    		e.printStackTrace();
                    	}
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally
            {
                // Edit ��ư Disable
                // [NO-SR][20150323] shcho, Edit Activity �۾� �� �������� ���� �� Edit Activity ��ư�� ��Ȱ���� ���·� �����ִ� ���� ����
                Display.getDefault().syncExec(new Runnable() {      
                    
                    @Override
                    public void run() {
                        enableEditButtons(viewId, true);
                    }
                });
            }
        }
    }

    private boolean updateOperationName(String viewId) {
        try {
            IDataSet dataSet = getDataSet();
            IDataMap dataMap = dataSet.getDataMap(viewId);
            dataMap.put("actionId", "UpdateOperationName", IData.STRING_FIELD);

            CustomActivityDao activitydao = new CustomActivityDao();
            String userId = ((TCSession) getSession()).getUser().getUserId();

            Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
            Sheet sheet = workbook.getSheetAt(0);
            int endRow = sheet.getLastRowNum();
            for(int i = 1; i <= endRow; i++) {
                Row row = sheet.getRow(i);
                String itemId = row.getCell(0).getStringCellValue();
                String itemRev = row.getCell(1).getStringCellValue();
                String objectEngName = row.getCell(3).getStringCellValue();

                TCComponentItemRevision revision = CustomUtil.findItemRevision(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, itemId, itemRev);

                if(revision == null) {
                    throw new NullPointerException("���� " + itemId + "/" + itemRev + "�� �������� �ʽ��ϴ�.");
                }

                activitydao.updateActivityEnglishName(revision.getUid(), objectEngName, userId, "OPERATION");

                dataMap.put(itemId, objectEngName, IData.STRING_FIELD);
            }

//            AbstractSDVViewPane viewPane = (AbstractSDVViewPane) UIManager.getCurrentDialog().getView("searchResultView");
            AbstractSDVViewPane viewPane = (AbstractSDVViewPane) UIManager.getCurrentDialog().getView(viewId);
            if(viewPane != null) {
                viewPane.setLocalDataMap(dataMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Edit Operation Name", MessageBox.ERROR);
        }

        return true;
    }

    private void openExcelFile(final String viewId, OleListener oleListener) {
        Composite oleComposite = null;

        if(viewId == null) {
            oleComposite = new Composite((AbstractSDVViewPane) UIManager.getCurrentDialog().getViews().get(0), SWT.NONE);
        } else {
            oleComposite = new Composite((AbstractSDVViewPane) UIManager.getCurrentDialog().getView(viewId), SWT.NONE);
        }

        oleComposite.setLayoutData(BorderLayout.CENTER);
        oleComposite.setLayout(new FillLayout());

        appControlSite = new OleControlSite(new OleFrame(oleComposite, SWT.NONE), SWT.NONE, "Excel.Application");
        appControlSite.doVerb(OLE.OLEIVERB_OPEN);

        OleAutomation application = new OleAutomation(appControlSite);

        application.setProperty(application.getIDsOfNames(new String[] {"Visible"})[0], new Variant(true));
        OleAutomation workbooks = application.getProperty(application.getIDsOfNames(new String[] {"Workbooks"})[0]).getAutomation();
        Variant varResult = workbooks.invoke(workbooks.getIDsOfNames(new String[] {"Open"})[0], new Variant[] {new Variant(filePath)});
        if(varResult != null) {
            System.out.println(" copy invoke result of BSHEET = " + varResult);
            varResult.dispose();

            // Edit ��ư Disable
            enableEditButtons(viewId, false);
        } else {
            System.out.println("=====failed invoke copySheet method ====");
        }

//        OleAutomation window = application.getProperty(application.getIDsOfNames(new String[] {"ActiveWindow"})[0]).getAutomation();
//        window.setProperty(window.getIDsOfNames(new String[] {"Visible"})[0], new Variant[] {new Variant(true)});
//        window.setProperty(window.getIDsOfNames(new String[] {"WindowState"})[0], new Variant[] {new Variant("xlMaximized")});
//        varResult = window.invoke(window.getIDsOfNames(new String[] {"Activate"})[0]);
//        if(varResult != null) {
//            System.out.println(" Activate invoke result = " + varResult);
//            varResult.dispose();
//        } else {
//            System.out.println("=====failed invoke Activate method ====");
//        }

        OleListener windowDeactivateListener = new OleListener() {

            @Override
            public void handleEvent(OleEvent event) {
                enableEditButtons(viewId, true);
            }
        };
        appControlSite.addEventListener(application, ExcelView.IID_AppEvents, ExcelView.WindowDeactivate, windowDeactivateListener);

        if(oleListener != null) {
            appControlSite.addEventListener(application, ExcelView.IID_AppEvents, ExcelView.WorkbookBeforeSave, oleListener);
        }

        workbooks.dispose();
        application.dispose();
    }

    private void enableEditButtons(String viewId, boolean flag) {
        LinkedHashMap<String, IButtonInfo> actionButtons = null;
        if(viewId == null) {
            actionButtons = ((AbstractSDVSWTDialog) UIManager.getCurrentDialog()).getCommandToolButtons();
        } else {
            actionButtons = ((AbstractSDVViewPane) UIManager.getCurrentDialog().getView(viewId)).getActionToolButtons();
        }

        if(actionButtons != null) {
            for(String key : actionButtons.keySet()) {
                if(actionButtons.get(key).getActionId().startsWith("Edit")) {
                    actionButtons.get(key).getButton().setEnabled(flag);
                }
            }
        }
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

    @Override
    public void endOperation() {

    }
    
    
    public void setFile(TCComponentDataset targetDataset) {
        try {
        	targetDataset.refresh();
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

}
