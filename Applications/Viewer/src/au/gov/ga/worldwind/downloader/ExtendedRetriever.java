package au.gov.ga.worldwind.downloader;

import gov.nasa.worldwind.retrieve.Retriever;

public interface ExtendedRetriever extends Retriever
{
	public Exception getError();

	public boolean isNotModified();
}
