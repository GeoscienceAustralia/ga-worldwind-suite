/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.terrain;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.terrain.AbstractElevationModel;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.opengl.util.BufferUtil;

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
 * @author Tom Gaskins
 * @version $Id: BasicElevationModel.java 9725 2009-03-27 22:25:09Z tgaskins $
 */
public class BasicElevationModel extends AbstractElevationModel
{
	private final LevelSet levels;
	private final double minElevation;
	private final double maxElevation;
	private String elevationDataPixelType = AVKey.INT16;
	private String elevationDataByteOrder = AVKey.LITTLE_ENDIAN;
	private double detailHint = 0.0;
	private final Object fileLock = new Object();
	private java.util.concurrent.ConcurrentHashMap<TileKey, Tile> levelZeroTiles = new java.util.concurrent.ConcurrentHashMap<TileKey, Tile>();
	private MemoryCache memoryCache;
	private int extremesLevel = -1;
	private BufferWrapper extremes = null;
	private int numExpectedValues = -1;

	public BasicElevationModel(AVList params)
	{
		if (params == null)
		{
			String message = Logging
					.getMessage("nullValue.ElevationModelConfigParams");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		String s = params.getStringValue(AVKey.BYTE_ORDER);
		if (s != null)
			this.setByteOrder(s);

		Double d = (Double) params.getValue(AVKey.DETAIL_HINT);
		if (d != null)
			this.setDetailHint(d);

		s = params.getStringValue(AVKey.DISPLAY_NAME);
		if (s != null)
			this.setName(s);

		d = (Double) params.getValue(AVKey.ELEVATION_MIN);
		this.minElevation = d != null ? d : 0;

		d = (Double) params.getValue(AVKey.ELEVATION_MAX);
		this.maxElevation = d != null ? d : 0;

		Long lo = (Long) params.getValue(AVKey.EXPIRY_TIME);
		if (lo != null)
			params.setValue(AVKey.EXPIRY_TIME, lo);

		d = (Double) params.getValue(AVKey.MISSING_DATA_SIGNAL);
		if (d != null)
			this.setMissingDataSignal(d);

		d = (Double) params.getValue(AVKey.MISSING_DATA_REPLACEMENT);
		if (d != null)
			this.setMissingDataReplacement(d);

		Boolean b = (Boolean) params.getValue(AVKey.NETWORK_RETRIEVAL_ENABLED);
		if (b != null)
			this.setNetworkRetrievalEnabled(b);

		s = params.getStringValue(AVKey.PIXEL_TYPE);
		if (s != null)
			this.setPixelType(s);

		s = params.getStringValue(AVKey.ELEVATION_EXTREMES_FILE);
		if (s != null)
			this.loadExtremeElevations(s);

		// Set some fallback values if not already set.
		setFallbacks(params);

		this.levels = new LevelSet(params);
		this.memoryCache = this.createMemoryCache(Tile.class.getName());

		this.setValue(AVKey.CONSTRUCTION_PARAMETERS, params.copy());
	}

	public BasicElevationModel(Document dom, AVList params)
	{
		this(getParamsFromDocument(dom.getDocumentElement(), params));
	}

	public BasicElevationModel(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	public BasicElevationModel(String restorableStateInXml)
	{
		this(restorableStateToParams(restorableStateInXml));

		RestorableSupport rs;
		try
		{
			rs = RestorableSupport.parse(restorableStateInXml);
		}
		catch (Exception e)
		{
			// Parsing the document specified by stateInXml failed.
			String message = Logging.getMessage(
					"generic.ExceptionAttemptingToParseStateXml",
					restorableStateInXml);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message, e);
		}

		this.doRestoreState(rs, null);
	}

	protected static void setFallbacks(AVList params)
	{
		if (params.getValue(AVKey.TILE_WIDTH) == null)
			params.setValue(AVKey.TILE_WIDTH, 150);
		if (params.getValue(AVKey.TILE_HEIGHT) == null)
			params.setValue(AVKey.TILE_HEIGHT, 150);
		if (params.getValue(AVKey.FORMAT_SUFFIX) == null)
			params.setValue(AVKey.FORMAT_SUFFIX, ".bil");
		if (params.getValue(AVKey.NUM_LEVELS) == null)
			params.setValue(AVKey.NUM_LEVELS, 2);
		if (params.getValue(AVKey.NUM_EMPTY_LEVELS) == null)
			params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
	}

	protected static AVList getParamsFromDocument(Element domElement,
			AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetStringParam(domElement, params, AVKey.BYTE_ORDER,
				"ByteOrder", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DATA_CACHE_NAME,
				"DataCacheName", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKey.DETAIL_HINT,
				"DataDetailHint", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DATASET_NAME,
				"DatasetName", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.DISPLAY_NAME,
				"DisplayName", xpath);
		WWXML.checkAndSetStringParam(domElement, params,
				AVKey.ELEVATION_EXTREMES_FILE, "ExtremeElevations/FileName",
				xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKey.ELEVATION_MAX,
				"ExtremeElevations/@max", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKey.ELEVATION_MIN,
				"ExtremeElevations/@min", xpath);
		WWXML.checkAndSetLongParam(domElement, params, AVKey.EXPIRY_TIME,
				"ExpiryTime", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.FORMAT_SUFFIX,
				"FormatSuffix", xpath);
		WWXML
				.checkAndSetLatLonParam(domElement, params,
						AVKey.LEVEL_ZERO_TILE_DELTA,
						"LevelZeroTileDelta/LatLon", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params,
				AVKey.MISSING_DATA_SIGNAL, "MissingDataSignal", xpath);
		WWXML
				.checkAndSetDoubleParam(domElement, params,
						AVKey.MISSING_DATA_REPLACEMENT,
						"MissingDataReplacement", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params,
				AVKey.NETWORK_RETRIEVAL_ENABLED, "NetworkRetrievalEnabled",
				xpath);
		WWXML.checkAndSetIntegerParam(domElement, params,
				AVKey.NUM_EMPTY_LEVELS, "NumLevels/@numEmpty", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKey.NUM_LEVELS,
				"NumLevels/@count", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.PIXEL_TYPE,
				"PixelType", xpath);
		WWXML.checkAndSetSectorParam(domElement, params, AVKey.SECTOR,
				"Sector", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.SERVICE_NAME,
				"Service/@serviceName", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.SERVICE,
				"Service/URL", xpath);
		WWXML.checkAndSetSectorResolutionParam(domElement, params,
				AVKey.SECTOR_RESOLUTION_LIMITS, "SectorResolutionLimit", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKey.TILE_HEIGHT,
				"TileSize/@height", xpath);
		WWXML.checkAndSetLatLonParam(domElement, params, AVKey.TILE_ORIGIN,
				"TileOrigin/LatLon", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKey.TILE_WIDTH,
				"TileSize/@width", xpath);

		return params;
	}

	protected MemoryCache getMemoryCache()
	{
		return memoryCache;
	}

	protected MemoryCache createMemoryCache(String cacheName)
	{
		if (WorldWind.getMemoryCacheSet().containsCache(cacheName))
		{
			return WorldWind.getMemoryCache(cacheName);
		}
		else
		{
			long size = Configuration.getLongValue(
					AVKey.ELEVATION_TILE_CACHE_SIZE, 5000000L);
			MemoryCache mc = new BasicMemoryCache((long) (0.85 * size), size);
			mc.setName("Elevation Tiles");
			WorldWind.getMemoryCacheSet().addCache(cacheName, mc);
			return mc;
		}
	}

	public LevelSet getLevels()
	{
		return this.levels;
	}

	protected Map<TileKey, Tile> getLevelZeroTiles()
	{
		return levelZeroTiles;
	}

	protected int getExtremesLevel()
	{
		return extremesLevel;
	}

	protected BufferWrapper getExtremes()
	{
		return extremes;
	}

	public double getMaxElevation()
	{
		return this.maxElevation;
	}

	public double getMinElevation()
	{
		return this.minElevation;
	}

	public double getBestResolution(Sector sector)
	{
		if (sector == null)
			return this.levels.getLastLevel().getTexelSize();

		Level level = this.levels.getLastLevel(sector);
		return level != null ? level.getTexelSize() : Double.MAX_VALUE;
	}

	public double getDetailHint(Sector sector)
	{
		return this.detailHint;
	}

	public void setDetailHint(double hint)
	{
		this.detailHint = hint;
	}

	public String getElevationDataPixelType()
	{
		return this.elevationDataPixelType;
	}

	public void setPixelType(String pixelType)
	{
		if (pixelType == null)
		{
			String message = Logging.getMessage("nullValue.PixelTypeIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.elevationDataPixelType = pixelType;
	}

	public String getElevationDataByteOrder()
	{
		return this.elevationDataByteOrder;
	}

	public void setByteOrder(String byteOrder)
	{
		if (byteOrder == null)
		{
			String message = Logging.getMessage("nullValue.ByteOrderIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.elevationDataByteOrder = byteOrder;
	}

	public int intersects(Sector sector)
	{
		if (this.levels.getSector().contains(sector))
			return 0;

		return this.levels.getSector().intersects(sector) ? 1 : -1;
	}

	public boolean contains(Angle latitude, Angle longitude)
	{
		if (latitude == null || longitude == null)
		{
			String msg = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		return this.levels.getSector().contains(latitude, longitude);
	}

	//**************************************************************//
	//********************  Elevation Tile Management  *************//
	//**************************************************************//

	// Create the tile corresponding to a specified key.

	protected Tile createTile(TileKey key)
	{
		Level level = this.levels.getLevel(key.getLevelNumber());

		// Compute the tile's SW lat/lon based on its row/col in the level's data set.
		Angle dLat = level.getTileDelta().getLatitude();
		Angle dLon = level.getTileDelta().getLongitude();
		Angle latOrigin = this.levels.getTileOrigin().getLatitude();
		Angle lonOrigin = this.levels.getTileOrigin().getLongitude();

		Angle minLatitude = Tile.computeRowLatitude(key.getRow(), dLat,
				latOrigin);
		Angle minLongitude = Tile.computeColumnLongitude(key.getColumn(), dLon,
				lonOrigin);

		Sector tileSector = new Sector(minLatitude, minLatitude.add(dLat),
				minLongitude, minLongitude.add(dLon));

		return new Tile(tileSector, level, key.getRow(), key.getColumn());
	}

	// Thread off a task to determine whether the file is local or remote and then retrieve it either from the file
	// cache or a remote server.
	protected void requestTile(TileKey key)
	{
		if (WorldWind.getTaskService().isFull())
			return;

		if (this.getLevels().isResourceAbsent(key))
			return;

		RequestTask request = new RequestTask(key, this);
		WorldWind.getTaskService().addTask(request);
	}

	protected static class RequestTask implements Runnable
	{
		protected final BasicElevationModel elevationModel;
		protected final TileKey tileKey;

		protected RequestTask(TileKey tileKey,
				BasicElevationModel elevationModel)
		{
			this.elevationModel = elevationModel;
			this.tileKey = tileKey;
		}

		public final void run()
		{
			try
			{
				// check to ensure load is still needed
				if (this.elevationModel.areElevationsInMemory(this.tileKey))
					return;

				Tile tile = this.elevationModel.createTile(this.tileKey);
				final URL url = WorldWind.getDataFileStore().findFile(
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
						WorldWind.getDataFileStore().removeFile(url);
						this.elevationModel.levels.markResourceAbsent(tile);
						String message = Logging.getMessage(
								"generic.DeletedCorruptDataFile", url);
						Logging.logger().info(message);
					}
				}

				this.elevationModel.downloadElevations(tile);
			}
			catch (IOException e)
			{
				String msg = Logging.getMessage(
						"ElevationModel.ExceptionRequestingElevations",
						this.tileKey.toString());
				Logging.logger().log(java.util.logging.Level.FINE, msg, e);
			}
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
	protected boolean loadElevations(Tile tile, java.net.URL url)
			throws IOException
	{
		BufferWrapper elevations = this.readElevations(url);
		if (elevations == null || elevations.length() == 0)
			return false;

		if (this.numExpectedValues > 0
				&& elevations.length() != this.numExpectedValues)
			return false; // corrupt file

		tile.elevations = elevations;
		this.addTileToCache(tile, elevations);

		return true;
	}

	protected void addTileToCache(Tile tile, BufferWrapper elevations)
	{
		// Level 0 tiles are held in the model itself; other levels are placed in the memory cache.
		if (tile.getLevelNumber() == 0)
			this.levelZeroTiles.putIfAbsent(tile.getTileKey(), tile);
		else
			this.memoryCache.add(tile.getTileKey(), tile, elevations
					.getSizeInBytes());
	}

	protected boolean areElevationsInMemory(TileKey key)
	{
		Tile tile = this.getTileFromMemory(key);
		return (tile != null && tile.elevations != null);
	}

	protected Tile getTileFromMemory(TileKey tileKey)
	{
		if (tileKey.getLevelNumber() == 0)
			return this.levelZeroTiles.get(tileKey);
		else
			return (Tile) this.memoryCache.getObject(tileKey);
	}

	// Read elevations from the file cache. Don't be confused by the use of a URL here: it's used so that files can
	// be read using System.getResource(URL), which will draw the data from a jar file in the classpath.
	protected BufferWrapper readElevations(URL url) throws IOException
	{
		try
		{
			ByteBuffer byteBuffer;
			synchronized (this.fileLock)
			{
				byteBuffer = WWIO.readURLContentToBuffer(url);
			}

			// Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
			AVList bufferParams = new AVListImpl();
			bufferParams.setValue(AVKey.DATA_TYPE, this.elevationDataPixelType);
			bufferParams
					.setValue(AVKey.BYTE_ORDER, this.elevationDataByteOrder);
			return BufferWrapper.wrap(byteBuffer, bufferParams);
		}
		catch (java.io.IOException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					"ElevationModel.ExceptionReadingElevationFile",
					url.toString());
			throw e;
		}
	}

	protected static ByteBuffer convertImageToElevations(ByteBuffer buffer,
			String contentType) throws IOException
	{
		File tempFile = File.createTempFile("wwj-", WWIO
				.makeSuffixForMimeType(contentType));
		try
		{
			WWIO.saveBuffer(buffer, tempFile);
			BufferedImage image = ImageIO.read(tempFile);
			ByteBuffer byteBuffer = BufferUtil.newByteBuffer(image.getWidth()
					* image.getHeight() * 2);
			byteBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			ShortBuffer bilBuffer = byteBuffer.asShortBuffer();

			WritableRaster raster = image.getRaster();
			int[] samples = new int[raster.getWidth() * raster.getHeight()];
			raster.getSamples(0, 0, raster.getWidth(), raster.getHeight(), 0,
					samples);
			for (int sample : samples)
			{
				bilBuffer.put((short) sample);
			}

			return byteBuffer;
		}
		finally
		{
			if (tempFile != null)
				//noinspection ResultOfMethodCallIgnored
				tempFile.delete();
		}
	}

	protected void downloadElevations(final Tile tile)
	{
		if (!this.isNetworkRetrievalEnabled())
		{
			this.getLevels().markResourceAbsent(tile);
			return;
		}

		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		java.net.URL url = null;
		try
		{
			url = tile.getResourceURL();
			if (WorldWind.getNetworkStatus().isHostUnavailable(url))
			{
				this.getLevels().markResourceAbsent(tile);
				return;
			}
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

	protected static class DownloadPostProcessor implements
			RetrievalPostProcessor
	{
		protected Tile tile;
		protected BasicElevationModel elevationModel;

		public DownloadPostProcessor(Tile tile,
				BasicElevationModel elevationModel)
		{
			// don't validate - constructor is only available to classes with private access.
			this.tile = tile;
			this.elevationModel = elevationModel;
		}

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
				{
					if (retriever.getState().equals(
							Retriever.RETRIEVER_STATE_ERROR))
						this.elevationModel.levels
								.markResourceAbsent(this.tile);

					return null;
				}

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

				final File outFile = WorldWind.getDataFileStore().newFile(
						tile.getPath());
				if (outFile == null)
					return null;

				if (outFile.exists())
					return buffer;

				if (buffer != null)
				{
					String contentType = r.getContentType();

					if (contentType.contains("xml")
							|| contentType.contains("html")
							|| contentType.contains("text"))
					{
						this.elevationModel.getLevels().markResourceAbsent(
								this.tile);

						StringBuffer sb = new StringBuffer();
						while (buffer.hasRemaining())
						{
							sb.append((char) buffer.get());
						}
						// TODO: parse out the message if the content is xml or html.
						Logging.logger().severe(sb.toString());

						return null;
					}

					else if (contentType.contains("image")
							&& !contentType.equals("image/bil"))
					{
						// Convert to .bil and save the result
						buffer = convertImageToElevations(buffer, contentType);
						System.out.printf("buffer size %d, content type %s\n",
								buffer.limit(), contentType);
					}

					synchronized (elevationModel.fileLock)
					{
						WWIO.saveBuffer(buffer, outFile);
					}

					return buffer;
				}
			}
			catch (ClosedByInterruptException e)
			{
				Logging.logger().log(
						java.util.logging.Level.FINE,
						Logging.getMessage("generic.OperationCancelled",
								"elevations retrieval"), e);
			}
			catch (java.io.IOException e)
			{
				this.elevationModel.getLevels().markResourceAbsent(this.tile);
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

	protected static class Elevations
	{
		protected final BasicElevationModel elevationModel;
		protected java.util.Set<Tile> tiles;
		protected double extremes[] = null;
		protected final double achievedResolution;

		protected Elevations(BasicElevationModel elevationModel,
				double achievedResolution)
		{
			this.elevationModel = elevationModel;
			this.achievedResolution = achievedResolution;
		}

		public double getElevation(Angle latitude, Angle longitude)
		{
			if (latitude == null || longitude == null)
			{
				String msg = Logging.getMessage("nullValue.AngleIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}

			if (this.tiles == null)
				return this.elevationModel.getMissingDataSignal();

			try
			{
				for (BasicElevationModel.Tile tile : this.tiles)
				{
					if (tile.getSector().contains(latitude, longitude))
						return this.elevationModel.lookupElevation(
								latitude.radians, longitude.radians, tile);
				}

				return this.elevationModel.getMissingDataSignal();
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
												latitude, longitude), e);

				return this.elevationModel.getMissingDataSignal();
			}
		}

		public double[] getExtremes(Angle latitude, Angle longitude)
		{
			if (latitude == null || longitude == null)
			{
				String msg = Logging.getMessage("nullValue.AngleIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}

			if (this.extremes != null)
				return this.extremes;

			if (this.tiles == null || tiles.size() == 0)
				return this.elevationModel.getExtremeElevations(latitude,
						longitude);

			return this.getExtremes();
		}

		public double[] getExtremes()
		{
			if (this.extremes != null)
				return this.extremes;

			if (this.tiles == null || tiles.size() == 0)
				return this.extremes = new double[] {
						this.elevationModel.getMinElevation(),
						this.elevationModel.getMaxElevation() };

			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;

			for (BasicElevationModel.Tile tile : this.tiles)
			{
				int len = tile.elevations.length();
				if (len == 0)
					return null;

				for (int i = 0; i < len; i++)
				{
					double h = tile.elevations.getDouble(i);

					if (h == this.elevationModel.getMissingDataSignal())
						h = this.elevationModel.getMissingDataReplacement();

					if (h > max)
						max = h;
					if (h < min)
						min = h;
				}
			}

			if (min == Double.MAX_VALUE)
				min = this.elevationModel.getMinElevation();
			if (max == -Double.MAX_VALUE)
				max = this.elevationModel.getMaxElevation();

			return this.extremes = new double[] { min, max };
		}
	}

	public double getUnmappedElevation(Angle latitude, Angle longitude)
	{
		if (latitude == null || longitude == null)
		{
			String msg = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Level lastLevel = this.levels.getLastLevel(latitude, longitude);
		final TileKey tileKey = new TileKey(latitude, longitude, this.levels,
				lastLevel.getLevelNumber());
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
			Level firstLevel = this.levels.getFirstLevel();
			final TileKey zeroKey = new TileKey(latitude, longitude,
					this.levels, firstLevel.getLevelNumber());
			this.requestTile(zeroKey);

			return this.getMissingDataSignal();
		}

		return this.lookupElevation(latitude.radians, longitude.radians, tile);
	}

	public double getElevations(Sector sector, List<? extends LatLon> latlons,
			double targetResolution, double[] buffer)
	{
		if (sector == null)
		{
			String msg = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (latlons == null)
		{
			String msg = Logging.getMessage("nullValue.LatLonListIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (buffer == null)
		{
			String msg = Logging.getMessage("nullValue.ElevationsBufferIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (buffer.length < latlons.size())
		{
			String msg = Logging.getMessage(
					"ElevationModel.ElevationsBufferTooSmall", latlons.size());
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Level targetLevel = this.getTargetLevel(sector, targetResolution);
		if (targetLevel == null)
			return 0;

		Elevations elevations = this.getElevations(sector, this.levels,
				targetLevel.getLevelNumber());
		if (elevations == null)
			return 0;

		for (int i = 0; i < latlons.size(); i++)
		{
			LatLon ll = latlons.get(i);
			if (ll == null)
				continue;

			double value = elevations.getElevation(ll.getLatitude(), ll
					.getLongitude());

			// If an elevation at the given location is available, then write that elevation to the destination buffer.
			// Otherwise do nothing.
			if (value != this.getMissingDataSignal())
				buffer[i] = value;
		}

		return elevations.achievedResolution;
	}

	protected Level getTargetLevel(Sector sector, double targetSize)
	{
		Level lastLevel = this.levels.getLastLevel(sector); // finest resolution available
		if (lastLevel == null)
			return null;

		if (lastLevel.getTexelSize() >= targetSize)
			return lastLevel; // can't do any better than this

		for (Level level : this.levels.getLevels())
		{
			if (level.getTexelSize() <= targetSize)
				return level;

			if (level == lastLevel)
				break;
		}

		return lastLevel;
	}

	protected double lookupElevation(final double latRadians,
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

		double eLeft = tile.elevations.getDouble(k);
		double eRight = i < (tileWidth - 1) ? tile.elevations.getDouble(k + 1)
				: eLeft;

		if (this.getMissingDataSignal() == eLeft
				|| this.getMissingDataSignal() == eRight)
			return this.getMissingDataSignal();

		double dw = sectorDeltaLon / (tileWidth - 1);
		double dh = sectorDeltaLat / (tileHeight - 1);
		double ssLon = (dLon - i * dw) / dw;
		double ssLat = (dLat - j * dh) / dh;

		double eTop = eLeft + ssLon * (eRight - eLeft);

		if (j < tileHeight - 1 && i < tileWidth - 1)
		{
			eLeft = tile.elevations.getDouble(k + tileWidth);
			eRight = tile.elevations.getDouble(k + tileWidth + 1);

			if (this.getMissingDataSignal() == eLeft
					|| this.getMissingDataSignal() == eRight)
				return this.getMissingDataSignal();
		}

		double eBot = eLeft + ssLon * (eRight - eLeft);
		return eTop + ssLat * (eBot - eTop);
	}

	public double[] getExtremeElevations(Angle latitude, Angle longitude)
	{
		if (latitude == null || longitude == null)
		{
			String msg = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (this.extremesLevel < 0 || this.extremes == null)
			return new double[] { this.getMinElevation(),
					this.getMaxElevation() };

		try
		{
			LatLon delta = this.levels.getLevel(this.extremesLevel)
					.getTileDelta();
			LatLon origin = this.levels.getTileOrigin();
			final int row = Tile.computeRow(delta.getLatitude(), latitude,
					origin.getLatitude());
			final int col = Tile.computeColumn(delta.getLongitude(), longitude,
					origin.getLongitude());

			final int nCols = Tile.computeColumn(delta.getLongitude(),
					Angle.POS180, Angle.NEG180) + 1;

			int index = 2 * (row * nCols + col);
			double min = this.extremes.getDouble(index);
			double max = this.extremes.getDouble(index + 1);

			if (min == this.getMissingDataSignal())
				min = this.getMissingDataReplacement();
			if (max == this.getMissingDataSignal())
				max = this.getMissingDataReplacement();

			return new double[] { min, max };
		}
		catch (Exception e)
		{
			String message = Logging.getMessage(
					"BasicElevationModel.ExceptionDeterminingExtremes",
					new LatLon(latitude, longitude));
			Logging.logger().log(java.util.logging.Level.WARNING, message, e);

			return new double[] { this.getMinElevation(),
					this.getMaxElevation() };
		}
	}

	public double[] getExtremeElevations(Sector sector)
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
			LatLon origin = this.levels.getTileOrigin();
			final int nwRow = Tile.computeRow(delta.getLatitude(), sector
					.getMaxLatitude(), origin.getLatitude());
			final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
					.getMinLongitude(), origin.getLongitude());
			final int seRow = Tile.computeRow(delta.getLatitude(), sector
					.getMinLatitude(), origin.getLatitude());
			final int seCol = Tile.computeColumn(delta.getLongitude(), sector
					.getMaxLongitude(), origin.getLongitude());

			final int nCols = Tile.computeColumn(delta.getLongitude(),
					Angle.POS180, Angle.NEG180) + 1;

			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;

			for (int row = seRow; row <= nwRow; row++)
			{
				for (int col = nwCol; col <= seCol; col++)
				{
					int index = 2 * (row * nCols + col);
					double a = this.extremes.getDouble(index);
					double b = this.extremes.getDouble(index + 1);

					if (a == this.getMissingDataSignal())
						a = this.getMissingDataReplacement();
					if (b == this.getMissingDataSignal())
						b = this.getMissingDataReplacement();

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

			// Set to model's limits if for some reason a limit wasn't determined
			if (min == Double.MAX_VALUE)
				min = this.getMinElevation();
			if (max == Double.MAX_VALUE)
				max = this.getMaxElevation();

			return new double[] { min, max };
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

	public void loadExtremeElevations(String extremesFileName)
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

			AVList bufferParams = new AVListImpl();
			bufferParams.setValue(AVKey.DATA_TYPE, this.elevationDataPixelType);
			bufferParams.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN); // Extremes are always saved in JVM byte order
			this.extremes = BufferWrapper.wrap(WWIO.readStreamToBuffer(is),
					bufferParams);
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
			WWIO.closeStream(is, extremesFileName);
		}
	}

	protected static class Tile extends gov.nasa.worldwind.util.Tile implements
			Cacheable
	{
		private BufferWrapper elevations; // the elevations themselves

		private Tile(Sector sector, Level level, int row, int col)
		{
			super(sector, level, row, col);
		}

		public BufferWrapper getElevations()
		{
			return elevations;
		}
	}

	protected Elevations getElevations(Sector sector, LevelSet levelSet,
			int targetLevelNumber)
	{
		// Compute the intersection of the requested sector with the LevelSet's sector.
		// The intersection will be used to determine which Tiles in the LevelSet are in the requested sector.
		sector = sector.intersection(levelSet.getSector());

		Level targetLevel = levelSet.getLevel(targetLevelNumber);
		LatLon delta = targetLevel.getTileDelta();
		LatLon origin = levelSet.getTileOrigin();
		final int nwRow = Tile.computeRow(delta.getLatitude(), sector
				.getMaxLatitude(), origin.getLatitude());
		final int nwCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMinLongitude(), origin.getLongitude());
		final int seRow = Tile.computeRow(delta.getLatitude(), sector
				.getMinLatitude(), origin.getLatitude());
		final int seCol = Tile.computeColumn(delta.getLongitude(), sector
				.getMaxLongitude(), origin.getLongitude());

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
		ArrayList<TileKey> requested = new ArrayList<TileKey>();

		boolean missingTargetTiles = false;
		boolean missingLevelZeroTiles = false;
		for (int row = seRow; row <= nwRow; row++)
		{
			for (int col = nwCol; col <= seCol; col++)
			{
				TileKey key = new TileKey(targetLevel.getLevelNumber(), row,
						col, targetLevel.getCacheName());
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
				TileKey fallbackKey;
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
						{
							tiles.add(tile);
						}
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

		Elevations elevations;

		if (missingLevelZeroTiles || tiles.isEmpty())
		{
			// Double.MAX_VALUE is a signal for no in-memory tile for a given region of the sector.
			elevations = new Elevations(this, Double.MAX_VALUE);
		}
		else if (missingTargetTiles)
		{
			// Use the level of the the lowest resolution found to denote the resolution of this elevation set.
			// The list of tiles is sorted first by level, so use the level of the list's last entry.
			elevations = new Elevations(this, tiles.last().getLevel()
					.getTexelSize());
		}
		else
		{
			elevations = new Elevations(this, tiles.last().getLevel()
					.getTexelSize());
		}

		elevations.tiles = tiles;

		return elevations;
	}

	@SuppressWarnings( { "UnusedDeclaration" })
	public ByteBuffer generateExtremeElevations(int levelNumber)
	{
		return null;
		//        long waitTime = 1000;
		//        long timeout = 10 * 60 * 1000;
		//
		//        ElevationModel.Elevations elevs;
		//        BasicElevationModel em = new EarthElevationModel();
		//
		//        double delta = 20d / Math.pow(2, levelNumber);
		//
		//        int numLats = (int) Math.ceil(180 / delta);
		//        int numLons = (int) Math.ceil(360 / delta);
		//
		//        System.out.printf("Building extreme elevations for layer %d, num lats %d, num lons %d\n",
		//            levelNumber, numLats, numLons);
		//
		//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2 * 2 * numLats * numLons);
		//        ShortBuffer buffer = byteBuffer.asShortBuffer();
		//        buffer.rewind();
		//
		//        Level level = this.levels.getLevel(levelNumber);
		//        for (int j = 0; j < numLats; j++)
		//        {
		//            double lat = -90 + j * delta;
		//            for (int i = 0; i < numLons; i++)
		//            {
		//                double lon = -180 + i * delta;
		//                Sector s = Sector.fromDegrees(lat, lat + delta, lon, lon + delta);
		//                long startTime = System.currentTimeMillis();
		//                while ((elevs = em.getElevations(s, level)) == null)
		//                {
		//                    try
		//                    {
		//                        Thread.sleep(waitTime);
		//                    }
		//                    catch (InterruptedException e)
		//                    {
		//                        e.printStackTrace();
		//                    }
		//                    if (System.currentTimeMillis() - startTime >= timeout)
		//                        break;
		//                }
		//
		//                if (elevs == null)
		//                {
		//                    System.out.printf("null elevations for (%f, %f) %s\n", lat, lon, s);
		//                    continue;
		//                }
		//
		//
		//                double[] extremes = elevs.getExtremes();
		//                if (extremes != null)
		//                {
		//                    System.out.printf("%d:%d, (%f, %f) min = %f, max = %f\n", j, i, lat, lon, extremes[0], extremes[1]);
		//                    buffer.put((short) extremes[0]).put((short) extremes[1]);
		//                }
		//                else
		//                    System.out.printf("no extremes for (%f, %f)\n", lat, lon);
		//            }
		//        }
		//
		//        return (ByteBuffer) buffer.rewind();
	}

	//
	//    public final int getTileCount(Sector sector, int resolution)
	//    {
	//        if (sector == null)
	//        {
	//            String msg = Logging.getMessage("nullValue.SectorIsNull");
	//            Logging.logger().severe(msg);
	//            throw new IllegalArgumentException(msg);
	//        }
	//
	//        // Collect all the elevation tiles intersecting the input sector. If a desired tile is not curently
	//        // available, choose its next lowest resolution parent that is available.
	//        final Level targetLevel = this.levels.getLevel(resolution);
	//
	//        LatLon delta = this.levels.getLevel(resolution).getTileDelta();
	//        final int nwRow = Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude());
	//        final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude());
	//        final int seRow = Tile.computeRow(delta.getLatitude(), sector.getMinLatitude());
	//        final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude());
	//
	//        return (1 + (nwRow - seRow) * (1 + seCol - nwCol));
	//    }

	//**************************************************************//
	//********************  Restorable Support  ********************//
	//**************************************************************//

	public String getRestorableState()
	{
		// We only create a restorable state XML if this elevation model was constructed with an AVList.
		AVList constructionParams = (AVList) this
				.getValue(AVKey.CONSTRUCTION_PARAMETERS);
		if (constructionParams == null)
			return null;

		RestorableSupport rs = RestorableSupport.newRestorableSupport();
		// Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
		if (rs == null)
			return null;

		this.doGetRestorableState(rs, null);
		return rs.getStateAsXml();
	}

	public void restoreState(String stateInXml)
	{
		String message = Logging
				.getMessage("RestorableSupport.RestoreRequiresConstructor");
		Logging.logger().severe(message);
		throw new UnsupportedOperationException(message);
	}

	protected void doGetRestorableState(RestorableSupport rs,
			RestorableSupport.StateObject context)
	{
		AVList constructionParams = (AVList) this
				.getValue(AVKey.CONSTRUCTION_PARAMETERS);
		if (constructionParams != null)
		{
			for (Map.Entry<String, Object> avp : constructionParams
					.getEntries())
			{
				this.doGetRestorableStateForAVPair(avp.getKey(),
						avp.getValue(), rs, context);
			}
		}

		rs
				.addStateValueAsString(context, "ElevationModel.Name", this
						.getName());
		rs.addStateValueAsDouble(context, "ElevationModel.MissingDataFlag",
				this.getMissingDataSignal());
		rs.addStateValueAsDouble(context, "ElevationModel.MissingDataValue",
				this.getMissingDataReplacement());
		rs.addStateValueAsBoolean(context,
				"ElevationModel.NetworkRetrievalEnabled", this
						.isNetworkRetrievalEnabled());
		rs.addStateValueAsDouble(context, "ElevationModel.MinElevation", this
				.getMinElevation());
		rs.addStateValueAsDouble(context, "ElevationModel.MaxElevation", this
				.getMaxElevation());
		rs.addStateValueAsString(context, "BasicElevationModel.DataPixelType",
				this.getElevationDataPixelType());
		rs.addStateValueAsString(context, "BasicElevationModel.DataByteOrder",
				this.getElevationDataByteOrder());

		// We'll write the detail hint attribute only when it's a nonzero value.
		if (this.detailHint != 0.0)
			rs.addStateValueAsDouble(context, "BasicElevationModel.DetailHint",
					this.detailHint);

		RestorableSupport.StateObject so = rs.addStateObject(context, "avlist");
		for (Map.Entry<String, Object> avp : this.getEntries())
		{
			this.doGetRestorableStateForAVPair(avp.getKey(), avp.getValue(),
					rs, so);
		}
	}

	protected void doGetRestorableStateForAVPair(String key, Object value,
			RestorableSupport rs, RestorableSupport.StateObject context)
	{
		if (value == null)
			return;

		if (key.equals(AVKey.CONSTRUCTION_PARAMETERS))
			return;

		if (value instanceof LatLon)
		{
			rs.addStateValueAsLatLon(context, key, (LatLon) value);
		}
		else if (value instanceof Sector)
		{
			rs.addStateValueAsSector(context, key, (Sector) value);
		}
		else
		{
			rs.addStateValueAsString(context, key, value.toString());
		}
	}

	protected void doRestoreState(RestorableSupport rs,
			RestorableSupport.StateObject context)
	{
		String s = rs.getStateValueAsString(context, "ElevationModel.Name");
		if (s != null)
			this.setName(s);

		Double d = rs.getStateValueAsDouble(context,
				"ElevationModel.MissingDataFlag");
		if (d != null)
			this.setMissingDataSignal(d);

		d = rs
				.getStateValueAsDouble(context,
						"ElevationModel.MissingDataValue");
		if (d != null)
			this.setMissingDataReplacement(d);

		Boolean b = rs.getStateValueAsBoolean(context,
				"ElevationModel.NetworkRetrievalEnabled");
		if (b != null)
			this.setNetworkRetrievalEnabled(b);

		s = rs.getStateValueAsString(context,
				"BasicElevationModel.DataPixelType");
		if (s != null)
			this.setPixelType(s);

		s = rs.getStateValueAsString(context,
				"BasicElevationModel.DataByteOrder");
		if (s != null)
			this.setByteOrder(s);

		d = rs.getStateValueAsDouble(context, "BasicElevationModel.DetailHint");
		if (d != null)
			this.setDetailHint(d);

		// Intentionally omitting "ElevationModel.MinElevation" and "ElevationModel.MaxElevation" because they are final
		// properties only configurable at construction.

		RestorableSupport.StateObject so = rs.getStateObject(context, "avlist");
		if (so != null)
		{
			RestorableSupport.StateObject[] avpairs = rs.getAllStateObjects(so,
					"");
			if (avpairs != null)
			{
				for (RestorableSupport.StateObject avp : avpairs)
				{
					if (avp != null)
						this.doRestoreStateForObject(rs, avp);
				}
			}
		}
	}

	@SuppressWarnings( { "UnusedDeclaration" })
	protected void doRestoreStateForObject(RestorableSupport rs,
			RestorableSupport.StateObject so)
	{
		if (so == null)
			return;

		this.setValue(so.getName(), so.getValue());
	}

	protected static AVList restorableStateToParams(String stateInXml)
	{
		if (stateInXml == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		RestorableSupport rs;
		try
		{
			rs = RestorableSupport.parse(stateInXml);
		}
		catch (Exception e)
		{
			// Parsing the document specified by stateInXml failed.
			String message = Logging.getMessage(
					"generic.ExceptionAttemptingToParseStateXml", stateInXml);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message, e);
		}

		AVList params = new AVListImpl();
		restoreStateForParams(rs, null, params);
		return params;
	}

	protected static void restoreStateForParams(RestorableSupport rs,
			RestorableSupport.StateObject context, AVList params)
	{
		StringBuilder sb = new StringBuilder();

		String s = rs.getStateValueAsString(context, AVKey.DATA_CACHE_NAME);
		if (s != null)
			params.setValue(AVKey.DATA_CACHE_NAME, s);

		s = rs.getStateValueAsString(context, AVKey.SERVICE);
		if (s != null)
			params.setValue(AVKey.SERVICE, s);

		s = rs.getStateValueAsString(context, AVKey.DATASET_NAME);
		if (s != null)
			params.setValue(AVKey.DATASET_NAME, s);

		s = rs.getStateValueAsString(context, AVKey.FORMAT_SUFFIX);
		if (s != null)
			params.setValue(AVKey.FORMAT_SUFFIX, s);

		Integer i = rs.getStateValueAsInteger(context, AVKey.NUM_EMPTY_LEVELS);
		if (i != null)
			params.setValue(AVKey.NUM_EMPTY_LEVELS, i);

		i = rs.getStateValueAsInteger(context, AVKey.NUM_LEVELS);
		if (i != null)
			params.setValue(AVKey.NUM_LEVELS, i);

		i = rs.getStateValueAsInteger(context, AVKey.TILE_WIDTH);
		if (i != null)
			params.setValue(AVKey.TILE_WIDTH, i);

		i = rs.getStateValueAsInteger(context, AVKey.TILE_HEIGHT);
		if (i != null)
			params.setValue(AVKey.TILE_HEIGHT, i);

		Long lo = rs.getStateValueAsLong(context, AVKey.EXPIRY_TIME);
		if (lo != null)
			params.setValue(AVKey.EXPIRY_TIME, lo);

		LatLon ll = rs.getStateValueAsLatLon(context,
				AVKey.LEVEL_ZERO_TILE_DELTA);
		if (ll != null)
			params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, ll);

		ll = rs.getStateValueAsLatLon(context, AVKey.TILE_ORIGIN);
		if (ll != null)
			params.setValue(AVKey.TILE_ORIGIN, ll);

		Sector sector = rs.getStateValueAsSector(context, AVKey.SECTOR);
		if (sector != null)
			params.setValue(AVKey.SECTOR, sector);

		Double d = rs.getStateValueAsDouble("ElevationModel.MinElevation");
		if (d != null)
		{
			params.setValue(AVKey.ELEVATION_MIN, d);
		}
		else
		{
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("term.minElevation");
		}

		d = rs.getStateValueAsDouble("ElevationModel.MaxElevation");
		if (d != null)
		{
			params.setValue(AVKey.ELEVATION_MAX, d);
		}
		else
		{
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("term.maxElevation");
		}

		if (sb.length() > 0)
		{
			String message = Logging.getMessage(
					"BasicElevationModel.InvalidDescriptorFields", sb
							.toString());
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
	}
}
