/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.worldwind.animator.animation.sun;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.common.sun.SunPositionService;

/**
 * {@link Animatable} implementation for the position of the sun.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface SunPositionAnimatable extends Animatable
{
	/**
	 * @return Sun position type parameter (see
	 *         {@link SunPositionService.SunPositionType})
	 */
	Parameter getType();

	/**
	 * @return Sun latitude position parameter (if type ==
	 *         {@link SunPositionService.SunPositionType#Constant})
	 */
	Parameter getLatitude();

	/**
	 * @return Sun longitude position parameter (if type ==
	 *         {@link SunPositionService.SunPositionType#Constant})
	 */
	Parameter getLongitude();

	/**
	 * @return Sun time parameter (if type ==
	 *         {@link SunPositionService.SunPositionType#SpecificTime})
	 */
	Parameter getTime();
}
