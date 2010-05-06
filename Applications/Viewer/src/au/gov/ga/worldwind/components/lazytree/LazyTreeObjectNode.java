package au.gov.ga.worldwind.components.lazytree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;


public class LazyTreeObjectNode extends LazyTreeNode
{
	public LazyTreeObjectNode(ITreeObject userObject, DefaultTreeModel model)
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
	public MutableTreeNode[] loadChildren(DefaultTreeModel model) throws Exception
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
}
