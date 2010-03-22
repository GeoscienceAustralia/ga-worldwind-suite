package au.gov.ga.worldwind.layers.local;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import au.gov.ga.worldwind.util.Util;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class LocalLayer extends TiledImageLayer
{
	private final Object fileLock = new Object();
	private final LocalLayerDefinition definition;
	private final File directory;

	public LocalLayer(LocalLayerDefinition definition)
	{
		super(makeLevels(definition));
		this.definition = definition;
		directory = new File(definition.getDirectory());
		setName(definition.getName());
		setUseMipMaps(true);
		setUseTransparentTextures(true);
	}

	public LocalLayerDefinition getDefinition()
	{
		return definition;
	}

	protected static LevelSet makeLevels(LocalLayerDefinition definition)
	{
		Sector sector = Sector.fromDegrees(definition.getMinLat(), definition
				.getMaxLat(), definition.getMinLon(), definition.getMaxLon());

		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, definition.getTilesize());
		params.setValue(AVKey.TILE_HEIGHT, definition.getTilesize());
		params.setValue(AVKey.DATA_CACHE_NAME, "Local/" + definition.getName()
				+ "/" + randomString(8));
		params.setValue(AVKey.SERVICE, null);
		params.setValue(AVKey.DATASET_NAME, definition.getName());
		params.setValue(AVKey.FORMAT_SUFFIX, "." + definition.getExtension());
		params.setValue(AVKey.NUM_LEVELS, definition.getLevelcount());
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, LatLon.fromDegrees(
				definition.getLztsd(), definition.getLztsd()));
		params.setValue(AVKey.SECTOR, sector);
		params.setValue(AVKey.TILE_URL_BUILDER, new TileUrlBuilder()
		{
			public URL getURL(Tile tile, String imageFormat)
					throws MalformedURLException
			{
				return null;
			}
		});

		return new LevelSet(params);
	}

	private static String randomString(int length)
	{
		String chars = new String(
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
		{
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	@Override
	protected void forceTextureLoad(TextureTile tile)
	{
		File file = getTileFile(tile);
		if (file.exists())
		{
			loadTexture(tile, file);
		}
	}

	protected void requestTextureLoad(TextureTile tile)
	{
		File file = getTileFile(tile);
		if (file.exists())
		{
			loadTexture(tile, file);
			getLevels().unmarkResourceAbsent(tile);
			firePropertyChange(AVKey.LAYER, null, this);
		}
		else
		{
			getLevels().markResourceAbsent(tile);
		}
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
		if (this.getReferencePoint() != null)
			tile.setPriority(centroid.distanceTo3(this.getReferencePoint()));

		RequestTask task = new RequestTask(tile, this);
		this.getRequestQ().add(task);
	}

	private File getTileFile(TextureTile tile)
	{
		return new File(directory, tile.getLevelNumber() + File.separator
				+ Util.paddedInt(tile.getRow(), 4) + File.separator
				+ Util.paddedInt(tile.getRow(), 4) + "_"
				+ Util.paddedInt(tile.getColumn(), 4) + "."
				+ definition.getExtension());
	}

	private boolean loadTexture(TextureTile tile, File file)
	{
		TextureData textureData = null;

		synchronized (this.fileLock)
		{
			try
			{
				textureData = readTexture(file.toURI().toURL(), this
						.isUseMipMaps());
			}
			catch (MalformedURLException e)
			{
			}
		}

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			this.addTileToCache(tile);

		return true;
	}

	private TextureData readTexture(java.net.URL url, boolean useMipMaps)
	{
		try
		{
			Color transparent = definition.getTransparentColor();
			if (!definition.isHasTransparentColor() || transparent == null)
				return TextureIO.newTextureData(url, useMipMaps, null);

			int fuzz = Math.max(0, definition.getTransparentFuzz() * 255 / 100);
			BufferedImage image = ImageIO.read(url);
			BufferedImage trans = new BufferedImage(image.getWidth(), image
					.getHeight(), BufferedImage.TYPE_INT_ARGB);

			for (int x = 0; x < image.getWidth(); x++)
			{
				for (int y = 0; y < image.getHeight(); y++)
				{
					int rgb = image.getRGB(x, y);
					int sr = (rgb >> 16) & 0xff;
					int sg = (rgb >> 8) & 0xff;
					int sb = (rgb >> 0) & 0xff;
					int cr = transparent.getRed();
					int cg = transparent.getGreen();
					int cb = transparent.getBlue();
					if (cr - fuzz <= sr && sr <= cr + fuzz && cg - fuzz <= sg
							&& sg <= cg + fuzz && cb - fuzz <= sb
							&& sb <= cb + fuzz)
					{
						rgb = (rgb & 0xffffff);
					}
					trans.setRGB(x, y, rgb);
				}
			}
			return TextureIO.newTextureData(trans, useMipMaps);
		}
		catch (Exception e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					"layers.TextureLayer.ExceptionAttemptingToReadTextureFile",
					e);
			return null;
		}
	}

	private void addTileToCache(TextureTile tile)
	{
		WorldWind.getMemoryCache(TextureTile.class.getName()).add(
				tile.getTileKey(), tile);
	}

	protected void setBlendingFunction(DrawContext dc)
	{
		GL gl = dc.getGL();
		double alpha = this.getOpacity();
		gl.glColor4d(alpha, alpha, alpha, alpha);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}

	private static class RequestTask implements Runnable,
			Comparable<RequestTask>
	{
		private final LocalLayer layer;
		private final TextureTile tile;

		private RequestTask(TextureTile tile, LocalLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		public void run()
		{
			layer.requestTextureLoad(tile);
		}

		public int compareTo(RequestTask that)
		{
			if (that == null)
			{
				String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}
			return this.tile.getPriority() == that.tile.getPriority() ? 0
					: this.tile.getPriority() < that.tile.getPriority() ? -1
							: 1;
		}

		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RequestTask that = (RequestTask) o;

			// Don't include layer in comparison so that requests are shared among layers
			return !(tile != null ? !tile.equals(that.tile) : that.tile != null);
		}

		public int hashCode()
		{
			return (tile != null ? tile.hashCode() : 0);
		}

		public String toString()
		{
			return this.tile.toString();
		}
	}
}
