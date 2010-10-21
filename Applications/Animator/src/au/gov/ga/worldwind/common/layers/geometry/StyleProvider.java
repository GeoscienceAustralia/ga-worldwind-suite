package au.gov.ga.worldwind.common.layers.geometry;

import gov.nasa.worldwind.avlist.AVList;

import java.util.Collection;

import au.gov.ga.worldwind.common.layers.point.Attribute;
import au.gov.ga.worldwind.common.layers.point.Style;

/**
 * A provider that yields the {@link Style} to use for a given set of {@link Attribute} values
 */
public interface StyleProvider
{

	/**
	 * @return The {@link Style} to use for the given attribute values
	 */
	Style getStyle(AVList attributeValues);
	
	/**
	 * @return The collection of styles this provider supports
	 */
	Collection<? extends Style> getStyles();
	
	/**
	 * Set the collection of styles this provider supports
	 */
	void setStyles(Collection<? extends Style> styles);
	
	/**
	 * @return The collection of attributes associated with this provider
	 */
	Collection<? extends Attribute> getAttributes();
	
	/**
	 * Set the collection of attributes associated with this provider.
	 * <p/>
	 * The attribute collection is used to map from attribute values to styles.
	 */
	void setAttributes(Collection<? extends Attribute> attributes);
	
}
