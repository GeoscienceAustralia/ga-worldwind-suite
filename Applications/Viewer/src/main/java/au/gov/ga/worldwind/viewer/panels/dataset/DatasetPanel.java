package au.gov.ga.worldwind.viewer.panels.dataset;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.viewer.util.message.ViewerMessageConstants.getDatasetsPanelTitleKey;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

import au.gov.ga.worldwind.common.ui.lazytree.DefaultLazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeObjectNode;
import au.gov.ga.worldwind.common.util.Icons;
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
}
