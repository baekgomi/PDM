package com.symc.plm.rac.prebom.dcs.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * [20160512][ymjang] ������ Ž������ �����ɼǿ��� Ȯ���� ����� ó���� �Ǿ� �ִ� ���, ���ϸ��� �����ϸ� Ȯ���� �ν� ���� ���� ����. --> Ȯ���� �ʴ� ���� ��� �߰�
 *
 */
public class DCSUIUtil {

	public DCSUIUtil() {

	}

	public static void openShell(Shell parentShell, Shell childShell) {
		childShell.open();
		childShell.layout();
		Display display = parentShell.getDisplay();
		while (!childShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public static String openFileDialog(Shell shell, String fileName) {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFileName(fileName);
		fileDialog.setOverwrite(true);

		return fileDialog.open();
	}

	/**
	 * [20160512][ymjang] ������ Ž������ �����ɼǿ��� Ȯ���� ����� ó���� �Ǿ� �ִ� ���, 
	 * ���ϸ��� �����ϸ� Ȯ���� �ν� ���� ���� ����. --> Ȯ���� �ʴ� ���� ��� �߰�
	 * @param shell
	 * @param fileName
	 * @param filterName
	 * @return
	 */
	public static String openFileDialogWithFilter(Shell shell, String fileName, String filterName) {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFileName(fileName);
		fileDialog.setFilterNames(new String [] {filterName});
		fileDialog.setFilterExtensions(new String [] {filterName});
		fileDialog.setOverwrite(true);

		return fileDialog.open();
	}
	
	public static void centerToParent(Shell parentShell, Shell childShell) {
		Rectangle parentRectangle = parentShell.getBounds();
		Rectangle childRectangle = childShell.getBounds();
		int x = parentShell.getLocation().x + (parentRectangle.width - childRectangle.width) / 2;
		int y = parentShell.getLocation().y + (parentRectangle.height - childRectangle.height) / 2;
		childShell.setLocation(x, y);
	}

}
