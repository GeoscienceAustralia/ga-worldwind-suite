package au.gov.ga.worldwind.tiler.util;

import java.util.logging.Logger;

public class SimpleProgressReporter extends ProgressReporterImpl
{
	public SimpleProgressReporter()
	{
		super(Logger.getAnonymousLogger());
	}

	public void done()
	{
		System.out.println("Done");
	}

	public void progress(double percent)
	{
	}
}
