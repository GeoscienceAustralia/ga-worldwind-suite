package au.gov.ga.worldwind.common.layers.styled;

/**
 * Generalised selectable style. A style is selected using the
 * {@link StyleProvider} for a particular set of attribute values, and then the
 * Style is used to set an object's properties.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Style extends PropertySetter
{
	protected String name;
	protected boolean defalt;

	/**
	 * Create a new style.
	 * 
	 * @param name
	 * @param defalt
	 *            Is this style the default style?
	 */
	public Style(String name, boolean defalt)
	{
		setName(name);
		setDefault(defalt);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isDefault()
	{
		return defalt;
	}

	public void setDefault(boolean defalt)
	{
		this.defalt = defalt;
	}
}
