package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.formats.dds.DXTCompressionAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWIO;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.tiled.image.GABasicTiledImageLayer;
import au.gov.ga.worldwind.util.AVKeyMore;
import au.gov.ga.worldwind.util.Bounded;

import com.sun.opengl.util.texture.TextureData;
import com.sun.opengl.util.texture.TextureIO;

public class DelegatorTiledImageLayer extends GABasicTiledImageLayer implements Bounded
{
	protected final Object fileLock;
	protected final URL context;
	protected final DelegateKit delegateKit;

	public DelegatorTiledImageLayer(Element domElement, AVList params, DelegateKit delegateKit)
	{
		super(domElement, params);

		this.delegateKit = delegateKit;

		Object o = params.getValue(AVKeyMore.CONTEXT_URL);
		if (o != null && o instanceof URL)
			context = (URL) o;
		else
			context = null;

		fileLock = FileLockSharer.getLock(getLevels().getFirstLevel().getCacheName());
	}

	@Override
	public Sector getSector()
	{
		return getLevels().getSector();
	}

	@Override
	protected LevelSet getLevels()
	{
		return super.getLevels();
	}

	@Override
	protected boolean isTextureFileExpired(TextureTile tile, URL textureURL, FileStore fileStore)
	{
		return super.isTextureFileExpired(tile, textureURL, fileStore);
	}

	private void addTileToCache(TextureTile tile)
	{
		TextureTile.getMemoryCache().add(tile.getTileKey(), tile);
	}

	public class DownloadPostProcessor extends BasicTiledImageLayer.DownloadPostProcessor
	{
		public DownloadPostProcessor(TextureTile tile, BasicTiledImageLayer layer)
		{
			super(tile, layer);
		}

		@Override
		protected Object getFileLock()
		{
			return fileLock;
		}
	}

	@Override
	protected void forceTextureLoad(TextureTile tile)
	{
		delegateKit.forceTextureLoad(tile, this);
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
		Vec4 referencePoint = this.getReferencePoint(dc);
		if (referencePoint != null)
			tile.setPriority(centroid.distanceTo3(referencePoint));

		Runnable task = delegateKit.createRequestTask(tile, this);
		this.getRequestQ().add(task);
	}

	//called by TileRequesterDelegate
	protected boolean loadTexture(TextureTile tile, URL textureURL)
	{
		TextureData textureData;

		synchronized (fileLock)
		{
			textureData = readTexture(textureURL);
		}

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !isRetainLevelZeroTiles())
			addTileToCache(tile);

