package au.gov.ga.worldwind.wmsbrowser;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.DefaultLazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.ITreeObject;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeObjectNode;

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
	
	public void addServer(WmsServer server)
	{
		if (servers.contains(server))
		{
			return;
		}
		servers.add(server);

		rootNode.refreshChildren(this);
		
		notifyTreeChanged(server);
	}
	
	public void addServers(List<WmsServer> servers)
	{
		this.servers.addAll(servers);
		
		rootNode.refreshChildren(this);
		notifyTreeChanged(servers);
	}
	
	public void notifyTreeChanged(Object source)
	{
		fireTreeStructureChanged(source, new Object[] { servers }, null, null);
	}
}
