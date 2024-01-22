/**
 *
 */
package com.symc.plm.me.sdv.operation.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;
import org.springframework.util.StringUtils;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentCfgAttachmentLine;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMECfgLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * [SR141219-020][20150108] shcho, Open with Time â������ Activity �۾����� ����ġ �� ���� ���� �Ұ� ���� �ű� ȭ�� �߰�
 */
public class ManageActivityActionOperation extends AbstractSDVActionOperation {

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public ManageActivityActionOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void executeOperation() throws Exception {
        // MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "Action Operation OK.", registry.getString("Inform.NAME"), MessageBox.INFORMATION);

        IDataSet dataSet = getDataSet();
        TCComponentBOMLine operationLine = (TCComponentBOMLine) dataSet.getValue("activityView", "OperationLine");
        Object newDataMap = dataSet.getValue("activityView", "newTableList");
        Object oldDataMap = dataSet.getValue("activityView", "oldTableList");
        /**
         * [SR190104-050] ������ ���� ��û Bop Activity ���� �� ����� �ɸ��� �ð� ���� ��û
         * ���� ���� : createActivities -> resaveActivities �� ����
         * ���� Activity ���� �� ���� �ÿ��� ���� ������ ��� Activity���� ���� ����� �ѵڿ� �Ӽ������� ���� ������
         * ���ʿ��� �������� ���� �ҿ�Ǵ� �ð��� ���� ���� ����
         * ���� ������ Activity���� ������ �� �Ͽ� ������ �Ӽ������� ���� 
         * �ٸ� ��쿡�� ���̳��� ������ŭ ���� �� �߰� ������ �Ͽ� Activity �Ӽ� ���� �����Ͽ� �ҿ� �ð� ����
         */
        List<HashMap<String, Object>> tableList = ((List<HashMap<String, Object>>) newDataMap);
        List<HashMap<String, Object>> oldTableList = ((List<HashMap<String, Object>>) oldDataMap);
        

        TCSession session = CustomUtil.getTCSession();
        Markpoint mp = new Markpoint(session);
        try {
        	
        	/**
             * [SR190104-050] ������ ���� ��û Bop Activity ���� �� ����� �ɸ��� �ð� ���� ��û
             * ���� ���� : createActivities -> resaveActivities �� ����
             * ���� Activity ���� �� ���� �ÿ��� ���� ������ ��� Activity���� ���� ����� �ѵڿ� �Ӽ������� ���� ������
             * ���ʿ��� �������� ���� �ҿ�Ǵ� �ð��� ���� ���� ����
             * ���� ������ Activity���� ������ �� �Ͽ� ������ �Ӽ������� ���� 
             * �ٸ� ��쿡�� ���̳��� ������ŭ ���� �� �߰� ������ �Ͽ� Activity �Ӽ� ���� �����Ͽ� �ҿ� �ð� ����
             */
//            createActivities( tableList, operationLine);
            resaveActivities( oldTableList, tableList, operationLine);
        } catch (Exception ex) {
            mp.rollBack();
            setErrorMessage(ex.getMessage());
            setExecuteError(ex);
            throw ex;
        }

        mp.forget();
    }

