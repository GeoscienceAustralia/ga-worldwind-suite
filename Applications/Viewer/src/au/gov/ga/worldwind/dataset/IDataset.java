package au.gov.ga.worldwind.dataset;

import java.util.List;

import au.gov.ga.worldwind.components.lazytree.layertree.ITreeObject;

public interface IDataset extends ITreeObject
{
	public String getName();
	public List<IDataset> getDatasets();
	public List<ILayerDefinition> getLayers();
}
