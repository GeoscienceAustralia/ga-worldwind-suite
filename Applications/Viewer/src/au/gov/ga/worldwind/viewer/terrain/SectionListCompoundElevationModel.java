package au.gov.ga.worldwind.viewer.terrain;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.viewer.panels.layers.SectionList;

public class SectionListCompoundElevationModel extends CompoundElevationModel implements
		SectionList<ElevationModel>
{
	private Map<Object, SectionListCompoundElevationModel> sectionMap =
			new HashMap<Object, SectionListCompoundElevationModel>();

	@Override
	public void registerSectionObject(Object section)
	{
		SectionListCompoundElevationModel em = new SectionListCompoundElevationModel();
		sectionMap.put(section, em);
		addElevationModel(em);
	}

	@Override
	public void addAllFromSection(Object section, Collection<? extends ElevationModel> c)
	{
		SectionListCompoundElevationModel model = this;
		if (sectionMap.containsKey(section))
			model = sectionMap.get(section);
		model.addAll(c);
	}

	@Override
	public void removeAllFromSection(Object section, Collection<? extends ElevationModel> c)
	{
		SectionListCompoundElevationModel model = this;
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
