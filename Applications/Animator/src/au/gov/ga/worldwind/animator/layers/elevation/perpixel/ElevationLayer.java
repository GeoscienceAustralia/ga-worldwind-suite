package au.gov.ga.worldwind.animator.layers.elevation.perpixel;

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
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import java.net.URL;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import nasa.worldwind.layers.TiledImageLayer;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public abstract class ElevationLayer extends TiledImageLayer
{
	protected ExtendedElevationModel elevationModel;

	protected double minElevationClamp = -Double.MAX_VALUE;
	protected double maxElevationClamp = Double.MAX_VALUE;

	protected Object fileLock = new Object();

	private DrawContext dc = null;

	public ElevationLayer(ExtendedElevationModel elevationModel,
			String cacheNamePrefix, String formatSuffix)
	{
		this(elevationModel, cacheNamePrefix, formatSuffix, null);
	}

	public ElevationLayer(ExtendedElevationModel elevationModel,
			String cacheNamePrefix, String formatSuffix, Sector sector)
	{
		super(makeLevels(elevationModel, sector, cacheNamePrefix, formatSuffix));
		this.elevationModel = elevationModel;
		setUseTransparentTextures(true);
		setUseMipMaps(true);
	}

	protected static LevelSet makeLevels(
			ExtendedElevationModel elevationModel, Sector sector,
			String cacheNamePrefix, String formatSuffix)
	{
		LevelSet levels = ((BasicElevationModel)elevationModel).getLevels();

		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, levels.getLastLevel().getTileWidth());
		params.setValue(AVKey.TILE_HEIGHT, levels.getLastLevel()
				.getTileHeight());
		params.setValue(AVKey.DATA_CACHE_NAME, cacheNamePrefix
				+ levels.getLastLevel().getCacheName());
		params.setValue(AVKey.SERVICE, null);
		params.setValue(AVKey.DATASET_NAME, levels.getLastLevel().getDataset());
		params.setValue(AVKey.FORMAT_SUFFIX, formatSuffix);
		params.setValue(AVKey.NUM_LEVELS, levels.getNumLevels());
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0); //?
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, levels.getFirstLevel()
				.getTileDelta());
		params.setValue(AVKey.SECTOR, sector == null ? levels.getSector()
				: sector);
		params.setValue(AVKey.TILE_URL_BUILDER, null);

		return new LevelSet(params);
	}

	protected abstract void setupShader(DrawContext dc);

	protected abstract void packupShader(DrawContext dc);

	protected abstract boolean loadTexture(TextureTile tile, URL textureURL);

	protected abstract boolean handleElevations(Globe globe, TextureTile tile,
			Sector sector, BufferWrapper[] elevations);

	@Override
	public void render(DrawContext dc)
	{
		this.dc = dc;
		
		if (!this.isEnabled())
			return;

		setupShader(dc);
		super.render(dc);
		packupShader(dc);
	}

	protected void setupTexture(DrawContext dc, Texture texture)
	{
		//do nothing (overridable)
	}

	protected Texture createTexture(TextureTile tile)
	{
		return TextureIO.newTexture(tile.getTextureData());
	}

	@Override
	protected void forceTextureLoad(TextureTile tile)
	{
		if (dc == null)
			return;
		attemptLoad(dc.getGlobe(), tile);
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		if (ImmediateMode.isImmediate())
		{
			attemptLoad(dc.getGlobe(), tile);
			return;
		}

		Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
        Vec4 referencePoint = this.getReferencePoint(dc);
        if (referencePoint != null)
            tile.setPriority(centroid.distanceTo3(referencePoint));

		RequestTask task = new RequestTask(dc.getGlobe(), tile, this);
		this.getRequestQ().add(task);
	}

	protected void attemptLoad(Globe globe, TextureTile tile)
	{
		URL textureURL = WorldWind.getDataFileStore().findFile(tile.getPath(),
				false);
		if (textureURL != null)
		{
			if (loadTexture(tile, textureURL))
			{
				return;
			}
		}

		if (requestElevations(globe, tile))
		{
			if (!ImmediateMode.isImmediate())
			{
				firePropertyChange(AVKey.LAYER, null, this);
			}
			else
			{
				textureURL = WorldWind.getDataFileStore().findFile(
						tile.getPath(), false);
				if (textureURL != null)
				{
					loadTexture(tile, textureURL);
				}
			}
		}
	}

	protected boolean requestElevations(Globe globe, TextureTile tile)
	{
		TileKey[] keys = new TileKey[9];
		Sector[] sectors = new Sector[9];
		boolean anyTouching = fillTouchingElevationTileKeys(tile, keys, sectors);
		if (!anyTouching)
		{
			getLevels().markResourceAbsent(tile);
			return false;
		}

		int width = tile.getLevel().getTileWidth();
		int height = tile.getLevel().getTileHeight();
		boolean allLoaded = true;
		BufferWrapper[] elevations = new BufferWrapper[9];
		for (int i = 0; i < elevations.length; i++)
		{
			if (keys[i] != null)
			{
				elevations[i] = getElevationsFromMemory(keys[i]);
				if (elevations[i] == null)
				{
					allLoaded = false;
				}
				else if (elevations[i].length() != width * height)
				{
					Logging.logger().severe(
							"Elevations array has incorrect length");
					getLevels().markResourceAbsent(tile);
					return false;
				}
			}
		}

		if (!allLoaded)
			return false;

		return handleElevations(globe, tile, sectors[4], elevations);
	}

	protected BufferWrapper getElevationsFromMemory(TileKey key)
	{
		BufferWrapper elevations = elevationModel.getElevationsFromMemory(key);
		if (elevations == null)
		{
			elevationModel.requestTile(key);
			if (ImmediateMode.isImmediate())
			{
				elevations = elevationModel.getElevationsFromMemory(key);
			}
		}
		return elevations;
	}

	protected void addTileToCache(TextureTile tile)
	{
		WorldWind.getMemoryCache(TextureTile.class.getName()).add(
				tile.getTileKey(), tile);
	}

	protected boolean fillTouchingElevationTileKeys(TextureTile tile,
			TileKey[] keys, Sector[] sectors)
	{
		if (keys.length != 9 || sectors.length != 9)
			throw new IllegalArgumentException("Illegal array length");

		LevelSet elevationLevels = ((BasicElevationModel)elevationModel).getLevels();
		Level elevationLevel = elevationLevels.getLevel(tile.getLevelNumber());
		if (elevationLevel == null)
			return false;

		//calculate column and row count over the globe for this level
		int colCount = (int) Math.ceil(Angle.POS360.divide(elevationLevel
				.getTileDelta().longitude));
		int rowCount = (int) Math.ceil(Angle.POS180.divide(elevationLevel
				.getTileDelta().latitude));

		//fill list of 9 tilekeys, 5'th one is the center tile
		boolean anyValid = false;
		for (int i = 0, r = -1; r <= 1; r++)
		{
			for (int c = -1; c <= 1; c++, i++)
			{
				int col = (c + tile.getColumn() + colCount) % colCount;
				int row = (r + tile.getRow() + rowCount) % rowCount;

				TileKey key = new TileKey(tile.getLevelNumber(), row, col,
						elevationLevel.getCacheName());
				sectors[i] = elevationLevels.computeSectorForKey(key);
				//only add if the sector intersects the levels sector
				if (elevationLevels.getSector().intersects(sectors[i]))
				{
					int maxlevel = elevationLevels.getLastLevel(sectors[i])
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
			return false;

		return true;
	}

	protected double[] calculatePaddedElevations(int width, int height,
			int padding, BufferWrapper[] elevations, double missingDataSignal)
	{
		if (padding > width || padding > height)
			throw new IllegalArgumentException(
					"Padding cannot be greater than width/height");

		int padding2 = padding * 2;
		double[] paddedElevations = new double[(width + padding2)
				* (height + padding2)];

		for (int y = -padding; y < height + padding; y++)
		{
			for (int x = -padding; x < width + padding; x++)
			{
				paddedElevations[getArrayIndex(width + padding2, height
						+ padding2, x + padding, y + padding)] = getValue(
						width, height, x, y, elevations, missingDataSignal);
			}
		}

		return paddedElevations;
	}

	protected Vec4[] calculateTileVerts(int width, int height, Globe globe,
			Sector sector, double[] paddedElevations, double missingDataSignal,
			int padding, double bakedExaggeration)
	{
		int padding2 = padding * 2;
		int size = (width + padding2) * (height + padding2);

		if (paddedElevations.length != size)
		{
			throw new IllegalArgumentException(
					"Illegal paddedElevations length");
		}

		Vec4[] verts = new Vec4[size];
		double dlon = sector.getDeltaLonDegrees() / width;
		double dlat = sector.getDeltaLatDegrees() / height;

		for (int y = 0; y < height + padding2; y++)
		{
			Angle lat = sector.getMaxLatitude().subtractDegrees(
					dlat * (y - padding));
			for (int x = 0; x < width + padding2; x++)
			{
				Angle lon = sector.getMinLongitude().addDegrees(
						dlon * (x - padding));
				//only add if lat/lon is inside level's sector
				if (this.getLevels().getSector().contains(lat, lon))
				{
					int index = getArrayIndex(width + padding2, height
							+ padding2, x, y);
					double elevation = paddedElevations[index];
					if (elevation != missingDataSignal
							&& elevation >= minElevationClamp
							&& elevation <= maxElevationClamp)
					{
						verts[index] = globe.computePointFromPosition(lat, lon,
								elevation * bakedExaggeration);
					}
				}
			}
		}

		return verts;
	}

	protected double getValue(int width, int height, int x, int y,
			BufferWrapper[] elevations, double missingDataSignal)
	{
		BufferWrapper elevs = elevations[4];
		boolean left = x < 0;
		boolean right = x >= width;
		boolean top = y < 0;
		boolean bottom = y >= height;

		//can we do this with simple integer maths?

		//assumption: tile edge contain same values as adjacent tile edge,
		//hence increment and decrement

		if (top)
		{
			x--;
			x += width;
			elevs = elevations[7];
		}
		else if (bottom)
		{
			x++;
			x -= width;
			elevs = elevations[1];
		}

		if (left)
		{
			y--;
			y += height;
			if (top)
				elevs = elevations[6];
			else if (bottom)
				elevs = elevations[0];
			else
				elevs = elevations[3];
		}
		else if (right)
		{
			y++;
			y -= height;
			if (top)
				elevs = elevations[8];
			else if (bottom)
				elevs = elevations[2];
			else
				elevs = elevations[5];
		}

		if (elevs == null)
			return missingDataSignal;

		return elevs.getDouble(getArrayIndex(width, height, x, y));
	}


	protected static int getArrayIndex(int width, int height, int x, int y)
	{
		while (x < 0)
			x += width;
		while (y < 0)
			y += height;
		while (x >= width)
			x -= width;
		while (y >= height)
			y -= height;
		return width * y + x;
	}

	protected static int clamp(int value, int min, int max)
	{
		return (value < min) ? min : (value > max) ? max : value;
	}

	protected static double clamp(double value, double min, double max)
	{
		return (value < min) ? min : (value > max) ? max : value;
	}

	public double getMinElevationClamp()
	{
		return minElevationClamp;
	}

	public void setMinElevationClamp(double minElevationClamp)
	{
		this.minElevationClamp = minElevationClamp;
	}

	public double getMaxElevationClamp()
	{
		return maxElevationClamp;
	}

	public void setMaxElevationClamp(double maxElevationClamp)
	{
		this.maxElevationClamp = maxElevationClamp;
	}

	@Override
	public void setSplitScale(double splitScale)
	{
		super.setSplitScale(splitScale);
	}

	@Override
	protected TextureTile createTile(Sector sector, Level level, int row,
			int col)
	{
		return new ElevationTextureTile(this, sector, level, row, col);
	}

	@Override
	protected void setBlendingFunction(DrawContext dc)
	{
		GL gl = dc.getGL();
		double alpha = this.getOpacity();
		gl.glColor4d(alpha, alpha, alpha, alpha);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
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

	protected static class ElevationTextureTile extends TextureTile
	{
		private final ElevationLayer layer;

		public ElevationTextureTile(ElevationLayer layer, Sector sector,
				Level level, int row, int col)
		{
			super(sector, level, row, col);
			this.layer = layer;
		}

		@Override
		protected Texture initializeTexture(DrawContext dc)
		{
			if (dc == null)
			{
				String message = Logging
						.getMessage("nullValue.DrawContextIsNull");
				Logging.logger().severe(message);
				throw new IllegalStateException(message);
			}

			Texture t = this.getTexture(dc.getTextureCache());
			if (t != null)
				return t;

			if (this.getTextureData() == null)
			{
				String msg = Logging.getMessage("nullValue.TextureDataIsNull");
				Logging.logger().severe(msg);
				throw new IllegalStateException(msg);
			}

			try
			{
				t = layer.createTexture(this);
			}
			catch (Exception e)
			{
				Logging
						.logger()
						.log(
								java.util.logging.Level.SEVERE,
								"layers.TextureLayer.ExceptionAttemptingToReadTextureFile",
								e);
				return null;
			}

			this.setTexture(dc.getTextureCache(), t);
			t.bind();

			this.setTextureParameters(dc, t);
			layer.setupTexture(dc, t);

			return t;
		}

		@Override
		public boolean bind(DrawContext dc)
		{
			if (dc == null)
			{
				String message = Logging
						.getMessage("nullValue.DrawContextIsNull");
				Logging.logger().severe(message);
				throw new IllegalStateException(message);
			}

			Texture t = this.getTexture(dc.getTextureCache());
			if (t == null && this.getTextureData() != null)
			{
				t = this.initializeTexture(dc);
				if (t != null)
					return true; // texture was bound during initialization.
			}

			if (t == null && this.getFallbackTile() != null)
			{
				ElevationTextureTile resourceTile = this.getFallbackTile();
				t = resourceTile.getTexture(dc.getTextureCache());
				if (t == null)
				{
					t = resourceTile.initializeTexture(dc);
					if (t != null)
						return true; // texture was bound during initialization.
				}
			}

			if (t != null)
			{
				t.bind();
				layer.setupTexture(dc, t);
			}

			return t != null;
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
				subTiles[0] = layer.createTile(new Sector(p0, p1, t0, t1),
						nextLevel, 2 * row, 2 * col);

			key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[1] = subTile;
			else
				subTiles[1] = layer.createTile(new Sector(p0, p1, t1, t2),
						nextLevel, 2 * row, 2 * col + 1);

			key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[2] = subTile;
			else
				subTiles[2] = layer.createTile(new Sector(p1, p2, t0, t1),
						nextLevel, 2 * row + 1, 2 * col);

			key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1,
					nextLevelCacheName);
			subTile = this.getTileFromMemoryCache(key);
			if (subTile != null)
				subTiles[3] = subTile;
			else
				subTiles[3] = layer.createTile(new Sector(p1, p2, t1, t2),
						nextLevel, 2 * row + 1, 2 * col + 1);

			return subTiles;
		}

		@Override
		public ElevationTextureTile getFallbackTile()
		{
			return (ElevationTextureTile) super.getFallbackTile();
		}
	}
}
