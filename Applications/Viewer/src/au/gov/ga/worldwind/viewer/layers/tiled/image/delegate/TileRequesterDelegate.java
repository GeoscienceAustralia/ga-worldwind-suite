package au.gov.ga.worldwind.viewer.layers.tiled.image.delegate;

import gov.nasa.worldwind.layers.TextureTile;

public interface TileRequesterDelegate extends Delegate
{
	void forceTextureLoad(TextureTile tile, DelegatorTiledImageLayer layer);
	Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer);
}
