package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.gov.ga.worldwind.downloader.Downloader;
import au.gov.ga.worldwind.downloader.RetrievalHandler;
import au.gov.ga.worldwind.downloader.RetrievalResult;

public class LayerEnabler
{
	private LayerList layerList;
	private CompoundElevationModel elevationModel;

	private List<ILayerNode> nodes = new ArrayList<ILayerNode>();
	private List<Wrapper> wrappers = new ArrayList<Wrapper>();

	public LayerEnabler(LayerList layerList, CompoundElevationModel elevationModel)
	{
		this.layerList = layerList;
		this.elevationModel = elevationModel;
	}

	//called by LayerTreeModel
	public synchronized void enable(List<ILayerNode> nodes)
	{
		System.out.println("enable() " + Arrays.toString(nodes.toArray()));

		if (nodes.equals(this.nodes))
		{
			//no layers have been removed, added, or moved position
			//simply enable/disable current layers
			this.nodes = nodes;
			refreshLists();
		}
		else
		{
			//TODO this can be implemented much more nicely!!
			//don't need to load everything everytime a single layer is added/moved/removed

			this.nodes = nodes;

			wrappers.clear();
			for (ILayerNode node : nodes)
			{
				wrappers.add(new PlaceholderWrapper(node));
				loadLayer(node);
			}

			refreshLists();
		}

		//check for any changes in list
		//if any removed, remove them (they may still be loading; ensure after load handler doesn't add it)

		//store a copy of the nodes list every time this function is called
		//then go through the list and call loadLayer for each
		//after the layer is loaded, find it's index in the copied list
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
		node.setLayerLoading(true);
		Downloader.downloadIfModified(url, handler, handler);
	}

	private synchronized void handleResult(ILayerNode node, RetrievalResult result)
	{
		if (result.getError() != null)
		{
			node.setLayerLoading(false);
			node.setError(result.getError());
			return;
		}

		if (!result.isFromCache())
			node.setLayerLoading(false);

		//data was not modified (already created layer from cache)
		if (result.isNotModified())
			return;

		if (!result.hasData())
		{
			//shouldn't get here
			node.setError(new Exception("Error"));
			return;
		}

		//create a layer or elevation model from the downloaded result
		Object layer = LayerLoader.load(result.getAsInputStream());
		if (layer == null)
			return;

		Wrapper wrapper = null;
		if (layer instanceof Layer)
		{
			wrapper = new LayerWrapper(node, (Layer) layer);
		}
		else if (layer instanceof ElevationModel)
		{
			wrapper = new ElevationModelWrapper(node, (ElevationModel) layer);
		}

		if (wrapper != null)
		{
			int index = nodes.indexOf(node);
			wrappers.set(index, wrapper);
			if (!result.isFromCache())
				refreshLists();
		}
	}

	private void refreshLists()
	{
		List<Layer> layers = new ArrayList<Layer>();
		List<ElevationModel> elevationModels = new ArrayList<ElevationModel>();
		for (Wrapper wrapper : wrappers)
		{
			if (wrapper.node.isEnabled()) //TODO rethink this?
			{
				if (wrapper instanceof LayerWrapper)
				{
					LayerWrapper lw = (LayerWrapper) wrapper;
					layers.add(lw.layer);
				}
				else if (wrapper instanceof ElevationModelWrapper)
				{
					ElevationModelWrapper emw = (ElevationModelWrapper) wrapper;
					elevationModels.add(emw.elevationModel);
				}
			}
		}

		//TODO instead of clearing layers and readding, only remove those that need to be removed,
		//and only add those that need to be added
		//should be easy, just cache those that were added last time

		layerList.clear();
		layerList.addAll(layers);

		while (!elevationModel.getElevationModels().isEmpty())
			elevationModel.removeElevationModel(0);
		for (ElevationModel em : elevationModels)
			elevationModel.addElevationModel(em);

		System.out.println("layerList = " + Arrays.toString(layerList.toArray()));
		System.out.println("elevationModel = "
				+ Arrays.toString(elevationModel.getElevationModels().toArray()));
	}

	private abstract static class Wrapper
	{
		public final ILayerNode node;

		public Wrapper(ILayerNode node)
		{
			this.node = node;
		}
	}

	private static class PlaceholderWrapper extends Wrapper
	{
		public PlaceholderWrapper(ILayerNode node)
		{
			super(node);
		}
	}

	private static class LayerWrapper extends Wrapper
	{
		public final Layer layer;

		public LayerWrapper(ILayerNode node, Layer layer)
		{
			super(node);
			this.layer = layer;
		}
	}

	private static class ElevationModelWrapper extends Wrapper
	{
		public final ElevationModel elevationModel;

		public ElevationModelWrapper(ILayerNode node, ElevationModel elevationModel)
		{
			super(node);
			this.elevationModel = elevationModel;
		}
	}
}
