package au.gov.ga.worldwind.animator.animation.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.WorldWindowImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.animation.parameter.BasicBezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.StaticMessageSource;
import au.gov.ga.worldwind.test.util.TestUtils;

/**
 * Tests for the {@link XmlAnimationWriter} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class XmlAnimationWriterTest
{
	// Class is stateless, so can instantiate here
	private XmlAnimationWriter classToBeTested = new XmlAnimationWriter();
	
	private String actualOutputFileName = "testOutput.xml";
	
	private InputStream expectedStream = getClass().getClassLoader().getResourceAsStream("au/gov/ga/worldwind/animator/animation/io/expectedXmlOutput.xml");
	
	private Animation animationToSave;
	
	@Before
	public void setup()
	{
		MessageSourceAccessor.set(new StaticMessageSource());
		
		animationToSave = createAnimation();
	}
	
	/**
	 * @return A new animation
	 */
	private Animation createAnimation()
	{
		Animation result = new WorldWindAnimationImpl(new WorldWindowImpl());
		
		ParameterValue eyeLatVal1 = new BasicBezierParameterValue(1.0, 0, result.getCamera().getEyeLat(), 1.1, 1.2, 1.3, 1.4);
		ParameterValue eyeLatVal2 = new BasicBezierParameterValue(2.0, 10, result.getCamera().getEyeLat(), 2.1, 2.2, 2.3, 2.4);
		ParameterValue eyeLatVal3 = new BasicBezierParameterValue(3.0, 20, result.getCamera().getEyeLat(), 3.1, 3.2, 3.3, 3.4);
		
		ParameterValue eyeLonVal1 = new BasicBezierParameterValue(11.0, 0, result.getCamera().getEyeLon(), 11.1, 11.2, 11.3, 11.4);
		ParameterValue eyeLonVal2 = new BasicBezierParameterValue(11.0, 11, result.getCamera().getEyeLon(), 12.1, 12.2, 12.3, 12.4);
		
		ParameterValue eyeElevationVal1 = new BasicBezierParameterValue(21.0, 31, result.getCamera().getEyeElevation(), 22.1, 22.2, 22.3, 22.4);
		
		result.insertKeyFrame(new KeyFrameImpl(0, Arrays.asList(new ParameterValue[]{eyeLatVal1, eyeLonVal1})), false);
		result.insertKeyFrame(new KeyFrameImpl(10, Arrays.asList(new ParameterValue[]{eyeLatVal2})), false);
		result.insertKeyFrame(new KeyFrameImpl(11, Arrays.asList(new ParameterValue[]{eyeLonVal2})), false);
		result.insertKeyFrame(new KeyFrameImpl(20, Arrays.asList(new ParameterValue[]{eyeLatVal3})), false);
		result.insertKeyFrame(new KeyFrameImpl(31, Arrays.asList(new ParameterValue[]{eyeElevationVal1})), false);
		
		return result;
	}

	@Test
	public void testWriteFileWithString() throws Exception
	{
		classToBeTested.writeAnimation(actualOutputFileName, animationToSave);
		
		File outputFile = new File(actualOutputFileName);
		assertTrue(outputFile.exists());
		
		
		FileInputStream actualStream = new FileInputStream(outputFile);
		assertEquals(TestUtils.readStreamToString(expectedStream), TestUtils.readStreamToString(actualStream));
		actualStream.close();
		outputFile.delete();
	}
	
	
}
