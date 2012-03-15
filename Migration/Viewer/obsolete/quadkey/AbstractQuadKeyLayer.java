/*******************************************************************************
 * Copyright (c) 2006 Vladimir Silva and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Silva - initial API and implementation
 *******************************************************************************/
package quadkey;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Sphere;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.BasicOrbitView;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sun.opengl.util.texture.TextureIO;

/**
 * Common subs for QuadKey based tiled servers: MS Virtual Earth/Google Earth
 * 
 * @author Owner
 * 
 */
public abstract class AbstractQuadKeyLayer extends AbstractLayer
{
	private static final Logger logger = Logger
			.getLogger(AbstractQuadKeyLayer.class.getName());

	static final double earthRadius = 6378137;
	static final double earthCircum = earthRadius * 2.0 * Math.PI;
	static final double earthHalfCirc = earthCircum / 2;

	// Min zoom at which tiles will be rendered
	protected int minZoomLevel = 6;

	// Default map type h=hybrid, a=aerial (only hybrid tiles are supported)
	protected String mapType = "h";

	// default tile extension for hybrid tiles
	protected String mapExtension = ".jpeg";

	// tile col,(x), row(y) & zoom
	protected int zoomLevel;
	protected int tileX;
	protected int tileY;

	// tile sector
	protected Sector sector;
	protected String cacheRoot = "Earth/";

	// Used for rendering thread synchronization
	protected ConcurrentHashMap<String, QuadKeyEarthTile> loadingTiles = new ConcurrentHashMap<String, QuadKeyEarthTile>();

	/*
	 * Abstract subs: implemented by subclasses
	 */
	abstract protected Box QuadKeyToBox(String quadKey, int x, int y,
			int zoomLevel);

	abstract protected String TileToQuadKey(int tx, int ty, int zl);

	abstract protected void renderNeighbor(int tileX, int tileY, int zoomLevel,
			DrawContext dc);

	abstract protected String buildRequestUrl(String quadKey, String mapType,
			String mapExtension);

	// draw bounding cylinders around a tile?
	private boolean drawBoundingVolumes = false;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Layer name
	 */
	public AbstractQuadKeyLayer(String name)
	{
		super.setName(name);
		initMemoryCache(name);
	}

	/**
	 * initMemoryCache
	 */
	private void initMemoryCache(String name)
	{
		if (!WorldWind.getMemoryCacheSet().containsCache(name))
		{
			long size = Configuration.getLongValue(
					AVKey.TEXTURE_IMAGE_CACHE_SIZE, 4000000L);

			MemoryCache cache = new BasicMemoryCache((long) (0.9 * size),
					size * 3);
			cache.setName(name + " Texture Tiles");
			WorldWind.getMemoryCacheSet().addCache(name, cache);

			logger.fine("Initialized memory cache: " + cache);
		}
	}

	/**
	 * Tile memory caching operations
	 * 
	 * @param tileKey
	 * @return
	 */
	protected TextureTile getTileFromMemoryCache(String tileKey)
	{
		TextureTile tile = (TextureTile) WorldWind.getMemoryCache(getName())
				.getObject(tileKey);
		logger.fine("Got tile from memory " + tile + " key=" + tileKey);
		return tile;
	}

	protected boolean isTileInMemory(String tileKey)
	{
		boolean bool = WorldWind.getMemoryCache(getName()).contains(tileKey);
		return bool;
	}

	protected void addTileToMemoryCache(String tileKey, TextureTile tile)
	{
		if (getTileFromMemoryCache(tileKey) == null)
		{
			logger.fine("Adding tile " + tile + " key=" + tileKey
					+ " to memory cache.");
			WorldWind.getMemoryCache(getName()).add(tileKey, tile);
		}
	}

