package au.gov.ga.worldwind.panels.dataset;

import gov.nasa.worldwind.WorldWindow;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.lazytree.LazyTree;
import au.gov.ga.worldwind.components.lazytree.LazyTreeObjectNode;
import au.gov.ga.worldwind.panels.WWPanel;

public class DatasetPanel extends JPanel implements WWPanel
{
	private LazyTree tree;
	private Dataset root;
	private LazyTreeObjectNode rootNode;
	private DefaultTreeModel model;

	public DatasetPanel()
	{
		super(new BorderLayout());

		root = new Dataset("root", null, null);
		model = new DefaultTreeModel(null);
		rootNode = new LazyTreeObjectNode(root, model);
		model.setRoot(rootNode);

		tree = new LazyTree(model);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setRowHeight(0);
		tree.setCellRenderer(new DatasetCellRenderer());

		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(20, 20));
	}

	public void addDataset(Dataset dataset)
	{
		root.getDatasets().add(dataset);
		rootNode.refreshChildren(model);
		Object[] path;
		if (rootNode.getChildCount() <= 0)
			path = new Object[] { rootNode };
		else
			path = new Object[] { rootNode, rootNode.getChildAt(rootNode.getChildCount() - 1) };
		tree.expandPath(new TreePath(path));
	}

	public JTree getTree()
	{
		return tree;
	}

	@Override
	public String getName()
	{
		return "Datasets";
	}

	@Override
	public JPanel getPanel()
	{
		return this;
	}

	@Override
	public void setup(WorldWindow wwd)
	{
	}

	@Override
	public void dispose()
	{
	}
}
