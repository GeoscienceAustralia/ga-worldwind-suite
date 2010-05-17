package au.gov.ga.worldwind.panels.dataset;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.components.lazytree.LazyTree;
import au.gov.ga.worldwind.components.lazytree.LazyTreeObjectNode;
import au.gov.ga.worldwind.panels.layers.LayerTreeModel;
import au.gov.ga.worldwind.theme.Theme;
import au.gov.ga.worldwind.theme.ThemePanel;

public class DatasetPanel extends JPanel implements ThemePanel
{
	private String displayName = "Datasets";
	
	private LazyTree tree;
	private Dataset root;
	private LazyTreeObjectNode rootNode;
	private DefaultTreeModel model;
	private DatasetCellRenderer renderer;

	public DatasetPanel()
	{
		super(new BorderLayout());

		root = new Dataset("root", null, null);
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
		scrollPane.setPreferredSize(new Dimension(50, 50));
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
	public JPanel getPanel()
	{
		return this;
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

	@Override
	public boolean isOn()
	{
		return isVisible();
	}

	@Override
	public void setOn(boolean on)
	{
		setVisible(on);
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
}
