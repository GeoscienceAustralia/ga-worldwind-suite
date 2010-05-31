package au.gov.ga.worldwind.downloader;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HandlerPostProcessor implements RetrievalPostProcessor
{
	private URL sourceURL;
	private Object lock = new Object();
	private final List<RetrievalHandler> handlers = new ArrayList<RetrievalHandler>();
	private RetrievalResult result = null;

	public HandlerPostProcessor(URL sourceURL, RetrievalHandler handler)
	{
		this.sourceURL = sourceURL;
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
		int size;
		synchronized (lock)
		{
			result = new ByteBufferRetrievalResult(sourceURL, buffer, false, notModified, error);
			size = handlers.size();
		}
		//iterate backwards through handlers, so that any added by addHandler will not be called
		for (int i = size - 1; i >= 0; i--)
			handlers.get(i).handle(result);
		return buffer;
	}
}
