package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getDefaultWmsLoadingErrorMsgKey;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getDefaultWmsLoadingErrorPrefixKey;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.getServerNotReachableErrorMsgKey;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import au.gov.ga.worldwind.common.ui.lazytree.ILazyTreeObject;
import au.gov.ga.worldwind.common.ui.lazytree.LazyLoadException;
import au.gov.ga.worldwind.common.ui.lazytree.LazyLoadListener;
import au.gov.ga.worldwind.common.ui.lazytree.LazyTreeModel;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServer;

/**
 * A lazy tree object backed by a single {@link WmsServer} instance
 */
public class WmsServerTreeObject implements ILazyTreeObject
{
	private WmsServer wmsServer;
	private final List<LazyLoadListener> listeners = new ArrayList<LazyLoadListener>();
	
	private static final String ERROR_PREFIX = getMessage(getDefaultWmsLoadingErrorPrefixKey());
	private static final Map<Class<? extends Throwable>, String> ERROR_MESSAGE_MAP = new HashMap<Class<? extends Throwable>, String>();
	static
	{
		ERROR_MESSAGE_MAP.put(FileNotFoundException.class, getMessage(getServerNotReachableErrorMsgKey()));
	}
	
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
		try
		{
			wmsServer.loadLayersImmediately();
			notifyLoaded();
		}
		catch (Exception e)
		{
			// Attempt to map the exception to a sensible error message
			throw new LazyLoadException(getMessageForError(e), e);
		}
	}

	/**
	 * @return The error message to use for the provided exception
	 */
	private static String getMessageForError(Throwable e)
	{
		String result = ERROR_MESSAGE_MAP.get(e.getClass());
		if (result != null)
		{
			return ERROR_PREFIX + " " + result;
		}
		if (result == null && e.getCause() == null)
		{
			return ERROR_PREFIX + " " + getMessage(getDefaultWmsLoadingErrorMsgKey());
		}
		return getMessageForError(e.getCause());
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
