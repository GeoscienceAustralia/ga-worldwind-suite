package au.gov.ga.worldwind.common.terrain;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import au.gov.ga.worldwind.common.layers.Bounded;

/**
 * Extension to {@link CompoundElevationModel} that implements the
 * {@link Bounded} interface.
 * 
 * @author Michael de Hoog
 */
public class BoundedCompoundElevationModel extends CompoundElevationModel implements Bounded
{
	@Override
	public Sector getSector()
	{
		Sector sector = null;
		for (ElevationModel model : getElevationModels())
		{
			if (model instanceof Bounded)
			{
				Bounded b = (Bounded) model;
				if (sector == null)
					sector = b.getSector();
				else
					sector = sector.union(b.getSector());
			}
		}
		return sector;
	}
}
