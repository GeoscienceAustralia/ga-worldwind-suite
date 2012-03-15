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
package au.gov.ga.worldwind.animator.application.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * An {@link AnimationEventListener} that logs animation events asynchronously
 * as it receives them.
 * <p/>
 * Used to log events for use in debug analysis.
 *
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimationEventLogger implements AnimationEventListener
{
	private final FileWriter logWriter;
	private final File logFile;
	
	private boolean enabled = true;
	
	private BlockingQueue<EventOccurance> queuedEvents = new LinkedBlockingQueue<EventOccurance>();

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd hh:mm:ss:SS");
	private Thread loggingThread = new Thread()
	{
		@Override
		public void run() {
			while(true)
			{
				try
				{
					// Poll the queue for an event to log
					EventOccurance occurance = queuedEvents.take();
					logWriter.write(">> Event occurred at " + DATE_FORMATTER.format(occurance.timestamp) + ": " + occurance.event + "\n");
					logWriter.flush();
				}
				catch (InterruptedException e)
				{
					// Keep on going...
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	};
	
	public AnimationEventLogger(String fileName)
	{
		Validate.notBlank(fileName, "A filename is required");
		try
		{
			logFile = new File(fileName);
			logWriter = new FileWriter(logFile, true);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Filename not valid", e);
		}
		loggingThread.setName("Animation Event Logger");
		loggingThread.setDaemon(true);
		loggingThread.start();
	}
	
	@Override
	public void receiveAnimationEvent(AnimationEvent event)
	{
		if (enabled)
		{
			queuedEvents.add(new EventOccurance(event, new Date()));
		}
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	private static class EventOccurance 
	{
		AnimationEvent event;
		Date timestamp;
		
		EventOccurance(AnimationEvent event, Date timestamp)
		{
			this.event = event;
			this.timestamp = timestamp;
		}
	}
}
