package com.symc.plm.me.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;

/**
 * Log ó���� ����  Custom Class
 * @author Taeku Jeong
 *
 */
public class LogFileUtility {
	private long started; // ���� �ð�
	private boolean fileAppend = false;
	private boolean simpleFileAppend = false;
	private File outFile;
	private File simpleOutFile;
	private String fileName;
	private String simpleFileName;
	private boolean outUseSystemOut = false;
	
	private boolean printTrace = false;
	
	/**
	 * ������
	 * @param logFileName LogFile��   ���� �̸� 
	 */
	public LogFileUtility(String logFileName) {

		// Log ������ ������ ��ġ
		String logFilepath = "C:\\TEMP";
		if(logFilepath!=null && logFilepath.trim().length()>0){
			logFilepath = logFilepath.trim();
			if(logFilepath.charAt((logFilepath.length()-1))=='\\'){
				logFilepath = logFilepath.substring(0, (logFilepath.length()-1));
			}
		}
		checkLogFilePath(logFilepath);
		
		fileAppend = false;
		simpleFileAppend = false;
		
		if(logFileName!=null){
			logFileName = logFileName.trim();
		}
		
		this.fileName = logFilepath+"\\"+logFileName;
		this.outFile = new File(this.fileName);
		this.outFile.delete();
		
		this.simpleFileName = logFilepath+"\\Simple_"+logFileName; 
		this.simpleOutFile = new File(this.simpleFileName);
		this.simpleOutFile.delete();
		
		setTimmerStarat();
	}
	
	/**
	 * �־���  ��ο� Log File�� �ִ��� Ȯ���ϰ� ������  Log File�� �����Ѵ�.
	 * @param logFilepath
	 */
	private void checkLogFilePath(String logFilepath){
		
		logFilepath = logFilepath.replace('\\', '/');
		String[] pathSplits = logFilepath.split("/");

		if(pathSplits!=null && pathSplits.length>0){
			String pathString = null;
			for (int i = 0; i < pathSplits.length; i++) {
				if(i<1){
					pathString = pathSplits[i].trim();
				}else{
					pathString = pathString + "\\"+pathSplits[i].trim();
				}
				
				if(i>0){
					File folder = new File(pathString);
					if(folder.exists()==false){
						folder.mkdir();
					}
				}
			}
		}
	}
	
	/**
	 * ���� �ð���  Time Stamp�� �ش��ϴ� ���ڿ��� �����ؼ� ���� �ش�.
	 * @return Time Stamp ����
	 */
	static public String getCurrentDateStamp(){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
		String time = df.format(new Date());
		return time;
	}
	
	/**
	 * Exception ������ Log ���Ͽ� ����Ѵ�.
	 * @param exception
	 */
	public void writeReport(String message) {
		
		Exception e1 = new Exception(message);
		StackTraceElement[] stackTraces = e1.getStackTrace();
		StackTraceElement messageStack = null;
		StackTraceElement messageParentStack = null;
		if(stackTraces!=null&&stackTraces.length>=2){
			messageStack = stackTraces[1];
		}
		if(stackTraces!=null&&stackTraces.length>=3){
			messageParentStack = stackTraces[2];
		}

		if(message==null || (message!=null && message.trim().length()<1)){
			message = "";
		}

		String outPutMessage = null;
		if(this.printTrace){
			if(messageStack!=null){
				outPutMessage = "["+getStackMessage(messageParentStack)+" -> "+getStackMessage(messageStack)+"]\n"+message;
			}else{
				outPutMessage = message;
			}
		}else{
			outPutMessage = message;
		}
		
		
		if(outFile!=null){
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = df.format(new Date());
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, fileAppend));
				String contentsString = "[" + time + "(" + getElapsedTime() + ")] "+ outPutMessage + "\n";
				Logger.getLogger(this.getClass()).debug(contentsString);
				if(outUseSystemOut==true){
					System.out.println(contentsString);
				}
				writer.write(contentsString);
				writer.flush();
				writer.close();
				if(!fileAppend){
					fileAppend = true;
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	/**
	 * Exception ������ Log ���Ͽ� ����Ѵ�.
	 * @param exception
	 */
	public void writeExceptionTrace(Exception exception){
		StringBuffer note = new StringBuffer();
		note.append("Error message : ");
		note.append(exception.getMessage());
		note.append("\n");
		note.append("Error stack : ");
		note.append("\n");

		StackTraceElement[] elements = exception.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			note.append(elements[i].toString());
			note.append("\n");
		}
		writeReport(note.toString());
	}
	
