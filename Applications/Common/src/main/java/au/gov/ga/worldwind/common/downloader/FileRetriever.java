package au.gov.ga.worldwind.common.downloader;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.URLRetriever;

import java.net.URL;

/**
 * Retriever which supports retrieval from a URL with the file:// protocol.
 * Required because HTTPRetiever (the standard subclass of URLRetriever) doesn't
 * support the file protocol. Simply a non-abstract subclass of the abstract
 * {@link URLRetriever} class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileRetriever extends URLRetriever
{
	public FileRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
	}
}
