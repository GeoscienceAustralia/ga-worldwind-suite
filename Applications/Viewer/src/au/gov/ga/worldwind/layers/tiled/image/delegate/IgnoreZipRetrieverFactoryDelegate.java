package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;

public class IgnoreZipRetrieverFactoryDelegate implements RetrieverFactoryDelegate
{
	@Override
	public Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		return new IgnoreZipRetriever(url, postProcessor);
	}
}
