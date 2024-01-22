package com.symc.plm.me.sdv.operation.ps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreePath;

import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.ProcessUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.treetable.TreeTableNode;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [P0080] [20150115] ymjang, Search ȭ�鿡�� "���°���","�ٸ�����" �����ϰ� Search �ص� �ش�Ǵ� "����","�ٸ�" ������ list ���� ����
 *                            1) ó�� ���� ��ġ ������
 * [P0071] [20150127] ymjang, Publish �� ���� ������������ �ұ��ϰ� "�̰��� ����" ���߸� Ŭ���ϰ� ��ȸ�ϸ� �̰���� ���̶�� �Ʒ��� ���� ����          
 *                            1) ������ Refresh() �߰�
 *                            2) ���� �������� ���� 'In Review' �� ǥ���ϰ�, �̰��� ������ ��Ÿ���� �ʵ��� ó��.
 * [P0083] [20150225] ymjang, Search ȭ�鿡�� �� ������ �Ϸù�ȣ�� �߰� ���.
 *                            1) rowIdx �׸� �߰�
 * [P0077] [20150303] ymjang, Publish �ð� ����
 * [NO-SR] [20150323] shcho, ������ InProcess�� ���, ���� �ٸ� ���� �˻��� �߻��ϴ� ���� ���� (�� ����� ������ǥ �������� ���� ���������� �Ѵ�.)
 * [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
 * [SR150206-017] [20150330] ymjang, �۾�ǥ�ؼ� ��ȸ ���� ���� ���� �Ƿ�
 * [SR150611-013][20150714] shcho, ���� �۾�ǥ�ؼ� ��ȸ â �� "�� ���� ����" ��� ���� (������ ��� Working�� ���� ��θ� ���� �� ��)
 * [NONE-SR][20151123] taeku.jeong �����۾� ǥ�ؼ��� �˻���ɰ��� (�ӵ� & �˻����� ��������)
 */
public class ProcessSheetSearchOperation extends AbstractSDVActionOperation {
	
	// New Operation (2016/03/17 ���� ���� Taeku.Jeong)

    private Registry registry;
    private int configId;
    private String publishItemPrefix;
    private String noVariantString;
    private String processType;
    private String lineCode;
    private String lineRev;
    private String stationCode;
    private String stationRev;
    private boolean isAppGroup = false;
    private int rowIdx = 0;
    long startTime = 0;
    
    public ProcessSheetSearchOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        registry = Registry.getRegistry(this);
    }

    public ProcessSheetSearchOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, actionId, ownerId, parameters, dataset);
        registry = Registry.getRegistry(this);
    }

    @Override
    public void startOperation(String commandId) {
		System.out.println("Current Search Operation Start...");
		startTime = System.currentTimeMillis();
		IDataSet dataset = getDataSet();
    }

    @Override
    public void endOperation() {
    	System.out.println("Current Search Operation End...");
    	
    	 // ���� �ð�
        long endTime = System.currentTimeMillis();
        // �ð� ���
        System.out.println("##  ���۽ð� : " + formatTime(startTime));
        System.out.println("##  ����ð� : " + formatTime(endTime));
        System.out.println("##  �ҿ�ð�(��.0f) : " + ( endTime - startTime )/1000.0f +"��"); 
    }
    
    public String formatTime(long lTime) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(lTime);
        return (c.get(Calendar.HOUR_OF_DAY) + "�� " + c.get(Calendar.MINUTE) + "�� " + c.get(Calendar.SECOND) + "." + c.get(Calendar.MILLISECOND) + "��");
    }

    @Override
    public void executeOperation() throws Exception {
    	
    	System.out.println("Current Search Operation Execute .......");
    	
        try {
            List<HashMap<String, Object>> operationList = new ArrayList<HashMap<String, Object>>();
            IDataSet dataset = getDataSet();
            if(dataset.containsMap("searchConditionView")) {
                IDataMap dataMap = dataset.getDataMap("searchConditionView");

                configId = dataMap.getIntValue("configId");
                publishItemPrefix = registry.getString("ProcessSheetItemIDPrefix." + configId);
                noVariantString = registry.getString("ProcessSheetCommonVariant." + configId);

                processType = dataMap.getStringValue("process_type");
                TCComponentBOPLine topLine = (TCComponentBOPLine) dataMap.get("shop").getValue();
                if(topLine instanceof TCComponentAppGroupBOPLine) {
                    isAppGroup = true;
                }
                
                if(dataMap.get("line") != null) {
                    topLine = (TCComponentBOPLine) dataMap.getValue("line");
                }
                
                if(isAppGroup==true){
                	// [P0077] [20150303] ymjang, Publish �ð� ����
                	// Line ���� �������� �̸� ������.
                	// [20151124] taeku.jeong DB�˻��� ���� ������� Data�� ã������ �ʿ���� �ܰ���.
                	expandBelow(topLine);
                }
                
                // [20151124] taeku.jeong �ҽ��ڵ� ���⸦ �����ϰ� �ϱ����� �Լ� ó����.
                HashMap<String, String> conditionMap = getBasicConditionMap(dataMap);
            	
                // [P0083] [20150225] ymjang, Search ȭ�鿡�� �� ������ �Ϸù�ȣ�� �߰� ���.
                rowIdx = 0;
                
                if(isAppGroup==false){
                	// [20151124] taeku.jeong DB�˻��� ���� ������� Data�� ã�´�.
                	operationList = dbSearchMathod(operationList, conditionMap);
                }else{
                	operationList = getChildLine(operationList, topLine, conditionMap);
                }
            }

            IDataMap dataMap = new RawDataMap();
            dataMap.put("operationList", operationList, IData.TABLE_FIELD);
            dataset.addDataMap("searchResultView", dataMap);

            setDataSet(dataset);
        } catch(Exception e) {
            //setExecuteResult(ISDVActionOperation.FAIL);
            //setExecuteError(e);

        	e.printStackTrace();
            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Search", MessageBox.ERROR);
        }
    }
    
    /**
     * [20151124] taeku.jeong DB�˻� ����� �����ϱ����� �Լ��� ������.
     * DB���� �˻��� ����� appearancePathNodePuid�� �̿��� ã�� Operation BOPLine��
     * ������ �˻� ��� ���Ŀ� ���� Return �Ѵ�.
     * @param operationList
     * @param conditionMap
     * @return
     */
    private List<HashMap<String, Object>>  dbSearchMathod(List<HashMap<String, Object>> operationList, HashMap<String, String> conditionMap){

    	// [NONE_SR] [20151119] taeku.jeong �����۾� ǥ�ؼ� �˻������ ���� �ؾ� �Ѵ�.
    	// Query�� �̿��� ������� ���� �ϰ����Ѵ�.
		TCSession session = (TCSession)this.getCurrentDesktop().getCurrentApplication().getSession();
		IDataSet dataset = getDataSet();
    	ProcessSheetSearchEngineNew processSheetSearchEngine = new ProcessSheetSearchEngineNew(dataset, session);

    	List<TCComponentBOMLine> findedOperationBOMLine = null;
    	List<HashMap<String, Object>> getOperationHashMapList = null;
    	ArrayList<HashMap> resultList = processSheetSearchEngine.findTargetOccurenceList(registry);
    	
    	if(resultList!=null && (resultList.size()>0 && resultList.size()<1000) ){
    		// �˻���� Operation Map ã�Ƽ� Return
    		operationList = processSheetSearchEngine.getOperationHashMapList(resultList);
    	}else if(resultList!=null && resultList.size()>1000){
    		// �˻� ����� �ʹ� ������� ����� ǥ���ϴ� Table ��� Data�� ǥ�� ���� �����Ƿ� �˻� ������
    		// �����ؼ� �˻� ����� ���̵��� �Ѵ�. (http://10.80.1.98:8080/hbom/login.jsp )
    		MessageBox.post(new Throwable("�߰� �˻� ����� "+resultList.size()+"���� 1000���� �ʰ� �մϴ�.\n"
    				+ "�˻������� ���� �Ͽ� �˻� �غ��ñ� �ٶ��ϴ�.\n"
    				+ "�뷮 �˻��� ��� HUB BOM�� �̿� �Ͻ� �� �ֽ��ϴ�.\n"
    				+ "http://10.80.1.98:8080/hbom/"));
    	}
    	
    	System.out.println("Search operation ended ..............");
    	
    	return operationList;
    }


    
    /**
     * [20151124] taeku.jeong DB�˻� ����� ��������� �ҽ��ڵ� ������������ �Լ��� ����.
     * @param dataMap
     * @return
     */
    private HashMap<String, String> getBasicConditionMap(IDataMap dataMap){
    	
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        String value = dataMap.getStringValue("station_code");
        if(value != null && value.length() > 0) conditionMap.put("station_code", value);
        value = dataMap.getStringValue(SDVPropertyConstant.ITEM_ITEM_ID);
        if(value != null && value.length() > 0) conditionMap.put(SDVPropertyConstant.ITEM_ITEM_ID, value);
        value = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_KOR_NAME);
        if(value != null && value.length() > 0) conditionMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, value);
        value = dataMap.getStringValue(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
        if(value != null && value.length() > 0) conditionMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, value);
        value = dataMap.getStringValue(SDVPropertyConstant.ITEM_OWNING_USER);
        if(value != null && value.length() > 0) conditionMap.put(SDVPropertyConstant.ITEM_OWNING_USER, value);
        value = dataMap.getStringValue(SDVPropertyConstant.OPERATION_REV_MECO_NO);
        if(value != null && value.length() > 0) conditionMap.put(SDVPropertyConstant.OPERATION_REV_MECO_NO, value);
        value = dataMap.getStringValue("publish_user");
        if(value != null && value.length() > 0) conditionMap.put("publish_user", value);
        value = String.valueOf(dataMap.getValue("empty_operation"));
        if(value != null && "true".equals(value)) conditionMap.put("empty_operation", value);
        value = String.valueOf(dataMap.getValue("different_operation"));
        if(value != null && "true".equals(value)) conditionMap.put("different_operation", value);
        value = String.valueOf(dataMap.getValue("norelease_operation"));
        if(value != null && "true".equals(value)) conditionMap.put("norelease_operation", value);
        value = String.valueOf(dataMap.getValue("release_operation"));
        if(value != null && "true".equals(value)) conditionMap.put("release_operation", value);
       
        return conditionMap;
    }

    private List<HashMap<String, Object>> getChildLine(List<HashMap<String, Object>> list, TCComponentBOPLine topLine, HashMap<String, String> conditionMap) throws Exception {
        if(ProcessSheetUtils.isLine(topLine)) {
            lineCode = topLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
            lineRev = topLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
        }

        if(topLine.getChildrenCount() > 0) {
            AIFComponentContext[] contexts = topLine.getChildren();
            for(AIFComponentContext context : contexts) {
                TCComponentBOPLine childLine = (TCComponentBOPLine) context.getComponent();
                TCComponentItemRevision childRev = childLine.getItemRevision();
                if(childRev != null) {
                    if(ProcessSheetUtils.isLine(childLine)) {
                        lineCode = childRev.getProperty(SDVPropertyConstant.LINE_REV_CODE);
                        lineRev = childRev.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                        list = getChildLine(list, childLine, conditionMap);
                    } else if(ProcessSheetUtils.isStation(childLine)) {
                        stationCode = childRev.getProperty(SDVPropertyConstant.STATION_REV_CODE);
                        stationRev = childRev.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                        list = getChildLine(list, childLine, conditionMap);
                    } else if(ProcessSheetUtils.isOperation(childLine)) {
                        HashMap<String, Object> operationMap = getOperationInfo(childLine, conditionMap);
                        if(operationMap != null) {
                            list.add(operationMap);
                        }
                    }
                }
            }
        }

        return list;
    }

    private HashMap<String, Object> getOperationInfo(TCComponentBOPLine bopLine, HashMap<String, String> conditionMap) throws Exception {
        TCComponentItem item = bopLine.getItem();
        item.refresh();
        TCComponentItemRevision itemRevision = bopLine.getItemRevision();
        HashMap<String, Object> operationMap = null;
        if(item != null && itemRevision != null) {
            operationMap = new HashMap<String, Object>();
            operationMap.put("OPERATION_BOPLINE", bopLine);

            // [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
            // ���� ������ ���¸� üũ�ϱ� ���Ͽ� ������ �ʿ� Release ���ڸ� ��´�.
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date pdate_released = itemRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
            operationMap.put(SDVPropertyConstant.ITEM_DATE_RELEASED, pdate_released == null ? null : format.format(pdate_released));
            
            String[] propNames = new String[] {
                    SDVPropertyConstant.ITEM_ITEM_ID,
                    SDVPropertyConstant.ITEM_REVISION_ID,
                    SDVPropertyConstant.OPERATION_REV_STATION_NO,
                    SDVPropertyConstant.ITEM_OBJECT_NAME,
                    SDVPropertyConstant.OPERATION_REV_ENG_NAME
            };
            String[] propValues = itemRevision.getProperties(propNames);

            String itemId = propValues[0];
            String itemRev = propValues[1];
            operationMap.put("process_type", processType);
            if(isAppGroup) {
                TCComponentBOPLine lineBopLine = ProcessSheetUtils.getLine(bopLine);
                if(lineBopLine != null) {
                    operationMap.put("line_code", lineBopLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE));
                    operationMap.put("line_rev", lineBopLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID));
                }
            } else {
                operationMap.put("line_code", lineCode);
                operationMap.put("line_rev", lineRev);
            }

            if("A".equals(processType)) {
                operationMap.put("station_code", propValues[2]);
            } else {
                operationMap.put("station_code", stationCode);
                operationMap.put("station_rev", stationRev);
            }

            operationMap.put(SDVPropertyConstant.ITEM_ITEM_ID, itemId);
            operationMap.put(SDVPropertyConstant.ITEM_REVISION_ID, itemRev);
            operationMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, propValues[3]);
            operationMap.put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, propValues[4]);
            String variant = bopLine.getProperty(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
            
            System.out.println("variant (BOMLine Property) = "+variant);
            
            if(variant != null && variant.length() > 0) {
                variant = (String) SDVBOPUtilities.getVariant(variant).get("printDescriptions");
            } else {
                variant = noVariantString;
            }
            System.out.println("variant (Description) = "+variant);
            
            operationMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, variant);

            TCComponentItemRevision mecoRevision = (TCComponentItemRevision) itemRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
            if(mecoRevision == null) {
                throw new Exception("���� " + itemId + "/" + itemRev + "�� MECO�� �������� �ʽ��ϴ�.");
            }
            operationMap.put(SDVPropertyConstant.OPERATION_REV_MECO_NO, mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));

            TCComponentUser user = (TCComponentUser) mecoRevision.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
            TCComponentPerson person = (TCComponentPerson) user.getUserInformation().get(0);
            // [SR150206-017] [20150330] ymjang, �۾�ǥ�ؼ� ��ȸ ���� ���� ���� �Ƿ�
            if(person != null) {
                operationMap.put("person", person);
                operationMap.put("user", user);
                operationMap.put(SDVPropertyConstant.ITEM_OWNING_USER, person.getProperty("user_name"));
            } else {
                operationMap.put("person", person);
                operationMap.put("user", user);
                operationMap.put(SDVPropertyConstant.ITEM_OWNING_USER, user.getUserId());
            }
            /*
            if(person != null) {
                operationMap.put("person", person);
                operationMap.put(SDVPropertyConstant.ITEM_OWNING_USER, person.getProperty("user_name"));
            } else {
                operationMap.put("person", null);
                operationMap.put(SDVPropertyConstant.ITEM_OWNING_USER, user.getUserId());
            }
			*/
            
            TCComponentItem publishItem = SDVBOPUtilities.FindItem(publishItemPrefix + itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
            if(publishItem != null) {
                TCComponentItemRevision publishItemRev = null;
                TCComponent[] publishItemRevs = publishItem.getRelatedComponents("revision_list");
                List<String> publishRevList = new ArrayList<String>();
                for(int i = 0; i < publishItemRevs.length; i++) {
                    String tempRev = publishItemRevs[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                    if((tempRev.substring(0, itemRev.length())).compareToIgnoreCase(itemRev) <= 0) {
                        publishRevList.add(tempRev);
                        publishItemRev = (TCComponentItemRevision) publishItemRevs[i];
                        // [P0071] [20150127] ymjang, Publish �� ���� ������������ �ұ��ϰ� "�̰��� ����" ���߸� Ŭ���ϰ� ��ȸ�ϸ� �̰���� ���̶�� �Ʒ��� ���� ���� 
                        publishItemRev.refresh();
                        operationMap.put("selected_publish_rev", tempRev);
                    }
                }

                operationMap.put("publish_rev", publishRevList.toArray());

                TCComponent[] releaseStatusList = publishItemRev.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                if(releaseStatusList != null && releaseStatusList.length > 0) {
                    operationMap.put("publish_status", releaseStatusList[releaseStatusList.length - 1].getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
                } else
                {
                	// [P0071] [20150127] ymjang, Publish �� ���� ������������ �ұ��ϰ� "�̰��� ����" ���߸� Ŭ���ϰ� ��ȸ�ϸ� �̰���� ���̶�� �Ʒ��� ���� ���� 
                	// ���� �������� ���� 'In Review' �� ǥ���ϰ�, �̰��� ������ ��Ÿ���� �ʵ��� ó��.
            		operationMap.put("publish_status", !ProcessUtil.isWorkingStatus(publishItemRev) ? "In Review" : "");
                }
                Date pubDate = publishItemRev.getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
                if(pubDate != null) {
                    operationMap.put("publsih_date", SDVStringUtiles.dateToString(pubDate, "yyyy-MM-dd HH:mm"));
                }

                user = (TCComponentUser) publishItemRev.getReferenceProperty(SDVPropertyConstant.PS_REV_LAST_PUB_USER);
                if(user != null) {
                    person = (TCComponentPerson) user.getUserInformation().get(0);
                    // [SR150206-017] [20150330] ymjang, �۾�ǥ�ؼ� ��ȸ ���� ���� ���� �Ƿ�
                    if(person != null) {
                        operationMap.put("publish_user_person", person);
                        operationMap.put("publish_user_user", user);
                        operationMap.put("publish_user", person.getProperty("user_name"));
                    } else {
                        operationMap.put("publish_user_person", person);
                        operationMap.put("publish_user_user", user);
                        operationMap.put("publish_user", user.getUserId());
                    }
                    /*
                    if(person != null) {
                        operationMap.put("publish_user_person", person);
                        operationMap.put("publish_user", person.getProperty("user_name"));
                    } else {
                        operationMap.put("publish_user_person", null);
                        operationMap.put("publish_user", user.getUserId());
                    }
                    */
                }
            }

            // [P0080] [20150115] ymjang, ó�� ���� ��ġ ������.
            // ������ ��쿡�� ������ Publish ���� ǥ��
            if(configId == 1) {
                
                String korPrefix = registry.getString("ProcessSheetItemIDPrefix.0");
                TCComponentItem korPublishItem = SDVBOPUtilities.FindItem(korPrefix + itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);            	
                if(korPublishItem != null) {
                    TCComponentItemRevision korPublishItemRev = korPublishItem.getLatestItemRevision(); 
                	String tempRev = korPublishItemRev.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                    if((tempRev.substring(0, itemRev.length())).compareToIgnoreCase(itemRev) <= 0)
                    {
                    	operationMap.put("kor_publish_rev", tempRev);
                        
                        TCComponent[] releaseStatusList = korPublishItemRev.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                        if(releaseStatusList != null && releaseStatusList.length > 0) {
                            operationMap.put("kor_publish_status", releaseStatusList[releaseStatusList.length - 1].getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
                        }
                        Date pubDate = korPublishItemRev.getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
                        if(pubDate != null) {
                            operationMap.put("kor_publsih_date", SDVStringUtiles.dateToString(pubDate, "yyyy-MM-dd HH:mm"));
                        }

                        user = (TCComponentUser) korPublishItemRev.getReferenceProperty(SDVPropertyConstant.PS_REV_LAST_PUB_USER);
                        if(user != null) {
                            person = (TCComponentPerson) user.getUserInformation().get(0);
                            if(person != null) {
                                operationMap.put("kor_publish_user", person.getProperty("user_name"));
                            } else {
                                operationMap.put("kor_publish_user", user.getUserId());
                            }
                        }         
                    }
                }
            }

            boolean isCheckedOk = false;
            if(checkCondition(operationMap, conditionMap)==true) {
                Date releaseDate = itemRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
                if(releaseDate != null) {
                    operationMap.put(SDVPropertyConstant.ITEM_DATE_RELEASED, SDVStringUtiles.dateToString(releaseDate, "yyyy-MM-dd"));
                }
                isCheckedOk = true;
                
                /* 
                // [P0080] [20150115] ymjang, ���� �ҽ� �ּ� ó����.
                // ������ ��쿡�� ������ Publish ���� ǥ��
                if(configId == 1) {
                    String korPrefix = registry.getString("ProcessSheetItemIDPrefix.0");
                    TCComponentItem korPublishItem = SDVBOPUtilities.FindItem(korPrefix + itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
                    if(korPublishItem != null) {
                        TCComponentItemRevision korPublishItemRev = null;
                        TCComponent[] revisions = korPublishItem.getRelatedComponents("revision_list");
                        for(int i = revisions.length - 1; i >= 0; i--) {
                            if(revisions[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(itemRev)) {
                                korPublishItemRev = (TCComponentItemRevision) revisions[i];
                                operationMap.put("kor_publish_rev", korPublishItemRev.getProperty(SDVPropertyConstant.ITEM_REVISION_ID));

                                TCComponent[] releaseStatusList = korPublishItemRev.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                                if(releaseStatusList != null && releaseStatusList.length > 0) {
                                    operationMap.put("kor_publish_status", releaseStatusList[releaseStatusList.length - 1].getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
                                }
                                Date pubDate = korPublishItemRev.getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
                                if(pubDate != null) {
                                    operationMap.put("kor_publsih_date", SDVStringUtiles.dateToString(pubDate, "yyyy-MM-dd HH:mm"));
                                }

                                user = (TCComponentUser) korPublishItemRev.getReferenceProperty(SDVPropertyConstant.PS_REV_LAST_PUB_USER);
                                if(user != null) {
                                    person = (TCComponentPerson) user.getUserInformation().get(0);
                                    if(person != null) {
                                        operationMap.put("kor_publish_user", person.getProperty("user_name"));
                                    } else {
                                        operationMap.put("kor_publish_user", user.getUserId());
                                    }
                                }

                                break;
                            }
                        }
                    }
                }
                */
                
                // [20151124] taeku.jeong ���ǿ� �´°͸� ��� List�� ���Եȴ�.
                if(isCheckedOk==true){
                	operationMap.put("UID", itemRevision.getUid());
                	// [P0083] [20150225] ymjang, Search ȭ�鿡�� �� ������ �Ϸù�ȣ�� �߰� ���.
                	operationMap.put("rowIdx", String.valueOf(++rowIdx));
                }
                
            } else {
                return null;
            }
        }

        return operationMap;
    }
    
    /**
     *  [P0080] [20150115] ymjang, ��ȸ���ǿ� ���� ����� Filtering ����
     */
    private boolean checkCondition(HashMap<String, Object> operationMap, HashMap<String, String> conditionMap) throws TCException {
        boolean result = true;

        for(String key : conditionMap.keySet()) {
            String conditionValue = conditionMap.get(key);
//            System.out.println("key = "+key+", conditionValue = "+conditionValue);
//            
//            if(isAppGroup==false &&  key.trim().trim().equalsIgnoreCase(" item_id")==true){
//            	continue;
//            }
//            if(isAppGroup==false &&  key.trim().trim().equalsIgnoreCase(" station_code")==true){
//            	continue;
//            }
//            if(isAppGroup==false &&  key.trim().trim().equalsIgnoreCase(SDVPropertyConstant.OPERATION_REV_KOR_NAME)==true){
//            	continue;
//            }
//            
//            if(isAppGroup==false &&  (conditionValue==null || (conditionValue!=null && conditionValue.trim().length()<1)) ){
//            	continue;
//            }
            
            // [SR150206-017] [20150330] ymjang, �۾�ǥ�ؼ� ��ȸ ���� ���� ���� �Ƿ�
            // Start of ---------------------------------------------->
            // Op.ID
            if(isAppGroup==true && SDVPropertyConstant.ITEM_ITEM_ID.equals(key)) {
                String item_id = (String) operationMap.get(SDVPropertyConstant.ITEM_ITEM_ID);
                
                // [P0071] [20150127] ymjang, Publish �� ���� ������������ �ұ��ϰ� "�̰��� ����" ���߸� Ŭ���ϰ� ��ȸ�ϸ� �̰���� ���̶�� �Ʒ��� ���� ���� 
            	// ���� �������� ���� 'In Review' �� ǥ���ϰ�, �̰��� ������ ��Ÿ���� �ʵ��� ó��.
        		if(item_id == null) {
                    result = false;
                    break;
                } 
        		
        		if (!checkConditionValue(conditionValue, item_id)) {
        			result = false;
                    break;
        		}
            } 

            // Op.Name
            if(isAppGroup==true && SDVPropertyConstant.OPERATION_REV_KOR_NAME.equals(key)) {
                String kor_name = (String) operationMap.get(SDVPropertyConstant.OPERATION_REV_KOR_NAME);
                
        		if(kor_name == null) {
                    result = false;
                    break;
                } 
        		
        		if (!checkConditionValue(conditionValue, kor_name)) {
        			result = false;
                    break;
        		}
            } 

            // Option
            if(SDVPropertyConstant.BL_OCC_MVL_CONDITION.equals(key)) {
                String options = (String) operationMap.get(SDVPropertyConstant.BL_OCC_MVL_CONDITION);
                
        		if(options == null) {
                    result = false;
                    break;
                } else if (!options.contains(conditionValue))
    			{
                    result = false;
                    break;
    			}
            } 
            
            // MECO Owner
            if(SDVPropertyConstant.ITEM_OWNING_USER.equals(key)) {            	
            	TCComponentUser user = (TCComponentUser) operationMap.get("user");
                TCComponentPerson person = (TCComponentPerson) operationMap.get("person");
                if (person != null)
                {
                	result = checkConditionValue(conditionValue, person.getProperty("user_name"));
	                if(!result) {
	                	result = false;
	                	//break;
	                }
                }
                
            	if (!result && user != null)
            	{
                	result = checkConditionValue(conditionValue,  user.getUserId());
                    if (!result) {
                    	result = false;
                    	break;
                    }
            	}
            }
            		
            // MECO ID
            if(SDVPropertyConstant.OPERATION_REV_MECO_NO.equals(key)) {            	
            	String meco_no = (String) operationMap.get(SDVPropertyConstant.OPERATION_REV_MECO_NO);
                if (meco_no != null)
                {
                	result = checkConditionValue(conditionValue, meco_no);
	                if(!result) {
	                	result = false;
	                	break;
	                }
                }
            }
            
            // Publisher User
            if("publish_user".equals(key)) {            	
            	TCComponentUser user = (TCComponentUser) operationMap.get("publish_user_user");
                TCComponentPerson person = (TCComponentPerson) operationMap.get("publish_user_person");
                if (person != null)
                {
                	result = checkConditionValue(conditionValue, person.getProperty("user_name"));
	                if(!result) {
	                	result = false;
	                	//break;
	                }
                }
                
            	if (!result && user != null)
            	{
                	result = checkConditionValue(conditionValue,  user.getUserId());
                    if (!result) {
                    	result = false;
                    	break;
                    }
            	}
            }
            // End of ---------------------------------------------->
            
            // �̰��� �۾�ǥ�ؼ�
            if("norelease_operation".equals(key)) {
                //����
                if(configId==1) {
                    Object[] revs = (Object[]) operationMap.get("publish_rev");
                    String publishStatus = (String) operationMap.get("publish_status");

                    // [P0071] [20150127] ymjang, Publish �� ���� ������������ �ұ��ϰ� "�̰��� ����" ���߸� Ŭ���ϰ� ��ȸ�ϸ� �̰���� ���̶�� �Ʒ��� ���� ����
                    // ���� �������� ���� 'In Review' �� ǥ���ϰ�, �̰��� ������ ��Ÿ���� �ʵ��� ó��.
                    if (revs == null || (publishStatus != null && publishStatus != "")) {
                        result = false;
                        break;
                    }
                } 
                //����
                //[SR150611-013][20150714] shcho, ���� �۾�ǥ�ؼ� ��ȸ â �� "�� ���� ����" ��� ���� (������ ��� Working�� ���� ��θ� ���� �� ��)
                else {
                    String pdate_released = (String) operationMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED);
                    if(pdate_released != null) {
                        result = false;
                        break;
                    }
                }
            } 

            // [SR150312-024] [20150324] ymjang, Latest Working for ME ���¿��� ���� �۾�ǥ�ؼ� �۾� ������� ����
            // ���� ����
            if("release_operation".equals(key)) {
            	String pdate_released = (String) operationMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED);
        		if(pdate_released == null) {
                    result = false;
                    break;
                }
            }             
        }

        if (!result)
        	return result;
                
        for(String key : conditionMap.keySet()) {
        	
        	if ("empty_operation".equals(key) || "different_operation".equals(key))
        		result = false;
        	
            // ���� ���� (Publishing �� ���� �ϳ��� ���� ����)
            if("empty_operation".equals(key)) {
            	
            	Object objpublish_rev = operationMap.get("selected_publish_rev");
            	Object objkorpublish_rev = operationMap.get("kor_publish_rev");
            	
            	if (objkorpublish_rev != null && objpublish_rev == null)
            		result = true;
            	else
            		result = false;
            }
            
            // �ٸ� ���� (Publishing �������� ���� �������� �ٸ� ����)
            // [NO-SR] [20150323] shcho, ������ InProcess�� ���, ���� �ٸ� ���� �˻��� �߻��ϴ� ���� ���� (�� ����� ������ǥ �������� ���� ���������� �Ѵ�.)
            if("different_operation".equals(key)) {
            	Object objpublish_rev = operationMap.get("selected_publish_rev");
            	Object objOperation_rev = operationMap.get("item_revision_id");
            	
            	if (objOperation_rev != null && objpublish_rev != null) {
            		// [NO-SR] [20150127] ymjang ������ ������ �������� ������ ���������θ� ����.(Publishing �������� �ƴ�)
	            	String selected_publish_rev = objpublish_rev.toString();
	            	selected_publish_rev = selected_publish_rev.substring(0, 3);
	                String operation_rev = objOperation_rev.toString();
	                if (!selected_publish_rev.equals(operation_rev))
	                	result = true;
	                else
	                	result = false;
            	}
            }
            
            if (result)
            	break;
        }

        return result;
    }
    
    private boolean checkConditionValue(String condition, String value) {
        boolean result = false;
        condition = condition.toUpperCase();
        value = value.toUpperCase();
        if(condition.startsWith("*") && condition.endsWith("*")) {
            if(value.contains(condition.replace("*", ""))) {
                return true;
            }
        } else if(condition.startsWith("*") && !condition.endsWith("*")) {
            if(value.endsWith(condition.replace("*", ""))) {
                return true;
            }
        } else if(!condition.startsWith("*") && condition.endsWith("*")) {
            if(value.startsWith(condition.replace("*", ""))) {
                return true;
            }
        } else {
            if(value.equals(condition)) {
                return true;
            }
        }

        return result;
    }

    /**
     * [P0077] [20150303] ymjang, Publish �ð� ����
     * ���� ������ ���������� Expand �Ѵ�.
     * @param bopLine
     * @throws Exception
     */
    private void expandBelow(TCComponentBOPLine bopLine) throws Exception {
        
    	if (bopLine == null)
    		return;
    	
    	MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
    	AbstractViewableTreeTable treetable = mfgApp.getAbstractViewableTreeTable();
    	
    	String itemType = null;
    	TreeTableNode[] allNodes = treetable.getAllNodes(BOMLineNode.class);
		if(allNodes != null) {
			for(TreeTableNode node : allNodes) {
				if ( ((TCComponentBOPLine) (((BOMLineNode) node).getBOMLine())).equals(bopLine) )
				{
					if (treetable.isExpanded(node))
						continue;
					
					treetable.setSelectionPaths(new TreePath[] { node.getTreePath() });
					SDVBOPUtilities.executeExpandOneLevel();
					
		    		AIFComponentContext[] contexts = bopLine.getChildren();
		    		TCComponentBOPLine[] childLines = new TCComponentBOPLine[contexts.length];
		    		for (int i = 0; i < childLines.length; ++i) { 
		    			
		    			childLines[i] = ((TCComponentBOPLine) contexts[i].getComponent());
		    			if (childLines[i] == null)
		    				break;
		    			
		    			itemType = childLines[i].getItem().getType();
		    			if (itemType.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
		    				itemType.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) ||
	    					itemType.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ) 
	    		        {
		    				break;
	    		        }
		    			
		    			expandBelow(childLines[i]);
		    		}
					
				}
			}
		}
    }
    
}
