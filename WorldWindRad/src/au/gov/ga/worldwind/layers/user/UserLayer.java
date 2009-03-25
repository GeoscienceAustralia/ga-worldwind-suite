package au.gov.ga.worldwind.layers.user;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class UserLayer extends TiledImageLayer
{
	private final Object fileLock = new Object();
	private final UserLayerDefinition definition;
	private final File directory;

	public UserLayer(UserLayerDefinition definition)
	{
		super(makeLevels(definition));
		this.definition = definition;
		directory = new File(definition.getDirectory());
		setName(definition.getName());
	}

	public UserLayerDefinition getDefinition()
	{
		return definition;
	}

	public static int levelCount(File directory)
	{
		int levels = -1;
		for (int i = 0; true; i++)
		{
			File leveldir = new File(directory, String.valueOf(i));
			if (!leveldir.exists())
				break;
			levels++;
		}
		return levels;
	}

	public static Sector sector(File directory, String extension, int levels,
			double lztsd)
	{
		int level = levels - 1;
		File lastLevelDirectory = new File(directory, String.valueOf(level));
		MinMax rowMinMax = getMinMaxRow(lastLevelDirectory);
		File firstRowDirectory = new File(lastLevelDirectory, paddedInt(
				rowMinMax.min, 4));
		MinMax colMinMax = getMinMaxCol(firstRowDirectory, extension);
		return Sector.fromDegrees(getTileLat(rowMinMax.min, level, lztsd),
				getTileLat(rowMinMax.max + 1, level, lztsd), getTileLon(
						colMinMax.min, level, lztsd), getTileLon(
						colMinMax.max + 1, level, lztsd));
	}

	private static class MinMax
	{
		private int min = Integer.MAX_VALUE;
		private int max = Integer.MIN_VALUE;
	}

	private static MinMax getMinMaxRow(File directory)
	{
		MinMax minmax = new MinMax();
		String[] list = directory.list();
		boolean match = false;
		for (String file : list)
		{
			if (file.matches("\\d+"))
			{
				match = true;
				int row = Integer.parseInt(file);
				minmax.min = Math.min(minmax.min, row);
				minmax.max = Math.max(minmax.max, row);
			}
		}
		return match ? minmax : null;
	}

	private static MinMax getMinMaxCol(File directory, String extension)
	{
		MinMax minmax = new MinMax();
		String[] list = directory.list();
		boolean match = false;
		for (String file : list)
		{
			if (file.toLowerCase().matches(
					"\\d+\\_\\d+\\Q." + extension.toLowerCase() + "\\E"))
			{
				Pattern pattern = Pattern.compile("\\d+");
				Matcher matcher = pattern.matcher(file);
				if (matcher.find() && matcher.find(matcher.end()))
				{
					match = true;
					int col = Integer.parseInt(matcher.group());
					minmax.min = Math.min(minmax.min, col);
					minmax.max = Math.max(minmax.max, col);
				}
			}
		}
		return match ? minmax : null;
	}

	private static double getTileLon(int col, int level, double lztsd)
	{
		double levelpow = Math.pow(0.5, level);
		return col * lztsd * levelpow - 180;
	}

	private static double getTileLat(int row, int level, double lztsd)
	{
		double levelpow = Math.pow(0.5, level);
		return row * lztsd * levelpow - 90;
	}

	private static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}

	protected static LevelSet makeLevels(UserLayerDefinition definition)
	{
		File directory = new File(definition.getDirectory());
		int levels = levelCount(directory);
		Sector sector = sector(directory, definition.getExtension(), levels,
				definition.getLztsd());

		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, definition.getTilesize());
		params.setValue(AVKey.TILE_HEIGHT, definition.getTilesize());
		params.setValue(AVKey.DATA_CACHE_NAME, "User/" + definition.getName());
		params.setValue(AVKey.SERVICE, null);
		params.setValue(AVKey.DATASET_NAME, definition.getName());
		params.setValue(AVKey.FORMAT_SUFFIX, "." + definition.getExtension());
		params.setValue(AVKey.NUM_LEVELS, levels);
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
				+ paddedInt(tile.getRow(), 4) + File.separator
				+ paddedInt(tile.getRow(), 4) + "_"
				+ paddedInt(tile.getColumn(), 4) + "."
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
		private final UserLayer layer;
		private final TextureTile tile;

		private RequestTask(TextureTile tile, UserLayer layer)
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
