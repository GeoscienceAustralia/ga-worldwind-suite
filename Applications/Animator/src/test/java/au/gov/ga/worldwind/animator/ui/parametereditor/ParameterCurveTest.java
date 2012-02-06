package au.gov.ga.worldwind.animator.ui.parametereditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.geom.Point2D;
import java.util.ArrayList;

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
			allowing(curveModel).getKeyFrameNodes(); will(returnValue(new ArrayList<ParameterCurveKeyNode>()));
		}});
		
		classToBeTested = new ParameterCurve(curveModel);
	}
	// Note: Screen coordinates [0,0] is top-left, while curve coordinates are bottom-left
	
	@Test
	public void testConvertScreenPointToCurvePoint1To1()
	{
		classToBeTested.setSize(100, 100);
		classToBeTested.setCurveBounds(0, 100, 0, 100);
		
		// S[50,50] -> C[50,50]
		Point2D.Double screenPoint = new Point2D.Double(50, 50);
		ParameterCurvePoint curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[0,50] -> C[0,50]
		screenPoint = new Point2D.Double(0, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(0, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[100,50] -> C[100,50]
		screenPoint = new Point2D.Double(100, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[50,0] -> C[50,100]
		screenPoint = new Point2D.Double(50, 0);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[50,100] -> C[50,0]
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
		
		// S[100,100] -> C[50,50]
		Point2D.Double screenPoint = new Point2D.Double(100, 100);
		ParameterCurvePoint curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[0,100] -> C[0,50]
		screenPoint = new Point2D.Double(0, 100);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(0, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[200,100] -> C[100,50]
		screenPoint = new Point2D.Double(200, 100);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[100,0] -> C[50,100]
		screenPoint = new Point2D.Double(100, 0);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[100,200] -> C[50,0]
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
		
		// S[50,50] -> C[100,100]
		Point2D.Double screenPoint = new Point2D.Double(50, 50);
		ParameterCurvePoint curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[0,50] -> C[0,100]
		screenPoint = new Point2D.Double(0, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(0, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[100,50] -> C[200,100]
		screenPoint = new Point2D.Double(100, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(200, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[50,0] -> C[100,200]
		screenPoint = new Point2D.Double(50, 0);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(200, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[50,100] -> C[100,0]
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
		
		// S[50,50] -> C[100,100]
		Point2D.Double screenPoint = new Point2D.Double(50, 50);
		ParameterCurvePoint curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[0,50] -> C[50,100]
		screenPoint = new Point2D.Double(0, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(50, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[100,50] -> C[150,100]
		screenPoint = new Point2D.Double(100, 50);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(150, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(100, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[50,0] -> C[100,150]
		screenPoint = new Point2D.Double(50, 0);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(150, curvePoint.value, ALLOWABLE_ERROR);
		
		// S[50,100] -> C[100,50]
		screenPoint = new Point2D.Double(50, 100);
		curvePoint = classToBeTested.getCurvePoint(screenPoint);

		assertNotNull(curvePoint);
		assertEquals(100, curvePoint.frame, ALLOWABLE_ERROR);
		assertEquals(50, curvePoint.value, ALLOWABLE_ERROR);
	}
	
	@Test
	public void testConvertCurvePointToScreenPoint1To1()
	{
		classToBeTested.setCurveBounds(0, 100, 0, 100);
		classToBeTested.setSize(100, 100);
		
		// C[0,0] -> S[0,100]
		ParameterCurvePoint curvePoint = new ParameterCurvePoint(0, 0);
		Point2D.Double screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(0, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(100, screenPoint.y, ALLOWABLE_ERROR);
		
		// C[50,50] -> S[50,50]
		curvePoint = new ParameterCurvePoint(50, 50);
		screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(50, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(50, screenPoint.y, ALLOWABLE_ERROR);
		
		// C[100,100] -> S[100,0]
		curvePoint = new ParameterCurvePoint(100, 100);
		screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(100, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(0, screenPoint.y, ALLOWABLE_ERROR);
		
	}
	
	@Test
	public void testConvertCurvePointToScreenPoint2To1()
	{
		classToBeTested.setCurveBounds(0, 200, 0, 200);
		classToBeTested.setSize(100, 100);
		
		// C[0,0] -> S[0,100]
		ParameterCurvePoint curvePoint = new ParameterCurvePoint(0, 0);
		Point2D.Double screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(0, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(100, screenPoint.y, ALLOWABLE_ERROR);
		
		// C[100,100] -> S[50,50]
		curvePoint = new ParameterCurvePoint(100, 100);
		screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(50, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(50, screenPoint.y, ALLOWABLE_ERROR);
		
		// C[200,200] -> S[100,0]
		curvePoint = new ParameterCurvePoint(200, 200);
		screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(100, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(0, screenPoint.y, ALLOWABLE_ERROR);
	}
	
	@Test
	public void testConvertCurvePointToScreenPoint1To2()
	{
		classToBeTested.setCurveBounds(0, 100, 0, 100);
		classToBeTested.setSize(200, 200);
		
		// C[0,0] -> S[0,200]
		ParameterCurvePoint curvePoint = new ParameterCurvePoint(0, 0);
		Point2D.Double screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(0, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(200, screenPoint.y, ALLOWABLE_ERROR);
		
		// C[50,50] -> S[100,100]
		curvePoint = new ParameterCurvePoint(50, 50);
		screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(100, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(100, screenPoint.y, ALLOWABLE_ERROR);
		
		// C[100,100] -> S[200,0]
		curvePoint = new ParameterCurvePoint(100, 100);
		screenPoint = classToBeTested.getScreenPoint(curvePoint);
		
		assertNotNull(screenPoint);
		assertEquals(200, screenPoint.x, ALLOWABLE_ERROR);
		assertEquals(0, screenPoint.y, ALLOWABLE_ERROR);
	}
}
