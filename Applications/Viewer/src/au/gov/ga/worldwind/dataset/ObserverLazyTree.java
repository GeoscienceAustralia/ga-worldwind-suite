package au.gov.ga.worldwind.dataset;

import java.awt.Image;

import javax.swing.tree.DefaultTreeModel;

import au.gov.ga.worldwind.components.lazytree.LazyTree;

public class ObserverLazyTree extends LazyTree
{
	public ObserverLazyTree(DefaultTreeModel model)
	{
		super(model);
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
