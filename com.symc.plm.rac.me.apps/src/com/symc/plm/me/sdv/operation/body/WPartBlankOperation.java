package com.symc.plm.me.sdv.operation.body;

import java.util.Date;

import com.symc.plm.me.utils.WPartBlankUtility;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.cme.accountabilitycheck.Activator;
import com.teamcenter.rac.cme.accountabilitycheck.services.impl.AccountabilityCheckResultServiceImpl;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;


/**
 * WPartBlankUtility Command�� Operation
 * [NON-SR] [20160219] taeku.jeong  Visualizatoin�� Load�� Part�� W�� Item Id�� ���۵Ǵ°��� ã�Ƽ� Blank �����ִ� Operation Class
 * @author Taeku
 *
 */
public class WPartBlankOperation extends AbstractAIFOperation {

    private TCSession session;
    private TCComponentUser loginUser ;
    private TCComponentRole loginRole ;
    private TCComponentGroup loginGroup ;
    
    private AccountabilityCheckResultServiceImpl localAccountabilityCheckResultServiceImpl;
    
    @Override
    public void executeOperation() throws Exception {
    	
//    	this.session = (TCSession) Activator.getDefault().getSession();
//		this.loginUser = session.getUser();
//		this.loginRole = session.getRole();
//		this.loginGroup = session.getGroup();
		
        try {
            System.out.println("[" + new Date() + "]" + "WPartBlankUtility Start.");
            
            WPartBlankUtility aWPartBlankUtility = new WPartBlankUtility();
            aWPartBlankUtility.doBlankStartWithItemIdIsW();
            
        } catch(Exception e) {
        	System.out.println("[" + new Date() + "]" + "WPartBlankUtility Exception.");
            e.printStackTrace();
            throw e;
        } finally {
        	// Operation �Ϸ�...
        	System.out.println("[" + new Date() + "]" + "WPartBlankUtility Complete.");
        }
		
    }

	
}
