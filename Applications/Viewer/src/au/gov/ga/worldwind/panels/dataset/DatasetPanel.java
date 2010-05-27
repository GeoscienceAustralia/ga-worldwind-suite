package au.gov.ga.worldwind.panels.dataset;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.lazytree.LazyTreeObjectNode;
import au.gov.ga.worldwind.panels.layers.LayerTreeModel;
import au.gov.ga.worldwind.theme.AbstractThemePanel;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.util.Icons;

public class DatasetPanel extends AbstractThemePanel
{
	private DatasetTree tree;
	private Dataset root;
	private LazyTreeObjectNode rootNode;

	public DatasetPanel()
	{
		super(new BorderLayout());
		setDisplayName("Datasets");

		root = new Dataset("root", null, null, true);
		DefaultTreeModel model = new DefaultTreeModel(null);
		rootNode = new LazyTreeObjectNode(root, model);
		model.setRoot(rootNode);

		tree = new DatasetTree(model);

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(MINIMUM_LIST_HEIGHT, MINIMUM_LIST_HEIGHT));
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

	public void registerLayerTreeModel(LayerTreeModel layerTreeModel)
	{
		tree.getDatasetCellRenderer().setLayerTreeModel(layerTreeModel);
	}

	@Override
	public void setup(Theme theme)
	{
		for (IDataset dataset : theme.getDatasets())
		{
			root.getDatasets().add(dataset);
		}
		rootNode.refreshChildren(tree.getModel());

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
