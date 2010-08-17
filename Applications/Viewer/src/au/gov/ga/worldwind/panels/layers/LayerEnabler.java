package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Sector;
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
import au.gov.ga.worldwind.util.Bounded;

public class LayerEnabler
{
	private LayerTree tree;

	private WorldWindow wwd;
	private SectionList<Layer> layerList;
	private SectionList<ElevationModel> elevationModel;

	private List<ILayerNode> nodes = new ArrayList<ILayerNode>();
	private List<Wrapper> wrappers = new ArrayList<Wrapper>();
	private Map<ILayerNode, Wrapper> nodeMap = new HashMap<ILayerNode, Wrapper>();

	private List<Layer> layers = new ArrayList<Layer>();
	private List<ElevationModel> elevationModels = new ArrayList<ElevationModel>();

	private List<RefreshListener> listeners = new ArrayList<RefreshListener>();

	public void setTree(LayerTree tree)
	{
		this.tree = tree;
	}

	public void addRefreshListener(RefreshListener listener)
	{
		listeners.add(listener);
	}

	public void removeRefreshListener(RefreshListener listener)
	{
		listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	public synchronized void setWwd(WorldWindow wwd)
	{
		this.wwd = wwd;

		LayerList ll = wwd.getModel().getLayers();
		if (ll instanceof SectionList<?>)
		{
			layerList = (SectionList<Layer>) ll;
		}
		else
		{
			throw new IllegalStateException("Model's layer list must implement SectionList<Layer>");
		}

		ElevationModel em = wwd.getModel().getGlobe().getElevationModel();
		if (em instanceof SectionList<?>)
		{
			elevationModel = (SectionList<ElevationModel>) em;
		}
		else
		{
			throw new IllegalStateException(
					"Globe's elevation model must implement SectionList<ElevationModel>");
		}

		layerList.registerSectionObject(this);
		elevationModel.registerSectionObject(this);

		refreshLists();
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
					loadLayer(node, true);
			}
		}

