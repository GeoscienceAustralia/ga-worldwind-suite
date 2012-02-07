package au.gov.ga.worldwind.common.layers.delegate;

import com.sun.opengl.util.texture.TextureData;

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.util.TileKey;

/**
 * An individual tile used by an {@link IDelegatorLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDelegatorTile extends Cacheable
{
	/**
	 * @return Download priority for this tile.
	 */
	double getPriority(); //Tile

	/**
	 * @return Local tile cache path.
	 */
	String getPath(); //Tile

	/**
	 * @return Tile's level's service.
	 */
	String getService(); //Level

	/**
	 * @return Tile's level's dataset.
	 */
	String getDataset(); //Level

	/**
	 * @return This tile's level number.
	 */
	int getLevelNumber(); //Level

	/**
	 * @return This tile's row.
	 */
	int getRow(); //Level

	/**
	 * @return This tile's column.
	 */
	int getColumn(); //Level

	/**
	 * @return This tile's {@link TileKey}, transformed by the
	 *         {@link ITileFactoryDelegate}.
	 */
	TileKey getTransformedTileKey();

	/**
	 * Set the texture data for this tile.
	 */
	void setTextureData(TextureData textureData);
}
