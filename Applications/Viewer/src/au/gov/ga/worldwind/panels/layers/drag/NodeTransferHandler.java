package au.gov.ga.worldwind.panels.layers.drag;

import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.panels.layers.ClearableBasicTreeUI;
import au.gov.ga.worldwind.panels.layers.INode;
import au.gov.ga.worldwind.panels.layers.LayerNode;
import au.gov.ga.worldwind.panels.layers.LayerTreeModel;

public class NodeTransferHandler extends TransferHandler
{
	private JTree layersTree;
	private JTree datasetTree;

	private JTree.DropLocation dropLocation;

	public NodeTransferHandler(JTree layersTree, JTree datasetTree)
	{
		super();
		this.layersTree = layersTree;
		this.datasetTree = datasetTree;
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		if (c != null)
		{
			if (c == datasetTree)
				return TransferHandler.COPY;
			if (c == layersTree)
				return TransferHandler.MOVE;
		}
		return TransferHandler.NONE;
	}

	@Override
	protected Transferable createTransferable(JComponent source)
	{
		dropLocation = null;

		if (source == null)
			return null;

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

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		if (source == null || dropLocation == null || data == null
				|| !(data instanceof TreeTransferable))
			return;

		TreeTransferable t = (TreeTransferable) data;
		LayerTreeModel model = (LayerTreeModel) layersTree.getModel();

		TreePath p = dropLocation.getPath();
		boolean noparent = p == null;
		INode parent = null;
		int index = 0;

		if (!noparent)
		{
			parent = (INode) p.getLastPathComponent();
			index = dropLocation.getChildIndex();

			if (index < 0)
				index = parent.getChildCount();
		}

		for (TreePath path : t.getPaths())
		{
			if (source == layersTree)
			{
				INode move = (INode) path.getLastPathComponent();

				if (!noparent)
				{
					if (move == parent || nodeAncestorOf(move, parent))
						continue;
					if (move.getParent() == parent && index > model.getIndexOfChild(parent, move))
						index--;
				}

				model.removeNodeFromParent(move, false);
				if (noparent)
					model.addToRoot(move, true);
				else
					model.insertNodeInto(move, parent, index++, true);
			}
			else if (source == datasetTree)
			{
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (dmtn != null && dmtn.getUserObject() instanceof ILayerDefinition)
				{
					ILayerDefinition definition = (ILayerDefinition) dmtn.getUserObject();
					INode node = LayerNode.createFromLayerDefinition(definition);
					if (noparent)
						model.addToRoot(node, true);
					else
						model.insertNodeInto(node, parent, index, true);

					Rectangle bounds = datasetTree.getPathBounds(path);
					if (bounds != null)
						datasetTree.repaint(bounds);
				}
			}
		}

		((ClearableBasicTreeUI) layersTree.getUI()).relayout();

		dropLocation = null;
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
