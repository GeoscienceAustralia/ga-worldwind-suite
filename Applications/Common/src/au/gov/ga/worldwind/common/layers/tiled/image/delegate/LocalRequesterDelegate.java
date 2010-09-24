package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.common.util.Util;

/**
 * Implementation of {@link TileRequesterDelegate} which provides loading from
 * tilesets stored in the local filesystem. This means that tiles are not
 * downloaded/cached, but are loaded directly from the tileset.
 * 
 * @author Michael de Hoog
 */
public class LocalRequesterDelegate implements TileRequesterDelegate
{
	private final static String DEFINITION_STRING = "LocalRequester";

	@Override
	public void forceTextureLoad(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		loadTexture(tile, layer);
	}

	@Override
	public Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		return new RequestTask(tile, layer);
	}

	@Override
	public URL getLocalTileURL(TextureTile tile, DelegatorTiledImageLayer layer,
			boolean searchClassPath)
	{
		return getTileURL(tile, layer);
	}

	/**
	 * Load the texture for a tile.
	 * 
	 * @param tile
	 *            Tile for which the texture should be loaded
	 * @param layer
	 *            Layer to call loadTexture() on
	 * @return true if the texture was loaded
	 */
	protected boolean loadTexture(TextureTile tile, DelegatorTiledImageLayer layer)
	{
		URL url = getLocalTileURL(tile, layer, false);
		if (url == null)
			return false;
		return layer.loadTexture(tile, url);
	}

	/**
	 * Return a URL which points to the tile's texture.
	 * 
	 * @param tile
	 *            Tile to get texture URL for
	 * @param layer
	 *            Tile's layer
	 * @return Tile's texture URL
	 */
	protected URL getTileURL(Tile tile, DelegatorTiledImageLayer layer)
	{
		String service = tile.getLevel().getService();
		String dataset = tile.getLevel().getDataset();

		if (dataset == null || dataset.length() <= 0)
			dataset = service;
		else if (service != null && service.length() > 0)
			dataset = service + "/" + dataset;

		if (dataset == null)
			dataset = "";

		boolean isZip = false;
		File parent = Util.getPathWithinContext(dataset, layer.context);
		if (parent == null)
		{
			//if the directory didn't exist, try a zip file
			isZip = true;
			parent = Util.getPathWithinContext(dataset + ".zip", layer.context);
		}

		if (parent == null)
			return null;

		//default to JPG
		String ext = "jpg";
		String format = layer.getDefaultImageFormat();
		if (format != null)
		{
			format = format.toLowerCase();
			if (format.contains("jpg") || format.contains("jpeg"))
				ext = "jpg";
			else if (format.contains("png"))
				ext = "png";
			else if (format.contains("zip"))
				ext = "zip";
			else if (format.contains("dds"))
				ext = "dds";
			else if (format.contains("bmp"))
				ext = "bmp";
			else if (format.contains("gif"))
				ext = "gif";
		}

		String filename =
				tile.getLevelNumber() + File.separator + Util.paddedInt(tile.getRow(), 4)
						+ File.separator + Util.paddedInt(tile.getRow(), 4) + "_"
						+ Util.paddedInt(tile.getColumn(), 4) + "." + ext;

		try
		{
			if (parent.isFile() && isZip)
			{
				//zip file; return URL using 'jar' protocol
				return Util.zipEntryUrl(parent, filename);
			}
			else if (parent.isDirectory())
			{
				//return standard 'file' protocol URL
				File file = new File(parent, filename);
				if (file.exists())
				{
					return file.toURI().toURL();
				}
			}
		}
		catch (MalformedURLException e)
		{
			String msg = "Converting tile file to URL failed";
			Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
		}
		return null;
	}

	/**
	 * Task which simply calls loadTexture(), and then (un)marks the tile
	 * absent. Instances of this class are returned by the createRequestTask()
	 * function.
	 * 
	 * @author Michael de Hoog
	 */
	protected class RequestTask implements Runnable, Comparable<RequestTask>
	{
		private final DelegatorTiledImageLayer layer;
		private final TextureTile tile;

		private RequestTask(TextureTile tile, DelegatorTiledImageLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		@Override
		public void run()
		{
			if (loadTexture(tile, layer))
			{
				layer.getLevels().unmarkResourceAbsent(tile);
				layer.firePropertyChange(AVKey.LAYER, null, this);
			}
			else
			{
				layer.getLevels().markResourceAbsent(tile);
			}
		}

		@Override
		public int compareTo(RequestTask that)
		{
			if (that == null)
			{
				String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}
			return this.tile.getPriority() == that.tile.getPriority() ? 0
					: this.tile.getPriority() < that.tile.getPriority() ? -1 : 1;
		}

		@Override
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

		@Override
		public int hashCode()
		{
			return (tile != null ? tile.hashCode() : 0);
		}

		@Override
		public String toString()
		{
			return this.tile.toString();
		}
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new LocalRequesterDelegate();
		return null;
	}

	@Override
	public String toDefinition()
	{
		return DEFINITION_STRING;
	}
}
