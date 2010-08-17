package au.gov.ga.worldwind.layers;

import au.gov.ga.worldwind.util.Bounded;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;

public abstract class BoundedTiledImageLayer extends TiledImageLayer implements Bounded
{
	public BoundedTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	@Override
	public Sector getSector()
	{
		return getLevels().getSector();
	}
}
