package au.gov.ga.worldwind.layers;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.util.Bounded;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;

public class BoundedBasicTiledImageLayer extends BasicTiledImageLayer implements Bounded
{
	public BoundedBasicTiledImageLayer(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	@Override
	public Sector getSector()
	{
		return getLevels().getSector();
	}
}
