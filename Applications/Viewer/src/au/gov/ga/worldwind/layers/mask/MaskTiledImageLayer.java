package au.gov.ga.worldwind.layers.mask;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;
import gov.nasa.worldwind.util.WWIO;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.application.GASandpit;
import au.gov.ga.worldwind.downloader.FileRetriever;

public class MaskTiledImageLayer extends BasicTiledImageLayer
{
	public MaskTiledImageLayer(Element domElement, AVList params)
	{
		super(domElement, setupParams(params));
	}

	protected static AVList setupParams(AVList params)
	{
		if (params == null)
			params = new AVListImpl();
		params.setValue(AVKey.TILE_URL_BUILDER, createURLBuilder(params));
		return params;
	}

	protected static TileUrlBuilder createURLBuilder(AVList params)
	{
		return new MaskUrlBuilder();
	}

	@Override
	protected void downloadTexture(TextureTile tile,
			BasicTiledImageLayer.DownloadPostProcessor postProcessor)
	{
		if (postProcessor != null)
		{
			super.downloadTexture(tile, postProcessor);
			return;
		}

		if (!this.isNetworkRetrievalEnabled())
		{
			this.getLevels().markResourceAbsent(tile);
			return;
		}

		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		URL textureUrl;
		URL maskUrl;
		try
		{
			textureUrl = tile.getResourceURL();
			maskUrl = tile.getResourceURL("mask");
		}
		catch (java.net.MalformedURLException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					Logging.getMessage("layers.TextureLayer.ExceptionCreatingTextureUrl", tile), e);
			return;
		}

		if (textureUrl == null || maskUrl == null)
			return;

		boolean textureFileProtocol = "file".equalsIgnoreCase(textureUrl.getProtocol());
		boolean maskFileProtocol = "file".equalsIgnoreCase(maskUrl.getProtocol());

		if ((!textureFileProtocol && WorldWind.getNetworkStatus().isHostUnavailable(textureUrl))
				|| (!maskFileProtocol && WorldWind.getNetworkStatus().isHostUnavailable(maskUrl)))
		{
			this.getLevels().markResourceAbsent(tile);
			return;
		}

		Retriever textureRetriever = null;
		Retriever maskRetriever = null;
		DownloadPostProcessor dpp = createPostProcessor(tile);

		if (textureFileProtocol)
			textureRetriever = new FileRetriever(textureUrl, dpp);
		else if ("http".equalsIgnoreCase(textureUrl.getProtocol()))
			textureRetriever = new HTTPRetriever(textureUrl, dpp);

		if (maskFileProtocol)
			maskRetriever = new FileRetriever(maskUrl, dpp);
		else if ("http".equalsIgnoreCase(maskUrl.getProtocol()))
			maskRetriever = new HTTPRetriever(maskUrl, dpp);

