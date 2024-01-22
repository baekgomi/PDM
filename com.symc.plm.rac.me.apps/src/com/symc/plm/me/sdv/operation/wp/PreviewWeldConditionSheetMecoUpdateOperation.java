package com.symc.plm.me.sdv.operation.wp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.symc.plm.me.sdv.excel.transformer.PreviewWeldConditionSheetExcelTransformer;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetDataHelper;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.symc.plm.me.utils.TcDefinition;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;


public class PreviewWeldConditionSheetMecoUpdateOperation {


    /**
     * MECO �Ϸ� ������ ��ġ�� ���� ���������� Release �Ǳ����� MECO ������ Update �Ѵ�
     * 1. MECO ItemRevision �� �����´�
     * 2. MECO ItemRevision ���� Solution Folder ���� WeldOperation �� �����´�
     * 3. WeldOperation �� ����Ǿ� �ִ� ���� MECO ������ �����´�
     * 4. WeldOperation �� �ִ� ��������ǥ �����ͼ� File ���������� Export �� �� Workbook �� �����Ͽ� ����
     * 5. ������ ��������ǥ �ٽ� Import �� ���� ������ �����Ѵ�
     *
     * @method setLastMecoInfo
     * @date 2013. 12. 16.
     * @param
     * @return boolean
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public static void setLastMecoInfo(String mecoID) throws Exception
    {
        TCSession session = CustomUtil.getTCSession();

        // �Ѿ�� MECO ID �� MECO ItemRevision �� �����´�
        TCComponentItem mecoItem = SYMTcUtil.findItem(session, mecoID);
        TCComponentItemRevision mecoItemRevision = mecoItem.getLatestItemRevision();
        

        // MECO ItemRevision ����
        List<TCComponentItemRevision> weldOPList = PreviewWeldConditionSheetDataHelper.getMecoTargetList(mecoItemRevision, SDVTypeConstant.MECO_SOLUTION_ITEM, SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV);

        // MECO List �� �����ͼ� MECO �������� �����Ѵ�
        for (TCComponentItemRevision weldOP : weldOPList)
        {
            // MECO �����͸� �����Ѵ�
            List<HashMap<String, Object>> mecoDataList = new ArrayList<HashMap<String, Object>>();
            List<String> mecoList = new ArrayList<String>();
            mecoList.add(mecoID);
            
            String weldConditionSheetType = ProcessSheetDataHelper.getWeldConditionSheetType(weldOP);
            
            mecoList = PreviewWeldConditionSheetDataHelper.getMecoList(weldOP, mecoList);
            mecoDataList = PreviewWeldConditionSheetDataHelper.getMecoInfoList(mecoList);
            // WeldOPRevision ���� Dataset �� ��������ǥ �����ͼ� ���� File �� �����´�
            File file = getFile(weldOP, session);
            // ������ file ���� Workbook�� �����Ͽ� ���� Sheet �� �Էµ� MECO �����Ѵ�
            //Workbook workbook = PreviewWeldConditionSheetExcelHelper.getWorkbook(file);
            Workbook workbook = PreviewWeldConditionSheetExcelTransformer.initWorkBook(file, weldConditionSheetType);
            workbook = deleteMecoInfo(workbook);
            // ������ Workbook �� Meco ������ Update �Ѵ�
            workbook = setMecoData(workbook, mecoDataList);
            // MECO ���� �������� �ý��� Sheet �� �߰��ϰ� ���������� �Է� ��Ų��
            file = setPageNO(file, workbook);

            // ������ ���� file�� Teamcenter Dataset �� Import �� �� �����Ѵ�
            importDatasetFile(weldOP, file);
            PreviewWeldConditionSheetExcelHelper.deleteLocalFile(file);
        }
    }

    /**
     * ������������ File �� �����´�
     *
     * @method getFile
     * @date 2013. 12. 17.
     * @param
     * @return File
     * @exception
     * @throws
     * @see
     */
    private static File getFile(TCComponentItemRevision weldOpRevision, TCSession session) throws Exception
    {
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        datasets = CustomUtil.getDatasets(weldOpRevision, TcDefinition.TC_SPECIFICATION_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        File[] localfile = null;
        localfile = CustomUtil.exportDataset(datasets.get(0), session.toString());
        return localfile[0];
    }

    /**
     * ���� ��ϵǾ� �ִ� MECOSheet ������ 1�� sheet �� ����� MECO ������ �����Ѵ�
     *
     * @method deleteMecoInfo
     * @date 2013. 12. 17.
     * @param
     * @return Workbook
     * @exception
     * @throws
     * @see
     */
    private static Workbook deleteMecoInfo(Workbook workbook)
    {
        int sheetTotalCount = workbook.getNumberOfSheets();
        List<String> sheetGroup = new ArrayList<String>();

        // sheet �̸��� �����Ѵ�
        for (int i = 0; i < sheetTotalCount; i++)
        {
            sheetGroup.add(workbook.getSheetName(i));
        }
        int sheetNO;
        // ������ sheet �̸��� ������ ���Ͽ� �����Ѵ�
        String mecoSheet = "MECOList";

        for (String sheet : sheetGroup)
        {
            if (sheet.equals(mecoSheet))
            {
                sheetNO = workbook.getSheetIndex(sheet);
                workbook.removeSheetAt(sheetNO);
            }
        }

        //BaseSheet �� �ִ� Meco ������ Clear �Ѵ�
        String baseSheet = PreviewWeldConditionSheetOpenOperation.DEFAULT_BASE_SHEET;
        int mecoListSize = PreviewWeldConditionSheetOpenOperation.DEFAULT_MECO_LIST_SIZE;
        int mecoStartIndex = PreviewWeldConditionSheetOpenOperation.DEFAULT_MECO_START_INDEX;
        int mecoCell[] = PreviewWeldConditionSheetOpenOperation.DEFAULT_MECO_CELL;

        int originalNO = workbook.getSheetIndex(baseSheet);
        Sheet currentSheet = workbook.getSheetAt(originalNO);

        // ���� ��ϵǾ� �ִ� MECO ������ Clear �Ѵ�
        for(int i = 0; i < mecoListSize; i++)
        {
            PreviewWeldConditionSheetOpenOperation.clearRow(currentSheet, mecoStartIndex + i, mecoCell);
        }
        return workbook;
    }

    /**
     * MECO ������ �Է��ϰ� 10���� �Ѿ������ Sheet �� �߰��Ͽ� ��� ����Ѵ�
     *
     * @method setMecoData
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @throws IOException
     * @exception
     * @throws
     * @see
     */
    private static Workbook setMecoData(Workbook workbook, List<HashMap<String, Object>> mecoDataList)
    {
        int sheetTotalCount = workbook.getNumberOfSheets();

        // MECO �� 10���� �Ѿ��� MECOList Sheet �߰� �ϰ� List �� ����Ѵ�
        if (mecoDataList.size() > 10)
        {
            int mecoSheetIndex = workbook.getSheetIndex("MECO_List");
            Sheet mecoSheet = workbook.cloneSheet(mecoSheetIndex);
            sheetTotalCount = workbook.getNumberOfSheets();
            workbook.setSheetName((sheetTotalCount - 1), "MECOList");
            for (int mecoListRow = 0; mecoListRow < mecoDataList.size(); mecoListRow++)
            {
                PreviewWeldConditionSheetExcelTransformer.mecoListPrintRow(mecoSheet, mecoListRow, mecoDataList.get(mecoListRow));
            }
            PreviewWeldConditionSheetExcelTransformer.setBorderAndAlign(workbook, mecoSheet);
        }

        // MECO List �� ����Ѵ�
        int systemSheet = workbook.getSheetIndex("1");
        for (int mecoRow = 0; mecoRow < mecoDataList.size(); mecoRow++)
        {
            PreviewWeldConditionSheetExcelTransformer.mecoPrintRow(workbook.getSheetAt(systemSheet), mecoRow, mecoDataList.get(mecoRow));
            if (mecoRow == 9)
                break;
        }

        return workbook;
    }

    private static File setPageNO(File file, Workbook workbook) throws IOException
    {
        int totalSheet = workbook.getNumberOfSheets();
        int pageNO = 1;
        for (int i = 0; i < totalSheet; i++)
        {
            Sheet currentSheet = workbook.getSheetAt(i);
            if (!currentSheet.getSheetName().equals("MECO_List") && !currentSheet.getSheetName().equals("copySheet"))
            {
                PreviewWeldConditionSheetExcelTransformer.setPageNumber(currentSheet, (totalSheet - 2), pageNO);
                pageNO++;
            }
        }

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }

    /**
     * Ÿ�� WeldOP �� ��������ǥ Dataset �� ���� ������ �����ϰ� ������ ������ Import �Ѵ�
     *
     * @method importDatasetFile
     * @date 2013. 12. 17.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private static void importDatasetFile(TCComponentItemRevision itemRevision, File file) throws Exception
    {
        Vector<TCComponentDataset> vData = new Vector<TCComponentDataset>();
        Vector<File> vFile = new Vector<File>();

        vData = CustomUtil.getDatasets(itemRevision, TcDefinition.TC_SPECIFICATION_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        vFile.add(file);
        CustomUtil.removeAllNamedReference(vData.get(0));
        SYMTcUtil.importFiles(vData.get(0), vFile);
    }

}
