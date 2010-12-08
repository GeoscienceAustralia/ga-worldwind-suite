package au.gov.ga.worldwind.wmsbrowser;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.DefaultLazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.ITreeObject;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeObjectNode;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;

/**
 * A tree model backed by a list of {@link WmsServer}s.
 * <p/>
 * Supports lazy-loading of the servers layer lists
 */
public class WmsServerTreeModel extends DefaultLazyTreeModel implements LazyTreeModel
{
	private static final long serialVersionUID = 20101119L;

	/** The servers list backing this tree */
	private List<WmsServer> servers = new ArrayList<WmsServer>();

	/** The root node in the tree */
	private LazyTreeObjectNode rootNode;
	
	public WmsServerTreeModel()
	{
		super(null);
		
		rootNode = new LazyTreeObjectNode(new ITreeObject(){
			@Override
			public MutableTreeNode[] getChildren(LazyTreeModel model)
			{
				MutableTreeNode[] result = new MutableTreeNode[servers.size()];
				int i = 0;
				for (WmsServer server : servers)
				{
					result[i] = new LazyTreeObjectNode(new WmsServerTreeObject(server), WmsServerTreeModel.this);
					i++;
				}
				return result;
			}
		}, this);
		setRoot(rootNode);
	}
	
	public int getNumberOfServers()
	{
		return servers.size();
	}
	
	public List<WmsServer> getServers()
	{
		return servers;
	}
	
	/**
	 * Add the provided server to this tree model
	 */
	public void addServer(WmsServer server)
	{
		boolean treeChanged = doAddServer(server);
		
		if (treeChanged)
		{
			rootNode.refreshChildren(this);
			notifyTreeChanged(server);
		}
	}
	
	/**
	 * Add each provided server to this tree model
	 */
	public void addServers(List<WmsServer> servers)
	{
		boolean treeChanged = false;
		for (WmsServer server : servers)
		{
			treeChanged = doAddServer(server) || treeChanged; // Order important - other way around will short-circuit and prevent adding of server
		}
		
		if (treeChanged)
		{
			rootNode.refreshChildren(this);
			notifyTreeChanged(servers);
		}
	}

	private boolean doAddServer(WmsServer newServer)
	{
		if (servers.contains(newServer))
		{
			// Update the layers/capabilities if the provided server has them loaded and the existing one doesn't
			WmsServer existingServer = servers.get(servers.indexOf(newServer));
			existingServer.copyLoadedDataFrom(newServer);
			return false;
		}
		servers.add(newServer);
		return true;
	}
	
	/**
	 * Remove the provided server from the model, and signal a tree changed event.
	 */
	public void removeServer(WmsServer server)
	{
		if (server == null || !servers.contains(server))
		{
			return;
		}
		
		servers.remove(server);
		rootNode.refreshChildren(this);
		notifyTreeChanged(server);
	}
	
	public void notifyTreeChanged(Object source)
	{
		fireTreeStructureChanged(source, new Object[] { servers }, null, null);
	}

}
