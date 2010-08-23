package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;

public class TextureTileFactoryDelegate implements TileFactoryDelegate
{
	@Override
	public TextureTile createTextureTile(Sector sector, Level level, int row, int col)
	{
		return new DelegatorTextureTile(sector, level, row, col, this);
	}
}
