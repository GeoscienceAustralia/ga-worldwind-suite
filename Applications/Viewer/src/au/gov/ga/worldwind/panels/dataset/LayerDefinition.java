package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

public class LayerDefinition extends AbstractData implements ILayerDefinition
{
	private URL url;

	public LayerDefinition(String name, URL url, URL descriptionURL, URL iconURL)
	{
		super(name, descriptionURL, iconURL);
		this.url = url;
	}

	public URL getURL()
	{
		return url;
	}
}
