package au.gov.ga.worldwind.viewer.components.lazytree;

import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class LoadingTree extends JTree
{
	public LoadingTree()
	{
	}

	public LoadingTree(TreeModel model)
	{
		super(model);
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h)
	{
		//Bug in SDK: the loading icon will continuously call repaint on JTree
		//(even if loadingIcon.setImageObserver(null) is called) unless this
		//method returns false if painting is not required. This is why we keep
		//track of any nodes using the loading icon (aka loadingNodes).

		boolean anyLoading = false;
		for (int i = 0; i < getRowCount(); i++)
		{
			TreePath path = getPathForRow(i);
			if (path != null)
			{
				Object node = path.getLastPathComponent();
				if (isLoading(node))
				{
					Rectangle bounds = getRowBounds(i);
					if (bounds != null)
					{
						repaint(bounds);
					}
					anyLoading = true;
				}
			}
		}
		return anyLoading;
	}

	private boolean isLoading(Object node)
	{
		if (node instanceof ILoadingNode)
		{
			return ((ILoadingNode) node).isLoading();
		}
		else if (node instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) node;
			Object o = dmtn.getUserObject();
			if (o != null && o instanceof ILoadingNode)
				return ((ILoadingNode) o).isLoading();
		}
		return false;
	}
}
