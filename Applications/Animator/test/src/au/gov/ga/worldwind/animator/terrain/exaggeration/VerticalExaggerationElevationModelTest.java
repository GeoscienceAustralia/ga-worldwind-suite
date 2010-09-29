package au.gov.ga.worldwind.animator.terrain.exaggeration;

import static org.junit.Assert.*;
import gov.nasa.worldwind.globes.ElevationModel;

import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link VerticalExaggerationElevationModel} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class VerticalExaggerationElevationModelTest
{
	private static final double ALLOWABLE_ERROR = 0.0001;

	private Mockery mockContext;
	
	private VerticalExaggerationElevationModel classToBeTested;

	private ElevationModel source;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		
		source = mockContext.mock(ElevationModel.class);
		
		classToBeTested = new VerticalExaggerationElevationModel(source);
	}
	
	@Test
	public void testReturnsInputWhenNoExaggerators()
	{
		assertEquals(123.45, classToBeTested.exaggerateElevation(123.45), ALLOWABLE_ERROR);
	}
	
	@Test
	public void testAppliesSingleExaggerationAt0()
	{
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(2.0, 0.0));
		
		double[] inputs = new double[]{44.4, 0.0, -0.5};
		double[] expect = new double[]{88.8, 0.0, -0.5};
		
		assertExaggerationsCorrect(inputs, expect);
	}
	
	@Test
	public void testAppliesMultipleExaggeratorsAllPositive()
	{
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(3.0, 40.0));
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(1.0, 20.0));
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(2.0, 00.0));
		
		double[] inputs = new double[]{-20.0, -10.0, -08.0, -03.0, 00.0, 10.0, 20.0, 30.0, 40.0, 50.0};
		double[] expect = new double[]{-20.0, -10.0, -08.0, -03.0, 00.0, 20.0, 40.0, 50.0, 60.0, 90.0};
		
		assertExaggerationsCorrect(inputs, expect);
	}
	
	@Test
	public void testAppliesMultipleExaggeratorsWithNegative()
	{
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(3.0, 40.0));
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(1.0, 20.0));
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(2.0, 00.0));
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(2.0, -05.0));
		classToBeTested.addExaggerator(new ElevationExaggerationImpl(5.0, -10.0));
		
		double[] inputs = new double[]{-20.0, -10.0, -08.0, -03.0, 00.0, 10.0, 20.0, 30.0, 40.0, 50.0};
		double[] expect = new double[]{-45.0, -35.0, -25.0, -06.0, 00.0, 20.0, 40.0, 50.0, 60.0, 90.0};
		
		assertExaggerationsCorrect(inputs, expect);
	}

	
	private void assertExaggerationsCorrect(double[] inputs, double[] expect)
	{
		double[] result = new double[inputs.length];
		for (int i = 0; i < inputs.length; i++)
		{
			result[i] = classToBeTested.exaggerateElevation(inputs[i]);
		}
		
		for (int i = 0; i < inputs.length; i++)
		{
			assertEquals(expect[i], result[i], ALLOWABLE_ERROR);
		}
	}
}
