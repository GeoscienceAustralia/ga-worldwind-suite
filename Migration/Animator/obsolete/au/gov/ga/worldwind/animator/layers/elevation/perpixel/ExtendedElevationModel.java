package au.gov.ga.worldwind.animator.layers.elevation.perpixel;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.TileKey;

public interface ExtendedElevationModel extends ElevationModel
{
	public BufferWrapper getElevationsFromMemory(TileKey tileKey);
	public void requestTile(TileKey key);
}
