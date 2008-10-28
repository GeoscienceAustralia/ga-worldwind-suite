package layers.mask;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class MaskTiledImageLayer extends TiledImageLayer
{
	private final Object fileLock = new Object();

	public MaskTiledImageLayer(LevelSet levelSet)
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

	protected void forceTextureLoad(TextureTile tile)
	{
		final URL textureURL = WorldWind.getDataFileCache().findFile(
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
					gov.nasa.worldwind.WorldWind.getDataFileCache().removeFile(
							textureURL);
					layer.getLevels().markResourceAbsent(tile);
					String message = Logging.getMessage(
							"generic.DeletedCorruptDataFile", textureURL);
					Logging.logger().info(message);
				}
			}

			layer.downloadTexture(tile);
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

	private boolean loadTexture(TextureTile tile, java.net.URL textureURL)
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

	private void addTileToCache(TextureTile tile)
	{
		WorldWind.getMemoryCache(TextureTile.class.getName()).add(
				tile.getTileKey(), tile);
	}

	protected void downloadTexture(final TextureTile tile)
	{
		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		URL textureUrl;
		URL maskUrl;
		try
		{
			textureUrl = tile.getResourceURL();
			if (textureUrl == null)
				return;

			if (WorldWind.getNetworkStatus().isHostUnavailable(textureUrl))
				return;

			if (textureUrl.getQuery() == null)
			{
				maskUrl = new URL(textureUrl.toExternalForm() + "?mask");
			}
			else
			{
				maskUrl = new URL(textureUrl.toExternalForm() + "&mask");
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

		Retriever textureRetriever;
		Retriever maskRetriever;
		DownloadPostProcessor dpp = new DownloadPostProcessor(tile, this);

		if ("http".equalsIgnoreCase(textureUrl.getProtocol()))
		{
			textureRetriever = new HTTPRetriever(textureUrl, dpp);
			maskRetriever = new HTTPRetriever(maskUrl, dpp);
		}
		else
		{
			Logging.logger().severe(
					Logging.getMessage(
							"layers.TextureLayer.UnknownRetrievalProtocol",
							textureUrl.toString()));
			return;
		}

		dpp.setRetrievers(textureRetriever, maskRetriever);

		// Apply any overridden timeouts.
		Integer cto = AVListImpl.getIntegerValue(this,
				AVKey.URL_CONNECT_TIMEOUT);
		if (cto != null && cto > 0)
		{
			textureRetriever.setConnectTimeout(cto);
			maskRetriever.setConnectTimeout(cto);
		}
		Integer cro = AVListImpl.getIntegerValue(this, AVKey.URL_READ_TIMEOUT);
		if (cro != null && cro > 0)
		{
			textureRetriever.setReadTimeout(cro);
			maskRetriever.setReadTimeout(cro);
		}
		Integer srl = AVListImpl.getIntegerValue(this,
				AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (srl != null && srl > 0)
		{
			textureRetriever.setStaleRequestLimit(srl);
			maskRetriever.setStaleRequestLimit(srl);
		}

		WorldWind.getRetrievalService().runRetriever(textureRetriever,
				tile.getPriority());
		WorldWind.getRetrievalService().runRetriever(maskRetriever,
				tile.getPriority());
	}

	private void saveImage(BufferedImage image, String format, File file)
			throws IOException
	{
		synchronized (this.fileLock) // sychronized with read of file in RequestTask.run()
		{
			ImageIO.write(image, format, file);
		}
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

	private static class DownloadPostProcessor implements
			RetrievalPostProcessor
	{
		private final TextureTile tile;
		private final MaskTiledImageLayer layer;

		private Retriever textureRetriever;
		private Retriever maskRetriever;

		private BufferedImage texture;
		private BufferedImage mask;

		public DownloadPostProcessor(TextureTile tile, MaskTiledImageLayer layer)
		{
			this.tile = tile;
			this.layer = layer;
		}

		public void setRetrievers(Retriever textureRetriever,
				Retriever maskRetriever)
		{
			this.textureRetriever = textureRetriever;
			this.maskRetriever = maskRetriever;
		}

		public ByteBuffer run(Retriever retriever)
		{
			if (retriever == null)
			{
				finish();
				String msg = Logging.getMessage("nullValue.RetrieverIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}

			if (retriever != textureRetriever && retriever != maskRetriever)
			{
				finish();
				throw new IllegalArgumentException();
			}

			ByteBuffer data = getData(retriever);
			if (data == null)
			{
				finish();
			}
			else
			{
				BufferedImage image = null;
				InputStream is = new ByteArrayInputStream(data.array());
				try
				{
					image = ImageIO.read(is);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					finish();
				}

				if (retriever == textureRetriever)
				{
					texture = image;
				}
				else
				{
					mask = image;
				}
			}

			if (texture != null && mask != null)
			{
				finish();
			}

			return data;
		}

		public ByteBuffer getData(Retriever retriever)
		{
			if (!retriever.getState().equals(
					Retriever.RETRIEVER_STATE_SUCCESSFUL))
			{
				return null;
			}

			if (retriever instanceof HTTPRetriever)
			{
				HTTPRetriever htr = (HTTPRetriever) retriever;
				if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
				{
					return null;
				}
			}

			URLRetriever r = (URLRetriever) retriever;
			String contentType = r.getContentType();
			if (contentType != null && contentType.contains("image"))
			{
				return r.getBuffer();
			}
			return null;
		}

		private void finish()
		{
			if (tryFinish())
			{
				this.layer.firePropertyChange(AVKey.LAYER, null, this);
			}
			else
			{
				texture = null;
				mask = null;
				this.layer.getLevels().markResourceAbsent(this.tile);
			}
		}

		private boolean tryFinish()
		{
			if (texture == null || mask == null)
			{
				return false;
			}

			final File outFile = WorldWind.getDataFileCache().newFile(
					this.tile.getPath());
			if (outFile == null)
				return false;

			if (outFile.exists())
				return true;

			Graphics2D g2d = mask.createGraphics();
			g2d.setComposite(AlphaComposite.SrcIn);
			g2d.drawImage(texture, 0, 0, null);
			g2d.dispose();

			try
			{
				//TODO take out hardcoded PNG
				layer.saveImage(mask, "PNG", outFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}

			return true;
		}
	}
}
