package com.ssangyong.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.EnvMapper;

/**
 * [20160928][ymjang] log4j�� ���� ���� �α� ���
 */
public class EnvDao extends AbstractDao {   
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap getTCWebEnv() {
        HashMap envMap = new HashMap();
        List<HashMap> result = null;
       
        try {
            SqlSession session = getSqlSession();
            EnvMapper mapper = session.getMapper(EnvMapper.class);
            result = mapper.getTCWebEnvList();
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage());
            
        } finally {
            sqlSessionClose();
        }

        /**
         * ����� �ٽ� Map�� �����Ѵ�.
         */
        for (int i = 0; result != null && i < result.size(); i++) {
            envMap.put(result.get(i).get("KEY"), result.get(i).get("VALUE"));
        }        
        return envMap;
    }
}
