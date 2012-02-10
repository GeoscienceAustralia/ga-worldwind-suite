package au.gov.ga.worldwind.common.ui.lazytree;

import au.gov.ga.worldwind.common.ui.ClearableBasicTreeUI;

/**
 * Tree subclass that uses the {@link LazyTreeController} to lazily load tree
 * nodes as they are expanded. Also overrides the tree's UI class with a
 * {@link ClearableBasicTreeUI}, which allows forcing a relayout of the tree
 * structure.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LazyTree extends LoadingTree
{
	public LazyTree(LazyTreeModel model)
	{
		super(model);
		setUI(new ClearableBasicTreeUI());
		LazyTreeController controller = new LazyTreeController(this, model);
		addTreeWillExpandListener(controller);
	}

	@Override
	public ClearableBasicTreeUI getUI()
	{
		return (ClearableBasicTreeUI) super.getUI();
	}
}
