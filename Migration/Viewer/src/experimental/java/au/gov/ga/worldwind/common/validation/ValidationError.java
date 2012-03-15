package au.gov.ga.worldwind.common.validation;

/**
 * Represents a validation error that might be generated during validation
 * of user input etc.
 */
public interface ValidationError
{
	String getCode();
	String getTitle();
	String getMessage();
	Object getSource();
}
