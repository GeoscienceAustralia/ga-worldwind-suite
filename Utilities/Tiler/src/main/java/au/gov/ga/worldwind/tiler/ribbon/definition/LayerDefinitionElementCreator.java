package au.gov.ga.worldwind.tiler.ribbon.definition;

import au.gov.ga.worldwind.tiler.ribbon.RibbonTilingContext;

/**
 * An interface for classes that can create element for a ribbon layer definition
 */
public interface LayerDefinitionElementCreator 
{
	/**
	 * @return The name of this element
	 */
	String getElementName();
	
	/**
	 * The string representation of this element
	 */
	String getElementString(int level, RibbonTilingContext context);
}
