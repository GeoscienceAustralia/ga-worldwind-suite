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

import static au.gov.ga.worldwind.common.util.Util.isBlank;
import static au.gov.ga.worldwind.common.util.Util.isEmpty;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.viewer.util.message.ViewerMessageConstants.*;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

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
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.FileFilters;
import au.gov.ga.worldwind.common.ui.lazytree.ILazyTreeObject;
import au.gov.ga.worldwind.common.ui.lazytree.LazyLoadListener;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.viewer.panels.dataset.IData;
import au.gov.ga.worldwind.viewer.panels.dataset.IDataset;
import au.gov.ga.worldwind.viewer.panels.dataset.ILayerDefinition;
import au.gov.ga.worldwind.viewer.panels.dataset.ILazyDataset;
import au.gov.ga.worldwind.viewer.panels.dataset.LayerDefinition;
import au.gov.ga.worldwind.viewer.panels.layers.drag.NodeTransferHandler;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

/**
 * {@link ThemePanel} that displays a list of layers that have been added to the
 * viewer. These layers are saved when closing the viewer, and reloaded next
 * time the viewer is started.
 * <p>
 * The layers persistance filename can be customized in the {@link Theme}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayersPanel extends AbstractLayersPanel
{
	private static final String DEFAULT_LAYERS_PERSISTANCE_FILENAME = "layers.xml";

	private boolean layersFileExisted;
	private boolean persistLayers;
	private String layersPersistanceFilename = DEFAULT_LAYERS_PERSISTANCE_FILENAME;

	private Window window;

	private BasicAction newLayerAction;
	private BasicAction openLayerAction;
	private BasicAction renameAction;
	private BasicAction editAction;
	private BasicAction deleteAction;
	private BasicAction newFolderAction;
	private BasicAction expandAllAction;
	private BasicAction collapseAllAction;
	private BasicAction refreshAction;
	private BasicAction reloadAction;

	private DatasetPanel datasetPanel;

	private JFileChooser chooser;

	public LayersPanel()
	{
		super();
		setDisplayName(getMessage(getLayersPanelTitleKey()));
	}

	@Override
	public void setup(final Theme theme)
	{
		super.setup(theme);

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

		linkPanels(theme.getPanels());
		setupDrag();

		if (!layersFileExisted)
		{
			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					for (IDataset dataset : theme.getDatasets())
					{
						addDefaultLayersFromDataset(dataset);
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
		}

		window = SwingUtilities.getWindowAncestor(this);
	}

	@Override
	public ImageIcon getIcon()
	{
		return Icons.hierarchy.getIcon();
	}

	@Override
	protected INode createRootNode(Theme theme)
	{
		persistLayers = theme.isPersistLayers();
		if (!isBlank(theme.getLayerPersistanceFilename()))
		{
			layersPersistanceFilename = theme.getLayerPersistanceFilename();
		}

		INode root = null;
		if (persistLayers)
		{
			try
			{
				root = LayerTreePersistance.readFromXML(getLayersFile());
				layersFileExisted = true;
			}
			catch (Exception e)
			{
			}
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
				new BasicAction(getMessage(getLayersNewFolderLabelKey()), getMessage(getLayersNewFolderTooltipKey()),
						Icons.newfolder.getIcon());
		newFolderAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				newFolder();
			}
		});

		newLayerAction =
				new BasicAction(getMessage(getLayersNewLayerLabelKey()), getMessage(getLayersNewLayerTooltipKey()),
						Icons.add.getIcon());
		newLayerAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				newLayer();
			}
		});

		openLayerAction =
				new BasicAction(getMessage(getLayersOpenLayerLabelKey()), getMessage(getLayersOpenLayerTooltipKey()),
						Icons.folder.getIcon());
		openLayerAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openLayerFile();
			}
		});

		renameAction =
				new BasicAction(getMessage(getLayersRenameLabelKey()), getMessage(getLayersRenameTooltipKey()),
						Icons.edit.getIcon());
		renameAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				renameSelected();
			}
		});

		editAction =
				new BasicAction(getMessage(getLayersEditLabelKey()), getMessage(getLayersEditTooltipKey()),
						Icons.properties.getIcon());
		editAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				editSelected();
			}
		});

		deleteAction =
				new BasicAction(getMessage(getLayersDeleteLabelKey()), getMessage(getLayersDeleteTooltipKey()),
						Icons.delete.getIcon());
		deleteAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				deleteSelected();
			}
		});

		expandAllAction =
				new BasicAction(getMessage(getLayersExpandAllLabelKey()), getMessage(getLayersExpandAllTooltipKey()),
						Icons.expandall.getIcon());
		expandAllAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				expandAll();
			}
		});

		collapseAllAction =
				new BasicAction(getMessage(getLayersCollapseAllLabelKey()),
						getMessage(getLayersCollapseAllTooltipKey()), Icons.collapseall.getIcon());
		collapseAllAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				collapseAll();
			}
		});

		refreshAction =
				new BasicAction(getMessage(getLayersRefreshLayerLabelKey()),
						getMessage(getLayersRefreshLayerTooltipKey()), Icons.refresh.getIcon());
		refreshAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				refreshSelected();
			}
		});

		reloadAction =
				new BasicAction(getMessage(getLayersReloadLayerLabelKey()),
						getMessage(getLayersReloadLayerTooltipKey()), Icons.reload.getIcon());
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
		FolderNode node = new FolderNode(getMessage(getTreeNewFolderLabelKey()), null, Icons.folder.getURL(), true);
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
		LayerEditor editor = new LayerEditor(window, getMessage(getNewLayerDialogTitleKey()), layerNode, getIcon());
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
			INode clone = node.clone();
			if (clone instanceof AbstractNode)
			{
				AbstractNode editing = (AbstractNode) clone;
				LayerEditor editor =
						new LayerEditor(window, getMessage(getEditLayerDialogTitleKey()), editing, getIcon());
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
	}

	private void deleteSelected()
	{
		TreePath[] paths = tree.getSelectionPaths();
		if (isEmpty(paths))
		{
			return;
		}

		int choice =
				JOptionPane.showConfirmDialog(this, createDeleteConfirmationMessage(paths),
						getMessage(getDeleteLayerDialogTitleKey()), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.YES_OPTION)
		{
			for (TreePath p : paths)
			{
				INode node = (INode) p.getLastPathComponent();
				deleteNode(node);
			}
		}
	}
	
	public void deleteNode(INode node)
	{
		getModel().removeNodeFromParent(node, true);
		if (datasetPanel != null)
		{
			datasetPanel.getTree().repaint();
		}
		tree.getUI().relayout();
	}

	private String createDeleteConfirmationMessage(TreePath[] paths)
	{
		String message;
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
			message = getMessage(getConfirmDeleteLayerMultipleMessageKey(), paths.length, anyChildren ? 1 : 0);
		}
		else
		{
			INode node = (INode) paths[0].getLastPathComponent();

			String typeKey = (node instanceof ILayerNode) ? getTermLayerKey() : getTermFolderKey();
			message =
					getMessage(getConfirmDeleteLayerSingleMessageKey(), getMessage(typeKey), node.getName(),
							node.getChildCount());
		}
		return message;
	}

	private void treeSelectionChanged()
	{
		ILayerNode node = null;
		TreePath p = tree.getSelectionPath();
		if (p != null)
		{
			Object o = p.getLastPathComponent();
			if (o instanceof ILayerNode)
			{
				node = (ILayerNode) o;
			}
		}

		refreshAction.setEnabled(node != null);
		reloadAction.setEnabled(node != null);
	}

	private void refreshSelected()
	{
		TreePath p = tree.getSelectionPath();
		if (p == null)
		{
			return;
		}

		Object o = p.getLastPathComponent();
		if (o instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) o;
			int choice =
					JOptionPane.showConfirmDialog(this, getMessage(getRefreshLayerConfirmationMessageKey()),
							refreshAction.getName(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.YES_OPTION)
			{
				getModel().setExpiryTime(layer, System.currentTimeMillis());
			}
		}
	}

	private void reloadSelected()
	{
		TreePath p = tree.getSelectionPath();
		if (p == null)
		{
			return;
		}

		Object o = p.getLastPathComponent();
		if (o instanceof ILayerNode)
		{
			ILayerNode layer = (ILayerNode) o;
			layerEnabler.reloadLayer(layer);

			tree.scrollPathToVisible(p);
			tree.getUI().relayout();
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
		return tree.getLayerModel();
	}

	@Override
	public void dispose()
	{
		if (persistLayers)
		{
			LayerTreePersistance.saveToXML(root, getLayersFile());
		}
	}

	protected File getLayersFile()
	{
		return SettingsUtil.getSettingsFile(layersPersistanceFilename);
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
		if (dataset instanceof ILazyDataset && dataset.getChildren().isEmpty())
		{
			final ILazyDataset lazy = (ILazyDataset) dataset;
			lazy.addListener(new LazyLoadListener()
			{
				@Override
				public void loaded(ILazyTreeObject object)
				{
					addDefaultLayersFromDataset(lazy, new ArrayList<IData>());
					lazy.removeListener(this);
					tree.relayoutOnEDT();
				}
			});
		}
		else
		{
			addDefaultLayersFromDataset(dataset, new ArrayList<IData>());
			tree.relayoutOnEDT();
		}
	}

	private void addDefaultLayersFromDataset(IDataset dataset, List<IData> parents)
	{
		parents.add(dataset);

		List<IData> children = dataset.getChildren();

		for (IData child : children)
		{
			if (child instanceof ILayerDefinition)
			{
				ILayerDefinition layer = (ILayerDefinition) child;
				if (layer.isDefault())
				{
					getModel().addLayer(layer, parents);
				}
			}
		}

		//recurse
		for (IData child : children)
		{
			if (child instanceof IDataset)
			{
				IDataset d = (IDataset) child;
				addDefaultLayersFromDataset(d, parents);
			}
		}

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
		{/* Do nothing...*/
		}
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
			chooser.setDialogTitle(getMessage(getOpenLayerDialogTitleKey()));
			chooser.setFileFilter(FileFilters.getLayerDefinitionFilter());
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
							new LayerDefinition(file.getName(), url, null, Icons.file.getURL(), true, false);
					addLayer(definition);
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, getMessage(getOpenLayerErrorMessageKey(), e),
						getMessage(getOpenLayerErrorTitleKey()), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void addLayer(ILayerDefinition definition)
	{
		INode node = LayerNode.createFromLayerDefinition(definition);
		getModel().addToRoot(node, true);

		tree.getUI().relayout();
	}

	public void addWmsLayer(WMSLayerInfo layerInfo)
	{
		if (layerInfo == null)
		{
			return;
		}
		getModel().addWmsLayer(layerInfo);
		tree.getUI().relayout();
	}

}
