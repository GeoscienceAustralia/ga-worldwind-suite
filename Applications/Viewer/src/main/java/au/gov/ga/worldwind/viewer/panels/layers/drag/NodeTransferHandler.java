package au.gov.ga.worldwind.viewer.panels.layers.drag;

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.viewer.panels.dataset.LayerDefinition;
import au.gov.ga.worldwind.viewer.panels.layers.INode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTree;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreeModel;

/**
 * {@link TransferHandler} used to drag-and-drop layers from the dataset tree to
 * the layers tree. Also supports dragging files into the layers tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NodeTransferHandler extends TransferHandler
{
	private LayerTree layersTree;
	private JTree datasetTree;

	private JTree.DropLocation dropLocation;

	public NodeTransferHandler(LayerTree layersTree, JTree datasetTree)
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
			TreePath[] dragPaths = layersTree.getSelectionPaths();
			if (dragPaths != null)
			{
				t = new TreeTransferable(layersTree, dragPaths);
			}
		}
		else if (source == datasetTree)
		{
			TreePath[] dragPaths = datasetTree.getSelectionPaths();
			if (dragPaths != null)
			{
				List<TreePath> paths = new ArrayList<TreePath>();

				for (TreePath dragPath : dragPaths)
				{
					Object o = dragPath.getLastPathComponent();
					if (o != null && o instanceof DefaultMutableTreeNode)
					{
						Object uo = ((DefaultMutableTreeNode) o).getUserObject();
						if (uo != null && uo instanceof ILayerDefinition)
							paths.add(dragPath);
					}
				}

				if (!paths.isEmpty())
				{
					TreePath[] p = paths.toArray(new TreePath[paths.size()]);
					t = new TreeTransferable(datasetTree, p);
				}
			}
		}
		return t;
	}

	@Override
	public boolean canImport(TransferSupport support)
	{
		if (!isDataFlavorValid(support))
			return false;

		return support.getComponent() == layersTree;
	}

	@Override
	public boolean importData(TransferSupport support)
	{
		if (!isDataFlavorValid(support))
			return false;

		DropLocation dl = support.getDropLocation();
		if (!(dl instanceof JTree.DropLocation))
			return false;
		dropLocation = (JTree.DropLocation) dl;

		Transferable transferable = support.getTransferable();
		if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			try
			{
				LayerTreeModel model = layersTree.getLayerModel();

				List<?> files = (List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				int i = 0;
				for (Object o : files)
				{
					if (o instanceof File)
					{
						File file = (File) o;
						URL url = file.toURI().toURL();
						ILayerDefinition definition =
								new LayerDefinition(file.getName(), url, null, Icons.file.getURL(), true, false);
						INode node = LayerNode.createFromLayerDefinition(definition);
						addNodeToTree(dropLocation, model, node, false, i++);
					}
				}

				layersTree.getUI().relayout();
				dropLocation = null;
			}
			catch (Exception e)
			{
				return false;
			}
		}

		return true;
	}

	private boolean isDataFlavorValid(TransferSupport support)
	{
		//only support string flavor, or a file list (if a file has been dragged into the tree)
		return support.isDataFlavorSupported(DataFlavor.stringFlavor)
				|| support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		if (source == null || dropLocation == null || data == null || !(data instanceof TreeTransferable))
			return;

		TreeTransferable t = (TreeTransferable) data;
		LayerTreeModel model = layersTree.getLayerModel();

		Boolean moveIntoFolders = null;

		int i = 0;
		for (TreePath path : t.getPaths())
		{
			if (source == layersTree)
			{
				INode node = (INode) path.getLastPathComponent();
				addNodeToTree(dropLocation, model, node, true, i++);
			}
			else if (source == datasetTree)
			{
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (dmtn != null && dmtn.getUserObject() instanceof ILayerDefinition)
				{
					if (moveIntoFolders == null)
					{
						String message = "Would you like to include the folder hierarchy with the dragged layers?";
						int result =
								JOptionPane.showConfirmDialog(layersTree, message, "Include folders",
										JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						moveIntoFolders = result == JOptionPane.YES_OPTION;
					}

					ILayerDefinition definition = (ILayerDefinition) dmtn.getUserObject();
					if (moveIntoFolders)
					{
						layersTree.getLayerModel().addLayer(definition, path.getPath());
					}
					else
					{
						INode node = LayerNode.createFromLayerDefinition(definition);
						addNodeToTree(dropLocation, model, node, false, i++);
					}

					Rectangle bounds = datasetTree.getPathBounds(path);
					if (bounds != null)
						datasetTree.repaint(bounds);
				}
			}
		}

		layersTree.getUI().relayout();
		dropLocation = null;
	}

	private void addNodeToTree(JTree.DropLocation dropLocation, LayerTreeModel model, INode node,
			boolean alreadyInTree, int offset)
	{
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
			else
				index += offset;
		}

		if (alreadyInTree)
		{
			if (!noparent)
			{
				if (node == parent || nodeAncestorOf(node, parent))
					return;
				if (node.getParent() == parent && index > model.getIndexOfChild(parent, node))
					index--;
			}

			model.removeNodeFromParent(node, false);
		}

		if (noparent)
			model.addToRoot(node, true);
		else
			model.insertNodeInto(node, parent, index, true);
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
