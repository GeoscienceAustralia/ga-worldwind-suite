/**
 * 
 */
package au.gov.ga.worldwind.animator.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.parameter.BasicBezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.BasicParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.BezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * Unit tests for the {@link BasicBezierParameterValue} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class BasicBezierParameterValueTest
{

	private static final double ACCEPTABLE_DELTA = 0.01;
	
	private Parameter owner;
	
	private Mockery context;
	
	@Before
	public void setup()
	{
		this.context = new Mockery();
		
		this.owner = context.mock(Parameter.class);
	}
	
	/**
	 * Tests the constructor with:
	 * <ul>
	 * 	<li>A previous and next point provided
	 * 	<li>All three points co-linear and parallel to the X-dimension
	 * </ul>
	 * Expect:
	 * <ul>
	 * 	<li>'in' and 'out' to lie on the same line as 'value'
	 *  <li>'in' and 'out' percentages to be default 0.4
	 * </ul>
	 */
	@Test
	public void testCreateBezierThreePointsColinearXAxis()
	{
		setPreviousValue(new BasicParameterValue(10, 0, owner));
		setNextValue(new BasicParameterValue(10, 10, owner));
		
		BezierParameterValue result = new BasicBezierParameterValue(10, 5, owner);
		
		assertNotNull(result);
		assertEquals(10, result.getValue(), ACCEPTABLE_DELTA);
		
		assertEquals(10, result.getInValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getInPercent(), ACCEPTABLE_DELTA);
		
		assertEquals(10, result.getOutValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getOutPercent(), ACCEPTABLE_DELTA);
	}
	
	/**
	 * Tests the constructor with:
	 * <ul>
	 * 	<li>A previous and next point provided
	 * 	<li>All three points co-linear but not parallel to the X-dimension
	 * </ul>
	 * Expect:
	 * <ul>
	 * 	<li>'in' and 'out' to lie on the same line as 'value'
	 *  <li>'in' and 'out' percentages to be default 0.4
	 * </ul>
	 */
	@Test
	public void testCreateBezierThreePointsColinearOutOfAxis()
	{
		setPreviousValue(new BasicParameterValue(0, 0, owner));
		setNextValue(new BasicParameterValue(10, 10, owner));
		
		BezierParameterValue result = new BasicBezierParameterValue(5, 5, owner);
		
		assertNotNull(result);
		assertEquals(5, result.getValue(), ACCEPTABLE_DELTA);
		
		assertEquals(3, result.getInValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getInPercent(), ACCEPTABLE_DELTA);
		
		assertEquals(7, result.getOutValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getOutPercent(), ACCEPTABLE_DELTA);
	}
	
	/**
	 * Tests the constructor with:
	 * <ul>
	 * 	<li>A previous and next point provided
	 * 	<li>All three points non co-linear
	 * 	<li>'previous' < 'value' < 'next'
	 * </ul>
	 * Expect:
	 * <ul>
	 * 	<li>'in' and 'out' to lie on a line parallel to that between 'previous' and 'next'
	 *  <li>'in' and 'out' percentages to be default 0.4
	 * </ul>
	 */
	@Test
	public void testCreateBezierThreePointsNonColinearPrevLTValLTNext()
	{
		setPreviousValue(new BasicParameterValue(0, 0, owner));
		setNextValue(new BasicParameterValue(10, 10, owner));
		
		BezierParameterValue result = new BasicBezierParameterValue(5, 1, owner);
		
		assertNotNull(result);
		assertEquals(5, result.getValue(), ACCEPTABLE_DELTA);
		
		assertEquals(4.6, result.getInValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getInPercent(), ACCEPTABLE_DELTA);
		
		assertEquals(8.6, result.getOutValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getOutPercent(), ACCEPTABLE_DELTA);
	}
	
	/**
	 * Tests the constructor with:
	 * <ul>
	 * 	<li>A previous and next point provided
	 * 	<li>All three points non co-linear
	 * 	<li>'previous' > 'value' > 'next'
	 * </ul>
	 * Expect:
	 * <ul>
	 * 	<li>'in' and 'out' to lie on a line parallel to that between 'previous' and 'next'
	 *  <li>'in' and 'out' percentages to be default 0.4
	 * </ul>
	 */
	@Test
	public void testCreateBezierThreePointsNonColinearPrevGTValGTNext()
	{
		setPreviousValue(new BasicParameterValue(10, 0, owner));
		setNextValue(new BasicParameterValue(0, 10, owner));
		
		BezierParameterValue result = new BasicBezierParameterValue(5, 1, owner);
		
		assertNotNull(result);
		assertEquals(5, result.getValue(), ACCEPTABLE_DELTA);
		
		assertEquals(5.4, result.getInValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getInPercent(), ACCEPTABLE_DELTA);
		
		assertEquals(1.4, result.getOutValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getOutPercent(), ACCEPTABLE_DELTA);
	}
	
	/**
	 * Tests the constructor with:
	 * <ul>
	 * 	<li>A previous and next point provided
	 * 	<li>All three points non co-linear
	 * 	<li>'previous' > 'value' < 'next'
	 * </ul>
	 * Expect:
	 * <ul>
	 * 	<li>'in' and 'out' to lie on the same line as 'value', parallel to the X dimension
	 *  <li>'in' and 'out' percentages to be default 0.4
	 * </ul>
	 */
	@Test
	public void testCreateBezierThreePointsNonColinearPrevGTValLTNext()
	{
		setPreviousValue(new BasicParameterValue(10, 0, owner));
		setNextValue(new BasicParameterValue(10, 10, owner));
		
		BezierParameterValue result = new BasicBezierParameterValue(5, 5, owner);
		
		assertNotNull(result);
		assertEquals(5, result.getValue(), ACCEPTABLE_DELTA);
		
		assertEquals(5, result.getInValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getInPercent(), ACCEPTABLE_DELTA);
		
		assertEquals(5, result.getOutValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getOutPercent(), ACCEPTABLE_DELTA);
	}
	
	/**
	 * Tests the constructor with:
	 * <ul>
	 * 	<li>A previous and next point provided
	 * 	<li>All three points non co-linear
	 * 	<li>'previous' < 'value' > 'next'
	 * </ul>
	 * Expect:
	 * <ul>
	 * 	<li>'in' and 'out' to lie on the same line as 'value', parallel to the X dimension
	 *  <li>'in' and 'out' percentages to be default 0.4
	 * </ul>
	 */
	@Test
	public void testCreateBezierThreePointsNonColinearPrevLTValGTNext()
	{
		setPreviousValue(new BasicParameterValue(10, 0, owner));
		setNextValue(new BasicParameterValue(10, 10, owner));
		
		BezierParameterValue result = new BasicBezierParameterValue(15, 5, owner);
		
		assertNotNull(result);
		assertEquals(15, result.getValue(), ACCEPTABLE_DELTA);
		
		assertEquals(15, result.getInValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getInPercent(), ACCEPTABLE_DELTA);
		
		assertEquals(15, result.getOutValue(), ACCEPTABLE_DELTA);
		assertEquals(0.4, result.getOutPercent(), ACCEPTABLE_DELTA);
	}
	
	/**
	 * Set the previous key frame value for the 'owner' parameter
	 */
	private void setPreviousValue(final BasicParameterValue value)
	{
		context.checking(new Expectations(){{
			oneOf(owner).getValueAtKeyFrameBeforeFrame(with(any(Integer.class))); will(returnValue(value));
		}});
	}
	
	/**
	 *  Set the next key frame value for the 'owner' parameter
	 */
	private void setNextValue(final BasicParameterValue value)
	{
		context.checking(new Expectations(){{
			oneOf(owner).getValueAtKeyFrameAfterFrame(with(any(Integer.class))); will(returnValue(value));
		}});
	}
	
	
}
