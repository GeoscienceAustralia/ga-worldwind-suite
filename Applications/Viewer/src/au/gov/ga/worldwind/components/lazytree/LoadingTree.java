package au.gov.ga.worldwind.components.lazytree;

import java.awt.Image;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

public class LoadingTree extends JTree
{
	private Set<Object> loadingNodes = new HashSet<Object>();

	public LoadingTree()
	{
	}

	public LoadingTree(TreeModel model)
	{
		super(model);
	}

	public boolean addLoadingNode(Object node)
	{
		synchronized (loadingNodes)
		{
			return loadingNodes.add(node);
		}
	}

	public boolean removeLoadingNode(Object node)
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

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h)
	{
		//Bug in SDK: the loading icon will continuously call repaint on JTree
		//(even if loadingIcon.setImageObserver(null) is called) unless this
		//method returns true if painting is not required. This is why we keep
		//track of any nodes using the loading icon (aka loadingNodes).

		if (loadingNodeCount() <= 0)
			return true;
		return super.imageUpdate(img, infoflags, x, y, w, h);
	}
}
