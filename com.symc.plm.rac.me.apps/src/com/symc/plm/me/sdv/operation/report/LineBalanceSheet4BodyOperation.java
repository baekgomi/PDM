/**
 *
 */
package com.symc.plm.me.sdv.operation.report;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawData;
import org.sdv.core.common.data.RawDataMap;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;
import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VectorHelper;

/**
 * Class Name : LineBalanceSheet4BodyOperation
 * Class Description :
 * [SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
 * 
 * @date 2013. 10. 29.
 *
 */
public class LineBalanceSheet4BodyOperation extends SimpleSDVExcelOperation {
    Registry registry;
    TCSession session;
    Shell parentShell;
    TCComponentBOPLine []targetBOPLines;

    /**
     * �۾��ں� ���̽ð��� �Ұ��� ���ð� Ŭ����
     *
     * Class Name : WorkerTimeInfo
     * Class Description :
     * @date 2013. 10. 31.
     *
     */
    class WorkerTimeInfo {
        // �۾��� �ڵ�
        public String workerCode;
        // �۾��� �۾��ð�
        public double workTime;
        // �۾��� ��� �ð�
        public double waitTime;
    }

    /**
     * �������� �ִ��۾��ð� ������ ã�� �����ϱ� ���� �ʿ��� Ŭ����
     * (���̽ð� �� �Ұ��� ���ð� ���� �ٽ� ã�� �ʱ� ���� ã�� ��� ���� ���Ϲޱ� ���� �����)
     *
     * Class Name : MaxTimeOpInfo
     * Class Description :
     *
     * @date 2013. 10. 17.
     *
     */
    class MaxTimeOpInfo {
        // �������� Activity�� ����/�Ұ��� ���ð��� ������ ���� �� ����
        public TCComponentBOPLine opLine;
        // ���̽ð��� ����
        public double maxUserWorkTime;
        // �Ұ��� ���ð��� ����
        public double maxUserWaitTime;
        // �۾��� �ڵ� ����Ʈ
        public ArrayList<WorkerTimeInfo> workerList;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.ISDVOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try {
            // MPP���� ������ ��� �� �ʱ� ���� �ε�
            initOperation();

            // ������ BOP ������ ���� ��� ������ �ִ�ð� ���� ��ȸ �� �׼����Ϸ� ������ ���� ó��
            IDataSet dataList = getData();

            // ������ġ �� ���ø����� ���� �ٿ�ε�
            String defaultFileName = registry.getString("exportLineBalacingBody.FileName", "LineBalancingListBody") + "_" + ExcelTemplateHelper.getToday("yyMMdd");
            transformer.print(ExcelTemplateHelper.EXCEL_SAVE, getTemplatePreference(), defaultFileName, dataList);

            setExecuteResult(SUCCESS);
        } catch(Exception e) {
        	setErrorMessage(e.getMessage());
            setExecuteError(e);
            // MessageBox�� ������ �޽���
            // �������� ������ default �޽����� �����ش�.
            //setErrorMessage("");
        }
    }

    /* (non-Javadoc)
     * @see com.symc.plm.me.sdv.operation.SimpleSDVExcelOperation#getData()
     */
    @Override
    protected IDataSet getData() throws TCException {
        // MPP���� ������ �� �������� �� üũ
        // ���� �������� ���� ���� ��������

        List<HashMap<String, Object>> dataList = new ArrayList<HashMap<String,Object>>();
        ArrayList<MaxTimeOpInfo> maxTimeOPForStationList = new ArrayList<MaxTimeOpInfo>();;
        IDataSet dataSet = null;
        try
        {
            // ���� ���� �������� �˻��Ͽ� �۾��ð��� ���� �� ��������Ʈ�� �����Ѵ�.
            getStationList(targetBOPLines, maxTimeOPForStationList);

            // ������ ���� ����Ʈ�� �׼��� ����ϱ� ���� ���·� ����Ÿ�� ��ȯ�Ѵ�.
            setExportDataInfo(maxTimeOPForStationList, dataList);

            // ����Ÿ�� �ٽ� ��ȯ
            dataSet = new DataSet();
            IDataMap dataMap = new RawDataMap();
            dataMap.put(registry.getString("BODYIDataBodyName"), dataList, IData.TABLE_FIELD);
            dataSet.addDataMap(registry.getString("BODYIDataBodyName"), dataMap);

            if (targetBOPLines != null && targetBOPLines.length > 0)
            {
                // ����Ÿ�� ������� ���� Shop JPH, Product Code
                IDataMap headMap = new RawDataMap();
                
                //[SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
                TCComponentBOPLine shopLine = (TCComponentBOPLine) targetBOPLines[0].window().getTopBOMLine();
                String str_shop_jph = shopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_JPH);
                double shopJPH = 0.0;
                if (str_shop_jph == null || str_shop_jph.trim().length() == 0)
                    shopJPH = 0.0;
                else
                    shopJPH = Double.parseDouble(str_shop_jph);

                String prodCode = shopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);


                IData jphData = new RawData();
                jphData.setValue(shopJPH);
                headMap.put(SDVPropertyConstant.SHOP_REV_JPH, jphData);

                IData prodCodeData = new RawData();
                prodCodeData.setValue(prodCode);
                headMap.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, prodCodeData);

