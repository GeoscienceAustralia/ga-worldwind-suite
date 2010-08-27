package au.gov.ga.worldwind.common.layers.tiled.image.delegate.nearestneighbor;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.Delegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.TextureTileFactoryDelegate;
import au.gov.ga.worldwind.common.layers.tiled.image.delegate.TileFactoryDelegate;

/**
 * Implementation of {@link TileFactoryDelegate} which creates
 * {@link NearestNeighborTextureTile}s.
 * 
 * @author Michael de Hoog
 */
public class NearestNeighborTextureTileFactoryDelegate extends TextureTileFactoryDelegate
{
	private final static String DEFINITION_STRING = "NearestNeighborTile";

	@Override
	public TextureTile createTextureTile(Sector sector, Level level, int row, int col)
	{
		return new NearestNeighborTextureTile(sector, level, row, col, this);
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
		{
			return new NearestNeighborTextureTileFactoryDelegate();
		}
		return null;
	}

	@Override
	public String toDefinition()
	{
		return DEFINITION_STRING;
	}
}
