package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.WorldWindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.DropMode;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import au.gov.ga.worldwind.components.lazytree.LoadingTree;
import au.gov.ga.worldwind.panels.WWPanel;
import au.gov.ga.worldwind.panels.layers.drag.NodeTransferHandler;
import au.gov.ga.worldwind.settings.Settings;

public class LayersPanel extends JPanel implements WWPanel
{
	private static final String LAYERS_FILENAME = "layers.xml";
	private static final File layersFile = new File(Settings.getUserDirectory(), LAYERS_FILENAME);

	private LoadingTree tree;
	private INode root;

	public LayersPanel()
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

		LayerTreeModel model = new LayerTreeModel(root);
		tree = new LoadingTree(model);
		tree.setUI(new ClearableBasicTreeUI());
		tree.setCellRenderer(new LayerCellRenderer());
		tree.setCellEditor(new DefaultTreeCellEditor(tree, new DefaultTreeCellRenderer()));
		tree.setEditable(true);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setRowHeight(0);
		tree.addTreeExpansionListener(model);
		model.expandNodes(tree);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(20, 20));
	}

	public void setupDrag(JTree datasetTree)
	{
		NodeTransferHandler handler = new NodeTransferHandler(tree, datasetTree);
		tree.setTransferHandler(handler);
		datasetTree.setTransferHandler(handler);
		tree.setDragEnabled(true);
		datasetTree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
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
