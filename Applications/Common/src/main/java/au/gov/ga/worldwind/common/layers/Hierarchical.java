package au.gov.ga.worldwind.common.layers;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.tree.TreeNode;

import java.util.ArrayList;

/**
 * This interface indicates that this layer contains a hierarchy of enableable
 * children. An example of this a KMLLayer which has children such as network
 * links or placemarks.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Hierarchical extends Layer
{
	void addHierarchicalListener(HierarchicalListener listener);

	void removeHierarchicalListener(HierarchicalListener listener);

	public static interface HierarchicalListener
	{
		void hierarchyChanged(Layer layer, TreeNode node);
	}

	/**
	 * Helper list containing a collection of {@link HierarchicalListener}s, for
	 * easy notification.
	 */
	public static class HierarchicalListenerList extends ArrayList<HierarchicalListener>
	{
		public void notifyListeners(Layer layer, TreeNode node)
		{
			for (int i = size() - 1; i >= 0; i--)
				get(i).hierarchyChanged(layer, node);
		}
	}
}
