package au.gov.ga.worldwind.common.ui;

/**
 * A wrapper exception that allows checked exceptions from the EDT thread to
 * be wrapped in an unchecked exception so clients can decide if they want to 
 * deal with them or not.
 *
 */
public class SwingEDTException extends RuntimeException
{
	private static final long serialVersionUID = 20101126L;

	public SwingEDTException(Throwable cause)
	{
		super(cause);
	}
}
