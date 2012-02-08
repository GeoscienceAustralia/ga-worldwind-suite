package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.requester.AbstractURLRequesterDelegate;

/**
 * Non-abstract implementation of the {@link AbstractURLRequesterDelegate}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImageURLRequesterDelegate extends AbstractURLRequesterDelegate<DelegatorTextureTile> implements
		IImageTileRequesterDelegate
{
	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new ImageURLRequesterDelegate();
		return null;
	}
}
