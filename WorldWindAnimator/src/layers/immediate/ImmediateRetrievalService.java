package layers.immediate;

import gov.nasa.worldwind.retrieve.RetrievalFuture;
import gov.nasa.worldwind.retrieve.Retriever;

import java.util.concurrent.ExecutionException;

public class ImmediateRetrievalService extends
		nasa.worldwind.retrieve.BasicRetrievalService
{
	public synchronized RetrievalFuture runRetriever(Retriever retriever,
			double priority)
	{
		RetrievalFuture future = super.runRetriever(retriever, priority);
		if (ImmediateMode.isImmediate() && future != null)
		{
			//wait for retriever to complete
			try
			{
				future.get();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}
		return future;
	}
}
