package au.gov.ga.worldwind.animator.animation.camera;

import static org.junit.Assert.*;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.parameter.BasicBezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.BasicParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.StaticMessageSource;

/**
 * Unit tests for the {@link CameraImpl} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraImplTest
{
	private static final double ACCEPTABLE_ERROR = 0.001;

	private Mockery mockContext;
	
	private CameraImpl classToBeTested;
	
	private Animation animation;
	
	private AnimationContext animationContext;
	
	@Before
	public void setup()
	{
		MessageSourceAccessor.set(new StaticMessageSource());
		
		mockContext = new Mockery();
		
		animation = new WorldWindAnimationImpl(mockContext.mock(WorldWindow.class));
		
		animationContext = new AnimationContextImpl(animation);
		
		classToBeTested = new CameraImpl(animation);
		
	}
	
	/**
	 * Tests the {@link CameraImpl#smoothEyeSpeed} class with no frames recorded
	 */
	@Test
	public void testSmoothEyeNoFrames()
	{
		setCameraKeyFrames(new ArrayList<KeyFrame>());
		
		classToBeTested.smoothEyeSpeed(animationContext);
		
		List<KeyFrame> newKeyFrames = animation.getKeyFrames();
		assertNotNull(newKeyFrames);
		assertEquals(0, newKeyFrames.size());
	}
	
	/**
	 * Tests the {@link CameraImpl#smoothEyeSpeed} class with one frame recorded
	 * <p/>
	 * Expect no changes.
	 */
	@Test
	public void testSmoothEyeOneFrame()
	{
		ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
		keyFrames.add(createEyeKeyFrame(0, 0.0, 0.0, 0.0));
		
		setCameraKeyFrames(keyFrames);
		
		classToBeTested.smoothEyeSpeed(animationContext);
		
		List<KeyFrame> newKeyFrames = animation.getKeyFrames();
		assertNotNull(newKeyFrames);
		assertEquals(1, newKeyFrames.size());
		assertEyeKeyFrameEquals(0, 0.0, 0.0, 0.0, newKeyFrames.get(0));
	}
	
	/**
	 * Tests the {@link CameraImpl#smoothEyeSpeed} class with two frames recorded
	 * <p/>
	 * Expect no changes.
	 */
	@Test
	public void testSmoothEyeTwoFrames()
	{
		ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
		keyFrames.add(createEyeKeyFrame(0, 0.0, 0.0, 0.0));
		keyFrames.add(createEyeKeyFrame(10, 10.0, 10.0, 10.0));
		
		setCameraKeyFrames(keyFrames);
		
		classToBeTested.smoothEyeSpeed(animationContext);
		
		List<KeyFrame> newKeyFrames = animation.getKeyFrames();
		assertNotNull(newKeyFrames);
		assertEquals(2, newKeyFrames.size());
		assertEyeKeyFrameEquals(0, 0.0, 0.0, 0.0, newKeyFrames.get(0));
		assertEyeKeyFrameEquals(10, 10.0, 10.0, 10.0, newKeyFrames.get(1));
	}

	/**
	 * Tests the {@link CameraImpl#smoothEyeSpeed} class with three frames recorded
	 * <p/>
	 * Expect end frames untouched, middle frame adjusted appropriately.
	 */
	@Test
	public void testSmoothEyeThreeFrames()
	{
		ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
		keyFrames.add(createEyeKeyFrame(0, 0.0, 0.0, 0.0));
		keyFrames.add(createEyeKeyFrame(8, 5.0, 5.0, 5.0));
		keyFrames.add(createEyeKeyFrame(10, 10.0, 10.0, 10.0));
		
		setCameraKeyFrames(keyFrames);
		
		classToBeTested.smoothEyeSpeed(animationContext);
		
		List<KeyFrame> newKeyFrames = animation.getKeyFrames();
		assertNotNull(newKeyFrames);
		assertEquals(3, newKeyFrames.size());
		assertEyeKeyFrameEquals(0, 0.0, 0.0, 0.0, newKeyFrames.get(0));
		assertEyeKeyFrameEquals(5, 5.0, 5.0, 5.0, newKeyFrames.get(1));
		assertEyeKeyFrameEquals(10, 10.0, 10.0, 10.0, newKeyFrames.get(2));
	}
	
	/**
	 * Tests the {@link CameraImpl#smoothEyeSpeed} called multiple times.
	 * <p/>
	 * Expect no changes after first call
	 */
	@Test
	public void testSmoothEyeMultipleTimes()
	{
		ArrayList<KeyFrame> keyFrames = new ArrayList<KeyFrame>();
		keyFrames.add(createEyeKeyFrame(0, 0.0, 0.0, 0.0));
		keyFrames.add(createEyeKeyFrame(8, 5.0, 5.0, 5.0));
		keyFrames.add(createEyeKeyFrame(10, 10.0, 10.0, 10.0));
		
		setCameraKeyFrames(keyFrames);
		
		classToBeTested.smoothEyeSpeed(animationContext);
		
		List<KeyFrame> newKeyFrames = animation.getKeyFrames();
		assertNotNull(newKeyFrames);
		assertEquals(3, newKeyFrames.size());
		assertEyeKeyFrameEquals(0, 0.0, 0.0, 0.0, newKeyFrames.get(0));
		assertEyeKeyFrameEquals(5, 5.0, 5.0, 5.0, newKeyFrames.get(1));
		assertEyeKeyFrameEquals(10, 10.0, 10.0, 10.0, newKeyFrames.get(2));
		
		classToBeTested.smoothEyeSpeed(animationContext);
		
		newKeyFrames = animation.getKeyFrames();
		assertNotNull(newKeyFrames);
		assertEquals(3, newKeyFrames.size());
		assertEyeKeyFrameEquals(0, 0.0, 0.0, 0.0, newKeyFrames.get(0));
		assertEyeKeyFrameEquals(5, 5.0, 5.0, 5.0, newKeyFrames.get(1));
		assertEyeKeyFrameEquals(10, 10.0, 10.0, 10.0, newKeyFrames.get(2));
		
		classToBeTested.smoothEyeSpeed(animationContext);
		
		newKeyFrames = animation.getKeyFrames();
		assertNotNull(newKeyFrames);
		assertEquals(3, newKeyFrames.size());
		assertEyeKeyFrameEquals(0, 0.0, 0.0, 0.0, newKeyFrames.get(0));
		assertEyeKeyFrameEquals(5, 5.0, 5.0, 5.0, newKeyFrames.get(1));
		assertEyeKeyFrameEquals(10, 10.0, 10.0, 10.0, newKeyFrames.get(2));
	}
	
	/**
	 * Test the {@link CameraImpl#fromXml()} method with a V2 file snippet
	 */
	@Test
	public void testFromXmlV2()
	{
		AnimationFileVersion version = AnimationFileVersion.VERSION020;
		
		AVList context = new AVListImpl();
		context.setValue(version.getConstants().getAnimationKey(), animation);
		
		Element element = WWXML.openDocument(getClass().getResourceAsStream("cameraXmlSnippet.xml")).getDocumentElement();
		
		CameraImpl result = (CameraImpl)new CameraImpl().fromXml(element, version, context);
		
		// Check the camera was de-serialised correctly
		assertNotNull(result);
		assertEquals("Test Render Camera", result.getName());
		
		assertNotNull(result.getEyeLat());
		assertTrue(result.getEyeLat().isEnabled());
		assertEquals("Test Render Camera - EyeLat", result.getEyeLat().getName());
		assertEquals(3, result.getEyeLat().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getEyeLon());
		assertFalse(result.getEyeLon().isEnabled());
		assertEquals("Test Render Camera - EyeLon", result.getEyeLon().getName());
		assertEquals(2, result.getEyeLon().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getEyeElevation());
		assertTrue(result.getEyeElevation().isEnabled());
		assertEquals("Test Render Camera - EyeElevation", result.getEyeElevation().getName());
		assertEquals(1, result.getEyeElevation().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getLookAtLat());
		assertTrue(result.getLookAtLat().isEnabled());
		assertEquals("Test Render Camera - LookAtLat", result.getLookAtLat().getName());
		assertEquals(0, result.getLookAtLat().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getLookAtLon());
		assertTrue(result.getLookAtLon().isEnabled());
		assertEquals("Test Render Camera - LookAtLon", result.getLookAtLon().getName());
		assertEquals(0, result.getLookAtLon().getKeyFramesWithThisParameter().size());
		
		assertNotNull(result.getLookAtElevation());
		assertFalse(result.getLookAtElevation().isEnabled());
		assertEquals("Test Render Camera - LookAtElevation", result.getLookAtElevation().getName());
		assertEquals(0, result.getLookAtElevation().getKeyFramesWithThisParameter().size());
		
		assertEquals(5, animation.getKeyFrameCount());
		
		// Test one key frame for correct values
		KeyFrame frame = animation.getFirstKeyFrame();
		assertEquals(0, frame.getFrame());
		assertEquals(2, frame.getParameterValues().size());
		
		ParameterValue eyeLatValue = frame.getValueForParameter(result.getEyeLat());
		assertNotNull(eyeLatValue);
		assertTrue(eyeLatValue instanceof BasicBezierParameterValue);
		assertEquals(1.0, eyeLatValue.getValue(), ACCEPTABLE_ERROR);
		assertEquals(1.1, ((BasicBezierParameterValue)eyeLatValue).getInValue(), ACCEPTABLE_ERROR);
		assertEquals(1.2, ((BasicBezierParameterValue)eyeLatValue).getInPercent(), ACCEPTABLE_ERROR);
		assertEquals(1.3, ((BasicBezierParameterValue)eyeLatValue).getOutValue(), ACCEPTABLE_ERROR);
		assertEquals(1.4, ((BasicBezierParameterValue)eyeLatValue).getOutPercent(), ACCEPTABLE_ERROR);
		assertFalse(((BasicBezierParameterValue)eyeLatValue).isLocked());
		
		ParameterValue eyeLonValue = frame.getValueForParameter(result.getEyeLon());
		assertNotNull(eyeLonValue);
		assertFalse(eyeLonValue instanceof BasicBezierParameterValue);
		assertEquals(11.0, eyeLonValue.getValue(), ACCEPTABLE_ERROR);
	}
	
	private void assertEyeKeyFrameEquals(int frame, double eyeLat, double eyeLon, double eyeEl, KeyFrame keyFrame)
	{
		assertEquals(frame, keyFrame.getFrame());
		assertEquals(eyeLat, keyFrame.getValueForParameter(classToBeTested.getEyeLat()).getValue(), ACCEPTABLE_ERROR);
		assertEquals(eyeLon, keyFrame.getValueForParameter(classToBeTested.getEyeLon()).getValue(), ACCEPTABLE_ERROR);
		assertEquals(eyeEl, keyFrame.getValueForParameter(classToBeTested.getEyeElevation()).getValue(), ACCEPTABLE_ERROR);
	}

	private KeyFrame createEyeKeyFrame(int frame, double eyeLat, double eyeLon, double eyeEl)
	{
		Collection<ParameterValue> values = new ArrayList<ParameterValue>();
		values.add(new BasicParameterValue(eyeLat, frame, classToBeTested.getEyeLat()));
		values.add(new BasicParameterValue(eyeLon, frame, classToBeTested.getEyeLon()));
		values.add(new BasicParameterValue(eyeEl, frame, classToBeTested.getEyeElevation()));
		
		return new KeyFrameImpl(frame, values);
	}

	/**
	 * Set the list of camera key frames in the animation
	 */
	private void setCameraKeyFrames(final ArrayList<KeyFrame> keyFrames)
	{
		for (KeyFrame keyFrame : keyFrames)
		{
			animation.insertKeyFrame(keyFrame);
		}
	}
	
}
