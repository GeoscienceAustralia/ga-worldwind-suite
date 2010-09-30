package au.gov.ga.worldwind.common.layers.shapefile.point;

import gov.nasa.worldwind.avlist.AVList;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Attribute
{
	protected String name;
	protected Map<String, String> switches = new HashMap<String, String>();
	protected Map<String, String> regexes = new HashMap<String, String>();
	protected Map<Range, String> ranges = new HashMap<Range, String>();
	protected StringWithPlaceholder textString;
	protected StringWithPlaceholder linkString;

	public Attribute(String name)
	{
		setName(name);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void addCase(String value, String style)
	{
		switches.put(value, style);
	}

	public void addRegex(String regex, String style)
	{
		regexes.put(regex, style);
	}

	public void addRange(double min, double max, String style)
	{
		Range range = new Range();
		range.min = min;
		range.max = max;
		ranges.put(range, style);
	}

	public void addText(String value, String placeholder)
	{
		textString = new StringWithPlaceholder();
		textString.string = value;
		textString.placeholder = placeholder;
	}

	public void addLink(String url, String placeholder)
	{
		linkString = new StringWithPlaceholder();
		linkString.string = url;
		linkString.placeholder = placeholder;
	}

	public String getText(AVList attributes)
	{
		return getPlaceholderString(textString, attributes);
	}

	public String getLink(AVList attributes)
	{
		return getPlaceholderString(linkString, attributes);
	}

	private String getPlaceholderString(StringWithPlaceholder string, AVList attributes)
	{
		if (string == null || !attributes.hasKey(name))
			return null;

		String stringValue = attributes.getValue(name).toString();
		return string.replacePlaceholder(stringValue);
	}

	public String getMatchingStyle(AVList attributes)
	{
		if (!attributes.hasKey(name))
			return null;

		String stringValue = attributes.getValue(name).toString();
		if (switches.containsKey(stringValue))
			return switches.get(stringValue);

		for (Entry<String, String> regex : regexes.entrySet())
		{
			if (Pattern.matches(regex.getKey(), stringValue))
				return regex.getValue();
		}

		Double doubleValue = null;
		try
		{
			doubleValue = Double.valueOf(stringValue);
		}
		catch (Exception e)
		{
		}
		if (doubleValue != null)
		{
			for (Entry<Range, String> range : ranges.entrySet())
			{
				if (range.getKey().contains(doubleValue))
					return range.getValue();
			}
		}

		return null;
	}

	protected class Range
	{
		public double min;
		public double max;

		public boolean contains(double value)
		{
			return min <= value && value <= max;
		}
	}

	protected class StringWithPlaceholder
	{
		public String string;
		public String placeholder;

		public String replacePlaceholder(String with)
		{
			return string.replaceAll(placeholder, with);
		}
	}
}
