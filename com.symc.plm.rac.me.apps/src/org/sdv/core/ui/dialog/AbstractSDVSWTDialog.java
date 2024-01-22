/**
 *
 */
package org.sdv.core.ui.dialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.ActionButtonInfo;
import org.sdv.core.common.CommandButtonInfo;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.ISDVActionOperation;
import org.sdv.core.common.ISDVOperation;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.IViewStubBean;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.DialogManager;
import org.sdv.core.ui.OperationBeanFactory;
import org.sdv.core.ui.SDVUILayoutManager;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.event.ISDVInitOperationListener;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.dialog.event.ViewInitOperationListener;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.ui.view.ViewDataManager;
import org.sdv.core.ui.view.layout.View;
import org.sdv.core.util.ProgressBar;
import org.sdv.core.util.ReflectUtil;
import org.sdv.core.util.UIUtil;
import org.springframework.util.StringUtils;

import swing2swt.layout.BorderLayout;

import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 *
 * SWT Base Dialog
 *
 * Class Name : AbstractSDVSWTDialog
 * Class Description :
 *
 * @date 2013. 10. 17.
 *
 */
public abstract class AbstractSDVSWTDialog extends Dialog implements IDialog, ISDVInitOperationListener {

	/**
	 * Logger
	 */
	private static final Logger			logger					 = Logger.getLogger(AbstractSDVSWTDialog.class);

	/**
	 * SDVSWTDialog���� �⺻������ �� �ִ� ��ư�� ���
	 */
	public static enum DefaultButtonEnum {
		Close, Cancel, Ok, Save, Open, Apply;
	}

	public static final String			DIALOG_PARAMETERS		  	= "DialogParameters";
	public static final String			TITLE_DESCRIPTION_INDENT   	= "     ";
	public static final String			TITLE_SEPERATOR				= " : ";

	public static final int				DEFAULT_COMMAND_BAR_HEIGHT 	= 30;
	public static final int			   	DEFAULT_BUTTON_WIDTH_HINT  	= 55;

	public static final int				DIALOG_RETURNCODE_CLOSE		= 0;
	public static final int				DIALOG_RETURNCODE_CANCEL   	= 0;
	public static final int				DIALOG_RETURNCODE_OK	   	= 1;
	public static final int				DIALOG_RETURNCODE_SAVE	 	= 2;
	public static final int				DIALOG_RETURNCODE_OPEN	 	= 3;
	public static final int				DIALOG_RETURNCODE_APPLY		= 4;


	protected boolean					optimalDisplay;
	protected Point						shellSize;
	protected Point						parentShellSize;

	protected Point						shellLocation;
	protected int						scrollBarWidthSize		 = 0;

	protected Composite					rootPane;
	protected Control					actionBar;
	protected Control					titleBar;

	protected Label						titleLabel;
	protected Label						descriptionLabel;

	protected Composite					rootComposite;
	protected Registry					parentRegistry;

	protected String					dialogTitle;
	protected Image						dialogImage;
	protected String					addtionalTitle;

	protected boolean					titleInitialzed;
	protected boolean					isApplyPressed			 = true;

	// Definition of member for SDV Framework
	protected DialogStubBean				dialogStub;
	private String						 id;
	private int							 configId;
	private Map<String, Object>			 parameters;

	protected Map<String, Integer>		  	buttonIdMap;
	protected LinkedHashMap<String, IButtonInfo> actionToolButtons;
	protected LinkedHashMap<String, IButtonInfo> commandToolButtons;

	/**
	 * View�� Layout�� UI���Ǹ� ���� �̸� �׸� ��� View�� ������ ��ġ�� View Container Composite�� Map
	 */
	protected HashMap<String, Composite>	viewContainerMap;

	/**
	 * Diaog�� ���Ե� View�� ���
	 */
	protected List<IViewPane>			    views;
	/**
	 * Diaog�� ���Ե� View�� ID�� ���ǵ� Map
	 */
	protected Map<String, IViewPane>		viewMap;

	/**
	 * �ʱ�ȭ�� View�� �ʱ�ȭ �������� View�� ���, �� ����Ʈ�� ��� �䰡 �������� ���̾˷α� �ʱ�ȭ�� �Ϸ�ȴ�.
	 */
	private ArrayList<IViewPane>		initializedViewList;
	private View						 currentViewLayout;
	private boolean						 initalized;

	// validation check ȣ�� ���� ( �ѹ��� ȣ���ϵ��� �ϱ� ���ؼ� ��� )
	private boolean						 validationChecked		  = false;

	// Check already set return code , because call from OKPressed
	private boolean						 alreadySetReturnCode	   = false;
//	private boolean isShowProgress;
	private ProgressBar progressShell;
	private Registry thisRegistry = null;
	private Shell thisShell = null;

	// ==================================================================================================
	//
	// Constructor
	//
	// ==================================================================================================

	/**
	 * Constructor
	 *
	 * @param shell
	 * @param dialogStub
	 */
	public AbstractSDVSWTDialog(Shell shell, DialogStubBean dialogStub) {
		this(shell, dialogStub, DEFAULT_CONFIG_ID);
	}

	/**
	 * Constructor
	 *
	 * @param shell
	 * @param dialogStub
	 */
	public AbstractSDVSWTDialog(Shell shell, DialogStubBean dialogStub, int configId) {
		super(shell);
		parentRegistry = Registry.getRegistry(AbstractSDVSWTDialog.class);
		setShellStyle(getShellStyle() | SWT.CLOSE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.RESIZE | SWT.BORDER);
		setBlockOnOpen(true);

		this.dialogStub = dialogStub;
		this.optimalDisplay = false;

		this.buttonIdMap = new HashMap<String, Integer>();

		this.id = dialogStub.getId();
		this.configId = configId;

		this.dialogTitle = (StringUtils.isEmpty(dialogStub.getTitle()) ? dialogStub.getId() : dialogStub.getTitle());
		this.dialogImage = this.dialogStub.getTitleImage();

		// ���̾˷α� ũ�� �ݿ�
		if (dialogStub.getWidth() > 0 && dialogStub.getHeight() > 0) {
			setDialogSize(dialogStub.getWidth(), dialogStub.getHeight() + DEFAULT_COMMAND_BAR_HEIGHT);
		}
		// �������� �ʴ� ������ ȣ���ϸ� ������ �߻��Ѵ�.
		validateConfig(configId);
	}

	public void setDialogSize(int width, int height){
		setParentDialogCompositeSize(new Point(width, height));
		if(getShell() != null && !getShell().isDisposed()){
			getShell().redraw();
		}
	}

