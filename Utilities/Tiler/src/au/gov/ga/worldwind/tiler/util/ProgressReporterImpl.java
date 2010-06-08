package au.gov.ga.worldwind.tiler.util;

import java.util.logging.Logger;

public abstract class ProgressReporterImpl implements ProgressReporter
{
	private boolean cancelled = false;
	private final Logger logger;

	public ProgressReporterImpl(Logger logger)
	{
		this.logger = logger;
	}

	public void cancel()
	{
		cancelled = true;
	}

	public Logger getLogger()
	{
		return logger;
	}

	public boolean isCancelled()
	{
		return cancelled;
	}
}
