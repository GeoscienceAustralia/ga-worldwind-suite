package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.Logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import au.gov.ga.worldwind.common.util.DirectDraw;
import au.gov.ga.worldwind.common.util.NativeJOGLLibs;

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
			handler.setFormatter(new SimpleFormatter());
			//log at INFO level
			handler.setLevel(Level.INFO);
			Logging.logger().addHandler(handler);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		//redirect standard out and standard error to the logger
		Logger logger = Logger.getLogger("stdout");
		LoggingOutputStream los = new LoggingOutputStream(logger, StdOutErrLevel.STDOUT);
		System.setOut(new PrintStream(los, true));

		logger = Logger.getLogger("stderr");
		los = new LoggingOutputStream(logger, StdOutErrLevel.STDERR);
		System.setErr(new PrintStream(los, true));


		//setup the libraries
		if (Configuration.isWindowsOS())
		{
			DirectDraw.disableDirectDraw();
		}
		NativeJOGLLibs.init();

		//start the animator
		Logging.logger().info("Starting Animator");
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
