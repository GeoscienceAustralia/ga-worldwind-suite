package au.gov.ga.worldwind.common.layers.curtain.delegate;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.requester.AbstractLocalRequesterDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegatorTextureTile;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.IImageTileRequesterDelegate;

public class CurtainLocalRequesterDelegate extends AbstractLocalRequesterDelegate<DelegatorTextureTile> implements
		IImageTileRequesterDelegate
{
	private final static String DEFINITION_STRING = "LocalRequester";

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new CurtainLocalRequesterDelegate();
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
