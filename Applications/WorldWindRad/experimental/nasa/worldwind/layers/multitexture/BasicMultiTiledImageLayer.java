/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.layers.multitexture;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.formats.dds.DDSConverter;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

/**
 * @author tag
 * @version $Id: BasicTiledImageLayer.java 5051 2008-04-14 04:51:50Z tgaskins $
 */
public class BasicMultiTiledImageLayer extends MultiTiledImageLayer
{
	private final Object fileLock = new Object();

	public BasicMultiTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);

		if (!WorldWind.getMemoryCacheSet().containsCache(
				MultiTextureTile.class.getName()))
		{
			long size = Configuration.getLongValue(
					AVKey.TEXTURE_IMAGE_CACHE_SIZE, 3000000L);
			MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
			cache.setName("Texture Tiles");
			WorldWind.getMemoryCacheSet().addCache(
					MultiTextureTile.class.getName(), cache);
		}
	}

	public BasicMultiTiledImageLayer(AVList params)
	{
		this(new LevelSet(params));
		this.setValue(AVKey.CONSTRUCTION_PARAMETERS, params);
	}

	protected void forceTextureLoad(MultiTextureTile tile)
	{
		final URL[] textureURLs = nullArray(getTextureURLs(tile));

		if (textureURLs != null && !this.areTexturesExpired(tile, textureURLs))
		{
			this.loadTextures(tile, textureURLs);
		}
	}

	protected void requestTexture(DrawContext dc, MultiTextureTile tile)
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
		private final BasicMultiTiledImageLayer layer;
		private final MultiTextureTile tile;

		private RequestTask(MultiTextureTile tile,
				BasicMultiTiledImageLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		public void run()
		{
			// TODO: check to ensure load is still needed

			final java.net.URL[] textureURLs = this.layer.nullArray(this.layer
					.getTextureURLs(tile));
			if (textureURLs != null
					&& !this.layer.areTexturesExpired(tile, textureURLs))
			{
				if (this.layer.loadTextures(tile, textureURLs))
				{
					layer.getLevels().unmarkResourceAbsent(tile);
					this.layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
				else
				{
					// Assume that something's wrong with the file and delete it.
					for (java.net.URL textureURL : textureURLs)
					{
						gov.nasa.worldwind.WorldWind.getDataFileCache()
								.removeFile(textureURL);
						layer.getLevels().markResourceAbsent(tile);
						String message = Logging.getMessage(
								"generic.DeletedCorruptDataFile", textureURL);
						Logging.logger().info(message);
					}
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

	private boolean areTexturesExpired(MultiTextureTile tile,
			java.net.URL[] textureURLs)
	{
		boolean outOfDate = false;
		for (java.net.URL url : textureURLs)
			if (WWIO.isFileOutOfDate(url, tile.getLevel().getExpiryTime()))
				outOfDate = true;

		if (outOfDate)
		{
			for (java.net.URL url : textureURLs)
			{
				// The files have expired. Delete them.
				gov.nasa.worldwind.WorldWind.getDataFileCache().removeFile(url);
				String message = Logging.getMessage("generic.DataFileExpired",
						url);
				Logging.logger().fine(message);
			}
		}
		return outOfDate;
	}

	private boolean loadTextures(MultiTextureTile tile,
			java.net.URL[] textureURLs)
	{
		if (textureURLs == null || textureURLs.length == 0)
			return false;

		TextureData[] textureData = new TextureData[textureURLs.length];

		synchronized (this.fileLock)
		{
			for (int i = 0; i < textureURLs.length; i++)
			{
				textureData[i] = readTexture(textureURLs[i], this
						.isUseMipMaps());
				if (textureData[i] == null)
					return false;
			}
		}

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

	private void addTileToCache(MultiTextureTile tile)
	{
		WorldWind.getMemoryCache(MultiTextureTile.class.getName()).add(
				tile.getTileKey(), tile);
	}

	protected void downloadTexture(final MultiTextureTile tile)
	{
		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		java.net.URL[] urls;
		try
		{
			urls = nullArray(getResourceURLs(tile));
			if (urls == null)
				return;

			for (java.net.URL url : urls)
				if (WorldWind.getNetworkStatus().isHostUnavailable(url))
					return;
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

		Retriever[] retrievers = new Retriever[urls.length];
		java.net.URL[] fileURLs = nullArray(getTextureURLs(tile));

		if (fileURLs == null)
			return;

		if (urls.length != fileURLs.length)
			throw new IllegalStateException(
					"Number of texture URLs does not match number of resource URLs");

		for (int i = 0; i < urls.length; i++)
		{
			if ("http".equalsIgnoreCase(urls[i].getProtocol()))
			{
				retrievers[i] = new HTTPRetriever(urls[i],
						new DownloadPostProcessor(tile, this, fileURLs[i]));
			}
			else
			{
				Logging.logger().severe(
						Logging.getMessage(
								"layers.TextureLayer.UnknownRetrievalProtocol",
								urls[i].toString()));
				return;
			}

			// Apply any overridden timeouts.
			Integer cto = AVListImpl.getIntegerValue(this,
					AVKey.URL_CONNECT_TIMEOUT);
			if (cto != null && cto > 0)
				retrievers[i].setConnectTimeout(cto);
			Integer cro = AVListImpl.getIntegerValue(this,
					AVKey.URL_READ_TIMEOUT);
			if (cro != null && cro > 0)
				retrievers[i].setReadTimeout(cro);
			Integer srl = AVListImpl.getIntegerValue(this,
					AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
			if (srl != null && srl > 0)
				retrievers[i].setStaleRequestLimit(srl);
		}

		for (Retriever retriever : retrievers)
		{
			WorldWind.getRetrievalService().runRetriever(retriever,
					tile.getPriority());
		}
	}

	private void saveBuffer(java.nio.ByteBuffer buffer, java.io.File outFile)
			throws java.io.IOException
	{
		synchronized (this.fileLock) // sychronized with read of file in RequestTask.run()
		{
			WWIO.saveBuffer(buffer, outFile);
		}
	}

	private static class DownloadPostProcessor implements
			RetrievalPostProcessor
	{
		// TODO: Rewrite this inner class, factoring out the generic parts.
		private final MultiTextureTile tile;
		private final BasicMultiTiledImageLayer layer;
		private final URL file;

		public DownloadPostProcessor(MultiTextureTile tile,
				BasicMultiTiledImageLayer layer, URL file)
		{
			this.tile = tile;
			this.layer = layer;
			this.file = file;
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

				final File outFile = WorldWind.getDataFileCache().newFile(
						this.file.getFile());
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
						buffer = DDSConverter.convertToDDS(buffer, contentType);
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

	private <E> E[] nullArray(E[] array)
	{
		if (array == null)
			return null;
		for (E e : array)
			if (e == null)
				return null;
		return array;
	}

	protected java.net.URL[] getResourceURLs(MultiTextureTile tile)
			throws MalformedURLException
	{
		return new URL[] { tile.getResourceURL() };
	}

	protected java.net.URL[] getTextureURLs(MultiTextureTile tile)
	{
		return new URL[] { WorldWind.getDataFileCache().findFile(
				tile.getPath(), false) };
	}
}
