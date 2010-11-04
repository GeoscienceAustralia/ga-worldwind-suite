package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;

import java.net.URL;

import org.w3c.dom.Element;

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
		return Util.getLocalTileURL(tile, layer.context, layer.getDefaultImageFormat(), "jpg");
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
	public Delegate fromDefinition(String definition, Element layerElement, AVList params)
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
