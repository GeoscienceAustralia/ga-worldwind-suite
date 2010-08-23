package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

public class DelegatorTextureTile extends TextureTile
{
	protected final TileFactoryDelegate delegate;
	
	public DelegatorTextureTile(Sector sector, Level level, int row, int col, TileFactoryDelegate delegate)
	{
		super(sector, level, row, col);
		this.delegate = delegate;
	}
	
	/* ************************************************************************************
	 * Below here is copied from TextureTile, with some modifications to use the delegate *
	 ************************************************************************************ */

	@Override
	public TextureTile[] createSubTiles(Level nextLevel)
	{
		if (nextLevel == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        Angle p0 = this.getSector().getMinLatitude();
        Angle p2 = this.getSector().getMaxLatitude();
        Angle p1 = Angle.midAngle(p0, p2);

        Angle t0 = this.getSector().getMinLongitude();
        Angle t2 = this.getSector().getMaxLongitude();
        Angle t1 = Angle.midAngle(t0, t2);

        String nextLevelCacheName = nextLevel.getCacheName();
        int nextLevelNum = nextLevel.getLevelNumber();
        int row = this.getRow();
        int col = this.getColumn();

        TextureTile[] subTiles = new TextureTile[4];

        TileKey key = new TileKey(nextLevelNum, 2 * row, 2 * col, nextLevelCacheName);
        TextureTile subTile = this.getTileFromMemoryCache(key);
        if (subTile != null)
            subTiles[0] = subTile;
        else
        	subTiles[0] = delegate.createTextureTile(new Sector(p0, p1, t0, t1), nextLevel, 2 * row, 2 * col);
            //subTiles[0] = new TextureTile(new Sector(p0, p1, t0, t1), nextLevel, 2 * row, 2 * col);

        key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1, nextLevelCacheName);
        subTile = this.getTileFromMemoryCache(key);
        if (subTile != null)
            subTiles[1] = subTile;
        else
        	subTiles[1] = delegate.createTextureTile(new Sector(p0, p1, t1, t2), nextLevel, 2 * row, 2 * col + 1);
            //subTiles[1] = new TextureTile(new Sector(p0, p1, t1, t2), nextLevel, 2 * row, 2 * col + 1);

        key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col, nextLevelCacheName);
        subTile = this.getTileFromMemoryCache(key);
        if (subTile != null)
            subTiles[2] = subTile;
        else
        	subTiles[2] = delegate.createTextureTile(new Sector(p1, p2, t0, t1), nextLevel, 2 * row + 1, 2 * col);
            //subTiles[2] = new TextureTile(new Sector(p1, p2, t0, t1), nextLevel, 2 * row + 1, 2 * col);

        key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1, nextLevelCacheName);
        subTile = this.getTileFromMemoryCache(key);
        if (subTile != null)
            subTiles[3] = subTile;
        else
            subTiles[3] = delegate.createTextureTile(new Sector(p1, p2, t1, t2), nextLevel, 2 * row + 1, 2 * col + 1);
        	//subTiles[3] = new TextureTile(new Sector(p1, p2, t1, t2), nextLevel, 2 * row + 1, 2 * col + 1);

        return subTiles;
	}
	
	private TextureTile getTileFromMemoryCache(TileKey tileKey)
    {
        return (TextureTile) getMemoryCache().getObject(tileKey);
    }
}
