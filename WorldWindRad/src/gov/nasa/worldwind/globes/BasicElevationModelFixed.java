/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Comparator;

// Implementation notes, not for API doc:
//
// Implements an elevation model based on a quad tree of elevation tiles. Meant to be subclassed by very specific
// classes, e.g. Earth/SRTM. A Descriptor passed in at construction gives the configuration parameters. Eventually
// Descriptor will be replaced by an XML configuration document.
//
// A "tile" corresponds to one tile of the data set, which has a corresponding unique row/column address in the data
// set. An inner class implements Tile. An inner class also implements TileKey, which is used to address the
// corresponding Tile in the memory cache.

// Clients of this class get elevations from it by first getting an Elevations object for a specific Sector, then
// querying that object for the elevation at individual lat/lon positions. The Elevations object captures information
// that is used to compute elevations. See in-line comments for a description.
//
// When an elevation tile is needed but is not in memory, a task is threaded off to find it. If it's in the file cache
// then it's loaded by the task into the memory cache. If it's not in the file cache then a retrieval is initiated by
// the task. The disk is never accessed during a call to getElevations(sector, resolution) because that method is
// likely being called when a frame is being rendered. The details of all this are in-line below.

/**
 * This class represents a single tile in the data set and contains the
 * information that needs to be cached.
 * 
 * @author Tom Gaskins
 * @version $Id: BasicElevationModel.java 5164 2008-04-24 20:57:37Z dcollins $
 */
