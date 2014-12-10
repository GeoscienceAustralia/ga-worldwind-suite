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

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWindowGLAutoDrawable;
import gov.nasa.worldwind.WorldWindowImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Dimension;
import java.io.File;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationReader;
import au.gov.ga.worldwind.animator.application.render.AnimationRenderer;
import au.gov.ga.worldwind.animator.application.settings.Settings;
import au.gov.ga.worldwind.animator.view.AnimatorView;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Main class for the console version of the Animator.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Console
{
	public static void main(String[] args)
	{
		ConsoleParameters parameters = handleCommandLineArguments(args);
		if (parameters == null)
		{
			return;
		}

		try
		{
			AnimatorConfiguration.initialiseConfiguration();
			Settings.get();

			final WorldWindowGLAutoDrawable wwd = new WorldWindowGLAutoDrawable()
			{
				@Override
				public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h)
				{
				}

				@Override
				public void redraw()
				{
				}
			};
			wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
			Model model = new BasicModel();
			wwd.setModel(model);
			wwd.setView(new AnimatorView());
			((OrbitView) wwd.getView()).getOrbitViewLimits().setPitchLimits(Angle.ZERO, Angle.POS180);

			File input = new File(parameters.inputFile);
			File output = new File(parameters.outputFile);
			XmlAnimationReader animationReader = new XmlAnimationReader();
			Animation animation = animationReader.readAnimation(input, wwd);
			AnimationRenderer renderer = new ConsoleOffscreenRenderer(wwd);

			((AnimatorSceneController) wwd.getSceneController()).setAnimation(animation);
			model.getGlobe().setElevationModel(animation.getRootElevationModel());

			LayerList layers = model.getLayers();
			for (Layer layer : animation.getLayers())
			{
				layers.add(layer);
			}

			if (parameters.start == null)
			{
				parameters.start = 0;
			}
			if (parameters.end == null)
			{
				parameters.end = animation.getFrameCount() - 1;
			}

			RenderParameters renderParams = animation.getRenderParameters();
			renderParams.setDetailLevel(parameters.lod);
			renderParams.setRenderDestination(output);
			renderParams.setStartFrame(parameters.start);
			renderParams.setEndFrame(parameters.end);

			if (parameters.width != null || parameters.height != null)
			{
				double aspectRatio = renderParams.getImageAspectRatio();
				int width = parameters.width != null ? parameters.width :
						(int) Math.round(parameters.height * aspectRatio);
				int height = parameters.height != null ? parameters.height :
						(int) Math.round(parameters.width / aspectRatio);
				renderParams.setImageDimension(new Dimension(width, height));
				renderParams.setImageScalePercent(100);
			}

			GLDrawableFactory factory = GLDrawableFactory.getFactory(GLProfile.get(GLProfile.GL2));
			GLOffscreenAutoDrawable drawable =
					factory.createOffscreenAutoDrawable(null, AnimatorConfiguration.getGLCapabilities(), null,
							renderParams.getImageDimension().width, renderParams.getImageDimension().height);
			drawable.display();
			wwd.initDrawable(drawable);

			Thread thread = renderer.render(animation, renderParams);
			thread.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("Main thread exiting");
	}

	private static ConsoleParameters handleCommandLineArguments(String[] args)
	{
		ConsoleParameters parameters = new ConsoleParameters();
		JCommander jCommander = null;
		try
		{
			jCommander = new JCommander();
			jCommander.setProgramName("animator");
			jCommander.addObject(parameters);
			jCommander.parse(args);
		}
		catch (ParameterException e)
		{
			if (!parameters.showUsage)
			{
				System.err.println(e.getLocalizedMessage());
			}
			jCommander.usage();
			return null;
		}
		if (parameters.showUsage)
		{
			StringBuilder builder = new StringBuilder();
			jCommander.usage(builder);
			System.out.println(builder.toString());
			return null;
		}
		return parameters;
	}
}