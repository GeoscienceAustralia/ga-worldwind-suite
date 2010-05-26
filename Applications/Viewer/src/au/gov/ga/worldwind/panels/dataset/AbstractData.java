package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

public abstract class AbstractData extends AbstractIconItem implements IData
{
	private String name;
	private URL descriptionURL;
	private boolean base;

	public AbstractData(String name, URL descriptionURL, URL iconURL, boolean base)
	{
		super(iconURL);
		this.name = name;
		this.descriptionURL = descriptionURL;
		this.base = base;
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
	
	@Override
	public boolean isBase()
	{
		return base;
	}
}
