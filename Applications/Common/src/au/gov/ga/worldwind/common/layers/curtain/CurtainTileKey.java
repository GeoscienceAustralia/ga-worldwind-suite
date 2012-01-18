package au.gov.ga.worldwind.common.layers.curtain;

import gov.nasa.worldwind.util.TileKey;

/**
 * Class used for uniquely identifying a {@link CurtainTile}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainTileKey extends TileKey
{
	public CurtainTileKey(int level, int row, int col, String cacheName)
	{
		super(level, row, col, cacheName);
	}

	public CurtainTileKey(CurtainTile tile)
	{
		this(tile.getLevel().getLevelNumber(), tile.getRow(), tile.getColumn(), tile.getLevel().getCacheName());
	}
}
