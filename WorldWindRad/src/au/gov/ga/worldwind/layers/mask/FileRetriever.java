package au.gov.ga.worldwind.layers.mask;

import java.net.URL;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.URLRetriever;

public class FileRetriever extends URLRetriever
{
	public FileRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
	}
}
