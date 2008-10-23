package mask;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.formats.dds.DDSConverter;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class MaskTiledImageLayer extends TiledImageLayer
{
	private final Object fileLock = new Object();
	private MaskLevelSet levelSet;

	public MaskTiledImageLayer(MaskLevelSet levelSet)
	{
		super(levelSet);
		this.levelSet = new MaskLevelSet(levelSet);

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

	@Override
	protected MaskLevelSet getLevels()
	{
		return levelSet;
	}

	private String getMaskPath(TextureTile tile)
	{
		String path = levelSet.getMaskCacheName() + "/"
				+ tile.getLevel().getLevelName() + "/" + tile.getRow() + "/"
				+ tile.getRow() + "_" + tile.getColumn();
		if (!tile.getLevel().isEmpty())
			path += levelSet.getMaskFormatSuffix();
		return path;
	}

	private URL getMaskResourceURL(TextureTile tile)
			throws MalformedURLException
	{
		return levelSet.getMaskTileUrlBuilder().getURL(tile, null);
	}

	protected void forceTextureLoad(TextureTile tile)
	{
		final URL textureURL = WorldWind.getDataFileCache().findFile(
				tile.getPath(), true);
		final URL maskURL = WorldWind.getDataFileCache().findFile(
				getMaskPath(tile), true);

		if (textureURL != null && maskURL != null
				&& !this.isTextureExpired(tile, textureURL)
				&& !this.isTextureExpired(tile, maskURL))
		{
			this.loadTexture(tile, textureURL, maskURL);
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
		private final MaskTiledImageLayer layer;
		private final TextureTile tile;

		private RequestTask(TextureTile tile, MaskTiledImageLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		public void run()
		{
			URL textureURL = WorldWind.getDataFileCache().findFile(
					tile.getPath(), false);
			URL maskURL = WorldWind.getDataFileCache().findFile(
					layer.getMaskPath(tile), false);

			if (textureURL != null && maskURL != null
					&& !this.layer.isTextureExpired(tile, textureURL)
					&& !this.layer.isTextureExpired(tile, maskURL))
			{
				if (this.layer.loadTexture(tile, textureURL, maskURL))
				{
					layer.getLevels().unmarkResourceAbsent(tile);
					this.layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
				else
				{
					// Assume that something's wrong with the file and delete it.
					gov.nasa.worldwind.WorldWind.getDataFileCache().removeFile(
							textureURL);
					gov.nasa.worldwind.WorldWind.getDataFileCache().removeFile(
							maskURL);
					layer.getLevels().markResourceAbsent(tile);
					String message = Logging.getMessage(
							"generic.DeletedCorruptDataFile", textureURL);
					Logging.logger().info(message);
				}
			}

			textureURL = WorldWind.getDataFileCache().findFile(tile.getPath(),
					false);
			maskURL = WorldWind.getDataFileCache().findFile(
					layer.getMaskPath(tile), false);

			List<Download> downloads = new ArrayList<Download>();
			try
			{
				if (textureURL == null)
				{
					Download download = new Download(tile.getResourceURL(),
							WorldWind.getDataFileCache()
									.newFile(tile.getPath()));
					downloads.add(download);
				}

				if (maskURL == null)
				{
					Download download = new Download(layer
							.getMaskResourceURL(tile), WorldWind
							.getDataFileCache()
							.newFile(layer.getMaskPath(tile)));
					downloads.add(download);
				}
			}
			catch (MalformedURLException e)
			{
				Logging
						.logger()
						.log(
								java.util.logging.Level.SEVERE,
								Logging
										.getMessage(
												"layers.TextureLayer.ExceptionCreatingTextureUrl",
												tile), e);
			}

			layer.downloadTexture(tile, downloads);
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

	private boolean isTextureExpired(TextureTile tile, java.net.URL url)
	{
		if (!WWIO.isFileOutOfDate(url, tile.getLevel().getExpiryTime()))
			return false;

		// The file has expired. Delete it.
		gov.nasa.worldwind.WorldWind.getDataFileCache().removeFile(url);
		String message = Logging.getMessage("generic.DataFileExpired", url);
		Logging.logger().fine(message);
		return true;
	}

	private boolean loadTexture(TextureTile tile, java.net.URL textureURL,
			java.net.URL maskURL)
	{
		TextureData textureData;

		synchronized (this.fileLock)
		{
			textureData = readTexture(textureURL, maskURL, this.isUseMipMaps());
		}

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			this.addTileToCache(tile);

		return true;
	}

	private void addTileToCache(TextureTile tile)
	{
		WorldWind.getMemoryCache(TextureTile.class.getName()).add(
				tile.getTileKey(), tile);
	}

	protected void downloadTexture(TextureTile tile, List<Download> downloads)
	{
		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		DownloadPostProcessor dpp = new DownloadPostProcessor(tile, this);

		for (Download download : downloads)
		{
			URL url = download.url;
			if (url == null
					|| WorldWind.getNetworkStatus().isHostUnavailable(url))
			{
				return;
			}
			if (!"http".equalsIgnoreCase(url.getProtocol()))
			{
				Logging.logger().severe(
						Logging.getMessage(
								"layers.TextureLayer.UnknownRetrievalProtocol",
								url.toString()));
				return;
			}

			Retriever retriever = new HTTPRetriever(url, dpp);
			download.retriever = retriever;

			// Apply any overridden timeouts.
			Integer cto = AVListImpl.getIntegerValue(this,
					AVKey.URL_CONNECT_TIMEOUT);
			if (cto != null && cto > 0)
				retriever.setConnectTimeout(cto);
			Integer cro = AVListImpl.getIntegerValue(this,
					AVKey.URL_READ_TIMEOUT);
			if (cro != null && cro > 0)
				retriever.setReadTimeout(cro);
			Integer srl = AVListImpl.getIntegerValue(this,
					AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
			if (srl != null && srl > 0)
				retriever.setStaleRequestLimit(srl);
		}

		dpp.setDownloads(downloads);

		for (Download download : downloads)
		{
			WorldWind.getRetrievalService().runRetriever(download.retriever,
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

	private static TextureData readTexture(java.net.URL url, java.net.URL mask,
			boolean useMipMaps)
	{
		try
		{
			BufferedImage src = null;
			BufferedImage msk = null;
			try
			{
				src = ImageIO.read(url);
				msk = ImageIO.read(mask);
			}
			catch (Exception e)
			{
			}
			if (src != null && msk != null)
			{
				Graphics2D g2d = msk.createGraphics();
				g2d.setComposite(AlphaComposite.SrcIn);
				g2d.drawImage(src, 0, 0, null);
				g2d.dispose();
				return TextureIO.newTextureData(msk, useMipMaps);
			}
			else
			{
				return TextureIO.newTextureData(url, useMipMaps, null);
			}
		}
		catch (Exception e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					"layers.TextureLayer.ExceptionAttemptingToReadTextureFile",
					e);
			return null;
		}
	}

	private static class DownloadData
	{
		ByteBuffer buffer;
		String contentType;

		public DownloadData(ByteBuffer buffer, String contentType)
		{
			this.buffer = buffer;
			this.contentType = contentType;
		}
	}

	private static class Download
	{
		URL url;
		File file;

		Retriever retriever;
		DownloadData data;

		public Download(URL url, File file)
		{
			this.url = url;
			this.file = file;
		}
	}

	private static class DownloadPostProcessor implements
			RetrievalPostProcessor
	{
		private final TextureTile tile;
		private final MaskTiledImageLayer layer;

		private List<Download> downloads;
		private Map<Retriever, Download> downloadMap = new HashMap<Retriever, Download>();

		public DownloadPostProcessor(TextureTile tile, MaskTiledImageLayer layer)
		{
			this.tile = tile;
			this.layer = layer;
		}

		public void setDownloads(List<Download> downloads)
		{
			this.downloads = downloads;
			downloadMap.clear();

			for (Download download : downloads)
			{
				downloadMap.put(download.retriever, download);
			}
		}

		public ByteBuffer run(Retriever retriever)
		{
			synchronized (downloadMap)
			{
				if (retriever == null)
				{
					finish();
					String msg = Logging
							.getMessage("nullValue.RetrieverIsNull");
					Logging.logger().severe(msg);
					throw new IllegalArgumentException(msg);
				}

				Download download = downloadMap.remove(retriever);

				if (download == null)
				{
					finish();
					Logging.logger().severe(
							"Unknown retriever passed to PostProcessor");
					return null;
				}

				if (download.file == null)
				{
					finish();
					return null;
				}

				DownloadData data = getData(retriever);
				download.data = data;

				if (data == null)
				{
					this.layer.getLevels().markResourceAbsent(this.tile);
				}

				if (downloadMap.isEmpty())
				{
					finish();
				}

				if (data == null)
				{
					return null;
				}
				return data.buffer;
			}
		}

		public DownloadData getData(Retriever retriever)
		{
			if (!retriever.getState().equals(
					Retriever.RETRIEVER_STATE_SUCCESSFUL))
			{
				return null;
			}

			URLRetriever r = (URLRetriever) retriever;
			ByteBuffer buffer = r.getBuffer();

			if (retriever instanceof HTTPRetriever)
			{
				HTTPRetriever htr = (HTTPRetriever) retriever;
				if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
				{
					return null;
				}
			}

			if (buffer != null)
			{
				String contentType = r.getContentType();
				if (contentType == null)
				{
					return null;
				}
				else if (contentType.contains("xml")
						|| contentType.contains("html")
						|| contentType.contains("text"))
				{
					return null;
				}
				return new DownloadData(buffer, contentType);
			}
			return null;
		}

		private void finish()
		{
			boolean error = false;
			for (Download download : downloads)
			{
				DownloadData data = download.data;
				if (data == null)
				{
					error = true;
					this.layer.getLevels().markResourceAbsent(this.tile);
				}
				else
				{
					try
					{
						String contentType = data.contentType;
						ByteBuffer buffer = data.buffer;
						File outFile = download.file;
						if (contentType.contains("dds"))
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
							buffer = DDSConverter.convertToDDS(buffer,
									contentType);
							if (buffer != null)
								this.layer.saveBuffer(buffer, outFile);
						}
						else if (contentType.contains("image"))
						{
							// Just save whatever it is to the cache.
							this.layer.saveBuffer(buffer, outFile);
						}
					}
					catch (IOException e)
					{
						error = true;
						this.layer.getLevels().markResourceAbsent(this.tile);
					}
				}
			}

			if (!error)
			{
				this.layer.firePropertyChange(AVKey.LAYER, null, this);
			}
		}
	}
}
