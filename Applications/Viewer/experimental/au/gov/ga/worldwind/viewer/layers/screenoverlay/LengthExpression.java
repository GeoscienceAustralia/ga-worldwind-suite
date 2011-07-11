package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Represents a length expression.
 * <p/>
 * Lengths can be expressed as an absolute value in pixels ("px") or
 * as a percentage of total screen size ("%").
 */
public class LengthExpression
{
	private float value;
	private ExpressionType type = ExpressionType.ABSOLUTE;
	
	private static final Pattern VALID_EXPRESSION = Pattern.compile("([\\d]*\\.[\\d]+|[\\d]+)(px|%)?");
	
	/**
	 * Create a new length expression from the provided expression string.
	 * <p/>
	 * Valid expressions are:
	 * <ul>
	 * 	<li>"Npx" - Create an absolute length of N pixels
	 * 	<li>"N%" - Create a relative length of N % of total size
	 * </ul>
	 * If no suffix is provided, the value is interpreted as an absolute pixel value.
	 * 
	 * @see #getLength(float)
	 */
	public LengthExpression(String expression)
	{
		Validate.notBlank(expression, "A length expression is required.");
		
		Matcher matcher = VALID_EXPRESSION.matcher(expression.trim());
		Validate.isTrue(matcher.matches(), "Invalid expression. Expected an expression of the form [Npx | N% | N]");
		
		this.value = Float.parseFloat(matcher.group(1));
		if (matcher.groupCount() == 2)
		{
			type = ExpressionType.forSuffix(matcher.group(2));
		}
	}
	
	/**
	 * @return The length in pixels of this expression evaluated against the provided screen size value
	 */
	public float getLength(float screenSize)
	{
		if (type == ExpressionType.PERCENTAGE)
		{
			return (value / 100) * screenSize;
		}
		return value;
	}
	
	private static enum ExpressionType
	{
		PERCENTAGE,
		ABSOLUTE;
		
		public static ExpressionType forSuffix(String suffix)
		{
			if (Util.isBlank(suffix) || suffix.equalsIgnoreCase("px"))
			{
				return ABSOLUTE;
			}
			if (suffix.equalsIgnoreCase("%"))
			{
				return PERCENTAGE;
			}
			return null;
		}
	}
}
