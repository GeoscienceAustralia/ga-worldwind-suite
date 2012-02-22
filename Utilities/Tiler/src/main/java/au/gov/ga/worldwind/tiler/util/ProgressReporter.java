package au.gov.ga.worldwind.tiler.util;

import java.util.logging.Logger;

/**
 * Interface representing an object that can report tiling progress.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ProgressReporter
{
	/**
	 * @return {@link Logger} to log to
	 */
	public Logger getLogger();

	/**
	 * Update the progress to the given percent.
	 * 
	 * @param percent
	 */
	public void progress(double percent);

	/**
	 * @return Has the user cancelled the tiling process?
	 */
	public boolean isCancelled();

	/**
	 * Cancel the tiling process.
	 */
	public void cancel();

	/**
	 * Called when the tiling process is finished.
	 */
	public void done();
}
