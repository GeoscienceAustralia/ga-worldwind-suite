package au.gov.ga.worldwind.animator.application.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An {@link AnimationEventListener} that logs animation events asynchronously
 * as it receives them.
 * <p/>
 * Used to log events for use in debug analysis.
 *
 */
public class AnimationEventLogger implements AnimationEventListener
{
	private final FileWriter logWriter;
	
	private boolean enabled = true;
	
	private BlockingQueue<EventOccurance> queuedEvents = new LinkedBlockingQueue<EventOccurance>();

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
					logWriter.write(">> Event occurred at " + occurance.timestamp + ": " + occurance.event + "\n");
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
			logWriter = new FileWriter(fileName, true);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Filename not valid", e);
		}
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