                dataSet.addDataMap(registry.getString("BODYIDataHeadName"), headMap);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            throw new TCException(ex);
        }

        return dataSet;
    }

    /**
     * �׼��� ����Ÿ�� ����ϱ� ���� ���� ó�� �Լ�
     * [SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
     *
     * @method setExportDataInfo
     * @date 2013. 10. 30.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setExportDataInfo(List<MaxTimeOpInfo> maxTimeOPForStationList, List<HashMap<String, Object>> dataList) throws Exception {
        double shop_JPH = 0.0;
        HashMap<String, List<String>> workers_station = new HashMap<String, List<String>>();

        if (maxTimeOPForStationList == null || maxTimeOPForStationList.size() == 0)
            throw new NullPointerException("Station List is null.");

        if (dataList == null)
            dataList = new ArrayList<HashMap<String,Object>>();

        // Shop JPH ��������
        String str_shop_jph = maxTimeOPForStationList.get(0).opLine.window().getTopBOMLine().getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_JPH);
        if (str_shop_jph == null || str_shop_jph.trim().length() == 0)
            shop_JPH = 0.0;
        else
            shop_JPH = Double.parseDouble(str_shop_jph);

        for (MaxTimeOpInfo opInfo : maxTimeOPForStationList)
        {
            HashMap<String, Object> propertyMap = new HashMap<String, Object>();

            String station_code = null;
            String str_worker = null;

            // �����ڵ� ��������
            station_code = opInfo.opLine.parent().getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE);

            // ���⿡ �۾��� �ڵ带 ����ϰ� üũ�ؼ� ���ڷ� �����ϰ� '()'(�ߺ� �۾���)�� ġ�� ���� �ؾ� �Ѵ�.
            int dup_worker_count = 0;
            int in_worker_count = 0;
            if (opInfo.workerList != null)
            {
                for (WorkerTimeInfo worker : opInfo.workerList) {
                    if (workers_station.containsKey(worker.workerCode)) {
                        // �۾��� ����Ʈ�� ������ �ߺ��۾���
                        if (! workers_station.get(worker.workerCode).contains(station_code))
                            workers_station.get(worker.workerCode).add(station_code);

                        dup_worker_count++;
                    } else {
                        // �۾��� ����Ʈ�� ������ �ߺ��۾��ڰ� �ƴϴ�.
                        ArrayList<String> worker_station = new ArrayList<String>();
                        worker_station.add(station_code);
                        workers_station.put(worker.workerCode, worker_station);

                        in_worker_count++;
                    }
                }
                // �׼��� �۾��� ���� ó���ϴ� �κ�
                if (dup_worker_count + in_worker_count > 0)
                {
                    if (dup_worker_count > 0)
                        str_worker = in_worker_count + "(" + dup_worker_count + ")";
                    else
                        str_worker = in_worker_count + "";
                }
            }
            // �۾��� �ڵ带 ���� ���ϰų� �ؼ� ���� �� ���� ���� �ִ�. �̷��� �Ǹ� ������ �۾��� ���� �ִ� ���� �о� ��������.
            if (dup_worker_count + in_worker_count == 0)
            {
                if (opInfo.opLine.getItemRevision().isValidPropertyName(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT))
                {
                    String str_worker_count = opInfo.opLine.getItemRevision().getProperty(SDVPropertyConstant.PAINT_OPERATION_REV_WORKER_COUNT);
                    if (str_worker_count == null || str_worker_count.trim().length() == 0)
                        in_worker_count = 0;
                    else
                        in_worker_count = Integer.valueOf(str_worker_count);
                    str_worker = in_worker_count + "";
                }
            }

            if (str_worker == null || str_worker.equals("0"))
            	continue;

            // ���� �ڵ�
            propertyMap.put(SDVPropertyConstant.BL_ITEM_ID, station_code);
            // ���� ��
            propertyMap.put(SDVPropertyConstant.BL_OBJECT_NAME, opInfo.opLine.getProperty(SDVPropertyConstant.BL_OBJECT_NAME));
            // �۾��� ��
            propertyMap.put(registry.getString("BODYOperationWorkerCount.ATTR.NAME", "WorkerCount"), str_worker);
            // ���(Shop�� JPH�� ������ JPH���� �ٸ� ���� ���)
            double line_JPH = 0.0;
            String str_line_jph = opInfo.opLine.parent().parent().getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_JPH);
            if (str_line_jph == null || str_line_jph.trim().length() == 0)
                line_JPH = 0.0;
            else
                line_JPH = Double.parseDouble(str_line_jph);
            propertyMap.put(registry.getString("BODYPrintETC.ATTR.NAME", "LineJPHOfETC"), line_JPH != 0 && line_JPH != shop_JPH ? line_JPH + "" : "");
            // �۾��� ���̽ð�
            double workTime = opInfo.maxUserWorkTime / (opInfo.workerList != null ? (double) opInfo.workerList.size() : 0);
            propertyMap.put(registry.getString("BODYUserWorkTime.ATTR.NAME", "UserWorkTime"), Double.isNaN(workTime) ? 0 : workTime);
            // �Ұ��� ���ð�
            double waitTime = opInfo.maxUserWaitTime / (opInfo.workerList != null ? (double) opInfo.workerList.size() : 0);
            propertyMap.put(registry.getString("BODYUserWaitTime.ATTR.NAME", "UserWaitTime"), Double.isNaN(waitTime) ? 0 : waitTime);
            // ���� JPH
            propertyMap.put(SDVPropertyConstant.LINE_REV_JPH, line_JPH == 0 ? shop_JPH : line_JPH);

            dataList.add(propertyMap);
        }

    }

    /**
     * �ʱ� ����Ÿ �ε�
     * @method initOperation
     * @date 2013. 10. 29.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void initOperation() throws Exception {
        TCComponentBOPLine selected_target = null;
        ArrayList<TCComponentBOPLine> target_BOP_list = new ArrayList<TCComponentBOPLine>();
        registry = Registry.getRegistry(LineBalanceSheet4BodyOperation.class);

        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
        session = (TCSession) abstractaifuiapplication.getSession();
        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

        boolean flag = false;
        boolean typeFlag = false;

        parentShell = AIFDesktop.getActiveDesktop().getShell();

        for (int i = 0; i < aaifcomponentcontext.length; i++) {
            if (aaifcomponentcontext != null && aaifcomponentcontext.length > 0 && (aaifcomponentcontext[i].getComponent() instanceof TCComponentBOPLine)) {
                selected_target = (TCComponentBOPLine) aaifcomponentcontext[i].getComponent();
                target_BOP_list.add((TCComponentBOPLine) aaifcomponentcontext[i].getComponent());
                String itemType = "";
                try {
                    itemType = selected_target.getItem().getType();
                } catch (TCException e) {
                    itemType = "";
                }
                if (itemType.equals("M7_BOPShop") || itemType.equals("M7_BOPLine") || itemType.equals("M7_BOPStation"))
                    typeFlag = true;
//                MFGStructureType mfgstructuretype = MFGStructureTypeUtil.getStructureType(selected_target);
//                if (mfgstructuretype.isProcess())
//                    flag = true;
                if(MFGStructureTypeUtil.isProcess(selected_target))
                	flag = true;
            } else {
                selected_target = null;
            }

            if (!flag || !typeFlag) {
                target_BOP_list.clear();
                //MessageBox.post(parentShell, registry.getString("body.wrongSelection.MESSAGE", "Shop/Line/Station process line needs to be selected."), registry.getString("error.TITLE", "Error"), MessageBox.ERROR);
                throw new Exception(registry.getString("body.wrongSelection.MESSAGE", "Shop/Line/Station process line needs to be selected."));
            }
        }

        if (target_BOP_list.size() == 0)
            targetBOPLines = null;
        else
            targetBOPLines = target_BOP_list.toArray(new TCComponentBOPLine[target_BOP_list.size()]);

        if (targetBOPLines == null)
        {
            //MessageBox.post(parentShell, registry.getString("body.wrongSelection.MESSAGE", "Shop/Line/Station process line needs to be selected."), registry.getString("error.TITLE", "Error"), MessageBox.ERROR);
            throw new Exception(registry.getString("body.wrongSelection.MESSAGE", "Shop/Line/Station process line needs to be selected."));
        }
    }

    /**
     * ������ BOP���� ���� ��� ��������Ʈ�� �������� ���� ���ȣ�� �Լ�
     *
     * @method getStationList
     * @date 2013. 10. 15.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void getStationList(TCComponentBOPLine[] targetBOPLines, List<MaxTimeOpInfo> maxOPList) throws Exception {
        // ��������Ʈ �ʱ⼳��
        if (maxOPList == null)
            maxOPList = new ArrayList<MaxTimeOpInfo>();

        for (TCComponentBOPLine targetBOPLine : targetBOPLines)
        {
            // Shop, ����, �������� üũ �ٸ����´� üũ���� �ʴ´�.
            String item_type = targetBOPLine.getItem().getType();
            if (! (item_type.equals("M7_BOPShop") || item_type.equals("M7_BOPLine") || item_type.equals("M7_BOPStation")))
                continue;

            if (! item_type.equals("M7_BOPStation"))
            {
                // ������ �ƴϸ� Shop�̰ų� �����̱� ������ ���� �˻�
                AIFComponentContext[] child_lines = targetBOPLine.getChildren();
                ArrayList<TCComponentBOPLine> child_line_list = new ArrayList<TCComponentBOPLine>();

                for (AIFComponentContext child_line : child_lines)
                {
                    child_line_list.add((TCComponentBOPLine) child_line.getComponent());
                }

                // �������� ������ �ִ��� �������� üũ �� ����Ʈ�� ��� ���� ��� ȣ��
                if (child_line_list.size() > 0)
                    getStationList(child_line_list.toArray(new TCComponentBOPLine[child_line_list.size()]), maxOPList);
            }
            else
            {
                // ����ð� ���� ��ȸ �� ����Ÿ ����
                getMaxTimeOperationOfStation(targetBOPLine, maxOPList);
            }
        }
    }

    /**
     * �������� ������ �� �۾��� ���̽ð��� �Ұ��� ���ð��� ���� ���� ���� ������ �����Ͽ� �����ϴ� �Լ�
     *
     * @method getMaxTimeOperationOfStation
     * @date 2013. 10. 17.
     * @param
     * @return MaxTimeOpInfo
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void getMaxTimeOperationOfStation(TCComponentBOPLine tcComponentBOPLine, List<MaxTimeOpInfo> maxOPList) throws Exception {
        AIFComponentContext[] child_lines = tcComponentBOPLine.getChildren();
        TCComponentBOPLine max_time_op_line = null;
        double max_op_time = 0;
        double max_user_worktime = 0;
        double max_user_waittime = 0;
        double shop_allowance = 0.0;
        double line_allowance = 0.0;
        double op_allowance = 0.0;
        ArrayList<WorkerTimeInfo> max_worker_list = new ArrayList<WorkerTimeInfo>();

        // Shop�� �δ����� �д´�.
        if (tcComponentBOPLine.window().getTopBOMLine().getItemRevision().isValidPropertyName(SDVPropertyConstant.SHOP_REV_ALLOWANCE))
        {
            String allowance = tcComponentBOPLine.window().getTopBOMLine().getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_ALLOWANCE);
            if (allowance == null || allowance.trim().equals(""))
                shop_allowance = 0.0;
            else
                shop_allowance = Double.valueOf(allowance);
        }

        // ������ �δ����� �д´�.
        if (tcComponentBOPLine.parent().getItemRevision().isValidPropertyName(SDVPropertyConstant.LINE_REV_ALLOWANCE))
        {
            String allowance = tcComponentBOPLine.window().getTopBOMLine().getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_ALLOWANCE);
            if (allowance == null || allowance.trim().equals(""))
                line_allowance = 0.0;
            else
                line_allowance = Double.valueOf(allowance);
        }

        // ������ �δ����� ������ SHOP�� �δ����� ����Ѵ�. �Ѵ� ������ �⺻ ���� 0.108�� ����Ѵ�.(�⺻���� Properties���� �����Ѵ�.)
        if (line_allowance == 0.0)
            if (shop_allowance == 0.0)
                op_allowance = Double.valueOf(registry.getString("BODYShopDefaultAllowance", "0.108"));
            else
                op_allowance = shop_allowance;
        else
            op_allowance = line_allowance;

        // �۾��ð��� üũ�ϱ� ���� Category ���� Properties���� �д´�.
        String[] time_check_categories = registry.getStringArray("BODYBOPActivity.Time.Check.Category");
        ArrayList<String> time_check_category_list = new ArrayList<String>();
        // �۾��� ���̽ð��� ó���ϱ� ���� Category���� Properties���� �д´�. - ���� �������� ���� ó�� - 2013-10-27
//        String check_user_time_category = registry.getString("BODYBOPActivity.Time.User.Category");
        // �Ұ��� ���ð��� üũ�ϱ� ���� �۾�ó�� ���� ���� Properties���� �д´�.
        String check_user_wait_category = registry.getString("BODYBOPActivity.Time.Wait.Category", "STANDBY");

        time_check_category_list.addAll(VectorHelper.toVector(time_check_categories));

        for (AIFComponentContext childLine : child_lines) {
            TCComponentBOPLine op_line = (TCComponentBOPLine) childLine.getComponent();

            // �������� üũ
            if (op_line.getItem().getType().equals("M7_BOPBodyOp"))
            {
                // ���� �۾��� ���� �� �Ұ��� ��� �� ��
                double work_total_time = 0;
                // ���� �۾��� ���� �� ��
                double work_user_work_time = 0;
                // ���� �Ұ��� ��� �� ��
                double work_user_wait_time = 0;
                // ������ �۾��ں� ���� �� �Ұ��� ����
                HashMap<String, WorkerTimeInfo> worker_list = new HashMap<String, WorkerTimeInfo>();

                // ���� ������
                TCComponentItemRevision op_rev = op_line.getItemRevision();
                // ���� ���� ��Ƽ��Ƽ ��Ʈ
                TCComponentMEActivity root_activity = (TCComponentMEActivity) op_rev.getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
                // ��Ƽ��Ƽ ���� ��ü ��Ƽ��Ƽ ����Ʈ
                TCComponentMEActivity[] child_activities = root_activity.listAllActivities();
                for (TCComponentMEActivity child_activity : child_activities) {
                    double work_time = 0;
                    String work_category = "";
                    String work_sub_category = "";
                    String []worker_code = null;

                    // ��Ƽ��Ƽ Ÿ��(�۾��� ����, �ڵ�, ����) üũ
                    if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY))
                        work_category = child_activity.getStringProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY);

                    if (time_check_category_list.contains(work_category)) {
                        // ��Ƽ��Ƽ ���� Ÿ��(�Ұ��� ���, �ߺ�)
                        if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_SUB_CATEGORY))
                            work_sub_category = child_activity.getProperty(SDVPropertyConstant.ACTIVITY_SUB_CATEGORY);

                        // ��Ƽ��Ƽ �ð�
                        if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME))
                            work_time = child_activity.getDoubleProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME);

                        // �۾��� �ڵ�
                        if (child_activity.isValidPropertyName(SDVPropertyConstant.ACTIVITY_WORKER))
                        {
                            TCProperty worker_property = child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORKER);

                            if (worker_property.isNotArray())
                                worker_code = new String[]{worker_property.getStringValue()};
                            else
                                worker_code = worker_property.getStringArrayValue();
                        }

                        // �۾��ں� �ð��� ����ϱ� ������ �۾��� �ڵ尡 ������ �ð��� üũ���� �ʴ´�.
                        // ���� �۾��ڰ� ������ ��ü �۾��ڿ��� �ð��� ���ؾ� �Ѵٸ� ��ü �۾��� ����Ʈ�� ���(����)���� ���� ���ǰ� �Ǿ�� �Ѵ�.
                        // ù Activity�� �۾��� �ڵ尡 ������ ��� ��ü �۾��ڿ��� �ð��� ���� �� ���ΰ� ����ؾ� ��.
                        if (worker_code != null)
                        {
                            for (String worker : worker_code)
                            {
                                if (worker == null)
                                    continue;

                                // �۾��ں��� �۾��ð��� ���ð��� �����Ѵ�.
                                if (! worker_list.containsKey(worker))
                                {
                                    WorkerTimeInfo worker_info = new WorkerTimeInfo();
                                    worker_info.workerCode = worker;
                                    worker_info.workTime = work_sub_category.equals(check_user_wait_category) ? 0.0 : work_time;
                                    worker_info.waitTime = work_sub_category.equals(check_user_wait_category) ? work_time : 0.0;

                                    worker_list.put(worker, worker_info);
                                }
                                else
                                {
                                    worker_list.get(worker).workTime += work_sub_category.equals(check_user_wait_category) ? 0.0 : work_time;
                                    worker_list.get(worker).waitTime += work_sub_category.equals(check_user_wait_category) ? work_time : 0.0;
                                }
                            }
                        }
                    }
                }

                // �۾��ں� �Ұ��� ��� �ð��� �δ����� üũ�Ͽ� �������Ѵ�. �Ұ��� ���ð��� �۾��ð��� 10.8%���� ������ �۾��ð��� 10.8%�� ���ð����� ����
                for (String op_worker : worker_list.keySet())
                {
                    if (worker_list.get(op_worker).waitTime == 0 || worker_list.get(op_worker).workTime * op_allowance > worker_list.get(op_worker).waitTime)
                    {
                        worker_list.get(op_worker).waitTime = worker_list.get(op_worker).workTime * op_allowance;
                    }

                    // ������ ��ü �۾��ð� �� �Ұ��� ���ð��� ����Ѵ�. �۾��ں��� ����Ѵٸ� �ʿ���� ���
                    work_total_time += worker_list.get(op_worker).workTime + worker_list.get(op_worker).waitTime;
                    work_user_work_time += worker_list.get(op_worker).workTime;
                    work_user_wait_time += worker_list.get(op_worker).waitTime;
                }

                // �������� ������ �� �۾��ð��� ���� �� ������ �����Ѵ�.
                if (max_op_time == 0 || max_op_time < work_total_time) {
                    // ���� �۾��ð� ����
                    max_time_op_line = op_line;
                    // �۾� �ð�
                    max_op_time = work_total_time;
                    // �۾��� ���̽ð�
                    max_user_worktime = work_user_work_time;
                    // �Ұ��� ���ð�
                    max_user_waittime = work_user_wait_time;

                    // �۾��� ����Ʈ ����
                    if (max_worker_list.size() > 0)
                        max_worker_list.clear();

                    // �۾��� �ڵ� ��ü �߰�
                    max_worker_list.addAll(worker_list.values());
                }

                worker_list.clear();
            }
        }

        // ����ð� ������ ����ҿ� ����.
        if (max_time_op_line != null) {
            MaxTimeOpInfo max_op_info = new MaxTimeOpInfo();

            max_op_info.opLine = max_time_op_line;
            max_op_info.maxUserWorkTime = max_user_worktime;
            max_op_info.maxUserWaitTime = max_user_waittime;
            if (max_worker_list != null && max_worker_list.size() > 0)
            {
                max_op_info.workerList = new ArrayList<WorkerTimeInfo>();
                max_op_info.workerList.addAll(max_worker_list);

                max_worker_list.clear();
            }
            if (time_check_category_list.size() > 0)
                time_check_category_list.clear();

            maxOPList.add(max_op_info);
        } else {
            max_time_op_line = null;
            max_user_worktime = 0;
            max_user_waittime = 0;
            if (max_worker_list != null)
                max_worker_list.clear();
            if (time_check_category_list.size() > 0)
                time_check_category_list.clear();
        }
    }

}
