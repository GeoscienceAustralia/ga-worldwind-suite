package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;


/**
 * Unit tests for the {@link LengthExpression} class
 */
public class LengthExpressionTest
{
	private static final float DELTA = 0.0001f;

	@Test
	public void testValidAbsoluteExpression()
	{
		LengthExpression classUnderTest = new LengthExpression("123.45px");
		
		assertEquals(123.45f, classUnderTest.getLength(800), DELTA);
		assertEquals(123.45f, classUnderTest.getLength(1000), DELTA);
	}
	
	@Test
	public void testValidExpressionWithWhitespace()
	{
		LengthExpression classUnderTest = new LengthExpression("  123px ");
		
		assertEquals(123f, classUnderTest.getLength(800), DELTA);
		assertEquals(123f, classUnderTest.getLength(1000), DELTA);
	}
	
	@Test
	public void testValidPercentExpression()
	{
		LengthExpression classUnderTest = new LengthExpression("50%");
		
		assertEquals(400f, classUnderTest.getLength(800), DELTA);
		assertEquals(500f, classUnderTest.getLength(1000), DELTA);
	}
	
	@Test
	public void testValidExpressionNoSuffix()
	{
		LengthExpression classUnderTest = new LengthExpression("50");
		
		assertEquals(50f, classUnderTest.getLength(800), DELTA);
		assertEquals(50f, classUnderTest.getLength(1000), DELTA);
	}
	
	@Test
	public void testInvalidExpression()
	{
		try
		{
			new LengthExpression("50pt");
			fail("Expected an illegal argument exception");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}
	
	@Test
	public void testInvalidExpressionWithSpace()
	{
		try
		{
			new LengthExpression("50 px");
			fail("Expected an illegal argument exception");
		}
		catch (IllegalArgumentException e)
		{
			// Pass
		}
	}
	
}
