package au.gov.ga.worldwind.animator.animation;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.WorldWindow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.parameter.BasicParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.util.message.MessageSourceAccessor;
import au.gov.ga.worldwind.animator.util.message.StaticMessageSource;

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
	}

	private void initialiseTestParameters()
	{
		testParameters = new ArrayList<Parameter>();
		for (int i = 0; i < 10; i++)
		{
			testParameters.add(mockContext.mock(Parameter.class, "Parameter" + i));
		}
	}
	
	/**
	 * Tests the {@link WorldWindAnimationImpl#getKeyFrameWithParameterBeforeFrame()} method with multiple frames
	 * with multiple parameters.
	 */
	@Test
	public void testGetFrameBeforeWithMultipleFrames()
	{
		KeyFrame kf1 = createKeyFrame(0, testParameters.get(0));
		KeyFrame kf2 = createKeyFrame(50, testParameters.get(0), testParameters.get(1));
		KeyFrame kf3 = createKeyFrame(100, testParameters.get(0), testParameters.get(1));
		
		classToBeTested.insertKeyFrame(kf1);
		classToBeTested.insertKeyFrame(kf2);
		classToBeTested.insertKeyFrame(kf3);
		
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
		KeyFrame kf1 = createKeyFrame(0, testParameters.get(0));
		KeyFrame kf2 = createKeyFrame(50, testParameters.get(0), testParameters.get(1));
		KeyFrame kf3 = createKeyFrame(100, testParameters.get(0), testParameters.get(1));
		
		classToBeTested.insertKeyFrame(kf1);
		classToBeTested.insertKeyFrame(kf2);
		classToBeTested.insertKeyFrame(kf3);
		
		assertEquals(0, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 0).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 25).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 50).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 75).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 100).getFrame());
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(0), 125));
		
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 0).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 25).getFrame());
		assertEquals(50, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 50).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 75).getFrame());
		assertEquals(100, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 100).getFrame());
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(1), 125));
		
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 0));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 25));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 50));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 75));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 100));
		assertEquals(null, classToBeTested.getKeyFrameWithParameterAfterFrame(testParameters.get(2), 125));
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
