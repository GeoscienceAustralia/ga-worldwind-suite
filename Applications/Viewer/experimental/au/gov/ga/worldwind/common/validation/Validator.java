package au.gov.ga.worldwind.common.validation;

import java.util.List;

/**
 * A simple validator interface
 */
public interface Validator<E>
{
	/**
	 * Perform validation of the provided object.
	 * <p/>
	 * Result should be a list of {@link ValidationError} objects.
	 * <p/>
	 * If no errors occur, the result should be an empty list.
	 */
	List<ValidationError> validate(E object);
}
