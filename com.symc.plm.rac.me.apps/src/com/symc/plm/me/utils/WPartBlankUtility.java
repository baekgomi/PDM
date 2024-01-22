package com.symc.plm.me.utils;

import java.util.Vector;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.treetable.TreeTableNode;

/**
 * ���� ���� ���������� Visulaization�� �̿��Ҷ� Accumulated Part�� ���Ե� Part�߿� �������� ���Ե� Part��
 * ǥ�õ��� �ʵ��� �ϴ� ����� �ʿ��� �����ϴ� Class.
 * [NON-SR][20160218] taeku.jeong  
 * @author Taeku
 *
 */
public class WPartBlankUtility {
	
	CMEBOMTreeTable targetTreeTable = null;
	InterfaceAIFComponent[]  userSelectTargetComponents = null;
	Vector<TreeTableNode> expandForcedV = null;

	public WPartBlankUtility(){
		
		TCComponentBOMWindow selectedWindow = null;
    	MFGLegacyApplication mppApplication = null;
    	if(AIFUtility.getCurrentApplication()!=null && AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication){
    		mppApplication = (MFGLegacyApplication)AIFUtility.getCurrentApplication();
    		
    		this.userSelectTargetComponents = mppApplication.getTargetComponents();
    		if(userSelectTargetComponents!=null){
    			for (int i = 0; i < userSelectTargetComponents.length; i++) {
    				if(userSelectTargetComponents[i] instanceof TCComponentBOMLine){
    					try {
							selectedWindow = ((TCComponentBOMLine)userSelectTargetComponents[i]).window();
							if(selectedWindow!=null){
								break;
							}
						} catch (TCException e) {
							e.printStackTrace();
						}
    				}
				}
    		}
		}
    	
    	if(selectedWindow!=null){
    		AbstractViewableTreeTable[] viewableTreeTables = mppApplication.getViewableTreeTables();
    		for (int i = 0;viewableTreeTables!=null &&  i < viewableTreeTables.length; i++) {
    			
    			AbstractViewableTreeTable currentTreeTable = viewableTreeTables[i];
    			TCComponentBOMLine currentBOMLine = currentTreeTable.getBOMRoot();
    			if(currentBOMLine!=null){
    				TCComponentBOMWindow tempWindow = null;
					try {
						tempWindow = currentBOMLine.window();
					} catch (TCException e) {
						e.printStackTrace();
					}
    				if(tempWindow!=null && tempWindow.equals(selectedWindow)==true){
    					this.targetTreeTable = (CMEBOMTreeTable)currentTreeTable;
    					break;
    				}
    			}
    		}
    	}
    	
	}
	
	/**
	 * Graphics Panel�� Load�� ����߿� Itme Id�� W�� ���۵Ǵ� Part�� ã�Ƽ� Graphics(Visulaizatoin)��
	 * ǥ�� ���� �ʵ��� �ϴ� ó���� �����Ѵ�.
	 * �̰��� �������� ������ ���� Visulaizatoin���� Ȯ�� �ϴ� ������ �������� ������ �ʵ��� ��ġ��
	 * �ʿ��� �������� �Ҵ��ϴ� �۾��� �����ϴµ� �ʿ��� �����
	 * Visulaization������ Type���� Filtering�ϴ� ����� �ֱ�� ������ Item Id�� �������� Filtering �ϴ�
	 * ����� ���� ������ ������ �ۼ���.
	 * [NON-SR][20160218] taeku.jeong 
	 */
	public void doBlankStartWithItemIdIsW(){
		
		if(this.targetTreeTable==null){
			return;
		}
		
		Vector<TCComponentBOMLine> wPartBOMLineV = new Vector<TCComponentBOMLine>();
		// Treetable�� �������ų� ������ �������� ������ Viaulaizatoitn�� Load�� ��� Node�� List�Ѵ�.
		TreeTableNode[] allNodes = this.targetTreeTable.getAllNodes(BOMLineNode.class);
		for (int i = 0;allNodes!=null && i < allNodes.length; i++) {
			if(allNodes[i]!=null && allNodes[i] instanceof BOMLineNode){
				boolean isChecked = allNodes[i].getChecked();
				TCComponentBOMLine tempBOMLine = ((BOMLineNode) allNodes[i]).getBOMLine();
				
				try {
					String itemId = tempBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
					String itemType = tempBOMLine.getItem().getType();
					
					if(itemType!=null && itemType.trim().equalsIgnoreCase(SDVTypeConstant.EBOM_VEH_PART)==true){
						if(itemId.trim().toUpperCase().startsWith("W")==true){
							expend(allNodes[i]);
							wPartBOMLineV.add(tempBOMLine);
						}
					}
					
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}

		if(wPartBOMLineV!=null && wPartBOMLineV.size()>0){
			
			expandForcedV = new Vector<TreeTableNode>();
			
			TCComponentBOMLine[] targetBOMLines = new TCComponentBOMLine[wPartBOMLineV.size()];
			for (int i = 0; i < targetBOMLines.length; i++) {
				targetBOMLines[i] = wPartBOMLineV.get(i);
			}
			
			// Visualization���� ������ �ʵ��� �ؾ� �� ������ ������ �ʵ��� ó���ϴ�.
			targetTreeTable.blank(targetBOMLines);
			
			// ������ Tree�� �ٽ� ���´�.
			collapseNodes();
		}
	}
	
	/**
	 * Tree�� ������ ���� ���¿����� Visulaization�� ������ �ʵ��� UnChecked ���� �����Ƿ� �ش� Node�� ���������� �Ѵ�.
	 * @param node
	 */
	private void expend(TreeTableNode node){

		TreeTableNode parentNode = (TreeTableNode) node.getParent();
		boolean isParentNodeExpanded = parentNode.isNodeExpanded();
		if(isParentNodeExpanded==false){
			expend(parentNode);
		}else{
			node.expandNode();
			if(expandForcedV!=null && expandForcedV.contains(node)==false){
				expandForcedV.add(node);
			}
		}
	}
	
	private void collapseNodes(){
		
		// ��������� �����ؼ� ���� �غ��� ���°��� ������ ���� �ʴ´�.
		// ������ �Ƹ��� Graphics���� Blank Operation�� ����Ϸ� ���� �ʾƼ� 
		// ���� Operation�� ���� ���� �ʴ°����� ������.
		
//			for (int i = expandForcedV.size();expandForcedV!=null && i > 0 ; i--) {
//				int indexNo = i-1;
//				TreeTableNode node = expandForcedV.get(indexNo);  
//				if(node!=null) {
//					node.collapseNode();
//				}
//			}
		
//		for (int i = 0;expandForcedV!=null && i < expandForcedV.size() ; i++) {
//			TreeTableNode node = expandForcedV.get(i);  
//			if(node!=null) {
//				node.collapseNode();
//			}
//		}
			expandForcedV.clear();
			expandForcedV = null;
	}
}
