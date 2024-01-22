package com.symc.plm.rac.prebom.prebom.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.symc.plm.rac.prebom.common.viewer.AbstractPreSYMCViewer;
import com.symc.plm.rac.prebom.prebom.view.preproject.PreProjectInfoPanel;
import com.teamcenter.rac.util.IPageComplete;

/**
 * Project Part ViewerTab���� ����ϴ� Rendering Class
 * 
 * ### ���� ��� #####
 * properties) viewer.properties => S7_PReProjectRevision.COMPONENTVIEWER=com.symc.plm.rac.prebom.prebom.rendering.SYMCPreProjectRevisionRendering
 * preference) defaultViewerConfig => S7_PreProjectRevision.SYMCPropertyViewer
 */
public class SYMCPreProjectRevisionRendering extends AbstractPreSYMCViewer implements IPageComplete
{

	private PreProjectInfoPanel infoPanel;

	public SYMCPreProjectRevisionRendering(Composite parent)
	{
		super(parent);
	}

	/**
	 * ȭ�� ����
	 */
	@Override
	public void createPanel(Composite parent)
	{
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		infoPanel = new PreProjectInfoPanel(sc, SWT.NONE, true);
		infoPanel.setViewMode();

		// Composite composite = infoPanel.getComposite();
		sc.setContent(infoPanel);
		sc.setMinSize(800, 600);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

	}

	/**
	 * ���� ���� Ȯ��
	 */
	@Override
	public boolean isDirty()
	{
		return infoPanel.isModified();
	}

	@Override
	public boolean isPageComplete()
	{

		return true;
	}

	@Override
	public void load()
	{

	}

	public void setControlReadWrite(Control control)
	{
		super.setControlReadWrite(control);
		infoPanel.setViewMode();
	}

	@Override
	public void save()
	{

		infoPanel.saveAction();
	}

	@Override
	public boolean isSavable()
	{

		return infoPanel.isSavable();
	}
}
