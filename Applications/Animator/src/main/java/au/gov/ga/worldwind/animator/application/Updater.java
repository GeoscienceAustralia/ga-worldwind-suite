/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
