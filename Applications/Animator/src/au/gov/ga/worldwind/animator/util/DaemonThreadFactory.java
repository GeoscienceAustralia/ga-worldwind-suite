package au.gov.ga.worldwind.animator.util;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} that yields low-priority daemon threads.
 */
public class DaemonThreadFactory implements ThreadFactory
{
	public Thread newThread(Runnable r)
	{
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		return thread;
	}
}
