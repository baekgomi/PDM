package com.ssangyong.mapper;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;

/**
 * [SR160621-031][20160707] taeku.jeong
 * �ְ� ������ ������ Pre-BOM �����͸� Ȱ���Ͽ� ������ ����� �� �ִ� ��� ����
 */
public interface PreBOMUssageExportMapper {

	public List<HashMap> getExportTargetProjectList(DataSet ds);
    
	public List<HashMap> geProjectUssageHeaderList(DataSet ds);
    
	public List<HashMap> geProjectMasterDataList(DataSet ds);
    
	public List<HashMap> geProjectUssageDataList(DataSet ds);
	
	public void updateCost(DataSet ds);
	
	public String getEaiDate(DataSet ds);
}
