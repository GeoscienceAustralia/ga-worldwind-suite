package au.gov.ga.worldwind.animator.animation.io;

import static org.junit.Assert.*;
import gov.nasa.worldwind.WorldWindowImpl;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.parameter.BasicBezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.animator.util.message.StaticMessageSource;

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
		File inputFile = new File(getClass().getResource("expectedXmlOutput.xml").toURI());
		
		Animation result = classToBeTested.readAnimation(inputFile, new WorldWindowImpl());
		
		assertNotNull(result);
		
		assertNotNull(result.getCamera());
		assertEquals("Render Camera", result.getCamera().getName());
		
		assertNotNull(result.getCamera().getEyeLat());
		assertTrue(result.getCamera().getEyeLat().isEnabled());
		assertEquals("Render Camera - Param", result.getCamera().getEyeLat().getName());
		assertEquals(3, result.getCamera().getEyeLat().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getCamera().getEyeLon());
		assertTrue(result.getCamera().getEyeLon().isEnabled());
		assertEquals("Render Camera - Param", result.getCamera().getEyeLon().getName());
		assertEquals(2, result.getCamera().getEyeLon().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getCamera().getEyeElevation());
		assertTrue(result.getCamera().getEyeElevation().isEnabled());
		assertEquals("Render Camera - Param", result.getCamera().getEyeElevation().getName());
		assertEquals(1, result.getCamera().getEyeElevation().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getCamera().getLookAtLat());
		assertTrue(result.getCamera().getLookAtLat().isEnabled());
		assertEquals("Render Camera - Param", result.getCamera().getLookAtLat().getName());
		assertEquals(0, result.getCamera().getLookAtLat().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getCamera().getLookAtLon());
		assertTrue(result.getCamera().getLookAtLon().isEnabled());
		assertEquals("Render Camera - Param", result.getCamera().getLookAtLon().getName());
		assertEquals(0, result.getCamera().getLookAtLon().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getCamera().getLookAtElevation());
		assertTrue(result.getCamera().getLookAtElevation().isEnabled());
		assertEquals("Render Camera - Param", result.getCamera().getLookAtElevation().getName());
		assertEquals(0, result.getCamera().getLookAtElevation().getKeyFramesWithThisParameter().size());
		
		assertEquals(5, result.getKeyFrameCount());
		
		// Test one key frame for correct values
		KeyFrame frame = result.getFirstKeyFrame();
		assertEquals(0, frame.getFrame());
		assertEquals(2, frame.getParameterValues().size());
		
		ParameterValue eyeLatValue = frame.getValueForParameter(result.getCamera().getEyeLat());
		assertNotNull(eyeLatValue);
		assertTrue(eyeLatValue instanceof BasicBezierParameterValue);
		assertEquals(1.0, eyeLatValue.getValue(), ACCEPTABLE_ERROR);
		assertEquals(1.1, ((BasicBezierParameterValue)eyeLatValue).getInValue(), ACCEPTABLE_ERROR);
		assertEquals(1.2, ((BasicBezierParameterValue)eyeLatValue).getInPercent(), ACCEPTABLE_ERROR);
		assertEquals(1.3, ((BasicBezierParameterValue)eyeLatValue).getOutValue(), ACCEPTABLE_ERROR);
		assertEquals(1.4, ((BasicBezierParameterValue)eyeLatValue).getOutPercent(), ACCEPTABLE_ERROR);
		assertFalse(((BasicBezierParameterValue)eyeLatValue).isLocked());
		
		ParameterValue eyeLonValue = frame.getValueForParameter(result.getCamera().getEyeLon());
		assertNotNull(eyeLonValue);
		assertTrue(eyeLonValue instanceof BasicBezierParameterValue);
		assertEquals(11.0, eyeLonValue.getValue(), ACCEPTABLE_ERROR);
		
		
	}
	
}
