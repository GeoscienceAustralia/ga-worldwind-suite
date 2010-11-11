package au.gov.ga.worldwind.animator.parameter;

import static org.junit.Assert.*;

import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.parameter.BasicBezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.InterpolatorFactory;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.math.interpolation.BezierInterpolator;
import au.gov.ga.worldwind.animator.math.interpolation.Interpolator;
import au.gov.ga.worldwind.animator.math.vector.Vector2;

/**
 * Unit tests for the {@link InterpolatorFactory} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class InterpolatorFactoryTest
{
	private Parameter owner;

	@Before
	public void setup()
	{
		Mockery context = new Mockery();
		
		owner = context.mock(Parameter.class);
	}
	
	/**
	 * Tests the {@link InterpolatorFactory#getInterpolator} method with:
	 * <ul>
	 * 	<li>Two values that are both Bezier values
	 * </ul>
	 * Expect:
	 * <ul>
	 * 	<li>A Bezier interpolator primed with the appropriate values
	 * </ul>
	 */
	@Test
	public void testGetInterpolatorTwoBezierValues()
	{
		ParameterValue startValue = new BasicBezierParameterValue(10.0, 0, owner, 8d, 0.4, 12d, 0.4);
		ParameterValue endValue = new BasicBezierParameterValue(20.0, 10, owner, 22d, 0.4, 18d, 0.4);
		
		Interpolator<Vector2> result = InterpolatorFactory.getInterpolator(startValue, endValue);
		
		assertNotNull(result);
		assertTrue(result instanceof BezierInterpolator<?>);
		
		BezierInterpolator<Vector2> interpolator = (BezierInterpolator<Vector2>)result;
		
		assertEquals(new Vector2(0, 10), interpolator.getBegin());
		assertEquals(new Vector2(0.4, 12), interpolator.getOut());
		assertEquals(new Vector2(0.6, 22), interpolator.getIn());
		assertEquals(new Vector2(1, 20), interpolator.getEnd());
	}

}
