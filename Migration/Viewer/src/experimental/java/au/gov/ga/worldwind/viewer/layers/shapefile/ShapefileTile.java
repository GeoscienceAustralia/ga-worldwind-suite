package au.gov.ga.worldwind.viewer.layers.shapefile;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;

public class ShapefileTile extends Tile implements Renderable
{
	private ShapefileTile fallbackTile;
	private long updateTime = 0;

	private ShapefileTileData data;

	/**
	 * Returns the memory cache used to cache tiles for this class and its
	 * subclasses, initializing the cache if it doesn't yet exist.
	 * 
	 * @return the memory cache associated with the tile.
	 */
	public static synchronized MemoryCache getMemoryCache()
	{
		if (!WorldWind.getMemoryCacheSet().containsCache(ShapefileTile.class.getName()))
		{
			long size = Configuration.getLongValue(AVKey.TEXTURE_IMAGE_CACHE_SIZE, 3000000L);
			MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
			cache.setName("Shapefile Tiles");
			WorldWind.getMemoryCacheSet().addCache(ShapefileTile.class.getName(), cache);
		}

		return WorldWind.getMemoryCacheSet().getCache(ShapefileTile.class.getName());
	}

	public static MemoryCache getDataMemoryCache()
	{
		return ShapefileTileData.getMemoryCache();
	}

	public ShapefileTile(Sector sector)
	{
		super(sector);
	}

	public ShapefileTile(Sector sector, Level level, int row, int column)
	{
		super(sector, level, row, column);
	}

	public Extent getExtent(DrawContext dc)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		return Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), this
				.getSector());
	}

	public boolean isTileExpired()
	{
		return this.isTileExpired(this.getLevel().getExpiryTime());
	}

	public boolean isTileExpired(long expiryTime)
	{
		return this.updateTime > 0 && this.updateTime < expiryTime;
	}

	public void setFallbackTile(ShapefileTile fallbackTile)
	{
		this.fallbackTile = fallbackTile;
	}

	public ShapefileTile getFallbackTile()
	{
		return fallbackTile;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final ShapefileTile tile = (ShapefileTile) o;

		return !(this.getTileKey() != null ? !this.getTileKey().equals(tile.getTileKey()) : tile
				.getTileKey() != null);
	}

	@Override
	public int hashCode()
	{
		return (this.getTileKey() != null ? this.getTileKey().hashCode() : 0);
	}

	@Override
	public String toString()
	{
		return this.getSector().toString();
	}

	public void setData(ShapefileTileData data)
	{
		if (data == null)
		{
			String msg = "ShapefileTileData is null";
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		this.updateTime = System.currentTimeMillis();
		this.data = data;
		updateMemoryCache();
	}

	public boolean isTileInMemory()
	{
		return data != null || getDataMemoryCache().getObject(getTileKey()) != null;
	}

	@Override
	public long getSizeInBytes()
	{
		long size = super.getSizeInBytes();
		if (data != null)
			size += data.getSizeInBytes();
		return size;
	}

	@Override
	public void render(DrawContext dc)
	{
		ShapefileTileData data = this.data;
		if (data != null)
		{
			getDataMemoryCache().add(getTileKey(), data);
		}
		else
		{
			data = (ShapefileTileData) getDataMemoryCache().getObject(getTileKey());
		}

		if (data != null)
			data.render(dc);
	}

	private ShapefileTile getTileFromMemoryCache(TileKey tileKey)
	{
		return (ShapefileTile) getMemoryCache().getObject(tileKey);
	}

	private void updateMemoryCache()
	{
		if (getTileFromMemoryCache(getTileKey()) != null)
			getMemoryCache().add(getTileKey(), this);
	}

	public ShapefileTile[] createSubTiles(Level nextLevel)
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

		ShapefileTile[] subTiles = new ShapefileTile[4];

		TileKey key = new TileKey(nextLevelNum, 2 * row, 2 * col, nextLevelCacheName);
		ShapefileTile subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[0] = subTile;
		else
			subTiles[0] =
					new ShapefileTile(new Sector(p0, p1, t0, t1), nextLevel, 2 * row, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[1] = subTile;
		else
			subTiles[1] =
					new ShapefileTile(new Sector(p0, p1, t1, t2), nextLevel, 2 * row, 2 * col + 1);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[2] = subTile;
		else
			subTiles[2] =
					new ShapefileTile(new Sector(p1, p2, t0, t1), nextLevel, 2 * row + 1, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[3] = subTile;
		else
			subTiles[3] =
					new ShapefileTile(new Sector(p1, p2, t1, t2), nextLevel, 2 * row + 1,
							2 * col + 1);

		return subTiles;
	}
}
