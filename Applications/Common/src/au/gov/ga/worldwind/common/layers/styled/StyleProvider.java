package au.gov.ga.worldwind.common.layers.styled;

import gov.nasa.worldwind.avlist.AVList;

import java.util.List;

/**
 * A provider that yields the {@link Style} to use for a given set of
 * {@link Attribute} values
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface StyleProvider
{
	/**
	 * @return The {@link StyleAndText} to use for the given attribute values
	 */
	StyleAndText getStyle(AVList attributeValues);

	/**
	 * @return The collection of styles this provider supports
	 */
	List<Style> getStyles();

	/**
	 * Set the collection of styles this provider supports
	 */
	void setStyles(List<Style> styles);

	/**
	 * @return The collection of attributes associated with this provider
	 */
	List<Attribute> getAttributes();

	/**
	 * Set the collection of attributes associated with this provider.
	 * <p/>
	 * The attribute collection is used to map from attribute values to styles.
	 */
	void setAttributes(List<Attribute> attributes);
}
