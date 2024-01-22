package com.symc.plm.me.sdv.dialog.common;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * [SR151207-041][20151215] taeku.jeong �������� ���������� Occurrence Name�� ã�Ƽ� ǥ�����ִ� �޴��߰�
 * �����ϰ� �������� �Ǵ� User �Է��� �޴� Dialog�� ���� ���� �ֵ��� ������ Class
 * @author Taeku
 *
 */
public class SimpleUserInputDialog {
	
	// User �Է� ������ �ӽ������ϱ����� ��������
	static private String tempString = null;
	
	/**
	 * User�� ���� ������ �Է� ������ �Է¹޾Ƽ� Return ���ִ� �Լ� 
	 * 
	 * @param dialogTieleString ����� �Է�â�� Title
	 * @param dialogMessageString ����� �Է�â�� ǥ�õ� ������ ������ ���� �޽���
	 * @param initialValueString �ʱⰪ���� �ԷµǾ� ���� ����
	 * @return
	 */
	static public String getUserInputString(String dialogTieleString, String dialogMessageString, String initialValueString){

	    	String userInputString = null;
	    	
			final String title = dialogTieleString;
			final String messageString = dialogMessageString;
			final String initialValue = initialValueString;
			final Shell shell = AIFUtility.getActiveDesktop().getShell();
			
			// �Ʒ��� UserInputLengthValidator �����ؼ� ��ü���� ������ �ʿ��Ҷ� ���� �Ұ�.
			final IInputValidator inputValidator = null;
			
			shell.getDisplay().syncExec(new Runnable() {
				
				public void run()
				{
					//UserInputLengthValidator validator = new LengthValidator();
					//UserInputLengthValidator validator = null;
					
					InputDialog dlg = new InputDialog(shell, title, messageString, initialValue, inputValidator);

					if (dlg.open() == Window.OK) {
				          // User clicked OK; update the label with the input
						tempString = dlg.getValue();
				    }
					
				}
			});
			
			if(tempString!=null && tempString.trim().length()>0){
				userInputString = tempString.trim();
			}
			
			return userInputString;
	    	
	    }
	
    class UserInputLengthValidator implements IInputValidator {
    	
    	private int minLength = 0;
    	private int maxLength = 0;
    	
    	private UserInputLengthValidator(int minLength, int maxLength){
    		this.maxLength = maxLength;
    		this.minLength = minLength;
    	}

    	/**
    	 * Validates the String. Returns null for no error, or an error message
    	 * 
    	 * @param newText the String to validate
    	 * @return String
    	 */
    	public String isValid(String newText) {
    		int len = newText.length();

    		String message = null;
    		// Determine if input is too short or too long
    		if (minLength!=0 && len < minLength){
    			message = "Too short";
    		}
    		
    		if (maxLength!=0 && len > maxLength){
    			message = "Too long";
    		}

    		// Input must be OK
    		return message;
    	}
	}
	 
}
