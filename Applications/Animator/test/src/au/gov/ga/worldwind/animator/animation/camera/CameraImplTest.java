package au.gov.ga.worldwind.animator.animation.camera;

import static org.junit.Assert.*;
import gov.nasa.worldwind.WorldWindow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.animation.parameter.BasicParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.animator.util.message.StaticMessageSource;

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
		asserteyeKeyFrameEquals(0, 0.0, 0.0, 0.0, newKeyFrames.get(0));
	}
	
	/**
	 * Tests the {@link CameraImpl#smoothEyeSpeed} class with two frames recorded
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
		asserteyeKeyFrameEquals(0, 0.0, 0.0, 0.0, newKeyFrames.get(0));
		asserteyeKeyFrameEquals(10, 10.0, 10.0, 10.0, newKeyFrames.get(1));
	}

	
	private void asserteyeKeyFrameEquals(int frame, double eyeLat, double eyeLon, double eyeEl, KeyFrame keyFrame)
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
