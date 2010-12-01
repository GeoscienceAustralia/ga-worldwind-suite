package au.gov.ga.worldwind.common.downloader;

import au.gov.ga.worldwind.common.util.MetersStatusBar;

public class DownloaderStatusBar extends MetersStatusBar
{
	@Override
	protected boolean anyActiveRetrievalTasks()
	{
		return super.anyActiveRetrievalTasks() || Downloader.getRetrievalService().hasActiveTasks();
	}
}
