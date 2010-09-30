package au.gov.ga.worldwind.animator.animation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gov.nasa.worldwind.WorldWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.parameter.BasicParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.common.util.message.StaticMessageSource;

/**
 * Unit tests for the {@link WorldWindAnimationImpl} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class WorldWindAnimationImplTest
{
	private Mockery mockContext;
	
	private WorldWindAnimationImpl classToBeTested;
	
	private WorldWindow worldWindow;
	
	/** Some parameters to use in tests */
	private List<Parameter> testParameters;
	
	@Before
	public void setup()
	{
		MessageSourceAccessor.set(new StaticMessageSource());
		
		mockContext = new Mockery();
		
		initialiseTestParameters();
		
		worldWindow = mockContext.mock(WorldWindow.class);
		
		classToBeTested = new WorldWindAnimationImpl(worldWindow);
		classToBeTested.setFrameCount(100);
	}

	private void initialiseTestParameters()
	{
		testParameters = new ArrayList<Parameter>();
		for (int i = 0; i < 10; i++)
		{
			final Parameter parameter = mockContext.mock(Parameter.class, "Parameter" + i);
			mockContext.checking(new Expectations(){{
				allowing(parameter).receiveAnimationEvent(with(any(AnimationEvent.class)));
			}});
			testParameters.add(parameter);
		}
	}
	
	/**
	 * Tests the {@link WorldWindAnimationImpl#getKeyFrameWithParameterBeforeFrame()} method with multiple frames
	 * with multiple parameters.
	 */
	@Test
	public void testGetFrameBeforeWithMultipleFrames()
	{
		classToBeTested.insertKeyFrame(createKeyFrame(0, testParameters.get(0)));
		classToBeTested.insertKeyFrame(createKeyFrame(50, testParameters.get(0), testParameters.get(1)));
		classToBeTested.insertKeyFrame(createKeyFrame(100, testParameters.get(0), testParameters.get(1)));
		
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(0), 0));
		assertEquals(0, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(0), 25).getFrame());
		assertEquals(0, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(0), 50).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(0), 75).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(0), 100).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(0), 125).getFrame());
		
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(1), 0));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(1), 25));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(1), 50));
		assertEquals(50, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(1), 75).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(1), 100).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(1), 125).getFrame());
		
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(2), 0));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(2), 25));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(2), 50));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(2), 75));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(2), 100));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterBeforeFrame(testParameters.get(2), 125));
	}
	
	/**
	 * Tests the {@link WorldWindAnimationImpl#getKeyFrameWithParameterBeforeFrame()} method with multiple frames
	 * with multiple parameters.
	 */
	@Test
	public void testGetFrameAfterWithMultipleFrames()
	{
		classToBeTested.insertKeyFrame(createKeyFrame(0, testParameters.get(0)));
		classToBeTested.insertKeyFrame(createKeyFrame(50, testParameters.get(0), testParameters.get(1)));
		classToBeTested.insertKeyFrame(createKeyFrame(100, testParameters.get(0), testParameters.get(1)));
		
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 0).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 25).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 50).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 75).getFrame());
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 100));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 125));
		
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 0).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 25).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 50).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 75).getFrame());
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 100));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 125));
		
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 0));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 25));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 50));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 75));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 100));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 125));
	}

	/**
	 * Tests the scale method with scale factor of 0
	 */
	@Test
	public void testScaleWith0()
	{
		classToBeTested.setFrameCount(100);
		
		classToBeTested.insertKeyFrame(createKeyFrame(0, testParameters.get(0)));
		classToBeTested.insertKeyFrame(createKeyFrame(50, testParameters.get(1)));
		classToBeTested.insertKeyFrame(createKeyFrame(100, testParameters.get(2)));
		
		try 
		{
			classToBeTested.scale(0.0);
			fail("Expected an exception but got none");
		}
		catch (IllegalArgumentException e)
		{
			// pass
		}
	}
	
	/**
	 * Tests the scale method with scale factor of < 1
	 */
	@Test
	public void testScaleWithLT1()
	{
		classToBeTested.setFrameCount(100);
		
		classToBeTested.insertKeyFrame(createKeyFrame(0, testParameters.get(0)));
		classToBeTested.insertKeyFrame(createKeyFrame(50, testParameters.get(1)));
		classToBeTested.insertKeyFrame(createKeyFrame(100, testParameters.get(2)));
		
		try 
		{
			classToBeTested.scale(0.5);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			fail("Got an exception when one was not expected");
		}
		
		assertEquals(100, classToBeTested.getFrameCount());
		
		List<KeyFrame> keyFrames = classToBeTested.getKeyFrames();
		assertEquals(3, keyFrames.size());
		
		// Check the frames have been updated, and the key frames are in the right order, and have the correct parameter values
		assertEquals(0, keyFrames.get(0).getFrame());
		assertEquals(0, keyFrames.get(0).getValueForParameter(testParameters.get(0)).getFrame());
		assertEquals(null, keyFrames.get(0).getValueForParameter(testParameters.get(1)));
		assertEquals(null, keyFrames.get(0).getValueForParameter(testParameters.get(2)));
		
		assertEquals(25, keyFrames.get(1).getFrame());
		assertEquals(null, keyFrames.get(1).getValueForParameter(testParameters.get(0)));
		assertEquals(25, keyFrames.get(1).getValueForParameter(testParameters.get(1)).getFrame());
		assertEquals(null, keyFrames.get(1).getValueForParameter(testParameters.get(2)));
		
		assertEquals(50, keyFrames.get(2).getFrame());
		assertEquals(null, keyFrames.get(2).getValueForParameter(testParameters.get(0)));
		assertEquals(null, keyFrames.get(2).getValueForParameter(testParameters.get(1)));
		assertEquals(50, keyFrames.get(2).getValueForParameter(testParameters.get(2)).getFrame());
	}
	
	/**
	 * Tests the scale method with scale factor of = 1
	 */
	@Test
	public void testScaleWithEQ1()
	{
		classToBeTested.setFrameCount(100);
		
		classToBeTested.insertKeyFrame(createKeyFrame(0, testParameters.get(0)));
		classToBeTested.insertKeyFrame(createKeyFrame(50, testParameters.get(1)));
		classToBeTested.insertKeyFrame(createKeyFrame(100, testParameters.get(2)));
		
		try 
		{
			classToBeTested.scale(1.0);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			fail("Got an exception when one was not expected");
		}
		
		assertEquals(100, classToBeTested.getFrameCount());
		
		List<KeyFrame> keyFrames = classToBeTested.getKeyFrames();
		assertEquals(3, keyFrames.size());
		
		// Check the frames have been updated, and the key frames are in the right order, and have the correct parameter values
		assertEquals(0, keyFrames.get(0).getFrame());
		assertEquals(0, keyFrames.get(0).getValueForParameter(testParameters.get(0)).getFrame());
		assertEquals(null, keyFrames.get(0).getValueForParameter(testParameters.get(1)));
		assertEquals(null, keyFrames.get(0).getValueForParameter(testParameters.get(2)));
		
		assertEquals(50, keyFrames.get(1).getFrame());
		assertEquals(null, keyFrames.get(1).getValueForParameter(testParameters.get(0)));
		assertEquals(50, keyFrames.get(1).getValueForParameter(testParameters.get(1)).getFrame());
		assertEquals(null, keyFrames.get(1).getValueForParameter(testParameters.get(2)));
		
		assertEquals(100, keyFrames.get(2).getFrame());
		assertEquals(null, keyFrames.get(2).getValueForParameter(testParameters.get(0)));
		assertEquals(null, keyFrames.get(2).getValueForParameter(testParameters.get(1)));
		assertEquals(100, keyFrames.get(2).getValueForParameter(testParameters.get(2)).getFrame());
	}
	
	/**
	 * Tests the scale method with scale factor of > 1
	 */
	@Test
	public void testScaleWithGT1()
	{
		classToBeTested.setFrameCount(100);
		
		classToBeTested.insertKeyFrame(createKeyFrame(0, testParameters.get(0)));
		classToBeTested.insertKeyFrame(createKeyFrame(50, testParameters.get(1)));
		classToBeTested.insertKeyFrame(createKeyFrame(100, testParameters.get(2)));
		
		try 
		{
			classToBeTested.scale(2.0);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			fail("Got an exception when one was not expected");
		}
		
		assertEquals(200, classToBeTested.getFrameCount());
		
		List<KeyFrame> keyFrames = classToBeTested.getKeyFrames();
		assertEquals(3, keyFrames.size());
		
		// Check the frames have been updated, and the key frames are in the right order, and have the correct parameter values
		assertEquals(0, keyFrames.get(0).getFrame());
		assertEquals(0, keyFrames.get(0).getValueForParameter(testParameters.get(0)).getFrame());
		assertEquals(null, keyFrames.get(0).getValueForParameter(testParameters.get(1)));
		assertEquals(null, keyFrames.get(0).getValueForParameter(testParameters.get(2)));
		
		assertEquals(100, keyFrames.get(1).getFrame());
		assertEquals(null, keyFrames.get(1).getValueForParameter(testParameters.get(0)));
		assertEquals(100, keyFrames.get(1).getValueForParameter(testParameters.get(1)).getFrame());
		assertEquals(null, keyFrames.get(1).getValueForParameter(testParameters.get(2)));
		
		assertEquals(200, keyFrames.get(2).getFrame());
		assertEquals(null, keyFrames.get(2).getValueForParameter(testParameters.get(0)));
		assertEquals(null, keyFrames.get(2).getValueForParameter(testParameters.get(1)));
		assertEquals(200, keyFrames.get(2).getValueForParameter(testParameters.get(2)).getFrame());
	}
	
	@Test
	public void testRemoveAnimatableClearsValuesFromKeyFrames()
	{
		final Animatable animatable = createAnimatable("Object1", testParameters.get(0), testParameters.get(1));
		
		classToBeTested.addAnimatableObject(animatable);
		
		classToBeTested.insertKeyFrame(createKeyFrame(0, testParameters.get(0)));
		classToBeTested.insertKeyFrame(createKeyFrame(1, testParameters.get(0), testParameters.get(1)));
		classToBeTested.insertKeyFrame(createKeyFrame(2, testParameters.get(0), testParameters.get(1), testParameters.get(2)));
		classToBeTested.insertKeyFrame(createKeyFrame(3, testParameters.get(1), testParameters.get(2)));
		classToBeTested.insertKeyFrame(createKeyFrame(4, testParameters.get(2)));
		
		assertEquals(5, classToBeTested.getKeyFrameCount());
		
		mockContext.checking(new Expectations(){{
			oneOf(animatable).removeChangeListener(with(classToBeTested));
		}});
		
		classToBeTested.removeAnimatableObject(animatable);
		
		assertEquals(3, classToBeTested.getKeyFrameCount());
		
		List<KeyFrame> keyFrames = classToBeTested.getKeyFrames();
		assertEquals(2, keyFrames.get(0).getFrame());
		assertEquals(3, keyFrames.get(1).getFrame());
		assertEquals(4, keyFrames.get(2).getFrame());
		
		assertEquals(0, classToBeTested.getKeyFrames(testParameters.get(0)).size());
		assertEquals(0, classToBeTested.getKeyFrames(testParameters.get(1)).size());
		assertEquals(3, classToBeTested.getKeyFrames(testParameters.get(2)).size());
	}
	
	@Test
	public void testChangeAnimatableOrderWithMoveUp()
	{
		final Animatable animatable1 = createAnimatable("Object1", testParameters.get(0), testParameters.get(1));
		final Animatable animatable2 = createAnimatable("Object2", testParameters.get(1));
		final Animatable animatable3 = createAnimatable("Object3", testParameters.get(2), testParameters.get(1));
		final Animatable animatable4 = createAnimatable("Object4", testParameters.get(0));
		final Animatable camera = classToBeTested.getCamera();
		final Animatable elevation = classToBeTested.getAnimatableElevation();
		
		classToBeTested.addAnimatableObject(animatable1);
		classToBeTested.addAnimatableObject(animatable2);
		classToBeTested.addAnimatableObject(animatable3);
		classToBeTested.addAnimatableObject(animatable4);
		
		assertEquals(0, classToBeTested.getAnimatableObjects().indexOf(camera));
		assertEquals(1, classToBeTested.getAnimatableObjects().indexOf(elevation));
		assertEquals(2, classToBeTested.getAnimatableObjects().indexOf(animatable1));
		assertEquals(3, classToBeTested.getAnimatableObjects().indexOf(animatable2));
		assertEquals(4, classToBeTested.getAnimatableObjects().indexOf(animatable3));
		assertEquals(5, classToBeTested.getAnimatableObjects().indexOf(animatable4));
		
		classToBeTested.changeOrderOfAnimatableObject(animatable3, 0);
		
		assertEquals(0, classToBeTested.getAnimatableObjects().indexOf(animatable3));
		assertEquals(1, classToBeTested.getAnimatableObjects().indexOf(camera));
		assertEquals(2, classToBeTested.getAnimatableObjects().indexOf(elevation));
		assertEquals(3, classToBeTested.getAnimatableObjects().indexOf(animatable1));
		assertEquals(4, classToBeTested.getAnimatableObjects().indexOf(animatable2));
		assertEquals(5, classToBeTested.getAnimatableObjects().indexOf(animatable4));
	}
	
	@Test
	public void testChangeAnimatableOrderWithMoveDown()
	{
		final Animatable animatable1 = createAnimatable("Object1", testParameters.get(0), testParameters.get(1));
		final Animatable animatable2 = createAnimatable("Object2", testParameters.get(1));
		final Animatable animatable3 = createAnimatable("Object3", testParameters.get(2), testParameters.get(1));
		final Animatable animatable4 = createAnimatable("Object4", testParameters.get(0));
		final Animatable camera = classToBeTested.getCamera();
		final Animatable elevation = classToBeTested.getAnimatableElevation();
		
		classToBeTested.addAnimatableObject(animatable1);
		classToBeTested.addAnimatableObject(animatable2);
		classToBeTested.addAnimatableObject(animatable3);
		classToBeTested.addAnimatableObject(animatable4);
		
		assertEquals(0, classToBeTested.getAnimatableObjects().indexOf(camera));
		assertEquals(1, classToBeTested.getAnimatableObjects().indexOf(elevation));
		assertEquals(2, classToBeTested.getAnimatableObjects().indexOf(animatable1));
		assertEquals(3, classToBeTested.getAnimatableObjects().indexOf(animatable2));
		assertEquals(4, classToBeTested.getAnimatableObjects().indexOf(animatable3));
		assertEquals(5, classToBeTested.getAnimatableObjects().indexOf(animatable4));
		
		classToBeTested.changeOrderOfAnimatableObject(animatable1, 4);
		
		assertEquals(0, classToBeTested.getAnimatableObjects().indexOf(camera));
		assertEquals(1, classToBeTested.getAnimatableObjects().indexOf(elevation));
		assertEquals(2, classToBeTested.getAnimatableObjects().indexOf(animatable2));
		assertEquals(3, classToBeTested.getAnimatableObjects().indexOf(animatable3));
		assertEquals(4, classToBeTested.getAnimatableObjects().indexOf(animatable1));
		assertEquals(5, classToBeTested.getAnimatableObjects().indexOf(animatable4));
	}
	
	@Test
	public void testChangeFiredWhenChangeAnimatableOrder()
	{
		final Animatable animatable1 = createAnimatable("Object1", testParameters.get(0), testParameters.get(1));
		final Animatable animatable2 = createAnimatable("Object2", testParameters.get(1));
		final Animatable animatable3 = createAnimatable("Object3", testParameters.get(2), testParameters.get(1));
		final Animatable animatable4 = createAnimatable("Object4", testParameters.get(0));
		
		classToBeTested.addAnimatableObject(animatable1);
		classToBeTested.addAnimatableObject(animatable2);
		classToBeTested.addAnimatableObject(animatable3);
		classToBeTested.addAnimatableObject(animatable4);

		// Expect 1 call to the listener
		final AnimationEventListener listener = mockContext.mock(AnimationEventListener.class);
		mockContext.checking(new Expectations(){{
			oneOf(listener).receiveAnimationEvent(with(any(AnimationEvent.class)));
		}});
		classToBeTested.addChangeListener(listener);
		
		classToBeTested.changeOrderOfAnimatableObject(animatable1, 2);
	}
	
	@Test
	public void testNoChangeFiredWhenChangeAnimatableOrderNotMoved()
	{
		final Animatable animatable1 = createAnimatable("Object1", testParameters.get(0), testParameters.get(1));
		final Animatable animatable2 = createAnimatable("Object2", testParameters.get(1));
		final Animatable animatable3 = createAnimatable("Object3", testParameters.get(2), testParameters.get(1));
		final Animatable animatable4 = createAnimatable("Object4", testParameters.get(0));
		
		classToBeTested.addAnimatableObject(animatable1);
		classToBeTested.addAnimatableObject(animatable2);
		classToBeTested.addAnimatableObject(animatable3);
		classToBeTested.addAnimatableObject(animatable4);

		// Expect 0 calls to the listener
		final AnimationEventListener listener = mockContext.mock(AnimationEventListener.class);
		mockContext.checking(new Expectations(){{
			never(listener).receiveAnimationEvent(with(any(AnimationEvent.class)));
		}});
		classToBeTested.addChangeListener(listener);
		
		assertEquals(2, classToBeTested.getAnimatableObjects().indexOf(animatable1));
		classToBeTested.changeOrderOfAnimatableObject(animatable1, 2);
	}
	
	private Animatable createAnimatable(final String name, final Parameter... parameters)
	{
		final Animatable result = mockContext.mock(Animatable.class, "Animatable" + name);
		mockContext.checking(new Expectations(){{
			allowing(result).getName();will(returnValue(name));
			allowing(result).getParameters();will(returnValue(Arrays.asList(parameters)));
			allowing(result).addChangeListener(with(any(AnimationEventListener.class)));
		}});
		return result;
	}

	/**
	 * @return A new key frame at the provided frame, containing a value for each provided parameter
	 */
	private KeyFrame createKeyFrame(int frame, Parameter... parameters)
	{
		Collection<ParameterValue> parameterValues = new ArrayList<ParameterValue>(parameters.length);
		for (int i = 0; i < parameters.length; i++)
		{
			parameterValues.add(new BasicParameterValue(1.0, frame, parameters[i]));
		}
		
		return new KeyFrameImpl(frame, parameterValues);
	}
	
}
