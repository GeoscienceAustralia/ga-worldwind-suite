/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.util;

import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A simple helper class that can be used to control exception logging.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ExceptionLogger
{
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
	private static BlockingQueue<ExceptionOccurance> queuedExceptions = new LinkedBlockingQueue<ExceptionOccurance>();
	
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
					ExceptionOccurance occurance = queuedExceptions.take();
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
				catch (InterruptedException e)
				{
					// Keep on going...
				}
			}
		}
	};
	static
	{
		loggingThread.setDaemon(true);
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
