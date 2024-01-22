package com.symc.plm.me.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.util.Registry;

/**
 * 
 * ���� ���� ��ƿ
 * 
 */
public class BundleUtil {

    /**
     * ��Ӱ��迡 ���� TextBundle Getter ���� Class���� Override�� Text Property �ΰ�� ���� Text
     * Property�� ������
     * 
     * ClassName+"."+MiddleName+"."+BundleName ex) BundleWorkDialog.MSG.ERROR
     * 
     * @param registry
     *            : TC Registry
     * @param strBundleName
     *            : Text Property ������ Name
     * @param strMiddleName
     *            : Text Property �߰��� ���ԵǴ� Name
     * @param dlgClass
     *            : ���� ����� ȣ���� Java Class Instance
     * @return Text Property
     */
    public static String getTextBundle(Registry registry, String strBundleName, String strMiddleName, Class<?> dlgClass) {
        // Middle Name ����
        String middleName = ".";
        if (strMiddleName != null && !strMiddleName.equals("")) {
            middleName = "." + strMiddleName + ".";
        }
        // Full Bundle Name
        String strFullBundleName = dlgClass.getSimpleName() + middleName + strBundleName;
        String strBundleText = registry.getString(strFullBundleName);
        // ���� Class Instance�� ���� �Ǿ� ���� �ʴٸ� ���� Class���� ã��
        if (strBundleText == null || strBundleText.equals("") || strFullBundleName.equals(strBundleText)) {           
            // BundleWorkDialog(�ֻ���) Class ������ Bundle�� ã�� �� ���ٸ� ������ Return
            if (dlgClass.getClass().getSimpleName().equals(dlgClass.getSimpleName())) {
                return "";
            } else {
                // ���� Class���� ã��(���ȣ��)
                Class<?> supDlgClass = dlgClass.getSuperclass();
                strBundleText = getTextBundle(registry, strBundleName, strMiddleName, supDlgClass);
            }
        }
        return strBundleText;
    }

    /**
     * ��Ӱ��迡 ���� TextBundle Array Getter ���� Class���� Override�� Text Property �ΰ��
     * ���� Text Property�� ������
     * 
     * ClassName+"."+MiddleName+"."+BundleName ex)
     * BundleWorkDialog.MSG.ClassArray
     * 
     * @param registry
     *            : TC Registry
     * @param strBundleName
     *            : Text Property ������ Name
     * @param strMiddleName
     *            : Text Property �߰��� ���ԵǴ� Name
     * @param dlgClass
     *            : ���� ����� ȣ���� Java Class Instance
     * @return Text Property Array
     */
    public static String[] getTextBundleArray(Registry registry, String strBundleName, String strMiddleName, Class<?> dlgClass) {
        String middleName = ".";
        if (strMiddleName != null && !strMiddleName.equals("")) {
            middleName = "." + strMiddleName + ".";
        }
        String[] szBundleText = null;
        String strFullBundleName = dlgClass.getSimpleName() + middleName + strBundleName;
        String strBundleText = registry.getString(strFullBundleName);
        if (strBundleText == null || strBundleText.equals("") || strFullBundleName.equals(strBundleText)) {
            if (dlgClass.getClass().getSimpleName().equals(dlgClass.getSimpleName())) {
                return null;
            } else {
                Class<?> supDlgClass = dlgClass.getSuperclass();
                szBundleText = getTextBundleArray(registry, strBundleName, strMiddleName, supDlgClass);
            }
        } else {
            szBundleText = BundleUtil.getSplitString(strBundleText, ",");
        }
        return szBundleText;
    }

    /**
     * splitter�� �����Ǵ� ���ڿ��� �ɰ��� Vector�� return�Ѵ�. ���ڿ��� ���ڿ��� ���־���.
     * 
     * @method getSplitString
     * @date 2013. 2. 6.
     * @param
     * @return
     * @exception
     * @throws
     * @see
     */
    public static String[] getSplitString(String strValue, String splitter) {
        if (strValue == null || strValue.length() == 0) {
            return null;
        }
        if (splitter == null) {
            return null;
        }
        StringTokenizer split = new StringTokenizer(strValue, splitter);
        ArrayList<String> strList = new ArrayList<String>();
        while (split.hasMoreTokens()) {
            strList.add(new String(split.nextToken().trim()));
        }
        String[] szValue = new String[strList.size()];
        for (int i = 0; i < strList.size(); i++) {
            szValue[i] = strList.get(i);
        }
        return szValue;
    }
    
    /**
     * Excel Cell Value Return
     * 
     * CELL_TYPE_NUMERIC�� ��� Integer�� Casting�Ͽ� ��ȯ��
     * Long ������ ���� ���Ұ�� �ٸ��� �����ؾ� ��.
     * 
     * @param cell
     * @return
     */
    public static String getCellText(Cell cell)
    {
        String value = "";
        if (cell != null)
        {
            
            switch (cell.getCellType())
            {
                case XSSFCell.CELL_TYPE_FORMULA:
                    value = cell.getCellFormula();
                    break;
                
                // Integer�� Casting�Ͽ� ��ȯ��
                case XSSFCell.CELL_TYPE_NUMERIC:
                    value = "" + (int) cell.getNumericCellValue();
                    break;
                
                case XSSFCell.CELL_TYPE_STRING:
                    value = "" + cell.getStringCellValue();
                    break;
                
                case XSSFCell.CELL_TYPE_BLANK:
                    // value = "" + cell.getBooleanCellValue();
                    value = "";
                    break;
                
                case XSSFCell.CELL_TYPE_ERROR:
                    value = "" + cell.getErrorCellValue();
                    break;
                default:
            }
            
        }
        
        return value;
    }
    
