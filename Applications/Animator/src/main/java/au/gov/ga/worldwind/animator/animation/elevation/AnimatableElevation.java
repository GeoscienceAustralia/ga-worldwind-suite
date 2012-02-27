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
package au.gov.ga.worldwind.animator.animation.elevation;

import java.util.List;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.terrain.exaggeration.VerticalExaggerationElevationModel;

/**
 * An interface for an {@link Animatable} object that allows control over the elevation model of an animation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface AnimatableElevation extends Animatable
{

	/**
	 * @return The elevation model this instance is controlling
	 */
	VerticalExaggerationElevationModel getRootElevationModel();
	
	/**
	 * @return The list of model identifiers that identify the elevation models included in this {@link AnimatableElevation} 
	 */
	List<ElevationModelIdentifier> getElevationModelIdentifiers();
	
	/**
	 * @return Whether this {@link AnimatableElevation} has the elevation model identified by the provided {@link ElevationModelIdentifier} 
	 */
	boolean hasElevationModel(ElevationModelIdentifier modelIdentifier);

	/**
	 * Add the elevation model identified by the provided identifier to this {@link AnimatableElevation}, if it isn't already
	 */
	void addElevationModel(ElevationModelIdentifier modelIdentifier);
	
	/**
	 * Remove the elevation model identified by the provided from this {@link AnimatableElevation}
	 */
	void removeElevationModel(ElevationModelIdentifier modelIdentifier);
	
	/**
	 * Add the provided elevation exaggerator to this {@link AnimatableElevation}
	 */
	void addElevationExaggerator(ElevationExaggeration exaggerator);
	
	/**
	 * Remove the provided elevation exaggerator from this {@link AnimatableElevation}
	 */
	void removeElevationExaggerator(ElevationExaggeration exaggerator);
}
