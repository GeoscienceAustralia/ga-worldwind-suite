package au.gov.ga.worldwind.viewer.terrain;

import org.w3c.dom.Element;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import au.gov.ga.worldwind.viewer.util.Bounded;

public class BoundedBasicElevationModel extends BasicElevationModel implements Bounded
{
	public BoundedBasicElevationModel(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	@Override
	public Sector getSector()
	{
		return getLevels().getSector();
	}
}
