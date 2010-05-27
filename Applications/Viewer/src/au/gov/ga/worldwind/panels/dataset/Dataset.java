package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.components.lazytree.LazyTreeObjectNode;

public class Dataset extends AbstractData implements IDataset
{
	private List<ILayerDefinition> layers = new ArrayList<ILayerDefinition>();
	private List<IDataset> datasets = new ArrayList<IDataset>();

	public Dataset(String name, URL infoURL, URL iconURL, boolean base)
	{
		super(name, infoURL, iconURL, base);
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
	public MutableTreeNode[] getChildren(DefaultTreeModel model)
	{
		MutableTreeNode[] array = new MutableTreeNode[datasets.size() + layers.size()];
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
}
