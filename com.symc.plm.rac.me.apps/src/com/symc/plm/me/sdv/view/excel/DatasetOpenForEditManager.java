package com.symc.plm.me.sdv.view.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.ui.PlatformUI;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnsignedShortHex;
import org.sdv.core.ui.UIManager;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.AISInstructionDatasetCopyUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCReservationService;
import com.teamcenter.rac.util.MessageBox;

/**
 * Dataset�� �����ٰ� �ݾ����� ���� ������ 
 * @author Taeku
 *
 */
public class DatasetOpenForEditManager extends Thread {
	
	private TCComponentDataset dataset;
	private TCComponentItemRevision itemRevision;
	private int datasetType = 0;
	
	private boolean isApplicationClosed = false;
	private boolean canOpenEditAble = false;
	private boolean exitFlag = false;
	private boolean isPreviewEditable = true;

	public static int KOR_WORK_INSTRUCTION = 0;
	public static int ENG_WORK_INSTRUCTION = 1;
	public static int WELD_CONDITION_SHEET = 2;
	
	public OleFrame frame;
    
	
	public DatasetOpenForEditManager(TCComponentDataset dataset){
		this.dataset = dataset;
		initItem();
		initEditMode();
	}
	
	public DatasetOpenForEditManager(TCComponentDataset dataset, OleFrame frame, boolean _isPreviewEditable){
		this.dataset = dataset;
		this.frame = frame;
		isPreviewEditable = _isPreviewEditable;
		initItem();
		initEditMode();
	}
	
	public DatasetOpenForEditManager(TCComponentItemRevision itemRevision, int datasetType){
		this.itemRevision = itemRevision;
		this.datasetType = datasetType;
		initDataset();
		initEditMode();
	}
	
