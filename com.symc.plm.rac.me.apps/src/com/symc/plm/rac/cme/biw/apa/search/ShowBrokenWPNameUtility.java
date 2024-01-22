package com.symc.plm.rac.cme.biw.apa.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.sdv.command.meco.dao.CustomBOPDao;
import com.symc.plm.me.sdv.dialog.common.SimpleUserInputDialog;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;

/**
 * [SR151207-041][20151215] taeku.jeong �������� ���������� Occurrence Name�� ã�Ƽ� ǥ�����ִ� ��� 
 * �������Ҵ��� ��������� bl_indexted_titl�� ��ȣ����
 * ���ڷ� ��Ÿ���� �������� ���� ��Ÿ���� ������ �̸��� ������ �ʰԵȴ�.
 * ��ü Engineer�� ������ � Part������ �˾ƾ� ���� ������ Part���� �ƴϸ� ���躯��Ǹ鼭
 * ������ Part���� �Ǵ��� �ϰ� �ļ� ��ġ�� �� �� �ְ� �ȴ�.
 * ���� �̷� ���� ���� �ϵ��� ������ �Ҵ�Ǿ��� �������� Occurrence Name�� ã�Ƽ� �ٽ� ������ �ִ� ����� ������ Class  
 * @author Taeku
 *
 */
public class ShowBrokenWPNameUtility {
	
	public ShowBrokenWPNameUtility(){
		
	}
	
