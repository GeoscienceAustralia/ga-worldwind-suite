package au.gov.ga.worldwind.common.layers.geometry;

import gov.nasa.worldwind.avlist.AVList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.common.layers.point.Attribute;
import au.gov.ga.worldwind.common.layers.point.Style;

/**
 * The default implementation of the {@link StyleProvider} interface
 */
public class BasicStyleProviderImpl implements StyleProvider
{
	protected Map<String, Style> styleMap = new HashMap<String, Style>();
	protected Style defaultStyle;
	protected Collection<Style> styles = new ArrayList<Style>();
	protected Collection<Attribute> attributes = new ArrayList<Attribute>();
	
	public BasicStyleProviderImpl(Collection<? extends Attribute> attributes, Collection<? extends Style> styles)
	{
		Validate.notNull(attributes, "Attributes are required");
		Validate.notNull(styles, "Styles are required");
		
		this.styles.addAll(styles);
		this.attributes.addAll(attributes);
	}
	
	@Override
	public Style getStyle(AVList attributeValues)
	{
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
		}
		
		if (style == null)
		{
			style = getDefaultStyle();
		}
		
		return style;
	}

	private Style getDefaultStyle()
	{
		if (defaultStyle == null)
		{
			for (Style style : styles)
			{
				if (style.isDefault())
				{
					defaultStyle = style;
					break;
				}
			}
		}
		return defaultStyle;
	}

	@Override
	public Collection<? extends Style> getStyles()
	{
		return Collections.unmodifiableCollection(styles);
	}
	
	@Override
	public void setStyles(Collection<? extends Style> styles)
	{
		if (styles == null)
		{
			return;
		}
		this.styles.clear();
		this.styles.addAll(styles);
	}

	@Override
	public Collection<? extends Attribute> getAttributes()
	{
		return Collections.unmodifiableCollection(attributes);
	}

	@Override
	public void setAttributes(Collection<? extends Attribute> attributes)
	{
		if (attributes == null)
		{
			return;
		}
		this.attributes.clear();
		this.attributes.addAll(attributes);
	}

}
