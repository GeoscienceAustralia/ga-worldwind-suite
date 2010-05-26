package au.gov.ga.worldwind.panels.layers;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.lazytree.ILazyTreeObject;
import au.gov.ga.worldwind.components.lazytree.LazyLoadListener;
import au.gov.ga.worldwind.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.panels.dataset.IData;
import au.gov.ga.worldwind.panels.dataset.IDataset;
import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.panels.dataset.ILazyDataset;
import au.gov.ga.worldwind.panels.layers.drag.NodeTransferHandler;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemePanel;
import au.gov.ga.worldwind.util.BasicAction;
import au.gov.ga.worldwind.util.Icons;
import au.gov.ga.worldwind.util.Util;

public class LayersPanel extends AbstractLayersPanel
{
	private static final String LAYERS_FILENAME = "layers.xml";
	private static final File layersFile = new File(Util.getUserDirectory(), LAYERS_FILENAME);
	private boolean layersFileExisted;

	private BasicAction newFolderAction, renameAction, deleteAction;

	private DatasetPanel datasetPanel;

	public LayersPanel()
	{
		super(new BorderLayout());
		setDisplayName("Layers");

		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				enableActions();
			}
		});

		enableActions();
		createPopupMenu();
	}

	@Override
	public Icon getIcon()
	{
		return Icons.hierarchy.getIcon();
	}

	@Override
	protected INode createRootNode()
	{
		INode root = null;
		try
		{
			root = LayerTreePersistance.readFromXML(layersFile);
			layersFileExisted = true;
		}
		catch (Exception e)
		{
		}
		if (root == null)
		{
			root = new FolderNode(null, null, true);
			layersFileExisted = false;
		}
		return root;
	}

	@Override
	protected void createActions()
	{
		newFolderAction =
				new BasicAction("New Folder", "Create New Folder", Icons.newfolder.getIcon());
		newFolderAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				newFolder();
			}
		});

		renameAction = new BasicAction("Rename", "Rename selected", Icons.edit.getIcon());
		renameAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				rename();
			}
		});

		deleteAction = new BasicAction("Delete", "Delete selected", Icons.deletevalue.getIcon());
		deleteAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				delete();
			}
		});
	}

	@Override
	protected void setupToolBarBeforeSlider(JToolBar toolBar)
	{
		toolBar.add(newFolderAction);
		toolBar.add(renameAction);
		toolBar.add(deleteAction);
		toolBar.addSeparator();
	}

	private void createPopupMenu()
	{
		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(renameAction);
		popupMenu.add(deleteAction);
		popupMenu.addSeparator();
		popupMenu.add(newFolderAction);

		tree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int row = tree.getRowForLocation(e.getX(), e.getY());
				if (row >= 0)
				{
					if (e.getButton() == MouseEvent.BUTTON3)
					{
						tree.setSelectionRow(row);
						popupMenu.show(tree, e.getX(), e.getY());
					}
				}
			}
		});
	}

	private void newFolder()
	{
		FolderNode node = new FolderNode("New Folder", Icons.folder.getURL(), true);
		TreePath p = tree.getSelectionPath();
		TreePath editPath;
		if (p == null)
		{
			getModel().addToRoot(node, false);
			editPath = new TreePath(new Object[] { root, node });
		}
		else
		{
			INode parent = (INode) p.getLastPathComponent();
			getModel().insertNodeInto(node, parent, parent.getChildCount(), false);
			editPath = p.pathByAddingChild(node);
		}
		tree.scrollPathToVisible(editPath);
		editAtPath(editPath);
	}

	private void rename()
	{
		TreePath p = tree.getSelectionPath();
		if (p != null)
		{
			editAtPath(p);
		}
	}

	private void delete()
	{
		TreePath p = tree.getSelectionPath();
		if (p != null)
		{
			INode node = (INode) p.getLastPathComponent();
			String type;
			if (node instanceof ILayerNode)
				type = "layer";
			else
				type = "folder";
			String children = "";
			if (node.getChildCount() > 0)
				children = " and its children";
			int choice =
					JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the "
							+ type + " '" + node.getName() + "'" + children + "?",
							"Confirm deletion", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.YES_OPTION)
			{
				getModel().removeNodeFromParent(node, true);
				if (datasetPanel != null)
					datasetPanel.getTree().repaint();
			}
		}
	}

	private void enableActions()
	{
		boolean anySelected = tree.getSelectionPath() != null;
		renameAction.setEnabled(anySelected);
		deleteAction.setEnabled(anySelected);
	}

	public LayerTreeModel getModel()
	{
		return tree.getModel();
	}

	@Override
	public void setup(Theme theme)
	{
		super.setup(theme);
		linkPanels(theme.getPanels());
		setupDrag();

		if (!layersFileExisted)
			for (IDataset dataset : theme.getDatasets())
				addDefaultLayersFromDataset(dataset);
	}

	@Override
	public void dispose()
	{
		LayerTreePersistance.saveToXML(root, layersFile);
	}

	private void linkPanels(Collection<ThemePanel> panels)
	{
		for (ThemePanel panel : panels)
		{
			if (panel instanceof DatasetPanel)
			{
				datasetPanel = (DatasetPanel) panel;
				break;
			}
		}
		if (datasetPanel != null)
		{
			datasetPanel.registerLayerTreeModel(getModel());
		}
	}

	private void addDefaultLayersFromDataset(IDataset dataset)
	{
		if (dataset instanceof ILazyDataset && dataset.getDatasets().isEmpty()
				&& dataset.getLayers().isEmpty())
		{
			final ILazyDataset lazy = (ILazyDataset) dataset;
			lazy.addListener(new LazyLoadListener()
			{
				@Override
				public void loaded(ILazyTreeObject object)
				{
					addDefaultLayersFromDataset(lazy, new ArrayList<IData>());
					lazy.removeListener(this);
				}
			});
		}
		else
		{
			addDefaultLayersFromDataset(dataset, new ArrayList<IData>());
		}
	}

	private void addDefaultLayersFromDataset(IDataset dataset, List<IData> parents)
	{
		parents.add(dataset);

		for (ILayerDefinition layer : dataset.getLayers())
			if (layer.isDefault())
				getModel().addLayer(layer, parents);

		//recurse
		for (IDataset d : dataset.getDatasets())
			addDefaultLayersFromDataset(d, parents);

		parents.remove(parents.size() - 1);
	}

	private void setupDrag()
	{
		JTree datasetTree = datasetPanel != null ? datasetPanel.getTree() : null;

		NodeTransferHandler handler = new NodeTransferHandler(tree, datasetTree);
		tree.setTransferHandler(handler);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);

		if (datasetTree != null)
		{
			datasetTree.setTransferHandler(handler);
			datasetTree.setDragEnabled(true);
		}
	}

	private void editAtPath(TreePath p)
	{
		tree.getModel().addTreeModelListener(new TreeModelListener()
		{
			@Override
			public void treeStructureChanged(TreeModelEvent e)
			{
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e)
			{
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e)
			{
			}

			@Override
			public void treeNodesChanged(TreeModelEvent e)
			{
				tree.setEditable(false);
				tree.getModel().removeTreeModelListener(this);
			}
		});

		tree.setEditable(true);
		tree.startEditingAtPath(p);
	}
}
