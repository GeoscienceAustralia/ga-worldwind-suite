package au.gov.ga.worldwind.components.lazytree;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class LazyTree extends JTree
{
	protected Set<TreeNode> loadingNodes = new HashSet<TreeNode>();

	public LazyTree(DefaultTreeModel model)
	{
		super(model);
		LazyTreeController controller = new LazyTreeController(this, model);
		addTreeWillExpandListener(controller);
	}

	public boolean addLoadingNode(TreeNode node)
	{
		return loadingNodes.add(node);
	}

	public boolean removeLoadingNode(TreeNode node)
	{
		return loadingNodes.remove(node);
	}
	
	public int loadingNodeCount()
	{
		return loadingNodes.size();
	}
}
