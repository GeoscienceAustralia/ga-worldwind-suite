package au.gov.ga.worldwind.common.ui.collapsiblesplit;

/**
 * The listener interface for receiving collapse toggle events from an
 * {@link ICollapsible} component.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface CollapseListener
{
	public void collapseToggled(ICollapsible component, boolean collapsed);
}