    /**
     * String ���ڰ� null �̸� "" ����
     * String ���ڰ� ������ trim ó�� �� ����
     * 
     * @method nullToString    
     * @param
     * @return
     * @exception
     * @throws
     * @see
     */
    public static String nullToString(String str) {
        if(str == null) {
            return "";
        } else {
            return str.trim();
        }
    }
    
    public static String nullObjToString(Object obj) {
    	if(obj == null) {
    		return "";
    	} else {
    		return String.valueOf(obj).trim();
    	}
    }
    
    /**
     * ���ڿ��� Ư�����ڸ� ġȯ�Ѵ�
     * 
     * @param str
     *            ����ڿ�
     * @param src
     *            ġȯ���� ����
     * @param tgt
     *            ġȯ�� ����
     * @return �ϼ��� ���ڿ�
     */
    public static String replace(String str, String src, String tgt) {
        StringBuffer buf = new StringBuffer();
        String ch = null;

        if (str == null || str.length() == 0)
            return "";

        int i = 0;
        int len = src.length();
        while (i < str.length() - len + 1) {

            ch = str.substring(i, i + len);
            if (ch.equals(src)) {
                buf.append(tgt);
                i = i + len;
            } else {
                buf.append(str.substring(i, i + 1));
                i++;
            }
        }

        if (i < str.length())
            buf.append(str.substring(i, str.length()));

        return buf.toString();
    }
    
    /**
     * properties���� ��ũ�� ���ڿ��� �����´�. EX) test={0}��° ���ڿ� StringUtil.getString(registry, "test", new String[]{"1"}) ==> 1��° ���ڿ�
     * 
     * @param registry
     * @param propertyName
     * @param param
     * @return
     */
    public static String getString(Registry registry, String propertyName, String[] param)
    {
      String value = registry.getString(propertyName);
      for (int i = 0; i < param.length; i++)
      {
        value = value.replaceAll("\\{" + i + "\\}", param[i]);
      }

      return value;
    }
    
    public static Text getCalendarTextField( final Text text) {
    

        text.setText("YYYY-MM-DD");
        final Calendar calendar = Calendar.getInstance();
        text.addListener(SWT.Verify, new Listener() {
          boolean ignore;

          public void handleEvent(Event e) {
            if (ignore)
              return;
            e.doit = false;
            StringBuffer buffer = new StringBuffer(e.text);
            char[] chars = new char[buffer.length()];
            buffer.getChars(0, chars.length, chars, 0);
            if (e.character == '\b') {
              for (int i = e.start; i < e.end; i++) {
                switch (i) {
                case 0: /* [Y]YYY */
                case 1: /* Y[Y]YY */
                case 2: /* YY[Y]Y */
                case 3: /* YYY[Y] */{
                  buffer.append('Y');
                  break;
                }
                case 5: /* [M]M */
                case 6: /* M[M] */{
                  buffer.append('M');
                  break;
                }
                case 8: /* [D]D */
                case 9: /* D[D] */{
                  buffer.append('D');
                  break;
                }
                case 4: /* YYYY[/]MM */
                case 7: /* MM[/]DD */{
                  buffer.append('-');
                  break;
                }
                default:
                  return;
                }
              }
              text.setSelection(e.start, e.start + buffer.length());
              ignore = true;
              text.insert(buffer.toString());
              ignore = false;
              text.setSelection(e.start, e.start);
              return;
            }

            int start = e.start;
            if (start > 9)
              return;
            int index = 0;
            for (int i = 0; i < chars.length; i++) {
              if (start + index == 4 || start + index == 7) {
                if (chars[i] == '-') {
                  index++;
                  continue;
                }
                buffer.insert(index++, '-');
              }
              if (chars[i] < '0' || '9' < chars[i])
                return;
              if (start + index == 5 && '1' < chars[i])
                return; /* [M]M */
              if (start + index == 8 && '3' < chars[i])
                return; /* [D]D */
              index++;
            }
            String newText = buffer.toString();
            int length = newText.length();
            StringBuffer date = new StringBuffer(text.getText());
            date.replace(e.start, e.start + length, newText);
            calendar.set(Calendar.YEAR, 1901);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DATE, 1);
            String yyyy = date.substring(0, 4);
            if (yyyy.indexOf('Y') == -1) {
              int year = Integer.parseInt(yyyy);
              calendar.set(Calendar.YEAR, year);
            }
            String mm = date.substring(5, 7);
            if (mm.indexOf('M') == -1) {
              int month = Integer.parseInt(mm) - 1;
              int maxMonth = calendar.getActualMaximum(Calendar.MONTH);
              if (0 > month || month > maxMonth)
                return;
              calendar.set(Calendar.MONTH, month);
            }
            String dd = date.substring(8, 10);
            if (dd.indexOf('D') == -1) {
              int day = Integer.parseInt(dd);
              int maxDay = calendar.getActualMaximum(Calendar.DATE);
              if (1 > day || day > maxDay)
                return;
              calendar.set(Calendar.DATE, day);
            } else {
              if (calendar.get(Calendar.MONTH) == Calendar.FEBRUARY) {
                char firstChar = date.charAt(8);
                if (firstChar != 'D' && '2' < firstChar)
                  return;
              }
            }
            text.setSelection(e.start, e.start + length);
            ignore = true;
            text.insert(newText);
            ignore = false;
          }
        });

    	return text;
    }
}
