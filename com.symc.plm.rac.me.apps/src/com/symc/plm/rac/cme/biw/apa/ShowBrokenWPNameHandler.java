package com.symc.plm.rac.cme.biw.apa;

import java.lang.reflect.Constructor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.symc.plm.rac.cme.biw.apa.search.ShowBrokenWPNameUtility;
import com.teamcenter.rac.aif.AbstractAIFCommand;

/**
 * [SR151207-041][20151215] taeku.jeong �������� ���������� Occurrence Name�� ã�Ƽ� ǥ�����ִ� �޴��߰�
 * @author Taeku
 *
 */
public class ShowBrokenWPNameHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ShowBrokenWPNameUtility aShowBrokenWPNameUtility = new ShowBrokenWPNameUtility();
		aShowBrokenWPNameUtility.findWPProductOccName();
		
//		try {
//			
//			String commandInfo = event.getCommand().getId();
//			
//			Class commandClass = Class.forName(commandInfo);
//			
//			Constructor constructor = commandClass.getConstructor() ;
//
//			AbstractAIFCommand aifCommand = (AbstractAIFCommand) constructor.newInstance() ;
//			aifCommand.executeModal();
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return null;
	}
}
