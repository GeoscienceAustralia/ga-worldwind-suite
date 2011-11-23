package au.gov.ga.worldwind.animator.panels.objectproperties;

import static org.junit.Assert.*;

import javax.swing.JTextField;

import gov.nasa.worldwind.avlist.AVList;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.application.LAFConstants;

/**
 * Unit tests for the {@link EditableParameterInputVerifier} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class EditableParameterInputVerifierTest
{

	private JTextField textField;
	
	private EditableParameterInputVerifier classToBeTested;

	private boolean result;
	
	@Before
	public void setup()
	{
		textField = new JTextField();
	}
	
	@Test
	public void testWithBoundParameterEmptyString()
	{
		initialiseWithBoundParameter();
		setTextValue("");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedInvalid();
	}
	
	@Test
	public void testWithBoundParameterBlankString()
	{
		initialiseWithBoundParameter();
		setTextValue("   ");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedInvalid();
	}
	
	@Test
	public void testWithBoundParameterInvalidString()
	{
		initialiseWithBoundParameter();
		setTextValue("Not a number");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedInvalid();
	}
	
	@Test
	public void testWithBoundParameterBelowMinBoundString()
	{
		initialiseWithBoundParameter();
		setTextValue("-0.1");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedInvalid();
	}
	
	@Test
	public void testWithBoundParameterAboveMaxBoundString()
	{
		initialiseWithBoundParameter();
		setTextValue("10.1");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedInvalid();
	}
	
	@Test
	public void testWithBoundParameterNearMinBoundString()
	{
		initialiseWithBoundParameter();
		setTextValue("0.001");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedValid();
	}
	
	@Test
	public void testWithBoundParameterNearMaxBoundString()
	{
		initialiseWithBoundParameter();
		setTextValue("9.999");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedValid();
	}
	
	@Test
	public void testWithBoundParameterOnMinBoundString()
	{
		initialiseWithBoundParameter();
		setTextValue("0.0");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedValid();
	}
	
	@Test
	public void testWithBoundParameterOnMaxBoundString()
	{
		initialiseWithBoundParameter();
		setTextValue("10.0");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedValid();
	}
	
	@Test
	public void testWithUnboundParameterEmptyString()
	{
		initialiseWithUnboundParameter();
		setTextValue("");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedInvalid();
	}
	
	@Test
	public void testWithUnboundParameterBlankString()
	{
		initialiseWithUnboundParameter();
		setTextValue("   ");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedInvalid();
	}
	
	@Test
	public void testWithUnboundParameterInvalidString()
	{
		initialiseWithUnboundParameter();
		setTextValue("Not a number");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedInvalid();
	}
	
	@Test
	public void testWithUnboundParameterValidPositiveString()
	{
		initialiseWithUnboundParameter();
		setTextValue("123.45");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedValid();
	}
	
	@Test
	public void testWithUnboundParameterValidNegativeString()
	{
		initialiseWithUnboundParameter();
		setTextValue("-123.45");
		
		result = classToBeTested.verify(textField);
		
		assertFieldMarkedValid();
	}

	private void assertFieldMarkedInvalid()
	{
		assertEquals(false, result);
		assertEquals(LAFConstants.getInvalidFieldColor(), textField.getBackground());
	}
	
	private void assertFieldMarkedValid()
	{
		assertEquals(true, result);
		assertEquals(LAFConstants.getValidFieldColor(), textField.getBackground());
	}
	
	private void initialiseWithBoundParameter()
	{
		classToBeTested = new EditableParameterInputVerifier(new BoundParameter());
	}
	
	private void initialiseWithUnboundParameter()
	{
		classToBeTested = new EditableParameterInputVerifier(new UnboundParameter());
	}
	
	private void setTextValue(String textValue)
	{
		textField.setText(textValue);
	}
	
	@EditableParameter(bound = true, minValue = 0.0, maxValue = 10.0)
	private static final class BoundParameter extends ParameterBase
	{
		private static final long serialVersionUID = 20100920L;
		@Override
		public ParameterValue getCurrentValue(AnimationContext context){ return null;}
		@Override
		protected void doApplyValue(double value) {}
		@Override
		protected ParameterBase createParameter(AVList context){ return null;}
		@Override
		protected String getXmlElementName(AnimationIOConstants constants){ return null;}
	}
	
	@EditableParameter(bound = false, minValue = 0.0, maxValue = 10.0)
	private static final class UnboundParameter extends ParameterBase
	{
		private static final long serialVersionUID = 20100920L;
		@Override
		public ParameterValue getCurrentValue(AnimationContext context){ return null;}
		@Override
		protected void doApplyValue(double value) {}
		@Override
		protected ParameterBase createParameter(AVList context){ return null;}
		@Override
		protected String getXmlElementName(AnimationIOConstants constants){ return null;}
	}
	
}
