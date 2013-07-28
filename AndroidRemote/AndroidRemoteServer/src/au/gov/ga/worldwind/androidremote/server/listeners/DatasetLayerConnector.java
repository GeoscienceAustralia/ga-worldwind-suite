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
package au.gov.ga.worldwind.androidremote.server.listeners;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.androidremote.server.listeners.tree.DatasetTreeListener;
import au.gov.ga.worldwind.androidremote.server.listeners.tree.LayerTreeListener;
import au.gov.ga.worldwind.androidremote.shared.Communicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.item.ItemMessage;
import au.gov.ga.worldwind.androidremote.shared.model.Item;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModel;
import au.gov.ga.worldwind.androidremote.shared.model.ItemModelListener;
import au.gov.ga.worldwind.viewer.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.viewer.panels.dataset.IData;
import au.gov.ga.worldwind.viewer.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.viewer.panels.layers.ILayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.INode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreeModel;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;

/**
 * Helper class that connects the {@link LayersPanel} and the
 * {@link DatasetPanel} to the dataset and layers {@link ItemModel}s. Uses
 * listeners on both the ItemModels and the panel's tree models to keep things
 * in sync.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetLayerConnector implements CommunicatorListener
{
	private final DatasetPanel datasetPanel;
	private final LayersPanel layersPanel;
	private final ItemModel datasetModel;
	private final ItemModel layersModel;
	private final DatasetTreeListener datasetTreeListener;
	private final LayerTreeListener layerTreeListener;
	private boolean listening = false;

	public DatasetLayerConnector(Communicator communicator, DatasetPanel datasetPanel, LayersPanel layersPanel)
	{
		this.datasetPanel = datasetPanel;
		this.layersPanel = layersPanel;

		datasetModel = new ItemModel(0, communicator);
		layersModel = new ItemModel(1, communicator);

		datasetModel.addListener(datasetItemModelListener);
		layersModel.addListener(layersItemModelListener);

		datasetTreeListener = new DatasetTreeListener(datasetModel, datasetPanel.getModel(), layersPanel.getModel());
		layerTreeListener = new LayerTreeListener(layersModel, layersPanel.getModel());
	}

	@Override
	public void stateChanged(State newState)
	{
		boolean connected = newState == State.CONNECTED;
		datasetTreeListener.clear();
		layerTreeListener.clear();
		if (connected)
		{
			datasetTreeListener.sendRefreshedMessage();
			layerTreeListener.sendRefreshedMessage();
		}
		listenToTreeModel(connected);
	}

	protected void listenToTreeModel(boolean listen)
	{
		if (listening != listen)
		{
			if (listen)
			{
				datasetPanel.getModel().addTreeModelListener(datasetTreeListener);
				layersPanel.getModel().addLayerTreeModelListener(layerTreeListener);
			}
			else
			{
				datasetPanel.getModel().removeTreeModelListener(datasetTreeListener);
				layersPanel.getModel().removeLayerTreeModelListener(layerTreeListener);
			}
			datasetTreeListener.setLayerTreeModelListenerEnabled(listen);
			listening = listen;
		}
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (message instanceof ItemMessage<?>)
		{
			ItemMessage<?> itemMessage = (ItemMessage<?>) message;
			//ItemModels check if the message is for them:
			datasetModel.handleMessage(itemMessage);
			layersModel.handleMessage(itemMessage);
		}
	}

	protected TreePath getTreePathForItem(TreeModel model, Item item)
	{
		return getTreePathForIndices(model, item.indicesToRoot());
	}

	protected TreePath getTreePathForIndices(TreeModel model, int[] treeIndexPath)
	{
		Object[] treePath = new Object[treeIndexPath.length + 1];
		treePath[0] = model.getRoot();
		for (int i = 0; i < treeIndexPath.length; i++)
		{
			treePath[i + 1] = model.getChild(treePath[i], treeIndexPath[i]);
		}
		return new TreePath(treePath);
	}

	private final ItemModelListener datasetItemModelListener = new ItemModelListener()
	{
		@Override
		public void itemsRefreshed(Item root, boolean remote)
		{
		}

		@Override
		public void itemsAdded(Item item, Item parent, int index, boolean remote)
		{
		}

		@Override
		public void itemRemoved(Item item, int[] oldIndicesToRoot, boolean remote)
		{
		}

		@Override
		public void itemEnabled(Item item, boolean enabled, boolean remote)
		{
		}

		@Override
		public void itemSelected(Item item, boolean remote)
		{
			JTree tree = datasetPanel.getTree();
			TreePath treePath = getTreePathForItem(tree.getModel(), item);
			tree.expandPath(treePath);

			ILayerDefinition layer = getLayerValue(getDataValue(treePath.getLastPathComponent()));
			if (layer != null)
			{
				LayerTreeModel layerTreeModel = layersPanel.getModel();
				if (layerTreeModel.containsLayer(layer))
					layerTreeModel.removeLayer(layer);
				else
					layerTreeModel.addLayer(layer, treePath.getPath());
			}
		}

		@Override
		public void itemOpacityChanged(Item item, float opacity, boolean remote)
		{
		}

		protected ILayerDefinition getLayerValue(IData value)
		{
			if (value != null && value instanceof ILayerDefinition)
				return (ILayerDefinition) value;
			return null;
		}

		protected IData getDataValue(Object value)
		{
			if (value != null && value instanceof DefaultMutableTreeNode)
			{
				Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
				if (userObject != null && userObject instanceof IData)
					return (IData) userObject;
			}
			return null;
		}
	};

	private final ItemModelListener layersItemModelListener = new ItemModelListener()
	{
		@Override
		public void itemsRefreshed(Item root, boolean remote)
		{
			//this will never be called from the Android client
		}

		@Override
		public void itemsAdded(Item item, Item parent, int index, boolean remote)
		{
			//at the moment this will never be called from the Android client
			//but may be at some point if layer movement is implemented
		}

		@Override
		public void itemRemoved(Item item, int[] oldIndicesToRoot, boolean remote)
		{
			if (remote)
			{
				INode node = getNode(oldIndicesToRoot);
				if (node != null)
				{
					layersPanel.deleteNode(node);
				}
			}
		}

		@Override
		public void itemSelected(Item item, boolean remote)
		{
			ILayerNode layer = getLayerNode(item);
			if (layer != null)
			{
				layersPanel.flyToLayer(layer);
			}
		}

		@Override
		public void itemOpacityChanged(Item item, float opacity, boolean remote)
		{
			ILayerNode layer = getLayerNode(item);
			if (layer != null)
			{
				layersPanel.setOpacity(layer, opacity);
			}
		}

		@Override
		public void itemEnabled(Item item, boolean enabled, boolean remote)
		{
			ILayerNode layer = getLayerNode(item);
			if (layer != null)
			{
				layersPanel.setEnabled(layer, enabled);
			}
		}

		private INode getNode(int[] indicesToRoot)
		{
			JTree tree = layersPanel.getTree();
			TreePath treePath = getTreePathForIndices(tree.getModel(), indicesToRoot);
			Object o = treePath.getLastPathComponent();
			return o instanceof INode ? (INode) o : null;
		}

		private ILayerNode getLayerNode(Item item)
		{
			JTree tree = layersPanel.getTree();
			TreePath treePath = getTreePathForItem(tree.getModel(), item);
			Object o = treePath.getLastPathComponent();
			return o instanceof ILayerNode ? (ILayerNode) o : null;
		}
	};
}
