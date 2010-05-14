package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.gov.ga.worldwind.downloader.Downloader;
import au.gov.ga.worldwind.downloader.RetrievalHandler;
import au.gov.ga.worldwind.downloader.RetrievalResult;

public class LayerEnabler
{
	private LayerTree tree;

	private WorldWindow wwd;
	private LayerList layerList;
	private ExtendedCompoundElevationModel elevationModel;

	private List<ILayerNode> nodes = new ArrayList<ILayerNode>();
	private List<Wrapper> wrappers = new ArrayList<Wrapper>();
	private Map<ILayerNode, Wrapper> nodeMap = new HashMap<ILayerNode, Wrapper>();

	public LayerEnabler(WorldWindow wwd, LayerList layerList,
			ExtendedCompoundElevationModel elevationModel)
	{
		this.wwd = wwd;
		this.layerList = layerList;
		this.elevationModel = elevationModel;
	}

	public void setTree(LayerTree tree)
	{
		this.tree = tree;
	}

	//called by LayerTreeModel
	public synchronized void enable(List<ILayerNode> nodes)
	{
		//check if the node list has changed (if not, simply call refreshLists() to enable/disable layers)
		if (!nodes.equals(this.nodes))
		{
			//build a set of added nodes
			Set<ILayerNode> added = new HashSet<ILayerNode>(nodes);
			added.removeAll(this.nodes);

			if (!added.isEmpty() || nodes.size() != this.nodes.size())
			{
				//if any nodes have been added, or the node count is not the same (therefore some
				//may have been removed; calculate the removed set, and then refresh the nodeMap

				Set<ILayerNode> removed = new HashSet<ILayerNode>(this.nodes);
				removed.removeAll(nodes);

				for (ILayerNode remove : removed)
				{
					nodeMap.remove(remove);
				}
				for (ILayerNode add : added)
				{
					Wrapper wrapper = new Wrapper(add);
					nodeMap.put(add, wrapper);
				}
			}

			//set the global here, so that handleResult can find the index of it's node in the list
			this.nodes = nodes;

			//rebuild the wrappers list so that it contains wrappers in the same order as the nodes list
			wrappers.clear();
			for (ILayerNode node : nodes)
			{
				wrappers.add(nodeMap.get(node));

				//load the layer if it has been added in this refresh
				if (added.contains(node))
					loadLayer(node);
			}
		}

		//build the layer lists and redraw
		this.nodes = nodes;
		refreshLists();
	}

	private void loadLayer(final ILayerNode node)
	{
		URL url = node.getLayerURL();
		RetrievalHandler handler = new RetrievalHandler()
		{
			@Override
			public void handle(RetrievalResult result)
			{
				handleResult(node, result);
			}
		};
		setLayerLoading(node, true);
		Downloader.downloadIfModified(url, handler, handler);
	}

	private void setLayerLoading(ILayerNode node, boolean loading)
	{
		node.setLayerLoading(loading);
		if (tree != null)
			tree.repaint();
	}

	private synchronized void handleResult(ILayerNode node, RetrievalResult result)
	{
		if (result.getError() != null)
		{
			setLayerLoading(node, false);
			node.setError(result.getError());
			return;
		}

		if (!result.isFromCache())
			setLayerLoading(node, false);

		//data was not modified (already created layer from cache)
		if (result.isNotModified())
			return;

		if (!result.hasData())
		{
			//shouldn't get here
			node.setError(new Exception("Error downloading layer"));
			return;
		}

		//create a layer or elevation model from the downloaded result
		Object layer;
		try
		{
			layer = LayerLoader.load(result.getAsInputStream());
		}
		catch (Exception e)
		{
			node.setError(e);
			return;
		}
		if (layer == null)
			return;

		int index = nodes.indexOf(node);
		if (index < 0) //layer must have been removed during loading
			return;

		Wrapper wrapper = wrappers.get(index);
		if (layer instanceof Layer)
		{
			wrapper.setLayer((Layer) layer);
		}
		else if (layer instanceof ElevationModel)
		{
			wrapper.setElevationModel((ElevationModel) layer);
		}

		//must've been a download, so have to refresh the layer list
		if (!result.isFromCache())
			refreshLists();
	}

	private void refreshLists()
	{
		List<Layer> layers = new ArrayList<Layer>();
		List<ElevationModel> elevationModels = new ArrayList<ElevationModel>();
		for (Wrapper wrapper : wrappers)
		{
			if (wrapper.node.isEnabled())
			{
				if (wrapper.hasLayer())
				{
					Layer layer = wrapper.getLayer();
					layer.setEnabled(true);
					layers.add(layer);
				}
				else if (wrapper.hasElevationModel())
				{
					elevationModels.add(wrapper.getElevationModel());
				}
			}
		}

		//TODO instead of clearing layers and readding, only remove those that need to be removed,
		//and only add those that need to be added
		//should be easy, just cache those that were added last time

		layerList.clear();
		layerList.addAll(layers);

		elevationModel.clear();
		elevationModel.addAll(elevationModels);

		wwd.redraw();
	}

	private static class Wrapper
	{
		public final ILayerNode node;

		private Layer layer;
		private ElevationModel elevationModel;

		public Wrapper(ILayerNode node)
		{
			this.node = node;
		}

		public boolean hasLayer()
		{
			return layer != null;
		}

		public Layer getLayer()
		{
			return layer;
		}

		public void setLayer(Layer layer)
		{
			this.layer = layer;
			if (layer != null)
			{
				setElevationModel(null);
			}
		}

		public boolean hasElevationModel()
		{
			return elevationModel != null;
		}

		public ElevationModel getElevationModel()
		{
			return elevationModel;
		}

		public void setElevationModel(ElevationModel elevationModel)
		{
			this.elevationModel = elevationModel;
			if (elevationModel != null)
			{
				setLayer(null);
			}
		}
	}
}
