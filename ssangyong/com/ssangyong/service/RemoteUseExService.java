package com.ssangyong.service;

import java.util.ArrayList;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.model.RemoteUseExData;

public class RemoteUseExService {

	/**
	 * Web Mybatis Remote ȣ�� ��� ����.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @throws Exception
	 * @since : 2012. 12. 13.
	 */
	@SuppressWarnings("unchecked")
    public ArrayList<RemoteUseExData> getRemoteUseExData() throws Exception {
		// �׻� Remote Util�� ��� �ϱ� ���ؼ� �ش� Key, Value�� �Ǵ� DataSet Ŭ������ ��Ƽ� ��� �Ͽ��� �Ѵ�.
		DataSet result = null;
		DataSet ds = new DataSet();

		ds.setString("TC_REVPUID", "RemoteUseEx");

//		SSANGYONGRemoteUtil remote = new SSANGYONGRemoteUtil("http://10.80.28.56:7001");
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		try {
			result = (DataSet) remote.execute("com.ssangyong.service.RemoteUseExService",
					"getRemoteUseExData", ds);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		if (result == null) {
			
		}else{
			ArrayList<RemoteUseExData> dataList = new ArrayList<RemoteUseExData>();
			if (result.getObject("result") != null) {
				dataList.addAll((List<RemoteUseExData>) result.getObject("result"));

				if (dataList != null && dataList.size() != 0) {
					return dataList;
				}
			}
		}
			
		return null;
	}
}
