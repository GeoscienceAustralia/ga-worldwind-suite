package au.gov.ga.worldwind.layers.nearestneighbor;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import javax.media.opengl.GL;

import com.sun.opengl.util.texture.Texture;

public class NearestNeighborTextureTile extends TextureTile
{
	private int numLevels = 0;

	public NearestNeighborTextureTile(Sector sector, Level level, int row, int col)
	{
		super(sector, level, row, col);

		AVList params = level.getParams();
		Object o = params.getValue(AVKey.NUM_LEVELS);
		if (o != null && o instanceof Integer)
			numLevels = (Integer) o;
	}

	//*****************************************************************
	//Below here is copied (with slight modifications) from TextureTile
	//*****************************************************************

	@Override
	protected void setTextureParameters(DrawContext dc, Texture t)
	{
		super.setTextureParameters(dc, t);

		//set the magnification filter to nearest neighbor if this tile's level is the layer's last level
		if ((numLevels > 0 && getLevelNumber() >= numLevels - 1)
				|| dc.getCurrentLayer().isAtMaxResolution())
		{
			dc.getGL().glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		}
	}

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
			subTiles[0] =
					new NearestNeighborTextureTile(new Sector(p0, p1, t0, t1), nextLevel, 2 * row,
							2 * col);

		key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[1] = subTile;
		else
			subTiles[1] =
					new NearestNeighborTextureTile(new Sector(p0, p1, t1, t2), nextLevel, 2 * row,
							2 * col + 1);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[2] = subTile;
		else
			subTiles[2] =
					new NearestNeighborTextureTile(new Sector(p1, p2, t0, t1), nextLevel,
							2 * row + 1, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[3] = subTile;
		else
			subTiles[3] =
					new NearestNeighborTextureTile(new Sector(p1, p2, t1, t2), nextLevel,
							2 * row + 1, 2 * col + 1);

		return subTiles;
	}

	private TextureTile getTileFromMemoryCache(TileKey tileKey)
	{
		return (TextureTile) getMemoryCache().getObject(tileKey);
	}
}
