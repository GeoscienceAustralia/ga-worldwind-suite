package au.gov.ga.worldwind.common.layers.curtain.delegate;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.requester.AbstractURLRequesterDelegate;

/**
 * {@link ICurtainTileRequesterDelegate} implementation which requests tiles
 * from a URL.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainURLRequesterDelegate extends AbstractURLRequesterDelegate<DelegatorCurtainTextureTile> implements
		ICurtainTileRequesterDelegate
{
	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new CurtainURLRequesterDelegate();
		return null;
	}
}
