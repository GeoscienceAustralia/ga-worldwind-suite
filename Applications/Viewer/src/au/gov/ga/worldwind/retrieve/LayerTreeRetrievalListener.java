package au.gov.ga.worldwind.retrieve;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.retrieve.Retriever;

import java.util.HashMap;
import java.util.Map;

import nasa.worldwind.retrieve.ExtendedRetrievalService.RetrievalListener;
import au.gov.ga.worldwind.panels.layers.ILayerNode;
import au.gov.ga.worldwind.panels.layers.LayerEnabler;

public class LayerTreeRetrievalListener implements RetrievalListener
{
	private final Map<Retriever, ILayerNode> retrievingNodes = new HashMap<Retriever, ILayerNode>();
	private final Map<ILayerNode, Integer> nodeRetrievingCount = new HashMap<ILayerNode, Integer>();
	private final LayerEnabler layerEnabler;

	public LayerTreeRetrievalListener(LayerEnabler layerEnabler)
	{
		this.layerEnabler = layerEnabler;
	}

	@Override
	public void beforeRetrieve(Retriever retriever)
	{
		Layer layer = RetrievalListenerHelper.getLayer(retriever);
		ILayerNode layerNode = null;
		if (layer != null)
		{
			layerNode = layerEnabler.getLayerNode(layer);
		}
		else
		{
			ElevationModel elevationModel = RetrievalListenerHelper.getElevationModel(retriever);
			if (elevationModel != null)
			{
				layerNode = layerEnabler.getLayerNode(elevationModel);
			}
		}

		if (layerNode != null)
		{
			synchronized (retrievingNodes)
			{
				retrievingNodes.put(retriever, layerNode);
				Integer count = nodeRetrievingCount.get(layerNode);
				count = count == null ? 1 : count + 1;
				nodeRetrievingCount.put(layerNode, count);

				layerNode.setLayerDataLoading(true);
				layerEnabler.getTree().repaint();

				System.out.println(layerNode.getName() + " = " + count);
			}
		}
	}

	@Override
	public void afterRetrieve(Retriever retriever)
	{
		ILayerNode layerNode;
		Integer count = null;

		synchronized (retrievingNodes)
		{
			layerNode = retrievingNodes.get(retriever);
			if (layerNode != null)
			{
				count = nodeRetrievingCount.get(layerNode);
				count = count == null ? 0 : count - 1;
				nodeRetrievingCount.put(layerNode, count);

				if (count <= 0)
				{
					retrievingNodes.remove(retriever);
					nodeRetrievingCount.remove(layerNode);

					layerNode.setLayerDataLoading(false);
					layerEnabler.getTree().repaint();
				}

				System.out.println(layerNode.getName() + " = " + count);
			}
		}
	}
}
