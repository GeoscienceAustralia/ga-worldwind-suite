package au.gov.ga.worldwind.common.terrain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.terrain.WMSBasicElevationModel;
import au.gov.ga.worldwind.common.layers.Bounded;

/**
 * Extension of {@link WMSBasicElevationModel} that implements the
 * {@link Bounded} interface.
 * 
 * @author Michael de Hoog
 */
public class BoundedWMSBasicElevationModel extends WMSBasicElevationModel implements Bounded
{
	public BoundedWMSBasicElevationModel(AVList params)
	{
		super(params);
	}

	@Override
	public Sector getSector()
	{
		return getLevels().getSector();
	}
}
