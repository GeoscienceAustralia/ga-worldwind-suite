package au.gov.ga.worldwind.tiler.util;

import java.util.logging.Logger;

public interface ProgressReporter
{
	public Logger getLogger();

	public void progress(double percent);

	public boolean isCancelled();

	public void cancel();

	public void done();
}