public class BasicElevationModelFixed extends WWObjectImpl implements
		ElevationModel
{
	private boolean isEnabled = true;
	private final LevelSet levels;
	private final double minElevation;
	private final double maxElevation;
	private long numExpectedValues = 0;
	private final Object fileLock = new Object();
	private java.util.concurrent.ConcurrentHashMap<TileKey, Tile> levelZeroTiles = new java.util.concurrent.ConcurrentHashMap<TileKey, Tile>();
	private MemoryCache memoryCache = new BasicMemoryCache(4000000, 5000000);
	private int extremesLevel = -1;
	private ShortBuffer extremes = null;

	private static final class Tile extends gov.nasa.worldwind.util.Tile
			implements Cacheable
	{
		private java.nio.ShortBuffer elevations; // the elevations themselves

		private Tile(Sector sector, Level level, int row, int col)
		{
			super(sector, level, row, col);
		}
	}

	/**
	 * @param levels
	 * @param minElevation
	 * @param maxElevation
	 * @throws IllegalArgumentException
	 *             if <code>levels</code> is null or invalid
	 */
	public BasicElevationModelFixed(LevelSet levels, double minElevation,
			double maxElevation)
	{
		if (levels == null)
		{
			String message = Logging.getMessage("nullValue.LevelSetIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		String cacheName = Tile.class.getName();
		if (WorldWind.getMemoryCacheSet().containsCache(cacheName))
		{
			this.memoryCache = WorldWind.getMemoryCache(cacheName);
		}
		else
		{
			long size = Configuration.getLongValue(
					AVKey.ELEVATION_TILE_CACHE_SIZE, 5000000L);
			this.memoryCache = new BasicMemoryCache((long) (0.85 * size), size);
			this.memoryCache.setName("Elevation Tiles");
			WorldWind.getMemoryCacheSet().addCache(cacheName, this.memoryCache);
		}

		this.levels = new LevelSet(levels); // the caller's levelSet may change internally, so we copy it.
		this.minElevation = minElevation;
		this.maxElevation = maxElevation;
	}

	public boolean isEnabled()
	{
		return this.isEnabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.isEnabled = enabled;
	}

	public LevelSet getLevels()
	{
		return this.levels;
	}

	public final double getMaxElevation()
	{
		return this.maxElevation;
	}

	public final double getMinElevation()
	{
		return this.minElevation;
	}

	public long getNumExpectedValuesPerTile()
	{
		return numExpectedValues;
	}

	public void setNumExpectedValuesPerTile(long numExpectedValues)
	{
		this.numExpectedValues = numExpectedValues;
	}

	// Create the tile corresponding to a specified key.
	private Tile createTile(TileKey key)
	{
		Level level = this.levels.getLevel(key.getLevelNumber());

		// Compute the tile's SW lat/lon based on its row/col in the level's data set.
		Angle dLat = level.getTileDelta().getLatitude();
		Angle dLon = level.getTileDelta().getLongitude();

		Angle minLatitude = Tile.computeRowLatitude(key.getRow(), dLat);
		Angle minLongitude = Tile.computeColumnLongitude(key.getColumn(), dLon);

		Sector tileSector = new Sector(minLatitude, minLatitude.add(dLat),
				minLongitude, minLongitude.add(dLon));

		return new Tile(tileSector, level, key.getRow(), key.getColumn());
	}

	// Thread off a task to determine whether the file is local or remote and then retrieve it either from the file
	// cache or a remote server.
	private void requestTile(TileKey key)
	{
		if (WorldWind.getTaskService().isFull())
			return;

		RequestTask request = new RequestTask(key, this);
		WorldWind.getTaskService().addTask(request);
	}

	private static class RequestTask implements Runnable
	{
		private final BasicElevationModelFixed elevationModel;
		private final TileKey tileKey;

		private RequestTask(TileKey tileKey,
				BasicElevationModelFixed elevationModel)
		{
			this.elevationModel = elevationModel;
			this.tileKey = tileKey;
		}

		public final void run()
		{
			// check to ensure load is still needed
			if (this.elevationModel.areElevationsInMemory(this.tileKey))
				return;

			Tile tile = this.elevationModel.createTile(this.tileKey);
			final java.net.URL url = WorldWind.getDataFileCache().findFile(
					tile.getPath(), false);
			if (url != null)
			{
				if (this.elevationModel.loadElevations(tile, url))
				{
					this.elevationModel.levels.unmarkResourceAbsent(tile);
					this.elevationModel.firePropertyChange(
							AVKey.ELEVATION_MODEL, null, this);
					return;
				}
				else
				{
					// Assume that something's wrong with the file and delete it.
					gov.nasa.worldwind.WorldWind.getDataFileCache().removeFile(
							url);
					this.elevationModel.levels.markResourceAbsent(tile);
					String message = Logging.getMessage(
							"generic.DeletedCorruptDataFile", url);
					Logging.logger().info(message);
				}
			}

			this.elevationModel.downloadElevations(tile);
		}

		public final boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RequestTask that = (RequestTask) o;

			//noinspection RedundantIfStatement
			if (this.tileKey != null ? !this.tileKey.equals(that.tileKey)
					: that.tileKey != null)
				return false;

			return true;
		}

		public final int hashCode()
		{
			return (this.tileKey != null ? this.tileKey.hashCode() : 0);
		}

		public final String toString()
		{
			return this.tileKey.toString();
		}
	}

	// Reads a tile's elevations from the file cache and adds the tile to the memory cache.
	private boolean loadElevations(Tile tile, java.net.URL url)
	{
		java.nio.ShortBuffer elevations = this.readElevations(url);
		if (elevations == null)
			return false;

		if (this.numExpectedValues > 0
				&& elevations.capacity() != this.numExpectedValues)
			return false; // corrupt file

		tile.elevations = elevations;
		this.addTileToCache(tile, elevations);

		return true;
	}

	private void addTileToCache(Tile tile, java.nio.ShortBuffer elevations)
	{
		// Level 0 tiles are held in the model itself; other levels are placed in the memory cache.
		if (tile.getLevelNumber() == 0)
			this.levelZeroTiles.putIfAbsent(tile.getTileKey(), tile);
		else
			this.memoryCache.add(tile.getTileKey(), tile,
					elevations.limit() * 2);
	}

	private boolean areElevationsInMemory(TileKey key)
	{
		Tile tile = this.getTileFromMemory(key);
		return (tile != null && tile.elevations != null);
	}

	private Tile getTileFromMemory(TileKey tileKey)
	{
		if (tileKey.getLevelNumber() == 0)
			return this.levelZeroTiles.get(tileKey);
		else
			return (Tile) this.memoryCache.getObject(tileKey);
	}

	// Read elevations from the file cache. Don't be confused by the use of a URL here: it's used so that files can
	// be read using System.getResource(URL), which will draw the data from a jar file in the classpath.
	// TODO: Look into possibly moving the mapping to a URL into WWIO.
	private java.nio.ShortBuffer readElevations(URL url)
	{
		try
		{
			ByteBuffer buffer;
			synchronized (this.fileLock)
			{
				buffer = WWIO.readURLContentToBuffer(url);
			}
			buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN); // TODO: byte order is format dependent
			return buffer.asShortBuffer();
		}
		catch (java.io.IOException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					"TiledElevationModel.ExceptionAttemptingToReadTextureFile",
					url.toString());
			return null;
		}
	}

	private void downloadElevations(final Tile tile)
	{
		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		java.net.URL url = null;
		try
		{
			url = tile.getResourceURL();
			if (WorldWind.getNetworkStatus().isHostUnavailable(url))
				return;
		}
		catch (java.net.MalformedURLException e)
		{
			Logging
					.logger()
					.log(
							java.util.logging.Level.SEVERE,
							Logging
									.getMessage(
											"TiledElevationModel.ExceptionCreatingElevationsUrl",
											url), e);
			return;
		}

		URLRetriever retriever = new HTTPRetriever(url,
				new DownloadPostProcessor(tile, this));
		if (WorldWind.getRetrievalService().contains(retriever))
			return;

		WorldWind.getRetrievalService().runRetriever(retriever, 0d);
	}

	/**
	 * @param dc
	 * @param sector
	 * @param density
	 * @return
	 * @throws IllegalArgumentException
	 *             if <code>dc</code> is null, <code>sector</code> is null or
	 *             <code>density is
     *                                  negative
	 */
	public final int getTargetResolution(DrawContext dc, Sector sector,
			int density)
	{
		if (!this.isEnabled)
			return 0;

		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (sector == null)
		{
			String msg = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (density < 0)
		{
			Logging.logger().severe("BasicElevationModel.DensityBelowZero");
		}

		LatLon c = this.levels.getSector().getCentroid();
		double radius = dc.getGlobe().getRadiusAt(c.getLatitude(),
				c.getLongitude());
		double sectorWidth = sector.getDeltaLatRadians() * radius;
		double targetSize = 0.8 * sectorWidth / (density); // TODO: make scale of density configurable

		for (Level level : this.levels.getLevels())
		{
			if (level.getTexelSize(radius) < targetSize)
			{
				return level.getLevelNumber();
			}
		}

		return this.levels.getNumLevels() - 1; // finest resolution available
	}

	public final int getTargetResolution(Globe globe, double size)
	{
		if (!this.isEnabled)
			return 0;

		if (globe == null)
		{
			String msg = Logging.getMessage("nullValue.GlobeIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (size < 0)
		{
			Logging.logger().severe("BasicElevationModel.DensityBelowZero");
		}

		LatLon c = this.levels.getSector().getCentroid();
		double radius = globe.getRadiusAt(c.getLatitude(), c.getLongitude());

		for (Level level : this.levels.getLevels())
		{
			if (level.getTexelSize(radius) < size)
			{
				return level.getLevelNumber();
			}
		}

		return this.levels.getNumLevels() - 1; // finest resolution available
	}

	private static class DownloadPostProcessor implements
			RetrievalPostProcessor
	{
		private Tile tile;
		private BasicElevationModelFixed elevationModel;

		public DownloadPostProcessor(Tile tile,
				BasicElevationModelFixed elevationModel)
		{
			// don't validate - constructor is only available to classes with private access.
			this.tile = tile;
			this.elevationModel = elevationModel;
		}

		/**
		 * @param retriever
		 * @return
		 * @throws IllegalArgumentException
		 *             if <code>retriever</code> is null
		 */
		public ByteBuffer run(Retriever retriever)
		{
			if (retriever == null)
			{
				String msg = Logging.getMessage("nullValue.RetrieverIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}

			try
			{
				if (!retriever.getState().equals(
						Retriever.RETRIEVER_STATE_SUCCESSFUL))
					return null;

				if (retriever instanceof HTTPRetriever)
				{
					HTTPRetriever htr = (HTTPRetriever) retriever;
					if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
					{
						// Mark tile as missing so avoid excessive attempts
						this.elevationModel.levels
								.markResourceAbsent(this.tile);
						return null;
					}
				}

				URLRetriever r = (URLRetriever) retriever;
				ByteBuffer buffer = r.getBuffer();

				final File outFile = WorldWind.getDataFileCache().newFile(
						tile.getPath());
				if (outFile == null)
					return null;

				if (outFile.exists())
					return buffer;

				if (buffer != null)
				{
					synchronized (elevationModel.fileLock)
					{
						WWIO.saveBuffer(buffer, outFile);
					}
					return buffer;
				}
			}
			catch (java.io.IOException e)
			{
				Logging
						.logger()
						.log(
								java.util.logging.Level.SEVERE,
								Logging
										.getMessage(
												"TiledElevationModel.ExceptionSavingRetrievedElevationFile",
												tile.getPath()), e);
			}
			finally
			{
				this.elevationModel.firePropertyChange(AVKey.ELEVATION_MODEL,
						null, this);
			}
			return null;
		}
	}

	private static class BasicElevations implements ElevationModel.Elevations
	{
		private final int resolution;
		private final Sector sector;
		private final BasicElevationModelFixed elevationModel;
		private java.util.Set<Tile> tiles;
		private short extremes[] = null;

		private BasicElevations(Sector sector, int resolution,
				BasicElevationModelFixed elevationModel)
		{
			this.sector = sector;
			this.resolution = resolution;
			this.elevationModel = elevationModel;
		}

		public int getResolution()
		{
			return this.resolution;
		}

		public Sector getSector()
		{
			return this.sector;
		}

		public boolean hasElevations()
		{
			return this.tiles != null && this.tiles.size() > 0;
		}

		public double getElevation(double latRadians, double lonRadians)
		{
			if (this.tiles == null)
				return 0;

			try
			{
				for (BasicElevationModelFixed.Tile tile : this.tiles)
				{
					if (tile.getSector()
							.containsRadians(latRadians, lonRadians))
						return this.elevationModel.lookupElevation(latRadians,
								lonRadians, tile);
				}

				return 0;
			}
			catch (Exception e)
			{
				// Throwing an exception within what's likely to be the caller's geometry creation loop
				// would be hard to recover from, and a reasonable response to the exception can be done here.
				Logging
						.logger()
						.log(
								java.util.logging.Level.SEVERE,
								Logging
										.getMessage(
												"BasicElevationModel.ExceptionComputingElevation",
												latRadians, lonRadians), e);

				return 0;
			}
		}

		public short[] getExtremes()
		{
			if (this.extremes != null)
				return this.extremes;

			if (this.tiles == null)
				return null;

			short min = Short.MAX_VALUE;
			short max = Short.MIN_VALUE;

			for (BasicElevationModelFixed.Tile tile : this.tiles)
			{
				tile.elevations.rewind();

				if (!tile.elevations.hasRemaining())
					return null;

				while (tile.elevations.hasRemaining())
				{
					short h = tile.elevations.get();
					if (h > max)
						max = h;
					if (h < min)
						min = h;
				}
			}

			return this.extremes = new short[] { min, max };
		}
	}

	/**
	 * @param latitude
	 * @param longitude
	 * @return
	 * @throws IllegalArgumentException
	 *             if <code>latitude</code> or <code>longitude</code> is null
	 */
	public final double getElevation(Angle latitude, Angle longitude)
	{
		if (!this.isEnabled())
			return 0;

		if (latitude == null || longitude == null)
		{
			String msg = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		// TODO: Make level to draw elevations from configurable
		final TileKey tileKey = new TileKey(latitude, longitude, this.levels
				.getLastLevel(latitude, longitude));
		Tile tile = this.getTileFromMemory(tileKey);

		if (tile == null)
		{
			int fallbackRow = tileKey.getRow();
			int fallbackCol = tileKey.getColumn();
			for (int fallbackLevelNum = tileKey.getLevelNumber() - 1; fallbackLevelNum >= 0; fallbackLevelNum--)
			{
				fallbackRow /= 2;
				fallbackCol /= 2;
				TileKey fallbackKey = new TileKey(fallbackLevelNum,
						fallbackRow, fallbackCol, this.levels.getLevel(
								fallbackLevelNum).getCacheName());
				tile = this.getTileFromMemory(fallbackKey);
				if (tile != null)
					break;
			}
		}

		if (tile == null)
		{
			final TileKey zeroKey = new TileKey(latitude, longitude,
					this.levels.getFirstLevel());
			this.requestTile(zeroKey);

			return 0;
		}

		return this.lookupElevation(latitude.radians, longitude.radians, tile);
	}

	public Double getBestElevation(Angle latitude, Angle longitude)
	{
		if (!this.isEnabled())
			return null;

		if (latitude == null || longitude == null)
		{
			String msg = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		final TileKey tileKey = new TileKey(latitude, longitude, this.levels
				.getLastLevel(latitude, longitude));
		Tile tile = this.getTileFromMemory(tileKey);

		if (tile != null)
		{
			return this.lookupElevation(latitude.radians, longitude.radians,
					tile);
		}
		else
		{
			this.requestTile(tileKey);
			return null;
		}
	}

	public Double getElevationAtResolution(Angle latitude, Angle longitude,
			int resolution)
	{
		if (!this.isEnabled())
			return null;

		if (latitude == null || longitude == null)
		{
			String msg = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (resolution < 0
				|| resolution > this.getLevels().getLastLevel(longitude,
						latitude).getLevelNumber())
			return this.getBestElevation(latitude, longitude);

		final TileKey tileKey = new TileKey(latitude, longitude, this.levels
				.getLevel(resolution));
		Tile tile = this.getTileFromMemory(tileKey);

		if (tile != null)
		{
			return this.lookupElevation(latitude.radians, longitude.radians,
					tile);
		}
		else
		{
			this.requestTile(tileKey);
			return null;
		}
	}

	public final int getTileCount(Sector sector, int resolution)
	{
		if (sector == null)
		{
			String msg = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (!this.isEnabled())
			return 0;

		// Collect all the elevation tiles intersecting the input sector. If a desired tile is not curently
		// available, choose its next lowest resolution parent that is available.
		final Level targetLevel = this.levels.getLevel(resolution);

		LatLon delta = targetLevel.getTileDelta();
		final int nwRow = Tile.computeRow(delta.getLatitude(), sector
				.getMaxLatitude());
		final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMinLongitude());
		final int seRow = Tile.computeRow(delta.getLatitude(), sector
				.getMinLatitude());
		final int seCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMaxLongitude());

		return (1 + (nwRow - seRow) * (1 + seCol - nwCol));
	}

	/**
	 * @param sector
	 * @param resolution
	 * @return
	 * @throws IllegalArgumentException
	 *             if <code>sector</code> is null
	 */
	public final Elevations getElevations(Sector sector, int resolution)
	{
		if (sector == null)
		{
			String msg = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (!this.isEnabled())
			return new BasicElevations(sector, Integer.MIN_VALUE, this);

		// Collect all the elevation tiles intersecting the input sector. If a desired tile is not curently
		// available, choose its next lowest resolution parent that is available.
		final Level targetLevel = this.levels.getLevel(resolution);

		LatLon delta = this.levels.getLevel(resolution).getTileDelta();
		final int nwRow = Tile.computeRow(delta.getLatitude(), sector
				.getMaxLatitude());
		final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMinLongitude());
		final int seRow = Tile.computeRow(delta.getLatitude(), sector
				.getMinLatitude());
		final int seCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMaxLongitude());

		java.util.TreeSet<Tile> tiles = new java.util.TreeSet<Tile>(
				new Comparator<Tile>()
				{
					public int compare(Tile t1, Tile t2)
					{
						if (t2.getLevelNumber() == t1.getLevelNumber()
								&& t2.getRow() == t1.getRow()
								&& t2.getColumn() == t1.getColumn())
							return 0;

						// Higher-res levels compare lower than lower-res
						return t1.getLevelNumber() > t2.getLevelNumber() ? -1
								: 1;
					}
				});
		java.util.ArrayList<TileKey> requested = new java.util.ArrayList<TileKey>();

		boolean missingTargetTiles = false;
		boolean missingLevelZeroTiles = false;
		for (int row = seRow; row <= nwRow; row++)
		{
			for (int col = nwCol; col <= seCol; col++)
			{
				TileKey key = new TileKey(resolution, row, col, targetLevel
						.getCacheName());
				Tile tile = this.getTileFromMemory(key);
				if (tile != null)
				{
					tiles.add(tile);
					continue;
				}

				missingTargetTiles = true;
				this.requestTile(key);

				// Determine the fallback to use. Simultaneously determine a fallback to request that is
				// the next resolution higher than the fallback chosen, if any. This will progressively
				// refine the display until the desired resolution tile arrives.
				TileKey fallbackToRequest = null;
				TileKey fallbackKey = null;

				int fallbackRow = row;
				int fallbackCol = col;
				for (int fallbackLevelNum = key.getLevelNumber() - 1; fallbackLevelNum >= 0; fallbackLevelNum--)
				{
					fallbackRow /= 2;
					fallbackCol /= 2;
					fallbackKey = new TileKey(fallbackLevelNum, fallbackRow,
							fallbackCol, this.levels.getLevel(fallbackLevelNum)
									.getCacheName());
					tile = this.getTileFromMemory(fallbackKey);
					if (tile != null)
					{
						if (!tiles.contains(tile))
							tiles.add(tile);
						break;
					}
					else
					{
						if (fallbackLevelNum == 0)
							missingLevelZeroTiles = true;
						fallbackToRequest = fallbackKey; // keep track of lowest level to request
					}
				}

				if (fallbackToRequest != null)
				{
					if (!requested.contains(fallbackToRequest))
					{
						this.requestTile(fallbackToRequest);
						requested.add(fallbackToRequest); // keep track to avoid overhead of duplicte requests
					}
				}
			}
		}

		BasicElevations elevations;

		//        int lev = tiles.size() > 0 ? tiles.first().getLevelNumber() : 0;
		//        System.out.printf("%d tiles, target = %d (%d, %d), level %d, target = %d\n", tiles.size(),
		//            (1 + nwRow - seRow) * (1 + seCol - nwCol), nwRow - seRow, seCol - nwCol,
		//            lev, targetLevel.getLevelNumber());

		if (missingLevelZeroTiles || tiles.isEmpty())
		{
			// Integer.MIN_VALUE is a signal for no in-memory tile for a given region of the sector.
			elevations = new BasicElevations(sector, Integer.MIN_VALUE, this);
		}

		else if (missingTargetTiles)
		{
			// Use the level of the the lowest resolution found to denote the resolution of this elevation set.
			// The list of tiles is sorted first by level, so use the level of the list's last entry.
			elevations = new BasicElevations(sector, tiles.last()
					.getLevelNumber(), this);
		}
		else
		{
			elevations = new BasicElevations(sector, resolution, this);
		}

		elevations.tiles = tiles;

		return elevations;
	}

	public final int getTileCountAtResolution(Sector sector, int resolution)
	{
		int targetResolution = this.getLevels().getLastLevel(sector)
				.getLevelNumber();
		if (resolution >= 0)
			targetResolution = Math.min(resolution, this.getLevels()
					.getLastLevel(sector).getLevelNumber());
		return this.getTileCount(sector, targetResolution);
	}

	public final Elevations getElevationsAtResolution(Sector sector,
			int resolution)
	{
		int targetResolution = this.getLevels().getLastLevel(sector)
				.getLevelNumber();
		if (resolution >= 0)
			targetResolution = Math.min(resolution, this.getLevels()
					.getLastLevel(sector).getLevelNumber());
		Elevations elevs = this.getElevations(sector, targetResolution);
		return elevs.getResolution() == targetResolution ? elevs : null;
	}

	public final Elevations getBestElevations(Sector sector)
	{
		return this.getElevationsAtResolution(sector, this.getLevels()
				.getLastLevel(sector).getLevelNumber());
	}

	private double lookupElevation(final double latRadians,
			final double lonRadians, final Tile tile)
	{
		Sector sector = tile.getSector();
		final int tileHeight = tile.getLevel().getTileHeight();
		final int tileWidth = tile.getLevel().getTileWidth();
		final double sectorDeltaLat = sector.getDeltaLat().radians;
		final double sectorDeltaLon = sector.getDeltaLon().radians;
		final double dLat = sector.getMaxLatitude().radians - latRadians;
		final double dLon = lonRadians - sector.getMinLongitude().radians;
		final double sLat = dLat / sectorDeltaLat;
		final double sLon = dLon / sectorDeltaLon;

		int j = (int) ((tileHeight - 1) * sLat);
		int i = (int) ((tileWidth - 1) * sLon);
		int k = j * tileWidth + i;

		double eLeft = tile.elevations.get(k);
		double eRight = i < (tileWidth - 1) ? tile.elevations.get(k + 1)
				: eLeft;

		double dw = sectorDeltaLon / (tileWidth - 1);
		double dh = sectorDeltaLat / (tileHeight - 1);
		double ssLon = (dLon - i * dw) / dw;
		double ssLat = (dLat - j * dh) / dh;

		double eTop = eLeft + ssLon * (eRight - eLeft);

		if (j < tileHeight - 1 && i < tileWidth - 1)
		{
			eLeft = tile.elevations.get(k + tileWidth);
			eRight = tile.elevations.get(k + tileWidth + 1);
		}

		double eBot = eLeft + ssLon * (eRight - eLeft);
		return eTop + ssLat * (eBot - eTop);
	}

	public double[] getMinAndMaxElevations(Sector sector)
	{
		if (sector == null)
		{
			String message = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (this.extremesLevel < 0 || this.extremes == null)
			return new double[] { this.getMinElevation(),
					this.getMaxElevation() };

		try
		{
			LatLon delta = this.levels.getLevel(this.extremesLevel)
					.getTileDelta();
			final int nwRow = Tile.computeRow(delta.getLatitude(), sector
					.getMaxLatitude());
			final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
					.getMinLongitude());
			final int seRow = Tile.computeRow(delta.getLatitude(), sector
					.getMinLatitude());
			final int seCol = Tile.computeColumn(delta.getLongitude(), sector
					.getMaxLongitude());

			final int nCols = Tile.computeColumn(delta.getLongitude(),
					Angle.POS180) + 1;

			short min = Short.MAX_VALUE;
			short max = Short.MIN_VALUE;

			for (int row = seRow; row <= nwRow; row++)
			{
				for (int col = nwCol; col <= seCol; col++)
				{
					int index = 2 * (row * nCols + col);
					short a = this.extremes.get(index);
					short b = this.extremes.get(index + 1);
					if (a > max)
						max = a;
					if (a < min)
						min = a;
					if (b > max)
						max = b;
					if (b < min)
						min = b;
				}
			}

			return new double[] { (double) min, (double) max };
		}
		catch (Exception e)
		{
			String message = Logging.getMessage(
					"BasicElevationModel.ExceptionDeterminingExtremes", sector);
			Logging.logger().log(java.util.logging.Level.WARNING, message, e);

			return new double[] { this.getMinElevation(),
					this.getMaxElevation() };
		}
	}

	protected void loadExtremeElevations(String extremesFileName)
	{
		if (extremesFileName == null)
		{
			String message = Logging
					.getMessage("nullValue.ExtremeElevationsFileName");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		InputStream is = null;
		try
		{
			is = this.getClass().getResourceAsStream("/" + extremesFileName);
			if (is == null)
			{
				// Look directly in the file system
				File file = new File(extremesFileName);
				if (file.exists())
					is = new FileInputStream(file);
				else
					Logging.logger().log(java.util.logging.Level.WARNING,
							"BasicElevationModel.UnavailableExtremesFile",
							extremesFileName);
			}

			if (is == null)
				return;

			// The level the extremes were taken from is encoded as the last element in the file name
			String[] tokens = extremesFileName.substring(0,
					extremesFileName.lastIndexOf(".")).split("_");
			this.extremesLevel = Integer.parseInt(tokens[tokens.length - 1]);
			if (this.extremesLevel < 0)
			{
				this.extremes = null;
				Logging.logger().log(java.util.logging.Level.WARNING,
						"BasicElevationModel.UnavailableExtremesLevel",
						extremesFileName);
				return;
			}

			ByteBuffer bb = WWIO.readStreamToBuffer(is);
			this.extremes = bb.asShortBuffer();
			this.extremes.rewind();

		}
		catch (FileNotFoundException e)
		{
			Logging
					.logger()
					.log(
							java.util.logging.Level.WARNING,
							Logging
									.getMessage(
											"BasicElevationModel.ExceptionReadingExtremeElevations",
											extremesFileName), e);
			this.extremes = null;
			this.extremesLevel = -1;
		}
		catch (IOException e)
		{
			Logging
					.logger()
					.log(
							java.util.logging.Level.WARNING,
							Logging
									.getMessage(
											"BasicElevationModel.ExceptionReadingExtremeElevations",
											extremesFileName), e);
			this.extremes = null;
			this.extremesLevel = -1;
		}
		finally
		{
			if (is != null)
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					Logging.logger().log(
							java.util.logging.Level.WARNING,
							Logging.getMessage(
									"generic.ExceptionClosingStream",
									extremesFileName), e);
				}
		}
	}
}
