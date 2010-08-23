package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.layers.TextureTile;

public interface TileRequesterDelegate
{
	void forceTextureLoad(TextureTile tile, DelegatorTiledImageLayer layer);
	Runnable createRequestTask(TextureTile tile, DelegatorTiledImageLayer layer);
}
