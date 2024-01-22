/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.springframework.util.StringUtils;

import com.symc.plm.me.utils.BundleUtil;

/**
 * Class Name : FileUtil
 * Class Description :
 * 
 * @date 2013. 11. 21.
 * 
 */
public class FileUtil {

    /**
     * Folder ���� ���� ����Ʈ�� ������ �´�.
     * 
     * @method getFileList
     * @date 2013. 11. 21.
     * @param
     * @return ArrayList<String>
     * @exception
     * @throws
     * @see
     */
    public static ArrayList<String> getFileList(String folderPath) {
        ArrayList<String> fileList = new ArrayList<String>();
        File dirFile = new File(folderPath);
        File[] folderFileList = dirFile.listFiles();
        for (File tempFile : folderFileList) {
            if (tempFile.isFile()) {
                // String tempPath = tempFile.getParent();
                String tempFileName = tempFile.getName();
                // System.out.println("Path=" + tempPath);
                // System.out.println("FileName=" + tempFileName);
                fileList.add(tempFileName);
            }
        }
        return fileList;
    }

    /**
     * File�� �о� Text�� �����´�.
     * 
     * @method getFileText
     * @date 2013. 11. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getFileText(String filePath) throws Exception {
        StringBuffer texts = new StringBuffer();
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(filePath);
            bis = new BufferedInputStream(fis);
            int data = 0;
            while ((data = bis.read()) != -1) { // -1 -> ������ ��
                texts.append((char) data);
            }
        } catch (FileNotFoundException e) {
            throw e;

        } catch (IOException e) {
            throw e;

        } finally { // ������ ���� �ݾ������. ����ó���� �������� �ȵ����� �Ѵ� �ݾ������.
                    // �Ѵ� �ݾ�����ϱ� ������ finally �� ���!
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                }
            if (bis != null)
                try {
                    bis.close();
                } catch (IOException e) {
                }
        }
        return texts.toString();
    }

    /**
     * ���� Ȯ���ڸ� ������ ���ϸ��� �����´�.
     * 
     * @method getExculsiveExtFileName
     * @date 2013. 11. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getExculsiveExtFileName(String fileName) {
        fileName = BundleUtil.nullToString(fileName);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }  

    /**
     * File�� Text�� appand�Ѵ�.
     * 
     * @method appandFileText
     * @date 2013. 11. 28.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void appandFileText(String filePath, String text) throws Exception {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        // ���� ���� �߰�
        text += "\r\n";
        RandomAccessFile raf = new RandomAccessFile(new File(filePath), "rw");
        long fileLength = raf.length();
        raf.seek(fileLength); // to the end
        raf.write(text.getBytes());
        raf.close();
    }
}
