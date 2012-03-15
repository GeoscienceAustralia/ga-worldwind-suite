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
package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.Logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import au.gov.ga.worldwind.common.util.DirectDraw;
import au.gov.ga.worldwind.common.util.NativeLibraries;

/**
 * Main class that runs the {@link Animator} as an executable. This class saves
 * the stdout to a log file, and also initializes native libraries.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Executable
{
	public static void main(String[] args)
	{
		// initialize logging to go to rolling log file
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();

		// set the logging handler
		try
		{
			Handler handler = new FileHandler("animator%u.log", true);
			handler.setFormatter(new OneLineFormatter());
			//log at INFO level
			handler.setLevel(Level.INFO);
			Logging.logger().addHandler(handler);
			Logging.logger().addHandler(new ConsoleHandler());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		//redirect standard out and standard error to the logger
		LoggingOutputStream los = new LoggingOutputStream(Logging.logger(), StdOutErrLevel.STDOUT);
		System.setOut(new PrintStream(los, true));

		los = new LoggingOutputStream(Logging.logger(), StdOutErrLevel.STDERR);
		System.setErr(new PrintStream(los, true));


		//setup the libraries
		if (Configuration.isWindowsOS())
		{
			DirectDraw.disableDirectDraw();
		}
		NativeLibraries.init();

		//start the animator
		System.out.println("Starting Animator");
		Animator.main(args);
	}

	private static class LoggingOutputStream extends ByteArrayOutputStream
	{
		private String lineSeparator;
		private Logger logger;
		private Level level;

		public LoggingOutputStream(Logger logger, Level level)
		{
			super();
			this.logger = logger;
			this.level = level;
			lineSeparator = System.getProperty("line.separator");
		}

		public void flush() throws IOException
		{
			synchronized (this)
			{
				super.flush();
				String record = this.toString();
				super.reset();

				if (record.length() == 0 || record.equals(lineSeparator))
				{
					// avoid empty records 
					return;
				}

				logger.logp(level, "", "", record);
			}
		}
	}

	private static class OneLineFormatter extends SimpleFormatter
	{
		private Date date = new Date();
		private final static String format = "{0,date} {0,time}";
		private MessageFormat formatter;
		private String lineSeparator = System.getProperty("line.separator");
		private Object args[] = new Object[1];

		@Override
		public String format(LogRecord record)
		{
			if (record.getLevel() != StdOutErrLevel.STDOUT && record.getLevel() != StdOutErrLevel.STDERR)
			{
				return super.format(record);
			}

			StringBuffer sb = new StringBuffer();
			// Minimize memory allocations here.
			date.setTime(record.getMillis());
			args[0] = date;
			StringBuffer text = new StringBuffer();
			if (formatter == null)
			{
				formatter = new MessageFormat(format);
			}
			formatter.format(args, text, null);
			sb.append(text);
			sb.append(" ");
			String message = formatMessage(record);
			sb.append(record.getLevel().getLocalizedName());
			sb.append(": ");
			sb.append(message);
			sb.append(lineSeparator);
			if (record.getThrown() != null)
			{
				try
				{
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					sb.append(sw.toString());
				}
				catch (Exception ex)
				{
				}
			}
			return sb.toString();
		}
	}

	private static class StdOutErrLevel extends Level
	{
		public static Level STDOUT = new StdOutErrLevel("STDOUT", Level.INFO.intValue() + 53);
		public static Level STDERR = new StdOutErrLevel("STDERR", Level.INFO.intValue() + 54);

		private StdOutErrLevel(String name, int value)
		{
			super(name, value);
		}

		/**
		 * Method to avoid creating duplicate instances when deserializing the
		 * object.
		 * 
		 * @return the singleton instance of this <code>Level</code> value in
		 *         this classloader
		 * @throws ObjectStreamException
		 *             If unable to deserialize
		 */
		protected Object readResolve() throws ObjectStreamException
		{
			if (this.intValue() == STDOUT.intValue())
				return STDOUT;
			if (this.intValue() == STDERR.intValue())
				return STDERR;
			throw new InvalidObjectException("Unknown instance :" + this);
		}
	}
}
