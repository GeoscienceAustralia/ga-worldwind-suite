package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

public class LayerDefinition extends AbstractData implements ILayerDefinition
{
	private URL layerURL;
	private URL legendURL;
	private URL queryURL;
	private boolean enabled;
	private boolean def;

	public LayerDefinition(String name, URL layerURL, URL infoURL, URL iconURL, URL legendURL,
			URL queryURL, boolean base, boolean def)
	{
		this(name, layerURL, infoURL, iconURL, legendURL, queryURL, base, def, true);
	}

	public LayerDefinition(String name, URL layerURL, URL infoURL, URL iconURL, URL legendURL,
			URL queryURL, boolean base, boolean def, boolean enabled)
	{
		super(name, infoURL, iconURL, base);
		this.layerURL = layerURL;
		this.legendURL = legendURL;
		this.queryURL = queryURL;
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

	@Override
	public URL getLegendURL()
	{
		return legendURL;
	}

	@Override
	public URL getQueryURL()
	{
		return queryURL;
	}
}
