package au.gov.ga.worldwind.animator.util;

import java.io.PrintStream;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A simple helper class that can be used to control exception logging.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class ExceptionLogger
{
	/**
	 * The period between polls of the exception queue. Defaults to 1 second.
	 */
	private static final long POLLING_PERIOD = 1000;
	
	/**
	 * The target stream to log to. Defaults to <code>System.err</code>
	 */
	private static PrintStream targetStream = System.err;
	
	/**
	 * The logging level to use. Defaults to <code>DETAILED</code>
	 */
	private static Level loggingLevel = Level.DETAILED;
	
	/**
	 * The queue of exceptions waiting to be logged
	 */
	private static Queue<ExceptionOccurance> queuedExceptions = new ConcurrentLinkedQueue<ExceptionOccurance>();
	
	/**
	 * The thread that performs the logging
	 */
	private static Thread loggingThread = new Thread()
	{
		@Override
		public void run() {
			while(true)
			{
				try
				{
					// Poll the queue for an exception to log
					ExceptionOccurance occurance = null;
					while ((occurance = queuedExceptions.poll()) != null)
					{
						targetStream.println(">> Exception occurred at " + occurance.date);
						
						if (loggingLevel == Level.DETAILED)
						{
							occurance.exception.printStackTrace(targetStream);
						}
						else 
						{
							targetStream.println(occurance.exception.getMessage());
						}
					}
					// If there are no entries, sleep for a bit
					targetStream.flush();
					sleep(POLLING_PERIOD);
				}
				catch (InterruptedException e)
				{
					// Keep on going...
				}
			}
		}
	};
	static
	{
		loggingThread.start();
	}
	
	/**
	 * Log the provided exception
	 * 
	 * @param t The exception to log
	 */
	public static void logException(Throwable t)
	{
		if (t == null)
		{
			return;
		}
		
		if (loggingLevel == Level.OFF)
		{
			return;
		}
		
		queuedExceptions.add(new ExceptionOccurance(t, new Date()));
	}
	
	/**
	 * An enumeration of logging levels.
	 * <ul>
	 * 	<li><code>OFF</code> - No exception logging performed
	 * 	<li><code>SUMMARY</code> - Only summary details logged (e.g. exception message)
	 * 	<li><code>DETAILED</code> - All exception details logged
	 * </ul>
	 */
	public static enum Level
	{
		OFF, SUMMARY, DETAILED;
	}
	
	/**
	 * A container class for an exception occurrence
	 */
	private static class ExceptionOccurance
	{
		public Throwable exception;
		public Date date;

		public ExceptionOccurance(Throwable t, Date d)
		{
			exception = t;
			date = d;
		}
	}

	/**
	 * @param targetStream the targetStream to set
	 */
	public static void setTargetStream(PrintStream targetStream)
	{
		ExceptionLogger.targetStream = targetStream;
	}

	/**
	 * @param loggingLevel the loggingLevel to set
	 */
	public static void setLoggingLevel(Level loggingLevel)
	{
		ExceptionLogger.loggingLevel = loggingLevel;
	}
}
