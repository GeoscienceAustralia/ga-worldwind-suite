/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.retrieve;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.retrieve.Retriever;

import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.common.retrieve.ExtendedRetrievalService;
import au.gov.ga.worldwind.common.retrieve.RetrievalListenerHelper;
import au.gov.ga.worldwind.common.retrieve.ExtendedRetrievalService.RetrievalListener;
import au.gov.ga.worldwind.viewer.panels.layers.ILayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerEnabler;

/**
 * {@link RetrievalListener} implementation used to mark layer tree nodes as
 * loading when the associated layer downloads a tile using the
 * {@link ExtendedRetrievalService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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
			}
		}
	}
}