		//build the layer lists and redraw
		this.nodes = nodes;
		refreshLists();
	}

	public void reloadLayer(ILayerNode node)
	{
		loadLayer(node, false);
	}

	private void loadLayer(final ILayerNode node, boolean onlyIfModified)
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
		setLayerLoading(node, true, true);
		if (onlyIfModified)
			Downloader.downloadIfModified(url, handler, handler);
		else
			Downloader.download(url, handler, false);
	}

	private void setError(ILayerNode node, Exception error)
	{
		node.setError(error);
		if (tree != null)
		{
			tree.getUI().relayout();
			tree.repaint();
		}
	}

	private void setLayerLoading(ILayerNode node, boolean loading, boolean repaintTree)
	{
		node.setLayerLoading(loading);
		if (repaintTree && tree != null)
		{
			tree.repaint();
		}
	}

	private synchronized void handleResult(ILayerNode node, RetrievalResult result)
	{
		if (result.getError() != null)
		{
			setLayerLoading(node, false, false);
			setError(node, result.getError());
			return;
		}

		if (!result.isFromCache())
			setLayerLoading(node, false, true);

		//data was not modified (already created layer from cache)
		if (result.isNotModified())
			return;

		if (!result.hasData())
		{
			//shouldn't get here
			setError(node, new Exception("Error downloading layer"));
			return;
		}

		//create a layer or elevation model from the downloaded result
		LoadedLayer loaded;
		try
		{
			loaded = LayerLoader.load(result.getSourceURL(), result.getAsInputStream());
		}
		catch (Exception e)
		{
			setError(node, e);
			return;
		}
		if (loaded == null)
			return;

		int index = nodes.indexOf(node);
		if (index < 0) //layer must have been removed during loading
			return;

		Wrapper wrapper = wrappers.get(index);
		wrapper.setLoaded(loaded);

		//must've been a download, so have to refresh the layer list
		if (!result.isFromCache())
			refreshLists();
	}

	private void refreshLists()
	{
		//TODO instead of clearing layers and readding, only remove those that need to be removed,
		//and only add those that need to be added, and move those that need to be moved

		if (wwd == null)
			return;

		//remove all that we added last time
		layerList.removeAllFromSection(this, layers);
		elevationModel.removeAllFromSection(this, elevationModels);

		//clear the lists
		layers.clear();
		elevationModels.clear();

		//rebuild the lists
		for (Wrapper wrapper : wrappers)
		{
			if (wrapper.node.isEnabled())
			{
				if (wrapper.hasLayer())
				{
					Layer layer = wrapper.getLayer();
					layer.setEnabled(wrapper.node.isEnabled());
					layer.setOpacity(wrapper.node.getOpacity());
					layers.add(layer);
				}
				else if (wrapper.hasElevationModel())
				{
					ElevationModel elevationModel = wrapper.getElevationModel();
					elevationModels.add(elevationModel);
				}
			}

			if (wrapper.isLoaded())
			{
				wrapper.node.setLegendURL(wrapper.getLoaded().getLegendURL());
				wrapper.node.setQueryURL(wrapper.getLoaded().getQueryURL());

				wrapper.updateExpiryTime();
			}
		}

		layerList.addAllFromSection(this, layers);
		elevationModel.addAllFromSection(this, elevationModels);

		//tell the listeners that the list has been refreshed
		for (RefreshListener listener : listeners)
			listener.refreshed();

		//relayout and repaint the tree, as the labels may have changed (maybe legend button added)
		tree.getUI().relayout();
		tree.repaint();
	}

	public synchronized boolean hasLayer(ILayerNode node)
	{
		if (nodeMap.containsKey(node))
			return nodeMap.get(node).hasLayer();
		return false;
	}

	public synchronized Sector getLayerExtents(ILayerNode node)
	{
		if (!nodeMap.containsKey(node))
			return null;

		Wrapper wrapper = nodeMap.get(node);
		Object wrapped =
				wrapper.hasLayer() ? wrapper.getLayer() : wrapper.hasElevationModel() ? wrapper
						.getElevationModel() : null;

		return Bounded.Reader.getSector(wrapped);
	}

	private static class Wrapper
	{
		public final ILayerNode node;
		private LoadedLayer loaded;

		public Wrapper(ILayerNode node)
		{
			this.node = node;
		}

		public ElevationModel getElevationModel()
		{
			return loaded != null ? loaded.getElevationModel() : null;
		}

		public boolean hasElevationModel()
		{
			return loaded != null && loaded.isElevationModel();
		}

		public Layer getLayer()
		{
			return loaded != null ? loaded.getLayer() : null;
		}

		public boolean hasLayer()
		{
			return loaded != null && loaded.isLayer();
		}

		public boolean isLoaded()
		{
			return loaded != null;
		}

		public LoadedLayer getLoaded()
		{
			return loaded;
		}

		public void setLoaded(LoadedLayer loaded)
		{
			this.loaded = loaded;
		}

		public void updateExpiryTime()
		{
			if (node.getExpiryTime() != null)
			{
				if (useNodesExpiryTime())
				{
					if (hasLayer())
						getLayer().setExpiryTime(node.getExpiryTime());
					else if (hasElevationModel())
						getElevationModel().setExpiryTime(node.getExpiryTime());
				}
				else
				{
					node.setExpiryTime(null);
				}
			}
		}

		private boolean useNodesExpiryTime()
		{
			if (node.getExpiryTime() == null)
				return false;

			if (loaded.getParams() == null)
				return true;

			Object o = loaded.getParams().getValue(AVKey.EXPIRY_TIME);
			if (o == null || !(o instanceof Long))
				return true;

			Long l = (Long) o;
			if (l < node.getExpiryTime())
				return true;

			return false;
		}
	}

	public static interface RefreshListener
	{
		public void refreshed();
	}
}
