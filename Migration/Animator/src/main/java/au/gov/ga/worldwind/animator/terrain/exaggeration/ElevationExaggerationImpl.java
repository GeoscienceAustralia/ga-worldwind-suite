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

import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of the {@link ElevationExaggeration} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ElevationExaggerationImpl implements ElevationExaggeration
{
	private double exaggeration = 1.0;
	private double boundary;
	
	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	
	public ElevationExaggerationImpl(double exaggeration, double boundary)
	{
		this.exaggeration = Math.max(0, exaggeration);
		this.boundary = boundary;
	}

	@Override
	public void setExaggeration(double exaggeration)
	{
		double newExaggeration = Math.max(0, exaggeration);
		boolean changed = newExaggeration != this.exaggeration;
		
		this.exaggeration = newExaggeration;
		
		if (changed)
		{
			fireChangeEvent();
		}
	}

	@Override
	public double getExaggeration()
	{
		return exaggeration;
	}

	@Override
	public double getElevationBoundary()
	{
		return boundary;
	}

	@Override
	public void addChangeListener(ChangeListener listener)
	{
		if (listener == null || changeListeners.contains(listener))
		{
			return;
		}
		changeListeners.add(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener)
	{
		changeListeners.remove(listener);
	}

	/**
	 * Notify listeners that a change has occurred
	 */
	private void fireChangeEvent()
	{
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			changeListeners.get(i).exaggerationChanged(this);
		}
		
	}
	
}
