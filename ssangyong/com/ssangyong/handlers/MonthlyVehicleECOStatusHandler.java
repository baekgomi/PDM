package com.ssangyong.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ssangyong.admin.ecoadmincheck.MonthlyVehicleECOStatusDialog;

public class MonthlyVehicleECOStatusHandler extends AbstractHandler
{

	public MonthlyVehicleECOStatusHandler()
	{
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final Shell shell = HandlerUtil.getActiveShell(event);
		MonthlyVehicleECOStatusDialog monthlyVehicleECOStatusDialog = new MonthlyVehicleECOStatusDialog(shell);
		monthlyVehicleECOStatusDialog.open();
		return null;
	}

}
