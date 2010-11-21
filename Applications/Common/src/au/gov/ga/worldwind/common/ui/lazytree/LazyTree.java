package au.gov.ga.worldwind.common.ui.lazytree;

import au.gov.ga.worldwind.common.ui.ClearableBasicTreeUI;

public class LazyTree extends LoadingTree
{
	public LazyTree(LazyTreeModel model)
	{
		super(model);
		LazyTreeController controller = new LazyTreeController(this, model);
		addTreeWillExpandListener(controller);
		
		setUI(new ClearableBasicTreeUI());
	}
	
	@Override
	public ClearableBasicTreeUI getUI()
	{
		return (ClearableBasicTreeUI) super.getUI();
	}
}
