package au.gov.ga.worldwind.common.layers.tiled.image.delegate.nearestneighbor;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Level;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.ITileFactoryDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.DelegatorTextureTile;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.TextureTileFactoryDelegate;

/**
 * Implementation of {@link ITileFactoryDelegate} which creates
 * {@link NearestNeighborTextureTile}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NearestNeighborTextureTileFactoryDelegate extends TextureTileFactoryDelegate
{
	private final static String DEFINITION_STRING = "NearestNeighborTile";

	@Override
	public DelegatorTextureTile createTextureTile(Sector sector, Level level, int row, int col)
	{
		return new NearestNeighborTextureTile(sector, level, row, col, this);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
		{
			return new NearestNeighborTextureTileFactoryDelegate();
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
