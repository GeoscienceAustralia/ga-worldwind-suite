package au.gov.ga.worldwind.animator.ui.parametereditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.geom.Point2D;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.ui.parametereditor.ParameterCurveModel.ParameterCurveModelListener;

/**
 * Unit tests for the {@link ParameterCurve} class
 */
public class ParameterCurveTest
{
	private static final double ALLOWABLE_ERROR = 0.000001;
	
	private Mockery mockContext;
	
	private ParameterCurve classToBeTested;
	
	private ParameterCurveModel curveModel;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		
		curveModel = mockContext.mock(ParameterCurveModel.class);
		mockContext.checking(new Expectations(){{
			allowing(curveModel).addListener(with(any(ParameterCurveModelListener.class)));
		}});
		
		classToBeTested = new ParameterCurve(curveModel);
	}
	// Note: Screen coordinates [0,0] is top-left, while curve coordinates are bottom-left
	
	@Test
	public void testConvertScreenPointToCurvePoint1To1()
	{
		classToBeTested.setSize(100, 100);
		classToBeTested.setCurveBounds(0, 100, 0, 100);
		
		// [50,50] -> [50,50]
		Point2D.Double screenPoint = new Point2D.Double(50, 50);
		ParameterCurvePoint curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// [0,50] -> [0,50]
		screenPoint = new Point2D.Double(0, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(0, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// [100,50] -> [100,50]
		screenPoint = new Point2D.Double(100, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// [50,0] -> [50,100]
		screenPoint = new Point2D.Double(50, 0);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// [50,100] -> [50,0]
		screenPoint = new Point2D.Double(50, 100);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(0, curvePoint.value, ALLOWABLE_ERROR);
	}
	
	@Test
	public void testConvertScreenPointToCurvePoint2To1()
	{
		classToBeTested.setSize(200, 200);
		classToBeTested.setCurveBounds(0, 100, 0, 100);
		
		// [100,100] -> [50,50]
		Point2D.Double screenPoint = new Point2D.Double(100, 100);
		ParameterCurvePoint curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// [0,100] -> [0,50]
		screenPoint = new Point2D.Double(0, 100);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(0, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// [200,100] -> [100,50]
		screenPoint = new Point2D.Double(200, 100);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// [100,0] -> [50,100]
		screenPoint = new Point2D.Double(100, 0);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// [100,200] -> [50,0]
		screenPoint = new Point2D.Double(100, 200);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(0, curvePoint.value, ALLOWABLE_ERROR);
	}
	
	
	@Test
	public void testConvertScreenPointToCurvePoint1To2()
	{
		classToBeTested.setSize(100, 100);
		classToBeTested.setCurveBounds(0, 200, 0, 200);
		
		// [50,50] -> [100,100]
		Point2D.Double screenPoint = new Point2D.Double(50, 50);
		ParameterCurvePoint curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// [0,50] -> [0,100]
		screenPoint = new Point2D.Double(0, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(0, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// [100,50] -> [200,100]
		screenPoint = new Point2D.Double(100, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(200, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// [50,0] -> [100,200]
		screenPoint = new Point2D.Double(50, 0);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(200, curvePoint.value, ALLOWABLE_ERROR);
		
		// [50,100] -> [100,0]
		screenPoint = new Point2D.Double(50, 100);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(0, curvePoint.value, ALLOWABLE_ERROR);
	}
	
	@Test
	public void testConvertScreenPointToCurvePointWithOffset()
	{
		classToBeTested.setSize(100, 100);
		classToBeTested.setCurveBounds(50, 150, 50, 150);
		
		// [50,50] -> [100,100]
		Point2D.Double screenPoint = new Point2D.Double(50, 50);
		ParameterCurvePoint curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// [0,50] -> [50,100]
		screenPoint = new Point2D.Double(0, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// [100,50] -> [150,100]
		screenPoint = new Point2D.Double(100, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(150, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// [50,0] -> [100,150]
		screenPoint = new Point2D.Double(50, 0);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(150, curvePoint.value, ALLOWABLE_ERROR);
		
		// [50,100] -> [100,50]
		screenPoint = new Point2D.Double(50, 100);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
	}
}
