package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.util.Collection;

public class ExtendedCompoundElevationModel extends CompoundElevationModel
{
	public void clear()
	{
		elevationModels.clear();
	}

	public void addAll(Collection<? extends ElevationModel> c)
	{
		elevationModels.addAll(c);
	}
}
