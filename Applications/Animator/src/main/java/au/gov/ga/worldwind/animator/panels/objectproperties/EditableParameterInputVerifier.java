package au.gov.ga.worldwind.animator.panels.objectproperties;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * An input verifier for text fields linked to {@link Parameter}s marked as 'editable' via the {@link EditableParameter} annotation.
 * <p/>
 * Will highlight the field in error and prevent navigation from the field if:
 * <ul>
 * 	<li>The value is not a valid double
 *  <li>The value lies outside of the bounds of a 'bounded' parameter (marked using the {@link EditableParameter#bound()} field)
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class EditableParameterInputVerifier extends InputVerifier
{
	private Parameter parameterToVerifyAgainst;
	
	public EditableParameterInputVerifier(Parameter parameterToVerifyAgainst)
	{
		Validate.notNull(parameterToVerifyAgainst, "A parameter must be provided");
		this.parameterToVerifyAgainst = parameterToVerifyAgainst;
	}

	@Override
	public boolean verify(JComponent input)
	{
		// Only apply verification to text fields
		if (!(input instanceof JTextField))
		{
			return true;
		}
		
		String textValue = ((JTextField)input).getText();
		if (!isNumeric(textValue))
		{
			markAsInvalid(input);
			return false;
		}
		
		if (isBoundParameter())
		{
			double doubleValue = Double.parseDouble(textValue); 
			if (doubleValue < getParameterMinValue() || doubleValue > getParameterMaxValue())
			{
				markAsInvalid(input);
				return false;
			}
		}
		
		markAsValid(input);
		return true;
	}
	
	/**
	 * @return Whether the provided string can be converted via {@link Double#parseDouble(String)} to a {@link Double}
	 */
	private boolean isNumeric(String currentValue)
	{
		if (currentValue == null || currentValue.trim().isEmpty())
		{
			return false;
		}
		try
		{
			Double.parseDouble(currentValue.trim());
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		return true;
	}

	private void markAsInvalid(JComponent input)
	{
		input.setBackground(LAFConstants.getInvalidFieldColor());
	}
	
	private void markAsValid(JComponent input)
	{
		input.setBackground(LAFConstants.getValidFieldColor());
	}
	
	private boolean isBoundParameter()
	{
		return parameterToVerifyAgainst.getClass().isAnnotationPresent(EditableParameter.class) && 
				parameterToVerifyAgainst.getClass().getAnnotation(EditableParameter.class).bound();
	}
	
	private double getParameterMaxValue()
	{
		return parameterToVerifyAgainst.getClass().getAnnotation(EditableParameter.class).maxValue();
	}

	private double getParameterMinValue()
	{
		return parameterToVerifyAgainst.getClass().getAnnotation(EditableParameter.class).minValue();
	}
}