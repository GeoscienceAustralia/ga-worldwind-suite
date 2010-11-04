package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;

import org.w3c.dom.Element;

/**
 * Implementation of {@link RetrieverFactoryDelegate} which creates
 * {@link HTTPRetriever}s.
 * 
 * @author Michael de Hoog
 */
public class HttpRetrieverFactoryDelegate implements RetrieverFactoryDelegate
{
	private final static String DEFINITION_STRING = "HttpRetriever";

	@Override
	public Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		return new HTTPRetriever(url, postProcessor);
	}

	@Override
	public Delegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new HttpRetrieverFactoryDelegate();
		return null;
	}

	@Override
	public String toDefinition()
	{
		return DEFINITION_STRING;
	}
}