		if (textureRetriever == null || maskRetriever == null)
		{
			Logging.logger().severe(
					Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", textureUrl
							.toString()));
			return;
		}

		dpp.setRetrievers(textureRetriever, maskRetriever);



		// Apply any overridden timeouts.
		Integer cto = AVListImpl.getIntegerValue(this, AVKey.URL_CONNECT_TIMEOUT);
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
		Integer srl = AVListImpl.getIntegerValue(this, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (srl != null && srl > 0)
		{
			textureRetriever.setStaleRequestLimit(srl);
			maskRetriever.setStaleRequestLimit(srl);
		}

		WorldWind.getRetrievalService().runRetriever(textureRetriever,
				textureFileProtocol ? tile.getPriority() - 1e100 : tile.getPriority());
		WorldWind.getRetrievalService().runRetriever(maskRetriever,
				maskFileProtocol ? tile.getPriority() - 1e100 : tile.getPriority());
	}
	
	protected DownloadPostProcessor createPostProcessor(TextureTile tile)
	{
		return new DownloadPostProcessor(tile, this);
	}

	protected static class DownloadPostProcessor extends BasicTiledImageLayer.DownloadPostProcessor
	{
		protected final MaskTiledImageLayer layer;

		protected Retriever textureRetriever;
		protected Retriever maskRetriever;

		protected BufferedImage texture;
		protected BufferedImage mask;

		public DownloadPostProcessor(TextureTile tile, MaskTiledImageLayer layer)
		{
			this(tile, layer, null);
		}

		public DownloadPostProcessor(TextureTile tile, MaskTiledImageLayer layer,
				FileStore fileStore)
		{
			super(tile, layer, fileStore);
			this.layer = layer;
		}

		public void setRetrievers(Retriever textureRetriever, Retriever maskRetriever)
		{
			this.textureRetriever = textureRetriever;
			this.maskRetriever = maskRetriever;
		}

		@Override
		public synchronized ByteBuffer run(Retriever retriever)
		{
			//synchronized so that run's from texture and mask retrievers don't run at the same time
			return super.run(retriever);
		}

		@Override
		protected ByteBuffer handleSuccessfulRetrieval()
		{
			Retriever retriever = getRetriever();
			if (retriever != textureRetriever && retriever != maskRetriever)
			{
				finish();
				throw new IllegalArgumentException();
			}

			ByteBuffer data = retriever.getBuffer();
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

		@Override
		protected boolean validateResponseCode()
		{
			if (getRetriever() instanceof FileRetriever)
				return true;
			return super.validateResponseCode();
		}

		private void finish()
		{
			if (tryFinish())
			{
				// We've successfully cached data. Check if there's a configuration file for this layer, create one
				// if there's not.
				this.layer.writeConfigurationFile(this.getFileStore());

				// Fire a property change to denote that the layer's backing data has changed.
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

			final File outFile = getFileStore().newFile(this.tile.getPath());
			if (outFile == null)
				return false;

			if (outFile.exists() && !overwriteExistingFile())
				return true;

			Graphics2D g2d = mask.createGraphics();
			g2d.setComposite(AlphaComposite.SrcIn);
			g2d.drawImage(texture, 0, 0, null);
			g2d.dispose();

			BufferedImage image = transformPixels(mask);
			try
			{
				String ext = outFile.getName().substring(outFile.getName().lastIndexOf('.') + 1);
				if (ext.toLowerCase().equals("dds"))
					saveDDS(image, outFile);
				else
					saveImage(image, ext, outFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}

			return true;
		}

		protected BufferedImage transformPixels(BufferedImage image)
		{
			return image;
		}

		private void saveDDS(BufferedImage image, File file) throws IOException
		{
			ByteBuffer buffer = DDSCompressor.compressImage(image);
			synchronized (this.getFileLock())
			{
				WWIO.saveBuffer(buffer, file);
			}
		}

		private void saveImage(BufferedImage image, String format, File file) throws IOException
		{
			synchronized (this.getFileLock()) // sychronized with read of file in RequestTask.run()
			{
				ImageIO.write(image, format, file);
			}
		}
	}

	protected static class MaskUrlBuilder implements TileUrlBuilder
	{
		public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
		{
			String service = tile.getLevel().getService();
			if (service == null || service.length() < 1)
				return null;

			boolean mask = "mask".equalsIgnoreCase(imageFormat);

			service = GASandpit.replace(service);

			StringBuffer sb = new StringBuffer(service);
			if (sb.lastIndexOf("?") < 0)
				sb.append("?");
			else
				sb.append("&");

			sb.append("T=");
			sb.append(tile.getLevel().getDataset());
			sb.append("&L=");
			sb.append(tile.getLevel().getLevelName());
			sb.append("&X=");
			sb.append(tile.getColumn());
			sb.append("&Y=");
			sb.append(tile.getRow());
			if (mask)
				sb.append("&mask");

			return new URL(sb.toString());
		}
	}
}
