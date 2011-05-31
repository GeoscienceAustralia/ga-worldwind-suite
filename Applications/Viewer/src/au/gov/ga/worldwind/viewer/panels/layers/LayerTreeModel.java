package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.viewer.panels.dataset.IData;
import au.gov.ga.worldwind.viewer.panels.dataset.IDataset;
import au.gov.ga.worldwind.viewer.panels.dataset.ILayerDefinition;

public class LayerTreeModel implements TreeModel, TreeExpansionListener
{
	private INode root;
	private LayerTree tree;
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	private List<ILayerNode> layerNodes = new ArrayList<ILayerNode>();
	private List<ILayerNode> invisibleLayers = new ArrayList<ILayerNode>();
	private Map<URL, Set<ILayerNode>> layerURLmap = new HashMap<URL, Set<ILayerNode>>();
	private LayerEnabler enabler;

	private WmsRootNode wmsRootFolderNode;
	
	public LayerTreeModel(LayerTree tree, INode root, LayerEnabler enabler)
	{
		this.tree = tree;
		this.root = root;
		this.enabler = enabler;
		addAnyLayers(root, true);
		findAndSetWmsRootFolderNode();
	}

	public void addWmsLayer(WMSLayerInfo layerInfo)
	{
		if (layerInfo == null)
		{
			return;
		}
		
		WmsRootNode wmsRootNode = getWmsRootNode();
		if (wmsRootNode == null)
		{
			wmsRootNode = createAndAddWmsRootNode();
		}
		WmsLayerNode wmsLayerNode = wmsRootNode.addWmsLayer(layerInfo);
		
		addAnyLayers(wmsLayerNode, true);
	}
	
	public void addToRoot(INode node, boolean refreshLayers)
	{
		insertNodeInto(node, root, root.getChildCount(), refreshLayers);
	}

	public void setEnabled(ILayerNode layer, boolean enabled)
	{
		if (layer.isEnabled() != enabled)
		{
			layer.setEnabled(enabled);
			refreshLayers();
		}
	}

	public boolean isEnabled(ILayerNode layer)
	{
		return layer.isEnabled();
	}

	public void setOpacity(ILayerNode layer, double opacity)
	{
		if (layer.getOpacity() != opacity)
		{
			layer.setOpacity(opacity);
			refreshLayers();
		}
	}

	public double getOpacity(ILayerNode layer)
	{
		return layer.getOpacity();
	}

	public void setExpiryTime(ILayerNode layer, Long expiryTime)
	{
		if (layer.getExpiryTime() != expiryTime)
		{
			layer.setExpiryTime(expiryTime);
			refreshLayers();
		}
	}

	public Long getExpiryTime(ILayerNode layer)
	{
		return layer.getExpiryTime();
	}

	private void refreshLayers()
	{
		List<ILayerNode> copy = new ArrayList<ILayerNode>(invisibleLayers);
		copy.addAll(layerNodes);
		enabler.enable(copy);
	}

	public void addInvisibleLayer(ILayerDefinition layer)
	{
		ILayerNode layerNode = LayerNode.createFromLayerDefinition(layer);
		invisibleLayers.add(layerNode);
		refreshLayers();
	}

	public void addLayer(ILayerDefinition layer, Object[] pathToRoot)
	{
		//convert the list of parents to a list of IData parents
		List<IData> parents = new ArrayList<IData>();
		if (pathToRoot != null)
		{
			for (Object o : pathToRoot)
			{
				if (o == null)
				{
					break;
				}

				IData data = null;
				if (o instanceof DefaultMutableTreeNode)
				{
					Object uo = ((DefaultMutableTreeNode) o).getUserObject();
					if (uo != null && uo instanceof IData)
					{
						data = (IData) uo;
					}
				}
				else if (o instanceof IData)
				{
					data = (IData) o;
				}
				if (data == null)
				{
					break;
				}

				if (data != layer)
				{
					parents.add(data);
				}
			}
		}
		addLayer(layer, parents);
	}

