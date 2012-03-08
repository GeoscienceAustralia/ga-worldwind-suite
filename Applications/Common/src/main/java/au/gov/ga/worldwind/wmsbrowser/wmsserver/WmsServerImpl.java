/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.gov.ga.worldwind.common.util.DaemonThreadFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Default implementation of the {@link WmsServer} interface
 * <p/>
 * By default uses the {@link WMSCapabilities#retrieve(java.net.URI)} method to retrieve capabilities from
 * a WMS server. This can be overridden using the {@link #setCapabilitiesService()} method.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class WmsServerImpl implements WmsServer
{
	/** The default capabilities service. Can be overridden through injection using the {@link #setCapabilitiesService()} method */
	private static final WmsCapabilitiesService DEFAULT_CAPABILITIES_SERVICE = new DefaultCapabilitiesService();
	
	private static ExecutorService loaderService = Executors.newSingleThreadExecutor(new DaemonThreadFactory("WMS Server layer loader"));

	private WmsServerIdentifier identifier;
	
	private List<WMSLayerInfo> layers = null;
	private List<LoadListener> loadListeners = new ArrayList<LoadListener>();

	private WmsCapabilitiesService capabilitiesService = DEFAULT_CAPABILITIES_SERVICE;
	private WMSCapabilities capabilities;
	
	public WmsServerImpl(URL serverUrl)
	{
		Validate.notNull(serverUrl, "A server url is required");
		this.identifier = new WmsServerIdentifierImpl(serverUrl);
	}
	
	public WmsServerImpl(String name, URL serverUrl)
	{
		Validate.notNull(serverUrl, "A server url is required");
		this.identifier = new WmsServerIdentifierImpl(name, serverUrl);
	}
	
	public WmsServerImpl(WmsServerIdentifier identifier)
	{
		Validate.notNull(identifier, "A server identifier is required");
		this.identifier = identifier;
	}
	
	/**
	 * Constructor to use if the capabilities for this server have already been retrieved.
	 */
	public WmsServerImpl(WmsServerIdentifier identifier, WMSCapabilities capabilities)
	{
		Validate.notNull(identifier, "A server identifier is required");
		this.identifier = identifier;
		this.capabilities = capabilities;
	}
	
	
	@Override
	public WmsServerIdentifier getIdentifier()
	{
		return identifier;
	}
	
	@Override
	public void setIdentifier(WmsServerIdentifier identifier)
	{
		if (identifier == null)
		{
			return;
		}
		boolean reloadRequired = !this.identifier.getCapabilitiesUrl().equals(identifier.getCapabilitiesUrl());
		
		this.identifier = identifier;
		
		if (reloadRequired)
		{
			capabilities = null;
			layers = null;
		}
	}
	
	@Override
	public String getName()
	{
		return identifier.getName();
	}
	
	@Override
	public URL getCapabilitiesUrl()
	{
		return identifier.getCapabilitiesUrl();
	}

	@Override
	public WMSCapabilities getCapabilities()
	{
		return capabilities;
	}
	
	@Override
	public boolean isCapabilitiesLoaded()
	{
		return capabilities != null;
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
		if (capabilities == null)
		{
			capabilities = capabilitiesService.retrieveCapabilities(getCapabilitiesUrl());
		}
		capabilities.parse();

		layers = new ArrayList<WMSLayerInfo>();
		
        // Gather up all the named layers
        final List<WMSLayerCapabilities> namedLayerCaps = capabilities.getNamedLayers();
        if (namedLayerCaps == null)
        {
            return;
        }
        
        for (WMSLayerCapabilities lc : namedLayerCaps)
        {
        	layers.addAll(WMSLayerInfo.createLayerInfos(capabilities, lc));
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
	public void copyLoadedDataFrom(WmsServer otherServer)
	{
		if (otherServer == null || !(otherServer.getIdentifier().equals(this.getIdentifier())))
		{
			return;
		}
		
		// Copy loaded capabilities if not already loaded
		if (!this.isCapabilitiesLoaded() && otherServer.isCapabilitiesLoaded())
		{
			this.capabilities = otherServer.getCapabilities();
		}
		
		// Copy loaded layers, if not already loaded
		if (!this.isLayersLoaded() && otherServer.isLayersLoaded())
		{
			this.layers = otherServer.getLayers();
		}
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
		
		return ((WmsServer)obj).getIdentifier().equals(this.getIdentifier());
	}
	
	@Override
	public int hashCode()
	{
		return getIdentifier().hashCode();
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getCapabilitiesUrl().toExternalForm() + "]";
	}
	
}
