package au.gov.ga.worldwind.common.ui.lazytree;

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
