package au.gov.ga.worldwind.viewer.terrain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.terrain.WMSBasicElevationModel;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.viewer.util.Bounded;

public class BoundedWMSBasicElevationModel extends WMSBasicElevationModel implements Bounded
{
	public BoundedWMSBasicElevationModel(Element domElement, AVList params)
	{
		super(domElement, params);
	}

	@Override
	public Sector getSector()
	{
		return getLevels().getSector();
	}
}
