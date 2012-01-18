package au.gov.ga.worldwind.common.layers.delegate.retriever;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IRetrieverFactoryDelegate;

/**
 * Implementation of {@link IRetrieverFactoryDelegate} which creates
 * {@link HTTPRetriever}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HttpRetrieverFactoryDelegate implements IRetrieverFactoryDelegate
{
	private final static String DEFINITION_STRING = "HttpRetriever";

	@Override
	public Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		return new HTTPRetriever(url, postProcessor);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new HttpRetrieverFactoryDelegate();
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}
}
