package au.gov.ga.worldwind.viewer.layers.shapefile.textured;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import java.awt.image.BufferedImage;

import javax.media.opengl.GLProfile;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

public class ShapefileTile extends TextureTile
{
	private TextureTile pickingTile;
	private String path;

	public ShapefileTile(Sector sector, Level level, int row, int col)
	{
		super(sector, level, row, col);
		pickingTile = new TextureTile(sector, level, row, col);
	}

	public void setShapefile(Shapefile shapefile, boolean useMipMaps)
	{
		BufferedImage image = ImageTessellator.tessellate(shapefile, getWidth(), getHeight(), getSector());
		TextureData data = AWTTextureIO.newTextureData(GLProfile.get(GLProfile.GL2), image, useMipMaps);
		setTextureData(data);
	}

	@Override
	public String getPath()
	{
		//TODO change the path when the tile's level is greater than number of shapefile levels

		if (this.path == null)
		{
			this.path = getLevel().getPath() + "/" + getRow() + "/" + getRow() + "_" + getColumn();
			if (!getLevel().isEmpty())
			{
				path += getLevel().getFormatSuffix();
			}
		}

		return this.path;
	}


	@Override
	public void applyInternalTransform(DrawContext dc, boolean textureIdentityActive)
	{
		if (dc.isPickingMode())
		{
			pickingTile.applyInternalTransform(dc, textureIdentityActive);
		}
		else
		{
			super.applyInternalTransform(dc, textureIdentityActive);
		}
	}

	@Override
	public boolean bind(DrawContext dc)
	{
		if (dc.isPickingMode())
		{
			return pickingTile.bind(dc);
		}
		else
		{
			return super.bind(dc);
		}
	}

	@Override
	public void setFallbackTile(TextureTile fallbackTile)
	{
		super.setFallbackTile(fallbackTile);
		pickingTile.setFallbackTile(fallbackTile);
	}

	//*****************************************************************
	//Below here is copied (with slight modifications) from TextureTile
	//*****************************************************************

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
		{
			subTiles[0] = subTile;
		}
		else
		{
			subTiles[0] = new ShapefileTile(new Sector(p0, p1, t0, t1), nextLevel, 2 * row, 2 * col);
		}

		key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
		{
			subTiles[1] = subTile;
		}
		else
		{
			subTiles[1] = new ShapefileTile(new Sector(p0, p1, t1, t2), nextLevel, 2 * row, 2 * col + 1);
		}

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
		{
			subTiles[2] = subTile;
		}
		else
		{
			subTiles[2] = new ShapefileTile(new Sector(p1, p2, t0, t1), nextLevel, 2 * row + 1, 2 * col);
		}

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1, nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
		{
			subTiles[3] = subTile;
		}
		else
		{
			subTiles[3] = new ShapefileTile(new Sector(p1, p2, t1, t2), nextLevel, 2 * row + 1, 2 * col + 1);
		}

		return subTiles;
	}

	@Override
	protected TextureTile getTileFromMemoryCache(TileKey tileKey)
	{
		return (TextureTile) getMemoryCache().getObject(tileKey);
	}
}
