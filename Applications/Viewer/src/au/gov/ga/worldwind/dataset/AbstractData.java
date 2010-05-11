package au.gov.ga.worldwind.dataset;

import java.net.URL;

public abstract class AbstractData extends AbstractIconItem implements IData
{
	private String name;
	private URL descriptionURL;

	public AbstractData(String name, URL descriptionURL, URL iconURL)
	{
		super(iconURL);
		this.name = name;
		this.descriptionURL = descriptionURL;
	}

	public String getName()
	{
		return name;
	}

	public URL getDescriptionURL()
	{
		return descriptionURL;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