    /**
     * <���� �����> Activity ���� (BOMWindow ���������ʰ� ActivityLine ����)
     * 
     * @method createActivities
     * @date 2014. 1. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createActivities(List<HashMap<String, Object>> tableList, TCComponentBOMLine operationLine) throws Exception, TCException {
    	long startTime = System.currentTimeMillis();
    	
    	
        // Child Activity Remove
        TCComponentCfgActivityLine[] childActivityList = getChildActivityList(operationLine);
        for (TCComponentCfgActivityLine meActivityLine : childActivityList) {
            TCComponentMECfgLine parentLine = meActivityLine.parent();
            ActivityUtils.removeActivity(meActivityLine);
            parentLine.save();
        }
        TCComponent root = operationLine.getReferenceProperty("bl_me_activity_lines");
        // activity refresh
        refreshBOMLine(root);
        refreshBOMLine(operationLine);

        // Activity ����
        TCComponent[] afterTCComponents = null;
        if (tableList != null && tableList.size() > 0) {
            TCComponent[] rootActivityComponent = new TCComponent[tableList.size()];
            TCComponent[] lastChildActivityComponent = new TCComponent[tableList.size()];
            for (int i = 0; i < rootActivityComponent.length; i++) {
                rootActivityComponent[i] = root;
            }
            afterTCComponents = ActivityUtils.createActivities(rootActivityComponent, lastChildActivityComponent, "Activity");
            // ���� ������ �Ӽ� ������Ʈ
            if (afterTCComponents != null && afterTCComponents.length > 0) {
                for (int i = 0; i < tableList.size(); i++) {
                    HashMap<String, Object> tableMap = tableList.get(i);
                    TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) afterTCComponents[i];
                    TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();

                    for (String key : tableMap.keySet()) {
                        // �� ���� �׸� : �ǳʶڴ�.
                        if (key.equals("seq")) {
                            continue;
                        }
                        // double ����
                        if (key.equals(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)) {
                            double timeSystemUnitTime = 0.0;
                            if (!StringUtils.isEmpty((String) tableMap.get(key))) {
                                timeSystemUnitTime = Double.parseDouble((String) tableMap.get(key));
                            }
                            activity.setDoubleProperty(key, timeSystemUnitTime);
                        }

                        // LOV Value ����
                        else if (key.equals(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY)) {
                            String value = tableMap.get(key).toString();
                            if (value != null) {
                                activity.setStringProperty(key, getCategoryLovValue(value));
                            }
                        }

                        // �迭 Value ����
                        else if (key.equals(SDVPropertyConstant.ACTIVITY_WORKER)) {
                            String value = tableMap.get(key).toString();
                            if (value != null) {
                                String[] arrValue = new String[] { value };
                                activity.getTCProperty(key).setStringValueArray(arrValue);
                            }
                        }

                        // String ����
                        else {
                            String value = tableMap.get(key).toString();
                            if (value != null) {
                                activity.setStringProperty(key, value);
                            }
                        }
                    }

                    // ITK���� object_name �� ����Ǹ� �ڵ����� m7_ENG_NAME�� �����Ѵ�.
                    // ������ ���� �������� m7_ENG_NAME�� �ٽ� �����Ͽ� ��ü m7_ENG_NAME�� ������� ���� ������ �Ѵ�.
                    String engName = (String) tableMap.get(SDVPropertyConstant.ACTIVITY_ENG_NAME);
                    if (engName != null) {
                        activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).setStringValue(engName);
                    }

                    activity.save();
                    root.save();
                }
                // ���� ���н�
            } else {
                for (int i = 0; i < childActivityList.length; i++) {
                    if (root instanceof TCComponentMECfgLine) {
                        ActivityUtils.addActivity((TCComponentMECfgLine) root, (TCComponentMEActivity) childActivityList[i].getUnderlyingComponent());
                    } else if (root instanceof TCComponentMEActivity) {
                        ActivityUtils.addActivity((TCComponentMEActivity) root, (TCComponentMEActivity) childActivityList[i].getUnderlyingComponent());
                    }
                }
                root.save();
            }
        }
        long endTime = System.currentTimeMillis();
        
        System.out.println("�ҿ�ð� : " + (endTime - startTime ) + "ms" ) ;
    }
    
    
    
    /**
     * [SR190104-050] ������ ���� ��û Bop Activity ���� �� ����� �ɸ��� �ð� ���� ��û
     * ���� ���� : createActivities -> resaveActivities �� ����
     * ���� Activity ���� �� ���� �ÿ��� ���� ������ ��� Activity���� ���� ����� �ѵڿ� �Ӽ������� ���� ������
     * ���ʿ��� �������� ���� �ҿ�Ǵ� �ð��� ���� ���� ����
     * ���� ������ Activity���� ������ �� �Ͽ� ������ �Ӽ������� ���� 
     * �ٸ� ��쿡�� ���̳��� ������ŭ ���� �� �߰� ������ �Ͽ� Activity �Ӽ� ���� �����Ͽ� �ҿ� �ð� ����
     * 
     * @method createActivities
     * @date 2014. 1. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void resaveActivities(List<HashMap<String, Object>> oldTableList, List<HashMap<String, Object>> newTableList, TCComponentBOMLine operationLine) throws Exception, TCException {
    	
    	long startTime = System.currentTimeMillis();
    	TCComponentCfgActivityLine[] childActivityList = getChildActivityList(operationLine);
    	 TCComponent root = operationLine.getReferenceProperty("bl_me_activity_lines");
         // activity refresh
         refreshBOMLine(root);
         refreshBOMLine(operationLine);
    	if( oldTableList.size() ==  newTableList.size() ) {
    		long equalQuantityStartTime = System.currentTimeMillis();
    		saveActivitiesProperties( root, newTableList, childActivityList);
    		 long equalQuantityEndTime = System.currentTimeMillis();
    	        System.out.println("���� ���� �ҿ�ð� : " + (equalQuantityEndTime - equalQuantityStartTime ) + "ms" ) ;
    		
    	} else {
    		long notEqualQuantityStartTime = System.currentTimeMillis();
    		TCComponent[] afterTCComponents = null;
    		int compareSize = ( newTableList.size() - oldTableList.size() );
    		
    		if( compareSize > 0 ) {
    			// Activity�� �߰� �Ǿ� ���� ��ŭ ����
    			
    			TCComponent[] rootActivityComponent = new TCComponent[compareSize];
                TCComponent[] lastChildActivityComponent = new TCComponent[compareSize];
                for (int i = 0; i < rootActivityComponent.length; i++) {
                    rootActivityComponent[i] = root;
                }
    			TCComponent[] activities =  ActivityUtils.createActivities(rootActivityComponent, lastChildActivityComponent, "Activity");

    			childActivityList = getChildActivityList(operationLine);
                refreshBOMLine(root);
                refreshBOMLine(operationLine);
                saveActivitiesProperties( root, newTableList, childActivityList);
                long notEqualQuantityEndTime = System.currentTimeMillis();
    	        System.out.println(" �߰� ���� �ҿ�ð� : " + (notEqualQuantityEndTime - notEqualQuantityStartTime ) + "ms" ) ;
    			
    		} else {
    			// Activity�� ���� �Ǿ� ������ŭ ����
    			   compareSize = - compareSize;
    			
    			for ( int i = childActivityList.length - 1; i >=  (childActivityList.length - compareSize); i -- ) {
    	            TCComponentMECfgLine parentLine = childActivityList[i].parent();
    	            ActivityUtils.removeActivity(childActivityList[i]);
    	            parentLine.save();
    	        }
    			childActivityList = getChildActivityList(operationLine);
    			refreshBOMLine(root);
    			refreshBOMLine(root);
    	        refreshBOMLine(operationLine);
    			saveActivitiesProperties( root, newTableList, childActivityList);
    			long notEqualQuantityEndTime = System.currentTimeMillis();
    	        System.out.println(" ���� �ҿ�ð� : " + (notEqualQuantityEndTime - notEqualQuantityStartTime ) + "ms" ) ;
    		}
    		
    		
    		
    	}
       
    	System.gc();
        long endTime = System.currentTimeMillis();
        System.out.println("�� �ҿ�ð� : " + (endTime - startTime ) + "ms" ) ;
    }
    
    
    public void saveActivitiesProperties ( TCComponent root ,List<HashMap<String, Object>> newTableList, TCComponentCfgActivityLine[] childActivityList ) throws Exception, TCException {
    	boolean chageFlag = false;
    	 for (int i = 0; i < newTableList.size(); i++) {
             HashMap<String, Object> tableMap = newTableList.get(i);
             TCComponentCfgActivityLine activityLine = (TCComponentCfgActivityLine) childActivityList[i];
             TCComponentMEActivity activity = (TCComponentMEActivity) activityLine.getUnderlyingComponent();

             for (String key : tableMap.keySet()) {
                 // �� ���� �׸� : �ǳʶڴ�.
                 if (key.equals("seq")) {
                     continue;
                 }
                 // double ����
                 if (key.equals(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME)) {
                     double timeSystemUnitTime = 0.0;
                     if (!StringUtils.isEmpty((String) tableMap.get(key))) {
                         timeSystemUnitTime = Double.parseDouble((String) tableMap.get(key));
                     } 
                     
                     double activyValue = activity.getDoubleProperty(key);
                     if( activyValue != timeSystemUnitTime) {
                    	 chageFlag = true;
                    	 activity.setDoubleProperty(key, timeSystemUnitTime);
                     }
                 }

                 // LOV Value ����
                 else if (key.equals(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY)) {
                     String value = tableMap.get(key).toString();
                     if (value != null) {
                    	 String activyValue = activity.getStringProperty(key);
                    	 if( !activyValue.equals(getCategoryLovValue(value))) {
                    		 chageFlag = true;
                    		 activity.setStringProperty(key, getCategoryLovValue(value));
                    	 }
                     }
                     
                 }

                 // �迭 Value ����
                 else if (key.equals(SDVPropertyConstant.ACTIVITY_WORKER)) {
                     String value = tableMap.get(key).toString();
                     if (value != null) {
                         String[] arrValue = value.split(",");
                         String[] actitypeArray =  activity.getTCProperty(key).getStringValueArray();
                         
                         try {
                        	 if( actitypeArray.length == 0 ) {
                        		  actitypeArray =  new String[] { "" };
                        		 
                        	 } else {
                        		 for( int j = 0; j < actitypeArray.length; j ++ ) {
                        			 if( actitypeArray[j] == null ) {
                            			 actitypeArray[j] =  "";
                            		 } else {
                            			 actitypeArray[j] = actitypeArray[j].trim();
                            		 }
                        		 }
                        	 }
                        	 
                        	 if( arrValue.length > 1) {
                        		 for( int j = 0; j < arrValue.length; j++ ) {
                        			 arrValue[j] = arrValue[j].trim();
                        		 }
                        	 }
                        	 
                         } catch ( ArrayIndexOutOfBoundsException e  ) {
                        	System.out.println( "���� �׸�" + tableMap.get("seq") + "	��� : " + (i + 1));
                         }
                         
                         if( !Arrays.equals(arrValue, actitypeArray)) {
                        	 chageFlag = true;
                        	 activity.getTCProperty(key).setStringValueArray(arrValue);
                         }
                     }
                 }

                 // String ����
                 else {
                     String value = tableMap.get(key).toString();
                     if (value != null) {
                    	 String activyValue = activity.getStringProperty(key);
                    	 if( !activyValue.equals(value)) {
                    		 chageFlag = true;
                    		 activity.setStringProperty(key, value);
                    	 }
                     }
                 }
             }

             // ITK���� object_name �� ����Ǹ� �ڵ����� m7_ENG_NAME�� �����Ѵ�.
             // ������ ���� �������� m7_ENG_NAME�� �ٽ� �����Ͽ� ��ü m7_ENG_NAME�� ������� ���� ������ �Ѵ�.
             String engName = (String) tableMap.get(SDVPropertyConstant.ACTIVITY_ENG_NAME);
             if (engName != null) {
            	 
            	 if( !activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).equals(engName)) {
            		 chageFlag = true;
            		 activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).setStringValue(engName);
            	 }
             }
             
//             if( chageFlag ) {
//            	 try {
//            		 
//            		 activity.save();
//            	 } catch(Exception e) {
//            		 e.getStackTrace();
//            	 }
//            	 
//             }
             root.save();
         }
    }

    /**
     * Activity Category�� LOV�� ��ȯ�Ѵ�.
     * 
     * @method getCategoryLovValue
     * @date 2013. 12. 10.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getCategoryLovValue(String category) {
        if ("�۾�������".equals(category)) {
            category = "01";
        } else if ("�ڵ�".equals(category)) {
            category = "02";
        } else if ("����".equals(category)) {
            category = "03";
        } else {
            category = "NA";
        }
        return category;
    }

    private TCComponentCfgActivityLine[] getChildActivityList(TCComponentBOMLine bomLine) throws Exception {
        ArrayList<TCComponentCfgActivityLine> childActivityList = new ArrayList<TCComponentCfgActivityLine>();
        TCComponent root = bomLine.getReferenceProperty("bl_me_activity_lines");
        if (root != null) {
            if (root instanceof TCComponentCfgActivityLine) {
                TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) root);
                for (TCComponent childLine : childLines) {
                    if (childLine instanceof TCComponentCfgActivityLine) {
                        childActivityList.add((TCComponentCfgActivityLine) childLine);
                    }
                }
            }
        }
        return childActivityList.toArray(new TCComponentCfgActivityLine[childActivityList.size()]);
    }

    /**
     * BOMLine refresh
     * 
     * @method refreshBOMLine
     * @date 2013. 12. 12.
     * @param
     * @exception
     * @return void
     * @throws
     * @see
     */
    private void refreshBOMLine(Object tcComponent) throws Exception {
        // Activity Refresh
        if (tcComponent instanceof TCComponentMECfgLine) {
            TCComponentMECfgLine tcComponentMECfgLine = (TCComponentMECfgLine) tcComponent;
            tcComponentMECfgLine.clearCache();
            tcComponentMECfgLine.window().fireChangeEvent();
            tcComponentMECfgLine.refresh();
        } else if (tcComponent instanceof TCComponentCfgAttachmentLine) {
            TCComponentCfgAttachmentLine tcComponentCfgAttachmentLine = (TCComponentCfgAttachmentLine) tcComponent;
            tcComponentCfgAttachmentLine.clearCache();
            tcComponentCfgAttachmentLine.window().fireChangeEvent();
            tcComponentCfgAttachmentLine.refresh();
        }
        // BOMLine Refresh
        else if (tcComponent instanceof TCComponentBOMLine) {
            TCComponentBOMLine tcComponentBOMLine = (TCComponentBOMLine) tcComponent;
            tcComponentBOMLine.clearCache();
            tcComponentBOMLine.refresh();
            tcComponentBOMLine.window().newIrfWhereConfigured(tcComponentBOMLine.getItemRevision());
            tcComponentBOMLine.window().fireComponentChangeEvent();
        }
    }

}
