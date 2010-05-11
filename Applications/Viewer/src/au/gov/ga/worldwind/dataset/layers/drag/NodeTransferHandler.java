package au.gov.ga.worldwind.dataset.layers.drag;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.dataset.ILayerDefinition;
import au.gov.ga.worldwind.dataset.layers.ClearableBasicTreeUI;
import au.gov.ga.worldwind.dataset.layers.INode;
import au.gov.ga.worldwind.dataset.layers.LayerNode;
import au.gov.ga.worldwind.dataset.layers.LayerTreeModel;

public class NodeTransferHandler extends TransferHandler
{
	private JTree layersTree;
	private JTree datasetTree;

	private JTree.DropLocation dropLocation;

	public NodeTransferHandler(JTree layersTree, JTree datasetTree)
	{
		super();
		this.datasetTree = datasetTree;
		this.layersTree = layersTree;
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		if (c == datasetTree)
			return TransferHandler.COPY;
		if (c == layersTree)
			return TransferHandler.MOVE;
		return TransferHandler.NONE;
	}

	@Override
	protected Transferable createTransferable(JComponent source)
	{
		dropLocation = null;

		TreeTransferable t = null;
		if (source == layersTree)
		{
			TreePath dragPath = layersTree.getSelectionPath();
			if (dragPath != null)
			{
				t = new TreeTransferable(layersTree, layersTree.getSelectionPaths());
			}
		}
		else if (source == datasetTree)
		{
			TreePath dragPath = datasetTree.getSelectionPath();
			if (dragPath != null)
			{
				DefaultMutableTreeNode node =
						(DefaultMutableTreeNode) dragPath.getLastPathComponent();
				Object o = node.getUserObject();
				if (o != null && o instanceof ILayerDefinition)
				{
					t = new TreeTransferable(datasetTree, dragPath);
				}
			}
		}
		return t;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		if (dropLocation == null || data == null || !(data instanceof TreeTransferable))
			return;

		TreeTransferable t = (TreeTransferable) data;
		LayerTreeModel model = (LayerTreeModel) layersTree.getModel();

		TreePath parentPath;
		INode parent = null;

		parentPath = dropLocation.getPath();
		parent = (INode) parentPath.getLastPathComponent();
		int index = dropLocation.getChildIndex();

		if (index < 0)
		{
			index = parent.getChildCount();
		}

		for (TreePath path : t.getPaths())
		{
			if (source == layersTree)
			{
				INode move = (INode) path.getLastPathComponent();

				if (move == parent || nodeAncestorOf(move, parent))
					continue;

				if (move.getParent() == parent)
				{
					if (index > model.getIndexOfChild(parent, move))
						index--;
				}

				model.removeNodeFromParent(move, path);
				model.insertNodeInto(move, parent, index, parentPath);
				index++;
			}
			else if (source == datasetTree)
			{
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (dmtn != null && dmtn.getUserObject() instanceof ILayerDefinition)
				{
					ILayerDefinition definition = (ILayerDefinition) dmtn.getUserObject();
					INode node = LayerNode.createFromLayerDefinition(definition);
					model.insertNodeInto(node, parent, index, parentPath);
				}
			}
		}

		if (layersTree.getUI() instanceof ClearableBasicTreeUI)
			((ClearableBasicTreeUI) layersTree.getUI()).relayout();

		dropLocation = null;
	}

	@Override
	public boolean canImport(TransferSupport support)
	{
		return support.getComponent() == layersTree;
	}

	@Override
	public boolean importData(TransferSupport support)
	{
		DropLocation dl = support.getDropLocation();
		if (dl instanceof JTree.DropLocation)
		{
			dropLocation = (JTree.DropLocation) dl;
			return true;
		}
		return false;
	}

	private boolean nodeAncestorOf(INode ancestor, INode child)
	{
		INode parent = child.getParent();
		while (parent != null)
		{
			if (parent == ancestor)
				return true;
			parent = parent.getParent();
		}
		return false;
	}
}
