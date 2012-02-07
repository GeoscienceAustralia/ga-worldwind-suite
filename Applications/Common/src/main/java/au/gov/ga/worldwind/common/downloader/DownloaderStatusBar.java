package au.gov.ga.worldwind.common.downloader;

import gov.nasa.worldwind.util.StatusBar;
import au.gov.ga.worldwind.common.util.MetersStatusBar;

/**
 * {@link StatusBar} subclass that displays the downloading message when the
 * {@link Downloader} retrieval service has active tasks.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DownloaderStatusBar extends MetersStatusBar
{
	@Override
	protected boolean anyActiveRetrievalTasks()
	{
		return super.anyActiveRetrievalTasks() || Downloader.getRetrievalService().hasActiveTasks();
	}
}
