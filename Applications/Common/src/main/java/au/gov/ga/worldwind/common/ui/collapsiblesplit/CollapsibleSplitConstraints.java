package au.gov.ga.worldwind.common.ui.collapsiblesplit;

/**
 * Constraints used by the {@link CollapsibleSplitLayout}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CollapsibleSplitConstraints
{
	/**
	 * Weight of this {@link ICollapsible}. Used when resizing the collapsible's
	 * parent component to distribute extra height to the layout's child
	 * collapsibles.
	 */
	public float weight;

	/**
	 * Can this {@link ICollapsible} be resized? If not, it has a fixed height.
	 */
	public boolean resizable;

	/**
	 * Is this {@link ICollapsible} collapsed?
	 */
	public boolean collapsed;
}
