package au.gov.ga.worldwind.layers.point.old;

import gov.nasa.worldwind.formats.shapefile.DBaseRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Attribute
{
	protected String name;

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

	public static class TextAttribute extends Attribute
	{
		public TextAttribute(String name)
		{
			super(name);
		}
	}

	public static class LinkAttribute extends Attribute
	{
		protected String url;
		protected String placeholder;

		public LinkAttribute(String name, String url, String placeholder)
		{
			super(name);
			setUrl(url);
			setPlaceholder(placeholder);
		}

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public String getPlaceholder()
		{
			return placeholder;
		}

		public void setPlaceholder(String placeholder)
		{
			this.placeholder = placeholder;
		}
	}

	public static class StyleAttribute extends Attribute
	{
		protected Map<String, String> switchMap = new HashMap<String, String>();
		protected Map<Range, String> rangeMap = new HashMap<Range, String>();
		protected List<String> variables = new ArrayList<String>();

		public StyleAttribute(String name)
		{
			super(name);
		}

		public void addCase(String value, String style)
		{
			switchMap.put(value, style);
		}

		public void addRange(double min, double max, String style)
		{
			Range range = new Range();
			range.min = min;
			range.max = max;
			rangeMap.put(range, style);
		}

		public void addVariable(String variable)
		{
			variables.add(variable);
		}

		protected class Range
		{
			public double min;
			public double max;
		}

		public String styleMatch(DBaseRecord attributes)
		{
			if (!attributes.hasKey(getName()))
				return null;
			
			Object value = attributes.getValue(getName());
			
			
			return null;
		}
	}
}
