package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.util.Collection;

public class ExtendedCompoundElevationModel extends CompoundElevationModel
{
	public void addAll(Collection<? extends ElevationModel> c)
	{
		elevationModels.addAll(c);
	}

	public void removeAll(Collection<?> c)
	{
		elevationModels.removeAll(c);
	}

	public void clear()
	{
		elevationModels.clear();
	}
}
