package au.gov.ga.worldwind.components.lazytree;

import javax.swing.tree.DefaultTreeModel;

public class LazyTree extends LoadingTree
{
	public LazyTree(DefaultTreeModel model)
	{
		super(model);
		LazyTreeController controller = new LazyTreeController(model);
		addTreeWillExpandListener(controller);
	}
}
