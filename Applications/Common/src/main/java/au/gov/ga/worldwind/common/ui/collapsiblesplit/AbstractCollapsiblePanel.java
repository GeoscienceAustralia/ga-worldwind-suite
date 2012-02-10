package au.gov.ga.worldwind.common.ui.collapsiblesplit;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Abstract class from which collapsible panels inherit.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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

	/**
	 * Notify the collapse listeners that this panel has been
	 * collapsed/uncollapsed.
	 * 
	 * @param collapsed
	 *            True if this panel has been collapsed.
	 */
	protected void notifyCollapseListeners(boolean collapsed)
	{
		for (CollapseListener listener : listeners)
			listener.collapseToggled(this, collapsed);
	}
}