	public void addLayer(ILayerDefinition layer, List<IData> parents)
	{
		INode layerNode = LayerNode.createFromLayerDefinition(layer);
		List<INode> expandPath = new ArrayList<INode>();

		//add any parents of this layer that don't already exist in the tree
		INode currentParent = getRoot();
		IData directParent = null;
		if (parents != null)
		{
			if (!parents.isEmpty())
			{
				directParent = parents.get(parents.size() - 1);
			}

			//if the list contains any 'base' IData's, then start from that index
			int startIndex = 0;
			for (int i = parents.size() - 1; i >= 0; i--)
			{
				if (parents.get(i).isBase())
				{
					startIndex = i;
					break;
				}
			}

			for (int i = startIndex; i < parents.size(); i++)
			{
				IData data = parents.get(i);
				INode node = null;
				for (int j = 0; j < currentParent.getChildCount(); j++)
				{
					INode child = currentParent.getChild(j);
					if (sameFolder(data, child))
					{
						node = child;
						break;
					}
				}
				if (node == null)
				{
					node = new FolderNode(data.getName(), data.getInfoURL(), data.getIconURL(), true);
					insertNodeInto(node, currentParent, currentParent.getChildCount(), false);
				}
				expandPath.add(currentParent);
				currentParent = node;
			}
		}
		expandPath.add(currentParent);
		expandPath.add(layerNode);

		//attempt to insert the layer into the same position as it is defined in the dataset
		int index = currentParent.getChildCount();
		if (directParent != null && directParent instanceof IDataset)
		{
			IDataset dataset = (IDataset) directParent;
			int insertionIndex = findInsertionIndex(layer, dataset, currentParent);
			if (insertionIndex >= 0)
			{
				index = insertionIndex;
			}
		}

		//add the layer
		insertNodeInto(layerNode, currentParent, index, true);
		TreePath expand = new TreePath(expandPath.toArray());

		//relayout the tree, and expand to make the layer node visible
		tree.getUI().relayout(expand);
		tree.scrollPathToVisible(expand);
	}

	protected int findInsertionIndex(ILayerDefinition layer, IDataset directParent, INode currentParent)
	{
		List<ILayerDefinition> layers = new ArrayList<ILayerDefinition>();
		List<IData> children = directParent.getChildren();
		for (IData child : children)
		{
			if (child instanceof ILayerDefinition)
			{
				layers.add((ILayerDefinition) child);
			}
		}

		int indexOfChild = layers.indexOf(layer);
		if (indexOfChild >= 0)
		{
			for (int i = indexOfChild + 1; i < layers.size(); i++)
			{
				ILayerDefinition l = layers.get(i);
				int j = indexOfLayerName(l.getName(), currentParent);
				if (j >= 0)
				{
					return j;
				}
			}
			for (int i = indexOfChild - 1; i >= 0; i--)
			{
				ILayerDefinition l = layers.get(i);
				int j = indexOfLayerName(l.getName(), currentParent);
				if (j >= 0)
				{
					return j + 1;
				}
			}
		}
		return -1;
	}

	protected int indexOfLayerName(String layerName, INode parent)
	{
		for (int i = 0; i < parent.getChildCount(); i++)
		{
			if (layerName.equalsIgnoreCase(parent.getChild(i).getName()))
			{
				return i;
			}
		}
		return -1;
	}

	protected boolean sameFolder(IData data, INode node)
	{
		return data.getName() == node.getName()
				|| (data.getName() != null && data.getName().equalsIgnoreCase(node.getName()));
	}

	public void removeLayer(ILayerDefinition layer)
	{
		URL url = layer.getLayerURL();
		if (layerURLmap.containsKey(url))
		{
			Set<ILayerNode> set = layerURLmap.get(url);
			while (!set.isEmpty())
			{
				//get the layer node to remove
				ILayerNode layerNode = set.iterator().next();
				INode remove = layerNode;

				//go up the list of parents if the parents have this layer as their only child
				while (remove.getParent() != null && remove.getParent() != root && remove.getParent().getChildCount() == 1)
				{
					remove = remove.getParent();
				}

				//the following will call removedLayer() which will remove the node from the set
				//(therefore this is not an infinite loop)
				removeNodeFromParent(remove, false);
			}
			//only rebuild the layers list at the end
			rebuildLayersList();
		}
	}

	public boolean containsLayer(ILayerDefinition layer)
	{
		return layerURLmap.containsKey(layer.getLayerURL());
	}

	private void addAnyLayers(INode node, boolean rebuildLayersList)
	{
		boolean changed = addAnyLayersBelow(node);
		if (changed && rebuildLayersList)
		{
			rebuildLayersList();
		}
	}

