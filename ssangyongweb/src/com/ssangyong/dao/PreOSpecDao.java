package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.PreOSpecMapper;

/**
 * [20160928][ymjang] log4j�� ���� ���� �α� ���
 */
public class PreOSpecDao extends AbstractDao {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insertTrim(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);
            
            String ospecID = (String)ds.get("OSPEC_NO");
            ArrayList<HashMap> trims = (ArrayList<HashMap>)ds.get("DATA");
            
            session.delete("com.ssangyong.mapper.PreOSpecMapper.deleteTrim", ds);
            for( HashMap map : trims){
            	map.put("OSPEC_NO", ospecID);
            	session.insert("com.ssangyong.mapper.PreOSpecMapper.insertTrim", map);
            }
            session.commit();
        } catch (Exception e) {
        	session.rollback();
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }		
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatory(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			resultList = mapper.selectPreOSpecMandatory(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatoryInfo(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			resultList = mapper.selectPreOSpecMandatoryInfo(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public ArrayList<HashMap<String, String>> selectPreOSpecMandatoryTrim(DataSet ds) throws Exception {
		ArrayList<HashMap<String, String>> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			resultList = mapper.selectPreOSpecMandatoryTrim(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}
	
	public void insertPreOSpecMandatoryInfo(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			mapper.insertPreOSpecMandatoryInfo(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
	}
	
	public void insertPreOSpecMandatoryTrim(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			mapper.insertPreOSpecMandatoryTrim(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
	}
	
	public void deletePreOSpecMandatoryInfo(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			mapper.deletePreOSpecMandatoryInfo(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
	}
	
	public void deletePreOSpecMandatoryTrim(DataSet ds) throws Exception {
		try{
			SqlSession sqlSession = getSqlSession();
			PreOSpecMapper mapper = sqlSession.getMapper(PreOSpecMapper.class);	
			mapper.deletePreOSpecMandatoryTrim(ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
	}
}
