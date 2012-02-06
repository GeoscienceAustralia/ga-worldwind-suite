package au.gov.ga.worldwind.animator.application;

import java.util.HashSet;
import java.util.Set;

import au.gov.ga.worldwind.animator.util.ExceptionLogger;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Used to update the animation outside of the render thread.
 * <p/>
 * New key frames are queued on a waiting queue, and then applied to the animation asynchronously.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Updater
{
	private Set<Integer> toApply = new HashSet<Integer>();
	private Object waiter = new Object();

	private Animator animator;
	
	public Updater(Animator targetAnimator)
	{
		Validate.notNull(targetAnimator, "An animator must be provided");
		animator = targetAnimator;
		
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				while (true)
				{
					Integer key = getNextKey();
					if (key != null)
					{
						animator.getCurrentAnimation().recordKeyFrame(key);
						animator.updateSlider();
						removeValue(key);
					}
					else
					{
						synchronized (waiter)
						{
							try
							{
								waiter.wait();
							}
							catch (InterruptedException e)
							{
								ExceptionLogger.logException(e);
							}
						}
					}
				}
			}
		};
		thread.setName("Animator Updator Thread");
		thread.setDaemon(true);
		thread.start();
	}

	private synchronized Integer getNextKey()
	{
		if (toApply.isEmpty())
		{
			return null;
		}
		return toApply.iterator().next();
	}

	private synchronized void removeValue(Integer key)
	{
		toApply.remove(key);
	}

	public synchronized void addFrame(int frame)
	{
		toApply.add(frame);
		synchronized (waiter)
		{
			waiter.notify();
		}
	}
}