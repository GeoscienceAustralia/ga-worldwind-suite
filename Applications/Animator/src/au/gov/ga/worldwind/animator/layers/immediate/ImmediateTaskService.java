package au.gov.ga.worldwind.animator.layers.immediate;

import gov.nasa.worldwind.util.TaskService;
import gov.nasa.worldwind.util.ThreadedTaskService;

/**
 * {@link TaskService} that runs tasks immediately when in immediate mode.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImmediateTaskService extends ThreadedTaskService
{
	@Override
	public synchronized void addTask(Runnable runnable)
	{
		if (ImmediateMode.isImmediate())
		{
			runnable.run();
			return;
		}
		super.addTask(runnable);
	}
}
