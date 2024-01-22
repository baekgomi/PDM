package com.symc.plm.me.sdv.operation.body;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.view.body.CreateWeldOPView;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;

/**
 * 
 * [SR140702-044][20140702] jwLee �������� IDü�� ���� (1. LOV�߰�, 2. Serial No.ü�� ����, 3. �������� �ߺ� �˻� �ҽ� �̵�)
 * [SR150524-002][20150730] shcho, �������� �����۾� �ð� ���� �ҿ� ���� 
 */
public class CreateWeldOPInitOperation extends AbstractSDVInitOperation {

    protected boolean applyFlag;
    // [SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ�
    private TCComponentBOPLine initedOperationBOPLine;

	public CreateWeldOPInitOperation() {
        super();
    }

    @Override
    public void executeOperation() throws Exception {
        try {
            // ������ ���� BOPLine �� �����´�.
        	TCComponentBOPLine operationBOPLine = initedOperationBOPLine;
        	// [SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ�
        	// UI�� �ʱ� �ν� �ɶ� ���õ� Operation BOPLine�� Ȱ���ϵ��� ����.
        	if(initedOperationBOPLine==null){
        		MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        		operationBOPLine = (TCComponentBOPLine) mfgApp.getSelectedBOMLines()[0];
        	}
            TCComponentBOPLine stationBOPLine = (TCComponentBOPLine) operationBOPLine.parent();
            RawDataMap targetDataMap = new RawDataMap();

            // MECO ItemRevision ����
            TCComponentItemRevision mecoItemRevision = null;
            String operationID = null;
            TCProperty[] tcProperty = operationBOPLine.getItemRevision().getTCProperties(new String[] { SDVPropertyConstant.OPERATION_REV_MECO_NO, SDVPropertyConstant.ITEM_ITEM_ID });
            if (tcProperty != null && tcProperty.length > 0) {
                mecoItemRevision = (TCComponentItemRevision) (tcProperty[0].getReferenceValue());
                operationID = tcProperty[1].getStringValue();
            }

            // ���� ���� WorkArea �� Gun ����Ʈ ����
            List<TCComponentBOPLine> workAreaList = new ArrayList<TCComponentBOPLine>();
            List<TCComponentBOPLine> gunBOPLineList = new ArrayList<TCComponentBOPLine>();
            getBelowBOPLines(operationBOPLine, workAreaList, gunBOPLineList);

            // Gun �� ����ϴ� ���������� �˾ƺ��� ���������� SerealNO �� �����´�
            String weldOPSerealNo = "01";
            weldOPSerealNo = getWeldOpSerealNo(stationBOPLine, operationID);

            targetDataMap.put("stationBOPLine", stationBOPLine, IData.OBJECT_FIELD); // TODO : View �� Action���� ó���� SDVTypeConstant.BOP_PROCESS_STATION_ITEM�� �͵��� StationBOPLine �� ���� �ʿ�
            targetDataMap.put("operationBOPLine", operationBOPLine, IData.OBJECT_FIELD); // TODO : View �� Action���� ó���� opList(LIST_FIELD)�� �͵��� OperationBOPLine (OBJECT_FIELD)�� ���� �ʿ�
            targetDataMap.put("mecoItemRevision", mecoItemRevision, IData.OBJECT_FIELD); // TODO : View �� Action���� ó���� mecoList(LIST_FIELD)�� �͵��� MECOItemRevision (OBJECT_FIELD)�� ���� �ʿ�, �׸��� MECORevision�� null�ΰ�쿡 ���� nulló�� �߰�
            targetDataMap.put(SDVPropertyConstant.STATION_IS_ALTBOP, stationBOPLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.STATION_IS_ALTBOP), IData.STRING_FIELD);
            targetDataMap.put(SDVPropertyConstant.STATION_ALT_PREFIX, stationBOPLine.getProperty(SDVPropertyConstant.STATION_ALT_PREFIX), IData.STRING_FIELD);
            targetDataMap.put("gunBOPLineList", gunBOPLineList, IData.LIST_FIELD); // TODO : View �� Action���� ó���� operationID (LIST_FIELD) �� �͵��� gunBOPLineList�� ���� �ʿ�
            targetDataMap.put("weldOPSerealNo", weldOPSerealNo, IData.STRING_FIELD); // TODO : View �� Action���� ó���� operationID + "-gun" (OBJECT_FIELD)�� �͵��� weldOPSerealNo (STRING_FIELD)�� ���� �ʿ�
            targetDataMap.put("workAreaList", workAreaList, IData.LIST_FIELD);// TODO : View �� Action���� ó���� operationID + "-workArea" �� �͵��� workAreaList�� ���� �ʿ�

            DataSet targetDataset = new DataSet();
            targetDataset.addDataMap("WeldOPViewInit", targetDataMap);

            setData(targetDataset);

            // apply �϶��� ����
            if (applyFlag != false) {
                AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
                CreateWeldOPView weldView = (CreateWeldOPView) dialog.getView("createWeldOP");
                weldView.setApplyDataSet(targetDataset);
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    private String getWeldOpSerealNo(TCComponentBOPLine stationBOMLine, String operationID) throws Exception {
        String nextSerialNo = "01";

        TCProperty tcProperty = stationBOMLine.getTCProperty("bl_rev_ps_children");
        String[] propValues = tcProperty.getStringDelimitedValues(",");
        HashSet<String> weldOPNoSet = new HashSet<String>();
        for (String displayableChildID : propValues) {
            if (displayableChildID.contains(operationID + "-WEOP")) {
            	// [NON_SR] [20151104] taeku.joeng ������ ����� ��û���� ������ �ľ��� ����
            	// �̹� ������ Weld Operatoin�� ������ Index�ϱ����� Data�� ����°����� ���ʿ���
            	// ���ڿ��� �Բ��־ ������ �߻���
            	String tempStr = displayableChildID.split("-")[5];
            	if(tempStr.indexOf("/")>=0){
            		tempStr = tempStr.substring(0, tempStr.indexOf("/"));
            	}
                weldOPNoSet.add(tempStr);
            }
        }

        if (weldOPNoSet.isEmpty()) {
            return nextSerialNo;
        }
        String[] arrWeldOpNo = weldOPNoSet.toArray(new String[weldOPNoSet.size()]);
        Arrays.sort(arrWeldOpNo);

        for (int i = 0; i < arrWeldOpNo.length; i++) {
            int serialNO = i + 1;
            String strWeldOPNo = arrWeldOpNo[i];
            int weldOPNo = Integer.parseInt(strWeldOPNo);
            
            if (serialNO == weldOPNo) {
                nextSerialNo = String.format("%02d", serialNO + 1);
                continue;
            } else {
                break;
            }
        }

        return nextSerialNo;
    }

    /**
     * 
     * 
     * @param gunList
     * @method getGunLengthList
     * @date 2014. 6. 23.
     * @param
     * @return
     * @return List<Integer>
     * @exception
     * @throws
     * @see
     */
    private void getBelowBOPLines(TCComponentBOPLine operationBOPLine, List<TCComponentBOPLine> workAreaList, List<TCComponentBOPLine> gunBOPLineList) throws Exception {
        TCProperty tcProperty = operationBOPLine.getTCProperty("Mfg0assigned_workarea");
        TCComponent[] tcComponents = tcProperty.getReferenceValueArray();
        for (TCComponent tcComponent : tcComponents) {
            TCComponentBOPLine assignedWorkArea = (TCComponentBOPLine) tcComponent;
            AIFComponentContext[] workAreaChildren = assignedWorkArea.getChildren();
            for (AIFComponentContext workAreaChildComp : workAreaChildren) {
                TCComponentBOPLine resourceBOPLine = (TCComponentBOPLine) workAreaChildComp.getComponent();
                if (resourceBOPLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_GUN_ITEM)) {
                    workAreaList.add(assignedWorkArea);
                    gunBOPLineList.add(resourceBOPLine);
                }
            }
        }
    }

    public void applyAction(boolean flag) throws Exception {
        this.applyFlag = flag;
        executeOperation();
    }
    
    /**
     * [SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ� 
     * @return
     */
    public TCComponentBOPLine getInitedOperationBOPLine() {
		return initedOperationBOPLine;
	}

    /**
     * [SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ�
     * @param initedOperationBOPLine
     */
	public void setInitedOperationBOPLine(TCComponentBOPLine initedOperationBOPLine) {
		this.initedOperationBOPLine = initedOperationBOPLine;
	}

}
