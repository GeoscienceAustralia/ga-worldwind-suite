package layers.mask;

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
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RestorableSupport;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import layers.file.FileRetriever;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class MaskTiledImageLayer extends TiledImageLayer
{
	private final Object fileLock = new Object();
	private AVList creationParams;

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

	public MaskTiledImageLayer(AVList params)
	{
		this(new LevelSet(params));
		this.setValue(AVKey.CONSTRUCTION_PARAMETERS, params);
		this.creationParams = params.copy();
	}

	public MaskTiledImageLayer(String stateInXml)
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
		private final MaskTiledImageLayer layer;
		private final TextureTile tile;

		private RequestTask(TextureTile tile, MaskTiledImageLayer layer)
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

	private boolean isTextureExpired(TextureTile tile, java.net.URL textureURL)
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

		java.net.URL textureUrl;
		java.net.URL maskUrl;
		try
		{
			textureUrl = tile.getResourceURL();
			maskUrl = tile.getResourceURL("mask");
			if (textureUrl == null || maskUrl == null)
				return;

			if (WorldWind.getNetworkStatus().isHostUnavailable(textureUrl)
					|| WorldWind.getNetworkStatus().isHostUnavailable(maskUrl))
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

		Retriever textureRetriever = null;
		Retriever maskRetriever = null;
		DownloadPostProcessor dpp = new DownloadPostProcessor(tile, this);

		boolean textureFileProtocol = "file".equalsIgnoreCase(textureUrl
				.getProtocol());
		boolean maskFileProtocol = "file".equalsIgnoreCase(maskUrl
				.getProtocol());

		if (textureFileProtocol)
		{
			textureRetriever = new FileRetriever(textureUrl, dpp);
		}
		else if ("http".equalsIgnoreCase(textureUrl.getProtocol())
				|| "https".equalsIgnoreCase(textureUrl.getProtocol()))
		{
			textureRetriever = new HTTPRetriever(textureUrl, dpp);
		}
		else
		{
			Logging.logger().severe(
					Logging.getMessage(
							"layers.TextureLayer.UnknownRetrievalProtocol",
							textureUrl.toString()));
			return;
		}

		if (maskFileProtocol)
		{
			maskRetriever = new FileRetriever(maskUrl, dpp);
		}
		else if ("http".equalsIgnoreCase(maskUrl.getProtocol())
				|| "https".equalsIgnoreCase(maskUrl.getProtocol()))
		{
			maskRetriever = new HTTPRetriever(maskUrl, dpp);
		}
		else
		{
			Logging.logger().severe(
					Logging.getMessage(
							"layers.TextureLayer.UnknownRetrievalProtocol",
							maskUrl.toString()));
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

		WorldWind.getRetrievalService().runRetriever(
				textureRetriever,
				textureFileProtocol ? tile.getPriority() - 1e100 : tile
						.getPriority());
		WorldWind.getRetrievalService().runRetriever(
				maskRetriever,
				maskFileProtocol ? tile.getPriority() - 1e100 : tile
						.getPriority());
	}

	private void saveDDS(BufferedImage image, File file) throws IOException
	{
		ByteBuffer buffer = DDSCompressor.compressImage(image);
		synchronized (this.fileLock)
		{
			WWIO.saveBuffer(buffer, file);
		}
	}

	private void saveImage(BufferedImage image, String format, File file)
			throws IOException
	{
		synchronized (this.fileLock) // sychronized with read of file in RequestTask.run()
		{
			ImageIO.write(image, format, file);
		}
	}

	private static class DownloadPostProcessor implements
			RetrievalPostProcessor
	{
		// TODO: Rewrite this inner class, factoring out the generic parts.
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

			final File outFile = WorldWind.getDataFileStore().newFile(
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
				String ext = outFile.getName().substring(
						outFile.getName().lastIndexOf('.') + 1);
				if (ext.toLowerCase().equals("dds"))
					layer.saveDDS(mask, outFile);
				else
					layer.saveImage(mask, ext, outFile);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}

			return true;
		}
	}

	public static TileUrlBuilder createDefaultUrlBuilder()
	{
		return createDefaultUrlBuilder((File) null, null, null, null);
	}

	public static TileUrlBuilder createDefaultUrlBuilder(
			String localTextureDir, String localMaskDir,
			String textureExtension, String maskExtension)
	{
		return createDefaultUrlBuilder(new File(localTextureDir), new File(
				localMaskDir), textureExtension, maskExtension);
	}

	public static TileUrlBuilder createDefaultUrlBuilder(File localTextureDir,
			File localMaskDir, String textureExtension, String maskExtension)
	{
		return new MaskUrlBuilder(localTextureDir, localMaskDir,
				textureExtension, maskExtension);
	}

	private static class MaskUrlBuilder implements TileUrlBuilder
	{
		private File localTextureDir;
		private File localMaskDir;
		private String textureExtension;
		private String maskExtension;

		public MaskUrlBuilder(File localTextureDir, File localMaskDir,
				String textureExtension, String maskExtension)
		{
			this.localTextureDir = localTextureDir;
			this.localMaskDir = localMaskDir;
			this.textureExtension = (textureExtension.startsWith(".") ? ""
					: ".")
					+ textureExtension;
			this.maskExtension = (maskExtension.startsWith(".") ? "" : ".")
					+ maskExtension;
		}

		private String prependZeros(int number, int length)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(number);
			while (sb.length() < length)
			{
				sb.insert(0, "0");
			}
			return sb.toString();
		}

		private File buildFile(Tile tile, File dir, String extension)
		{
			if (dir == null || extension == null)
				return null;

			String level = String.valueOf(tile.getLevelNumber());
			String row = prependZeros(tile.getRow(), 4);
			String col = prependZeros(tile.getColumn(), 4);
			return new File(dir, level + "/" + row + "/" + row + "_" + col
					+ extension);
		}

		public URL getURL(Tile tile, String imageFormat)
				throws MalformedURLException
		{
			boolean mask = "mask".equalsIgnoreCase(imageFormat);
			File file = buildFile(tile, mask ? localMaskDir : localTextureDir,
					mask ? maskExtension : textureExtension);
			if (file != null && file.exists())
			{
				return file.toURI().toURL();
			}
			else
			{
				String service = tile.getLevel().getService();
				if (service == null || service.length() < 1)
					return null;

				StringBuffer sb = new StringBuffer(tile.getLevel().getService());
				if (sb.lastIndexOf("?") != sb.length() - 1)
					sb.append("?");
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

	protected void setBlendingFunction(DrawContext dc)
	{
		GL gl = dc.getGL();
		double alpha = this.getOpacity();
		gl.glColor4d(1.0, 1.0, 1.0, alpha);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}
}
