package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

/**
 * Abstract implementation of the {@link IData} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractData extends AbstractIconItem implements IData
{
	private String name;
	private URL infoURL;
	private boolean base;

	public AbstractData(String name, URL infoURL, URL iconURL, boolean base)
	{
		super(iconURL);
		this.name = name;
		this.infoURL = infoURL;
		this.base = base;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public URL getInfoURL()
	{
		return infoURL;
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
