package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.layers.TextureTile;

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
}
