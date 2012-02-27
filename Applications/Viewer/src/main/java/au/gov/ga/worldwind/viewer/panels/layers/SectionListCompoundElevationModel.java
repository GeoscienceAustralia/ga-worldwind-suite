/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ElevationModel} which implements the {@link SectionList} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SectionListCompoundElevationModel extends CompoundElevationModel implements SectionList<ElevationModel>
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