	/**
	 * ����ڰ� ������ �������� abs occurrence id�� ã�Ƽ� Occurrence Name�� ã�Ƽ� �Ҵ�� ���� �����ִ�
	 * �Լ��� ȣ���ϴµ� �ʿ��� �غ��� ���� Function�� ȣ���Ѵ�.
	 */
	public void findWPProductOccName(){
		
    	MFGLegacyApplication mppApplication = null;
    	if(AIFUtility.getCurrentApplication()!=null && AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication){
    		mppApplication = (MFGLegacyApplication)AIFUtility.getCurrentApplication();
		}else{
			System.out.println("Use MPP Application.....");
		}
    	
    	TCComponentBOMLine aWeldPointBOMLine = null;
    	ArrayList<String> weldPointBOMLineAbsOccIdList = new ArrayList<String>();
    	
    	InterfaceAIFComponent[] targetComponents = mppApplication.getTargetComponents();
		for (int i = 0;targetComponents!=null && i < targetComponents.length; i++) {
			if(targetComponents[i]!=null && targetComponents[i] instanceof TCComponentBOMLine){
				aWeldPointBOMLine = (TCComponentBOMLine)targetComponents[i];
				try {
					String currentAbsOccId = aWeldPointBOMLine.getProperty("bl_abs_occ_id");
					if(currentAbsOccId!=null && 
							currentAbsOccId.trim().length()>0 && 
							weldPointBOMLineAbsOccIdList.contains(currentAbsOccId)==false){
						weldPointBOMLineAbsOccIdList.add(currentAbsOccId);
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
		
		TCComponentBOMLine processTopBOMLine = null;
		TCComponentItemRevision processItemRevision = null;
		String processItemId = null;
		boolean isShop = false;
		try {
			processTopBOMLine = aWeldPointBOMLine.window().getTopBOMLine();
			processItemRevision = processTopBOMLine.getItemRevision();
			processItemId = processTopBOMLine.getItem().getProperty("item_id");
			String processItemType = processTopBOMLine.getItem().getType();
			if(processItemType!=null && processItemType.trim().equalsIgnoreCase("M7_BOPShop")==true){
				isShop = true;
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(isShop == false){
			System.out.println("Select under shop...");
		}
		
		
		AbstractViewableTreeTable aProcessAbstractViewableTreeTable = null;
		
    	AbstractViewableTreeTable[] viewableTreeTables = mppApplication.getViewableTreeTables();
    	for (int i = 0;viewableTreeTables!=null &&  i < viewableTreeTables.length; i++) {
    		
    		AbstractViewableTreeTable currentTreeTable = viewableTreeTables[i];
    		TCComponentBOMLine currentBOMLine = currentTreeTable.getBOMRoot();
    		if(currentBOMLine==null){
    			continue;
    		}
    		
    		try {
				String item_type = currentBOMLine.getItem().getType();

				if(item_type!=null && item_type.trim().equalsIgnoreCase("M7_BOPShop")==true){
					String currentItemId = currentBOMLine.getItem().getProperty("item_id");
					if(currentItemId!=null && currentItemId.trim().equalsIgnoreCase(processItemId.trim())==true){
						aProcessAbstractViewableTreeTable = currentTreeTable;
					}
				}
				
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
    	
		TCComponentBOMWindow mProductBOMWindow = null;
		String mProductId = null;
		try {
			mProductBOMWindow = SDVBOPUtilities.getConnectedMProductBOMWindow(processItemRevision);
			mProductId = mProductBOMWindow.getTopBOMLine().getItem().getProperty("item_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		doFindAbssOccurrence(mProductId, 
				weldPointBOMLineAbsOccIdList,
				aProcessAbstractViewableTreeTable );
		
	}
	
	/**
	 * �־��� Prodcut�� BOM �����̷¸� �̿��� �־��� abs Occurrence name�� ã��
	 * Link�� �������鼭 ������ Occurrence Name�� �ٽ� ���� �Ѵ�.
	 * �׸��� Process Tree���� �˻��� BOMLine�� �ߺ��Ǿ� �Ҵ� �Ǿ� �ִٸ� �ش�Ǵ�
	 * BOMLine�� Tree���� �����ش�.
	 *   
	 * @param targetMProductId Occurrence Name�� ã�� ����� �Ǵ� Process�� ���õ� M-Product Id
	 * @param userSelectedBOMLineAbsOccIdList ����ڰ� ������ ������ ������ BOMLine�� Abs Occurrence Id List
	 * @param processAbstractViewableTreeTable ����ڰ� ������ BOMLine�� ���Ե� TreeTable
	 */
    private void doFindAbssOccurrence(String targetMProductId, 
    		ArrayList<String> userSelectedBOMLineAbsOccIdList,
    		AbstractViewableTreeTable processAbstractViewableTreeTable ){

    	/*
		// User Input Dialog�� ����ϴ� ��� Sample
		String dialogTitle = "Dialog Title";
		String dialogMessage = "Dialog Message....";
		String initialValue="KR...";
		
		String userInPutValue = SimpleUserInputDialog.getUserInputString(dialogTitle, dialogMessage, initialValue);
		System.out.println("userInPutValue = "+userInPutValue);
		*/
		
		if(userSelectedBOMLineAbsOccIdList==null || (userSelectedBOMLineAbsOccIdList!=null && userSelectedBOMLineAbsOccIdList.size()<1)){
			System.out.println("Can't find user input find condition....");
			return;
		}
		
		System.out.println("targetMProductId = "+targetMProductId);
		
		for (int i = 0;userSelectedBOMLineAbsOccIdList!=null && i < userSelectedBOMLineAbsOccIdList.size(); i++) {
			System.out.println("userInputValues["+i+"] = "+userSelectedBOMLineAbsOccIdList.get(i));
		}
		
        ArrayList<HashMap> resultList = null;
        
        CustomBOPDao customBOPDao = new CustomBOPDao();  
        try {
			resultList = customBOPDao.getFindWPProductOccurenceName(targetMProductId, userSelectedBOMLineAbsOccIdList);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        if(resultList==null || (resultList!=null && resultList.size()<1)){
        	System.out.println("can't find product occurrence data");
        	return;
        }
        
		TCComponentBOMWindow currentBOMWindow = null;
		try {
			currentBOMWindow = processAbstractViewableTreeTable.getBOMWindow();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(currentBOMWindow==null){
			return;
		}

		// ã�� Appearance�� �ٷ� ��ġ�� ���
		TCComponentBOMLine topBOMLie = null;
		try {
			topBOMLie = currentBOMWindow.getTopBOMLine();
		} catch (TCException e) {
			e.printStackTrace();
		}
        
		for (int i = 0;resultList!=null && i < resultList.size(); i++) {
			HashMap aHash = resultList.get(i);

			if(aHash.get("ABS_OCC_ID")!=null){
				
				String absOccId = aHash.get("ABS_OCC_ID").toString();
				String occName = aHash.get("OCC_NAME").toString();

				System.out.println("absOccId["+i+"] = "+absOccId);
				System.out.println("occName["+i+"] = "+occName);
				
				try {
					TCComponentBOMLine[] finded = currentBOMWindow.findConfigedBOMLinesForAbsOccID(absOccId, true, topBOMLie);
					for (int k = 0; k < finded.length; k++) {
						String currentOccName = finded[k].getProperty("bl_occurrence_name");
						if(currentOccName==null || (currentOccName!=null && currentOccName.trim().length()<1)){
							finded[k].setProperty("bl_occurrence_name", occName);
							finded[k].refresh();
						}
					}
				} catch (TCException e) {
					e.printStackTrace();
				}
				
				List<TCComponent> parameter = new ArrayList<TCComponent>();
				parameter.add(topBOMLie);
				int k1 = processAbstractViewableTreeTable.findAbsOccInTreeTable(absOccId, parameter, true);

			}
			
		}
		
		try {
			processAbstractViewableTreeTable.getBOMWindow().save();
		} catch (TCException e) {
			e.printStackTrace();
		}

    }
}
