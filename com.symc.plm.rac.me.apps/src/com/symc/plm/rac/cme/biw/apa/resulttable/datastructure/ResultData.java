// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

package com.symc.plm.rac.cme.biw.apa.resulttable.datastructure;

import com.symc.plm.rac.cme.biw.apa.APADialog;
import com.teamcenter.rac.util.Registry;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * [SR140611-032][20140611] jwlee �����ǳ� �˻� ������ 15������ 25���� ����.
 *  �����ǳ� Dialog�� ǥ�õǴ� ������ Preference�� ������ �� �ֵ��� ����.
 *  �����ǳ� Dialog�� ǥ�õǴ� ������ �˻���󿡼� Assay Item�� ���� (Part ID�� �ټ���° �ڸ��� 0�̸� Assay Item�̴� �̰͵��� �˻���󿡼� ���� ó����)
 * [NON-SR][20150703] shcho, rowContext �Ѳ����� ��ü�� �� �ֵ��� setRowContexts �Լ� �߰�
 * 
 */

public class ResultData
{

    public int getSerialNumber()
    {
        return serialNumber;
    }

    public void setSerialNumber(int i)
    {
        serialNumber = i;
    }

    @SuppressWarnings("rawtypes")
    public ResultData()
    {
        rowContext = null;
        columnValues = null;
        filterOut = false;
        columnValues = new Hashtable();
        rowContext = new Hashtable();
    }

    @SuppressWarnings("unchecked")
    public void setRowContext(String s, Object obj)
    {
        if(!rowContext.containsKey(s))
            rowContext.put(s, obj);
    }
    
    @SuppressWarnings("rawtypes")
    public void setRowContexts(Hashtable hashtable)
    {
            rowContext = hashtable;
    }

    @SuppressWarnings("rawtypes")
    public Hashtable getRowContext()
    {
        return rowContext;
    }

    public Object getRowContextObject(String s)
    {
        return rowContext.get(s);
    }

    @SuppressWarnings("unchecked")
    public void addColumn(String s, Object obj, Object obj1)
    {
        if(!columnValues.containsKey(s))
        {
            columnValues.put(s, obj);
            if(s.equals("MFGType") || s.equals("MFG_Number_Of_Sheet") )
                setRowContext(s, obj);
            else
                //jwlee Part4 ������ 15 ���� ����
            	// [SR151207-042][20151209] taeku.jeong Find No �߰�
            if((s.equals("MFGName") || s.equals("Part1") || s.equals("Part2") || s.equals("Part3") || s.equals("Part4")
                    || s.equals("Part5") || s.equals("Part6") || s.equals("Part7") || s.equals("Part8") || s.equals("Part9")
                    || s.equals("Part10") || s.equals("Part11") || s.equals("Part12") || s.equals("Part13") || s.equals("Part14")
                    || s.equals("Part15")
                    //[SR140611-032][20140611] jwlee �����ǳ� �˻� ������ 15������ 25���� ����.
                    || s.equals("Part16")
                    || s.equals("Part17")
                    || s.equals("Part18")
                    || s.equals("Part19")
                    || s.equals("Part20")
                    || s.equals("Part21")
                    || s.equals("Part22")
                    || s.equals("Part23")
                    || s.equals("Part24")
                    || s.equals("Part25")
                    || s.equals("MFGFindNo")
                    //---------------------------------------------------------
                    ) && obj1 != null)
                setRowContext(s, obj1);
        }
    }

    public void removeColumn(String s)
    {
        if(columnValues.containsKey(s))
            columnValues.remove(s);
        if(rowContext.containsKey(s))
            rowContext.remove(s);
    }

    @SuppressWarnings("rawtypes")
    public void setColumnValues(Hashtable hashtable)
    {
        columnValues = hashtable;
    }

    public String getColumnValue(String s)
    {
        return columnValues.get(s) != null ? columnValues.get(s).toString() : null;
    }

    public Object getColumnObjectValue(String s)
    {
        return columnValues.get(s) != null ? columnValues.get(s) : null;
    }

    @SuppressWarnings("rawtypes")
    public Hashtable getColumnValues()
    {
        return columnValues;
    }

    public int getColumnSize()
    {
        return columnValues.size();
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getKeys()
    {
        return columnValues.keys();
    }

    public void setfilterOut(boolean flag)
    {
        filterOut = flag;
    }

    public boolean getfilterOut()
    {
        return filterOut;
    }

    public static String columnIndexToKey(int i)
        throws Exception
    {
        switch(i)
        {
        case 0: // '\0'
            return String.valueOf("MFGName");

        case 1: // '\001'
            return String.valueOf("MFG_Number_Of_Sheet");
            
        case 2:
        	return String.valueOf("MFGFindNo");

        case 3: // '\001'
            return String.valueOf("MFGType");

        case 4: // '\002'
            return String.valueOf("Part1");

        case 5: // '\003'
            return String.valueOf("Part2");

        case 6: // '\004'
            return String.valueOf("Part3");

        case 7: // '\005'
            return String.valueOf("Part4");
        // jwlee �ҽ����� case 5 -> case 15 ����

        case 8: // '\006'
            return String.valueOf("Part5");

        case 9: // '\007'
            return String.valueOf("Part6");

        case 10: // '\008'
            return String.valueOf("Part7");

        case 11: // '\009'
            return String.valueOf("Part8");

        case 12: // '\010'
            return String.valueOf("Part9");

        case 13: // '\011'
            return String.valueOf("Part10");

        case 14: // '\012'
            return String.valueOf("Part11");

        case 15: // '\013'
            return String.valueOf("Part12");

        case 16: // '\014'
            return String.valueOf("Part13");

        case 17: // '\015'
            return String.valueOf("Part14");

        case 18: // '\016'
            return String.valueOf("Part15");

        //[SR140611-032][20140611] jwlee �����ǳ� �˻� ������ 15������ 25���� ����.
        case 19: // '\017'
            return String.valueOf("Part16");
        case 20: // '\018'
            return String.valueOf("Part17");
        case 21: // '\019'
            return String.valueOf("Part18");
        case 22: // '\020'
            return String.valueOf("Part19");
        case 23: // '\021'
            return String.valueOf("Part20");
        case 24: // '\022'
            return String.valueOf("Part21");
        case 25: // '\023'
            return String.valueOf("Part22");
        case 26: // '\024'
            return String.valueOf("Part23");
        case 27: // '\025'
            return String.valueOf("Part24");
        case 28: // '\026'
            return String.valueOf("Part25");
        //--------------------------------------------
        }

        String s = getReg().getString("ResultData.InvalidColumnIndex");
        throw new Exception(s);
    }

    private static Registry getReg()
    {
        if(registry == null)
            registry = Registry.getRegistry(APADialog.class);
        return registry;
    }

    private static Registry registry = null;
    @SuppressWarnings("rawtypes")
    public Hashtable rowContext;
    @SuppressWarnings("rawtypes")
    public Hashtable columnValues;
    private boolean filterOut;
    private int serialNumber;

}
