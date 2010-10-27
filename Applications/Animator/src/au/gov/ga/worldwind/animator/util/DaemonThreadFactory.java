package au.gov.ga.worldwind.animator.util;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} that yields low-priority daemon threads.
 */
public class DaemonThreadFactory implements ThreadFactory
{
	private String threadName = "Daemon Thread";
	
	public DaemonThreadFactory()
	{
	}
	
	public DaemonThreadFactory(String threadName)
	{
		this.threadName = threadName;
	}

	public Thread newThread(Runnable r)
	{
		return newThread(r, threadName);
	}
	
	public static Thread newThread(Runnable r, String threadName)
	{
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setName(threadName + "-" + thread.getId());
		return thread;
	}
}