	/**
	 * Compute sector/quad key for the eye point
	 * 
	 * @param dc
	 */
	protected String computeSectors(DrawContext dc)
	{
		BasicOrbitView view = (BasicOrbitView) dc.getView();

		// get the eye position
		Position eyePoint = view.getEyePosition();

		double altitude = eyePoint.getElevation();
		double factor = altitude / earthRadius;
		//double distance = view.getAltitude();
		double distance = altitude;

		Angle trueViewRange;

		// calculate the true view range for the eye pos
		if (factor < 1)
			trueViewRange = Angle.fromRadians(Math.abs(Math.asin((distance)
					/ earthRadius)) * 2);
		else
			trueViewRange = Angle.fromRadians(Math.PI);

		// Get the zoom level 
		zoomLevel = GetZoomLevelByTrueViewRange(trueViewRange.degrees);

		// return if not at the min display zoom level
		if (zoomLevel < minZoomLevel)
			return null;

		/**
		 * Compute the quadkey for the eye point
		 */
		final double lat = eyePoint.getLatitude().degrees;
		final double lon = eyePoint.getLongitude().degrees;

		// pixel resolution
		final int meterY = LatitudeToYAtZoom(lat, zoomLevel);
		final int meterX = LongitudeToXAtZoom(lon, zoomLevel);

		// constant tile size
		final int imageSize = 256;

		// tile column/row
		tileX = meterX / imageSize;
		tileY = meterY / imageSize;

		final String quadKey = TileToQuadKey(tileX, tileY, zoomLevel);

		if (mapType.equals("h") || mapType.equals("a"))
			mapExtension = ".jpeg";
		else if (mapType.equals("r"))
			mapExtension = ".png";

		// tile sector
		sector = QuadKeyToSector(quadKey);

		logger.fine("Zoom=" + zoomLevel + " Quadkey=" + quadKey
				+ " tile sector=" + sector + " altitude=" + altitude
				+ " eye point=" + eyePoint + " trueViewRange=" + trueViewRange
				+ " lat=" + lat + " lon=" + lon);

		return quadKey;
	}

	/**
	 * Render tiles
	 * 
	 * @param dc
	 */
	protected void doRenderTiles(DrawContext dc)
	{
		final String quadKey = computeSectors(dc);

		// return if not at the min display zoom level
		if (quadKey == null)
			return;

		String url = buildRequestUrl(quadKey, getMapType(), mapExtension);
		System.out.println(url);

		// render center (eye) tile
		renderTile(dc, quadKey + mapExtension, url, sector);

		// Render other tiles outwards in surrounding circles
		for (int i = 1; i <= 5; i++)
		{
			renderNeighborTiles(tileY, tileX, zoomLevel, dc, i);
		}
	}

