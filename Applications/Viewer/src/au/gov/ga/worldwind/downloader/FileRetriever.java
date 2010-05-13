package au.gov.ga.worldwind.downloader;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.URLRetriever;

import java.net.URL;

public class FileRetriever extends URLRetriever
{
	public FileRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
	}
}
