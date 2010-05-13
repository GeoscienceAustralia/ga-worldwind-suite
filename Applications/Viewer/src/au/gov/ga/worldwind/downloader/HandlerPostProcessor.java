package au.gov.ga.worldwind.downloader;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HandlerPostProcessor implements RetrievalPostProcessor
{
	private Object lock = new Object();
	private final List<RetrievalHandler> handlers = new ArrayList<RetrievalHandler>();
	private RetrievalResult result = null;

	public HandlerPostProcessor(RetrievalHandler handler)
	{
		handlers.add(handler);
	}

	public void addHandler(RetrievalHandler handler)
	{
		synchronized (lock)
		{
			if (result == null)
				//result has not been calculated yet, so wait for run() to be called
				handlers.add(handler);
			else
				handler.handle(result);
		}
	}

	@Override
	public ByteBuffer run(Retriever retriever)
	{
		Exception error = null;
		boolean notModified = false;
		if (retriever instanceof ExtendedRetriever)
		{
			ExtendedRetriever er = (ExtendedRetriever) retriever;
			error = er.getError();
			notModified = er.isNotModified();
		}

		ByteBuffer buffer = retriever.getBuffer();
		synchronized (lock)
		{
			result = new ByteBufferRetrievalResult(buffer, false, notModified, error);
		}
		for (RetrievalHandler handler : handlers)
			handler.handle(result);
		return buffer;
	}
}
