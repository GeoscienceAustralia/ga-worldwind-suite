package au.gov.ga.worldwind.common.layers.styled;

import gov.nasa.worldwind.avlist.AVList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class that maps a set of attribute values to a Style. Also generates
 * the text and link for the attribute values.
 * 
 * @author Michael de Hoog
 */
public class BasicStyleProvider implements StyleProvider
{
	protected List<Style> styles;
	protected Map<String, Style> styleMap = new HashMap<String, Style>();
	protected Style defaultStyle;
	protected List<Attribute> attributes;

	/**
	 * Get a matching style for the provided set of attribute values
	 * 
	 * @param attributeValues
	 * @return
	 */
	@Override
	public StyleAndText getStyle(AVList attributeValues)
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

		return new StyleAndText(style, text, link);
	}

	@Override
	public List<Style> getStyles()
	{
		return styles;
	}

	@Override
	public synchronized void setStyles(List<Style> styles)
	{
		this.styles = styles;

		styleMap.clear();
		defaultStyle = null;

		if (styles != null)
		{
			for (Style style : styles)
			{
				if (style.isDefault() && defaultStyle == null)
				{
					defaultStyle = style;
				}
				styleMap.put(style.getName(), style);
			}
		}

		if (defaultStyle == null)
		{
			defaultStyle = new Style(null, true);
		}
	}

	@Override
	public List<Attribute> getAttributes()
	{
		return attributes;
	}

	@Override
	public synchronized void setAttributes(List<Attribute> attributes)
	{
		this.attributes = attributes;
	}
}
