package au.gov.ga.worldwind.dataset;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.components.lazytree.layertree.LazyTreeObjectNode;

public class Dataset implements IDataset
{
	private List<ILayerDefinition> layers = new ArrayList<ILayerDefinition>();
	private List<IDataset> datasets = new ArrayList<IDataset>();
	private String name;

	public Dataset(String name)
	{
		this.name = name;
	}

	@Override
	public List<IDataset> getDatasets()
	{
		return datasets;
	}

	@Override
	public List<ILayerDefinition> getLayers()
	{
		return layers;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public MutableTreeNode[] getChildren(DefaultTreeModel model)
	{
		MutableTreeNode[] array = new MutableTreeNode[datasets.size()
				+ layers.size()];
		int i = 0;
		for (IDataset dataset : datasets)
		{
			array[i++] = new LazyTreeObjectNode(dataset, model);
		}
		for (ILayerDefinition layer : layers)
		{
			array[i++] = new DefaultMutableTreeNode(layer, false);
		}
		return array;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