	public DialogManager getDialogManager(){
		return (DialogManager)getWindowManager();
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Override dialog Metode
	//
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	protected void openPressed() {
		setReturnCode(DIALOG_RETURNCODE_OPEN);
		okPressed();
	}

	protected void savePressed() {
		setReturnCode(DIALOG_RETURNCODE_SAVE);
		okPressed();
	}

	@Override
	protected void okPressed() {
		if (!validationChecked && !validationCheck()) {
			return;
		}
		isApplyPressed = false;
		if (apply()) {
			setReturnCode(DIALOG_RETURNCODE_OK);
			close();
		}
	}

	/**
	 * ���� ��ư�� Ŭ�������� �̺�Ʈ ó��
	 * ���� Ŭ�������� ��ӹ޾� ������
	 */
	protected void applyPressed() {
		if (!validationChecked && !validationCheck()) {
			return;
		}
		isApplyPressed = true;
		apply();
		// apply�� â�� ���� �����Ƿ� apply �����ڵ尡 ����.
		// setReturnCode(DIALOG_RETURNCODE_APPLY);
	}

	protected void closePressed() {
		setReturnCode(DIALOG_RETURNCODE_CLOSE);
		this.getShell().close();
		close();
	}

	protected void cancelPressed() {
		setReturnCode(DIALOG_RETURNCODE_CANCEL);
		this.getShell().close();
		close();
	}

	@Override
	protected void setReturnCode(int code) {
		if (!alreadySetReturnCode) {
			super.setReturnCode(code);
			alreadySetReturnCode = true;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	public int getConfigId() {
		return configId;
	}

	protected Registry getRegistry() {
		try {
			if (thisRegistry == null)
				thisRegistry = Registry.getRegistry(this);

			return thisRegistry;
		} catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug(ex);
			}
			return parentRegistry;
		}
	}

	protected HashMap<String, Composite> addViewContainer(String viewId, Composite composite) {
		if (viewContainerMap == null) {
			viewContainerMap = new HashMap<String, Composite>();
		}
		viewContainerMap.put(viewId, composite);
		return viewContainerMap;
	}

	public Map<String, Composite> getViewContainer(Composite parent, String viewId) {
		return viewContainerMap;
	}

	/*
	 * ######################################################################################################
	 * #
	 * # Define abstract method
	 * #
	 * ######################################################################################################
	 */
	/**
	 * UI Render ���� �� ����
	 */
	protected abstract void afterCreateContents();

	public abstract void setLocalDataMap(IDataMap dataMap);

	public abstract IDataMap getLocalDataMap();

	public abstract IDataMap getLocalSelectDataMap();

	/**
	 *
	 *
	 * @method createDialogWindow
	 * @date 2013. 11. 21.
	 * @param
	 * @return void
	 * @see
	 */
	protected void createDialogWindow(Composite parentComposite) {

	}

	protected Composite getParentDialogComposite() {
		return this.rootComposite;
	}

	protected void showErrorMessage(String message, Throwable th) {

	}

	/**
	 * Ȯ��, ���� ��ư ������� ó��.<br>
	 * ���� �Ϸ�Ǿ����� true�� ��ȯ�ϵ��� �Ѵ�.
	 */
	protected boolean apply() {
		return true;
	}

	/*
	 * ######################################################################################################
	 * #
	 * # Overridden Method from Dialog(Window)
	 * #
	 * ######################################################################################################
	 */
	/**
	 *
	 * @method getTitleText
	 * @date 2013. 12. 1.
	 * @author CS.Park
	 * @param
	 * @return String
	 * @throws
	 * @see
	 */
	public String getDialogTitle() {
		return this.dialogTitle;
	}

	public Image getDialogImage() {
		return this.dialogImage;
	}

	public void setDialogTitle(String dialogTitle) {
		setDialogTextAndImage(dialogTitle, this.dialogImage);
	}

	public void setDialogTitle(Image dialogImage) {
		setDialogTextAndImage(this.dialogTitle, dialogImage);
	}

	/**
	 * SWTDilaog �� Ÿ��Ʋ �� �̹��������� ���� �޼ҵ�.
	 *
	 * @Copyright : KIMIES
	 * @author : CS.Park
	 * @since : 2013. 1. 10.
	 * @param title
	 * @param image
	 */
	protected void setDialogTextAndImage(String newTitle, Image newImage) {
		Shell dialogShell = getShell();
		boolean titleChanegd = false;

		if (titleInitialzed) {
			if (this.dialogTitle != newTitle) {
				this.dialogTitle = newTitle;
				titleChanegd = true;
			}

			if (this.dialogImage != newImage) {
				this.dialogImage = newImage;
				titleChanegd = true;
			}
			// ������ �Էµ� ���� �����ϸ� ������ ������ ���� �ʴ´�.
			if (!titleChanegd)
				return;

		} else {
			titleInitialzed = true;
		}

		String localeTitle = this.dialogTitle;
		// ��ӹ��� �ش� Ŭ�������� ���̾˷α� ID�� �ش�Ǵ� Ÿ��Ʋ�� �����Ͽ� �����´�.
		localeTitle = getRegistry().getString(this.dialogTitle + ".TITLE", this.dialogTitle);

		// â�� ������ �����մϴ�.
		dialogShell.setText(localeTitle);

		// â ������ �̹��� ����
		if (this.dialogImage == null) {
			// �־��� �̹����� ���ٸ� �ش� �̹����� ������Ʈ������ ���ҽ��� ã�´�.
			try {
				this.dialogImage = getRegistry().getImage(getId() + ".IMAGE");
			} catch (Exception ex) {
				logger.error(ex);
			}
		}

		if (this.dialogImage != null) {
			dialogShell.setImage(this.dialogImage);
		}
	}

	/**
	 * paramComposite �� ũ�⸦ ������ �ϱ����� �޼ҵ�.
	 *
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2013. 1. 10.
	 * @param width
	 * @param height
	 */
	protected void setParentDialogCompositeSize(Point size) {
		parentShellSize = size;
	}

	@Override
	final protected synchronized Control createContents(Composite parent) {
		Composite rootContentsPane = null;
		
		try{
			thisShell = (Shell) parent;

    		if (this.parentShellSize != null) {
    			parent.setSize(this.parentShellSize.x + 40, this.parentShellSize.y + 30);
    			UIUtil.centerToParent(parent.getParent().getShell(), (Shell)parent);
    		}

    		lockParent(true);
    		if (! getId().startsWith("symc.dialog.processSheetPreviewDialog"))
    		{
    			showProgress(true);
    		}

    		// â ������� ǥ���ϱ� ���� Ÿ��Ʋ ������ ���� ������ �ִ´�.
    		this.titleBar = createTitleBar(parent);
    
    		rootContentsPane = new Composite(parent, SWT.NONE);
    
    		// Ÿ��Ʋ�ٰ� �Ⱥ��ϰ�� �ʹ� â ���� �ٰ� �ǹǷ� ���� ������ �ش�.
    		GridLayout rootLayout = UIUtil.getGridLayout(1, 10, 0, (this.titleBar == null ? 10 : 0), 10, 5, 10, false);
    
    		rootContentsPane.setLayout(rootLayout);
    		rootContentsPane.setLayoutData(UIUtil.getGridData(GridData.FILL_BOTH)); // VERTICAL_ALIGN_FILL | GRAB_VERTICAL | VERTICAL_ALIGN_FILL | GRAB_HORIZONTAL
    		applyDialogFont(rootContentsPane);
    		initializeDialogUnits(rootContentsPane);
    
    		// set title
    		setDialogTextAndImage(this.dialogTitle, this.dialogImage);
    
    		this.actionBar = createActionBar(rootContentsPane);
    		this.dialogArea = createDialogArea(rootContentsPane);
    		this.buttonBar = createButtonBar(rootContentsPane);
    
    		//��� ȭ���� ��������� ���� �ʱ�ȭ����� �����Ѵ�. �� ���� �ʱ�ȭ ���۷��̼��� ȣ���ϰԵȴ�.
    		this.initializedViewList = new ArrayList<IViewPane>();
    		callViewInitOperations(new ArrayList<IViewPane>(views));
    		
		}finally{
			
			synchronized(this){
    			//�ʱ�ȭ�� ���� ��� ���α׷����� ������� ������ ���� �۾�, initialize operation�� ���� �ִٸ� ������ �޼��忡�� ó����
    			if(this.initializedViewList.size() == 0 || this.initalized ){
//    				showProgress(false);
    			}
			}
		}
		return rootContentsPane;
	}

	protected String getAddtionalTitle() {
		return addtionalTitle;
	}

	public void setAddtionalTitle(String addtionalTitle) {
		this.addtionalTitle = addtionalTitle;
		setTitle(null);
	}

	public void setTitleBackground(Color color) {
		if (this.titleBar == null)
			return;
		this.titleBar.setBackground(color);
	}

	protected Control createTitleBar(Composite parentComposite) {

		String title = this.dialogStub.getTitle();
		String description = this.dialogStub.getDescription();
		if (StringUtils.isEmpty(title) && StringUtils.isEmpty(description)) {
			return null;
		}

		Composite titleBar = new Composite(parentComposite, SWT.NONE);
		titleBar.setLayoutData(UIUtil.getGridData(GridData.FILL_HORIZONTAL)); // VERTICAL_ALIGN_FILL | GRAB_VERTICAL | VERTICAL_ALIGN_FILL | GRAB_HORIZONTAL
		titleBar.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
		titleBar.setLayout(UIUtil.getGridLayout(1, 20, 5, 0, 0, 5, 10, true));

		// Ÿ��Ʋ�� �ִ� ��� ���������� ������ ���� ���м��� �־��ش�.
		UIUtil.addUnderline(titleBar, UIUtil.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), UIUtil.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		// UIUtil.addBoxBorder(titleBar, UIUtil.getColor(SWT.COLOR_RED));

		// Ÿ��Ʋ �ٰ� ���̰� �Ǹ� ������ Ÿ��Ʋ �ٸ� ǥ�����ش�.
		if (StringUtils.isEmpty(title)) {
			title = this.dialogTitle;
		}

		this.titleLabel = new Label(titleBar, SWT.LEFT | SWT.BOLD);

		FontData[] fD = this.titleLabel.getFont().getFontData();
		fD[0].setHeight(12);
		fD[0].setStyle(SWT.BOLD);
		this.titleLabel.setFont(new Font(getShell().getDisplay(), fD[0]));
		this.titleLabel.setLayoutData(UIUtil.getGridData(GridData.FILL_HORIZONTAL));

		// Ÿ��Ʋ�ٰ� ������ ��� �ϰ����� ũ��� ���̱� ���� description�� ������ ������ �ڸ��� ä��� �Ѵ�.
		description = TITLE_DESCRIPTION_INDENT + (description == null ? "" : description);
		this.descriptionLabel = new Label(titleBar, SWT.LEFT);
		;
		this.descriptionLabel.setLayoutData(UIUtil.getGridData(GridData.FILL_HORIZONTAL));

		setTitle(title);
		setDescription(description);

		return titleBar;
	}

	public void setTitle(String title) {
		if (this.titleLabel == null)
			return;
		if (StringUtils.isEmpty(title)) {
			title = this.dialogTitle;
		}
		this.titleLabel.setText(title + (StringUtils.isEmpty(getAddtionalTitle()) ? "" : TITLE_SEPERATOR + getAddtionalTitle()));
	}

	public void setDescription(String description) {
		if (this.descriptionLabel == null)
			return;
		this.descriptionLabel.setText(description);
	}

	/**
	 * �θ�Ŭ���� Dialog�� createDialogArea (Dialog ǥ�ÿ��� ����)�� ȣ���ϰ� ���� ������ Cookieó���ϱ� ���� Override
	 *
	 * @method createDialogWindow
	 * @date 2013. 11. 21.
	 * @param parentComposite
	 * @return void
	 * @see
	 */
	@Override
	protected Control createDialogArea(Composite parentComposite) {

		this.rootComposite = (Composite) super.createDialogArea(parentComposite);

		Shell dialogParentShell = getShell();
		// dialogParentShell.setBackground(dialogParentShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		dialogParentShell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		FillLayout fillLayout = new FillLayout(SWT.NONE);
		this.rootComposite.setLayout(fillLayout);

		createDialogPanel(this.rootComposite);

		readFromCookies();

		return rootComposite;
	}

	protected Composite createDialogPanel(Composite parent) {

		Composite parentComposite = parent;

		if (isScolledDialog()) {
			ScrolledComposite scrolledComposite = new ScrolledComposite(parentComposite, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
			scrolledComposite.setExpandVertical(true);
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.getVerticalBar().setIncrement(10);
			scrolledComposite.setLayout(new FillLayout());
			if (parentShellSize != null) {
				scrolledComposite.setSize(parentShellSize);
				scrollBarWidthSize = parentShellSize.x;
			}
			parentComposite = scrolledComposite;
		}

		rootPane = new Composite(parentComposite, SWT.NONE);
		FillLayout rootLayout = new FillLayout(SWT.VERTICAL);
		rootPane.setLayout(rootLayout);

		try {
			// �並 �����Ѵ�.
			if (viewContainerMap == null || !viewContainerMap.containsKey(DEFAULT_VIEW_CONTAINER_ID)) {
				viewContainerMap = addViewContainer(DEFAULT_VIEW_CONTAINER_ID, rootPane);
			}
			List<IViewPane> views = SDVUILayoutManager.createViewPaneLayout(viewContainerMap, dialogStub, getParameters());
			if (views != null) {
				setViews(views);
			}

			if (isScolledDialog()) {
				final ScrolledComposite scrolledComposite = (ScrolledComposite) parentComposite;

				scrolledComposite.setContent(rootPane);
				scrolledComposite.addControlListener(new ControlAdapter() {

					public void controlResized(ControlEvent e) {
						int x;
						if (scrollBarWidthSize != 0) {
							x = scrollBarWidthSize;
						} else {
							x = rootPane.getClientArea().x;
						}
						scrolledComposite.setMinSize(rootPane.computeSize(x, SWT.DEFAULT));
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage("View Component Init Error!!", e);
		}
		return rootPane;
	}

	/**
	 *
	 * Description :
	 *
	 * @method :
	 * @date : 2013. 11. 21.
	 * @author : cspark
	 * @param : rootComposite Composite
	 * @return : Control
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {

		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_OUT);
		separator.setLayoutData(UIUtil.getGridData(GridData.FILL_HORIZONTAL));

		// CommnadBar�� ���̱� ���� ���̾˷α� �Ʒ� �ǳ�, ���̾˷α� ��ü ���� �����ϱ� ���� �����Ѵ�.
		Composite commandBar = new Composite(parent, SWT.NONE);
		GridData commandBarLayoutData = UIUtil.getGridData(GridData.FILL_HORIZONTAL); // VERTICAL_ALIGN_FILL | GRAB_VERTICAL | HORIZONTAL_ALIGN_FILL | GRAB_HORIZONTAL
		commandBarLayoutData.heightHint = DEFAULT_COMMAND_BAR_HEIGHT;
		commandBar.setLayoutData(commandBarLayoutData);
		commandBar.setLayout(new FillLayout());
		UIUtil.applyRandomBackground(commandBar);


		// �⺻ ��ɹٿ� ���� ��ɹٸ� �ֱ� ���� ��ư�ٵ��� ��� ���� Ŀ�ǵ��
		Composite buttonBar = new Composite(commandBar, SWT.NONE);
		buttonBar.setLayout(new BorderLayout(20, 0));

		// �⺻ ��ɾ���� �����ʺ��� ä���� ��ɹ�
		Composite defaultButtonArea = new Composite(buttonBar, SWT.NONE);
		defaultButtonArea.setLayoutData(BorderLayout.EAST);
		defaultButtonArea.setLayout(UIUtil.getGridLayout(1, 0, 0, 0, 0, convertHorizontalDLUsToPixels(4), convertVerticalDLUsToPixels(4), false));
		createButtonsForButtonBar(defaultButtonArea);
		UIUtil.applyRandomBackground(defaultButtonArea);
//
		// �߰� ��ɾ���� ���ʺ��� ä���� ��ɹ�
		Composite customButtonArea = new Composite(buttonBar, SWT.NONE);
		customButtonArea.setLayoutData(BorderLayout.CENTER);
		customButtonArea.setLayout(UIUtil.getGridLayout(1, 0, 0, 0, 0, convertHorizontalDLUsToPixels(4), convertVerticalDLUsToPixels(4), false));
		UIUtil.applyRandomBackground(customButtonArea);

		return createCustomButtonsForButtonBar(customButtonArea, customButtonArea);
		//return parent;
	}

	protected Composite createCustomButtonsForButtonBar(Composite customButtonBar, Composite defaultButtonBar) {

		Map<String, String> commandActions = ((DialogStubBean) getStub()).getCommandBarActions();
		Composite buttonBar = customButtonBar;

		if (commandActions != null && commandActions.size() > 0) {

			// �⺻ ��ư����� ���ٸ� �⺻ ��ư ��ġ�� ����� ��ư�� �Է��Ѵ�.
			if (!commandActions.containsKey("ACTION_CONFIG")) {
				buttonBar = defaultButtonBar;
			}
			// Custom ��ư�� ���� ��ȣ�� 10���� �����Ѵ�.
			int baseButtonId = 1000;
			for(String actionInfo : commandActions.keySet()) {
				if (actionInfo.equals("ACTION_CONFIG"))
					continue;

				int buttonId = -1;

				String commandOperation = commandActions.get(actionInfo);

				String[] commandInfos = actionInfo.split(";");
				String commandId = commandInfos[0];
				String buttonImage = commandId;

				// ������������ �־��� ��� (�־��� ������ ������ .ICON surfix�� �ٿ��� �̹����� ã�´�. )
				if (commandInfos.length > 1) {
					buttonImage = commandInfos[1];
				}

				// ��ư ��ȣ�� �־��� ���
				if (commandInfos.length > 2) {
					try {
						buttonId = Integer.parseInt(commandInfos[2]);
					} catch (Exception ex) {
						// ������ ���� �⺻ ��ư ���̵� ����ȴ�.
						// ó�� ��ư ��ȣ�� 1000���� �����Ѵ�.
						buttonId = ++baseButtonId;
					}
				} else {
					// ó�� ��ư ��ȣ�� 1000���� �����Ѵ�.
					buttonId = ++baseButtonId;
				}
				//�־��� ���۷��̼��� �⺻���� ��ư�� ���� �̸��� �ִ��� Ȯ���ϰ� �⺻ ��ư�� ��� �⺻ ��ư ������ ����Ѵ�.
				try{
					createDefaultCommandButton(DefaultButtonEnum.valueOf(commandId), buttonBar, commandActions);
				}catch(IllegalArgumentException iex){
					//�⺻��ư�� �ƴҰ�� Enum ������ ������ �߻��Ѵ�.
					//�⺻��ư�� �ƴҰ�� ��ư�� �����Ѵ�.  �̶� ��ư ���̵�� �⺻��ư�� �ƴϹǷ�  â�� �����ų� �ϴ� ����� ������ �޼��带 ȣ���Ͽ��� �Ѵ�.
					String buttonText = getRegistry().getString(commandId + ".TEXT", commandId);
					createButtonNOperation(buttonBar, commandActions, commandId, buttonId, buttonText, buttonImage, commandOperation, false);
				}
			}
		}

		return buttonBar;
	}

	/**
	 * DefaultButton�� �̹��� �߰� �� �����ư �߰�
	 *
	 * @Copyright : S-PALM
	 * @author : �ǿ���
	 * @since : 2013. 1. 10.
	 * @override
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		int actionConfig = 0;
		Map<String, String> commandActions = this.dialogStub.getCommandBarActions();

		// �־��� Action ������ ������ ��� , �������� �ʾ����� �ݱ� ��ư�� �⺻���� ���δ�.
		if (commandActions != null && commandActions.size() > 0) {
			if (commandActions.containsKey("ACTION_CONFIG")) {
				String actionConfigStr = (String) commandActions.get("ACTION_CONFIG");
				try {
					actionConfig = Integer.parseInt(actionConfigStr);
				} catch (Exception ex) {
					// �⺻���� ������ ���� ������ �߸������� ��쿡
					// �⺻ �����ܿ� �ٸ� ��ư���� ��ϵǾ� �ִٸ� �ƹ��� ��ư�� �������� �ʴ´�.
					// �ٸ� ��ư�� ���ǵǾ� ���� �ʴٸ� �ݱ��ư�� ���δ�.
					if (commandActions.size() > 1) {
						actionConfig = -1;
					}
				}
			} else {
				// �⺻���� ������ ���� ������ �߸������� ��� �ƹ��� ��ư�� �������� �ʴ´�.
				actionConfig = -1;
			}
		}
		// �⺻ ���� ��ư��
		Composite actionParent = parent;
		switch (actionConfig) {
		case 6:
			createDefaultCommandButton(DefaultButtonEnum.Open, actionParent, commandActions);
			createDefaultCommandButton(DefaultButtonEnum.Save, actionParent, commandActions);
			createDefaultCommandButton(DefaultButtonEnum.Cancel, actionParent, commandActions);
			break;
		case 5:
			createDefaultCommandButton(DefaultButtonEnum.Open, actionParent, commandActions);
			createDefaultCommandButton(DefaultButtonEnum.Cancel, actionParent, commandActions);
			break;
		case 4:
			createDefaultCommandButton(DefaultButtonEnum.Save, actionParent, commandActions);
			createDefaultCommandButton(DefaultButtonEnum.Cancel, actionParent, commandActions);
			break;
		case 3:
			createDefaultCommandButton(DefaultButtonEnum.Apply, actionParent, commandActions);
			createDefaultCommandButton(DefaultButtonEnum.Ok, actionParent, commandActions);
			createDefaultCommandButton(DefaultButtonEnum.Cancel, actionParent, commandActions);
			break;
		case 2:
			createDefaultCommandButton(DefaultButtonEnum.Ok, actionParent, commandActions);
			createDefaultCommandButton(DefaultButtonEnum.Close, actionParent, commandActions);
			break;
		case 1:
			createDefaultCommandButton(DefaultButtonEnum.Ok, actionParent, commandActions);
			createDefaultCommandButton(DefaultButtonEnum.Cancel, actionParent, commandActions);
			break;
		case -1: // �ƹ��� ��ư�� �������� �ʴ´�.
			break;
		case 0:
		default:
			createDefaultCommandButton(DefaultButtonEnum.Close, actionParent, commandActions);
		}
	}


	protected IButtonInfo createDefaultCommandButton(DefaultButtonEnum command, Composite parent, Map<String, String> commandActions) {
		switch (command) {
		case Apply:
			return createButtonNOperation(parent, commandActions, "Apply", 4, getRegistry().getString("Apply.TEXT"), "Apply_16", DEFAULT_METHOD_INDICATOR, false);
		case Open:
			return createButtonNOperation(parent, commandActions, "Open", 3, IDialogConstants.OPEN_LABEL, "Open_16", DEFAULT_METHOD_INDICATOR, false);
		case Save:
			return createButtonNOperation(parent, commandActions, "Save", 2, getRegistry().getString("Save.TEXT"), "Save_16", DEFAULT_METHOD_INDICATOR, false);
		case Ok:
			return createButtonNOperation(parent, commandActions, "Ok", 1, IDialogConstants.OK_LABEL, "OK_16", DEFAULT_METHOD_INDICATOR, false);
		case Close:
			return createButtonNOperation(parent, commandActions, "Close", 0, IDialogConstants.CLOSE_LABEL, "Close_16", DEFAULT_METHOD_INDICATOR, false);
		case Cancel:
			return createButtonNOperation(parent, commandActions, "Cancel", 0, IDialogConstants.CANCEL_LABEL, "Cancel_16", DEFAULT_METHOD_INDICATOR, false);
		}
		return null;
	}

	protected IButtonInfo createButtonNOperation(Composite parent, Map<String, String> commandActions, String commandId, int buttonId, String buttonText, String buttonImage, String commandOperationId, boolean defaultButton) {

		// �̹� ��ϵ� buttonId�̸� ��ϵ� ��ư�� ��ȯ�ϰ� �����Ѵ�. (��ư���̵� �ߺ��ǰ� �������� �ʴ´�.'
		for(IButtonInfo buttonInfo : getCommandToolButtons().values()){
			if(buttonInfo.getActionId().equals(commandId)){
				return buttonInfo;
			}
		}

		// ��ư�� �����Ѵ�.
		Button commandButton = createButton(parent, buttonId, buttonText, defaultButton);
//		commandButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent event) {
//				if (! event.widget.isDisposed())
//				{
//					final int clickButtonID = ((Integer) event.widget.getData()).intValue();
//					
//					Display.getCurrent().syncExec(new Runnable() {
//						@Override
//						public void run() {
//							if (thisShell.isEnabled())
//							{
////								showProgress(true);
//								
//								buttonPressed(clickButtonID);
//							}
//						}
//					});
//				}
//			}
//		});

		// ��ư�� �̹��� ����
		if (buttonImage != null) {
			if (!buttonImage.endsWith(".ICON")) {
				buttonImage += ".ICON";
			}

			Image iconImage = getRegistry().getImage(buttonImage);
			if (iconImage != null) {
				commandButton.setImage(iconImage);
				setButtonLayoutData(commandButton);
			}
		}

		// Context XML�� ���ǵ� ���۷��̼��� �ִٸ� ���۷��̼��� ������ش�. ���� ��� �⺻ ����� �����Ѵ�.
		if (commandActions.containsKey(commandId)) {
			commandOperationId = commandActions.get(commandId);
		}

		// ��ư ������ �����Ѵ�.
		IButtonInfo buttonInfo = new CommandButtonInfo(buttonId, commandId, commandOperationId, buttonImage, commandButton);
		getCommandToolButtons().put(String.valueOf(buttonId), buttonInfo);

		return buttonInfo;
	}

	@Override
	protected void setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = convertHorizontalDLUsToPixels(DEFAULT_BUTTON_WIDTH_HINT);
		int imageSize = 0;
		Point minSize = button.computeSize(-1, -1, true);

		if(button.getImage() != null){
			imageSize =8;
		}

		if(StringUtils.isEmpty(button.getText())){
			//4 == �����ܸ� ���� ��� ����
			data.widthHint = minSize.x + 4;
		}else{
			//�������� ���ԵǾ� ���� ��� computeSize�� ���� ���� ������ ũ�⸦ �������� �����Ƿ� �� ������ ó���Ѵ�.
			data.widthHint = Math.max(widthHint, minSize.x + imageSize);
		}
		button.setLayoutData(data);
	}

	public void setToolbarLocation(int location, Composite toolbar){

		switch(location){
		case SWT.LEFT :
			toolbar.setLayoutData(BorderLayout.WEST);
			break;
		case SWT.RIGHT :
			toolbar.setLayoutData(BorderLayout.EAST);
			break;
		case SWT.BOTTOM :
			toolbar.setLayoutData(BorderLayout.SOUTH);
			break;
		case SWT.TOP :
			default :
			toolbar.setLayoutData(BorderLayout.NORTH);
		}
	}

	protected Control createActionBar(Composite paramComposite) {

		Composite actionBar = new Composite(paramComposite, 0);
		actionBar.setLayoutData(UIUtil.getGridData(GridData.FILL_HORIZONTAL));
		actionBar.setLayout(UIUtil.getGridLayout(1, 0, 0, 0, 0, convertHorizontalDLUsToPixels(4), convertHorizontalDLUsToPixels(4), false));

		createButtonsForActionBar(actionBar);
		return actionBar;
	}

	protected void createButtonsForActionBar(Composite parent) {

		Map<String, String> toolbarActions = this.dialogStub.getToolbarActions();
		if (toolbarActions == null || toolbarActions.size() == 0) {
			parent.dispose();
			return;
		}

		for(String actionId : toolbarActions.keySet()) {
			String buttonId = actionId;
			String buttonImage = actionId;
			boolean isIconButton = false;

			String operationId = toolbarActions.get(actionId);

			if (actionId.indexOf(";") != -1) {
				String[] actionInfos = actionId.split(";");
				if (actionInfos.length > 1  && !StringUtils.isEmpty(actionInfos[1])) {
					buttonImage = actionInfos[1];
				} else {
					buttonImage = actionInfos[0];
				}

				// ���������� ǥ���ϱ�� �����Ǿ� ������ ��ư �ؽ�Ʈ�� �������� �����ش�.
				if (actionInfos.length > 2) {
					isIconButton = Boolean.valueOf(actionInfos[2]);
				}
				buttonId = actionInfos[0];
			}
			
			if(actionId.startsWith(LABEL_TYPE_ACTION_INDICATOR)){
				createActionLabel(parent, actionId, buttonId, buttonImage, operationId, isIconButton );
			}else if (!getActionToolButtons().containsKey(actionId)) {
				IButtonInfo actionButtonInfo = createActionButton(parent, actionId, buttonId, buttonImage, operationId, isIconButton );
				getActionToolButtons().put(buttonId, actionButtonInfo);
			}
		}

		// ��ȿ� �䰡 ������ ��� �䰡 �־��� �ǳ��� ũ�Ⱑ ó���� �۰� ������ ��ư�� �Ⱥ��̰� �Ǵ� ��찡 �߻��ϹǷ�
		// �ʱ� ���ǳ��� ��ü ũ�⸦ �⵵�� �����ϰ� ��ư�� ������ ���� ���ķ� �����Ͽ� �ش�.
		if (parent.getLayoutData() != null || parent.getLayoutData() instanceof GridData) {
			//GridData parentLayoutData = (GridData) parent.getLayoutData();

			int toolbarAlign = this.dialogStub.getToolbarStyle(IViewStubBean.TOOLBAR_ALIGN);
			switch(toolbarAlign){
			case SWT.RIGHT :
				parent.setLayoutData(UIUtil.getGridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
				break;
			case SWT.CENTER :
				parent.setLayoutData(UIUtil.getGridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.GRAB_HORIZONTAL));
				break;
			case SWT.FILL :
				parent.setLayoutData(UIUtil.getGridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
				break;
			case SWT.LEFT :
				default :
					parent.setLayoutData(UIUtil.getGridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
			}
		}
	}
	
	protected void createActionLabel(Composite parent, String actionId, String labelId, String labelImage, String labelText, boolean isIconLabel){
		
		((GridLayout) parent.getLayout()).numColumns += 1;

		Label label = new Label(parent, SWT.NONE);
		
		String [] labelTextInfos = (labelText ==  null? "":labelText).split(";");

		String localLabelText = getRegistry().getString(labelId + ".TEXT", labelTextInfos[0]);
		String labelToolTip = (isIconLabel? localLabelText:"");
		
		if(labelTextInfos.length > 1){
			label.setForeground(UIUtil.getColor(labelTextInfos[1], label.getForeground()));
		}
		
		if(labelTextInfos.length > 2){
			label.setBackground(UIUtil.getColor(labelTextInfos[2], label.getBackground()));
		}

		if (isIconLabel) {
			// ���� ����
			label.setToolTipText(labelToolTip);
		}else{
			// ��ư�� �ؽ�Ʈ ����
			label.setText(localLabelText);
			label.setFont(JFaceResources.getDialogFont());
		}

		label.setData(labelId);

		// ���� �̹��� ����
		if (labelImage != null) {
			if(!labelImage.endsWith(".ICON")){
				labelImage += ".ICON";
			}

			Image iconImage = getRegistry().getImage(labelImage);
			if(iconImage != null){
				label.setImage(iconImage);
			}
		}
		
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		int widthHint = convertHorizontalDLUsToPixels(DEFAULT_BUTTON_WIDTH_HINT);
		int imageSize = 0;
		Point minSize = label.computeSize(-1, -1, true);

		if(label.getImage() != null){
			imageSize =8;
		}

		if(StringUtils.isEmpty(label.getText())){
			//4 == �����ܸ� ���� ��� ����
			data.widthHint = minSize.x + 4;
		}else{
			//�������� ���ԵǾ� ���� ��� computeSize�� ���� ���� ������ ũ�⸦ �������� �����Ƿ� �� ������ ó���Ѵ�.
			data.widthHint = Math.max(widthHint, minSize.x + 4 + imageSize);
		}
		label.setLayoutData(data);
		
	}

	protected LinkedHashMap<String, IButtonInfo> getActionToolButtons() {
		if (actionToolButtons == null) {
			actionToolButtons = new LinkedHashMap<String, IButtonInfo>();
		}
		return actionToolButtons;
	}

	public LinkedHashMap<String, IButtonInfo> getCommandToolButtons() {
		if (commandToolButtons == null) {
			commandToolButtons = new LinkedHashMap<String, IButtonInfo>();
		}
		return commandToolButtons;
	}

	protected IButtonInfo createActionButton(Composite parent, String actionId, String buttonId, String buttonImage, String operationId, boolean isIconButton) {
		((GridLayout) parent.getLayout()).numColumns += 1;

		Button button = new Button(parent, SWT.PUSH);

		String buttonText = getRegistry().getString(buttonId + ".TEXT", buttonId);
		String buttonToolTip = (isIconButton? buttonText:"");

		if (isIconButton) {
			// ���� ����
			button.setToolTipText(buttonToolTip);
		}else{
			// ��ư�� �ؽ�Ʈ ����
    		button.setText(buttonText);
    		button.setFont(JFaceResources.getDialogFont());
		}

		button.setData(buttonId);

		// ��ư�� �̹��� ����
		if (buttonImage != null) {
			if(!buttonImage.endsWith(".ICON")){
				buttonImage += ".ICON";
			}

			Image iconImage = getRegistry().getImage(buttonImage);
			if(iconImage != null){
				button.setImage(iconImage);
			}
		}

		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				final String buttonId = (String) event.widget.getData();

				Display.getCurrent().syncExec(new Runnable() {
					@Override
					public void run() {
						if (thisShell.isEnabled())
						{
//							showProgress(true);
							
							actionPressed(buttonId);
						}
					}
				});
			}
		});

		setButtonLayoutData(button);
		return new ActionButtonInfo(actionId, buttonId, operationId, buttonImage, button);
	}

	protected boolean isScolledDialog() {
		return this.dialogStub.isScrolledDialog();
	}

	protected Point getInitialSize() {
		if (this.shellSize == null)
			this.shellSize = getShell().computeSize(-1, -1, true);
		return this.shellSize;
	}

	protected Point getInitialLocation(Point paramPoint) {
		if (this.shellLocation == null) {
			Composite localComposite = getShell().getParent();
			Monitor localMonitor = getShell().getDisplay().getPrimaryMonitor();
			if (localComposite != null)
				localMonitor = localComposite.getMonitor();
			Rectangle localRectangle = localMonitor.getClientArea();
			Point localPoint;
			if (localComposite != null)
				localPoint = Geometry.centerPoint(localComposite.getBounds());
			else
				localPoint = Geometry.centerPoint(localRectangle);
			this.shellLocation = new Point(localPoint.x - (paramPoint.x / 2), Math.max(localRectangle.y, Math.min(localPoint.y - (paramPoint.y * 2 / 3), localRectangle.y + localRectangle.height - paramPoint.y)));
		}
		return this.shellLocation;
	}

	protected void readFromCookies() {
		readDisplayParameters();
	}

	protected void saveDisplayParameters() {
		try {
		} catch (Exception localException) {
			logger.error(localException.getClass().getName(), localException);
		}
	}

	protected void saveAdditionalCookieParams(Cookie paramCookie, String paramString) {
	}

	protected void readDisplayParameters() {
		// try {
		// Shell localShell = getShell();
		// Point localPoint = localShell.getSize();
		// if (!(this.optimalDisplay)) {
		// this.shellSize = new Point(k, l);
		// this.shellLocation = new Point(i, j);
		// } else if ((localPoint.x <= k) && (localPoint.y <= l)) {
		// this.shellSize = new Point(k, l);
		// this.shellLocation = new Point(i, j);
		// }
		// loadAdditionalCookieParams(localCookie, str2);
		// } catch (Exception localException) {
		// logger.error(localException.getClass().getName(), localException);
		// }
	}

	protected void loadAdditionalCookieParams(Cookie paramCookie, String paramString) {
	}

	protected String getDisplayParametersKey() {
		return super.getClass().getName();
	}

	public boolean getOptimalDisplay() {
		return this.optimalDisplay;
	}

	public void setOptimalDisplay(boolean optimalDisplay) {
		this.optimalDisplay = optimalDisplay;
	}

	/*
	 * ######################################################################################################
	 * #
	 * #
	 * #
	 * ######################################################################################################
	 */

	protected void setViews(List<IViewPane> views) {
		this.views = views;
		if (viewMap == null)
			viewMap = new LinkedHashMap<String, IViewPane>();
		for(IViewPane view : views) {
			viewMap.put(view.getId(), view);
		}
	}

	/**
	 *
	 * @method callViewInitOperations
	 * @date 2013. 11. 28.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	protected synchronized void callViewInitOperations(List<IViewPane> views) {
		if (!initalized) {
			if (views != null) {
				try{
    				for(IViewPane view : views) {
						AbstractSDVInitOperation viewInitOperation = (AbstractSDVInitOperation) view.getInitOperation();
    					if (viewInitOperation != null) {
    						ViewInitOperationListener initOperationListener = new ViewInitOperationListener(this, view.getId(), viewInitOperation);
    						initOperationListener.addInitListener(this);
    						viewInitOperation.addOperationListener(initOperationListener);
    						viewInitOperation.getSession().queueOperation(viewInitOperation);
    					} else {
    						synchronized(this){
    							this.initializedViewList.add(view);
    						}
    						checkComplete();
    					}
    				}
				}catch(Exception ex){
					logger.error(ex);
					showErrorMessage("Initiliaze Error", ex);
					return;
				}
			} else {
				checkComplete();
			}
			initalized = true;
		}
	}

	protected void lockParent(boolean lock)
	{
		for (Control childControl : thisShell.getChildren())
		{
			childControl.setEnabled(lock ? false : true);
			childControl.update();
		}
//		thisShell.setEnabled(lock ? false : true);
//		thisShell.update();
	}

	protected void showProgress(boolean show){
//		if(this.isShowProgress != show){
			if(show){
				if(progressShell == null)
				{
					try
					{
						thisShell.getDisplay().syncExec(new Runnable() {
							@Override
							public void run() {
//							    thisShell.setVisible(false);
								progressShell = new ProgressBar(thisShell);
								progressShell.start();
//								thisShell.setVisible(true);
								progressShell.setActive();
							}
						});
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}else {
				lockParent(false);
				
				if(progressShell != null){
					thisShell.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							progressShell.close();
							progressShell = null;
//							thisShell.setVisible(true);
							thisShell.setActive();
							thisShell.setFocus();
//							thisShell.se
						}
					});
				}
			}

//			isShowProgress = show;
//		}
	}

	protected synchronized void checkComplete() {
		synchronized(this){
    		if (this.views.size() == 0 || this.views.size() == this.initializedViewList.size()) {
    			viewInitCompleted();
    			this.initializedViewList.clear();
    		}
		}
	}

	protected void setViewParamter(Map<String, Object> paramters) {
		if (getViewCount() > 0) {
			for(IViewPane view : getViews()) {
				view.setParameters(paramters);
			}
		}
	}

	/*---------------------------------------------------------------------------------------------
	 *
	 *  Commmand Button ó��
	 *
	 *---------------------------------------------------------------------------------------------*/

	protected void actionPressed(final String buttonId) {
		if (StringUtils.isEmpty(buttonId) || getActionToolButtons() == null || !getActionToolButtons().containsKey(buttonId)){
			return;
		}

		IButtonInfo buttonInfo = getActionToolButtons().get(buttonId);
		String operationId = buttonInfo.getOperationId();

		if (StringUtils.isEmpty(operationId))
			return;

		String[] operationParams = operationId.split(";");
		Map<String, Object> parameters = null;

		if (operationParams.length > 1) {
			parameters = parseActionParameters(operationParams[1]);
		}

		final String paramOperationId = operationParams[0];

		lockParent(true);

		// ���� â�� �޼��带 ȣ���ϴ� ���۷��̼�
		if (paramOperationId.startsWith(INTERNAL_METHOD_INDICATOR)) {
			localExecute(buttonId, operationId);
			return;
			// ���� â�� ����� ���� �޼��带 ȣ���ϴ� ���۷��̼�
		} else if (paramOperationId.startsWith(INTERNAL_VIEW_METHOD_INDICATOR)) {
			localChildExecute(buttonId, operationId);
			return;
		}
		try {
			final ISDVOperation operation = OperationBeanFactory.getActionOperator(paramOperationId, buttonId, getId(), parameters, getSelectDataSetAll());

			if (operation instanceof ISDVActionOperation) {
				if (! validationCheck())
				{
					lockParent(false);
					return;
				}

				if (! operationId.equals("com.symc.plm.me.sdv.operation.ps.ProcessSheetDownloadOperation;targetId=previewView"))
					showProgress(true);
				
				final ISDVActionOperation actionOperation = (ISDVActionOperation) operation;
				actionOperation.addOperationListener(new InterfaceAIFOperationListener() {

					@Override
					public void endOperation() {
						if (thisShell != null)
						{
							thisShell.getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									showProgress(false);
								}
							});
						}

						getShell().getDisplay().syncExec(new Runnable() {

							@Override
							public void run() {
								IDataSet result = actionOperation.getDataSet();
								String targetId = actionOperation.getTargetId();
								String ownerId = actionOperation.getOwnerId();
								AbstractSDVSWTDialog ownerDialog = (AbstractSDVSWTDialog) UIManager.getAvailableDialog(ownerId);
								IDataMap dataMap = result.getDataMap(targetId);
								if (dataMap == null) {
									dataMap = new RawDataMap();
								}
								dataMap.put("actionId", buttonId, IData.STRING_FIELD);
								ownerDialog.setDataSet(targetId, result);
							}
						});

					}

					@Override
					public void startOperation(String arg0) {
//						if (!AbstractSDVSWTDialog.this.validationCheck()) {
//							actionOperation.cancel();
//							throw new IllegalStateException();
//						}
					}
				});
				AIFUtility.getDefaultSession().queueOperation((Job) actionOperation);
			} else {
				showProgress(true);

				new Job(getId() + "::" + buttonId + "::" + operationId) {

					@Override
					protected IStatus run(IProgressMonitor arg0) {
						try {
							operation.startOperation(buttonId);
							operation.executeOperation();
							operation.endOperation();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							thisShell.getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									showProgress(false);
								}
							});
						}
						return null;
					}
				}.schedule();
			}
			return;
		} catch (ClassNotFoundException cnfex) {
			lockParent(false);
			logger.error(cnfex.getMessage());
		} catch (SDVException ex) {
			lockParent(false);
			logger.error(ex);
			return;
		} catch (Exception e) {
			lockParent(false);
			logger.error(e);
			return;
		}
	}

