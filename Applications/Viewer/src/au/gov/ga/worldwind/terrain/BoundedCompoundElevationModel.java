package au.gov.ga.worldwind.terrain;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import au.gov.ga.worldwind.util.Bounded;

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
