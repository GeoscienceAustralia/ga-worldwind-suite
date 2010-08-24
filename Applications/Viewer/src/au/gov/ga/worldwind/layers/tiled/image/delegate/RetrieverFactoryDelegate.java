package au.gov.ga.worldwind.layers.tiled.image.delegate;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;

public interface RetrieverFactoryDelegate extends Delegate
{
	Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor);
}
