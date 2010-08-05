package au.gov.ga.worldwind.panels.layers;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.lazytree.ILazyTreeObject;
import au.gov.ga.worldwind.components.lazytree.LazyLoadListener;
import au.gov.ga.worldwind.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.panels.dataset.IData;
import au.gov.ga.worldwind.panels.dataset.IDataset;
import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.panels.dataset.ILazyDataset;
import au.gov.ga.worldwind.panels.dataset.LayerDefinition;
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

	private Window window;

	private BasicAction newLayerAction, openLayerAction, renameAction, editAction, deleteAction,
			newFolderAction, expandAllAction, collapseAllAction, refreshAction, reloadAction;

	private DatasetPanel datasetPanel;

	private JFileChooser chooser;

	public LayersPanel()
	{
		super();
		setDisplayName("Layers");

		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				treeSelectionChanged();
				enableActions();
			}
		});

		treeSelectionChanged();
		enableActions();
		createPopupMenus();
	}

	@Override
	public ImageIcon getIcon()
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
			root = new FolderNode(null, null, null, true);
			layersFileExisted = false;
		}
		return root;
	}

	@Override
	protected void createActions()
	{
		super.createActions();

		newFolderAction =
				new BasicAction("New folder", "Create new folder", Icons.newfolder.getIcon());
		newFolderAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				newFolder();
			}
		});

		newLayerAction = new BasicAction("New layer", "Add new layer", Icons.add.getIcon());
		newLayerAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				newLayer();
			}
		});

		openLayerAction = new BasicAction("Open layer", Icons.folder.getIcon());
		openLayerAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openLayerFile();
			}
		});

		renameAction = new BasicAction("Rename", "Rename selected", Icons.edit.getIcon());
		renameAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				renameSelected();
			}
		});

		editAction = new BasicAction("Edit", "Edit selected", Icons.properties.getIcon());
		editAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				editSelected();
			}
		});

		deleteAction = new BasicAction("Delete", "Delete selected", Icons.delete.getIcon());
		deleteAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				deleteSelected();
			}
		});

		expandAllAction = new BasicAction("Expand all", Icons.expandall.getIcon());
		expandAllAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				expandAll();
			}
		});

		collapseAllAction = new BasicAction("Collapse all", Icons.collapseall.getIcon());
		collapseAllAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				collapseAll();
			}
		});

		refreshAction = new BasicAction("Refresh layer", Icons.refresh.getIcon());
		refreshAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				refreshSelected();
			}
		});

		reloadAction = new BasicAction("Reload layer", Icons.reload.getIcon());
		reloadAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				reloadSelected();
			}
		});
	}

	@Override
	protected void setupToolBarBeforeSlider(JToolBar toolBar)
	{
		toolBar.add(newFolderAction);
		toolBar.addSeparator();
		toolBar.add(newLayerAction);
		toolBar.add(openLayerAction);
		toolBar.add(renameAction);
		toolBar.add(editAction);
		toolBar.add(deleteAction);
		toolBar.addSeparator();
		/*toolBar.add(expandAllAction);
		toolBar.add(collapseAllAction);
		toolBar.addSeparator();*/
	}

	private void createPopupMenus()
	{
		final JPopupMenu itemPopupMenu = new JPopupMenu();
		itemPopupMenu.add(newFolderAction);
		itemPopupMenu.addSeparator();
		itemPopupMenu.add(renameAction);
		itemPopupMenu.add(editAction);
		itemPopupMenu.add(deleteAction);
		itemPopupMenu.addSeparator();
		itemPopupMenu.add(refreshAction);
		itemPopupMenu.add(reloadAction);

		final JPopupMenu otherPopupMenu = new JPopupMenu();
		otherPopupMenu.add(newFolderAction);
		otherPopupMenu.addSeparator();
		otherPopupMenu.add(expandAllAction);
		otherPopupMenu.add(collapseAllAction);

		tree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					int row = tree.getRowForLocation(e.getX(), e.getY());
					if (row >= 0)
					{
						tree.setSelectionRow(row);
						itemPopupMenu.show(tree, e.getX(), e.getY());
					}
					else
					{
						otherPopupMenu.show(tree, e.getX(), e.getY());
					}
				}
			}
		});
	}

	private void newFolder()
	{
		FolderNode node = new FolderNode("New Folder", null, Icons.folder.getURL(), true);
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

	private void renameSelected()
	{
		TreePath p = tree.getSelectionPath();
		if (p != null)
		{
			editAtPath(p);
		}
	}

	private void newLayer()
	{
		LayerNode layerNode = new LayerNode("", null, null, true, null, true, 1, null);
		LayerEditor editor = new LayerEditor(window, "New layer", layerNode, getIcon());
		int value = editor.getOkCancel();
		if (value == JOptionPane.OK_OPTION)
		{
			TreePath p = tree.getSelectionPath();
			TreePath newPath;
			if (p == null)
			{
				getModel().addToRoot(layerNode, false);
				newPath = new TreePath(new Object[] { root, layerNode });
			}
			else
			{
				INode parent = (INode) p.getLastPathComponent();
				getModel().insertNodeInto(layerNode, parent, parent.getChildCount(), false);
				newPath = p.pathByAddingChild(layerNode);
			}
			tree.getUI().relayout(newPath);
			tree.scrollPathToVisible(newPath);
		}
	}

	private void editSelected()
	{
		TreePath p = tree.getSelectionPath();
		if (p != null)
		{
			INode node = (INode) p.getLastPathComponent();
			AbstractNode editing = null;
			if (node instanceof ILayerNode)
			{
				editing = new LayerNode((ILayerNode) node);
			}
			else
			{
				editing = new FolderNode(node);
			}
			LayerEditor editor = new LayerEditor(window, "Edit layer", editing, getIcon());
			int value = editor.getOkCancel();
			if (value == JOptionPane.OK_OPTION)
			{
				INode parent = node.getParent();
				int index = getModel().getIndexOfChild(parent, node);
				getModel().removeNodeFromParent(node, false);
				getModel().insertNodeInto(editing, parent, index, true);

				p = p.getParentPath().pathByAddingChild(editing);
				tree.scrollPathToVisible(p);
				tree.getUI().relayout();
			}
		}
	}

	private void deleteSelected()
	{
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null && paths.length > 0)
		{
			String text;
			if (paths.length > 1)
			{
				boolean anyChildren = false;
				for (TreePath p : paths)
				{
					INode node = (INode) p.getLastPathComponent();
					if (node.getChildCount() > 0)
					{
						anyChildren = true;
						break;
					}
				}
				text =
						"Are you sure you want to delete these " + paths.length + " items"
								+ (anyChildren ? " and their children" : "") + "?";
			}
			else
			{
				TreePath p = paths[0];
				INode node = (INode) p.getLastPathComponent();
				String type;
				if (node instanceof ILayerNode)
					type = "layer";
				else
					type = "folder";
				boolean anyChildren = node.getChildCount() > 0;
				text =
						"Are you sure you want to delete the " + type + " '" + node.getName() + "'"
								+ (anyChildren ? " and its children" : "") + "?";
			}

			int choice =
					JOptionPane.showConfirmDialog(this, text, "Confirm deletion",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.YES_OPTION)
			{
				for (TreePath p : paths)
				{
					INode node = (INode) p.getLastPathComponent();
					getModel().removeNodeFromParent(node, true);
					if (datasetPanel != null)
						datasetPanel.getTree().repaint();
				}

				tree.getUI().relayout();
			}
		}
	}

	private void treeSelectionChanged()
	{
		ILayerNode layer = null;
		TreePath p = tree.getSelectionPath();
		if (p != null)
		{
			Object o = p.getLastPathComponent();
			if (o instanceof ILayerNode)
				layer = (ILayerNode) o;
		}

		refreshAction.setEnabled(layer != null);
		reloadAction.setEnabled(layer != null);
	}

	private void refreshSelected()
	{
		TreePath p = tree.getSelectionPath();
		if (p != null)
		{
			Object o = p.getLastPathComponent();
			if (o instanceof ILayerNode)
			{
				ILayerNode layer = (ILayerNode) o;
				int choice =
						JOptionPane
								.showConfirmDialog(
										this,
										"This will refresh all previously downloaded data for this layer. Are you sure?",
										refreshAction.getName(), JOptionPane.YES_NO_OPTION,
										JOptionPane.WARNING_MESSAGE);
				if (choice == JOptionPane.YES_OPTION)
				{
					getModel().setExpiryTime(layer, System.currentTimeMillis());
				}
			}
		}
	}

	private void reloadSelected()
	{
		TreePath p = tree.getSelectionPath();
		if (p != null)
		{
			Object o = p.getLastPathComponent();
			if (o instanceof ILayerNode)
			{
				ILayerNode layer = (ILayerNode) o;
				layerEnabler.reloadLayer(layer);
				
				tree.scrollPathToVisible(p);
				tree.getUI().relayout();
			}
		}
	}

	private void enableActions()
	{
		boolean anySelected = tree.getSelectionPath() != null;
		renameAction.setEnabled(anySelected);
		editAction.setEnabled(anySelected);
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

		window = SwingUtilities.getWindowAncestor(this);
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

	protected void collapseAll()
	{
		while (collapseLast())
			;
	}

	private boolean collapseLast()
	{
		for (int i = tree.getRowCount() - 1; i >= 0; i--)
		{
			if (tree.isExpanded(i))
			{
				tree.collapseRow(i);
				return true;
			}
		}
		return false;
	}

	protected void expandAll()
	{
		for (int i = 0; i < tree.getRowCount(); i++)
		{
			tree.expandRow(i);
		}
	}

	private void createFileChooserIfRequired()
	{
		if (chooser == null)
		{
			chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Open layer");
			chooser.setFileFilter(new LayerDefinitionFileFilter());
		}
	}

	public void openLayerFile()
	{
		createFileChooserIfRequired();

		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				File[] files = chooser.getSelectedFiles();
				for (File file : files)
				{
					URL url = file.toURI().toURL();
					ILayerDefinition definition =
							new LayerDefinition(file.getName(), url, null, Icons.file.getURL(),
									true, false);
					addLayer(definition);
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "Error adding layer file: " + e, "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void addLayer(ILayerDefinition definition)
	{
		/*INode parent = null;
		int index = -1;
		TreePath p = tree.getSelectionPath();
		if (p != null && isOn())
		{
			Object o = p.getLastPathComponent();
			if (o instanceof INode)
			{
				parent = (INode) o;
				index = parent.getChildCount();
			}
		}

		INode node = LayerNode.createFromLayerDefinition(definition);
		if (parent == null)
			getModel().addToRoot(node, true);
		else
			getModel().insertNodeInto(node, parent, index, true);*/

		INode node = LayerNode.createFromLayerDefinition(definition);
		getModel().addToRoot(node, true);

		tree.getUI().relayout();
	}

	public static class LayerDefinitionFileFilter extends FileFilter
	{
		@Override
		public String getDescription()
		{
			return "Layer definition file (*.xml)";
		}

		@Override
		public boolean accept(File f)
		{
			if (f.isDirectory())
				return true;
			return f.getName().toLowerCase().endsWith(".xml");
		}
	}
}
