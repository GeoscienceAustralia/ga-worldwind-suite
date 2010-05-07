package au.gov.ga.worldwind.dataset.layers;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.dataset.layers.LayerTreePersistance.NodeItem;

public class LayerTreeModel implements TreeModel
{
	public static LayerTreeModel loadFromXML(Object source) throws MalformedURLException
	{
		return new LayerTreeModel(LayerTreePersistance.readFromXML(source));
	}

	public void saveToXML(File output)
	{
		LayerTreePersistance.saveToXML(this.root, output);
	}

	private NodeItem root;
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

	public LayerTreeModel(NodeItem root)
	{
		this.root = root;
	}

	@Override
	public Object getChild(Object parent, int index)
	{
		NodeItem node = (NodeItem) parent;
		if (index < 0 || index >= node.getChildCount())
			return null;
		return node.getChild(index);
	}

	@Override
	public int getChildCount(Object parent)
	{
		NodeItem node = (NodeItem) parent;
		return node.getChildCount();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		NodeItem node = (NodeItem) parent;
		return node.getChildIndex(child);
	}

	@Override
	public NodeItem getRoot()
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
			NodeItem item = (NodeItem) path.getLastPathComponent();
			if (!item.getName().equals(s))
			{
				item.setName(s);
				TreeModelEvent e = new TreeModelEvent(this, path);
				for (TreeModelListener l : listeners)
					l.treeNodesChanged(e);
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

	public void removeNodeFromParent(NodeItem node, TreePath path)
	{
		NodeItem parent = node.getParent();
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
	}

	public void insertNodeInto(NodeItem moveNode, NodeItem targetNode, int index, TreePath path)
	{
		targetNode.insertChild(index, moveNode);

		int[] newIndexs = new int[1];
		newIndexs[0] = index;
		nodesWereInserted(targetNode, newIndexs);
	}

	public NodeItem getParent(NodeItem targetNode)
	{
		return targetNode.getParent();
	}

	//---------------------------------------------------------------------------------------
	//METHODS BELOW ARE FROM DefaultTreeModel
	//---------------------------------------------------------------------------------------

	private void nodesWereInserted(NodeItem node, int[] childIndices)
	{
		if (node != null && childIndices != null && childIndices.length > 0)
		{
			int cCount = childIndices.length;
			Object[] newChildren = new Object[cCount];

			for (int counter = 0; counter < cCount; counter++)
				newChildren[counter] = getChild(node, childIndices[counter]);
			fireTreeNodesInserted(this, getPathToRoot(node), childIndices, newChildren);
		}
	}

	private void nodesWereRemoved(NodeItem node, int[] childIndices, Object[] removedChildren)
	{
		if (node != null && childIndices != null)
		{
			fireTreeNodesRemoved(this, getPathToRoot(node), childIndices, removedChildren);
		}
	}

	private NodeItem[] getPathToRoot(NodeItem aNode)
	{
		return getPathToRoot(aNode, 0);
	}

	private NodeItem[] getPathToRoot(NodeItem aNode, int depth)
	{
		NodeItem[] nodes;
		// This method recurses, traversing towards the root in order
		// size the array. On the way back, it fills in the nodes,
		// starting from the root and working back to the original node.

		/* Check for null, in case someone passed in a null node, or
		   they passed in an element that isn't rooted at root. */
		if (aNode == null)
		{
			if (depth == 0)
				return null;
			else
				nodes = new NodeItem[depth];
		}
		else
		{
			depth++;
			if (aNode == root)
				nodes = new NodeItem[depth];
			else
				nodes = getPathToRoot(aNode.getParent(), depth);
			nodes[nodes.length - depth] = aNode;
		}
		return nodes;
	}

	private void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices,
			Object[] children)
	{
		TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
		for (TreeModelListener l : listeners)
			l.treeNodesInserted(e);
	}

	private void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices,
			Object[] children)
	{
		TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
		for (TreeModelListener l : listeners)
			l.treeNodesRemoved(e);
	}
}
