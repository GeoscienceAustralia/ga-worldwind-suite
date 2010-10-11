package au.gov.ga.worldwind.common.layers.point;

import gov.nasa.worldwind.avlist.AVList;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that maps a set of attribute values to a Style. Also generates
 * the text and link for the attribute values.
 * 
 * @author Michael de Hoog
 */
public class StyleProvider
{
	protected Style[] styles;
	protected Map<String, Style> styleMap = new HashMap<String, Style>();
	protected Style defaultStyle;
	protected Attribute[] attributes;

	/**
	 * Get a matching style for the provided set of attribute values
	 * 
	 * @param attributeValues
	 * @return
	 */
	public PointProperties getStyle(AVList attributeValues)
	{
		String link = null;
		String text = null;
		Style style = null;

		for (Attribute attribute : attributes)
		{
			if (style == null)
			{
				String styleName = attribute.getMatchingStyle(attributeValues);
				if (styleMap.containsKey(styleName))
				{
					style = styleMap.get(styleName);
				}
			}

			String t = attribute.getText(attributeValues);
			if (t != null)
				text = text == null ? t : text + t;

			link = link != null ? link : attribute.getLink(attributeValues);
		}

		if (style == null)
			style = defaultStyle;

		return new PointProperties(style, text, link);
	}

	public Style[] getStyles()
	{
		return styles;
	}

	public synchronized void setStyles(Style[] styles)
	{
		this.styles = styles;

		styleMap.clear();
		defaultStyle = null;

		for (Style style : styles)
		{
			if (style.isDefault() && defaultStyle == null)
				defaultStyle = style;
			styleMap.put(style.getName(), style);
		}

		if (defaultStyle == null)
		{
			defaultStyle = new Style(null, true);
		}
	}

	public Attribute[] getAttributes()
	{
		return attributes;
	}

	public synchronized void setAttributes(Attribute[] attributes)
	{
		this.attributes = attributes;
	}
}
