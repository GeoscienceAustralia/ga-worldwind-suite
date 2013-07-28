/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.layers.multitexture;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.cache.TextureCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

/**
 * @author tag
 * @version $Id: TextureTile.java 5215 2008-04-30 04:37:46Z tgaskins $
 */
public class MultiTextureTile extends Tile implements SurfaceTile
{
	private volatile TextureData[] textureData;
	private MultiTextureTile fallbackTile = null; // holds texture to use if own texture not available
	private Vec4 centroid; // Cartesian coordinate of lat/lon center
	private Extent extent = null; // bounding volume
	private double extentVerticalExaggertion = Double.MIN_VALUE; // VE used to calculate the extent
	private Globe globe;
	private Object globeStateKey;
	private double minDistanceToEye = Double.MAX_VALUE;
	private int textureCount = 0;

	public MultiTextureTile(Sector sector)
	{
		super(sector);
	}

	public MultiTextureTile(Sector sector, Level level, int row, int col)
	{
		super(sector, level, row, col);
	}

	@Override
	public final long getSizeInBytes()
	{
		long size = super.getSizeInBytes();

		if (this.textureData != null)
			for (TextureData td : textureData)
				size += td.getEstimatedMemorySize();

		return size;
	}

	public MultiTextureTile getFallbackTile()
	{
		return this.fallbackTile;
	}

	public void setFallbackTile(MultiTextureTile fallbackTile)
	{
		this.fallbackTile = fallbackTile;
	}

	public TextureData[] getTextureData()
	{
		return this.textureData;
	}

	public void setTextureData(TextureData textureData)
	{
		setTextureData(new TextureData[] { textureData });
	}

	public void setTextureData(TextureData[] textureData)
	{
		this.textureData = textureData;
		textureCount = textureData.length;
	}

	public int getTextureCount()
	{
		return textureCount;
	}

	private Object getIndexKey(int index)
	{
		return this.getTileKey().hashCode() * 29 + index;
	}

