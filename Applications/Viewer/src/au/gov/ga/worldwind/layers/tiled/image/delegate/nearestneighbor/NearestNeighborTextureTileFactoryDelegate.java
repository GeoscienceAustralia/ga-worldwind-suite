package au.gov.ga.worldwind.layers.tiled.image.delegate.nearestneighbor;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;
import au.gov.ga.worldwind.layers.tiled.image.delegate.TileFactoryDelegate;

public class NearestNeighborTextureTileFactoryDelegate implements TileFactoryDelegate
{
	@Override
	public TextureTile createTextureTile(Sector sector, Level level, int row, int col)
	{
		return new NearestNeighborTextureTile(sector, level, row, col, this);
	}
}
