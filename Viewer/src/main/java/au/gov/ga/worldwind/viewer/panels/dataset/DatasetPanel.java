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
package au.gov.ga.worldwind.viewer.panels.dataset;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.viewer.util.message.ViewerMessageConstants.getDatasetsPanelTitleKey;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.SwingUtil;
import au.gov.ga.worldwind.common.ui.lazytree.DefaultLazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeObjectNode;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.viewer.panels.layers.ILayerNode;
import au.gov.ga.worldwind.viewer.panels.layers.LayerTreeModel;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;

/**
 * {@link ThemePanel} that displays the datasets (sets of layers) available to
 * the Viewer. The datasets are loaded from a hierarchy of XML files, and tree
 * node children are downloaded lazily as the user expands their parent nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetPanel extends AbstractThemePanel
{
	private DatasetTree tree;
	private Dataset root;
	private LazyTreeObjectNode rootNode;

	private BasicAction addAllAction;
	private BasicAction addAction;
	private BasicAction removeAction;

	private LayerTreeModel layerTreeModel;

	public DatasetPanel()
	{
		super(new BorderLayout());
		setDisplayName(getMessage(getDatasetsPanelTitleKey()));

		root = new Dataset("root", null, null, true);
		DefaultLazyTreeModel model = new DefaultLazyTreeModel(null);
		rootNode = new LazyTreeObjectNode(root, model);
		model.setRoot(rootNode);

		tree = new DatasetTree(model);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(MINIMUM_LIST_HEIGHT, MINIMUM_LIST_HEIGHT));

		createActions();
		createPopupMenus();
	}

	@Override
	public Icon getIcon()
	{
		return Icons.datasets.getIcon();
	}

	public DatasetTree getTree()
	{
		return tree;
	}

	public LazyTreeModel getModel()
	{
		return tree.getModel();
	}

	public void registerLayerTreeModel(LayerTreeModel layerTreeModel)
	{
		this.layerTreeModel = layerTreeModel;
		tree.getDatasetCellRenderer().setLayerTreeModel(layerTreeModel);
	}

	@Override
	public void setup(Theme theme)
	{
		for (IDataset dataset : theme.getDatasets())
		{
			root.addChild(dataset);
		}
		rootNode.refreshChildren(tree.getModel());

		//expand root by default
		if (rootNode.getChildCount() <= 0)
		{
			Object[] path = new Object[] { rootNode };
			tree.expandPath(new TreePath(path));
		}
		else
		{
			int count = rootNode.getChildCount();
			Object[][] paths = new Object[count][];
			for (int i = 0; i < count; i++)
			{
				paths[i] = new Object[] { rootNode, rootNode.getChildAt(i) };
			}
			for (Object[] path : paths)
			{
				tree.expandPath(new TreePath(path));
			}
		}
	}

	@Override
	public void dispose()
	{
	}

	protected void createActions()
	{
		addAction = new BasicAction("Add layer", Icons.add.getIcon());
		addAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				TreePath path = tree.getSelectionPath();
				IData selected = getSelectedData(path);
				if (!(selected instanceof ILayerDefinition))
					return;
				ILayerDefinition layer = (ILayerDefinition) selected;
				layerTreeModel.addLayer(layer, path.getPath());
				tree.repaint();
				enableActions();
			}
		});

		removeAction = new BasicAction("Remove layer", Icons.remove.getIcon());
		removeAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				TreePath path = tree.getSelectionPath();
				IData selected = getSelectedData(path);
				if (!(selected instanceof ILayerDefinition))
					return;
				ILayerDefinition layer = (ILayerDefinition) selected;
				layerTreeModel.removeLayer(layer);
				tree.repaint();
				enableActions();
			}
		});

		addAllAction = new BasicAction("Add all", Icons.add.getIcon());
		addAllAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final TreePath path = tree.getSelectionPath();
				final IData selected = getSelectedData(path);
				if (selected != null)
				{
					int choice =
							JOptionPane
									.showConfirmDialog(
											DatasetPanel.this,
											"Any layers below the selected dataset will be added to the layer tree. This could take a while depending on the number of layers. Are you sure you want to continue?",
											"Add all", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (choice == JOptionPane.YES_OPTION)
					{
						Thread thread = new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								addAll(selected, path.getPath());
								SwingUtil.invokeLaterTaskOnEDT(new Runnable()
								{
									@Override
									public void run()
									{
										tree.repaint();
										enableActions();
									}
								});
							}
						});
						thread.setDaemon(true);
						thread.start();
					}
				}
			}
		});

		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent event)
			{
				IData selected = getSelectedData(event.getPath());
				enableActions(selected);
			}
		});
	}

	protected void enableActions()
	{
		IData selected = getSelectedData(tree.getSelectionPath());
		enableActions(selected);
	}

	protected void enableActions(IData selected)
	{
		if (selected == null)
			return;

		addAction.setEnabled(false);
		removeAction.setEnabled(false);
		if (selected instanceof ILayerDefinition)
		{
			ILayerDefinition layer = (ILayerDefinition) selected;
			if (layerTreeModel.containsLayer(layer))
				removeAction.setEnabled(true);
			else
				addAction.setEnabled(true);

			addAllAction.setEnabled(false);
		}
		else
		{
			addAllAction.setEnabled(true);
		}
	}

	protected void addAll(IData selected, Object[] path)
	{
		if (selected instanceof ILayerDefinition)
		{
			ILayerDefinition layer = (ILayerDefinition) selected;
			if (!layerTreeModel.containsLayer(layer))
			{
				ILayerNode node = layerTreeModel.addLayer(layer, path);
				layerTreeModel.setEnabled(node, false);
			}
		}
		else if (selected instanceof IDataset)
		{
			if (selected instanceof ILazyDataset)
			{
				final ILazyDataset lazy = (ILazyDataset) selected;
				if (!lazy.isLoaded())
				{
					try
					{
						lazy.load();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			IDataset dataset = (IDataset) selected;
			for (IData child : dataset.getChildren())
			{
				Object[] newPath = new Object[path.length + 1];
				System.arraycopy(path, 0, newPath, 0, path.length);
				newPath[newPath.length - 1] = child;
				addAll(child, newPath);
			}
		}
	}

	private IData getSelectedData(TreePath path)
	{
		if (path == null)
			return null;
		if (!(path.getLastPathComponent() instanceof DefaultMutableTreeNode))
			return null;
		Object selected = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		if (!(selected instanceof IData))
			return null;
		return (IData) selected;
	}

	private void createPopupMenus()
	{
		final JPopupMenu itemPopupMenu = new JPopupMenu();
		itemPopupMenu.add(addAction);
		itemPopupMenu.add(removeAction);
		itemPopupMenu.addSeparator();
		itemPopupMenu.add(addAllAction);

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
				}
			}
		});
	}
}
