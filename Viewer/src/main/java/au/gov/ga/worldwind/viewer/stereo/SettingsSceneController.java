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
package au.gov.ga.worldwind.viewer.stereo;

import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.DrawContext;

import java.nio.ByteBuffer;

import javax.media.opengl.GL2;

import au.gov.ga.worldwind.common.render.ExtendedSceneController;
import au.gov.ga.worldwind.common.view.delegate.IDelegateView;
import au.gov.ga.worldwind.common.view.delegate.IViewDelegate;
import au.gov.ga.worldwind.common.view.stereo.IStereoViewDelegate;
import au.gov.ga.worldwind.viewer.settings.Settings;

import com.jogamp.common.nio.Buffers;

/**
 * {@link SceneController} implementation that synchronizes with the application
 * settings (stereo, vertical exaggeration, field of view, etc) before painting.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SettingsSceneController extends ExtendedSceneController
{
	private double lastVerticalExaggeration = -1;
	private double lastFieldOfView = -1;
	private boolean stereoTested = false;

	@Override
	public void doRepaint(DrawContext dc)
	{
		Settings settings = Settings.get();
		double verticalExaggeration = settings.getVerticalExaggeration();
		if (lastVerticalExaggeration != verticalExaggeration)
		{
			setVerticalExaggeration(verticalExaggeration);
			lastVerticalExaggeration = verticalExaggeration;
		}

		double fieldOfView = settings.getFieldOfView();
		if (lastFieldOfView != fieldOfView)
		{
			dc.getView().setFieldOfView(Angle.fromDegrees(fieldOfView));
			lastFieldOfView = fieldOfView;
		}

		GL2 gl = dc.getGL().getGL2();
		if (!stereoTested)
		{
			ByteBuffer buffer16 = Buffers.newDirectByteBuffer(16);
			gl.glGetBooleanv(GL2.GL_STEREO, buffer16);
			Settings.setStereoSupported(buffer16.get() == 1);
			stereoTested = true;
		}

		View view = dc.getView();
		if (view instanceof IDelegateView)
		{
			IDelegateView delegateView = (IDelegateView) view;
			IViewDelegate delegate = delegateView.getDelegate();
			if (delegate instanceof IStereoViewDelegate)
			{
				((IStereoViewDelegate) delegate).setParameters(settings);
			}
		}

		super.doRepaint(dc);
	}
}
