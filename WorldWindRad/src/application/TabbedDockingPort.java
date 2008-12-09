package application;

import javax.swing.JTabbedPane;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.DockingStrategy;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.DefaultDockingStrategy;

public class TabbedDockingPort extends DefaultDockingPort
{
	private DockingStrategy dockingStrategy = new TabbedDockingStrategy();

	public TabbedDockingPort(String id)
	{
		super(id);
	}

	public boolean dock(Dockable dockable)
	{
		return dock(dockable, CENTER_REGION);
	}

	@Override
	protected JTabbedPane createTabbedPane()
	{
		JTabbedPane tabbedPane = super.createTabbedPane();
		return tabbedPane;
	}

	@Override
	public DockingStrategy getDockingStrategy()
	{
		return dockingStrategy;
	}

	private class TabbedDockingStrategy extends DefaultDockingStrategy
	{
		@Override
		protected DockingPort createDockingPortImpl(DockingPort base)
		{
			return new TabbedDockingPort(base.getPersistentId() + "_child");
		}
	}
}
