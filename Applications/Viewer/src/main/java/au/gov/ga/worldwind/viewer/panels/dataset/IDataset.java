package au.gov.ga.worldwind.viewer.panels.dataset;

import java.util.List;

import au.gov.ga.worldwind.common.ui.lazytree.ITreeObject;

/**
 * Interface representing a dataset. Datasets can contain child {@link IDataset}
 * s or child {@link ILayerDefinition}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDataset extends IData, ITreeObject
{
	/**
	 * @return List of children of this dataset
	 */
	List<IData> getChildren();

	/**
	 * Add a child to this dataset
	 * 
	 * @param child
	 *            Child to add
	 */
	void addChild(IData child);
}