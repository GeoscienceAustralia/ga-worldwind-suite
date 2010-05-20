package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

public class LayerDefinition extends AbstractData implements ILayerDefinition
{
	private URL url;
	private boolean enabled;

	public LayerDefinition(String name, URL url, URL descriptionURL, URL iconURL, boolean root)
	{
		this(name, url, descriptionURL, iconURL, root, true);
	}

	public LayerDefinition(String name, URL url, URL descriptionURL, URL iconURL, boolean root,
			boolean enabled)
	{
		super(name, descriptionURL, iconURL, root);
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
