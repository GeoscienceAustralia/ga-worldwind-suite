package layers.metacarta;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;

public class MetacartaCountryBoundariesLayer extends BasicTiledImageLayer
{
	public MetacartaCountryBoundariesLayer()
	{
		super(MetacartaLayerUtil.makeLevels("Earth/Metacarta Country Boundaries",
				"country_02"));
	}

	@Override
	public String toString()
	{
		return "Metacarta country boundaries";
	}
}
