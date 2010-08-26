package au.gov.ga.worldwind.viewer.layers.tiled.image.delegate;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;

public interface TileFactoryDelegate extends Delegate
{
	TextureTile createTextureTile(Sector sector, Level level, int row, int col);
}
