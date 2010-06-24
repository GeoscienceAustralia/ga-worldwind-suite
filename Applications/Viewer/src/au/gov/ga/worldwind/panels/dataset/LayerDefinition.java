package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

public class LayerDefinition extends AbstractData implements ILayerDefinition
{
	private URL layerURL;
	private boolean enabled;
	private boolean def;

	public LayerDefinition(String name, URL layerURL, URL infoURL, URL iconURL, boolean base,
			boolean def)
	{
		this(name, layerURL, infoURL, iconURL, base, def, true);
	}

	public LayerDefinition(String name, URL layerURL, URL infoURL, URL iconURL, boolean base,
			boolean def, boolean enabled)
	{
		super(name, infoURL, iconURL, base);
		this.layerURL = layerURL;
		this.def = def;
		this.enabled = enabled;
	}

	@Override
	public URL getLayerURL()
	{
		return layerURL;
	}

	@Override
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
