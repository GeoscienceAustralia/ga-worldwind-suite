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
package au.gov.ga.worldwind.viewer.panels.layers;

import static au.gov.ga.worldwind.viewer.util.Message.getMessage;
import static au.gov.ga.worldwind.viewer.util.message.ViewerMessageConstants.getTreeWmsRootNodeLabel;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import au.gov.ga.worldwind.common.util.Icons;

/**
 * Represents the root node for WMS layers in the layer panel.
 * <p/>
 * Contains convenience methods for adding WMS layers from the WMS browser etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class WmsRootNode extends FolderNode
{
	public WmsRootNode()
	{
		super(getMessage(getTreeWmsRootNodeLabel()), null, Icons.wmsbrowser.getURL(), true);
	}
	
	public WmsRootNode(String name, URL iconURL, boolean expanded)
	{
		super(name, null, iconURL, expanded);
	}

	/**
	 * Add the provided WMS layer to this node's subtree, creating appropriate
	 * {@link WmsServerNode}s if needed.
	 * 
	 * @return the added WMS Layer node
	 */
	public WmsLayerNode addWmsLayer(WMSLayerInfo layer)
	{
		if (layer == null)
		{
			return null;
		}
		
		WmsServerNode serverNode = findServerNodeForLayer(layer);
		if (serverNode == null)
		{
			serverNode = new WmsServerNode(layer.getCaps());
			addChild(serverNode);
		}
		
		WmsLayerNode wmsLayerNode = new WmsLayerNode(layer, true, 1.0);
		serverNode.addChild(wmsLayerNode);
		
		return wmsLayerNode;
	}
	
	private WmsServerNode findServerNodeForLayer(WMSLayerInfo layer)
	{
		// Perform a breadth-first search for the server node 
		// (it's most likely to be at the first level, if it exists)
		INode currentNode = null;
		Queue<INode> nodesToSearch = new ConcurrentLinkedQueue<INode>();
		nodesToSearch.add(this);
		
		while (!nodesToSearch.isEmpty())
		{
			currentNode = nodesToSearch.remove();
			
			if (currentNode instanceof WmsServerNode 
					&& ((WmsServerNode)currentNode).isOriginOf(layer))
			{
				return (WmsServerNode)currentNode;
			}
			
			nodesToSearch.addAll(currentNode.getChildren());
		}
		
		return null;
	}
	
}
