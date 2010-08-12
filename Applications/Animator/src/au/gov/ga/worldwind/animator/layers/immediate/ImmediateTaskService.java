package au.gov.ga.worldwind.animator.layers.immediate;

import gov.nasa.worldwind.util.ThreadedTaskService;

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
