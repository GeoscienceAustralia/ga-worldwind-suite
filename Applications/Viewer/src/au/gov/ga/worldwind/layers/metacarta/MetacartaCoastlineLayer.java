package au.gov.ga.worldwind.layers.metacarta;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;

public class MetacartaCoastlineLayer extends BasicTiledImageLayer
{
	public MetacartaCoastlineLayer()
	{
		super(MetacartaLayerUtil.makeLevels("Earth/Metacarta Coastline",
				"coastline_02"));
	}

	@Override
	public String toString()
	{
		return "Metacarta coastline";
	}
}
