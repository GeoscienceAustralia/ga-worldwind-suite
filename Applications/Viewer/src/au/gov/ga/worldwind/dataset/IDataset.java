package au.gov.ga.worldwind.dataset;

import java.util.List;

import au.gov.ga.worldwind.components.lazytree.ITreeObject;

public interface IDataset extends IData, ITreeObject
{
	public List<IDataset> getDatasets();

	public List<ILayerDefinition> getLayers();
}
