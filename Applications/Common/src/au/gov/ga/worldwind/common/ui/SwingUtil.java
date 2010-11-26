package au.gov.ga.worldwind.common.ui;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/**
 * Utilities for working with Swing GUIs
 */
public class SwingUtil
{
	/**
	 * Invokes the provided runnable task on the EDT. Use to ensure 
	 * GUI updates are performed on the EDT, where they should be.
	 * 
	 * @throws SwingEDTException if an exception occurs while executing the provided
	 * task on the EDT. This exception is unchecked and contains the original cause.
	 */
	public static void invokeTaskOnEDT(Runnable task) throws SwingEDTException
	{
		try
		{
			if (SwingUtilities.isEventDispatchThread())
			{
				task.run();
			}
			else
			{
				SwingUtilities.invokeAndWait(task);
			}
		}
		catch (InvocationTargetException e)
		{
			throw new SwingEDTException(e.getCause());
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
