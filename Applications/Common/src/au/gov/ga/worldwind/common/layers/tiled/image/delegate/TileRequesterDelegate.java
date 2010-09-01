package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.layers.TextureTile;

import java.net.URL;

/**
 * Instances of {@link TileRequesterDelegate} are used to customise the
 * requesting of tile textures.
 * 
 * @author Michael de Hoog
 */
public interface TileRequesterDelegate extends Delegate
{
	/**
	 * Load a texture immediately if cached.
	 * 
	 * @param tile
	 *            Tile to load texture for
	 * @param layer
	 *            Tile's layer
	 */
	void forceTextureLoad(TextureTile tile, DelegatorTiledImageLayer layer);

	/**
	 * Create a new Runnable which will make a request for tile's texture.
	 * 
	 * @param tile
	 *            Tile to request texture for
	 * @param layer
	 *            Tile's layer
	 * @return Runnable that will make the tile request
	 */
	Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer);

	/**
	 * Return the tile's file URL within the local filesystem (ie the path where
	 * the tile is stored in the cache; or - in the case of a local tileset -
	 * the file URL where the tile is stored).
	 * 
	 * @param tile
	 *            Tile to get file URL for
	 * @param layer
	 *            Tile's layer
	 * @param searchClassPath
	 *            Should the classpath be searched when finding the tile's file?
	 * @return tile's local file URL
	 */
	URL getLocalTileURL(TextureTile tile, DelegatorTiledImageLayer layer, boolean searchClassPath);
}
