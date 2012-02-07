package au.gov.ga.worldwind.common.util.exaggeration;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.render.DrawContext;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link DefaultVerticalExaggerationServiceImpl} class
 */
public class DefaultVerticalExaggerationServiceImplTest
{
	
	private static final double DELTA = 0.0001;
	private Mockery mockContext;
	private DrawContext dc;
	private DefaultVerticalExaggerationServiceImpl classUnderTest;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		dc = mockContext.mock(DrawContext.class);
		classUnderTest = new DefaultVerticalExaggerationServiceImpl();
	}
	
	@Test
	public void testApplyWithZeroExaggeration()
	{
		setDrawContextExaggeration(0);
		assertEquals(0d, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testApplyWithPositiveExaggeration()
	{
		setDrawContextExaggeration(10);
		assertEquals(1000d, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testApplyWithNegativeExaggeration()
	{
		setDrawContextExaggeration(-10);
		assertEquals(-1000d, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testApplyWithFractionalExaggeration()
	{
		setDrawContextExaggeration(0.2);
		assertEquals(20, classUnderTest.applyVerticalExaggeration(dc, 100), DELTA);
	}
	
	@Test
	public void testGetGlobalExaggeration()
	{
		setDrawContextExaggeration(10);
		assertEquals(10, classUnderTest.getGlobalVerticalExaggeration(dc), DELTA);
	}
	
	private void setDrawContextExaggeration(final double exaggeration)
	{
		mockContext.checking(new Expectations(){{
			allowing(dc).getVerticalExaggeration(); will(returnValue(exaggeration));
		}});
	}
	
}
