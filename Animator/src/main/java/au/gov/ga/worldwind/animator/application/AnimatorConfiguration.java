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
package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.swing.UIManager;

import au.gov.ga.worldwind.animator.application.input.AnimatorInputHandler;
import au.gov.ga.worldwind.animator.layers.AnimationLayerFactory;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateRetrievalService;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateTaskService;
import au.gov.ga.worldwind.animator.terrain.AnimatorElevationModelFactory;
import au.gov.ga.worldwind.animator.terrain.ImmediateRectangularTessellator;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.view.AnimatorView;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.view.stereo.StereoViewDelegate;
import au.gov.ga.worldwind.common.view.target.TargetOrbitViewInputHandler;

/**
 * Class that holds and initialises the configuration details for the Animator
 * application
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimatorConfiguration
{
	private static final GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));

	public static final void initialiseConfiguration()
	{
		initialiseGLCapabilities();
		initialisePlatformDependentConfiguration();
		initialiseLAF();
		initialiseWorldWindConfiguration();
		initialiseMessageSource();
	}

	private static void initialiseGLCapabilities()
	{
		caps.setAlphaBits(8);
		caps.setRedBits(8);
		caps.setGreenBits(8);
		caps.setBlueBits(8);
		caps.setDepthBits(24);
		caps.setDoubleBuffered(true);
		caps.setNumSamples(4);
	}

	private static void initialisePlatformDependentConfiguration()
	{
		if (Configuration.isMacOS())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Application");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			System.setProperty("apple.awt.brushMetalLook", "true");
		}
	}

	private static void initialiseLAF()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			ExceptionLogger.logException(e);
		}
	}

	private static void initialiseWorldWindConfiguration()
	{
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, AnimatorView.class.getName());
		Configuration.setValue(AVKey.VIEW_INPUT_HANDLER_CLASS_NAME, TargetOrbitViewInputHandler.class.getName());
		Configuration.setValue(AVKeyMore.DELEGATE_VIEW_DELEGATE_CLASS_NAME, StereoViewDelegate.class.getName());
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, AnimatorSceneController.class.getName());
		Configuration.setValue(AVKey.TASK_SERVICE_CLASS_NAME, ImmediateTaskService.class.getName());
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, ImmediateRetrievalService.class.getName());
		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, ImmediateRectangularTessellator.class.getName());
		Configuration.setValue(AVKey.AIRSPACE_GEOMETRY_CACHE_SIZE, 16777216L * 8); // 128 mb
		Configuration.setValue(AVKey.ELEVATION_TILE_CACHE_SIZE, 20000000);
		Configuration.setValue(AVKey.TEXTURE_IMAGE_CACHE_SIZE, 20000000);
		Configuration.setValue(AVKey.TEXTURE_CACHE_SIZE, 1000000000);
		Configuration.setValue(AVKey.SECTOR_GEOMETRY_CACHE_SIZE, 20000000);
		Configuration.setValue(AVKey.LAYER_FACTORY, AnimationLayerFactory.class.getName());
		Configuration.setValue(AVKey.ELEVATION_MODEL_FACTORY, AnimatorElevationModelFactory.class.getName());
		Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, AnimatorInputHandler.class.getName());
	}

	private static void initialiseMessageSource()
	{
		MessageSourceAccessor.addBundle("messages.animatorMessages");
	}

	public static GLCapabilities getGLCapabilities()
	{
		return caps;
	}
}
