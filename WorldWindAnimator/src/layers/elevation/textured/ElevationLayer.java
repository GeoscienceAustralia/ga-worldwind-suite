package layers.elevation.textured;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.media.opengl.GL;

import layers.elevation.ElevationTesselator;
import nasa.worldwind.layers.TiledImageLayer;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class ElevationLayer extends TiledImageLayer
{
	protected static final String CACHE_NAME_PREFIX = "Elevation Layer/";

	protected ExtendedBasicElevationModel elevationModel;

	private int shaderprogram = -1;
	private int minElevationUniform;
	private int maxElevationUniform;
	private int minTexElevationUniform;
	private int maxTexElevationUniform;
	private int opacityUniform;

	public ElevationLayer(ExtendedBasicElevationModel elevationModel)
	{
		super(makeLevels(elevationModel));
		this.elevationModel = elevationModel;
	}

	protected static LevelSet makeLevels(
			ExtendedBasicElevationModel elevationModel)
	{
		LevelSet levels = elevationModel.getLevels();

		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, levels.getLastLevel().getTileWidth());
		params.setValue(AVKey.TILE_HEIGHT, levels.getLastLevel()
				.getTileHeight());
		params.setValue(AVKey.DATA_CACHE_NAME, CACHE_NAME_PREFIX
				+ levels.getLastLevel().getCacheName());
		params.setValue(AVKey.SERVICE, null);
		params.setValue(AVKey.DATASET_NAME, levels.getLastLevel().getDataset());
		params.setValue(AVKey.FORMAT_SUFFIX, ".bil");
		params.setValue(AVKey.NUM_LEVELS, levels.getNumLevels());
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0); //?
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, levels.getFirstLevel()
				.getTileDelta());
		params.setValue(AVKey.SECTOR, levels.getSector());
		params.setValue(AVKey.TILE_URL_BUILDER, null);

		return new LevelSet(params);
	}

	@Override
	public void render(DrawContext dc)
	{
		if (shaderprogram == -1)
		{
			setupShader(dc);
		}
		GL gl = dc.getGL();
		gl.glUseProgram(shaderprogram);

		double minElevation = ((ElevationTesselator) dc.getGlobe()
				.getTessellator()).getMinElevation();
		double maxElevation = ((ElevationTesselator) dc.getGlobe()
				.getTessellator()).getMaxElevation();
		gl.glUniform1f(minElevationUniform, (float) minElevation);
		gl.glUniform1f(maxElevationUniform, (float) maxElevation);

		gl.glUniform1f(opacityUniform, (float) getOpacity());

		super.render(dc);
		gl.glUseProgram(0);
	}

	private void setupShader(DrawContext dc)
	{
		GL gl = dc.getGL();
		int v = gl.glCreateShader(GL.GL_VERTEX_SHADER);
		int f = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
		String vsrc = "", fsrc = "", line;

		try
		{
			BufferedReader brv = new BufferedReader(new InputStreamReader(this
					.getClass().getResourceAsStream("vertexshader.glsl")));
			while ((line = brv.readLine()) != null)
			{
				vsrc += line + "\n";
			}

			BufferedReader brf = new BufferedReader(new InputStreamReader(this
					.getClass().getResourceAsStream("fragmentshader.glsl")));
			while ((line = brf.readLine()) != null)
			{
				fsrc += line + "\n";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		gl.glShaderSource(v, 1, new String[] { vsrc }, new int[] { vsrc
				.length() }, 0);
		gl.glCompileShader(v);
		gl.glShaderSource(f, 1, new String[] { fsrc }, new int[] { fsrc
				.length() }, 0);
		gl.glCompileShader(f);

		shaderprogram = gl.glCreateProgram();
		gl.glAttachShader(shaderprogram, v);
		gl.glAttachShader(shaderprogram, f);
		gl.glLinkProgram(shaderprogram);
		gl.glValidateProgram(shaderprogram);

		gl.glUseProgram(shaderprogram);
		gl.glUniform1i(gl.glGetUniformLocation(shaderprogram, "tex0"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderprogram, "tex1"), 1);
		minElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"minElevation");
		maxElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"maxElevation");
		minTexElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"minTexElevation");
		maxTexElevationUniform = gl.glGetUniformLocation(shaderprogram,
				"maxTexElevation");
		opacityUniform = gl.glGetUniformLocation(shaderprogram, "opacity");
	}

	@Override
	protected void forceTextureLoad(TextureTile tile)
	{
		requestTexture(null, tile);
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
		if (this.getReferencePoint() != null)
			tile.setPriority(centroid.distanceTo3(this.getReferencePoint()));

		RequestTask task = new RequestTask(dc.getGlobe(), tile, this);
		this.getRequestQ().add(task);
	}

	protected void attemptLoad(Globe globe, TextureTile tile)
	{
		if (loadElevations(globe, tile))
		{
			getLevels().unmarkResourceAbsent(tile);
			firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	protected boolean loadElevations(Globe globe, TextureTile tile)
	{
		TileKey[] keys = getTouchingElevationTileKeys(tile);
		if (keys == null)
		{
			getLevels().markResourceAbsent(tile);
			return false;
		}

		boolean allLoaded = true;
		BufferWrapper[] elevations = new BufferWrapper[9];
		for (int i = 0; i < elevations.length; i++)
		{
			if (keys[i] != null)
			{
				elevations[i] = elevationModel.getElevationsFromMemory(keys[i]);
				if (elevations[i] == null)
				{
					elevationModel.requestTile(keys[i]);
					allLoaded = false;
				}
			}
		}

		if (allLoaded)
		{
			double[] minmax = getMinMax(elevations[4]);
			TextureData textureData = convertToTextureData(tile.getLevel()
					.getTileWidth(), tile.getLevel().getTileHeight(), globe,
					elevations, minmax[0], minmax[1]);

			if (textureData == null)
			{
				Logging.logger().severe(
						"Could not create texture data for " + tile);
				getLevels().markResourceAbsent(tile);
				return false;
			}

			if (!(tile instanceof MinMaxTextureTile))
			{
				Logging.logger().severe(
						"Tile is not instance of " + MinMaxTextureTile.class);
				getLevels().markResourceAbsent(tile);
				return false;
			}

			((MinMaxTextureTile) tile).setMinElevation(minmax[0]);
			((MinMaxTextureTile) tile).setMaxElevation(minmax[1]);

			tile.setTextureData(textureData);
			//if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			addTileToCache(tile);
			return true;
		}

		return false;
	}

	protected void addTileToCache(TextureTile tile)
	{
		WorldWind.getMemoryCache(TextureTile.class.getName()).add(
				tile.getTileKey(), tile);
	}

	protected TileKey[] getTouchingElevationTileKeys(TextureTile tile)
	{
		LevelSet elevationLevels = elevationModel.getLevels();
		Level elevationLevel = elevationLevels.getLevel(tile.getLevelNumber());
		if (elevationLevel == null)
			return null;

		//calculate column and row count over the globe for this level
		int colCount = (int) Math.ceil(Angle.POS360.divide(elevationLevel
				.getTileDelta().longitude));
		int rowCount = (int) Math.ceil(Angle.POS180.divide(elevationLevel
				.getTileDelta().latitude));

		TileKey[] keys = new TileKey[9];
		boolean anyValid = false;

		//create a list of 9 tilekeys, 5'th one is the center tile
		for (int i = 0, r = -1; r <= 1; r++)
		{
			for (int c = -1; c <= 1; c++, i++)
			{
				int col = (c + tile.getColumn()) % colCount;
				int row = (r + tile.getRow()) % rowCount;

				TileKey key = new TileKey(tile.getLevelNumber(), row, col,
						elevationLevels.getFirstLevel().getCacheName());
				Sector sector = elevationLevels.computeSectorForKey(key);
				//only add if the sector intersects the levels sector
				if (elevationLevels.getSector().intersects(sector))
				{
					int maxlevel = elevationLevels.getLastLevel(sector)
							.getLevelNumber();
					if (maxlevel >= elevationLevel.getLevelNumber())
					{
						keys[i] = key;
						anyValid = true;
					}
				}
			}
		}

		if (!anyValid || keys[4] == null)
			return null;

		return keys;
	}

	protected double[] getMinMax(BufferWrapper elevations)
	{
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < elevations.length(); i++)
		{
			double value = elevations.getDouble(i);
			if (value != elevationModel.getMissingDataSignal())
			{
				min = Math.min(min, elevations.getDouble(i));
				max = Math.max(max, elevations.getDouble(i));
			}
		}
		return new double[] { min, max };
	}

	protected TextureData convertToTextureData(int width, int height,
			Globe globe, BufferWrapper[] elevations, double minElevation,
			double maxElevation)
	{
		for (BufferWrapper elevs : elevations)
		{
			if (elevs != null && elevs.length() != width * height)
			{
				Logging.logger()
						.severe("Elevations buffer length is incorrect");
				return null;
			}
		}

		BufferWrapper elevs = elevations[4];

		byte[] bytes = new byte[elevs.length()];
		byte[] alpha = new byte[elevs.length()];
		for (int i = 0; i < elevs.length(); i++)
		{
			double value = elevs.getDouble(i);
			bytes[i] = (byte) (255d * (value - minElevation) / (maxElevation - minElevation));
			alpha[i] = (byte) ((value == elevationModel.getMissingDataSignal()) ? 0
					: 255);
		}

		byte[][] bands = new byte[4][];
		bands[0] = bytes;
		bands[1] = bytes;
		bands[2] = bytes;
		bands[3] = alpha;

		DataBuffer db = new DataBufferByte(bands, bands[0].length);
		SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE, width,
				height, bands.length);
		Raster raster = Raster.createRaster(sm, db, null);
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		image.setData(raster);
		return TextureIO.newTextureData(image, isUseMipMaps());
	}

	protected static class RequestTask implements Runnable,
			Comparable<RequestTask>
	{
		private final Globe globe;
		private final ElevationLayer layer;
		private final TextureTile tile;

		public RequestTask(Globe globe, TextureTile tile, ElevationLayer layer)
		{
			this.globe = globe;
			this.layer = layer;
			this.tile = tile;
		}

		public void run()
		{
			layer.attemptLoad(globe, tile);
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

	@Override
	protected TextureTile createTile(Sector sector, Level level, int row,
			int col)
	{
		return new MinMaxTextureTile(this, sector, level, row, col);
	}

	protected static class MinMaxTextureTile extends TextureTile
	{
		private double minElevation = Double.MAX_VALUE;
		private double maxElevation = -Double.MAX_VALUE;
		private final ElevationLayer layer;

		public MinMaxTextureTile(ElevationLayer layer, Sector sector,
				Level level, int row, int col)
		{
			super(sector, level, row, col);
			this.layer = layer;
		}

		public double getMinElevation()
		{
			return minElevation;
		}

		public void setMinElevation(double minElevation)
		{
			this.minElevation = minElevation;
		}

		public double getMaxElevation()
		{
			return maxElevation;
		}

		public void setMaxElevation(double maxElevation)
		{
			this.maxElevation = maxElevation;
		}

		@Override
		public boolean bind(DrawContext dc)
		{
			boolean success = super.bind(dc);

			GL gl = dc.getGL();
			MinMaxTextureTile tile = this;
			while (tile != null)
			{
				if (tile.isTextureInMemory(dc.getTextureCache()))
					break;
				if (tile.getFallbackTile() instanceof MinMaxTextureTile)
					tile = (MinMaxTextureTile) tile.getFallbackTile();
				else
					tile = null;
			}
			if (tile != null)
			{
				gl.glUniform1f(layer.minTexElevationUniform, (float) tile
						.getMinElevation());
				gl.glUniform1f(layer.maxTexElevationUniform, (float) tile
						.getMaxElevation());
			}

			return success;
		}

		protected TextureTile getTileFromMemoryCache(TileKey tileKey)
		{
			return (TextureTile) WorldWind.getMemoryCache(
					TextureTile.class.getName()).getObject(tileKey);
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

			TileKey key = new TileKey(nextLevelNum, 2 * row, 2 * col,
					nextLevelCacheName);
			TextureTile subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[0] = subTile;
			else
				subTiles[0] = new MinMaxTextureTile(layer, new Sector(p0, p1,
						t0, t1), nextLevel, 2 * row, 2 * col);

			key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[1] = subTile;
			else
				subTiles[1] = new MinMaxTextureTile(layer, new Sector(p0, p1,
						t1, t2), nextLevel, 2 * row, 2 * col + 1);

			key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[2] = subTile;
			else
				subTiles[2] = new MinMaxTextureTile(layer, new Sector(p1, p2,
						t0, t1), nextLevel, 2 * row + 1, 2 * col);

			key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[3] = subTile;
			else
				subTiles[3] = new MinMaxTextureTile(layer, new Sector(p1, p2,
						t1, t2), nextLevel, 2 * row + 1, 2 * col + 1);

			return subTiles;
		}
	}
}
