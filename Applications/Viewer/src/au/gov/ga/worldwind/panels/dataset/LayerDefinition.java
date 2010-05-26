package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

public class LayerDefinition extends AbstractData implements ILayerDefinition
{
	private URL url;
	private boolean enabled;
	private boolean def;

	public LayerDefinition(String name, URL url, URL descriptionURL, URL iconURL, boolean base,
			boolean def)
	{
		this(name, url, descriptionURL, iconURL, base, def, true);
	}

	public LayerDefinition(String name, URL url, URL descriptionURL, URL iconURL, boolean base,
			boolean def, boolean enabled)
	{
		super(name, descriptionURL, iconURL, base);
		this.url = url;
		this.def = def;
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

	@Override
	public boolean isDefault()
	{
		return def;
	}
}
