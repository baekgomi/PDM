/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Node;

import com.symc.plm.me.sdv.service.migration.job.peif.DefaultValidationUtil;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.utils.BundleUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * Class Name : TCData
 * Class Description :
 * 
 * @date 2013. 11. 14.
 * 
 */
public abstract class TCData extends TreeItem {

    public static final String TC_TYPE_CLASS_NAME_SHOP = "SHOP"; // SHOP CLASS TYPE
    public static final String TC_TYPE_CLASS_NAME_LINE = "LINE"; // LINE CLASS TYPE
    public static final String TC_TYPE_CLASS_NAME_OPERATION = "OPERATION"; // ���� CLASS TYPE
    public static final String TC_TYPE_CLASS_NAME_ACTIVITY = "ACTIVITY";
    public static final String TC_TYPE_CLASS_NAME_ACTIVITY_SUB = "ACTIVITY_SUB";
    public static final String TC_TYPE_CLASS_NAME_TOOL = "TOOL";
    public static final String TC_TYPE_CLASS_NAME_EQUIPMENT = "EQUIPMENT";
	public static final String TC_TYPE_CLASS_NAME_END_ITEM = "END_ITEM";
	public static final String TC_TYPE_CLASS_NAME_SUBSIDIARY = "SUBSIDIARY";

    // STATUS
    public static final int STATUS_STANDBY = 0;
    public static final int STATUS_INPROGRESS = 1;
    public static final int STATUS_VALIDATE_COMPLETED = 2;
    public static final int STATUS_EXECUTE_COMPLETED = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_ERROR = 5;
    public static final int STATUS_WARNING = 6;
    public static final int STATUS_SKIP = 7;
    public static final String[] STATUS_STR = new String[] { "STANDBY", "INPROGRESS", "VALIDATE_COMPLETED", "EXECUTE_COMPLETED", "COMPLETED", "ERROR", "WARNING", "SKIP" };
    
    private String[] changeInformationFlags;
    
    // DecidedChagneType
    public static final int DECIDED_NO_CHANGE = 0;
    public static final int DECIDED_ADD = 1;
    public static final int DECIDED_REMOVE = 2;
    public static final int DECIDED_REVISE = 3;
    public static final int DECIDED_REPLACE = 4;
    public static final String[] CHANGE_TYPE_STR = new String[] { "NO_CHANGE", "ADD", "REMOVE", "REVISE", "REPLACE"};
    private int decidedChagneType = 0;		// Validation ��� ���� ���¸� ����ϴ� ���� �⺻���� No Change ��.

	// Item Class Type (Item(Item+revision) or Dataset)
    protected String classType;
    // TreeItem level
    protected int nLevel = -1;
    // �ش� TreeItem�� ���°�
    protected int nStatus = 0;
    // ���� �޼���
    protected String statusMassage;

    // Tree Colums
    protected TreeColumn[] columns;
    
    protected TCComponentBOMLine bopBomLine;
    protected Node bomLineNode;
	protected Node masterDataNode;
	
	private boolean haveMajorError = false;

