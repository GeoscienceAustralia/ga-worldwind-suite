package au.gov.ga.worldwind.common.ui.collapsiblesplit;

/**
 * Represents a collapsible component.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ICollapsible
{
	/**
	 * Add a listener for collapse events to this object.
	 * 
	 * @param listener
	 */
	public void addCollapseListener(CollapseListener listener);

	/**
	 * Remove a listener for collapse events from this object.
	 * 
	 * @param listener
	 */
	public void removeCollapseListener(CollapseListener listener);

	/**
	 * @return Is this component is collapsed?
	 */
	public boolean isCollapsed();

	/**
	 * Collapse/expand this component.
	 * 
	 * @param collapsed
	 */
	public void setCollapsed(boolean collapsed);
}
