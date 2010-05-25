package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExtendedCompoundElevationModel extends CompoundElevationModel implements
		SectionList<ElevationModel>
{
	private Map<Object, ExtendedCompoundElevationModel> sectionMap =
			new HashMap<Object, ExtendedCompoundElevationModel>();

	@Override
	public void registerSectionObject(Object section)
	{
		ExtendedCompoundElevationModel em = new ExtendedCompoundElevationModel();
		sectionMap.put(section, em);
		addElevationModel(em);
	}

	@Override
	public void addAllFromSection(Object section, Collection<? extends ElevationModel> c)
	{
		ExtendedCompoundElevationModel model = this;
		if (sectionMap.containsKey(section))
			model = sectionMap.get(section);
		model.addAll(c);
	}

	@Override
	public void removeAllFromSection(Object section, Collection<? extends ElevationModel> c)
	{
		ExtendedCompoundElevationModel model = this;
		if (sectionMap.containsKey(section))
			model = sectionMap.get(section);
		model.removeAll(c);
	}

	protected void addAll(Collection<? extends ElevationModel> c)
	{
		elevationModels.addAll(c);
	}

	protected void removeAll(Collection<?> c)
	{
		elevationModels.removeAll(c);
	}
}
