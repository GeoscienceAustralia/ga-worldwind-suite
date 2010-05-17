package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

public class LayerDefinition extends AbstractData implements ILayerDefinition
{
	private URL url;
	private boolean enabled;
	
	public LayerDefinition(String name, URL url, URL descriptionURL, URL iconURL)
	{
		this(name, url, descriptionURL, iconURL, true);
	}

	public LayerDefinition(String name, URL url, URL descriptionURL, URL iconURL, boolean enabled)
	{
		super(name, descriptionURL, iconURL);
		this.url = url;
		this.enabled = enabled;
	}

	public URL getLayerURL()
	{
		return url;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
}