	private void removeAnyLayers(INode node, boolean rebuildLayersList)
	{
		boolean changed = removeAnyLayersBelow(node);
		if (changed && rebuildLayersList)
		{
			rebuildLayersList();
		}
	}

	private boolean addAnyLayersBelow(INode node)
	{
		boolean changed = false;
		if (node instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) node;
			URL url = layer.getLayerURL();
			Set<ILayerNode> set;
			if (layerURLmap.containsKey(url))
			{
				set = layerURLmap.get(url);
			}
			else
			{
				set = new HashSet<ILayerNode>();
				layerURLmap.put(url, set);
			}
			set.add(layer);
			changed = true;
		}
		//call recursively for all node's children
		for (int i = 0; i < node.getChildCount(); i++)
			changed |= addAnyLayersBelow(node.getChild(i));

		return changed;
	}

	private boolean removeAnyLayersBelow(INode node)
	{
		boolean changed = false;
		if (node instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) node;
			URL url = layer.getLayerURL();
			if (layerURLmap.containsKey(url))
			{
				Set<ILayerNode> set = layerURLmap.get(url);
				set.remove(layer);
				if (set.isEmpty())
				{
					layerURLmap.remove(url);
				}
			}
			changed = true;
		}
		//call recursively for all node's children
		for (int i = 0; i < node.getChildCount(); i++)
		{
			changed |= removeAnyLayersBelow(node.getChild(i));
		}

		return changed;
	}

	private void rebuildLayersList()
	{
		synchronized (layerNodes)
		{
			layerNodes.clear();
			addLayersToLayerList(root, layerNodes);
			refreshLayers();
		}
	}

	private void addLayersToLayerList(INode node, List<ILayerNode> layerNodes)
	{
		if (node instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) node;
			layerNodes.add(layer);
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			addLayersToLayerList(node.getChild(i), layerNodes);
		}
	}

	@Override
	public Object getChild(Object parent, int index)
	{
		INode node = (INode) parent;
		if (index < 0 || index >= node.getChildCount())
		{
			return null;
		}
		return node.getChild(index);
	}

	@Override
	public int getChildCount(Object parent)
	{
		INode node = (INode) parent;
		return node.getChildCount();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		INode node = (INode) parent;
		return node.getChildIndex(child);
	}

	@Override
	public INode getRoot()
	{
		return root;
	}

	@Override
	public boolean isLeaf(Object node)
	{
		return getChildCount(node) == 0;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		if (newValue instanceof String)
		{
			String s = (String) newValue;
			INode item = (INode) path.getLastPathComponent();
			if (!item.getName().equals(s))
			{
				item.setName(s);
				nodeChanged(item);
			}
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		listeners.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		listeners.remove(l);
	}

	public void insertNodeInto(INode newNode, INode parentNode, int index, boolean rebuildLayersList)
	{
		parentNode.insertChild(index, newNode);

		int[] newIndexs = new int[1];
		newIndexs[0] = index;
		nodesWereInserted(parentNode, newIndexs);

		addAnyLayers(newNode, rebuildLayersList);
	}

	public void removeNodeFromParent(INode node, boolean rebuildLayersList)
	{
		INode parent = node.getParent();
		if (parent != null)
		{
			int index = getIndexOfChild(parent, node);
			parent.removeChild(node);

			int[] childIndex = new int[1];
			Object[] removedArray = new Object[1];

			childIndex[0] = index;
			removedArray[0] = node;
			nodesWereRemoved(parent, childIndex, removedArray);
		}

		removeAnyLayers(node, rebuildLayersList);
	}

	public INode getParent(INode targetNode)
	{
		return targetNode.getParent();
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event)
	{
		Object o = event.getPath().getLastPathComponent();
		INode node = (INode) o;
		node.setExpanded(false);
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event)
	{
		Object o = event.getPath().getLastPathComponent();
		INode node = (INode) o;
		node.setExpanded(true);
	}

	public void expandNodes()
	{
		List<INode> path = new ArrayList<INode>();
		path.add(root);
		expandIfRequired(tree, path);
	}

	private void expandIfRequired(JTree tree, List<INode> path)
	{
		INode last = path.get(path.size() - 1);
		if (last.isExpanded() && last.getChildCount() > 0)
		{
			TreePath tp = new TreePath(path.toArray());
			tree.expandPath(tp);

			for (int i = 0; i < last.getChildCount(); i++)
			{
				INode child = last.getChild(i);
				path.add(child);
				expandIfRequired(tree, path); //call recursively
				path.remove(path.size() - 1);
			}
		}
	}
	
	public WmsRootNode getWmsRootNode()
	{
		return wmsRootFolderNode;
	}
	
	public WmsRootNode createAndAddWmsRootNode()
	{
		if (wmsRootFolderNode != null)
		{
			return wmsRootFolderNode;
		}
		
		wmsRootFolderNode = new WmsRootNode();
		addToRoot(wmsRootFolderNode, false);
		return wmsRootFolderNode;
	}
	
	/** 
	 * Locate and update the WMS root folder from the current node tree
	 */
	private void findAndSetWmsRootFolderNode()
	{
		// Perform a breadth-first search for the WMS root node 
		// (it's most likely to be at the first level, if it exists)
		INode currentNode = null;
		Queue<INode> nodesToSearch = new ConcurrentLinkedQueue<INode>();
		nodesToSearch.add(root);
		
		while (!nodesToSearch.isEmpty())
		{
			currentNode = nodesToSearch.remove();
			
			if (currentNode instanceof WmsRootNode)
			{
				this.wmsRootFolderNode = (WmsRootNode)currentNode;
				return;
			}
			
			nodesToSearch.addAll(currentNode.getChildren());
		}
	}

	//----------------------------------------------------------------------------------//
	// METHODS BELOW ARE FROM DefaultTreeModel, adapted for the LayerTreeModel's INodes //
	//----------------------------------------------------------------------------------//

	private void nodeChanged(INode node)
	{
		if (node != null)
		{
			INode parent = node.getParent();

			if (parent != null)
			{
				int anIndex = getIndexOfChild(parent, node);
				if (anIndex != -1)
				{
					int[] cIndexs = new int[1];

					cIndexs[0] = anIndex;
					nodesChanged(parent, cIndexs);
				}
			}
			else if (node == getRoot())
			{
				nodesChanged(node, null);
			}
		}
	}

	private void nodesChanged(INode node, int[] childIndices)
	{
		if (node != null)
		{
			if (childIndices != null)
			{
				int cCount = childIndices.length;

				if (cCount > 0)
				{
					Object[] cChildren = new Object[cCount];

					for (int counter = 0; counter < cCount; counter++)
					{
						cChildren[counter] = getChild(node, childIndices[counter]);
					}
					fireTreeNodesChanged(this, getPathToRoot(node), childIndices, cChildren);
				}
			}
			else if (node == getRoot())
			{
				fireTreeNodesChanged(this, getPathToRoot(node), null, null);
			}
		}
	}

	private void nodesWereInserted(INode node, int[] childIndices)
	{
		if (node != null && childIndices != null && childIndices.length > 0)
		{
			int cCount = childIndices.length;
			Object[] newChildren = new Object[cCount];

			for (int counter = 0; counter < cCount; counter++)
			{
				newChildren[counter] = getChild(node, childIndices[counter]);
			}
			fireTreeNodesInserted(this, getPathToRoot(node), childIndices, newChildren);
		}
	}

	private void nodesWereRemoved(INode node, int[] childIndices, Object[] removedChildren)
	{
		if (node != null && childIndices != null)
		{
			fireTreeNodesRemoved(this, getPathToRoot(node), childIndices, removedChildren);
		}
	}

	public INode[] getPathToRoot(INode aNode)
	{
		return getPathToRoot(aNode, 0);
	}

	private INode[] getPathToRoot(INode aNode, int depth)
	{
		INode[] nodes;
		// This method recurses, traversing towards the root in order
		// size the array. On the way back, it fills in the nodes,
		// starting from the root and working back to the original node.

		/* Check for null, in case someone passed in a null node, or
		   they passed in an element that isn't rooted at root. */
		if (aNode == null)
		{
			if (depth == 0)
			{
				return null;
			}
			else
			{
				nodes = new INode[depth];
			}
		}
		else
		{
			depth++;
			if (aNode == root)
			{
				nodes = new INode[depth];
			}
			else
			{
				nodes = getPathToRoot(aNode.getParent(), depth);
			}
			nodes[nodes.length - depth] = aNode;
		}
		return nodes;
	}

	protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).treeNodesChanged(e);
		}
	}

	private void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).treeNodesInserted(e);
		}
	}

	private void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).treeNodesRemoved(e);
		}
	}
}
