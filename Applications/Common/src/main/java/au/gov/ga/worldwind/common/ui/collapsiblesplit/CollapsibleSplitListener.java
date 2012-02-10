package au.gov.ga.worldwind.common.ui.collapsiblesplit;

/**
 * The listener interface for receiving events from the component
 * {@link LayoutPlaceholder}s in the {@link CollapsibleSplitLayout}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface CollapsibleSplitListener
{
	/**
	 * Called when the component's resize weight changes.
	 * 
	 * @param weight
	 */
	public void weightChanged(float weight);

	/**
	 * Called when the component's resizable property is toggled.
	 * 
	 * @param resizable
	 */
	public void resizableToggled(boolean resizable);

	/**
	 * Called when the component is expanded or collapsed.
	 * 
	 * @param expanded
	 */
	public void expandedToggled(boolean expanded);

	/**
	 * Blank implementation of the {@link CollapsibleSplitListener} interface.
	 */
	public class CollapsibleSplitAdapter implements CollapsibleSplitListener
	{
		@Override
		public void expandedToggled(boolean expanded)
		{
		}

		@Override
		public void resizableToggled(boolean resizable)
		{
		}

		@Override
		public void weightChanged(float weight)
		{
		}
	}
}