	protected Map<String, Object> parseActionParameters(String paramStr) {
		Map<String, Object> parameters = null;
		if (!StringUtils.isEmpty(paramStr)) {
			parameters = new HashMap<String, Object>();
			String[] entries = paramStr.split(":");
			for(String entryStr : entries) {
				String[] entry = entryStr.split("=");
				if (entry.length > 1) {
					parameters.put(entry[0], entry[1]);
				} else {
					parameters.put(entry[0], entry[0]);
				}
			}
		}
		return parameters;
	}

	protected void localExecute(int buttonId, String operationId) {
		localExecute(String.valueOf(buttonId), operationId);
	}

	protected void localExecute(String actionId, String operationId) {
		Object[] arguments = new Object[] { actionId, operationId };
		String methodName = operationId.substring(INTERNAL_METHOD_INDICATOR.length());
		Class<?> current = getClass();

		try {
			Class<?>[] argumentTypes = new Class<?>[] { String.class, String.class };
			Method method = ReflectUtil.getLocalMethod(current, methodName, argumentTypes);

			if (method != null) {
				method.invoke(this, arguments);
				return;
			}
			method = ReflectUtil.getLocalMethod(current, methodName, null);
			if (method != null) {
				method.invoke(this);
				return;
			}
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (InvocationTargetException e) {
			logger.error(e);
		} finally {
			lockParent(false);
		}
	}

	protected void localChildExecute(int buttonId, String operationId) {
		localChildExecute(String.valueOf(buttonId), operationId);
	}

	protected void localChildExecute(String buttonId, String operationId) {
		Object[] arguments = new Object[] { buttonId, operationId };
		String methodInfo = operationId.substring(INTERNAL_VIEW_METHOD_INDICATOR.length());

		try {
			int methodIndex = methodInfo.lastIndexOf(".");
			// �� �̸��� �����Ƿ� �۾��� �ߴ��Ѵ�.
			if (methodIndex < 1)
				return;
			
			String viewId = methodInfo.substring(0, methodIndex);
			String methodName = methodInfo.substring(methodIndex + 1);
			
			IViewPane targetView = getView(viewId);
			if (targetView == null)
				return;
			
			Class<?> current = targetView.getClass();
			Class<?>[] argumentTypes = new Class<?>[] { String.class, String.class };
			Method method = ReflectUtil.getLocalMethod(current, methodName, argumentTypes);

			if (method != null) {
				method.invoke(targetView, arguments);
				return;
			}
			method = ReflectUtil.getLocalMethod(current, methodName, null);
			if (method != null) {
				method.invoke(targetView);
				return;
			}
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (InvocationTargetException e) {
			logger.error(e);
		} finally {
			lockParent(false);
		}
	}

	@Override
	protected void buttonPressed(final int buttonId) {
		if (! thisShell.isEnabled())
			return;
		
		this.validationChecked = false;

		IButtonInfo buttonInfo = getCommandToolButtons().get(String.valueOf(buttonId));
		String operationId = buttonInfo.getOperationId();

		String[] operationParams = operationId.split(";");

		Map<String, Object> parameters = null;
		if (operationParams.length > 1) {
			parameters = parseActionParameters(operationParams[1]);
		} else {
		    // targetId�� xml�� �ƴ� ���α׷����� �ֱ� ���� �߰�
		    Map<String, Object> dialogParam = getParameters();
		    if(dialogParam != null && dialogParam.size() > 0) {
		        parameters = dialogParam;
		    }
		}

		final String paramOperationId = operationParams[0];
		final String actionId = buttonInfo.getActionId();
		final String buttonInfoStr = buttonInfo.toString();

		// ������ ���۷��̼��� ���ٸ� �⺻ ���� ��ư ���۷��̼��� ����
		if (StringUtils.isEmpty(paramOperationId) || paramOperationId.equals(DEFAULT_METHOD_INDICATOR)) {
			defaultButtonPressed(buttonId);
			return;
		}else if (paramOperationId.startsWith(INTERNAL_METHOD_INDICATOR)) {
			// ���� â�� �޼��带 ȣ���ϴ� ���۷��̼�
			lockParent(true);
			localExecute(actionId, paramOperationId);
			return;
			// ���� â�� ����� ���� �޼��带 ȣ���ϴ� ���۷��̼�
		} else if (paramOperationId.startsWith(INTERNAL_VIEW_METHOD_INDICATOR)) {
			lockParent(true);
			localChildExecute(actionId, operationId);
			return;
		}

		lockParent(true);

		try {
			final ISDVOperation operation = OperationBeanFactory.getCommandOperator(paramOperationId, buttonId, getId(), parameters, getSelectDataSetAll());
			if (operation instanceof ISDVActionOperation) {
				if (! validationCheck())
				{
					lockParent(false);
					return;
				}

				if (! operationId.equals("com.symc.plm.me.sdv.operation.ps.ProcessSheetDownloadOperation;targetId=previewView"))
					showProgress(true);

				final ISDVActionOperation actionOperation = (ISDVActionOperation) operation;
				actionOperation.addOperationListener(new InterfaceAIFOperationListener() {

					@Override
					public void endOperation() {
						if (thisShell != null)
						{
							thisShell.getDisplay().syncExec(new Runnable() {
								
								@Override
								public void run() {
									showProgress(false);
								}
							});
						}

						Display display = getParentShell().getDisplay();
						display.syncExec(new Runnable() {

							@Override
							public void run() {

								if (actionOperation.getExecuteResult() == ISDVActionOperation.FAIL) {
									try {
										Throwable throwable = actionOperation.getExecuteError();
										if (throwable != null) {
											logger.error(throwable);
											MessageBox.post(actionOperation.getErrorMessage(), throwable, getDialogTitle(), MessageBox.ERROR);
										} else {
											MessageBox.post(actionOperation.getErrorMessage(), getDialogTitle(), MessageBox.ERROR);
										}
									} catch (Exception ex) {
										logger.error(ex);
									}
									return;
								}
								IDataSet result = actionOperation.getDataSet();
								String targetId = actionOperation.getTargetId();
								String targetDialog = null;
								if(targetId != null) {
								    String[] targetInfo = targetId.split("/");
								    targetId = targetInfo[targetInfo.length - 1];
								    if(targetInfo.length > 1) {
								        targetDialog = targetInfo[0];
								    }
								}

								IDataMap dataMap = result.getDataMap(targetId);
								if (dataMap == null) {
									dataMap = new RawDataMap();
								}
								dataMap.put("actionId", actionId, IData.STRING_FIELD);
								dataMap.put("operationId", paramOperationId, IData.STRING_FIELD);
								dataMap.put("commandButtonId", buttonId, IData.INTEGER_FIELD);

								AbstractSDVSWTDialog ownerDialog = (AbstractSDVSWTDialog) UIManager.getAvailableDialog(targetDialog);
								ownerDialog.setDataSet(targetId, result);

								defaultButtonPressed(buttonId);
							}
						});
					}

					@Override
					public void startOperation(String arg0) {
//						if (!AbstractSDVSWTDialog.this.validationCheck()) {
//							actionOperation.cancel();
//							throw new IllegalStateException();
//						}

						validationChecked = true;
					}
				});

				logger.debug(getId() + "-> " + buttonInfoStr);
				AIFUtility.getDefaultSession().queueOperation((Job) actionOperation);
			} else {
				showProgress(true);

				new Job(operationId) {

					@Override
					protected IStatus run(IProgressMonitor arg0) {
						logger.debug(getId() + "-> " + buttonInfoStr);
						try {
							operation.startOperation(paramOperationId);
							operation.executeOperation();
							operation.endOperation();
							// Listener�� ���� ��
							defaultButtonPressed(buttonId);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							thisShell.getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									showProgress(false);
								}
							});
						}
						return null;
					}
				}.schedule();
			}
			return;
		} catch (ClassNotFoundException cnfex) {
			lockParent(false);
			logger.error(cnfex.getMessage());
		} catch (SDVException ex) {
			lockParent(false);
			logger.error(ex);
			return;
		} catch (Exception e) {
			lockParent(false);
			logger.error(e);
			return;
		}
	}

	/**
	 *
	 * @method defaultButtonPressed
	 * @date 2013. 11. 26.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	protected void defaultButtonPressed(int buttonId) {

		// �ش� ��ư�� Ŭ���� ������ ó��
		// Return ó���� �ʿ���� ��쿡�� �ٽ� reset���־�� �Ѵ�.
		switch (buttonId) {
		case DIALOG_RETURNCODE_APPLY /* 4 */:
			applyPressed();
			break;
		case DIALOG_RETURNCODE_OPEN /* 3 */:
			openPressed();
			break;
		case DIALOG_RETURNCODE_SAVE /* 2 */:
			savePressed();
			break;
		case DIALOG_RETURNCODE_OK /* 1 */:
			okPressed();
			break;
		case DIALOG_RETURNCODE_CLOSE /* 0 , DIALOG_RETURNCODE_CANCEL */:
			cancelPressed();
			break;
		}
	}

	/**
	 * Validation üũ �޼ҵ�
	 * Implements Method
	 *
	 * @return
	 */
	protected boolean validationCheck() {
		return true;
	}

	protected void handleShellCloseEvent() {
		saveDisplayParameters();
		super.handleShellCloseEvent();
	}

	/*
	 * ######################################################################################################
	 * #
	 * # Implemented Method of IViewPane
	 * #
	 * ######################################################################################################
	 */

	/**
	 * @return the parameters
	 */
	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * @param paramters
	 *            the parameters to set
	 */
	@Override
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the currentViewLayout
	 */
	public View getCurrentViewLayout() {
		return currentViewLayout;
	}

	/**
	 * @param currentViewLayout
	 *            the currentViewLayout to set
	 */
	public void setCurrentViewLayout(View currentViewLayout) {
		this.currentViewLayout = currentViewLayout;
	}

	/**
	 * @return the stub
	 */
	public IViewStubBean getStub() {
		return this.dialogStub;
	}

	/**
	 * @param stub
	 *            the stub to set
	 */
	public void setStub(IViewStubBean stub) {
		this.dialogStub = (DialogStubBean) stub;
	}

	@Override
	public Composite getRootContext() {
		return this.rootComposite;
	}

	/************************************************************************************
	 * Implement method from IViewPane interface
	 ************************************************************************************/

	/*
	 * @see org.sdv.core.common.IViewPane#getViews()
	 */
	@Override
	public List<IViewPane> getViews() {
		return views;
	}

	@Override
	public int getViewCount() {
		if (views == null)
			return 0;
		return views.size();
	}

	/*
	 * @see org.sdv.core.common.IViewPane#getView(java.lang.String)
	 */
	@Override
	public IViewPane getView(String viewId) {
		if (viewMap != null && viewMap.containsKey((viewId == null ? "" : viewId))) {
			return viewMap.get(viewId);
		}
		return null;
	}

	/*
	 * @see org.sdv.core.common.IViewPane#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/*
	 * @see org.sdv.core.common.IViewPane#refresh()
	 */
	@Override
	public void refresh() {

	}

	/*---------------------------------------------------------------------------------------------
	 *
	 *  Data ó��
	 *
	 *---------------------------------------------------------------------------------------------*/

	public void setDataSet(String targetViewId, IDataSet dataSet) {
		setDataSet(targetViewId, targetViewId, dataSet);
	}

	public void setDataSet(String targetViewId, String sourceViewId, IDataSet dataSet) {
		if (dataSet == null || sourceViewId == null || !dataSet.containsMap(sourceViewId))
			return;
		setDataMap(targetViewId, dataSet.getDataMap(sourceViewId));
	}

	public void setDataMap(String targetViewId, IDataMap dataMap) {
		if (targetViewId == null)
			return;
		if (targetViewId.equals(getId())) {
			setLocalDataMap(dataMap);
		} else {
			IViewPane targetView = getView(targetViewId);
			if (targetView == null) {
				for(IViewPane viewPane : getViews()) {
					if (viewPane instanceof AbstractSDVViewPane) {
						((AbstractSDVViewPane) viewPane).setDataMap(targetViewId, dataMap);
					}
				}
			} else if (targetView instanceof AbstractSDVViewPane) {
				((AbstractSDVViewPane) targetView).setLocalDataMap(dataMap);
			}
		}
	}

	public void setDataSetAll(IDataSet dataSet) {
		IDataMap dataMap = dataSet.getDataMap(getId());
		if (dataMap != null)
			this.setLocalDataMap(dataMap);

		if (getViewCount() > 0) {
			for(IViewPane viewPane : getViews()) {
				if (viewPane instanceof AbstractSDVViewPane) {
					((AbstractSDVViewPane) viewPane).setDataSetAll(dataSet);
				}
			}
		}
	}

	/**
	 * ��û�� config�� �����Ǵ��� ���θ� Ȯ���Ѵ�. �⺻ ���� �̿��� ������ �����Ϸ��� �� �޼��带 Override�Ͽ� �����Ѵ�.
	 *
	 * @method validateConfig
	 * @date 2013. 11. 21.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	protected void validateConfig(int configId) {
		if (configId != DEFAULT_CONFIG_ID)
			throw new SDVRuntimeException("View[" + getId() + " not supported config Id :" + configId);
	}

	/*
	 * @see org.sdv.core.common.IViewPane#getDataSet(java.lang.String)
	 */
	@Override
	public IDataSet getDataSet(final String viewId) {
		final IDataSet newDataset = new DataSet();
		getParentShell().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				ViewDataManager.addDataSet(AbstractSDVSWTDialog.this, newDataset, viewId);
			}
		});
		return newDataset;

	}

	/*
	 * @see org.sdv.core.common.IViewPane#getDataSetAll()
	 */
	@Override
	public IDataSet getDataSetAll() {
		final IDataSet newDataset = new DataSet();
		getParentShell().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				ViewDataManager.addDataSetAll(AbstractSDVSWTDialog.this, newDataset);
			}
		});
		return newDataset;
	}

	/*
	 * @see org.sdv.core.common.IViewPane#getSelectDataSet(java.lang.String)
	 */
	@Override
	public IDataSet getSelectDataSet(final String viewId) {
		final IDataSet newDataset = new DataSet();
		getParentShell().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				ViewDataManager.addSelectDataSet(AbstractSDVSWTDialog.this, newDataset, viewId);
			}
		});
		return newDataset;
	}

	/*
	 * @see org.sdv.core.common.IViewPane#getSelectDataSetAll()
	 */
	@Override
	public IDataSet getSelectDataSetAll() {
		final IDataSet newDataset = new DataSet();
		getParentShell().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				ViewDataManager.addSelectDataSetAll(AbstractSDVSWTDialog.this, newDataset);
			}
		});
		return newDataset;
	}

	/**
	 * Description :
	 *
	 * @method :
	 * @date : 2013. 11. 22.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.common.IViewPane#getActionTools()
	 */
	@Override
	public List<IAction> getActionTools() {
		return null;
	}

	/**
	 * Description :
	 *
	 * @method :
	 * @date : 2013. 11. 22.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.common.IViewPane#getActionMenus()
	 */
	@Override
	public List<IAction> getActionMenus() {
		return null;
	}

	/**
	 * Description :
	 *
	 * @method :
	 * @date : 2013. 11. 28.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.common.IViewPane#getInitOperation()
	 */
	@Override
	public AbstractSDVInitOperation getInitOperation() {
		return null;
	}

	@Override
	public void uiLoadCompleted() {

	}

	@Override
	public void initalizeData(int result, IViewPane owner, IDataSet dataset) {

	}

	/**
	 * Description :
	 *
	 * @method :
	 * @date : 2013. 11. 28.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.dialog.event.ISDVInitOperationListener#startInitalize(org.sdv.core.ui.dialog.event.SDVInitEvent)
	 */
	@Override
	public void willInitalize(SDVInitEvent sdvInitEvent) {
		// ��� �䰡 �ʱ�ȭ �Ǿ����� Ȯ���ϰ� �ʱ�ȭ �Ǹ� �ٽ� �� �信 �˷��ֱ� ���� Ȯ���Ѵ�.
		String viewId = sdvInitEvent.getId();
		viewInitalizeData(getView(viewId), SDVInitEvent.INIT_SUCCESS, sdvInitEvent.getData());
	}

	/**
	 *
	 * @method failInitalize
	 * @date 2013. 11. 29.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	public void failInitalize(SDVInitEvent sdvInitEvent) {
		if (this.views == null)
			return;

		// ��� �䰡 �ʱ�ȭ �Ǿ����� Ȯ���ϰ� �ʱ�ȭ �Ǹ� �ٽ� �� �信 �˷��ֱ� ���� Ȯ���Ѵ�.
		String viewId = sdvInitEvent.getId();
		viewInitalizeData(getView(viewId), SDVInitEvent.INIT_FAILED, sdvInitEvent.getData());
	}

	protected synchronized void viewInitalizeData(IViewPane view, int result, IDataSet data) {
		if (view != null){
			try {
				view.initalizeData(result, this, data);
			} catch (Exception ex) {
				logger.error(ex);
				showErrorMessage("View initialize Error [" + view.getId() + "]", ex);
			} finally {
				synchronized(this){
					this.initializedViewList.add(view);
				}
				// �ʱ�ȭ ���� ���� ������ ����ٸ� ��� �ʱ�ȭ �Ǿ��ٰ� �Ǵ��ϰ� �ϷḦ ��Ų��.
				this.checkComplete();
			}
		}
	}

	/**
	 *
	 * @method viewInitCompleted
	 * @date 2013. 11. 28.
	 * @author CS.Park
	 * @param
	 * @throws
	 * @see
	 */
	protected void viewInitCompleted() {
		//���� �ʱ�ȭ�� �Ϸ�Ǹ� �� �信 �ʱ�ȭ�� �Ϸ�Ǿ����� �˷��ش�.
		try{
    		if (getViews() != null){
    			for(IViewPane view : getViews()) {
    				view.uiLoadCompleted();
    			}
    		}
		}catch(Exception ex){
			//������ ���� ��� ���α׷����� �������� �� �� ó���ϴ� ������ �߰��Ǿ�� �Ѵ�.
			showErrorMessage("Error occuered when init completing", ex);
		}finally{
			if (thisShell != null)
			{
				thisShell.getDisplay().syncExec(new Runnable() {
					
					@Override
					public void run() {
						showProgress(false);
					}
				});
			}
		}
	}

	@Override
	protected ShellListener getShellListener(){
		return new DialogShellListener();
	}

	protected class DialogShellListener extends  ShellAdapter{

		public void shellClosed(ShellEvent event) {
			event.doit = false;
			getDialogManager().removeDialog(AbstractSDVSWTDialog.this);
	    	if(logger.isDebugEnabled()){
	    		logger.debug("Dialog closed [" + getId() + "] " );
	    	}
			//Original Window Shell Listener
			//Never remove below lines
			if (canHandleShellCloseEvent()){
				handleShellCloseEvent();
			}
		}

	    public void shellActivated(ShellEvent shellEvent){
	    	getDialogManager().setCurrentDialog(AbstractSDVSWTDialog.this);
	    	if(logger.isDebugEnabled()){
	    		logger.debug("Dialog activated [" + getId() + "] " );
	    	}
	    }

	    public void shellDeactivated(ShellEvent shellEvent){
//	    	getDialogManager().unsetCurrentDialog(AbstractSDVSWTDialog.this);
//	    	if(logger.isDebugEnabled()){
//	    		logger.debug("Dialog activated [" + getId() + "] " );
//	    	}
	    }
	}
}
