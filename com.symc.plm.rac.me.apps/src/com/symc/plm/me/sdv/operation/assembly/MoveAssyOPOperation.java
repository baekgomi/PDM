/**
 * 
 */
package com.symc.plm.me.sdv.operation.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.tree.TreePath;

import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.BOPLineUtility;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.psebase.common.AbstractViewableNode;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.treetable.TreeTableNode;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : MoveBOPLineOperation
 * Class Description : ���õ� BOPLINE(����)�� �ٸ� �������� �̵� ��Ű�� Operation
 * 
 * @date 2014. 2. 17.
 * 
 */
public class MoveAssyOPOperation extends AbstractSDVActionOperation {

    private String COPY_PROP_LIST = "SYMC_MOVE_OP_PROP_LIST"; // ������ BOPLINE �Ӽ� ����Ʈ
    private Registry registry = null;

    public MoveAssyOPOperation(int actionId, String ownerId, IDataSet dataSet) {
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

        if (isAbortRequested())
            return;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {

        IDataSet dataSet = getDataSet();
        registry = Registry.getRegistry(this);
        try {
            TCComponentBOMLine targetBOMLine = (TCComponentBOMLine) dataSet.getValue("inform", "TARGET_BOMLINE");
            @SuppressWarnings("unchecked")
            ArrayList<TCComponentBOMLine> srcBOMLineList = (ArrayList<TCComponentBOMLine>) dataSet.getValue("inform", "MOVE_LIST");

            TCSession tcSession = targetBOMLine.getSession();
            TCPreferenceService tcPrefService = tcSession.getPreferenceService();
            TCVariantService tcVarService = tcSession.getVariantService();
            /**
             * Copy �� BOP �Ӽ��� ����Ʈ�� Preference���� ������
             */
//            String[] copyPropList = tcPrefService.getStringArray(TCPreferenceService.TC_preference_site, COPY_PROP_LIST);
            String[] copyPropList = tcPrefService.getStringValuesAtLocation(COPY_PROP_LIST, TCPreferenceLocation.OVERLAY_LOCATION);

            if (copyPropList.length == 0) {
                setAbortRequested(true);
                throw new Exception(registry.getString("NotExistMovePref.MSG"));
            }

            /**
             * Target�� �̵��� ������ �߰� �� BOP �Ӽ� ����
             * 1. Target�� ���� �߰�
             * 2. ���� BOP ����
             * 3. ���� �ڵ�, �۾��� �ڵ� ���� �̵��� Line �ڵ�� Update
             * 4. Find NO �̵��� Line �ڵ�� Update
             */
            for (TCComponentBOMLine srcBOMLine : srcBOMLineList) {

                if (srcBOMLine.parent().equals(targetBOMLine))
                    continue;
                // 1. Target�� ���� �߰�
                TCComponentBOMLine newBOMLine = targetBOMLine.add(null, srcBOMLine.getItemRevision(), null, false);

                String targetLineCode = targetBOMLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE); // Line �ڵ�

                String srcStationNo = srcBOMLine.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO); // ���� �ڵ�
                String srcWorkerCode = srcBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_WORKER_CODE); // �۾��� �ڵ�
                String processSeq = srcBOMLine.getItem().getProperty(SDVPropertyConstant.OPERATION_PROCESS_SEQ); // �۾� ��

                String[] stationSplit = srcStationNo.split("-");
                String[] workCodeSplit = srcWorkerCode.split("-");

                // 2. ������ BOP �Ӽ��� ������
                for (String propertyName : copyPropList) {
                    // Variant Condition �Ӽ��� ���
                    if (propertyName.equals(SDVPropertyConstant.BL_VARIANT_CONDITION)) {
                        String variantCondition = srcBOMLine.getProperty(SDVPropertyConstant.BL_VARIANT_CONDITION);
                        tcVarService.setLineMvlCondition(newBOMLine, variantCondition);
                    } else {
                        if (!srcBOMLine.isValidPropertyName(propertyName))
                            continue;
                        // BOP �Ӽ� ����
                        newBOMLine.setProperty(propertyName, srcBOMLine.getProperty(propertyName));
                    }
                }

                // �����ڵ�, �۾��� �ڵ� üũ
                if (stationSplit.length != 2 || workCodeSplit.length != 2)
                    continue;
                // 3. ���� �ڵ�, �۾��� �ڵ� ���� �̵��� Line �ڵ�� Update
                String newStationNo = targetLineCode.concat("-").concat(stationSplit[1]);
                String newWorkerCode = targetLineCode.concat("-").concat(workCodeSplit[1]);
                newBOMLine.getItemRevision().setProperty(SDVPropertyConstant.OPERATION_REV_STATION_NO, newStationNo);
                newBOMLine.getItem().setProperty(SDVPropertyConstant.OPERATION_WORKER_CODE, newWorkerCode);
                // 4. Find NO �̵��� Line �ڵ�� Update
                String findNo = newStationNo.replace("-", "").concat("|").concat(newWorkerCode.replace("-", "")).concat("|").concat(processSeq);
                newBOMLine.setProperty(SDVPropertyConstant.BL_SEQUENCE_NO, findNo);
                
                // [NON-SR][20160113] taeku.jeong Line, Station, Operation, weldOperation�� bl_abs_occ_id ���� �����Ѵ�. 
            	BOPLineUtility.updateLineToOperationAbsOccId(newBOMLine);
                
                newBOMLine.refresh();
            }

            /**
             * Source ���� BOP���� ����
             */
            for (TCComponentBOMLine srcBOMLine : srcBOMLineList) {
                if (srcBOMLine.parent().equals(targetBOMLine))
                    continue;
                SDVBOPUtilities.disconnectObjects(srcBOMLine.parent(), new ArrayList<TCComponentBOMLine>(Arrays.asList(srcBOMLine)));
            }

            /**
             * �߰��� �Ŀ� Target BOPLINE Expand ��
             */
            MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
            AbstractViewableTreeTable treetable = mfgApp.getAbstractViewableTreeTable();
            AbstractViewableNode rootNode = treetable.getRootNode();
            // �ֻ����� Expand �� �Ǿ������� Expand
            if (!treetable.isExpanded(rootNode))
                SDVBOPUtilities.executeExpandOneLevel();
            Iterator<TreeTableNode> iterator = rootNode.allChildrenIterator(true);
            while (iterator != null && iterator.hasNext()) {
                AbstractViewableNode childNode = (AbstractViewableNode) iterator.next();
                if (childNode.getName().startsWith(targetBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID))) {
                    // ���õǰ� �ϰ�
                    treetable.setSelectionPaths(new TreePath[] { childNode.getTreePath() });
                    // Expand ��
                    SDVBOPUtilities.executeExpandOneLevel();
                    break;
                }
            }

        } catch (Exception ex) {
            setAbortRequested(true);
            throw ex;
        }
    }
}