	public Texture[] getTextures(TextureCache tc)
	{
		if (tc == null)
		{
			String message = Logging.getMessage("nullValue.TextureCacheIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (textureCount <= 0)
			return null;

		Texture[] textures = new Texture[textureCount];
		for (int i = 0; i < textureCount; i++)
		{
			textures[i] = tc.get(getIndexKey(i));
			if (textures[i] == null)
				return null;
		}
		return textures;
	}

	public boolean areTexturesInMemory(TextureCache tc)
	{
		if (tc == null)
		{
			String message = Logging.getMessage("nullValue.TextureCacheIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		return this.getTextures(tc) != null || (this.getTextureData() != null);
	}

	public void setTextures(TextureCache tc, Texture[] textures)
	{
		if (tc == null)
		{
			String message = Logging.getMessage("nullValue.TextureCacheIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		if (textures.length != textureCount)
		{
			throw new IllegalStateException("Texture count is incorrect");
		}

		for (int i = 0; i < textures.length; i++)
		{
			tc.put(getIndexKey(i), textures[i]);
		}

		// No more need for texture data; allow garbage collector and memory cache to reclaim it.
		this.textureData = null;
		this.updateMemoryCache();
	}

	public Vec4 getCentroidPoint(Globe globe)
	{
		if (globe == null)
		{
			String msg = Logging.getMessage("nullValue.GlobeIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (this.centroid == null)
		{
			LatLon c = this.getSector().getCentroid();
			this.centroid = globe.computePointFromPosition(c.getLatitude(), c
					.getLongitude(), 0);
		}

		return this.centroid;
	}

	public double getMinDistanceToEye()
	{
		return this.minDistanceToEye;
	}

	public void setMinDistanceToEye(double minDistanceToEye)
	{
		if (minDistanceToEye < 0)
		{
			String msg = Logging
					.getMessage("layers.TextureTile.MinDistanceToEyeNegative");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		this.minDistanceToEye = minDistanceToEye;
	}

	public Extent getExtent(DrawContext dc)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (this.extent == null || !this.isExtentValid(dc))
		{
			this.extent = dc.getGlobe().computeBoundingCylinder(
					dc.getVerticalExaggeration(), this.getSector());
			this.extentVerticalExaggertion = dc.getVerticalExaggeration();
			this.globe = dc.getGlobe();
			this.globeStateKey = this.globe != null ? this.globe.getStateKey()
					: null;
			this.centroid = null;
		}

		return this.extent;
	}

	private boolean isExtentValid(DrawContext dc)
	{
		return !(dc.getGlobe() == null || this.globe == null || this.globeStateKey == null)
				&& this.extentVerticalExaggertion == dc
						.getVerticalExaggeration()
				&& this.globe == dc.getGlobe()
				&& this.globeStateKey.equals(dc.getGlobe().getStateKey());

	}

	public MultiTextureTile[] createSubTiles(Level nextLevel)
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

		MultiTextureTile[] subTiles = new MultiTextureTile[4];

		TileKey key = new TileKey(nextLevelNum, 2 * row, 2 * col,
				nextLevelCacheName);
		MultiTextureTile subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[0] = subTile;
		else
			subTiles[0] = new MultiTextureTile(new Sector(p0, p1, t0, t1),
					nextLevel, 2 * row, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1,
				nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[1] = subTile;
		else
			subTiles[1] = new MultiTextureTile(new Sector(p0, p1, t1, t2),
					nextLevel, 2 * row, 2 * col + 1);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col,
				nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[2] = subTile;
		else
			subTiles[2] = new MultiTextureTile(new Sector(p1, p2, t0, t1),
					nextLevel, 2 * row + 1, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1,
				nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[3] = subTile;
		else
			subTiles[3] = new MultiTextureTile(new Sector(p1, p2, t1, t2),
					nextLevel, 2 * row + 1, 2 * col + 1);

		return subTiles;
	}

	private MultiTextureTile getTileFromMemoryCache(TileKey tileKey)
	{
		return (MultiTextureTile) WorldWind.getMemoryCache(
				MultiTextureTile.class.getName()).getObject(tileKey);
	}

	private void updateMemoryCache()
	{
		if (this.getTileFromMemoryCache(this.getTileKey()) != null)
			WorldWind.getMemoryCache(MultiTextureTile.class.getName()).add(
					this.getTileKey(), this);
	}

	private Texture[] initializeTextures(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Texture[] t = this.getTextures(dc.getTextureCache());
		if (t != null)
			return t;

		if (this.getTextureData() == null)
		{
			String msg = Logging.getMessage("nullValue.TextureDataIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		boolean[] usingMipmaps = new boolean[textureCount];
		try
		{
			TextureData[] textureData = this.getTextureData();
			t = new Texture[textureCount];
			for (int i = 0; i < textureCount; i++)
			{
				usingMipmaps[i] = textureData[i].getMipmapData() != null;
				t[i] = TextureIO.newTexture(textureData[i]);
			}
		}
		catch (Exception e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					"layers.TextureLayer.ExceptionAttemptingToReadTextureFile",
					e);
			return null;
		}

		this.setTextures(dc.getTextureCache(), t);

		for (int i = 0; i < textureCount; i++)
		{
			t[i].bind();

			GL gl = dc.getGL().getGL2();
			if (usingMipmaps[i]
					&& this.getSector().getMaxLatitude().degrees < 80d
					&& this.getSector().getMinLatitude().degrees > -80)
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
						GL.GL_LINEAR_MIPMAP_LINEAR);
			else
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
						GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
					GL.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
					GL.GL_CLAMP_TO_EDGE);
		}

		return t;
	}

	public boolean bind(DrawContext dc)
	{
		return bind(dc, false, 0);
	}

	public boolean bind(DrawContext dc, int unit)
	{
		return bind(dc, true, unit);
	}

	private boolean bind(DrawContext dc, boolean multi, int unit)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Texture[] t = this.getTextures(dc.getTextureCache());
		if (t == null && this.getTextureData() != null)
		{
			t = this.initializeTextures(dc);
			if (t != null && !multi)
				return true; // texture was bound during initialization.
		}

		if (t == null && this.getFallbackTile() != null)
		{
			MultiTextureTile resourceTile = this.getFallbackTile();
			t = resourceTile.getTextures(dc.getTextureCache());
			if (t == null)
			{
				t = resourceTile.initializeTextures(dc);
				if (t != null && !multi)
					return true; // texture was bound during initialization.
			}
		}

		if (t != null)
		{
			if (multi)
			{
				GL gl = dc.getGL().getGL2();
				for (int i = 1; i < t.length; i++)
				{
					gl.glActiveTexture(unit + i - 1);
					t[i].bind();
				}
				gl.glActiveTexture(GL.GL_TEXTURE0);
				t[0].bind();
			}
			else
			{
				t[0].bind();
			}
		}

		return t != null;
	}

	public void applyInternalTransform(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		// Use the tile's texture if available.
		Texture[] t = this.getTextures(dc.getTextureCache());
		if (t == null && this.getTextureData() != null)
			t = this.initializeTextures(dc);

		if (t != null)
		{
			if (t[0].getMustFlipVertically())
			{
				GL gl = GLContext.getCurrent().getGL();
				gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glLoadIdentity();
				gl.glScaled(1, -1, 1);
				gl.glTranslated(0, -1, 0);
			}
			return;
		}

		// Use the tile's fallback texture if its primary texture is not available.
		MultiTextureTile resourceTile = this.getFallbackTile();
		if (resourceTile == null) // no fallback specified
			return;

		t = resourceTile.getTextures(dc.getTextureCache());
		if (t == null && resourceTile.getTextureData() != null)
			t = resourceTile.initializeTextures(dc);

		if (t == null) // was not able to initialize the fallback texture
			return;

		// Apply necessary transforms to the fallback texture.
		GL gl = GLContext.getCurrent().getGL();
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glLoadIdentity();

		if (t[0].getMustFlipVertically())
		{
			gl.glScaled(1, -1, 1);
			gl.glTranslated(0, -1, 0);
		}

		this.applyResourceTextureTransform(dc);
	}

	private void applyResourceTextureTransform(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (this.getLevel() == null)
			return;

		int levelDelta = this.getLevelNumber()
				- this.getFallbackTile().getLevelNumber();
		if (levelDelta <= 0)
			return;

		double twoToTheN = Math.pow(2, levelDelta);
		double oneOverTwoToTheN = 1 / twoToTheN;

		double sShift = oneOverTwoToTheN * (this.getColumn() % twoToTheN);
		double tShift = oneOverTwoToTheN * (this.getRow() % twoToTheN);

		dc.getGL().getGL2().glTranslated(sShift, tShift, 0);
		dc.getGL().getGL2().glScaled(oneOverTwoToTheN, oneOverTwoToTheN, 1);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final MultiTextureTile tile = (MultiTextureTile) o;

		return !(this.getTileKey() != null ? !this.getTileKey().equals(
				tile.getTileKey()) : tile.getTileKey() != null);
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
}
