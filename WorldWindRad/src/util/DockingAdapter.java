package util;

import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.event.DockingListener;

public abstract class DockingAdapter implements DockingListener
{
	public void dockingCanceled(DockingEvent evt)
	{
	}

	public void dockingComplete(DockingEvent evt)
	{
	}

	public void dragStarted(DockingEvent evt)
	{
	}

	public void dropStarted(DockingEvent evt)
	{
	}

	public void undockingComplete(DockingEvent evt)
	{
	}

	public void undockingStarted(DockingEvent evt)
	{
	}
}
