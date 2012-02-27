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
package au.gov.ga.worldwind.animator.util;

/**
 * Further AV Keys specific to the animator application
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface AVKeyMore extends au.gov.ga.worldwind.common.util.AVKeyMore
{
	// Sky sphere params
	final static String SKYSPHERE_SLICES = "au.gov.ga.worldwind.animator.util.AVKeyMore.SkysphereSlices";
	final static String SKYSPHERE_SEGMENTS = "au.gov.ga.worldwind.animator.util.AVKeyMore.SkysphereSegments";
	final static String SKYSPHERE_ANGLE = "au.gov.ga.worldwind.animator.util.AVKeyMore.SkysphereAngle";
	
	// Fog params
	final static String FOG_NEAR_FACTOR = "au.gov.ga.worldwind.animator.util.AVKeyMore.FogNear";
	final static String FOG_FAR_FACTOR = "au.gov.ga.worldwind.animator.util.AVKeyMore.FogFar";
	final static String FOG_COLOR = "au.gov.ga.worldwind.animator.util.AVKeyMore.FogColor";
}