	public TCData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, SWT.NONE, index);
        init(parentTree, classType, columns);
    }

    public TCData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, SWT.NONE, index);
        init(parentItem, classType, columns);
    }

    protected void init(Widget parentTreeObject, String classType, TreeColumn[] columns) {
    	
    	changeInformationFlags = new String[]{"*", "*", "*", "*", "*", "*", "*", "*", "*"};
    	
        this.classType = classType;
        this.columns = columns;
        if (parentTreeObject instanceof TreeItem) {
            ((TreeItem) parentTreeObject).setExpanded(true);
        }
        setClassImage();
        this.setStatus(STATUS_STANDBY);
    }

    protected abstract void setClassImage();

    /**
     * @return the classType
     */
    public String getClassType() {
        return classType;
    }

    /**
     * Level Setter
     */
    public void setLevel(int nLevel) {
        this.nLevel = nLevel;
    }

    /**
     * Level Getter
     */
    public int getLevel() {
        return this.nLevel;
    }

    public void setStatus(int nStatus) {
        setStatus(nStatus, BundleUtil.nullToString(statusMassage));
    }

    /**
     * Tree Status
     * 
     * @method getStatus
     * @date 2013. 11. 26.
     * @param
     * @return int
     * @exception
     * @throws
     * @see
     */
    public int getStatus() {
        return nStatus;
    }

    /**
     * Status Setter
     * 
     * @param nStatus
     */
    public void setStatus(int nStatus, String statusMassage) {
        this.nStatus = nStatus;
        this.statusMassage = statusMassage;
        int statusColumnIndex = columns.length - 1;
        // ���¹��� �߰�
        statusMassage = "[" + getStatusStr() + "] " + statusMassage;
        this.setText(statusColumnIndex, statusMassage);
        // Error �ΰ�� Background ����
        if (this.nStatus == STATUS_ERROR) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_RED));
        } else if (this.nStatus == STATUS_WARNING) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
        } else if (this.nStatus == STATUS_VALIDATE_COMPLETED) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if (this.nStatus == STATUS_EXECUTE_COMPLETED) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        } else if (this.nStatus == STATUS_SKIP) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
        } else {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        }
    }

    /**
     * Sync ���� �޼����� �����´�.
     * 
     * @method getStatusMessage
     * @date 2013. 11. 26.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getSyncStatusMessage(Shell shell) {
        final ArrayList<String> message = new ArrayList<String>();
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                message.add(getStatusMessage());
            }
        });
        if (message.size() > 0) {
            return message.get(0);
        } else {
            return "";
        }
    }

    /**
     * Sync getText
     * 
     * @method getSyncText
     * @date 2013. 11. 26.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getSyncText(Shell shell) {
        final ArrayList<String> text = new ArrayList<String>();
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                text.add(getText());
            }
        });
        if (text.size() > 0) {
            return text.get(0);
        } else {
            return "";
        }

    }

    /**
     * ���� �޼����� �����´�.
     * 
     * @method getStatusMessage
     * @date 2013. 11. 26.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getStatusMessage() {
        int statusColumnIndex = columns.length - 1;
        return this.getText(statusColumnIndex);
    }

    /**
     * ���� �ڵ带 ������ ���� ���ڸ� �����´�.
     * 
     * @method getStatusStr
     * @date 2013. 11. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getStatusStr() {
        return STATUS_STR[nStatus];
    }

    /**
     * �ʼ� Override Method
     */
    @Override
    protected void checkSubclass() {
    }
    
    public void setValidationResult(DefaultValidationUtil validationUtil){
    	
    	int validationResultChangeType = validationUtil.getValidationResultChangeType();
    	if(validationResultChangeType==TCData.DECIDED_NO_CHANGE){
    		TreeItem parentItem = getParentItem();
    		if(parentItem instanceof OperationItemData){
    			OperationItemData tempOperationItemData = (OperationItemData)parentItem;
    			// �������ϸ� �����ϰ� Parent Operatoin�� �߰��� ��� �̸�
    			// Child Node�� ��� �߰��� ó�� �Ѵ�.
    			if(tempOperationItemData.getDecidedChagneType()==TCData.DECIDED_ADD){
    				setDecidedChagneType(TCData.DECIDED_ADD);	
    			}
    		}else if(parentItem instanceof ActivityMasterData){
    			// �������ϸ� �����ϰ� Parent Activity Master�� �߰��� ��� �̸�
    			// Child Node�� ��� �߰��� ó�� �Ѵ�.
    			ActivityMasterData tempActivityMasterData = (ActivityMasterData)parentItem;
    			if(tempActivityMasterData.getDecidedChagneType()==TCData.DECIDED_ADD){
    				setDecidedChagneType(TCData.DECIDED_ADD);	
    			}    				
    		}    		
    	}else{
    		setDecidedChagneType(validationResultChangeType);
    	}

    	boolean isBOMLineAttChanged = validationUtil.isBOMAttributeChanged();
    	setBOMAttributeChangeFlag(isBOMLineAttChanged);

    	// End Item�� ��� Master Data�� ���� ������ ���� �����Ƿ� ���� ����� �ƴ�.
    	String currentClassType = getClassType();
    	if(currentClassType!=null){
    		if(currentClassType.trim().equalsIgnoreCase(TCData.TC_TYPE_CLASS_NAME_END_ITEM)==false){
    			boolean isAttChanged = validationUtil.isMasterDataChanged();
    			setAttributeChangeFlag(isAttChanged);
    		}
    	}
    	
    	setHaveMajorError(validationUtil.haveMajorError);
    	
    	updateChangeStatusText();


    }

    /**
     * ���� Node�� Attribute ���� ���θ� changeInformationFlags�� ��� �Ѵ�.
     * @param isChanged ����� ��� true�� �Է��Ѵ�.
     */
    public void setAttributeChangeFlag(boolean isChanged){
    	
    	if(isChanged==true){
    		changeInformationFlags[0] = "A";
    	}else{
    		changeInformationFlags[0] = "*";
    	}
    	updateChangeStatusText();
    	setParentFlags();
    }
    
    public boolean getAttributeChangeFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[0].trim().equalsIgnoreCase("A")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }

    /**
     * ���� Node�� BOMLine Attribute�� ����Ǿ����� ��� �Ѵ�.
     * @param isAdded
     */
    public void setBOMAttributeChangeFlag(boolean isAdded){
    	
    	if(isAdded==true){
    		changeInformationFlags[1] = "BA";
    	}else{
    		changeInformationFlags[1] = "*";
    	}
    	updateChangeStatusText();
    	setParentFlags();
    }
    
    public boolean getBOMAttributeChangeFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[1].trim().equalsIgnoreCase("BA")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
	 * Validation ����� ������ ���� Type�� ���� �����ϴ� �Լ�
	 * @param decidedChagneType
	 */
	public void setDecidedChagneType(int decidedChagneType) {
		
		this.decidedChagneType = decidedChagneType;
		
		if(decidedChagneType==TCData.DECIDED_NO_CHANGE){
			changeInformationFlags[2] = "*";
		}else if(decidedChagneType==TCData.DECIDED_ADD){
			changeInformationFlags[2] = "+";
		}else if(decidedChagneType==TCData.DECIDED_REMOVE){
			changeInformationFlags[2] = "-";
		}else if(decidedChagneType==TCData.DECIDED_REPLACE){
			changeInformationFlags[2] = "C";
		}else if(decidedChagneType==TCData.DECIDED_REVISE){
			changeInformationFlags[2] = "R";
		}
		updateChangeStatusText();
		
		TreeItem parentTreeItem = getParentItem();
		if(parentTreeItem!=null){
			if(decidedChagneType==DECIDED_ADD){
				((TCData)parentTreeItem).setChildNodeAddedFlag(true);
			} else if(decidedChagneType==DECIDED_REMOVE){
				((TCData)parentTreeItem).setChildNodeRemovedFlag(true);
			} else if(decidedChagneType==DECIDED_REVISE){
				((TCData)parentTreeItem).setChildNodeRevisedFlag(true);
			} else if(decidedChagneType==DECIDED_REPLACE){
				((TCData)parentTreeItem).setChildNodeReplacedFlag(true);
			}
			
			((TCData)parentTreeItem).updateChangeStatusText();
			setParentFlags();
		}
	
	}

	/**
	 * Validation ����� ������ ���� Type�� ���� �о� ���� �Լ�
	 * @return
	 */
	public int getDecidedChagneType() {
		return decidedChagneType;
	}

	/**
     * ���� Node�� Child Node�� �߰��� ���� �ִ��� changeInformationFlags�� ��� �Ѵ�.
     * @param isAdded
     */
    public void setChildNodeAddedFlag(boolean isAdded){
    	
    	if(isAdded==true){
    		changeInformationFlags[3] = "+";
    	}else{
    		changeInformationFlags[3] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildNodeAddedFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[3].trim().equalsIgnoreCase("+")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * ���� Node�� Child Node�� ���ŵ� ���� �ִ��� changeInformationFlags�� ��� �Ѵ�.
     * @param isRemoved
     */
    public void setChildNodeRemovedFlag(boolean isRemoved){
    	
    	if(isRemoved==true){
    		changeInformationFlags[4] = "-";
    	}else{
    		changeInformationFlags[4] = "*";
    	}
    	updateChangeStatusText();
   }
    
    public boolean getChildNodeRemovedFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[4].trim().equalsIgnoreCase("-")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * ���� Node�� Child Node�� Replace�� ���� �ִ��� changeInformationFlags�� ��� �Ѵ�.
     * @param isReplaced
     */
    public void setChildNodeReplacedFlag(boolean isReplaced){
    	
    	if(isReplaced==true){
    		changeInformationFlags[5] = "C";
    	}else{
    		changeInformationFlags[5] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildNodeReplacedFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[5].trim().equalsIgnoreCase("C")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * ���� Node�� Child Node�� Revise�� ���� �ִ��� changeInformationFlags�� ��� �Ѵ�.
     * @param isRevised
     */
    public void setChildNodeRevisedFlag(boolean isRevised){
    	
    	if(isRevised==true){
    		changeInformationFlags[6] = "R";
    	}else{
    		changeInformationFlags[6] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildNodeRevisedFlag(){

    	boolean isSet = false;
    	if(changeInformationFlags[6].trim().equalsIgnoreCase("R")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * ���� Node�� Child Node�� BOMLine Attribute ���� ���θ� ��� �Ѵ�.
     * @param isChildBOMLineChanged
     */
    public void setChildAttributeChangedFlag(boolean isChildBOMLineChanged){
    	
    	if(isChildBOMLineChanged==true){
    		changeInformationFlags[7] = "A";
    	}else{
    		changeInformationFlags[7] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildAttributeChangedFlag(){

    	boolean isSet = false;
    	if(changeInformationFlags[7].trim().equalsIgnoreCase("A")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * ���� Node�� Child Node�� BOMLine Attribute ���� ���θ� ��� �Ѵ�.
     * @param isChildBOMLineChanged
     */
    public void setChildBOMLineChangedFlag(boolean isChildBOMLineChanged){
    	
    	if(isChildBOMLineChanged==true){
    		changeInformationFlags[8] = "CB";
    	}else{
    		changeInformationFlags[8] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildBOMLineChangedFlag(){

    	boolean isSet = false;
    	if(changeInformationFlags[8].trim().equalsIgnoreCase("CB")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * ���� Node�� Change Flag�� ��ȭ�� ������ ����� Flag�� ������ Parent Node�� Change Flag�� �ݿ��Ѵ�.
     * 
     * �� �Լ��� ���� Node�� �������� ���� ���� Node����ϵ� ������ ������ �Բ� ����ϴ� ����� ����ǹǷ� �������� ��� ȣ���� �Ǵ� �����̴�.
     * �̷��� �ϴ� ������ Tc Structure Data�� N/F(Neutral Format Data) Tree Node�� ���� �ϸ鼭 Validation �� ������ �̿��� Interface ó���� ���� �Ҷ�
     * ������ ����ó���� ���Ǽ��� ������ �����̴�. 
     */
    private void setParentFlags(){
    	
    	TCData parentTCData =null;
    	if(getParentItem() == null || (getParentItem()==null && (getParentItem() instanceof TCData)==false) ){
    		return;
    	}else{
    		parentTCData = (TCData)getParentItem();
    	}    	
    	
    	String currentClassType = getClassType();
    	
    	boolean isAttributeChenged = getAttributeChangeFlag();						// 0
    	boolean isBOMAttributeChenged = getBOMAttributeChangeFlag();		// 1
		int parentChangeType = parentTCData.getDecidedChagneType();
    	
    	if(isAttributeChenged==true){

    		//--------------------------------------------------------------------------------------
    		// Attribute�� �ٲ�ٴ°��� �� Current Node�� ���� �ȴٴ°��� �ǹ� �Ѵ�.
    		//--------------------------------------------------------------------------------------
    		parentTCData.setChildAttributeChangedFlag(true);
    		
	    	if(currentClassType.trim().equalsIgnoreCase(TCData.TC_TYPE_CLASS_NAME_ACTIVITY_SUB)){
	    		// �ֿ��� ���� Rule�� ���� Sub Activity�� �߰� �ǰų� ����Ǹ� Activity Root ���� ���� �����Ѵ�. 
	    		parentTCData.setChildNodeRemovedFlag(true);
	    	}else if(currentClassType.trim().equalsIgnoreCase(TCData.TC_TYPE_CLASS_NAME_ACTIVITY)){
	    		// �ֿ��� ���� Rule�� ���� Sub Activity�� �߰� �ǰų� ����Ǹ� Activity Root ���� ���� �����ϹǷ� ������ Activity Root Node�� �����ؾ��Ѵ�.
	    		parentTCData.setChildNodeRemovedFlag(true);
	    	}
	    	
	    	int currentChangeType = getDecidedChagneType();
	    	if(currentChangeType == TCData.DECIDED_NO_CHANGE){
	    		setDecidedChagneType(TCData.DECIDED_REVISE);
	    	}
    	}
    	
    	// ���� Node�� BOMLine Attribute�� ����Ǹ� Parent Node�� Child Node�� BOMLine Attribute�� ����Ǿ�����
    	// ����ϵ��� �Ѵ�.
    	if(isBOMAttributeChenged==true){
   			parentTCData.setChildBOMLineChangedFlag(true);
   			
   			if(parentTCData.classType == TCData.TC_TYPE_CLASS_NAME_OPERATION){
   				parentChangeType = parentTCData.getDecidedChagneType();
   	       		if(parentChangeType == TCData.DECIDED_NO_CHANGE){
   	       			parentTCData.setDecidedChagneType(DECIDED_REVISE);
   	       		}
   			}
    	}

    	parentChangeType = parentTCData.getDecidedChagneType();

    	// Parent Type ���о��� �������� ����� ���� 
		if(decidedChagneType==TCData.DECIDED_NO_CHANGE){
			
		}else if(decidedChagneType==TCData.DECIDED_ADD){
			parentTCData.setChildNodeAddedFlag(true);
			if(parentChangeType == TCData.DECIDED_NO_CHANGE){
				parentTCData.setDecidedChagneType(DECIDED_REVISE);
			}    
		}else if(decidedChagneType==TCData.DECIDED_REMOVE){
			parentTCData.setChildNodeRemovedFlag(true);
			if(parentChangeType == TCData.DECIDED_NO_CHANGE){
				parentTCData.setDecidedChagneType(DECIDED_REVISE);
			}   
		}else if(decidedChagneType==TCData.DECIDED_REPLACE){
			parentTCData.setChildNodeReplacedFlag(true);
			if(parentChangeType == TCData.DECIDED_NO_CHANGE){
				parentTCData.setDecidedChagneType(DECIDED_REVISE);
			}   
		}else if(decidedChagneType==TCData.DECIDED_REVISE){
			parentTCData.setChildNodeRevisedFlag(true);
		}
    	
		// Parent�� Operation�� ��� ����� ����
    	if(parentTCData.getClassType() == TCData.TC_TYPE_CLASS_NAME_OPERATION){
    		if(decidedChagneType==TCData.DECIDED_NO_CHANGE){
    		}else if(decidedChagneType==TCData.DECIDED_ADD){
    		}else if(decidedChagneType==TCData.DECIDED_REMOVE){
    		}else if(decidedChagneType==TCData.DECIDED_REPLACE){
    		}else if(decidedChagneType==TCData.DECIDED_REVISE){
   	       		if(parentChangeType == TCData.DECIDED_NO_CHANGE){
   	       			parentTCData.setDecidedChagneType(DECIDED_REVISE);
   	       		}
    		}
    	}
       	
       	if(parentTCData!=null){
       		parentTCData.updateChangeStatusText();
       	}

    }
    
    /**
     * Tree Node�� Validation�� ���� �ϴ� ������ Validation ����� Tree Node�� ǥ�õǴ� Text�� �ݿ����ִ� �Լ���.
     * Attribute�� ����ǰų� ChildNode�� �߰�, ����, ����, ������ ���¸� ���ڷ� ǥ�õǴµ� �̰��� ���� �ǵ��� �Ѵ�.
     */
	public void updateChangeStatusText() {
		
		String titleStrings = getFirstText((String[])null);
		String newString	 = titleStrings.trim() + " "+ getChangeStatusText();
		
		super.setText(newString);
	}

	@Override
	public void setText(String[] strings) {
		
		String titleStrings = null;
			titleStrings = getFirstText(strings);
		
		if(strings!=null && strings.length>=1){
			strings[0] = titleStrings.trim() + " "+ getChangeStatusText();
		}
		
		super.setText(strings);
	}
	
	
    
    @Override
	public void setText(int index, String string) {
    	if(index==0){
    		String titleString = null;
			titleString = getFirstText(new String[]{string});
		
			if(titleString!=null && titleString.length()>=1){
				string = titleString.trim() + " "+ getChangeStatusText();
			}
    	}
		super.setText(index, string);
	}

	@Override
	public void setText(String string) {
		
		String titleString = null;
		titleString = getFirstText(new String[]{string});
	
		if(titleString!=null && titleString.length()>=1){
			string = titleString.trim() + " "+ getChangeStatusText();
		}
		
		super.setText(string);
	}

	private String getFirstText(String[] strings){
    	
		String titleString = null;
		
		if(strings!=null && strings.length>0 && strings[0]!=null){
			titleString = strings[0].trim();
		}else{
			String tempStr = getText(0);
			if(tempStr!=null && tempStr.trim().length()>0){
				titleString = tempStr.trim(); 
			}
		}
		
		String tempStringA = null;
		
		if(titleString!=null){
			tempStringA = titleString.trim();
		}
		
		int keyIndex = tempStringA.trim().lastIndexOf("[");
		
		if(keyIndex>-1){
			tempStringA = titleString.trim().substring(0, (keyIndex-1));
			
			if(tempStringA!=null){
				tempStringA= tempStringA.trim();
			}else{
				tempStringA = "";
			}
		}
		
		return tempStringA;
    }
    
    private String getChangeStatusText(){
    	
    	String changeStatusStr = "["
    			+changeInformationFlags[0]+" "
    			+changeInformationFlags[1]+" "
    			+changeInformationFlags[2]+" "
    			+changeInformationFlags[3]+" "
    			+changeInformationFlags[4]+" "
    			+changeInformationFlags[5]+" "
    			+changeInformationFlags[6]+" "
    			+changeInformationFlags[7]+" "
    			+changeInformationFlags[8]+"]";
    	
    	return changeStatusStr;
    }
    
    /**
     * @return the bopBomLine
     */
    public TCComponentBOMLine getBopBomLine() {
        return bopBomLine;
    }

    /**
     * @param bopBomLine
     *            the bomLine to set
     */
    public void setBopBomLine(TCComponentBOMLine bopBomLine) {
        this.bopBomLine = bopBomLine;
    }
    
    public Node getBomLineNode() {
		return bomLineNode;
	}

	public void setBomLineNode(Node bomLineNode) {
		this.bomLineNode = bomLineNode;
	}

	public Node getMasterDataNode() {
		return masterDataNode;
	}

	public void setMasterDataNode(Node masterDataNode) {
		this.masterDataNode = masterDataNode;
	}
    
    public boolean isHaveMajorError() {
		return haveMajorError;
	}

	public void setHaveMajorError(boolean haveMajorError) {
		this.haveMajorError = haveMajorError;
		if(this.haveMajorError==true){
			setStatus(TCData.STATUS_ERROR);
		}
	}
}