	/**
	 * Log ���Ͽ� ���� �� ���� ǥ���Ѵ�.
	 * @param spaceCount
	 */
	public void writeBlankeRowReport(int spaceCount) {
		if(outFile!=null){
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, fileAppend));
				if(spaceCount>0){
					for (int i = 0; i < spaceCount; i++) {
						//Logger.getLogger(this.getClass()).debug("\n");
						writer.write("\n");
					}
				}
				writer.flush();
				writer.close();
				if(!fileAppend){
					fileAppend = true;
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Function�� ȣ��Ǵ� ������ �ð��� ���� �ð����� �����Ѵ�.
	 *
	 */
	public void setTimmerStarat(){
		started = Calendar.getInstance().getTimeInMillis();	
	}

	/**
	 * started �ð����� ���� �ҿ�� �ð��� ���ڿ��� ���� �ش�.
	 * @return �ҿ�ð� ���ڿ�
	 */
	public String getElapsedTime() {
		String elapseStrig = null;

		float elapsedTime = ((System.currentTimeMillis() - started) / 1000);
		float elapsedMinut = elapsedTime / 60;
		float elapsedSec = elapsedTime % 60;
		int sec = 0;
		int minut = 0;
		int houre = 0;
		Format formatter = new DecimalFormat("##");
		Format formatter2 = new DecimalFormat("######");
		sec = Integer.parseInt(formatter.format(new Double(elapsedSec)));
		minut = Integer.parseInt(formatter2.format(new Double(elapsedMinut)));
		if (elapsedMinut > 60) {
			houre = minut / 60;
			minut = minut % 60;
		}
		houre = Integer.parseInt(formatter2.format(new Double(houre)));
		minut = Integer.parseInt(formatter.format(new Double(minut)));

		elapseStrig = houre + ":" + minut + ":" + sec;

		return elapseStrig;
	}

	/**
	 * Log File��  File �̸��� Return�Ѵ�.
	 * @return Log File�� �̸� (��� ����)
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Log File�� ������ ���� �̸��� SIMPLE�� ���� ������ Log File�� ����� Log�� �����.
	 * �� Function�� ����ڿ��� ������ ��������  Debug������ ������ �ƴ϶� ���� ������ �������� Log�� ���涧 ����Ѵ�.
	 * 
	 * @param message Log���Ͽ� ��ϵ� ������ ���� ���ڿ� 
	 */
	public void writeSimpleReport(String message) {
		if(simpleOutFile!=null){
			DateFormat df = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
			String tiem = df.format(new Date());
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(simpleOutFile, simpleFileAppend));
				String contentsString = "[" + tiem + "(" + getElapsedTime() + ")] "+ message + "\n";
				Logger.getLogger(this.getClass()).debug("SIMPLE "+contentsString);
				writer.write(contentsString);
				writer.flush();
				writer.close();
				if(!simpleFileAppend){
					simpleFileAppend = true;
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * SIMPLE�̶�� �̸��� �߰��� Log File�� ���� ���� �߰��Ѵ�.
	 * @param spaceCount
	 */
	public void writeBlankeRowSimpleReport(int spaceCount) {
		if(simpleOutFile!=null){
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(simpleOutFile, simpleFileAppend));
				if(spaceCount>0){
					for (int i = 0; i < spaceCount; i++) {
						writer.write("\n");
					}
				}
				writer.flush();
				writer.close();
				if(!simpleFileAppend){
					simpleFileAppend = true;
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Simple Log ������ �̸�(��� ��)�� Return�Ѵ�.
	 * @return
	 */
	public String getSimpleFileName() {
		return simpleFileName;
	}

	/**
	 * Simple Log File�� Return�Ѵ�.
	 * @return
	 */
	public File getSimpleFile() {
		return simpleOutFile;
	}
	
	/**
	 * Static ���·� ȣ�� �Ǿ� Log ���Ͽ� Log�� ������ �ʰ� Log�� Console���� �����.
	 * @param message
	 */
	public static void consoleWrite(String message){
		Exception e1 = new Exception(message);
		StackTraceElement[] stackTraces = e1.getStackTrace();
		StackTraceElement messageStack = null;
		StackTraceElement messageParentStack = null;
		if(stackTraces!=null&&stackTraces.length>=2){
			messageStack = stackTraces[1];
		}
		if(stackTraces!=null&&stackTraces.length>=3){
			messageParentStack = stackTraces[2];
		}

		if(message==null || (message!=null && message.trim().length()<1)){
			message = "";
		}

		String outPutMessage = null;
		if(messageStack!=null){
			outPutMessage = "  ["+getCurrentDateStamp()+"\t"+getStackMessage(messageParentStack)+" -> "+getStackMessage(messageStack)+"]\n"+message;
		}else{
			outPutMessage = "  ["+getCurrentDateStamp()+"]\n"+message;
		}
		Logger.getLogger("com.teamcenter.volvo.util.LogFileUtility").debug(outPutMessage);
	}
	
	/**
	 * Message Stack���� Log�� ���� ������ Class�� ȣ�� ������ ���ڿ��� Return�Ѵ�.
	 * @param messageStack
	 * @return
	 */
	private static String getStackMessage(StackTraceElement messageStack){
		
		if(messageStack==null){
			return (String)"";
		}
		
		return ""+messageStack.getClassName()+"."+messageStack.getMethodName()+" ["+messageStack.getLineNumber()+"]";
	}

	public boolean isOutUseSystemOut() {
		return outUseSystemOut;
	}

	public void setOutUseSystemOut(boolean outUseSystemOut) {
		this.outUseSystemOut = outUseSystemOut;
	}
}