	/**
	 * Render a VE tile at a given sector
	 * 
	 * @param dc
	 * @param tileKey
	 * @param tileURL
	 * @param sector
	 */
	protected synchronized void renderTile(DrawContext dc, String tileKey,
			final String tileURL, Sector sector)
	{
		final String tileCachePath = cacheRoot + tileKey;

		try
		{
			QuadKeyEarthTile veTile = loadingTiles.containsKey(tileKey) ? (QuadKeyEarthTile) loadingTiles
					.get(tileKey)
					: new QuadKeyEarthTile(tileKey, tileURL, cacheRoot);

			TextureTile tile = null;

			if (veTile.isLoading())
			{
				return;
			}

			if (isTileInMemory(tileKey))
			{
				tile = getTileFromMemoryCache(tileKey);
			}
			else
			{
				// tile on disk?
				URL fileUrl = WorldWind.getDataFileCache().findFile(
						tileCachePath, false);

				if (fileUrl != null)
				{
					logger.fine("Tile from Disk:" + fileUrl);

					// Yes, load from disk
					tile = new TextureTile(sector);

					try
					{
						tile.setTexture(dc.getTextureCache(), TextureIO
								.newTexture(fileUrl, true, null));

						// save in mem cache
						addTileToMemoryCache(tileKey, tile);

						loadingTiles.remove(tileKey);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					// No, load from remote url
					logger.fine("Tile from Remote url:" + tileURL);

					loadingTiles.put(tileKey, veTile);

					veTile.download();
				}
			}

			if (tile != null)
				dc.getGeographicSurfaceTileRenderer().renderTile(dc, tile);

			if (drawBoundingVolumes)
				drawBoundingVolumes(dc, sector);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.severe(e + " for tile:" + tileKey);
			loadingTiles.remove(tileKey);

			// delete tile
			try
			{
				URL fileUrl = WorldWind.getDataFileCache().findFile(
						tileCachePath, false);

				if (fileUrl != null)
				{
					logger.severe("Deleting " + fileUrl);
					new File(fileUrl.toURI()).delete();
				}
			}
			catch (Exception ex)
			{
			}
		}
	}

	/**
	 * drawBoundingVolumes
	 * 
	 * @param dc
	 * @param sector
	 */
	private void drawBoundingVolumes(DrawContext dc, Sector sector)
	{
		Sphere s = (Sphere) Sector.computeBoundingSphere(dc.getGlobe(), dc
				.getVerticalExaggeration(), sector);
		dc.getGL().glColor3d(1, 1, 0);
		s.render(dc);

	}

	/**
	 * @param mapType
	 *            the mapType to set
	 */
	public void setMapType(String mapType)
	{
		this.mapType = mapType;
	}

	/**
	 * @return the mapType
	 */
	public String getMapType()
	{
		return mapType;
	}

	/**
	 * A box describing the bounds of a earth map tile
	 * 
	 * @author Owner
	 */
	static class Box
	{
		public int x;
		public int y;
		public int width;
		public int height;

		public Box(int x, int y, int width, int height)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

	/**
	 * Render other tiles outwards in surrounding circles
	 * 
	 * @param tileX
	 *            column
	 * @param tileY
	 *            row
	 * @param zoomLevel
	 * @param dc
	 */
	protected void renderNeighborTiles(int row, int col, int zoomLevel,
			DrawContext dc, int range)
	{
		int minRow = row - range;
		int maxRow = row + range;
		int minCol = col - range;
		int maxCol = col + range;

		for (int i = minRow; i <= maxRow; i++)
		{
			for (int j = minCol; j <= maxCol; j++)
			{
				//only outer edges, inner tiles should already be added
				if (i == minRow || i == maxRow || j == minCol || j == maxCol)
				{
					renderNeighbor(j, i, zoomLevel, dc);
				}
			}
		}
	}


	public Box QuadKeyToBox(String quadKey)
	{
		int x = 0;
		int y = 262144;
		return QuadKeyToBox(quadKey, x, y, 1);
	}

	/**
	 * Converts radians to degrees
	 * 
	 * @param d
	 * @return
	 */
	public double RadToDeg(double d)
	{
		return d / Math.PI * 180.0;
	}

	/**
	 * Converts a grid row to Latitude
	 * 
	 * @param y
	 * @param zoom
	 * @return
	 */
	public double YToLatitudeAtZoom(int y, int zoom)
	{
		double arc = earthCircum / ((1 << zoom) * 256);
		double metersY = earthHalfCirc - (y * arc);
		double a = Math.exp(metersY * 2 / earthRadius);
		double result = RadToDeg(Math.asin((a - 1) / (a + 1)));
		return result;
	}

	/**
	 * Converts a grid column to Longitude
	 * 
	 * @param x
	 * @param zoom
	 * @return
	 */
	public double XToLongitudeAtZoom(int x, int zoom)
	{
		double arc = earthCircum / ((1 << zoom) * 256);
		double metersX = (x * arc) - earthHalfCirc;
		double result = RadToDeg(metersX / earthRadius);
		return result;
	}

	/**
	 * Get the Lat/lon sector fro a given VE tile
	 * 
	 * @param quadKey
	 * @return
	 */
	public Sector QuadKeyToSector(String quadKey)
	{
		// Use the quadkey to determine a bounding box for the requested tile
		Box boundingBox = QuadKeyToBox(quadKey);

		// Get the lat longs of the corners of the box
		double lon = XToLongitudeAtZoom(boundingBox.x * 256, 18);
		double lat = YToLatitudeAtZoom(boundingBox.y * 256, 18);

		double lon2 = XToLongitudeAtZoom(
				(boundingBox.x + boundingBox.width) * 256, 18);
		double lat2 = YToLatitudeAtZoom(
				(boundingBox.y - boundingBox.height) * 256, 18);

		return new Sector(Angle.fromDegrees(lat), Angle.fromDegrees(lat2),
				Angle.fromDegrees(lon), Angle.fromDegrees(lon2));
	}

	/**
	 * Ve Zoom level by true view range
	 * 
	 * @param trueViewRange
	 * @return
	 */
	protected int GetZoomLevelByTrueViewRange(double trueViewRange)
	{
		int maxLevel = 3;
		int minLevel = 19;
		int numLevels = minLevel - maxLevel + 1;
		int retLevel = maxLevel;
		for (int i = 0; i < numLevels; i++)
		{
			retLevel = i + maxLevel;

			double viewAngle = 180;
			for (int j = 0; j < i; j++)
			{
				viewAngle = viewAngle / 2.0;
			}
			if (trueViewRange >= viewAngle)
			{
				break;
			}
		}
		return retLevel;
	}

	/**
	 * Convert degrees to radians
	 * 
	 * @param d
	 * @return
	 */
	protected double DegToRad(double d)
	{
		return d * Math.PI / 180.0;
	}

	/**
	 * Convert a latitude to row
	 * 
	 * @param lat
	 * @param zoom
	 * @return
	 */
	protected int LatitudeToYAtZoom(double lat, int zoom)
	{
		double arc = earthCircum / ((1 << zoom) * 256);
		double sinLat = Math.sin(DegToRad(lat));
		double metersY = earthRadius / 2
				* Math.log((1 + sinLat) / (1 - sinLat));
		int y = (int) Math.round((earthHalfCirc - metersY) / arc);
		return y;
	}

	/**
	 * Convert a longitude to column
	 * 
	 * @param lon
	 * @param zoom
	 * @return
	 */
	protected int LongitudeToXAtZoom(double lon, int zoom)
	{
		double arc = earthCircum / ((1 << zoom) * 256);
		double metersX = earthRadius * DegToRad(lon);
		int x = (int) Math.round((earthHalfCirc + metersX) / arc);
		return x;
	}
}
