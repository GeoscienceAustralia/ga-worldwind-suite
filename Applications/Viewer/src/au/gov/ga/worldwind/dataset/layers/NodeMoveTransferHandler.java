package au.gov.ga.worldwind.dataset.layers;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.dataset.layers.LayerTreePersistance.NodeItem;

public class NodeMoveTransferHandler extends TransferHandler
{
	private NodeItem draggedNode;
	private TreePath dragPath;

	public NodeMoveTransferHandler()
	{
		super();
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		Transferable t = null;
		if (c instanceof JTree)
		{
			JTree tree = (JTree) c;
			t = new GenericTransferable(tree.getSelectionPaths());
			dragPath = tree.getSelectionPath();
			if (dragPath != null)
			{
				draggedNode = (NodeItem) dragPath.getLastPathComponent();
			}
		}
		return t;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		if (source instanceof JTree)
		{
			JTree tree = (JTree) source;
			LayerTreeModel model = (LayerTreeModel) tree.getModel();
			TreePath currentPath = tree.getSelectionPath();
			if (currentPath != null)
			{
				addNodes(currentPath, model, data);
			}
			else
			{
				insertNodes(tree, model, data);
			}
		}
		draggedNode = null;
		super.exportDone(source, data, action);
	}

	private void addNodes(TreePath targeted, LayerTreeModel model, Transferable data)
	{
		NodeItem target = (NodeItem) targeted.getLastPathComponent();
		try
		{
			TreePath[] movedPaths = (TreePath[]) data.getTransferData(DataFlavor.stringFlavor);
			for (TreePath moved : movedPaths)
			{
				NodeItem move = (NodeItem) moved.getLastPathComponent();
				if (move != target && !nodeAncestorOf(move, target) && move.getParent() != target)
				{
					model.removeNodeFromParent(move, moved);
					int index = target.getChildCount();
					model.insertNodeInto(move, target, index, targeted);
				}
			}
		}
		catch (UnsupportedFlavorException e)
		{
		}
		catch (IOException e)
		{
		}
	}

	private void insertNodes(JTree tree, LayerTreeModel model, Transferable data)
	{
		Point location = ((TreeDropTarget) tree.getDropTarget()).getMostRecentDragLocation();
		TreePath targeted = tree.getClosestPathForLocation(location.x, location.y);
		NodeItem sibling = (NodeItem) targeted.getLastPathComponent();
		NodeItem target = model.getParent(sibling);

		if (target == null) //must be the root, so don't move
			return;

		try
		{
			TreePath[] movedPaths = (TreePath[]) data.getTransferData(DataFlavor.stringFlavor);
			for (TreePath moved : movedPaths)
			{
				NodeItem move = (NodeItem) moved.getLastPathComponent();
				if (move != sibling && move != target && !nodeAncestorOf(move, target)
						&& move.getParent() != target)
				{
					model.removeNodeFromParent(move, moved);
					int index = model.getIndexOfChild(target, sibling);
					model.insertNodeInto(move, target, index, targeted);
				}
			}
		}
		catch (UnsupportedFlavorException e)
		{
		}
		catch (IOException e)
		{
		}
	}

	private boolean nodeAncestorOf(NodeItem ancestor, NodeItem child)
	{
		NodeItem parent = child.getParent();
		while (parent != null)
		{
			if (parent == ancestor)
				return true;
			parent = parent.getParent();
		}
		return false;
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		return TransferHandler.MOVE;
	}

	public BufferedImage getDragImage(JTree tree)
	{
		BufferedImage image = null;
		try
		{
			if (dragPath != null)
			{
				Rectangle pathBounds = tree.getPathBounds(dragPath);
				TreeCellRenderer r = tree.getCellRenderer();
				TreeModel m = tree.getModel();
				boolean nIsLeaf = m.isLeaf(dragPath.getLastPathComponent());
				JComponent lbl =
						(JComponent) r.getTreeCellRendererComponent(tree, draggedNode, false, tree
								.isExpanded(dragPath), nIsLeaf, 0, false);
				lbl.setBounds(pathBounds);
				image =
						new BufferedImage(lbl.getWidth(), lbl.getHeight(),
								java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
				Graphics2D graphics = image.createGraphics();

				/*graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
				graphics.fillRect(0, 0, lbl.getWidth(), lbl.getHeight());*/

				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
				lbl.setOpaque(false);
				lbl.paint(graphics);
				graphics.dispose();
			}
		}
		catch (RuntimeException re)
		{
		}
		return image;
	}
}
