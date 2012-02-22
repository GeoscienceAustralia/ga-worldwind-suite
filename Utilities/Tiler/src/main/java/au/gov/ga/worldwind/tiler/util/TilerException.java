package au.gov.ga.worldwind.tiler.util;

/**
 * Exception subclass raised when a Tiler error occurs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TilerException extends Exception
{
	public TilerException(String message)
	{
		super(message);
	}
}
