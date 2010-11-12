package au.gov.ga.worldwind.common.layers.curtain.delegate;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.requester.AbstractURLRequesterDelegate;

public class CurtainURLRequesterDelegate extends AbstractURLRequesterDelegate<DelegatorCurtainTextureTile> implements
		ICurtainTileRequesterDelegate
{
	private final static String DEFINITION_STRING = "URLRequester";

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new CurtainURLRequesterDelegate();
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
