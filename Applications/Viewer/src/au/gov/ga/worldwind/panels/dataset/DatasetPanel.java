package au.gov.ga.worldwind.panels.dataset;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.lazytree.LazyTree;
import au.gov.ga.worldwind.components.lazytree.LazyTreeObjectNode;
import au.gov.ga.worldwind.panels.layers.LayerTreeModel;
import au.gov.ga.worldwind.theme.AbstractThemePanel;
import au.gov.ga.worldwind.theme.Theme;

public class DatasetPanel extends AbstractThemePanel
{
	private LazyTree tree;
	private Dataset root;
	private LazyTreeObjectNode rootNode;
	private DefaultTreeModel model;
	private DatasetCellRenderer renderer;

	public DatasetPanel()
	{
		super(new BorderLayout());
		setDisplayName("Datasets");

		root = new Dataset("root", null, null, true);
		model = new DefaultTreeModel(null);
		rootNode = new LazyTreeObjectNode(root, model);
		model.setRoot(rootNode);
		renderer = new DatasetCellRenderer();

		tree = new LazyTree(model);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setRowHeight(0);
		tree.setCellRenderer(renderer);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(MINIMUM_LIST_HEIGHT, MINIMUM_LIST_HEIGHT));
	}

	public JTree getTree()
	{
		return tree;
	}

	public void registerLayerTreeModel(LayerTreeModel layerTreeModel)
	{
		renderer.setLayerTreeModel(layerTreeModel);
	}

	@Override
	public void setup(Theme theme)
	{
		for(IDataset dataset : theme.getDatasets())
		{
			root.getDatasets().add(dataset);
		}
		rootNode.refreshChildren(model);
		
		//expand root by default
		Object[] path;
		if (rootNode.getChildCount() <= 0)
			path = new Object[] { rootNode };
		else
			path = new Object[] { rootNode, rootNode.getChildAt(rootNode.getChildCount() - 1) };
		tree.expandPath(new TreePath(path));
	}

	@Override
	public void dispose()
	{
	}
}
