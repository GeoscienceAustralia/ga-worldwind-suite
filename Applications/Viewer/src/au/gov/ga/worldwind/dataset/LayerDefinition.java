package au.gov.ga.worldwind.dataset;

import java.net.URL;

public class LayerDefinition implements ILayerDefinition
{
	private String name;
	private URL url;

	public LayerDefinition(String name, URL url)
	{
		this.name = name;
		this.url = url;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public URL getURL()
	{
		return url;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
}
