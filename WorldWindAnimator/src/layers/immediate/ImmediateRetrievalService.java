package layers.immediate;

import gov.nasa.worldwind.retrieve.RetrievalFuture;
import gov.nasa.worldwind.retrieve.Retriever;
import nasa.worldwind.retrieve.BasicRetrievalService;

public class ImmediateRetrievalService extends BasicRetrievalService
{
	@Override
	public synchronized RetrievalFuture runRetriever(Retriever retriever,
			double priority)
	{
		if (ImmediateMode.isImmediate())
		{
			RetrievalTask task = new RetrievalTask(retriever, priority);
			task.run();
			return task;
		}
		return super.runRetriever(retriever, priority);
	}
}
