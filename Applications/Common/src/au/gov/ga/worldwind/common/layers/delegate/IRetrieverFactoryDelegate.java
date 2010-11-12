package au.gov.ga.worldwind.common.layers.delegate;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;

/**
 * Instances of {@link IRetrieverFactoryDelegate} are used to create
 * {@link Retriever}s when downloading tiles.
 * 
 * @author Michael de Hoog
 */
public interface IRetrieverFactoryDelegate extends IDelegate
{
	/**
	 * Create a new {@link Retriever} for downloading from url.
	 * 
	 * @param url
	 *            URL to pass to {@link Retriever}'s constuctor
	 * @param postProcessor
	 *            Post processor to pass to {@link Retriever}'s constuctor
	 * @return New {@link Retriever}
	 */
	Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor);
}
