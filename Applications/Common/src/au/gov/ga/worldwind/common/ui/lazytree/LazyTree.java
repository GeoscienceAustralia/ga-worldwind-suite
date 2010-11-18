package au.gov.ga.worldwind.common.ui.lazytree;


public class LazyTree extends LoadingTree
{
	public LazyTree(LazyTreeModel model)
	{
		super(model);
		LazyTreeController controller = new LazyTreeController(this, model);
		addTreeWillExpandListener(controller);
	}
}
