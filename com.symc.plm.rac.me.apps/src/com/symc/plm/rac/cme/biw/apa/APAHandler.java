package com.symc.plm.rac.cme.biw.apa;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.symc.plm.me.utils.BOPStructureDataUtility;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.AbstractAIFSession;
import com.teamcenter.rac.aifrcp.AIFUtility;
//import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;

// Referenced classes of package com.teamcenter.rac.cme.biw.apa:
//            APADialog


/**
 * [NON-SR][20150610] shcho, �������� �����ؼ� �۾��� ������ �߻��ϴ� ���� ���� (TC9������ ������ ���ý� ������ �������� TC10���� ���� �߻�)
 * 
 *
 */
public class APAHandler extends AbstractHandler
    implements IExecutableExtension
{
	public String findKey = null;
	
    public APAHandler()
    {
    }

    @SuppressWarnings("rawtypes")
	public Object execute(ExecutionEvent executionevent)
        throws ExecutionException
    {
        final TCComponentBOMLine selectedTarget;
        final AbstractAIFSession session;
        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();

        APAContentProvider getConProvider = new APAContentProvider(apaDialog);

        session = abstractaifuiapplication.getSession();
        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();
//        boolean flag = false;
        boolean viewFlag = false;
//        boolean occurrenceFlag = false;
        boolean pertCountFlag = true;
        boolean pertErrorCountFlag = true;
        boolean pertListFlag = false;
        String unPERTInformatikonStr = null;
        
        int stationCount = 0;
        ArrayList<HashMap<String, Object>> pertResult = new ArrayList<HashMap<String, Object>>();
        ArrayList<HashMap<String, Object>> pertDBResult = new ArrayList<HashMap<String, Object>>();
        if(aaifcomponentcontext != null && aaifcomponentcontext.length == 1 && (aaifcomponentcontext[0].getComponent() instanceof TCComponentBOMLine))
        {
            selectedTarget = (TCComponentBOMLine)aaifcomponentcontext[0].getComponent();
            // jwlee ������ BOP ������ E-BOM ���� ���� ������
            rootBOMLine = getConProvider.getBOMLine(selectedTarget);

            if (rootBOMLine != null){
                viewFlag = true;
            }
            
    		if(selectedTarget!=null){

    			//[NON-SR][20160128] Taeku.jeong Connected Part ������Ȯ�ϴ� ���� Query�� �ʹ� ������
        		// �ٸ� ������� Ȯ�� �ϴ� Query�� �����ϱ����� �Ʒ��� �κ��� �߰� �Ѵ�.
    			// [END] PERT ���� Ȯ�� ��� ���� �߰� ---- [20160128]
    			if(selectedTarget instanceof TCComponentBOMLine){
                	BOPStructureDataUtility aBOPStructureDataUtility = new BOPStructureDataUtility();
                	try {
						aBOPStructureDataUtility.deleteOldShopStructureData();
					} catch (Exception e) {
						e.printStackTrace();
					}

                	// BOM Line������ Ȯ�� �Ѵ�. // ���õ� Bomline�� Shop, Line, Station ������ ����
                	aBOPStructureDataUtility.initBOMLineData((TCComponentBOMLine)selectedTarget);

                	String shopItemId = aBOPStructureDataUtility.getShopId();
                	String lineItemId = aBOPStructureDataUtility.getLineId();
                	String stationItemId = aBOPStructureDataUtility.getStationId();
                	
                	// BOP Structure Data�� �����Ѵ�.
                	String findKey = null;
                	try {
                		//2020-09-02 seho ������ �� ������ �ʿ��Ͽ� ������. ���⼭�� �׳� latest revision����.. 
						findKey = aBOPStructureDataUtility.makeNewBOPInformationData(shopItemId, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
                	
                	if(findKey!=null){
                		
                		int predecessorStationCount = 0;
                		int allStationCount = aBOPStructureDataUtility.getAllStationCount(findKey);
                		if(stationItemId!=null){
                			List<HashMap> allPredecessorStationList = aBOPStructureDataUtility.getPredecessorStationsAtLine(findKey, stationItemId);
                			if(allPredecessorStationList!=null){
                				predecessorStationCount = allPredecessorStationList.size();
                			}
                		}
                		
                		Vector<String> predecessorLinesIdV = new Vector<String>();
                		List<HashMap> predecessorLines = aBOPStructureDataUtility.getPredecessorLines(findKey, lineItemId);
                		if(predecessorLines!=null){
                			for (int i = 0; predecessorLines!=null && i < predecessorLines.size(); i++) {
                				HashMap tempHashMap = predecessorLines.get(i);
                				// LINE_ID
                				String tempLineId = (String)tempHashMap.get("LINE_ID");
                				if(tempLineId!=null && 
                						tempLineId.trim().length()>0 && 
                						predecessorLinesIdV.contains(tempLineId.trim())==false){
                					predecessorLinesIdV.add(tempLineId.trim());
                				}
							}
                		}
                		List<HashMap> notPERTedList = aBOPStructureDataUtility.getUnPertedStationList(findKey);
                		int includedCurrentLine = 0;
                		if(notPERTedList!=null){
                			// PERT ������� ���� Station�� ���� Line�� ����  Connected PART�� ���� �Ϸ��� �ϴ� Line��
                			// ������ �ִ��� Ȯ�� �ؾ� �Ѵ�.
                			for (int i = 0;notPERTedList!=null && i < notPERTedList.size(); i++) {
                				HashMap aHashMap = notPERTedList.get(i);
                				//PARENT_ID, CHILD_ID, PARENT_APP_PATH_NODE_PUID, APP_NODE_PUID
                				String tempLineId = (String)aHashMap.get("PARENT_ID");
                				String tempStationId = (String)aHashMap.get("CHILD_ID");
                				if(tempLineId!=null && tempLineId.trim().equalsIgnoreCase(lineItemId.trim())==true){
                					includedCurrentLine++;
                					if(unPERTInformatikonStr==null){
                						unPERTInformatikonStr = tempLineId +" <-> "+tempStationId;
                					}else{
                						unPERTInformatikonStr = unPERTInformatikonStr+"\n" +tempLineId +" <-> "+tempStationId;
                					}
                				}else if(predecessorLinesIdV.contains(tempLineId)==true) {
                					includedCurrentLine++;
                					if(unPERTInformatikonStr==null){
                						unPERTInformatikonStr = tempLineId +" <-> "+tempStationId;
                					}else{
                						unPERTInformatikonStr = unPERTInformatikonStr+"\n" +tempLineId +" <-> "+tempStationId;
                					}
                				}
							}
                			// �̰� ���߿� Warning ������ �ʿ��ҵ�.
                			int notPERTedListSize = notPERTedList.size();
                		}
                		
                		// Structure�� ������ Station�� ���� ���
                		if(allStationCount < 1){
                			pertCountFlag = false;
                		}
                		// PERT �������� ���� Data�� Connected Part �����Ϸ��� Station��
                		// PERT ���� ��ο� ���Ե� ���
                		if(includedCurrentLine>0){
                			pertErrorCountFlag = false;
                		}
                		
                	}
                	
    			}
    			// [END] PERT ���� Ȯ�� ��� ���� �߰� ---- [20160128]
    			
    		}
    		
            /**
             *  jwlee PERT ������ ���������� �ԷµǾ� �ִ��� Ȯ���Ѵ�
             *  ���� 1 : PERT ������� ������ ���� ������ BOP �� ���� ���� ��
             *  ���� 2 : ������ �ԷµǾ� �ִ� ���� ������ DB �� ����Ǿ� �ִ� PERT ���� ��
             */

    		// [NON-SR][20160128] Taeku.jeong Connected Part ������Ȯ�ϴ� ���� Query�� �ʹ� ������
    		// �ٸ� ������� Ȯ�� �ϴ� Query�� �����ϱ����� �Ʒ��� �κ��� Remark �Ѵ�.
    		// Remark Start --------- [20160128]
    		/*
            try {
                stationCount = getConProvider.getSelectBopStationCount(selectedTarget.window().getTopBOMLine());
                pertResult = getConProvider.getSelectBopStationPertCountList(selectedTarget.window().getTopBOMLine());
                pertDBResult = getConProvider.getSelectBopStationDecessorsList(selectedTarget.window().getTopBOMLine());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((stationCount == 0 || pertResult.size() == 0) || (stationCount != pertResult.size())) {
                pertCountFlag = false;
            }

            */
    		// Remark End --------- [20160128]
            
            // jwlee ������ BOP ������ OccurrenceGroup �� �����Ǿ� �ִ��� üũ�Ѵ�
//            try {
//                occurrenceFlag = getConProvider.occurrenceGroupCheck(selectedTarget);
//            } catch (TCException e) {
//                e.printStackTrace();
//            }


//            MFGStructureType mfgstructuretype = MFGStructureTypeUtil.getStructureType(selectedTarget);
//            if(mfgstructuretype.isProduct() || mfgstructuretype.isProcess() || mfgstructuretype.isOperation()){
//                flag = true;
//            }  
            //[NON-SR][20150610] shcho, �������� �����ؼ� �۾��� ������ �߻��ϴ� ���� ���� (TC9������ ������ ���ý� ������ �������� TC10���� ���� �߻�)
//            if(MFGStructureTypeUtil.isProduct(selectedTarget) || MFGStructureTypeUtil.isProcess(selectedTarget) || MFGStructureTypeUtil.isOperation(selectedTarget)) {
//            	flag = true;
//            }
        }
        else
        {
            selectedTarget = null;
        }
        //[NON-SR][20150610] shcho, �������� �����ؼ� �۾��� ������ �߻��ϴ� ���� ���� (TC9������ ������ ���ý� ������ �������� TC10���� ���� �߻�)
//        if(!flag)
//        {
//            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
//            MessageBox.post(registry.getString("wrongSelection.MESSAGE"), registry.getString("wrongSelection.TITLE"), 1);
//            return null;
//        }
        if(!viewFlag)
        {
            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
            MessageBox.post(registry.getString("wrongNoOpenBOMView.MESSAGE"), registry.getString("wrongSelection.TITLE"), 1);
            return null;
        }
        if (!pertCountFlag) {
            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
            MessageBox.post(registry.getString("wrongNotPertInfo.MESSAGE"), registry.getString("wrongSelection.TITLE"), 1);
            return null;
        }
        
        // PERT ������ Successors �� ���� ������ 1�� �̻����� üũ�Ѵ�
        //if (!getConProvider.getPertNotHaveSuccessorsCount(pertResult)) {
        // [NON-SR][20160128] Taeku.jeong Connected Part ������ ���� ��λ�
        // PERT ������ ������ Data�� �ִ��� Ȯ�� �ϰ� ������ Data�� ���� �ش�.
        if(pertErrorCountFlag==false){
            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
            String messageStr = registry.getString("wrongNotPertInfo.MESSAGE")+"\n"
            		+unPERTInformatikonStr;
            String messageTitle = registry.getString("wrongSelection.TITLE");
            
            System.out.println(""+messageStr);
            MessageBox.post(messageStr, messageTitle, 1);
            return null;
        }
  
/*
        // PERT ������ Update �� �ʿ����� üũ �ʿ��ϴٸ� Update �ϰ� ����
        pertListFlag = getConProvider.comparePertInfo(pertResult, pertDBResult);
        if (!pertListFlag) {
            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
            Shell shell = AIFUtility.getActiveDesktop().getShell();
            int confirmRet = ConfirmDialog.prompt(shell, registry.getString("Confirmation.TITLE", "Confirm"), registry.getString("BOPPertInfoUpdate.MESSAGE", "History information is also modified PERT. \n Do you want to modify?"));
            if (confirmRet == 2){
                try {
                    getConProvider.updatePertInfo(selectedTarget.window().getTopBOMLine(), pertDBResult);
                } catch (TCException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
*/        
//        if (!occurrenceFlag) {
//            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
//            MessageBox.post(registry.getString("wrongNotCreateOccurrenceGroup.MESSAGE"), registry.getString("wrongSelection.TITLE"), 1);
//            return null;
//        }
        try
        {
        	
/*        	
            Display display = Display.getDefault();
            display.asyncExec(new Runnable() {

                public void run()
                {
                    Shell shell = AIFUtility.getActiveDesktop().getShell();
                    apaDialog = new APADialog(shell, session, selectedTarget, rootBOMLine);
                    apaDialog.setContent();
                    if(apaDialog.isEmpty())
                        apaDialog.close();
                    else
                        apaDialog.open();
                }

                @SuppressWarnings("unused")
                final APAHandler this$0;


            {
                this$0 = APAHandler.this;
            }
            }
        );
*/
        	// [NON-SR][20160119] taeku.jeong Connected Part Dialog���� Connected Part �˻��� UI ����������� ��Ÿ���� ���� ���� 
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
            Shell shell = workbenchWindow.getShell();

            apaDialog = new APADialog(shell, session, selectedTarget, rootBOMLine);
            apaDialog.setContent();
            if(apaDialog.isEmpty()){
                apaDialog.close();
            }else{
                apaDialog.open();
            }
        }
        catch(Exception exception)
        {
            logger.error(exception.getClass().getName(), exception);
            MessageBox messagebox = new MessageBox(parentFrame, exception);
            messagebox.setModal(true);
            messagebox.setVisible(true);
        }
        return null;
    }

    public void setInitializationData(IConfigurationElement iconfigurationelement, String s, Object obj)
        throws CoreException
    {
        parentFrame = AIFDesktop.getActiveDesktop();
    }

    private static final Logger logger = Logger.getLogger(APAHandler.class);
    protected APADialog apaDialog;
    protected Frame parentFrame;
    protected TCComponentBOMLine rootBOMLine;
    protected Registry registry;

}
