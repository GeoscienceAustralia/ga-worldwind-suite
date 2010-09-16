package au.gov.ga.worldwind.animator.animation.io;

import static org.junit.Assert.*;
import gov.nasa.worldwind.WorldWindowImpl;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.Animatable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.layer.AnimatableLayer;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter.Type;
import au.gov.ga.worldwind.animator.animation.parameter.BasicBezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.BasicParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.StaticMessageSource;

/**
 * Unit tests for the {@link XmlAnimationReader} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class XmlAnimationReaderTest
{

	private static final double ACCEPTABLE_ERROR = 0.001;
	
	/** The class to test. */
	// This class is stateless, so we can instantiate it here.
	private XmlAnimationReader classToBeTested = new XmlAnimationReader();
	
	@Before
	public void setup()
	{
		MessageSourceAccessor.set(new StaticMessageSource());
	}
	
	/**
	 * Test reading a V2 animation file
	 */
	@Test
	public void testReadAnimationV2() throws Exception
	{
		File inputFile = new File(getClass().getResource("v2AnimationFile.xml").toURI());
		
		Animation result = classToBeTested.readAnimation(inputFile, new WorldWindowImpl());
		
		assertNotNull(result);
		
		assertNotNull(result.getCamera());
		assertEquals("Render Camera", result.getCamera().getName());
		
		assertParameterCorrect(result.getCamera().getEyeLat(), "Render Camera - Eye Lat", 3, true);
		assertParameterCorrect(result.getCamera().getEyeLon(), "Render Camera - Eye Lon", 2, true);
		assertParameterCorrect(result.getCamera().getEyeElevation(), "Render Camera - Eye Elevation", 1, true);
		
		assertParameterCorrect(result.getCamera().getLookAtLat(), "Render Camera - Lookat Lat", 0, true);
		assertParameterCorrect(result.getCamera().getLookAtLon(), "Render Camera - Lookat Lon", 0, true);
		assertParameterCorrect(result.getCamera().getLookAtElevation(), "Render Camera - Lookat Elevation", 0, true);
		
		// Check the layers loaded correctly
		assertEquals(3, result.getLayers().size());
		assertContainsAnimatableLayer(result, "Layer1");
		assertContainsAnimatableLayer(result, "Layer2");
		assertContainsAnimatableLayer(result, "Layer3");
		
		AnimatableLayer layer2 = getAnimatableLayer(result, "Layer2");
		assertAnimatableLayerCorrect(layer2);
		
		AnimatableLayer layer3 = getAnimatableLayer(result, "Layer3");
		assertAnimatableLayerCorrect(layer3);
		
		// Check the key frames
		assertEquals(10, result.getKeyFrameCount());
		
		// Test one key frame for correct values
		KeyFrame frame = result.getFirstKeyFrame();
		assertEquals(0, frame.getFrame());
		assertEquals(4, frame.getParameterValues().size());
		
		ParameterValue eyeLatValue = frame.getValueForParameter(result.getCamera().getEyeLat());
		assertBezierParameterValueCorrect(eyeLatValue, 1.0, 1.1, 1.2, 1.3, 1.4, false);
		
		ParameterValue eyeLonValue = frame.getValueForParameter(result.getCamera().getEyeLon());
		assertBezierParameterValueCorrect(eyeLonValue, 11.0, 11.1, 11.2, 11.3, 11.4, true);
		
		ParameterValue layer2OpacityValue = frame.getValueForParameter(layer2.getParameterOfType(Type.OPACITY));
		assertBezierParameterValueCorrect(layer2OpacityValue, 1.0, 1.0, 0.4, 1.0, 0.4, true);
		
		ParameterValue layer3OpacityValue = frame.getValueForParameter(layer3.getParameterOfType(Type.OPACITY));
		assertLinearParameterValueCorrect(layer3OpacityValue, 0.5);
	}

	private void assertAnimatableLayerCorrect(AnimatableLayer layer)
	{
		assertNotNull(layer);
		assertEquals(1, layer.getParameters().size());
		assertNotNull(layer.getParameterOfType(Type.OPACITY));
	}

	private void assertContainsAnimatableLayer(Animation animation, String name)
	{
		for (Animatable animatableObject: animation.getAnimatableObjects())
		{
			if (animatableObject instanceof AnimatableLayer && animatableObject.getName().equals(name))
			{
				return;
			}
		}
		fail();
	}
	
	private AnimatableLayer getAnimatableLayer(Animation animation, String name)
	{
		for (Animatable animatableObject: animation.getAnimatableObjects())
		{
			if (animatableObject instanceof AnimatableLayer && animatableObject.getName().equals(name))
			{
				return (AnimatableLayer)animatableObject;
			}
		}
		return null;
	}
	

	private void assertParameterCorrect(Parameter parameter, String parameterName, int numberOfKeyFrames, boolean enabled)
	{
		assertNotNull(parameter);
		assertEquals(enabled, parameter.isEnabled());
		assertEquals(parameterName, parameter.getName());
		assertEquals(numberOfKeyFrames, parameter.getKeyFramesWithThisParameter().size());
	}
	
	private void assertBezierParameterValueCorrect(ParameterValue pv, double value, double inValue, double inPercent, double outValue, double outPercent, boolean locked)
	{
		assertNotNull(pv);
		assertTrue(pv instanceof BasicBezierParameterValue);
		assertEquals(value, pv.getValue(), ACCEPTABLE_ERROR);
		assertEquals(inValue, ((BasicBezierParameterValue)pv).getInValue(), ACCEPTABLE_ERROR);
		assertEquals(inPercent, ((BasicBezierParameterValue)pv).getInPercent(), ACCEPTABLE_ERROR);
		assertEquals(outValue, ((BasicBezierParameterValue)pv).getOutValue(), ACCEPTABLE_ERROR);
		assertEquals(outPercent, ((BasicBezierParameterValue)pv).getOutPercent(), ACCEPTABLE_ERROR);
		assertEquals(locked, ((BasicBezierParameterValue)pv).isLocked());
	}
	
	private void assertLinearParameterValueCorrect(ParameterValue pv, double value)
	{
		assertNotNull(pv);
		assertTrue(pv instanceof BasicParameterValue);
		assertEquals(value, pv.getValue(), ACCEPTABLE_ERROR);
	}
	
}