		return true;
	}

	protected TextureData readTexture(URL url)
	{
		try
		{
			boolean isDDS = url.toString().toLowerCase().endsWith("dds");
			if (!isDDS)
			{
				BufferedImage image = delegateKit.readImage(url);
				if (image == null)
				{
					image = ImageIO.read(url);
				}

				image = delegateKit.transformImage(image);

				if (isCompressTextures())
				{
					DXTCompressionAttributes attributes =
							DDSCompressor.getDefaultCompressionAttributes();
					attributes.setBuildMipmaps(isUseMipMaps());

					ByteBuffer buffer;
					if (image != null)
					{
						buffer = new DDSCompressor().compressImage(image, attributes);
					}
					else
					{
						buffer = DDSCompressor.compressImageURL(url, attributes);
					}

					return TextureIO.newTextureData(WWIO.getInputStreamFromByteBuffer(buffer),
							isUseMipMaps(), null);
				}

				if (image != null)
				{
					return TextureIO.newTextureData(image, isUseMipMaps());
				}
			}

			return TextureIO.newTextureData(url, isUseMipMaps(), null);
		}
		catch (Exception e)
		{
			String msg =
					Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile",
							url);
			Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
			return null;
		}
	}

	/* **********************************************************************************************
	 * Below here is copied from BasicTiledImageLayer, with some modifications to use the delegates *
	 ********************************************************************************************** */

	private ArrayList<TextureTile> topLevels;

	@Override
	public ArrayList<TextureTile> getTopLevels()
	{
		if (this.topLevels == null)
			this.createTopLevelTiles();

		return topLevels;
	}

	private void createTopLevelTiles()
	{
		Sector sector = this.getLevels().getSector();

		Level level = this.getLevels().getFirstLevel();
		Angle dLat = level.getTileDelta().getLatitude();
		Angle dLon = level.getTileDelta().getLongitude();
		Angle latOrigin = this.getLevels().getTileOrigin().getLatitude();
		Angle lonOrigin = this.getLevels().getTileOrigin().getLongitude();

		// Determine the row and column offset from the common World Wind global tiling origin.
		int firstRow = Tile.computeRow(dLat, sector.getMinLatitude(), latOrigin);
		int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude(), lonOrigin);
		int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude(), latOrigin);
		int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude(), lonOrigin);

		int nLatTiles = lastRow - firstRow + 1;
		int nLonTiles = lastCol - firstCol + 1;

		this.topLevels = new ArrayList<TextureTile>(nLatTiles * nLonTiles);

		Angle p1 = Tile.computeRowLatitude(firstRow, dLat, latOrigin);
		for (int row = firstRow; row <= lastRow; row++)
		{
			Angle p2;
			p2 = p1.add(dLat);

			Angle t1 = Tile.computeColumnLongitude(firstCol, dLon, lonOrigin);
			for (int col = firstCol; col <= lastCol; col++)
			{
				Angle t2;
				t2 = t1.add(dLon);

				//MODIFIED
				this.topLevels.add(delegateKit.createTextureTile(new Sector(p1, p2, t1, t2), level,
						row, col));
				//MODIFIED
				t1 = t2;
			}
			p1 = p2;
		}
	}

	@Override
	public TextureTile[][] getTilesInSector(Sector sector, int levelNumber)
	{
		if (sector == null)
		{
			String msg = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Level targetLevel = this.getLevels().getLastLevel();
		if (levelNumber >= 0)
		{
			for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
			{
				if (this.getLevels().isLevelEmpty(i))
					continue;

				targetLevel = this.getLevels().getLevel(i);
				break;
			}
		}

		// Collect all the tiles intersecting the input sector.
		LatLon delta = targetLevel.getTileDelta();
		LatLon origin = this.getLevels().getTileOrigin();
		final int nwRow =
				Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
		final int nwCol =
				Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(),
						origin.getLongitude());
		final int seRow =
				Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
		final int seCol =
				Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(),
						origin.getLongitude());

		int numRows = nwRow - seRow + 1;
		int numCols = seCol - nwCol + 1;
		TextureTile[][] sectorTiles = new TextureTile[numRows][numCols];

		for (int row = nwRow; row >= seRow; row--)
		{
			for (int col = nwCol; col <= seCol; col++)
			{
				TileKey key =
						new TileKey(targetLevel.getLevelNumber(), row, col,
								targetLevel.getCacheName());
				Sector tileSector = this.getLevels().computeSectorForKey(key);
				//MODIFIED
				sectorTiles[nwRow - row][col - nwCol] =
						delegateKit.createTextureTile(tileSector, targetLevel, row, col); //new TextureTile(tileSector, targetLevel, row, col);
				//MODIFIED
			}
		}

		return sectorTiles;
	}

	@Override
	protected void downloadTexture(final TextureTile tile,
			BasicTiledImageLayer.DownloadPostProcessor postProcessor)
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
			Logging.logger().log(java.util.logging.Level.SEVERE,
					Logging.getMessage("layers.TextureLayer.ExceptionCreatingTextureUrl", tile), e);
			return;
		}

		Retriever retriever;

		if ("http".equalsIgnoreCase(url.getProtocol())
				|| "https".equalsIgnoreCase(url.getProtocol()))
		{
			if (postProcessor == null)
				postProcessor = new DownloadPostProcessor(tile, this);
			//MODIFIED
			//retriever = new HTTPRetriever(url, postProcessor);
			retriever = delegateKit.createRetriever(url, postProcessor);
			//MODIFIED
		}
		else
		{
			Logging.logger().severe(
					Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol",
							url.toString()));
			return;
		}

		// Apply any overridden timeouts.
		Integer cto = AVListImpl.getIntegerValue(this, AVKey.URL_CONNECT_TIMEOUT);
		if (cto != null && cto > 0)
			retriever.setConnectTimeout(cto);
		Integer cro = AVListImpl.getIntegerValue(this, AVKey.URL_READ_TIMEOUT);
		if (cro != null && cro > 0)
			retriever.setReadTimeout(cro);
		Integer srl = AVListImpl.getIntegerValue(this, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (srl != null && srl > 0)
			retriever.setStaleRequestLimit(srl);

		WorldWind.getRetrievalService().runRetriever(retriever, tile.getPriority());
	}

	@Override
	protected void writeConfigurationParams(FileStore fileStore, AVList params)
	{
		// Determine what the configuration file name should be based on the configuration parameters. Assume an XML
		// configuration document type, and append the XML file suffix.
		String fileName = DataConfigurationUtils.getDataConfigFilename(params, ".xml");
		if (fileName == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new WWRuntimeException(message);
		}

		// Check if this component needs to write a configuration file. This happens outside of the synchronized block
		// to improve multithreaded performance for the common case: the configuration file already exists, this just
		// need to check that it's there and return. If the file exists but is expired, do not remove it -  this
		// removes the file inside the synchronized block below.
		if (!this.needsConfigurationFile(fileStore, fileName, params, false))
			return;

		synchronized (this.fileLock)
		{
			// Check again if the component needs to write a configuration file, potentially removing any existing file
			// which has expired. This additional check is necessary because the file could have been created by
			// another thread while we were waiting for the lock.
			if (!this.needsConfigurationFile(fileStore, fileName, params, true))
				return;

			this.doWriteConfigurationParams(fileStore, fileName, params);
		}
	}
}
