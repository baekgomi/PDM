package com.symc.plm.me.sdv.operation.ps;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.AISInstructionDatasetCopyUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

public class EngSheetUnProtectOperation extends AbstractSDVActionOperation {

    private Registry registry;

    public EngSheetUnProtectOperation(String actionId, String ownerId, IDataSet dataset) {
        super(actionId, ownerId, dataset);
        registry = Registry.getRegistry(this);
    }

    public EngSheetUnProtectOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
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
                            unProtection(viewId, opList);
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * [SR160324-013] [20160329] taeku.jeong ���� �۾�ǥ�ؼ� �ۼ� �� �׸��κ� �����Ұ� ���� ���� ���� �Ƿڰ�
     * ���������۾� ǥ�ؼ��� Workbook, Sheet�� ��ȣ���� �ϴ� Function
     * ���� �����۾� ǥ�ؼ��� �۾� ������ �������� �׸��̳� Cell�� Copy�ؼ� ���̴°��
     * ������ Sheet�� ����Ǵ� Sheet ��ȣ�� ���� �״�� �����Ǵ� ��찡 �־
     * ������ ��� Sheet ��ȣ�� �����ϴ� ����� �߰��� ���� �Ǿ���
     * @param viewId
     * @param opList
     */
    private void unProtection(final String viewId, List<HashMap<String, Object>> opList) {
    	
    	System.out.println("viewId = "+viewId);
    	
    	// Preference�� BOP.EngUnProtect.Users �� ��ϵ� ��������� Check �Ѵ�. 
    	boolean isContinuAble = false;
    	TCSession tcsession = (TCSession)getSession();
    	String loginUserId = tcsession.getUser().getUid();
    	String[] userIdList = tcsession.getPreferenceService().getStringValues("BOP.EngUnProtect.Users");
    	for (int i = 0;userIdList!=null && i < userIdList.length; i++) {
			if(userIdList[i]!=null && userIdList[i].trim().equalsIgnoreCase(loginUserId.trim())){
				isContinuAble = true;
				break;
			}
		}
    	
    	if(isContinuAble==false){
    		System.out.println("To use this feature, you need to be registered.");
    		return;
    	}
    	
		AISInstructionDatasetCopyUtil aAISInstructionDatasetCopyUtil = new AISInstructionDatasetCopyUtil((TCSession)getSession());
    	
    	for (int i = 0;opList!=null && i < opList.size(); i++) {
            HashMap<String, Object> dataMap = opList.get(i);
            String itemId = (String) dataMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
            String itemRevId = (String) dataMap.get(SDVPropertyConstant.ITEM_REVISION_ID);
            
            // �־��� Item Revision�� ã�Ƽ� ���� �����۾�ǥ�ؼ� Image�� ����ִ� Excel ������
            // Work Book & Sheet ��ȣ�� ���� �Ѵ�.
            System.out.println("Operation = "+itemId+"/"+itemRevId);
            try {
				TCComponentItemRevision itemRevision = CustomUtil.findItemRevision(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, itemId, itemRevId);
            aAISInstructionDatasetCopyUtil.englishAssemblyInstructionSheetUnProtect(itemRevision);
			} catch (Exception e) {
				e.printStackTrace();
			}
            
		}
    }
    

    @Override
    public void endOperation() {

    }

}
