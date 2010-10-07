package au.gov.ga.worldwind.animator.application;

import java.util.logging.Level;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GLCapabilities;
import javax.swing.UIManager;

import au.gov.ga.worldwind.animator.layers.immediate.ImmediateRetrievalService;
import au.gov.ga.worldwind.animator.layers.immediate.ImmediateTaskService;
import au.gov.ga.worldwind.animator.terrain.ImmediateRectangularTessellator;
import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.animator.view.orbit.BasicOrbitView;
import au.gov.ga.worldwind.common.layers.LayerFactory;
import au.gov.ga.worldwind.common.terrain.ElevationModelFactory;
import au.gov.ga.worldwind.common.util.GASandpit;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

/**
 * Class that holds and initialises the configuration details for the Animator application
 */
public class AnimatorConfiguration
{
	private static final GLCapabilities caps = new GLCapabilities();
	
	public static final void initialiseConfiguration()
	{
		initialiseLogging();
		initialiseGLCapabilities();
		initialisePlatformDependentConfiguration();
		initialiseLAF();
		initialiseProxySettings();
		initialiseWorldWindConfiguration();
		initialiseSandpitMode();
		initialiseMessageSource();
	}

	private static void initialiseLogging()
	{
		Logging.logger().setLevel(Level.FINER);
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
	
	private static void initialiseProxySettings()
	{
		System.setProperty("http.proxyHost", "proxy.agso.gov.au");
		System.setProperty("http.proxyPort", "8080");
		System.setProperty("http.nonProxyHosts", "localhost");
	}
	
	private static void initialiseWorldWindConfiguration()
	{
		Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, "");
		Configuration.setValue(AVKey.VIEW_CLASS_NAME, BasicOrbitView.class.getName());
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, AnimatorSceneController.class.getName());
		Configuration.setValue(AVKey.TASK_SERVICE_CLASS_NAME, ImmediateTaskService.class.getName());
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, ImmediateRetrievalService.class.getName());
		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, ImmediateRectangularTessellator.class.getName());
		Configuration.setValue(AVKey.AIRSPACE_GEOMETRY_CACHE_SIZE, 16777216L * 8); // 128 mb
		Configuration.setValue(AVKey.LAYER_FACTORY, LayerFactory.class.getName());
		Configuration.setValue(AVKey.ELEVATION_MODEL_FACTORY, ElevationModelFactory.class.getName());
	}
	
	private static void initialiseSandpitMode()
	{
		GASandpit.setSandpitMode(true);
	}
	
	private static void initialiseMessageSource()
	{
		MessageSourceAccessor.addBundle("au.gov.ga.worldwind.animator.data.messages.animatorMessages");
	}
	
	public static GLCapabilities getGLCapabilities()
	{
		return caps;
	}
}
