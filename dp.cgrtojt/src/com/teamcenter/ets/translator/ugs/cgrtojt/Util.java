package com.teamcenter.ets.translator.ugs.cgrtojt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Util {
    // ���� �α׸� ����.
	
	void setLog(String s) throws IOException {
  
		File zLogFile = new File( "c:\\temp\\log.txt" );			
		zLogFile.createNewFile();	
	      
		BufferedWriter out = new BufferedWriter(new FileWriter(zLogFile));                                    
		out.write(s);	      
		out.newLine();	      
		out.close();		
	} 
}
