package au.gov.ga.worldwind.wmsbrowser;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * Default implementation of the {@link WmsServer} interface
 * <p/>
 * By default uses the {@link WMSCapabilities#retrieve(java.net.URI)} method to retrieve capabilities from
 * a WMS server. This can be overridden using the {@link #setCapabilitiesService()} method.
 */
public class WmsServerImpl implements WmsServer
{
	/** The default capabilities service. Can be overridden through injection using the {@link #setCapabilitiesService()} method */
	private static final WmsCapabilitiesService DEFAULT_CAPABILITIES_SERVICE = new WmsCapabilitiesService()
	{
		@Override
		public WMSCapabilities retrieveCapabilities(URL url) throws Exception
		{
			return WMSCapabilities.retrieve(url.toURI());
		}
	};
	
	private static ExecutorService loaderService = Executors.newSingleThreadExecutor(new DaemonThreadFactory("WMS Server layer loader"));
	
	private List<WMSLayerInfo> layers = null;
	private List<LoadListener> loadListeners = new ArrayList<LoadListener>();

	private WmsCapabilitiesService capabilitiesService = DEFAULT_CAPABILITIES_SERVICE;
	private WMSCapabilities capabilities;
	private URL serverUrl;
	
	public WmsServerImpl(URL serverUrl)
	{
		Validate.notNull(serverUrl, "A server url is required");
		this.serverUrl = serverUrl;
	}
	
	@Override
	public URL getCapabilitiesUrl()
	{
		return serverUrl;
	}

	@Override
	public List<WMSLayerInfo> getLayers()
	{
		return layers;
	}

	@Override
	public void loadLayers()
	{
		if (isLayersLoaded())
		{
			return;
		}
		
		loaderService.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					notifyLoadStarting();
					doLoad();
					notifyLoadComplete();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					notifyLoadFailed(e);
				}
			}
		});
	}
	
	@Override
	public void loadLayersImmediately() throws Exception
	{
		if (isLayersLoaded())
		{
			return;
		}
		
		try
		{
			notifyLoadStarting();
			doLoad();
			notifyLoadComplete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			notifyLoadFailed(e);
			throw e;
		}
	}
	
	private void doLoad() throws Exception
	{
		// Load the capabilities
		capabilities = capabilitiesService.retrieveCapabilities(serverUrl);
		capabilities.parse();

		layers = new ArrayList<WMSLayerInfo>();
		
        // Gather up all the named layers and make a world wind layer for each.
        final List<WMSLayerCapabilities> namedLayerCaps = capabilities.getNamedLayers();
        if (namedLayerCaps == null)
        {
            return;
        }

        for (WMSLayerCapabilities lc : namedLayerCaps)
        {
        	Set<WMSLayerStyle> styles = lc.getStyles();
        	if (styles == null || styles.size() == 0)
        	{
        		WMSLayerInfo layerInfo = new WMSLayerInfo(capabilities, lc, null);
        		layers.add(layerInfo);
        	}
        	else
        	{
        		for (WMSLayerStyle style : styles)
        		{
        			WMSLayerInfo layerInfo = new WMSLayerInfo(capabilities, lc, style);
        			layers.add(layerInfo);
        		}
        	}
        }
	}

	@Override
	public boolean isLayersLoaded()
	{
		return layers != null;
	}

	@Override
	public void addLoadListener(LoadListener listener)
	{
		if (loadListeners.contains(listener))
		{
			return;
		}
		loadListeners.add(listener);
	}

	@Override
	public void removeLoadListener(LoadListener listener)
	{
		loadListeners.remove(listener);
	}

	private void notifyLoadStarting()
	{
		for (int i = loadListeners.size() - 1; i >= 0; i--)
		{
			loadListeners.get(i).beginningLoad(this);
		}
	}
	
	private void notifyLoadComplete()
	{
		for (int i = loadListeners.size() - 1; i >= 0; i--)
		{
			loadListeners.get(i).loaded(this);
		}
	}
	
	private void notifyLoadFailed(Exception e)
	{
		for (int i = loadListeners.size() - 1; i >= 0; i--)
		{
			loadListeners.get(i).loadFailed(this, e);
		}
	}

	@Override
	public void setCapabilitiesService(WmsCapabilitiesService service)
	{
		this.capabilitiesService = service == null ? DEFAULT_CAPABILITIES_SERVICE : service;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (!(obj instanceof WmsServer))
		{
			return false;
		}
		
		return ((WmsServer)obj).getCapabilitiesUrl().equals(this.getCapabilitiesUrl());
	}
	
	@Override
	public int hashCode()
	{
		return serverUrl.hashCode();
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + serverUrl.toExternalForm() + "]";
	}
	
}
