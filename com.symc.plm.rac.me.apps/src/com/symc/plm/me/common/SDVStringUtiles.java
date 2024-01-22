package com.symc.plm.me.common;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SDVStringUtiles {

    public static String dateToString(Date date, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }
    
    /**
     * String ���ڿ��� Double ���� ������
     * Double ������ ��ȯ���� �ʴ� ���� 0���� return ��
     * @method getDoubleFromString 
     * @date 2014. 1. 8.
     * @param
     * @return double
     * @exception
     * @throws
     * @see
     */
    public static double getDoubleFromString(String inputString)
    {
        double doubleValue = 0;
        StringBuilder sb = new StringBuilder();
        String regEx = "(?!=\\d\\.\\d\\.)([\\d.]+)";
        Pattern pattern = Pattern.compile(regEx);

        Matcher match = pattern.matcher(inputString);
        while (match.find()) {
            sb.append(match.group());
        }        
        try {
            doubleValue = Double.parseDouble(sb.toString());
        } catch (NumberFormatException ex) {
            return 0; 
        }
        return doubleValue; 
    }
    
	/**
	 * �־��� �ѱ��� ���Ե� ���ڿ��� byte �������� �ٲ����� byte���� ���̰� �־��� targetByteSize �̳��� ũ����
	 * ���ڿ��� �߶� Return �Ѵ�.
	 * 
	 * @param inputStr �ѱ��� ���Ե� ���� ���ڿ�
	 * @param cheracterSetName ���ڿ� Byte ó���Ҷ� ����� CharacterSet Name (EUC-KR, UTF-8, UTF-16, MS949 ...)
	 * @param targetByteSize ���ڿ��� byte ���·� ��ȯ�� byte Size
	 * @return �־��� byte size �Ǵ� �׺��� ���� ũ���� ���ڿ�
	 */
	public static String getByteSizedStr(String inputStr, String cheracterSetName, int targetByteSize){
		String returnStr = null;
	
		for (int lastIndex = inputStr.length(); inputStr!=null && lastIndex >= 0; lastIndex--) {
			String tempStr = inputStr.substring(0, lastIndex);
			if(tempStr!=null && tempStr.trim().length()>0){
				int currentLength = 0;
				try {
					currentLength = tempStr.getBytes(cheracterSetName).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				if(currentLength<=targetByteSize){
					returnStr = tempStr;
					break;
				}
			}
		}
		
		return returnStr;
	}

}
