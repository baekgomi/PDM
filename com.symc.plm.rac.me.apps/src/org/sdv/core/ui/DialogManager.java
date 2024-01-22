/**
 * 
 */
package org.sdv.core.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;

/**
 * Class Name : DialogManager
 * Class Description :
 * 
 * @date 2014. 1. 8.
 * @author CS.Park
 * 
 */
public class DialogManager extends WindowManager {

	// dialogStack.add(null);  			 // ���� �������� �Է��� ��Ұ� �ֻ�ܿ� ��ġ
	// dialogStack.peek();    			 // ���� ��ܿ� �ִ� ��Ҹ� ��ȯ
	// dialogStack.pop();      			 // ���� ��ܿ� �ִ� ��Ҹ� ��ȯ�ϰ� ����
	// dialogStack.push(item); 			 // ��Ҹ� �ֻ������ �о��.
	// dialogStack.set(index, elemenet); // ��Ҹ� Ư�� index ��ȣ�� �о��.
	// dialogStack.search(0);   	     //�ش����� �ε��� ��ȣ ȣ��
	private Stack<IDialog> dialogStack = new Stack<IDialog>();
	private List<DialogManager>	  subManagers;
	private UIManager uiManager;

	
	public DialogManager(UIManager uiManager)
	{
		super();
	}

	public DialogManager(DialogManager parent, UIManager uiManager)
	{
		this(uiManager);
		parent.addDialogManager(this);
	}
	
	public UIManager getUIManager(){
		return this.uiManager;
	}

	protected void addDialogManager(DialogManager dm)
	{
		if (this.subManagers == null) {
			this.subManagers = new ArrayList<DialogManager>();
		}
		if (!(this.subManagers.contains(dm)))
			this.subManagers.add(dm);
	}
	
	public int getDialogCount()
	{
		return this.dialogStack.size();
	}
	
	public Dialog[] getDialogs()
	{
		Dialog[] bs = new Dialog[this.dialogStack.size()];
		this.dialogStack.toArray(bs);
		return bs;
	}
	
	public void add(IDialog dialog)
	{
		if (!(this.dialogStack.contains(dialog))) {
			this.dialogStack.add(dialog);
			this.setCurrentDialog(dialog);
			dialog.setWindowManager(this);
		}
	}
	
	@Override
	public void add(Window window)
	{
		if(window instanceof IDialog){
			add((IDialog)window);
		}
	}	

	@SuppressWarnings("unchecked")
	@Override
	public boolean close()
	{
		List<Dialog> t = (List<Dialog>)this.dialogStack.clone();
		for(Dialog dialog : t){
			boolean closed = dialog.close();
			if (!(closed)) {
				return false;
			}
		}
		
		if (this.subManagers != null) {
			for(DialogManager dm : this.subManagers){
				boolean closed = dm.close();
				if (!(closed)) {
					return false;
				}
			}
		}
		return true;
	}

	public void remove(IDialog dialog)
	{
		if (this.dialogStack.contains(dialog)) {
			this.dialogStack.remove(dialog);
			dialog.setWindowManager(null);
		}
	}
	
	
	@Override
	public int getWindowCount()
	{
		return getDialogCount();
	}	
	
	@Override
	public Window[] getWindows()
	{
		Window[] bs = new Window[this.dialogStack.size()];
		this.dialogStack.toArray(bs);
		return bs;
	}

	/**
	 * 
	 * @method getCurrentDialog 
	 * @date 2014. 1. 8.
	 * @author CS.Park
	 * @param
	 * @return IDialog
	 * @throws
	 * @see
	 */
	public IDialog getCurrentDialog() {
		return this.dialogStack.peek();
	}
	
	public void setCurrentDialog(IDialog dialog){
		if(this.dialogStack.contains(dialog)){
			int index = dialogStack.search(dialog);
			if(index < dialogStack.size() -1){
				dialogStack.set(dialogStack.size() -1, dialog);
			}
		}else{
			dialogStack.push(dialog);
		}
	}

	/**
	 * 
	 * @method unsetCurrentDialog 
	 * @date 2014. 1. 8.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	public void unsetCurrentDialog(IDialog dialog){
		//�ֻ���� �ƴҰ�� �����Ѵ�.
		if(dialogStack.peek() == dialog){
			//�Ѱ��ۿ� ���� ��쿡�� �����Ѵ�.
			if(dialogStack.size() == 1) return;
			dialogStack.pop();
			//2��° ���̾˷α�
			IDialog secondDlg = dialogStack.pop();
			//2��°�� ������ �ι�° ��ġ�� ���� ��ġ�� �ִ´�.
			dialogStack.push(dialog);
			if(secondDlg != null){
				dialogStack.push(secondDlg);
			}
		}
	}
	
	/**
	 * 
	 * @method removeDialog 
	 * @date 2014. 1. 8.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	public void removeDialog(IDialog dialog) {
		dialogStack.remove(dialog);
		remove(dialog);
	}

	/**
	 * 
	 * @method getDialog 
	 * @date 2014. 1. 8.
	 * @author CS.Park
	 * @param
	 * @return IDialog
	 * @throws
	 * @see
	 */
	public IDialog getDialog(String dialogId) {
        //���̾˷α��� ���̵� �־����� �ʾҴٸ� �ֱ� ��ȯ�� ���̾˷α׸� ��ȯ�մϴ�.
        if(dialogId == null){
        	return dialogStack.peek();
        }
        
        IDialog dialog = null;
        for(IDialog dlg : dialogStack){
            if(dialogId.equals(dlg.getId())){
            	Shell shell = dlg.getShell();
            	if(shell == null || shell.isDisposed()){
            		remove(dlg);
            	}else{
            		dialog =dlg;    
            	}
            	break;
            }
        }
        return dialog;
	}
}