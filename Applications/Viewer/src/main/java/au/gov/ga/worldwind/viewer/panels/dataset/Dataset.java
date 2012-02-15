package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeObjectNode;

/**
 * Basic implementation of the {@link IDataset} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Dataset extends AbstractData implements IDataset
{
	private final List<IData> children = new ArrayList<IData>();

	public Dataset(String name, URL infoURL, URL iconURL, boolean base)
	{
		super(name, infoURL, iconURL, base);
	}

	@Override
	public List<IData> getChildren()
	{
		return children;
	}

	@Override
	public void addChild(IData child)
	{
		synchronized (children)
		{
			children.add(child);
		}
	}

	@Override
	public MutableTreeNode[] getChildren(LazyTreeModel model)
	{
		synchronized (children)
		{
			MutableTreeNode[] array = new MutableTreeNode[children.size()];
			int i = 0;
			for (IData child : children)
			{
				array[i++] = child.createMutableTreeNode(model);
			}
			return array;
		}
	}

	@Override
	public MutableTreeNode createMutableTreeNode(LazyTreeModel model)
	{
		return new LazyTreeObjectNode(this, model);
	}
}
