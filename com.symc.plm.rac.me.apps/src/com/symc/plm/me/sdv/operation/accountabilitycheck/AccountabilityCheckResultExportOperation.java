package com.symc.plm.me.sdv.operation.accountabilitycheck;

import java.util.Date;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.cme.accountabilitycheck.Activator;
import com.teamcenter.rac.cme.accountabilitycheck.services.impl.AccountabilityCheckResultServiceImpl;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;


/**
 * Accountability Check Result �� ����ϴ� Command�� Operation
 * [SR150106-015] 20151023 taeku.jeong  Accountability Check ��� Excel�� Export ��� �߰�
 * @author Taeku
 *
 */
public class AccountabilityCheckResultExportOperation extends AbstractAIFOperation {

    private TCSession session;
    private TCComponentUser loginUser ;
    private TCComponentRole loginRole ;
    private TCComponentGroup loginGroup ;
    
    private AccountabilityCheckResultServiceImpl localAccountabilityCheckResultServiceImpl;
    
    @Override
    public void executeOperation() throws Exception {
    	
    	this.session = (TCSession) Activator.getDefault().getSession();
		this.loginUser = session.getUser();
		this.loginRole = session.getRole();
		this.loginGroup = session.getGroup();
		
		System.out.println("loginUser = "+loginUser);
		System.out.println("loginRole = "+loginRole);
		System.out.println("loginGroup = "+loginGroup);
		
        try {
            System.out.println("[" + new Date() + "]" + "AccountabilityCheckResultExport started.");
            
            runAccountabilityCheckResultExport();

            System.out.println("[" + new Date() + "]" + "AccountabilityCheckResultExport was complete successfully.");
            
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[" + new Date() + "]" + "AccountabilityCheckResultExport was complete with exception.");
            throw e;
        } finally {
        	// Operation �Ϸ�...
        }
		
    }
    
    private void runAccountabilityCheckResultExport() throws Exception{
    	AccountabilityCheckResultExport accountabilityCheckResultExport = new AccountabilityCheckResultExport(session);
    	accountabilityCheckResultExport.runExportAction();
    }
	
}
