package au.gov.ga.worldwind.layers.point.old;

import java.util.HashMap;
import java.util.Map;

public class Style
{
	public enum StyleType
	{
		Annotation, Icon, Marker, Text
	}

	protected String name;
	protected StyleType type;
	protected Object attributes;
	protected boolean defalt; //cannot use java keyword 'default'
	protected Map<String, String> variables = new HashMap<String, String>();

	public Style(String name, StyleType type, boolean defalt)
	{
		setName(name);
		setType(type);
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

	public StyleType getType()
	{
		return type;
	}

	public void setType(StyleType type)
	{
		this.type = type;
	}

	public Object getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Object attributes)
	{
		this.attributes = attributes;
	}

	public void addVariable(String attribute, String variable)
	{
		variables.put(attribute, variable);
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;

		if (obj instanceof String)
			return obj.equals(name);

		if (obj instanceof Style)
		{
			Style s = (Style) obj;
			if (name == null)
				return s.getName() == null;
			return name.equals(s.getName());
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		if (name != null)
			return name.hashCode();
		return super.hashCode();
	}
}