	@Override
	public void run() {
		
		exitFlag = false;
		
		try {
			openDataset();
			if(!canOpenEditAble)
			{
				exitFlag = true;
				return;
			}
			// Dataset�� Open �Ǹ� Check Out �Ǿ�� �ϴµ�
			// Check Out �Ǵ��� Ȯ�� �Ѵ�.
			while (dataset.isCheckedOut()==false) {
				if(exitFlag==true){
					break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Dataset�� �������̸� Check Out �� ���¸� �����ϹǷ� Check In �ɶ�����
			// ��ٸ��� �κ�
			while (dataset.isCheckedOut()==true) {
				if(exitFlag==true){
					break;
				}
				//System.out.print(".");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			exitFlag = true;
			
		} catch (Exception e) {
			MessageBox.post( UIManager.getCurrentDialog().getShell(), e.toString() , "Error", MessageBox.ERROR);
			return;
		}finally{
			if(canOpenEditAble==true){
				protect();
			}
			isApplicationClosed = true;
			System.out.println("Closed .................");
		}
		
	}
	
	public TCComponentItemRevision getItemRevision( ) {
		return this.itemRevision;
	}

	public boolean isExitFlag() {
		return exitFlag;
	}

	public void setExitFlag(boolean exitFlag) {
		this.exitFlag = exitFlag;
	}

	public boolean isApplicationClosed() {
		return isApplicationClosed;
	}

	public boolean isCanOpenEditAble() {
		return canOpenEditAble;
	}

	private void initEditMode(){
		
		boolean isEditAble = isUpdateAble();
		
		boolean isCheckedOut = dataset.isCheckedOut();
		if(isCheckedOut==true){
			TCComponentUser checkOutUser = null;
			try {
				checkOutUser = (TCComponentUser) dataset.getReferenceProperty("checked_out_user");
			} catch (TCException e) {
				e.printStackTrace();
			}
			TCComponentUser loginUser = dataset.getSession().getUser();
			if(loginUser!=null && checkOutUser!=null && checkOutUser.equals(loginUser)==false){
				isEditAble = false;
			}
		}
		
		if(isEditAble && isPreviewEditable){
			canOpenEditAble = true;
		}
		
	}

	private void protect(){
		
		if(isCanOpenEditAble()==false){
			System.out.println("You have not access right...");
			return;
		}
		
		File file = getExcelFile();
		
		String password = getItemUidStr(); 
		Workbook workbook = null;
		try {
			workbook = new XSSFWorkbook(new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if(workbook==null){
			return;
		}
		
        STUnsignedShortHex convertedPassword = AISInstructionDatasetCopyUtil.stringToExcelPassword(workbook, password);
        
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
            CTSheetProtection sheetProtection = sheet.getCTWorksheet().getSheetProtection();
            if (sheetProtection == null) {
                sheetProtection = sheet.getCTWorksheet().addNewSheetProtection();
            }
            // [NON-SR][20160205] taeku.jeong WorkSheet ��ȣ�� �ȵǴ� ������ �־ Protection������ ó�� �߰�
            sheetProtection.xsetPassword(convertedPassword);
            sheetProtection.setSheet(true);
            sheetProtection.setScenarios(true);
            sheetProtection.setObjects(false);
        }

        CTWorkbookProtection workbookProtection = ((XSSFWorkbook) workbook).getCTWorkbook().getWorkbookProtection();
        if (workbookProtection == null) {
            workbookProtection = ((XSSFWorkbook) workbook).getCTWorkbook().addNewWorkbookProtection();
            workbookProtection.setLockStructure(true);
            workbookProtection.setLockWindows(true);
        }
        workbookProtection.xsetWorkbookPassword(convertedPassword);
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			workbook.write(fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		try {
			SDVBOPUtilities.datasetUpdate(file, this.dataset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Do Protect...");
	}

	private void openDataset() throws Exception{
		
		if(canOpenEditAble==true){
			unProtect();			
		}
		
		try {
	
			if(canOpenEditAble==true){
				dataset.openForEdit();
			}else{
				System.out.println("���������� ������� ���� �� �ֽ��ϴ�");
				dataset.openForView();
			}
			isApplicationClosed = false;
		} catch (TCException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			;
		}
		
	}

	private boolean unProtect() throws Exception{
		
		boolean isSuccess = false;
		
		try {
			this.dataset.refresh();
		} catch (TCException e2) {
			e2.printStackTrace();
		}
		
		File file = getExcelFile();
		Workbook workbook = null;
		try {
			workbook = new XSSFWorkbook(new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if(workbook==null){
			throw new Exception("You can not open excel file.");
		}
		
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
            CTSheetProtection sheetProtection = sheet.getCTWorksheet().getSheetProtection();
            if (sheetProtection != null) {
                sheet.getCTWorksheet().unsetSheetProtection();
            }
        }

        CTWorkbookProtection workbookProtection = ((XSSFWorkbook) workbook).getCTWorkbook().getWorkbookProtection();
        if (workbookProtection != null) {
            ((XSSFWorkbook) workbook).getCTWorkbook().unsetWorkbookProtection();
        }
        
		try {
			FileOutputStream fos = new FileOutputStream(file);
			workbook.write(fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			SDVBOPUtilities.datasetUpdate(file, this.dataset);
			this.dataset.refresh();
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Do UnProtect...");
		
		return isSuccess;
	}
	
	private boolean isUpdateAble(){
		
		boolean isUpdateAble = false;
		
		// ��Ģ�� ���� ���� ItemRevision�� PUID�� ��ȣ�� �����ϱ����� PUID���� �д´�.
		// [NONE_SR] [20151120] taeku.jeong �����۾�ǥ�ؼ� ������ȣ ��ȣ ������ ���� ����
		boolean isReleased1 = false;
		if(this.itemRevision!=null){
			try {
				isReleased1 = SYMTcUtil.isReleased(this.itemRevision);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		boolean isReleased2 = true;
		if(this.dataset!=null){
			try {
				isReleased2 = SYMTcUtil.isReleased(this.dataset);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("isReleased1 = "+isReleased1);
		System.out.println("isReleased2 = "+isReleased2);
		
		boolean hasBypass = dataset.getSession().hasBypass();
		
		System.out.println("hasBypass = "+hasBypass);
		
		if(hasBypass==true){
			isUpdateAble = true;
		}else{
			if(datasetType == DatasetOpenForEditManager.ENG_WORK_INSTRUCTION){
				if(isReleased2==false){
					isUpdateAble = true;
				}
			}else{
				if(isReleased1==false){
					if(isReleased2==false){
						isUpdateAble = true;
					}
				}
			}
		}
		
		System.out.println("isUpdateAble = "+isUpdateAble);

		return isUpdateAble;
		
	}
	
	private String getItemUidStr(){
		String newUidStr = null;
		
		if(this.itemRevision!=null){
			try {
				newUidStr = this.itemRevision.getItem().getUid();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}else{
			newUidStr = "symc";
		}
		
		System.out.println("newUidStr = "+newUidStr);
		
		return newUidStr;
	}
	
	private void initDataset(){
		
		String[] targetRelationType = null;
		
		switch (this.datasetType) {
		case 0 :
			targetRelationType = new String[]{SDVTypeConstant.PROCESS_SHEET_KO_RELATION};	
			break;
		case 1 :
			targetRelationType = new String[]{SDVTypeConstant.PROCESS_SHEET_EN_RELATION};
			break;
		case 2 :
			targetRelationType = new String[]{SDVTypeConstant.WELD_CONDITION_SHEET_RELATION};
			break;
		default:
			
			break;
		}
		
		try {
			AIFComponentContext[] childContexts = this.itemRevision.getChildren(targetRelationType);
			for (int i = 0;childContexts!=null && i < childContexts.length; i++) {

				AIFComponentContext currentContext = childContexts[i];
				if(currentContext!=null){
					TCComponent component = (TCComponent)currentContext.getComponent();
					if(component instanceof TCComponentDataset){
						this.dataset = (TCComponentDataset)component;
						break;
					}
				}
			}
			
		} catch (TCException e) {
			e.printStackTrace();
		}
	
	}
	
	
	private void initItem(){
		
		String[] relationNames = new String[]{
				SDVTypeConstant.PROCESS_SHEET_KO_RELATION,
				SDVTypeConstant.PROCESS_SHEET_EN_RELATION,
				SDVTypeConstant.WELD_CONDITION_SHEET_RELATION
			};
	
		try {
			AIFComponentContext[] referenced = this.dataset.whereReferencedByTypeRelation( null, relationNames);
			for (int i = 0;referenced!=null && i < referenced.length; i++) {
				
				AIFComponentContext currentContext = referenced[i];
				String contextType = (String)currentContext.getContext();
				TCComponent component = (TCComponent)referenced[i].getComponent();
				if(component!=null && component instanceof TCComponentItemRevision){
					this.itemRevision = (TCComponentItemRevision)component;
					String itemRevisionType = component.getType();
					if(itemRevisionType!=null && itemRevisionType.equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV)){
						this.datasetType = DatasetOpenForEditManager.WELD_CONDITION_SHEET;
					}else if(itemRevisionType!=null && (
							itemRevisionType.equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV) ||
							itemRevisionType.equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV) ||
							itemRevisionType.equalsIgnoreCase(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV)
							)){
						
						if(contextType!=null && contextType.equalsIgnoreCase(SDVTypeConstant.PROCESS_SHEET_KO_RELATION)){
							this.datasetType = DatasetOpenForEditManager.KOR_WORK_INSTRUCTION;
						}else if(contextType!=null && contextType.equalsIgnoreCase(SDVTypeConstant.PROCESS_SHEET_EN_RELATION)){
							this.datasetType = DatasetOpenForEditManager.ENG_WORK_INSTRUCTION;
						}
					}
				}
			}
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private File getExcelFile(){
		
		File newFile = null;
		
        TCComponentTcFile[] tcFiles = null;
		try {
			tcFiles = this.dataset.getTcFiles();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		// ������ �����۾�ǥ�ؼ� ���� ������ �ִ��� Ȯ���ϰ� File�� �����´�.
		for (int i = 0; i < tcFiles.length; i++) {
            String orgFileName = null;
			try {
				orgFileName = tcFiles[i].getProperty("original_file_name");
			} catch (TCException e) {
				e.printStackTrace();
			}
            String[] orgFileNameSplit = orgFileName.split("[.]");
            String extFileName = orgFileNameSplit[orgFileNameSplit.length - 1];
            if(extFileName!=null ){
            	// [20170525] bck ���� 
            	// Excel Dataset ���� �߰� xlsm
            	if(extFileName.trim().equalsIgnoreCase("xls") || extFileName.trim().equalsIgnoreCase("xlsx")|| extFileName.trim().equalsIgnoreCase("xlsm")){
            		try {
            			newFile = tcFiles[i].getFile(null, orgFileName );
            		} catch (TCException e) {
            			e.printStackTrace();
            		}
            	}
            }
            
            if(newFile!=null && newFile.exists()==true){
            	break;
            }
		}
		
		return newFile;
		
	}
	

	/**
	 * �ش� Components�� CheckIn �ϴ� Function
	 * @param targetComponents
	 * @return
	 * @throws Exception
	 */
    private boolean checkInComponent(TCComponent[] targetComponents) throws Exception {
        boolean isSuccess = true;
        try {
            TCReservationService tcreservationservice = targetComponents[0].getSession().getReservationService();
            tcreservationservice.unreserve(targetComponents);
            tcreservationservice.reserve(targetComponents);
        } catch (Exception e) {
            isSuccess = false;
        }
        return isSuccess;
    }
    
	/**
	 * �ش� Components�� Checkout �ϴ� Function
	 * @param targetComponents
	 * @return
	 * @throws Exception
	 */
    private boolean checkOutComponent(TCComponent[] targetComponents) throws Exception {
        boolean isSuccess = true;
        try {
            TCReservationService tcreservationservice = targetComponents[0].getSession().getReservationService();
            tcreservationservice.reserve(targetComponents);
        } catch (Exception e) {
            isSuccess = false;
        }
        return isSuccess;
    }
}

