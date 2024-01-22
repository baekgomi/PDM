package com.symc.plm.me.sdv.handler;

import java.lang.reflect.Constructor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aif.AbstractAIFCommand;

/**
 * Accountability Check Result �� ����ϴ� Command�� Handler
 * [SR150106-015] 20151023 taeku.jeong  Accountability Check ��� Excel�� Export ��� �߰�
 * @author Taeku
 *
 */
public class AccountabilityCheckResultExportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			
			String commandInfo = event.getCommand().getId();
			
			Class commandClass = Class.forName(commandInfo);
			
			Constructor constructor = commandClass.getConstructor() ;

			AbstractAIFCommand aifCommand = (AbstractAIFCommand) constructor.newInstance() ;
			aifCommand.executeModal();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
