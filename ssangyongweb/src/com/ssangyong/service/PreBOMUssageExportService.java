package com.ssangyong.service;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.PreBOMUssageExportDao;

/**
 * [SR160621-031][20160707] taeku.jeong
 * �ְ� ������ ������ Pre-BOM �����͸� Ȱ���Ͽ� ������ ����� �� �ִ� ��� ����
 */
public class PreBOMUssageExportService {

	private PreBOMUssageExportDao dao;
	
    public List<HashMap> getExportTargetProjectList(DataSet ds){
		dao = new PreBOMUssageExportDao();
        return dao.getExportTargetProjectList(ds);
    }

    public List<HashMap> geProjectUssageHeaderList(DataSet ds){
		dao = new PreBOMUssageExportDao();
        return dao.geProjectUssageHeaderList(ds);
    }
    
    public List<HashMap> geProjectMasterDataList(DataSet ds){
		dao = new PreBOMUssageExportDao();
        return dao.geProjectMasterDataList(ds);
    }
    
    public List<HashMap> geProjectUssageDataList(DataSet ds){
		dao = new PreBOMUssageExportDao();
        return dao.geProjectUssageDataList(ds);
    }
    
    public void updateCost(DataSet ds) throws Exception{
    	dao = new PreBOMUssageExportDao();
		dao.updateCost(ds);		
	}
    
    public String getEaiDate(DataSet ds) throws Exception{
    	dao = new PreBOMUssageExportDao();
    	return dao.getEaiDate(ds);
    }
}
