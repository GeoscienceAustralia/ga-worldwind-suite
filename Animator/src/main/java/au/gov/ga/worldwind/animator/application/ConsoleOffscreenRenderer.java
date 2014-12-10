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
package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.WorldWindow;
import au.gov.ga.worldwind.animator.application.render.StereoOffscreenRenderer;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateMode;
import au.gov.ga.worldwind.animator.terrain.DetailedElevationModel;
import au.gov.ga.worldwind.animator.view.AnimatorView;

/**
 * Renderer implementation used by the {@link Console} version of the animator.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ConsoleOffscreenRenderer extends StereoOffscreenRenderer
{
	public ConsoleOffscreenRenderer(WorldWindow wwd)
	{
		super(wwd, null);
	}
	
	@Override
	protected void validateNotNullAnimatorApplication(Animator targetApplication)
	{
		//console version doesn't have an associated application
	}

	@Override
	protected void updateSlider(int frame)
	{
		System.out.println("Rendering frame " + frame);
	}

	@Override
	protected void setupForRendering(double detailHint)
	{
		ImmediateMode.setImmediate(true);

		AnimatorView view = (AnimatorView) wwd.getView();
		view.setDetectCollisions(false);
		view.setTargetMode(true);

		((DetailedElevationModel) wwd.getModel().getGlobe().getElevationModel()).setDetailHint(detailHint);
	}

	@Override
	protected void resetViewingParameters()
	{
	}
}
