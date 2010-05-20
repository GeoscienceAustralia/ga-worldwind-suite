package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

public abstract class AbstractData extends AbstractIconItem implements IData
{
	private String name;
	private URL descriptionURL;
	private boolean root;

	public AbstractData(String name, URL descriptionURL, URL iconURL, boolean root)
	{
		super(iconURL);
		this.name = name;
		this.descriptionURL = descriptionURL;
		this.root = root;
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
	public boolean isRoot()
	{
		return root;
	}
}
