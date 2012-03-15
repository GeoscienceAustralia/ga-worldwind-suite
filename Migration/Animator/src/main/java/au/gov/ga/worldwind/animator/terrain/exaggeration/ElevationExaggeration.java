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

/**
 * An interface for classes that can provide configurable vertical exaggeration information for elevation data.
 * <p/>
 * The elevation boundary is immutable, while the exaggeration can be changed once an instance is created.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ElevationExaggeration
{

	/**
	 * Set the exaggeration this instance applied to elevation data
	 */
	void setExaggeration(double exaggeration);
	
	/**
	 * @return The exaggeration this instance is applying to elevation data
	 */
	double getExaggeration();

	/**
	 * @return the lower bound for the set of elevations that this exaggeration should be applied to.
	 */
	double getElevationBoundary();

	/**
	 * Add the provided change listener to this exaggeration's list of listeners
	 */
	void addChangeListener(ChangeListener listener);

	/**
	 * Remove the provided change listener from this exaggeration's list of listeners
	 */
	void removeChangeListener(ChangeListener listener);
	
	/**
	 * An interface for objects that want to know when an elevation exaggeration has changed
	 */
	public interface ChangeListener
	{
		void exaggerationChanged(ElevationExaggeration exaggeration);
	}
}
