package au.gov.ga.worldwind.common.ui.lazytree;

/**
 * Represents a tree node that has a loading state. The tree should display the
 * loading state of this node in some fashion.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILoadingNode
{
	/**
	 * @return Is this tree node loading?
	 */
	boolean isLoading();
}
