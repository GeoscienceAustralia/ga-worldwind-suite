package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.panels.WWPanel;
import au.gov.ga.worldwind.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.panels.layers.drag.NodeTransferHandler;
import au.gov.ga.worldwind.settings.Settings;
import au.gov.ga.worldwind.util.Icons;

public class LayersPanel extends JPanel implements WWPanel
{
	private static final String LAYERS_FILENAME = "layers.xml";
	private static final File layersFile = new File(Settings.getUserDirectory(), LAYERS_FILENAME);

	private LayerTree tree;
	private INode root;

	private DatasetPanel datasetPanel;

	public LayersPanel(WorldWindow wwd)
	{
		super(new BorderLayout());

		try
		{
			root = LayerTreePersistance.readFromXML(layersFile);
		}
		catch (Exception e)
		{
		}
		if (root == null)
			root = createDefaultRoot();

		LayerList layerList = new LayerList();
		ExtendedCompoundElevationModel elevationModel = new ExtendedCompoundElevationModel();
		LayerEnabler enabler = new LayerEnabler(wwd, layerList, elevationModel);
		tree = new LayerTree(root, enabler);

		wwd.getModel().setLayers(layerList);
		wwd.getModel().getGlobe().setElevationModel(elevationModel);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(50, 50));


		Action newFolderAction = new AbstractAction("Create Folder", Icons.newfolder.getIcon())
		{
			@Override
			public void actionPerformed(ActionEvent e)
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
				tree.startEditingAtPath(editPath);
			}
		};

		Action renameAction = new AbstractAction("Rename selected", Icons.edit.getIcon())
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				TreePath p = tree.getSelectionPath();
				if (p != null)
				{
					tree.startEditingAtPath(p);
				}
			}
		};

		Action deleteAction = new AbstractAction("Delete selected", Icons.delete.getIcon())
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				TreePath p = tree.getSelectionPath();
				if (p != null)
				{
					INode node = (INode) p.getLastPathComponent();
					getModel().removeNodeFromParent(node, true);
					if (datasetPanel != null)
						datasetPanel.getTree().repaint();
				}
			}
		};

		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.add(newFolderAction);
		toolBar.add(renameAction);
		toolBar.add(deleteAction);
		add(toolBar, BorderLayout.PAGE_START);
	}

	public void linkWithDatasetPanel(DatasetPanel datasetPanel)
	{
		this.datasetPanel = datasetPanel;
		datasetPanel.registerLayerTreeModel(getModel());
	}

	public void setupDrag()
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

	public LayerTreeModel getModel()
	{
		return tree.getModel();
	}

	private INode createDefaultRoot()
	{
		return new FolderNode("root", null, true);
	}

	@Override
	public String getName()
	{
		return "Layers";
	}

	@Override
	public JPanel getPanel()
	{
		return this;
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose()
	{
		LayerTreePersistance.saveToXML(root, layersFile);
	}
}
