package au.gov.ga.worldwind.layers.metacarta;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;

public class MetacartaStateBoundariesLayer extends BasicTiledImageLayer
{
	public MetacartaStateBoundariesLayer()
	{
		super(MetacartaLayerUtil.makeLevels("Earth/Metacarta State Boundaries",
				"stateboundary"));
	}

	@Override
	public String toString()
	{
		return "Metacarta state boundaries";
	}
}
