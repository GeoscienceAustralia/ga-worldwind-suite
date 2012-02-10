package au.gov.ga.worldwind.common.ui.lazytree;

/**
 * Thrown by the load() method when lazy loading tree nodes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LazyLoadException extends Exception
{
	public LazyLoadException(String message)
	{
		super(message);
	}

	public LazyLoadException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
