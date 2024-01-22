/**
 * 
 */
package org.sdv.core.ui;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.IViewStubBean;
import org.sdv.core.ui.view.layout.View;
import org.sdv.core.util.SDVSpringContextUtil;

/**
 * Class Name : UIManager
 * Class Description :
 * 
 * @date 2013. 9. 23.
 * 
 */
public abstract class UIManager {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(UIManager.class);
    
    public static final String UIMANAGER_BEAN_NAME = "UIManager";

    public static final String REF_UI_MANAGER_CLASS = "uiManagerClass";
    
    public static final String REF_UI_DIALOG_LAYOUT_CLASS = "dialogLayoutClass";

    
    private static UIManager uiManagerImpl = null;
    
    
    /**
     * UIManager�� ������ ���̾˷α׸� �����ϰ� �����ϴ� Map Container;
     */
    private DialogManager dialogManager;

//    
//    private IDialog currentDialog;
    
    /**
     * Spring Bean���� UIManager�� �����´�.
     * 
     * @method getUIManagerImpl
     * @date 2013. 10. 14.
     * @param
     * @return UIManager
     * @exception
     * @throws
     * @see
     */
    protected static UIManager getUIManagerImpl(){
        if(uiManagerImpl == null){
            uiManagerImpl = (UIManager)SDVSpringContextUtil.getBean(UIMANAGER_BEAN_NAME);
        }
        
        if(uiManagerImpl ==  null){
            throw new IllegalStateException("Can not instantiation for implemented UIManager Class!!!");
        }
        
        return uiManagerImpl;
    }
    
    /**
     * dialogId�� ������ Dialog�� �����Ѵ�.
     * 
     * @method getDialog
     * @date 2013. 10. 2.
     * @param
     * @return AbstractSDVDialog
     * @exception
     * @throws
     * @see
     */
    public static IDialog getDialog(Shell shell, String dialogId) throws Exception {
        return (IDialog)getUIManagerImpl().getDialogImpl(shell, dialogId);
    }
    
    public static IDialog getAvailableDialog(String dialogId){
        return (IDialog)getUIManagerImpl().getDialog(dialogId);
    }

    public static IDialog getCurrentDialog(){
        return (IDialog)getUIManagerImpl().getDialog();
    }

    /**
     * 
     * @method getView
     * @date 2013. 10. 14.
     * @param
     * @return IViewPane
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public static IViewPane getView(Composite container, IViewStubBean viewstub, View layoutView) throws Exception {
        return ViewStubBeanFactory.getViewInstance(container, SWT.NONE, viewstub, layoutView);
    }    
    
    /**
     * 
     * @method getView
     * @date 2013. 10. 14.
     * @param
     * @return IViewPane
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public static IViewPane getView(String parentId, String viewId) throws Exception {
        return getUIManagerImpl().getViewImpl(parentId, viewId);
    }  
    
   
    /**
     * UIManager Constructor realize �Ͽ� implement �� Ŭ���������� ���� ����
     */
    protected UIManager() {
    	dialogManager = new DialogManager(this);
    }
    

    /**
     * @return the getDialogSkeleton
     */
    protected abstract IDialog getDialogSkeleton(Shell shell, String Id) throws Exception;
    
    /**
     * 
     * @method getCurrentDialog 
     * @date 2013. 11. 14.
     * @param
     * @return IDialog
     * @exception
     * @throws
     * @see
     */
    public IDialog getDialog() {
        return getDialog(null);
    }    
   
    /**
     * �����ϰ� �ִ� ���̾˷α� ����Ʈ �߿��� �־��� ���̵��� ���̾˷α׸� ã�Ƽ� ��ȯ�մϴ�.
     * 
     * @method getDialog 
     * @date 2013. 11. 14.
     * @param
     * @return IDialog
     * @exception
     * @throws
     * @see
     */
    public IDialog getDialog(String dialogId){
    	return dialogManager.getDialog(dialogId);
    }
    
    /**
     * ���̾˷α׸� ��ȯ �޼��� ����
     * 
     * @method getDialogImpl 
     * @date 2013. 11. 14.
     * @param
     * @return IDialog
     * @exception
     * @throws
     * @see
     */
    protected IDialog getDialogImpl(Shell newShell, String dialogId) throws Exception {
        
        IDialog dialog = getDialog(dialogId);
        
        //�����ǰ� �ִ� ���̾˷αװ� ���ٸ� ���Ӱ� ���̾˷α׸� �����Ͽ� ���̾˷α� �����̳ʿ� �����  ��ȯ�Ѵ�,
        if(dialog == null){
            dialog = (IDialog) getDialogSkeleton(newShell, dialogId);
            //������ ���̾˷α׸� �����ϱ� ���� ������.
            addDialog(dialogId, dialog);            
        }
        return dialog;
    }
    
    
    protected IViewPane getViewImpl(String parentId, String viewId){
        IViewPane viewPane = null;
        
        if(dialogManager.getWindowCount() == 0) return viewPane;
        try{
        for(Window window : dialogManager.getWindows()){
           if(window instanceof IDialog){
               viewPane = getChildView((IViewPane)window, parentId, viewId);
               if(viewPane != null) break;
           }
        }
        }catch(IllegalStateException ex){
          //�θ���̵�� ã������ �ش� �θ� �� �������� �ڽ� �並 ã�� ���� ��쿡 ������ �߻���Ų��.
        }
        return viewPane;
    }
    
    protected IViewPane getChildView(IViewPane parentView, String parentId, String viewId){
        
        IViewPane view = null;
        if(parentView == null || viewId == null ) return view;
        //�־��� �䰳ü�� ã�� ���� �θ���̵�� ������ ���Ѵ�.  �ٸ����� �ڽĵ���� ������ ã�´�.
        boolean isParent = (parentView.getId() != null && parentView.getId().equals(parentId));
        
        //�־��� �θ�� ��� ���� ���̵� ���� ���� �־��� ��쿡�� �ڽ��� �ƴ� �ش� View�� ��ȯ�Ѵ�. 
        if(isParent && parentId == viewId ) return parentView;
        
        for(IViewPane childView : parentView.getViews()){
            //�ڽ��߿��� ���� ���� �ִ��� ã�´�.
            if(childView != null && childView.getId() != null && childView.getId().equals(viewId)){
                view = childView;
                break;
            }
            //�ڽ� �信�� ã�����ϸ� �ٽ� �� ������ ������ ã�´�.
            view = getChildView(childView, parentId, viewId);
            if(view != null)break;
        }
        //�θ���̵�� ������  �ڽĺ� �߿��� �ش� �䰡 ���°�쿡��  ������ �߻��Ͽ� ��ü������ ã�� �۾��� �ߴܽ�Ų��.
        if(isParent && view == null) throw new IllegalStateException();
        return view;
    }
    
    /*#########################################################################################
     * 
     * DialogListenr Implement Method
     * 
     * ########################################################################################
     */
    protected IDialog addDialog(String dialogId, IDialog dialog) {
        if(dialog == null) return null;
        dialogManager.add((Dialog)dialog);
        return dialog;
    }

}
