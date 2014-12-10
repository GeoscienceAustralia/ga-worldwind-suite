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
package au.gov.ga.worldwind.animator.application.render;

import gov.nasa.worldwind.WorldWindow;

import java.io.File;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.animator.animation.camera.StereoCamera;
import au.gov.ga.worldwind.animator.application.Animator;
import au.gov.ga.worldwind.common.view.delegate.IDelegateView;
import au.gov.ga.worldwind.common.view.stereo.IStereoViewDelegate;
import au.gov.ga.worldwind.common.view.stereo.IStereoViewDelegate.Eye;

/**
 * An extension of the basic {@link OffscreenRenderer} that supports a stereo view,
 * rendering two file sequences when stereo is enabled, one for the left and right eye.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StereoOffscreenRenderer extends OffscreenRenderer
{
	public StereoOffscreenRenderer(WorldWindow wwd, Animator targetApplication)
	{
		super(wwd, targetApplication);
	}

	@Override
	protected void renderFrame(int frame, Animation animation, RenderParameters renderParams)
	{
		//if the view is not a stereo view, then just render with the super method
		boolean stereo = wwd.getView() instanceof IDelegateView &&
				((IDelegateView) wwd.getView()).getDelegate() instanceof IStereoViewDelegate &&
				animation.getCamera() instanceof StereoCamera;
		if (!stereo)
		{
			super.renderFrame(frame, animation, renderParams);
			return;
		}

		IDelegateView view = (IDelegateView) wwd.getView();
		IStereoViewDelegate delegate = (IStereoViewDelegate) view.getDelegate();
		delegate.setup(true, Eye.LEFT);

		File targetFile = AnimationImageSequenceNameFactory.createStereoImageSequenceFile(animation, frame, renderParams.getFrameName(), renderParams.getRenderDirectory(), Eye.LEFT);
		doRender(frame, targetFile, animation, renderParams);

		delegate.setup(true, Eye.RIGHT);

		targetFile = AnimationImageSequenceNameFactory.createStereoImageSequenceFile(animation, frame, renderParams.getFrameName(), renderParams.getRenderDirectory(), Eye.RIGHT);
		doRender(frame, targetFile, animation, renderParams);

		delegate.setup(false, Eye.LEFT);
	}

}
