package au.gov.ga.worldwind.common.ui.lazytree;

import javax.swing.tree.MutableTreeNode;


public class LazyTreeObjectNode extends LazyTreeNode
{
	public LazyTreeObjectNode(ITreeObject userObject, LazyTreeModel model)
	{
		super(userObject, model);
		if (userObject instanceof ILazyTreeObject)
		{
			//wait to load children later
		}
		else
		{
			setChildren(userObject.getChildren(model));
		}
	}

	@Override
	public MutableTreeNode[] loadChildren(LazyTreeModel model) throws Exception
	{
		if (getUserObject() instanceof ILazyTreeObject)
		{
			ILazyTreeObject object = (ILazyTreeObject) getUserObject();
			object.load();
			return object.getChildren(model);
		}

		setAllowsChildren(false);
		return null;
	}

	public void refreshChildren(LazyTreeModel model)
	{
		if (getUserObject() instanceof ITreeObject)
		{
			ITreeObject userObject = (ITreeObject) getUserObject();
			reset();
			if (userObject instanceof ILazyTreeObject)
			{
			}
			else
			{
				setChildren(userObject.getChildren(model));
			}
		}
	}
}
