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
package au.gov.ga.worldwind.animator.application.effects;

import au.gov.ga.worldwind.animator.application.effects.depthoffield.DepthOfFieldEffect;
import au.gov.ga.worldwind.animator.application.effects.edge.EdgeDetectionEffect;

/**
 * Helper class that simply registers the known built-in effects with the
 * {@link EffectRegistry}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BuiltInEffects
{
	public static void registerBuiltInEffects()
	{
		EffectRegistry.instance.registerEffect(DepthOfFieldEffect.class);
		EffectRegistry.instance.registerEffect(EdgeDetectionEffect.class);
	}
}
