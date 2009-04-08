/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.layers;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.Map;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

/**
 * @author tag
 * @version $Id: BasicTiledImageLayer.java 8941 2009-02-21 00:33:27Z dcollins $
 */
public class BasicTiledImageLayer extends TiledImageLayer
{
	private final Object fileLock = new Object();
	private AVList creationParams;

	public BasicTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);

		if (!WorldWind.getMemoryCacheSet().containsCache(
				TextureTile.class.getName()))
		{
			long size = Configuration.getLongValue(
					AVKey.TEXTURE_IMAGE_CACHE_SIZE, 3000000L);
			MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
			cache.setName("Texture Tiles");
			WorldWind.getMemoryCacheSet().addCache(TextureTile.class.getName(),
					cache);
		}
	}

	public BasicTiledImageLayer(AVList params)
	{
		this(new LevelSet(params));
		this.setValue(AVKey.CONSTRUCTION_PARAMETERS, params);
		this.creationParams = params.copy();
	}

	public BasicTiledImageLayer(String stateInXml)
	{
		this(xmlStateToParams(stateInXml));

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

		Boolean b = rs.getStateValueAsBoolean("Layer.Enabled");
		if (b != null)
			this.setEnabled(b);

		Double d = rs.getStateValueAsDouble("Layer.Opacity");
		if (d != null)
			this.setOpacity(d);

		d = rs.getStateValueAsDouble("Layer.MinActiveAltitude");
		if (d != null)
			this.setMinActiveAltitude(d);

		d = rs.getStateValueAsDouble("Layer.MaxActiveAltitude");
		if (d != null)
			this.setMaxActiveAltitude(d);

		b = rs.getStateValueAsBoolean("Layer.NetworkRetrievalEnabled");
		if (b != null)
			this.setNetworkRetrievalEnabled(b);

		String s = rs.getStateValueAsString("Layer.Name");
		if (s != null)
			this.setName(s);

		b = rs.getStateValueAsBoolean("TiledImageLayer.UseTransparentTextures");
		if (b != null)
			this.setUseTransparentTextures(b);

		RestorableSupport.StateObject so = rs.getStateObject("avlist");
		if (so != null)
		{
			RestorableSupport.StateObject[] avpairs = rs.getAllStateObjects(so,
					"");
			for (RestorableSupport.StateObject avp : avpairs)
			{
				if (avp != null)
					this.setValue(avp.getName(), avp.getValue());
			}
		}
	}

	protected static AVList xmlStateToParams(String stateInXml)
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

		String s = rs.getStateValueAsString(AVKey.DATA_CACHE_NAME);
		if (s != null)
			params.setValue(AVKey.DATA_CACHE_NAME, s);

		s = rs.getStateValueAsString(AVKey.SERVICE);
		if (s != null)
			params.setValue(AVKey.SERVICE, s);

		s = rs.getStateValueAsString(AVKey.DATASET_NAME);
		if (s != null)
			params.setValue(AVKey.DATASET_NAME, s);

		s = rs.getStateValueAsString(AVKey.FORMAT_SUFFIX);
		if (s != null)
			params.setValue(AVKey.FORMAT_SUFFIX, s);

		Integer i = rs.getStateValueAsInteger(AVKey.NUM_EMPTY_LEVELS);
		if (i != null)
			params.setValue(AVKey.NUM_EMPTY_LEVELS, i);

		i = rs.getStateValueAsInteger(AVKey.NUM_LEVELS);
		if (i != null)
			params.setValue(AVKey.NUM_LEVELS, i);

		i = rs.getStateValueAsInteger(AVKey.TILE_WIDTH);
		if (i != null)
			params.setValue(AVKey.TILE_WIDTH, i);

		i = rs.getStateValueAsInteger(AVKey.TILE_HEIGHT);
		if (i != null)
			params.setValue(AVKey.TILE_HEIGHT, i);

		Double d = rs.getStateValueAsDouble(AVKey.EXPIRY_TIME);
		if (d != null)
			params.setValue(AVKey.EXPIRY_TIME, Math.round(d));

		LatLon ll = rs.getStateValueAsLatLon(AVKey.LEVEL_ZERO_TILE_DELTA);
		if (ll != null)
			params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, ll);

		ll = rs.getStateValueAsLatLon(AVKey.TILE_ORIGIN);
		if (ll != null)
			params.setValue(AVKey.TILE_ORIGIN, ll);

		Sector sector = rs.getStateValueAsSector(AVKey.SECTOR);
		if (sector != null)
			params.setValue(AVKey.SECTOR, sector);

		return params;
	}

	private RestorableSupport makeRestorableState(AVList params)
	{
		RestorableSupport rs = RestorableSupport.newRestorableSupport();
		// Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
		if (rs == null)
			return null;

		for (Map.Entry<String, Object> p : params.getEntries())
		{
			if (p.getValue() == null)
				continue;

			if (p.getValue() instanceof LatLon)
			{
				rs.addStateValueAsLatLon(p.getKey(), (LatLon) p.getValue());
			}
			else if (p.getValue() instanceof Sector)
			{
				rs.addStateValueAsSector(p.getKey(), (Sector) p.getValue());
			}
			else
			{
				rs.addStateValueAsString(p.getKey(), p.getValue().toString());
			}
		}

		rs.addStateValueAsBoolean("Layer.Enabled", this.isEnabled());
		rs.addStateValueAsDouble("Layer.Opacity", this.getOpacity());
		rs.addStateValueAsDouble("Layer.MinActiveAltitude", this
				.getMinActiveAltitude());
		rs.addStateValueAsDouble("Layer.MaxActiveAltitude", this
				.getMaxActiveAltitude());
		rs.addStateValueAsBoolean("Layer.NetworkRetrievalEnabled", this
				.isNetworkRetrievalEnabled());
		rs.addStateValueAsString("Layer.Name", this.getName());
		rs.addStateValueAsBoolean("TiledImageLayer.UseTransparentTextures",
				this.isUseTransparentTextures());

		RestorableSupport.StateObject so = rs.addStateObject("avlist");
		for (Map.Entry<String, Object> p : this.getEntries())
		{
			if (p.getValue() == null)
				continue;

			if (p.getKey().equals(AVKey.CONSTRUCTION_PARAMETERS))
				continue;

			rs.addStateValueAsString(so, p.getKey(), p.getValue().toString());
		}

		return rs;
	}

	public String getRestorableState()
	{
		if (this.creationParams == null)
			return null;

		return this.makeRestorableState(this.creationParams).getStateAsXml();
	}

	public void restoreState(String stateInXml)
	{
		String message = Logging
				.getMessage("RestorableSupport.RestoreRequiresConstructor");
		Logging.logger().severe(message);
		throw new UnsupportedOperationException(message);
	}

	protected void forceTextureLoad(TextureTile tile)
	{
		final URL textureURL = WorldWind.getDataFileStore().findFile(
				tile.getPath(), true);

		if (textureURL != null && !this.isTextureExpired(tile, textureURL))
		{
			this.loadTexture(tile, textureURL);
		}
	}

	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
		if (this.getReferencePoint() != null)
			tile.setPriority(centroid.distanceTo3(this.getReferencePoint()));

		RequestTask task = new RequestTask(tile, this);
		this.getRequestQ().add(task);
	}

	private static class RequestTask implements Runnable,
			Comparable<RequestTask>
	{
		private final BasicTiledImageLayer layer;
		private final TextureTile tile;

		private RequestTask(TextureTile tile, BasicTiledImageLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		public void run()
		{
			// TODO: check to ensure load is still needed

			final java.net.URL textureURL = WorldWind.getDataFileStore()
					.findFile(tile.getPath(), false);
			if (textureURL != null
					&& !this.layer.isTextureExpired(tile, textureURL))
			{
				if (this.layer.loadTexture(tile, textureURL))
				{
					layer.getLevels().unmarkResourceAbsent(tile);
					this.layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
				else
				{
					// Assume that something's wrong with the file and delete it.
					gov.nasa.worldwind.WorldWind.getDataFileStore().removeFile(
							textureURL);
					layer.getLevels().markResourceAbsent(tile);
					String message = Logging.getMessage(
							"generic.DeletedCorruptDataFile", textureURL);
					Logging.logger().info(message);
				}
			}

			this.layer.downloadTexture(this.tile);
		}

		/**
		 * @param that
		 *            the task to compare
		 * @return -1 if <code>this</code> less than <code>that</code>, 1 if
		 *         greater than, 0 if equal
		 * @throws IllegalArgumentException
		 *             if <code>that</code> is null
		 */
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

	protected boolean isTextureExpired(TextureTile tile, java.net.URL textureURL)
	{
		if (!WWIO.isFileOutOfDate(textureURL, tile.getLevel().getExpiryTime()))
			return false;

		// The file has expired. Delete it.
		gov.nasa.worldwind.WorldWind.getDataFileStore().removeFile(textureURL);
		String message = Logging.getMessage("generic.DataFileExpired",
				textureURL);
		Logging.logger().fine(message);
		return true;
	}

	protected boolean loadTexture(TextureTile tile, java.net.URL textureURL)
	{
		TextureData textureData;

		synchronized (this.fileLock)
		{
			textureData = readTexture(textureURL, this.isUseMipMaps());
		}

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			this.addTileToCache(tile);

		return true;
	}

	private static TextureData readTexture(java.net.URL url, boolean useMipMaps)
	{
		try
		{
			return TextureIO.newTextureData(url, useMipMaps, null);
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

	protected void downloadTexture(final TextureTile tile)
	{
		if (!this.isNetworkRetrievalEnabled())
		{
			this.getLevels().markResourceAbsent(tile);
			return;
		}

		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		java.net.URL url;
		try
		{
			url = tile.getResourceURL();
			if (url == null)
				return;

			if (WorldWind.getNetworkStatus().isHostUnavailable(url))
			{
				this.getLevels().markResourceAbsent(tile);
				return;
			}
		}
		catch (java.net.MalformedURLException e)
		{
			Logging.logger().log(
					java.util.logging.Level.SEVERE,
					Logging.getMessage(
							"layers.TextureLayer.ExceptionCreatingTextureUrl",
							tile), e);
			return;
		}

		Retriever retriever;

		if ("http".equalsIgnoreCase(url.getProtocol())
				|| "https".equalsIgnoreCase(url.getProtocol()))
		{
			retriever = new HTTPRetriever(url, new DownloadPostProcessor(tile,
					this));
		}
		else
		{
			Logging.logger().severe(
					Logging.getMessage(
							"layers.TextureLayer.UnknownRetrievalProtocol", url
									.toString()));
			return;
		}

		// Apply any overridden timeouts.
		Integer cto = AVListImpl.getIntegerValue(this,
				AVKey.URL_CONNECT_TIMEOUT);
		if (cto != null && cto > 0)
			retriever.setConnectTimeout(cto);
		Integer cro = AVListImpl.getIntegerValue(this, AVKey.URL_READ_TIMEOUT);
		if (cro != null && cro > 0)
			retriever.setReadTimeout(cro);
		Integer srl = AVListImpl.getIntegerValue(this,
				AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (srl != null && srl > 0)
			retriever.setStaleRequestLimit(srl);

		WorldWind.getRetrievalService().runRetriever(retriever,
				tile.getPriority());
	}

	protected void saveBuffer(java.nio.ByteBuffer buffer, java.io.File outFile)
			throws java.io.IOException
	{
		synchronized (this.fileLock) // sychronized with read of file in RequestTask.run()
		{
			WWIO.saveBuffer(buffer, outFile);
		}
	}

	protected static class DownloadPostProcessor implements
			RetrievalPostProcessor
	{
		// TODO: Rewrite this inner class, factoring out the generic parts.
		private final TextureTile tile;
		private final BasicTiledImageLayer layer;

		public DownloadPostProcessor(TextureTile tile,
				BasicTiledImageLayer layer)
		{
			this.tile = tile;
			this.layer = layer;
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
					return null;

				URLRetriever r = (URLRetriever) retriever;
				ByteBuffer buffer = r.getBuffer();

				if (retriever instanceof HTTPRetriever)
				{
					HTTPRetriever htr = (HTTPRetriever) retriever;
					if (htr.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT)
					{
						// Mark tile as missing to avoid excessive attempts
						this.layer.getLevels().markResourceAbsent(this.tile);
						return null;
					}
					else if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
					{
						// Also mark tile as missing, but for an unknown reason.
						this.layer.getLevels().markResourceAbsent(this.tile);
						return null;
					}
				}

				final File outFile = WorldWind.getDataFileStore().newFile(
						this.tile.getPath());
				if (outFile == null)
					return null;

				if (outFile.exists())
					return buffer;

				// TODO: Better, more generic and flexible handling of file-format type
				if (buffer != null)
				{
					String contentType = r.getContentType();
					if (contentType == null)
					{
						// TODO: logger message
						return null;
					}

					if (contentType.contains("xml")
							|| contentType.contains("html")
							|| contentType.contains("text"))
					{
						this.layer.getLevels().markResourceAbsent(this.tile);

						StringBuffer sb = new StringBuffer();
						while (buffer.hasRemaining())
						{
							sb.append((char) buffer.get());
						}
						// TODO: parse out the message if the content is xml or html.
						Logging.logger().severe(sb.toString());

						return null;
					}
					else if (contentType.contains("dds"))
					{
						this.layer.saveBuffer(buffer, outFile);
					}
					else if (contentType.contains("zip"))
					{
						// Assume it's zipped DDS, which the retriever would have unzipped into the buffer.
						this.layer.saveBuffer(buffer, outFile);
					}
					else if (outFile.getName().endsWith(".dds"))
					{
						// Convert to DDS and save the result.
						buffer = DDSCompressor.compressImageBuffer(buffer);
						if (buffer != null)
							this.layer.saveBuffer(buffer, outFile);
					}
					else if (contentType.contains("image"))
					{
						// Just save whatever it is to the cache.
						this.layer.saveBuffer(buffer, outFile);
					}

					if (buffer != null)
					{
						this.layer.firePropertyChange(AVKey.LAYER, null, this);
					}
					return buffer;
				}
			}
			catch (ClosedByInterruptException e)
			{
				Logging.logger().log(
						java.util.logging.Level.FINE,
						Logging.getMessage("generic.OperationCancelled",
								"tiled image retrieval"), e);
			}
			catch (java.io.IOException e)
			{
				this.layer.getLevels().markResourceAbsent(this.tile);
				Logging
						.logger()
						.log(
								java.util.logging.Level.SEVERE,
								Logging
										.getMessage(
												"layers.TextureLayer.ExceptionSavingRetrievedTextureFile",
												tile.getPath()), e);
			}

			return null;
		}
	}
}
