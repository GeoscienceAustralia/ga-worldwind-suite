package au.gov.ga.worldwind.wmsbrowser;

import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.ILazyTreeObject;
import au.gov.ga.worldwind.common.ui.lazytree.LazyLoadListener;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A lazy tree object backed by a single {@link WmsServer} instance
 */
public class WmsServerTreeObject implements ILazyTreeObject
{
	private WmsServer wmsServer;
	private final List<LazyLoadListener> listeners = new ArrayList<LazyLoadListener>();
	
	public WmsServerTreeObject(WmsServer server)
	{
		Validate.notNull(server, "A server is required");
		wmsServer = server;
	}
	
	@Override
	public MutableTreeNode[] getChildren(LazyTreeModel model)
	{
		if (!wmsServer.isLayersLoaded())
		{
			return new MutableTreeNode[0];
		}
		
		List<WMSLayerInfo> layers = wmsServer.getLayers();
		MutableTreeNode[] result = new MutableTreeNode[layers.size()];
		int i = 0;
		for (WMSLayerInfo layer : layers)
		{
			result[i] = new DefaultMutableTreeNode(layer, false);
			i++;
		}
		return result;
	}

	@Override
	public void load() throws Exception
	{
		wmsServer.loadLayersImmediately();
		notifyLoaded();
	}

	@Override
	public void addListener(LazyLoadListener listener)
	{
		if (listener == null || listeners.contains(listener))
		{
			return;
		}
		listeners.add(listener);
	}

	@Override
	public void removeListener(LazyLoadListener listener)
	{
		listeners.remove(listener);
	}
	
	private void notifyLoaded()
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).loaded(this);
		}
	}

	public WmsServer getWmsServer()
	{
		return wmsServer;
	}

}
