package au.gov.ga.worldwind.components.collapsiblesplit;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public abstract class AbstractCollapsiblePanel extends JPanel implements ICollapsible
{
	private List<CollapseListener> listeners = new ArrayList<CollapseListener>();

	@Override
	public void addCollapseListener(CollapseListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeCollapseListener(CollapseListener listener)
	{
		listeners.remove(listener);
	}

	protected void notifyCollapseListeners(boolean collapsed)
	{
		for (CollapseListener listener : listeners)
			listener.collapseToggled(this, collapsed);
	}
}
