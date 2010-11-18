package au.gov.ga.worldwind.wmsbrowser;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A tree model backed by a list of {@link WmsServer}s.
 * <p/>
 * Supports lazy-loading of the servers
 */
public class WmsServerTreeModel implements TreeModel
{
	/** The servers list backing this tree */
	private List<WmsServer> servers = new ArrayList<WmsServer>();
	
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	
	public void addServer(WmsServer server)
	{
		if (servers.contains(server))
		{
			return;
		}
		servers.add(server);
		
		notifyServerAdded(server);
	}
	
	@Override
	public Object getRoot()
	{
		return servers;
	}

	@Override
	public Object getChild(Object parent, int index)
	{
		if (parent == servers)
		{
			return servers.get(index);
		}
		else if (parent instanceof WmsServer)
		{
			return ((WmsServer)parent).getLayers().get(index);
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent)
	{
		if (parent == servers)
		{
			return servers.size();
		}
		else if (parent instanceof WmsServer)
		{
			if (!((WmsServer)parent).isLayersLoaded())
			{
				return 0;
			}
			return ((WmsServer)parent).getLayers().size();
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node)
	{
		// Make server nodes appear to have children if we don't know any better 
		if (node instanceof WmsServer && !((WmsServer)node).isLayersLoaded())
		{
			return true;
		}
		return getChildCount(node) == 0;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		// Do nothing
	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		if (parent == servers)
		{
			return servers.indexOf(child);
		}
		else if (parent instanceof WmsServer)
		{
			return ((WmsServer)parent).getLayers().indexOf(child);
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		if (listeners.contains(l))
		{
			return;
		}
		listeners.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		listeners.remove(l);
	}

	public void notifyServerAdded(WmsServer server)
	{
		TreeModelEvent e = new TreeModelEvent(server, new Object[]{servers});
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).treeStructureChanged(e);
		}
	}
	
}
