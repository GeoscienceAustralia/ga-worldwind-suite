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
package au.gov.ga.worldwind.animator.terrain.exaggeration;

import gov.nasa.worldwind.render.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import au.gov.ga.worldwind.common.util.exaggeration.VerticalExaggerationService;

/**
 * An implementation of the {@link VerticalExaggerationService} that uses the {@link VerticalExaggerationElevationModel} to
 * calculate the exaggeration to be applied to a given elevation value
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimatorVerticalExaggerationServiceImpl implements VerticalExaggerationService
{

	private Map<Object, VerticalExaggerationSettings> marks = new WeakHashMap<Object, VerticalExaggerationSettings>();
	
	@Override
	public double applyVerticalExaggeration(DrawContext dc, double elevation)
	{
		return ((VerticalExaggerationElevationModel)dc.getGlobe().getElevationModel()).exaggerateElevation(elevation);
	}

	@Override
	public double unapplyVerticalExaggeration(DrawContext dc, double exaggeratedElevation)
	{
		// TODO: Implement unapply
		return exaggeratedElevation;
	}
	
	@Override
	public double getGlobalVerticalExaggeration(DrawContext dc)
	{
		return 1.0;
	}
	
	@Override
	public void markVerticalExaggeration(Object key, DrawContext dc)
	{
		marks.put(key, new VerticalExaggerationSettings(dc));
	}
	
	@Override
	public boolean isVerticalExaggerationChanged(Object key, DrawContext dc)
	{
		if (!marks.containsKey(key))
		{
			return true;
		}
		VerticalExaggerationSettings currentSettings = new VerticalExaggerationSettings(dc);
		VerticalExaggerationSettings oldSettings = marks.get(key);
		return !currentSettings.equals(oldSettings);
	}
	
	@Override
	public void clearMark(Object key)
	{
		marks.remove(key);
	}
	
	@Override
	public boolean checkAndMarkVerticalExaggeration(Object key, DrawContext dc)
	{
		boolean isChanged = isVerticalExaggerationChanged(key, dc);
		if (!isChanged)
		{
			return false;
		}
		markVerticalExaggeration(key, dc);
		return true;
	}

	private static class VerticalExaggerationSettings
	{
		private List<ExaggerationEntry> exaggerations = new ArrayList<ExaggerationEntry>();
		
		public VerticalExaggerationSettings(DrawContext dc)
		{
			for (ElevationExaggeration ee : ((VerticalExaggerationElevationModel)dc.getGlobe().getElevationModel()).getExaggerators())
			{
				exaggerations.add(new ExaggerationEntry(ee));
			}
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof VerticalExaggerationSettings))
			{
				return false;
			}
			if (obj == this)
			{
				return true;
			}
			VerticalExaggerationSettings other = (VerticalExaggerationSettings)obj;
			if (other.exaggerations.size() != this.exaggerations.size())
			{
				return false;
			}
			for (int i = 0; i < this.exaggerations.size(); i++)
			{
				if (!this.exaggerations.get(i).equals(other.exaggerations.get(i)))
				{
					return false;
				}
			}
			return true;
		}
	}
	
	private static class ExaggerationEntry
	{
		private double boundary;
		private double exaggeration;
		
		public ExaggerationEntry(ElevationExaggeration exaggerator)
		{
			this.boundary = exaggerator.getElevationBoundary();
			this.exaggeration = exaggerator.getExaggeration();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof ExaggerationEntry))
			{
				return false;
			}
			if (obj == this)
			{
				return true;
			}
			ExaggerationEntry other = (ExaggerationEntry)obj;
			return other.boundary == this.boundary && other.exaggeration == this.exaggeration;
		}
	}
	
}
