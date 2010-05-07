package au.gov.ga.worldwind.components.lazytree;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class LazyTree extends JTree
{
	private Set<TreeNode> loadingNodes = new HashSet<TreeNode>();

	public LazyTree(DefaultTreeModel model)
	{
		super(model);
		LazyTreeController controller = new LazyTreeController(this, model);
		addTreeWillExpandListener(controller);
	}

	public boolean addLoadingNode(TreeNode node)
	{
		synchronized (loadingNodes)
		{
			return loadingNodes.add(node);
		}
	}

	public boolean removeLoadingNode(TreeNode node)
	{
		synchronized (loadingNodes)
		{
			return loadingNodes.remove(node);
		}
	}

	public int loadingNodeCount()
	{
		synchronized (loadingNodes)
		{
			return loadingNodes.size();
		}
	}
}
