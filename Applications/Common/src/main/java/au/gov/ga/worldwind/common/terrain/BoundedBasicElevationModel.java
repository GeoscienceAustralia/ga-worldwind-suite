package au.gov.ga.worldwind.common.terrain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import au.gov.ga.worldwind.common.layers.Bounded;

/**
 * Extension to {@link BasicElevationModel} that implements the {@link Bounded}
 * interface.
 * 
 * @author Michael de Hoog
 */
public class BoundedBasicElevationModel extends BasicElevationModel implements Bounded
{
	public BoundedBasicElevationModel(AVList params)
	{
		super(params);
	}

	@Override
	public Sector getSector()
	{
		return getLevels().getSector();
	}
}
